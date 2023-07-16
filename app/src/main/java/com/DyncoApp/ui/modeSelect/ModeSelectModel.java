package com.DyncoApp.ui.modeSelect;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.DyncoApp.R;
import com.DyncoApp.ui.common.Constants;

public class ModeSelectModel {
    private final String KEY_CID;
    private final String KEY_CREATE_COLLECTION_MODE;

    private final SharedPreferences sharedPreferences;

    public ModeSelectModel(Context context) {
        KEY_CID = context.
                getString(R.string.mode_select_screen_cid);
        KEY_CREATE_COLLECTION_MODE = context.
                getString(R.string.mode_select_screen_create_collection_mode);
        sharedPreferences = context.getSharedPreferences(context.
                getString(R.string.shared_preferences), MODE_PRIVATE);
    }

    String getSavedCidText() {
        return sharedPreferences.getString(KEY_CID, Constants.DEFAULT_CID);
    }

    boolean getSavedCreateCollectionValue() {
        return sharedPreferences.getBoolean(KEY_CREATE_COLLECTION_MODE, Constants.DEFAULT_CREATE_COLLECTION);
    }

    void saveData(String cid, boolean createCollection) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CID, cid).putBoolean(KEY_CREATE_COLLECTION_MODE, createCollection).apply();
    }
}