package com.DyncoApp.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.StrictMode;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.DyncoApp.R;
import com.mddi.misc.InstanceType;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class A4_ResultScreen extends AppCompatActivity {

    protected TextView scoreTextView;
    protected RatingBar responseRatingBar;
    protected Vibrator vibrator;
    protected GlobalVariables globalVariables = new GlobalVariables();
    protected Bitmap searchBitmap;
    protected Bitmap addBitmap;

    protected static String SQL_DRIVER = "net.sourceforge.jtds.jdbc.Driver";

    protected ImageButton backButton;
    protected ImageButton homeButton;
    protected ImageView responseImageView;
    protected Button saveDescriptionButton;
    protected ImageButton closeButton;

    protected TextView sqlTextView;
    protected TextView uidTextView;

    protected String sqlUid;
    protected String sqlScore;
    protected String sqlSno;

    protected String mddiUid;
    protected String mddiSno;
    protected float mddiScore;
    protected int mddiRating;
    protected float scoreRating;
    protected ImageView loadingImageView;
    protected AnimationDrawable loadingAnimation;
    protected boolean duplicateEntry = false;

    protected int currentLayout;
    protected final int grpcLayout = 0;
    protected final int sqlLayout = 1;
    protected int count_uid_sql = 0;
    protected String value_uid_sql = "";
    protected Bitmap sqlBitmap;
    protected String sqlDescription;

    protected int imageViewWidth;
    protected int imageViewHeight;

    protected boolean insideSqlPingLoop = false;
    private final List<Thread> backgroundThreads = new ArrayList<>(1);

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a4_resultscreen);
        globalVariables = (GlobalVariables) getApplicationContext();
        assignIDs();
        enableLayout();
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE));
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        // getting the bundle from the intent
        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        String option = bundle.getString("Option");
        currentLayout = grpcLayout;

        backButton.setOnClickListener(view -> {
            vibrator.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE));
            if (currentLayout == grpcLayout) {
                onBackPressed();
            } else if (currentLayout == sqlLayout) {
                showMddiLayout();
            }
        });

        homeButton.setOnClickListener(view -> {
            vibrator.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE));
            Intent intent = new Intent(getApplicationContext(), A1_HomeScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        saveDescriptionButton.setOnClickListener(v -> {
            loadingImageView.setVisibility(View.VISIBLE);
            loadingAnimation.start();
            //Make the main layout and push button invisible until the sql DB is connected
            saveDescriptionButton.setEnabled(false);
            HandlerThread connectionHandlerThread = new HandlerThread("connection handler");
            connectionHandlerThread.start();
            this.backgroundThreads.add(connectionHandlerThread);
            Handler connectionHandler = new Handler(connectionHandlerThread.getLooper());
            connectionHandler.post(() -> {
                try {
                    checkSqlConnection();
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
        });

        closeButton.setOnClickListener(v -> showMddiLayout());

        if (Objects.equals(option, "Search")) {
            if (getIntent().hasExtra("byteArray")) {
                searchBitmap = BitmapFactory.decodeByteArray(
                        getIntent().getByteArrayExtra("byteArray"), 0,
                        Objects.requireNonNull(getIntent().getByteArrayExtra("byteArray")).length);
                //Set the image view with the created bitmap
                searchBitmap = waterMarkMddi(searchBitmap);
                runOnUiThread(() -> responseImageView.setImageBitmap(searchBitmap));
            }
            mddiUid = getIntent().getStringExtra("UID");
            mddiSno = getIntent().getStringExtra("SNO");
            mddiScore = getIntent().getFloatExtra("SCORE", 0);
            mddiRating = getIntent().getIntExtra("RATING", 0);
            sqlUid = globalVariables.sqlCid + ":" + mddiUid;
            sqlSno = "SNO = " + mddiSno;
            sqlScore = String.valueOf(mddiScore);
            uidTextView.setText(mddiUid);
            scoreTextView.setText(String.format("Score = %s", mddiScore));
            if (globalVariables.currentInstance == InstanceType.DB_SNO) {
                sqlTextView.setText("SNO = " + mddiSno);
            } else {
                sqlTextView.setVisibility(View.INVISIBLE);
            }
            responseRatingBar.setRating(mddiRating);
            scoreRating = responseRatingBar.getRating();
        } else if (Objects.equals(option, "Add")) {
            closeButton.setEnabled(false);
            saveDescriptionButton.setEnabled(false);
            scoreTextView.setEnabled(false);
            uidTextView.setEnabled(true);
            responseRatingBar.setEnabled(false);
            closeButton.setVisibility(View.INVISIBLE);
            saveDescriptionButton.setVisibility(View.INVISIBLE);
            scoreTextView.setVisibility(View.INVISIBLE);
            uidTextView.setVisibility(View.VISIBLE);
            responseRatingBar.setVisibility(View.INVISIBLE);
            if (getIntent().hasExtra("byteArray")) {
                addBitmap = BitmapFactory.decodeByteArray(
                        getIntent().getByteArrayExtra("byteArray"), 0,
                        Objects.requireNonNull(getIntent().getByteArrayExtra("byteArray")).length);
                //Set the image view with the created bitmap
                runOnUiThread(() -> responseImageView.setImageBitmap(addBitmap));
            }
            //Set the respective message for the add api
            uidTextView.setTextSize(28);
            uidTextView.setText(R.string.Added);
            if (globalVariables.currentInstance == InstanceType.DB_SNO) {
                sqlTextView.setEnabled(true);
                sqlTextView.setVisibility(View.VISIBLE);
                sqlTextView.setText("SNO = " + getIntent().getStringExtra("SNO"));
            } else {
                sqlTextView.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * Events to be happened while pausing the activity
     */
    @Override
    protected void onPause() {
        super.onPause();
        for (Thread thread : this.backgroundThreads) {
            try {
                thread.interrupt();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    /**
     * Events to be happened while resuming the activity
     */
    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    /**
     * Events to be happened while pressing the back button
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), A3_CameraScan.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    /**
     * Assign the layout IDs to the items
     */
    public void assignIDs() {
        responseImageView = findViewById(R.id.sqlImageView);
        scoreTextView = findViewById(R.id.responseScoreTextView);
        responseRatingBar = findViewById(R.id.response_RB);
        saveDescriptionButton = findViewById(R.id.sqlpushButton);
        closeButton = findViewById(R.id.sqlCloseImageButton);
        sqlTextView = findViewById(R.id.sqlTextView);
        uidTextView = findViewById(R.id.sqlUIDTextView);
        loadingImageView = findViewById(R.id.loadingSQLImageView);
        backButton = findViewById(R.id.backButton);
        homeButton = findViewById(R.id.homeButton);
    }

    /**
     * Enable the layout items
     */
    public void enableLayout() {
        scoreTextView.setEnabled(true);
        responseImageView.setEnabled(true);
        responseRatingBar.setEnabled(true);
        scoreTextView.setVisibility(View.VISIBLE);
        responseImageView.setVisibility(View.VISIBLE);
        responseRatingBar.setVisibility(View.VISIBLE);
        closeButton.setEnabled(false);
        closeButton.setVisibility(View.INVISIBLE);
        imageViewWidth = responseImageView.getLayoutParams().width;
        imageViewHeight = responseImageView.getLayoutParams().height;
        if (globalVariables.sqlDbEnabled) {
            saveDescriptionButton.setVisibility(View.VISIBLE);
        } else {
            saveDescriptionButton.setVisibility(View.INVISIBLE);
        }
        loadingImageView.setBackgroundResource(R.drawable.animationscan_loading);
        loadingAnimation = (AnimationDrawable) loadingImageView.getBackground();
        loadingImageView.setVisibility(View.INVISIBLE);
    }

    /**
     * Show the mddi layout
     */
    private void showMddiLayout() {
        currentLayout = grpcLayout;
        responseRatingBar.setVisibility(View.VISIBLE);
        scoreTextView.setVisibility(View.VISIBLE);
        responseRatingBar.setRating(scoreRating);
        responseImageView.requestLayout();
        responseImageView.getLayoutParams().width = imageViewWidth;
        responseImageView.getLayoutParams().height = imageViewHeight;
        responseImageView.setImageBitmap(searchBitmap);
        scoreTextView.setText(String.format("Score = %s", mddiScore));
        if (globalVariables.currentInstance == InstanceType.DB_SNO) {
            sqlTextView.setText("SNO = " + mddiSno);
        } else {
            sqlTextView.setVisibility(View.INVISIBLE);
        }
        sqlTextView.setTextSize(24);
        uidTextView.setText(mddiUid);
        saveDescriptionButton.setEnabled(true);
        saveDescriptionButton.setVisibility(View.VISIBLE);
        closeButton.setEnabled(false);
        closeButton.setVisibility(View.INVISIBLE);
    }

    /**
     * Show the sql layout
     */
    private void showSqlLayout() {
        currentLayout = sqlLayout;
        responseRatingBar.setVisibility(View.INVISIBLE);
        scoreTextView.setVisibility(View.INVISIBLE);
        sqlTextView.setVisibility(View.VISIBLE);
        sqlTextView.setEnabled(true);
        responseImageView.requestLayout();
        responseImageView.getLayoutParams().width = (int) (imageViewWidth * 1.1);
        responseImageView.getLayoutParams().height = (int) (imageViewHeight * 1.1);
        responseImageView.setImageBitmap(sqlBitmap);
        sqlTextView.setTextSize(30);
        sqlTextView.setText(sqlDescription);
        uidTextView.setText(sqlUid);
        closeButton.setEnabled(true);
        closeButton.setVisibility(View.VISIBLE);
        saveDescriptionButton.setEnabled(false);
        saveDescriptionButton.setVisibility(View.INVISIBLE);
    }

    /**
     * Watermark the bitmap
     */
    private static Bitmap waterMarkMddi(Bitmap original) {
        Bitmap src = original.copy(Bitmap.Config.ARGB_8888, true);
        Bitmap result = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(src, 0, 0, null);
        Paint paint = new Paint();
        Paint.FontMetrics fm = new Paint.FontMetrics();
        paint.setColor(Color.BLACK);
        paint.setAlpha(50);
        paint.setTextSize(100);
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

    /**
     * Check if the uid already exists
     */
    protected boolean uidAlreadyExists() {
        try {
            int countUidSql = 0;
            String valueUidSql = "";
            Class.forName(SQL_DRIVER);
            globalVariables.sqlConnection = DriverManager.getConnection(globalVariables.sqlUrl, globalVariables.sqlUser, globalVariables.sqlPwd);
            Statement statement = globalVariables.sqlConnection.createStatement();
            ResultSet result1 = statement.executeQuery("SELECT COUNT(*) from " + globalVariables.sqlTable);
            while (result1.next()) {
                countUidSql = result1.getInt(1);
            }
            ResultSet result2 = statement.executeQuery("SELECT " + getString(R.string.sqlUID) + " from " + globalVariables.sqlTable + " where " + getString(R.string.sqlUID) + " like " + "'" + sqlUid + "'");
            while (result2.next()) {
                valueUidSql = result2.getString(1);
            }
            if (countUidSql != 0) {
                if (!String.valueOf(valueUidSql).equals(sqlUid)) {
                    saveDescriptionButton.setText(R.string.sql_NotAdded);
                    return false;
                } else {
                    if ((saveDescriptionButton.getText().toString().equals("Save Description"))) {
                        Toast.makeText(A4_ResultScreen.this, "Descriptive image and text already exists for this ID!!!!", Toast.LENGTH_SHORT).show();
                    }
                    saveDescriptionButton.setText(R.string.sql_Added);
                    return true;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Alert builder to add the sql data
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sqlAddAlertBuilder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setCancelable(false);

        builder.setTitle("Add additional data").
                setMessage("Do you want to add descriptive image and text?").setIcon(R.drawable.dynamicelementlogo);

        builder.setPositiveButton("Yes", (dialog, option) -> {
            addSqlData();
        });

        builder.setNegativeButton("No", (dialog, option) -> {
            runOnUiThread(() -> loadingImageView.setVisibility(View.INVISIBLE));
            runOnUiThread(() -> loadingAnimation.stop());
            // Do something when user clicked the No button
            Toast.makeText(getApplicationContext(), "The operation has been cancelled", Toast.LENGTH_SHORT).show();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Add the sql data
     */
    public void addSqlData() {
        runOnUiThread(() -> loadingImageView.setVisibility(View.INVISIBLE));
        runOnUiThread(() -> loadingAnimation.stop());
        try {
            Statement statement = globalVariables.sqlConnection.createStatement();
            ResultSet result1 = statement.executeQuery("SELECT COUNT(*) from " + globalVariables.sqlTable);
            while (result1.next()) {
                count_uid_sql = result1.getInt(1);
            }
            String sqlStatement = "SELECT " + getString(R.string.sqlUID) + " from " + globalVariables.sqlTable + " where " + getString(R.string.sqlUID) + " like " + "'" + sqlUid + "'";
            ResultSet result2 = statement.executeQuery(sqlStatement);
            while (result2.next()) {
                value_uid_sql = result2.getString(1);
            }
            if (count_uid_sql == 0) {
                Intent intent = new Intent(getApplicationContext(), A5_SqlCameraScreen.class);
                // storing the string value in the bundle which is mapped to key
                intent.putExtra("SQL_UID", sqlUid);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                Toast.makeText(A4_ResultScreen.this, "Opening Camera..Take the descriptive image..", Toast.LENGTH_SHORT).show();
            } else {
                if (String.valueOf(value_uid_sql).equals(sqlUid)) {
                    ResultSet result3 = statement.executeQuery("SELECT " + getString(R.string.sqlImage) + "," + getString(R.string.sqlDescription) + " from " + globalVariables.sqlTable + " where " + getString(R.string.sqlUID) + " like " + "'" + sqlUid + "'" + ";");
                    result3.next();
                    byte[] resultBytes = result3.getBytes(1);
                    sqlBitmap = BitmapFactory.decodeByteArray(resultBytes, 0, resultBytes.length);
                    sqlDescription = result3.getString(2);
                    showSqlLayout();
                    runOnUiThread(() -> saveDescriptionButton.setText(R.string.sql_Added));
                    if (!duplicateEntry) {
                        Toast.makeText(A4_ResultScreen.this, "Data already exists for this UID", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Intent intent = new Intent(getApplicationContext(), A5_SqlCameraScreen.class);
                    // storing the string value in the bundle which is mapped to key
                    intent.putExtra("SQL_UID", sqlUid);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    Toast.makeText(A4_ResultScreen.this, "Opening Camera..Take the descriptive image..", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(A4_ResultScreen.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Check the sql server connection
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void checkSqlConnection() throws Exception {

        try {
            insideSqlPingLoop = true;
            Class.forName(SQL_DRIVER);
            globalVariables.sqlConnection = DriverManager.getConnection(globalVariables.sqlUrl, globalVariables.sqlUser, globalVariables.sqlPwd);
            runOnUiThread(() -> {
                saveDescriptionButton.setEnabled(true);
                loadingImageView.setVisibility(View.INVISIBLE);
                loadingAnimation.stop();
            });

            if (uidAlreadyExists()) {
                runOnUiThread(() -> {
                    duplicateEntry = true;
                    addSqlData();
                });
            } else {
                runOnUiThread(() -> {
                    loadingImageView.setVisibility(View.VISIBLE);
                    loadingAnimation.start();
                    sqlAddAlertBuilder();
                });
            }
            runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Connected with SQL", Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            globalVariables.sqlDbEnabled = false;
            runOnUiThread(() -> {
                saveDescriptionButton.setEnabled(true);
                loadingImageView.setVisibility(View.INVISIBLE);
                loadingAnimation.stop();
            });
            throw e;
        }
    }
}
