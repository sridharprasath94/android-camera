package com.flashandroid.sdk.ui;

import android.graphics.Bitmap;

import com.flashandroid.sdk.misc.exceptions.ExceptionType;

public interface CameraCallback {
    /**
     * @param bitmap is the actual camera image in full resolution in bitmap format.
     */
    void onImageObtained(Bitmap bitmap, String barcodeResult);

    /**
     * @param type is the type of exception.
     * @param e    is the actual exception.
     */
    void onError(ExceptionType type, Exception e);
}
