package com.b09302083gmail.controller;

import android.os.Handler;
import android.os.Message;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by JessieChen on 2016/03/28.
 */
public class MainController extends Handler {
    private static final String TAG = MainController.class.getSimpleName();

    private static MainController sMainController = null;

    /* HTTP POST API MSG */
    public static final int MSG_GET_YOUTUBE_URL_PARSER_IS_SUCCESS = 1;

    public static final int MSG_GET_YOUTUBE_URL_PARSER_IS_FAIL = 2;

    private CopyOnWriteArrayList<Handler.Callback> mListenerList = new CopyOnWriteArrayList<>();

    public static synchronized MainController getInstance() {
        if (null == sMainController) {
            synchronized (MainController.class) {
                if (sMainController == null) {
                    sMainController = new MainController();
                }
            }
        }
        return sMainController;
    }

    @Override
    public void handleMessage(final Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            default:
                notifyAllListeners(msg);
                break;
        }
    }

    public void registerUiListener(Callback aCallback) {
        if (!mListenerList.contains(aCallback)) {
            mListenerList.add(aCallback);
        }
    }

    public boolean deregisterUiListener(Handler.Callback aCallback) {
        return mListenerList.remove(aCallback);
    }

    private void notifyAllListeners(Message aMessage) {
        for (Callback callback : mListenerList) {
            callback.handleMessage(aMessage);
        }
    }
}
