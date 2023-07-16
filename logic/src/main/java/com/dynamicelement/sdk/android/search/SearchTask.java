package com.dynamicelement.sdk.android.search;

import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.appendMessages;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.buildBitmapFromMddiImage;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.calculateTimeIn24HourFormat;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.centerCropBitmap;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.channelClosedCheck;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.closeGrpcChannel;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.getStreamImageFromBitmap;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.requireNonNull;
import static com.dynamicelement.sdk.android.misc.MddiConstants.RESPONSE_NO_ERROR;
import static com.dynamicelement.sdk.android.misc.MddiConstants.SEARCH_ERROR_INVALID_IMAGE;
import static com.dynamicelement.sdk.android.misc.MddiConstants.SEARCH_ERROR_INVALID_SNO;
import static com.dynamicelement.sdk.android.misc.MddiConstants.TIME_DELAY;

import android.graphics.Bitmap;
import android.util.Log;

import com.dynamicelement.mddi.MddiTenantServiceGrpc;
import com.dynamicelement.mddi.SearchStreamResponse;
import com.dynamicelement.mddi.StreamImage;
import com.dynamicelement.sdk.android.exceptions.ClientException;
import com.dynamicelement.sdk.android.exceptions.ExceptionType;
import com.dynamicelement.sdk.android.exceptions.ServerException;
import com.dynamicelement.sdk.android.mddiclient.ClientService;
import com.dynamicelement.sdk.android.misc.PingStates;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

/**
 * Task for performing Asynchronous Search API - To search the images and provide with the
 * desired responses from the MDDI backend
 */
public class SearchTask {
    private static final String TAG = "MDDI_LOGIC_TASK_SEARCH";
    private final ClientService clientService;
    private int currentCount = 1;
    private Bitmap imageBitmap;
    private int imageCount = 1;
    private long startTime;
    private final NumberFormat f = new DecimalFormat("00");
//    private final ArrayList<SearchStreamResponse> summaryResponses = new ArrayList<>();
    private final StreamObserver<StreamImage> streamImageStreamObserver;
    private final SearchType currentSearchType;
    protected SearchState currentSearchState;
    private String summaryLogMessage;
    private final SearchCallBack searchCallback;

    /**
     * SearchTask - Task to search the provided images from the MDDI backend.
     *
     * @param clientService    is initialised with the credentials and configuration of the MDDI
     *                         backend.
     * @param continuousSearch is set as 'true' makes the MDDI search continuous. The search
     *                         process will not
     *                         be stopped automatically(The connection will be open).'stopSearch'
     *                         should be
     *                         called to stop the MDDI search.
     *                         Set as 'false' makes the MDDI search finite. It will open the
     *                         connection for only one
     *                         MDDI request and then it will be closed automatically.
     * @param managedChannel   is the gRPC channel which is used for client server
     *                         communication.
     * @param asyncStub        is the asynchronous channel stub which is used for calling the server APIs.
     * @param searchCallback   is used to observe the search response from MDDI backend.
     */
    public SearchTask(ClientService clientService,
                      boolean continuousSearch,
                      ManagedChannel managedChannel,
                      MddiTenantServiceGrpc.MddiTenantServiceStub asyncStub,
                      SearchCallBack searchCallback) {
        requireNonNull(clientService, searchCallback);
        this.clientService = clientService;
        this.searchCallback = searchCallback;
        this.currentSearchType = continuousSearch ? SearchType.SEARCH_UNTIL_STOPPED : SearchType.SEARCH_FINITE;
        this.currentSearchState = SearchState.SEARCH_RESUME;
        streamImageStreamObserver =
                asyncStub.searchStream(new StreamObserver<SearchStreamResponse>() {
                    @Override
                    public void onNext(SearchStreamResponse searchStreamResponse) {
                        if (searchStreamResponse.getError().getErrorCode() != RESPONSE_NO_ERROR &&
                                searchStreamResponse.getError().getErrorCode() != SEARCH_ERROR_INVALID_IMAGE) {
                            throwError(
                                    searchStreamResponse.getError().getErrorCategory() + ":" +
                                            searchStreamResponse.getError().getErrorCode() + " - " +
                                            searchStreamResponse.getError().getErrorMessage());
                            return;
                        }
                        SearchResult searchResult = new SearchResult();
                        searchResult.setPing(PingStates.calculatePing(searchStreamResponse.getSingleTripTime()));
                        searchResult.setImageCount(imageCount);
                        searchResult.setScore(searchStreamResponse.getSearchresponse().getScore());
                        searchResult.setUid(searchStreamResponse.getSearchresponse().getUid());
                        searchResult.setCid(searchStreamResponse.getCid());
                        searchResult.setSno(searchStreamResponse.getSno());
                        searchResult.setMddiImage(imageBitmap);
                        StringBuffer imageLogBuffer = new StringBuffer();
                        appendMessages(imageLogBuffer, "Image {0}  : Score = {1} , UID = {2} , " +
                                        "SNO = {3} " + ", CID = {4}", f.format(imageCount),
                                searchResult.getScore(), searchResult.getUid(), searchResult.getSno(), searchResult.getCid());
                        searchResult.setImageLogMessage(imageLogBuffer.toString());

                        if (searchResult.getScore() >= clientService.getMddiThreshold()) {
                            searchResult.setRating(getRating(searchResult.getScore()));
                            searchCallback.onPositiveResponse(searchStreamResponse, searchResult);
                        } else {
                            searchCallback.onNegativeResponse(searchStreamResponse, searchResult);
                        }
                        Log.d(TAG + "_ON_NEXT_LOG_MESSAGE", searchResult.getImageLogMessage());
                        //Log buffer to collect all the responses
                        if (imageCount > 1) {
                            summaryLogMessage += searchResult.getImageLogMessage();
                        } else {
                            summaryLogMessage = searchResult.getImageLogMessage();
                        }
//                        summaryResponses.add(searchStreamResponse);
                        imageCount++;
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.d(TAG + "_ON_CONNECTION_ERROR", t.getMessage());
                        if (channelClosedCheck(t)) return;
                        searchCallback.onError(ExceptionType.SERVER_EXCEPTION,
                                new ServerException(t.getMessage()));
                        closeGrpcChannel(TAG, managedChannel);
                    }

                    @Override
                    public void onCompleted() {
                        imageCount = 0;
                        //Get all the response
                        summaryLogMessage = "Search Summary : " + System.lineSeparator() + summaryLogMessage;
                        Log.d(TAG + "_ON_COMPLETED", summaryLogMessage);
                        String elapsedTime = calculateTimeIn24HourFormat(startTime,
                                System.currentTimeMillis());
                        searchCallback.onSearchCompleted(elapsedTime,
                                summaryLogMessage);
                        closeGrpcChannel(TAG, managedChannel);
                    }
                });
    }

    /**
     * Execute the search task for the bitmap with given cid and sno.
     *
     * @param bitmap is the image in bitmap format which is to be searched from the MDDI backend.
     * @param width  of the bitmap.Note that it should not be 0 or less than 480(Minimum width).
     * @param height of the bitmap.Note that it should not be 0 or less than 640(Minimum height).
     * @param cid    is the cid of the current MDDI request.
     * @param sno    is the sno of the current MDDI request.
     */
    public void execute(Bitmap bitmap,
                        int width,
                        int height,
                        String cid,
                        String sno) {
        requireNonNull(bitmap, cid, sno);
        if (this.currentSearchState == SearchState.SEARCH_STOP) {
            return;
        }
        int mddiImageWidth = this.clientService.getMddiImageSize().getWidth();
        int mddiImageHeight = this.clientService.getMddiImageSize().getHeight();
        if (width == 0 || height == 0) {
            this.searchCallback.onError(ExceptionType.CLIENT_EXCEPTION,
                    new ClientException(width == 0 ? "Width cannot be 0" : "Height cannot be 0"));
            return;
        }
        if (width < mddiImageWidth || height < mddiImageHeight) {
            this.searchCallback.onError(ExceptionType.CLIENT_EXCEPTION,
                    new ClientException(width < mddiImageWidth ? "Width cannot be less than " + mddiImageWidth :
                            "Height cannot be less than " + mddiImageHeight));
            return;
        }
        Bitmap croppedBitmap = centerCropBitmap(bitmap, width, height);
        StreamImage streamImage = getStreamImageFromBitmap(croppedBitmap, cid, sno,
                this.clientService.getInstanceType(),
                this.clientService.getTenantId());
        this.searchNextStreamImage(streamImage);
    }

    /**
     * Search the next stream image.
     *
     * @param streamImage is the constructed image with required parameters like cid, sno,
     *                    timestamp etc.
     */
    private void searchNextStreamImage(StreamImage streamImage) {
        if (this.currentSearchState == SearchState.SEARCH_STOP || streamImageStreamObserver == null) {
            Log.d(TAG + "_STOPPED", "Search is already stopped");
            return;
        }
        try {
            switch (currentSearchType) {
                case SEARCH_UNTIL_STOPPED:
//                    try {
//                        Thread.sleep(TIME_DELAY);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                    onNext(streamImage);
                    this.currentCount++;
                    break;
                case SEARCH_FINITE:
                    onNext(streamImage);
                    this.stopSearch();
                    this.currentCount++;
                    break;
            }
        } catch (Exception e) {
            Log.d(TAG + "_CALLING_ON_ERROR", e.getMessage());
            streamImageStreamObserver.onError(e);
        }
    }

    /**
     * @param error is the error message.
     */
    private void throwError(String error) {
        this.currentSearchState = SearchState.SEARCH_STOP;
        Log.d(TAG + "_ON_RESPONSE_ERROR", error);
        searchCallback.onError(ExceptionType.SERVER_EXCEPTION, new ServerException(error));
    }

    /**
     * Search the next stream image.
     *
     * @param streamImage is the constructed image with required parameters like cid, sno,
     *                    timestamp etc.
     */
    private void onNext(StreamImage streamImage) {
        // Start the timer for the first image
        if (this.currentCount == 1) {
            startTime = System.currentTimeMillis();
        }
        this.imageBitmap = buildBitmapFromMddiImage(streamImage.getImage(),
                this.clientService.getMddiImageSize().getWidth(),
                this.clientService.getMddiImageSize().getHeight(), Bitmap.Config.ARGB_8888);
        if (this.currentSearchState == SearchState.SEARCH_STOP || streamImageStreamObserver == null) {
            return;
        }
        Log.d(TAG + "_CALLING_ON_NEXT", "Incoming search request");
        streamImageStreamObserver.onNext(streamImage);
    }

    /**
     * Stop the search process.
     */
    public void stopSearch() {
        if(this.currentSearchState == SearchState.SEARCH_STOP){
            return;
        }
        this.currentSearchState = SearchState.SEARCH_STOP;
        if (streamImageStreamObserver == null) {
            return;
        }
        Log.d(TAG + "_CALLING_ON_COMPLETED", "Calling the stream on completed method");
        streamImageStreamObserver.onCompleted();
    }

    private enum SearchState {
        SEARCH_RESUME,
        SEARCH_STOP,
    }

    protected enum SearchType {
        /**
         * Underlying connection stream will be kept open for further search requests.
         * It will be only stopped by calling stopSearch() on the client service.
         */
        SEARCH_UNTIL_STOPPED,
        /**
         * Underlying connection stream will be kept open until the specified number of images
         * were searched.
         * After that, the connection stream will be closed automatically.
         * Also, The connection stream can still be closed by calling stopSearch() on the client
         * service.
         */
        SEARCH_FINITE
    }

    /**
     * The rating is proportional to the obtained MDDI score.
     * The possible range of values are 0 to 5.
     * Value '0' denotes the unmatched image.
     * Value '1' denotes the matched image with least score.
     * Value '5' denotes the matched image with high score.
     *
     * @param searchScore is the obtained MDDI score.
     * @return the rating value.
     */
    protected static int getRating(double searchScore) {
        if (searchScore >= 0.60 && searchScore <= 0.70) {
            return 1;
        }

        if (searchScore > 0.70 && searchScore <= 0.80) {
            return 2;
        }

        if (searchScore > 0.80 && searchScore <= 0.90) {
            return 3;
        }

        if (searchScore > 0.90 && searchScore <= 0.95) {
            return 4;
        }

        if (searchScore > 0.95 && searchScore <= 1) {
            return 5;
        }

        return 0;
    }
}
