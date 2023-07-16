package com.dynamicelement.sdk.android.add;

import static com.dynamicelement.sdk.android.add.AddImageStatus.DUPLICATE;
import static com.dynamicelement.sdk.android.add.AddImageStatus.ERROR;
import static com.dynamicelement.sdk.android.add.AddImageStatus.SUCCESS;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.appendMessages;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.buildBitmapFromMddiImage;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.calculateTimeIn24HourFormat;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.centerCropBitmap;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.channelClosedCheck;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.closeGrpcChannel;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.getStreamImageFromBitmap;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.requireNonNull;
import static com.dynamicelement.sdk.android.misc.MddiConstants.ADD_ERROR_DUPLICATE_IMAGE;
import static com.dynamicelement.sdk.android.misc.MddiConstants.ADD_ERROR_INVALID_IMAGE;
import static com.dynamicelement.sdk.android.misc.MddiConstants.RESPONSE_NO_ERROR;
import static com.dynamicelement.sdk.android.misc.MddiConstants.TIME_DELAY;

import android.graphics.Bitmap;
import android.util.Log;

import com.dynamicelement.mddi.AddStreamResponse;
import com.dynamicelement.mddi.MddiTenantServiceGrpc;
import com.dynamicelement.mddi.StreamImage;
import com.dynamicelement.sdk.android.exceptions.ClientException;
import com.dynamicelement.sdk.android.exceptions.ExceptionType;
import com.dynamicelement.sdk.android.exceptions.ServerException;
import com.dynamicelement.sdk.android.mddiclient.ClientService;
import com.dynamicelement.sdk.android.misc.PingStates;
import com.dynamicelement.sdk.android.search.SearchTask;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

/**
 * Task for performing Asynchronous Add API - To add the images and provide with the desired
 * responses from the MDDI backend.
 */
public class AddTask {
    private static final String TAG = "MDDI_LOGIC_TASK_ADD";
    private final ClientService clientService;
    private int currentCount = 1;
    private Bitmap imageBitmap;
    private int imageCount = 1;
    private long startTime;
    private final NumberFormat f = new DecimalFormat("00");
    private final StreamObserver<StreamImage> streamImageStreamObserver;
    private final AddType currentAddType;
    protected AddState currentAddState;
    private String summaryMessage;
    protected AddCallback addCallback;
    private final StringBuffer imageLogBuffer = new StringBuffer();
    private final StringBuffer summaryLogBuffer = new StringBuffer();


    /**
     * AddTask - Task to add the provided images to the MDDI backend.
     *
     * @param clientService  is initialised with the credentials and configuration of the MDDI
     *                       backend.
     * @param continuousAdd  indicates whether the add is continuous or is finite.
     * @param managedChannel is the gRPC channel which is used for client server
     *                       communication.
     * @param asyncStub      is the asynchronous channel stub which is used for calling the server APIs.
     * @param addCallback    is used to observe the add response from MDDI backend.
     */
    public AddTask(ClientService clientService,
                   boolean continuousAdd,
                   ManagedChannel managedChannel,
                   MddiTenantServiceGrpc.MddiTenantServiceStub asyncStub,
                   AddCallback addCallback) {
        requireNonNull(clientService, addCallback);
        this.clientService = clientService;
        this.addCallback = addCallback;
        this.currentAddType = continuousAdd ? AddType.ADD_UNTIL_STOPPED : AddType.ADD_FINITE;
        this.currentAddState = AddState.ADD_RESUME;
        this.streamImageStreamObserver =
                asyncStub.addStream(new StreamObserver<AddStreamResponse>() {
                    @Override
                    public void onNext(AddStreamResponse addStreamResponse) {
                        if (addStreamResponse.hasAddReponses()) {
                            appendMessages(summaryLogBuffer, System.lineSeparator() +
                                            "Add Summary" + " : " + System.lineSeparator() +
                                            "1. Total Images = {3}" + System.lineSeparator() +
                                            "2. Added Images = {1}" + System.lineSeparator() +
                                            "3. Duplicate Images = {0}" + System.lineSeparator() +
                                            "4. Error Images = {2}" + System.lineSeparator(),
                                    addStreamResponse.getAddReponses().getDupAddCount(),
                                    addStreamResponse.getAddReponses().getSuccessAddCount(),
                                    addStreamResponse.getAddReponses().getErrorAddCount(),
                                    addStreamResponse.getAddReponses().getTotalCount());
                            Log.d(TAG + "_ON_NEXT_FINISHED", summaryLogBuffer.toString());
                        } else if (addStreamResponse.getError().getErrorCode() == RESPONSE_NO_ERROR ||
                                addStreamResponse.getError().getErrorCode() == ADD_ERROR_DUPLICATE_IMAGE ||
                                addStreamResponse.getError().getErrorCode() == ADD_ERROR_INVALID_IMAGE) {
                            AddResult addResult = new AddResult();
                            addResult.setPing(PingStates.calculatePing(addStreamResponse
                                    .getSingleTripTime()));
                            addResult.setImageCount(imageCount);
                            addResult.setMddiImage(imageBitmap);
                            switch (Math.toIntExact(addStreamResponse.getError().getErrorCode())) {
                                case RESPONSE_NO_ERROR:
                                    addResult.setAddImageStatus(SUCCESS);
                                    break;
                                case ADD_ERROR_DUPLICATE_IMAGE:
                                    addResult.setAddImageStatus(DUPLICATE);
                                    break;
                                default:
                                    addResult.setAddImageStatus(ERROR);
                            }
                            appendMessages(imageLogBuffer,
                                    "Image {0} : " + addResult.getAddImageStatus().name(),
                                    f.format(imageCount));
                            addResult.setImageLogMessage(imageLogBuffer.toString());
                            Log.d(TAG + "_ON_NEXT_LOG_MESSAGE", addResult.getImageLogMessage());
                            addCallback.onNextResponse(addStreamResponse, addResult);
                        } else {
                            throwError(
                                    addStreamResponse.getError().getErrorCategory() +
                                            ":" + addStreamResponse.getError().getErrorCode() +
                                            " - " + addStreamResponse.getError().getErrorMessage());
                            return;
                        }
                        imageCount++;
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.d(TAG + "_ON_CONNECTION_ERROR", t.getMessage());
                        if (channelClosedCheck(t)) {
                            return;
                        }
                        addCallback.onError(ExceptionType.SERVER_EXCEPTION,
                                new ServerException(t.getMessage()));
                        closeGrpcChannel(TAG, managedChannel);
                    }

                    @Override
                    public void onCompleted() {
                        imageCount = 0;
                        // Get all the individual image response along with summary response.
                        if (!imageLogBuffer.toString().isEmpty() && !summaryLogBuffer.toString().isEmpty()) {
                            summaryMessage =
                                    summaryLogBuffer + System.lineSeparator() + "Log " +
                                            "Messages :" + System.lineSeparator() + imageLogBuffer;
                            Log.d(TAG + "_ON_COMPLETED", summaryMessage);
                        }
                        String elapseTime = calculateTimeIn24HourFormat(startTime,
                                System.currentTimeMillis());
                        if (summaryMessage != null) {
                            addCallback.onCompleted(elapseTime, summaryMessage);
                        }
                        closeGrpcChannel(TAG, managedChannel);
                    }
                });
    }

    /**
     * Execute the add task for the bitmap with given cid and sno.
     *
     * @param bitmap is the image in bitmap format which is to be added to the MDDI backend.
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
        int mddiImageWidth = this.clientService.getMddiImageSize().getWidth();
        int mddiImageHeight = this.clientService.getMddiImageSize().getHeight();
        if (width == 0 || height == 0) {
            this.addCallback.onError(ExceptionType.CLIENT_EXCEPTION,
                    new ClientException(width == 0 ? "Width cannot be 0" : "Height cannot be 0"));
            return;
        }
        if (width < mddiImageWidth || height < mddiImageHeight) {
            this.addCallback.onError(ExceptionType.CLIENT_EXCEPTION,
                    new ClientException(width < mddiImageWidth ? "Width cannot be less than " + mddiImageWidth :
                            "Height cannot be less than " + mddiImageHeight));
            return;
        }
        Bitmap croppedBitmap = centerCropBitmap(bitmap, width, height);
        StreamImage streamImage = getStreamImageFromBitmap(croppedBitmap, cid, sno,
                this.clientService.getInstanceType(),
                this.clientService.getTenantId());

        this.addNextStreamImage(streamImage);
    }

    /**
     * Add the next stream image.
     *
     * @param streamImage is the constructed image with required parameters like cid, sno,
     *                    timestamp etc.
     */
    private void addNextStreamImage(StreamImage streamImage) {
        if (this.currentAddState == AddState.ADD_STOP || streamImageStreamObserver == null) {
            Log.d(TAG + "_STOPPED", "Add is already stopped");
            return;
        }
        try {
            switch (currentAddType) {
                case ADD_UNTIL_STOPPED:
                    try {
                        Thread.sleep(TIME_DELAY);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    onNext(streamImage);
                    this.currentCount++;
                    break;
                case ADD_FINITE:
                    onNext(streamImage);
                    // Stop the add for the final image.
                    this.stopAdd();
                    this.currentCount++;
                    break;
            }
        } catch (Exception e) {
            Log.d(TAG + "_CALLING_ON_ERROR", e.getMessage());
            if (this.currentAddState == AddState.ADD_STOP) {
                return;
            }
            streamImageStreamObserver.onError(e);
        }
    }

    /**
     * @param error is the error message.
     */
    private void throwError(String error) {
        this.currentAddState = AddState.ADD_STOP;
        Log.d(TAG + "_ON_RESPONSE_ERROR", error);
        addCallback.onError(ExceptionType.SERVER_EXCEPTION, new ServerException(error));
    }

    /**
     * Add the next stream image.
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
                this.clientService.getMddiImageSize().getWidth(), this.clientService.getMddiImageSize().getHeight(),
                Bitmap.Config.ARGB_8888);
        if (this.currentAddState == AddState.ADD_STOP || streamImageStreamObserver == null) {
            return;
        }
        Log.d(TAG + "_CALLING_ON_NEXT", "Incoming add request");
        streamImageStreamObserver.onNext(streamImage);
    }

    /**
     * Stop the add process.
     */
    public void stopAdd() {
        if(this.currentAddState == AddState.ADD_STOP){
            return;
        }
        this.currentAddState = AddState.ADD_STOP;
        if (streamImageStreamObserver == null) {
            return;
        }
        Log.d(TAG + "_CALLING_ON_COMPLETED", "Calling the stream on completed method");
        streamImageStreamObserver.onCompleted();
    }

    private enum AddState {
        ADD_RESUME,
        ADD_STOP,
    }

    protected enum AddType {
        /**
         * Underlying connection stream will be kept open for further add requests.
         * It will be only stopped by calling stopAdd() on the client service.
         */
        ADD_UNTIL_STOPPED,
        /**
         * Underlying connection stream will be kept open until the specified number of images
         * were added.
         * After that, the connection stream will be closed automatically.
         * Also, The connection stream can still be closed by calling stopAdd() on the client
         * service.
         */
        ADD_FINITE
    }
}
