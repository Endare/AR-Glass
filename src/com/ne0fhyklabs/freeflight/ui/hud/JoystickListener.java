/*
 * JoystickListener
 *
 *  Created on: May 26, 2011
 *      Author: Dmytro Baryskyy
 */

package com.ne0fhyklabs.freeflight.ui.hud;

public abstract class JoystickListener 
{
	public abstract void onChanged(JoystickBase joystick, float x, float y);
	public abstract void onPressed(JoystickBase joystick);
	public abstract void onReleased(JoystickBase joystick);
}
