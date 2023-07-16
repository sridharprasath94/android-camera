package com.dynamicelement.sdk.android.ui;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.buildBitmapFromMddiImage;
import static com.dynamicelement.sdk.android.mddiclient.MddiVariables.MddiImageSize.FORMAT_480X640;
import static com.dynamicelement.sdk.android.mddiclient.MddiVariables.MddiImageSize.FORMAT_512X512;
import static com.dynamicelement.sdk.android.misc.InstanceType.DB_SNO;
import static com.dynamicelement.sdk.android.misc.InstanceType.IVF;
import static com.dynamicelement.sdk.android.ui.CameraConstants.CameraMode.DEFAULT_CLIENT;
import static com.dynamicelement.sdk.android.ui.CameraConstants.CameraMode.WITHOUT_MDDI_CLIENT;
import static com.dynamicelement.sdk.android.ui.CameraConstants.CameraMode.WITH_MDDI_CLIENT;
import static com.dynamicelement.sdk.android.ui.CameraMddiSearch.SearchState.SEARCH_IS_STOPPED;
import static com.dynamicelement.sdk.android.ui.CameraMddiSearch.SearchState.SEARCH_YET_TO_START;
import static com.dynamicelement.sdk.android.ui.CameraParameters.CameraRatioMode.RATIO_1X1;
import static com.dynamicelement.sdk.android.ui.CameraParameters.CameraRatioMode.RATIO_3X4;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.dynamicelement.mddi.StreamImage;
import com.dynamicelement.sdk.android.exceptions.ExceptionType;
import com.dynamicelement.sdk.android.mddiclient.MddiData;
import com.dynamicelement.sdk.android.search.SearchCallBack;
import com.dynamicelement.sdk.android.search.SearchResult;

public class CameraMddiSearch {
    private static final String TAG = "CAMERA_SEARCH";
    private final CameraView cameraView;
    private final CameraSessionHandler cameraSessionHandler;
    protected SearchState searchState = SEARCH_YET_TO_START;
    private final boolean barcodeOnly;
    private boolean firstSearch;
    private boolean blurBeforeBarcode;
    protected Image currentImage = null;
    protected int currentRotationDegree = 0;
    protected boolean currentCapture;

    protected enum SearchState {SEARCH_YET_TO_START, SEARCH_IS_STOPPED}

    public CameraMddiSearch(CameraView cameraView, CameraSessionHandler cameraSessionHandler, boolean barcodeOnly,
                            boolean blurBeforeBarcode) {
        this.cameraView = cameraView;
        this.cameraSessionHandler = cameraSessionHandler;
        this.barcodeOnly = barcodeOnly;
        this.firstSearch = true;
        this.blurBeforeBarcode = blurBeforeBarcode;
        this.currentCapture = false;
    }

    /**
     * Camera background task for the image.
     */
    protected void cameraTask(Image mediaImage, int rotationDegree, boolean checkBarcodeFormat, Context context) throws Exception {
        if (this.cameraView.enableMddiSearch) {
            AnimationDrawable scanAnimation =
                    (AnimationDrawable) this.cameraView.binding.scanImageView.getBackground();
            if (this.cameraView.enableLayout) {
                scanAnimation.start();
            } else {
                scanAnimation.stop();
            }
        }
        this.cameraView.safeToSwitchCamera = true;

        if (this.cameraView.currentCameraMode == DEFAULT_CLIENT) {
            if (this.currentCapture) {
                return;
            }
            if (this.currentImage != null) {
                this.currentImage.close();
            }
            this.currentImage = mediaImage;
            this.currentRotationDegree = rotationDegree;
            return;
        }

        // Default width and height
        int cropWidth = this.cameraSessionHandler.getHeightCropped();
        int cropHeight = this.cameraSessionHandler.getWidthCropped();

        if (this.cameraView.currentCameraMode == WITH_MDDI_CLIENT) {
            if (this.cameraView.ratioMode == RATIO_3X4 &&
                    this.cameraView.clientService.getMddiImageSize() == FORMAT_512X512) {
                cropHeight = cropWidth;
            }

            if (this.cameraView.ratioMode == RATIO_1X1 &&
                    this.cameraView.clientService.getMddiImageSize() == FORMAT_480X640) {
                cropWidth = (int) (cropHeight * RATIO_3X4.getNumVal());
            }
        }

        new MddiData(mediaImage, context, cropWidth, cropHeight,
                this.cameraView.currentCameraMode == WITH_MDDI_CLIENT ?
                        this.cameraView.clientService.getInstanceType() :
                        this.cameraView.dbSnoMode ? DB_SNO : IVF, this.cameraView.cid,
                this.cameraView.sno, this.cameraView.enableScan,
                this.barcodeOnly,
                rotationDegree, this.cameraView.currentCameraMode == WITH_MDDI_CLIENT ?
                this.cameraView.clientService.getTenantId() : "", this.firstSearch, this.blurBeforeBarcode,
                checkBarcodeFormat,
                new MddiData.MddiDataCallback() {
                    @Override
                    public void onDBSNO(String barcodeResult, String mddiCid, String mddiSno,
                                        StreamImage mddiImage, Bitmap originalBitmap) {
                        cameraView.activity.runOnUiThread(() -> {
                            Log.d("BARCODE", barcodeResult != null ? barcodeResult : "No barcode");
                            if (originalBitmap != null) {
                                Log.d("CONVERTED_IMAGE_WIDTH", String.valueOf(originalBitmap.getWidth()));
                                Log.d("CONVERTED_IMAGE_HEIGHT", String.valueOf(originalBitmap.getHeight()));
                            }
                            updateLayoutDbSno(barcodeResult);
                            executeMddiSearch(originalBitmap, mddiImage, barcodeResult, mddiCid,
                                    mddiSno);
                        });
                    }

                    @Override
                    public void onIVF(String mddiCid, String mddiSno, StreamImage mddiImage,
                                      Bitmap originalBitmap) {
                        cameraView.activity.runOnUiThread(() -> {
                            updateLayoutIvf();
                            executeMddiSearch(originalBitmap, mddiImage, null, mddiCid, mddiSno);
                        });
                    }
                });
        this.firstSearch = false;
    }

    private void updateLayoutIvf() {
        if (!cameraView.enableLayout) {
            cameraView.binding.recImageView.setVisibility(INVISIBLE);
            cameraView.binding.scanImageView.setVisibility(INVISIBLE);
            return;
        }
        cameraView.binding.recImageView.setVisibility(VISIBLE);
        cameraView.binding.scanImageView.setVisibility(cameraView.enableScan && cameraView.enableMddiSearch ? VISIBLE : INVISIBLE);
    }

    private void updateLayoutDbSno(String barcodeResult) {
        if (!cameraView.enableLayout) {
            cameraView.binding.recImageView.setVisibility(INVISIBLE);
            cameraView.binding.scanImageView.setVisibility(INVISIBLE);
            cameraView.binding.barcodeHintTextView.setVisibility(INVISIBLE);
            return;
        }

        cameraView.binding.recImageView.setVisibility(VISIBLE);

        if (!cameraView.enableScan) {
            cameraView.binding.scanImageView.setVisibility(cameraView.enableMddiSearch ? View.VISIBLE : INVISIBLE);
            return;
        }

        if (barcodeResult == null) {
            cameraView.binding.barcodeHintTextView.setVisibility(INVISIBLE);
            cameraView.binding.scanImageView.setVisibility(INVISIBLE);
            cameraView.binding.recImageView.setBackgroundResource((cameraView.enableMddiSearch) ? R.drawable.ic_qr_rectangle : R.drawable.ic_qr_rectangle_white);
            return;
        }
        cameraView.binding.scanImageView.setVisibility(cameraView.enableMddiSearch ? View.VISIBLE : INVISIBLE);
        cameraView.binding.barcodeHintTextView.setVisibility(VISIBLE);
        cameraView.binding.barcodeHintTextView.setText(barcodeResult);
        cameraView.binding.recImageView.setBackgroundResource(R.drawable.ic_qr_rectangle);
    }

    /**
     * Mddi Search process - Do the search request for all the images.
     *
     * @param mddiStreamBitmap is the image which will be compared with the Mddi database images.
     * @param mddiStreamCid    is the cid of the Mddi search request.
     * @param mddiStreamSno    is the sno of the Mddi search request.
     */
    protected void searchTask(Bitmap mddiStreamBitmap, String mddiStreamCid, String mddiStreamSno) {
        cameraView.clientService.resetStopStatus().searchImage(mddiStreamBitmap, mddiStreamCid, mddiStreamSno,
                true, new SearchCallBack() {
                    @Override
                    public void onNegativeResponse(com.dynamicelement.mddi.SearchStreamResponse searchStreamResponse, SearchResult searchResult) {
                        if (searchState != SEARCH_IS_STOPPED) {
                            cameraView.cameraMddiCallback.onNegativeResponse(searchStreamResponse, searchResult);
                        }
                    }

                    @Override
                    public void onPositiveResponse(com.dynamicelement.mddi.SearchStreamResponse searchStreamResponse, SearchResult searchResult) {
                        if (searchState != SEARCH_IS_STOPPED) {
                            cameraView.cameraMddiCallback.onPositiveResponse(searchStreamResponse, searchResult);
                        }
                    }

                    @Override
                    public void onSearchCompleted(String elapsedTime, String summaryMessage) {
                        cameraView.cameraMddiCallback.onCompleted(elapsedTime,summaryMessage);
                    }

                    @Override
                    public void onError(ExceptionType exceptionType, Exception e) {
                        Log.d(TAG + "_MDDI_EXCEPTION", e.toString());
                        stopMddiSearch();
                        cameraView.cameraMddiCallback.onError(exceptionType, e);
                    }
                });
    }

    /**
     * Execute the mddi search for the given image.
     */
    protected void executeMddiSearch(Bitmap cameraBitmap, StreamImage mddiImage,
                                     String barcodeResult, String cid, String sno) {
        AsyncTask.execute(() -> {
            if (cameraView.currentCameraMode == WITHOUT_MDDI_CLIENT || (cid == null || sno == null)) {
                if (barcodeResult == null) {
                    return;
                }
                cameraView.cameraCallback.onBarcodeObtained(barcodeResult);
                return;
            }

            cameraView.cameraMddiCallback.onImageObtained(cameraBitmap, barcodeResult, cid,
                    sno);

            if (mddiImage == null || !cameraView.enableMddiSearch || searchState == SEARCH_IS_STOPPED) {
                return;
            }

            Bitmap mddiBitmap = buildBitmapFromMddiImage(mddiImage.getImage(),
                    cameraView.clientService.getMddiImageSize().getWidth(),
                    cameraView.clientService.getMddiImageSize().getHeight(), Bitmap.Config.ARGB_8888);

            if (cameraView.clientService.getInstanceType() != DB_SNO || !cameraView.enableScan) {
                searchTask(mddiBitmap, cid, sno);
                return;
            }

            // For DB SNO type of instance, when the barcode scan is enabled, the MDDI search will happen only
            // if there is barcode.
            if (barcodeResult == null) return;
            searchTask(mddiBitmap, cid, sno);
        });
    }

    /**
     * Start (or) Resume the Mddi search.
     * Calling this method explicitly start the Mddi search without considering any other factors.
     * Suppose if the camera mode is 'CAMERA_MDDI_SEARCH_OFF' and after some time, there is a
     * need to start Mddi search.
     * At that time, this method can be called to start the Mddi search.
     */
    protected void resumeMddiSearch() {
        this.searchState = SEARCH_YET_TO_START;
        this.cameraView.enableMddiSearch = true;
        this.cameraView.enableScan = this.cameraView.currentCameraMode == WITH_MDDI_CLIENT && this.cameraView.cameraParameters != null && cameraView.cameraParameters.enableBarcodeScan;
    }

    /**
     * Stop the mddi search(If the Mddi search is ON).
     * Calling this method abruptly stops the Mddi search.
     * Also note that, calling the 'onPause' method already stops the Mddi search.
     * The difference is that the 'onPause' method closes the camera as well.
     * This method stops the Mddi search without closing the camera.
     */
    protected void stopMddiSearch() {
        this.searchState = SEARCH_IS_STOPPED;
        this.cameraView.enableMddiSearch = false;
        this.cameraView.enableScan = false;
        this.cameraView.clientService.stopMddi();
    }

    protected void stopMddiIfRunning() {
        if (this.cameraView.enableMddiSearch) {
            this.searchState = SEARCH_IS_STOPPED;
            this.cameraView.clientService.stopMddi();
        }
    }
}
