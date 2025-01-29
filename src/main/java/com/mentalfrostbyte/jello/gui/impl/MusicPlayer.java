package com.mentalfrostbyte.jello.gui.impl;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.base.Animation;
import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.base.Direction;
import com.mentalfrostbyte.jello.gui.base.QuadraticEasing;
import com.mentalfrostbyte.jello.gui.unmapped.*;
import com.mentalfrostbyte.jello.managers.MusicManager;
import com.mentalfrostbyte.jello.managers.util.music.MusicPlayerInstance;
import com.mentalfrostbyte.jello.managers.MusicVideoManager;
import com.mentalfrostbyte.jello.managers.util.music.YoutubeContentType;
import com.mentalfrostbyte.jello.managers.util.music.YoutubeVideoData;
import com.mentalfrostbyte.jello.util.ClientColors;
import com.mentalfrostbyte.jello.util.ColorHelper;
import com.mentalfrostbyte.jello.util.ResourceRegistry;
import com.mentalfrostbyte.jello.util.YoutubeUtil;
import com.mentalfrostbyte.jello.util.render.*;
import com.mentalfrostbyte.jello.util.unmapped.Class2218;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.util.BufferedImageUtil;

import java.io.IOException;
import java.util.*;

public class MusicPlayer extends AnimatedIconPanelWrap {
    private int width = 250;
    private int height = 40;
    private int field20847 = 64;
    private int field20848 = 94;
    private String field20849 = "Music Player";
    private MusicTabs musicTabs;
    private MusicTabs field20852;
    private CustomGuiScreen musicControls;
    private MusicManager musicManager = Client.getInstance().musicManager;
    public static Map<String, MusicVideoManager> videoMap = new LinkedHashMap<>();
    private ButtonPanel play;
    private ButtonPanel pause;
    private ButtonPanel forwards;
    private ButtonPanel backwards;
    private VolumeSlider volumeSlider;
    private int field20863;
    private Texture texture;
    private CustomGuiScreen field20865;
    public SearchBoxButton searchBox;
    public Class4359 field20867;
    public static List<MusicVideoManager> videos = new ArrayList<>();
    public static long time = 0L;
    public float field20871 = 0.0F;
    public float field20872 = 0.0F;
    private final Animation field20873 = new Animation(80, 150, Direction.BACKWARDS);
    public boolean field20874 = false;

    public JelloClickGUI parent;

    public MusicPlayer(JelloClickGUI parent, String var2) {
        super(parent, var2, 875, 55, 800, 600, false);
        this.parent = parent;

        if (videos.size() != 8) {
            videos.clear();
            videos.add(new MusicVideoManager("Trap Nation", "PLC1og_v3eb4hrv4wsqG1G5dsNZh9bIscJ", YoutubeContentType.PLAYLIST));
            videos.add(new MusicVideoManager("Chill Nation", "PL3EfCK9aCbkptFjtgWYJ8wiXgJQw5k3M3", YoutubeContentType.PLAYLIST));
            videos.add(new MusicVideoManager("VEVO", "PL9tY0BWXOZFu8MzzbNVtUvHs0cQ_gZ03m", YoutubeContentType.PLAYLIST));
            videos.add(new MusicVideoManager("Rap Nation", "PLayVKgoNNljOZifkJNtvwfmrmh2OglYzx", YoutubeContentType.PLAYLIST));
            videos.add(new MusicVideoManager("MrSuicideSheep", "PLyqoPTKp-zlrI_PEqytQ7J9FgPhptcC64", YoutubeContentType.PLAYLIST));
            videos.add(new MusicVideoManager("Trap City", "PLU_bQfSFrM2PemIeyVUSjZjJhm6G7auOY", YoutubeContentType.PLAYLIST));
            videos.add(new MusicVideoManager("CloudKid", "PLejelFTZDTZM1yOroUyveJkjE7IY9Zj73", YoutubeContentType.PLAYLIST));
            videos.add(new MusicVideoManager("NCS", "PLRBp0Fe2Gpgm_u2w2a2isHw29SugZ34cD", YoutubeContentType.PLAYLIST));
        }

        time = System.nanoTime();
        this.setWidthA(800);
        this.setHeightA(600);
        this.setXA(Math.abs(this.getXA()));
        this.setYA(Math.abs(this.getYA()));
        this.addToList(this.musicTabs = new MusicTabs(this, "musictabs", 0, this.field20847 + 14, this.width, this.getHeightA() - 64 - this.field20848));
        this.addToList(
                this.musicControls = new MusicTabs(
                        this, "musiccontrols", this.width, this.getHeightA() - this.field20848, this.getWidthA() - this.width, this.field20848
                )
        );
        this.addToList(this.field20865 = new CustomGuiScreen(this, "reShowView", 0, 0, 1, this.getHeightA()));
        Class4265 var5;
        this.addToList(var5 = new Class4265(this, "spectrumButton", 15, this.heightA - 140, 40, 40, this.musicManager.isSpectrum()));
        var5.method13292(true);
        var5.doThis((var1x, var2x) -> {
            this.musicManager.setSpectrum(!this.musicManager.isSpectrum());
            ((Class4265) var1x).method13099(this.musicManager.isSpectrum());
        });
        this.musicTabs.setListening(false);
        var5.setListening(false);
        this.musicControls.setListening(false);
        this.field20865.setListening(false);
        ColorHelper color = new ColorHelper(1250067, -15329770).method19410(ClientColors.LIGHT_GREYISH_BLUE.getColor()).method19414(Class2218.field14492);
        List<Thread> threads = new ArrayList<>();
        MusicPlayer player = this;

        for (MusicVideoManager video : videos) {
            threads.add(new Thread(() -> {
                if (!videoMap.containsKey(video.videoId) && !video.isUpdated) {
                    video.isUpdated = true;
                    video.refreshVideoList();

                    videoMap.put(video.videoId, video);
                }

                this.runThisOnDimensionUpdate(new MusicPlayerInstance(this, video, color, player));
            }));
            threads.get(threads.size() - 1).start();
        }

        int var15 = (this.getWidthA() - this.width - 38) / 2;
        this.musicControls
                .addToList(
                        this.play = new PNGIconButton(
                                this.musicControls, "play", var15, 27, 38, 38, Resources.playPNG, new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()), null
                        )
                );
        this.musicControls
                .addToList(
                        this.pause = new PNGIconButton(
                                this.musicControls, "pause", var15, 27, 38, 38, Resources.pausePNG, new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()), null
                        )
                );
        this.musicControls
                .addToList(
                        this.forwards = new PNGIconButton(
                                this.musicControls, "forwards", var15 + 114, 23, 46, 46, Resources.forwardsPNG, new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()), null
                        )
                );
        this.musicControls
                .addToList(
                        this.backwards = new PNGIconButton(
                                this.musicControls, "backwards", var15 - 114, 23, 46, 46, Resources.backwardsPNG, new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()), null
                        )
                );
        this.musicControls.addToList(this.volumeSlider = new VolumeSlider(this.musicControls, "volume", this.getWidthA() - this.width - 19, 14, 4, 40));
        PNGButtonChanging repeat;
        this.musicControls.addToList(repeat = new PNGButtonChanging(this.musicControls, "repeat", 14, 34, 27, 20, this.musicManager.getRepeatMode()));
        repeat.onPress(var2x -> this.musicManager.setRepeat(repeat.getRepeatMode()));
        this.addToList(this.field20867 = new Class4359(this, "progress", this.width, this.getHeightA() - 5, this.getWidthA() - this.width, 5));
        this.field20867.method13292(true);
        this.field20867.setListening(false);
        this.field20865.method13292(true);
        this.field20865.method13247((var1x, var2x) -> {
            this.field20874 = true;
            this.field20871 = (float) this.getXA();
            this.field20872 = (float) this.getYA();
        });
        this.pause.setEnabled(false);
        this.play.setEnabled(false);
        this.play.doThis((var1x, var2x) -> this.musicManager.setPlaying(true));
        this.pause.doThis((var1x, var2x) -> this.musicManager.setPlaying(false));
        this.forwards.doThis((var1x, var2x) -> this.musicManager.playNextSong());
        this.backwards.doThis((var1x, var2x) -> this.musicManager.playPreviousSong());
        this.volumeSlider.method13709(var1x -> this.musicManager.setVolume((int) ((1.0F - this.volumeSlider.getVolume()) * 100.0F)));
        this.volumeSlider.setVolume(1.0F - (float) this.musicManager.getVolume() / 100.0F);
        this.addToList(
                this.searchBox = new SearchBoxButton(
                        this, "search", this.width, 0, this.getWidthA() - this.width, this.getHeightA() - this.field20848, "Search..."
                )
        );
        this.searchBox.setEnabled(true);
        this.searchBox.setListening(false);
    }

    private void method13189(MusicTabs var1) {
        if (this.field20852 != null) {
            this.field20852.setEnabled(false);
        }

        var1.setEnabled(true);
        this.field20849 = var1.getTypedText();
        this.field20852 = var1;
        this.searchBox.setEnabled(false);
        this.field20852.field21207 = 65;
    }

    private void playSong(MusicVideoManager manager, YoutubeVideoData video) {
        this.musicManager.playSong(manager, video);
    }

    @Override
    public void updatePanelDimensions(int newHeight, int newWidth) {
        long var5 = System.nanoTime() - time;
        float var7 = Math.min(10.0F, Math.max(0.0F, (float) var5 / 1.810361E7F));
        time = System.nanoTime();
        super.updatePanelDimensions(newHeight, newWidth);
        if (this.parent != null) {
            if (!this.method13216()) {
                if ((this.field20909 || this.field20874) && !this.method13214() && !this.method13216()) {
                    this.field20874 = true;
                    int var11 = this.parent.getWidthA() - 20 - this.getWidthA();
                    int var13 = (this.parent.getHeightA() - this.getHeightA()) / 2;
                    this.field20871 = Math.max(this.field20871 - (this.field20871 - (float) var11) * 0.25F * var7, (float) var11);
                    if (!(this.field20872 - (float) var13 > 0.0F)) {
                        Math.min(this.field20872 = this.field20872 - (this.field20872 - (float) var13) * 0.2F * var7, (float) var13);
                    } else {
                        Math.max(this.field20872 = this.field20872 - (this.field20872 - (float) var13) * 0.2F * var7, (float) var13);
                    }

                    if (!(this.field20871 - (float) var11 < 0.0F)) {
                        if (this.field20871 - (float) var11 - (float) this.getWidthA() > 0.0F) {
                            this.field20871 = (float) var11;
                        }
                    } else {
                        this.field20871 = (float) var11;
                    }

                    this.setXA((int) this.field20871);
                    this.setYA((int) this.field20872);
                    if (Math.abs(this.field20871 - (float) var11) < 2.0F && Math.abs(this.field20872 - (float) var13) < 2.0F) {
                        this.method13215(true);
                        this.field20874 = false;
                    }
                } else if (this.getXA() + this.getWidthA() > this.parent.getWidthA() || this.getXA() < 0 || this.getYA() < 0) {
                    if (this.field20871 == 0.0F || this.field20872 == 0.0F) {
                        this.field20871 = (float) this.getXA();
                        this.field20872 = (float) this.getYA();
                    }

                    int var8 = this.parent.getWidthA() - 40;
                    int var9 = (this.parent.getHeightA() - this.getHeightA()) / 2;
                    this.field20871 = Math.min(this.field20871 - (this.field20871 - (float) var8) * 0.25F * var7, (float) var8);
                    if (!(this.field20872 - (float) var9 > 0.0F)) {
                        Math.min(this.field20872 = this.field20872 - (this.field20872 - (float) var9) * 0.2F * var7, (float) var9);
                    } else {
                        Math.max(this.field20872 = this.field20872 - (this.field20872 - (float) var9) * 0.2F * var7, (float) var9);
                    }

                    if (!(this.field20871 - (float) var8 > 0.0F)) {
                        if (this.field20871 - (float) var8 + (float) this.getWidthA() < 0.0F) {
                            this.field20871 = (float) var8;
                        }
                    } else {
                        this.field20871 = (float) var8;
                    }

                    if (Math.abs(this.field20871 - (float) var8) < 2.0F && Math.abs(this.field20872 - (float) var9) < 2.0F) {
                        this.field20871 = (float) this.getXA();
                        this.field20872 = (float) this.getYA();
                    }

                    this.setXA((int) this.field20871);
                    this.setYA((int) this.field20872);
                    this.method13215(false);
                    this.method13217(false);
                }
            } else {
                int var12 = newHeight - this.sizeWidthThingy - (this.parent == null ? 0 : this.parent.method13271());
                int var14 = 200;
                if (var12 + this.getWidthA() > this.parent.getWidthA() + var14 && newHeight - this.field20878 > 70) {
                    int var15 = var12 - this.getXA() - var14;
                    this.setXA((int) ((float) this.getXA() + (float) var15 * 0.5F));
                    this.field20871 = (float) this.getXA();
                    this.field20872 = (float) this.getYA();
                }
            }
        }
    }

    @Override
    public void draw(float partialTicks) {
        super.method13224();
        super.method13225();
        this.field20865.setWidthA(this.getXA() + this.getWidthA() <= this.parent.getWidthA() ? 0 : 41);
        this.field20873
                .changeDirection(this.getXA() + this.getWidthA() > this.parent.getWidthA() && !this.field20874 ? Direction.FORWARDS : Direction.BACKWARDS);
        partialTicks *= 0.5F + (1.0F - this.field20873.calcPercent()) * 0.5F;
        if (this.musicManager.isPlayingSong()) {
            this.play.setEnabled(false);
            this.pause.setEnabled(true);
        } else {
            this.play.setEnabled(true);
            this.pause.setEnabled(false);
        }

        RenderUtil.drawRoundedRect(
                (float) (this.getXA() + this.width),
                (float) this.getYA(),
                (float) (this.getXA() + this.getWidthA()),
                (float) (this.getYA() + this.getHeightA() - this.field20848),
                ColorUtils.applyAlpha(-14277082, partialTicks * 0.8F)
        );
        RenderUtil.drawRoundedRect(
                (float) this.getXA(),
                (float) this.getYA(),
                (float) (this.getXA() + this.width),
                (float) (this.getYA() + this.getHeightA() - this.field20848),
                ColorUtils.applyAlpha(-16777216, partialTicks * 0.95F)
        );
        this.method13193(partialTicks);
        this.method13194(partialTicks);
        this.method13192(partialTicks);
        float var4 = 55;
        RenderUtil.drawString(
                ResourceRegistry.JelloLightFont40,
                var4 + this.getXA(),
                (float) (this.getYA() + 20),
                "Jello",
                ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
        );
        RenderUtil.drawString(
                ResourceRegistry.JelloLightFont20,
                var4 + this.getXA() + 80,
                (float) (this.getYA() + 40),
                "music",
                ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
        );
        RenderUtil.drawRoundedRect((float) this.getXA(), (float) this.getYA(), (float) this.getWidthA(), (float) this.getHeightA(), 14.0F, partialTicks);
        super.draw(partialTicks);
        if (this.field20852 != null) {
            this.method13196(partialTicks);
        }
    }

    private void method13192(float var1) {
        int var4 = (int) this.musicManager.getDuration();
        int var5 = this.musicManager.getDurationInt();
        RenderUtil.drawString(
                ResourceRegistry.JelloLightFont14,
                (float) (this.getXA() + this.width + 14),
                (float) (this.getYA() + this.getHeightA() - 10) - 22.0F * var1,
                YoutubeUtil.method34955(var4),
                ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var1 * var1)
        );
        RenderUtil.drawString(
                ResourceRegistry.JelloLightFont14,
                (float) (this.getXA() + this.getWidthA() - 14 - ResourceRegistry.JelloLightFont14.getWidth(YoutubeUtil.method34955(var5))),
                (float) (this.getYA() + this.getHeightA() - 10) - 22.0F * var1,
                YoutubeUtil.method34955(var5),
                ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var1 * var1)
        );
    }

    private void method13193(float var1) {
        Texture var4 = this.musicManager.getNotificationImage();
        Texture var5 = this.musicManager.getSongThumbnail();
        if (var4 != null && var5 != null) {
            RenderUtil.drawImage(
                    (float) this.getXA(),
                    (float) (this.getYA() + this.getHeightA() - this.field20848),
                    (float) this.getWidthA(),
                    (float) this.field20848,
                    var5,
                    ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var1 * var1)
            );
            RenderUtil.drawRoundedRect(
                    (float) this.getXA(),
                    (float) (this.getYA() + this.getHeightA() - this.field20848),
                    (float) (this.getXA() + this.getWidthA()),
                    (float) (this.getYA() + this.getHeightA() - 5),
                    ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.43F * var1)
            );
            RenderUtil.drawRoundedRect(
                    (float) this.getXA(),
                    (float) (this.getYA() + this.getHeightA() - 5),
                    (float) (this.getXA() + this.width),
                    (float) (this.getYA() + this.getHeightA()),
                    ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.43F * var1)
            );
            RenderUtil.drawImage(
                    (float) (this.getXA() + (this.width - 114) / 2),
                    (float) (this.getYA() + this.getHeightA() - 170),
                    114.0F,
                    114.0F,
                    var4,
                    ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var1)
            );
            RenderUtil.drawRoundedRect(
                    (float) (this.getXA() + (this.width - 114) / 2), (float) (this.getYA() + this.getHeightA() - 170), 114.0F, 114.0F, 14.0F, var1
            );
        } else {
            RenderUtil.drawImage(
                    (float) this.getXA(),
                    (float) (this.getYA() + this.getHeightA() - this.field20848),
                    (float) this.getWidthA(),
                    (float) this.field20848,
                    Resources.bgPNG,
                    ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var1 * var1)
            );
            RenderUtil.drawRoundedRect(
                    (float) this.getXA(),
                    (float) (this.getYA() + this.getHeightA() - this.field20848),
                    (float) (this.getXA() + this.getWidthA()),
                    (float) (this.getYA() + this.getHeightA() - 5),
                    ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.43F * var1)
            );
            RenderUtil.drawRoundedRect(
                    (float) this.getXA(),
                    (float) (this.getYA() + this.getHeightA() - 5),
                    (float) (this.getXA() + this.width),
                    (float) (this.getYA() + this.getHeightA()),
                    ColorUtils.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.43F * var1)
            );
            RenderUtil.drawImage(
                    (float) (this.getXA() + (this.width - 114) / 2),
                    (float) (this.getYA() + this.getHeightA() - 170),
                    114.0F,
                    114.0F,
                    Resources.artworkPNG,
                    ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var1)
            );
            RenderUtil.drawRoundedRect(
                    (float) (this.getXA() + (this.width - 114) / 2), (float) (this.getYA() + this.getHeightA() - 170), 114.0F, 114.0F, 14.0F, var1
            );
        }
    }

    private void method13194(float var1) {
        if (this.musicManager.getSongTitle() != null) {
            String[] var4 = this.musicManager.getSongTitle().split(" - ");
            int var5 = 30;
            if (var4.length <= 1) {
                this.drawString(var1, !var4[0].isEmpty() ? var4[0] : "Jello Music", this.width - var5 * 2, 12, 0);
            } else {
                this.drawString(var1, var4[1], this.width - var5 * 2, 0, 0);
                this.drawString(var1, var4[0], this.width - var5 * 2, 20, -1000);
            }
        }
    }

    private void drawString(float var1, String text, int var3, int var4, int var5) {
        Date var8 = new Date();
        float var9 = (float) ((var8.getTime() + (long) var5) % 8500L) / 8500.0F;
        if (!(var9 < 0.4F)) {
            var9 -= 0.4F;
            var9 = (float) ((double) var9 * 1.6666666666666667);
        } else {
            var9 = 0.0F;
        }

        var9 = QuadraticEasing.easeInOutQuad(var9, 0.0F, 1.0F, 1.0F);
        int var10 = ResourceRegistry.JelloLightFont14.getWidth(text);
        int var11 = Math.min(var3, var10);
        int var12 = ResourceRegistry.JelloLightFont14.getHeight();
        int var13 = this.getXA() + (this.width - var11) / 2;
        int var14 = this.getYA() + this.getHeightA() - 50 + var4;
        int var15 = Math.max(0, var10 - var11) * 2;
        if (var10 <= var3) {
            var9 = 0.0F;
        }

        RenderUtil.drawPortalBackground(var13, var14, var13 + var11, var14 + var12, true);
        RenderUtil.drawString(
                ResourceRegistry.JelloLightFont14,
                (float) var13 - (float) var10 * var9 - 50.0F * var9,
                (float) var14,
                text,
                ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var1 * var1 * Math.min(1.0F, Math.max(0.0F, 1.0F - var9 * 0.75F)))
        );
        if (var9 > 0.0F) {
            RenderUtil.drawString(
                    ResourceRegistry.JelloLightFont14,
                    (float) var13 - (float) var10 * var9 + (float) var10,
                    (float) var14,
                    text,
                    ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var1 * var1)
            );
        }

        RenderUtil.endScissor();
    }

    private void method13196(float var1) {
        this.field20852.method13292(false);
        if (this.field20863 != this.field20852.method13513()) {
            try {
                if (this.texture != null) {
                    this.texture.release();
                }

                this.texture = BufferedImageUtil.getTexture(
                        "blur",
                        ImageUtil.method35037(this.getXA() + this.width, this.getYA(), this.getWidthA() - this.width, this.field20847, 10, 10)
                );
            } catch (IOException var5) {
                var5.printStackTrace();
            }
        }

        float var4 = this.field20863 < 50 ? (float) this.field20863 / 50.0F : 1.0F;
        if (this.texture != null) {
            RenderUtil.drawTexture(
                    (float) this.width,
                    0.0F,
                    (float) (this.getWidthA() - this.width),
                    (float) this.field20847,
                    this.texture,
                    ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var4 * var1)
            );
        }

        RenderUtil.drawRoundedRect(
                (float) this.width,
                0.0F,
                (float) this.getWidthA(),
                (float) this.field20847,
                ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var4 * var1 * 0.2F)
        );
        RenderUtil.drawString(
                ResourceRegistry.JelloLightFont25,
                (float) ((this.getWidthA() - ResourceRegistry.JelloLightFont25.getWidth(this.field20849) + this.width) / 2),
                16.0F + (1.0F - var4) * 14.0F,
                this.field20849,
                ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var4)
        );
        RenderUtil.drawString(
                ResourceRegistry.JelloMediumFont25,
                (float) ((this.getWidthA() - ResourceRegistry.JelloMediumFont25.getWidth(this.field20849) + this.width) / 2),
                16.0F + (1.0F - var4) * 14.0F,
                this.field20849,
                ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 1.0F - var4)
        );
        RenderUtil.drawImage(
                (float) this.width,
                (float) this.field20847,
                (float) (this.getWidthA() - this.width),
                20.0F,
                Resources.shadowBottomPNG,
                ColorUtils.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var4 * var1 * 0.5F)
        );
        this.field20863 = this.field20852.method13513();
    }

    public static MusicTabs getTabs(MusicPlayer player) {
        return player.musicTabs;
    }

    public static int getHeight(MusicPlayer player) {
        return player.height;
    }

    public static int getWidth(MusicPlayer player) {
        return player.width;
    }

    public static int method13209(MusicPlayer player) {
        return player.field20848;
    }

    public static void method13210(MusicPlayer player, MusicTabs tabs) {
        player.method13189(tabs);
    }

    public static void playSong(MusicPlayer player, MusicVideoManager videoManager, YoutubeVideoData video) {
        player.playSong(videoManager, video);
    }
}
