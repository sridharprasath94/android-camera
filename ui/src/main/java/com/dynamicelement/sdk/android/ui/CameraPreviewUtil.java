package com.dynamicelement.sdk.android.ui;

import android.graphics.ImageFormat;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Log;
import android.util.Size;

import java.util.ArrayList;
import java.util.Arrays;

public class CameraPreviewUtil {
    private static final String TAG = "CAMERA_PREVIEW_UTIL";

    /**
     * @param cameraView            is the camera view.
     * @param cameraSession         is the current camera session.
     * @param cameraCharacteristics is the camera characteristics of the selected camera.
     */
    protected static void setCameraFrameSize(CameraView cameraView, CameraSessionHandler cameraSession, CameraCharacteristics cameraCharacteristics) {
        StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        assert map != null;
        Size[] availableResolutionSizes = map.getOutputSizes(ImageFormat.YUV_420_888);

        Log.d(TAG + "_AVAILABLE_RESOLUTION_SIZES", Arrays.toString(availableResolutionSizes));
        if (cameraView.ratioMode == CameraParameters.CameraRatioMode.RATIO_1X1) {
            ArrayList<Size> possible1X1Resolutions = getPossible1X1Resolutions(availableResolutionSizes);
            cameraSession.widthUncropped = possible1X1Resolutions.get(0).getWidth();
            cameraSession.heightUncropped = possible1X1Resolutions.get(0).getHeight();
        } else {
            ArrayList<Size> possible4X3Resolutions = getPossible4X3Resolutions(availableResolutionSizes);
            cameraSession.widthUncropped = possible4X3Resolutions.get(0).getWidth();
            cameraSession.heightUncropped = possible4X3Resolutions.get(0).getHeight();
        }
        cameraSession.heightCropped = (int) (cameraSession.heightUncropped);
        cameraSession.widthCropped = (int) (cameraSession.heightUncropped / cameraView.ratioMode.getNumVal());
        Log.d(TAG + "_IMAGE_WIDTH_HEIGHT_RATIO", String.valueOf(cameraView.ratioMode.getNumVal()));
        Log.d(TAG + "_IMAGE_UNCROPPED_WIDTH", String.valueOf(cameraSession.widthUncropped));
        Log.d(TAG + "_IMAGE_UNCROPPED_HEIGHT", String.valueOf(cameraSession.heightUncropped));
        Log.d(TAG + "_IMAGE_CROPPED_WIDTH", String.valueOf(cameraSession.widthCropped));
        Log.d(TAG + "_IMAGE_CROPPED_HEIGHT", String.valueOf(cameraSession.heightCropped));
    }

    /**
     * Check possible 1:1 resolutions.
     *
     * @param sizeArray is the list of all the available resolution sizes from the camera.
     * @return All the available 1:1 resolutions.
     */
    private static ArrayList<Size> getPossible1X1Resolutions(Size[] sizeArray) {
        ArrayList<Size> optimumSizeArray = new ArrayList<>();
        // Check all the possible 4:3 sizes from the list.
        for (Size size : sizeArray) {
            if ((float) size.getHeight() /
                    (float) size.getWidth() == CameraParameters.CameraRatioMode.RATIO_1X1.getNumVal()) {
                optimumSizeArray.add(size);
            }
        }
        return optimumSizeArray;
    }

    /**
     * Check possible 4:3 resolutions.(width:height)
     * Note: We get 4:3 resolution from the sensor.
     * While displaying in the preview, it is rotated as 3:4 and being displayed.
     *
     * @param sizeArray is the list of all the available resolution sizes from the camera.
     * @return All the available 4:3 resolutions.
     */
    private static ArrayList<Size> getPossible4X3Resolutions(Size[] sizeArray) {
        ArrayList<Size> optimumSizeArray = new ArrayList<>();
        // Check all the possible 4:3 sizes from the list.
        for (Size size : sizeArray) {
            if ((float) size.getHeight() /
                    (float) size.getWidth() == CameraParameters.CameraRatioMode.RATIO_3X4.getNumVal()) {
                optimumSizeArray.add(size);
            }
        }
        return optimumSizeArray;
    }
}
