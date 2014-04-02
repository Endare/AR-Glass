/**
 * @author Fredia Huya-Kouadio
 * @date Sep 14, 2013
 */
package com.ne0fhyklabs.freeflight.controllers;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnGenericMotionListener;
import android.view.View.OnTouchListener;
import com.ne0fhyklabs.freeflight.activities.ControlDroneActivity;
import com.ne0fhyklabs.freeflight.sensors.DeviceOrientationManager;
import com.ne0fhyklabs.freeflight.ui.HudViewController;
import com.ne0fhyklabs.freeflight.ui.hud.Sprite;

// TODO: check if possible to move HudView sprites into a subclass of this
// That subclass would be parent to the controller that are enabled in touch mode.
public abstract class Controller implements KeyEvent.Callback, OnGenericMotionListener,
        OnTouchListener {

    public enum ControllerType {
        GAMEPAD {
            @Override
            public Gamepad getImpl(final ControlDroneActivity droneControl) {
                return new Gamepad(droneControl);
            }
        },
        GOOGLE_GLASS {
            @Override
            public GoogleGlass getImpl(final ControlDroneActivity droneControl) {
                return new GoogleGlass(droneControl);
            }
        },
        GAMEPAD_AND_GLASS {
            @Override
            public GamepadGlass getImpl(final ControlDroneActivity droneControl) {
                return new GamepadGlass(droneControl);
            }
        };

        public abstract Controller getImpl(final ControlDroneActivity droneControl);
    }

    private boolean mWasDestroyed;
    protected final ControlDroneActivity mDroneControl;

    Controller(final ControlDroneActivity droneControl) {
        mDroneControl = droneControl;
    }

    public boolean init() {
        checkIfAlive();
        boolean initStatus = initImpl();

        return initStatus;
    }

    protected abstract boolean initImpl();

    public DeviceOrientationManager getDeviceOrientationManager() {
        checkIfAlive();
        return getDeviceOrientationManagerImpl();
    }

    protected abstract DeviceOrientationManager getDeviceOrientationManagerImpl();

    @Override
    public boolean onGenericMotion(View view, MotionEvent event) {
        checkIfAlive();
        return onGenericMotionImpl(view, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        checkIfAlive();
        return onKeyDownImpl(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        checkIfAlive();
        return onKeyUpImpl(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        checkIfAlive();
        return onKeyLongPressImpl(keyCode, event);
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int count, KeyEvent event) {
        checkIfAlive();
        return false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        checkIfAlive();
        return onTouchImpl(view, event);
    }

    protected abstract boolean onKeyDownImpl(int keyCode, KeyEvent event);

    protected boolean onKeyLongPressImpl(int keyCode, KeyEvent event){
        return false;
    }

    protected abstract boolean onKeyUpImpl(int keyCode, KeyEvent event);

    protected abstract boolean onGenericMotionImpl(View view, MotionEvent event);

    protected abstract boolean onTouchImpl(View view, MotionEvent event);

    public void resume() {
        checkIfAlive();
        resumeImpl();
    }

    protected abstract void resumeImpl();

    public void pause() {
        checkIfAlive();
        pauseImpl();
    }

    protected abstract void pauseImpl();

    public void destroy() {
        checkIfAlive();

        destroyImpl();
        mWasDestroyed = true;
    }

    protected abstract void destroyImpl();

    protected void checkIfAlive() {
        if (mWasDestroyed)
            throw new IllegalStateException("Can't reuse controller after it has been destroyed.");
    }
}