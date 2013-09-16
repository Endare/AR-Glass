/**
 * @author Fredia Huya-Kouadio
 * @date Sep 15, 2013
 */
package com.parrot.freeflight.controllers;

import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.parrot.freeflight.activities.ControlDroneActivity;
import com.parrot.freeflight.sensors.DeviceOrientationManager;
import com.parrot.freeflight.ui.hud.Sprite;

public class Gamepad extends Controller {

    static final String TAG = Gamepad.class.getName();

    private InputDevice mLastInputDevice;

    Gamepad(final ControlDroneActivity droneControl) {
        super(droneControl);
    }

    private boolean isSourceValid(int inputDeviceSource) {
        return (inputDeviceSource & InputDevice.SOURCE_CLASS_JOYSTICK) != 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.parrot.freeflight.controllers.Controller#initImpl()
     */
    @Override
    protected boolean initImpl() {
        // Check to see if there's a device of class JOYSTICK
        int[] deviceIds = InputDevice.getDeviceIds();
        for ( int deviceId : deviceIds ) {
            InputDevice device = InputDevice.getDevice(deviceId);
            if ( isSourceValid(device.getSources()) )
                return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.parrot.freeflight.controllers.Controller#getSpritesImpl()
     */
    @Override
    protected Sprite[] getSpritesImpl() {
        return new Sprite[0];
    }

    /*
     * (non-Javadoc)
     *
     * @see com.parrot.freeflight.controllers.Controller#getDeviceOrientationManagerImpl()
     */
    @Override
    protected DeviceOrientationManager getDeviceOrientationManagerImpl() {
        // TODO: implement a stub device orientation manager with isMagnetoAvailable returning true.
        // Will be used to enable absolute control mode with gamepad.
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.parrot.freeflight.controllers.Controller#onEventImpl(android.view.View,
     * android.view.MotionEvent)
     */
    @Override
    protected boolean onGenericMotionImpl(View view, MotionEvent event) {
        // Check that the event came from a joystick
        if ( isSourceValid(event.getSource()) && event.getAction() == MotionEvent.ACTION_MOVE ) {
            // Cache the most recently obtained device information.
            // The device information may change over time but it can be somewhat expensive to query
            if ( mLastInputDevice == null || mLastInputDevice.getId() != event.getDeviceId() ) {
                mLastInputDevice = event.getDevice();
                // It's possible for the device id to be invalid, in which case, getDevice() return
                // null
                if ( mLastInputDevice == null )
                    return false;
            }

            // Process all historical movement samples in the batch
            final int historySize = event.getHistorySize();
            for ( int i = 0; i < historySize; i++ ) {
                processJoystickInput(event, i);
            }

            // Process the current movement sample in the batch.
            processJoystickInput(event, -1);
            return true;
        }

        return false;
    }

    @Override
    protected boolean onKeyDownImpl(int keyCode, KeyEvent event) {
        // TODO: complete
        return false;
    }

    @Override
    protected boolean onKeyUpImpl(int keyCode, KeyEvent event) {
        // TODO: complete
        return false;
    }

    @Override
    protected boolean onTouchImpl(View view, MotionEvent event) {
        return false;
    }

    private void processJoystickInput(MotionEvent event, int historyPos) {
        // Get joystick position
        // Many game pads with two joysticks report the position of the second joystick
        // using the Z, and RZ axes so we also handle those.
        // TODO: allow configuration of joystick
        float x = getCenteredAxis(event, mLastInputDevice, MotionEvent.AXIS_X, historyPos);
        if ( x == 0 )
            x = getCenteredAxis(event, mLastInputDevice, MotionEvent.AXIS_HAT_X, historyPos);
        if ( x == 0 )
            x = getCenteredAxis(event, mLastInputDevice, MotionEvent.AXIS_Z, historyPos);

        float y = getCenteredAxis(event, mLastInputDevice, MotionEvent.AXIS_Y, historyPos);
        if ( y == 0 )
            y = getCenteredAxis(event, mLastInputDevice, MotionEvent.AXIS_HAT_Y, historyPos);
        if ( y == 0 )
            y = getCenteredAxis(event, mLastInputDevice, MotionEvent.AXIS_RZ, historyPos);
    }

    private static float getCenteredAxis(MotionEvent event, InputDevice device, int axis,
            int historyPos) {
        final InputDevice.MotionRange range = device.getMotionRange(axis, event.getSource());
        if ( range != null ) {
            final float flat = range.getFlat();
            final float value = historyPos < 0 ? event.getAxisValue(axis) : event
                    .getHistoricalAxisValue(axis, historyPos);

            // Ignore axis values that are within the 'flat' region of the joystick axis center.
            // A joystick at rest does not always report an absolute position of (0,0).
            if ( Math.abs(value) > flat ) {
                return value;
            }
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.parrot.freeflight.controllers.Controller#resumeImpl()
     */
    @Override
    protected void resumeImpl() {
        // Nothing to do
    }

    /*
     * (non-Javadoc)
     *
     * @see com.parrot.freeflight.controllers.Controller#pauseImpl()
     */
    @Override
    protected void pauseImpl() {
        // Nothing to do
    }

    /*
     * (non-Javadoc)
     *
     * @see com.parrot.freeflight.controllers.Controller#destroyImpl()
     */
    @Override
    protected void destroyImpl() {
        // Nothing to do
    }
}