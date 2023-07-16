package com.DyncoApp.ui.common;

public interface CompletionCallback<R> {
    void onSuccess(R response);

    void onError(Exception e);

    default void showAlert(String alertMessage) {
    }
}
