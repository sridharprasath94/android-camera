package com.DyncoApp.ui.cameraScan;

import static android.content.Context.MODE_PRIVATE;
import static com.DyncoApp.ui.common.Constants.DEFAULT_OVERLAY;
import static com.DyncoApp.ui.common.Constants.DEFAULT_TOGGLE_FLASH;
import static com.DyncoApp.ui.common.Constants.NEGATIVE_SEARCH_THRESHOLD;
import static com.DyncoApp.ui.common.MddiMode.REGISTER;
import static com.DyncoApp.ui.common.MddiMode.VERIFY;
import static com.dynamicelement.sdk.android.add.AddImageStatus.DUPLICATE;
import static com.dynamicelement.sdk.android.add.AddImageStatus.ERROR;
import static com.dynamicelement.sdk.android.add.AddImageStatus.SUCCESS;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.createResizedBitmap;
import static com.dynamicelement.sdk.android.ui.CameraMddiMode.MDDI_SEARCH_OFF;
import static com.dynamicelement.sdk.android.ui.CameraMddiMode.MDDI_SEARCH_ON;
import static com.dynamicelement.sdk.android.ui.CameraParameters.CameraRatioMode.RATIO_1X1;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.DyncoApp.R;
import com.DyncoApp.ui.common.Constants;
import com.DyncoApp.ui.common.MddiMode;
import com.dynamicelement.mddi.AddStreamResponse;
import com.dynamicelement.mddi.SearchStreamResponse;
import com.dynamicelement.sdk.android.Callback;
import com.dynamicelement.sdk.android.add.AddCallback;
import com.dynamicelement.sdk.android.add.AddResult;
import com.dynamicelement.sdk.android.collection.CollectionResult;
import com.dynamicelement.sdk.android.exceptions.ExceptionType;
import com.dynamicelement.sdk.android.getsample.GetSampleResult;
import com.dynamicelement.sdk.android.mddiclient.ClientService;
import com.dynamicelement.sdk.android.search.SearchResult;
import com.dynamicelement.sdk.android.ui.CameraMddiCallback;
import com.dynamicelement.sdk.android.ui.CameraParameters;
import com.dynamicelement.sdk.android.ui.CameraView;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class CameraScanModel extends ViewModel {

    public MutableLiveData<Integer> getServerResponseCountObserver() {
        return serverResponseCountObserver;
    }

    public MutableLiveData<Bitmap> getGetSampleImageObserver() {
        return getSampleImageObserver;
    }

    public MutableLiveData<SearchResult> getPositiveResponseObserver() {
        return positiveResponseObserver;
    }

    public MutableLiveData<Exception> getExceptionObserver() {
        return exceptionObserver;
    }

    public LiveData<Void> getNegativeThresholdObserver() {
        return negativeThresholdObserver;
    }

    public MutableLiveData<AddResult> getAddResponseObserver() {
        return addResponseObserver;
    }

    public MutableLiveData<String> getVerifyTextObserver() {
        return verifyTextObserver;
    }

    public Bitmap getCurrentBitmap() {
        return currentBitmap;
    }

    public ClientService getClientService() {
        return clientService;
    }

    private final String KEY_OVERLAY_OPTION;

    private final String KEY_TOGGLE_FLASH;
    private final WeakReference<Context> contextRef;
    private final ClientService clientService;
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;
    private final MutableLiveData<Integer> serverResponseCountObserver = new MutableLiveData<>();
    private final MutableLiveData<Bitmap> getSampleImageObserver = new MutableLiveData<>();
    private final MutableLiveData<SearchResult> positiveResponseObserver = new MutableLiveData<>();
    private final MutableLiveData<Void> negativeThresholdObserver = new MutableLiveData<>();
    private final MutableLiveData<AddResult> addResponseObserver = new MutableLiveData<>();
    private final MutableLiveData<String> verifyTextObserver = new MutableLiveData<>();
    private final MutableLiveData<Exception> exceptionObserver = new MutableLiveData<>();
    private Integer negativeResponseCount = 0;
    private Timer timer;
    private int verifyDotCount = 1;
    private boolean isUpdatingVerifyText = false;
    private Bitmap currentBitmap;

    public CameraScanModel(Context context, ClientService clientService) {
        this.contextRef = new WeakReference<>(context);
        this.clientService = clientService;
        KEY_OVERLAY_OPTION = context.
                getString(R.string.camera_scan_screen_overlay_option);
        KEY_TOGGLE_FLASH = context.
                getString(R.string.secret_screen_flash);
        sharedPreferences = context.getSharedPreferences(context.
                getString(R.string.shared_preferences), MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }


    boolean getSavedFlash() {
        return sharedPreferences.getBoolean(KEY_TOGGLE_FLASH, DEFAULT_TOGGLE_FLASH);
    }

    void saveFlash(boolean toggleFlash) {
        editor.putBoolean(KEY_TOGGLE_FLASH, toggleFlash).apply();
    }

    void getTheSampleImage(String cid) {
        clientService.getSample(cid, true, new Callback<GetSampleResult>() {
            @Override
            public void onResponse(GetSampleResult response) {
                if (response.getSampleImage() == null) {
                    return;
                }
                AsyncTask.execute(() -> {
                    Bitmap resized = createResizedBitmap(response.getSampleImage(),
                            clientService.getMddiImageSize().getWidth() / 2,
                            clientService.getMddiImageSize().getHeight() / 2,
                            Bitmap.Config.ARGB_8888);
                    getSampleImageObserver.postValue(resized);

                });
            }

            @Override
            public void onError(ExceptionType exceptionType, Exception e) {
                Log.d("Camera scan model", e.getMessage());
            }
        });
    }

    boolean getOverlayValue() {
        return sharedPreferences.getBoolean(KEY_OVERLAY_OPTION, DEFAULT_OVERLAY);
    }

    void saveOverlayValue(boolean overlayEnabled) {
        editor.putBoolean(KEY_OVERLAY_OPTION, overlayEnabled).apply();
    }


    void createCollection(Bitmap bitmap, String cid, boolean createCollection) {
        if (createCollection) {
            clientService.createCollection(bitmap, cid,
                    cid,
                    Constants.getCollectionInfo(), new Callback<CollectionResult>() {
                        @Override
                        public void onResponse(CollectionResult response) {
                            if (response.isCollectionCreated()) {
                                addImage(bitmap, cid);

                            }
                        }

                        @Override
                        public void onError(ExceptionType exceptionType, Exception e) {
                            exceptionObserver.postValue(e);
                        }
                    });
        } else {
            addImage(bitmap, cid);
        }
    }

    void addImage(Bitmap bitmap, String cid) {
        clientService.addImage(bitmap, cid,
                Constants.DEFAULT_SNO,
                false, new AddCallback() {
                    @Override
                    public void onNextResponse(AddStreamResponse addStreamResponse,
                                               AddResult result) {
                        if (result.getAddImageStatus() == SUCCESS) {
                            addResponseObserver.postValue(result);
                        } else if (result.getAddImageStatus() == DUPLICATE) {
                            exceptionObserver.postValue(new Exception("Duplicate Image. Image already in the database"));
                        } else if (result.getAddImageStatus() == ERROR) {
                            exceptionObserver.postValue(new Exception("Error Image. Not enough features"));
                        }
                    }

                    @Override
                    public void onCompleted(String elapsedTime, String summaryMessage) {
                    }

                    @Override
                    public void onError(ExceptionType exceptionType, Exception e) {
                        exceptionObserver.postValue(e);
                    }
                });
    }

    void initMddi(CameraView cameraView, MddiMode mddiMode, String cid) {
        if (mddiMode == VERIFY) {
            getTheSampleImage(cid);
        }
        CameraParameters cameraParameters = new CameraParameters.Builder()
                .selectRatio(RATIO_1X1)
                .enableBarcodeScan(false)
                .enableDefaultLayout(mddiMode == VERIFY)
                .selectPrimaryCamera(false)
                .blurBeforeBarcode(true)
                .build();

        cameraView.initCameraMddi(clientService,
                mddiMode == REGISTER ? MDDI_SEARCH_OFF : MDDI_SEARCH_ON,
                (Activity) this.contextRef.get(),
                cid,
                Constants.DEFAULT_SNO
                ,
                cameraParameters, new CameraMddiCallback() {
                    @Override
                    public void onInitialised(int viewWidth, int viewHeight) {

                    }

                    @Override
                    public void onImageObtained(Bitmap bitmap, String barcodeResult, String cid, String sno) {
                        currentBitmap = bitmap;
                    }

                    @Override
                    public void onNegativeResponse(SearchStreamResponse searchStreamResponse, SearchResult searchResult) {
                        negativeResponseCount++;
                        if (!isUpdatingVerifyText) {
                            updateVerifyText();
                        }

                        serverResponseCountObserver.postValue(negativeResponseCount);

                        if (negativeResponseCount == NEGATIVE_SEARCH_THRESHOLD) {
                            stopVerifyText();
                            negativeThresholdObserver.postValue(null);
                            negativeResponseCount = 0;
                        }
                    }


                    @Override
                    public void onPositiveResponse(SearchStreamResponse searchStreamResponse, SearchResult searchResult) {
                        positiveResponseObserver.postValue(searchResult);
                        stopVerifyText();
                    }

                    @Override
                    public void onCompleted(String elapsedTime, String summaryMessage) {
                        stopVerifyText();
                    }

                    @Override
                    public void onError(ExceptionType type, Exception e) {
                        exceptionObserver.postValue(e);
                        stopVerifyText();
                    }
                });
    }


    private void updateVerifyText() {
        isUpdatingVerifyText = true;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                String dots = new String(new char[verifyDotCount]).replace("\0", ".");
                verifyTextObserver.postValue("Verifying" + dots);
                verifyDotCount = (verifyDotCount % 3) + 1;

            }
        }, 0, 500);
    }

    void stopVerifyText() {
        isUpdatingVerifyText = false;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
