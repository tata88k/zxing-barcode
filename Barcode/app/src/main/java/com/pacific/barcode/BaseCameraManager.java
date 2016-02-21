package com.pacific.barcode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Vibrator;
import android.util.Log;
import android.view.SurfaceHolder;

import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.pacific.common.Application;
import com.pacific.common.ResourceUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by UsherBaby on 2016/2/19.
 */

public abstract class BaseCameraManager {
    private Point qrBoxSize;

    protected boolean hook = false;
    protected int rotate;
    protected int count = 0;
    protected boolean isRelease = true;
    protected ExecutorService executor;
    protected int displayOrientation;
    protected MultiFormatReader reader;
    protected OnResultListener onResultListener;

    public BaseCameraManager() {
        executor = Executors.newSingleThreadExecutor();
        reader = new MultiFormatReader();
        qrBoxSize = new Point();
        qrBoxSize.x = (int) ResourceUtils.getDimension(R.dimen.width_qr_box_view);
        qrBoxSize.y = (int) ResourceUtils.getDimension(R.dimen.height_qr_box_view);
    }

    public void vibrate() {
        Vibrator vibrator = (Vibrator) Application.getInstance().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(300);
    }


    protected QRResult getCodeValue(byte[] data, Point previewSize) {
        Bitmap bitmap = null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream(data.length);
        YuvImage image = new YuvImage(data, ImageFormat.NV21, previewSize.x, previewSize.y, null);
        int left = previewSize.x - qrBoxSize.x >> 1;
        int right = previewSize.x + qrBoxSize.x >> 1;
        int top = previewSize.y - qrBoxSize.y >> 1;
        int bottom = previewSize.y + qrBoxSize.y >> 1;
        Rect rect = new Rect(left, top, right, bottom);
        if (image.compressToJpeg(rect, 100, stream)) {
            byte[] bytes = stream.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
        try {
            stream.close();
        } catch (IOException e) {
            Log.e("onPreviewFrame", e.toString());
        }

        if (displayOrientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(displayOrientation);
            Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
            bitmap.recycle();
            bitmap = newBitmap;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        Result result = QRUtils.decode(new RGBLuminanceSource(width, height, pixels), reader);
        if (result != null) {
            return new QRResult(bitmap, result);
        } else {
            bitmap.recycle();
            return null;
        }
    }

    public void setHook(boolean hook) {
        this.hook = hook;
    }

    public void setOnResultListener(OnResultListener onResultListener) {
        this.onResultListener = onResultListener;
    }

    public void shutdownExecutor() {
        executor.shutdown();
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public void setRotate(int rotate) {
        this.rotate = rotate;
    }

    public abstract void connectCamera(SurfaceHolder surfaceHolder);

    public abstract void setCameraParameter();

    public abstract void startCapture();

    public abstract void releaseCamera();

    public interface OnResultListener {
        void onResult(QRResult qrResult);
    }
}
