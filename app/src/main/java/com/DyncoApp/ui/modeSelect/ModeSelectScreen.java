package com.DyncoApp.ui.modeSelect;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.DyncoApp.R;
import com.DyncoApp.databinding.ModeSelectScreenBinding;
import com.DyncoApp.ui.cameraScan.CameraScanScreen;
import com.DyncoApp.ui.common.MddiMode;
import com.DyncoApp.ui.selectCollection.SelectCollectionScreen;

import java.util.Optional;

public class ModeSelectScreen extends AppCompatActivity {
    private ModeSelectScreenBinding binding;
    private ModeSelectModel modeSelectModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ModeSelectScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        modeSelectModel = new ModeSelectModel(this);


        boolean createCollectionSelected = getIntent().getBooleanExtra(getString(R.string.create_collection),
                modeSelectModel.getSavedCreateCollectionValue());
        String mddiCid = Optional.ofNullable(getIntent().getStringExtra(getString(R.string.mddi_cid)))
                .orElse(modeSelectModel.getSavedCidText());

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
        binding.backButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), SelectCollectionScreen.class);
            intent.putExtra(getString(R.string.create_collection), false);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    private void moveToCameraScanScreen(boolean createCollectionSelected, String mddiCid, MddiMode mddiMode) {
        Intent intent = new Intent(getApplicationContext(), CameraScanScreen.class);
        intent.putExtra(getString(R.string.create_collection), createCollectionSelected)
                .putExtra(getString(R.string.mddi_cid), mddiCid)
                .putExtra(getString(R.string.mddi_mode), mddiMode);
        modeSelectModel.saveData(mddiCid, createCollectionSelected);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

}