package com.DyncoApp.ui.home;

import static android.content.Context.MODE_PRIVATE;
import static com.DyncoApp.ui.common.Constants.DEFAULT_LOGO_PRESS_COUNT;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.DyncoApp.R;
import com.dynamicelement.sdk.android.Callback;
import com.dynamicelement.sdk.android.mddiclient.ClientService;
import com.dynamicelement.sdk.android.ping.PingResult;

public class HomeScreenModel {
    private final String KEY_SPINNER_POSITION;
    private static final int DEF_SPINNER_POSITION = 1;
    private final Context context;
    private final ClientService clientService;
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;
    private int logoPress;

    public HomeScreenModel(Context context, ClientService clientService) {
        this.context = context;
        this.clientService = clientService;
        this.logoPress = 0;
        KEY_SPINNER_POSITION = context.getString(R.string.home_screen_spinner_position);

        checkStoragePermission();
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.shared_preferences), MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public Integer getSpinnerPosition() {
        return sharedPreferences.getInt(KEY_SPINNER_POSITION, DEF_SPINNER_POSITION);
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (context instanceof Activity) {
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        50);
            }
        }
    }

    public void onLogoPressActionCompleted(Runnable completionRunnable) {
        logoPress++;
        if (logoPress == DEFAULT_LOGO_PRESS_COUNT) {
            logoPress = 0;
            if (completionRunnable != null) {
                completionRunnable.run();
            }
        }
    }

    public void saveData(int spinnerPosition) {
        editor.putInt(KEY_SPINNER_POSITION, spinnerPosition).apply();
    }

    public void checkConnection(Callback<PingResult> pingResultCallback) {
        HandlerThread connectionHandlerThread = new HandlerThread("connection handler");
        connectionHandlerThread.start();
        Handler connectionHandler = new Handler(connectionHandlerThread.getLooper());
        connectionHandler.post(() -> clientService.checkConnection(pingResultCallback));
    }
}