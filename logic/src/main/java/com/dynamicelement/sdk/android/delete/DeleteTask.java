package com.dynamicelement.sdk.android.delete;

import static com.dynamicelement.sdk.android.delete.DeleteStatus.CID_NOT_EXISTS;
import static com.dynamicelement.sdk.android.delete.DeleteStatus.DELETED;
import static com.dynamicelement.sdk.android.delete.DeleteStatus.ERROR_DELETING;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.closeGrpcChannel;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.requireNonNull;
import static com.dynamicelement.sdk.android.misc.MddiConstants.DELETE_ERROR_INVALID_CID;
import static com.dynamicelement.sdk.android.misc.MddiConstants.RESPONSE_NO_ERROR;

import android.os.AsyncTask;
import android.util.Log;

import com.dynamicelement.mddi.MddiTenantServiceGrpc;
import com.dynamicelement.mddi.RequestDeleteCollection;
import com.dynamicelement.mddi.RequestDeleteTenantCollection;
import com.dynamicelement.mddi.ResponseDeleteCollection;
import com.dynamicelement.mddi.ResponseDeleteTenantCollection;
import com.dynamicelement.mddi.mddiError;
import com.dynamicelement.sdk.android.Callback;
import com.dynamicelement.sdk.android.exceptions.ExceptionType;
import com.dynamicelement.sdk.android.exceptions.ServerException;
import com.dynamicelement.sdk.android.mddiclient.ClientService;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

/**
 * Delete background Task to perform delete a particular collection(Cid) of the MDDI.
 * Call the delete task with the given parameters and observe the response from the callback
 */
public class DeleteTask extends AsyncTask<Void, Void, DeleteResult> {
    private static final String TAG = "MDDI_LOGIC_TASK_DELETE";
    private final Callback<DeleteResult> deleteCallback;
    private final String cid;
    private final ClientService clientService;
    private final ManagedChannel managedChannel;
    private final MddiTenantServiceGrpc.MddiTenantServiceBlockingStub blockingStub;

    /**
     * @param cid            is the cid of the MDDI get sample request.
     * @param clientService  is initialised with the credentials and configuration of the MDDI
     *                       backend.
     * @param managedChannel is the gRPC channel which is used for client server communication.
     * @param blockingStub   is the blocking channel stub which is used for calling the server APIs.
     * @param deleteCallback is used to observe the delete response from MDDI backend.
     */
    public DeleteTask(String cid,
                      ClientService clientService,
                      ManagedChannel managedChannel,
                      MddiTenantServiceGrpc.MddiTenantServiceBlockingStub blockingStub,
                      Callback<DeleteResult> deleteCallback) {
        requireNonNull(cid, clientService, deleteCallback);
        this.clientService = clientService;
        this.deleteCallback = deleteCallback;
        this.managedChannel = managedChannel;
        this.blockingStub = blockingStub;
        this.cid = cid;
    }

    @Override
    public DeleteResult doInBackground(Void... Void) {
        try {
            DeleteTaskRunnable deleteTaskRunnable = new DeleteTaskRunnable(this.clientService, blockingStub);
            return deleteTaskRunnable.run(this.cid);
        } catch (ServerException e) {
            this.deleteCallback.onError(ExceptionType.SERVER_EXCEPTION, e);
            return null;
        }
    }

    @Override
    public void onPostExecute(DeleteResult deleteResult) {
        if (deleteResult == null) {
            return;
        }
        closeGrpcChannel(TAG, this.managedChannel);
        if (deleteResult.getDeleteStatus() == DELETED ||
                deleteResult.getDeleteStatus() == CID_NOT_EXISTS) {
            Log.d(TAG + "_ON_POSITIVE_RESPONSE", deleteResult.getResponse());
            this.deleteCallback.onResponse(deleteResult);
            return;
        }
        Log.d(TAG + "_ON_RESPONSE_ERROR", deleteResult.getResponse());
        this.deleteCallback.onError(ExceptionType.SERVER_EXCEPTION,
                new ServerException(deleteResult.getResponse()));
    }

    /**
     * Runnable interface - Delete Task.
     */
    protected interface DeleteRunnable {
        DeleteResult run(String cid) throws ServerException;
    }

    /**
     * Makes runnable gRPC stream call to delete a collection in our MDDI backend.
     */
    protected static class DeleteTaskRunnable implements DeleteRunnable {
        protected ClientService clientService;
        private final MddiTenantServiceGrpc.MddiTenantServiceBlockingStub blockingStub;

        protected DeleteTaskRunnable(ClientService clientService,
                                     MddiTenantServiceGrpc.MddiTenantServiceBlockingStub blockingStub) {
            this.clientService = clientService;
            this.blockingStub = blockingStub;
        }

        /**
         * @param cid is the cid of the current MDDI request.
         * @return DeleteResult
         * @throws ServerException
         */
        @Override
        public DeleteResult run(String cid) throws ServerException {
            DeleteResult deleteResult = new DeleteResult();
            String defaultMessage;
            mddiError mddiError;

            try {
                // Check whether the connect IP is default EC2 instance.
                // The server request and response are different for default EC2 instance.
                if (this.clientService.isDefaultEC2IPSelected()) {
                    ResponseDeleteTenantCollection responseDeleteTenantCollection =
                            getDeleteResponseDefaultEc2(cid, clientService);
                    mddiError = responseDeleteTenantCollection.getError();
                    defaultMessage = responseDeleteTenantCollection.getAck();
                } else {
                    ResponseDeleteCollection responseDeleteCollection =
                            getDeleteResponse(cid, clientService);
                    mddiError = responseDeleteCollection.getError();
                    defaultMessage = responseDeleteCollection.getAck();
                }
                if (mddiError.getErrorCode() != RESPONSE_NO_ERROR) {
                    deleteResult.setDeleteStatus(mddiError.getErrorCode() == DELETE_ERROR_INVALID_CID ?
                            DeleteStatus.CID_NOT_EXISTS : ERROR_DELETING);
                    deleteResult.setResponse(mddiError.getErrorCategory() +
                            ":" + mddiError.getErrorCode() +
                            " - " + mddiError.getErrorMessage());
                    return deleteResult;
                }
                deleteResult.setDeleteStatus(DELETED);
                deleteResult.setResponse(defaultMessage);
                return deleteResult;

            } catch (StatusRuntimeException e) {
                deleteResult.setDeleteStatus(ERROR_DELETING);
                deleteResult.setResponse(e.getMessage());
                return deleteResult;
            }
        }

        /**
         * Delete Response for the given delete request.
         *
         * @param cid is the cid of the current delete request.
         */
        private ResponseDeleteCollection getDeleteResponse(String cid,
                                                           ClientService clientService) {
            requireNonNull(cid);
            RequestDeleteCollection requestDeleteCollection = RequestDeleteCollection.newBuilder().
                    setTimestamp(Long.toString(System.currentTimeMillis())).setVersionId("V1.0").
                    setCid(cid).setTenantId(clientService.getTenantId()).build();
            // Request the server using the 'requestDeleteCollection' via delete collection GRPC and
            // returns the response 'responseDeleteCollection'
            return this.blockingStub.deleteCollection(requestDeleteCollection);
        }

        /**
         * Delete Response for the given delete request for the default EC2 instance.
         *
         * @param cid is the cid of the current delete request.
         */
        private ResponseDeleteTenantCollection getDeleteResponseDefaultEc2(String cid,
                                                                           ClientService clientService) {
            requireNonNull(cid);
            RequestDeleteTenantCollection requestDeleteCollection =
                    RequestDeleteTenantCollection.newBuilder().
                            setTimestamp(Long.toString(System.currentTimeMillis())).setVersionId(
                            "V1.0").setCid(cid).setTenantId(clientService.getTenantId()).build();
            return this.blockingStub.deleteTenantCollection(requestDeleteCollection);
        }
    }
}
