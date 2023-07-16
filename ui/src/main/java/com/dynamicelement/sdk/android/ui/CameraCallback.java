package com.dynamicelement.sdk.android.ui;

import com.dynamicelement.sdk.android.exceptions.ExceptionType;

public interface CameraCallback {
    /**
     * The camera image gets processed in the background and the result is obtained in this callback.
     * This is a callback from background thread. In order to update UI elements on this method, you need to call 'runOnUiThread(() -> {})'.
     * For each 1000 ms(default value), the camera image gets processed and the resultant bitmap will be obtained in this method.
     * When the input 'selectBarcodeMode' is enabled, if any SQR is present, it will be decoded and the result text will be obtained.
     *
     * @param barcodeResult is the decoded barcode text if the SQR code is present in the camera frame.
     *                      (When the input 'selectBarcodeMode' is enabled). Otherwise, it will be null.
     */
    void onBarcodeObtained(String barcodeResult);

    /**
     * @param type is the type of exception.
     * @param e    is the actual exception.
     */
    void onError(ExceptionType type, Exception e);
}
