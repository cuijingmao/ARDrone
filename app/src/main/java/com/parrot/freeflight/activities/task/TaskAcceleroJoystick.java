/*
 * AcceleroJoystick
 *
 *  Created on: May 26, 2011
 *      Author: Dmytro Baryskyy
 */

package com.parrot.freeflight.activities.task;

import android.content.Context;

import com.parrot.freeflight.R;

public class TaskAcceleroJoystick
	extends TaskJoystickBase
{
	public TaskAcceleroJoystick(Context context, Align align, boolean absolute)
	{
		super(context, align, absolute);
	}
	
	
	@Override
	protected int getBackgroundDrawableId() 
	{
		// Transparent background
		return R.drawable.accelero_background;
	}


	@Override
	protected int getTumbDrawableId() 
	{
		return R.drawable.joystick_gyro;
	}

	
	@Override
	protected void onActionMove(float x, float y) 
	{
		// Ignore action move.
	}
}
