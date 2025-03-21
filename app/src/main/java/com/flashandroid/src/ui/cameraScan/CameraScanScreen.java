package com.flashandroid.src.ui.cameraScan;

import static com.flashandroid.src.ui.common.Constants.DEFAULT_ZOOM_BUTTON_VISIBLE;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.flashandroid.R;
import com.flashandroid.databinding.CameraScanScreenBinding;
import com.flashandroid.src.ui.common.VibrationModel;


public class CameraScanScreen extends Fragment {
    private CameraScanScreenBinding binding;
    protected boolean flashState;
    protected boolean isZoomButtonVisible = DEFAULT_ZOOM_BUTTON_VISIBLE;
    private VibrationModel vibrationModel;
    private CameraScanModel cameraScanModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = CameraScanScreenBinding.inflate(inflater, container, false);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().finishAffinity();
                requireActivity().finish();
            }
        });
        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraScanModel = new CameraScanModel(this.getContext());
        this.vibrationModel = new VibrationModel(requireActivity());
        initializeLayout();

        cameraScanModel.initCamera(binding.cameraView);
        cameraScanModel.getStreamBitmapObserver().observe(this.requireActivity(), this::handleStreamBitmap);
        cameraScanModel.getBarcodeResultObserver().observe(this.requireActivity(), this::handleBarcodeResult);
        cameraScanModel.getExceptionObserver().observe(this.requireActivity(), this::handleException);

        flashState = cameraScanModel.getSavedFlash();
        binding.cameraView.changeFlashState(flashState);
        binding.flashButton.setImageResource(flashState ? R.drawable.ic_flash_on : R.drawable
                .ic_flash_off);
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
                Toast.makeText(requireActivity(),
                        getString(R.string.camera_scan_zoom_level) +
                                " " + seekBar.getProgress() + "/" + seekBar.getMax(), Toast.LENGTH_SHORT).show();
            }
        });

        this.binding.cameraView.setOnClickListener(v -> binding.cameraView.focusCamera());
        this.binding.cameraView.setOnLongClickListener(v -> {
            requireActivity().runOnUiThread(() -> {
                binding.currentCaptureImage.setImageBitmap(binding.cameraView.captureCurrentImage());
                new Handler(Looper.getMainLooper()).postDelayed(() ->
                        binding.currentCaptureImage.setImageBitmap(null), 2000);
            });
            return true;
        });

        this.binding.flashButton.setOnClickListener(view14 -> {
            this.vibrationModel.createVibration();
            this.binding.flashButton.setImageResource(flashState ? R.drawable.ic_flash_off : R.drawable
                    .ic_flash_on);
            this.binding.cameraView.changeFlashState(!flashState);
            this.flashState = !flashState;
        });

        binding.zoomButton.setOnClickListener(view12 -> {
            this.vibrationModel.createVibration();
            this.binding.zoomButton.setImageResource(isZoomButtonVisible ? R.drawable.ic_zoom : R
                    .drawable.ic_baseline_zoom_in_24);
            this.binding.zoomSeekbar.setVisibility(isZoomButtonVisible ? View.INVISIBLE : View.VISIBLE);
            this.binding.zoomSeekbar.setEnabled(!isZoomButtonVisible);
            this.isZoomButtonVisible = !isZoomButtonVisible;
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
        binding.zoomSeekbar.setVisibility(View.INVISIBLE);
    }


    protected void handleException(Exception exception) {
        Log.d("CAMERA_SCAN_SCREEN", "Exception" + exception);
        requireActivity().runOnUiThread(() -> Toast.makeText(CameraScanScreen.this.requireActivity(),
                exception.getMessage(), Toast.LENGTH_SHORT).show());
    }

    protected void handleStreamBitmap(Bitmap bitmap) {
        Log.d("CAMERA_SCAN_SCREEN", "Bitmap " + bitmap);
        requireActivity().runOnUiThread(() -> binding.previewImage.setImageBitmap(bitmap));
    }

    protected void handleBarcodeResult(String barcodeResult) {
        Log.d("CAMERA_SCAN_SCREEN", "Barcode: " + barcodeResult);
        requireActivity().runOnUiThread(() -> binding.barcodeText.setText(barcodeResult != null ? barcodeResult : ""));
    }
}
