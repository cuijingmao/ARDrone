package com.parrot.freeflight.activities.image;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.parrot.freeflight.R;
import com.parrot.freeflight.ui.hud.Image;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageActivity extends Activity {
    final String LOG_TAG = getClass().getSimpleName();

    ImageProcessor imageProcessor;

    ImageView imageBefore;
    ImageView imageAfter;
    Bitmap before;
    Bitmap after;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;

        imageProcessor = new ImageProcessor();
        setContentView(R.layout.activity_image);
        imageBefore = (ImageView) findViewById(R.id.image_before);
        imageAfter = (ImageView) findViewById(R.id.image_after);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(screenWidth /2, screenHeight);
        imageBefore.setLayoutParams(layoutParams);
        imageAfter.setLayoutParams(layoutParams);

        before = BitmapFactory.decodeResource(getResources(), R.drawable.blueball3);
        imageBefore.setImageBitmap(before);
    }

    @Override
    protected void onResume() {
        super.onResume();

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS: {
                        Log.i(LOG_TAG, "OpenCV loaded successfully");

                        after = imageProcessor.processImage(before);
                        imageAfter.setImageBitmap(after);
                      //  ImageProcessor.TestForFindBall(before,ColorType.YELLOW);   //测试lookForRedBall函数
                        //    saveImageToGallery(getBaseContext(),after);
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

    @Override
    protected void onPause() {
        if (!before.isRecycled())
            before.recycle();
        if (!after.isRecycled())
            after.recycle();
        System.gc();
        super.onPause();
    }

    public static void saveImageToGallery(Context context, Bitmap bmp) {
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), "ArdroneCui");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + appDir)));
    }

}
