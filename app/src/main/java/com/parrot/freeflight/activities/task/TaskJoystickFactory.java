/*
 * JoystickFactory
 *
 *  Created on: May 26, 2011
 *      Author: Dmytro Baryskyy
 */
package com.parrot.freeflight.activities.task;


import android.content.Context;

import com.parrot.freeflight.ui.hud.Sprite.Align;

public class TaskJoystickFactory
{
	public static TaskJoystickBase createAnalogueJoystick(Context context, boolean absolute,
														  TaskJoystickListener analogueListener)
	{
		TaskAnalogueJoystick joy = new TaskAnalogueJoystick(context, Align.NO_ALIGN, absolute);
		joy.setOnAnalogueChangedListener(analogueListener);

		return joy;
	}


	public static TaskJoystickBase createAcceleroJoystick(Context context,
														  boolean absolute,
														  TaskJoystickListener acceleroListener)
	{
		TaskAcceleroJoystick joy = new TaskAcceleroJoystick(context, Align.NO_ALIGN, absolute);
		joy.setOnAnalogueChangedListener(acceleroListener);

		return joy;
	}


	public static TaskJoystickBase createCombinedJoystick(Context context,
														  boolean absolute,
														  TaskJoystickListener analogueListener,
														  TaskJoystickListener acceleroListener)
	{
		TaskJoystickBase joy = new TaskAnalogueJoystick(context, Align.NO_ALIGN, absolute);
		joy.setOnAnalogueChangedListener(analogueListener);
		
		return joy;
	}
}