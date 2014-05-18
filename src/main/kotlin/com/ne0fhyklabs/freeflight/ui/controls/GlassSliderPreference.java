package main.kotlin.com.ne0fhyklabs.freeflight.ui.controls;

import main.kotlin.com.ne0fhyklabs.freeflight.ui.controls.GlassSliderDialog.GlassSliderDialogListener;

import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.glass.custom.widget.SliderView;
import com.ne0fhyklabs.freeflight.R;
import com.ne0fhyklabs.freeflight.fragments.GlassPreferenceFragment;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

public class GlassSliderPreference extends Preference implements GlassSliderDialogListener {
	
	private final static String TAG = "GlassSliderPreference";
	private final static String ANDROIDNS = "http://schemas.android.com/apk/res/android";
	private final static String APPLICATIONNS = "http://robobun\ny.com";
	private final static int DEFAULT_VALUE = 50;
	
	private int mMaxValue      = 100;
	private int mMinValue      = 0;
	private int mCurrentValue;
	private String mUnit  = "";

	public GlassSliderPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mMaxValue = attrs.getAttributeIntValue(ANDROIDNS, "max", 100);
		mMinValue = attrs.getAttributeIntValue(APPLICATIONNS, "min", 0);
		mCurrentValue = 0;
		mUnit = getAttributeStringValue(attrs, APPLICATIONNS, "unit", "");
	}
	
	private String getAttributeStringValue(AttributeSet attrs, String namespace, String name, String defaultValue) {
		String value = attrs.getAttributeValue(namespace, name);
		return (value != null ? value : defaultValue);
	}
	
	@Override
	protected Object onGetDefaultValue(TypedArray ta, int index) {
		return ta.getInt(index, DEFAULT_VALUE);
	}
	
	public void setValue(int value) {
		mCurrentValue = value;
		persistInt(value);
	}
	
	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		if(restoreValue) {
			mCurrentValue = getPersistedInt(mCurrentValue);
		}
		else {
			int temporary = 0;
			try {
				temporary = (Integer) defaultValue;
			}
			catch(Exception e) {
				Log.e(TAG, "Invalid default value " + defaultValue);
				temporary = 0;
			}
			mCurrentValue = temporary;
			persistInt(temporary);
		}
	}
	
	public boolean persistAndNotify(int value) {
		boolean retValue = super.persistInt(value);
		callChangeListener(value);
		return retValue;
	}
	
	public void launchSliderDialog(FragmentManager fm) {
		//Pass some of the parameters in the bundle
		Bundle args= new Bundle();
		args.putInt("mCurrentValue", mCurrentValue);
		args.putInt("mMinValue", mMinValue);
		args.putInt("mMaxValue", mMaxValue);
		args.putString("mUnit", mUnit);
		//Create the new dialog
		GlassSliderDialog dialog = new GlassSliderDialog();
		dialog.setListener(this);
		dialog.setArguments(args);
		dialog.show(fm, "Slider preference dialog");
	}

}
