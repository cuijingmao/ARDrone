package com.parrot.freeflight.activities.base;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parrot.freeflight.R;
import com.parrot.freeflight.receivers.MediaStorageReceiver;
import com.parrot.freeflight.receivers.MediaStorageReceiverDelegate;
import com.parrot.freeflight.ui.StatusBar;

@SuppressLint("Registered")
// There is no need to register this activity in the manifest as this is a base activity for others.
public class DashboardActivityBase extends ParrotActivity 
implements OnClickListener,
MediaStorageReceiverDelegate
{
	protected static final String TAG = "DashboardActivity";
	
	public enum EPhotoVideoState
	{
	    UNKNOWN,
	    READY,
	    NO_MEDIA,
	    NO_SDCARD
	}
	
	private StatusBar header = null;
	
	private CheckedTextView btnPicDemo;
	private CheckedTextView btnRealPath;
	private CheckedTextView btnPhotosVideos;
	private CheckedTextView btnPath;
	private CheckedTextView btnTask;
	private CheckedTextView btnBall;

    private AlertDialog alertDialog;

    private MediaStorageReceiver externalStorageStateReceiver;
    
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.dashboard_screen);
		// added by cui
		Point size = new Point();
		getWindowManager().getDefaultDisplay().getSize(size);
		int screenWidth = size.x;
		int screenHeight = size.y;

		btnPicDemo = (CheckedTextView) findViewById(R.id.btnPicDemo);
		btnRealPath = (CheckedTextView) findViewById(R.id.btnRealPath);
		btnPhotosVideos = (CheckedTextView) findViewById(R.id.btnPhotosVideos);
		btnPath = (CheckedTextView) findViewById(R.id.btnPath);
		btnTask = (CheckedTextView) findViewById(R.id.btnTask);
		btnBall = (CheckedTextView) findViewById(R.id.btnBall);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(screenWidth /3, (screenHeight-28)/2);
		btnPicDemo.setLayoutParams(layoutParams);
		btnRealPath.setLayoutParams(layoutParams);
		btnPhotosVideos.setLayoutParams(layoutParams);
		btnPath.setLayoutParams(layoutParams);
		btnTask.setLayoutParams(layoutParams);
		btnBall.setLayoutParams(layoutParams);
		// added by cui
		View headerView = findViewById(R.id.header_preferences);

		header = new StatusBar(this, headerView);
				
		initUI();
		initListeners();
		initBroadcastReceivers();
	}
	

	private void initBroadcastReceivers()
    {
       externalStorageStateReceiver = new MediaStorageReceiver(this);
    }


    private void initUI() 
	{
		btnPicDemo     = (CheckedTextView) findViewById(R.id.btnPicDemo);
		btnRealPath        = (CheckedTextView) findViewById(R.id.btnRealPath);
		btnPhotosVideos   = (CheckedTextView) findViewById(R.id.btnPhotosVideos);
		btnPath = (CheckedTextView) findViewById(R.id.btnPath);
		btnTask    = (CheckedTextView) findViewById(R.id.btnTask);
		btnBall   = (CheckedTextView) findViewById(R.id.btnBall);
	}


	private void initListeners()
	{
		btnPicDemo.setOnClickListener(this);
		btnRealPath.setOnClickListener(this);
		btnPhotosVideos.setOnClickListener(this);
		btnPath.setOnClickListener(this);
		btnTask.setOnClickListener(this);
		btnBall.setOnClickListener(this);
	}

	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	
	@Override
	protected void onPause()
	{
		super.onPause();
		header.stopUpdating();

        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        
        externalStorageStateReceiver.unregisterFromEvents(this);
	}

	
	@Override
	protected void onResume()
	{
		super.onResume();
		header.startUpdating();
		requestUpdateButtonsState();
		
		externalStorageStateReceiver.registerForEvents(this);
	}

	
	public void requestUpdateButtonsState()
	{
		if (Looper.myLooper() == null)
			throw new IllegalStateException("Should be called from UI thread");

		btnPicDemo.setChecked(isFreeFlightEnabled());
		btnRealPath.setChecked(isAcademyEnabled());
		btnPhotosVideos.setChecked(getPhotoVideoState().equals(EPhotoVideoState.READY));
		btnPath.setChecked(isFirmwareUpdateEnabled());
		btnTask.setChecked(isParrotGamesEnabled());
		btnBall.setChecked(isGuestSpaceEnabled());
	}

	
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.btnPicDemo:
				// Open freeflight
			//	if (!isFreeFlightEnabled() || !onStartPicDemo())
				if ( !onStartPicDemo())
				{
					showErrorMessageForTime(v, getString(R.string.wifi_not_available_please_connect_device_to_drone), 2000);
				}

				break;
			case R.id.btnRealPath:
				//commented by cui
//				// Open academy
//				if (!isAcademyEnabled() || !onStartAcademy())
//				{
//					showErrorMessageForTime(v, getString(R.string.internet_connection_not_available), 2000);
//				}
				//commented by cui
				//added by cui
			if (!isFreeFlightEnabled() || !onStartRealPath())
				{
			    	showErrorMessageForTime(v, getString(R.string.wifi_not_available_please_connect_device_to_drone), 2000);
				}
				//added by cui

				break;
			case R.id.btnPhotosVideos:
				// Open photos/videos
			    EPhotoVideoState state = getPhotoVideoState();
			    switch (state) {
			    case READY:
			        onStartPhotosVideos();
			        break;
			    case NO_MEDIA:
			        showErrorMessageForTime(v, getString(R.string.there_is_no_flight_photos_or_videos_saved_in_your_phone), 2000);
			        break;
			    case NO_SDCARD:
			        showErrorMessageForTime(v, getString(R.string.NO_SD_CARD_INSERTED), 2000);
			        break;
		        default:
		            Log.w(TAG, "Unknown media state " + state.name());
			        break;
			    }
				break;
			case R.id.btnPath:
				// Open drone update
				//commented by cui
//				if (!isFirmwareUpdateEnabled() || !onStartFirmwareUpdate())
//				{
//					showErrorMessageForTime(v, getString(R.string.your_ar_drone_is_up_to_date), 2000);
//				}
				//commented by cui
				//added by cui
				if (!isFreeFlightEnabled() || !onStartPath())
				{
					showErrorMessageForTime(v, "wifi连接错误!!!", 2000);
				}
				break;
			case R.id.btnTask:
				// Open Parrot Games
				if (!isParrotGamesEnabled() || !onStartTask())
				{
					showErrorMessageForTime(v, getString(R.string.we_have_no_games_yet), 2000);
				}
				break;
			case R.id.btnBall:
				// Open get your drone
				if (!isGuestSpaceEnabled() || !onStartBall())
				{
					showErrorMessageForTime(v, getString(R.string.not_implemented_yet), 2000);
				}
				break;
		}
	}

	protected boolean isAcademyEnabled()
	{
		return false;
	}

	protected boolean isFreeFlightEnabled()
	{
		return false;
	}

	
	protected EPhotoVideoState getPhotoVideoState()
	{
	    return EPhotoVideoState.NO_SDCARD;
	}
	
	
	@Deprecated
	protected boolean isPhotoVideosEnabled()
	{
		return false;
	}

	protected boolean isARDroneUpdateEnabled()
	{
		return false;
	}

	protected boolean isParrotGamesEnabled()
	{
		return false;
	}

	protected boolean isGuestSpaceEnabled()
	{
		return false;
	}

	protected boolean isFirmwareUpdateEnabled()
	{
		return false;
	}

	protected boolean onStartPicDemo()
	{
		return false;
	}

	protected boolean onStartRealPath()
	{
		return false;
	}

	protected boolean onStartPhotosVideos()
	{
		return false;
	}

	protected boolean onStartPath()
	{
		return false;
	}

	protected boolean onStartTask()
	{
		return false;
	}

	protected boolean onStartBall()
	{
		return false;
	}

	private void showErrorMessageForTime(View v, String string, int i)
	{
		final View oldView = v;
		final ViewGroup parent = (ViewGroup) v.getParent();
		final int index = parent.indexOfChild(v);

		TextView buttonNok = (TextView) v.getTag();

		if (buttonNok == null)
		{
			buttonNok = (TextView) inflateView(R.layout.dashboard_button_nok, parent, false);
			buttonNok.setLayoutParams(v.getLayoutParams());
			v.setTag(buttonNok);
		}

		buttonNok.setText(string);

		parent.removeView(v);
		parent.addView(buttonNok, index);

		Runnable runnable = new Runnable()
		{
			public void run()
			{
				parent.removeViewAt(index);
				parent.addView(oldView, index);
			}
		};

		parent.postDelayed(runnable, i);
	}


    protected void showAlertDialog(String title, String message, final Runnable actionOnDismiss)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialog = alertDialogBuilder.setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton(getString(android.R.string.ok), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.dismiss();
                        if (actionOnDismiss != null) {
                            actionOnDismiss.run();
                        }
                    }
                }).create();
        
        alertDialog.show();
    }


    @Override
    public void onMediaStorageMounted()
    {
        // Left unimplemented
    }


    @Override
    public void onMediaStorageUnmounted()
    {
       // Left unimplemented
    }


    @Override
    public void onMediaEject()
    {   
        // Left unimplemented
    }

}
