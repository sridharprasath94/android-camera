package com.DyncoApp.ui.verificationFailure;

import static com.DyncoApp.ui.common.Constants.VIBRATION_MS;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.DyncoApp.R;
import com.DyncoApp.databinding.VerificationFailureScreenBinding;
import com.DyncoApp.ui.cameraScan.CameraScanScreen;
import com.DyncoApp.ui.common.MddiMode;
import com.DyncoApp.ui.home.HomeScreen;
import com.DyncoApp.ui.modeSelect.ModeSelectScreen;

public class VerificationFailureScreen extends AppCompatActivity {


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        com.DyncoApp.databinding.VerificationFailureScreenBinding binding = VerificationFailureScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        binding.tryAgainButton.setOnClickListener(view -> {
            vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_MS, VibrationEffect.DEFAULT_AMPLITUDE));
            Intent tryAgainIntent = new Intent(getApplicationContext(), CameraScanScreen.class);
            tryAgainIntent.putExtra(getString(R.string.mddi_mode), MddiMode.VERIFY);
            startActivity(tryAgainIntent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        binding.modeSelectButton.setOnClickListener(view -> {
            vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_MS, VibrationEffect.DEFAULT_AMPLITUDE));
            Intent modeSelectIntent = new Intent(getApplicationContext(), ModeSelectScreen.class);
            startActivity(modeSelectIntent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        binding.goHomeButton.setOnClickListener(view -> {
            vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_MS, VibrationEffect.DEFAULT_AMPLITUDE));
            Intent goHomeIntent = new Intent(getApplicationContext(), HomeScreen.class);
            goHomeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(goHomeIntent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

    }
}
