package com.DyncoApp.ui;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.DyncoApp.R;

import java.util.Objects;

public class A0_Onboarding extends AppCompatActivity {
    public static String SHARED_PREFS_ONBOARDING = "sharedprefsonboarding";
    public static String StartState = "currentstartstate";
    protected ViewPager mSlideViewPager;
    protected LinearLayout mDotLayout;
    protected Button nextButton;
    protected Button prevButton;
    protected Button skipButton;
    protected SharedPreferences sharedPreferences;
    protected SharedPreferences.Editor editor;
    protected SliderAdapter sliderAdapter;
    protected TextView[] mDots;
    protected int currentPage;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.NoActionBarTheme);
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences(SHARED_PREFS_ONBOARDING, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        Intent intent = new Intent(getApplicationContext(), A1_HomeScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //Start the next activity
        startActivity(intent);
//
//        if (Objects.equals(sharedPreferences.getString(StartState, ""), getString(R.string.finishText))) {
//            Intent intent = new Intent(getApplicationContext(), A1_HomeScreen.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            //Start the next activity
//            startActivity(intent);
//        } else {
//            setContentView(R.layout.a0_onboarding);
//            mSlideViewPager = findViewById(R.id.slideViewPager);
//            mDotLayout = findViewById(R.id.dotsLayout);
//            nextButton = findViewById(R.id.nextButton);
//            prevButton = findViewById(R.id.previousButton);
//            skipButton = findViewById(R.id.skipButton);
//            sliderAdapter = new SliderAdapter(this);
//            mSlideViewPager.setAdapter(sliderAdapter);
//            addDotsIndicator(0);
//            mSlideViewPager.addOnPageChangeListener(viewListener);
//            nextButton.setOnClickListener(view -> {
//                if (nextButton.getText().toString().equals(getString(R.string.finishText))) {
//                    finishProcess();
//                }
//                mSlideViewPager.setCurrentItem(currentPage + 1);
//            });
//            prevButton.setOnClickListener(view -> mSlideViewPager.setCurrentItem(currentPage - 1));
//            skipButton.setOnClickListener(view -> {
//                Intent intent = new Intent(getApplicationContext(), A1_HomeScreen.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                //Start the next activity
//                startActivity(intent);
//                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
//            });
//        }
    }

    public void addDotsIndicator(int position) {
        mDots = new TextView[3];
        mDotLayout.removeAllViews();
        for (int i = 0; i < mDots.length; i++) {
            mDots[i] = new TextView(this);
            mDots[i].setText(Html.fromHtml("&#8226", 1));
            mDots[i].setTextSize(35);
            mDots[i].setTextColor(getResources().getColor(R.color.colorTransparentWhite, null));
            mDotLayout.addView(mDots[i]);
        }
        if (mDots.length > 0) {
            mDots[position].setTextColor(getResources().getColor(R.color.colorWhite, null));
        }
    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            addDotsIndicator(position);
            currentPage = position;
            if (position == 0) {
                nextButton.setEnabled(true);
                prevButton.setEnabled(false);
                prevButton.setVisibility(View.INVISIBLE);
                nextButton.setText(getString(R.string.nextText));
                prevButton.setText("");
            } else if (position == mDots.length - 1) {
                nextButton.setEnabled(true);
                prevButton.setEnabled(true);
                prevButton.setVisibility(View.VISIBLE);
                nextButton.setText(getString(R.string.finishText));
                prevButton.setText(getString(R.string.prevText));
            } else {
                nextButton.setEnabled(true);
                prevButton.setEnabled(true);
                prevButton.setVisibility(View.VISIBLE);
                nextButton.setText(getString(R.string.nextText));
                prevButton.setText(getString(R.string.prevText));
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    /**
     * Dialog builder when finish button is pressed
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void finishProcess() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setCancelable(true);
        builder.setTitle("Do you want to see this 'Get Started' view?");
        builder.setMessage("Select the desired option");
        builder.setIcon(R.drawable.dynamicelementlogo);
        builder.setPositiveButton("Never see it again", (dialog, option) -> {
            saveCurrentState(getString(R.string.finishText));
        });
        // Set the alert dialog no button click listener
        builder.setNegativeButton("See it again at the app startup", (dialog, option) -> {
            saveCurrentState("");
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Save the current state and move to the next activity
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void saveCurrentState(String currentStartState) {
        editor.putString(StartState, currentStartState);
        editor.apply();
        Intent intent = new Intent(getApplicationContext(), A1_HomeScreen.class);
        startActivity(intent);
    }
}