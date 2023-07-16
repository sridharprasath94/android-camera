package com.dynamicelement.sdk.android.search;

import android.graphics.Bitmap;

import com.dynamicelement.mddi.SearchStreamResponse;
import com.dynamicelement.sdk.android.exceptions.ExceptionType;

import java.util.List;

/**
 * Callback Interface - Search Task.
 */
public interface SearchCallBack {

    /**
     * @param searchStreamResponse is the search stream response for the current MDDI request.
     * @param searchResult         contains additional information like ping, current request count,
     *                             actual requested MDDI image, cid, sno, score, rating, uid and log message
     */
    void onPositiveResponse(SearchStreamResponse searchStreamResponse, SearchResult searchResult);

    /**
     * @param searchStreamResponse is the search stream response for the current MDDI request.
     * @param searchResult         contains additional information like ping, current request count,
     *                             actual requested MDDI image, cid, sno, score, rating, uid and log message
     */
    void onNegativeResponse(SearchStreamResponse searchStreamResponse, SearchResult searchResult);

    /**
     * @param elapsedTime           is the time calculated between the first MDDI request and the
     *                              last MDDI request.
     * @param summaryMessage        is the collection of log messages from the search stream
     *                              responses.
     */

    void onSearchCompleted(String elapsedTime,
                           String summaryMessage);

    /**
     * @param exceptionType is the type of exception.
     * @param e             is the actual exception.
     */
    void onError(ExceptionType exceptionType, Exception e);
}
