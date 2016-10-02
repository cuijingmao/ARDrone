package com.parrot.freeflight.activities.firm;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.parrot.freeflight.activities.game.GameCommand;
import com.parrot.freeflight.activities.image.ColorType;
import com.parrot.freeflight.activities.image.ImageProcessor;
import com.parrot.freeflight.ui.gl.GLBGVideoSprite;
import com.parrot.freeflight.ui.hud.Image;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.Arrays;

/**
 * Created by shisy13 on 16/8/23.
 */
public class FirmImageToCommand {
    static private final String LOG_TAG = "ImageToCommand";

    Context context;
 static   GLBGVideoSprite glbgVideoSprite;

    static int imgWidth = 640;
    static int imgHeight = 360;

    int lastCnt = 0;

    public FirmImageToCommand(Context context){
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

    static public GameCommand pointToCommand(Point[] line){
        GameCommand command = new GameCommand();
//        if (line == null && (points[0].x < -1 && points[1].x < -1)) {
        if (line == null) {
            command.command = "stable";
        }
        else {
            float k = 100.0f;
            Point center = new Point();
            if (line == null){
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
            }
            else {
                k = (float) -((line[0].y - line[1].y) / (line[0].x - line[1].x));
                center = new Point((line[0].x+line[1].x)/imgWidth-1, 1-(line[0].y+line[1].y)/imgHeight);
            }


            float rollThre = 0.01f;
            float kThre = 3.0f;
            float pitchThre = 0.4f;
            Log.d(LOG_TAG, "point:"+center.x + "," + center.y);
            if (Math.abs(center.x) > rollThre){
                int sign = 1;
                if (center.x < 0)
                    sign = -1;
                float power = (float) (Math.pow(2, Math.abs(center.x))-1);
                power = (float) (Math.pow(2, power) - 1);
                power = power / 100;
//                power = (float) Math.abs(center.x)/100;
                command.roll = sign * power;
            }
            if (Math.abs(k) < kThre ){
                int sign = 1;
                if (k < 0)
                    sign = -1;
                float power = (float) (Math.pow(2, (2 - Math.abs(k))/2) - 1);
                command.yaw = sign * power / 4;
//                command.yaw = 0;
            }

            if (Math.abs(center.y) > pitchThre){
                int sign = 1;
                if (center.y > 0)
                    sign = -1;
                float power = (float) (Math.pow(2, Math.abs(center.y))-1);
                power = (float) (Math.pow(2, power) - 1);
                power = power / 100;
                command.pitch = sign * power;
            }
            else if (line[1].y < 50 && line[0].y > 200 || (command.roll == 0 && command.yaw == 0)){
                command.pitch = -0.005f;
            }

            if (line != null)
                Log.d(LOG_TAG, "line:"+line[0].x + "," + line[0].y + ";" + line[1].x + "," + line[1].y);
            else
                Log.d(LOG_TAG, "line:null");
        }


        Log.d(LOG_TAG, "command:" + command.pitch + "," + command.roll + "," + command.yaw);
        return command;
    }

    public GameCommand getCommand(){
        // read photo to bitmap
        Bitmap bitmap = loadImage();
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        // use CJM

        Point[] line = ImageProcessor.findLinesP(mat, ColorType.RED);
        PointF[] points = new PointF[3];
//        Mat hsv = ImageProcessor.hsvFilter(mat);
//        Utils.matToBitmap(hsv, bitmap);
//        points = ImageProcessor.centroid(bitmap);
        if (!bitmap.isRecycled()){
            bitmap.recycle();
            System.gc();
        }


        return pointToCommand(line);
    }


    public GameCommand pointToCommandBall(double[] data){
        GameCommand command = new GameCommand();

        if (data[2] == -2){
            command.command = "stable";
        }
        else {
            double gazThre = 0.0;
            double yawThre = 0.2;

            if (Math.abs(data[4]) > gazThre){
                int sign = 1;
                if (data[4] < 0)
                    sign = -1;
//                command.gaz = (float) (sign * (Math.pow(2, Math.abs(data[4])) - 1));
                command.gaz = (float) data[4];
            }
            if (Math.abs(data[3]) > yawThre){
                int sign = 1;
                if (data[3] < 0)
                    sign = -1;

                command.yaw = (float) data[3] / 2;

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

    public GameCommand getCommandBall(){
        Bitmap bitmap = loadImage();
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        Mat hsv = ImageProcessor.hsvFilterRed(mat);
//        Imgproc.resize(mat, mat, new Size(640, 320));
        double[] data = ImageProcessor.lookForRedBall(hsv);

        if (!bitmap.isRecycled()){
            bitmap.recycle();
            System.gc();
        }
        return pointToCommandBall(data);
    }




 public  static  Bitmap loadImage(){
        if (glbgVideoSprite.updateVideoFrame()){
            Log.e("注意！","glbgVideoSprite.updateVideoFrame()出错！");
        }
        while (!glbgVideoSprite.updateVideoFrame()){
        }
        Bitmap bitmap = glbgVideoSprite.getVideoBitmap();
     Log.e(LOG_TAG,"已经捕获到视频帧！");
     //commented by cui
       bitmap= ImageProcessor.convertToBlackWhite(bitmap,ColorType.RED);
     //commented by cui
//        imgWidth = bitmap.getWidth();
//        imgHeight = bitmap.getHeight();
//        saveBitmap(bitmap);
        return bitmap;
    }

    private void saveBitmap(Bitmap bitmap){


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
}
