package com.b09302083gmail.utils.httpget;

import com.b09302083gmail.controller.MainController;
import com.b09302083gmail.model.Config;
import com.b09302083gmail.model.VideoInfo;
import com.b09302083gmail.utils.Format;
import com.b09302083gmail.utils.QLog;
import com.b09302083gmail.utils.VideoStream;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;

import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by JessieChen on 2016/03/28.
 */
public class HttpGetRequestTask extends AsyncTask<String, String, String> {
    private static final String TAG = HttpGetRequestTask.class.getSimpleName();

    HttpClient mClient;

    HttpGet mHttpGet;

    private String mYouTubeFmtQuality = null;

    private boolean mFallback = false;

    private static final int REQUEST_TIMEOUT = 5 * 1000;

    private static final int SO_TIMEOUT = 20 * 1000;

    private BasicHttpParams mParams = new BasicHttpParams();

    public HttpGetRequestTask(String aRequestURI, String aQuality, boolean isFallback) {
        mHttpGet = new HttpGet(aRequestURI);
        mYouTubeFmtQuality = aQuality;
        mFallback = isFallback;
    }

    public void setHttpGetRequest() {
        HttpConnectionParams.setConnectionTimeout(mParams, REQUEST_TIMEOUT);
        HttpConnectionParams.setSoTimeout(mParams, SO_TIMEOUT);
//        mHttpGet.addHeader("APIVersion", "1.0");
//        mHttpGet.addHeader("Content-Type", "application/json");
//        mHttpGet.addHeader("language", "zh_tw");
    }

    @Override
    protected synchronized String doInBackground(String... params) {
        HttpClient httpclient = new DefaultHttpClient(mParams);
        HttpResponse response;
        String responseString = null;
        try {
            response = httpclient.execute(mHttpGet);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                responseString = out.toString("UTF-8");
                out.close();
            } else {
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        QLog.v(TAG, "doInBackground: " + responseString);
        return responseString;
    }

    @Override
    protected synchronized void onPostExecute(String result) {
        super.onPostExecute(result);

        QLog.d(TAG, "onPostExecute: " + result);

        //Error Handling...
        if (result == null) {
            onPostError();
            return;
        }

        String[] lArgs = result.split("&");
        Map<String, String> lArgMap = new HashMap<String, String>();
        for (int i = 0; i < lArgs.length; i++) {
            String[] lArgValStrArr = lArgs[i].split("=");
            if (lArgValStrArr != null) {
                if (lArgValStrArr.length >= 2) {
                    lArgMap.put(lArgValStrArr[0], URLDecoder.decode(lArgValStrArr[1]));
                }
            }
        }

        // Find out the URI string from the parameters

        // Populate the list of formats for the video
        ArrayList<Format> lFormats = new ArrayList<Format>();
        if (lArgMap.get("fmt_list") != null) {
            String lFmtList = URLDecoder.decode(lArgMap.get("fmt_list"));
            if (null != lFmtList) {
                String lFormatStrs[] = lFmtList.split(",");
                for (String lFormatStr : lFormatStrs) {
                    Format lFormat = new Format(lFormatStr);
                    lFormats.add(lFormat);
                }
            }
        }

        // Populate the list of streams for the video
        if (lArgMap.get("url_encoded_fmt_stream_map") != null) {
            String lStreamList = lArgMap.get("url_encoded_fmt_stream_map");
            if (null != lStreamList) {
                String lStreamStrs[] = lStreamList.split(",");
                ArrayList<VideoStream> lStreams = new ArrayList<VideoStream>();
                for (String lStreamStr : lStreamStrs) {
                    VideoStream lStream = new VideoStream(lStreamStr);
                    lStreams.add(lStream);
                }

                // Search for the given format in the list of video formats
                // if it is there, select the corresponding stream
                // otherwise if fallback is requested, check for next lower format
                int lFormatId = Integer.parseInt(mYouTubeFmtQuality);

                Format lSearchFormat = new Format(lFormatId);

                while (!lFormats.contains(lSearchFormat) && mFallback) {
                    int lOldId = lSearchFormat.getId();
                    int lNewId = getSupportedFallbackId(lOldId);

                    if (lOldId == lNewId) {
                        break;
                    }
                    lSearchFormat = new Format(lNewId);
                }

                if (lSearchFormat.getId() == 22 || lSearchFormat.getId() == 37) {
                    VideoInfo.getInstance().setHdSupport(true);
                } else {
                    VideoInfo.getInstance().setHdSupport(false);
                }

                // Check if HD is supported in this video
                if (mYouTubeFmtQuality.equals(Config.m360p)) {
                    Format lHdFormat = new Format(Integer.parseInt(Config.m720p));
                    if (lFormats.contains(lHdFormat)) {
                        VideoInfo.getInstance().setHdSupport(true);
                    } else {
                        VideoInfo.getInstance().setHdSupport(false);
                    }
                }

                int lIndex = lFormats.indexOf(lSearchFormat);
                if (lIndex >= 0) {
                    VideoStream lSearchStream = lStreams.get(lIndex);
                    VideoInfo.getInstance().setUrl(lSearchStream.getUrl());
                    QLog.d(TAG, "Url: " + lSearchStream.getUrl());
                    MainController.getInstance()
                            .sendEmptyMessage(MainController.MSG_GET_YOUTUBE_URL_PARSER_IS_SUCCESS);
                }
            }
        }

        // Set error reason in URL by format: "error:reason..."
        // and show error reason on player
        String error = null;
        if (lArgs[1].contains("errorcode") && lArgs[2] != null) {
            if (lArgs[2].contains("=")) {
                error = URLDecoder.decode(lArgs[2]);
                int end = error.indexOf("<");
                end = (end == -1) ? error.length() - 1 : end;
                error = "error:" + error.substring(error.indexOf("=") + 1, end);
                VideoInfo.getInstance().setUrl(error);
            }
        }
    }

    private void onPostError() {
        QLog.d(TAG, "onPostError!!!");
        MainController.getInstance().sendEmptyMessage(
                MainController.MSG_GET_YOUTUBE_URL_PARSER_IS_FAIL);
    }

    public static int getSupportedFallbackId(int pOldId) {
        // Actually, i only care about:
        // 18 for 360p, 22 for 720p and 37 for 1080p.
        final int lSupportedFormatIds[] = {
                5,
                13,  // 3GPP (MPEG-4 encoded) Low quality
                17,  // 3GPP (MPEG-4 encoded) Medium quality
                18,  // MP4  (H.264 encoded) Normal quality, 360p
                22,  // MP4  (H.264 encoded) High quality, 720p
                34,  // FLV  360p
                35,  // FLV  480p
                37   // MP4  (H.264 encoded) High quality, 1080p
        };

        int lFallbackId = pOldId;
        for (int i = lSupportedFormatIds.length - 1; i >= 0; i--) {
            if (pOldId == lSupportedFormatIds[i] && i > 0) {
                lFallbackId = lSupportedFormatIds[i - 1];
                QLog.d(TAG, "Video Format: " + lFallbackId);
            }
        }

        return lFallbackId;
    }
}
