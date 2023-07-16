package com.DyncoApp.ui;

import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.getBytesFromBitmap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.DyncoApp.R;
import com.DyncoApp.ui.cameraScan.CameraScanScreen;
import com.DyncoApp.ui.common.Constants;
import com.dynamicelement.mddi.AddStreamResponse;
import com.dynamicelement.sdk.android.Callback;
import com.dynamicelement.sdk.android.add.AddCallback;
import com.dynamicelement.sdk.android.add.AddImageStatus;
import com.dynamicelement.sdk.android.add.AddResult;
import com.dynamicelement.sdk.android.collection.CollectionResult;
import com.dynamicelement.sdk.android.exceptions.ExceptionType;
import com.dynamicelement.sdk.android.mddiclient.ClientService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Backup {

    private void saveImage(Bitmap bitmap, File directoryPath, String sno, String fileName) {
        FileOutputStream streamImage = null;
        byte[] imageBytes = getBytesFromBitmap(bitmap);

        File destPath = new File(directoryPath.getPath());
        if (!destPath.exists()) {
            destPath.mkdirs();
        }
        File imageFile = new File(destPath, (fileName + ".jpg"));

        try {
            streamImage = new FileOutputStream(imageFile);
            streamImage.write(imageBytes);
            Log.d("Image", "Image saved successfully");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Image", "Image saving error" + e.getMessage());
        } finally {
            if (streamImage != null) {
                try {
                    streamImage.close();
                    Log.d("Image", "Image closed");
                } catch (IOException e) {
                    Log.d("Image", "Image not closed");
                    e.printStackTrace();
                }
            }
        }
    }

//    /**
//     * Enable the layout items
//     */
//    private void enableLayoutItems() {
//        flashButton.setVisibility(View.VISIBLE);
//        zoomAdjustButton.setVisibility(View.VISIBLE);
//        overlayButton.setVisibility(View.VISIBLE);
//        backButton.setVisibility(View.VISIBLE);
//    }
//
//    /**
//     * Disable the layout items
//     */
//    private void disableLayoutItems() {
//        flashButton.setVisibility(View.INVISIBLE);
//        zoomAdjustButton.setVisibility(View.INVISIBLE);
//        overlayButton.setVisibility(View.INVISIBLE);
//        backButton.setVisibility(View.INVISIBLE);
//        zoomAdjustButton.setImageResource(R.drawable.ic_zoom);
//    }
    private void saveImageLocally(Bitmap bitmap, File directoryPath, String fileNameStart, String fileNameSecondHalf) {
        FileOutputStream streamImage = null;
        byte[] imageBytes = getBytesFromBitmap(bitmap);

        File destPath = new File(directoryPath.getPath() + "/" + fileNameStart);
        if (!destPath.exists()) {
            destPath.mkdirs();
        }
        SimpleDateFormat s = new SimpleDateFormat("hhmmss");
        String format = s.format(new Date());
        File imageFile = new File(destPath,
                (fileNameStart + "_" + fileNameSecondHalf + "_" + format + ".jpg"));

        try {
            streamImage = new FileOutputStream(imageFile);
            streamImage.write(imageBytes);
            Log.d("Image", "Image saved successfully..");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Image", "Image saving error" + e.getMessage());
        } finally {
            if (streamImage != null) {
                try {
                    streamImage.close();
                    Log.d("Image", "Image closed");
                } catch (IOException e) {
                    Log.d("Image", "Image not closed");
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean removeDirectory(File inputFolder) {

        boolean[] deleteSubFolders;
        boolean[] deleteFiles = new boolean[0];
        boolean deleteFolder;

        //If the folder exists
        if (!inputFolder.exists()) {
            return false;
        }
        //Get the list of files from the folder
        File[] inputFiles = inputFolder.listFiles();
        if (inputFiles == null) {
            return false;
        }
        deleteSubFolders = new boolean[inputFiles.length];
        for (File file : inputFiles) {
            if (file.isDirectory()) {
                String[] children = file.list();
                assert children != null;
                for (String child : children) {
                    deleteFiles = new boolean[children.length];
                    Arrays.fill(deleteFiles, new File(file, child).delete());
                }
            }
            Arrays.fill(deleteSubFolders, file.delete());
        }
        deleteFolder = inputFolder.delete();
        return deleteFolder && areAllTrue(deleteFiles) && areAllTrue(deleteSubFolders);
    }


    private static boolean areAllTrue(boolean[] array) {
        for (boolean b : array)
            if (!b)
                return false;
        return true;
    }
}


//
//                    double sharpness = getImageVariance(addBitmap,
//                            globalVariables.clientService.getMddiImageSize().getWidth(),
//                            globalVariables.clientService.getMddiImageSize().getHeight(),
//                            Bitmap.Config.ARGB_8888);
//                    if (sharpness < 40) {
//                        Toast.makeText(CameraScanScreen.this, "Not enough features. Add another image",
//                                Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    cameraview.stopMddiSearch();
//            Toast.makeText(this, "The sharpness is " + sharpness, Toast.LENGTH_SHORT).show();



//    /**
//     * Add task to add the image to the Mddi database
//     */
//    protected void add(ClientService clientService, Bitmap bitmap, String cid, boolean createCollection) {
//        if (createCollection) {
//            clientService.createCollection(bitmap, cid,
//                    cid,
//                    Constants.getCollectionInfo(), new Callback<CollectionResult>() {
//                        @Override
//                        public void onResponse(CollectionResult response) {
//                            if (response.isCollectionCreated()) {
//                                Log.d("MDDI_LOGIC_ADD_BITMAP",
//                                        String.valueOf(response.getResponse()));
//                                addImage(clientService, bitmap, cid);
//                            }
//                        }
//
//                        @Override
//                        public void onError(ExceptionType exceptionType, Exception e) {
//                            runOnUiThread(() -> Toast.makeText(CameraScanScreen.this, e.getMessage(), Toast.LENGTH_SHORT).show());
//                            onBackPressed();
//                        }
//                    });
//        } else {
//            addImage(clientService, bitmap, cid);
//        }
//    }
//
//    private void addImage(ClientService clientService, Bitmap bitmap, String cid) {
//        clientService.addImage(bitmap, cid,
//                cid,
//                false, new AddCallback() {
//                    @Override
//                    public void onNextResponse(AddStreamResponse addStreamResponse,
//                                               AddResult result) {
//                        if (result.getAddImageStatus() == AddImageStatus.SUCCESS) {
//                            runOnUiThread(() -> Toast.makeText(CameraScanScreen.this, "Added the " +
//                                            "image",
//                                    Toast.LENGTH_SHORT).show());
//
//                            Intent intent = new Intent(getApplicationContext(),
//                                    A4_ResultScreen.class);
//                            Bundle bundle = new Bundle();
//                            bundle.putString("Option", "Add");
//
//                            ByteArrayOutputStream bs = new ByteArrayOutputStream();
//                            bitmap.compress(Bitmap.CompressFormat.PNG, 50, bs);
//                            intent.putExtra("byteArray", bs.toByteArray()).putExtra("SNO",
//                                            cid).putExtra("CID", cid).
//                                    putExtras(bundle).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                            bitmap.recycle();
//                            startActivity(intent);
//                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
//                        } else if (result.getAddImageStatus() == AddImageStatus.DUPLICATE) {
//                            handleException(new Exception("Duplicate Image. Image already in the database"));
//                        } else if (result.getAddImageStatus() == AddImageStatus.ERROR) {
//                            handleException(new Exception("Error Image. Not enough features"));
//                        }
//                    }
//
//                    @Override
//                    public void onCompleted(String elapsedTime, String summaryMessage) {
//                    }
//
//                    @Override
//                    public void onError(ExceptionType exceptionType, Exception e) {
//                        handleException(e);
//                    }
//                });
//    }