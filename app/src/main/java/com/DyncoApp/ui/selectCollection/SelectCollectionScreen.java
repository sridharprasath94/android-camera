package com.DyncoApp.ui.selectCollection;

import static android.content.Context.VIBRATOR_SERVICE;
import static android.os.VibrationEffect.DEFAULT_AMPLITUDE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.DyncoApp.ui.common.Constants.VIBRATION_MS;
import static com.DyncoApp.ui.common.Miscellaneous.isInternetAvailable;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.DyncoApp.R;
import com.DyncoApp.databinding.SelectCollectionScreenBinding;
import com.DyncoApp.navigation.NavigationService;
import com.DyncoApp.ui.common.CompletionCallback;
import com.DyncoApp.ui.common.Constants;
import com.dynamicelement.sdk.android.delete.DeleteResult;
import com.dynamicelement.sdk.android.getsample.GetSampleResult;

public class SelectCollectionScreen extends Fragment {
    private SelectCollectionScreenBinding binding;
    private SelectCollectionModel selectCollectionModel;
    private AnimationDrawable loadingAnimation;
    private boolean adminMode;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = SelectCollectionScreenBinding.inflate(inflater, container, false);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavigationService.SelectCollectionNav.moveToHomeView(getView());
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpLayoutItems();

        loadingAnimation = (AnimationDrawable) binding.loadingView.getBackground();
        selectCollectionModel = new SelectCollectionModel(this.requireContext(), Constants.getDefaultEc2ClientService1_1());

        setUpInputs();
        setUpCidEditText();
        setUpBackButton();
        setUpConnectButton();
    }

    private void setUpInputs() {
        SelectCollectionScreenArgs args = SelectCollectionScreenArgs.fromBundle(getArguments());
        adminMode = args.getUserMode();
        boolean createCollectionSelected = adminMode && args.getCreateCollection();

        binding.createCollectionLayout.setVisibility(adminMode ? VISIBLE : INVISIBLE);
        binding.createCollectionLayout.setEnabled(adminMode);

        binding.cidEditText.setText(selectCollectionModel.getSavedCidText());
        binding.createCollectionSwitch.setChecked(createCollectionSelected);
    }

    private void setUpCidEditText() {
        selectCollectionModel.saveCidOnFinishEditing(binding.cidEditText);
    }

    private void setUpConnectButton() {
        binding.connectButton.setOnClickListener(v -> {
            if (!isInternetAvailable(this.requireContext())) {
                Toast.makeText(requireActivity().getApplicationContext(),
                        getString(R.string.no_internet_connection),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (binding.cidEditText.getText().toString().isEmpty()) {
                Toast.makeText(requireActivity().getApplicationContext(),
                        getString(R.string.enter_the_details),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            requireActivity().runOnUiThread(() -> {
                Vibrator vibrator = (Vibrator) requireActivity().getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_MS, DEFAULT_AMPLITUDE));
                loadingAnimation.start();
                binding.loadingView.setVisibility(VISIBLE);
                binding.connectButton.setEnabled(false);
            });

            selectCollectionModel.checkExistingCid(binding.cidEditText.getText().toString(),
                    binding.createCollectionSwitch.isChecked(),
                    getCompletionCallback(binding.loadingView, loadingAnimation));
        });
    }

    private void setUpBackButton() {
        binding.backButton.setOnClickListener(v -> NavigationService.SelectCollectionNav.moveToHomeView(getView()));
    }

    private void setUpLayoutItems() {
        binding.backButton.setVisibility(VISIBLE);
        binding.connectButton.setEnabled(true);
        binding.cidLayout.setVisibility(VISIBLE);
        binding.createCollectionSwitch.setVisibility(VISIBLE);
        binding.loadingView.setBackgroundResource(R.drawable.animationscan_loading);
        binding.loadingView.setVisibility(INVISIBLE);
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
                requireActivity().runOnUiThread(() -> {
                    loadingAnimation.stop();
                    loadingImageView.setVisibility(INVISIBLE);
                    binding.connectButton.setEnabled(true);
                });
                requireActivity().runOnUiThread(() -> Toast.makeText(requireActivity().getApplicationContext(), e.getMessage(),
                        Toast.LENGTH_SHORT).show());

                NavigationService.SelectCollectionNav.moveToHomeView(getView());
            }


            private void showDeleteAlert(String alertMessage) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SelectCollectionScreen.this.requireContext()).setCancelable(false);
                builder.setIcon(R.drawable.dynamicelementlogo).setTitle(getString(R.string.new_collection)).
                        setMessage(alertMessage);

                builder.setPositiveButton(getString(R.string.text_yes),
                        (dialog, option) -> selectCollectionModel.deleteCollection(binding.cidEditText.getText().toString(),
                                new CompletionCallback<DeleteResult>() {
                                    @Override
                                    public void onSuccess(DeleteResult response) {
                                        moveToNextActivity();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        handleException(e);
                                    }
                                }));

                builder.setNegativeButton(getString(R.string.text_no), (dialog, option) -> {
                    Toast.makeText(requireActivity().getApplicationContext(), getString(R.string.operation_cancel),
                            Toast.LENGTH_SHORT).show();
                    binding.createCollectionSwitch.setChecked(false);
                    requireActivity().runOnUiThread(() -> {
                        loadingAnimation.stop();
                        loadingImageView.setVisibility(INVISIBLE);
                        binding.connectButton.setEnabled(true);
                    });
                    dialog.dismiss();
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        };
    }

    private void moveToNextActivity() {
        Toast.makeText(requireActivity().getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
        requireActivity().runOnUiThread(() -> {
            loadingAnimation.stop();
            binding.loadingView.setVisibility(INVISIBLE);
        });

        NavigationService.SelectCollectionNav.moveToModeSelectView(getView(), adminMode,
                binding.createCollectionSwitch.isChecked(), binding.cidEditText.getText().toString());
    }
}