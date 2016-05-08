package com.pacific.barcode;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.SurfaceHolder;

import com.google.zxing.MultiFormatReader;
import com.pacific.mvc.Activity;
import com.trello.rxlifecycle.ActivityEvent;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class QRActivity extends Activity<QRModel>{
    public static final int CODE_PICK_IMAGE = 0x100;
    private BaseCameraManager cameraManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        if (Build.VERSION_CODES.LOLLIPOP >= Build.VERSION.SDK_INT) {
            cameraManager = new CameraManager(getApplication());
        } else {
            cameraManager = new CameraManager(getApplication());
        }
        model = new QRModel(new QRView(this));
        model.onCreate();

        cameraManager.setOnResultListener(new BaseCameraManager.OnResultListener() {
            @Override
            public void onResult(QRResult qrResult) {
                model.resultDialog(qrResult);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        model.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        model.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraManager.releaseCamera();
        cameraManager.shutdownExecutor();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CODE_PICK_IMAGE) {
            String[] columns = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(data.getData(), columns, null, null, null);
            if (cursor.moveToFirst()) {
                Observable
                        .just(cursor.getString(cursor.getColumnIndex(columns[0])))
                        .observeOn(Schedulers.from(cameraManager.getExecutor()))
                        .compose(this.<String>bindUntilEvent(ActivityEvent.PAUSE))
                        .map(new Func1<String, QRResult>() {
                            @Override
                            public QRResult call(String str) {
                                return QRUtils.decode(str, new MultiFormatReader());
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<QRResult>() {
                            @Override
                            public void call(QRResult qrResult) {
                                model.resultDialog(qrResult);
                            }
                        });
            }
            cursor.close();
        }
    }

    public void onSurfaceCreated(SurfaceHolder surfaceHolder) {
        if (cameraManager.getExecutor().isShutdown()) return;
        Observable
                .just(surfaceHolder)
                .compose(this.<SurfaceHolder>bindUntilEvent(ActivityEvent.PAUSE))
                .observeOn(Schedulers.from(cameraManager.getExecutor()))
                .map(new Func1<SurfaceHolder, Object>() {
                    @Override
                    public Object call(SurfaceHolder holder) {
                        cameraManager.setRotate(getWindowManager().getDefaultDisplay().getRotation());
                        cameraManager.connectCamera(holder);
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        model.setEmptyViewVisible(false);
                        cameraManager.startCapture();
                    }
                });
    }

    public void onSurfaceDestroyed(){
        cameraManager.releaseCamera();
    }

    public void restartCapture() {
        cameraManager.startCapture();
    }

    public void setHook(boolean hook) {
        cameraManager.setHook(hook);
    }
}
