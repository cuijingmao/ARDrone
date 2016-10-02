package com.parrot.freeflight.activities.game;

import android.util.Log;

import com.parrot.freeflight.activities.image.ColorType;
import com.parrot.freeflight.activities.image.ImageToCommand;
import com.parrot.freeflight.service.DroneControlService;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;


import java.util.Timer;


/**
 * Created by shisy13 on 16/8/24.
 */
public class GameController {
    final String LOG_TAG = getClass().getSimpleName();
    GameActivity gameActivity;
    DroneControlService controlService;
    boolean  toTrackBALL;   //设置点击切换摄像头时是否由跟踪路径模式切换为跟踪小球模式
    Timer commandTimer;
    Thread controlThread;
    ImageToCommand imageToCommand;
    int ardroneStatus = 0;

    public GameController(GameActivity gameActivity, DroneControlService controlService) {
        this.gameActivity = gameActivity;
        this.controlService = controlService;
        imageToCommand = new ImageToCommand(gameActivity);
        commandTimer = new Timer();
    }

    public void start() {
        final float gaz_roll_mod = -0.00175f;
        final float pitch_roll_mod = -0.002f;
        final double xThre = 0.1;  //表示黄色圆形停止标记形心x相对镜头中心的最大容许偏移量
        final double yThre = 0.1;  //表示黄色圆形停止标记形心y相对镜头中心的最大容许偏移量

        controlThread = new Thread(new Runnable() {
            @Override
            public void run() {
            controlService.switchCamera(); //切换为下摄像头
                controlService.triggerTakeOff();//准备起飞
                try {
                 //   Thread.sleep(4000);     // wait takeoff
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                controlService.moveForward(power); // slowly move forward
                controlService.setProgressiveCommandEnabled(false);//起飞阶段，前进指令禁用
                controlService.setGaz(0.5f);   // 设置四旋翼的飞行速度


                try {
                    Thread.sleep(3000);     //让四旋翼飞一段时间，2秒
                } catch (InterruptedException e) {

                    e.printStackTrace();
                }
                controlService.setGaz(0.0f);   //停止上升

                long timePre = 0;
                long timeNow = 0;
                int flyAboveYellowCount = 0;  //飞行器飘过黄色圆上方的次数
//                ColorType colorType;  //标明识别球的颜色
//                TaskMode taskMode ;   //默认跟踪路径
                GameCommand mainCommand = new GameCommand();
                GameCommand command;
                boolean isAboveStopSign = false;        //是否已经在停止标记上方
                Log.e(LOG_TAG, "开始进入循环");
                while (ardroneStatus == 0 && !Thread.currentThread().isInterrupted()) {
//                    colorType=mainCommand.colorType;
//                    taskMode=mainCommand.taskMode;
                    timePre = System.currentTimeMillis();

                    //boolean ball = true; //true代表找小球，false表示沿路径
                    //     GameCommand command = imageToCommand.getCommandBall(colorType);//寻找小球的主要函数
                    // command = imageToCommand.getCommand(mainCommand.taskMode, mainCommand.colorType); // 寻找路径的主要函数
                    command = imageToCommand.getCommand(TaskMode.FOLLOWPATH, ColorType.RED); // 寻找路径的主要函数
//                    mainCommand.taskMode = command.taskMode;
//                    mainCommand.colorType = command.colorType;
                    if (mainCommand.taskMode == TaskMode.FOLLOWPATH && mainCommand.colorType == ColorType.YELLOW) {
                        //此时已经飞到终点附近,在靠近黄色停止标志的过程中
                        double x = Math.abs(command.relativePosition[0]);
                        double y = Math.abs(command.relativePosition[1]);
                        if (x < xThre && y < yThre) { //大约在黄色停止标志的上方
                            if (flyAboveYellowCount < 100) {
                                flyAboveYellowCount = flyAboveYellowCount + 1;
                            }
                        }
                    }
                    if (flyAboveYellowCount > 100) {
                        isAboveStopSign = true;
                    }

                    if (mainCommand.taskMode == TaskMode.FOLLOWPATH && isAboveStopSign) {
                        //如果还在沿路飞行阶段，且已经飞在黄色停止标记上方
                        controlService.switchCamera(); //切换为上镜头，跟踪小球
                        mainCommand.taskMode = TaskMode.TRACKBALL;//切换为跟踪小球模式
                        Log.e(LOG_TAG, "已经切换为跟踪小球模式");
                        /**
                         * 此处可以加入播放警告声音，翻转等动作，
                         * 表示已经进入跟踪小球模式
                         */
                    }

                    timeNow = System.currentTimeMillis();
                    long timeDelta = timeNow - timePre;
                    if (timeDelta < 150) {
                        try {
                            Thread.sleep(150 - timeDelta);
                        } catch (Exception e) {
                        }
                    }


                    if (command.command.equals("stable")) {//如果命令是悬停
                        controlService.setProgressiveCommandEnabled(false);//让平移指令失效
                        controlService.setYaw(0.0f);//正值向右转头，负值向左转头
                        controlService.setRoll(0.0f);//正值向右平移，负值向左平移
                        controlService.setPitch(0.0f);//正值向后，负值向前
                        controlService.setGaz(0.0f);//正值上升，负值下降
                    } else {

                        if (command.taskMode == TaskMode.TRACKBALL) {//如果是跟踪小球模式
                            controlService.setProgressiveCommandEnabled(false);//
                            controlService.setProgressiveCommandCombinedYawEnabled(false);//
                            controlService.setGaz(command.gaz);//上升下降
                            controlService.setYaw(command.yaw);//左右转
                            controlService.setRoll(0.0f);//不左右平移
                            controlService.setPitch(0.0f);//不前进后
                        } else {//处理跟踪路径模式
                            if (command.yaw != 0) {//表示可以转头
                                controlService.setProgressiveCommandEnabled(true);
                                controlService.setProgressiveCommandCombinedYawEnabled(true);
                                controlService.setYaw(command.yaw);
                                controlService.setPitch(command.pitch);
                                controlService.setRoll(command.roll);
                                controlService.setGaz(command.gaz);

//                            try {
//                                Thread.sleep(500);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
                            } else {//如果不能转头
                                controlService.setGaz(command.gaz);
                                controlService.setYaw(command.yaw);
                                controlService.setPitch(command.pitch);
                                controlService.setRoll(command.roll);
                                controlService.setProgressiveCommandEnabled(true);
                                controlService.setProgressiveCommandCombinedYawEnabled(false);
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

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, gameActivity, new BaseLoaderCallback(gameActivity) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS: {
                        Log.i(LOG_TAG, "OpenCV loaded successfully");
                        controlThread.start();
                    }
                    break;
                    default: {
                        super.onManagerConnected(status);
                    }
                    break;
                }
            }
        });
    }


    public void stop() {
        controlService.triggerTakeOff();
        ardroneStatus = -1;
        controlThread.interrupt();
    }
}
