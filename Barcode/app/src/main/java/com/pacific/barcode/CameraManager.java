package com.pacific.barcode;

import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * This class is for android targets below 5.0 and it uses old camera api
 */
public class CameraManager extends BaseCameraManager implements Camera.AutoFocusCallback, Camera.PreviewCallback {

    private Camera camera;

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        if (hook || isRelease) return;
        camera.setOneShotPreviewCallback(this);
    }

    @Override
    public void connectCamera(SurfaceHolder surfaceHolder) {
        if(!isRelease) return;
        try {
            camera = Camera.open();
            isRelease = false;
            camera.setPreviewDisplay(surfaceHolder);
            setCameraParameter();
            camera.startPreview();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void releaseCamera() {
        if (isRelease) return;
        isRelease = true;
        camera.cancelAutoFocus();
        camera.stopPreview();
        try {
            camera.setPreviewDisplay(null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        camera.release();
        camera = null;
    }

    @Override
    public void startCapture() {
        if (hook || isRelease || executor.isShutdown()) return;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                camera.autoFocus(CameraManager.this);
            }
        });
    }

    @Override
    public void setCameraParameter() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(0, cameraInfo);
        int degrees = 0;
        switch (rotate) {
            case Surface.ROTATION_0: {
                degrees = 0;
                break;
            }
            case Surface.ROTATION_90: {
                degrees = 90;
                break;
            }
            case Surface.ROTATION_180: {
                degrees = 180;
                break;
            }
            case Surface.ROTATION_270: {
                degrees = 270;
                break;
            }
        }

        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            displayOrientation = (cameraInfo.orientation + degrees) % 360;
            displayOrientation = (360 - displayOrientation) % 360;
        } else {
            displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
        }

        /** Warning : may throw exception with parameters not supported */
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size bestSize = previewSizes.get(0);
        for (int i = 1; i < previewSizes.size(); i++) {
            if (previewSizes.get(i).width * previewSizes.get(i).height > bestSize.width * bestSize.height) {
                bestSize = previewSizes.get(i);
            }
        }
        parameters.setPreviewSize(bestSize.width, bestSize.height);

        List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
        bestSize = pictureSizes.get(0);
        for (int i = 1; i < pictureSizes.size(); i++) {
            if (pictureSizes.get(i).width * pictureSizes.get(i).height > bestSize.width * bestSize.height) {
                bestSize = pictureSizes.get(i);
            }
        }
        parameters.setPictureSize(bestSize.width, bestSize.height);
        camera.setParameters(parameters);
        camera.setDisplayOrientation(displayOrientation);
    }

    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        if (hook || executor.isShutdown())return;
        Observable
                .just(camera.getParameters().getPreviewSize())
                .subscribeOn(Schedulers.from(executor))
                .map(new Func1<Camera.Size, QRResult>() {
                    @Override
                    public QRResult call(Camera.Size size) {
                        return getCodeValue(data, new Point(size.width, size.height));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<QRResult>() {
                    @Override
                    public void call(QRResult qrResult) {
                        if (qrResult == null) {
                            count++;
                            startCapture();
                            return;
                        }
                        vibrate();
                        if (onResultListener != null) {
                            onResultListener.onResult(qrResult);
                        }
                        count = 0;
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("CameraManager", "getCodeValue() failed .");
                    }
                });
    }
}
