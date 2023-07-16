package com.DyncoApp.ui.common;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.DyncoApp.R;

import java.util.Objects;

public class Miscellaneous {
    /**
     * Check the internet connection
     */
    public static boolean isInternetAvailable(Context context) {
        NetworkInfo info = ((ConnectivityManager)
                Objects.requireNonNull(context.getSystemService(Context.CONNECTIVITY_SERVICE))).getActiveNetworkInfo();
        if (info == null) {
            Log.d("Network", "no internet connection");
            return false;
        } else {
            if (info.isConnected()) {
                Log.d("Network", " internet connection available...");
            } else {
                Log.d("Network", " internet connection");
            }
            return true;
        }
    }

    public static void setUpActionBar(AppCompatActivity activity) {
        ActionBar actionBar = activity.getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(activity.getColor(R.color.colorGreen));
        assert actionBar != null;
        actionBar.setBackgroundDrawable(colorDrawable);
    }
}
