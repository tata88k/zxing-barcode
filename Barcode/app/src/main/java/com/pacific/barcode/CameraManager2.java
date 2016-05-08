package com.pacific.barcode;

import android.annotation.TargetApi;
import android.content.Context;
import android.view.SurfaceHolder;

/**
 * This class is for android targets android 5.0 or above and it uses camera2 api
 */
@TargetApi(21)
public class CameraManager2 extends BaseCameraManager {

    public CameraManager2(Context context) {
        super(context);
    }

    @Override
    public void connectCamera(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void setCameraParameter() {

    }

    @Override
    public void startCapture() {

    }

    @Override
    public void releaseCamera() {

    }
}
