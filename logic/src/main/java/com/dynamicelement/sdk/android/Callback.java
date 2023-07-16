package com.dynamicelement.sdk.android;

import com.dynamicelement.sdk.android.exceptions.ExceptionType;

/**
 * Callback Interface - For Ping, GetSample, Delete, CreateCollection Tasks.
 */
public interface Callback<R> {
    /**
     * @param response is the returned value from Ping, GetSample, Delete, CreateCollection Tasks.
     *                 Depending on each tasks, different responses are returned.
     */
    void onResponse(R response);

    /**
     * @param exceptionType is the type of exception.
     * @param e             is the actual exception.
     */
    void onError(ExceptionType exceptionType, Exception e);
}
