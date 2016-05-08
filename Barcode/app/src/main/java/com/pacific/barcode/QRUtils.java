package com.pacific.barcode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Vibrator;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

/**
 * Created by UsherBaby on 2015/12/3.
 */
public class QRUtils {

    /**
     * decode a image file.
     *
     * @param url    image file path
     * @param reader Z_X_ing MultiFormatReader
     */
    public static QRResult decode(String url, MultiFormatReader reader) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(url, options);
            if (options.outWidth >= 1920) {
                options.inSampleSize = 6;
            } else if (options.outWidth >= 1280) {
                options.inSampleSize = 5;
            } else if (options.outWidth >= 1024) {
                options.inSampleSize = 4;
            } else if (options.outWidth >= 960) {
                options.inSampleSize = 3;
            }
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeFile(url);
            if (bitmap == null) return null;
            int width = bitmap.getWidth(), height = bitmap.getHeight();
            int[] pixels = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            Result result = decode(new RGBLuminanceSource(width, height, pixels), reader);
            if (result != null) {
                return new QRResult(bitmap, result);
            }
            bitmap.recycle();
            return null;
        } catch (Exception e) {
            Log.e("decode exception", e.toString());
            return null;
        }
    }

    /**
     * decode a LuminanceSource bitmap.
     *
     * @param source LuminanceSource bitmap
     * @param reader Z_X_ing MultiFormatReader
     */
    public static Result decode(LuminanceSource source, MultiFormatReader reader) {
        Result result = null;
        if (source != null) {
            BinaryBitmap bBitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                result = reader.decodeWithState(bBitmap);
            } catch (ReaderException e) {
                result = null;
            } finally {
                reader.reset();
            }
        }
        return result;
    }

    public static void vibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(300);
    }
}
