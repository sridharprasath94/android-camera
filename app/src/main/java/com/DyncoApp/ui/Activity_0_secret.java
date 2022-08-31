package com.DyncoApp.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.DyncoApp.R;

import androidx.appcompat.app.AppCompatActivity;

public class Activity_0_secret extends AppCompatActivity {
    protected GlobalVariables global = new GlobalVariables();
//    protected TextView model_TV;
    protected TextView version_TV;
    protected TextView device_TV;
    protected TextView model_TV;
    protected ImageButton back_secretLayout;
    protected ToggleButton flashToggle;
    protected ToggleButton scoreToggle;
    public static String SHARED_PREFS_SECRET = "sharedPrefsSecret";
    public static String FLASH_TOGGLE = "flashToggle";
    public static String SCORE_TOGGLE = "scoreToggle";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        global = (GlobalVariables) getApplicationContext();
        //Set the corresponding layout for the activity
        setContentView(R.layout.secretlayout);
        showFullScreen();

//        model_TV = findViewById(R.id.model_2_textview);
        version_TV = findViewById(R.id.version_2_textview);
        device_TV = findViewById(R.id.deviceDescTextView);
        model_TV = findViewById(R.id.modelDescTextView);
        flashToggle = findViewById(R.id.flashToggleView);
        scoreToggle = findViewById(R.id.scoreToggleView);


        back_secretLayout = findViewById(R.id.backButton);

//        model_TV.setText(global.model);
        version_TV.setText(getResources().getString(R.string.versionId));
        model_TV.setText(getAndroidVersion());
        device_TV.setText(new StringBuilder().append(Build.BRAND).append(" ").append(Build.MODEL).toString());
         loadData();

        flashToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                global.toggleFlash = isChecked;
                saveData();
            }
        });

        scoreToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                global.showScore = isChecked;
                saveData();
            }
        });

//       switch (global.currentInstanceMode){
//           case InstanceType.DB_SNO:
////               if(global.grpcStubConnection.getHost().equals(getResources().getString(R.string.testmddidbsnotokyo))){
////                    instance_TV.setText("DB SNO TOKYO");
////                }else if(global.grpcStubConnection.getHost().equals(getResources().getString(R.string.testmddidbsno))){
////                   instance_TV.setText("DB SNO DE");
////               }
//
//               instance_TV.setText("DB SNO DE");
//               break;
//           case InstanceType.IVF:
//               instance_TV.setText("IVF");
//               break;
//           case InstanceType.IVF_SNO:
//               instance_TV.setText("IVF SNO");
//               break;
//       }

        back_secretLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//
//                global.backFromScan = true;
                //Move  to this activity
                Intent intent = new Intent(getApplicationContext(), A1_HomeScreen.class);
                Bundle b = ActivityOptions.makeSceneTransitionAnimation(Activity_0_secret.this).toBundle();
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private String getAndroidVersion() {
        String release = Build.VERSION.RELEASE;
        int sdkVersion = Build.VERSION.SDK_INT;
        return "Android SDK: " + sdkVersion + " (" + release +")";
    }

    /**
     * Called when the activity has detected the user's press of the back
     * key. The {@link #getOnBackPressedDispatcher() OnBackPressedDispatcher} will be given a
     * chance to handle the back button before the default behavior of
     * {@link Activity#onBackPressed()} is invoked.
     *
     * @see #getOnBackPressedDispatcher()
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        saveData();
    }

    protected void showFullScreen()    {

        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private SharedPreferences sharedPreferences;

    /**
     * Save the data in the shared preferences.
     */
    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_SECRET, MODE_PRIVATE);
        //Save the data to shared preferences
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(FLASH_TOGGLE, global.toggleFlash);
        editor.putBoolean(SCORE_TOGGLE, global.showScore);
        editor.apply();
    }
    /**
        * Load the data from the shared preferences
     */
    public void loadData() {
        sharedPreferences = getSharedPreferences(SHARED_PREFS_SECRET, MODE_PRIVATE);
        flashToggle.setChecked(sharedPreferences.getBoolean(FLASH_TOGGLE,global.toggleFlash));
        scoreToggle.setChecked(sharedPreferences.getBoolean(SCORE_TOGGLE,global.showScore));
    }

    @Override
    protected void onResume() {
        super.onResume();
        showFullScreen();
    }}

