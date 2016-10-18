package com.vokevr.simpleviewer;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.vrtoolkit.cardboard.ScreenOnFlagHelper;
import com.jackalopelite.JackalopeUtil;
import com.jackalopelite.JackalopeView;
import com.jackalopelite.scoreboard.ScoreboardType;
import com.vokevr.simpleviewer.helpers.FullscreenMode;
import com.vokevr.simpleviewer.helpers.SimpleOrientationListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Timer;
import java.util.TimerTask;

public class VideoPlayerActivity extends Activity implements JackalopeView.VideoPlayerEventListener, View.OnClickListener {
    
    public final String TAG = this.getClass().getSimpleName();

    public static final String KEY_URL = "KEY_URL";
    public static final String KEY_MODE = "KEY_MODE";
    public static final String KEY_TYPE = "KEY_TYPE";

    private String url = "";
    private JackalopeView.VideoType displayMode = JackalopeView.VideoType.VideoTB;
    private boolean isVrEnabled = true;
    private ImageView ivVideoPlay, ivVideoPause, ivVideoCardBoard, ivVideoCardboardBack, ivToggleOrientation;
    private SeekBar seekBarVideo;
    private LinearLayout llVideoControls;

    private int gvrViewHeight = 0;
    private int gvrViewWidth = 0;
    private int requestedOrientation = Configuration.ORIENTATION_UNDEFINED;

    private JackalopeView jackalopeView;
    private FullscreenMode fullscreenMode;
    private final ScreenOnFlagHelper screenOnFlagHelper = new ScreenOnFlagHelper(this);

    private Timer timer = new Timer();
    private GestureDetector detector;

    private static final CookieManager defaultCookieManager;

    static {
        defaultCookieManager = new CookieManager();
        defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        Log.d(TAG, "onCreate: ");
        CookieHandler currentHandler = CookieHandler.getDefault();
        if (currentHandler != defaultCookieManager) {
            CookieHandler.setDefault(defaultCookieManager);
        }

        this.fullscreenMode = new FullscreenMode(this.getWindow());
        this.screenOnFlagHelper.setScreenAlwaysOn(true);
        detector = new GestureDetector(this, new GestureTap());

        ivVideoPlay = (ImageView) findViewById(R.id.iv_video_play);
        ivVideoPause = (ImageView) findViewById(R.id.iv_video_pause);
        ivVideoCardBoard = (ImageView) findViewById(R.id.iv_video_cardboard);
        seekBarVideo = (SeekBar) findViewById(R.id.seekbar_video_progress);
        llVideoControls = (LinearLayout) findViewById(R.id.ll_video_controls);
        ivVideoCardboardBack = (ImageView) findViewById(R.id.iv_video_cardboard_back);
        ivToggleOrientation = (ImageView) findViewById(R.id.iv_video_cardboard_resize);

        if (getIntent() != null && getIntent().hasExtra(KEY_URL)) {
            url = getIntent().getStringExtra(KEY_URL);
        }

        if (getIntent() != null && getIntent().hasExtra(KEY_TYPE)) {
            displayMode = JackalopeView.VideoType.values()[getIntent().getIntExtra(KEY_TYPE, JackalopeView.VideoType.VideoTB.ordinal())];
        }

        if (getIntent() != null && getIntent().hasExtra(KEY_MODE)) {
            isVrEnabled = getIntent().getBooleanExtra(KEY_MODE, true);
        }

        // Check whether or not the VR stream can be supported
        if (displayMode == JackalopeView.VideoType.VideoTB) {
            if (!JackalopeUtil.isVrStreamSupported())
                Toast.makeText(VideoPlayerActivity.this, "VR Stream is not supported", Toast.LENGTH_SHORT).show();
            if (!JackalopeUtil.isHardwareSupported(VideoPlayerActivity.this))
                Toast.makeText(VideoPlayerActivity.this, "Hardware is not supported", Toast.LENGTH_SHORT).show();
        }

        jackalopeView = (JackalopeView) findViewById(R.id.jv_root);
        jackalopeView.setVideoPlayerEventListener(this);
        jackalopeView.setVRModeEnabled(isVrEnabled);
        jackalopeView.post(new Runnable() {
            @Override
            public void run() {
                gvrViewHeight = jackalopeView.getMeasuredHeight();
                gvrViewWidth = jackalopeView.getMeasuredWidth();
                updateGvrViewLayoutParams();
            }
        });
        jackalopeView.setVideoSource(url);
        jackalopeView.prepare();

        jackalopeView.setOnTouchListenerForJackalopeView(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                detector.onTouchEvent(event);
                return true;
            }
        });

        ivVideoCardBoard.setOnClickListener(this);
        ivVideoPlay.setOnClickListener(this);
        ivVideoPause.setOnClickListener(this);
        ivVideoCardboardBack.setOnClickListener(this);
        ivToggleOrientation.setOnClickListener(this);

        hideVideoControls();
        ivVideoCardboardBack.setVisibility(View.GONE);
        seekBarVideo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    jackalopeView.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        SimpleOrientationListener mOrientationListener = new SimpleOrientationListener(this) {

            @Override
            public void onSimpleOrientationChanged(int orientation) {
                boolean isAutoRotateEnabled = (android.provider.Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1);
                if (orientation == requestedOrientation && orientation != -1 && isAutoRotateEnabled) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    requestedOrientation = Configuration.ORIENTATION_UNDEFINED;
                }
            }
        };

        mOrientationListener.enable();

    }

    @Override
    public void onIdle() {
        Log.d(TAG, "onIdle: ");
    }

    @Override
    public void onPreparing() {
        Log.d(TAG, "onPreparing: ");
    }

    @Override
    public void onBuffering() {
        Log.d(TAG, "onBuffering: ");
    }

    @Override
    public void onReadyToPlay() {
        Log.d(TAG, "onReadyToPlay: ");
        seekBarVideo.setMax((int) jackalopeView.getDuration());
        startTimer();

        if (isVrEnabled) {
            ivVideoCardboardBack.setVisibility(View.VISIBLE);
            hideVideoControls();
        } else {
            showVideoControls();
            ivVideoCardboardBack.setVisibility(View.GONE);
        }
    }

    @Override
    public void onError(int what, int extra, Exception e) {
        Log.d(TAG, "onError: ");
        if (what == 2) {
            String message = getMessageForInternalError(extra);
            Toast.makeText(VideoPlayerActivity.this, message, Toast.LENGTH_SHORT).show();
        }

        Log.i("SimpleViewer", "Exception: " + e.toString() + " message: " + e.getMessage());
    }

    @Override
    public void onEndOfStream() {
        Log.d(TAG, "onEndOfStream: ");
    }

    @Override
    public void onJackalopeViewReady() {
        Log.d(TAG, "onJackalopeViewReady: ");
        jackalopeView.setVideoType(displayMode);
        jackalopeView.setLoopEnabled(false);

        AssetManager assetManager = getAssets();

        InputStream istr;
        Bitmap bitmap = null;

        try {
            istr = assetManager.open("starrynight_tb_low.jpg");
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
        }

        jackalopeView.setBackground(bitmap);

        jackalopeView.enableScoreboard(ScoreboardType.ScoreboardType_Kabaddi, true);
    }

    private String getMessageForInternalError(int extra) {
        Log.d(TAG, "getMessageForInternalError: ");
        switch (extra) {
            case 2:
                return "Fail to initialize the renderer ";
            case 3:
                return "Fail to initialize the Audio Track ";
            case 4:
                return "Fail to write the Audio Track ";
            case 5:
                return "Underrun Audio Track ";
            case 6:
                return "Fail to initialize the decoder ";
            case 7:
                return "Crypto Error ";
            case 8:
                return "Fail to load the media stream ";
            case 9:
                return "FError on DRM session manager ";
            default:
                return "Unknown Error ";
        }
    }

    public void showVideoControls() {
        Log.d(TAG, "showVideoControls: ");
        llVideoControls.setVisibility(View.VISIBLE);
        ivVideoCardBoard.setVisibility(View.VISIBLE);
    }

    public void hideVideoControls() {
        Log.d(TAG, "hideVideoControls: ");
        llVideoControls.setVisibility(View.GONE);
        ivVideoCardBoard.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        jackalopeView.onResume();
        if (jackalopeView.isPlaying()) {
            startTimer();
        }

        this.fullscreenMode.goFullscreen();
        this.screenOnFlagHelper.start();
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.d(TAG, "onWindowFocusChanged: ");
        this.fullscreenMode.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: ");
        stopTimer();
        jackalopeView.onPause();
        this.screenOnFlagHelper.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        this.screenOnFlagHelper.setScreenAlwaysOn(false);
        jackalopeView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick: ");
        switch (v.getId()) {
            case R.id.iv_video_play:
                jackalopeView.resume();
                startTimer();
                break;
            case R.id.iv_video_pause:
                jackalopeView.pause();
                stopTimer();
                break;
            case R.id.iv_video_cardboard:
                hideVideoControls();
                isVrEnabled = true;
                jackalopeView.setVRModeEnabled(isVrEnabled);
                ivVideoCardboardBack.setVisibility(View.VISIBLE);
                break;
            case R.id.iv_video_cardboard_back:
                ivVideoCardboardBack.setVisibility(View.GONE);
                isVrEnabled = false;
                jackalopeView.setVRModeEnabled(isVrEnabled);
                showVideoControls();
                break;
            case R.id.iv_video_cardboard_resize:
                toggleOrientation();
                break;
            default:
                break;
        }
    }

    private void startTimer() {
        Log.d(TAG, "startTimer: ");
        stopTimer();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                int currentPosition = jackalopeView.getCurrentPosition();
                if (seekBarVideo != null && currentPosition != -1) {
                    seekBarVideo.setProgress(currentPosition);
                }
            }
        }, 0, 1000);
    }

    private void stopTimer() {
        Log.d(TAG, "stopTimer: ");
        try {
            timer.cancel();
        } catch (IllegalStateException e) {

        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateGvrViewLayoutParams();
    }

    public void toggleOrientation() {
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            requestedOrientation = Configuration.ORIENTATION_PORTRAIT;
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            requestedOrientation = Configuration.ORIENTATION_LANDSCAPE;
        }
    }

    private void updateGvrViewLayoutParams() {
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            ViewGroup.LayoutParams params = jackalopeView.getLayoutParams();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            setFullScreen(VideoPlayerActivity.this, true);
            jackalopeView.setLayoutParams(params);
        } else {
            ViewGroup.LayoutParams params = jackalopeView.getLayoutParams();
            params.height = gvrViewHeight;
            params.width = gvrViewWidth;
            setFullScreen(VideoPlayerActivity.this, false);
            jackalopeView.setLayoutParams(params);
        }
        jackalopeView.requestLayout();//TODO: Not intended here
    }

    private void setFullScreen(Activity activity, boolean isFullScreen) {

        if (isFullScreen) {
            WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            activity.getWindow().setAttributes(attrs);
        } else {
            WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
            attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.getWindow().setAttributes(attrs);
        }
    }

    private class GestureTap extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (llVideoControls.getVisibility() == View.VISIBLE) {
                hideVideoControls();
            } else {
                if (!isVrEnabled) {
                    showVideoControls();
                }
            }
            return true;
        }
    }
}
