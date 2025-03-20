package com.flashandroid.sdk.misc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.camera2.CameraCharacteristics;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.util.Log;

import java.nio.ByteBuffer;

public class ImageUtil {
    private static final String TAG = "IMAGE_UTIL";
    /**
     * Convert camera image to bitmap.
     *
     * @param image          is the image from the camera.
     * @param sensorRotation is the calculated rotation value for the given camera sensor.
     * @param context        is the application context.
     * @return Bitmap.
     */
    public static Bitmap buildBitmapFromCameraImage(android.media.Image image,
                                                    float sensorRotation, Context context) {

        Log.d(TAG, "Image width: " + image.getWidth() + " image height: " + image.getHeight());
        Bitmap bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(),
                Bitmap.Config.ARGB_8888);
        final ByteBuffer yuvBytes = imageToByteBuffer(image);
        // Convert YUV to RGB
        final RenderScript rs = RenderScript.create(context);
        final Allocation allocationRgb = Allocation.createFromBitmap(rs, bitmap);
        final Allocation allocationYuv = Allocation.createSized(rs, Element.U8(rs),
                yuvBytes.array().length);
        allocationYuv.copyFrom(yuvBytes.array());
        ScriptIntrinsicYuvToRGB scriptYuvToRgb = ScriptIntrinsicYuvToRGB.create(rs,
                Element.U8_4(rs));
        scriptYuvToRgb.setInput(allocationYuv);
        scriptYuvToRgb.forEach(allocationRgb);
        allocationRgb.copyTo(bitmap);

        // Based on the sensor rotation degrees, we also rotate the bitmap.
        if (sensorRotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(sensorRotation);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,
                    bitmap.getWidth(), bitmap.getHeight(), true);
            bitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        }
        return bitmap;
    }

    /**
     * Convert camera image to byte buffer.
     *
     * @param image is the image from the camera.
     * @return ByteBuffer.
     */
    private static ByteBuffer imageToByteBuffer(final android.media.Image image) {
        final Rect crop = image.getCropRect();
        final int width = crop.width();
        final int height = crop.height();
        final android.media.Image.Plane[] planes = image.getPlanes();
        final byte[] rowData = new byte[planes[0].getRowStride()];
        final int bufferSize =
                width * height * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8;
        final ByteBuffer output = ByteBuffer.allocateDirect(bufferSize);
        int channelOffset;
        int outputStride;
        for (int planeIndex = 0; planeIndex < 3; planeIndex++) {
            if (planeIndex == 0) {
                channelOffset = 0;
                outputStride = 1;
            } else if (planeIndex == 1) {
                channelOffset = width * height + 1;
                outputStride = 2;
            } else {
                channelOffset = width * height;
                outputStride = 2;
            }
            final ByteBuffer buffer = planes[planeIndex].getBuffer();
            final int rowStride = planes[planeIndex].getRowStride();
            final int pixelStride = planes[planeIndex].getPixelStride();
            final int shift = (planeIndex == 0) ? 0 : 1;
            final int widthShifted = width >> shift;
            final int heightShifted = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < heightShifted; row++) {
                final int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = widthShifted;
                    buffer.get(output.array(), channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (widthShifted - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < widthShifted; col++) {
                        output.array()[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < heightShifted - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
        }
        return output;
    }

    /**
     * Calculate the camera sensor rotation from the device orientation.
     *
     * @param cameraCharacteristics    is the characteristics of the current camera hardware.
     * @param defaultDeviceOrientation is the default device orientation.
     * @return the camera sensor rotation.
     */
    public static int cameraSensorRotation(CameraCharacteristics cameraCharacteristics,
                                           int defaultDeviceOrientation) {
        int sensorRotation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        return (sensorRotation + defaultDeviceOrientation + 360) % 360;
    }
}
