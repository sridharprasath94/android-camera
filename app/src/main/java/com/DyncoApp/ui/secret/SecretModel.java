package com.DyncoApp.ui.secret;

import static android.content.Context.MODE_PRIVATE;
import static com.DyncoApp.ui.common.Constants.DEFAULT_SHOW_SCORE;
import static com.DyncoApp.ui.common.Constants.DEFAULT_TOGGLE_FLASH;

import android.content.Context;
import android.content.SharedPreferences;

import com.DyncoApp.R;

public class SecretModel {
    private final String KEY_TOGGLE_FLASH;
    private final String KEY_SHOW_SCORE;

    private final SharedPreferences sharedPreferences;

    public SecretModel(Context context) {
        KEY_TOGGLE_FLASH = context.
                getString(R.string.secret_screen_flash);
        KEY_SHOW_SCORE = context.
                getString(R.string.secret_screen_score);
        sharedPreferences = context.getSharedPreferences(context.
                getString(R.string.shared_preferences), MODE_PRIVATE);
    }

    boolean getSavedToggleFlash() {
        return sharedPreferences.getBoolean(KEY_TOGGLE_FLASH, DEFAULT_TOGGLE_FLASH);
    }

    boolean getSavedShowScore() {
        return sharedPreferences.getBoolean(KEY_SHOW_SCORE, DEFAULT_SHOW_SCORE);
    }

    void saveToggleFlash(boolean flash) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_TOGGLE_FLASH, flash).apply();
    }

    void saveShowScore(boolean showScore) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_SHOW_SCORE, showScore).apply();
    }
}
