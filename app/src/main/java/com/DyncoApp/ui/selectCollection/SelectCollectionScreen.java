package com.DyncoApp.ui.selectCollection;

import static com.DyncoApp.ui.common.Constants.VIBRATION_MS;
import static com.DyncoApp.ui.common.Miscellaneous.isInternetAvailable;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.DyncoApp.R;
import com.DyncoApp.databinding.SelectCollectionScreenBinding;
import com.DyncoApp.ui.common.CompletionCallback;
import com.DyncoApp.ui.common.Constants;
import com.DyncoApp.ui.home.HomeScreen;
import com.DyncoApp.ui.modeSelect.ModeSelectScreen;
import com.dynamicelement.sdk.android.delete.DeleteResult;
import com.dynamicelement.sdk.android.getsample.GetSampleResult;

public class SelectCollectionScreen extends AppCompatActivity {
    private SelectCollectionScreenBinding binding;
    private SelectCollectionModel selectCollectionModel;
    private AnimationDrawable loadingAnimation;

    @SuppressLint("WrongThread")
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = SelectCollectionScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setUpLayoutItems();

        loadingAnimation = (AnimationDrawable) binding.loadingView.getBackground();
        selectCollectionModel = new SelectCollectionModel(this, Constants.getDefaultEc2ClientService1_1());

        setUpInputs();

        setUpCreateCollectionSwitch();
        setUpCidEditText();
        setUpBackButton();
        setUpConnectButton();
    }

    private void setUpInputs() {
        boolean adminMode = getIntent().getBooleanExtra(getString(R.string.user_mode),
                selectCollectionModel.isAdminUserSelected());
        boolean createCollectionSelected = adminMode && getIntent().getBooleanExtra(getString(R.string.create_collection),
                selectCollectionModel.getSavedCreateCollectionValue());

        binding.createCollectionLayout.setVisibility(adminMode ? View.VISIBLE : View.INVISIBLE);
        binding.createCollectionLayout.setEnabled(adminMode);

        binding.cidEditText.setText(selectCollectionModel.getSavedCidText());
        binding.createCollectionSwitch.setChecked(createCollectionSelected);

        selectCollectionModel.saveUserMode(adminMode);
    }

    private void setUpCidEditText() {
        selectCollectionModel.saveCidOnFinishEditing(binding.cidEditText);
    }

    private void setUpCreateCollectionSwitch() {
        selectCollectionModel.saveCreateCollectionOnFinishChanging(binding.createCollectionSwitch);
    }

    private void setUpConnectButton() {
        binding.connectButton.setOnClickListener(v -> {
            if (!isInternetAvailable(this)) {
                Toast.makeText(getApplicationContext(), "No Internet Connection",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (binding.cidEditText.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Enter the details and proceed...",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            runOnUiThread(() -> {
                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_MS, VibrationEffect.DEFAULT_AMPLITUDE));
                loadingAnimation.start();
                binding.loadingView.setVisibility(View.VISIBLE);
            });

            selectCollectionModel.checkExistingCid(binding.cidEditText.getText().toString(),
                    binding.createCollectionSwitch.isChecked(),
                    getCompletionCallback(binding.loadingView, loadingAnimation));
        });
    }

    private void setUpBackButton() {
        binding.backButton.setOnClickListener(v -> onBackPressed());
    }

    private void setUpLayoutItems() {
        binding.backButton.setVisibility(View.VISIBLE);
        binding.connectButton.setEnabled(true);
        binding.cidLayout.setVisibility(View.VISIBLE);
        binding.createCollectionSwitch.setVisibility(View.VISIBLE);
        binding.loadingView.setBackgroundResource(R.drawable.animationscan_loading);
        binding.loadingView.setVisibility(View.INVISIBLE);
    }


    @NonNull
    private CompletionCallback<GetSampleResult> getCompletionCallback(ImageView loadingImageView, AnimationDrawable loadingAnimation) {
        return new CompletionCallback<GetSampleResult>() {
            @Override
            public void onSuccess(GetSampleResult response) {
                moveToNextActivity();
            }

            @Override
            public void onError(Exception e) {
                handleException(e);
            }

            @Override
            public void showAlert(String alertMessage) {
                showDeleteAlert(alertMessage);
            }

            private void handleException(Exception e) {
                runOnUiThread(() -> {
                    loadingAnimation.stop();
                    loadingImageView.setVisibility(View.INVISIBLE);
                });
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), e.getMessage(),
                        Toast.LENGTH_SHORT).show());

                Intent intent = new Intent(getApplicationContext(), SelectCollectionScreen.class);
                intent.putExtra(getString(R.string.create_collection), binding.createCollectionSwitch.isChecked());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }


            private void showDeleteAlert(String alertMessage) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SelectCollectionScreen.this).setCancelable(false);
                builder.setIcon(R.drawable.dynamicelementlogo).setTitle("New Collection").
                        setMessage(alertMessage);

                builder.setPositiveButton("Yes",
                        (dialog, option) -> selectCollectionModel.deleteCollection(binding.cidEditText.getText().toString(), new CompletionCallback<DeleteResult>() {
                            @Override
                            public void onSuccess(DeleteResult response) {
                                moveToNextActivity();
                            }

                            @Override
                            public void onError(Exception e) {
                                handleException(e);
                            }
                        }));

                builder.setNegativeButton("No", (dialog, option) -> {
                    Toast.makeText(getApplicationContext(), "The operation has been cancelled",
                            Toast.LENGTH_SHORT).show();
                    binding.createCollectionSwitch.setChecked(false);
                    runOnUiThread(() -> {
                        loadingAnimation.stop();
                        loadingImageView.setVisibility(View.INVISIBLE);
                    });
                    dialog.dismiss();
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        };
    }

    private void moveToNextActivity() {
        Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), ModeSelectScreen.class);
        intent.putExtra(getString(R.string.create_collection), binding.createCollectionSwitch.isChecked());
        binding.createCollectionSwitch.setChecked(false);
        intent.putExtra(getString(R.string.mddi_cid), binding.cidEditText.getText().toString());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), HomeScreen.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}