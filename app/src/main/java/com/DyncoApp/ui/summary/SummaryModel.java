package com.DyncoApp.ui.summary;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.DyncoApp.R;
import com.DyncoApp.ui.common.Constants;

public class SummaryModel {
    private final String KEY_SHOW_SCORE;

    private final SharedPreferences sharedPreferences;

    public SummaryModel(Context context) {
        KEY_SHOW_SCORE = context.
                getString(R.string.secret_screen_score);
        sharedPreferences = context.getSharedPreferences(context.
                getString(R.string.shared_preferences), MODE_PRIVATE);
    }

    boolean getSavedShowScore() {
        return sharedPreferences.getBoolean(KEY_SHOW_SCORE, Constants.DEFAULT_SHOW_SCORE);
    }
}