package com.pacific.barcode;

import com.pacific.mvc.ActivityModel;

public class QRModel extends ActivityModel<QRView>{

    public QRModel(QRView view) {
        super(view);
    }

    public void resultDialog(QRResult qrResult){
        view.resultDialog(qrResult);
    }

    public void onResume() {
        view.setSurfaceViewVisible(true);
    }

    public void onPause() {
        view.setSurfaceViewVisible(false);
    }

    public void setEmptyViewVisible(boolean visible){
        view.setEmptyViewVisible(visible);
    }
}
