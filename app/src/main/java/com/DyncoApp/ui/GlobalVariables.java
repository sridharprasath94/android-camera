package com.DyncoApp.ui;

import android.app.Application;
import android.os.Environment;

import com.mddi.mddiclient.ClientService;
import com.mddi.misc.InstanceType;

import java.sql.Connection;

public class GlobalVariables extends Application {
    ClientService clientService;
    com.mddiv1.mddiclient.ClientService clientServiceV1;
    Boolean versionOneSelected;
    SelectedUserMode selectedUserMode = SelectedUserMode.READONLY;
    instanceMode currentInstanceMode = instanceMode.IVF;
    String model;
    Boolean toggleFlash = true;
    Boolean showScore = true;

    int width = 480;
    int height = 640;

    String picDir = Environment.DIRECTORY_PICTURES + "/" + "CrossFunctionality" + "/" + "IVFDatasets";

    String userName;
    String userId;
    String password;
    String userCid;
    String userSno;

    boolean createCollection = false;
    Connection sqlConnection = null;
    String sqlUrl;
    String sqlUser;
    String sqlPwd;
    String sqlTable;

    InstanceType currentInstance;
    String sqlCid = "";
    boolean sqlDbEnabled = false;
}

enum instanceMode{IVF,
    DB_SNO};