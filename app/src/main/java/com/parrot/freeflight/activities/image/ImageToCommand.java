package com.parrot.freeflight.activities.image;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.parrot.freeflight.activities.game.GameCommand;
import com.parrot.freeflight.activities.game.TaskMode;
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

//    public GameCommand getCommand(){
//        GameCommand command = new GameCommand();
//        // read photo to bitmap
//        Bitmap bitmap = loadImage();
//        Mat mat = new Mat();
//        Utils.bitmapToMat(bitmap, mat);
//        // use CJM
//        Mat hsv = ImageProcessor.hsvFilter(mat);
//        Point[] line = ImageProcessor.findLinesP(mat);
//        Utils.matToBitmap(hsv, bitmap);
//        PointF[] points = ImageProcessor.centroid(bitmap);
//        if (!bitmap.isRecycled()){
//            bitmap.recycle();
//            System.gc();
//        }
//        float offset = (float) Math.sqrt(points[0].x*points[0].x+points[1].x*points[1].x);
//
//        float power = (float) (Math.pow(2, offset)-1);
//        power = (float) (Math.pow(2, power) - 1) / 300;
////        power = power - lastpower;
////        lastpower = power;
//
//        float yawthre = 0.05f;
//        float rollthre = 0.1f;
//        float kthre = 2.0f;
//
//        float k;
//        if (line == null)
//            k = 100.0f;
//        else
//            k = (float) ((line[0].y - line[1].y)/(line[0].x - line[1].x));
//
//        if (points[0].x < -1 && points[0].y < -1 && points[1].x < -1 && points[1].y < -1){
//            command.command = "stable";
//        }
//        else if (points[0].x < -1 && points[0].y < -1 && (points[1].x > -1 || points[1].y > -1)  && Math.abs(k) > kthre){
//            command.pitch = power;
//        }
//        else if (points[1].x < -1 && points[1].y < -1 && (points[0].x > -1 || points[0].y > -1)  && Math.abs(k) > kthre){
//            command.pitch = -power;
//        }
//        else {
//            Log.d(LOG_TAG, "k:" + k);
//            if (points[0].x < -rollthre && points[1].x < -rollthre && Math.abs(k) > kthre){
//                command.roll = -power;
//            }
//            else if (points[0].x > rollthre && points[1].x > rollthre && Math.abs(k) > kthre){
//                command.roll = power;
//            }
//            else if (k > 0 && k < 0.5) {
//                command.yaw = (float) 0.1/k;
//            }
//            else if (k < 0 && k >-0.5){
//                command.yaw = (float) 0.1/k;
//            }
//        }
//
//        Log.d(LOG_TAG, "center:"+points[0].x + "," + points[0].y + ";" + points[1].x + "," + points[1].y);
//        Log.d(LOG_TAG, "command:" + command.pitch + "," + command.roll + "," + command.yaw);
//        return command;
//    }

    /**
     * @param line 上半图路径形心和下半图路径形心，为两个Point结构
     * @return GameCommand包含了控制信息
     * 被getCommand调用
     */
    static public GameCommand pointToCommand(Point[] line) {
        GameCommand command = new GameCommand();
        command.taskMode = TaskMode.FOLLOWPATH;
        command.colorType=ColorType.RED;
//        if (line == null && (points[0].x < -1 && points[1].x < -1)) {
        if (line == null) {//如果没有找到路径，
            command.command = "stable";  //让四旋翼悬停
        } else {     //如果找到路径
            float k = 100.0f;
            Point center = new Point();   //形心位置
            if (line == null) {
//                k = 100.0f;
//                if (points[0].x < -1){
//                    center.x = points[1].x;
//                    center.y = points[1].y;
//                }
//                else if (points[1].x < -1) {
//                    center.x = points[0].x;
//                    center.y = points[0].y;
//                }
//                else {
//                    center.x = (points[0].x + points[1].x)/2;
//                    center.y = (points[0].y + points[1].y)/2;
//                    k = (points[0].y - points[1].y) / (points[0].x - points[1].x);
//                }
            } else {
                k = (float) -((line[0].y - line[1].y) / (line[0].x - line[1].x));//k代表斜率

                center = new Point((line[0].x + line[1].x) / imgWidth - 1, 1 - (line[0].y + line[1].y) / imgHeight);//形心位置
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
               // float power = (float) (Math.pow( Math.abs(center.x),2) ); //控制速度大小
               // power = (float) (Math.pow(2, power) - 1);
              //  power = power / 100;
                power = power / 500;

//                power = (float) Math.abs(center.x)/100;
                command.roll = sign * power;  //左右平移速度和方向
                if (sign == 1) {
                    Log.e(LOG_TAG, "飞行器右移！速度：" + command.roll);
                } else {
                    Log.e(LOG_TAG, "飞行器左移！速度:" + command.roll);
                }

            }
            /**
             * 此处根据斜率，控制左右旋转
             */
            if (Math.abs(k) < kThre) {  //斜率小于阈值
                int sign = 1;    //控制方向，1代表向右
                if (k < 0)
                    sign = -1;  //-1代表向左
                float power = (float) (Math.pow(2, (2 - Math.abs(k)) / 2) - 1); //角度越大，偏转速度越大
               // command.yaw = sign * power / 2;
                command.yaw = sign * power/2 ;
                if (sign == 1) {
                    Log.e(LOG_TAG, "飞行器向右转弯,速度：" + command.yaw);
                } else {
                    Log.e(LOG_TAG, "飞行器向左转弯,速度：" + command.yaw);
                }

//                command.yaw = 0;
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
                command.pitch = sign * power;
                if (sign == 1) {
                    Log.e(LOG_TAG, "飞行器后退！");
                } else {
                    Log.e(LOG_TAG, "飞行器后退！速度：" + command.pitch);
                }
            } else if (line[1].y < 50 && line[0].y > 200 || (command.roll == 0 && command.yaw == 0)) {
                //如果在路径正上方，直接前进
                command.pitch = -0.005f;//  缓慢前进
                Log.e(LOG_TAG, "在路径正上方，直接前进,速度：" + command.pitch);
            }

            if (line != null)
                Log.d(LOG_TAG, "line:" + line[0].x + "," + line[0].y + ";" + line[1].x + "," + line[1].y);
            else
                Log.d(LOG_TAG, "line:null");
        }


        Log.d(LOG_TAG, "command:" + command.pitch + "," + command.roll + "," + command.yaw);
        return command;
    }

    static public GameCommand pointToCommand(Point[] line, GameCommand command) {   //传入command 是为了不改变command的状态


        if (line == null) {//如果没有找到路径，
            command.command = "stable";  //让四旋翼悬停
        } else {     //如果找到路径
            float k = 100.0f;
            Point center = new Point();   //形心位置
            if (line == null) {
//                k = 100.0f;
//                if (points[0].x < -1){
//                    center.x = points[1].x;
//                    center.y = points[1].y;
//                }
//                else if (points[1].x < -1) {
//                    center.x = points[0].x;
//                    center.y = points[0].y;
//                }
//                else {
//                    center.x = (points[0].x + points[1].x)/2;
//                    center.y = (points[0].y + points[1].y)/2;
//                    k = (points[0].y - points[1].y) / (points[0].x - points[1].x);
//                }
            } else {
                k = (float) -((line[0].y - line[1].y) / (line[0].x - line[1].x));//k代表斜率

                center = new Point((line[0].x + line[1].x) / imgWidth - 1, 1 - (line[0].y + line[1].y) / imgHeight);//形心位置
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
                power = power / 100;
//                power = (float) Math.abs(center.x)/100;
                command.roll = sign * power;  //左右平移速度和方向
                if (sign == 1) {
                    Log.e(LOG_TAG, "飞行器右移！速度：" + command.roll);
                } else {
                    Log.e(LOG_TAG, "飞行器左移！速度:" + command.roll);
                }

            }
            /**
             * 此处根据斜率，控制左右旋转
             */
            if (Math.abs(k) < kThre) {  //斜率小于阈值
                int sign = 1;    //控制方向，1代表向右
                if (k < 0)
                    sign = -1;  //-1代表向左
                float power = (float) (Math.pow(2, (2 - Math.abs(k)) / 2) - 1); //角度越大，偏转速度越大
                command.yaw = sign * power / 4;
                if (sign == 1) {
                    Log.e(LOG_TAG, "飞行器向右转弯,速度：" + command.yaw);
                } else {
                    Log.e(LOG_TAG, "飞行器向左转弯,速度：" + command.yaw);
                }

//                command.yaw = 0;
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
                command.pitch = sign * power;
                if (sign == 1) {
                    Log.e(LOG_TAG, "飞行器后退！");
                } else {
                    Log.e(LOG_TAG, "飞行器后退！速度：" + command.pitch);
                }
            } else if (line[1].y < 50 && line[0].y > 200 || (command.roll == 0 && command.yaw == 0)) {
                //如果在路径正上方，直接前进
                command.pitch = -0.005f;//  缓慢前进
                Log.e(LOG_TAG, "在路径正上方，直接前进,速度：" + command.pitch);
            }

            if (line != null)
                Log.d(LOG_TAG, "line:" + line[0].x + "," + line[0].y + ";" + line[1].x + "," + line[1].y);
            else
                Log.d(LOG_TAG, "line:null");
        }


        Log.d(LOG_TAG, "command:" + command.pitch + "," + command.roll + "," + command.yaw);
        return command;
    }

    /**
     * @return 加载一个视频帧，识别路径，给出控制命令
     */
    public GameCommand getCommand(TaskMode taskMode, ColorType colorType) {
        GameCommand command=new GameCommand();

        Bitmap bitmap = loadImage();   //加载视频图像
//        command=seekStopSign(bitmap);
//        //如果已经发现黄色停止标志,而且好在寻找热点
//        if(command.colorType==ColorType.YELLOW && command.taskMode==TaskMode.FOLLOWPATH){
//      return   command;
//        }
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        switch (taskMode) {
            case FOLLOWPATH:  //如果是沿路飞行模式
                // read a frame to bitmap
                //  Bitmap bitmap = loadImage();   //加载视频图像
//                Mat mat = new Mat();
//                Utils.bitmapToMat(bitmap, mat);
                // use CJM

//                command =seekStopSign(bitmap);
//                //如果已经发现黄色停止标志,而且正在在寻找路径
//                if(command.colorType==ColorType.YELLOW && command.taskMode==TaskMode.FOLLOWPATH){
//                    return   command;}

                Point[] line = ImageProcessor.findLinesP(mat, colorType);  //返回查找到的直线的两个端点
                //      PointF[] points = new PointF[3];
//        Mat hsv = ImageProcessor.hsvFilter(mat);
//        Utils.matToBitmap(hsv, bitmap);
//        points = ImageProcessor.centroid(bitmap);
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                    System.gc();
                }

                return pointToCommand(line);
            case TRACKBALL:  //如果是跟踪小球模式
                //  Bitmap bitmap = loadImage();
//added by cui
//                command = seekStopSign(bitmap);
//                if (command.colorType == ColorType.YELLOW) {   //找到黄色停止标记
//                    return command;  //直接返回
//                }

                //  Mat mat = new Mat();
                //Utils.bitmapToMat(bitmap, mat);
                Mat hsv = ImageProcessor.hsvFilter(mat, colorType);
//               Imgproc.resize(mat, mat, new Size(640, 320));
                double[] data = ImageProcessor.lookForBall(hsv, colorType);
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                    System.gc();
                }
                return pointToCommandBall(data);


        }
        return new GameCommand();

    }

    /**
     * 根据小球球心位置，给出控制飞行的命令
     * 被getCommandBall()调用
     *
     * @param data 包含小球球心位置信息的double数组
     * @return GameCommand结构，包含控制信息
     */
    public GameCommand pointToCommandBall(double[] data) {
        GameCommand command = new GameCommand();//用来包装命令数据
        command.taskMode=TaskMode.TRACKBALL;
        command.colorType=ColorType.RED;
        if (data[0] == -2 || data[1] == -2 || data[2] == -2 || data[3] == -2 || data[4] == -2) {
            command.command = "stable";  //悬停
            Log.e(LOG_TAG, "未发现小球，保持悬停");
        } else {
            double gazThre = 0.0;
            double yawThre = 0.2;
            /**
             * 控制上升下降
             */
            if (Math.abs(data[4]) > gazThre) {
                int sign = 1;   //上升
                if (data[4] < 0)
                    sign = -1;   //下降
//                command.gaz = (float) (sign * (Math.pow(2, Math.abs(data[4])) - 1));
                // command.gaz = (float) data[4];
                command.gaz = (float) (data[4]*1.5);
                //  command.gaz = (float) data[4]/5;
                if (sign > 0) {
                    Log.e(LOG_TAG, "飞行器上升，速度：" + command.gaz);
                } else {
                    Log.e(LOG_TAG, "飞行器下降，速度：" + command.gaz);
                }
            }
            /**
             * 控制左右转头
             */
            if (Math.abs(data[3]) > yawThre) {
                int sign = 1;   //右转
                if (data[3] < 0)
                    sign = -1;  //左转

              //  command.yaw = (float) data[3] / 2;
              command.yaw = (float) data[3] ;
                if (sign > 0) {
                    Log.e(LOG_TAG, "飞行器右转，速度：" + command.yaw);
                } else {
                    Log.e(LOG_TAG, "飞行器左转，速度：" + command.yaw);
                }

//                double power = Math.pow(2, Math.abs(data[3])) - 1;
//                power = Math.pow(2, power) - 1;
//                command.roll = (float) (sign * power)/100;
                //                double power = Math.pow(2, Math.abs(data[3])) - 1;
//                power = Math.pow(2, power) - 1;
//                command.roll = (float) (sign * power)/100;
            }
//            command.pitch = -0.003f;
//            double radius = 20;
//            double offset = data[2]-radius;
//            if (Math.abs(offset) > 1){
//                if (offset > 0){
//                    command.pitch = 0.005f;
//                }
//                else {
//                    command.pitch = -0.005f;
//                }
//            }
            Log.d(LOG_TAG, "radius:" + data[2]);
        }
        Log.d(LOG_TAG, "data:" + data[2] + "," + data[3] + "," + data[4]);
        Log.d(LOG_TAG, "command:" + command.pitch + "," + command.roll + "," + command.yaw + "," + command.gaz);
        return command;
    }

    /**
     * 加载一个视频帧，找出形心位置，以此设置gamecommand，传递控制信息
     * 调用了lookForRedBall,用来查找小球
     *
     * @return
     */
    public GameCommand getCommandBall(ColorType colorType) {
        Bitmap bitmap = loadImage();
//added by cui
        GameCommand command = seekStopSign(bitmap);
        if (command.colorType == ColorType.YELLOW) {   //找到黄色停止标记
            return command;  //直接返回
        }

        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        Mat hsv = ImageProcessor.hsvFilter(mat, colorType);
//        Imgproc.resize(mat, mat, new Size(640, 320));
        double[] data = ImageProcessor.lookForBall(hsv, colorType);
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
            System.gc();
        }
        return pointToCommandBall(data);
    }


    public Bitmap loadImage() {
        while (!glbgVideoSprite.updateVideoFrame()) {
        }
        Bitmap bitmap = glbgVideoSprite.getVideoBitmap();
//        imgWidth = bitmap.getWidth();
//        imgHeight = bitmap.getHeight();
//        saveBitmap(bitmap);
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


    GameCommand closeToYellow(double[] data) {
        GameCommand command = new GameCommand();//用来包装命令数据
        if (data[0] == -2 || data[1] == -2 || data[2] == -2 || data[3] == -2 || data[4] == -2) {
            command.command = "stable";  //悬停
            Log.e(LOG_TAG, "未发现黄色停止标志，保持悬停");
        } else {
            command.gaz = 0.0f;  //停止升降；
            double xThre = 0.1;  //允许飞行器偏离黄色圆中心x方向的最大相对距离
            double yThre = 0.1;  //允许飞行器偏离黄色圆中心x方向的最大相对距离
            if (Math.abs(data[3]) > xThre) {
                int sign = 1;//右移
                if (data[3] < 0) {    //黄圆圆心在图像中心的左边，飞行器左移
                    sign = -1;
                }
                command.roll = (float) data[3] / 5;
                if (sign > 0) {
                    Log.e(LOG_TAG, "飞行器右移靠近停止目标，速度：" + command.roll);
                } else {
                    Log.e(LOG_TAG, "飞行器左移靠近停止目标，速度：" + command.roll);
                }
            }
            if (Math.abs(data[4]) > yThre) {
                int sign = 1;   //前进
                if (data[4] < 0) {  //  黄圆圆心在图像中心的后面，飞行器后移
                    sign = -1;   //后退
                }
                command.pitch = (float) -data[4] / 5;   //pitch正直后退，负值前进
                if (sign > 0) {
                    Log.e(LOG_TAG, "飞行器后退靠近停止目标，速度：" + command.roll);
                } else {
                    Log.e(LOG_TAG, "飞行器前进靠近停止目标，速度：" + command.roll);
                }

            }

        }
        return command;
    }

    public static GameCommand seekStopSign(Bitmap bitmap) {
        GameCommand gameCommand = new GameCommand();
        double xThre = 0.1;  //允许飞行器偏离黄色圆中心x方向的最大相对距离
        double yThre = 0.1;  //允许飞行器偏离黄色圆中心x方向的最大相对距离
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Mat matYellow = new Mat();
        Utils.bitmapToMat(bitmap, matYellow);
        matYellow = ImageProcessor.hsvFilter(matYellow,ColorType.YELLOW);
        double[] data = ImageProcessor.lookForBall(matYellow, ColorType.YELLOW);
        if (data[0] == -2 || data[1] == -2 || data[2] == -2 || data[3] == -2 || data[4] != -2) {//未找到圆形黄色停止标志
            gameCommand.colorType = ColorType.RED;
        } else {  //如果找到黄色停止标记
            gameCommand.colorType = ColorType.YELLOW;
            gameCommand.gaz = 0.0f;  //停止升降；
            gameCommand.taskMode=TaskMode.FOLLOWPATH;
            gameCommand.relativePosition[0]=data[3];
            gameCommand.relativePosition[0]=data[4];
            if (Math.abs(data[3]) > xThre) {
                int sign = 1;//右移
                if (data[3] < 0) {    //黄圆圆心在图像中心的左边，飞行器左移
                    sign = -1;
                }
                gameCommand.roll = (float) data[3] / 5;
                if (sign > 0) {
                    Log.e(LOG_TAG, "飞行器右移靠近黄色停止目标，速度：" + gameCommand.roll);
                } else {
                    Log.e(LOG_TAG, "飞行器左移靠近黄色停止目标，速度：" + gameCommand.roll);
                }
            }
            if (Math.abs(data[4]) > yThre) {
                int sign = 1;   //前进
                if (data[4] < 0) {  //  黄圆圆心在图像中心的后面，飞行器后移
                    sign = -1;   //后退
                }
                gameCommand.pitch = (float) -data[4] / 5;   //pitch正直后退，负值前进
                if (sign > 0) {
                    Log.e(LOG_TAG, "飞行器后退靠近黄色停止目标，速度：" + gameCommand.roll);
                } else {
                    Log.e(LOG_TAG, "飞行器前进靠近黄色停止目标，速度：" + gameCommand.roll);
                }

            }


        }
        return gameCommand;
    }
}
