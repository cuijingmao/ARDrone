package com.parrot.freeflight.activities.task;

import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import com.parrot.freeflight.activities.ControlDroneActivity;
import com.parrot.freeflight.activities.picdemo.ColorType;;
import com.parrot.freeflight.activities.picdemo.ImageToCommand;
import com.parrot.freeflight.service.DroneControlService;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.util.Timer;


public class TaskController {
    final String LOG_TAG = getClass().getSimpleName();
    TaskActivity taskActivity;
    ControlDroneActivity controlDroneActivity;
    DroneControlService controlService;
    Timer commandTimer;
    Thread controlThread;
    ImageToCommand imageToCommand;
    int ardroneStatus = 0;
    TaskCommand taskCommand = new TaskCommand();
    int end = TaskCommand.n - 1;
    int mySoundId;
    SoundPool soundPool;
    boolean hasConverted = false;

    public TaskController(TaskActivity taskActivity, DroneControlService controlService) {

        soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        mySoundId = soundPool.load("/raw/battery", 1);
        this.taskActivity = taskActivity;
        this.controlService = controlService;
        imageToCommand = new ImageToCommand(taskActivity);
        commandTimer = new Timer();

    }

    /**
     * 响铃功能
     */
//    public void ringBell() {
//        Log.e(LOG_TAG,"已进入ringBell函数");
//
//     //   File  file =new File("/res/raw/battery");
//
//        try{
//
//            try{
//                Thread.sleep(2000);
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        Log.e(LOG_TAG,"成功加载声音！！");
//     int  idd= soundPool.play(mySoundId, 1, 1, 1, -1, 1);}
//        catch (Exception e){
//            e.printStackTrace();
//        }
//        try {
//            Thread.sleep(2000);
//            soundPool.release();
//         //   soundPool.stop(idd);
//        } catch (Exception e) {
//            e.printStackTrace();
//
//        }
//
//    }
    public void start() {
        final float gaz_roll_mod = -0.00175f;
        final float pitch_roll_mod = -0.002f;
        final double xThre = 0.1;  //表示圆形停止标记形心x相对镜头中心的最大容许偏移量
        final double yThre = 0.1;  //表示圆形停止标记形心y相对镜头中心的最大容许偏移量
        // final double power =0.05;  //表示

        controlThread = new Thread(new Runnable() {
            @Override
            public void run() {
                taskCommand.taskMode = TaskMode.FOLLOWPATH;
                controlService.switchCamera(); //切换为下摄像头
                controlService.triggerTakeOff();//准备起飞

                try {
                    Thread.sleep(4000);     // wait takeoff

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //    controlService.moveForward(power); // slowly move forward
                controlService.setProgressiveCommandEnabled(false);//起飞阶段，前进指令禁用
                controlService.setGaz(0.5f);   // 设置四旋翼的飞行速度
                try {
                    Thread.sleep(4000);     //让四旋翼飞一段时间，2秒
                } catch (InterruptedException e) {

                    e.printStackTrace();
                }

                //      ControlDroneActivity.stopEmergencySound();

                controlService.setGaz(0.0f);   //停止上升
                taskCommand.runTime = 0;
                while (ardroneStatus == 0 && !Thread.currentThread().isInterrupted()) {
                    long timePre = System.currentTimeMillis();
                    taskCommand = imageToCommand.getCommand(taskCommand, ColorType.RED); // 寻找路径的主要函数

                    taskCommand = ImageToCommand.pidTaskCommand(taskCommand);

                    long timePos = System.currentTimeMillis();
                    taskCommand.runTime = taskCommand.runTime + timePos - timePre;
                    Log.e(LOG_TAG, "当前运行时间：" + taskCommand.runTime + "毫秒");
                    if (taskCommand.runTime >35000 & hasConverted == false) {
                        taskCommand.taskMode = TaskMode.TRACKBALL;
                        hasConverted = true;
                        controlService.switchCamera();//切换为上摄像头
                        Log.e(LOG_TAG, "*************************************************************************");
                        Log.e(LOG_TAG, "已经切换为跟踪目标模式！！！");
                        Log.e(LOG_TAG, "已经切换为跟踪目标模式！！！");
                        Log.e(LOG_TAG, "已经切换为跟踪目标模式！！！");
                        Log.e(LOG_TAG, "已经切换为跟踪目标模式！！！");
                        Log.e(LOG_TAG, "已经切换为跟踪目标模式！！！");
                        Log.e(LOG_TAG, "已经切换为跟踪目标模式！！！");
                        Log.e(LOG_TAG, "已经切换为跟踪目标模式！！！");
                        Log.e(LOG_TAG, "已经切换为跟踪目标模式！！！");
                        Log.e(LOG_TAG, "已经切换为跟踪目标模式！！！");
                        Log.e(LOG_TAG, "已经切换为跟踪目标模式！！！");
                        Log.e(LOG_TAG, "已经切换为跟踪目标模式！！！");
                        Log.e(LOG_TAG, "已经切换为跟踪目标模式！！！");
                        Log.e(LOG_TAG, "已经切换为跟踪目标模式！！！");
                        Log.e(LOG_TAG, "已经切换为跟踪目标模式！！！");
                        Log.e(LOG_TAG, "已经切换为跟踪目标模式！！！");
                        Log.e(LOG_TAG, "已经切换为跟踪目标模式！！！");
                        Log.e(LOG_TAG, "已经切换为跟踪目标模式！！！");
                        Log.e(LOG_TAG, "已经切换为跟踪目标模式！！！");
                        Log.e(LOG_TAG, "已经切换为跟踪目标模式！！！");
                        Log.e(LOG_TAG, "已经切换为跟踪目标模式！！！");
                        Log.e(LOG_TAG, "已经切换为跟踪目标模式！！！");
                        Log.e(LOG_TAG, "已经切换为跟踪目标模式！！！");
                        Log.e(LOG_TAG, "已经切换为跟踪目标模式！！！");
                        Log.e(LOG_TAG, "已经切换为跟踪目标模式！！！");
                        Log.e(LOG_TAG, "已经切换为跟踪目标模式！！！");
                        Log.e(LOG_TAG, "已经切换为跟踪目标模式！！！");
                        Log.e(LOG_TAG, "已经切换为跟踪目标模式！！！");
                        Log.e(LOG_TAG, "*************************************************************************");
                    } else {

                    }
                    /**
                     * 此段代码用于处理转换过程
                     */
                    Log.e(LOG_TAG, "控制飞行前：");
                    if (taskCommand.command.equals("stable")) {//如果命令是悬停
                        controlService.setProgressiveCommandEnabled(false);//让平移指令失效
                        controlService.setYaw(0.0f);//正值向右转头，负值向左转头
                        controlService.setRoll(0.0f);//正值向右平移，负值向左平移
                        controlService.setPitch(0.0f);//正值向后，负值向前
                        controlService.setGaz(0.0f);//正值上升，负值下降

                    } else {

                        if (taskCommand.taskMode == TaskMode.TRACKBALL) {//如果是跟踪小球模式
                            controlService.setProgressiveCommandEnabled(false);//
                            controlService.setProgressiveCommandCombinedYawEnabled(false);//
                            controlService.setGaz((float) taskCommand.gaze[end]);//上升下降
                            controlService.setYaw((float) taskCommand.yaw[end]);//左右转
                            controlService.setRoll(0.0f);//不左右平移
                            controlService.setPitch(0.0f);//不前进后

                        } else { //处理跟踪路径模式
                            if (taskCommand.yaw[taskCommand.n - 1] != 0) {//表示可以转头
                                controlService.setProgressiveCommandEnabled(true);
                                controlService.setProgressiveCommandCombinedYawEnabled(true);
                                controlService.setYaw((float) taskCommand.yaw[end]);
                                controlService.setPitch((float) taskCommand.pitch[end]);
                                controlService.setRoll((float) taskCommand.roll[end]);
                                controlService.setGaz((float) taskCommand.gaze[end]);

//                            try {
//                                Thread.sleep(500);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
                            } else {//如果不能转头
                                controlService.setGaz((float) taskCommand.gaze[end]);
                                controlService.setYaw((float) taskCommand.yaw[end]);
                                controlService.setPitch((float) taskCommand.pitch[end]);
                                controlService.setRoll((float) taskCommand.roll[end]);
                                controlService.setProgressiveCommandEnabled(true);
                                controlService.setProgressiveCommandCombinedYawEnabled(false);
                            }
                        }

                    }

                }
            }
        });

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, taskActivity, new BaseLoaderCallback(taskActivity) {
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
        // controlService.triggerTakeOff();
        ardroneStatus = -1;
        controlThread.interrupt();
    }
}
