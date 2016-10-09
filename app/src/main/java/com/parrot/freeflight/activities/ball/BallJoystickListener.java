/*
 * JoystickListener
 *
 *  Created on: May 26, 2011
 *      Author: Dmytro Baryskyy
 */
package com.parrot.freeflight.activities.ball;

public abstract class BallJoystickListener
{
	public abstract void onChanged(BallJoystickBase joystick, float x, float y);
	public abstract void onPressed(BallJoystickBase joystick);
	public abstract void onReleased(BallJoystickBase joystick);
}
