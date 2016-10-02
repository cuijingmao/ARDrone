/*
 * JoystickListener
 *
 *  Created on: May 26, 2011
 *      Author: Dmytro Baryskyy
 */
package com.parrot.freeflight.activities.game;

public abstract class GameJoystickListener
{
	public abstract void onChanged(GameJoystickBase joystick, float x, float y);
	public abstract void onPressed(GameJoystickBase joystick);
	public abstract void onReleased(GameJoystickBase joystick);
}
