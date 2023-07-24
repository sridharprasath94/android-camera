package com.DyncoApp.ui.verificationFailure;

import static android.content.Context.VIBRATOR_SERVICE;
import static android.os.VibrationEffect.DEFAULT_AMPLITUDE;
import static com.DyncoApp.ui.common.Constants.VIBRATION_MS;

import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.DyncoApp.databinding.VerificationFailureScreenBinding;
import com.DyncoApp.navigation.NavigationService;

public class VerificationFailureScreen extends Fragment {
    private VerificationFailureScreenBinding binding;
    private VerificationFailureScreenArgs args;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = VerificationFailureScreenBinding.inflate(inflater, container, false);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavigationService.VerificationFailureNav.moveToCameraView(getView(), args.getUserMode(),
                        args.getCreateCollection(), args.getMddiCid(), args.getMddiMode());
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Vibrator vibrator = (Vibrator) requireActivity().getSystemService(VIBRATOR_SERVICE);
        args = VerificationFailureScreenArgs.fromBundle(getArguments());

        binding.tryAgainButton.setOnClickListener(view -> {
            vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_MS, DEFAULT_AMPLITUDE));
            NavigationService.VerificationFailureNav.moveToCameraView(getView(), args.getUserMode(),
                    args.getCreateCollection(), args.getMddiCid(), args.getMddiMode());
        });

        binding.modeSelectButton.setOnClickListener(view -> {
            vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_MS, DEFAULT_AMPLITUDE));
            NavigationService.VerificationFailureNav.moveToModeSelectView(getView(), args.getUserMode(),
                    args.getCreateCollection(), args.getMddiCid());
        });

        binding.goHomeButton.setOnClickListener(view -> {
            vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_MS, DEFAULT_AMPLITUDE));
            NavigationService.VerificationFailureNav.moveToHomeView(getView());
        });

    }

}
