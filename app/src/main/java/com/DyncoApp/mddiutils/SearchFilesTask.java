package com.DyncoApp.mddiutils;

import static com.DyncoApp.mddiutils.MddiUtils.assignCidSno;
import static com.mddi.misc.MddiConstants.TIME_DELAY;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.mddi.exceptions.ClientException;
import com.mddi.exceptions.ExceptionType;
import com.mddi.mddiclient.ClientService;
import com.mddi.misc.InstanceType;
import com.mddi.search.SearchCallBack;
import com.mddi.search.SearchTask;

import java.io.File;

import lombok.Builder;

/**
 * Search_FromFiles_Task.
 */
@RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
public class SearchFilesTask extends SearchTask {

    @Builder
    @RequiresApi(api = Build.VERSION_CODES.O)
    public SearchFilesTask(@NonNull InstanceType instanceType,
                           @NonNull ClientService clientService,
                           @NonNull File searchFolder,
                           @NonNull SearchCallBack searchCallback) {
        super(clientService, searchFolder, searchCallback);
        try {
            CheckFiles.builder().currentFolder(searchFolder).instanceType(instanceType).checkFilesCallback(new CheckFilesCallback() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onCorrectFormat(File[] jpgCallbackFiles, File[] txtCallbackFiles) {
                    setFiniteSearch(jpgCallbackFiles.length);
                    searchTask(jpgCallbackFiles, txtCallbackFiles);
                }

                @Override
                public void onError(ExceptionType type, Exception e) {
                    searchCallback.onError(type, e);
                    stopSearch();
                }
            }).build();
        } catch (ClientException e) {
            this.searchCallback.onError(ExceptionType.CLIENT_EXCEPTION, new Exception(e.getMessage()));
            stopSearch();
        }
    }

    ////////////////////////// Task to search the images //////////////////////////
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void searchTask(File[] jpgFiles, File[] txtFiles) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                for (File file : jpgFiles) {
                    if (searchStopped) {
                        return;
                    }
                    try {
                        Thread.sleep(TIME_DELAY);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (file == jpgFiles[0]) {
                        setFiniteSearch(jpgFiles.length);
                    }

                    Log.d("File", file.getName());
                    // Assigning CID and SNO to the particular'.jpg' image(from the '.txt' file)
                    MddiUtils.CidSnoResult cidSnoResult = assignCidSno(file, txtFiles, instanceType);
                    searchNextImageFile(file, cidSnoResult.cid, cidSnoResult.sno);
                }
            }
        });
    }
}
