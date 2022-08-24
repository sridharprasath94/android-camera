package com.DyncoApp.ui;

import static com.DyncoApp.ui.A1_HomeScreen.isInternetAvailable;
import static com.DyncoApp.ui.Constants.getDbSnoProClientService;
import static com.DyncoApp.ui.Constants.getIvfProClientService;
import static com.DyncoApp.ui.Constants.getIvfSnoClientService;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.DyncoApp.R;
import com.mddi.Callback;
import com.mddi.delete.DeleteResult;
import com.mddi.delete.DeleteStatus;
import com.mddi.exceptions.ExceptionType;
import com.mddi.mddiclient.ClientService;
import com.mddi.ping.PingResult;
import com.mddiv1.getsample.GetSampleResult;
import com.mddiv1.misc.InstanceType;

import java.util.ArrayList;
import java.util.List;

import lombok.SneakyThrows;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class A2_LoginScreen extends AppCompatActivity {
    public static final String DEF_CID = "1";
    public static final boolean DEF_DEBUG_MODE = false;
    public static int DEF_SPINNER_POSITION = 1;
    protected SharedPreferences sharedPreferences;
    public static String SHARED_PREFS_LOGIN_SCREEN = "sharedPrefsLoginScreen";
    public static String CREATE_COLLECTION_MODE = "createCollectionMode";
    public static String SPINNER_POSITION = "spinnerPosition";
    public static String CID = "defaultCID";

    protected LinearLayout createCollectionLayout;
    protected LinearLayout cidLayout;
    protected ImageView loadingImageView;
    protected AnimationDrawable loadingAnimation;
    protected Button connectButton;
    protected Button defaultsButton;
    protected SwitchCompat createCollectionTB;

    protected Spinner spinnerSqInstances;
    protected Vibrator vibrator;

    protected EditText cidEditText;
    protected TextView instanceTextView;
    protected Intent intent;
    protected GlobalVariables globalVariables = new GlobalVariables();

    protected List<String> sqlList = new ArrayList<>();
    protected ArrayAdapter<String> arrayAdapter;
    private final List<Thread> backgroundThreads = new ArrayList<>(1);
    protected ImageButton backButton;

    @SuppressLint("WrongThread")
    @SneakyThrows
    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a2_loginscreen);
        ActionBar actionBar = getSupportActionBar();
        ColorDrawable colorDrawable = new ColorDrawable(getColor(R.color.colorGreen));
        assert actionBar != null;
        actionBar.setBackgroundDrawable(colorDrawable);

        initializeLayout();
        enableLayoutItems();
        globalVariables = (GlobalVariables) getApplicationContext();

        checkStoragePermission();

        if (globalVariables.selectedUserMode == SelectedUserMode.ADMIN) {
            sqlList.add(getResources().getString(R.string.proDbSno));
            sqlList.add(getResources().getString(R.string.internalDbSno));
            sqlList.add(getResources().getString(R.string.proIvf));
            sqlList.add(getResources().getString(R.string.internalIvf));
            sqlList.add(getResources().getString(R.string.proIvfSno));
        } else if(globalVariables.selectedUserMode == SelectedUserMode.READONLY){
            cidLayout.setVisibility(View.INVISIBLE);
            sqlList.add(getResources().getString(R.string.internalIvf));
            sqlList.add(getResources().getString(R.string.internalDbSno));
        } else {
            sqlList.add(getResources().getString(R.string.proIvf));
        }
        loadData();

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        createCollectionTB.setChecked(globalVariables.createCollection);
        createCollectionTB.setOnClickListener(v -> globalVariables.createCollection = createCollectionTB.isChecked());

        arrayAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinneritems, sqlList);
        spinnerSqInstances.setAdapter(arrayAdapter);

        spinnerSqInstances.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String instanceName = parent.getItemAtPosition(position).toString();
                if(instanceName.equals(getResources().getString(R.string.internalDbSno))){
                    globalVariables.versionOneSelected = true;
                    globalVariables.currentInstanceMode = instanceMode.DB_SNO;
                    globalVariables.clientServiceV1 = Constants.getDirectoryClientService();
                }
                else if(instanceName.equals(getResources().getString(R.string.internalIvf))){
                    globalVariables.versionOneSelected = true;
                    globalVariables.currentInstanceMode = instanceMode.IVF;
                    globalVariables.clientServiceV1 = Constants.getDirectoryClientService();
                }
                else{
                    globalVariables.clientService = instanceName.equals(getResources().getString(R.string.proDbSno)) ?
                            getDbSnoProClientService() :  instanceName.equals(getResources().getString(R.string.proIvf)) ?
                            getIvfProClientService() : getIvfSnoClientService();
                    globalVariables.versionOneSelected = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        defaultsButton.setOnClickListener(v -> restoreDefaults());

        backButton.setOnClickListener(v -> onBackPressed());

        connectButton.setOnClickListener(v -> {
            if (!isInternetAvailable(this)) {
                Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                return;
            }

            if (sqlList.isEmpty() || cidEditText.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Enter the details and proceed...", Toast.LENGTH_SHORT).show();
                return;
            }
            vibrator.vibrate(VibrationEffect.createOneShot(75, VibrationEffect.DEFAULT_AMPLITUDE));

            runOnUiThread(() -> {
                loadingAnimation.start();
                loadingImageView.setVisibility(View.VISIBLE);
            });

            HandlerThread connectionHandlerThread = new HandlerThread("connection handler");
            connectionHandlerThread.start();
            this.backgroundThreads.add(connectionHandlerThread);
            Handler connectionHandler = new Handler(connectionHandlerThread.getLooper());
            connectionHandler.post(() -> {
                if(globalVariables.versionOneSelected){
                    checkPingDirectory(globalVariables.currentInstanceMode,globalVariables.clientServiceV1);
                }else{
                    checkConnectionV0();
                }

            });
        });
    }

    /**
     * Check storage permission
     */
    private void checkStoragePermission() {
        //Check storage permission
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    50);
        }
    }

    /**
     * Check the connection for version 0
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    private void checkConnectionV0() {
        ClientService clientService = globalVariables.clientService.createNewSession();
        clientService.checkConnection(new Callback<PingResult>() {
            @Override
            public void onResponse(PingResult response) {
                if (noInternetAvailable()) return;
                globalVariables.userCid = cidEditText.getText().toString();
                globalVariables.createCollection = createCollectionTB.isChecked();

                if (globalVariables.createCollection) {
                    if (!(clientService.getHost().equals(getResources().getString(R.string.hostProDbSno)) ||
                            clientService.getHost().equals(getResources().getString(R.string.hostProIvf)))) {
                        deleteAlertMessage();
                        return;
                    }
                }
                moveToNextActivity();
            }

            @Override
            public void onError(ExceptionType exceptionType, Exception e) {
                handleException(e);
            }
        });
    }

    private boolean noInternetAvailable() {
        if (!isInternetAvailable(getApplicationContext())) {
            enableLayoutItems();
            runOnUiThread(() -> {
                loadingAnimation.stop();
                loadingImageView.setVisibility(View.INVISIBLE);
            });
            return true;
        }
        return false;
    }

    /**
     * Check the connection for version 1
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void checkConnectionV1(String cid) {
        com.mddiv1.mddiclient.ClientService clientService = globalVariables.clientServiceV1.createNewSession();
        clientService.checkConnection(new com.mddiv1.Callback<com.mddiv1.ping.PingResult>() {
            @Override
            public void onResponse(com.mddiv1.ping.PingResult response) {
                if (noInternetAvailable()) return;

                globalVariables.userCid = cid;
                globalVariables.createCollection = createCollectionTB.isChecked();

                moveToNextActivity();

//                            if (globalVariables.createCollection) {
//                                if (!(clientService.getHost().equals(getResources().getString(R.string.productionmddidbsno)) ||
//                                        clientService.getHost().equals(getResources().getString(R.string.productionmddiivf)))) {
//                                    deleteAlertMessage();
//                                    return;
//                                }
//                            }

            }

            @Override
            public void onError(com.mddiv1.exceptions.ExceptionType exceptionType, Exception e) {
                handleException(e);
            }
        });
    }

    /**
     * Check the connection for the directory service
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void checkPingDirectory(instanceMode currentInstance,com.mddiv1.mddiclient.ClientService clientService) {
        try (com.mddiv1.mddiclient.ClientService clientServiceAutoClosable = clientService.createNewSession()) {
            clientServiceAutoClosable.getSample(currentInstance == instanceMode.DB_SNO ? "fm1" : "1", new com.mddiv1.Callback<GetSampleResult>() {
                @Override
                public void onResponse(GetSampleResult response) {
                    String[] instanceConfig = getBackendInstance(response.imageResponse);
                    Log.d("INSTANCE",instanceConfig[1]);
                    globalVariables.clientServiceV1 = Constants.getMddiBackendService(instanceConfig[1],
                            Integer.parseInt(instanceConfig[2]),globalVariables.currentInstanceMode == instanceMode.DB_SNO
                                    ? InstanceType.DB_SNO : InstanceType.IVF);

                    checkConnectionV1( currentInstance == instanceMode.DB_SNO ? "fm1" : "1");
                }

                /**
                 * @param exceptionType is the type of exception.
                 * @param e             is the actual exception.
                 */
                @Override
                public void onError(com.mddiv1.exceptions.ExceptionType exceptionType, Exception e) {
                    handleException(e);
                }
            });
        } catch (Exception e) {
            handleException(e);
        }
    }

    /**
     * Handle the exception from Mddi service call
     */
    private void handleException(Exception e) {
        runOnUiThread(() -> {
            loadingAnimation.stop();
            loadingImageView.setVisibility(View.INVISIBLE);
        });
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
        enableLayoutItems();
        Intent intent = new Intent(getApplicationContext(), A2_LoginScreen.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    /**
     * Get the backend instance credentials from the directory service
     */
    private String[] getBackendInstance(String response) {
        return response.split(",");
    }

    /**
     * When press the back button
     */
    @Override
    public void onBackPressed() {
        goBackToLogin();
    }

    /**
     * On pausing the activity
     */
    @Override
    public void onPause() {
        super.onPause();
        for (Thread thread : this.backgroundThreads) {
            try {
                thread.interrupt();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        saveData();
    }

    /**
     * On resuming the activity
     */
    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    /**
     * Move to the next activity
     */
    private void moveToNextActivity() {
        createCollectionTB.setChecked(false);
        Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), A3_CameraScan.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    /**
     * Save the data to the shared preferences
     */
    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_LOGIN_SCREEN, MODE_PRIVATE);
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = sharedPreferences.edit();
        //Save the credentials and configuration data to shared preferences
        editor.putBoolean(CREATE_COLLECTION_MODE, createCollectionTB.isChecked()).putString(CID, cidEditText.getText().toString()).
                putInt(SPINNER_POSITION, spinnerSqInstances.getSelectedItemPosition()).apply();
    }

    /**
     * Load the data from the shared preferences
     */
    public void loadData() {
        sharedPreferences = getSharedPreferences(SHARED_PREFS_LOGIN_SCREEN, MODE_PRIVATE);
        cidEditText.setText(sharedPreferences.getString(CID, DEF_CID));
        createCollectionTB.setChecked(sharedPreferences.getBoolean(CREATE_COLLECTION_MODE, DEF_DEBUG_MODE));
        //Set the spinner with the last position only if  it is greater than the last position
        if (spinnerSqInstances.getCount() > sharedPreferences.getInt(SPINNER_POSITION, DEF_SPINNER_POSITION)) {
            spinnerSqInstances.setSelection(sharedPreferences.getInt(SPINNER_POSITION, DEF_SPINNER_POSITION));
        }
        if (globalVariables.selectedUserMode != SelectedUserMode.ADMIN) {
            createCollectionLayout.setEnabled(false);
            createCollectionLayout.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Assigning all the layout items to specific items
     */
    public void initializeLayout() {
        connectButton = findViewById(R.id.connectButton);
        defaultsButton = findViewById(R.id.defaultsButton);
        loadingImageView = findViewById(R.id.loadingImageView);
        instanceTextView = findViewById(R.id.instanceTextView);
        spinnerSqInstances = findViewById(R.id.instanceSpinner);
        cidEditText = findViewById(R.id.cidEditText_Login);
        cidLayout = findViewById(R.id.cidlayout_Login);
        createCollectionTB = findViewById(R.id.createCollection_TB);
        createCollectionLayout = findViewById(R.id.createCollectionLayout);
        backButton = findViewById(R.id.backButton);
    }

    /**
     * Enable the layout items
     */
    public void enableLayoutItems() {
        createCollectionTB.setVisibility(View.VISIBLE);
        connectButton.setEnabled(true);
        defaultsButton.setEnabled(false);
        defaultsButton.setVisibility(View.INVISIBLE);
        loadingImageView.setBackgroundResource(R.drawable.animationscan_loading);
        loadingAnimation = (AnimationDrawable) loadingImageView.getBackground();
        loadingImageView.setVisibility(View.INVISIBLE);
        backButton.setVisibility(View.VISIBLE);
    }

    /**
     * Go back to login page
     */
    public void goBackToLogin() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setCancelable(true);
        builder.setIcon(R.drawable.dynamicelementlogo).setTitle("Logout").setMessage("Are you sure you want to go back to the login screen?");

        builder.setPositiveButton("Yes", (dialog, option) -> {
            Intent intent = new Intent(getApplicationContext(), A1_HomeScreen.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        builder.setNegativeButton("No", (dialog, option) ->
                Toast.makeText(getApplicationContext(), "The operation has been cancelled", Toast.LENGTH_SHORT).show());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Alert message when clicking the delete button
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    protected void deleteAlertMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setCancelable(false);
        builder.setIcon(R.drawable.dynamicelementlogo).setTitle("New Collection").
                setMessage("Are you sure to delete the existing collection and create a new collection?");

        builder.setPositiveButton("Yes", (dialog, option) -> {
            if(globalVariables.versionOneSelected){
                deleteCollectionV1();
            }else{
                deleteCollection();
            }

        });

        builder.setNegativeButton("No", (dialog, option) -> {
            Toast.makeText(getApplicationContext(), "The operation has been cancelled", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), A2_LoginScreen.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void deleteCollectionV1() {
        com.mddiv1.mddiclient.ClientService clientService = globalVariables.clientServiceV1;
        clientService.deleteCollection(globalVariables.userCid, new com.mddiv1.Callback<com.mddiv1.delete.DeleteResult>() {
            @Override
            public void onResponse(com.mddiv1.delete.DeleteResult response) {
                if (response.deleteStatus == com.mddiv1.delete.DeleteStatus.DELETED ||
                        response.deleteStatus == com.mddiv1.delete.DeleteStatus.CID_NOT_EXISTS) {
                    globalVariables.createCollection = createCollectionTB.isChecked();
                    moveToNextActivity();
                }
            }

            @Override
            public void onError(com.mddiv1.exceptions.ExceptionType exceptionType, Exception e) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void deleteCollection() {
        ClientService clientService = globalVariables.clientService;
        clientService.deleteCollection(globalVariables.userCid, new Callback<DeleteResult>() {
            @Override
            public void onResponse(DeleteResult response) {
                if (response.deleteStatus == DeleteStatus.DELETED ||
                        response.deleteStatus == DeleteStatus.CID_NOT_EXISTS) {
                    globalVariables.createCollection = createCollectionTB.isChecked();
                    moveToNextActivity();
                }
            }

            @Override
            public void onError(ExceptionType exceptionType, Exception e) {

            }
        });
    }

    /**
     * Dialog builder for restoring the default values
     */
    public void restoreDefaults() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Dialog_Alert).setCancelable(true);
        builder.setIcon(R.drawable.dynam);
        builder.setTitle("Restore to Defaults");
        builder.setMessage("Are you sure you want to restore the default values?");

        builder.setPositiveButton("Yes", (dialog, option) -> {
            sqlList.clear();
            if (globalVariables.selectedUserMode == SelectedUserMode.ADMIN) {
                sqlList.add(getResources().getString(R.string.proDbSno));
                sqlList.add(getResources().getString(R.string.internalDbSno));
                sqlList.add(getResources().getString(R.string.proIvf));
                sqlList.add(getResources().getString(R.string.internalIvf));
                sqlList.add(getResources().getString(R.string.proIvfSno));
            } else if(globalVariables.selectedUserMode == SelectedUserMode.READONLY){
                sqlList.add(getResources().getString(R.string.internalIvf));
                sqlList.add(getResources().getString(R.string.internalDbSno));
            } else {
                sqlList.add(getResources().getString(R.string.proIvf));
            }
            cidEditText.setText(DEF_CID);
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Defaults restored", Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("No", (dialog, option) -> runOnUiThread(() ->
                Toast.makeText(getApplicationContext(), "The operation has been cancelled", Toast.LENGTH_SHORT).show()));
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    /**
     * Request storage permission
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 50) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast this message
                Toast.makeText(this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                //Toast this message
                Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        }
    }
}

//Restore to defaults//////////////////////////
//defaults_Button.setOnClickListener(v -> restoreDefaults());
//
//  Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.dbsno1);

// double sharpness = getImageVariance(bitmap, 480, 640, Bitmap.Config.ARGB_8888);

//        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 480, 640, true);
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//
//        HandlerThread backgroundHandler = new HandlerThread("surface view handler");
//        backgroundHandler.start();
//
//        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//
//        byte[] uncroppedBytes = stream.toByteArray();
//
//        try {
//            File outputFile = File.createTempFile("test", ".jpg", this.getCacheDir());
//            FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
//            fileOutputStream.write(uncroppedBytes);
//            Bitmap bitmap1 = MddiParameters.createResizedBitmapFromFile(outputFile.getPath(),480,640, Bitmap.Config.ARGB_8888);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
// cvtToGrayScale(bitmap,bitmap);
// Bitmap resized = Bitmap.createBitmap(200,200, Bitmap.Config.ARGB_8888);
// resizeBitmap(bitmap,resized,200,200);

//
//        sqlLiteData = new SqlLiteData(A2_LoginScreen.this, tableName, configDatabase, new SqlLocalDataCallback() {
//            @Override
//            public void onInstanceAdded(List<String> list, CredentialsData result) {
//                arrayAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinneritems, list);
//                spinnerSqInstances.setAdapter(arrayAdapter);
//                Toast.makeText(getApplicationContext(), "Instance Created", Toast.LENGTH_SHORT).show();
//                spinnerSqInstances.setSelection(list.indexOf(result.getCurrentInstance()));
//            }
//
//            @Override
//            public void onDeletingInstance(List<String> list) {
//                arrayAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinneritems, list);
//                spinnerSqInstances.setAdapter(arrayAdapter);
//            }
//
//            @Override
//            public void onDefaultsRestored(String message) {
//                sqlLiteData.getDefaultData(globalVariables.adminMode);
//                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onGettingDefaultData(List<String> List) {
//                sqlList = List;
//                arrayAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.spinneritems, List);
//                spinnerSqInstances.setAdapter(arrayAdapter);
//                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_SQLITE_Data, MODE_PRIVATE);
//                //Set the spinner with the last position only if  it is greater than the last position
//                if (spinnerSqInstances.getCount() > sharedPreferences.getInt(SPINNER_POSITION, DEF_SPINNER_POSITION)) {
//                    spinnerSqInstances.setSelection(sharedPreferences.getInt(SPINNER_POSITION, DEF_SPINNER_POSITION));
//                }
//            }
//
//            @Override
//            public void onGettingInputData(CredentialsData credentialsData) {
//                switch (credentialsData.getInstanceType1()) {
//                    case "dbsno":
//                        globalVariables.currentInstance = InstanceType.DB_SNO;
//                        cidLayout.setVisibility(View.INVISIBLE);
//                        cidLayout.setEnabled(false);
//                        break;
//                    case "ivf":
//                        globalVariables.currentInstance = InstanceType.IVF;
//                        cidLayout.setVisibility(View.VISIBLE);
//                        cidLayout.setEnabled(true);
//                        break;
//                    case "ivfSno":
//                        globalVariables.currentInstance = InstanceType.IVF_SNO;
//                        cidLayout.setVisibility(View.VISIBLE);
//                        cidLayout.setEnabled(true);
//                        break;
//                }
//
//                if (credentialsData.isSqlEnable1()) {
//                    globalVariables.sqlDbEnabled = true;
//                    SQL_URL = "jdbc:jtds:sqlserver://" + credentialsData.getHost2() + ":" + credentialsData.getPort2() + "/" + credentialsData.getDatabase2();
//                    SQL_USER = credentialsData.getUsername2();
//                    SQL_PWD = credentialsData.getPassword2();
//                    SQL_TABLE = credentialsData.getTable2();
//                } else {
//                    globalVariables.sqlDbEnabled = false;
//                }
//                hostIp = credentialsData.getHost1();
//                port = credentialsData.getPort1();
//                portNumber = Integer.parseInt(port);
//                globalVariables.userName = credentialsData.getUsername1();
//                globalVariables.userId = credentialsData.getUserid1();
//                globalVariables.password = credentialsData.getPassword1();
//            }
//
//            @Override
//            public void onError(String error) {
//                Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
//            }
//        });
//        sqlLiteData.getDefaultData(globalVariables.adminMode);
//
//        instanceDeleteButton.setOnClickListener(view -> {
//            sqlInstanceName = spinnerSqInstances.getSelectedItem().toString();
//            sqlLiteData.getInputData(sqlInstanceName);
//            sqlLiteData.deleteDialogBuilder(spinnerSqInstances);
//        });
//        instanceAddButton.setOnClickListener(view -> {
//            sqlInstanceName = spinnerSqInstances.getSelectedItem().toString();
//            sqlLiteData.getInputData(sqlInstanceName);
//            sqlLiteData.addDialog();
//        });
//        instanceEditButton.setOnClickListener(view -> {
//            sqlInstanceName = spinnerSqInstances.getSelectedItem().toString();
//            sqlLiteData.getInputData(sqlInstanceName);
//            sqlLiteData.editDialog(sqlInstanceName);
//        });

// defaultsButton.setOnClickListener(v -> sqlLiteData.restoreDefaults(sqlList));

//
//    /**
//     * The runnable tries to connect to the Mddi instance and gives the response
//     */
//    private void getClientService() {
//        if (hostIp.equals(getResources().getString(R.string.testmddismartstamp)) ||
//                hostIp.equals(getResources().getString(R.string.productionmddidbsno)) ||
//                hostIp.equals(getResources().getString(R.string.testmddidbsno)) ||
//                hostIp.equals(getResources().getString(R.string.testmddiivf))) {
//            globalVariables.clientService = ClientService.builder().host(hostIp).port(portNumber).userName(globalVariables.userName).
//                    password(globalVariables.password).userID(globalVariables.userId).hostName("www.mddiservice.com").
//                    certID(getResources().getString(R.string.ca3)).InstanceType(InstanceType.IVF_SNO).build();
//        } else if (hostIp.equals(getResources().getString(R.string.productionmddiivf))) {
//            globalVariables.clientService = ClientService.builder().host(hostIp).port(portNumber).userName(globalVariables.userName).
//                    password(globalVariables.password).userID(globalVariables.userId).hostName("mddiservice").
//                    certID(getResources().getString(R.string.ca)).InstanceType(InstanceType.IVF).build();
//        } else {
//            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Wrong instance", Toast.LENGTH_SHORT).show());
//            enableLayoutItems();
//            Intent intent = new Intent(getApplicationContext(), A2_LoginScreen.class);
//            startActivity(intent);
//            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
//        }
//        globalVariables.testIP = hostIp;
//    }

//        View instanceSelectView = getLayoutInflater().inflate(R.layout.instanceselectlayout, null);
//        EditText hostIPEditText = instanceSelectView.findViewById(R.id.host1EditText);
//        portNumber = TextUtils.isEmpty(port) ? 0 : Integer.parseInt(port);
//        ((InputMethodManager) Objects.requireNonNull(getSystemService(Context.INPUT_METHOD_SERVICE)))
//                .hideSoftInputFromWindow(hostIPEditText.getWindowToken(), 0);
//        getClientService();

//    /**
//     * Check the sql server connection
//     */
//
//    private void checkSqlConnection() throws Exception {
//        insideSqlPingLoop = true;
//        try {
//            Class.forName(SQL_DRIVER);
//            globalVariables.sqlUrl = SQL_URL;
//            globalVariables.sqlUser = SQL_USER;
//            globalVariables.sqlPwd = SQL_PWD;
//            globalVariables.sqlTable = SQL_TABLE;
//            connectionSql = DriverManager.getConnection(SQL_URL, SQL_USER, SQL_PWD);
//            globalVariables.sqlConnection = connectionSql;
//            isSqlInstanceConnected = true;
//            globalVariables.sqlDbEnabled = true;
//            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Connected with SQL", Toast.LENGTH_SHORT).show());
//        } catch (Exception e) {
//            isSqlInstanceConnected = false;
//            globalVariables.sqlDbEnabled = false;
//            throw e;
//        }
//    }

//            if (globalVariables.sqlDbEnabled) {
//                connectionHandler.post(() -> {
//                    try {
//                        checkSqlConnection();
//                    } catch (Exception e) {
//                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
//                    }
//                });
//            }

//    /**
//     * Disable the layout items
//     */
//    public void disableLayoutItems() {
//        connectButton.setEnabled(false);
//        defaultsButton.setEnabled(false);
//    }

//    protected static String SQL_DRIVER = "net.sourceforge.jtds.jdbc.Driver";
//    public static String SHARED_PREFS_SQLITE_Data = "sharedPrefsSqlLiteData";
//    protected boolean insideSqlPingLoop = false;
//    protected boolean isSqlInstanceConnected = false;
//    protected SQLiteDatabase configDatabase;
//    protected Connection connectionSql = null;
//    protected String tableName;
//    public static String SNO = "defaultSNO";
//    protected String SQL_URL;
//    protected String SQL_USER;
//    protected String SQL_PWD;
//    protected String SQL_TABLE;
//    protected ImageButton instanceDeleteButton;
//    protected ImageButton instanceAddButton;
//    protected ImageButton instanceEditButton;
// protected boolean isConnected = false;
//protected boolean insidePingLoop = false;

//        instanceDeleteButton = findViewById(R.id.instanceCloseImageButton);
//        instanceAddButton = findViewById(R.id.instanceAddImageButton);
//        instanceEditButton = findViewById(R.id.instanceEditImageButton);

//        if(!globalVariables.adminMode){
//                instanceAddButton.setVisibility(View.INVISIBLE);
//                instanceAddButton.setEnabled(false);
//                instanceEditButton.setVisibility(View.INVISIBLE);
//                instanceEditButton.setEnabled(false);
//                instanceDeleteButton.setVisibility(View.INVISIBLE);
//                instanceDeleteButton.setEnabled(false);
//                createCollectionLayout.setEnabled(false);
//                createCollectionLayout.setVisibility(View.INVISIBLE);
//                }

//tableName = getString(R.string.TableNameAdmin);
//tableName = getString(R.string.TableNameTest);
// configDatabase = this.openOrCreateDatabase("configDynco.db", MODE_PRIVATE, null);