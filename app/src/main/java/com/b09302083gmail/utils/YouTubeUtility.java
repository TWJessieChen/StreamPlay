package com.b09302083gmail.utils;

import com.b09302083gmail.model.Config;
import com.b09302083gmail.model.VideoInfo;
import com.b09302083gmail.utils.httpget.HttpGetUtil;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by JessieChen on 2016/03/28.
 */
public class YouTubeUtility {
    private static final String TAG = YouTubeUtility.class.getSimpleName();

    public static void calculateYouTubeUrl(
            String aYouTubeFmtQuality, boolean isFallback) throws
            IOException, ClientProtocolException, UnsupportedEncodingException {

        try {
            HttpGetUtil
                    .requestCode(VideoInfo.getInstance().getId(), aYouTubeFmtQuality, isFallback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void getYouTubeUrl(boolean isHD) {
        try {
            String quality = getYouTubeFmtQuality(isHD);
            calculateYouTubeUrl(quality, true);
        } catch (ClientProtocolException e) {
            QLog.e("getYouTubeUrl", "ClientProtocolException");
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            QLog.e("getYouTubeUrl", "UnsupportedEncodingException");
            e.printStackTrace();
        } catch (IOException e) {
            QLog.e("getYouTubeUrl", "IOException");
            e.printStackTrace();
        }
    }

    /*
     * return argument for YouTube for quality
     * 34 is the best choice for SIS player on BenQ TV
     *
     * 37 	MP4 	1080p 	H.264 	High
     * 22   MP4     720p    H.264   High
     * 18 	MP4 	360p 	H.264 	Baseline
     * */
    private static String getYouTubeFmtQuality(boolean isHD) {
        if (isHD) {
            return Config.m720p;
        } else {
            return Config.m360p;
        }
    }
}
