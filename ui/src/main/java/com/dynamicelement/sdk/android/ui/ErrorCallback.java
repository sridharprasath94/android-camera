package com.dynamicelement.sdk.android.ui;

import com.dynamicelement.sdk.android.exceptions.ExceptionType;

public interface ErrorCallback {

    /**
     * @param type is the type of exception.
     * @param e    is the actual exception.
     */
    void onError(ExceptionType type, Exception e);
}
