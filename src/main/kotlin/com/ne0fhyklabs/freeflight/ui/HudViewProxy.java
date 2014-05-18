package main.kotlin.com.ne0fhyklabs.freeflight.ui;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ne0fhyklabs.androhud.widget.SimplePitchRoll;
import com.ne0fhyklabs.freeflight.R;
import com.ne0fhyklabs.freeflight.activities.ControlDroneActivity;

public class HudViewProxy {
	
	private final static String TAG = "HudViewProxy";
	
	private ControlDroneActivity activity;
	private SimplePitchRoll mHudPitchRoll;
	private TextView mHudBatteryInfo;
	private ImageView mHudWifiInfo;
	private TextView mHudStatusInfo;
	private TextView mHudRecInfo;
	private TextView mHudUsbInfo;

    /**
     * Used to track the last time the pitch and roll value were updated.
     */
    private long mLastPitchRollUpdate = System.currentTimeMillis();

    /**
     * Update rate for the pitch and roll value.
     */
    private double UPDATE_RATE = 24;

    /**
     * Delay between successive updates in order to attain the desired update rate.
     */
    private int UPDATE_DELAY = 1000;

	public HudViewProxy(ControlDroneActivity activity) {
		super();
		this.activity = activity;
		this.mHudPitchRoll = (SimplePitchRoll) this.view(R.id.hud_pitch_roll_widget);
		this.mHudBatteryInfo = (TextView) this.view(R.id.hud_battery_info);
		this.mHudWifiInfo = (ImageView) this.view(R.id.hud_wifi_info);
		this.mHudStatusInfo = (TextView) this.view(R.id.hud_status_info);
		this.mHudRecInfo = (TextView) this.view(R.id.hud_rec_info);
		this.mHudUsbInfo = (TextView) this.view(R.id.hud_usb_info);
	}
		    
    private View view(Integer id) {
    	View view = activity.findViewById(id);
    	if (view == null) {
    		throw new IllegalArgumentException("Given id could not be found in current layout!");
    	}
    	return view;
    }

    /**
     * Enable/Disable video recording status
     * TODO: maybe propagate to the control
     */
    public void enableRecording(boolean enable){
        mHudRecInfo.setEnabled(enable);
    }
    
    /**
     * Enable/Disable video recording from the drone's camera.
     * @param inProgress true to enable recording, false to disable
     */
    public void setRecording(boolean inProgress) {
        if (inProgress) {
            mHudRecInfo.setTextColor(Color.RED);
            mHudRecInfo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.activated_btn_record, 0, 0, 0);
        }
        else {
            mHudRecInfo.setTextColor(Color.WHITE);
            mHudRecInfo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.btn_record, 0, 0, 0);
        }
    }
    
    /**
     * Enable/Disable usb indicator.
     * @param usbActivate true to enable usb indicator.
     */
    public void setUsbIndicatorEnabled(boolean usbActivate) {
        mHudUsbInfo.setVisibility(usbActivate ? View.VISIBLE : View.INVISIBLE);
    }
    
    /**
     * Updates remaining video time that can be stored on the usb stick.
     * @param seconds amount of time remaining in seconds
     */
    public void setUsbRemainingTime(int seconds) {
    	String remainingTime;
    	if(seconds > 3600) {
    		remainingTime = ">1h";
    	}
    	else if(seconds > 2700) {
    		remainingTime = "45m";
    	}
    	else if(seconds > 1800) {
    		remainingTime = "30m";
    	}
    	else if(seconds > 900) {
    		remainingTime = "15m";
    	}
    	else if(seconds > 600) {
    		remainingTime = "10m";
    	}
    	else if(seconds > 300) {
    		remainingTime = "5m";
    	}
    	else {
    		int remainingMinutes = seconds / 60;
    		int remainingSeconds = seconds % 60;
    		if(remainingSeconds == 0 && remainingMinutes == 0) {
    			remainingTime = "FULL";
    		}
    		else {
    			remainingTime = remainingMinutes + ":";
    			if(remainingSeconds >= 10) {
    				remainingTime += remainingSeconds;
    			}
    			else {
    				remainingTime += "0" + remainingSeconds;
    			}
    		}
    	}
    	mHudUsbInfo.setText(remainingTime);
    	mHudUsbInfo.setTextColor((seconds < 30 ? Color.rgb(170, 0, 0) : Color.WHITE));
    }

    /**
     * Updates the status info based on the flying state of the drone.
     */
    public void setIsFlying(boolean flying){
        mHudStatusInfo.setText((flying ? R.string.status_flying : R.string.status_landed));
    }

    /**
     * Updates the drone's battery level
     */
    public void setBatteryValue(int percent){
        if(percent > 100 || percent < 0){
            Log.w(TAG, "Can't set battery value. Invalid value " + percent);
        }
        else{
        	int imgNum = (int) Math.min(Math.max(0.0, Math.round(percent / 100.0f * 3.0f)), 3.0);
            mHudBatteryInfo.setText(percent + "%");
            Drawable[] compoundDrawables = mHudBatteryInfo.getCompoundDrawables();
            if(compoundDrawables != null && compoundDrawables[0] != null) {
                compoundDrawables[0].setLevel(imgNum);
            }
        }
    }

    /**
     * Updates the drone's wifi connection level
     */
    public void setWifiValue(int wifiLevel){
        mHudWifiInfo.setImageLevel(wifiLevel);
    }

    /**
     * Sets the max value for the roll angle.
     * @param rollMax max roll value in degrees
     */
    public void setMaxRoll(float rollMax){
        mHudPitchRoll.setRollMax(rollMax);
    }
    
    /**
     * Sets the min value for the roll angle.
     * @param rollMin min roll value in degrees
     */
    public void setMinRoll(float rollMin){
        mHudPitchRoll.setRollMin(rollMin);
    }

    /**
     * Sets the max value for the pitch angle.
     * @param pitchMax max pitch value in degrees
     */
    public void setPitchMax(float pitchMax){
        mHudPitchRoll.setPitchMax(pitchMax);
    }

    /**
     * Sets the min value for the pitch angle.
     * @param pitchMin min pitch value in degrees
     */
    public void setPitchMin(float pitchMin){
        mHudPitchRoll.setPitchMin(pitchMin);
    }

    /**
     * Sets the pitch and roll angle. Both values are between -1 and 1,
     * so they needs to be converted back to degrees.
     * @param pitch pitch value
     * @param roll roll value
     */
    public void setPitchRoll(float pitch, float roll) {
        float rollMax = mHudPitchRoll.getRollMax();
        float pitchMax = mHudPitchRoll.getPitchMax();

        //Limit the rate of updates
        long currentTime = System.currentTimeMillis();
        long timeSinceLastUpdate = currentTime - mLastPitchRollUpdate;
        if (timeSinceLastUpdate > UPDATE_DELAY) {
            float actualRoll = roll * rollMax;
            float actualPitch = pitch * pitchMax;
            mHudPitchRoll.setPitchRoll(actualPitch, actualRoll);
            mLastPitchRollUpdate = currentTime;
        }
    }

    /**
     * Used to reset the hud pitch and roll crosshair
     */
    public void resetPitchRoll() {
    	mHudPitchRoll.setPitchRoll(0f, 0f);
    }
    
}
