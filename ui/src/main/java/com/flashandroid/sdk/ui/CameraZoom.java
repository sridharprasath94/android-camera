package com.flashandroid.sdk.ui;

import android.graphics.Rect;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;

public class CameraZoom {
    private static final String TAG = "CAMERA_ZOOM";
    protected static final float DEFAULT_ZOOM_FACTOR = 1.0f;

    private final Rect mCropRegion = new Rect();

    protected final float maxZoom;

    @Nullable
    private final Rect mSensorSize;

    private final boolean hasSupport;

    /**
     * Initialise the camera zoom with the camera characteristics.
     *
     * @param characteristics is the camera characteristics of the camera manager from the system camera service.
     */
    public CameraZoom(final CameraCharacteristics characteristics) {
        this.mSensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        if (this.mSensorSize == null) {
            this.maxZoom = DEFAULT_ZOOM_FACTOR;
            this.hasSupport = false;
            return;
        }
        final Float value = characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
        this.maxZoom = ((value == null) || (value < DEFAULT_ZOOM_FACTOR))
                ? DEFAULT_ZOOM_FACTOR
                : value;
        this.hasSupport = (Float.compare(this.maxZoom, DEFAULT_ZOOM_FACTOR) > 0);
    }

    /**
     * Set the given zoom value for the camera capture request.
     *
     * @param builder is the camera capture request builder.
     * @param zoom    is the zoom value to be set for the camera.
     */
    public void setZoom(final CaptureRequest.Builder builder, final float zoom) {
        if (!this.hasSupport) {
            return;
        }
        final float newZoom = MathUtils.clamp(zoom, DEFAULT_ZOOM_FACTOR, this.maxZoom);
        assert this.mSensorSize != null;
        Log.d(TAG, "Setting zoom to: " + newZoom + " with max zoom: " + this.maxZoom + " and requested zoom: " + zoom);
        final int centerX = this.mSensorSize.width() / 2;
        final int centerY = this.mSensorSize.height() / 2;
        final int deltaX = (int) ((0.5f * this.mSensorSize.width()) / newZoom);
        final int deltaY = (int) ((0.5f * this.mSensorSize.height()) / newZoom);
        this.mCropRegion.set(centerX - deltaX,
                centerY - deltaY,
                centerX + deltaX,
                centerY + deltaY);
        builder.set(CaptureRequest.SCALER_CROP_REGION, this.mCropRegion);
    }

    /**
     * The minimum zoom value is the DEFAULT_ZOOM_FACTOR(1).
     *
     * @return 1
     */
    protected static int getMinZoomValue() {
        return (int) DEFAULT_ZOOM_FACTOR;
    }

    /**
     * Get the current zoom value for the given android model.
     * For some of the tested devices, the optimum camera zoom value for the MDDI service is fixed. So, it is hardcoded.
     * For other devices, the default value is set as two zoom factors less than the maximum available zoom factor.
     *
     * @param maxZoom is the maximum available zoom value.
     * @return current zoom value.
     */
    protected static int getCurrentZoomValue(int maxZoom, String cameraID) {
        Log.d(TAG, Build.MODEL);
        int currentZoom;
        if (cameraID.equals("0")) {
            Log.d(TAG, "Main camera (0) selected, selecting max zoom factor");
            return maxZoom;
        }
        switch (Build.MODEL) {
            case "VOG-L29":
                currentZoom = 7;
                break;
            case "motorola one fusion+":
                currentZoom = 5;
                break;
            case "moto g(100)":
                currentZoom = 6;
                break;
            case "V2056A":
                currentZoom = 9;
                break;
            default:
                currentZoom = maxZoom;
                break;
        }
        Log.d(TAG, "Max zoom factor: " + maxZoom + ", selected zoom factor: " + currentZoom);
        return currentZoom;
    }
}
