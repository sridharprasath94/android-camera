package com.flashandroid.sdk.misc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Build;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

public class ImageProcessing {

    /**
     * Converts the input bitmap to grayscaled bitmap.
     *
     * @param bitmapIn  is the input bitmap.
     * @param bitmapOut is the output bitmap.
     */
    private static native void cvtGrayscaledBitmap(Bitmap bitmapIn, Bitmap bitmapOut);

    /**
     * Converts the input bitmap to resized bitmap.
     *
     * @param bitmapIn  is the input bitmap.
     * @param bitmapOut is the output bitmap.
     */
    private static native void cvtResizedBitmap(Bitmap bitmapIn, Bitmap bitmapOut);

    /**
     * Converts the input bitmap to resized and grayscaled bitmap.
     *
     * @param bitmapIn  is the input bitmap.
     * @param bitmapOut is the output bitmap.
     */
    private static native void cvtResizedGrayscaledBitmap(Bitmap bitmapIn, Bitmap bitmapOut);

    /**
     * Converts the image file to resized and grayscaled bitmap.
     *
     * @param filePath  is the file path for the image.(in .jpg,.png,.bmp formats).
     * @param bitmapOut is the output bitmap.
     */
    private static native void cvtResizedGrayscaledBitmapFromFile(String filePath,
                                                                  Bitmap bitmapOut);

    /**
     * Converts the input bitmap to barcode bitmap.(Method 1)
     *
     * @param bitmapIn  is the input bitmap.
     * @param bitmapOut is the output bitmap.
     * @param sigma     is the blur value of the output bitmap.
     */
    private static native void cvtBarcodeBitmap1(Bitmap bitmapIn, Bitmap bitmapOut, float sigma);

    /**
     * Converts the input bitmap to barcode bitmap.(Method 2)
     *
     * @param bitmapIn  is the input bitmap.
     * @param bitmapOut is the output bitmap.
     * @param sigma     is the blur value of the output bitmap.
     */
    private static native void cvtBarcodeBitmap2(Bitmap bitmapIn, Bitmap bitmapOut, float sigma);

    /**
     * Gets the image variance value from input bitmap.
     *
     * @param bitmapIn  is the input bitmap.
     * @param bitmapOut is the output bitmap.
     * @return Image Variance.
     */
    private static native double getImageVariance(Bitmap bitmapIn, Bitmap bitmapOut);

    /**
     * Create resized bitmap from a given bitmap.
     *
     * @param bitmap is the input bitmap.
     * @param width  is the width of the output bitmap.
     * @param height is the height of the output bitmap.
     * @param config is the configuration of the output bitmap.
     * @return Resized Bitmap.
     */
    public static Bitmap createResizedBitmap(Bitmap bitmap,
                                             int width,
                                             int height,
                                             Bitmap.Config config) {

        requireNonNull(bitmap);
        System.loadLibrary("lib_opencv_sdk");
        Bitmap resized = Bitmap.createBitmap(width, height, config);
        cvtResizedGrayscaledBitmap(bitmap, resized);
        return resized;
    }

    /**
     * Get the bytes from the bitmap.
     *
     * @param bitmap is the input bitmap.
     * @return byte[].
     */
    public static byte[] getBytesFromBitmap(Bitmap bitmap) {
        requireNonNull(bitmap);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    /**
     * Crop the center of the source bitmap with given width and height.
     *
     * @param bitmap is the input bitmap.
     * @param width  is the width of the output bitmap.
     * @param height is the height of the output bitmap.
     * @return Center cropped bitmap.
     */
    public static Bitmap centerCropBitmap(Bitmap bitmap, int width, int height) {
        requireNonNull(bitmap);
        Matrix matrix = new Matrix();
        // Scaling factor of 1
        matrix.postScale(1f, 1f);
        // Crop the bitmap only if the source bitmap is bigger than the new bitmap
        if (bitmap.getWidth() >= width && bitmap.getHeight() >= height) {
            // Starting point of new bitmap in X-axis
            int xStart = (bitmap.getWidth() - width) / 2;
            // Starting point of new bitmap in Y axis
            int yStart = (bitmap.getHeight() - height) / 2;
            // Return the new bitmap
            return Bitmap.createBitmap(bitmap, xStart, yStart, width, height, matrix, true);
        }
        // Otherwise return the original bitmap itself
        else {
            return bitmap;
        }
    }

    /**
     * Build the barcode bitmap from the given bytes and the blur value
     */
    protected static Bitmap buildBarcodeBitmapFromBytes(byte[] bytes, int blurValue) {
        System.loadLibrary("lib_opencv_sdk");
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        int resizeWidth;
        int resizeHeight;

        if (bitmap.getWidth() > 1000 && bitmap.getHeight() > 1000) {
            resizeWidth = bitmap.getWidth() / 4;
            resizeHeight = bitmap.getHeight() / 4;
        } else {
            resizeWidth = bitmap.getWidth() / 2;
            resizeHeight = bitmap.getHeight() / 2;
        }

        Bitmap barcodeBitmap = Bitmap.createBitmap(resizeWidth, resizeHeight, bitmap.getConfig());
        cvtBarcodeBitmap2(bitmap, barcodeBitmap, blurValue);

        int borderWidth = resizeWidth / 3;
        int borderHeight = resizeHeight / 3;
        // To maintain the ratio 4:3
        while (borderHeight % 4 != 0) {
            borderHeight++;
            borderWidth = borderHeight * 3 / 4;
        }

        // Add the white border around the bitmap - It helps to identify the barcodes easily.
        Bitmap bitmapWithBorder = Bitmap.createBitmap(barcodeBitmap.getWidth() + borderWidth * 2,
                barcodeBitmap.getHeight() + borderHeight * 2, Objects.requireNonNull(barcodeBitmap.getConfig()));
        Canvas canvas = new Canvas(bitmapWithBorder);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(barcodeBitmap, borderWidth, borderHeight, null);
        // Returns the white border bitmap.
        return bitmapWithBorder;
    }


    public static Bitmap convertBitmapToMddiSpecs(Bitmap bitmap, int minWidth, int minHeight) {
        System.loadLibrary("lib_opencv_sdk");
        byte[] bytes = getBytesFromBitmap(bitmap);
        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Bitmap resized = Bitmap.createBitmap(minWidth, minHeight, Bitmap.Config.ARGB_8888);
        cvtResizedGrayscaledBitmap(decodedBitmap, resized);
        return resized;
    }

    /**
     * Check whether the provided parameters are non null.
     *
     * @param params is the list of objects.(The objects can be of any type).
     */
    public static void requireNonNull(Object... params) {
        for (Object par : params) {
            Objects.requireNonNull(par);
        }
    }

    /**
     * Get the user agent information required for the MDDI requests.
     */
    public static String getUserAgent(Context context) {
        String applicationName = convertToCamelCase(context.getApplicationInfo().loadLabel(context.getPackageManager()).toString());
        String applicationVersion = "";
        try {
            applicationVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Application:" + applicationName + "/" + applicationVersion +
                " DeviceId:" + Build.MANUFACTURER + "/" + Build.MODEL +
                " OperatingSystem:" + "Android" + "/" + Build.VERSION.RELEASE;
    }

    private static String convertToCamelCase(String input) {
        String[] words = input.split("\\s+");
        StringBuilder camelCaseString = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                camelCaseString.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    camelCaseString.append(word.substring(1).toLowerCase());
                }
            }
        }
        return camelCaseString.toString();
    }
}
