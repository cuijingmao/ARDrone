package com.parrot.freeflight.activities.picdemo;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.parrot.freeflight.activities.task.TaskCommand;
import com.parrot.freeflight.activities.task.TaskMode;
import com.parrot.freeflight.ui.gl.GLBGVideoSprite;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by shisy13 on 16/8/23.
 */
public class ImageToCommand {
    static private final String LOG_TAG = "ImageToCommand";

    Context context;
    GLBGVideoSprite glbgVideoSprite;

    static int imgWidth = 640;
    static int imgHeight = 360;

    int lastCnt = 0;

    public ImageToCommand(Context context) {
        this.context = context;
        glbgVideoSprite = new GLBGVideoSprite(context.getResources());
        glbgVideoSprite.setAlpha(1.0f);
    }

    float lastpower = 0.0f;
    int end = TaskCommand.n - 1;

    /**
     * @param line 上半图路径形心和下半图路径形心，为两个Point结构
     * @return GameCommand包含了控制信息
     * 被getCommand调用
     */
    static public TaskCommand pointToCommand(Point[] line, TaskCommand taskCommand) {
        int end = TaskCommand.n - 1;
        if(Math.abs(taskCommand.yaw[end])<0.2 ) {
            taskCommand.pitch[end] = -0.01;    //寻找路径始终保持前进
        }
        else{
            taskCommand.pitch[end] = -0.005;//路径转弯时候前进速度减慢
        }
        if (line == null) {//如果没有找到路径，
            taskCommand.command = "stable";  //让四旋翼悬停
        } else {     //如果找到路径
            float k = 100.0f;
            Point center = new Point();   //形心位置
            if (line == null) {

            } else {
                k = (float) -((line[0].y - line[1].y) / (line[0].x - line[1].x));//k代表斜率

                center = new Point((line[0].x + line[1].x) / imgWidth - 1, 1 - (line[0].y + line[1].y) / imgHeight);//形心位置
                taskCommand.centersX[end] = (line[0].x + line[1].x) / imgWidth - 1;
                taskCommand.centersY[end] = 1 - (line[0].y + line[1].y) / imgHeight;
            }
            float rollThre = 0.01f;   //设置左右平移的最大速度-----或者是  形心位置相对图像中心左右最大偏离位置
            float kThre = 3.0f;       //设置最大斜率
            float pitchThre = 0.4f;   //设置前进后退最大速度
            Log.d(LOG_TAG, "point:" + center.x + "," + center.y);
            /**
             * 此处控制左右平移
             */
            if (Math.abs(center.x) > rollThre) {//如果 形心位置偏离坐标系超过阈值
                int sign = 1;       //控制方向，1右移
                if (center.x < 0)
                    sign = -1;     //左移
                float power = (float) (Math.pow(2, Math.abs(center.x)) - 1); //控制速度大小
                power = (float) (Math.pow(2, power) - 1);
                power = power / 600;
                taskCommand.roll[end] = sign * power;  //左右平移速度和方向
                if (sign == 1) {
                    Log.e(LOG_TAG, "飞行器右移！速度：" + taskCommand.roll[end]);
                } else {
                    Log.e(LOG_TAG, "飞行器左移！速度:" + taskCommand.roll[end]);
                }

            }
            else {
                taskCommand.roll[end]=0.0;  //此处置零防止以前时刻roll的值会一直影响下去
            }
            /**
             * 此处根据斜率，控制左右旋转
             */
            if (Math.abs(k) < kThre) {  //斜率小于阈值
                int sign = 1;    //控制方向，1代表向右
                if (k < 0)
                    sign = -1;  //-1代表向左
                float power = (float) (Math.pow(2, (2 - Math.abs(k)) / 2) - 1); //角度越大，偏转速度越大
                taskCommand.yaw[end] = sign * power / 2*1.3;
                if (sign == 1) {
                    Log.e(LOG_TAG, "飞行器向右转弯,速度：" + taskCommand.yaw[end]);
                } else {
                    Log.e(LOG_TAG, "飞行器向左转弯,速度：" + taskCommand.yaw[end]);
                }
            }
            else{
                taskCommand.yaw[end]=0.0;    //此处置零防止以前时刻yaw的值会一直影响下去
            }
            /**
             * 此处控制前进后退
             */
            if (Math.abs(center.y) > pitchThre) {  //如果形心位置y 偏离图像中心超过阈值
                int sign = 1;   //后退
                if (center.y > 0)
                    sign = -1;  //前进
                float power = (float) (Math.pow(2, Math.abs(center.y)) - 1);
                power = (float) (Math.pow(2, power) - 1);
                power = power / 100;
                taskCommand.pitch[end] = sign * power;
                if (sign == 1) {
                    Log.e(LOG_TAG, "飞行器后退！");
                } else {
                    Log.e(LOG_TAG, "飞行器后退！速度：" + taskCommand.pitch[end]);
                }
            } else if (line[1].y < 50 && line[0].y > 200 || (taskCommand.roll[end] == 0 && taskCommand.yaw[end] == 0)) {
                //如果在路径正上方，直接前进
              //  taskCommand.pitch[end] = -0.005f;//  缓慢前进
                Log.e(LOG_TAG, "在路径正上方，直接前进,速度：" + taskCommand.pitch);
            }

            if (line != null)
                Log.d(LOG_TAG, "line:" + line[0].x + "," + line[0].y + ";" + line[1].x + "," + line[1].y);
            else
                Log.d(LOG_TAG, "line:null");
        }


        Log.d(LOG_TAG, "command:" + taskCommand.pitch[end] + "," + taskCommand.roll[end] + "," + taskCommand.yaw[end]);
        return taskCommand;
    }


    public TaskCommand getCommand( TaskCommand taskCommand,ColorType colorType) {

        Bitmap bitmap = loadImage();   //加载视频图像
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
       taskCommand=ImageProcessor.checkConvert(mat,ColorType.BLUE,taskCommand);//判断是否切换模式

        TaskMode taskMode= taskCommand.taskMode;
        Log.e(LOG_TAG,"目前任务模式为："+taskMode);
        switch (taskMode) {
            case FOLLOWPATH:  //如果是沿路飞行模式
                Point[] line = ImageProcessor.findLinesP(mat, colorType);  //返回查找到的直线的两个端点
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                    System.gc();
                }
                Log.e(LOG_TAG,"before   return pointToCommand(line, taskCommand);");
                return pointToCommand(line, taskCommand);
            case TRACKBALL:  //如果是跟踪小球模式
                Mat hsv = ImageProcessor.hsvFilter(mat, colorType);
                double[] data = ImageProcessor.lookForBall(hsv, colorType);
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                    System.gc();
                }
                return pointToCommandBall(data, taskCommand);
        }
        return new TaskCommand();

    }

    /**
     * 根据小球球心位置，给出控制飞行的命令
     * 被getCommandBall()调用
     *
     * @param data 包含小球球心位置信息的double数组
     *             五元数组：球心x,y像素坐标，x,y像素对坐标，球的像素半径
     * @return GameCommand结构，包含控制信息
     */
    public TaskCommand pointToCommandBall(double[] data, TaskCommand taskCommand) {
        int end = TaskCommand.n - 1;
       taskCommand.pitch[end]=0.0;
        taskCommand.roll[end]=0.0;
        taskCommand.taskMode = TaskMode.TRACKBALL;
        taskCommand.colorType = ColorType.RED;
        if (data[0] == -2 || data[1] == -2 || data[2] == -2 || data[3] == -2 || data[4] == -2) {
            taskCommand.command = "stable";  //悬停
            Log.e(LOG_TAG, "未发现小球，保持悬停");
        } else {
            double pitchThre=50; //控制前进后退的阈值
            double gazThre = 0.0; //控制上升下降速度的阈值
            double yawThre = 0.2; //控制专项速度的阈值
//            if(Math.abs(data[4]-TaskCommand.radiusToKeep)>pitchThre)  {
//                taskCommand.pitch[end]=(data[4]-TaskCommand.radiusToKeep)/10000;  //如果图像中球的半径大于阈值，则适当后退，否则适当后退
//            }
//            else{
//                taskCommand.pitch[end]=0.0;   //如果远近合适就不再前进后退
//            }
            /**
             * 控制上升下降
             */
            if (Math.abs(data[4]) > gazThre) {
                int sign = 1;   //上升
                if (data[4] < 0)
                    sign = -1;   //下降
                taskCommand.gaze[end] = (float) (data[4] * 1.5);
                if (sign > 0) {
                    Log.e(LOG_TAG, "飞行器上升，速度：" + taskCommand.gaze[end]);
                } else {
                    Log.e(LOG_TAG, "飞行器下降，速度：" + taskCommand.gaze[end]);
                }
            }
            else {
                taskCommand.gaze[end]=0.0;    //此处置零防止以前时刻gaze的值会一直影响下去
            }
            /**
             * 控制左右转头
             */
            if (Math.abs(data[3]) > yawThre) {
                int sign = 1;   //右转
                if (data[3] < 0)
                    sign = -1;  //左转
                taskCommand.yaw[end] = (float) data[3] * 1.0;//乘以1.5太大
                if (sign > 0) {
                    Log.e(LOG_TAG, "飞行器右转，速度：" + taskCommand.yaw[end]);
                } else {
                    Log.e(LOG_TAG, "飞行器左转，速度：" + taskCommand.yaw[end]);
                }

            }
            else {
                taskCommand.yaw[end]=0.0 ;   //此处置零防止以前时刻yaw的值会一直影响下去
            }
        }
        Log.e(LOG_TAG, "data:" + data[2] + "," + data[3] + "," + data[4]);
        Log.e(LOG_TAG, "command:前进速度" +(- taskCommand.pitch[end]) + "\n右偏速度" + taskCommand.roll[end] + "\n右转速度" + taskCommand.yaw[end] + "," + taskCommand.gaze[end]);
        return taskCommand;
    }


    public Bitmap loadImage() {
        while (!glbgVideoSprite.updateVideoFrame()) {
        }
        Bitmap bitmap = glbgVideoSprite.getVideoBitmap();
        return bitmap;
    }

    /**
     * @param bitmap 将bitmap保存在设备，加入媒体库
     */
    private void saveBitmap(Bitmap bitmap) {
        File imageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        Log.d(LOG_TAG, imageDir.getAbsolutePath());
        File ardroneDir = new File(imageDir, "AR.Drone");
        Log.d(LOG_TAG, ardroneDir.getAbsolutePath());
        File save = new File(ardroneDir, "test.jpg");
        try {
            FileOutputStream out = new FileOutputStream(save);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, save.getAbsolutePath());
        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }


    TaskCommand closeToStopSign(double[] data, TaskCommand taskCommand) {

        if (data[0] == -2 || data[1] == -2 || data[2] == -2 || data[3] == -2 || data[4] == -2) {
            taskCommand.command = "stable";  //悬停
            Log.e(LOG_TAG, "未发现黄色停止标志，保持悬停");
        } else {
            taskCommand.gaze[end] = 0.0f;  //停止升降；
            double xThre = 0.1;  //允许飞行器偏离黄色圆中心x方向的最大相对距离
            double yThre = 0.1;  //允许飞行器偏离黄色圆中心x方向的最大相对距离
            if (Math.abs(data[3]) > xThre) {
                int sign = 1;//右移
                if (data[3] < 0) {    //黄圆圆心在图像中心的左边，飞行器左移
                    sign = -1;
                }
                taskCommand.roll[end] = (float) data[3] / 5;
                if (sign > 0) {
                    Log.e(LOG_TAG, "飞行器右移靠近停止目标，速度：" + taskCommand.roll[end]);
                } else {
                    Log.e(LOG_TAG, "飞行器左移靠近停止目标，速度：" + taskCommand.roll[end]);
                }
            }
            if (Math.abs(data[4]) > yThre) {
                int sign = 1;   //前进
                if (data[4] < 0) {  //  黄圆圆心在图像中心的后面，飞行器后移
                    sign = -1;   //后退
                }
                taskCommand.pitch[end] = (float) -data[4] / 5;   //pitch正直后退，负值前进
                if (sign > 0) {
                    Log.e(LOG_TAG, "飞行器后退靠近停止目标，速度：" + taskCommand.roll[end]);
                } else {
                    Log.e(LOG_TAG, "飞行器前进靠近停止目标，速度：" + taskCommand.roll[end]);
                }

            }

        }
        return taskCommand;
    }

    public static TaskCommand seekStopSign(Bitmap bitmap, TaskCommand taskCommand) {
        int end = TaskCommand.n - 1;
        double xThre = 0.1;  //允许飞行器偏离黄色圆中心x方向的最大相对距离
        double yThre = 0.1;  //允许飞行器偏离黄色圆中心x方向的最大相对距离
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Mat matYellow = new Mat();
        Utils.bitmapToMat(bitmap, matYellow);
        matYellow = ImageProcessor.hsvFilter(matYellow, ColorType.YELLOW);
        double[] data = ImageProcessor.lookForBall(matYellow, ColorType.YELLOW);
        if (data[0] == -2 || data[1] == -2 || data[2] == -2 || data[3] == -2 || data[4] != -2) {//未找到圆形黄色停止标志
            taskCommand.colorType = ColorType.RED;
        } else {  //如果找到黄色停止标记
            taskCommand.colorType = ColorType.YELLOW;
            taskCommand.gaze[end] = 0.0f;  //停止升降；
            taskCommand.taskMode = TaskMode.FOLLOWPATH;
            taskCommand.centersX[end] = data[3];
            taskCommand.centersY[end] = data[4];
            if (Math.abs(data[3]) > xThre) {
                int sign = 1;//右移
                if (data[3] < 0) {    //黄圆圆心在图像中心的左边，飞行器左移
                    sign = -1;
                }
                taskCommand.roll[end] = (float) data[3] / 5;
                if (sign > 0) {
                    Log.e(LOG_TAG, "飞行器右移靠近黄色停止目标，速度：" + taskCommand.roll);
                } else {
                    Log.e(LOG_TAG, "飞行器左移靠近黄色停止目标，速度：" + taskCommand.roll);
                }
            }
            if (Math.abs(data[4]) > yThre) {
                int sign = 1;   //前进
                if (data[4] < 0) {  //  黄圆圆心在图像中心的后面，飞行器后移
                    sign = -1;   //后退
                }
                taskCommand.pitch[end] = (float) -data[4] / 5;   //pitch正直后退，负值前进
                if (sign > 0) {
                    Log.e(LOG_TAG, "飞行器后退靠近黄色停止目标，速度：" + taskCommand.roll);
                } else {
                    Log.e(LOG_TAG, "飞行器前进靠近黄色停止目标，速度：" + taskCommand.roll);
                }

            }


        }
        return taskCommand;
    }

    /**
     * 此函数实现pid控制
     * 此处采用PD控制
     * 基本原理公式：   U（t+1)=U(t)+a*e(t)+c*De(t)/Dt
     *
     * @param taskCommand
     * @return
     */
    public static TaskCommand pidTaskCommand(TaskCommand taskCommand) {
        TaskCommand command = new TaskCommand();
        ColorType colorType = taskCommand.colorType;
        TaskMode taskMode = taskCommand.taskMode;
        double[] centersX = taskCommand.centersX;    //中心点的x相对坐标
        double[] centersY = taskCommand.centersY;   //中心点的y相对坐标
        double[] gaze = taskCommand.gaze;      //gaze正表示上升，负表示下降
        double[] pitch = taskCommand.pitch; //pitch正表示为后退，负值表示前进
        double[] roll = taskCommand.roll;  //roll正表示右偏移，负值表示左偏移
        double[] yaw = taskCommand.yaw;  //yaw正代表右转弯，负左转表示左转弯
        double pRatio = taskCommand.pRatio;             //pid算法比例因子
        double iRatio = taskCommand.iRatio;             //pid算法的积分因子,暂且不用
        double dRatio = taskCommand.dRatio;             //pid算法的微分因子
        int n = taskCommand.centersX.length;            //记录的个数
        double[] centersXErr = new double[n - 1];         //依次记录相邻两个中心点的x相对坐标的差值
        double[] centersYErr = new double[n - 1];         //依次记录相邻两个中心点的y相对坐标的差值
        double[] gazeErr = new double[n - 1];             //依次记录相邻两个上升下降速度的差值
        double[] pitchErr = new double[n - 1];       //依次记录相邻两个前进后退方向上的速度的差值
        double[] rollErr = new double[n - 1];       //依次记录相邻两个左右方向上的速度的差值
        double[] yawErr = new double[n - 1];      //依次记录相邻两个角速度的差值
        try {
            for (int i = 0; i < n - 1; i++) {
                centersXErr[i] = centersX[i + 1] - centersX[i];
                centersYErr[i] = centersY[i + 1] - centersY[i];
                gazeErr[i] = gaze[i + 1] - gaze[i];
                pitchErr[i] = pitch[i + 1] - pitch[i];
                rollErr[i] = roll[i + 1] - roll[i];
                yawErr[i] = yaw[i + 1] - yaw[i];
                //时间推移

                command.centersX[i] = centersX[i + 1];
                command.centersY[i] = centersY[i + 1];
                command.gaze[i] = gaze[i + 1];
                command.pitch[i] = pitch[i + 1];
                command.roll[i] = roll[i + 1];
                command.yaw[i] = yaw[i + 1];
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in for circle!");
            e.printStackTrace();
        }
        //此处取算术平均值
        //getMean(gazeErr)其实相当于gaze最后一个元素与第一个元素的差除以gaze的长度
        //其他的类似

        command.centersX[n - 1] = getMean(centersX);
        command.centersY[n - 1] = getMean(centersY);
        if (taskMode == TaskMode.FOLLOWPATH){
            //此处是PD算法的核心
            command.gaze[n - 1] = gaze[n - 1] + dRatio * getMean(gazeErr);
        command.pitch[n - 1] = pitch[n - 1] - pRatio * getMean(centersYErr) - dRatio * getMean(pitchErr);
        command.roll[n - 1] = roll[n - 1] + pRatio * getMean(centersXErr) + dRatio * getMean(rollErr);
       command.yaw[n - 1] = yaw[n - 1] + dRatio * getMean(yawErr);
    }
    else if(taskMode==TaskMode.TRACKBALL){
            command.gaze[n - 1] = gaze[n - 1] + dRatio * getMean(gazeErr);
      //      command.pitch[n - 1] = pitch[n - 1] - pRatio * getMean(centersYErr) - dRatio * getMean(pitchErr);
          //  command.roll[n - 1] = roll[n - 1] + pRatio * getMean(centersXErr) + dRatio * getMean(rollErr);
          command.yaw[n - 1] = yaw[n - 1] + dRatio * getMean(yawErr);
          //  command.yaw[n - 1] = yaw[n - 1];

        }

        return command;
    }

    public static double getMean(double[] a) {
        int m = a.length;
        double sum = 0.0;
        double mean = 0.0;
        if (m > 0) {
            for (int i = 0; i < m; i++) {
                sum = sum + a[1];
            }
            mean = sum / m;
        }
        return mean;
    }
}
