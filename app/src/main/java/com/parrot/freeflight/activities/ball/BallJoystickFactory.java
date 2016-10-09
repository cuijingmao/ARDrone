/*
 * JoystickFactory
 *
 *  Created on: May 26, 2011
 *      Author: Dmytro Baryskyy
 */
package com.parrot.freeflight.activities.ball;


import android.content.Context;

import com.parrot.freeflight.ui.hud.Sprite.Align;

public class BallJoystickFactory
{
	public static BallJoystickBase createAnalogueJoystick(Context context, boolean absolute,
														  BallJoystickListener analogueListener)
	{
		BallAnalogueJoystick joy = new BallAnalogueJoystick(context, Align.NO_ALIGN, absolute);
		joy.setOnAnalogueChangedListener(analogueListener);

		return joy;
	}


	public static BallJoystickBase createAcceleroJoystick(Context context,
														  boolean absolute,
														  BallJoystickListener acceleroListener)
	{
		BallAcceleroJoystick joy = new BallAcceleroJoystick(context, Align.NO_ALIGN, absolute);
		joy.setOnAnalogueChangedListener(acceleroListener);

		return joy;
	}


	public static BallJoystickBase createCombinedJoystick(Context context,
														  boolean absolute,
														 BallJoystickListener analogueListener,
														  BallJoystickListener acceleroListener)
	{
		BallJoystickBase joy = new BallAnalogueJoystick(context, Align.NO_ALIGN, absolute);
		joy.setOnAnalogueChangedListener(analogueListener);
		
		return joy;
	}
}