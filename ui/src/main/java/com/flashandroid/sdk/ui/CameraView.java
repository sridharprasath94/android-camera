package com.flashandroid.sdk.ui;

import static com.flashandroid.sdk.misc.ImageUtil.buildBitmapFromCameraImage;
import static com.flashandroid.sdk.ui.CameraConstants.CameraMode;
import static com.flashandroid.sdk.ui.CameraConstants.CameraMode.CAMERA_CAPTURE;
import static com.flashandroid.sdk.ui.CameraSessionHandler.CAMERA_REQUEST_CODE;
import static com.flashandroid.sdk.ui.CameraSessionHandler.UPDATE_PREVIEW_DELAY;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.flashandroid.sdk.ui.databinding.CamerapreviewlayoutBinding;


public class CameraView extends ConstraintLayout {
    private static final String TAG = "CAMERA_VIEW";
    protected boolean enableScan = false;
    protected boolean enableLayout;
    protected CamerapreviewlayoutBinding binding;
    protected CameraCallback cameraCallback;
    protected CameraParameters cameraParameters;
    protected boolean cameraInitialized;
    protected boolean barcodeScanMode;
    protected boolean safeToSwitchCamera = true;
    private CameraSessionHandler cameraSessionHandler;
    protected CameraParameters.CameraRatioMode ratioMode = null;
    protected Activity activity;
    private CameraMode currentCameraMode;

    public CameraView(Context context) {
        super(context);
        initView(context);
    }

    public CameraView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public CameraView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public int getMaxZoom() {
        return this.cameraSessionHandler.getMaxZoom();
    }

    public int getMinZoom() {
        return this.cameraSessionHandler.getMinZoom();
    }

    public int getCurrentZoom() {
        return this.cameraSessionHandler.getCurrentZoom();
    }

    public boolean isFlashEnabled() {
        return this.cameraSessionHandler.isFlashEnabled();
    }

    public boolean isSafeToSwitchCamera() {
        return this.safeToSwitchCamera;
    }

    /**
     * Forces a ratio change to the given ratio.
     * This can be done due to necessary ratio switches as explained here:
     * <a href="https://stackoverflow.com/questions/36156837">...</a>
     */
    public void forceRatio(CameraParameters.CameraRatioMode ratioMode) {
        this.onPause();
        this.cameraParameters.cameraRatioMode = ratioMode;
        this.onResume();
    }

    /**
     * Initialise the camera preview with camera preview layout
     */
    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.camerapreviewlayout, this);
        this.binding = CamerapreviewlayoutBinding.bind(view);
    }

    /**
     * Initialize the camera (CAMERA_CAPTURE) with the default settings.
     * Flash will be ON by default.
     * Changing camera flash level and zoom level will be stored in the shared preferences.
     * Next time,load all the stored settings from shared preferences.
     *
     * @param cameraParameters is the camera parameters.
     * @param activity         is the current activity context.
     */
    public void initCameraCaptureOnly(CameraParameters cameraParameters, Activity activity, CameraCallback cameraCallback) {
        this.currentCameraMode = CameraMode.CAMERA_CAPTURE;
        this.activity = activity;
        this.cameraCallback = cameraCallback;
        this.cameraParameters = cameraParameters;
        this.ratioMode = cameraParameters.cameraRatioMode;
        this.initCamera(this.currentCameraMode, cameraParameters.defaultLayout,
                cameraParameters.enableBarcodeScan,
                cameraParameters.enableBarcodeScan,
                cameraParameters.primaryCamera,
                cameraParameters.captureDelay);
    }

    /**
     * Initialize the camera (CAMERA_CAPTURE) with the default settings.
     * Flash will be ON by default.
     * Changing camera flash level and zoom level will be stored in the shared preferences.
     * Next time,load all the stored settings from shared preferences.
     *
     * @param cameraParameters is the camera parameters.
     * @param activity         is the current activity context.
     */
    public void initCameraCaptureWithBarcodeScan(CameraParameters cameraParameters, Activity activity, CameraCallback cameraCallback) {
        this.currentCameraMode = CameraMode.BARCODE_SCAN;
        this.activity = activity;
        this.cameraCallback = cameraCallback;
        this.cameraParameters = cameraParameters;
        this.ratioMode = cameraParameters.cameraRatioMode;
        this.initCamera(this.currentCameraMode, cameraParameters.defaultLayout,
                cameraParameters.enableBarcodeScan,
                cameraParameters.enableBarcodeScan,
                cameraParameters.primaryCamera,
                cameraParameters.captureDelay);
    }

    /**
     * @param defaultLayout       is set as 'true' if the default layout needs to be enabled.
     *                            Otherwise set as 'false'.
     * @param barcodeScanMode     is set as 'true' if the barcode mode needs to be enabled.
     *                            Otherwise set as 'false'.
     * @param enableScanAnimation is set as 'true' if the scanning animation needs to be enabled.
     *                            Otherwise set as 'false'.
     * @param selectPrimaryCamera is set as 'true' if the primary camera needs to be enabled.
     *                            Otherwise set as 'false'.
     */
    private void initCamera(CameraMode cameraMode, boolean defaultLayout,
                            boolean barcodeScanMode,
                            boolean enableScanAnimation,
                            boolean selectPrimaryCamera,
                            Integer captureDelayMs) {
        this.currentCameraMode = CameraMode.CAMERA_CAPTURE;
        this.cameraInitialized = true;
        this.enableScan = enableScanAnimation;
        this.enableLayout = defaultLayout;
        this.barcodeScanMode = barcodeScanMode;
        this.cameraSessionHandler = new CameraSessionHandler(this,cameraMode, selectPrimaryCamera,
                captureDelayMs, this.ratioMode);
    }

    /**
     * When resuming the application.
     * Check the camera permission and initialize the camera.
     * If the MDDI process is selected, reset the search state so that the process resumes.
     * Nominal place to call this on 'onResume' method of the activity.
     */
    public void onResume() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) getContext(),
                    new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
            return;
        }
        if (this.cameraInitialized) {
            return;
        }
        Log.d(TAG + "_ON_RESUME", binding.previewView.isAvailable() ? "Preview is available in " +
                "OnResume method. Initialising Camera" : "Preview is not available in OnResume " +
                "method. Setting up the texture view listener");
        // When resuming, if the preview is available, open the camera.
        if (binding.previewView.isAvailable()) {
            this.initCameraCaptureOnly(this.cameraParameters, this.activity, this.cameraCallback);

        } else {
            cameraSessionHandler.setupTextureListener(this.ratioMode);
        }
    }

    /**
     * When pausing the application, close the camera.
     * If the MDDI search process is running, stop the process.
     * Nominal place to call this on 'onPause' method of the activity.
     */
    public void onPause() {
        if (this.cameraSessionHandler == null) {
            return;
        }
        cameraSessionHandler.saveData();
        cameraSessionHandler.closeCamera();
    }

    /**
     * Toggle the flash state.
     */
    public void changeFlashState(boolean flashEnabled) {
        this.cameraSessionHandler.updateFlashState(flashEnabled);
    }

    /**
     * Returns the current captured image in bitmap format
     */
    public Bitmap captureCurrentImage() {
        if (this.currentCameraMode != CAMERA_CAPTURE) {
            return null;
        }
        if (this.cameraSessionHandler.currentImage == null) {
            return null;
        }
        this.cameraSessionHandler.currentCapture = true;
        return buildBitmapFromCameraImage(this.cameraSessionHandler.currentImage,
                this.cameraSessionHandler.currentRotationDegree, this.activity);
    }

    /**
     * Switch the next camera.
     */
    public void switchNextCamera() {
        this.safeToSwitchCamera = false;
        Log.d(TAG + "_SWITCH_CAMERA", "CAMERA_SWITCH");
        this.cameraSessionHandler.selectNextCamera();
        onPause();
        this.postDelayed(this::onResume, UPDATE_PREVIEW_DELAY);
    }

    /**
     * Check the camera permission.
     * Nominal place to call this on 'onRequestPermissionsResult' method of the activity.
     */
    public void checkCameraPermission(int requestCode, String[] permissions, int[] grantResults) {
        this.cameraSessionHandler.checkCameraPermission(requestCode, grantResults);
    }

    /**
     * Update the camera preview with the changed zoom level.
     */
    public void changeZoomLevel(int zoomValue) {
        Log.d(TAG, "Changing zoom level to " + zoomValue);
        this.cameraSessionHandler.changeZoomLevel(zoomValue);
    }

    /**
     * Provides auto focus of the camera.
     * Restart the camera capture session.
     */
    public void focusCamera() {
        this.cameraSessionHandler.updateFocus();
    }

    /**
     * Change the delay between each camera captured frames.
     * It is nominal to keep the delay value equal or greater than 250 ms.
     */
    public void changeCaptureDelay(int captureDelayInMilliSeconds, boolean forceOverwrite) {
        this.cameraSessionHandler.changeCaptureDelay(captureDelayInMilliSeconds, forceOverwrite);
    }
}
