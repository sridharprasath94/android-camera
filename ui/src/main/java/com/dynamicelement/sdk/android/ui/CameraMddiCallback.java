package com.dynamicelement.sdk.android.ui;

import android.graphics.Bitmap;

import com.dynamicelement.mddi.SearchStreamResponse;
import com.dynamicelement.sdk.android.exceptions.ExceptionType;
import com.dynamicelement.sdk.android.search.SearchResult;

public interface CameraMddiCallback {

    void onInitialised(int viewWidth, int viewHeight);

    /**
     * The camera image gets processed in the background and the result is obtained in this callback.
     * This is a callback from background thread. In order to update UI elements on this method, you need to call 'runOnUiThread(() -> {})'.
     * For each 500 ms(default value), the camera image gets processed and the resultant bitmap will be obtained in this method.
     * Also, if any barcode is present for DB-SNO instance type, it will be also decoded and the result will be obtained.
     *
     * @param bitmap        is the actual camera image in full resolution in bitmap format.
     * @param barcodeResult for the instance type DB-SNO, if the SQR code is present in the camera frame,
     *                      the barcode result is obtained. For the other instance types, the barcode result.
     *                      will be null.
     * @param cid           is the cid of the current MDDI request.
     * @param sno           is the sno of the current MDDI request.
     */
    void onImageObtained(Bitmap bitmap, String barcodeResult, String cid, String sno);

    /**
     * MDDI request will happen in the background and the response is obtained in this callback.
     * This is a callback from background thread. In order to update UI elements on this method, you need to call 'runOnUiThread(() -> {})'.
     * When the MDDI search is enabled, the search stream response from the server is obtained for negative responses(Non matched MDDI image).
     * When the MDDI search is disabled, this method will not be called.
     *
     * @param searchStreamResponse is the actual search stream response for the non matching images.
     * @param searchResult         contains additional information like ping, current request count,
     *                             actual requested MDDI image, cid, sno, score, rating, uid and log message
     */
    void onNegativeResponse(SearchStreamResponse searchStreamResponse, SearchResult searchResult);

    /**
     * MDDI request will happen in the background and the response is obtained in this callback.
     * This is a callback from background thread. In order to update UI elements on this method, you need to call 'runOnUiThread(() -> {})'.
     * When the MDDI search is enabled, the search stream response from the server is obtained for positive responses(Matched MDDI image).
     * When the MDDI search is disabled, this method will not be called.
     *
     * @param searchStreamResponse is the search stream response for the positive matched image.
     * @param searchResult         contains additional information like ping, current request count,
     *                             actual requested MDDI image, cid, sno, score, rating, uid and log message
     */
    void onPositiveResponse(SearchStreamResponse searchStreamResponse, SearchResult searchResult);

    default void onCompleted(String elapsedTime, String summaryMessage) {
        // Default implementation does nothing
    }
    /**
     * @param type is the type of exception.
     * @param e    is the actual exception.
     */
    void onError(ExceptionType type, Exception e);
}
