package com.DyncoApp.ui.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Objects;

public class Miscellaneous {
    /**
     * Check the internet connection
     */
    public static boolean isInternetAvailable(Context context) {
        NetworkInfo info = ((ConnectivityManager)
                Objects.requireNonNull(context.getSystemService(Context.CONNECTIVITY_SERVICE))).getActiveNetworkInfo();
        return info != null;
    }
}
