package com.DyncoApp.ui;




import static com.mddi.misc.ImageUtil.buildBitmapFromCameraImage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.DyncoApp.R;
import com.mddicamera.CameraView;
import com.mddicamera.CameraZoom;

import java.io.ByteArrayOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class A5_SqlCameraScreen extends AppCompatActivity implements View.OnClickListener {

    public static String SHARED_PREFS_SQL_CAMERA = "sharedPrefsSqlCamera";
    public static String ZoomCurrentValue = "zoomlevel";

    protected String SQL_UID;
    protected String DEFAULT_CID;
    protected String DEFAULT_SNO;
    protected String USERNAME;
    protected String PASSWORD;
    protected String USERID;

    protected static final int CAMERA_REQUEST_CODE = 60;

    protected static final int STATE_PREVIEW = 0;
    protected static final int STATE_WAIT_LOCK = 1;
    protected int captureState;

    protected static final int STATE_FOCUS_NOT_LOCKED = 2;
    protected static final int STATE_FOCUS_LOCK = 3;
    protected int lockState;

    protected TextureView previewTextureView;

    protected boolean isZoomSBVisible;
    protected boolean isCameraOpening = false;

    protected static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    protected CameraManager cameraManager;
    protected CameraCharacteristics cameraCharacteristics;
    protected CameraDevice cameraDevice;
    protected Handler cameraHandler;
    protected HandlerThread cameraHandlerThread;
    protected CameraCaptureSession cameraCaptureSession;
    protected CaptureRequest.Builder cameraCaptureRequestBuilder;
    protected CameraCaptureSession.CaptureCallback cameraCaptureCallback;

    protected SurfaceTexture previewSurfaceTexture;
    protected Surface previewSurface;
    protected List<Surface> outputSurfaces = new ArrayList<>(4);
    protected ImageReader streamImageReader;
    protected Surface streamSurface;
    protected int frameCount;
    protected Image cameraImage;
    protected byte[] uncroppedBytes;
    protected Size imageSize;
    protected ImageButton cameraButton;
    protected ImageButton flashButton;
    protected ImageButton zoomButton;

    protected enum flash {FLASH_ON, FLASH_OFF}

    protected flash flashState;
    protected SeekBar zoomSeekbar;
    protected int zoomMin;
    protected int zoomMax;
    protected int zoomCurrent;
    protected int widthUncropped;
    protected int heightUncropped;
    protected int widthCropped;
    protected int heightCropped;
    protected int actualRotation;
    protected String cameraMacroId = "0";
    protected CaptureResult captureResult;

    protected Integer autoFocusState;

    protected String androidId;
    protected Vibrator vibrator;

    protected GlobalVariables globalVariables = new GlobalVariables();
    protected CameraZoom zoomLevel;

    protected Display display;
    protected LinearLayout mainLayout;
    protected LinearLayout mainLayout2;
    protected ConstraintLayout sqlLayout;
    protected ImageView sqlImageview;
    protected Button sqlPushButton;
    protected Bitmap sqlBitmap;
    protected byte[] sqlBytes;
    protected ImageButton sqlCloseButton;
    protected EditText sqlEditText;
    protected TextView uidSQLTextView;
    protected int countUidSql = 0;
    protected String valueUidSql = "";
    protected String Time;
    protected String Date;
    protected ImageView loadingImageView;
    protected AnimationDrawable loadingAnimation;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a5_sqlcamerascreen);
        globalVariables = (GlobalVariables) getApplicationContext();
        initializeLayout();
        SQL_UID = getIntent().getStringExtra("SQL_UID");
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        assert cameraManager != null;
        loadData();
        setupLayoutItems();
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        assert vibrator != null;
        vibrator.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE));
        display = getWindowManager().getDefaultDisplay();

        Time = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(new Date());
        Date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        Time = Date + " , " + Time;

        zoomSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                zoomCurrent = Math.max(progress, zoomMin);
                zoomSeekbar.setProgress(zoomCurrent);
                saveData();
                updatePreview();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isZoomSBVisible = true;
                disableLayoutItems();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                zoomSeekbar.setVisibility(View.INVISIBLE);
                isZoomSBVisible = false;
                setupLayoutItems();
            }
        });
        previewTextureView.setOnClickListener(v -> lockFocus());

        cameraButton.setOnClickListener(v ->
        {
            if (uncroppedBytes != null) {
                takePicture();
            } else {
                Toast.makeText(A5_SqlCameraScreen.this, "Please wait for some time..", Toast.LENGTH_SHORT).show();
            }
        });
        sqlPushButton.setOnClickListener(v -> {
            disableSqlItems();
            runOnUiThread(this::sqlAddData);
        });
        sqlCloseButton.setOnClickListener(v -> {
            sqlLayout.setVisibility(View.INVISIBLE);
            mainLayout.setVisibility(View.VISIBLE);
            uidSQLTextView.setVisibility(View.INVISIBLE);
            uidSQLTextView.setText("");
            mainLayout.setEnabled(true);
            mainLayout.setAlpha((float) 1.0);
            mainLayout2.setVisibility(View.VISIBLE);
            mainLayout2.setEnabled(true);
            mainLayout2.setAlpha((float) 1.0);
        });
    }

    /**
     * On click listener for the image buttons
     */
    @SuppressLint("NonConstantResourceId")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.flash_IB:
                switch (flashState) {
                    case FLASH_OFF:
                        flashSwitcher(R.drawable.ic_flash_on, flash.FLASH_ON);
                        try {
                            cameraManager.setTorchMode("0", true);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                        break;
                    case FLASH_ON:
                        flashSwitcher(R.drawable.ic_flash_off_, flash.FLASH_OFF);
                        try {
                            cameraManager.setTorchMode("0", false);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                        break;
                }
                updatePreview();
                break;
            case R.id.zoom_IB:
                if (!isZoomSBVisible) {
                    zoomSeekbar.setEnabled(true);
                    isZoomSBVisible = true;
                    zoomSeekbar.setVisibility(View.VISIBLE);
                } else {
                    zoomSeekbar.setEnabled(false);
                    isZoomSBVisible = false;
                    zoomSeekbar.setVisibility(View.INVISIBLE);
                }
                updatePreview();
                break;
        }
    }

    /**
     * When the activity is resumed
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if (previewTextureView.isAvailable()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                openCamera(cameraMacroId);
            }
        } else {
            previewTextureView.setSurfaceTextureListener(textureListener);
        }
    }

    /**
     * When the activity is paused
     */
    @Override
    protected void onPause() {
        super.onPause();
        stopBackgroundThread();
        runOnUiThread(this::closeCamera);
    }

    /**
     * When press the back button
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * Start the background thread
     */
    private void startBackgroundThread() {
        cameraHandlerThread = new HandlerThread("Camera Background");
        cameraHandlerThread.start();
        cameraHandler = new Handler(cameraHandlerThread.getLooper());
    }

    /**
     * Stop the background thread
     */
    private void stopBackgroundThread() {
        if (!isCameraOpening) {
            cameraHandlerThread.quitSafely();
        }
        try {
            cameraHandlerThread.join();
            cameraHandlerThread = null;
            cameraHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Surface Texture Listener for listening the current state of the TextureView
     */
    private final TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            if (ContextCompat.checkSelfPermission(A5_SqlCameraScreen.this, Manifest.permission.CAMERA) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(A5_SqlCameraScreen.this, new String[]{Manifest.permission.CAMERA},
                        CAMERA_REQUEST_CODE);
                return;
            }
            openCamera(cameraMacroId);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            closeCamera();
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    /**
     * Open the camera of the android
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    private void openCamera(String cameraMacro_ID) {
        try {
            cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraMacro_ID);
            cameraManager.setTorchMode("0", true);
            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageSize = map.getOutputSizes(ImageFormat.JPEG)[0];

            widthUncropped = imageSize.getWidth();
            heightUncropped = imageSize.getHeight();

            widthCropped = imageSize.getWidth();
            heightCropped = imageSize.getHeight();

            zoomLevel = new CameraZoom(cameraCharacteristics);

            if (ContextCompat.checkSelfPermission(A5_SqlCameraScreen.this, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED) {
                cameraManager.openCamera(cameraMacro_ID, cameraStateCallBack, cameraHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private final CameraDevice.StateCallback cameraStateCallBack = new CameraDevice.StateCallback() {
        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            if (!isCameraOpening) {
                streamPicture();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    /**
     * Update the camera preview
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void updatePreview() {
        captureRequestBuilderFunction(cameraCaptureRequestBuilder);
        cameraCaptureCallback = new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                super.onCaptureStarted(session, request, timestamp, frameNumber);
            }

            @Override
            public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                super.onCaptureFailed(session, request, failure);
            }

            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
                autoFocusState = result.get(CaptureResult.CONTROL_AF_STATE);
                assert autoFocusState != null;
                captureResult = result;
                processFocus(result);
            }
        };
        try {
            if (!isCameraOpening) {
                cameraCaptureSession.setRepeatingRequest(cameraCaptureRequestBuilder.build(), cameraCaptureCallback, cameraHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Build the capture request
     */
    private void captureRequestBuilderFunction(CaptureRequest.Builder previewCaptureBuilder) {
        zoomLevel.setZoom(previewCaptureBuilder, zoomCurrent);
        int deviceOrientation = ORIENTATIONS.get(getWindowManager().getDefaultDisplay().getRotation());
//        actualRotation = sensorToDeviceRotation(cameraCharacteristics, deviceOrientation);
        actualRotation = 90;
        previewCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(actualRotation));
        previewCaptureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
        switch (flashState) {
            case FLASH_ON:
                previewCaptureBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                break;
            case FLASH_OFF:
                previewCaptureBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                break;
        }
    }

    /**
     * Create the camera preview with the given width and height
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    private void streamPicture() {
        try {
            outputSurfaces = new ArrayList<>(3);
            previewSurfaceTexture = previewTextureView.getSurfaceTexture();
            assert previewSurfaceTexture != null;
            previewSurfaceTexture.setDefaultBufferSize(widthUncropped, heightUncropped);
            previewSurface = new Surface(previewSurfaceTexture);
            outputSurfaces.add(previewSurface);
            cameraCaptureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            cameraCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            cameraCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
            cameraCaptureRequestBuilder.addTarget(previewSurface);
            streamImageReader = ImageReader.newInstance(widthUncropped, heightUncropped, ImageFormat.YUV_420_888, 50);
            streamSurface = streamImageReader.getSurface();
            outputSurfaces.add(streamSurface);
            cameraCaptureRequestBuilder.addTarget(streamSurface);
            captureRequestBuilderFunction(cameraCaptureRequestBuilder);
            streamImageReader.setOnImageAvailableListener(reader -> {
                frameCount++;
                try {
                    cameraImage = reader.acquireLatestImage();
                    if (frameCount % 15 == 0 && cameraImage != null) {
                        System.gc();
                        cameraTask(cameraImage);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (cameraImage != null) {
                        cameraImage.close();
                    }
                }
            }, cameraHandler);
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        if (!isCameraOpening) {
                            session.capture(cameraCaptureRequestBuilder.build(), cameraCaptureCallback, cameraHandler);
                            cameraCaptureSession = session;
                            updatePreview();
                            lockFocus();
                        }
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                }

                @Override
                public void onClosed(@NonNull CameraCaptureSession session) {
                    super.onClosed(session);
                }

                @Override
                public void onActive(@NonNull CameraCaptureSession session) {
                    super.onActive(session);
                }
            }, cameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Camera task to convert the captured image to byte array
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    private void cameraTask(Image cameraImage) {
        liveFocus(captureResult);
        Bitmap bitmap = buildBitmapFromCameraImage(cameraImage, actualRotation, getApplicationContext());
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, globalVariables.width, globalVariables.height, true);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        uncroppedBytes = stream.toByteArray();
    }

    /**
     * Take the picture
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    protected void takePicture() {
        sqlBitmap = waterMark(uncroppedBytes, Time);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        sqlBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        sqlBytes = stream.toByteArray();
        runOnUiThread(() -> {
            mainLayout.setVisibility(View.INVISIBLE);
            mainLayout2.setVisibility(View.INVISIBLE);
            sqlLayout.setVisibility(View.VISIBLE);
            sqlImageview.setVisibility(View.VISIBLE);
            sqlImageview.setImageBitmap(sqlBitmap);
            uidSQLTextView.setVisibility(View.VISIBLE);
            uidSQLTextView.setText(SQL_UID);
        });
    }

    /**
     * Close the Camera
     */
    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != cameraImage) {
            cameraImage.close();
            cameraImage = null;
        }
        if (null != streamImageReader) {
            streamImageReader.close();
            streamImageReader = null;
        }
    }

    /**
     * Switch the flash state based on the current state
     */
    public void flashSwitcher(int drawableId, flash nextState) {
        flashButton.setImageResource(drawableId);
        flashState = nextState;
    }

    /**
     * Process the capture result
     */
    private void processFocus(CaptureResult result) {
        switch ((captureState)) {
            case STATE_PREVIEW:
                //Optional
                break;
            case STATE_WAIT_LOCK:
                Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                assert afState != null;
                if (afState == CaptureRequest.CONTROL_AF_STATE_FOCUSED_LOCKED) {
                    unlockFocus();
                }
                break;
        }
    }

    /**
     * Set the focus based on the capture result
     */
    private void liveFocus(CaptureResult result) {
        Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
        assert afState != null;
        if (afState != CaptureRequest.CONTROL_AF_STATE_FOCUSED_LOCKED) {
            lockState = STATE_FOCUS_LOCK;
            lockFocus();
        }
    }

    /**
     * Lock the focus
     */
    private void lockFocus() {
        try {
            captureState = STATE_WAIT_LOCK;
            cameraCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
            cameraCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
            cameraCaptureSession.capture(cameraCaptureRequestBuilder.build(), cameraCaptureCallback, cameraHandler);
            cameraCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
            cameraCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
            cameraCaptureSession.setRepeatingRequest(cameraCaptureRequestBuilder.build(), cameraCaptureCallback, cameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Unlock the focus
     */
    private void unlockFocus() {
        try {
            captureState = STATE_PREVIEW;
            cameraCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_CANCEL);
            cameraCaptureSession.capture(cameraCaptureRequestBuilder.build(), cameraCaptureCallback, cameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save the data to shared preferences
     */
    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_SQL_CAMERA, MODE_PRIVATE);
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(ZoomCurrentValue, zoomCurrent).apply();
    }

    /**
     * Load the data from shared preferences
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_SQL_CAMERA, MODE_PRIVATE);
        USERNAME = globalVariables.userName;
        USERID = globalVariables.userId;
        PASSWORD = globalVariables.password;
        DEFAULT_CID = globalVariables.userCid;
        DEFAULT_SNO = "0";
        androidId = Build.MODEL;
        zoomCurrent = sharedPreferences.getInt(ZoomCurrentValue, 8);
        zoomMax = 8;
        zoomMin = 1;
    }

    /**
     * Assigning all the layout items to specific items
     */
    public void initializeLayout() {
        previewTextureView = findViewById(R.id.preview_TextureView);
        flashButton = findViewById(R.id.flash_IB);
        zoomButton = findViewById(R.id.zoom_IB);
        cameraButton = findViewById(R.id.camera_IB);
        zoomSeekbar = findViewById(R.id.zoom_SB);
        mainLayout = findViewById(R.id.linearLayout1);
        mainLayout2 = findViewById(R.id.linearLayout2);
        sqlLayout = findViewById(R.id.grpcLayout);
        uidSQLTextView = findViewById(R.id.UID_sqlTextView);
        sqlPushButton = findViewById(R.id.sqlpushButton);
        sqlImageview = findViewById(R.id.sqlImageView);
        sqlCloseButton = findViewById(R.id.sqlCloseImageButton);
        sqlEditText = findViewById(R.id.sqlEditText);
        loadingImageView = findViewById(R.id.loadingSQLImageView);
    }

    /**
     * Setup the layout items
     */
    public void setupLayoutItems() {
        flashButton.setBackground(ContextCompat.getDrawable(this, android.R.color.transparent));
        zoomButton.setBackground(ContextCompat.getDrawable(this, android.R.color.transparent));
        flashButton.setImageResource(R.drawable.ic_flash_on);
        zoomButton.setImageResource(R.drawable.ic_baseline_zoom_in_24);
        flashButton.setOnClickListener(this);
        zoomButton.setOnClickListener(this);
        flashState = flash.FLASH_ON;
        previewTextureView.setSurfaceTextureListener(textureListener);
        zoomSeekbar.setMax(zoomMax);
        zoomSeekbar.setProgress(zoomCurrent);
        uidSQLTextView.setVisibility(View.INVISIBLE);
        lockState = STATE_FOCUS_NOT_LOCKED;
        sqlLayout.setVisibility(View.INVISIBLE);
        loadingImageView.setBackgroundResource(R.drawable.animationscan_loading);
        loadingAnimation = (AnimationDrawable) loadingImageView.getBackground();
        loadingImageView.setVisibility(View.INVISIBLE);
    }

    /**
     * Disable the layout items
     */
    public void disableLayoutItems() {
        ColorDrawable colorDrawable
                = new ColorDrawable(Color.parseColor("#808080"));
        flashButton.setBackground(colorDrawable);
        zoomButton.setBackground(colorDrawable);
    }

    /**
     * Disable the sql layout items
     */
    public void disableSqlItems() {
        runOnUiThread(() -> {
            mainLayout.setEnabled(false);
            sqlLayout.setEnabled(false);
            mainLayout2.setEnabled(false);
            mainLayout.setAlpha((float) 0.05);
            sqlLayout.setAlpha((float) 0.05);
            mainLayout2.setAlpha((float) 0.05);
            loadingImageView.setVisibility(View.VISIBLE);
            loadingAnimation.start();
        });
    }

    /**
     * Enable the sql layout items
     */
    public void enableSqlItems() {
        runOnUiThread(() -> {
            mainLayout.setEnabled(true);
            sqlLayout.setEnabled(true);
            mainLayout2.setEnabled(true);
            mainLayout.setAlpha((float) 1.0);
            sqlLayout.setAlpha((float) 1.0);
            mainLayout2.setAlpha((float) 1.0);
            loadingImageView.setVisibility(View.INVISIBLE);
            loadingAnimation.stop();
        });
    }

    /**
     * Add the sql data to database
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sqlAddData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setCancelable(false);
        builder.setTitle("Add additional data").setMessage("Do you want to add this descriptive image and text for this ID?").
                setIcon(R.drawable.dynamicelementlogo);

        builder.setPositiveButton("Yes", (dialog, option) -> {
            try {
                Statement statement = globalVariables.sqlConnection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) from " + globalVariables.sqlTable);
                while (resultSet.next()) {
                    countUidSql = resultSet.getInt(1);
                }
                ResultSet result2 = statement.executeQuery("SELECT " + getString(R.string.sqlUID) + " from " + globalVariables.sqlTable + " where " + getString(R.string.sqlUID) + " like " + "'" + SQL_UID + "'");
                while (result2.next()) {
                    valueUidSql = result2.getString(1);
                }
                if (countUidSql == 0) {
                    PreparedStatement preparedStatement = globalVariables.sqlConnection.
                            prepareStatement("INSERT INTO " + globalVariables.sqlTable + "(" + getString(R.string.sqlUID) + "," + getString(R.string.sqlDescription) + "," + getString(R.string.sqlImage) + ") VALUES(?,?,?)");
                    preparedStatement.setString(1, SQL_UID);
                    preparedStatement.setString(2, sqlEditText.getText().toString());
                    preparedStatement.setBytes(3, sqlBytes);
                    preparedStatement.execute();
                    sqlLayout.setVisibility(View.VISIBLE);
                    sqlImageview.setVisibility(View.VISIBLE);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(sqlBytes, 0, sqlBytes.length);
                    runOnUiThread(() -> sqlImageview.setImageBitmap(bitmap));
                    enableSqlItems();
                    Toast.makeText(A5_SqlCameraScreen.this, "Table Created. Saved the Image with ID " + SQL_UID, Toast.LENGTH_SHORT).show();
                    runOnUiThread(() -> {
                        sqlImageview.setVisibility(View.INVISIBLE);
                        mainLayout.setVisibility(View.VISIBLE);
                        mainLayout2.setVisibility(View.VISIBLE);
                        mainLayout.setEnabled(false);
                        mainLayout.setAlpha((float) 0.05);
                        mainLayout2.setEnabled(false);
                        mainLayout2.setAlpha((float) 0.05);
                    });
                    sqlLayout.setVisibility(View.INVISIBLE);
                    onBackPressed();
                } else {
                    if (!String.valueOf(valueUidSql).equals(SQL_UID)) {
                        PreparedStatement preparedStatement = globalVariables.sqlConnection.prepareStatement("INSERT INTO " + globalVariables.sqlTable + "(" + getString(R.string.sqlUID) + ",description,image) VALUES(?,?,?)");
                        preparedStatement.setString(1, SQL_UID);
                        preparedStatement.setString(2, sqlEditText.getText().toString());
                        preparedStatement.setBytes(3, sqlBytes);
                        preparedStatement.execute();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(sqlBytes, 0, sqlBytes.length);
                        enableSqlItems();
                        runOnUiThread(() -> {
                            sqlImageview.setImageBitmap(bitmap);
                            sqlLayout.setVisibility(View.INVISIBLE);
                            sqlImageview.setVisibility(View.INVISIBLE);
                            mainLayout.setEnabled(false);
                            mainLayout.setAlpha((float) 0.05);
                            mainLayout2.setEnabled(false);
                            mainLayout2.setAlpha((float) 0.05);
                            Toast.makeText(A5_SqlCameraScreen.this, "Saving the Image with ID = " + SQL_UID, Toast.LENGTH_SHORT).show();
                        });

                        onBackPressed();
                    } else {
                        Statement statement1 = globalVariables.sqlConnection.createStatement();
                        ResultSet resultSet1 = statement1.executeQuery("SELECT " + getString(R.string.sqlImage) + "," + getString(R.string.sqlDescription) + " from " + globalVariables.sqlTable + " where " + getString(R.string.sqlUID) + " like " + "'" + SQL_UID + "'" + ";");
                        resultSet1.next();
                        byte[] resultBytes = resultSet1.getBytes(1);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(resultBytes, 0, resultBytes.length);
                        String sqlDescription = resultSet1.getString(2);
                        runOnUiThread(() -> {
                            sqlLayout.setVisibility(View.VISIBLE);
                            mainLayout.setEnabled(false);
                            mainLayout.setAlpha((float) 0.05);
                            mainLayout2.setEnabled(false);
                            mainLayout2.setAlpha((float) 0.05);
                            sqlImageview.setImageBitmap(bitmap);
                            sqlEditText.setText(sqlDescription);
                            Toast.makeText(A5_SqlCameraScreen.this, "Duplicate Entry ", Toast.LENGTH_SHORT).show();
                        });
                        enableSqlItems();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Toast.makeText(A5_SqlCameraScreen.this, "Host unreachable or Some error occured", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("No", (dialog, option) -> {
            runOnUiThread(() -> {
                mainLayout.setEnabled(true);
                sqlLayout.setEnabled(true);
                mainLayout.setAlpha((float) 1.0);
                sqlLayout.setAlpha((float) 1.0);
                loadingImageView.setVisibility(View.INVISIBLE);
                loadingAnimation.stop();
                Toast.makeText(getApplicationContext(), "The operation has been cancelled", Toast.LENGTH_SHORT).show();
            });
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Watermark the bitmap
     */
    private static Bitmap waterMark(byte[] sourceBytes, String watermark) {
        Bitmap src = BitmapFactory.decodeByteArray(sourceBytes, 0, sourceBytes.length, null);
        src = src.copy(Bitmap.Config.ARGB_8888, true);
        Bitmap result = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src, 0, 0, null);
        Paint paint = new Paint();
        Paint.FontMetrics fm = new Paint.FontMetrics();
        paint.setColor(Color.WHITE);
        paint.setAlpha(90);
        paint.setTextSize(20);
        paint.getFontMetrics(fm);
        float height = paint.measureText("yY");
        int margin = 5;
        canvas.drawRect(src.getWidth() - paint.measureText(watermark) - margin, src.getHeight() - height + fm.top - margin,
                src.getWidth() - margin, src.getHeight() - height + fm.bottom
                        + margin, paint);
        paint.setColor(Color.BLACK);
        canvas.drawText(watermark, src.getWidth() - paint.measureText(watermark) - margin, src.getHeight() - height, paint);
        return result;
    }
}

