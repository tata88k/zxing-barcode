package com.pacific.barcode;

import android.graphics.Bitmap;

import com.google.zxing.Result;

/**
 * Created by UsherBaby on 2015/12/4.
 */
public class QRResult {
    private Bitmap bitmap;
    private Result result;

    public QRResult(Bitmap bitmap, Result result) {
        this.bitmap = bitmap;
        this.result = result;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }
}
