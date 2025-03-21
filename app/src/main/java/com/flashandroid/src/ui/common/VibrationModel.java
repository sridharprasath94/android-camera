package com.flashandroid.src.ui.common;

import static android.content.Context.VIBRATOR_SERVICE;
import static com.flashandroid.src.ui.common.Constants.VIBRATION_MS;

import android.app.Activity;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class VibrationModel {
    private final Vibrator vibrator;

    public VibrationModel(Activity activity) {
        this.vibrator = (Vibrator) activity.getSystemService(VIBRATOR_SERVICE);
    }

    public void createVibration() {
        createVibration(VIBRATION_MS);
    }

    public void createVibration(long milliseconds) {
        vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
    }
}
