package com.mentalfrostbyte.jello.gui.impl.jello.ingame.clickgui.musicplayer;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.base.animations.Animation;
import com.mentalfrostbyte.jello.gui.base.elements.impl.button.Button;
import com.mentalfrostbyte.jello.gui.base.elements.impl.button.types.ChangingButton;
import com.mentalfrostbyte.jello.gui.base.elements.impl.button.types.SpectrumButton;
import com.mentalfrostbyte.jello.gui.base.elements.impl.image.types.SmallImage;
import com.mentalfrostbyte.jello.gui.combined.AnimatedIconPanel;
import com.mentalfrostbyte.jello.gui.combined.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.impl.jello.ingame.clickgui.musicplayer.elements.*;
import com.mentalfrostbyte.jello.util.client.render.Resources;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil;
import com.mentalfrostbyte.jello.util.game.render.RenderUtil2;
import com.mentalfrostbyte.jello.util.system.math.smoothing.QuadraticEasing;
import com.mentalfrostbyte.jello.gui.impl.jello.buttons.ScrollableContentPanel;
import com.mentalfrostbyte.jello.gui.impl.jello.ingame.clickgui.ClickGuiScreen;
import com.mentalfrostbyte.jello.managers.MusicManager;
import com.mentalfrostbyte.jello.managers.util.Thumbnails;
import com.mentalfrostbyte.jello.util.client.network.youtube.YoutubeContentType;
import com.mentalfrostbyte.jello.util.client.network.youtube.YoutubeVideoData;
import com.mentalfrostbyte.jello.util.client.render.theme.ClientColors;
import com.mentalfrostbyte.jello.util.client.render.theme.ColorHelper;
import com.mentalfrostbyte.jello.util.client.render.ResourceRegistry;
import com.mentalfrostbyte.jello.util.client.network.youtube.YoutubeUtil;
import com.mentalfrostbyte.jello.util.client.render.FontSizeAdjust;
import com.mentalfrostbyte.jello.util.system.network.ImageUtil;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.util.BufferedImageUtil;

import java.io.IOException;
import java.util.*;

public class MusicPlayer extends AnimatedIconPanel {
    private final int width = 250;
    private final int height = 40;
    private final int field20847 = 64;
    private final int field20848 = 94;
    private String field20849 = "Music Player";
    private final ScrollableContentPanel musicTabs;
    private ScrollableContentPanel field20852;
    private final CustomGuiScreen musicControls;
    private final MusicManager musicManager = Client.getInstance().musicManager;
    public static Map<String, Thumbnails> videoMap = new LinkedHashMap<>();
    private final Button play;
    private final Button pause;
    private final Button forwards;
    private final Button backwards;
    private final VolumeSlider volumeSlider;
    private int field20863;
    private Texture texture;
    private final CustomGuiScreen field20865;
    public SearchBox searchBox;
    public ProgressBar field20867;
    public static List<Thumbnails> videos = new ArrayList<>();
    public static long time = 0L;
    public float field20871 = 0.0F;
    public float field20872 = 0.0F;
    private final Animation field20873 = new Animation(80, 150, Animation.Direction.BACKWARDS);
    public boolean field20874 = false;

    public ClickGuiScreen parent;

    public MusicPlayer(ClickGuiScreen parent, String var2) {
        super(parent, var2, 875, 55, 800, 600, false);
        this.parent = parent;

        if (videos.size() != 8) {
            videos.clear();
            videos.add(new Thumbnails("Trap Nation", "PLC1og_v3eb4hrv4wsqG1G5dsNZh9bIscJ", YoutubeContentType.PLAYLIST));
            videos.add(new Thumbnails("Chill Nation", "PL3EfCK9aCbkptFjtgWYJ8wiXgJQw5k3M3", YoutubeContentType.PLAYLIST));
            videos.add(new Thumbnails("VEVO", "PL9tY0BWXOZFu8MzzbNVtUvHs0cQ_gZ03m", YoutubeContentType.PLAYLIST));
            videos.add(new Thumbnails("Rap Nation", "PLayVKgoNNljOZifkJNtvwfmrmh2OglYzx", YoutubeContentType.PLAYLIST));
            videos.add(new Thumbnails("MrSuicideSheep", "PLyqoPTKp-zlrI_PEqytQ7J9FgPhptcC64", YoutubeContentType.PLAYLIST));
            videos.add(new Thumbnails("Trap City", "PLU_bQfSFrM2PemIeyVUSjZjJhm6G7auOY", YoutubeContentType.PLAYLIST));
            videos.add(new Thumbnails("CloudKid", "PLejelFTZDTZM1yOroUyveJkjE7IY9Zj73", YoutubeContentType.PLAYLIST));
            videos.add(new Thumbnails("NCS", "PLRBp0Fe2Gpgm_u2w2a2isHw29SugZ34cD", YoutubeContentType.PLAYLIST));
        }

        time = System.nanoTime();
        this.setWidthA(800);
        this.setHeightA(600);
        this.setXA(Math.abs(this.getXA()));
        this.setYA(Math.abs(this.getYA()));
        this.addToList(this.musicTabs = new ScrollableContentPanel(this, "musictabs", 0, this.field20847 + 14, this.width, this.getHeightA() - 64 - this.field20848));
        this.addToList(
                this.musicControls = new ScrollableContentPanel(
                        this, "musiccontrols", this.width, this.getHeightA() - this.field20848, this.getWidthA() - this.width, this.field20848
                )
        );
        this.addToList(this.field20865 = new CustomGuiScreen(this, "reShowView", 0, 0, 1, this.getHeightA()));
        SpectrumButton var5;
        this.addToList(var5 = new SpectrumButton(this, "spectrumButton", 15, this.heightA - 140, 40, 40, this.musicManager.isSpectrum()));
        var5.setReAddChildren(true);
        var5.onClick((var1x, var2x) -> {
            this.musicManager.setSpectrum(!this.musicManager.isSpectrum());
            ((SpectrumButton) var1x).method13099(this.musicManager.isSpectrum());
        });
        this.musicTabs.setListening(false);
        var5.setListening(false);
        this.musicControls.setListening(false);
        this.field20865.setListening(false);
        ColorHelper color = new ColorHelper(1250067, -15329770).setTextColor(ClientColors.LIGHT_GREYISH_BLUE.getColor()).method19414(FontSizeAdjust.NEGATE_AND_DIVIDE_BY_2);
        List<Thread> threads = new ArrayList<>();
        MusicPlayer player = this;

        for (Thumbnails video : videos) {
            threads.add(new Thread(() -> {
                if (!videoMap.containsKey(video.videoId) && !video.isUpdated) {
                    video.isUpdated = true;
                    video.refreshVideoList();

                    videoMap.put(video.videoId, video);
                }

                this.runThisOnDimensionUpdate(new MusicInitializer(this, video, color, player));
            }));
            threads.get(threads.size() - 1).start();
        }

        int var15 = (this.getWidthA() - this.width - 38) / 2;
        this.musicControls
                .addToList(
                        this.play = new SmallImage(
                                this.musicControls, "play", var15, 27, 38, 38, Resources.playPNG, new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()), null
                        )
                );
        this.musicControls
                .addToList(
                        this.pause = new SmallImage(
                                this.musicControls, "pause", var15, 27, 38, 38, Resources.pausePNG, new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()), null
                        )
                );
        this.musicControls
                .addToList(
                        this.forwards = new SmallImage(
                                this.musicControls, "forwards", var15 + 114, 23, 46, 46, Resources.forwardsPNG, new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()), null
                        )
                );
        this.musicControls
                .addToList(
                        this.backwards = new SmallImage(
                                this.musicControls, "backwards", var15 - 114, 23, 46, 46, Resources.backwardsPNG, new ColorHelper(ClientColors.LIGHT_GREYISH_BLUE.getColor()), null
                        )
                );
        this.musicControls.addToList(this.volumeSlider = new VolumeSlider(this.musicControls, "volume", this.getWidthA() - this.width - 19, 14, 4, 40));
        ChangingButton repeat;
        this.musicControls.addToList(repeat = new ChangingButton(this.musicControls, "repeat", 14, 34, 27, 20, this.musicManager.getRepeat()));
        repeat.onPress(var2x -> this.musicManager.setRepeat(repeat.getRepeatMode()));
        this.addToList(this.field20867 = new ProgressBar(this, "progress", this.width, this.getHeightA() - 5, this.getWidthA() - this.width, 5));
        this.field20867.setReAddChildren(true);
        this.field20867.setListening(false);
        this.field20865.setReAddChildren(true);
        this.field20865.method13247((var1x, var2x) -> {
            this.field20874 = true;
            this.field20871 = (float) this.getXA();
            this.field20872 = (float) this.getYA();
        });
        this.pause.setSelfVisible(false);
        this.play.setSelfVisible(false);
        this.play.onClick((var1x, var2x) -> this.musicManager.setPlaying(true));
        this.pause.onClick((var1x, var2x) -> this.musicManager.setPlaying(false));
        this.forwards.onClick((var1x, var2x) -> this.musicManager.playNextSong());
        this.backwards.onClick((var1x, var2x) -> this.musicManager.playPreviousSong());
        this.volumeSlider.method13709(var1x -> this.musicManager.setVolume((int) ((1.0F - this.volumeSlider.getVolume()) * 100.0F)));
        this.volumeSlider.setVolume(1.0F - (float) this.musicManager.getVolume() / 100.0F);
        this.addToList(
                this.searchBox = new SearchBox(
                        this, "search", this.width, 0, this.getWidthA() - this.width, this.getHeightA() - this.field20848, "Search..."
                )
        );
        this.searchBox.setSelfVisible(true);
        this.searchBox.setListening(false);
    }

    private void method13189(ScrollableContentPanel var1) {
        if (this.field20852 != null) {
            this.field20852.setSelfVisible(false);
        }

        var1.setSelfVisible(true);
        this.field20849 = var1.getText();
        this.field20852 = var1;
        this.searchBox.setSelfVisible(false);
        this.field20852.scrollStep = 65;
    }

    private void playSong(Thumbnails manager, YoutubeVideoData video) {
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
                if (var12 + this.getWidthA() > this.parent.getWidthA() + var14 && newHeight - this.mouseX > 70) {
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
                .changeDirection(this.getXA() + this.getWidthA() > this.parent.getWidthA() && !this.field20874 ? Animation.Direction.FORWARDS : Animation.Direction.BACKWARDS);
        partialTicks *= 0.5F + (1.0F - this.field20873.calcPercent()) * 0.5F;
        if (this.musicManager.isPlayingSong()) {
            this.play.setSelfVisible(false);
            this.pause.setSelfVisible(true);
        } else {
            this.play.setSelfVisible(true);
            this.pause.setSelfVisible(false);
        }

        RenderUtil.drawRoundedRect(
                (float) (this.getXA() + this.width),
                (float) this.getYA(),
                (float) (this.getXA() + this.getWidthA()),
                (float) (this.getYA() + this.getHeightA() - this.field20848),
                RenderUtil2.applyAlpha(-14277082, partialTicks * 0.8F)
        );
        RenderUtil.drawRoundedRect(
                (float) this.getXA(),
                (float) this.getYA(),
                (float) (this.getXA() + this.width),
                (float) (this.getYA() + this.getHeightA() - this.field20848),
                RenderUtil2.applyAlpha(-16777216, partialTicks * 0.95F)
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
                RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
        );
        RenderUtil.drawString(
                ResourceRegistry.JelloLightFont20,
                var4 + this.getXA() + 80,
                (float) (this.getYA() + 40),
                "music",
                RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), partialTicks)
        );
        RenderUtil.drawRoundedRect((float) this.getXA(), (float) this.getYA(), (float) this.getWidthA(), (float) this.getHeightA(), 14.0F, partialTicks);
        super.draw(partialTicks);
        if (this.field20852 != null) {
            this.method13196(partialTicks);
        }
    }

    private void method13192(float var1) {
        int duration1 = (int) this.musicManager.getDuration();
        int duration = this.musicManager.getDurationInt();
        RenderUtil.drawString(
                ResourceRegistry.JelloLightFont14,
                (float) (this.getXA() + this.width + 14),
                (float) (this.getYA() + this.getHeightA() - 10) - 22.0F * var1,
                YoutubeUtil.parseSongTime(duration1),
                RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var1 * var1)
        );
        RenderUtil.drawString(
                ResourceRegistry.JelloLightFont14,
                (float) (this.getXA() + this.getWidthA() - 14 - ResourceRegistry.JelloLightFont14.getWidth(YoutubeUtil.parseSongTime(duration))),
                (float) (this.getYA() + this.getHeightA() - 10) - 22.0F * var1,
                YoutubeUtil.parseSongTime(duration),
                RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var1 * var1)
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
                    RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var1 * var1)
            );
            RenderUtil.drawRoundedRect(
                    (float) this.getXA(),
                    (float) (this.getYA() + this.getHeightA() - this.field20848),
                    (float) (this.getXA() + this.getWidthA()),
                    (float) (this.getYA() + this.getHeightA() - 5),
                    RenderUtil2.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.43F * var1)
            );
            RenderUtil.drawRoundedRect(
                    (float) this.getXA(),
                    (float) (this.getYA() + this.getHeightA() - 5),
                    (float) (this.getXA() + this.width),
                    (float) (this.getYA() + this.getHeightA()),
                    RenderUtil2.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.43F * var1)
            );
            RenderUtil.drawImage(
                    (float) (this.getXA() + (this.width - 114) / 2),
                    (float) (this.getYA() + this.getHeightA() - 170),
                    114.0F,
                    114.0F,
                    var4,
                    RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var1)
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
                    RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var1 * var1)
            );
            RenderUtil.drawRoundedRect(
                    (float) this.getXA(),
                    (float) (this.getYA() + this.getHeightA() - this.field20848),
                    (float) (this.getXA() + this.getWidthA()),
                    (float) (this.getYA() + this.getHeightA() - 5),
                    RenderUtil2.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.43F * var1)
            );
            RenderUtil.drawRoundedRect(
                    (float) this.getXA(),
                    (float) (this.getYA() + this.getHeightA() - 5),
                    (float) (this.getXA() + this.width),
                    (float) (this.getYA() + this.getHeightA()),
                    RenderUtil2.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.43F * var1)
            );
            RenderUtil.drawImage(
                    (float) (this.getXA() + (this.width - 114) / 2),
                    (float) (this.getYA() + this.getHeightA() - 170),
                    114.0F,
                    114.0F,
                    Resources.artworkPNG,
                    RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var1)
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

        RenderUtil.startScissor(var13, var14, var13 + var11, var14 + var12, true);
        RenderUtil.drawString(
                ResourceRegistry.JelloLightFont14,
                (float) var13 - (float) var10 * var9 - 50.0F * var9,
                (float) var14,
                text,
                RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var1 * var1 * Math.min(1.0F, Math.max(0.0F, 1.0F - var9 * 0.75F)))
        );
        if (var9 > 0.0F) {
            RenderUtil.drawString(
                    ResourceRegistry.JelloLightFont14,
                    (float) var13 - (float) var10 * var9 + (float) var10,
                    (float) var14,
                    text,
                    RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var1 * var1)
            );
        }

        RenderUtil.restoreScissor();
    }

    private void method13196(float var1) {
        this.field20852.setReAddChildren(false);
        if (this.field20863 != this.field20852.getScrollBarOffset()) {
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
                    RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var4 * var1)
            );
        }

        RenderUtil.drawRoundedRect(
                (float) this.width,
                0.0F,
                (float) this.getWidthA(),
                (float) this.field20847,
                RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var4 * var1 * 0.2F)
        );
        RenderUtil.drawString(
                ResourceRegistry.JelloLightFont25,
                (float) ((this.getWidthA() - ResourceRegistry.JelloLightFont25.getWidth(this.field20849) + this.width) / 2),
                16.0F + (1.0F - var4) * 14.0F,
                this.field20849,
                RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var4)
        );
        RenderUtil.drawString(
                ResourceRegistry.JelloMediumFont25,
                (float) ((this.getWidthA() - ResourceRegistry.JelloMediumFont25.getWidth(this.field20849) + this.width) / 2),
                16.0F + (1.0F - var4) * 14.0F,
                this.field20849,
                RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 1.0F - var4)
        );
        RenderUtil.drawImage(
                (float) this.width,
                (float) this.field20847,
                (float) (this.getWidthA() - this.width),
                20.0F,
                Resources.shadowBottomPNG,
                RenderUtil2.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), var4 * var1 * 0.5F)
        );
        this.field20863 = this.field20852.getScrollBarOffset();
    }

    public static ScrollableContentPanel getTabs(MusicPlayer player) {
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

    public static void method13210(MusicPlayer player, ScrollableContentPanel tabs) {
        player.method13189(tabs);
    }

    public static void playSong(MusicPlayer player, Thumbnails videoManager, YoutubeVideoData video) {
        player.playSong(videoManager, video);
    }
}
