package com.flashandroid.sdk.ui;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import java.util.Arrays;
import java.util.Objects;

public class CameraFocus {

    /**
     * Get the distance calibration value from the camera characteristics.
     *
     * @param cameraCharacteristics is the camera characteristics from the camera manager.
     * @return the focus distance calibration value.
     */
    protected static int getFocusDistanceCalibration(CameraCharacteristics cameraCharacteristics) {
        return cameraCharacteristics.get(CameraCharacteristics.LENS_INFO_FOCUS_DISTANCE_CALIBRATION);
    }

    /**
     * Get the minimum focus distance from the camera characteristics.
     *
     * @param cameraCharacteristics is the camera characteristics from the camera manager.
     * @return the minimum focus distance.
     */
    protected static float getMinimumFocusDistance(CameraCharacteristics cameraCharacteristics) {
        return cameraCharacteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
    }

    /**
     * Set the camera focus for the currently selected camera.
     *
     * @param previewCaptureBuilder is the builder for the camera capture request.
     * @param cameraCaptureSession  is the camera capture session.
     * @param handler               is the background handler.
     */
    protected static void setCameraFocus(CaptureRequest.Builder previewCaptureBuilder,
                                         CameraCaptureSession cameraCaptureSession,
                                         Handler handler) {

        Log.d("Manual AF focus starts", "Manual AF focusing starts");
        try {
            CameraCaptureSession.CaptureCallback captureCallbackHandler = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);

                    if (request.getTag() == "FOCUS_TAG") {
                        Log.d("CAMERA_FOCUS_AFTER_CAPTURE_COMPLETED", "Cancelling the camera trigger..");
                        //the focus trigger is complete -
                        //resume repeating (preview surface will get frames), clear AF trigger
                        previewCaptureBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
//                    cameraCaptureSession.setRepeatingRequest(cameraCaptureRequestBuilder.build(), null, null);
                    }
                }

                @Override
                public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
                    super.onCaptureFailed(session, request, failure);
                    Log.e("CAMERA_FOCUS_Manual AF failure", "Manual AF failure: " + failure);
                }
            };

            // First stop the existing repeating request.
            cameraCaptureSession.stopRepeating();

            // Cancel any existing AF trigger (repeated touches, etc.).
            previewCaptureBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            previewCaptureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
            cameraCaptureSession.capture(previewCaptureBuilder.build(), captureCallbackHandler, handler);

            previewCaptureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            previewCaptureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
            previewCaptureBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
            Log.d("CAMERA_FOCUS_AFTER_CAPTURE_COMPLETED", "Starting the camera trigger..");
            // We'll capture this later for resuming the preview.
            previewCaptureBuilder.setTag("FOCUS_TAG");

            cameraCaptureSession.capture(previewCaptureBuilder.build(), captureCallbackHandler, handler);
        } catch (CameraAccessException e) {
            Log.d("CAMERA_FOCUS_EXCEPTION", Objects.requireNonNull(e.getMessage()));
        }
    }

    protected static void initialiseCameraFocus(CaptureRequest.Builder previewCaptureBuilder,
                                                CameraCaptureSession cameraCaptureSession,
                                                Handler handler) {
        try {
            previewCaptureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            cameraCaptureSession.capture(previewCaptureBuilder.build(), null, handler);
        } catch (CameraAccessException e) {
            Log.d("CAMERA_FOCUS_EXCEPTION", Objects.requireNonNull(e.getMessage()));
        }
    }

    /**
     * Check whether the integer array contains a particular integer value
     *
     * @param arr The integer array
     * @param key The integer value
     * @return
     */
    private static boolean contains(final int[] arr, final int key) {
        return Arrays.stream(arr).anyMatch(i -> i == key);
    }

    /**
     * Compare the lens focus distance (from the capture result) with the minimum focus distance.
     * Only for the macro camera of the Samsung devices.
     *
     * @param cameraCharacteristics is the camera characteristics from the camera manager.
     * @param selectedCamera        is the selected camera ID.
     * @param captureResult         is the capture result from the capture session.
     * @return 1 if the focus distance is not equal. Otherwise return 0.
     */
    protected static int compareFocalDistance(CameraCharacteristics cameraCharacteristics,
                                              String selectedCamera,
                                              CaptureResult captureResult) {
        if (Build.BRAND.equals("samsung") && selectedCamera.equals("2")) {
            if (captureResult.get(CaptureResult.LENS_FOCUS_DISTANCE) != getMinimumFocusDistance(cameraCharacteristics)) {
                return 1;
            }
        }
        return 0;
    }

    /**
     * Check whether the camera has inbuilt flash support.
     *
     * @param cameraCharacteristics is the camera characteristics from the camera manager.
     * @return true if there is inbuilt support for the selected camera. Otherwise false.
     */
    protected static boolean getFlashInbuiltSupport(CameraCharacteristics cameraCharacteristics) {
        return cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
    }
}
