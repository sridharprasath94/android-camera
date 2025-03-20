package com.flashandroid.sdk.ui;

import static android.content.Context.MODE_PRIVATE;
import static android.view.View.INVISIBLE;
import static com.flashandroid.sdk.misc.BarcodeReader.getBarcodeText;
import static com.flashandroid.sdk.misc.ImageProcessing.centerCropBitmap;
import static com.flashandroid.sdk.misc.ImageProcessing.getBytesFromBitmap;
import static com.flashandroid.sdk.misc.ImageUtil.cameraSensorRotation;
import static com.flashandroid.sdk.ui.CameraFocus.getFlashInbuiltSupport;
import static com.flashandroid.sdk.ui.CameraFocus.initialiseCameraFocus;
import static com.flashandroid.sdk.ui.CameraFocus.setCameraFocus;
import static com.flashandroid.sdk.ui.CameraParameters.CameraRatioMode.RATIO_3X4;
import static com.flashandroid.sdk.ui.CameraPreviewUtil.setCameraFrameSize;
import static com.flashandroid.sdk.ui.CameraSelect.detectMainBackLens;
import static com.flashandroid.sdk.ui.CameraSelect.getCameraList;
import static com.flashandroid.sdk.ui.CameraSelect.selectCamera;
import static com.flashandroid.sdk.ui.CameraZoom.getCurrentZoomValue;
import static com.flashandroid.sdk.ui.CameraZoom.getMinZoomValue;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.flashandroid.sdk.misc.ImageUtil;
import com.flashandroid.sdk.misc.exceptions.ExceptionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class CameraSessionHandler {
    protected final static int CAMERA_REQUEST_CODE = 60;
    protected static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final String ZoomCurrentValue = "zoomLevel";
    private static final String CurrentCameraKey = "currentCamera";
    private static final String currentFlashMode = "currentFlashMode";
    private static final String currentCaptureState = "currentCaptureState";
    private final static String CAMERA_SETTINGS = "sharedPrefsCameraSettings";
    private final static String CAMERA_SETTINGS_PRIMARY_CAMERA = "sharedPrefsPrimaryCamera";

    private boolean captureStarted = false;
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAIT_LOCK = 1;
    private static final int STATE_FOCUSING = 1;
    private static final int FLASH_STATE_OFF = 0;
    private static final int FLASH_STATE_ON = 1;
    private static final int DEF_FLASH_MODE = 1;
    private static final String TAG = "CAMERA_SESSION";
    protected static int UPDATE_PREVIEW_DELAY = 100;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    private final SharedPreferences sharedPreferences;
    private final CameraManager cameraManager;
    private final CameraView cameraView;
    protected int CAPTURE_DELAY_MS = 1000;
    protected int widthUncropped;
    protected int heightUncropped;
    protected int widthCropped;
    protected int heightCropped;
    protected List<Thread> backgroundThreads;
    private int zoomMin;
    private int maxZoom;
    private int currentZoom;
    private boolean flashEnabled;
    private Handler cameraPreviewHandler;
    private Handler cameraCaptureHandler;
    private Handler updatePreviewHandler;
    protected CaptureRequest.Builder cameraCaptureRequestBuilder;
    private CameraCharacteristics cameraCharacteristics;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    protected ImageReader streamImageReader;
    protected boolean enableImageReader;
    private int captureState = STATE_WAIT_LOCK;
    private Timer captureTimer;
    private String[] cameraList = new String[10];
    private CameraZoom cameraZoom;
    private String selectedCamera;
    private flash currentFlashState;
    protected int currentRotationDegree = 0;
    protected boolean currentCapture;
    protected Image currentImage = null;
    protected CameraConstants.CameraMode currentCameraMode;

    public CameraSessionHandler(CameraView cameraView, CameraConstants.CameraMode cameraMode, boolean selectPrimaryCamera,
                                Integer captureDelayMs,

                                CameraParameters.CameraRatioMode cameraRatioMode) {
        this.cameraView = cameraView;
        this.currentCameraMode = cameraMode;
        this.backgroundThreads = new ArrayList<>();
        this.sharedPreferences =
                this.cameraView.activity.getSharedPreferences(selectPrimaryCamera ?
                        CAMERA_SETTINGS_PRIMARY_CAMERA : CAMERA_SETTINGS, MODE_PRIVATE);
        this.cameraManager =
                (CameraManager) this.cameraView.activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            this.loadData(selectPrimaryCamera);
        } catch (Exception e) {
            Log.d(TAG + "_EXCEPTION_LOAD_DATA", e.toString());
            throwErrorOnCallback(e);
        }
        this.cameraView.binding.scanImageView.setVisibility(INVISIBLE);
        if (captureDelayMs != null) {
            CAPTURE_DELAY_MS = captureDelayMs;
        }

        if (this.cameraView.barcodeScanMode) {
            this.cameraView.binding.recImageView.setBackgroundResource(R.drawable.ic_qr_rectangle);
            this.cameraView.binding.recImageView.setVisibility(this.cameraView.enableLayout ?
                    View.VISIBLE : INVISIBLE);
            this.cameraView.binding.scanImageView.setBackgroundResource(R.drawable.animationscan);
        } else {
            this.cameraView.binding.recImageView.setBackgroundResource(R.drawable.ic_ivf_rectangle);
            this.cameraView.binding.recImageView.setVisibility(this.cameraView.enableLayout ?
                    View.VISIBLE : INVISIBLE);
            this.cameraView.binding.scanImageView.setBackgroundResource(R.drawable.animation);
        }
        this.setFlash();

        Log.d(TAG + "_PREVIEW", this.cameraView.binding.previewView.isAvailable() ? "Preview is " +
                "available in Init Camera method. Opening Camera" : "Preview is not available in " +
                "Init Camera method. Setting up the texture view listener");
        if (cameraView.binding.previewView.isAvailable()) {
            openCamera();
        } else {
            setupTextureListener(cameraRatioMode);
        }
    }

    /**
     * Create a new handler thread and add it to the list of background threads.
     *
     * @param nameOfTheHandler  is the name of the handler thread.
     * @param backgroundThreads is the list of background threads in which the current handler
     *                          thread should be added.
     * @return the current handler thread.
     */
    protected static Handler addBackgroundHandlerToList(String nameOfTheHandler,
                                                        List<Thread> backgroundThreads) {
        HandlerThread backgroundHandler = new HandlerThread(nameOfTheHandler);
        backgroundHandler.start();
        backgroundThreads.add(backgroundHandler);
        return new Handler(backgroundHandler.getLooper());
    }

    protected boolean isFlashEnabled() {
        return this.flashEnabled;
    }

    protected int getMinZoom() {
        return this.zoomMin;
    }

    protected int getMaxZoom() {
        return this.maxZoom;
    }

    protected int getCurrentZoom() {
        return this.currentZoom;
    }

    /**
     * Check the camera permission.
     */
    protected void checkCameraPermission(int requestCode, int[] grantResults) {
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.cameraView.activity.runOnUiThread(() -> Toast.makeText(this.cameraView.activity, "Camera Permission Granted", Toast.LENGTH_SHORT).show());
            } else {
                throwErrorOnCallback(new Exception("Camera " +
                        "Permission Denied"));
            }
        }
    }

    /**
     * Set up the texture listener.
     * When the surface texture is available, open the camera.
     */
    protected void setupTextureListener(CameraParameters.CameraRatioMode ratioMode) {
        fitLayoutItems(ratioMode);
        this.cameraView.binding.previewView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                try {
                    cameraPreviewHandler = addBackgroundHandlerToList("camera preview handler",
                            backgroundThreads);
                    openCamera();
                    saveData();
                } catch (Exception e) {
                    throwErrorOnCallback(e);
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
            }
        });
    }

    /**
     * Fitting the layout items according to the aspect ratio.
     */
    private void fitLayoutItems(CameraParameters.CameraRatioMode ratioMode) {
        this.cameraView.binding.previewView.setAspectRatio(ratioMode);
        this.cameraView.binding.surfaceView.setAspectRatio(ratioMode);
        this.cameraView.binding.scanImageView.setAspectRatio(ratioMode);
        this.cameraView.binding.recImageView.setAspectRatio(ratioMode);
        this.cameraView.binding.barcodeHintTextView.setAspectRatio(ratioMode);
    }

    /**
     * Open the specified camera.
     */
    protected void openCamera() {
        try {
            Log.d(TAG + "_CAMERA_SELECTED", this.selectedCamera);
            this.cameraCharacteristics =
                    this.cameraManager.getCameraCharacteristics(this.selectedCamera);
            if (!getFlashInbuiltSupport(this.cameraCharacteristics)) {
                Log.d(TAG + "_CAMERA_FORCE_FLASH", "Forcing flash on...");
                try {
                    this.cameraManager.setTorchMode("0",
                            this.currentFlashState == CameraSessionHandler.flash.FLASH_ON);
                } catch (Exception e) {
                    Log.d(TAG + "_EXCEPTION_FLASH", e.toString());
                }
            }
            setCameraFrameSize(this.cameraView, this, this.cameraCharacteristics);

            //If the camera permission is granted, open the camera
            if (ContextCompat.checkSelfPermission(this.cameraView.activity,
                    Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                try {
                    this.cameraManager.openCamera(this.selectedCamera,
                            new CameraDevice.StateCallback() {
                                @Override
                                public void onOpened(@androidx.annotation.NonNull CameraDevice camera) {
                                    //This is called when the camera is open
                                    cameraDevice = camera;
                                    try {
                                        startCameraPreview();
                                        Log.d(TAG + "_CAMERA_OPENED", selectedCamera);
                                    } catch (CameraAccessException e) {
                                        Log.d(TAG + "_EXCEPTION_CAMERA_START_PREVIEW", e.toString());
                                        throwErrorOnCallback(e);
                                    }
                                }

                                @Override
                                public void onDisconnected(@androidx.annotation.NonNull CameraDevice camera) {
                                    Log.d(TAG + "_CAMERA_DISCONNECT", "CAMERA disconnected");
                                }

                                @Override
                                public void onError(@androidx.annotation.NonNull CameraDevice camera,
                                                    int error) {
                                    Log.d(TAG + "_EXCEPTION_OPEN_CAMERA_ERROR", "Camera error when " +
                                            "opening");
                                    saveData();
                                    throwErrorOnCallback(new Exception(
                                            "Camera error when opening"));
                                }
                            }, this.cameraPreviewHandler);
                } catch (Exception e) {
                    Log.d(TAG + "_EXCEPTION_OPENING_CAMERA", e.toString());
                    selectNextCamera();
                    saveData();
                    throwErrorOnCallback(e);
                }
            }
        } catch (CameraAccessException e) {
            Log.d(TAG + "_EXCEPTION_CAMERA_CHARACTERISTICS", e.toString());
            throwErrorOnCallback(e);
        }
    }

    /**
     * Event to capture the preview stream at required 1 fps.
     */
    @SuppressLint("WrongConstant")
    protected void startCameraPreview() throws CameraAccessException {
        Log.d(TAG + "_START_PREVIEW", cameraView.binding.previewView.isAvailable() ? "Preview is " +
                "available in startCameraPreview method. Starting the preview" : "Preview is not " +
                "available in startCameraPreview method. Will throw error here");
        try {
            SurfaceTexture surfaceTexture = this.cameraView.binding.previewView.getSurfaceTexture();
            assert surfaceTexture != null;
            surfaceTexture.setDefaultBufferSize(this.widthUncropped, this.heightUncropped);
            Surface surface = new Surface(surfaceTexture);
            Handler cameraCaptureSessionHandler = addBackgroundHandlerToList("camera capture " +
                    "session handler", this.backgroundThreads);
            Handler cameraImageHandler = addBackgroundHandlerToList("capture image handler",
                    this.backgroundThreads);

            ImageReader imageReader = ImageReader.newInstance(this.widthUncropped,
                    this.heightUncropped, ImageFormat.YUV_420_888, 5);
            imageReader.setOnImageAvailableListener(reader -> {
                if (!this.captureStarted) {
                    this.enableImageReader = false;
                    this.captureStarted = true;
                }
                Image image;
                try {
                    Log.d("SRIDHAR_CAMERA_SCAN_MODEL", "onImageObtained: ");
                    image = imageReader.acquireLatestImage();
                } catch (Throwable t) {
                    // No logging here, as a warning occurs if image cannot be retrieved (e.g.
                    // buffer size not sufficient).
                    return;
                }
                // Do not listen for further images as we already retrieved one.
                this.cameraCaptureRequestBuilder.removeTarget(imageReader.getSurface());
                if (image == null) {
                    return;
                }

                try {
                    int imageRotation = getImageRotation();
                    if (this.enableImageReader) {
                        this.enableImageReader = false;

                        Handler imageProcessHandler = addBackgroundHandlerToList("image process " +
                                "handler", backgroundThreads);

                        imageProcessHandler.post(() -> {
                            try {
                                if (this.currentCapture) {
                                    return;
                                }

                                this.currentImage = image;
                                this.currentRotationDegree = imageRotation;
                                Bitmap bitmap = ImageUtil.buildBitmapFromCameraImage(image, imageRotation, cameraView.activity);
                                Bitmap croppedBitmap = centerCropBitmap(bitmap, this.cameraView.ratioMode == RATIO_3X4 ?
                                                this.heightCropped : (int) (this.heightCropped * RATIO_3X4.getNumVal()),
                                        this.cameraView.ratioMode == RATIO_3X4 ? this.heightCropped : this.widthCropped);
                                String barcodeResult = this.currentCameraMode == CameraConstants.CameraMode.BARCODE_SCAN ?
                                        getBarcodeText(null, getBytesFromBitmap(croppedBitmap), imageRotation) : null;
                                cameraView.activity.runOnUiThread(() -> cameraView.cameraCallback.onImageObtained(bitmap, barcodeResult));
                                this.currentImage.close();
                                image.close();
                            } catch (Exception e) {
                                Log.d(TAG + "_EXCEPTION_CAMERA_TASK", e.toString());
                                throwErrorOnCallback(e);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.d(TAG + "__EXCEPTION_CAMERA_IMAGE_BUFFER", e.toString());
                    if (this.enableImageReader) {
                        this.enableImageReader = false;
                    }
                    throwErrorOnCallback(e);
                }
                this.enableImageReader = true;
            }, cameraImageHandler);
            this.streamImageReader = imageReader;
            this.cameraCaptureRequestBuilder =
                    this.cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            this.cameraCaptureRequestBuilder.addTarget(surface);
            Log.d(TAG, "Set zoom in startPreview with current zoom " + this.currentZoom);
            this.cameraZoom.setZoom(this.cameraCaptureRequestBuilder, this.currentZoom);
            this.cameraDevice.createCaptureSession(Arrays.asList(surface,
                    imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSessionCallback) {
                    // The camera is already closed.
                    if (cameraDevice == null) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSession = cameraCaptureSessionCallback;
                    enableImageReader = true;
                    updatePreviewHandler = addBackgroundHandlerToList("Update preview handler",
                            backgroundThreads);
                    updatePreview();
                    cameraCaptureHandler = addBackgroundHandlerToList("camera capture handler",
                            backgroundThreads);
                    startImageCapture();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                }

                @Override
                public void onClosed(@NonNull CameraCaptureSession session) {
                    super.onClosed(session);
                }
            }, cameraCaptureSessionHandler);
        } catch (Exception e) {
            if (Objects.requireNonNull(e.getMessage()).startsWith("CAMERA_ERROR")) {
                selectNextCamera();
            }
            throwErrorOnCallback(e);
        }
    }

    /**
     * Capture the image at a given interval of time.
     */
    protected void startImageCapture() {
        // Use a timer instance to trigger a capture repeatedly.
        this.captureTimer = new Timer();
        this.captureTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (enableImageReader && streamImageReader != null) {
                    if (cameraDevice == null || cameraCaptureSession == null) {
                        return;
                    }
                    try {
                        cameraCaptureRequestBuilder.addTarget(streamImageReader.getSurface());
                        cameraCaptureSession.capture(cameraCaptureRequestBuilder.build(),
                                new CameraCaptureSession.CaptureCallback() {
                                    @Override
                                    public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                                                   @NonNull CaptureRequest request,
                                                                   @NonNull TotalCaptureResult result) {
                                        super.onCaptureCompleted(session, request, result);
                                    }
                                }, cameraCaptureHandler);
                    } catch (Exception e) {
                        Log.d(TAG + "_EXCEPTION_CAMERA_CAPTURE", e.toString());
                    }
                }
            }
        }, 0, CAPTURE_DELAY_MS);
    }

    /**
     * Change the delay between each camera captured frames.
     * It is nominal to keep the delay value equal or greater than 250 ms.
     */
    public void changeCaptureDelay(int captureDelayInMilliSeconds, boolean forceOverwrite) {
        if (forceOverwrite) {
            CAPTURE_DELAY_MS = captureDelayInMilliSeconds;
        } else {
            CAPTURE_DELAY_MS = Math.max(captureDelayInMilliSeconds, 250);
        }
    }

    /**
     * Building the capture request.
     */
    protected void captureRequestBuilder() {
        if (this.cameraCaptureRequestBuilder == null) {
            return;
        }
        this.cameraZoom.setZoom(this.cameraCaptureRequestBuilder, this.currentZoom);
        this.cameraCaptureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, getImageRotation());
        this.cameraCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE,
                this.currentFlashState == CameraSessionHandler.flash.FLASH_ON ?
                        CameraMetadata.FLASH_MODE_TORCH : CameraMetadata.FLASH_MODE_OFF);
        this.flashEnabled = Objects.requireNonNull(this.cameraCaptureRequestBuilder.get(CaptureRequest.FLASH_MODE)) == CameraMetadata.FLASH_MODE_TORCH;
        this.cameraCaptureRequestBuilder.removeTarget(this.streamImageReader.getSurface());
        if (this.captureState == STATE_PREVIEW) {
            this.captureState = STATE_WAIT_LOCK;
            Log.d(TAG + "_CAMERA_FOCUS_INITIALIZE", "Initialising the camera focus trigger");
            initialiseCameraFocus(this.cameraCaptureRequestBuilder, this.cameraCaptureSession,
                    this.updatePreviewHandler);
        } else if (this.captureState == STATE_FOCUSING) {
            // Prevent endless focussing for older devices.
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                Log.d(TAG + "_CAMERA_FOCUS_RESTART", "Restarting the camera focus trigger");
                setCameraFocus(this.cameraCaptureRequestBuilder, this.cameraCaptureSession,
                        this.updatePreviewHandler);
            }
        }
    }

    protected void updateFocus() {
        captureState = STATE_FOCUSING;
        updatePreview();
    }

    /**
     * Update the preview.
     */
    protected void updatePreview() {
        if (this.cameraDevice == null || this.cameraCaptureSession == null) {
            return;
        }
        try {
            this.cameraCaptureSession.stopRepeating();
            this.cameraCaptureSession.setRepeatingRequest(this.cameraCaptureRequestBuilder.build(), null, null);
        } catch (Exception e) {
            Log.d(TAG + "_EXCEPTION_CAMERA_SESSION", e.toString());
            return;
        }
        captureRequestBuilder();
        try {
            if (this.cameraDevice == null || this.cameraCaptureSession == null) {
                return;
            }
            // Starting preview.
            this.cameraCaptureSession.setRepeatingRequest(this.cameraCaptureRequestBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@androidx.annotation.NonNull CameraCaptureSession session, @androidx.annotation.NonNull CaptureRequest request, @androidx.annotation.NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                }

                @Override
                public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request,
                                            @NonNull CaptureFailure failure) {
                    super.onCaptureFailed(session, request, failure);
                }
            }, this.updatePreviewHandler);
        } catch (CameraAccessException e) {
            Log.d(TAG + "_EXCEPTION_CAMERA_SESSION", e.toString());
        }
    }

    /**
     * Get the image rotation based on the camera sensor.
     *
     * @return the rotation value.
     */
    protected int getImageRotation() {
        return cameraSensorRotation(this.cameraCharacteristics,
                ORIENTATIONS.get(this.cameraView.activity.getWindowManager().getDefaultDisplay().getRotation()));
    }

    /**
     * Update the flash state of the camera.
     */
    protected void updateFlashState(boolean flashEnabled) {
        this.currentFlashState = flashEnabled ? CameraSessionHandler.flash.FLASH_ON :
                CameraSessionHandler.flash.FLASH_OFF;
        saveData();
        if (this.cameraCharacteristics == null) {
            try {
                this.cameraCharacteristics = this.cameraManager.getCameraCharacteristics("0");
            } catch (CameraAccessException e) {
                Log.d(TAG + "_CAMERA_ACCESS_EXCEPTION", e.toString());
            }
        }

        if (!getFlashInbuiltSupport(this.cameraCharacteristics)) {
            try {
                this.cameraManager.setTorchMode("0", flashEnabled);
            } catch (Exception e) {
                Log.d(TAG + "_EXCEPTION_FLASH", e.toString());
            }
            this.captureState = STATE_PREVIEW;
            this.cameraView.postDelayed(this::updatePreview, UPDATE_PREVIEW_DELAY);
        } else {
            this.captureState = STATE_PREVIEW;
            updatePreview();
        }
    }

    /**
     * By default, flash mode is ON.
     * If the flash mode is changed next time, it will be stored in shared preferences.
     * Next time, set the flash from the shared preferences.
     */
    protected void setFlash() {
        if (this.sharedPreferences.getInt(currentFlashMode, DEF_FLASH_MODE) == FLASH_STATE_OFF) {
            this.currentFlashState = CameraSessionHandler.flash.FLASH_OFF;
            this.flashEnabled = false;
        } else if (this.sharedPreferences.getInt(currentFlashMode, DEF_FLASH_MODE) == FLASH_STATE_ON) {
            this.currentFlashState = CameraSessionHandler.flash.FLASH_ON;
            this.flashEnabled = true;
        }
    }

    /**
     * Update the camera preview with the changed zoom level.
     */
    protected void changeZoomLevel(int zoomValue) {
        this.currentZoom = Math.max(zoomValue, this.zoomMin);
        //save the current settings
        saveData();
        //Update the camera preview using the above settings
        updatePreview();
    }

    /**
     * Throw the error on the callback
     */
    protected void throwErrorOnCallback(Exception e) {
        cameraView.cameraCallback.onError(ExceptionType.CAMERA_EXCEPTION, e);
    }

    /**
     * Select the next camera.
     */
    protected void selectNextCamera() {
        String nextCamera;
        int index = -1;
        Log.d(TAG + "_CAMERA_SELECT_LIST", Arrays.toString(this.cameraList));
        for (int i = 0; i < this.cameraList.length; i++) {
            if (this.cameraList[i].equals(this.selectedCamera)) {
                index = i;
                break;
            }
        }
        int cameraCount;
        if (index < this.cameraList.length - 1) {
            cameraCount = index + 1;
        } else {
            cameraCount = 0;
        }

        //Get the corresponding string value of the camera count
        nextCamera = this.cameraList[cameraCount];
        this.selectedCamera = nextCamera;
        Log.d(TAG + "CAMERA_NEXT", this.selectedCamera);
    }

    /**
     * Close the camera.
     */
    protected void closeCamera() {
        this.enableImageReader = false;
        if (this.captureTimer != null) {
            this.captureTimer.cancel();
        }
        this.cameraView.cameraInitialized = false;
        this.captureState = STATE_PREVIEW;
        try {
            if (this.cameraCaptureSession != null) {
                this.cameraCaptureSession.stopRepeating();
                this.cameraCaptureSession.abortCaptures();
                this.cameraCaptureSession.close();
                this.cameraCaptureSession = null;
            }
        } catch (Exception e) {
            Log.d(TAG + "_EXCEPTION_CLOSING_CAMERA", e.toString());
        }
        if (null != this.cameraDevice) {
            this.cameraDevice.close();
            this.cameraDevice = null;
        }
        if (null != this.streamImageReader) {
            this.streamImageReader.close();
            this.streamImageReader = null;
        }
        if (this.cameraCharacteristics != null && !getFlashInbuiltSupport(this.cameraCharacteristics)) {
            try {
                this.cameraManager.setTorchMode("0", false);
            } catch (Exception e) {
                Log.d(TAG + "_EXCEPTION_FLASH", e.toString());
            }
        }
        this.cameraCaptureSession = null;
        for (Thread thread : this.backgroundThreads) {
            try {
                thread.interrupt();
            } catch (Throwable t) {
                Log.d(TAG + "_EXCEPTION_BACKGROUND_THREADS", t.toString());
            }
        }
        if (this.cameraDevice != null) {
            this.cameraDevice.close();
            this.cameraDevice = null;
        }
        this.cameraView.binding.previewView.setSurfaceTextureListener(null);
        Log.d(TAG, "Camera session closed successfully.");
    }

    /**
     * Save the data in the shared preferences.
     */
    protected void saveData() {
        //Save the data to shared preferences
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor =
                this.sharedPreferences.edit();
        editor.putInt(ZoomCurrentValue, this.currentZoom);
        editor.putInt(currentCaptureState, this.captureState);
        editor.putString(CurrentCameraKey, this.selectedCamera);
        editor.putInt(currentFlashMode,
                this.currentFlashState == CameraSessionHandler.flash.FLASH_ON ? FLASH_STATE_ON :
                        FLASH_STATE_OFF);

        editor.apply();
    }

    /**
     * Load the data for the camera.
     */
    protected void loadData(boolean primaryCameraEnabled) throws CameraAccessException, IllegalArgumentException {
        this.cameraList = getCameraList(this.cameraManager).toArray(new String[0]);
        Log.d(TAG + "_CAMERA_LIST", Arrays.toString(this.cameraList));
        this.selectedCamera = this.sharedPreferences.getString(CurrentCameraKey,
                primaryCameraEnabled ? detectMainBackLens(this.cameraManager) : selectCamera(this.cameraManager));
        if (this.sharedPreferences.getInt(currentFlashMode, FLASH_STATE_ON) == FLASH_STATE_ON) {
            this.currentFlashState = CameraSessionHandler.flash.FLASH_ON;
        } else {
            this.currentFlashState = CameraSessionHandler.flash.FLASH_OFF;
        }
        this.captureState = this.sharedPreferences.getInt(currentCaptureState, STATE_PREVIEW);
        // Load the data from shared preferences.
        // Get the current, minimum and maximum values for the zoom.
        CameraCharacteristics cameraCharacteristics =
                this.cameraManager.getCameraCharacteristics(this.selectedCamera);
        this.cameraZoom = new CameraZoom(cameraCharacteristics);
        this.maxZoom = (int) this.cameraZoom.maxZoom;
        this.currentZoom = this.sharedPreferences.getInt(ZoomCurrentValue, primaryCameraEnabled ?
                1 : getCurrentZoomValue(this.maxZoom, this.selectedCamera));
        this.zoomMin = getMinZoomValue();
        Log.d(TAG, "Camera with ID " + this.selectedCamera + " loaded and zoom " + this.currentZoom);
    }

    private enum flash {FLASH_ON, FLASH_OFF}
}
