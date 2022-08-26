package com.DyncoApp.ui;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.DyncoApp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class A1_HomeScreen extends AppCompatActivity {
    protected static final String SHARED_PREFS_HOME_SCREEN = "sharedPrefsHomeScreen";
    protected static final String LOGIN_USERNAME_ADMIN = "username";
    protected static final String LOGIN_PASSWORD_ADMIN = "password";
    protected static final String  DEF_LOGIN_USERNAME_ADMIN = "Admin";
    protected static final String DEF_LOGIN_PASSWORD_ADMIN = "admin";

    protected static final String LOGIN_USERNAME_TEST = "usernameTest";
    protected static final String LOGIN_PASSWORD_TEST = "passwordTest";
    protected static final String DEF_LOGIN_USERNAME_TEST = "Test";
    protected static final String DEF_LOGIN_PASSWORD_TEST = "test";

    protected static final String LOGIN_USERNAME_READONLY = "usernameReadonly";
    protected static final String LOGIN_PASSWORD_READONLY = "passwordReadonly";
    protected static final String DEF_LOGIN_USERNAME_READONLY = "ReadOnly";
    protected static final String DEF_LOGIN_PASSWORD_READONLY = "readonly";

    protected static final String SPINNER_POSITION = "spinnerPosition";
    protected static int DEF_SPINNER_POSITION = 1;

    protected EditText userIdEditText;
    protected EditText passwordEditText;

    protected Button defaultsButton;
    protected Button loginButton;
    protected Spinner userTypeSpinner;
    protected ImageView imageLogoView;
    protected List<String> userList = new ArrayList<>();

    protected Vibrator vibrator;

    protected LinearLayout userIdLayout;
    protected LinearLayout passwordLayout;
    protected SharedPreferences sharedPreferences;
    protected SharedPreferences.Editor editor;
    protected GlobalVariables globalVariables = new GlobalVariables();
    protected int press = 0;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.DarkActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a1_homescreen);

        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(
                new ColorDrawable(getColor(R.color.colorGreen)));

        sharedPreferences = getSharedPreferences(SHARED_PREFS_HOME_SCREEN, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        initializeLayout();

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        globalVariables.model = Build.MODEL;


        loginButton.setOnClickListener(v -> {
            if (!isInternetAvailable(this)) {
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                return;
            }
            //The input fields should not be empty
            if (!(!userIdEditText.getText().toString().isEmpty()
                    && !passwordEditText.getText().toString().isEmpty())) {
                Toast.makeText(this, "Username or Password cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userTypeSpinner.getSelectedItem().toString().equals(getString(R.string.AdminUser))) {
                if (userIdEditText.getText().toString().equals(getString(R.string.AdminUser)) &&
                        passwordEditText.getText().toString().equals(getString(R.string.adminPwd))) {
                    saveAndMoveToNextActivity(userTypeSpinner.getSelectedItem().toString());
                } else {
                    //Display this message
                    Toast.makeText(this, "Wrong Credentials", Toast.LENGTH_SHORT).show();
                }
            } else if (userTypeSpinner.getSelectedItem().toString().equals(getString(R.string.TestUser))) {
                if (userIdEditText.getText().toString().equals(getString(R.string.TestUser)) &&
                        passwordEditText.getText().toString().equals(getString(R.string.testPwd))) {
                    saveAndMoveToNextActivity(userTypeSpinner.getSelectedItem().toString());
                } else {
                    //Display this message
                    Toast.makeText(this, "Wrong Credentials", Toast.LENGTH_SHORT).show();
                }
            } else if (userTypeSpinner.getSelectedItem().toString().equals(getString(R.string.ReadonlyUser))) {
                if (userIdEditText.getText().toString().equals(getString(R.string.ReadonlyUser)) &&
                        passwordEditText.getText().toString().equals(getString(R.string.readOnlyPwd))) {

                    saveAndMoveToNextActivity(userTypeSpinner.getSelectedItem().toString());
                } else {
                    //Display this message
                    Toast.makeText(this, "Wrong Credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });
        userTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String userType = parent.getItemAtPosition(position).toString();
                loadDataFromSharedPreferences(userType);
                saveData(userType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        defaultsButton.setOnClickListener(v -> restoreDefaults());

        imageLogoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                press++;
                if(press==5){
                    //Move  to this activity
                    Intent intent = new Intent(getApplicationContext(), Activity_0_secret.class);

                    press = 0;
                    Bundle b = ActivityOptions.makeSceneTransitionAnimation(A1_HomeScreen.this).toBundle();
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }

            }
        });
    }
    /**
     * When the back button is pressed
     */
    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    /**
     * Assigning all the layout items to specific items
     */
    private void initializeLayout() {
        loginButton = findViewById(R.id.connectButton);
        passwordEditText = findViewById(R.id.passwordEditText);
        userIdEditText = findViewById(R.id.useridEditText);
        defaultsButton = findViewById(R.id.defaultsButton);
        userTypeSpinner = findViewById(R.id.userTypeSpinner);
        userIdLayout = findViewById(R.id.userIDLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        imageLogoView = findViewById(R.id.logoView);

        loginButton.setEnabled(true);
        passwordEditText.setEnabled(true);
        userIdEditText.setEnabled(true);
        //Hide the password initially
        passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());

        globalVariables = (GlobalVariables) getApplicationContext();
        defaultsButton.setVisibility(View.INVISIBLE);
        defaultsButton.setEnabled(false);

        userList.add(getString(R.string.AdminUser));
        userList.add(getString(R.string.TestUser));
        userList.add(getString(R.string.ReadonlyUser));
        userTypeSpinner.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.spinneritemuser, userList));
        userTypeSpinner.setSelection(this.sharedPreferences.getInt(SPINNER_POSITION, DEF_SPINNER_POSITION));
        loadDataFromSharedPreferences(userTypeSpinner.getSelectedItem().toString());
    }

    /**
     * Load the data from shared preferences based on the user type
     */
    private void loadDataFromSharedPreferences(String userType) {
        if (userType.equals(getString(R.string.AdminUser))) {
            userIdEditText.setText(this.sharedPreferences.getString(LOGIN_USERNAME_ADMIN, DEF_LOGIN_USERNAME_ADMIN));
            passwordEditText.setText(this.sharedPreferences.getString(LOGIN_PASSWORD_ADMIN, DEF_LOGIN_PASSWORD_ADMIN));
        } else if (userType.equals(getString(R.string.TestUser))) {
            userIdEditText.setText(this.sharedPreferences.getString(LOGIN_USERNAME_TEST, DEF_LOGIN_USERNAME_TEST));
            passwordEditText.setText(this.sharedPreferences.getString(LOGIN_PASSWORD_TEST, DEF_LOGIN_PASSWORD_TEST));
        }else if (userType.equals(getString(R.string.ReadonlyUser))) {
            userIdEditText.setText(this.sharedPreferences.getString(LOGIN_USERNAME_READONLY, DEF_LOGIN_USERNAME_READONLY));
            passwordEditText.setText(this.sharedPreferences.getString(LOGIN_PASSWORD_READONLY, DEF_LOGIN_PASSWORD_READONLY));
        }
    }

    /**
     * Save the data to shared preferences based on the user type
     */
    private void saveData(String userType) {
        if (userType.equals(getString(R.string.AdminUser))) {
            this.editor.putString(LOGIN_USERNAME_ADMIN, userIdEditText.getText().toString());
            this.editor.putString(LOGIN_PASSWORD_ADMIN, passwordEditText.getText().toString());
        } else if (userType.equals(getString(R.string.TestUser))) {
            this.editor.putString(LOGIN_USERNAME_TEST, userIdEditText.getText().toString());
            this.editor.putString(LOGIN_PASSWORD_TEST, passwordEditText.getText().toString());
        }else if (userType.equals(getString(R.string.ReadonlyUser))) {
            this.editor.putString(LOGIN_USERNAME_READONLY, userIdEditText.getText().toString());
            this.editor.putString(LOGIN_PASSWORD_READONLY, passwordEditText.getText().toString());
        }
        this.editor.putInt(SPINNER_POSITION, userTypeSpinner.getSelectedItemPosition());
        this.editor.apply();
    }

    /**
     * Save the user type and move to the next activity
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveAndMoveToNextActivity(String userType) {
        vibrator.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE));
        if (userType.equals(getString(R.string.AdminUser))) {
            globalVariables.selectedUserMode = SelectedUserMode.ADMIN;
        } else if (userType.equals(getString(R.string.TestUser))) {
            globalVariables.selectedUserMode = SelectedUserMode.TEST;
        }else if (userType.equals(getString(R.string.ReadonlyUser))) {
            globalVariables.selectedUserMode = SelectedUserMode.READONLY;
        }

        Intent intent = new Intent(getApplicationContext(), A2_LoginScreen.class);
        saveData(userType);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * Restore the default values
     */
    private void restoreDefaults() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setCancelable(true);
        builder.setIcon(R.drawable.dynamicelementlogo).setTitle("Restore to Defaults").setMessage("Are you sure you want to restore the default values?");

        builder.setPositiveButton("Yes", (dialog, option) -> {
            if (userTypeSpinner.getSelectedItem().toString().equals(getString(R.string.AdminUser))) {
                userIdEditText.setText(DEF_LOGIN_USERNAME_ADMIN);
                passwordEditText.setText(DEF_LOGIN_PASSWORD_ADMIN);
            } else if (userTypeSpinner.getSelectedItem().toString().equals(getString(R.string.TestUser))) {
                userIdEditText.setText(DEF_LOGIN_USERNAME_TEST);
                passwordEditText.setText(DEF_LOGIN_PASSWORD_TEST);
            }else if (userTypeSpinner.getSelectedItem().toString().equals(getString(R.string.ReadonlyUser))) {
                userIdEditText.setText(DEF_LOGIN_USERNAME_READONLY);
                passwordEditText.setText(DEF_LOGIN_PASSWORD_READONLY);
            }
            Toast.makeText(this, "Defaults restored", Toast.LENGTH_SHORT).show();
            saveData(userTypeSpinner.getSelectedItem().toString());
        });
        builder.setNegativeButton("No", (dialog, option) -> Toast.makeText(getApplicationContext(),
                "The operation has been cancelled", Toast.LENGTH_SHORT).show());
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    /**
     * Check the internet connection
     */
    protected static boolean isInternetAvailable(Context context) {
        NetworkInfo info = ((ConnectivityManager)
                Objects.requireNonNull(context.getSystemService(Context.CONNECTIVITY_SERVICE))).getActiveNetworkInfo();
        if (info == null) {
            Log.d("Network", "no internet connection");
            return false;
        } else {
            if (info.isConnected()) {
                //Log.d("Network"," internet connection available...");
            } else {
                Log.d("Network", " internet connection");
            }
            return true;
        }
    }
}