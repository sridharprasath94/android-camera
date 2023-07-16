package com.dynamicelement.sdk.android.ui;

import static com.dynamicelement.sdk.android.mddiclient.MddiData.checkBitmap;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.centerCropBitmap;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.convertBitmapToMddiSpecs;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.requireNonNull;
import static com.dynamicelement.sdk.android.misc.ImageUtil.buildBitmapFromCameraImage;
import static com.dynamicelement.sdk.android.misc.InstanceType.DB_SNO;
import static com.dynamicelement.sdk.android.ui.CameraConstants.CameraMode;
import static com.dynamicelement.sdk.android.ui.CameraConstants.CameraMode.DEFAULT_CLIENT;
import static com.dynamicelement.sdk.android.ui.CameraConstants.CameraMode.WITHOUT_MDDI_CLIENT;
import static com.dynamicelement.sdk.android.ui.CameraConstants.CameraMode.WITH_MDDI_CLIENT;
import static com.dynamicelement.sdk.android.ui.CameraMddiMode.MDDI_SEARCH_ON;
import static com.dynamicelement.sdk.android.ui.CameraSessionHandler.CAMERA_REQUEST_CODE;
import static com.dynamicelement.sdk.android.ui.CameraSessionHandler.UPDATE_PREVIEW_DELAY;

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
import androidx.core.util.Pair;

import com.dynamicelement.sdk.android.Callback;
import com.dynamicelement.sdk.android.add.AddCallback;
import com.dynamicelement.sdk.android.collection.CollectionInfo;
import com.dynamicelement.sdk.android.collection.CollectionResult;
import com.dynamicelement.sdk.android.delete.DeleteResult;
import com.dynamicelement.sdk.android.exceptions.ClientException;
import com.dynamicelement.sdk.android.exceptions.ExceptionType;
import com.dynamicelement.sdk.android.getsample.GetSampleResult;
import com.dynamicelement.sdk.android.mddiclient.ClientService;
import com.dynamicelement.sdk.android.mddiclient.MddiVariables;
import com.dynamicelement.sdk.android.ping.PingResult;
import com.dynamicelement.sdk.android.search.SearchCallBack;
import com.dynamicelement.sdk.android.testMddiImage.TestMddiImageResult;
import com.dynamicelement.sdk.android.ui.databinding.CamerapreviewlayoutBinding;

public class CameraView extends ConstraintLayout {
    private static final String DEF_CID = "1";
    private static final String DEF_SNO = "1";
    private static final String TAG = "CAMERA_VIEW";
    protected ClientService clientService;
    protected String sno;
    protected String cid;
    protected boolean enableScan = false;
    protected boolean enableLayout;
    protected boolean enableMddiSearch;
    protected CamerapreviewlayoutBinding binding;
    protected CameraMddiCallback cameraMddiCallback;
    protected CameraCallback cameraCallback;
    protected ErrorCallback errorCallback;
    protected CameraParameters cameraParameters;
    protected boolean cameraInitialized;
    protected CameraMode currentCameraMode = WITHOUT_MDDI_CLIENT;
    protected boolean dbSnoMode;
    protected boolean safeToSwitchCamera = true;
    private CameraSessionHandler cameraSessionHandler;
    protected CameraParameters.CameraRatioMode ratioMode = null;
    protected Activity activity;
    private Integer captureDelayMs;
    private CameraMddiMode cameraMddiMode;
    private boolean blurBeforeBarcode;
    private boolean checkBarcodeFormat;

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
     * https://stackoverflow.com/questions/36156837
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
     * Initialize the camera(With MDDI client) with the default settings.
     * Changing camera flash level and zoom level will be stored in the shared preferences.
     * Next time,load all the stored settings from shared preferences.
     * The values will be stored in different set of shared preferences if the
     * 'selectPrimaryCamera' is enabled.
     *
     * @param clientService    can be initialized with necessary credentials and configuration.
     *                         Make sure to initialise it correctly to use the MDDI service.
     * @param cid              is the default cid. If the DB-SNO instance type is enabled and if
     *                         there is
     *                         any SQR in the frame, cid will be extracted from the SQR itself.
     * @param sno              is the default sno. If the DB-SNO instance type is enabled and if
     *                         there is
     *                         any SQR in the frame, sno will be extracted from the SQR itself.
     * @param cameraParameters is required to specify parameters like MDDI search(On or Off),
     *                         Default Layout(On or Off),
     *                         Default Scan animation(On or off).
     * @param cameraCallback   has 4 callback methods.
     */
    public void initCameraMddi(ClientService clientService, CameraMddiMode cameraMddiMode, Activity activity, String cid, String sno,
                               CameraParameters cameraParameters, CameraMddiCallback cameraCallback) {
        requireNonNull(cameraCallback, cid, sno, clientService);
        this.clientService = clientService;
        this.activity = activity;
        this.cameraMddiCallback = cameraCallback;
        this.cameraParameters = cameraParameters;
        this.cameraMddiMode = cameraMddiMode;
        this.captureDelayMs = cameraParameters.captureDelay;
        this.blurBeforeBarcode = cameraParameters.blurBeforeBarcode;
        this.ratioMode = cameraParameters.cameraRatioMode;
        this.checkBarcodeFormat = cameraParameters.checkBarcodeFormat;
        this.initCamera(WITH_MDDI_CLIENT, cameraParameters.defaultLayout,
                this.clientService.getInstanceType() == DB_SNO,
                cameraParameters.enableBarcodeScan,
                this.cameraMddiMode == MDDI_SEARCH_ON, cid, sno,
                cameraParameters.primaryCamera, captureDelayMs, blurBeforeBarcode,
                cameraParameters.checkBarcodeFormat);
    }

    /**
     * Initialize the camera (Without MDDI client) with the default settings.
     * Changing camera flash level and zoom level will be stored in the shared preferences.
     * Next time,load all the stored settings from shared preferences.
     *
     * @param cameraParameters is required to specify camera related parameters.
     * @param cameraCallback   has 2 callback methods.
     */
    public void initCameraBarcode(CameraParameters cameraParameters, Activity activity, CameraCallback cameraCallback) {
        requireNonNull(cameraCallback);
        this.activity = activity;
        this.cameraCallback = cameraCallback;
        this.cameraParameters = cameraParameters;
        this.captureDelayMs = cameraParameters.captureDelay;
        this.blurBeforeBarcode = cameraParameters.blurBeforeBarcode;
        this.ratioMode = cameraParameters.cameraRatioMode;
        this.checkBarcodeFormat = cameraParameters.checkBarcodeFormat;
        this.initCamera(WITHOUT_MDDI_CLIENT, this.cameraParameters.defaultLayout,
                this.cameraParameters.enableBarcodeScan, this.cameraParameters.enableBarcodeScan,
                false, DEF_CID, DEF_SNO, this.cameraParameters.primaryCamera,
                this.cameraParameters.captureDelay, this.cameraParameters.blurBeforeBarcode,
                this.checkBarcodeFormat);
    }


    /**
     * Initialize the camera (Default client) with the default settings.
     * Flash will be ON by default.
     * Changing camera flash level and zoom level will be stored in the shared preferences.
     * Next time,load all the stored settings from shared preferences.
     *
     * @param cameraRatioMode is required to specify the camera ratio mode.
     * @param activity        is the current activity context.
     * @param errorCallback   has a callback method for the errors.
     */
    public void initCameraCaptureOnly(CameraParameters.CameraRatioMode cameraRatioMode, int captureDelay,
                                      boolean primaryCamera, Activity activity, ErrorCallback errorCallback) {
        requireNonNull(errorCallback);
        this.activity = activity;
        this.errorCallback = errorCallback;
        this.cameraParameters = new CameraParameters.Builder()
                .selectRatio(cameraRatioMode)
                .enableDefaultLayout(false)
                .enableBarcodeScan(false)
                .blurBeforeBarcode(false)
                .selectPrimaryCamera(primaryCamera)
                .initialiseCaptureDelay(captureDelay)
                .build();
        this.ratioMode = cameraRatioMode;
        this.captureDelayMs = this.cameraParameters.captureDelay;
        this.blurBeforeBarcode = this.cameraParameters.blurBeforeBarcode;
        this.checkBarcodeFormat = this.cameraParameters.checkBarcodeFormat;
        this.initCamera(DEFAULT_CLIENT, this.cameraParameters.defaultLayout,
                this.cameraParameters.enableBarcodeScan, this.cameraParameters.enableBarcodeScan,
                false, DEF_CID, DEF_SNO, this.cameraParameters.primaryCamera,
                this.cameraParameters.captureDelay, this.cameraParameters.blurBeforeBarcode,
                this.cameraParameters.checkBarcodeFormat);
    }

    /**
     * @param currentCameraMode   is the camera mode with or without client service. Possible
     *                            values are
     *                            WITHOUT_MDDI_CLIENT and WITH_MDDI_CLIENT.
     * @param defaultLayout       is set as 'true' if the default layout needs to be enabled.
     *                            Otherwise set as 'false'.
     * @param dbSnoMode           is set as 'true' if the barcode mode needs to be enabled.
     *                            Otherwise set as 'false'.
     * @param enableScan          is set as 'true' if the scanning animation needs to be enabled.
     *                            Otherwise set as 'false'.
     * @param enableMddiSearch    is set as 'true' if the MDDI search needs to be enabled.
     *                            Otherwise set as 'false'.
     * @param cid                 is the default cid. If the DB-SNO instance type is enabled and
     *                            if there is
     *                            any SQR in the frame, cid will be extracted from the SQR itself.
     * @param sno                 is the default sno. If the DB-SNO instance type is enabled and
     *                            if there is
     *                            any SQR in the frame, sno will be extracted from the SQR itself.
     * @param selectPrimaryCamera is set as 'true' if the primary camera needs to be enabled.
     *                            Otherwise set as 'false'.
     */
    private void initCamera(CameraMode currentCameraMode, boolean defaultLayout,
                            boolean dbSnoMode, boolean enableScan, boolean enableMddiSearch,
                            String cid, String sno, boolean selectPrimaryCamera,
                            Integer captureDelayMs, boolean blurBeforeBarcode,
                            boolean checkBarcodeFormat) {
        this.cameraInitialized = true;
        this.currentCameraMode = currentCameraMode;
        this.enableMddiSearch = enableMddiSearch;
        this.enableScan = enableScan;
        this.enableLayout = defaultLayout;
        this.cid = currentCameraMode == WITH_MDDI_CLIENT ? cid : DEF_CID;
        this.sno = currentCameraMode == WITH_MDDI_CLIENT ? sno : DEF_SNO;
        this.dbSnoMode = dbSnoMode;
        this.cameraSessionHandler = new CameraSessionHandler(this, selectPrimaryCamera,
                !this.enableMddiSearch && enableScan, captureDelayMs, blurBeforeBarcode,
                checkBarcodeFormat, this.ratioMode);
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
        this.cameraSessionHandler.cameraMddiSearch.currentCapture = false;
        if (this.cameraInitialized) {
            return;
        }
        Log.d(TAG + "_ON_RESUME", binding.previewView.isAvailable() ? "Preview is available in " +
                "OnResume method. Initialising Camera" : "Preview is not available in OnResume " +
                "method. Setting up the texture view listener");
        // When resuming, if the preview is available, open the camera.
        if (binding.previewView.isAvailable()) {
            if (this.currentCameraMode == WITH_MDDI_CLIENT) {
                this.initCameraMddi(this.clientService, this.cameraMddiMode, this.activity, this.cid,
                        this.sno, this.cameraParameters, this.cameraMddiCallback);
            } else if (this.currentCameraMode == WITHOUT_MDDI_CLIENT) {
                this.initCameraBarcode(this.cameraParameters, this.activity, this.cameraCallback);
            } else {
                this.initCameraCaptureOnly(this.cameraParameters.cameraRatioMode, this.captureDelayMs,
                        this.cameraParameters.primaryCamera, this.activity, this.errorCallback);
            }
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
        stopMddiIfRunning();
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
     * Returns the current captured image and a converted version of it as a pair of bitmaps.
     * Caution: This method can only be used for capture only views, namely views that are
     * initialised with initCameraCaptureOnly(...).
     *
     * @param convertImage Indicates whether the bitmap should be also converted according the
     *                     specifications for MDDI.
     */
    public Pair<Bitmap, Bitmap> captureCurrentImage(boolean convertImage) throws ClientException {
        if (this.currentCameraMode != DEFAULT_CLIENT) {
            return null;
        }
        if (this.cameraSessionHandler.cameraMddiSearch.currentImage == null) {
            return null;
        }
        this.cameraSessionHandler.cameraMddiSearch.currentCapture = true;
        Bitmap bitmap = buildBitmapFromCameraImage(this.cameraSessionHandler.cameraMddiSearch.currentImage,
                this.cameraSessionHandler.cameraMddiSearch.currentRotationDegree, this.activity);
        this.cameraSessionHandler.cameraMddiSearch.currentImage.close();
        this.cameraSessionHandler.cameraMddiSearch.currentImage = null;
        if (bitmap != null && !convertImage) {
            return Pair.create(bitmap, null);
        }
        if (bitmap != null) {
            checkBitmap(bitmap, this.cameraSessionHandler.getWidthCropped(), this.cameraSessionHandler.getHeightCropped());
            Bitmap croppedBitmap = centerCropBitmap(bitmap, this.cameraSessionHandler.getWidthCropped(), this.cameraSessionHandler.getHeightCropped());
            MddiVariables.MddiImageSize imageSize = this.ratioMode == CameraParameters.CameraRatioMode.RATIO_1X1 ?
                    MddiVariables.MddiImageSize.FORMAT_512X512 : MddiVariables.MddiImageSize.FORMAT_480X640;
            Bitmap convertedBitmap = convertBitmapToMddiSpecs(croppedBitmap, imageSize.getWidth(), imageSize.getHeight());
            return Pair.create(bitmap, convertedBitmap);
        }
        return null;
    }

    /**
     * Switch the next camera.
     */
    public void switchNextCamera() {
        this.safeToSwitchCamera = false;
        Log.d(TAG + "_SWITCH_CAMERA", "CAMERA_SWITCH");
        this.stopMddiIfRunning();
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

    /**
     * Stop the mddi search(If the Mddi search is ON).
     */
    public void stopMddiSearch() {
        if (cameraSessionHandler == null) {
            return;
        }
        this.cameraSessionHandler.cameraMddiSearch.stopMddiSearch();
    }

    /**
     * Stop the mddi search if it is running.
     */
    private void stopMddiIfRunning() {
        this.cameraSessionHandler.cameraMddiSearch.stopMddiIfRunning();
    }

    /**
     * Resume the Mddi search.
     */
    public void resumeMddiSearch() {
        if (cameraSessionHandler == null) {
            return;
        }
        this.cameraSessionHandler.cameraMddiSearch.resumeMddiSearch();
    }

    /**
     * Check the ping of the MDDI backend with callback - AsyncTask.
     *
     * @param pingCallback is used to observe the ping response from MDDI backend.
     */
    public void checkConnection(Callback<PingResult> pingCallback) {
        if (this.currentCameraMode != WITH_MDDI_CLIENT) {
            pingCallback.onError(ExceptionType.CLIENT_EXCEPTION, new ClientException("Client service is not provided"));
            return;
        }
        this.clientService.checkConnection(pingCallback);
    }

    /**
     * Task to get sample image of a particular collection from the MDDI backend with callback -
     * AsyncTask.
     *
     * @param cid               is the cid of the MDDI get sample request.
     * @param imageNeeded       set this to true when the image should be sent along with the response. If not needed, set it to false.
     * @param getSampleCallback is used to observe the get sample response from MDDI backend.
     */
    public void getSample(String cid, boolean imageNeeded, Callback<GetSampleResult> getSampleCallback) {
        if (this.currentCameraMode != WITH_MDDI_CLIENT) {
            getSampleCallback.onError(ExceptionType.CLIENT_EXCEPTION, new ClientException("Client service is not provided"));
            return;
        }
        this.clientService.getSample(cid, imageNeeded,getSampleCallback);
    }

    /**
     * Task to delete a particular collection from the MDDI backend.
     *
     * @param cid            is the cid of the MDDI delete request.
     * @param deleteCallback is used to observe the delete response from MDDI backend.
     */
    public void deleteCollection(String cid, Callback<DeleteResult> deleteCallback) {
        if (this.currentCameraMode != WITH_MDDI_CLIENT) {
            deleteCallback.onError(ExceptionType.CLIENT_EXCEPTION, new ClientException("Client service is not provided"));
            return;
        }
        this.clientService.deleteCollection(cid, deleteCallback);
    }

    /**
     * Create the collection task for the given cid, sno and bitmap.
     *
     * @param image                  is the bitmap used to create the collection.
     * @param cid                    is the collection ID of the current MDDI request.
     * @param sno                    is the SNO of the current MDDI request.
     * @param collectionInfo         is the collection Info required to create a new collection.
     * @param collectionTaskCallback is used to observe the collection response from MDDI backend.
     */
    public void createCollection(Bitmap image, String cid, String sno, CollectionInfo collectionInfo, Callback<CollectionResult> collectionTaskCallback) {
        if (this.currentCameraMode != WITH_MDDI_CLIENT) {
            collectionTaskCallback.onError(ExceptionType.CLIENT_EXCEPTION, new ClientException("Client service is not provided"));
            return;
        }
        this.clientService.createCollection(image, cid, sno, collectionInfo, collectionTaskCallback);
    }

    /**
     * Task to add the image(in bitmap format) to the MDDI backend with given cid and sno
     *
     * @param image       is the image in bitmap format which is to be added to the MDDI
     *                    backend.
     * @param cid         is the cid of the current MDDI request.
     * @param sno         is the sno of the current MDDI request.
     * @param addCallback is used to observe the add response from MDDI backend.
     */
    public void addImage(Bitmap image, String cid, String sno, AddCallback addCallback) {
        if (this.currentCameraMode != WITH_MDDI_CLIENT) {
            addCallback.onError(ExceptionType.CLIENT_EXCEPTION, new ClientException("Client service is not provided"));
            return;
        }
        this.clientService.addImage(image, cid, sno, false, addCallback);
    }

    /**
     * Task to search the image from the MDDI backend with given cid and sno.
     *
     * @param image          is the image in bitmap format which is to be searched from the
     *                       MDDI backend.
     * @param cid            is the cid of the current MDDI request.
     * @param sno            is the sno of the current MDDI request.
     * @param searchCallBack is used to observe the search response from MDDI backend.
     */
    public void searchImage(Bitmap image, String cid, String sno, SearchCallBack searchCallBack) {
        if (this.currentCameraMode != WITH_MDDI_CLIENT) {
            searchCallBack.onError(ExceptionType.CLIENT_EXCEPTION, new ClientException("Client service is not provided"));
            return;
        }
        this.clientService.searchImage(image, cid, sno, false, searchCallBack);
    }

    /**
     * Task to check whether the image is suitable for adding to the MDDI backend.
     *
     * @param image                       is the image in bitmap format which is to be tested whether it is suitable for MDDI backend.
     * @param cid                         is the cid of the current MDDI request.
     * @param sno                         is the sno of the current MDDI request.
     * @param testMddiImageResultCallback is used to observe the test Mddi Image response from MDDI backend.
     */
    public void testMddiImage(Bitmap image, String cid, String sno, Callback<TestMddiImageResult> testMddiImageResultCallback) {
        if (this.currentCameraMode != WITH_MDDI_CLIENT) {
            testMddiImageResultCallback.onError(ExceptionType.CLIENT_EXCEPTION, new ClientException("Client service is not provided"));
            return;
        }
        this.clientService.testMddiImage(image, cid, sno, testMddiImageResultCallback);
    }
}
