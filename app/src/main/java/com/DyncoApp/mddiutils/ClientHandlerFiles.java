package com.DyncoApp.mddiutils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;


import java.io.File;
import java.util.Objects;
import java.util.stream.Stream;

import com.dynamicelement.mddi.StreamImage;
import com.dynamicelement.sdk.android.add.AddCallback;
import com.dynamicelement.sdk.android.exceptions.ClientException;
import com.dynamicelement.sdk.android.mddiclient.ClientService;
import com.dynamicelement.sdk.android.mddiclient.MddiData;
import com.dynamicelement.sdk.android.misc.InstanceType;
import com.dynamicelement.sdk.android.search.SearchCallBack;

public class ClientHandlerFiles {
    protected ClientService clientService;
    protected Context context;
    protected String[] collectionInfo = new String[5];
    protected InstanceType instanceType;
    protected boolean searchTaskCreated = false;
    protected boolean addTaskCreated = false;
    protected MddiData mddiData;
    protected SearchFilesTask searchFilesTask;
    protected AddFilesTask addFilesTask;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////// Creating mddi client//////////////////////////
    @RequiresApi(api = Build.VERSION_CODES.N)
    public ClientHandlerFiles(@NonNull ClientService clientService, InstanceType instanceType, @NonNull Context context) throws ClientException {
        this.clientService = clientService;
        this.context = context;
        this.instanceType = instanceType;

        if (this.instanceType != InstanceType.DB_SNO && this.instanceType != InstanceType.IVF) {
            throw new ClientException("Wrong instance type");
        }

        if (Stream.of(this.clientService, this.context).anyMatch(Objects::isNull)) {
            throw new ClientException("Some required parameters are not provided correctly. They are null");
        }
    }

    public boolean isSearchTaskCreated() {
        return searchTaskCreated;
    }

    public void setSearchTaskCreated(boolean searchTaskCreated) {
        this.searchTaskCreated = searchTaskCreated;
    }

    public void resetVerifyTaskCreated() {
        this.searchTaskCreated = false;
    }

    public boolean isAddTaskCreated() {
        return addTaskCreated;
    }

    public void setAddTaskCreated(boolean addTaskCreated) {
        this.addTaskCreated = addTaskCreated;
    }

    public void resetAddTaskCreated() {
        this.addTaskCreated = false;
    }

//    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//    ////////////////////////// Creating the mddi data//////////////////////////
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    public void createMDDIData(Bitmap bitmap, int width, int height, String defaultCID, String defaultSNO, boolean barcodeMode, MddiData.MddiDataCallback mddiCallback) throws Exception {
//        //Initialise the mddi_process function for the camera image
//        new MddiData(bitmap, width, height, instanceType, defaultCID, defaultSNO, true, true, new MddiData.MddiDataCallback() {
//            @Override
//            public void onDBSNO(String barcodeResult, String mddiCid, String mddiSno,
//                                StreamImage mddiStreamImage, Bitmap centerCroppedBitmap) {
//                mddiCallback.onDBSNO(barcodeResult, mddiCid, mddiSno, mddiStreamImage, centerCroppedBitmap);
//            }
//
//            @Override
//            public void onIVF(String mddiCid, String mddiSno, StreamImage mddiImage, Bitmap centerCroppedBitmap) {
//                mddiCallback.onIVF(mddiCid, mddiSno, mddiImage, centerCroppedBitmap);
//            }
//        }, "dev");
//    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////// Search from files//////////////////////////
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void searchFromFiles(File searchFolder, SearchCallBack searchCallBack) throws ClientException {
        if (searchCallBack != null) {
            searchFilesTask = SearchFilesTask.builder().clientService(this.clientService).instanceType(this.instanceType).
                    searchFolder(searchFolder).searchCallback(searchCallBack).build();

            this.setSearchTaskCreated(true);
        } else {
            throw new ClientException("MDDI Client : Search callback cannot be null");
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////// Stop the search process//////////////////////////
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void stopSearch() {
        if (this.searchTaskCreated) {
            this.resetVerifyTaskCreated();
            this.searchFilesTask.stopSearch();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////// Add from files//////////////////////////
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void addFromFiles(File addFolder, boolean createCollection, AddCallback addCallback) throws Exception {
        addFilesTask = AddFilesTask.builder().clientService(this.clientService).addFolder(addFolder).
                instanceType(this.instanceType).createNewCollection(createCollection).addCallback(addCallback).build();

        this.setAddTaskCreated(true);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////// Stop the add process//////////////////////////
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void stopAdd() {
        if (this.isAddTaskCreated()) {
            this.resetAddTaskCreated();
            this.addFilesTask.stopAdd();
        }
    }
}
