package com.DyncoApp.ui.secret;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.DyncoApp.R;
import com.DyncoApp.databinding.SecretScreenBinding;

public class SecretScreen extends Fragment {
    private SecretScreenBinding binding;
    private SecretModel secretModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = SecretScreenBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        secretModel = new SecretModel(requireContext());

        binding.versionDescTextview.setText(getResources().getString(R.string.versionId));
        binding.modelDescTextView.setText(getAndroidVersion());
        binding.deviceDescTextView.setText(Build.BRAND + " " + Build.MODEL);
        binding.flashToggleView.setChecked(secretModel.getSavedToggleFlash());
        binding.scoreToggleView.setChecked(secretModel.getSavedShowScore());
        binding.flashToggleView.setOnCheckedChangeListener((buttonView, isChecked) -> secretModel.saveToggleFlash(isChecked));
        binding.scoreToggleView.setOnCheckedChangeListener((buttonView, isChecked) -> secretModel.saveShowScore(isChecked));

        binding.backButton.setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_secretScreen_to_homeScreen));
    }

    private String getAndroidVersion() {
        String release = Build.VERSION.RELEASE;
        int sdkVersion = Build.VERSION.SDK_INT;
        return "Android SDK: " + sdkVersion + " (" + release + ")";
    }

    @Override
    public void onPause() {
        super.onPause();
        secretModel.saveShowScore(binding.scoreToggleView.isChecked());
        secretModel.saveToggleFlash(binding.flashToggleView.isChecked());
    }

    protected void showFullScreen() {
        requireActivity().getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void onResume() {
        super.onResume();
        secretModel.saveShowScore(binding.scoreToggleView.isChecked());
        secretModel.saveToggleFlash(binding.flashToggleView.isChecked());
        showFullScreen();
    }
}
