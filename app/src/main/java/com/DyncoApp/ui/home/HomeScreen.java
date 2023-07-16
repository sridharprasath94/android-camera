package com.DyncoApp.ui.home;

import static com.DyncoApp.ui.common.Constants.VIBRATION_MS;
import static com.DyncoApp.ui.common.Miscellaneous.isInternetAvailable;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.DyncoApp.R;
import com.DyncoApp.databinding.HomeScreenBinding;
import com.DyncoApp.ui.common.Constants;
import com.DyncoApp.ui.secret.SecretScreen;
import com.DyncoApp.ui.selectCollection.SelectCollectionScreen;
import com.dynamicelement.sdk.android.Callback;
import com.dynamicelement.sdk.android.exceptions.ExceptionType;
import com.dynamicelement.sdk.android.ping.PingResult;

import java.util.Arrays;

public class HomeScreen extends AppCompatActivity {
    private HomeScreenBinding binding;
    private AnimationDrawable loadingAnimation;

    private HomeScreenModel homeScreenModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = HomeScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setUpLayoutItems();

        homeScreenModel = new HomeScreenModel(this, Constants.getDefaultEc2ClientService1_1());

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
        binding.userTypeSpinner.setAdapter(new ArrayAdapter<>(getApplicationContext(),
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
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        binding.connectButton.setOnClickListener(v -> {
            if (!isInternetAvailable(this)) {
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                return;
            }
            vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_MS, VibrationEffect.DEFAULT_AMPLITUDE));

            runOnUiThread(() -> {
                loadingAnimation.start();
                binding.loadingHomeScreenView.setVisibility(View.VISIBLE);
            });

            homeScreenModel.checkConnection(new Callback<PingResult>() {
                @Override
                public void onResponse(PingResult response) {
                    String userType = binding.userTypeSpinner.getSelectedItem().toString();
                    vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_MS, VibrationEffect.DEFAULT_AMPLITUDE));
                    stopLoadingAnimation();
                    Intent intent = new Intent(getApplicationContext(), SelectCollectionScreen.class);
                    intent.putExtra(getString(R.string.user_mode), userType.equals(getString(R.string.admin_User)));
                    homeScreenModel.saveData(binding.userTypeSpinner.getSelectedItemPosition());
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }

                @Override
                public void onError(ExceptionType exceptionType, Exception e) {
                    stopLoadingAnimation();
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), e.getMessage(),
                            Toast.LENGTH_SHORT).show());
                }

                private void stopLoadingAnimation() {
                    runOnUiThread(() -> {
                        loadingAnimation.stop();
                        binding.loadingHomeScreenView.setVisibility(View.INVISIBLE);
                    });
                }
            });
        });
    }

    private void setupImageLogoView() {
        binding.logoView.setOnClickListener(v -> homeScreenModel.onLogoPressActionCompleted(() -> {
            FragmentTransaction transaction =  getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_in_left)
                    .replace(R.id.homeContainer, new SecretScreen())
                    .addToBackStack(null);
            transaction.commit();
        }));
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}