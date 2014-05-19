package main.kotlin.com.ne0fhyklabs.freeflight.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.glass.custom.widget.SliderView;
import com.ne0fhyklabs.freeflight.R;

public class GlassVideoPlayerActivity extends Activity {
	
	private final static String TAG = "GlassVideoPlayerActivity";
    public final static String EXTRA_VIDEO_URI = "main.kotlin.com.ne0fhyklabs.freeflight.activities.EXTRA_VIDEO_URI";

    private final static long INFO_VISIBILITY_DURATION = 2000L; //milliseconds
    private final static long INFO_FADEOUT_DURATION = 300L; // milliseconds
    
    /**
     * Handle to the video player.
     */
    private VideoView mVideoView;
    
    /**
     * Handle to the video player pause icon.
     */
    private ImageView mPauseImg;
    
    /**
     * Handle to the video player progress bar
     */
    private SliderView mVideoProgress;
    
    /**
     * Handle to the video player time display.
     */
    private TextView mVideoTimer;
    
    /**
     * Handle to glass gesture detector.
     */
    private GestureDetector mGlassDetector;
    
    /**
     * This callback updates the video time.
     */
    private Runnable mVideoTimeUpdater = new Runnable() {
		@Override
		public void run() {
            mCallbacksRunner.removeCallbacks(mVideoTimeUpdater);
            int currentPosition = mVideoView.getCurrentPosition() / 1000;
            int duration = mVideoView.getDuration() / 1000;
            mVideoTimer.setText(currentPosition + " / " + duration);
            mCallbacksRunner.postDelayed(mVideoTimeUpdater, 500);
		}
    };
    private Runnable mInfoVisibilityUpdater = new Runnable() {
		@Override
		public void run() {
            mVideoInfoContainer.animate().alpha(0f).setDuration(INFO_FADEOUT_DURATION);
        }
    };
    
    /**
     * This view contains the video progress bar, and time view.
     */
    private View mVideoInfoContainer;
    
    /**
     * This is used to run the callbacks used in this activity.
     */
    private Handler mCallbacksRunner = new Handler();

    @Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_glass_video_player);
        Intent intent = getIntent();
        handleIntent(intent);
		//Initialise mVideoView
		mVideoView = (VideoView) findViewById(R.id.glass_video_player);
		mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				finish();
			}
		});
		mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
	            Log.e(TAG, "Unable to play video (error type: " + what + ", error code: " +  extra + ")");
	            finish();
				return true;
			}
		});
		mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {	
			@Override
			public void onPrepared(MediaPlayer mp) {
				playVideo();
			}
		});
		//Locate the UI elements
		mPauseImg = (ImageView) findViewById(R.id.glass_video_pause);
		mVideoProgress = (SliderView) findViewById(R.id.glass_video_progress);
		mVideoTimer = (TextView) findViewById(R.id.glass_video_time);
		mVideoInfoContainer = findViewById(R.id.glass_video_info);
		//Setup the gesture detector
		mGlassDetector = new GestureDetector(getApplicationContext());
		mGlassDetector.setBaseListener(new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				if(gesture == Gesture.TAP){ 
                    togglePlayPause();
					return true;
				}
				return false;
			}
		});
		mGlassDetector.setScrollListener(new GestureDetector.ScrollListener() {		
			@Override
			public boolean onScroll(float displacement, float delta, float velocity) {
				showVideoInfo();
				int updatedPosition = mVideoView.getCurrentPosition() + (((int) delta) * 10);
	            if (updatedPosition >= 0 && updatedPosition <= mVideoView.getDuration()) {
	                mVideoView.seekTo(updatedPosition);
	                enableInfoUpdate(mVideoView.isPlaying());
	            }
				return true;
			}
		});
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleIntent(intent);
	}

	@Override
	protected void onPause() {
		super.onPause();
        stopVideo();
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.playSoundEffect(Sounds.DISMISSED);
	}

	@Override
	protected void onStop() {
		super.onStop();
		finish();
	}

	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		return mGlassDetector.onMotionEvent(event) || super.onGenericMotionEvent(event);
	}
    
	private void handleIntent(Intent intent) {
        //Retrieve the video uri
        String videoUri = intent.getStringExtra(EXTRA_VIDEO_URI);
        if (videoUri == null) {
            Log.e(TAG, "Intent is missing video uri argument.");
            Toast.makeText(getApplicationContext(), "Invalid video!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            mVideoView.setVideoURI(Uri.parse(videoUri));
        }
    }

	private void togglePlayPause() {
		if (mVideoView.isPlaying()) 
			pauseVideo();
		else
			playVideo();
	}
	
	private void pauseVideo() {
        showVideoInfo();
        enableInfoUpdate(false);
        mVideoView.pause();
        mPauseImg.setVisibility(View.VISIBLE);
    }

    private void playVideo() {
        mPauseImg.setVisibility(View.INVISIBLE);
        mVideoView.start();
        enableInfoUpdate(true);
        showVideoInfo();
    }

    private void stopVideo() {
        mPauseImg.setVisibility(View.INVISIBLE);
        enableInfoUpdate(false);
        mVideoView.stopPlayback();
    }
    
    private void showVideoInfo() {
        mCallbacksRunner.removeCallbacks(mInfoVisibilityUpdater);
        mVideoInfoContainer.setAlpha(1f);
        mCallbacksRunner.postDelayed(mInfoVisibilityUpdater, INFO_VISIBILITY_DURATION);
    }
    
    private void enableInfoUpdate(boolean enable) {
        mVideoTimeUpdater.run();

        int videoDuration = mVideoView.getDuration();
        int videoPosition = mVideoView.getCurrentPosition();
        int progressDuration = videoDuration - videoPosition;
        if (progressDuration >= 0 ) {
            mVideoProgress.stopProgress(false);
            float progressStart = ((float) videoPosition) / ((float) videoDuration);
            mVideoProgress.setManualProgress(progressStart);
            if (enable) {
                mVideoProgress.startProgress(progressDuration);
            } else {
                mCallbacksRunner.removeCallbacks(mVideoTimeUpdater);
            }
        }

    }
}
