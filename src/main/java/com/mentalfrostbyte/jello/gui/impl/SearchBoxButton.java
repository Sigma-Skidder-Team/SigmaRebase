package com.mentalfrostbyte.jello.gui.impl;

import com.mentalfrostbyte.Client;
import com.mentalfrostbyte.jello.gui.base.CustomGuiScreen;
import com.mentalfrostbyte.jello.gui.unmapped.AnimatedIconPanelWrap;
import com.mentalfrostbyte.jello.gui.unmapped.YoutubeVideoThumbnail;
import com.mentalfrostbyte.jello.gui.unmapped.MusicTabs;
import com.mentalfrostbyte.jello.gui.unmapped.UIInput;
import com.mentalfrostbyte.jello.managers.MusicManager;
import com.mentalfrostbyte.jello.managers.util.music.YoutubeJPGThumbnail;
import com.mentalfrostbyte.jello.managers.util.music.YoutubeVideoData;
import com.mentalfrostbyte.jello.util.ColorHelper;
import com.mentalfrostbyte.jello.util.ThumbnailUtil;

import java.util.ArrayList;

public class SearchBoxButton extends AnimatedIconPanelWrap {
    public MusicTabs field20840;
    public UIInput searchBox;
    private ArrayList<YoutubeVideoData> field20842;
    private final MusicManager field20843 = Client.getInstance().musicManager;

    public SearchBoxButton(CustomGuiScreen var1, String var2, int var3, int var4, int var5, int var6, String var7) {
        super(var1, var2, var3, var4, var5, var6, ColorHelper.field27961, var7, false);
        this.addToList(this.field20840 = new MusicTabs(this, "albumView", 0, 0, var5, var6, ColorHelper.field27961, "View"));
        this.addToList(this.searchBox = new UIInput(this, "searchInput", 30, 14, var5 - 60, 70, UIInput.field20742, "", "Search..."));
        this.searchBox.method13292(true);
    }

    @Override
    public void draw(float partialTicks) {
        super.draw(partialTicks);
    }

    @Override
    public void keyPressed(int keyCode) {
        if (keyCode == 257 && this.searchBox.method13297()) {
            this.searchBox.method13145(false);
            new Thread(
                    () -> {
                        this.field20842 = new ArrayList<>();
                        YoutubeJPGThumbnail[] var3 = ThumbnailUtil.search(this.searchBox.getTypedText());

                        for (YoutubeJPGThumbnail var7 : var3) {
                            this.field20842.add(new YoutubeVideoData(var7.videoID, var7.title, var7.fullUrl));
                        }

                        this.runThisOnDimensionUpdate(
                                () -> {
                                    this.method13236(this.field20840);
                                    this.addToList(
                                            this.field20840 = new MusicTabs(this, "albumView", 0, 0, this.widthA, this.heightA, ColorHelper.field27961, "View")
                                    );
                                    if (this.field20842 != null) {
                                        for (int var3x = 0; var3x < this.field20842.size(); var3x++) {
                                            YoutubeVideoData var4 = this.field20842.get(var3x);
                                            YoutubeVideoThumbnail var7x;
                                            this.field20840
                                                    .addToList(
                                                            var7x = new YoutubeVideoThumbnail(
                                                                    this.field20840,
                                                                    10 + var3x % 3 * 183 - (var3x % 3 <= 0 ? 0 : 10) - (var3x % 3 <= 1 ? 0 : 10),
                                                                    80 + 10 + (var3x - var3x % 3) / 3 * 210,
                                                                    183,
                                                                    220,
                                                                    var4
                                                            )
                                                    );
                                            var7x.doThis((var2, var3xx) -> this.field20843.playSong(null, var4));
                                        }
                                    }
                                }
                        );
                    }
            )
                    .start();
        }

        super.keyPressed(keyCode);
    }
}
