package com.dynamicelement.sdk.android.ping;

import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.buildCollectionID;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.buildCollectionIDDefaultEC2;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.closeGrpcChannel;
import static com.dynamicelement.sdk.android.misc.MddiConstants.RESPONSE_NO_ERROR;

import android.os.AsyncTask;
import android.util.Log;

import com.dynamicelement.mddi.CollectionId;
import com.dynamicelement.mddi.CollectionIdData;
import com.dynamicelement.mddi.MddiTenantServiceGrpc;
import com.dynamicelement.mddi.SampleImageResponse;
import com.dynamicelement.mddi.TenantCollectionId;
import com.dynamicelement.mddi.mddiError;
import com.dynamicelement.sdk.android.Callback;
import com.dynamicelement.sdk.android.exceptions.ExceptionType;
import com.dynamicelement.sdk.android.exceptions.ServerException;
import com.dynamicelement.sdk.android.mddiclient.ClientService;
import com.dynamicelement.sdk.android.mddiclient.MddiParameters;
import com.dynamicelement.sdk.android.misc.MddiConstants;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

/**
 * Ping background Task to check the MDDI connection.
 */
public class PingTask extends AsyncTask<Void, Void, PingResult> {
    private static final String TAG = "MDDI_LOGIC_TASK_PING";
    private final ClientService clientService;
    private final Callback<PingResult> pingCallback;
    private final ManagedChannel managedChannel;
    private final MddiTenantServiceGrpc.MddiTenantServiceBlockingStub blockingStub;

    /**
     * @param clientService  is initialised with the credentials and configuration of the MDDI
     *                       backend.
     * @param managedChannel is the gRPC channel which is used for client server communication.
     * @param blockingStub   is the blocking channel stub which is used for calling the server APIs.
     * @param pingCallback   is used to observe the ping response from MDDI backend.
     */
    public PingTask(ClientService clientService,
                    ManagedChannel managedChannel,
                    MddiTenantServiceGrpc.MddiTenantServiceBlockingStub blockingStub,
                    Callback<PingResult> pingCallback) {
        MddiParameters.requireNonNull(clientService, pingCallback);
        this.clientService = clientService;
        this.pingCallback = pingCallback;
        this.managedChannel = managedChannel;
        this.blockingStub = blockingStub;
    }

    @Override
    public PingResult doInBackground(Void... Void) {
        try {
            PingTaskRunnable pingTaskRunnable = new PingTaskRunnable(this.clientService, blockingStub);
            return pingTaskRunnable.run();
        } catch (ServerException e) {
            this.pingCallback.onError(ExceptionType.SERVER_EXCEPTION, e);
            return null;
        }
    }

    @Override
    public void onPostExecute(PingResult pingResult) {
        if (pingResult == null) {
            return;
        }
        closeGrpcChannel(TAG, this.managedChannel);
        if (pingResult.isConnected()) {
            Log.d(TAG + "_ON_POSITIVE_RESPONSE", pingResult.getPingResponse());
            this.pingCallback.onResponse(pingResult);
            return;
        }
        Log.d(TAG + "_ON_RESPONSE_ERROR", pingResult.getPingResponse());
        this.pingCallback.onError(ExceptionType.SERVER_EXCEPTION,
                new ServerException(pingResult.getPingResponse()));
    }

    /**
     * Runnable interface - Ping Task.
     */
    private interface PingRunnable {
        PingResult run() throws ServerException;
    }

    protected static class PingTaskRunnable implements PingRunnable {
        private final ClientService clientService;
        private final MddiTenantServiceGrpc.MddiTenantServiceBlockingStub blockingStub;

        protected PingTaskRunnable(ClientService clientService,
                                   MddiTenantServiceGrpc.MddiTenantServiceBlockingStub blockingStub) {
            this.clientService = clientService;
            this.blockingStub = blockingStub;
        }

        /**
         * @return PingResult
         * @throws ServerException
         */
        @Override
        public PingResult run() throws ServerException {
            PingResult pingResult = new PingResult();
            mddiError mddiError;
            String ack;
            try {
                // Check whether the connect IP is default EC2 instance.
                // The server request and response are different for default EC2 instance.
                if (this.clientService.isDefaultEC2IPSelected()) {
                    CollectionIdData collectionIdData =
                            getPingResponseDefaultEc2();
                    mddiError = collectionIdData.getError();
                    ack = collectionIdData.getAck();
                } else {
                    SampleImageResponse sampleImageResponse =
                            getPingResponse();
                    mddiError = sampleImageResponse.getError();
                    ack = sampleImageResponse.getAck();
                }
                if (mddiError.getErrorCode() == RESPONSE_NO_ERROR ||
                        mddiError.getErrorCode() == MddiConstants.GET_SAMPLE_ERROR_INVALID_CID) {
                    pingResult.setPingResponse(ack);
                    pingResult.setConnected(true);
                    return pingResult;
                }
                pingResult.setPingResponse(mddiError.getErrorCategory() +
                        ":" + mddiError.getErrorCode() +
                        " - " + mddiError.getErrorMessage());
                pingResult.setConnected(false);
                return pingResult;
            } catch (StatusRuntimeException e) {
                pingResult.setPingResponse(e.getMessage());
                pingResult.setConnected(false);
                return pingResult;
            }
        }

        /**
         * Ping response for the ping request.
         */
        private SampleImageResponse getPingResponse() {
            CollectionId collectionId = buildCollectionID("1", false, false, this.clientService.getTenantId());
            // Request the server using the 'collectionId' via getSampleImage GRPC and returns the
            // response 'sampleImageResponse'
            return this.blockingStub.getSampleImage(collectionId);
        }

        /**
         * Ping response for the ping request for default Ec2 instance.
         */
        public CollectionIdData getPingResponseDefaultEc2() {
            TenantCollectionId tenantCollectionId = buildCollectionIDDefaultEC2("1", false,false,
                    this.clientService.getTenantId());
            // Request the server using the 'tenantCollectionId' via getSampleImage GRPC and returns
            // the response 'collectionIdData'
            return this.blockingStub.getCollectionData(tenantCollectionId);
        }
    }
}
