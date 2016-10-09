/*
 * JoystickListener
 *
 *  Created on: May 26, 2011
 *      Author: Dmytro Baryskyy
 */
package com.parrot.freeflight.activities.task;

public abstract class TaskJoystickListener
{
	public abstract void onChanged(TaskJoystickBase joystick, float x, float y);
	public abstract void onPressed(TaskJoystickBase joystick);
	public abstract void onReleased(TaskJoystickBase joystick);
}
