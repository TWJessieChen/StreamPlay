package com.b09302083gmail.utils;

import com.b09302083gmail.model.Config;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class YouTubeUtility {
    public final static String TAG = "YouTubeUtility";

    private final static String m720p = "22";

    private final static String m360p = "18";

    private final static String m1080p = "37";

    static VideoInfo vi;

    public static class VideoInfo {
        String mYouTubeUrl;

        String mYouTubeId;

        boolean mYouTubeHd;

        VideoInfo() {
            mYouTubeUrl = null;
            mYouTubeId = null;
            mYouTubeHd = false;
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

    public static VideoInfo getInstance() {
        vi = null;
        vi = new VideoInfo();

        return vi;
    }

    /**
     * Calculate the YouTube URL to load the video.  Includes retrieving a token that YouTube
     * requires to play the video.
     *
     * @param pYouTubeFmtQuality quality of the video.  17=low, 18=high
     * @param -bFallback         whether to fallback to lower quality in case the supplied quality
     *                           is not available
     * @param -pYouTubeVideoId   the id of the video
     * @return the url string that will retrieve the video
     */
    public static void calculateYouTubeUrl(
            String pYouTubeFmtQuality, boolean pFallback, VideoInfo pVi) throws
            IOException, ClientProtocolException, UnsupportedEncodingException {

        HttpClient lClient = new DefaultHttpClient();

        HttpGet lGetMethod =
                new HttpGet(Config.YOUTUBE_VIDEO_INFORMATION_URL + pVi.getId());

        HttpResponse lResp = null;

        lResp = lClient.execute(lGetMethod);

        ByteArrayOutputStream lBOS = new ByteArrayOutputStream();
        String lInfoStr = null;

        lResp.getEntity().writeTo(lBOS);
        lInfoStr = new String(lBOS.toString("UTF-8"));

        String[] lArgs = lInfoStr.split("&");
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
                int lFormatId = Integer.parseInt(pYouTubeFmtQuality);

                Format lSearchFormat = new Format(lFormatId);

                while (!lFormats.contains(lSearchFormat) && pFallback) {
                    int lOldId = lSearchFormat.getId();
                    int lNewId = getSupportedFallbackId(lOldId);

                    if (lOldId == lNewId) {
                        break;
                    }
                    lSearchFormat = new Format(lNewId);
                }

                if (lSearchFormat.getId() == 22 || lSearchFormat.getId() == 37) {
                    vi.setHdSupport(true);
                } else {
                    vi.setHdSupport(false);
                }

                // Check if HD is supported in this video
                if (pYouTubeFmtQuality.equals(m360p)) {
                    Format lHdFormat = new Format(Integer.parseInt(m720p));
                    if (lFormats.contains(lHdFormat)) {
                        vi.setHdSupport(true);
                    } else {
                        vi.setHdSupport(false);
                    }
                }

                int lIndex = lFormats.indexOf(lSearchFormat);
                if (lIndex >= 0) {
                    VideoStream lSearchStream = lStreams.get(lIndex);
                    vi.setUrl(lSearchStream.getUrl());
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
                pVi.setUrl(error);
            }
        }
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

    public static String getYouTubeUrl(VideoInfo mVi, boolean isHD) {
        try {
            String quality = getYouTubeFmtQuality(isHD);
            calculateYouTubeUrl(quality, true, mVi);
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

        return mVi.getUrl();
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
            return m720p;
        } else {
            return m360p;
        }
    }
}
