package com.DyncoApp.mddiutils;

import com.mddi.exceptions.ExceptionType;

import java.io.File;

public interface CheckFilesCallback {

    void onCorrectFormat(File[] jpgFiles, File[] txtFiles);
    void onError(ExceptionType exceptionType, Exception e);
}
