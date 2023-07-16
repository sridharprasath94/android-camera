package com.dynamicelement.sdk.android.testMddiImage;

import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.centerCropBitmap;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.closeGrpcChannel;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.getStreamImageFromBitmap;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.requireNonNull;
import static com.dynamicelement.sdk.android.misc.MddiConstants.RESPONSE_NO_ERROR;
import static com.dynamicelement.sdk.android.misc.MddiConstants.TEST_MDDI_INVALID_IMAGE;

import android.graphics.Bitmap;
import android.util.Log;

import com.dynamicelement.mddi.MddiTenantServiceGrpc;
import com.dynamicelement.mddi.StreamImage;
import com.dynamicelement.mddi.TestImageBeforeAddStreamResponse;
import com.dynamicelement.sdk.android.Callback;
import com.dynamicelement.sdk.android.exceptions.ClientException;
import com.dynamicelement.sdk.android.exceptions.ExceptionType;
import com.dynamicelement.sdk.android.exceptions.ServerException;
import com.dynamicelement.sdk.android.mddiclient.ClientService;

import java.util.Objects;

import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

/**
 * Task for performing Asynchronous test MDDI image API - To test the images whether it is
 * suitable for MDDI purpose and provide with the desired
 * responses from the MDDI backend.
 */
public class TestMddiImageTask {
    private static final String TAG = "MDDI_LOGIC_TASK_TEST_IMAGE";
    private final ClientService clientService;
    private final Callback<TestMddiImageResult> testMddiImageCallback;
    private final StreamObserver<StreamImage> streamImageStreamObserver;
    private TestMddiImageState currentTestMddiImageState;

    /**
     * TestMddiImageTask - Send request and receive response for the test MDDI image API.
     *
     * @param clientService         is initialised with the credentials and configuration of the
     *                              MDDI
     *                              backend.
     * @param managedChannel        is the gRPC channel which is used for client server
     *                              communication.
     * @param asyncStub             is the asynchronous channel stub which is used for calling the server APIs.
     * @param testMddiImageCallback is used to observe the test MDDI image response from the MDDI
     *                              backend.
     */
    public TestMddiImageTask(ClientService clientService,
                             ManagedChannel managedChannel,
                             MddiTenantServiceGrpc.MddiTenantServiceStub asyncStub,
                             Callback<TestMddiImageResult> testMddiImageCallback) {
        requireNonNull(clientService, testMddiImageCallback);
        this.clientService = clientService;
        this.testMddiImageCallback = testMddiImageCallback;
        this.streamImageStreamObserver =
                asyncStub.testImageBeforeAdd(new StreamObserver<TestImageBeforeAddStreamResponse>() {
                    @Override
                    public void onNext(TestImageBeforeAddStreamResponse value) {
                        TestMddiImageResult testMddiImageResult = new TestMddiImageResult();
                        if (value.getError().getErrorCode() == RESPONSE_NO_ERROR ||
                                value.getError().getErrorCode() == TEST_MDDI_INVALID_IMAGE) {
                            testMddiImageResult.setResponse(value.getAck());
                            testMddiImageResult.setValidImage(value.getError().getErrorCode() == RESPONSE_NO_ERROR);
                            if (currentTestMddiImageState == TestMddiImageState.TEST_MDDI_IMAGE_STOP) {
                                return;
                            }
                            Log.d(TAG + "_ON_NEXT_LOG_MESSAGE", testMddiImageResult.getResponse());
                            testMddiImageCallback.onResponse(testMddiImageResult);
                        } else {
                            testMddiImageResult.setResponse(value.getError().getErrorCategory() + ":" +
                                    value.getError().getErrorCode() + " - " +
                                    value.getError().getErrorMessage());
                            if (currentTestMddiImageState == TestMddiImageState.TEST_MDDI_IMAGE_STOP) {
                                return;
                            }
                            Log.d(TAG + "_ON_RESPONSE_ERROR", testMddiImageResult.getResponse());
                            testMddiImageCallback.onError(ExceptionType.SERVER_EXCEPTION,
                                    new ServerException(testMddiImageResult.getResponse()));
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.d(TAG + "_ON_CONNECTION_ERROR", t.getMessage());
                        if (Objects.requireNonNull(t.getMessage()).endsWith("Channel shutdownNow invoked")) {
                            return;
                        }
                        testMddiImageCallback.onError(ExceptionType.SERVER_EXCEPTION,
                                new ServerException(t.getMessage()));
                        closeGrpcChannel(TAG, managedChannel);
                    }

                    @Override
                    public void onCompleted() {
                        Log.d(TAG + "_ON_COMPLETED", "Test Mddi image call is completed");
                        closeGrpcChannel(TAG, managedChannel);
                    }
                });
    }

    /**
     * Execute the test MDDI image task for the bitmap with given cid, sno.
     *
     * @param bitmap is the image in bitmap format which is to be tested whether it is suitable
     *               for MDDI backend.
     * @param width  of the bitmap. Note that it should not be 0 or less than the minimum width.
     * @param height of the bitmap. Note that it should not be 0 or less than minimum height.
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
            this.testMddiImageCallback.onError(ExceptionType.CLIENT_EXCEPTION,
                    new ClientException(width == 0 ? "Width cannot be 0" : "Height cannot be 0"));
            return;
        }
        if (width < mddiImageWidth || height < mddiImageHeight) {
            this.testMddiImageCallback.onError(ExceptionType.CLIENT_EXCEPTION,
                    new ClientException(width < mddiImageWidth ?
                            "Width cannot be less than " + mddiImageWidth :
                            "Height cannot be less than " + mddiImageHeight));
            return;
        }
        Bitmap croppedBitmap = centerCropBitmap(bitmap, width, height);
        StreamImage streamImage = getStreamImageFromBitmap(croppedBitmap, cid, sno,
                this.clientService.getInstanceType(),
                this.clientService.getTenantId());
        this.testMddiNextStreamImage(streamImage);
    }

    /**
     * Test the next stream image whether it is suitable to be added to the MDDI backend.
     *
     * @param streamImage is the constructed image with required parameters like cid, sno,
     *                    timestamp etc.
     */
    private void testMddiNextStreamImage(StreamImage streamImage) {
        if (this.currentTestMddiImageState == TestMddiImageState.TEST_MDDI_IMAGE_STOP || this.streamImageStreamObserver == null) {
            Log.d(TAG + "_STOPPED", "Test Mddi image task is already stopped");
            return;
        }
        try {
            Log.d(TAG + "_CALLING_ON_NEXT", "Incoming test mddi image request");
            // For the subsequent images
            this.streamImageStreamObserver.onNext(streamImage);
        } catch (Exception e) {
            Log.d(TAG + "_CALLING_ON_ERROR", e.getMessage());
            streamImageStreamObserver.onError(e);
        }
    }

    private enum TestMddiImageState {
        TEST_MDDI_IMAGE_RESUME,
        TEST_MDDI_IMAGE_STOP
    }

    /**
     * Stop the test MDDI image task.
     */
    public void stopTestMddi() {
        this.currentTestMddiImageState = TestMddiImageState.TEST_MDDI_IMAGE_STOP;
        Log.d(TAG + "_CALLING_ON_COMPLETED", "Calling the stream on completed method");
        if (streamImageStreamObserver == null) {
            return;
        }
        streamImageStreamObserver.onCompleted();
    }
}

