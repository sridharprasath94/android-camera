package com.mddicamera.ui.cameraScan;

import static android.content.Context.VIBRATOR_SERVICE;
import static android.os.VibrationEffect.DEFAULT_AMPLITUDE;
import static android.util.Base64.DEFAULT;
import static com.mddicamera.ui.common.Constants.DEFAULT_OVERLAY;
import static com.mddicamera.ui.common.Constants.DEFAULT_ZOOM_BUTTON_VISIBLE;
import static com.mddicamera.ui.common.Constants.NEGATIVE_SEARCH_THRESHOLD;
import static com.mddicamera.ui.common.Constants.VIBRATION_MS;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.createResizedBitmap;

import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.mddicamera.R;
import com.mddicamera.databinding.CameraScanScreenBinding;
import com.mddicamera.ui.common.Constants;
import com.mddicamera.ui.common.MddiMode;
import com.mddicamera.ui.common.SummaryViewArguments;

import java.io.ByteArrayOutputStream;

public class CameraScanScreen extends Fragment {
    private CameraScanScreenBinding binding;
    protected AnimationDrawable uploadingAnimation;
    protected boolean flashState;
    protected boolean isZoomButtonVisible = DEFAULT_ZOOM_BUTTON_VISIBLE;
    protected boolean overlayEnabled = DEFAULT_OVERLAY;
    protected Vibrator vibrator;
    private CameraScanModel cameraScanModel;
    private boolean createCollectionSelected;
    private boolean userMode;
    private String mddiCid;
    private MddiMode mddiMode;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = CameraScanScreenBinding.inflate(inflater, container, false);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().finishAffinity();
                requireActivity().finish();
              //  requireActivity().onBackPressed();
//                NavigationService.CameraNav.moveToModeSelectView(getView(), userMode, createCollectionSelected, mddiCid);
            }
        });
        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraScanModel = new CameraScanModel(this.getContext(), Constants.getDefaultEc2ClientService1_1());
        overlayEnabled = cameraScanModel.getOverlayValue();
        initializeLayout();

//        CameraScanScreenArgs args = CameraScanScreenArgs.fromBundle(getArguments());

//        createCollectionSelected = false;
//        mddiCid = args.getMddiCid();
//        mddiMode = args.getMddiMode();
//        userMode = args.getUserMode();

        createCollectionSelected = false;
        mddiCid = "sridhar01";
        mddiMode = MddiMode.REGISTER;
        userMode = true;

        cameraScanModel.initMddi(binding.cameraView, mddiMode, mddiCid);

        if (mddiMode == MddiMode.REGISTER) {
            registerProcess(createCollectionSelected, mddiCid);
        } else {
            verifyProcess();
        }

        cameraScanModel.getExceptionObserver().observe(this.requireActivity(), this::handleException);

        flashState = cameraScanModel.getSavedFlash();
        binding.cameraView.changeFlashState(flashState);
        binding.cameraView.changeZoomLevel(binding.cameraView.getCurrentZoom());


        binding.cameraView.setOnClickListener(v -> binding.cameraView.focusCamera());


    }

    private void verifyProcess() {
        cameraScanModel.getGetSampleImageObserver().observe(this.requireActivity(), bitmap -> {
            binding.overlayImageView.setVisibility(View.VISIBLE);
            binding.overlayImageView.setAlpha((float) 0.4);
            binding.overlayImageView.setImageBitmap(bitmap);
            if (!overlayEnabled) {
                binding.overlayImageView.setVisibility(View.INVISIBLE);
            }
        });


        cameraScanModel.getPositiveResponseObserver().observe(this.requireActivity(),
                searchResult -> {
                    binding.cameraView.stopMddiSearch();
                    cameraScanModel.saveFlash(binding.cameraView.isFlashEnabled());

                    ByteArrayOutputStream bs = new ByteArrayOutputStream();
                    searchResult.getMddiImage().compress(Bitmap.CompressFormat.JPEG, 10, bs);
                    String base64String = Base64.encodeToString(bs.toByteArray(), DEFAULT);

                    SummaryViewArguments arguments = new SummaryViewArguments();
                    arguments.setCreateCollection(createCollectionSelected);
                    arguments.setUserMode(userMode);
                    arguments.setMddiMode(mddiMode.name());
                    arguments.setMddiBase64Image(base64String);
                    arguments.setMddiCid(searchResult.getCid());
                    arguments.setMddiRating(searchResult.getRating());
                    arguments.setMddiScore(searchResult.getScore());
                    arguments.setMddiUid(searchResult.getUid());

//                    NavigationService.CameraNav.moveToSummaryView(getView(), arguments);
                });

        cameraScanModel.getNegativeThresholdObserver().observe(this.requireActivity(), unused -> {
            binding.cameraView.onPause();
            cameraScanModel.saveFlash(binding.cameraView.isFlashEnabled());
            //binding.progressBar.setProgress(0);
//            NavigationService.CameraNav.moveToVerificationFailureView(getView(), userMode,
//                    createCollectionSelected, mddiCid, mddiMode);
        });
    }

    private void registerProcess(boolean createCollectionSelected, String mddiCid) {
//        binding.registerButton.setVisibility(View.VISIBLE);
        binding.overlayImageView.setVisibility(View.INVISIBLE);
//
//        binding.registerButton.setOnClickListener(view -> {
//            Bitmap addBitmap = cameraScanModel.getCurrentBitmap();
//            if (addBitmap == null) {
//                Toast.makeText(CameraScanScreen.this.requireActivity(),
//                        getString(R.string.wait_for_some_time), Toast.LENGTH_SHORT).show();
//                return;
//            }
//            Bitmap resizedBitmap = createResizedBitmap(addBitmap,
//                    cameraScanModel.getClientService().getMddiImageSize().getWidth(),
//                    cameraScanModel.getClientService().getMddiImageSize().getHeight(),
//                    Bitmap.Config.ARGB_8888);
//            addAlertDialog(resizedBitmap, mddiCid, createCollectionSelected);
//        });

        cameraScanModel.getAddResponseObserver().observe(this.requireActivity(), addResult -> {
            requireActivity().runOnUiThread(() -> Toast.makeText(CameraScanScreen.this.requireActivity(),
                    getString(R.string.summary_screen_successful_add),
                    Toast.LENGTH_SHORT).show());
            cameraScanModel.saveFlash(binding.cameraView.isFlashEnabled());

            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            addResult.getMddiImage().compress(Bitmap.CompressFormat.JPEG, 10, bs);
            String base64String = Base64.encodeToString(bs.toByteArray(), DEFAULT);

            SummaryViewArguments arguments = new SummaryViewArguments();
            arguments.setCreateCollection(createCollectionSelected);
            arguments.setUserMode(userMode);
            arguments.setMddiMode(mddiMode.name());
            arguments.setMddiBase64Image(base64String);
            arguments.setMddiCid(mddiCid);

//            NavigationService.CameraNav.moveToSummaryView(getView(), arguments);
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onResume() {
        super.onResume();
        binding.cameraView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.cameraView.onPause();
        cameraScanModel.saveFlash(binding.cameraView.isFlashEnabled());
    }

    /**
     * Initialize the layout
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    protected void initializeLayout() {
        binding.overlayImageView.setVisibility(View.INVISIBLE);
        vibrator = (Vibrator) requireActivity().getSystemService(VIBRATOR_SERVICE);
        binding.overlayImageView.setVisibility(overlayEnabled ? View.VISIBLE : View.INVISIBLE);
    }


    public void addAlertDialog(Bitmap bitmap, String cid, boolean createCollection) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this.requireActivity()).setCancelable(false)
                        .setTitle(getString(R.string.camera_scan_add_alert_title))
                        .setMessage(getString(R.string.camera_scan_add_alert_message)).
                        setIcon(R.drawable.dynamicelementlogo);

        ImageView showImage = new ImageView(CameraScanScreen.this.requireActivity());
        showImage.setImageBitmap(bitmap);
        builder.setView(showImage);

        builder.setPositiveButton(getString(R.string.text_yes), (dialog, option) -> {
//            requireActivity().runOnUiThread(() -> {
//                binding.addTextView.setVisibility(View.VISIBLE);
//                uploadingAnimation.start();
//                binding.uploadingImageView.setVisibility(View.VISIBLE);
//            });
            cameraScanModel.createCollection(bitmap, cid, createCollection);
        });

        builder.setNegativeButton(getString(R.string.text_no), (dialog, option) ->
                Toast.makeText(requireActivity().getApplicationContext(),
                        getString(R.string.operation_cancel),
                        Toast.LENGTH_SHORT).show());

        builder.create().show();
    }

    protected void handleException(Exception exception) {
        requireActivity().runOnUiThread(() -> {
//            binding.addTextView.setVisibility(View.INVISIBLE);
            uploadingAnimation.stop();
//            binding.uploadingImageView.setVisibility(View.INVISIBLE);
            Toast.makeText(CameraScanScreen.this.requireActivity(),
                    exception.getMessage(), Toast.LENGTH_SHORT).show();
        });
//        NavigationService.CameraNav.moveToModeSelectView(Objects.requireNonNull(getView()), userMode,
//                createCollectionSelected, mddiCid);
    }
}
