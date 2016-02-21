package com.pacific.barcode;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.SurfaceHolder;

import com.google.zxing.MultiFormatReader;
import com.pacific.common.Activity;
import com.pacific.common.AndroidUtils;
import com.trello.rxlifecycle.ActivityEvent;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class QRActivity extends Activity implements SurfaceHolder.Callback {

    private QRMediator qrMediator;
    private BaseCameraManager cameraManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        qrMediator = new QRMediator(this);
        if (AndroidUtils.is_21()) {
            cameraManager = new CameraManager();
        } else {
            cameraManager = new CameraManager();
        }
        cameraManager.setOnResultListener(qrMediator);
    }

    @Override
    protected void onResume() {
        super.onResume();
        qrMediator.setSurfaceViewVisible(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        qrMediator.setSurfaceViewVisible(false);
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
        if (resultCode == RESULT_OK && requestCode == qrMediator.getPickImageCode()) {
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
                                qrMediator.onResult(qrResult);
                            }
                        });
            }
            cursor.close();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(cameraManager.getExecutor().isShutdown()) return;
        Observable
                .just(holder)
                .compose(this.<SurfaceHolder>bindUntilEvent(ActivityEvent.PAUSE))
                .observeOn(Schedulers.from(cameraManager.getExecutor()))
                .map(new Func1<SurfaceHolder, Object>() {
                    @Override
                    public Object call(SurfaceHolder surfaceHolder) {
                        cameraManager.setRotate(getWindowManager().getDefaultDisplay().getRotation());
                        cameraManager.connectCamera(surfaceHolder);
                        return null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        qrMediator.setEmptyViewVisible(false);
                        cameraManager.startCapture();
                    }
                });
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        qrMediator.setEmptyViewVisible(true);
        cameraManager.releaseCamera();
    }

    public void restartCapture() {
        cameraManager.startCapture();
    }

    public void setHook(boolean hook) {
        cameraManager.setHook(hook);
    }
}
