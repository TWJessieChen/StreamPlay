package com.b09302083gmail.utils.httpget;

import com.b09302083gmail.model.Config;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;

/**
 * Created by JessieChen on 2016/03/28.
 */
public class HttpGetUtil {
    private static final String TAG = HttpGetUtil.class.getSimpleName();

    private static final String getRequestKey
            = Config.YOUTUBE_VIDEO_INFORMATION_URL;

    public synchronized static void requestCode(String aVideoId, String aQuality,
            boolean isFallback)
            throws UnsupportedEncodingException, JSONException {
        String URI = getRequestKey + aVideoId;
        HttpGetRequestTask postRequest = new HttpGetRequestTask(URI, aQuality, isFallback);
        postRequest.setHttpGetRequest();
        postRequest.execute();
    }
}
