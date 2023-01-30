package com.DyncoApp.ui;

import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.buildBitmapFromIntegerList;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.createResizedBitmap;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.getBytesFromBitmap;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.getImageVariance;
import static com.dynamicelement.sdk.android.ui.CameraMddiMode.MDDI_SEARCH_ON;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.DyncoApp.R;
import com.dynamicelement.mddi.AddStreamResponse;
import com.dynamicelement.mddi.SearchStreamResponse;
import com.dynamicelement.sdk.android.Callback;
import com.dynamicelement.sdk.android.add.AddCallback;
import com.dynamicelement.sdk.android.add.AddImageStatus;
import com.dynamicelement.sdk.android.add.AddResult;
import com.dynamicelement.sdk.android.exceptions.ExceptionType;
import com.dynamicelement.sdk.android.getsample.GetSampleResult;
import com.dynamicelement.sdk.android.mddiclient.ClientService;
import com.dynamicelement.sdk.android.misc.InstanceType;
import com.dynamicelement.sdk.android.search.SearchResult;
import com.dynamicelement.sdk.android.ui.CameraMddiCallback;
import com.dynamicelement.sdk.android.ui.CameraParameters;
import com.dynamicelement.sdk.android.ui.CameraView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class A3_CameraScan extends AppCompatActivity implements View.OnClickListener {
    private static final String sharedprefs = "sharedPrefsOverlay";
    private static final String overlayOption = "overlayOption";
    private static final String DirectoryCount = "DirectoryCount";
    private SharedPreferences sharedPreferences;
    protected CameraView cameraview;
    protected AnimationDrawable uploadingAnimation;
    protected TextView addingTextview;
    protected ImageView uploadingImageview;
    protected ImageView overlayImageView;
    //    protected ImageButton flashButton;
//    protected ImageButton switchCameraButton;
    protected ImageButton overlayButton;
    //    protected ImageButton zoomAdjustButton;
    protected ImageButton backButton;
    protected ImageButton homeButton;
    protected ImageButton wifiButton;
    protected TextView sttTextView;
    protected SeekBar zoomSeekbar;
    protected ImageView mddiImageView;
    protected TextView mddiTextView;
    protected GlobalVariables globalVariables = new GlobalVariables();
    protected Bitmap addBitmap;
    protected String mddiCid;
    protected String mddiSno;
    protected String decodedBarcodeResult;
    protected boolean flashState;
    protected boolean isZoomButtonVisible = false;
    protected boolean sampleImageObtained = false;
    protected boolean overlayEnabled = false;
    protected Vibrator vibrator;

    protected TextView picCountView;
    protected int pictureCount = 1;
    protected int DEFAULT_DIR_COUNT = 0;
    protected int currentDirectoryCount;
    protected File picDir;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @SuppressLint("CommitPrefEdits")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a3_camerascan);
        sharedPreferences = getSharedPreferences(sharedprefs, MODE_PRIVATE);

        globalVariables = (GlobalVariables) getApplicationContext();
        globalVariables.userSno = "1";
        initializeLayout();

//        currentDirectoryCount = sharedPreferences.getInt(DirectoryCount, DEFAULT_DIR_COUNT);
//        currentDirectoryCount++;
//        sharedPreferences.edit().putInt(DirectoryCount,currentDirectoryCount).apply();
//        Log.d("Directory count", String.valueOf(currentDirectoryCount));
//        picDir = (Environment.getExternalStoragePublicDirectory(globalVariables.picDir + "/" +
//        "Mddi_" + String.valueOf(currentDirectoryCount)));
//
//
//        boolean ff = removeDirectory(picDir);
//        Log.d("Image", String.valueOf(ff));
//        if (!picDir.exists()) {
//            picDir.mkdirs();
//        }

        // cameraSettingsMddiSdk = "PRIMARY CAMERA";

        if (globalVariables.versionOneSelected) {
//            startSearchV1();
        } else {
            startSearch();
        }

        flashState = globalVariables.toggleFlash;
//        flashButton.setImageResource(flashState ? R.drawable.ic_flash_on : R.drawable
//        .ic_flash_off_);
        zoomSeekbar.setProgress(5);
        zoomSeekbar.setMin(1);
        zoomSeekbar.setMax(cameraview.getMaxZoom());
        cameraview.changeZoomLevel(6);
//        cameraview.autoFitViewToDisplay();

        zoomSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                cameraview.changeZoomLevel(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isZoomButtonVisible = true;
                zoomSeekbar.setProgress(seekBar.getProgress());
                disableLayoutItems();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isZoomButtonVisible = false;
                zoomSeekbar.setVisibility(View.INVISIBLE);
                zoomSeekbar.setEnabled(false);
                Toast.makeText(A3_CameraScan.this,
                        "Zoom Level is " + seekBar.getProgress() + "/" + seekBar.getMax(),
                        Toast.LENGTH_SHORT).show();
                //Enable the layout buttons when we stop tracking the seekbar
                enableLayoutItems();
            }
        });

        cameraview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraview.focusCamera();
            }
        });
        cameraview.setOnLongClickListener(v -> {

            if (globalVariables.selectedUserMode == SelectedUserMode.READONLY) {
                return false;
            }

            if (globalVariables.versionOneSelected) {
                return false;
            }
            if (addBitmap == null) {
                Toast.makeText(this, "Wait for some time", Toast.LENGTH_SHORT).show();
                return false;
            }

            double sharpness = getImageVariance(addBitmap, 480, 640, Bitmap.Config.ARGB_8888);
            if (sharpness < 40) {
                Toast.makeText(this, "Not enough features. Add another image",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
            cameraview.stopMddiSearch();
//            Toast.makeText(this, "The sharpness is " + sharpness, Toast.LENGTH_SHORT).show();
            Bitmap resizedBitmap = createResizedBitmap(addBitmap, 480, 640,
                    Bitmap.Config.ARGB_8888);
            addProcess(resizedBitmap);

            return false;
        });
    }

    /**
     * Start the search for version 1.0
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    private void startSearch() {
        CameraParameters cameraParameters = new CameraParameters.Builder()
                .selectRatio(CameraParameters.CameraRatioMode.RATIO_1X1)
                .enableBarcodeScan(true)
                .enableDefaultLayout(false)
                .selectPrimaryCamera(true)
                .blurBeforeBarcode(true)
                .build();
        cameraview.initCameraMddi(globalVariables.clientService, MDDI_SEARCH_ON, this,
                globalVariables.userCid,
                globalVariables.userSno,
                cameraParameters, new CameraMddiCallback() {
                    @Override
                    public void onInitialised(int overlayWidth, int overlayHeight) {

                        if (globalVariables.clientService.getInstanceType() != InstanceType.DB_SNO) {
                            if (!sampleImageObtained) {
                                getSampleImage(globalVariables.userCid);
                            }
                        } else {
                            getSampleImage("bm4");
                        }
                        Log.d("OVERLAY_HEIGHT", String.valueOf(overlayHeight));
                        Log.d("OVERLAY_WIDTH", String.valueOf(overlayWidth));
                        runOnUiThread(() -> {
                            overlayImageView.requestLayout();
                            overlayImageView.getLayoutParams().height = overlayHeight;
                            overlayImageView.getLayoutParams().width = overlayWidth;
                        });
                    }

                    @Override
                    public void onImageObtained(Bitmap bitmap, String barcodeResult, String cid,
                                                String sno) {

                        Log.d("ONGETIMAGE", String.valueOf(bitmap.getWidth()));
                        if (globalVariables.clientService.getInstanceType() == InstanceType.DB_SNO) {
                            if (decodedBarcodeResult == null) {
                                return;
                            }
                            sampleImageObtained = false;
                            getSampleImage(cid);
                        }

                        addBitmap = bitmap;
                        mddiCid = cid;
                        mddiSno = sno;
                        decodedBarcodeResult = barcodeResult;
                    }

                    @Override
                    public void onNegativeResponse(SearchStreamResponse searchStreamResponse,
                                                   Bitmap bitmap, SearchResult searchResult) {
//                        runOnUiThread(() -> {
//                            wifiButton.setVisibility(View.VISIBLE);
//                            sttTextView.setVisibility(View.VISIBLE);
//                            wifiButton.setImageResource(searchResult.ping == PingStates
//                            .PING_GOOD ?
//                                    R.drawable.ic_wifi_1 : searchResult.ping == PingStates
//                                    .PING_AVERAGE ?
//                                    R.drawable.ic_wifi_2 : searchResult.ping == PingStates
//                                    .PING_BAD ?
//                                    R.drawable.ic_wifi_3 : R.drawable.ic_wifi_4);
//                            sttTextView.setText(searchStreamResponse.getSingleTripTime());
//                        });
                    }

                    @Override
                    public void onPositiveResponse(SearchStreamResponse searchStreamResponse,
                                                   Bitmap bitmap, float searchScore, int rating,
                                                   String sno, String uid) {
                        cameraview.stopMddiSearch();
                        globalVariables.sqlCid = searchStreamResponse.getCid();

                        //Go to the next activity
                        Intent intent = new Intent(getApplicationContext(), A4_ResultScreen.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("Option", "Search");
                        ByteArrayOutputStream bs = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, bs);
                        intent.putExtra("byteArray", bs.toByteArray()).putExtra("UID", uid).putExtra("SNO", sno).
                                putExtra("SCORE", searchScore).putExtra("RATING", rating).
                                putExtras(bundle).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }

                    @Override
                    public void onError(ExceptionType type, Exception e) {
                        if (!globalVariables.createCollection) {
                            runOnUiThread(() -> Toast.makeText(getApplicationContext(),
                                    e.getMessage(), Toast.LENGTH_SHORT).show());
                            Intent intent = new Intent(getApplicationContext(),
                                    A2_LoginScreen.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                            return;
                        }
                        if (!Objects.requireNonNull(e.getMessage()).startsWith("Invalid " +
                                "collection")) {
                            onBackPressed();
                        }
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(),
                                "Provided collection not exists. Long press the screen to add the" +
                                        " first image", Toast.LENGTH_SHORT).show());
                    }
                });
    }

//    /**
//     * Start the search for version 1.1
//     */
//    @RequiresApi(api = Build.VERSION_CODES.P)
//    private void startSearchV1() {
//        CameraParameters cameraParameters = new CameraParameters.Builder(MDDI_SEARCH_ON)
//                .setLayoutConfig(LAYOUT_WITH_SCAN)
//                .build();
//        cameraview.initCameraV1(globalVariables.clientServiceV1, globalVariables.userCid,
//                globalVariables.userSno, cameraParameters, new CameraV1MddiCallback() {
//                    @Override
//                    public void onInitialised(int overlayWidth, int overlayHeight) {
//                        if (globalVariables.clientServiceV1.instanceType != com.mddiv1.misc
//                        .InstanceType.DB_SNO) {
//                            if (sampleImageObtained) {
//                                return;
//                            }
//                            getSampleImageV1(globalVariables.userCid);
//                        } else {
//                            getSampleImageV1(globalVariables.userCid);
//                        }
//                        Log.d("OVERLAY_HEIGHT", String.valueOf(overlayHeight));
//                        Log.d("OVERLAY_WIDTH", String.valueOf(overlayWidth));
//                        runOnUiThread(() -> {
//                            overlayImageView.requestLayout();
//                            overlayImageView.getLayoutParams().height = overlayHeight;
//                            overlayImageView.getLayoutParams().width = overlayWidth;
//                        });
//
////                        if (sampleImageObtained) {
////                            return;
////                        }
////                        getSampleImageV1(globalVariables.userCid);
////                        runOnUiThread(() -> {
////                            overlayImageView.requestLayout();
////                            overlayImageView.getLayoutParams().height = overlayHeight;
////                            overlayImageView.getLayoutParams().width = overlayWidth;
////                        });
//                    }
//
//                    @Override
//                    public void onImageObtained(Bitmap bitmap, String barcodeResult, String cid,
//                                                String sno) {
//
//                        Log.d("ONGETIMAGE", String.valueOf(bitmap.getWidth()));
//                        if (globalVariables.clientServiceV1.instanceType == com.mddiv1.misc
//                        .InstanceType.DB_SNO) {
//                            if (barcodeResult == null) {
//                                return;
//                            }
//                            sampleImageObtained = false;
//                            getSampleImageV1(cid);
//                        }
//
//                        addBitmap = bitmap;
//                        mddiCid = cid;
//                        mddiSno = sno;
////                        decodedBarcodeResult = barcodeResult;
//                    }
//
//                    @Override
//                    public void onNegativeResponse(io.dynamicelement.grpc.mddi
//                    .SearchStreamResponse searchStreamResponse, Bitmap bitmap, com.mddiv1
//                    .search.SearchResult searchResult) {
//                        Log.d("Mddi score",
//                                String.valueOf(searchStreamResponse.getSearchresponse()
//                                .getScore()));
//
////                        runOnUiThread(() -> {
//////                            picCountView.setVisibility(View.VISIBLE);
//////                            picCountView.setText(String.valueOf(pictureCount));
////                            mddiTextView.setText(searchResult.imageLogMessage);
////                            mddiImageView.setImageBitmap(bitmap);
////                        });
////
////                        saveImage(bitmap, picDir, "IVF_ImageDatasets",  String.valueOf
////                        (pictureCount));
////                        pictureCount++;
//
////                        runOnUiThread(() -> {
////                            wifiButton.setVisibility(View.VISIBLE);
////                            sttTextView.setVisibility(View.VISIBLE);
////                            wifiButton.setImageResource(searchResult.ping == com.mddiv1.misc
////                            .PingStates.PING_GOOD?
////                                    R.drawable.ic_wifi_1 : searchResult.ping == com.mddiv1.misc
////                                    .PingStates.PING_AVERAGE ?
////                                    R.drawable.ic_wifi_2 : searchResult.ping == com.mddiv1.misc
////                                    .PingStates.PING_BAD ?
////                                    R.drawable.ic_wifi_3 : R.drawable.ic_wifi_4);
////                            sttTextView.setText(searchStreamResponse.getSingleTripTime());
////                        });
//                    }
//
//                    @Override
//                    public void onPositiveResponse(io.dynamicelement.grpc.mddi
//                    .SearchStreamResponse searchStreamResponse, Bitmap bitmap, float
//                    searchScore, int rating, String sno, String uid) {
//                        cameraview.stopMddiSearch();
//                        globalVariables.sqlCid = searchStreamResponse.getCID();
//
//                        //Go to the next activity
//                        Intent intent = new Intent(getApplicationContext(), A4_ResultScreen
//                        .class);
//                        Bundle bundle = new Bundle();
//                        bundle.putString("Option", "Search");
//                        ByteArrayOutputStream bs = new ByteArrayOutputStream();
//                        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, bs);
//                        intent.putExtra("byteArray", bs.toByteArray()).putExtra("UID", uid)
//                        .putExtra("SNO", sno).
//                                putExtra("SCORE", searchScore).putExtra("RATING", rating).
//                                putExtras(bundle).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                        startActivity(intent);
//                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
//                    }
//
//                    @Override
//                    public void onError(com.mddiv1.exceptions.ExceptionType type, Exception e) {
//                        if (!globalVariables.createCollection) {
//                            runOnUiThread(() -> Toast.makeText(getApplicationContext(),
//                                    e.getMessage(), Toast.LENGTH_SHORT).show());
//                            Intent intent = new Intent(getApplicationContext(),
//                                    A2_LoginScreen.class);
//                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                            startActivity(intent);
//                            overridePendingTransition(R.anim.slide_in_left, R.anim
//                            .slide_out_right);
//                            return;
//                        }
//                        if (!Objects.requireNonNull(e.getMessage()).startsWith("Invalid " +
//                                "collection")) {
//                            onBackPressed();
//                        }
//                        runOnUiThread(() -> Toast.makeText(getApplicationContext(),
//                                "Provided collection not exists. Long press the screen to add
//                                the" +
//                                        " first image", Toast.LENGTH_SHORT).show());
//                    }
//                });
//        cameraview.changeFlashState(globalVariables.toggleFlash);
//    }

    /**
     * On click listener for the image buttons
     */
    @SuppressLint("NonConstantResourceId")
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onClick(View v) {
        vibrator.vibrate(VibrationEffect.createOneShot(75, VibrationEffect.DEFAULT_AMPLITUDE));
        switch (v.getId()) {
//            case R.id.switchCameraButton:
//                cameraview.switchNextCamera();
//                break;
//
//            case R.id.flashButton:
//                flashButton.setImageResource(flashState ? R.drawable.ic_flash_off_ : R.drawable
//                .ic_flash_on);
//                cameraview.changeFlashState(!flashState);
//                flashState = !flashState;
//                break;
//
//            case R.id.zoomButton:
//                zoomAdjustButton.setImageResource(isZoomButtonVisible ? R.drawable.ic_zoom : R
//                .drawable.ic_baseline_zoom_in_24);
//                zoomSeekbar.setVisibility(isZoomButtonVisible ? View.INVISIBLE : View.VISIBLE);
//                zoomSeekbar.setEnabled(!isZoomButtonVisible);
//                isZoomButtonVisible = !isZoomButtonVisible;
//                break;

            case R.id.overlayButton:
                overlayButton.setImageResource(overlayEnabled ? R.drawable.ic_stack_slash :
                        R.drawable.ic_stack);
                overlayImageView.setVisibility(overlayEnabled ? View.INVISIBLE : View.VISIBLE);
                overlayEnabled = !overlayEnabled;
                sharedPreferences.edit().putBoolean(overlayOption, overlayEnabled).apply();
                break;

            case R.id.backButton:
                onBackPressed();
                break;
            case R.id.homeButton:
                cameraview.onPause();
                Intent intent = new Intent(getApplicationContext(), A1_HomeScreen.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                break;
        }
    }

    /**
     * Check camera permission
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraview.checkCameraPermission(requestCode, permissions, grantResults);
    }

    /**
     * When the activity is resumed
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onResume() {
        super.onResume();
        cameraview.onResume();
    }

    /**
     * When the activity is paused
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onPause() {
        super.onPause();
        cameraview.onPause();
    }

    /**
     * When press the back button
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBackPressed() {
        cameraview.onPause();
        Intent intent = new Intent(getApplicationContext(), A2_LoginScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    /**
     * Initialize the layout
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    protected void initializeLayout() {
        cameraview = findViewById(R.id.cameraView);
        uploadingImageview = findViewById(R.id.uploadingImageView);
        overlayImageView = findViewById(R.id.overlayImageView);
        addingTextview = findViewById(R.id.addTextView);
//        flashButton = findViewById(R.id.flashButton);
//        switchCameraButton = findViewById(R.id.switchCameraButton);
        overlayButton = findViewById(R.id.overlayButton);
//        zoomAdjustButton = findViewById(R.id.zoomButton);
        zoomSeekbar = findViewById(R.id.zoomSeekbar);
        backButton = findViewById(R.id.backButton);
        homeButton = findViewById(R.id.homeButton);
        wifiButton = findViewById(R.id.wifiButton);
        sttTextView = findViewById(R.id.singleTripTimeTextView);
        mddiImageView = findViewById(R.id.mddiImageView);
        mddiTextView = findViewById(R.id.mddiTextView);
        picCountView = findViewById(R.id.picCountTextView);

//        flashButton.setOnClickListener(this);
//        zoomAdjustButton.setOnClickListener(this);
//        switchCameraButton.setOnClickListener(this);
        overlayButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
        homeButton.setOnClickListener(this);

        uploadingImageview.setBackgroundResource(R.drawable.upload_anim);
        uploadingAnimation = (AnimationDrawable) uploadingImageview.getBackground();
        uploadingImageview.setVisibility(View.INVISIBLE);
        overlayImageView.setVisibility(View.INVISIBLE);
        addingTextview.setVisibility(View.INVISIBLE);
        zoomSeekbar.setVisibility(View.INVISIBLE);

        picCountView.setVisibility(View.INVISIBLE);
        //Get the vibration service from the system
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        overlayEnabled = sharedPreferences.getBoolean(overlayOption, true);

        overlayButton.setImageResource(overlayEnabled ? R.drawable.ic_stack :
                R.drawable.ic_stack_slash);
        overlayImageView.setVisibility(overlayEnabled ? View.VISIBLE : View.INVISIBLE);

        wifiButton.setVisibility(View.INVISIBLE);
        sttTextView.setVisibility(View.INVISIBLE);
        mddiImageView.setVisibility(View.VISIBLE);
        mddiTextView.setVisibility(View.VISIBLE);
//        cameraview.changeFlashState(false);

//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
////        int height = displayMetrics.heightPixels;
//        int width = displayMetrics.widthPixels;
//        width = width - 150;
//
//        while (width % 3 != 0) {
//            width++;
//        }
//        int height = (width * 4) / 3;
//        cameraview.requestLayout();
//        cameraview.getLayoutParams().width = width;
//        cameraview.getLayoutParams().height = height;
    }

    /**
     * Enable the layout items
     */
    private void enableLayoutItems() {
//        flashButton.setVisibility(View.VISIBLE);
//        switchCameraButton.setVisibility(View.VISIBLE);
//        zoomAdjustButton.setVisibility(View.VISIBLE);
        overlayButton.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.VISIBLE);
        homeButton.setVisibility(View.VISIBLE);
    }

    /**
     * Disable the layout items
     */
    private void disableLayoutItems() {
//        flashButton.setVisibility(View.INVISIBLE);
//        switchCameraButton.setVisibility(View.INVISIBLE);
//        zoomAdjustButton.setVisibility(View.INVISIBLE);
        overlayButton.setVisibility(View.INVISIBLE);
        backButton.setVisibility(View.INVISIBLE);
        homeButton.setVisibility(View.INVISIBLE);
//        zoomAdjustButton.setImageResource(R.drawable.ic_zoom);
    }

//    /**
//     * Get sample task
//     */
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    protected void getSampleTask(String cid) {
//        if(globalVariables.versionOneSelected){
//            getSampleImageV1(cid);
//        }else{
//            getSampleImage(cid);
//        }
//
//    }

//    @RequiresApi(api = Build.VERSION_CODES.O)
//    private void getSampleImageV1(String cid) {
//        try (com.mddiv1.mddiclient.ClientService clientServiceAutoClosable =
//                     globalVariables.clientServiceV1.createNewSession()) {
//            clientServiceAutoClosable.getSample(cid,
//                    new com.mddiv1.Callback<com.mddiv1.getsample.GetSampleResult>() {
//                        @Override
//                        public void onResponse(com.mddiv1.getsample.GetSampleResult response) {
//                            sampleImageObtained = true;
//                            overlayImageView.setAlpha(100);
//
//                            AsyncTask.execute(() -> {
//                                Bitmap bitmap = buildBitmapFromIntegerList(response.pixelsList,
//                                        512, 512,
//                                        Bitmap.Config.RGB_565);
//                                runOnUiThread(() -> {
////                            overlayImageView.setVisibility(View.VISIBLE);
//                                    overlayImageView.setImageBitmap(bitmap);
//                                });
//                            });
//                        }
//
//                        @Override
//                        public void onError(com.mddiv1.exceptions.ExceptionType exceptionType,
//                                            Exception e) {
//                            e.printStackTrace();
//                        }
//                    });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getSampleImage(String cid) {
        try (ClientService clientServiceAutoClosable =
                     globalVariables.clientService.createNewSession()) {
            clientServiceAutoClosable.getSample(cid, new Callback<GetSampleResult>() {
                @Override
                public void onResponse(GetSampleResult response) {
                    Log.d("ONGET", String.valueOf(response.status));
                    sampleImageObtained = true;
                    overlayImageView.setAlpha(100);

                    AsyncTask.execute(() -> {
                        Bitmap bitmap = buildBitmapFromIntegerList(response.pixelsList, 480, 640,
                                Bitmap.Config.RGB_565);
                        Bitmap resized = createResizedBitmap(bitmap, 240, 320,
                                Bitmap.Config.ARGB_8888);
                        runOnUiThread(() -> {
//                            overlayImageView.setVisibility(View.VISIBLE);
                            overlayImageView.setImageBitmap(resized);
                        });
                    });
                }

                @Override
                public void onError(ExceptionType exceptionType, Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Dialog builder to add an image
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void addProcess(Bitmap bitmap) {

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this).setCancelable(false).setTitle("Add the image").
                        setMessage("Do you want to add this image?").setIcon(R.drawable.dynamicelementlogo);

        ImageView showImage = new ImageView(A3_CameraScan.this);
        showImage.setImageBitmap(bitmap);
        builder.setView(showImage);

        builder.setPositiveButton("Yes", (dialog, option) -> {
            runOnUiThread(() -> {
                addingTextview.setVisibility(View.VISIBLE);
                uploadingAnimation.start();
                uploadingImageview.setVisibility(View.VISIBLE);
            });
            add(bitmap);
        });

        builder.setNegativeButton("No", (dialog, option) -> {
            cameraview.resumeMddiSearch();

            Toast.makeText(getApplicationContext(), "The operation has been cancelled",
                    Toast.LENGTH_SHORT).show();
        });

        builder.create().show();
    }

    /**
     * Add task to add the image to the Mddi database
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void add(Bitmap bitmap) {
        if (globalVariables.versionOneSelected) {
//            addImageV1(bitmap);
        } else {
            addImage(bitmap);
        }
    }

//    @RequiresApi(api = Build.VERSION_CODES.O)
//    private void addImageV1(Bitmap bitmap) {
//        globalVariables.clientServiceV1.createNewSession().addBitmap(bitmap, mddiCid, mddiSno,
//                globalVariables.createCollection, new com.mddiv1.add.AddCallback() {
//                    @Override
//                    public void onNextResponse(io.dynamicelement.grpc.mddi.AddStreamResponse
//                    addStreamResponse, Bitmap bitmap, com.mddiv1.add.AddResult result) {
//                        if (result.addImageStatus == com.mddiv1.add.AddImageStatus.SUCCESS) {
//
//                            globalVariables.createCollection = false;
//                            runOnUiThread(() -> Toast.makeText(A3_CameraScan.this, "Added the " +
//                                            "image",
//                                    Toast.LENGTH_SHORT).show());
//
//                            Intent intent = new Intent(getApplicationContext(),
//                                    A4_ResultScreen.class);
//                            Bundle bundle = new Bundle();
//                            bundle.putString("Option", "Add");
//
//                            ByteArrayOutputStream bs = new ByteArrayOutputStream();
//                            bitmap.compress(Bitmap.CompressFormat.PNG, 50, bs);
//                            intent.putExtra("byteArray", bs.toByteArray()).putExtra("SNO",
//                                    mddiSno).putExtra("CID", mddiCid).
//                                    putExtras(bundle).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                            bitmap.recycle();
//                            startActivity(intent);
//                            overridePendingTransition(R.anim.slide_in_right, R.anim
//                            .slide_out_left);
//                        } else if (result.addImageStatus == com.mddiv1.add.AddImageStatus
//                        .DUPLICATE) {
//                            errorWhileAdding("Duplicate Image. Image already in the database");
//                        } else if (result.addImageStatus == com.mddiv1.add.AddImageStatus.ERROR) {
//                            errorWhileAdding("Error Image. Not enough features");
//                        }
//                    }
//
//                    @Override
//                    public void onCompleted(List<io.dynamicelement.grpc.mddi.AddStreamResponse>
//                    addStreamResponses, String elapsedTime, String summaryMessage) {
//
//                    }
//
//                    @Override
//                    public void onError(com.mddiv1.exceptions.ExceptionType exceptionType,
//                                        Exception e) {
//                        errorWhileAdding(exceptionType + e.getMessage());
//                    }
//                });
//    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void addImage(Bitmap bitmap) {
        globalVariables.clientService.createNewSession().addBitmap(bitmap, mddiCid, mddiSno,
                globalVariables.createCollection, new AddCallback() {
                    @Override
                    public void onNextResponse(AddStreamResponse addStreamResponse, Bitmap bitmap,
                                               AddResult result) {
                        if (result.addImageStatus == AddImageStatus.SUCCESS) {

                            globalVariables.createCollection = false;
                            runOnUiThread(() -> Toast.makeText(A3_CameraScan.this, "Added the " +
                                            "image",
                                    Toast.LENGTH_SHORT).show());

                            Intent intent = new Intent(getApplicationContext(),
                                    A4_ResultScreen.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("Option", "Add");

                            ByteArrayOutputStream bs = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 50, bs);
                            intent.putExtra("byteArray", bs.toByteArray()).putExtra("SNO",
                                    mddiSno).putExtra("CID", mddiCid).
                                    putExtras(bundle).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            bitmap.recycle();
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        } else if (result.addImageStatus == AddImageStatus.DUPLICATE) {
                            errorWhileAdding("Duplicate Image. Image already in the database");
                        } else if (result.addImageStatus == AddImageStatus.ERROR) {
                            errorWhileAdding("Error Image. Not enough features");
                        }
                    }

                    @Override
                    public void onCompleted(List<AddStreamResponse> addStreamResponses,
                                            String elapsedTime, String summaryMessage) {
                    }

                    @Override
                    public void onError(ExceptionType exceptionType, Exception e) {
                        errorWhileAdding(exceptionType + e.getMessage());
                    }
                });
    }

    /**
     * When there is any error while adding, stop the uploading animation
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void errorWhileAdding(String message) {
        runOnUiThread(() -> {
            addingTextview.setVisibility(View.INVISIBLE);
            uploadingAnimation.stop();
            uploadingImageview.setVisibility(View.INVISIBLE);
        });
        cameraview.resumeMddiSearch();
        runOnUiThread(() -> Toast.makeText(A3_CameraScan.this, message, Toast.LENGTH_SHORT).show());
        Intent intent = new Intent(getApplicationContext(), A3_CameraScan.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void saveImage(Bitmap bitmap, File directoryPath, String sno, String fileName) {
        FileOutputStream streamImage = null;
        byte[] imageBytes = getBytesFromBitmap(bitmap);

        File destPath = new File(directoryPath.getPath());
        if (!destPath.exists()) {
            destPath.mkdirs();
        }
        File imageFile = new File(destPath, (fileName + ".jpg"));

        try {
            streamImage = new FileOutputStream(imageFile);
            streamImage.write(imageBytes);
            Log.d("Image", "Image saved successfully" + pictureCount);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Image", "Image saving error" + e.getMessage());
        } finally {
            if (streamImage != null) {
                try {
                    streamImage.close();
                    Log.d("Image", "Image closed");
                } catch (IOException e) {
                    Log.d("Image", "Image not closed");
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean removeDirectory(File inputFolder) {

        boolean[] deleteSubFolders;
        boolean[] deleteFiles = new boolean[0];
        boolean deleteFolder;

        //If the folder exists
        if (!inputFolder.exists()) {
            return false;
        }
        //Get the list of files from the folder
        File[] inputFiles = inputFolder.listFiles();
        if (inputFiles == null) {
            return false;
        }
        deleteSubFolders = new boolean[inputFiles.length];
        for (File file : inputFiles) {
            if (file.isDirectory()) {
                String[] children = file.list();
                assert children != null;
                for (String child : children) {
                    deleteFiles = new boolean[children.length];
                    Arrays.fill(deleteFiles, new File(file, child).delete());
                }
            }
            Arrays.fill(deleteSubFolders, file.delete());
        }
        deleteFolder = inputFolder.delete();
        return deleteFolder && areAllTrue(deleteFiles) && areAllTrue(deleteSubFolders);
    }

    private static boolean areAllTrue(boolean[] array) {
        for (boolean b : array)
            if (!b)
                return false;
        return true;
    }
}
