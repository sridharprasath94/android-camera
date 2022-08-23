package com.DyncoApp.mddiutils;

import static com.DyncoApp.mddiutils.MddiUtils.assignCidSno;
import static com.mddi.misc.MddiConstants.MIN_HEIGHT;
import static com.mddi.misc.MddiConstants.MIN_WIDTH;
import static com.mddi.misc.MddiConstants.TIME_DELAY;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.mddi.Callback;
import com.mddi.add.AddCallback;
import com.mddi.add.AddTask;
import com.mddi.collection.CollectionResult;
import com.mddi.exceptions.ClientException;
import com.mddi.exceptions.ExceptionType;
import com.mddi.mddiclient.ClientService;
import com.mddi.misc.InstanceType;
import com.mddi.misc.MddiParameters;

import java.io.File;
import java.util.Objects;

import lombok.Builder;

/**
 * Add task for images - When the input images are in .jpeg file format
 */
@RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
public class AddFilesTask extends AddTask {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Builder
    public AddFilesTask(@NonNull InstanceType instanceType,
                        @NonNull ClientService clientService,
                        @NonNull Boolean createNewCollection,
                        @NonNull File addFolder,
                        @NonNull AddCallback addCallback) {

        super(clientService, addFolder, createNewCollection, addCallback);
        Objects.requireNonNull(addCallback);
        try {
            CheckFiles.builder().currentFolder(addFolder).instanceType(instanceType).checkFilesCallback(new CheckFilesCallback() {
                @Override
                public void onCorrectFormat(File[] jpgFiles, File[] txtFiles) {
                    setFiniteAdd(jpgFiles.length);
                    if (createNewCollection) {
                        //Get the Mat from the file
                        Bitmap collectionBitmap = MddiParameters.createResizedBitmapFromFile(jpgFiles[0].getPath(), MIN_WIDTH, MIN_HEIGHT, Bitmap.Config.ARGB_8888);

                        //Assign Cid and Sno
                        MddiUtils.CidSnoResult cidSnoResult = assignCidSno(jpgFiles[0], txtFiles, instanceType);

                        executeCreateCollectionTask(collectionBitmap, cidSnoResult.cid, cidSnoResult.sno, new Callback<CollectionResult>() {
                            @Override
                            public void onResponse(CollectionResult response) {
                                addTask(jpgFiles, txtFiles);
                            }

                            @Override
                            public void onError(ExceptionType exceptionType, Exception e) {
                                addCallback.onError(exceptionType, e);
                            }
                        });
                    } else {
                        addTask(jpgFiles, txtFiles);
                    }
                }

                @Override
                public void onError(ExceptionType type, Exception e) {
                    addCallback.onError(type, e);
                    stopAdd();
                }
            }).build();

        } catch (ClientException e) {
            this.addCallback.onError(ExceptionType.CLIENT_EXCEPTION, new Exception(e.getMessage()));
            e.printStackTrace();
            stopAdd();
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////// Runnable Task to add the images //////////////////////////

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void addTask(File[] jpgFiles, File[] txtFiles) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                for (File file : jpgFiles) {
                    if (addStopped) {
                        return;
                    }
                    try {
                        Thread.sleep(TIME_DELAY);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Assigning CID and SNO to the particular'.jpg' image(from the '.txt' file)
                    MddiUtils.CidSnoResult cidSnoResult = assignCidSno(file, txtFiles, instanceType);
                    addNextImageFile(file, cidSnoResult.cid, cidSnoResult.sno);
                }
            }
        });
    }
}
