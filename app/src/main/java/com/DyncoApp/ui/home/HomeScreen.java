package com.DyncoApp.ui.home;

import static android.content.Context.VIBRATOR_SERVICE;
import static com.DyncoApp.ui.common.Constants.VIBRATION_MS;
import static com.DyncoApp.ui.common.Miscellaneous.isInternetAvailable;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.DyncoApp.R;
import com.DyncoApp.databinding.HomeScreenBinding;
import com.DyncoApp.navigation.NavigationService;
import com.DyncoApp.ui.common.Constants;
import com.dynamicelement.sdk.android.Callback;
import com.dynamicelement.sdk.android.exceptions.ExceptionType;
import com.dynamicelement.sdk.android.ping.PingResult;

import java.util.Arrays;

public class HomeScreen extends Fragment {
    private HomeScreenBinding binding;
    private AnimationDrawable loadingAnimation;

    private HomeScreenModel homeScreenModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = HomeScreenBinding.inflate(inflater, container, false);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                requireActivity().finish();
                requireActivity().finishAffinity();
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpLayoutItems();
        homeScreenModel = new HomeScreenModel(this.requireContext(), Constants.getDefaultEc2ClientService1_1());
        setupUserTypeSpinner();
        setupConnectButton();
        setupImageLogoView();
    }

    private void setUpLayoutItems() {
        binding.loadingHomeScreenView.setBackgroundResource(R.drawable.animationscan_loading);
        loadingAnimation = (AnimationDrawable) binding.loadingHomeScreenView.getBackground();

        binding.connectButton.setEnabled(true);
        binding.userIdTextView.setEnabled(true);
        binding.loadingHomeScreenView.setVisibility(View.INVISIBLE);
    }

    private void setupUserTypeSpinner() {
        binding.userTypeSpinner.setAdapter(new ArrayAdapter<>(requireActivity().getApplicationContext(),
                R.layout.spinner_item_user, Arrays.asList(
                getString(R.string.admin_User),
                getString(R.string.test_user)
        )));

        binding.userTypeSpinner.setSelection(homeScreenModel.getSpinnerPosition());
        binding.userIdTextView.setText(binding.userTypeSpinner.getSelectedItem().toString());

        binding.userTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String userType = parent.getItemAtPosition(position).toString();
                binding.userIdTextView.setText(userType);
                homeScreenModel.saveData(binding.userTypeSpinner.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupConnectButton() {
        Vibrator vibrator = (Vibrator) requireActivity().getSystemService(VIBRATOR_SERVICE);

        binding.connectButton.setOnClickListener(v -> {
            if (!isInternetAvailable(this.requireContext())) {
                Toast.makeText(this.requireContext(), getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                return;
            }
            vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_MS, VibrationEffect.DEFAULT_AMPLITUDE));

            startLoading();

            homeScreenModel.checkConnection(new Callback<PingResult>() {
                @Override
                public void onResponse(PingResult response) {
                    String userType = binding.userTypeSpinner.getSelectedItem().toString();
                    vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_MS, VibrationEffect.DEFAULT_AMPLITUDE));
                    stopLoading();
                    homeScreenModel.saveData(binding.userTypeSpinner.getSelectedItemPosition());
                    NavigationService.HomeNav.moveToSelectCollectionView(getView(), userType.equals(getString(R.string.admin_User)), false);
                }

                @Override
                public void onError(ExceptionType exceptionType, Exception e) {
                    stopLoading();
                    requireActivity().runOnUiThread(() -> Toast.makeText(requireActivity().getApplicationContext(), e.getMessage(),
                            Toast.LENGTH_SHORT).show());
                }

            });
        });
    }


    private void startLoading() {
        requireActivity().runOnUiThread(() -> {
            loadingAnimation.start();
            binding.loadingHomeScreenView.setVisibility(View.VISIBLE);
            binding.connectButton.setEnabled(false);
        });
    }

    private void stopLoading() {
        requireActivity().runOnUiThread(() -> {
            loadingAnimation.stop();
            binding.loadingHomeScreenView.setVisibility(View.INVISIBLE);
            binding.connectButton.setEnabled(true);
        });
    }


    private void setupImageLogoView() {
        binding.logoView.setOnClickListener(v -> homeScreenModel.onLogoPressActionCompleted(() -> NavigationService.HomeNav.moveToSecretView(getView())));
    }
}