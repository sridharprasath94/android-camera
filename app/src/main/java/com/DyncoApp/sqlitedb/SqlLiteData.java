package com.DyncoApp.sqlitedb;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.widget.SwitchCompat;

import com.DyncoApp.R;

import java.util.ArrayList;
import java.util.List;

public class SqlLiteData {

    protected static final String DEF_USERNAME_1 = "sri";
    protected static final String DEF_PASSWORD_1 = "9101";
    protected static final String DEF_USERID_1 = "sri9101";

    protected static final String DEF_HOST_1_DBSNO_PRO = "18.193.44.245";
    protected static final String DEF_HOST_1_DBSNO_INTERNAL = "3.65.251.79";
    protected static final String DEF_HOST_1_IVF_PRO = "18.159.47.218";
    protected static final String DEF_HOST_1_IVF_INTERNAL = "18.193.133.33";
    protected static final String DEF_HOST_1_IVF_SNO = "3.68.148.143";
    protected static final String DEF_PORT_1 = "443";
    protected static final String DEF_INSTANCE_TYPE_1 = "dbsno";
    protected static final boolean DEF_CHANNEL_ENABLE_2 = false;

    protected static final String DEF_HOST_2 = "192.168.0.209";
    protected static final String DEF_PORT_2 = "1433";
    protected static final String DEF_USERNAME_2 = "sridhar";
    protected static final String DEF_PASSWORD_2 = "sri@123";
    protected static final String DEF_DATABASE_2 = "SQL_DYNCO";
    protected static final String DEF_TABLE_2_DBSNO = "dbSNO_sql";
    protected static final String DEF_TABLE_2_IVF = "ivf_sql";

    protected static String HOST_1 = "hostIp1";
    protected static String PORT_1 = "port1";
    protected static String USERNAME_1 = "username1";
    protected static String PASSWORD_1 = "password1";
    protected static String USERID_1 = "userID1";
    protected static String CHANNEL_ENABLE_2 = "enableSql1";
    protected static String INSTANCE_TYPE_1 = "instanceType1";
    protected static String SHARED_PREFS_SQlite = "sharedPrefsSqlite";

    protected static String HOST_2 = "hostIp2";
    protected static String PORT_2 = "port2";
    protected static String USERNAME_2 = "username2";
    protected static String PASSWORD_2 = "pwd2";
    protected static String DATABASE_2 = "database2";
    protected static String TABLE_2 = "table2";

    protected Activity activity;
    protected SqlLocalDataCallback sqlLocalDataCallback;

    protected SharedPreferences sharedPreferences;
    protected Context context;
    protected List<String> sqlList = new ArrayList<>();
    protected List<String> instanceList = new ArrayList<>();
    protected SQLiteDatabase sqLiteDatabase;
    protected String tableName;
    protected boolean editMode = false;

    public enum instanceSelectMode {
        EDIT,
        ADD
    }

    protected static class Channel1Data {
        String host1;
        String port1;
        String username1;
        String userid1;
        String password1;
        String instanceType1;
        String instanceName1;
        boolean channelEnable2;

        protected Channel1Data(String host, String port, String username, String userid, String password, String instanceType, String instanceName, boolean channelEnable2) {
            this.host1 = host;
            this.port1 = port;
            this.username1 = username;
            this.userid1 = userid;
            this.password1 = password;
            this.instanceType1 = instanceType;
            this.instanceName1 = instanceName;
            this.channelEnable2 = channelEnable2;
        }
    }

    protected static class Channel2Data {
        String host2;
        String port2;
        String username2;
        String password2;
        String database2;
        String tablename2;

        protected Channel2Data(String host, String port, String username, String password, String database2, String tablename2) {
            this.host2 = host;
            this.port2 = port;
            this.username2 = username;
            this.password2 = password;
            this.database2 = database2;
            this.tablename2 = tablename2;
        }
    }

    protected interface addInstanceCallback {
        void onAddingNewInstance(List<String> sqlList, CredentialsData credentialsData);

        void onError(String error);
    }

    protected interface checkChannel1Callback {
        void onResult(String host, String port, String username, String userid, String password);

        void onError(String error);
    }

    protected interface checkChannel2Callback {
        void onResult(String host, String port, String username, String password, String dbname, String tablename);

        void onError(String error);
    }

    public SqlLiteData(Activity activity, String tableName, SQLiteDatabase sqLiteDatabase, SqlLocalDataCallback sqlLocalDataCallback) {
        this.activity = activity;
        this.sqlLocalDataCallback = sqlLocalDataCallback;
        this.sqLiteDatabase = sqLiteDatabase;
        this.tableName = tableName;
        instanceList.add("dbsno");
        instanceList.add("ivf");
        instanceList.add("ivfSno");
        this.sharedPreferences = activity.getSharedPreferences(SHARED_PREFS_SQlite, MODE_PRIVATE);
    }

    /**
     * Dialog builder for edit mode
     */
    public void editDialog(String currentSelectedInstance) {
        channel1DialogBuilder(instanceSelectMode.EDIT, currentSelectedInstance);
    }

    /**
     * Dialog builder for add mode
     */
    public void addDialog() {
        channel1DialogBuilder(instanceSelectMode.ADD, null);
    }

    /**
     * Dialog builder for Channel 1
     */
    private void channel1DialogBuilder(instanceSelectMode instanceSelectMode, String mddiInstanceName) {
        editMode = instanceSelectMode == SqlLiteData.instanceSelectMode.EDIT;
        Builder builder = new AlertDialog.Builder(this.activity).setCancelable(true);
        builder.setIcon(R.drawable.dynam);
        builder.setTitle(editMode ? "Modify this instance" : "Add new instance");
        builder.setMessage(editMode ? "Do you want to modify the currently selected instance?" : "Do you want to add a new instance?");
        builder.setPositiveButton("Yes", (dialogInterface, i) -> {
            Builder channelBuilder = new AlertDialog.Builder(activity).setCancelable(true);
            channelBuilder.setIcon(R.drawable.dynam);
            channelBuilder.setTitle(editMode ? "Modify Instance?" : "New Instance");
            channelBuilder.setMessage(editMode ? "Modify the credentials for this instance" : "Enter the credentials for the new instance");
            View firstChannelView = activity.getLayoutInflater().inflate(R.layout.instanceselectlayout, null);

            if (editMode) {
                channelBuilder.setView(firstChannelView).setPositiveButton("Save", null).
                        setNegativeButton("Cancel", null).setNeutralButton("Save as", null);
            } else {
                channelBuilder.setView(firstChannelView).setPositiveButton("Save", null).
                        setNegativeButton("Cancel", null);
            }

            EditText host1EditText = firstChannelView.findViewById(R.id.host1EditText);
            EditText port1EditText = firstChannelView.findViewById(R.id.port1EditText);
            EditText usernameEditText = firstChannelView.findViewById(R.id.username1EditText);
            EditText userId1EditText = firstChannelView.findViewById(R.id.userId1EditText);
            EditText pwd1EditText = firstChannelView.findViewById(R.id.pwd1EditText);
            Button channel2ModifyBtn = firstChannelView.findViewById(R.id.modifyChannel2Btn);
            SwitchCompat channel2EnableTB = firstChannelView.findViewById(R.id.channel2EnableTB);
            Spinner instance1Spinner = firstChannelView.findViewById(R.id.instance1Spinner);

            host1EditText.setText(getChannel1Data().host1);
            port1EditText.setText(getChannel1Data().port1);
            usernameEditText.setText(getChannel1Data().username1);
            userId1EditText.setText(getChannel1Data().userid1);
            pwd1EditText.setText(getChannel1Data().password1);
            channel2EnableTB.setChecked(getChannel1Data().channelEnable2);
            instance1Spinner.setAdapter(new ArrayAdapter<>(activity.getApplicationContext(), R.layout.spinneritems, instanceList));
            instance1Spinner.setSelection(instanceList.indexOf(getChannel1Data().instanceType1));

            if (!channel2EnableTB.isChecked()) {
                channel2ModifyBtn.setVisibility(View.INVISIBLE);
            } else {
                channel2ModifyBtn.setVisibility(View.VISIBLE);
                channel2ModifyBtn.setText(R.string.modifySecondChannel);
                channel2ModifyBtn.setBackgroundResource(R.drawable.buttoncustom_blue);
            }

            channel2EnableTB.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    channel2ModifyBtn.setVisibility(View.VISIBLE);
                    channel2ModifyBtn.setText(R.string.modifySecondChannel);
                    channel2ModifyBtn.setBackgroundResource(R.drawable.buttoncustom_blue);
                } else {
                    channel2ModifyBtn.setVisibility(View.INVISIBLE);
                }
            });

            AlertDialog channel1Dialog = channelBuilder.create();
            channel1Dialog.show();

            Button negBtnChannel1 = channel1Dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            negBtnChannel1.setOnClickListener(v ->
                    channel1Dialog.dismiss()
            );

            //Rewrite the existing instance
            Button neutralBtnChannel1 = channel1Dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            neutralBtnChannel1.setOnClickListener(v -> checkChannel1(host1EditText, port1EditText, usernameEditText, userId1EditText,
                    pwd1EditText, new checkChannel1Callback() {
                        @Override
                        public void onResult(String host, String port, String username, String userid, String password) {
                            Channel1Data channel1Data = new Channel1Data(host, port, username, userid, password,
                                    instance1Spinner.getSelectedItem().toString(), mddiInstanceName, channel2EnableTB.isChecked());

                            editAndAdd(channel1Data, null, new addInstanceCallback() {
                                @Override
                                public void onAddingNewInstance(List<String> list, CredentialsData credentialsData) {
                                    saveDataSharedPreferences(credentialsData);
                                    sqlLocalDataCallback.onInstanceAdded(list, credentialsData);
                                    channel1Dialog.dismiss();
                                }

                                @Override
                                public void onError(String error) {
                                    sqlLocalDataCallback.onError(error);
                                }
                            });
                        }

                        @Override
                        public void onError(String error) {
                            sqlLocalDataCallback.onError(error);
                        }
                    }));

            Button posBtnChannel1;
            if (editMode) {
                posBtnChannel1 = channel1Dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            } else {
                posBtnChannel1 = channel1Dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            }

            //Add to a new instance
            posBtnChannel1.setOnClickListener(v -> checkChannel1(host1EditText, port1EditText, usernameEditText, userId1EditText,
                    pwd1EditText, new checkChannel1Callback() {
                        @Override
                        public void onResult(String host, String port, String username, String userid, String password) {

                            Channel1Data channel1Data = new Channel1Data(host, port, username, userid, password,
                                    instance1Spinner.getSelectedItem().toString(), mddiInstanceName, channel2EnableTB.isChecked());

                            Builder instanceSaveBuilder = new Builder(activity).setCancelable(false);
                            instanceSaveBuilder.setIcon(R.drawable.dynam);
                            instanceSaveBuilder.setTitle("Instance Name");
                            instanceSaveBuilder.setMessage("Enter a unique name for the new instance");
                            View instanceSaveView = activity.getLayoutInflater().inflate(R.layout.instancesavelayout, null);
                            instanceSaveBuilder.setView(instanceSaveView).setPositiveButton("Save", null).
                                    setNegativeButton("Cancel", null);

                            AlertDialog instanceSaveDialog = instanceSaveBuilder.create();
                            instanceSaveDialog.show();
                            EditText instanceNameEditText = instanceSaveView.findViewById(R.id.instanceNameEditText);
                            Button posBtnInstanceSave = instanceSaveDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            Button negBtnInstanceSave = instanceSaveDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                            posBtnInstanceSave.setOnClickListener(v ->
                                    //Add the existing instance on the given name
                                    editAndAdd(channel1Data, instanceNameEditText.getText().toString(), new addInstanceCallback() {
                                        @Override
                                        public void onAddingNewInstance(List<String> list, CredentialsData credentialsData) {
                                            saveDataSharedPreferences(credentialsData);
                                            sqlLocalDataCallback.onInstanceAdded(list, credentialsData);
                                            instanceSaveDialog.dismiss();
                                            channel1Dialog.dismiss();
                                        }

                                        @Override
                                        public void onError(String error) {
                                            sqlLocalDataCallback.onError(error);
                                        }
                                    }));
                            negBtnInstanceSave.setOnClickListener(v -> instanceSaveDialog.dismiss());
                        }

                        @Override
                        public void onError(String error) {
                            sqlLocalDataCallback.onError(error);
                        }
                    }));

            channel2ModifyBtn.setOnClickListener(v -> channel2DialogBuilder(channel2ModifyBtn));
        });
        builder.setNegativeButton("No", (dialogInterface, i) -> sqlLocalDataCallback.onError("Operation has been cancelled"));
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Dialog builder for Channel 2
     */
    private void channel2DialogBuilder(Button modifyButton) {
        AlertDialog.Builder channel2Builder = new AlertDialog.Builder(activity).setCancelable(false);
        //Set the icon for the alert dialog
        channel2Builder.setIcon(R.drawable.dynam);
        channel2Builder.setTitle("SQL configuration");
        channel2Builder.setMessage("Enter the SQL Config");
        View channel2View = activity.getLayoutInflater().inflate(R.layout.channel2layout, null);
        channel2Builder.setView(channel2View).setPositiveButton("Save", null).
                setNegativeButton("Cancel", null);
        EditText host2EditText = channel2View.findViewById(R.id.host2EditText);
        EditText port2EditText = channel2View.findViewById(R.id.port2EditText);
        EditText username2EditText = channel2View.findViewById(R.id.username2EditText);
        EditText pwd2EditText = channel2View.findViewById(R.id.pwd2EditText);
        EditText dbName2EditText = channel2View.findViewById(R.id.dbName2EditText);
        EditText tableName2EditText = channel2View.findViewById(R.id.tableName2EditText);
        AlertDialog channel2Dialog = channel2Builder.create();
        channel2Dialog.show();

        //Load the SQL saved data from shared preferences
        host2EditText.setText(getChannel2Data().host2);
        port2EditText.setText(getChannel2Data().port2);
        username2EditText.setText(getChannel2Data().username2);
        pwd2EditText.setText(getChannel2Data().password2);
        dbName2EditText.setText(getChannel2Data().database2);
        tableName2EditText.setText(getChannel2Data().tablename2);

        Button posBtnChannel2 = channel2Dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negBtnChannel2 = channel2Dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        posBtnChannel2.setOnClickListener(v -> checkChannel2(host2EditText, port2EditText, username2EditText,
                pwd2EditText, dbName2EditText, tableName2EditText, new checkChannel2Callback() {
                    @Override
                    public void onResult(String host, String port, String username, String password, String dbname, String tablename) {
                        putChannel2Data(host, port, username, password, dbname, tablename);
                        modifyButton.setText(R.string.modifySecondChannel);
                        channel2Dialog.dismiss();
                    }

                    @Override
                    public void onError(String error) {
                        sqlLocalDataCallback.onError(error);
                    }
                }));
        negBtnChannel2.setOnClickListener(v -> channel2Dialog.dismiss());
    }

    /**
     * Dialog builder for deleting the instance
     */
    public void deleteDialogBuilder(Spinner spinner) {
        Builder builder = new Builder(this.activity).setCancelable(true);
        builder.setIcon(R.drawable.dynam);
        builder.setTitle("Delete this instance");
        builder.setMessage("Do you want to delete the currently selected instance?");

        builder.setPositiveButton("Yes", (dialog, option) -> {
            int count = spinner.getCount();
            // int delete = spinner.getSelectedItemPosition();
            String selectedItem = spinner.getSelectedItem().toString();
            if (count <= 1) {
                sqlLocalDataCallback.onError("Cannot delete this instance. There should be atleast one instance");
                return;
            }

            if (deleteInstance(selectedItem)) {
                sqlLocalDataCallback.onError("Deleted the instance");
            } else {
                sqlLocalDataCallback.onError("Problems while deleting the instance");
            }
        });

        builder.setNegativeButton("No", (dialog, option) -> sqlLocalDataCallback.onError("The operation has been cancelled"));
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Dialog builder for restoring the default values
     */
    public void restoreDefaults(List<String> list) {
        Builder builder = new Builder(activity, R.style.Theme_AppCompat_Dialog_Alert).setCancelable(true);
        builder.setIcon(R.drawable.dynam);
        builder.setTitle("Restore to Defaults");
        builder.setMessage("Are you sure you want to restore the default values?");

        builder.setPositiveButton("Yes", (dialog, option) -> {
            list.clear();
            if (deleteAllEntriesSQL()) {
                sqlLocalDataCallback.onDefaultsRestored("Defaults Restored");
            } else {
                sqlLocalDataCallback.onError("Problems while restoring the defaults");
            }
        });

        builder.setNegativeButton("No", (dialog, option) -> sqlLocalDataCallback.onError("The operation has been cancelled"));
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Delete all entries from SQLite database
     */
    private boolean deleteAllEntriesSQL() {
        this.sqLiteDatabase = this.activity.openOrCreateDatabase("configDynco.db", MODE_PRIVATE, null);
        try (Cursor query = sqLiteDatabase.rawQuery("DELETE FROM " + this.tableName, null)) {
            //Do nothing
            return !query.moveToFirst();
        }
    }

    /**
     * Fill the SQLite database with default data
     */
    public void getDefaultData(boolean adminMode) {
        String tableName = this.tableName;
        //String tableName;
        this.sqLiteDatabase = this.activity.openOrCreateDatabase("configDynco.db", MODE_PRIVATE, null);
        //configDatabase.execSQL("DROP TABLE IF EXISTS config");
        this.sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + tableName + "(ip VARCHAR,port VARCHAR,username VARCHAR,userid VARCHAR," +
                "password VARCHAR, instancetype VARCHAR,configname VARCHAR, enablesql VARCHAR, sqlip VARCHAR,sqlport VARCHAR," +
                "sqlusername sqlVARCHAR,sqlpassword VARCHAR,sqldb VARCHAR, sqltable VARCHAR)");
        Cursor count = this.sqLiteDatabase.rawQuery("SELECT COUNT(*) from " + tableName, null);
        count.moveToFirst();
        int count_data = count.getInt(0);
        count.close();
        if (count_data == 0) {
            if (adminMode) {
                String instancetype = "dbsno";
                String configname = "DB-SNO Pro";
                String enablesql = "disable";
                String sql = "INSERT into " + tableName + "(ip,port,username,userid,password,instancetype,configname,enablesql,sqlip," +
                        "sqlport,sqlusername,sqlpassword,sqldb,sqltable) VALUES ('" + DEF_HOST_1_DBSNO_PRO + "','" + DEF_PORT_1 + "','" + DEF_USERNAME_1 + "','" +
                        DEF_USERID_1 + "','" + DEF_PASSWORD_1 + "','" + instancetype + "','" + configname + "','" + enablesql + "','" + DEF_HOST_2
                        + "','" + DEF_PORT_2 + "','" + DEF_USERNAME_2 + "','" + DEF_PASSWORD_2 + "','" + DEF_DATABASE_2 + "','" + DEF_TABLE_2_DBSNO + "')";
                this.sqLiteDatabase.execSQL(sql);
                instancetype = "dbsno";
                configname = "DB-SNO internal";
                enablesql = "disable";
                sql = "INSERT into " + tableName + "(ip,port,username,userid,password,instancetype,configname,enablesql,sqlip," +
                        "sqlport,sqlusername,sqlpassword,sqldb,sqltable) VALUES ('" + DEF_HOST_1_DBSNO_INTERNAL + "','" + DEF_PORT_1 + "','" + DEF_USERNAME_1 + "','" +
                        DEF_USERID_1 + "','" + DEF_PASSWORD_1 + "','" + instancetype + "','" + configname + "','" + enablesql + "','" + DEF_HOST_2
                        + "','" + DEF_PORT_2 + "','" + DEF_USERNAME_2 + "','" + DEF_PASSWORD_2 + "','" + DEF_DATABASE_2 + "','" + DEF_TABLE_2_DBSNO + "')";
                this.sqLiteDatabase.execSQL(sql);
                instancetype = "ivf";
                configname = "IVF pro";
                enablesql = "disable";
                sql = "INSERT into " + tableName + "(ip,port,username,userid,password,instancetype,configname,enablesql,sqlip," +
                        "sqlport,sqlusername,sqlpassword,sqldb,sqltable) VALUES ('" + DEF_HOST_1_IVF_PRO + "','" + DEF_PORT_1 + "','" + DEF_USERNAME_1 + "','" +
                        DEF_USERID_1 + "','" + DEF_PASSWORD_1 + "','" + instancetype + "','" + configname + "','" + enablesql + "','" + DEF_HOST_2
                        + "','" + DEF_PORT_2 + "','" + DEF_USERNAME_2 + "','" + DEF_PASSWORD_2 + "','" + DEF_DATABASE_2 + "','" + DEF_TABLE_2_IVF + "')";
                this.sqLiteDatabase.execSQL(sql);
                instancetype = "ivf";
                configname = "IVF internal";
                enablesql = "disable";
                sql = "INSERT into " + tableName + "(ip,port,username,userid,password,instancetype,configname,enablesql,sqlip," +
                        "sqlport,sqlusername,sqlpassword,sqldb,sqltable) VALUES ('" + DEF_HOST_1_IVF_INTERNAL + "','" + DEF_PORT_1 + "','" + DEF_USERNAME_1 + "','" +
                        DEF_USERID_1 + "','" + DEF_PASSWORD_1 + "','" + instancetype + "','" + configname + "','" + enablesql + "','" + DEF_HOST_2
                        + "','" + DEF_PORT_2 + "','" + DEF_USERNAME_2 + "','" + DEF_PASSWORD_2 + "','" + DEF_DATABASE_2 + "','" + DEF_TABLE_2_IVF + "')";
                this.sqLiteDatabase.execSQL(sql);
                instancetype = "ivfSno";
                configname = "SNO Smart Stamp";
                enablesql = "disable";
                sql = "INSERT into " + tableName + "(ip,port,username,userid,password,instancetype,configname,enablesql,sqlip," +
                        "sqlport,sqlusername,sqlpassword,sqldb,sqltable) VALUES ('" + DEF_HOST_1_IVF_SNO + "','" + DEF_PORT_1 + "','" + DEF_USERNAME_1 + "','" +
                        DEF_USERID_1 + "','" + DEF_PASSWORD_1 + "','" + instancetype + "','" + configname + "','" + enablesql + "','" + DEF_HOST_2
                        + "','" + DEF_PORT_2 + "','" + DEF_USERNAME_2 + "','" + DEF_PASSWORD_2 + "','" + DEF_DATABASE_2 + "','" + DEF_TABLE_2_IVF + "')";
                this.sqLiteDatabase.execSQL(sql);
            } else {
                String instancetype = "ivf";
                String configname = "IVF pro";
                String enablesql = "disable";
                String sql = "INSERT into " + tableName + "(ip,port,username,userid,password,instancetype,configname,enablesql,sqlip," +
                        "sqlport,sqlusername,sqlpassword,sqldb,sqltable) VALUES ('" + DEF_HOST_1_IVF_PRO + "','" + DEF_PORT_1 + "','" + DEF_USERNAME_1 + "','" +
                        DEF_USERID_1 + "','" + DEF_PASSWORD_1 + "','" + instancetype + "','" + configname + "','" + enablesql + "','" + DEF_HOST_2
                        + "','" + DEF_PORT_2 + "','" + DEF_USERNAME_2 + "','" + DEF_PASSWORD_2 + "','" + DEF_DATABASE_2 + "','" + DEF_TABLE_2_IVF + "')";
                this.sqLiteDatabase.execSQL(sql);
            }
        }
        try (Cursor query = this.sqLiteDatabase.rawQuery("SELECT * FROM " + tableName, null)) {
            if (query.moveToFirst()) {
                do {
                    String name = query.getString(6);
                    //String enable = query.getString(7);
                    this.sqlList.add(name);
                } while (query.moveToNext());
            }
            if (!query.moveToFirst()) {
                this.sqlLocalDataCallback.onError("Empty DB");
                return;
            }
            this.sqlLocalDataCallback.onGettingDefaultData(this.sqlList);
        }
    }

    /**
     * Check the credentials of channel 1
     */
    private void checkChannel1(EditText host1ET, EditText port1ET, EditText username1ET, EditText userid1ET, EditText password1ET,
                               checkChannel1Callback checkChannel1Callback) {
        String host1 = host1ET.getText().toString();
        String port1 = port1ET.getText().toString();
        String username1 = username1ET.getText().toString();
        String userid1 = userid1ET.getText().toString();
        String password1 = password1ET.getText().toString();

        if (!(!host1.isEmpty() & !port1.isEmpty() & !username1.isEmpty() & !userid1.isEmpty() & !password1.isEmpty())) {
            checkChannel1Callback.onError("Enter the details and proceed...");
            return;
        }

        if (port1.length() >= 5) {
            checkChannel1Callback.onError("The value of port cannot exceed more than 5 characters.. Try with different value for port");
            return;
        }
        checkChannel1Callback.onResult(host1, port1, username1, userid1, password1);
    }

    private void checkChannel2(EditText host2ET, EditText port2ET, EditText username2ET, EditText password2ET,
                               EditText dbname2ET, EditText tablename2ET, checkChannel2Callback checkChannel2Callback) {
        String host2 = host2ET.getText().toString();
        String port2 = port2ET.getText().toString();
        String username2 = username2ET.getText().toString();
        String password2 = password2ET.getText().toString();
        String dbname2 = dbname2ET.getText().toString();
        String tablename2 = tablename2ET.getText().toString();

        if (!(!host2.isEmpty() & !port2.isEmpty() & !username2.isEmpty() & !password2.isEmpty() & !dbname2.isEmpty() & !tablename2.isEmpty())) {
            checkChannel2Callback.onError("Some fields are empty");
            return;
        }
        if (port2.length() >= 5) {
            checkChannel2Callback.onError("The value of port cannot exceed more than 5 characters.. Try with different value for port");
            return;
        }
        checkChannel2Callback.onResult(host2, port2, username2, password2, dbname2, tablename2);
    }

    /**
     * Add the instance on new name
     * If the newInstanceName is provided as null, then rewrite the already existing instance
     */
    private void editAndAdd(Channel1Data channel1Data, String newInstanceName, addInstanceCallback addInstanceCallback) {
        if (newInstanceName != null) {
            if (newInstanceName.isEmpty()) {
                addInstanceCallback.onError("Enter the details and proceed...");
                return;
            }

            if (!checkInstanceNameInSQL(newInstanceName)) {
                addInstanceCallback.onError("Instance Name already exists in DB. Try different name");
                return;
            }

            if (channel1Data.instanceName1 != null) {
                if (!deleteFromDatabase(channel1Data.instanceName1)) {
                    addInstanceCallback.onError("Problems in deleting the instance");
                    return;
                }
            }
        } else {
            if (!deleteFromDatabase(channel1Data.instanceName1)) {
                addInstanceCallback.onError("Problems in deleting the instance");
                return;
            }
        }
        Channel2Data channel2Data = getChannel2Data();
        if (!addNewInstance(channel1Data.host1, channel1Data.port1, channel1Data.username1, channel1Data.userid1,
                channel1Data.password1, channel1Data.instanceType1, newInstanceName != null ? newInstanceName : channel1Data.instanceName1,
                channel1Data.channelEnable2, channel2Data.host2, channel2Data.port2, channel2Data.username2, channel2Data.password2,
                channel2Data.database2, channel2Data.tablename2)) {
            addInstanceCallback.onError("Problems in creating the instance");
            return;
        }

        CredentialsData credentialsData = CredentialsData.builder().host1(channel1Data.host1).
                port1(channel1Data.port1).username1(channel1Data.username1).userid1(channel1Data.userid1).
                password1(channel1Data.password1).instanceType(channel1Data.instanceType1).sqlEnable1(channel1Data.channelEnable2).
                currentInstance(newInstanceName != null ? newInstanceName : channel1Data.instanceName1).
                host2(channel2Data.host2).port2(channel2Data.port2).username2(channel2Data.username2)
                .password2(channel2Data.password2).table2(channel2Data.database2).database2(channel2Data.tablename2).build();

        addInstanceCallback.onAddingNewInstance(this.sqlList, credentialsData);
    }

    /**
     * Check if instance name is already there in SQLite DB
     */
    private boolean checkInstanceNameInSQL(String instanceName) {
        String tableName = this.tableName;
        String sqlCommand = "SELECT * FROM " + tableName + " where configname like " + "'" + instanceName + "'";
        try (Cursor query = sqLiteDatabase.rawQuery(sqlCommand, null)) {
            if (query.moveToFirst()) {
                do {
                    String name = query.getString(6);
                    //sqlList.add(name);
                } while (query.moveToNext());
                return false;
            } else {
                return true;
            }
        }
    }

    /**
     * Delete instance from the SQlite database
     */
    private boolean deleteFromDatabase(String instanceName) {
        String tableName = this.tableName;
        String sql = "DELETE from " + tableName + " where configname like " + "'" + instanceName + "'";
        sqLiteDatabase.execSQL(sql);
        try (Cursor query = sqLiteDatabase.rawQuery("SELECT * FROM " + tableName, null)) {
            if (query.moveToFirst()) {
                sqlList.clear();
                do {
                    String name = query.getString(6);
                    sqlList.add(name);
                } while (query.moveToNext());
            }
        }
        return true;
    }

    /**
     * Delete instance from SQLite database
     */
    private boolean deleteInstance(String instanceName) {
        String tableName = this.tableName;
        String sql = "DELETE from " + tableName + " where configname like " + "'" + instanceName + "'";
        sqLiteDatabase.execSQL(sql);
        try (Cursor query = sqLiteDatabase.rawQuery("SELECT * FROM " + tableName, null)) {
            if (query.moveToFirst()) {
                sqlList.clear();
                do {
                    String name = query.getString(6);
                    sqlList.add(name);
                } while (query.moveToNext());
            }
            sqlLocalDataCallback.onDeletingInstance(sqlList);
        }
        return true;
    }

    /**
     * Add new instance to the SQlite database
     */
    private boolean addNewInstance(String hostIp, String port, String username, String userID, String password,
                                   String instanceType, String instanceName, boolean sqlEnable, String sqlip,
                                   String sqlport, String sqluser, String sqlpwd, String sqldb, String sqltable) {
        String sqlEnableOption;
        String tableName = this.tableName;
        if (sqlEnable) {
            sqlEnableOption = "enable";
        } else {
            sqlEnableOption = "disable";
        }
        String sql = "INSERT into " + tableName + "(ip,port,username,userid,password,instancetype,configname,enablesql,sqlip,sqlport,sqlusername,sqlpassword,sqldb,sqltable) " +
                "VALUES ('" + hostIp + "','" + port + "','" + username + "','" +
                userID + "','" + password + "','" + instanceType + "','" + instanceName + "','" + sqlEnableOption
                + "','" + sqlip + "','" + sqlport + "','" + sqluser + "','" + sqlpwd + "','" + sqldb + "','" + sqltable + "')";
        sqLiteDatabase.execSQL(sql);
        try (Cursor query = sqLiteDatabase.rawQuery("SELECT * FROM " + tableName, null)) {
            if (query.moveToFirst()) {
                sqlList.clear();
                do {
                    String name = query.getString(6);
                    sqlList.add(name);
                } while (query.moveToNext());
            }
        }
        return true;
    }

    /**
     * Getting all the values for a current instance
     */
    public void getInputData(String mddiInstance) {
        //String tableName = activity.getString(R.string.TableNameAdmin);
        boolean sqlEnable = false;
        String sqlCommand = "SELECT * FROM " + this.tableName + " where configname like " + "'" + mddiInstance + "'";
        try (Cursor query = sqLiteDatabase.rawQuery(sqlCommand, null)) {
            if (query.moveToFirst()) {
                String host1 = query.getString(0);
                String port1 = query.getString(1);
                String username1 = query.getString(2);
                String userid1 = query.getString(3);
                String password1 = query.getString(4);
                String instanceType = query.getString(5);
                String instanceName = query.getString(6);
                String sqlEnableState = query.getString(7);
                String host2 = query.getString(8);
                String port2 = query.getString(9);
                String username2 = query.getString(10);
                String password2 = query.getString(11);
                String database2 = query.getString(12);
                String table2 = query.getString(13);
                if (sqlEnableState.equals("enable")) {
                    sqlEnable = true;
                }

                CredentialsData credentialsData = CredentialsData.builder().host1(host1).port1(port1)
                        .username1(username1).userid1(userid1).password1(password1).instanceType(instanceType)
                        .sqlEnable1(sqlEnable).currentInstance(instanceName).host2(host2).port2(port2).username2(username2)
                        .password2(password2).table2(table2).database2(database2).build();

                saveDataSharedPreferences(credentialsData);
                sqlLocalDataCallback.onGettingInputData(credentialsData);
                //Make the boolean true to denote the selection of barcode option
            } else {
                sqlLocalDataCallback.onError("Empty DB...");
            }
        }
    }

    /**
     * Check whether the selected instance is SNO instance or not
     */
    public boolean snoInstanceOrNot(String sqlInstanceName) {
        boolean condition = false;
        String sqlCommand = "SELECT * FROM " + this.tableName + " where configname like " + "'" + sqlInstanceName + "'";
        try (Cursor query = this.sqLiteDatabase.rawQuery(sqlCommand, null)) {
            if (query.moveToFirst()) {
                String instanceType = query.getString(5);
                //Make the boolean true to denote the selection of barcode option
                if (instanceType.equals("dbsno") || instanceType.equals("ivfSno")) {
                    condition = true;
                }
            }
            return condition;
        }
    }

    /**
     * Get channel 1 data
     */
    private Channel1Data getChannel1Data() {
        String host1 = this.sharedPreferences.getString(HOST_1, DEF_HOST_1_DBSNO_PRO);
        String port1 = this.sharedPreferences.getString(PORT_1, DEF_PORT_1);
        String username1 = this.sharedPreferences.getString(USERNAME_1, DEF_USERNAME_1);
        String userid1 = this.sharedPreferences.getString(USERID_1, DEF_USERID_1);
        String password1 = this.sharedPreferences.getString(PASSWORD_1, DEF_PASSWORD_1);
        String instanceType1 = sharedPreferences.getString(INSTANCE_TYPE_1, DEF_INSTANCE_TYPE_1);
        boolean channelEnable2 = this.sharedPreferences.getBoolean(CHANNEL_ENABLE_2, DEF_CHANNEL_ENABLE_2);
        return new Channel1Data(host1, port1, username1, userid1, password1, instanceType1, null, channelEnable2);
    }

    /**
     * Get channel 2 data
     */
    private Channel2Data getChannel2Data() {
        String host2 = this.sharedPreferences.getString(HOST_2, DEF_HOST_2);
        String port2 = this.sharedPreferences.getString(PORT_2, DEF_PORT_2);
        String username2 = this.sharedPreferences.getString(USERNAME_2, DEF_USERNAME_2);
        String password2 = this.sharedPreferences.getString(PASSWORD_2, DEF_PASSWORD_2);
        String dbname2 = this.sharedPreferences.getString(DATABASE_2, DEF_DATABASE_2);
        String tablename2 = this.sharedPreferences.getString(TABLE_2, DEF_TABLE_2_DBSNO);
        return new Channel2Data(host2, port2, username2, password2, dbname2, tablename2);
    }

    /**
     * Get channel 2
     */
    private void putChannel2Data(String host, String port, String username, String password, String dbname, String tablename) {
        //Store the values in shared preferences
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = this.sharedPreferences.edit();
        //Save the credentials and configuration data to shared preferences
        editor.putString(HOST_2, host);
        editor.putString(PORT_2, port);
        editor.putString(USERNAME_2, username);
        editor.putString(PASSWORD_2, password);
        editor.putString(DATABASE_2, dbname);
        editor.putString(TABLE_2, tablename);
        editor.apply();
    }

    /**
     * Save the data in shared preferences
     */
    protected void saveDataSharedPreferences(CredentialsData credentialsData) {
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = this.sharedPreferences.edit();
        //Save the credentials and configuration data to shared preferences
        editor.putString(HOST_1, credentialsData.host1);
        editor.putString(PORT_1, credentialsData.port1);
        editor.putString(USERNAME_1, credentialsData.username1);
        editor.putString(PASSWORD_1, credentialsData.password1);
        editor.putString(USERID_1, credentialsData.userid1);
        editor.putString(INSTANCE_TYPE_1, credentialsData.instanceType1);
        editor.putBoolean(CHANNEL_ENABLE_2, credentialsData.sqlEnable1);
        editor.putString(HOST_2, credentialsData.host2);
        editor.putString(PORT_2, credentialsData.port2);
        editor.putString(USERNAME_2, credentialsData.username2);
        editor.putString(PASSWORD_2, credentialsData.password2);
        editor.putString(DATABASE_2, credentialsData.database2);
        editor.putString(TABLE_2, credentialsData.table2);
        editor.apply();
    }
}
