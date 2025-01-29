package com.mentalfrostbyte.jello.managers;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.ClientMode;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2DOffset;
import com.mentalfrostbyte.jello.event.impl.game.render.EventRender2DCustom;
import com.mentalfrostbyte.jello.event.impl.player.EventPlayerTick;
import com.mentalfrostbyte.jello.managers.util.notifs.Notification;
import com.mentalfrostbyte.jello.managers.util.music.*;
import com.mentalfrostbyte.jello.util.YoutubeUtil;
import com.mentalfrostbyte.jello.util.*;
import com.mentalfrostbyte.jello.util.render.*;
import com.sapher.youtubedl.YoutubeDL;
import com.sapher.youtubedl.YoutubeDLException;
import com.sapher.youtubedl.YoutubeDLRequest;
import com.sapher.youtubedl.YoutubeDLResponse;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import com.tagtraum.jipes.math.FFTFactory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.mp4.MP4Container;
import net.sourceforge.jaad.mp4.api.AudioTrack;
import net.sourceforge.jaad.mp4.api.Frame;
import net.sourceforge.jaad.mp4.api.Movie;
import net.sourceforge.jaad.mp4.api.Track;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.util.BufferedImageUtil;
import team.sdhq.eventBus.EventBus;
import team.sdhq.eventBus.annotations.EventTarget;
import totalcross.json.JSONException;
import totalcross.json.JSONObject;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.sound.sampled.FloatControl.Type;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class MusicManager {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    public BufferedImage scaledThumbnail;
    public String songTitle = "";
    public List<double[]> visualizerData = new ArrayList<>();
    public ArrayList<Double> amplitudes = new ArrayList<>();
    public SourceDataLine sourceDataLine;
    private boolean playing = false;
    private MusicVideoManager videoManager;
    private int volume = 50;
    private long duration = -1L;
    private Texture notificationImage;
    private BufferedImage thumbnailImage;
    private Texture songThumbnail;
    private boolean isThumbnailProcessing = false;
    private transient volatile Thread audioThread = null;
    private int currentVideoIndex2;
    private long totalDuration = 0L;
    private int currentVideoIndex;
    private YoutubeVideoData currentVideo;
    private boolean spectrum = true;
    private AudioRepeatMode repeatMode = AudioRepeatMode.REPEAT;
    private boolean finished = false;
    private double field32168;
    private boolean field32169 = false;
    private double field32170 = 0.0;

    public void init() {
        EventBus.register(this);
        try {
            this.loadMusicSettings();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        if (!this.doesExecutableExist()) {
            this.setupDownloadThread();
        }

        this.finished = false;
    }

    public void saveMusicSettings() {
        JSONObject object = new JSONObject();
        object.put("volume", this.volume);
        object.put("spectrum", this.spectrum);
        object.put("repeat", this.repeatMode.type);
        Client.getInstance().getConfig().put("music", object);
    }

    private void loadMusicSettings() throws JSONException {
        if (Client.getInstance().getConfig().has("music")) {
            JSONObject object = Client.getInstance().getConfig().getJSONObject("music");
            if (object != null) {
                if (object.has("volume")) {
                    this.volume = Math.max(0, Math.min(100, object.getInt("volume")));
                }

                if (object.has("spectrum")) {
                    this.spectrum = object.getBoolean("spectrum");
                }

                if (object.has("repeat")) {
                    this.repeatMode = AudioRepeatMode.parseRepeat(object.getInt("repeat"));
                }
            }
        }
    }

    @EventTarget
    public void onRender(EventRender2DOffset event) {
        if (Client.getInstance().clientMode == ClientMode.JELLO) {
            if (this.playing && !this.visualizerData.isEmpty()) {
                double[] visualizers = this.visualizerData.get(0);
                if (this.amplitudes.isEmpty()) {
                    for (double amplitude : visualizers) {
                        if (this.amplitudes.size() < 1024) {
                            this.amplitudes.add(amplitude);
                        }
                    }
                }

                float fpsRatio = 60.0F / (float) MinecraftClient.getFps();

                for (int i = 0; i < visualizers.length; i++) {
                    double var7 = this.amplitudes.get(i) - visualizers[i];
                    boolean var9 = !(this.amplitudes.get(i) < Double.MAX_VALUE);
                    this.amplitudes.set(i, Math.min(2.256E7,
                            Math.max(0.0, this.amplitudes.get(i) - var7 * (double) Math.min(0.335F * fpsRatio, 1.0F))));
                    if (var9) {
                        this.amplitudes.set(i, 0.0);
                    }
                }
            }
        }
    }

    @EventTarget
    public void onRender(EventRender2DCustom render) {
        if (this.playing && !this.visualizerData.isEmpty() && this.spectrum) {
            if (this.notificationImage != null) {
                if (!this.amplitudes.isEmpty()) {
                    float maxWidth = 114.0F;
                    float width = (float) Math.ceil((float) mc.getMainWindow().getWidth() / maxWidth);

                    for (int i = 0; (float) i < maxWidth; i++) {
                        float alphaValue = 1.0F - (float) (i + 1) / maxWidth;
                        float heightRatio = (float) mc.getMainWindow().getHeight() / 1080.0F;
                        float height = ((float) (Math.sqrt(this.amplitudes.get(i)) / 12.0) - 5.0F) * heightRatio;
                        RenderUtil.drawRoundedRect2(
                                (float) i * width,
                                (float) mc.getMainWindow().getHeight() - height,
                                width,
                                height,
                                ColorUtils.applyAlpha(ClientColors.MID_GREY.getColor(), 0.2F * alphaValue));
                    }

                    RenderUtil.initStencilBuffer();

                    for (int i = 0; (float) i < maxWidth; i++) {
                        float heightRatio = (float) mc.getMainWindow().getHeight() / 1080.0F;
                        float height = ((float) (Math.sqrt(this.amplitudes.get(i)) / 12.0) - 5.0F) * heightRatio;
                        RenderUtil.drawRoundedRect2((float) i * width, (float) mc.getMainWindow().getHeight() - height,
                                width, height, ClientColors.LIGHT_GREYISH_BLUE.getColor());
                    }

                    RenderUtil.configureStencilTest(StencilMode.NOTEQUAL);
                    if (this.notificationImage != null && this.songThumbnail != null) {
                        RenderUtil.drawImage(0.0F, 0.0F, (float) mc.getMainWindow().getWidth(),
                                (float) mc.getMainWindow().getHeight(), this.songThumbnail, 0.4F);
                    }

                    RenderUtil.restorePreviousStencilBuffer();
                    double var9 = 0.0;
                    float var16 = 4750;

                    for (int i = 0; i < 3; i++) {
                        var9 = Math.max(var9, Math.sqrt(this.amplitudes.get(i)) - 1000.0);
                    }

                    float scale = 1.0F
                            + (float) Math.round((float) (var9 / (double) (var16 - 1000)) * 0.14F * 75.0F) / 75.0F;
                    GL11.glPushMatrix();
                    GL11.glTranslated(60.0, mc.getMainWindow().getHeight() - 55, 0.0);
                    GL11.glScalef(scale, scale, 0.0F);
                    GL11.glTranslated(-60.0, -(mc.getMainWindow().getHeight() - 55), 0.0);
                    RenderUtil.drawImage(10.0F, (float) (mc.getMainWindow().getHeight() - 110), 100.0F, 100.0F,
                            this.notificationImage);
                    RenderUtil.drawRoundedRect(10.0F, (float) (mc.getMainWindow().getHeight() - 110), 100.0F, 100.0F,
                            14.0F, 0.3F);
                    GL11.glPopMatrix();
                    String[] titleSplit = this.songTitle.split(" - ");
                    if (titleSplit.length <= 1) {
                        RenderUtil.drawString(
                                ResourceRegistry.JelloLightFont18_1,
                                130.0F,
                                (float) (mc.getMainWindow().getHeight() - 70),
                                titleSplit[0],
                                ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.5F));
                        RenderUtil.drawString(
                                ResourceRegistry.JelloLightFont18,
                                130.0F,
                                (float) (mc.getMainWindow().getHeight() - 70),
                                titleSplit[0],
                                ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.7F));
                    } else {
                        RenderUtil.drawString(
                                ResourceRegistry.JelloMediumFont20_1,
                                130.0F,
                                (float) (mc.getMainWindow().getHeight() - 81),
                                titleSplit[0],
                                ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.4F));
                        RenderUtil.drawString(
                                ResourceRegistry.JelloLightFont18_1,
                                130.0F,
                                (float) (mc.getMainWindow().getHeight() - 56),
                                titleSplit[1],
                                ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.5F));
                        RenderUtil.drawString(
                                ResourceRegistry.JelloLightFont18,
                                130.0F,
                                (float) (mc.getMainWindow().getHeight() - 56),
                                titleSplit[1],
                                ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.7F));
                        RenderUtil.drawString(
                                ResourceRegistry.JelloMediumFont20,
                                130.0F,
                                (float) (mc.getMainWindow().getHeight() - 81),
                                titleSplit[0],
                                ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.6F));
                    }
                }
            }
        }
    }

    private int ticks = 0;

    @EventTarget
    public void onTick(EventPlayerTick event) {
        if (!this.playing) {
            this.visualizerData.clear();
            this.amplitudes.clear();
        }

        if (this.playing) {
            ticks += 1;
        }

        if (ticks >= 1800 && getDuration() == 0L) {
            isThumbnailProcessing = false;
            playing = false;

            currentVideoIndex = 0;

            thumbnailImage = null;
            scaledThumbnail = null;
            currentVideo = null;

            songThumbnail.release();
            notificationImage.release();

            visualizerData.clear();
            amplitudes.clear();

            audioThread.interrupt();
            sourceDataLine.close();

            ticks = 0;
            duration = 0L;

            Client.getInstance().notificationManager.send(
                    new Notification("Failed to Play Song", "You bricked your music player :("));
        }

        try {
            if (this.isThumbnailProcessing && this.thumbnailImage != null && this.scaledThumbnail != null
                    && this.currentVideo == null && !mc.isGamePaused()) {
                if (this.songThumbnail != null) {
                    this.songThumbnail.release();
                }

                if (this.notificationImage != null) {
                    this.notificationImage.release();
                }

                this.songThumbnail = BufferedImageUtil.getTexture("picture", this.thumbnailImage);
                this.notificationImage = BufferedImageUtil.getTexture("picture", this.scaledThumbnail);
                Client.getInstance().notificationManager
                        .send(new Notification("Now Playing", this.songTitle, 7000, this.notificationImage));
                this.isThumbnailProcessing = false;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (!this.isThumbnailProcessing) {
            this.startProcessingVideoThumbnail();
        }
    }

    private void startProcessingVideoThumbnail() {
        if (this.currentVideo != null) {
            this.visualizerData.clear();
            new Thread(() -> this.processVideoThumbnail(this.currentVideo)).start();
        }
    }

    private void initializeAudioPlayback() {
        this.visualizerData.clear();
        if (this.videoManager != null) {
            while (this.audioThread != null && this.audioThread.isAlive()) {
                this.audioThread.interrupt();
            }

            this.audioThread = new Thread(
                    () -> {
                        byte[] pcmBufferData;
                        if (this.currentVideoIndex < 0
                                || this.currentVideoIndex >= this.videoManager.videoList.size()) {
                            this.currentVideoIndex = 0;
                        }

                        for (int i = this.currentVideoIndex; i < this.videoManager.videoList.size(); i++) {
                            URL videoURL = YoutubeUtil.getVideoStreamURL(this.videoManager.videoList.get(i).videoId);
                            Client.getInstance().getLogger().setThreadName(videoURL.toString());
                            this.currentVideoIndex2 = i;
                            this.currentVideo = this.videoManager.videoList.get(i);
                            this.visualizerData.clear();

                            while (!this.playing) {
                                this.visualizerData.clear();
                                if (Thread.interrupted()) {
                                    this.sourceDataLine.close();
                                    return;
                                }
                            }

                            try {
                                System.out.println(videoURL);
                                URL audioStreamURL = this.resolveAudioStream(videoURL);
                                Client.getInstance().getLogger().setThreadName(
                                        audioStreamURL == null ? "No stream" : audioStreamURL.toString());
                                if (audioStreamURL != null) {
                                    MusicStream stream = getMusicStream(audioStreamURL);
                                    MP4Container mp4Container = new MP4Container(stream);
                                    Movie movie = mp4Container.getMovie();
                                    List<Track> tracks = movie.getTracks();
                                    if (tracks.isEmpty()) {
                                        Client.getInstance().getLogger().setThreadName("No content");
                                    }

                                    AudioTrack audioTrack = (AudioTrack) movie.getTracks().get(1);
                                    AudioFormat audioFormat = new AudioFormat((float) audioTrack.getSampleRate(),
                                            audioTrack.getSampleSize(), audioTrack.getChannelCount(), true, true);
                                    this.sourceDataLine = AudioSystem.getSourceDataLine(audioFormat);
                                    this.sourceDataLine.open();
                                    this.sourceDataLine.start();
                                    this.duration = (long) movie.getDuration();
                                    if (this.duration > 1300L) {
                                        stream.close();
                                        Client.getInstance().notificationManager
                                                .send(new Notification("Now Playing", "Music is too long."));
                                    }

                                    Decoder decoder = new Decoder(audioTrack.getDecoderSpecificInfo());
                                    SampleBuffer sampleBuffer = new SampleBuffer();

                                    while (audioTrack.hasMoreFrames()) {
                                        while (!this.playing) {
                                            this.visualizerData.clear();
                                            if (Thread.interrupted()) {
                                                this.sourceDataLine.close();
                                                return;
                                            }
                                        }

                                        Frame nextFrame = audioTrack.readNextFrame();
                                        decoder.decodeFrame(nextFrame.getData(), sampleBuffer);
                                        pcmBufferData = sampleBuffer.getData();
                                        this.sourceDataLine.write((byte[]) pcmBufferData, 0,
                                                ((byte[]) pcmBufferData).length);
                                        float[] var29 = MathUtils.convertToPCMFloatArray(sampleBuffer.getData(),
                                                audioFormat);
                                        FFTFactory.JavaFFT var19 = new FFTFactory.JavaFFT(var29.length);
                                        float[][] var20 = var19.transform(var29);
                                        float[] var21 = var20[0];
                                        float[] var22 = var20[1];

                                        this.visualizerData.add(MathUtils.calculateAmplitudes(var21, var22));
                                        if (this.visualizerData.size() > 18) {
                                            this.visualizerData.remove(0);
                                        }

                                        this.adjustAudioVolume(this.sourceDataLine, this.volume);
                                        if (!Thread.interrupted()) {
                                            this.totalDuration = Math.round(audioTrack.getNextTimeStamp());
                                            this.field32170 = audioTrack.method23326();
                                            if (this.field32169) {
                                                audioTrack.seek(this.field32168);
                                                this.totalDuration = (long) this.field32168;
                                                this.field32169 = false;
                                            }
                                        }

                                        if (!audioTrack.hasMoreFrames()
                                                && (this.repeatMode == AudioRepeatMode.LOOP_CURRENT
                                                        || this.repeatMode == AudioRepeatMode.REPEAT
                                                                && this.videoManager.videoList.size() == 1)) {
                                            audioTrack.seek(0.0);
                                            this.totalDuration = 0L;
                                        }

                                        if (Thread.interrupted()) {
                                            this.sourceDataLine.close();
                                            return;
                                        }
                                    }

                                    this.sourceDataLine.close();
                                    stream.close();
                                }
                            } catch (IOException exception) {
                                if (exception.getMessage() != null && exception.getMessage().contains("403")) {
                                    System.out.println("installing");
                                    NetworkUtil.download(finished);
                                }
                            } catch (LineUnavailableException lineException) {
                                lineException.printStackTrace();
                            }

                            if (this.repeatMode == AudioRepeatMode.LOOP_CURRENT) {
                                i--;
                            } else if (this.repeatMode == AudioRepeatMode.REPEAT
                                    && i == this.videoManager.videoList.size() - 1) {
                                i = -1;
                            } else if (this.repeatMode == AudioRepeatMode.NO_REPEAT) {
                                return;
                            }

                            if (i >= this.videoManager.videoList.size()) {
                                i = 0;
                            }
                        }
                    });
            this.audioThread.start();
        }
    }

    private @NotNull MusicStream getMusicStream(URL audioStreamURL) throws IOException {
        URLConnection connection = audioStreamURL.openConnection();
        connection.setConnectTimeout(14000);
        connection.setReadTimeout(14000);
        connection.setUseCaches(true);
        connection.setDoOutput(true);
        connection.setRequestProperty("Connection", "Keep-Alive");
        InputStream inputStream = connection.getInputStream();
        MusicStream stream = new MusicStream(inputStream, new BasicAudioProcessor());
        return stream;
    }

    public void setRepeat(AudioRepeatMode repeat) {
        this.repeatMode = repeat;
        this.saveMusicSettings();
    }

    public AudioRepeatMode getRepeatMode() {
        return this.repeatMode;
    }

    public void processVideoThumbnail(YoutubeVideoData video) {
        try {
            this.isThumbnailProcessing = true;
            BufferedImage image = ImageIO.read(new URL(video.fullUrl));
            this.thumbnailImage = ImageUtil.applyBlur(image, 15);
            this.thumbnailImage = this.thumbnailImage
                    .getSubimage(0, (int) ((float) this.thumbnailImage.getHeight() * 0.75F),
                            this.thumbnailImage.getWidth(), (int) ((float) this.thumbnailImage.getHeight() * 0.2F));
            this.songTitle = video.title;
            if (image.getHeight() != image.getWidth()) {
                if (this.songTitle.contains("[NCS Release]")) {
                    this.scaledThumbnail = image.getSubimage(1, 3, 170, 170);
                } else {
                    this.scaledThumbnail = image.getSubimage(70, 0, 180, 180);
                }
            } else {
                this.scaledThumbnail = image;
            }

            this.currentVideo = null;
        } catch (IOException | NumberFormatException exception) {
            exception.printStackTrace();
        }
    }

    public void setPlaying(boolean playing) {
        if (!playing && this.sourceDataLine != null) {
            this.sourceDataLine.flush();
        }

        this.playing = playing;
    }

    public void setVolume(int volume) {
        this.volume = volume;
        this.saveMusicSettings();
    }

    public void setSpectrum(boolean spectrum) {
        this.spectrum = spectrum;
        this.saveMusicSettings();
    }

    public boolean isSpectrum() {
        return this.spectrum;
    }

    public int getVolume() {
        return this.volume;
    }

    public void playPreviousSong() {
        if (this.videoManager != null) {
            this.currentVideoIndex = this.currentVideoIndex2 - 1;
            this.totalDuration = 0L;
            this.field32170 = 0.0;
            this.initializeAudioPlayback();
        }
    }

    public void playNextSong() {
        if (this.videoManager != null) {
            this.currentVideoIndex = this.currentVideoIndex2 + 1;
            this.totalDuration = 0L;
            this.field32170 = 0.0;
            this.initializeAudioPlayback();
        }
    }

    public void playSong(MusicVideoManager manager, YoutubeVideoData video) {
        if (playing && totalDuration == 0L) {
            return;
        }

        if (video.videoId.equals("DArzZ3RvejU")) {
            Client.getInstance().notificationManager.send(
                    new Notification("Failed to Play Song", "Prevented brick :)"));
            return;
        }

        if (manager == null) {
            manager = new MusicVideoManager("temp", "temp", YoutubeContentType.PLAYLIST);
            manager.videoList.add(video);
        }

        this.videoManager = manager;
        this.playing = true;
        this.totalDuration = 0L;
        this.field32170 = 0.0;

        for (int i = 0; i < manager.videoList.size(); i++) {
            if (manager.videoList.get(i) == video) {
                this.currentVideoIndex = i;
            }
        }

        this.initializeAudioPlayback();
    }

    public boolean isPlayingSong() {
        return this.playing;
    }

    public long getDuration() {
        return this.totalDuration;
    }

    public double method24322() {
        return this.field32170;
    }

    public URL resolveAudioStream(URL songUrl) {
        YoutubeDLRequest request = new YoutubeDLRequest(songUrl.toString(), System.getProperty("user.home"));
        request.setOption("get-url");
        request.setOption("no-check-certificate");
        request.setOption("rm-cache-dir");
        request.setOption("retries", "10");
        request.setOption("format", "18");

        try {
            YoutubeDL.setExecutablePath(this.prepareYtDlpExecutable());
            YoutubeDLResponse response = YoutubeDL.execute(request);
            String out = response.getOut();
            return new URL(out);
        } catch (YoutubeDLException var9) {
            Client.getInstance().notificationManager.send(
                    new Notification("Failed to Play Song", "Check the logs for more details."));

            this.prepareYtDlpExecutable();

            return null;
        } catch (MalformedURLException exception) {
            MultiUtilities.sendChatMessage("URL Error: " + exception);
            exception.printStackTrace();

            Client.getInstance().notificationManager.send(
                    new Notification("Failed to Play Song", "Invalid URL encountered."));

            this.prepareYtDlpExecutable();

            return null;
        }
    }

    public String getSongTitle() {
        return this.songTitle;
    }

    public Texture getSongThumbnail() {
        return this.songThumbnail;
    }

    public Texture getNotificationImage() {
        return this.notificationImage;
    }

    public int getDurationInt() {
        return (int) this.duration;
    }

    private void adjustAudioVolume(SourceDataLine var1, int var2) {
        try {
            FloatControl var5 = (FloatControl) var1.getControl(Type.MASTER_GAIN);
            BooleanControl var6 = (BooleanControl) var1.getControl(BooleanControl.Type.MUTE);
            if (var2 == 0) {
                var6.setValue(true);
            } else {
                var6.setValue(false);
                var5.setValue((float) (Math.log((double) var2 / 100.0) / Math.log(10.0) * 20.0));
            }
        } catch (Exception ignored) {
        }
    }

    public void method24329(double var1) {
        this.field32168 = var1;
        this.totalDuration = (long) this.field32168;
        this.field32169 = true;
    }

    public boolean doesExecutableExist() {
        File file = new File(Client.getInstance().file + "/music/yt-dlp");
        if (Util.getOSType() == Util.OS.WINDOWS) {
            file = new File(Client.getInstance().file + "/music/yt-dlp.exe");
        } else if (Util.getOSType() == Util.OS.LINUX) {
            file = new File(Client.getInstance().file + "/music/yt-dlp_linux");
        } else if (Util.getOSType() == Util.OS.OSX) {
            file = new File(Client.getInstance().file + "/music/yt-dlp_macos");
        }

        return file.exists();
    }

    public void setupDownloadThread() {
        Client.getInstance().getLogger().setThreadName("Updating dependencies threaded");
        new Thread(() -> {
            boolean result = NetworkUtil.download(false);
            if (result) {
                this.finished = true;
            } else {
                System.out.println("Download failed.");
            }
        }).start();
    }

    public String prepareYtDlpExecutable() {
        String fileName = Util.getOSType() == Util.OS.WINDOWS ? "yt-dlp.exe"
                : Util.getOSType() == Util.OS.LINUX ? "yt-dlp_linux"
                        : "yt-dlp_macos";
        String ytDlpFile = Client.getInstance().file.getAbsolutePath() + "/music/" + fileName;
        if (Util.getOSType() != Util.OS.WINDOWS) {
            File var4 = new File(ytDlpFile);
            var4.setExecutable(true);
        }

        return ytDlpFile;
    }

    public boolean hasPython() {
        String[] commands;
        if (Util.getOSType() == Util.OS.WINDOWS) {
            commands = new String[] { "python --version || py --version || python3 --version" };
        } else {
            commands = new String[] { "python3 --version || python --version" };
        }

        for (String command : commands) {
            try {
                Process process = new ProcessBuilder(getCommandArray(command)).redirectErrorStream(true).start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String output = reader.readLine();
                while (output != null) {
                    if (
                    // Windows / command prompt
                    output.contains("is not recognized as an internal or external command")
                            // bash
                            || output.contains("command not found")) {
                        break; // Move to the next command
                    }
                    if (output.toLowerCase().contains("python")) {
                        String version = output.replaceAll("[^0-9.]", "");
                        if (isVersionAtLeast(version, "3.12")) {
                            return true;
                        }
                    }
                    output = reader.readLine();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private String[] getCommandArray(String command) {
        if (Util.getOSType() == Util.OS.WINDOWS) {
            return new String[] { "cmd", "/c", command };
        } else {
            return new String[] { "bash", "-c", command };
        }
    }

    private boolean isVersionAtLeast(String actual, String required) {
        String[] actualParts = actual.split("\\.");
        String[] requiredParts = required.split("\\.");
        int length = Math.max(actualParts.length, requiredParts.length);
        for (int i = 0; i < length; i++) {
            int actualPart = i < actualParts.length ? Integer.parseInt(actualParts[i]) : 0;
            int requiredPart = i < requiredParts.length ? Integer.parseInt(requiredParts[i]) : 0;
            if (actualPart > requiredPart)
                return true;
            if (actualPart < requiredPart)
                return false;
        }
        return true;
    }

    public boolean hasVCRedist() {
        if (Util.getOSType() != Util.OS.WINDOWS) {
            return true;
        } else {
            boolean hasRedist = false;

            try {
                hasRedist = Advapi32Util.registryGetIntValue(
                        WinReg.HKEY_LOCAL_MACHINE,
                        "SOFTWARE\\WOW6432Node\\Microsoft\\VisualStudio\\10.0\\VC\\VCRedist\\x86", "Installed") == 1;
            } catch (RuntimeException e) {
                e.printStackTrace();
            }

            try {
                hasRedist = hasRedist
                        || Advapi32Util.registryGetIntValue(WinReg.HKEY_LOCAL_MACHINE,
                                "SOFTWARE\\Microsoft\\VisualStudio\\10.0\\VC\\VCRedist\\x86", "Installed") == 1;
            } catch (RuntimeException e) {
                e.printStackTrace();
            }

            return hasRedist;
        }
    }
}
