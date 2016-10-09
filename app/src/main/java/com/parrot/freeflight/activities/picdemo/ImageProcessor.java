package com.parrot.freeflight.activities.picdemo;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.parrot.freeflight.R;
import com.parrot.freeflight.activities.task.TaskCommand;
import com.parrot.freeflight.activities.task.TaskMode;
import com.parrot.freeflight.ui.gl.GLBGVideoSprite;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;


//import java.lang.Object;

/**
 * Created by shisy13 on 16/8/22.
 */

public class ImageProcessor {
    Context context;
    static GLBGVideoSprite glbgVideoSprite;

    static final String LOG_TAG = ImageProcessor.class.getSimpleName();

    public ImageProcessor() {
    }

    public ImageProcessor(Context context) {
        this.context = context;
        glbgVideoSprite = new GLBGVideoSprite(context.getResources());
        glbgVideoSprite.setAlpha(1.0f);
    }


    static Bitmap processImage(Bitmap image) {

        int width = image.getWidth();
        int height = image.getHeight();
        // Bitmap bitmap = image.copy(Bitmap.Config.ARGB_8888, false);
        Bitmap bitmap;
//        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);/
        bitmap = convertToBlackWhite(image, ColorType.BLUE);


//        Mat mat=new Mat();
//        Utils.bitmapToMat(image,mat);
//       mat=hsvFilter(mat,ColorType.RED);
//       mat=findCircles(mat);
//       Utils.matToBitmap(mat,bitmap);
//        bitmap=findBall(image,ColorType.RED);
        return bitmap;
    }

    /**
     * @param bmp RGB图像
     * @return 红球被画圈圈出的RGB图
     */
    public static Bitmap findBall(Bitmap bmp, ColorType color) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        // Bitmap bitmap=Bitmap.createBitmap(bmp);///会改变原图像！！
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Mat rgbMat = new Mat();
        Utils.bitmapToMat(bmp, rgbMat);
        Mat mat;
        mat = hsvFilter(rgbMat, color);
        double[] data = lookForBall(mat, color);   //查找指定颜色的球
        Point pt = new Point(Math.round(data[0]), Math.round(data[1]));
        int radius = (int) Math.round(data[2]);
        if (radius > 0)
            Imgproc.circle(rgbMat, pt, radius, new Scalar(0, 0, 255, 255), 16);//16像素的蓝色小球
        //此处Scalar四个参数依次为R,G,B,A,最后一个参数为0时,全显示白色
        Utils.matToBitmap(rgbMat, bitmap);
        if (rgbMat != null) {
            rgbMat.release();
        }
        return bitmap;
    }

    /**
     * 路径在图像中一般呈平行四边形，计算形心的位置
     * 目的是：根据形心与图像中心的差，动态调整四旋翼的路径
     * 输入：经过处理后的二色rgb图像（红与黑）
     * 以图形中心为坐标中心，x轴向右，y轴向上，各自范围[-1,1]
     * 返回路径中心的坐标，[-1,1]之间
     * 若返回[-2,-2]，表示图像中白点少于100个，摄像机未拍到路径
     * 上下两部分
     */
    static public PointF[] centroid(Bitmap bmp) {
        //  PointF pointF = new PointF();
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int pixColor = 0; //像素信息
        int pixR = 0;
        int pixG = 0;
        int pixB = 0;
        int whiteUpNum = 0; //黑白图中，白点的总个数
        int whiteDownNum = 0; //黑白图中，白点的总个数
        PointF[] pointFs = new PointF[3];
        pointFs[0] = new PointF();
        pointFs[1] = new PointF();
        pointFs[2] = new PointF();

        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);   //读取像素信息
        double centerXup = 0;  //形心的x坐标
        double centerXdown = 0;  //形心的x坐标
        double centerYup = 0; //形心的y坐标
        double centerYdown = 0; //形心的y坐标
        int halfHeight = height / 2;


        for (int i = 0; i < halfHeight; i++) {
            for (int j = 0; j < width; j++) {

                pixColor = pixels[i * width + j];
                pixR = Color.red(pixColor);
                pixG = Color.green(pixColor);
                pixB = Color.blue(pixColor);

                //如果红色通道大于0，则为红色，则累加centerx，centery,
                //否则为黑色，不累加
                if (pixR == 255 && pixG == 255 && pixB == 255) {
                    whiteUpNum = whiteUpNum + 1;
                    centerXup += j;
                    centerYup += (height - i);
                }

            }
        }

        centerXup = 2 * centerXup / whiteUpNum / width - 1;
        centerYup = 2 * centerYup / whiteUpNum / height - 1;
        pointFs[0].x = (float) centerXup;
        pointFs[0].y = (float) centerYup;
        Log.d(LOG_TAG, "whiteUpNum" + whiteUpNum);
        if (whiteUpNum < 1000) {
            pointFs[0].x = (float) -2.0;
            pointFs[0].y = (float) -2.0;
        }
        pointFs[2].x = whiteUpNum;

        for (int i = halfHeight; i < height; i++) {
            for (int j = 0; j < width; j++) {

                pixColor = pixels[i * width + j];
                pixR = Color.red(pixColor);
                pixG = Color.green(pixColor);
                pixB = Color.blue(pixColor);

                //如果红色通道大于0，则为红色，则累加centerx，centery,
                //否则为黑色，不累加
                if (pixR == 255 && pixG == 255 && pixB == 255) {
                    whiteDownNum = whiteDownNum + 1;
                    centerXdown += j;
                    centerYdown += (height - i);
                }

            }
        }
        centerXdown = 2 * centerXdown / whiteDownNum / width - 1;
        centerYdown = 2 * centerYdown / whiteDownNum / height - 1;
        pointFs[1].x = (float) centerXdown;
        pointFs[1].y = (float) centerYdown;

        Log.d(LOG_TAG, "whiteDownNum" + whiteDownNum);
        if (whiteDownNum < 1000) {
            pointFs[1].x = (float) -2.0;
            pointFs[1].y = (float) -2.0;

        }
        pointFs[2].y = whiteDownNum;
        return pointFs;
    }


    /**
     * hsv三通道滤波，提取特定颜色
     *
     * @param origin RGB图像对应的Mat
     * @return
     */
    static public Mat hsvFilter(Mat origin, ColorType color) {

        Mat originHSV = new Mat();
        /**对于参数Imgproc.COLOR_BGR2HSV， H,S ,V
         *红色H在 0-10，  156-180
         */
        Imgproc.cvtColor(origin, originHSV, Imgproc.COLOR_BGR2HSV, 3);  //参数Imgproc.COLOR_BGR2HSV将H通道映射为0--180，S:0--255，V:0--255
        //Imgproc.cvtColor(origin, originHSV, Imgproc.COLOR_BGR2HSV_FULL, 3);//参数Imgproc.COLOR_BGR2HSV将H通道映射为0--360，S:0--255，V:0--255
        Mat lower = new Mat();
        Mat upper = new Mat();
        Mat mat = new Mat();
        switch (color) {
            case RED:
                Core.inRange(originHSV, new Scalar(0, 80, 40), new Scalar(0, 255, 255), lower);  //By cui
                Core.inRange(originHSV, new Scalar(100, 100, 40), new Scalar(130, 255, 255), upper);//By cui
                Core.addWeighted(lower, 1.0, upper, 1.0, 0.0, mat);
                break;
            case YELLOW:
                Core.inRange(originHSV, new Scalar(70, 70, 70), new Scalar(100, 255, 255), mat);
                break;
            case BLUE:

                /**
                 * 这段可以很好地识别紫色，但是能识别红黄！！！
                 */
//                Core.inRange(originHSV, new Scalar(0, 80, 46), new Scalar(100, 255, 255), lower);  //By cui
//                Core.inRange(originHSV, new Scalar(100, 43, 46), new Scalar(230, 255, 255), upper);//By cui
//                Core.addWeighted(lower, 1.0, upper, 1.0, 0.0, mat);
                /**
                 * 很好地规避了黄，红色
                 */
//                Core.inRange(originHSV, new Scalar(0, 80, 46), new Scalar(10, 255, 255), lower);  //By cui
//                Core.inRange(originHSV, new Scalar(150, 43, 46), new Scalar(230, 255, 255), upper);//By cui
//                Core.addWeighted(lower, 1.0, upper, 1.0, 0.0, mat);

                Core.inRange(originHSV, new Scalar(0, 80, 46), new Scalar(10, 255, 255), lower);  //By cui
                Core.inRange(originHSV, new Scalar(170, 43, 46), new Scalar(200, 255, 255), upper);//By cui
                Core.addWeighted(lower, 1.0, upper, 1.0, 0.0, mat);
        }

        Imgproc.GaussianBlur(mat, mat, new Size(9, 9), 2, 2);  //高斯滤波
        origin.release();
        return mat;
    }

    /**
     * 查找直线，返回两个端点的像素坐标
     *
     * @param bmp rgb图像对应的矩阵
     * @return point[0]位于point[1]下方
     */
    static public Point[] findLinesP(Mat bmp, ColorType colorType) {

        Mat blackWhite = hsvFilter(bmp, colorType);   //hsv滤波处理
        Mat lines = new Mat();
        Imgproc.HoughLinesP(blackWhite, lines, 1, Math.PI / 180, 50, 200, 200);  //霍夫变换查找边缘直线
        Point start;
        Point end;
        double length = 0.0;
        Point[] points = new Point[2];
        if (lines.cols() == 0) {
            return null;
        }
        for (int x = 0; x < lines.cols(); x++) //查找最长的直线
        {
            double[] vec = lines.get(0, x);
            double x1 = vec[0],
                    y1 = vec[1],
                    x2 = vec[2],
                    y2 = vec[3];
            start = new Point(x1, y1);
            end = new Point(x2, y2);
            double tmpl = Math.pow(start.x - end.x, 2) + Math.pow(start.y - end.y, 2);
            if (tmpl > length) {
                points[0] = start;
                points[1] = end;
                length = tmpl;
            }
        }
        if (points[0].y < points[1].y) {   //保证point[0]位于point[1]下方
            double tmp = points[0].y;
            points[0].y = points[1].y;
            points[1].y = tmp;
            tmp = points[0].x;
            points[0].x = points[1].x;
            points[1].x = tmp;
        }
        return points;
    }

    /**
     * @param blackWhite 经过hsvFilter滤波的二值化图
     * @param color      需要寻找的球的颜色
     * @return 五元数组：球心x,y像素坐标，x,y相对对坐标，球的像素半径
     */
    public static double[] lookForBall(Mat blackWhite, ColorType color) {
        long timePre = System.currentTimeMillis();
        long timePos;
        int width = blackWhite.width();
        int height = blackWhite.height();
        /**
         * 腐蚀膨胀操作，会让识别更加精准，
         * 但是时间会从50ms升到400ms
         */
//        Mat erodeStruct = Imgproc.getStructuringElement(Imgproc.MORPH_ERODE, new Size(9, 9));
//        Mat dilateStruct = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(9, 9));
//        Imgproc.erode(blackWhite, blackWhite, erodeStruct);
//        Imgproc.dilate(blackWhite, blackWhite, dilateStruct);
        double[] Ball = {-2.0, -2.0, -2.0, -2.0, -2.0};
        int iCannyUpperThreshold = 100;
        int iMinRadius = 20;
        int iMaxRadius = 300;
        int iAccumulator = 50;

        double radius = 0.0;
        double scale = 0.5;
        Size size = new Size();
        size.height = 0;
        size.width = 0;
        double fx = scale;   //宽度缩放比例
        double fy = scale;  //长度缩放比例
        Mat resizedMat = new Mat();
        Imgproc.resize(blackWhite, resizedMat, size, fx, fy, Imgproc.INTER_LINEAR); //线性插值，缩小图形2倍
        //   Log.e(LOG_TAG + "_lookForBall", "开始定位" + color.getName() + "球！");
        Mat circles = new Mat();
        Imgproc.HoughCircles(resizedMat, circles, Imgproc.CV_HOUGH_GRADIENT, 2, 100, iCannyUpperThreshold, iAccumulator, iMinRadius, iMaxRadius); //hough变换找圆
        Log.e(LOG_TAG + "_lookForBall", "霍夫圆检测,共检测出 " + circles.cols() + "个" + color.getName() + "球");
        if (circles.cols() == 0) {  //如果未找到红球，直接返回
            Log.e(LOG_TAG + "_lookForBall", "定位" + color.getName() + "球失败!!!");
            return Ball;
        }
        if (circles.cols() > 1) {
            //    Log.e(LOG_TAG + "_lookForBall", "红球个数多于一个！将只定位半径最大的" + color.getName() + "球！");
        }
        //寻找半径最大的球
        for (int i = 0; i < circles.cols(); i++) {
            double circle[] = circles.get(0, i);
            if (circle[2] > radius) {
                Ball[0] = circle[0] / scale;//x坐标
                Ball[1] = circle[1] / scale;//y坐标
                Ball[2] = circle[2] / scale;//半径长度
                Ball[3] = 2.0 * Ball[0] / width - 1.0;
                Ball[4] = 1.0 - 2.0 * Ball[1] / height;
                radius = circle[2] / scale;
                Log.e(LOG_TAG + "_lookForBall", color.getName() + "球半径" + radius);
            }
        }
        //   Log.d(LOG_TAG + "_lookForBall", "已定位" + color.getName() + "球位置!");
        timePos = System.currentTimeMillis();
        long timeUsed = timePos - timePre;
        Log.e(LOG_TAG + "_lookForBall", "函数lookForBall用时：" + timeUsed + "毫秒");
        return Ball;
    }

    /**
     * @param bmp       四旋翼接收的rgb图像矩阵
     * @param colorType 停止标志的颜色
     * @return 是否切换任务模式的boolean变量
     */
    public static TaskCommand checkConvert(Mat bmp, ColorType colorType, TaskCommand taskCommand) {
        //如果已在跟踪小球，或者已经切换为寻找小球，就不再检测，直接返回
        if (taskCommand.taskMode == TaskMode.TRACKBALL | taskCommand.convertToSeekBall == true) {  //注意||是短路或，应该用|
            return taskCommand;
        }
        Mat blackWhite = hsvFilter(bmp, colorType);   //hsv滤波处理

        double[] data = lookForBall(blackWhite, colorType);
        double xThre = 0.2;
        double yThre = 0.2;  //圆停止标志中心在此范围内就切换模式
        double x = data[2];
        double y = data[3];
        if (Math.abs(x) > xThre | Math.abs(y) > yThre) {//注意||是短路或，应该用|
            taskCommand.convertToSeekBall = false;
        } else {
            taskCommand.convertToSeekBall = true;
            taskCommand.taskMode = TaskMode.TRACKBALL;
            Log.e(LOG_TAG, "####################################################################################################");
            Log.e(LOG_TAG, "####################################################################################################");
            Log.e(LOG_TAG, "已经切换为跟踪目标模式");
            Log.e(LOG_TAG, "已经切换为跟踪目标模式");
            Log.e(LOG_TAG, "已经切换为跟踪目标模式");
            Log.e(LOG_TAG, "已经切换为跟踪目标模式");
            Log.e(LOG_TAG, "已经切换为跟踪目标模式");
            Log.e(LOG_TAG, "已经切换为跟踪目标模式");
            Log.e(LOG_TAG, "已经切换为跟踪目标模式");
            Log.e(LOG_TAG, "已经切换为跟踪目标模式");
            Log.e(LOG_TAG, "已经切换为跟踪目标模式");
            Log.e(LOG_TAG, "已经切换为跟踪目标模式");
            Log.e(LOG_TAG, "已经切换为跟踪目标模式");
            Log.e(LOG_TAG, "已经切换为跟踪目标模式");
            Log.e(LOG_TAG, "已经切换为跟踪目标模式");
            Log.e(LOG_TAG, "已经切换为跟踪目标模式");
            Log.e(LOG_TAG, "已经切换为跟踪目标模式");
            Log.e(LOG_TAG, "####################################################################################################");
            Log.e(LOG_TAG, "####################################################################################################");

        }
        return taskCommand;
    }

    /**
     * 根据askCommand状态，做相应的动作
     * 这里调用响铃
     *
     * @param taskCommand
     */
    public TaskCommand convertToSeekBall(TaskCommand taskCommand) {

//    try{
//        Thread.sleep(2000);
//    }
//    catch (Exception e){
//        e.printStackTrace();
//    }
        return taskCommand;
    }


    /**
     * 用于视频帧指定的色彩提取出来，显示为白色，其余的显示为黑色
     *
     * @param video     待处理的视频帧
     * @param colorType 要被提取出来的色彩，只有RED和YELLOW可用
     * @return
     */
    static public Bitmap convertToBlackWhite(Bitmap video, ColorType colorType) {
        int width = video.getWidth();
        int height = video.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Mat mat = new Mat();
        Utils.bitmapToMat(video, mat);
        Mat blackWhite = hsvFilter(mat, colorType);
        Utils.matToBitmap(blackWhite, bitmap);
        //video.recycle();
        // System.gc();
        return bitmap;

    }

    public static void TestForFindBall(Bitmap bitmap, ColorType colorType) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        mat = hsvFilter(mat, colorType);
        double[] data = lookForBall(mat, colorType);

        if (data[0] == -2 || data[1] == -2 || data[2] == -2 || data[3] == -2 || data[4] == -2) {
            Log.e(LOG_TAG + "_TestForFindBall", "未找到小球！！");
            return;
        }
        Log.e(LOG_TAG + "_TestForFindBall", "原图像宽为：" + bitmap.getWidth());
        Log.e(LOG_TAG + "_TestForFindBall", "原图像高为：" + bitmap.getHeight());
        Log.e(LOG_TAG + "_TestForFindBall", "球心像素X坐标：" + data[0]);
        Log.e(LOG_TAG + "_TestForFindBall", "球心像素Y坐标：" + data[1]);
        Log.e(LOG_TAG + "_TestForFindBall", "球心相对于图像中心坐标系X坐标：" + data[3]);
        Log.e(LOG_TAG + "_TestForFindBall", "球心相对于图像中心坐标系Y坐标：" + data[4]);
    }

}