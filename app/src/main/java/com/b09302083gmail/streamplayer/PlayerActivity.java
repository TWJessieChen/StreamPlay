package com.b09302083gmail.streamplayer;

import com.b09302083gmail.utils.OnTaskCompleted;
import com.b09302083gmail.utils.QLog;
import com.b09302083gmail.utils.YouTubeUtility;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by JessieChen on 2016/3/9.
 */
public class PlayerActivity extends Activity implements View.OnClickListener, Handler.Callback,
        android.view.SurfaceHolder.Callback, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnVideoSizeChangedListener, OnTaskCompleted {
    private static final String TAG = PlayerActivity.class.getSimpleName();

    private MediaPlayer mMediaPlayer;

    private SurfaceView mSurfaceView;

    private SurfaceHolder mSurfaceHolder;

    private String mVideoURL = "https://www.youtube.com/watch?v=Q3oItpVa9fs";

    private GetLinkTask getlinktask;

    private int media_length;

    private AlertDialog mAlertDialog;

    private class GetLinkTask extends AsyncTask<String, Integer, String> {
        YouTubeUtility.VideoInfo vi = YouTubeUtility.getInstance();

        private OnTaskCompleted listener;

        public GetLinkTask(OnTaskCompleted listener) {
            this.listener = listener;
        }

        @Override
        protected String doInBackground(String... urls) {
            String result = "ok";

            Uri uri = Uri.parse(urls[0]);
            QLog.d(TAG, "Uri: " + uri);
            vi.setId(uri.getQueryParameter("v"));
            QLog.d(TAG, "vi: " + vi.getId());

            String url = YouTubeUtility.getYouTubeUrl(vi, false);
            QLog.d(TAG, "Url: " + url);
            if (mMediaPlayer != null && url != null) {
                if (!(url.contains("error"))) {
                    try {
                        mMediaPlayer.setDataSource(PlayerActivity.this, Uri.parse(url));
                        mMediaPlayer.prepareAsync();
                        QLog.i(TAG, "media player enters preparing status");
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        result = "error";
                        QLog.d(TAG, "IllegalArgumentException!!!");
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                        result = "error";
                        QLog.d(TAG, "IllegalStateException!!!");
                    } catch (IOException e) {
                        e.printStackTrace();
                        result = "error";
                        QLog.d(TAG, "IOException!!!");
                    }
                } else {
                    String[] errorReason = url.split(":");
                    result = errorReason[1];
                }
            } else {
                result = "error";
            }
            return result;
        }

        protected void onPostExecute(String result) {
            QLog.d(TAG, "result: " + result);
            if (result != "ok") {
                Toast.makeText(PlayerActivity.this, result, Toast.LENGTH_SHORT).show();
            } else {
                listener.onTaskCompleted(vi);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player);
//        if (mVideoURL == null || mVideoURL.isEmpty()) {
//            Toast.makeText(this, "invalid video url", Toast.LENGTH_LONG).show();
//            finish();
//            return;
//        }
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        mSurfaceView.setOnClickListener(this);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceView.setOnTouchListener(new OnTouchListenerIml());

        mAlertDialog = getAlertDialog("Please enter url(youtube)","Url: ");
    }

    @Override
    public void onResume() {
        super.onResume();
        QLog.v(TAG, "onResume");
        if(!mAlertDialog.isShowing()) {
            mAlertDialog.show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private AlertDialog getAlertDialog(String title,String message){

        AlertDialog.Builder builder = new AlertDialog.Builder(PlayerActivity.this);
        builder.setTitle(title);
        builder.setMessage(message);
        final EditText input = new EditText(MainApplication.getAppContext());
        input.setHeight(100);
        input.setWidth(340);
        input.setGravity(Gravity.LEFT);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(PlayerActivity.this, "Wait a minute...", Toast.LENGTH_LONG).show();
                QLog.d(TAG, "Url input: " + input.getText());
                mVideoURL = input.getText().toString().replaceAll("https://youtu.be/","https://www.youtube.com/watch?v=");
                play(mVideoURL);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(PlayerActivity.this, "Play preset youtube video.", Toast.LENGTH_LONG).show();
                play(mVideoURL);
            }
        });

        return builder.create();
    }

    private class OnTouchListenerIml implements View.OnTouchListener {
        public boolean onTouch(View v, MotionEvent event) {
            QLog.d(TAG, "OnTouch Event.");
            if (mMediaPlayer.isPlaying()) {
                QLog.d(TAG, "Touch Pause time: " + mMediaPlayer.getCurrentPosition());
                mMediaPlayer.pause();
                media_length = mMediaPlayer.getCurrentPosition();
            } else {
                QLog.d(TAG, "Touch Start time: " + mMediaPlayer.getCurrentPosition());
                mMediaPlayer.seekTo(media_length);
                mMediaPlayer.start();
            }
            return false;
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        QLog.d(TAG, "surfaceCreated.");
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mMediaPlayer.setOnVideoSizeChangedListener(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mSurfaceHolder.setFixedSize(width, height);
        mMediaPlayer.setDisplay(mSurfaceHolder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            default:
                break;
        }

        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mMediaPlayer.start();
        mMediaPlayer.setOnBufferingUpdateListener(this);
    }

    @Override
    public void onTaskCompleted(YouTubeUtility.VideoInfo v) {

    }

    private void play(String url) {
        reset();
        getlinktask = new GetLinkTask(this);
        getlinktask.execute(url);
    }

    private void reset() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
        }
    }

    private void release() {
        reset();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void cancelTask() {
        getlinktask.cancel(false);
    }
}
