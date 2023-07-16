package com.dynamicelement.sdk.android.getsample;

import static com.dynamicelement.sdk.android.getsample.GetSampleStatus.EXISTING_CID;
import static com.dynamicelement.sdk.android.getsample.GetSampleStatus.INVALID_RESPONSE;
import static com.dynamicelement.sdk.android.getsample.GetSampleStatus.NON_EXISTING_CID;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.buildBitmapFromIntegerList;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.buildCollectionID;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.buildCollectionIDDefaultEC2;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.closeGrpcChannel;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.getRevisedList;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.requireNonNull;
import static com.dynamicelement.sdk.android.misc.MddiConstants.GET_SAMPLE_ERROR_INVALID_CID;
import static com.dynamicelement.sdk.android.misc.MddiConstants.RESPONSE_NO_ERROR;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.dynamicelement.mddi.CollectionId;
import com.dynamicelement.mddi.CollectionIdData;
import com.dynamicelement.mddi.Image;
import com.dynamicelement.mddi.MddiTenantServiceGrpc;
import com.dynamicelement.mddi.SampleImageResponse;
import com.dynamicelement.mddi.TenantCollectionId;
import com.dynamicelement.mddi.mddiError;
import com.dynamicelement.sdk.android.Callback;
import com.dynamicelement.sdk.android.exceptions.ExceptionType;
import com.dynamicelement.sdk.android.exceptions.ServerException;
import com.dynamicelement.sdk.android.mddiclient.ClientService;

import java.util.List;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

/**
 * Get Sample background Task to get the sample image response of a particular collection(Cid) of
 * the MDDI.
 * Call the get sample task with the given parameters and observe the response from the callback
 */
public class GetSampleTask extends AsyncTask<Void, Void, GetSampleResult> {
    private static final String TAG = "MDDI_LOGIC_TASK_GET_SAMPLE";
    private final ClientService clientService;
    private final Callback<GetSampleResult> callback;
    private final String cid;
    private final ManagedChannel managedChannel;
    private final MddiTenantServiceGrpc.MddiTenantServiceBlockingStub blockingStub;

    private final boolean imageNeeded;

    /**
     * @param cid            is the cid of the MDDI get sample request.
     * @param clientService  is initialised with the credentials and configuration of the MDDI
     *                       backend.
     * @param managedChannel is the gRPC channel which is used for client server communication.
     * @param blockingStub   is the blocking channel stub which is used for calling the server APIs.
     * @param callback       is used to observe the get sample response from MDDI backend.
     */
    public GetSampleTask(String cid, boolean imageNeeded, ClientService clientService,
                         ManagedChannel managedChannel,
                         MddiTenantServiceGrpc.MddiTenantServiceBlockingStub blockingStub,
                         Callback<GetSampleResult> callback) {
        requireNonNull(cid, clientService, callback);
        this.clientService = clientService;
        this.callback = callback;
        this.managedChannel = managedChannel;
        this.blockingStub = blockingStub;
        this.cid = cid;
        this.imageNeeded = imageNeeded;
    }

    @Override
    public GetSampleResult doInBackground(Void... Void) {
        try {
            GetSampleTaskRunnable getSampleTaskRunnable =
                    new GetSampleTaskRunnable(this.clientService, blockingStub);
            return getSampleTaskRunnable.run(this.cid, this.imageNeeded);
        } catch (ServerException e) {
            this.callback.onError(ExceptionType.SERVER_EXCEPTION, e);
            return null;
        }
    }

    @Override
    public void onPostExecute(GetSampleResult getSampleResult) {
        if (getSampleResult == null) {
            return;
        }
        closeGrpcChannel(TAG, this.managedChannel);
        if (getSampleResult.getStatus() == NON_EXISTING_CID ||
                getSampleResult.getStatus() == INVALID_RESPONSE) {
            Log.d(TAG + "_ON_RESPONSE_ERROR", getSampleResult.getImageResponse());
            this.callback.onError(getSampleResult.getImageResponse().startsWith("Client service") ?
                            ExceptionType.CLIENT_EXCEPTION : ExceptionType.SERVER_EXCEPTION,
                    new ServerException(getSampleResult.getImageResponse()));
            return;
        }
        Log.d(TAG + "_ON_POSITIVE_RESPONSE", getSampleResult.getImageResponse());
        this.callback.onResponse(getSampleResult);
    }

    private interface GetSampleRunnable {
        GetSampleResult run(String cid, boolean imageNeeded) throws ServerException;
    }

    protected static class GetSampleTaskRunnable implements GetSampleRunnable {
        private final ClientService clientService;
        private final MddiTenantServiceGrpc.MddiTenantServiceBlockingStub blockingStub;

        protected GetSampleTaskRunnable(ClientService clientService,
                                        MddiTenantServiceGrpc.MddiTenantServiceBlockingStub blockingStub) {
            this.clientService = clientService;
            this.blockingStub = blockingStub;
        }

        /**
         * @param cid is the cid of the MDDI get sample request.
         * @return GetSampleResult
         * @throws ServerException
         */
        @Override
        public GetSampleResult run(String cid, boolean imageNeeded) throws ServerException {
            GetSampleResult getSampleResult = new GetSampleResult();
            mddiError mddiError;
            boolean hasSampleImage;
            Image image;
            try {
                // Check whether the connect IP is default EC2 instance.
                // The server request and response are different for default EC2 instance.
                if (this.clientService.isDefaultEC2IPSelected()) {
                    CollectionIdData collectionIdData =
                            getSampleResponseDefaultEc2(cid, imageNeeded, this.clientService);
                    getSampleResult.setImageResponse(collectionIdData.getAck());
                    getSampleResult.setHostIP(collectionIdData.getIp());
                    getSampleResult.setPortNumber(collectionIdData.getPort());
                    getSampleResult.setInstanceType(collectionIdData.getCidType());
                    getSampleResult.setMddiImage(collectionIdData.getSampleImage());
                    mddiError = collectionIdData.getError();
                    hasSampleImage = collectionIdData.hasSampleImage();
                    image = collectionIdData.getSampleImage();
                } else {
                    SampleImageResponse sampleImageResponse =
                            getSampleResponse(cid, imageNeeded, this.clientService);
                    getSampleResult.setImageResponse(sampleImageResponse.getAck());
                    getSampleResult.setMddiImage(sampleImageResponse.getSampleImage());
                    mddiError = sampleImageResponse.getError();
                    hasSampleImage = sampleImageResponse.hasSampleImage();
                    image = sampleImageResponse.getSampleImage();
                }

                if (mddiError.getErrorCode() != RESPONSE_NO_ERROR) {
                    getSampleResult.setStatus(mddiError.getErrorCode() == GET_SAMPLE_ERROR_INVALID_CID ?
                            NON_EXISTING_CID : INVALID_RESPONSE);
                    getSampleResult.setPixelsList(null);
                    getSampleResult.setImageResponse(mddiError.getErrorCategory() +
                            ":" + mddiError.getErrorCode() +
                            " - " + mddiError.getErrorMessage());
                    getSampleResult.sampleImage = null;
                    return getSampleResult;
                }

                if (!imageNeeded) {
                    getSampleResult.setPixelsList(null);
                    getSampleResult.setSampleImage(null);
                    getSampleResult.setStatus(EXISTING_CID);
                    return getSampleResult;
                }
                // Get the list of pixels from the received sample image
                List<Integer> pixelsList = hasSampleImage ? image.getImagePixelsList() : null;

                if (pixelsList == null) {
                    getSampleResult.setPixelsList(null);
                    getSampleResult.setSampleImage(null);
                    getSampleResult.setImageResponse("List of pixels for the given cid is null");
                    getSampleResult.setStatus(INVALID_RESPONSE);
                    return getSampleResult;
                }
                  /*
                     0 represents grayscaled image. 1 represents coloured image.
                     For grayscaled image,all the received pixels are required to convert into
                     bitmap.
                     For coloured images(RGB),every 3rd pixel is enough to construct a gray
                     bitmap out of it.
                    */
                pixelsList = image.getImageFormat() == 1 ? getRevisedList(pixelsList) :
                        pixelsList;
                if (pixelsList.size() != this.clientService.getMddiImageSize().getWidth() *
                        this.clientService.getMddiImageSize().getHeight()) {
                    getSampleResult.setPixelsList(null);
                    getSampleResult.setSampleImage(null);
                    getSampleResult.setImageResponse("Client service mddi image width and height does " +
                            "not match the list of received pixels list from the server");
                    getSampleResult.setStatus(INVALID_RESPONSE);
                    return getSampleResult;
                }
                getSampleResult.sampleImage = buildBitmapFromIntegerList(pixelsList,
                        this.clientService.getMddiImageSize().getWidth(),
                        this.clientService.getMddiImageSize().getHeight(),
                        Bitmap.Config.RGB_565);
                getSampleResult.setStatus(EXISTING_CID);
                getSampleResult.setPixelsList(pixelsList);
                return getSampleResult;
            } catch (StatusRuntimeException e) {
                getSampleResult.setImageResponse(e.getMessage());
                getSampleResult.setStatus(INVALID_RESPONSE);
                return getSampleResult;
            }
        }

        /**
         * Get sample Response for the given get sample request.
         *
         * @param cid is the cid of the current get sample request.
         */
        private SampleImageResponse getSampleResponse(String cid, boolean imageNeeded, ClientService clientService) {
            requireNonNull(cid);
            CollectionId collectionId = buildCollectionID(cid, imageNeeded, true,
                    clientService.getTenantId());
            // Request the server using the 'collectionId' via getSampleImage GRPC and returns the
            // response 'sampleImageResponse'
            return this.blockingStub.getSampleImage(collectionId);
        }

        /**
         * Get sample Response for the given get sample request for default Ec2 instance.
         *
         * @param cid is the cid of the current get sample request.
         */
        private CollectionIdData getSampleResponseDefaultEc2(String cid, boolean imageNeeded, ClientService clientService) {
            requireNonNull(cid);
            TenantCollectionId tenantCollectionId = buildCollectionIDDefaultEC2(cid, imageNeeded, true,
                    clientService.getTenantId());
            // Request the server using the 'tenantCollectionId' via getSampleImage GRPC and returns
            // the response 'collectionIdData'
            return this.blockingStub.getCollectionData(tenantCollectionId);
        }
    }
}
