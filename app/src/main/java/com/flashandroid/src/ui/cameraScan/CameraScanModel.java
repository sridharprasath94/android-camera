package com.flashandroid.src.ui.cameraScan;

import static android.content.Context.MODE_PRIVATE;
import static com.flashandroid.sdk.ui.CameraParameters.CameraRatioMode.RATIO_1X1;
import static com.flashandroid.src.ui.common.Constants.DEFAULT_TOGGLE_FLASH;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.flashandroid.R;
import com.flashandroid.sdk.misc.exceptions.ExceptionType;
import com.flashandroid.sdk.ui.CameraCallback;
import com.flashandroid.sdk.ui.CameraParameters;
import com.flashandroid.sdk.ui.CameraView;

import java.lang.ref.WeakReference;

public class CameraScanModel extends ViewModel {
    public Bitmap getCurrentBitmap() {
        return currentBitmap;
    }

    public MutableLiveData<Exception> getExceptionObserver() {
        return exceptionObserver;
    }

    private final WeakReference<Context> contextRef;
    private final String KEY_TOGGLE_FLASH;
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;
    private Bitmap currentBitmap;
    private final MutableLiveData<Exception> exceptionObserver = new MutableLiveData<>();

    public CameraScanModel(Context context) {
        this.contextRef = new WeakReference<>(context);
        KEY_TOGGLE_FLASH = context.
                getString(R.string.secret_screen_flash);
        sharedPreferences = context.getSharedPreferences(context.
                getString(R.string.shared_preferences), MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }


    boolean getSavedFlash() {
        return sharedPreferences.getBoolean(KEY_TOGGLE_FLASH, DEFAULT_TOGGLE_FLASH);
    }

    void saveFlash(boolean toggleFlash) {
        editor.putBoolean(KEY_TOGGLE_FLASH, toggleFlash).apply();
    }

    void initCamera(CameraView cameraView) {
        CameraParameters cameraParameters = new CameraParameters.Builder()
                .selectRatio(RATIO_1X1)
                .enableBarcodeScan(true)
                .enableDefaultLayout(false)
                .selectPrimaryCamera(false)
                .blurBeforeBarcode(true)
                .build();

        cameraView.initCameraCapture(cameraParameters, (Activity) this.contextRef.get(), new CameraCallback() {
            @Override
            public void onImageObtained(Bitmap bitmap, String barcodeResult) {
                currentBitmap = bitmap;
                Log.d("SRIDHAR_CAMERA_SCAN_MODEL", "onImageObtained: with barcode" + barcodeResult);
            }

            @Override
            public void onError(ExceptionType type, Exception e) {
                exceptionObserver.postValue(e);
            }
        });
    }
}
