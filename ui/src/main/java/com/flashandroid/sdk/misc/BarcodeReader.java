package com.flashandroid.sdk.misc;

import static com.flashandroid.sdk.misc.ImageProcessing.buildBarcodeBitmapFromBytes;

import static java.util.Objects.requireNonNull;

import android.graphics.Bitmap;
import android.media.Image;
import android.util.Log;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class BarcodeReader {
    private static String TAG = "BARCODE_SCAN";

    /**
     * Get the bytes from the bitmap
     */
    private static byte[] getBytesFromBitmap(Bitmap bitmap) {
        requireNonNull(bitmap);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream);
        return stream.toByteArray();
    }
    public static String getBarcodeText(Image mediaImage, byte[] croppedBytes, int rotationDegree) {
        AtomicReference<String> barcodeResult = new AtomicReference<>(null);
        CountDownLatch latch = new CountDownLatch(1);

        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build();
        BarcodeScanner barcodeScanning = BarcodeScanning.getClient(options);
        InputImage image;
        if (mediaImage != null) {
            try {
                image = InputImage.fromMediaImage(mediaImage, rotationDegree);
            } catch (Exception e) {
                Log.e(TAG, "getBarcodeText: ", e);
                return null;
            }
        } else {
            image = InputImage.fromBitmap(buildBarcodeBitmapFromBytes(croppedBytes, 10), rotationDegree);
        }
        barcodeScanning.process(image)
                .addOnSuccessListener(barcodes -> {
                    // Task completed successfully
                    if (!barcodes.isEmpty()) {
                        // Print the QR code's message
                        barcodeResult.set(barcodes.get(0).getDisplayValue());
                        latch.countDown();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("CAMERA_IMAGE_QR", requireNonNull(e.getMessage()));
                    barcodeResult.set(null);
                    latch.countDown();
                });

        try {
            if (latch.await(100, TimeUnit.MILLISECONDS)) {
                return barcodeResult.get();
            } else {
                return null;
            }
        } catch (InterruptedException e) {
            return null;
        }
    }
}
