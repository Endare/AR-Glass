package main.kotlin.com.ne0fhyklabs.freeflight.ui.controls;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.glass.custom.widget.SliderView;
import com.ne0fhyklabs.freeflight.R;
import com.ne0fhyklabs.freeflight.fragments.GlassPreferenceFragment;

import android.app.DialogFragment;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

public class GlassSliderDialog extends DialogFragment {
	
	//Transferred fields from the calling class
	private int mCurrentValue;
	private int mMinValue;
	private int mMaxValue;
	private String mUnit;
	
	private int mStartValue;
	private SliderView mSliderBar;
	private TextView mSliderValue;
	private GestureDetector mGlassDetector;
	private GlassSliderDialogListener listener;

	private GestureDetector createGestureDetector(Context context) {
		final AudioManager audioMan = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		GestureDetector gestureDetector = new GestureDetector(context);
		
		gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
			
			@Override
			public boolean onGesture(Gesture gesture) {
				if(gesture == Gesture.TAP) {
					audioMan.playSoundEffect(SoundEffectConstants.CLICK);
					
					//Save the current value
					if(mCurrentValue != mStartValue && listener != null){
						listener.persistAndNotify(mCurrentValue);
					}
					
					//Close the preference dialog
					getDialog().dismiss();
					listener = null;
					return true;
					
				}
				return false;
			}
		});
		
		gestureDetector.setScrollListener(new GestureDetector.ScrollListener() {
			
			@Override
			public boolean onScroll(float displacement, float delta, float velocity) {
				if(mSliderBar != null) {
					float range = (float) mMaxValue - mMinValue;
					float update = delta * (range / 600.0f);
					mCurrentValue += (int) update;
					if(mCurrentValue > mMaxValue) {
						mCurrentValue = mMaxValue;
					}
					else if(mCurrentValue < mMinValue) {
						mCurrentValue = mMinValue;
					}
					setSliderProgress();
					mSliderValue.setText(mCurrentValue + " " + mUnit);
				}
				return true;
			}
		});
		return gestureDetector;
	};
	
	private void setSliderProgress() {
		float updateFraction = ((float) (mCurrentValue - mMinValue)) / ((float) mMaxValue - mMinValue);
		mSliderBar.setManualProgress(updateFraction);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		mGlassDetector = createGestureDetector(getActivity().getApplicationContext());
		
		//Fetch the parameters
		Bundle bundle = getArguments();
		mCurrentValue = bundle.getInt("mCurrentValue");
		mMaxValue = bundle.getInt("mMaxValue");
		mMinValue = bundle.getInt("mMinValue");
		mUnit = bundle.getString("mUnit");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_preference_glass_slider, container, false);
		mSliderValue = (TextView) view.findViewById(R.id.glass_slider_value);
		mSliderValue.setText(mCurrentValue + " " + mUnit);
		mSliderBar = (SliderView) view.findViewById(R.id.glass_slider_bar);
		return view;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		GlassPreferenceFragment.updateWindowCallback(getDialog().getWindow(), mGlassDetector);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		GlassPreferenceFragment.restoreWindowCallback(getDialog().getWindow());
	}
	
	@Override
	public void onResume()  {
		super.onResume();
		setSliderProgress();
	}

	public void setListener(GlassSliderDialogListener listener) {
		this.listener = listener;
	}
	
	public interface GlassSliderDialogListener {
        boolean persistAndNotify(int value);
    }
}
