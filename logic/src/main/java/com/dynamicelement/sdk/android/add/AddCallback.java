package com.dynamicelement.sdk.android.add;

import android.graphics.Bitmap;

import com.dynamicelement.mddi.AddStreamResponse;
import com.dynamicelement.sdk.android.exceptions.ExceptionType;

import java.util.List;

/**
 * Callback Interface - Add Client Task
 */
public interface AddCallback {
    /**
     * @param addStreamResponse is the add stream response for each MDDI request.
     * @param result            contains additional information like ping, add status, current
     *                          count etc.
     */
    void onNextResponse(AddStreamResponse addStreamResponse, AddResult result);

    /**
     * @param elapsedTime        is the time calculated between the first MDDI request and the
     *                           last MDDI request.
     * @param summaryMessage     is the collection of log messages from the add stream responses.
     */
    void onCompleted(String elapsedTime, String summaryMessage);

    /**
     * @param exceptionType is the type of exception.
     * @param e             is the actual exception.
     */
    void onError(ExceptionType exceptionType, Exception e);
}
