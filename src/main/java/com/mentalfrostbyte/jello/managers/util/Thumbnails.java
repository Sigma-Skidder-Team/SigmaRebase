package com.mentalfrostbyte.jello.managers.util;

import com.mentalfrostbyte.jello.util.client.network.youtube.YoutubeContentType;
import com.mentalfrostbyte.jello.util.client.network.youtube.YoutubeJPGThumbnail;
import com.mentalfrostbyte.jello.util.client.network.youtube.YoutubeVideoData;
import com.mentalfrostbyte.jello.util.client.network.youtube.ThumbnailUtil;

import java.util.ArrayList;
import java.util.List;

public class Thumbnails {
    public String name;
    public String videoId;
    public YoutubeContentType contentType;
    public List<YoutubeVideoData> videoList = new ArrayList<>();
    public boolean isUpdated = false;

    public Thumbnails(String name, String videoId, YoutubeContentType contentType) {
        this.name = name;
        this.videoId = videoId;
        this.contentType = contentType;
    }

    public void refreshVideoList() {
        this.videoList = new ArrayList<>();
        YoutubeJPGThumbnail[] thumbnails = new YoutubeJPGThumbnail[0];
        if (this.contentType != YoutubeContentType.CHANNEL) {
            if (this.contentType == YoutubeContentType.PLAYLIST) {
                thumbnails = ThumbnailUtil.getFromPlaylist(this.videoId);
            }
        } else {
            thumbnails = ThumbnailUtil.getFromChannel(this.videoId);
        }

        for (YoutubeJPGThumbnail thumbnail : thumbnails) {
            this.videoList.add(new YoutubeVideoData(thumbnail.videoID, thumbnail.title, thumbnail.fullUrl));
        }
    }

    @Override
    public boolean equals(Object thumbnail) {
        if (thumbnail != this) {
            if (thumbnail instanceof Thumbnails thumbnails) {
                return thumbnails.videoId.equals(this.videoId);
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
}
