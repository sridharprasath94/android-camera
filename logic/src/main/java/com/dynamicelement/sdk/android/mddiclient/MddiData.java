package com.dynamicelement.sdk.android.mddiclient;

import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.buildBarcodeBitmapFromBytes;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.centerCropBitmap;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.getStreamImageFromBitmap;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.requireNonNull;
import static com.dynamicelement.sdk.android.mddiclient.MddiVariables.MIN_WIDTH;
import static com.dynamicelement.sdk.android.misc.ImageUtil.buildBitmapFromCameraImage;
import static com.dynamicelement.sdk.android.misc.InstanceType.IVF;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.util.Log;

import com.dynamicelement.mddi.StreamImage;
import com.dynamicelement.sdk.android.exceptions.ClientException;
import com.dynamicelement.sdk.android.misc.InstanceType;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Build the MDDI image according to the given parameters like instanceType, bitmap, cid, sno etc.
 * Provide the cropWidth and cropHeight only if you want to crop the bitmap. Otherwise, provide
 * the width and height of the bitmap itself.
 * Currently, barcode mode is only for DB SNO instance. Select true if the image(barcode) needed
 * to be decoded to get CID and SNO. Otherwise build the MDDI image with default CID and SNO.
 * For IVF and IVF-SNO instances, barcode mode is not valid. This always constructs the MDDI
 * image with provided CID and SNO.
 */
public class MddiData {
    private static String TAG = "MDDI_DATA";

    /**
     * @param mediaImage        image from image reader (camera preview).
     * @param cropWidth         is the width of the output bitmap.
     * @param cropHeight        is the height of the output bitmap.
     * @param instanceType      is the instance type of the MDDI backend.
     * @param cid               is the default cid of the MDDI request.
     * @param sno               is the default sno of the MDDI request.
     * @param barcodeMode       is set as 'true' if the cid and sno needs to be extracted
     *                          from the barcode itself. Set as 'false' if the
     *                          default cid and sno to be used.This is mainly for DB-SNO instance.
     *                          For other instance types, use the default cid and sno.
     * @param mddiDataCallback  is for observing the response from MDDI Data.
     * @param firstCall         true if it is the first call to this constructor within one session.
     *                          Used to verify for older phones whether the ratio is correctly captured.
     *                          Therefore, the conversion to the bitmap is required.
     * @param blurBeforeBarcode Blur captured image before barcode detection to get better results
     *                          for view-filling images.
     * @throws ClientException
     */
    public MddiData(Image mediaImage,
                    Context context,
                    int cropWidth,
                    int cropHeight,
                    InstanceType instanceType,
                    String cid,
                    String sno,
                    boolean barcodeMode,
                    boolean barcodeOnly,
                    int rotationDegree,
                    String tenantId,
                    boolean firstCall,
                    boolean blurBeforeBarcode,
                    boolean checkBarcodeFormat,
                    MddiDataCallback mddiDataCallback) throws ClientException {
        requireNonNull(mediaImage, instanceType, cid, sno, barcodeMode, mddiDataCallback);
        if (barcodeOnly && firstCall) {
            // Used to verify for older phones whether the ratio is correctly captured.
            // Therefore, the conversion to the bitmap is required.
            checkBitmap(buildBitmapFromCameraImage(mediaImage, (float) rotationDegree,
                    context), cropWidth, cropHeight);
        }
        String mddiCid = cid;
        String mddiSno = sno;
        String barcodeResult;
        boolean isCorrectFormat = true;

        if (barcodeOnly && !blurBeforeBarcode) {
            barcodeResult = barcodeMode ? getBarcodeText(mediaImage, null, rotationDegree) : null;
            if (barcodeResult == null) {
                mediaImage.close();
                return;
            }
            if (checkBarcodeFormat) {
                isCorrectFormat = isThisTheCorrectFormat(barcodeResult);
                mddiCid = isCorrectFormat ? getBarcodeCid(barcodeResult) : null;
                mddiSno = isCorrectFormat ? getBarcodeSno(barcodeResult) : null;
            }
            mediaImage.close();
            mddiDataCallback.onDBSNO(
                    isCorrectFormat ? barcodeResult : null,
                    mddiCid,
                    mddiSno,
                    null,
                    null
            );
            return;
        }

        Bitmap bitmap = buildBitmapFromCameraImage(mediaImage, (float) rotationDegree,
                context);
        mediaImage.close();
        checkBitmap(bitmap, cropWidth, cropHeight);
        Bitmap croppedBitmap = centerCropBitmap(bitmap, cropWidth, cropHeight);

        barcodeResult = barcodeMode ? getBarcodeText(null, getBytesFromBitmap(croppedBitmap), rotationDegree) : null;
        if (checkBarcodeFormat) {
            isCorrectFormat = barcodeMode && isThisTheCorrectFormat(barcodeResult);
            mddiCid = (barcodeMode && isCorrectFormat) ? getBarcodeCid(barcodeResult) : cid;
            mddiSno = (barcodeMode && isCorrectFormat) ? getBarcodeSno(barcodeResult) : sno;
        }

        if (barcodeOnly) {
            mddiDataCallback.onDBSNO(
                    (barcodeMode && isCorrectFormat) ? barcodeResult : null,
                    mddiCid,
                    mddiSno,
                    null,
                    null
            );
            return;
        }

        if (instanceType == IVF) {
            mddiCid = cid;
            mddiSno = sno;
            StreamImage mddiImageIVF = getStreamImageFromBitmap(croppedBitmap, mddiCid, mddiSno,
                    instanceType, tenantId);
            mddiDataCallback.onIVF(mddiCid, mddiSno, mddiImageIVF, croppedBitmap);
            return;
        }
        StreamImage mddiImage = getStreamImageFromBitmap(croppedBitmap, mddiCid, mddiSno,
                instanceType, tenantId);
        mddiDataCallback.onDBSNO(
                (barcodeMode && isCorrectFormat) ? barcodeResult : null,
                mddiCid,
                mddiSno,
                mddiImage,
                croppedBitmap
        );
    }

    /**
     * Checks the bitmap according to ratio requirements.
     */
    public static void checkBitmap(Bitmap bitmap, int cropWidth, int cropHeight) throws ClientException {
        if ((float) bitmap.getWidth() / (float) bitmap.getHeight() < 0.75 ||
                (float) cropWidth / (float) cropHeight < 0.75) {
            Log.d(TAG, "Bitmap width: " + bitmap.getWidth() + " and bitmap height: " + bitmap.getHeight());
            throw new ClientException((float) bitmap.getWidth() / (float) bitmap.getHeight() < 0.75 ?
                    "Bitmap height and width are less than the ratio 4:3" :
                    "Crop height and width are less than the ratio 4:3");
        }
        if (bitmap.getWidth() < MIN_WIDTH || cropWidth < MIN_WIDTH) {
            throw new ClientException(bitmap.getWidth() < MIN_WIDTH ? "Bitmap width should be " +
                    "atleast " + MIN_WIDTH : "Crop width should be atleast " + MIN_WIDTH);
        }
    }

    /**
     * Try to decode the barcode text with the help of libraries by priority : "zbar" and "zxing".
     * Returns null value if the barcode is not decoded.
     */
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
                    if (barcodes.size() != 0) {
                        // Print the QR code's message
                        barcodeResult.set(barcodes.get(0).getDisplayValue());
                        latch.countDown();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("CAMERA_IMAGE_QR", e.getMessage());
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

    /**
     * Get the bytes from the bitmap
     */
    private static byte[] getBytesFromBitmap(Bitmap bitmap) {
        requireNonNull(bitmap);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream);
        return stream.toByteArray();
    }

    /**
     * Get the sno from the decoded barcode text.
     *
     * @param barcode is the barcode text.
     * @return The last 10 Indices of the barcode text.
     */
    public static String getBarcodeSno(String barcode) {
        if (barcode.length() == 26) {
            return barcode.substring(20, 26);
        } else if (barcode.length() == 27) {
            return barcode.substring(17, 27);
        } else {
            return null;
        }
    }

    /**
     * Get the cid from the decoded barcode text that correctly formatted - Indices 13 to 16.
     *
     * @param barcode is the barcode text.
     * @return The indices 13 to 16 of the barcode text.
     */
    public static String getBarcodeCid(String barcode) {
        if (barcode.length() == 26) {
            return barcode.substring(17, 20);
        } else if (barcode.length() == 27) {
            return barcode.substring(13, 16);
        } else {
            return null;
        }
    }

    /**
     * Checks if a barcode is 27 alpha-numeric characters long with the prefix "http://d2.vc/".
     *
     * @param barcode is the barcode text.
     * @return true if the format is correct. Otherwise false.
     */
    protected static boolean isThisTheCorrectFormat(String barcode) {
        if (barcode == null || barcode.length() < 26 || barcode.length() > 27) {
            return false;
        }
        if (barcode.length() == 26) {
            return barcode.startsWith("http://qr4.bz/");
        } else {
            return barcode.startsWith("http://d2.vc/");
        }
    }

    /**
     * Callback Interface - To return the MDDI Data.
     */
    public interface MddiDataCallback {
        /**
         * @param barcodeResult       is the decoded barcode result if there is any SQR in the
         *                            frame. Otherwise return null.
         * @param mddiCid             is the cid of the MDDI request.
         * @param mddiSno             is the sno of the MDDI request.
         * @param mddiStreamImage     is the MDDI stream image.
         * @param centerCroppedBitmap is the center cropped bitmap from the original bitmap
         *                            (according to the provided width and height).
         */
        void onDBSNO(String barcodeResult, String mddiCid, String mddiSno,
                     StreamImage mddiStreamImage, Bitmap centerCroppedBitmap);

        /**
         * @param mddiCid             is the cid of the MDDI request.
         * @param mddiSno             is the sno of the MDDI request.
         * @param mddiImage           is the MDDI stream image.
         * @param centerCroppedBitmap is the center cropped bitmap from the original bitmap
         *                            (according to the provided width and height).
         */
        void onIVF(String mddiCid, String mddiSno, StreamImage mddiImage,
                   Bitmap centerCroppedBitmap);
    }
}
