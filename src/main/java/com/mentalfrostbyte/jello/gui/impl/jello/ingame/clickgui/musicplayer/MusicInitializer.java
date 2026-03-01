package com.mentalfrostbyte.jello.gui.impl.jello.ingame.clickgui.musicplayer;

import com.mentalfrostbyte.jello.gui.base.elements.impl.button.Button;
import com.mentalfrostbyte.jello.gui.base.elements.impl.button.types.ThumbnailButton;
import com.mentalfrostbyte.jello.gui.impl.jello.buttons.ScrollableContentPanel;
import com.mentalfrostbyte.jello.managers.util.Thumbnails;
import com.mentalfrostbyte.jello.util.client.network.youtube.YoutubeVideoData;
import com.mentalfrostbyte.jello.util.client.render.ResourceRegistry;
import com.mentalfrostbyte.jello.util.client.render.theme.ColorHelper;

public class MusicInitializer implements Runnable {
    public final Thumbnails thumbnail;
    public final ColorHelper colorHelper;
    public final MusicPlayer musicPlayer;
    public final MusicPlayer musicPlayer2;

    public MusicInitializer(MusicPlayer var1, Thumbnails var2, ColorHelper var3, MusicPlayer var4) {
        this.musicPlayer2 = var1;
        this.thumbnail = var2;
        this.colorHelper = var3;
        this.musicPlayer = var4;
    }

    @Override
    public void run() {
        if (!MusicPlayer.getTabs(this.musicPlayer2).isntQueue(this.thumbnail.videoId)) {
            Button var3;
            MusicPlayer.getTabs(this.musicPlayer2)
                    .addToList(
                            var3 = new Button(
                                    MusicPlayer.getTabs(this.musicPlayer2),
                                    this.thumbnail.videoId,
                                    0,
                                    MusicPlayer.getTabs(this.musicPlayer2).getButtonList().getChildren().size() * MusicPlayer.getHeight(this.musicPlayer2),
                                    MusicPlayer.getWidth(this.musicPlayer2),
                                    MusicPlayer.getHeight(this.musicPlayer2),
                                    this.colorHelper,
                                    this.thumbnail.name,
                                    ResourceRegistry.JelloLightFont14
                            )
                    );
            ScrollableContentPanel queue;
            this.musicPlayer
                    .addToList(
                            queue = new ScrollableContentPanel(
                                    this.musicPlayer,
                                    this.thumbnail.videoId,
                                    MusicPlayer.getWidth(this.musicPlayer2),
                                    0,
                                    this.musicPlayer.getWidthA() - MusicPlayer.getWidth(this.musicPlayer2),
                                    this.musicPlayer.getHeightA() - MusicPlayer.method13209(this.musicPlayer2),
                                    ColorHelper.field27961,
                                    this.thumbnail.name
                            )
                    );
            queue.setOnlyUpdateWhenSelfVisible(true);
            queue.setSelfVisible(false);
            queue.setListening(false);
            if (this.thumbnail.videoList != null) {
                for (int i = 0; i < this.thumbnail.videoList.size(); i++) {
                    YoutubeVideoData song = this.thumbnail.videoList.get(i);
                    ThumbnailButton thumbnail;
                    int x = 65;
                    int y = 10;
                    if (!queue.isntQueue(this.thumbnail.videoId)) {
                        queue.addToList(
                                thumbnail = new ThumbnailButton(
                                        queue,
                                        y + i % 3 * 183 - (i % 3 <= 0 ? 0 : y) - (i % 3 <= 1 ? 0 : y),
                                        x + y + (i - i % 3) / 3 * 210,
                                        183,
                                        220,
                                        song
                                )
                        );
                        thumbnail.onClick((parent, idk) -> {
                            if (this.musicPlayer.parent.hasJelloMusicRequirements())
                                MusicPlayer.playSong(this.musicPlayer2, this.thumbnail, song);
                        });
                    }
                }
            }

            var3.onClick((var2, var3x) -> MusicPlayer.method13210(this.musicPlayer2, queue));
        }
    }
}
