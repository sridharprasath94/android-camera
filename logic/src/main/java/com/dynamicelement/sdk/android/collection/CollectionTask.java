package com.dynamicelement.sdk.android.collection;

import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.buildCollectionRequest;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.buildCollectionRequestDefaultEC2;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.buildImageFromBitmap;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.closeGrpcChannel;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.requireNonNull;
import static com.dynamicelement.sdk.android.misc.MddiConstants.COLLECTION_TIMEOUT;
import static com.dynamicelement.sdk.android.misc.MddiConstants.RESPONSE_NO_ERROR;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.dynamicelement.mddi.Image;
import com.dynamicelement.mddi.MddiTenantServiceGrpc;
import com.dynamicelement.mddi.RequestCollection;
import com.dynamicelement.mddi.RequestTenantCollection;
import com.dynamicelement.mddi.ResponseCollection;
import com.dynamicelement.mddi.ResponseTenantCollection;
import com.dynamicelement.sdk.android.Callback;
import com.dynamicelement.sdk.android.exceptions.ClientException;
import com.dynamicelement.sdk.android.exceptions.ExceptionType;
import com.dynamicelement.sdk.android.exceptions.ServerException;
import com.dynamicelement.sdk.android.mddiclient.ClientService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

/**
 * Create Collection background Task to perform 'create collection' api of the MDDI.
 */
public class CollectionTask extends AsyncTask<Void, Void, CollectionResult> {
    private static final String TAG = "MDDI_LOGIC_TASK_CREATE_COLLECTION";
    private final ClientService clientService;
    private final Callback<CollectionResult> callback;
    private final Bitmap imageBitmap;
    private final String cid;
    private final String sno;
    private final CollectionInfo collectionInfo;
    private final ManagedChannel managedChannel;
    private final MddiTenantServiceGrpc.MddiTenantServiceStub asyncStub;

    /**
     * @param imageBitmap            is the bitmap used to create the collection.
     * @param cid                    is the cid of the current MDDI collection request.
     * @param sno                    is the sno of the current MDDI collection request.
     * @param collectionInfo         is the list of description(or)information about the collection.
     * @param clientService          is initialised with the credentials and configuration of the
     *                               MDDI backend.
     * @param managedChannel         is the gRPC channel which is used for client server
     *                               communication.
     * @param asyncStub              is the asynchronous channel stub which is used for calling the server APIs.
     * @param collectionTaskCallback is used to observe the collection response from MDDI backend.
     */
    public CollectionTask(Bitmap imageBitmap, String cid, String sno, CollectionInfo collectionInfo,
                          ClientService clientService,
                          ManagedChannel managedChannel,
                          MddiTenantServiceGrpc.MddiTenantServiceStub asyncStub,
                          Callback<CollectionResult> collectionTaskCallback) {
        requireNonNull(imageBitmap, cid, sno, collectionInfo, clientService,
                collectionTaskCallback);
        this.clientService = clientService;
        this.callback = collectionTaskCallback;
        this.imageBitmap = imageBitmap;
        this.cid = cid;
        this.sno = sno;
        this.collectionInfo = collectionInfo;
        this.managedChannel = managedChannel;
        this.asyncStub = asyncStub;
        if (imageBitmap.getWidth() == 0 || imageBitmap.getHeight() == 0) {
            collectionTaskCallback.onError(ExceptionType.CLIENT_EXCEPTION,
                    new ClientException(imageBitmap.getWidth() == 0 ? "Width cannot be 0" :
                            "Height cannot be 0"));
            return;
        }
        int mddiImageWidth = clientService.getMddiImageSize().getWidth();
        int mddiImageHeight = clientService.getMddiImageSize().getHeight();
        if (imageBitmap.getWidth() < mddiImageWidth || imageBitmap.getHeight() < mddiImageHeight) {
            collectionTaskCallback.onError(ExceptionType.CLIENT_EXCEPTION,
                    new ClientException(imageBitmap.getWidth() < mddiImageWidth ? "Width cannot " +
                            "be " +
                            "less than " + mddiImageWidth :
                            "Height cannot be less than " + mddiImageHeight));
        }
    }

    @Override
    public CollectionResult doInBackground(Void... Void) {
        CollectionTaskRunnable collectionTaskRunnable =
                new CollectionTaskRunnable(this.clientService, asyncStub);
        return collectionTaskRunnable.run(this.imageBitmap, this.cid, this.sno,
                this.collectionInfo);
    }

    @Override
    public void onPostExecute(CollectionResult collectionResult) {
        closeGrpcChannel(TAG, this.managedChannel);
        if (collectionResult.isCollectionCreated()) {
            this.callback.onResponse(collectionResult);
            return;
        }
        this.callback.onError(ExceptionType.SERVER_EXCEPTION,
                new ServerException(collectionResult.getResponse()));
    }

    /**
     * Runnable interface - Create Collection Task.
     */

    protected interface CollectionRunnable {
        CollectionResult run(Bitmap imageBitmap, String cid, String sno,
                             CollectionInfo collectionInfo) throws ServerException;
    }

    /**
     * Makes runnable gRPC stream call to create a new collection entry in our MDDI backend.
     * Default timeout for this action is 15 sec.
     */

    protected static class CollectionTaskRunnable implements CollectionRunnable {
        protected ClientService clientService;
        protected CountDownLatch latch = new CountDownLatch(1);
        protected MddiTenantServiceGrpc.MddiTenantServiceStub asyncStub;

        protected CollectionTaskRunnable(ClientService clientService,
                                         MddiTenantServiceGrpc.MddiTenantServiceStub asyncStub) {
            this.clientService = clientService;
            this.asyncStub = asyncStub;
        }

        /**
         * @param imageBitmap    is the bitmap used to create the collection.
         * @param cid            is the cid of the current MDDI collection request.
         * @param sno            is the sno of the current MDDI collection request.
         * @param collectionInfo is the list of description(or)information about the collection.
         * @return CollectionResult
         */
        @Override
        public CollectionResult run(Bitmap imageBitmap, String cid, String sno,
                                    CollectionInfo collectionInfo) {
            CollectionResult result = new CollectionResult();
            Image sampleImage = buildImageFromBitmap(imageBitmap, sno);

            // Check whether the connect IP is default EC2 instance.
            // The server request and response are different for default EC2 instance.
            if (clientService.isDefaultEC2IPSelected()) {
                RequestTenantCollection requestTenantCollection =
                        buildCollectionRequestDefaultEC2(collectionInfo,
                                cid, sampleImage, clientService.getTenantId());
                this.asyncStub.createTenantCollection(requestTenantCollection,
                        new StreamObserver<ResponseTenantCollection>() {
                            @Override
                            public void onNext(ResponseTenantCollection response) {
                                if (response.getError().getErrorCode() != RESPONSE_NO_ERROR) {
                                    String exception = response.getError().getErrorCategory() +
                                            ":" + response.getError().getErrorCode() +
                                            " - " + response.getError().getErrorMessage();
                                    Log.d(TAG + "_ON_RESPONSE_ERROR", exception);
                                    result.setCollectionCreated(false);
                                    result.setResponse(exception);
                                    latch.countDown();
                                    return;
                                }
                                Log.d(TAG + "_ON_NEXT", requestTenantCollection.getCid() + " " +
                                        "collection is created.");
                                result.setCollectionCreated(true);
                                result.setResponse(requestTenantCollection.getCid());
                                latch.countDown();
                            }

                            @Override
                            public void onError(Throwable t) {
                                Log.d(TAG + "_ON_CONNECTION_ERROR", t.getMessage());
                                result.setCollectionCreated(false);
                                result.setResponse(t.getMessage());
                                latch.countDown();
                            }

                            @Override
                            public void onCompleted() {
                                Log.d(TAG + "_ON_COMPLETED", "Collection task( default EC2 " +
                                        "IP) is completed");
                            }
                        });
            } else {
                RequestCollection requestCollection = buildCollectionRequest(collectionInfo,
                        cid, sampleImage, clientService.getTenantId());

                this.asyncStub.createCollection(requestCollection,
                        new StreamObserver<ResponseCollection>() {
                            @Override
                            public void onNext(ResponseCollection response) {
                                if (response.getError().getErrorCode() != RESPONSE_NO_ERROR) {
                                    String exception = response.getError().getErrorCategory() +
                                            ":" + response.getError().getErrorCode() +
                                            " - " + response.getError().getErrorMessage();
                                    Log.d(TAG + "_ON_RESPONSE_ERROR", exception);
                                    result.setCollectionCreated(false);
                                    result.setResponse(exception);
                                    latch.countDown();
                                    return;
                                }
                                Log.d(TAG + "_ON_NEXT", requestCollection.getCid() + " " +
                                        "collection is created.");
                                result.setCollectionCreated(true);
                                result.setResponse(requestCollection.getCid());
                                latch.countDown();
                            }

                            @Override
                            public void onError(Throwable t) {
                                Log.d(TAG + "_ON_CONNECTION_ERROR", t.getMessage());
                                result.setCollectionCreated(false);
                                result.setResponse(t.getMessage());
                                latch.countDown();
                            }

                            @Override
                            public void onCompleted() {
                                Log.d(TAG + "_ON_COMPLETED", "Collection task is completed");
                            }
                        });
            }

            try {
                latch.await(COLLECTION_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return result;
        }
    }
}
