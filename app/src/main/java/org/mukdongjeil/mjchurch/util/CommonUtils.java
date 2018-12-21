package org.mukdongjeil.mjchurch.util;

public class CommonUtils {

    private static final String YOUTUBE_THUMB_URL_PREFIX = "http://img.youtube.com/vi/";
    private static final String YOUTUBE_THUMB_URL_POSTFIX = "/0.jpg";

    public static final String getYoutubeThumbnailUrl(String youtubeUrl) {
        String[] videoPath = youtubeUrl.split("/");
        StringBuilder photoUrl = new StringBuilder();
        photoUrl.append(YOUTUBE_THUMB_URL_PREFIX);
        photoUrl.append(videoPath[videoPath.length-1]);
        photoUrl.append(YOUTUBE_THUMB_URL_POSTFIX);
        return photoUrl.toString();
    }

}
