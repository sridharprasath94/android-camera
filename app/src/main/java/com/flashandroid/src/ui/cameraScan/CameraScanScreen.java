package com.flashandroid.src.ui.cameraScan;

import static android.content.Context.VIBRATOR_SERVICE;
import static com.flashandroid.src.ui.common.Constants.DEFAULT_ZOOM_BUTTON_VISIBLE;

import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.flashandroid.databinding.CameraScanScreenBinding;


public class CameraScanScreen extends Fragment {
    private CameraScanScreenBinding binding;
    protected AnimationDrawable uploadingAnimation;
    protected boolean flashState;
    protected boolean isZoomButtonVisible = DEFAULT_ZOOM_BUTTON_VISIBLE;
    protected Vibrator vibrator;
    private CameraScanModel cameraScanModel;

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
        cameraScanModel = new CameraScanModel(this.getContext());
        initializeLayout();

        cameraScanModel.initMddi(binding.cameraView);
        cameraScanModel.getExceptionObserver().observe(this.requireActivity(), this::handleException);

        flashState = cameraScanModel.getSavedFlash();
        binding.cameraView.changeFlashState(flashState);
        binding.cameraView.changeZoomLevel(binding.cameraView.getCurrentZoom());
        binding.cameraView.setOnClickListener(v -> binding.cameraView.focusCamera());


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
        vibrator = (Vibrator) requireActivity().getSystemService(VIBRATOR_SERVICE);
    }


    protected void handleException(Exception exception) {
        requireActivity().runOnUiThread(() -> {
            uploadingAnimation.stop();
            Toast.makeText(CameraScanScreen.this.requireActivity(),
                    exception.getMessage(), Toast.LENGTH_SHORT).show();
        });
//        NavigationService.CameraNav.moveToModeSelectView(Objects.requireNonNull(getView()), userMode,
//                createCollectionSelected, mddiCid);
    }
}
