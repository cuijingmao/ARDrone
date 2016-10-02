/*
 * JoystickFactory
 *
 *  Created on: May 26, 2011
 *      Author: Dmytro Baryskyy
 */
package com.parrot.freeflight.activities.game;


import android.content.Context;

import com.parrot.freeflight.ui.hud.Sprite.Align;

public class GameJoystickFactory
{
	public static GameJoystickBase createAnalogueJoystick(Context context, boolean absolute,
															GameJoystickListener analogueListener)
	{
		GameAnalogueJoystick joy = new GameAnalogueJoystick(context, Align.NO_ALIGN, absolute);
		joy.setOnAnalogueChangedListener(analogueListener);
		
		return joy;
	}
	
	
	public static GameJoystickBase createAcceleroJoystick(Context context,
															boolean absolute,
															GameJoystickListener acceleroListener)
	{
		GameAcceleroJoystick joy = new GameAcceleroJoystick(context, Align.NO_ALIGN, absolute);
		joy.setOnAnalogueChangedListener(acceleroListener);
		
		return joy;
	}
	
	
	public static GameJoystickBase createCombinedJoystick(Context context,
															boolean absolute,
															GameJoystickListener analogueListener,
															GameJoystickListener acceleroListener)
	{
		GameJoystickBase joy = new GameAnalogueJoystick(context, Align.NO_ALIGN, absolute);
		joy.setOnAnalogueChangedListener(analogueListener);
		
		return joy;
	}
}