package com.DyncoApp.ui.modeSelect;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.DyncoApp.databinding.ModeSelectScreenBinding;
import com.DyncoApp.navigation.NavigationService;
import com.DyncoApp.ui.common.MddiMode;

public class ModeSelectScreen extends Fragment {
    private ModeSelectScreenBinding binding;
    private boolean userMode;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = ModeSelectScreenBinding.inflate(inflater, container, false);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavigationService.ModeSelectNav.moveToSelectCollectionView(getView(), userMode, false);
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModeSelectScreenArgs args = ModeSelectScreenArgs.fromBundle(getArguments());
        boolean createCollectionSelected = args.getCreateCollection();
        String mddiCid = args.getMddiCid();
        userMode = args.getUserMode();

        setUpRegisterButton(mddiCid, createCollectionSelected);
        setUpVerifyButton(mddiCid, createCollectionSelected);
        setUpBackButton();

    }

    private void setUpRegisterButton(String mddiCid, boolean createCollectionSelected) {
        binding.registerButton.setOnClickListener(view -> moveToCameraScanScreen(createCollectionSelected, mddiCid, MddiMode.REGISTER));
    }

    private void setUpVerifyButton(String mddiCid, boolean createCollectionSelected) {
        binding.verifyButton.setOnClickListener(view -> moveToCameraScanScreen(createCollectionSelected, mddiCid, MddiMode.VERIFY));
    }

    private void setUpBackButton() {
        binding.backButton.setOnClickListener(view -> NavigationService.ModeSelectNav.moveToSelectCollectionView(getView(), userMode, false));
    }


    private void moveToCameraScanScreen(boolean createCollectionSelected, String mddiCid, MddiMode mddiMode) {
        NavigationService.ModeSelectNav.moveToCameraView(getView(), userMode, createCollectionSelected, mddiCid, mddiMode);
    }
}