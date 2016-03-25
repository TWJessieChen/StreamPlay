package com.b09302083gmail.model;

/**
 * Created by jessiechen on 16/3/25.
 */
public class VideoInfo {
    private static final String TAG = VideoInfo.class.getSimpleName();

    private static VideoInfo sVideoInfo = null;

    String mYouTubeUrl;

    String mYouTubeId;

    boolean mYouTubeHd;

    private VideoInfo() {
        mYouTubeUrl = null;
        mYouTubeId = null;
        mYouTubeHd = false;
    }

    public static synchronized VideoInfo getInstance() {
        if (null == sVideoInfo) {
            synchronized (VideoInfo.class) {
                if (sVideoInfo == null) {
                    sVideoInfo = new VideoInfo();
                }
            }
        }
        return sVideoInfo;
    }

    public void setUrl(String url) {
        mYouTubeUrl = url;
    }

    public void setId(String id) {
        mYouTubeId = id;
    }

    public void setHdSupport(boolean mHd) {
        mYouTubeHd = mHd;
    }

    public String getUrl() {
        return mYouTubeUrl;
    }

    public String getId() {
        return mYouTubeId;
    }

    public boolean getHdSupport() {
        return mYouTubeHd;
    }
}