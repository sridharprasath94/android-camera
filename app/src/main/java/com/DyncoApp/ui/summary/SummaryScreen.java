package com.DyncoApp.ui.summary;

import static com.DyncoApp.ui.common.Constants.VIBRATION_MS;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.DyncoApp.R;
import com.DyncoApp.databinding.SummaryScreenBinding;
import com.DyncoApp.ui.cameraScan.CameraScanScreen;
import com.DyncoApp.ui.common.MddiMode;
import com.DyncoApp.ui.home.HomeScreen;

import java.util.Objects;

public class SummaryScreen extends AppCompatActivity {
    private SummaryScreenBinding binding;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = SummaryScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_MS, VibrationEffect.DEFAULT_AMPLITUDE));

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        assert bundle != null;
        String option = bundle.getString(getString(R.string.summary_screen_bundle_key), "");

        setImageViewWithBitmap(intent.getByteArrayExtra(getString(R.string.summary_screen_bundle_value_image)));
        SummaryModel model = new SummaryModel(this);

        if (Objects.equals(option, getString(R.string.summary_screen_bundle_type_search))) {
            binding.verifyAgainButton.setText(getString(R.string.summary_screen_verify_again));
            binding.descriptionTextView.setText(intent.getStringExtra(getString(R.string.summary_screen_bundle_value_uid)));
            if (model.getSavedShowScore()) {
                binding.scoreTextView.setText(String.format("Score = %s", intent.getFloatExtra(getString(R.string.summary_screen_bundle_value_score), 0)));
                binding.ratingBar.setRating(intent.getIntExtra(getString(R.string.summary_screen_bundle_value_rating), 0));
            } else {
                binding.scoreTextView.setVisibility(View.INVISIBLE);
                binding.ratingBar.setVisibility(View.INVISIBLE);
            }
        } else {
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
            vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_MS, VibrationEffect.DEFAULT_AMPLITUDE));
            Intent verifyAgainIntent = new Intent(getApplicationContext(), CameraScanScreen.class);
            verifyAgainIntent.putExtra(getString(R.string.mddi_mode), MddiMode.VERIFY);
            startActivity(verifyAgainIntent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        binding.registerNewButton.setOnClickListener(view -> {
            vibrator.vibrate(VibrationEffect.createOneShot(VIBRATION_MS, VibrationEffect.DEFAULT_AMPLITUDE));
            Intent registerNewIntent = new Intent(getApplicationContext(), CameraScanScreen.class);
            registerNewIntent.putExtra(getString(R.string.mddi_mode), MddiMode.REGISTER);
            startActivity(registerNewIntent);
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

    private void setImageViewWithBitmap(byte[] byteArray) {
        if (byteArray != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            Bitmap watermarkedBitmap = waterMarkBitmap(bitmap);
            runOnUiThread(() -> binding.imageView.setImageBitmap(watermarkedBitmap));
        }
    }

    private static Bitmap waterMarkBitmap(Bitmap original) {
        Bitmap src = original.copy(Bitmap.Config.ARGB_8888, true);
        Bitmap result = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src, 0, 0, null);
        Paint paint = new Paint();
        Paint.FontMetrics fm = new Paint.FontMetrics();
        paint.setColor(Color.BLACK);
        paint.setTextSize(65);
        paint.getFontMetrics(fm);
        int margin = 5;
        float height = paint.measureText("yY");
        int left = 0;
        int top = (int) (src.getHeight() / 2 - height / 2);
        int right = src.getWidth() - margin;
        int bottom = (int) (src.getHeight() / 2 + height / 2);
        canvas.drawRect(left, top, right, bottom, paint);
        paint.setColor(Color.RED);
        int x = (int) (src.getWidth() / 2 - paint.measureText("VERIFIED") / 2);
        int y = (int) ((top + bottom) / 2 + height / 3);
        canvas.drawText("VERIFIED", x, y, paint);
        return result;
    }
}
