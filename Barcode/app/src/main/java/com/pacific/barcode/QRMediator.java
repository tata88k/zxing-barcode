package com.pacific.barcode;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pacific.common.ActivityMediator;
import com.pacific.common.AndroidUtils;

/**
 * Created by UsherBaby on 2016/2/18.
 */
public class QRMediator extends ActivityMediator<QRActivity> implements BaseCameraManager.OnResultListener {

    private final int CODE_PICK_IMAGE = 0x100;
    private QRCodeView qrCodeView;
    private SurfaceView surfaceView;

    public QRMediator(QRActivity activity) {
        super(activity);
    }

    @Override
    protected void findView() {
        surfaceView = retrieveView(R.id.sv_preview);
        qrCodeView = retrieveView(R.id.qr_view);
    }

    @Override
    protected void addListener() {
        qrCodeView.setPickImageListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.setHook(true);
                Intent galleryIntent = new Intent();
                if (AndroidUtils.is_19()) {
                    galleryIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                } else {
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                }
                galleryIntent.setType("image/*");
                Intent wrapperIntent = Intent.createChooser(galleryIntent, "选择二维码图片");
                activity.startIntentForResult(wrapperIntent, CODE_PICK_IMAGE, null);
            }
        });
        surfaceView.getHolder().addCallback(activity);
    }

    @Override
    protected void bindAdapter() {
    }

    @Override
    protected void initialize() {
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void onResult(QRResult qrResult) {
        if (qrResult == null) {
            new AlertDialog.Builder(activity)
                    .setTitle("No Barcode Result")
                    .setMessage("Can't decode barcode from target picture , \nplease confirm the picture has barcode value.")
                    .setPositiveButton("Ok", null)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            activity.setHook(false);
                            activity.restartCapture();
                        }
                    })
                    .create()
                    .show();
            return;
        }
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_result, null);
        if (!TextUtils.isEmpty(String.valueOf(qrResult.getResult()))) {
            ((TextView) view.findViewById(R.id.tv_value)).setText(String.valueOf(qrResult.getResult()));
        }
        if (qrResult.getBitmap() != null) {
            ((ImageView) view.findViewById(R.id.img_barcode)).setImageBitmap(qrResult.getBitmap());
        }
        new AlertDialog.Builder(activity)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        activity.setHook(false);
                        activity.restartCapture();
                    }
                })
                .setView(view)
                .create()
                .show();
    }

    public void setEmptyViewVisible(boolean visible) {
        if (visible) {
            retrieveView(R.id.v_empty).setVisibility(View.VISIBLE);
        } else {
            retrieveView(R.id.v_empty).setVisibility(View.GONE);
        }
    }

    public void setSurfaceViewVisible(boolean visible) {
        if (visible) {
            surfaceView.setVisibility(View.VISIBLE);
        } else {
            surfaceView.setVisibility(View.INVISIBLE);
        }
    }

    public int getPickImageCode() {
        return CODE_PICK_IMAGE;
    }
}
