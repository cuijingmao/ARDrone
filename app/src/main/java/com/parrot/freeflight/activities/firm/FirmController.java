package com.parrot.freeflight.activities.firm;

import android.content.Intent;
import android.util.Log;

import com.parrot.freeflight.activities.game.GameCommand;
import com.parrot.freeflight.activities.image.ImageToCommand;
import com.parrot.freeflight.service.DroneControlService;
import com.parrot.freeflight.utils.SystemUtils;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by shisy13 on 16/8/24.
 */
public class FirmController {
    final String LOG_TAG = getClass().getSimpleName();
    FirmActivity firmActivity;
    DroneControlService controlService;

    Timer commandTimer;
    Thread controlThread;
    FirmImageToCommand firmimageToCommand;

    int ardroneStatus = 0;

    public FirmController(FirmActivity firmActivity, DroneControlService controlService){
        this.firmActivity = firmActivity;
        this.controlService = controlService;
        firmimageToCommand = new FirmImageToCommand(firmActivity);
        commandTimer = new Timer();


    }

    public void start(){
//        final float gaz_roll_mod = -0.00175f;
//        final float pitch_roll_mod = -0.002f;
        controlThread = new Thread(new Runnable() {
            @Override
            public void run() {
//                controlService.switchCamera(); //switch to the camera below
 //               controlService.triggerTakeOff();
                try {
                    Thread.sleep(3000);     // wait takeoff
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//
//                controlService.setProgressiveCommandEnabled(false);
//                controlService.setGaz(1.0f);
                try {
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                controlService.setGaz(0.0f);

                long timePre = 0;
                long timeNow = 0;

                while (ardroneStatus == 0 && !Thread.currentThread().isInterrupted()){

                    timePre = System.currentTimeMillis();

                    boolean ball = true;
                    GameCommand command = firmimageToCommand.getCommandBall();
//                    GameCommand command = imageToCommand.getCommand();

                    timeNow = System.currentTimeMillis();
                    long timeDelta = timeNow - timePre;
                    if (timeDelta < 150){
                        try {
                            Thread.sleep(150 - timeDelta);
                        } catch (Exception e) {
                        }
                    }


                    if (command.command.equals("stable")){
//                        controlService.setProgressiveCommandEnabled(false);
//                        controlService.setYaw(0.0f);
//                        controlService.setRoll(0.0f);
//                        controlService.setPitch(0.0f);
//                        controlService.setGaz(0.0f);
                    }
                    else {

                        if (ball){
//                            controlService.setProgressiveCommandEnabled(false);
//                            controlService.setProgressiveCommandCombinedYawEnabled(false);
//                            controlService.setGaz(command.gaz);
//                            controlService.setYaw(command.yaw);
//                            controlService.setRoll(0.0f);
//                            controlService.setPitch(0.0f);
                        }
                        else {
                            if (command.yaw != 0){
//                                controlService.setProgressiveCommandEnabled(true);
//                                controlService.setProgressiveCommandCombinedYawEnabled(true);
//                                controlService.setYaw(command.yaw);
//                                controlService.setPitch(command.pitch);
//                                controlService.setRoll(command.roll);
//                                controlService.setGaz(command.gaz);

//                            try {
//                                Thread.sleep(500);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
                            }
                            else {
//                                controlService.setGaz(command.gaz);
//                                controlService.setYaw(command.yaw);
//                                controlService.setPitch(command.pitch);
//                                controlService.setRoll(command.roll);
//                                controlService.setProgressiveCommandEnabled(true);
//                                controlService.setProgressiveCommandCombinedYawEnabled(false);
                            }
                        }


                    }
//                    else if (command.pitch != 0){
//                        controlService.setYaw(0.0f);
//                        controlService.setRoll(0.0f);
////                        controlService.setPitch(0.0f);
//                        if (command.pitch < 0)
//                            controlService.setPitch(-pitch_power);
//                        else
//                            controlService.setPitch(pitch_power);
//                        controlService.setProgressiveCommandEnabled(true);
//                    }
//                    else if (command.roll != 0){
//                        controlService.setProgressiveCommandCombinedYawEnabled(false);
//                        controlService.moveForward(pitch_power);
//                        controlService.setYaw(0.0f);
//                        controlService.setRoll(command.roll);
//                        controlService.setProgressiveCommandEnabled(true);
//                    }
//                    else if (command.yaw != 0){
//                        controlService.setProgressiveCommandCombinedYawEnabled(false);
//                        controlService.setProgressiveCommandEnabled(false);
//                        controlService.moveForward(0.0f);
//                        controlService.setRoll(0.0f);
//                        controlService.setYaw(command.yaw);
//                        try {
//                            Thread.sleep(300);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    else {
//                        controlService.setProgressiveCommandCombinedYawEnabled(false);
//                        controlService.setRoll(0.0f);
//                        controlService.setYaw(0.0f);
////                        controlService.moveForward(0.0f);
//                        controlService.moveForward(pitch_power);
//                        controlService.setProgressiveCommandEnabled(true);
//                    }
                }
            }
        });

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, firmActivity, new BaseLoaderCallback(firmActivity) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS:
                    {
                        Log.i(LOG_TAG, "OpenCV loaded successfully");
                        controlThread.start();
                    } break;
                    default:
                    {
                        super.onManagerConnected(status);
                    } break;
                }
            }
        });
    }

    public void stop(){
        controlService.triggerTakeOff();//????????为何？
        ardroneStatus = -1;
        controlThread.interrupt();
    }
}
