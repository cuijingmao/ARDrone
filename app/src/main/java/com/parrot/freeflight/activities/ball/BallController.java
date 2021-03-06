package com.parrot.freeflight.activities.ball;

import android.util.Log;

import com.parrot.freeflight.activities.picdemo.ColorType;
import com.parrot.freeflight.activities.picdemo.ImageToCommand;
import com.parrot.freeflight.activities.task.TaskCommand;
import com.parrot.freeflight.activities.task.TaskMode;
import com.parrot.freeflight.service.DroneControlService;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.util.Timer;

public class BallController {
    final String LOG_TAG = getClass().getSimpleName();
   BallActivity ballActivity;
    DroneControlService controlService;
    Timer commandTimer;
    Thread controlThread;
    ImageToCommand imageToCommand;
    int ardroneStatus = 0;
    TaskCommand taskCommand=new TaskCommand();
    int end= TaskCommand.n-1;
    public BallController(BallActivity ballActivity, DroneControlService controlService) {
        this.ballActivity = ballActivity;
        this.controlService = controlService;
        imageToCommand = new ImageToCommand(ballActivity);
        commandTimer = new Timer();
    }

    public void start() {
        controlThread = new Thread(new Runnable() {
            @Override
            public void run() {
      //   controlService.switchCamera(); //切换为下摄像头
  controlService.triggerTakeOff();//准备起飞
                try {
                    Thread.sleep(4000);     // wait takeoff
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                controlService.setProgressiveCommandEnabled(false);//起飞阶段，前进指令禁用
                controlService.setGaz(0.5f);   // 设置四旋翼的飞行速度
                try {
                    Thread.sleep(2000);     //让四旋翼飞一段时间，2秒
                } catch (InterruptedException e) {

                    e.printStackTrace();
                }
                controlService.setGaz(0.0f);   //停止上升
                Log.e(LOG_TAG, "开始进入循环");
                while (ardroneStatus == 0 && !Thread.currentThread().isInterrupted()) {
                    taskCommand.taskMode=TaskMode.TRACKBALL;
                   taskCommand = imageToCommand.getCommand(taskCommand, ColorType.RED); // 寻找路径的主要函数
                    taskCommand= ImageToCommand.pidTaskCommand(taskCommand);
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
                            controlService.setGaz((float)taskCommand.gaze[end]);//上升下降
                            controlService.setYaw((float)taskCommand.yaw[end]);//左右转
                            controlService.setRoll(0.0f);//不左右平移
                            controlService.setPitch(0.0f);//不前进后
                        } else {//处理跟踪路径模式
                            if (taskCommand.yaw[end] != 0) {//表示可以转头
                                controlService.setProgressiveCommandEnabled(true);
                                controlService.setProgressiveCommandCombinedYawEnabled(true);
                                controlService.setYaw((float)taskCommand.yaw[end]);
                                controlService.setPitch((float)taskCommand.pitch[end]);
                                controlService.setRoll((float)taskCommand.roll[end]);
                                controlService.setGaz((float)taskCommand.gaze[end]);
//                            try {
//                                Thread.sleep(500);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
                            } else {//如果不能转头
                                controlService.setGaz((float)taskCommand.gaze[end]);
                                controlService.setYaw((float)taskCommand.yaw[end]);
                                controlService.setPitch((float)taskCommand.pitch[end]);
                                controlService.setRoll((float)taskCommand.roll[end]);
                                controlService.setProgressiveCommandEnabled(true);
                                controlService.setProgressiveCommandCombinedYawEnabled(false);
                            }
                        }

                    }

                }
            }
        });

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, ballActivity, new BaseLoaderCallback(ballActivity) {
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
