package com.DyncoApp.ui.cameraScan;

import static com.DyncoApp.ui.common.Constants.DEFAULT_OVERLAY;
import static com.DyncoApp.ui.common.Constants.DEFAULT_ZOOM_BUTTON_VISIBLE;
import static com.DyncoApp.ui.common.Constants.NEGATIVE_SEARCH_THRESHOLD;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.createResizedBitmap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.DyncoApp.R;
import com.DyncoApp.databinding.CameraScanScreenBinding;
import com.DyncoApp.ui.common.Constants;
import com.DyncoApp.ui.common.MddiMode;
import com.DyncoApp.ui.modeSelect.ModeSelectScreen;
import com.DyncoApp.ui.summary.SummaryScreen;
import com.DyncoApp.ui.verificationFailure.VerificationFailureScreen;

import java.io.ByteArrayOutputStream;
import java.util.Optional;

public class CameraScanScreen extends AppCompatActivity implements View.OnClickListener {
    private CameraScanScreenBinding binding;
    protected AnimationDrawable uploadingAnimation;
    protected boolean flashState;
    protected boolean isZoomButtonVisible = DEFAULT_ZOOM_BUTTON_VISIBLE;
    protected boolean overlayEnabled = DEFAULT_OVERLAY;
    protected Vibrator vibrator;
    private CameraScanModel cameraScanModel;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @SuppressLint("CommitPrefEdits")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = CameraScanScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cameraScanModel = new CameraScanModel(this, Constants.getDefaultEc2ClientService1_1());
        overlayEnabled = cameraScanModel.getOverlayValue();
        initializeLayout();

        MddiMode mddiMode = (MddiMode) Optional.ofNullable(getIntent().
                        getSerializableExtra(getString(R.string.mddi_mode)))
                .orElse(cameraScanModel.getMddiMode());

        boolean createCollectionSelected = getIntent().getBooleanExtra(getString(R.string.create_collection),
                cameraScanModel.getSavedCreateCollectionValue());
        String mddiCid = Optional.ofNullable(getIntent().getStringExtra(getString(R.string.mddi_cid)))
                .orElse(cameraScanModel.getSavedCidText());

        binding.collectionTextView.setText(String.format("Collection name : %s", mddiCid));
        cameraScanModel.initMddi(binding.cameraView, mddiMode, mddiCid);

        if (mddiMode == MddiMode.REGISTER) {
            registerProcess(createCollectionSelected, mddiCid);
        } else {
            verifyProcess();
        }

        cameraScanModel.getExceptionObserver().observe(this, this::handleException);

        flashState = cameraScanModel.getSavedFlash();
        binding.cameraView.changeFlashState(flashState);
        binding.flashButton.setImageResource(flashState ? R.drawable.ic_flash_on : R.drawable
                .ic_flash_off_);
        binding.zoomSeekbar.setProgress(binding.cameraView.getCurrentZoom());
        binding.zoomSeekbar.setMin(1);
        binding.zoomSeekbar.setMax(binding.cameraView.getMaxZoom());
        binding.cameraView.changeZoomLevel(binding.cameraView.getCurrentZoom());
        binding.zoomSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                binding.cameraView.changeZoomLevel(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isZoomButtonVisible = true;
                binding.zoomSeekbar.setProgress(seekBar.getProgress());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isZoomButtonVisible = false;
                binding.zoomSeekbar.setVisibility(View.INVISIBLE);
                binding.zoomSeekbar.setEnabled(false);
                Toast.makeText(CameraScanScreen.this,
                        "Zoom Level is " + seekBar.getProgress() + "/" + seekBar.getMax(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        binding.cameraView.setOnClickListener(v -> binding.cameraView.focusCamera());
    }

    private void verifyProcess() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.verifyTextView.setVisibility(View.VISIBLE);
        binding.progressBar.setMax(NEGATIVE_SEARCH_THRESHOLD);


        cameraScanModel.getGetSampleImageObserver().observe(this, bitmap -> {
            binding.overlayImageView.setVisibility(View.VISIBLE);
            binding.overlayImageView.setAlpha((float) 0.4);
            binding.overlayImageView.setImageBitmap(bitmap);
            if (!overlayEnabled) {
                binding.overlayImageView.setVisibility(View.INVISIBLE);
            }
        });

        cameraScanModel.getVerifyTextObserver().observe(this, text -> binding.verifyTextView.setText(text));

        cameraScanModel.getServerResponseCountObserver().observe(this, binding.progressBar::setProgress);

        cameraScanModel.getPositiveResponseObserver().observe(this, searchResult -> {
            binding.cameraView.stopMddiSearch();
            cameraScanModel.saveFlash(binding.cameraView.isFlashEnabled());
            //Go to the next activity
            Intent intent = new Intent(getApplicationContext(), SummaryScreen.class);
            Bundle bundle = new Bundle();
            bundle.putString(getString(R.string.summary_screen_bundle_key), getString(R.string.summary_screen_bundle_type_search));
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            searchResult.getMddiImage().compress(Bitmap.CompressFormat.JPEG, 10, bs);
            intent.putExtra(getString(R.string.summary_screen_bundle_value_image), bs.toByteArray())
                    .putExtra(getString(R.string.summary_screen_bundle_value_uid), searchResult.getUid())
                    .putExtra(getString(R.string.summary_screen_bundle_value_score), searchResult.getScore())
                    .putExtra(getString(R.string.summary_screen_bundle_value_rating), searchResult.getRating()).
                    putExtras(bundle).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        cameraScanModel.getNegativeThresholdObserver().observe(this, unused -> {
            binding.cameraView.onPause();
            cameraScanModel.saveFlash(binding.cameraView.isFlashEnabled());
            Intent intent = new Intent(getApplicationContext(), VerificationFailureScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void registerProcess(boolean createCollectionSelected, String mddiCid) {
        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setVisibility(View.VISIBLE);
        binding.overlayImageView.setVisibility(View.INVISIBLE);
        binding.overlayButton.setVisibility(View.INVISIBLE);

        registerButton.setOnClickListener(view -> {
            Bitmap addBitmap = cameraScanModel.getCurrentBitmap();
            if (addBitmap == null) {
                Toast.makeText(CameraScanScreen.this, "Wait for some time", Toast.LENGTH_SHORT).show();
                return;
            }
            Bitmap resizedBitmap = createResizedBitmap(addBitmap,
                    cameraScanModel.getClientService().getMddiImageSize().getWidth(),
                    cameraScanModel.getClientService().getMddiImageSize().getHeight(),
                    Bitmap.Config.ARGB_8888);
            addAlertDialog(resizedBitmap, mddiCid, createCollectionSelected);
        });

        cameraScanModel.getAddResponseObserver().observe(this, addResult -> {
            runOnUiThread(() -> Toast.makeText(CameraScanScreen.this, "Added the " +
                            "image",
                    Toast.LENGTH_SHORT).show());
            cameraScanModel.saveFlash(binding.cameraView.isFlashEnabled());
            Intent intent = new Intent(getApplicationContext(),
                    SummaryScreen.class);
            Bundle bundle = new Bundle();
            bundle.putString(getString(R.string.summary_screen_bundle_key), getString(R.string.summary_screen_bundle_type_add));

            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            addResult.getMddiImage().compress(Bitmap.CompressFormat.PNG, 50, bs);
            intent.putExtra(getString(R.string.summary_screen_bundle_value_image), bs.toByteArray())
                    .putExtras(bundle).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            addResult.getMddiImage().recycle();
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }


    /**
     * On click listener for the image buttons
     */
    @SuppressLint("NonConstantResourceId")
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onClick(View v) {
        vibrator.vibrate(VibrationEffect.createOneShot(75, VibrationEffect.DEFAULT_AMPLITUDE));
        switch (v.getId()) {
            case R.id.flashButton:
                binding.flashButton.setImageResource(flashState ? R.drawable.ic_flash_off_ : R.drawable
                        .ic_flash_on);
                binding.cameraView.changeFlashState(!flashState);
                flashState = !flashState;
                break;

            case R.id.zoomButton:
                binding.zoomButton.setImageResource(isZoomButtonVisible ? R.drawable.ic_zoom : R
                        .drawable.ic_baseline_zoom_in_24);
                binding.zoomSeekbar.setVisibility(isZoomButtonVisible ? View.INVISIBLE : View.VISIBLE);
                binding.zoomSeekbar.setEnabled(!isZoomButtonVisible);
                isZoomButtonVisible = !isZoomButtonVisible;
                break;

            case R.id.overlayButton:
                binding.overlayButton.setImageResource(overlayEnabled ? R.drawable.ic_stack_slash :
                        R.drawable.ic_stack);
                binding.overlayImageView.setVisibility(overlayEnabled ? View.INVISIBLE : View.VISIBLE);
                overlayEnabled = !overlayEnabled;
                cameraScanModel.saveOverlayValue(overlayEnabled);
                break;

            case R.id.backButton:
                onBackPressed();
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
        binding.cameraView.checkCameraPermission(requestCode, permissions, grantResults);
    }

    /**
     * When the activity is resumed
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onResume() {
        super.onResume();
        binding.cameraView.onResume();

    }

    /**
     * When the activity is paused
     */
    @Override
    protected void onPause() {
        super.onPause();
        binding.cameraView.onPause();
        cameraScanModel.saveFlash(binding.cameraView.isFlashEnabled());
    }

    /**
     * When press the back button
     */
    @Override
    public void onBackPressed() {
        binding.cameraView.onPause();
        cameraScanModel.saveFlash(binding.cameraView.isFlashEnabled());
        Intent intent = new Intent(getApplicationContext(), ModeSelectScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    /**
     * Initialize the layout
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    protected void initializeLayout() {
        binding.flashButton.setOnClickListener(this);
        binding.zoomButton.setOnClickListener(this);
        binding.overlayButton.setOnClickListener(this);
        binding.backButton.setOnClickListener(this);

        binding.uploadingImageView.setBackgroundResource(R.drawable.upload_anim);
        uploadingAnimation = (AnimationDrawable) binding.uploadingImageView.getBackground();
        binding.uploadingImageView.setVisibility(View.INVISIBLE);
        binding.overlayImageView.setVisibility(View.INVISIBLE);
        binding.addTextView.setVisibility(View.INVISIBLE);
        binding.zoomSeekbar.setVisibility(View.INVISIBLE);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        binding.overlayButton.setImageResource(overlayEnabled ? R.drawable.ic_stack :
                R.drawable.ic_stack_slash);
        binding.overlayImageView.setVisibility(overlayEnabled ? View.VISIBLE : View.INVISIBLE);
    }


    public void addAlertDialog(Bitmap bitmap, String cid, boolean createCollection) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this).setCancelable(false).setTitle("Add the image").
                        setMessage("Do you want to add this image?").setIcon(R.drawable.dynamicelementlogo);

        ImageView showImage = new ImageView(CameraScanScreen.this);
        showImage.setImageBitmap(bitmap);
        builder.setView(showImage);

        builder.setPositiveButton("Yes", (dialog, option) -> {
            runOnUiThread(() -> {
                binding.addTextView.setVisibility(View.VISIBLE);
                uploadingAnimation.start();
                binding.uploadingImageView.setVisibility(View.VISIBLE);
            });
            cameraScanModel.createCollection(bitmap, cid, createCollection);
        });

        builder.setNegativeButton("No", (dialog, option) -> Toast.makeText(getApplicationContext(),
                "The operation has been cancelled",
                Toast.LENGTH_SHORT).show());

        builder.create().show();
    }

    protected void handleException(Exception exception) {
        runOnUiThread(() -> {
            binding.addTextView.setVisibility(View.INVISIBLE);
            uploadingAnimation.stop();
            binding.uploadingImageView.setVisibility(View.INVISIBLE);
        });
//        binding.cameraView.resumeMddiSearch();
        runOnUiThread(() -> Toast.makeText(CameraScanScreen.this, exception.getMessage(), Toast.LENGTH_SHORT).show());
        Intent intent = new Intent(getApplicationContext(), ModeSelectScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
