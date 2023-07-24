package com.DyncoApp.ui.summary;

import static android.content.Context.VIBRATOR_SERVICE;
import static android.os.VibrationEffect.DEFAULT_AMPLITUDE;
import static android.util.Base64.DEFAULT;
import static com.DyncoApp.ui.common.Constants.VIBRATION_MS;
import static com.DyncoApp.ui.common.MddiMode.REGISTER;
import static com.DyncoApp.ui.common.MddiMode.VERIFY;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.DyncoApp.R;
import com.DyncoApp.databinding.SummaryScreenBinding;
import com.DyncoApp.navigation.NavigationService;
import com.DyncoApp.ui.common.MddiMode;
import com.DyncoApp.ui.common.SummaryViewArguments;

import java.util.Objects;

public class SummaryScreen extends Fragment {
    private SummaryScreenBinding binding;
    private SummaryViewArguments customArguments;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = SummaryScreenBinding.inflate(inflater, container, false);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavigationService.SummaryNav.moveToCameraView(getView(), customArguments.isUserMode(),
                        customArguments.isCreateCollection(), customArguments.getMddiCid(), MddiMode.valueOf(customArguments.getMddiMode()));
            }
        });
        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Vibrator vibrator = (Vibrator) requireActivity().getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_MS, DEFAULT_AMPLITUDE));

        SummaryModel model = new SummaryModel(this.requireContext());
        SummaryScreenArgs args = SummaryScreenArgs.fromBundle(getArguments());
        customArguments = args.getSummaryArg();

        if (Objects.equals(customArguments.getMddiMode(), VERIFY.name())) {
            setImageViewWithBitmap(Base64.decode(customArguments.getMddiBase64Image(), DEFAULT), true);
            binding.verifyAgainButton.setText(getString(R.string.summary_screen_verify_again));
            binding.descriptionTextView.setText(customArguments.getMddiUid());
            if (model.getSavedShowScore()) {
                binding.scoreTextView.setText(String.format(getString(R.string.summary_view_score) + " = %s", customArguments.getMddiScore()));
                binding.ratingBar.setRating(customArguments.getMddiRating());
            } else {
                binding.scoreTextView.setVisibility(View.INVISIBLE);
                binding.ratingBar.setVisibility(View.INVISIBLE);
            }
        } else {
            setImageViewWithBitmap(Base64.decode(customArguments.getMddiBase64Image(), DEFAULT), false);
            binding.verifyAgainButton.setText(getString(R.string.summary_screen_verify));
            binding.scoreTextView.setEnabled(false);
            binding.descriptionTextView.setEnabled(true);
            binding.ratingBar.setEnabled(false);
            binding.scoreTextView.setVisibility(View.INVISIBLE);
            binding.descriptionTextView.setVisibility(View.VISIBLE);
            binding.ratingBar.setVisibility(View.INVISIBLE);
            binding.descriptionTextView.setTextSize(28);
            binding.descriptionTextView.setText(R.string.summary_screen_successful_add);
        }

        binding.verifyAgainButton.setOnClickListener(view -> {
            vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_MS, DEFAULT_AMPLITUDE));
            NavigationService.SummaryNav.moveToCameraView(getView(),
                    customArguments.isUserMode(), customArguments.isCreateCollection(),
                    customArguments.getMddiCid(), VERIFY);
        });

        binding.registerNewButton.setOnClickListener(view -> {
            vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_MS, DEFAULT_AMPLITUDE));
            NavigationService.SummaryNav.moveToCameraView(getView(), customArguments.isUserMode(), customArguments.isCreateCollection(),
                    customArguments.getMddiCid(), REGISTER);
        });

        binding.goHomeButton.setOnClickListener(view -> {
            vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_MS, DEFAULT_AMPLITUDE));
            NavigationService.SummaryNav.moveToHomeView(getView());
        });

    }

    private void setImageViewWithBitmap(byte[] byteArray, boolean waterMarkNeeded) {
        if (byteArray != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            Bitmap watermarkedBitmap = bitmap;
            if (waterMarkNeeded) {
                watermarkedBitmap = waterMarkBitmap(bitmap, getString(R.string.summary_view_verified));
            }
            Bitmap finalWatermarkedBitmap = watermarkedBitmap;
            requireActivity().runOnUiThread(() -> binding.imageView.setImageBitmap(finalWatermarkedBitmap));
        }
    }

    private static Bitmap waterMarkBitmap(Bitmap original, String waterMarkText) {
        Bitmap src = original.copy(Bitmap.Config.ARGB_8888, true);
        Bitmap result = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src, 0, 0, null);
        Paint paint = new Paint();
        Paint.FontMetrics fm = new Paint.FontMetrics();
        paint.setColor(Color.BLACK);
        paint.setTextSize(120);
        paint.getFontMetrics(fm);
        float height = paint.measureText("yY");
        int left = 0;
        int top = (int) (src.getHeight() / 2 - height / 2);
        int right = src.getWidth();
        int bottom = (int) (src.getHeight() / 2 + height / 2);
        canvas.drawRect(left, top, right, bottom, paint);
        paint.setColor(Color.RED);
        int x = (int) (src.getWidth() / 2 - paint.measureText(waterMarkText) / 2);
        int y = (int) ((top + bottom) / 2 + height / 3);
        canvas.drawText(waterMarkText, x, y, paint);
        return result;
    }
}
