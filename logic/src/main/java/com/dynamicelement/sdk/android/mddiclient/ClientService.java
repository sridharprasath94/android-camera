package com.dynamicelement.sdk.android.mddiclient;

import static com.dynamicelement.mddi.MddiTenantServiceGrpc.newBlockingStub;
import static com.dynamicelement.mddi.MddiTenantServiceGrpc.newStub;
import static com.dynamicelement.sdk.android.exceptions.ExceptionType.CLIENT_EXCEPTION;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.requireNonNull;
import static com.dynamicelement.sdk.android.mddiclient.MddiVariables.MIN_HEIGHT;
import static com.dynamicelement.sdk.android.mddiclient.MddiVariables.MIN_WIDTH;
import static com.dynamicelement.sdk.android.mddiclient.MddiVariables.MddiImageSize;
import static com.dynamicelement.sdk.android.mddiclient.MddiVariables.MddiImageSize.FORMAT_480X640;
import static com.dynamicelement.sdk.android.misc.MddiConstants.DEFAULT_MDDI_THRESHOLD;
import static com.dynamicelement.sdk.android.misc.SSLUtil.createSslFactoryContext;
import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;
import static io.grpc.stub.MetadataUtils.attachHeaders;

import android.graphics.Bitmap;
import android.util.Log;

import com.dynamicelement.mddi.MddiTenantServiceGrpc;
import com.dynamicelement.sdk.android.Callback;
import com.dynamicelement.sdk.android.add.AddCallback;
import com.dynamicelement.sdk.android.add.AddTask;
import com.dynamicelement.sdk.android.collection.CollectionInfo;
import com.dynamicelement.sdk.android.collection.CollectionResult;
import com.dynamicelement.sdk.android.collection.CollectionTask;
import com.dynamicelement.sdk.android.delete.DeleteResult;
import com.dynamicelement.sdk.android.delete.DeleteTask;
import com.dynamicelement.sdk.android.exceptions.ConfigurationException;
import com.dynamicelement.sdk.android.getsample.GetSampleResult;
import com.dynamicelement.sdk.android.getsample.GetSampleTask;
import com.dynamicelement.sdk.android.misc.InstanceType;
import com.dynamicelement.sdk.android.ping.PingResult;
import com.dynamicelement.sdk.android.ping.PingTask;
import com.dynamicelement.sdk.android.search.SearchCallBack;
import com.dynamicelement.sdk.android.search.SearchTask;
import com.dynamicelement.sdk.android.testMddiImage.TestMddiImageResult;
import com.dynamicelement.sdk.android.testMddiImage.TestMddiImageTask;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.okhttp.OkHttpChannelBuilder;

/**
 * Client service to handle all the MDDI related channel and api services.
 */
public class ClientService {
    private static final String TAG = "MDDI_LOGIC_TASK_CLIENT_SERVICE";
    private static final String IPV4_REGEX = "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." + "(25" +
            "[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." + "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\" +
            "." + "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
    private static final Pattern IPv4_PATTERN = Pattern.compile(IPV4_REGEX);
    private final String host;
    private final Integer port;
    private final String hostName;
    private final String userName;
    private final String password;
    private final String userID;
    private final String caCert;
    private final String tenantId;
    private final boolean isEC2IP;
    private float mddiThreshold;
    private final InstanceType instanceType;
    private String exceptionMessage;
    protected boolean searchTaskCreated = false;
    protected boolean addTaskCreated = false;
    protected boolean testMddiTaskCreated = false;
    private SearchTask searchTask;
    private AddTask addTask;
    private boolean isAlreadyStopped = false;
    private TestMddiImageTask testMddiImageTask;
    private MddiImageSize currentMddiImageSize = FORMAT_480X640;
    private boolean continuousSearch;
    private boolean continuousAdd;

    /**
     * @param host         is the IP address of the backend MDDI service.
     * @param port         is the port number of the backend MDDI service.
     * @param userName     is the username for connecting to the MDDI service.
     * @param password     is the password for connecting to the MDDI service.
     * @param userID       is the userID for connecting to the MDDI service.
     * @param hostName     is the hostname for connecting to the MDDI service.
     * @param caCert       is the 'CA' certificate for connecting to the MDDI service.
     * @param InstanceType is the instance type. Possible values are DB_SNO, IVF and IVF_SNO.
     *                     Provide the correct instance type for the given IP.
     * @throws ConfigurationException
     */
    public ClientService(String host, Integer port, String userName, String password,
                         String userID, String hostName, String caCert, InstanceType InstanceType
            , String tenantId, boolean isEC2IP) throws ConfigurationException {
        requireNonNull(host, port, userName, password, userID, hostName, caCert, InstanceType);
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.userID = userID;
        this.hostName = hostName;
        this.caCert = caCert;
        this.instanceType = InstanceType;
        this.tenantId = tenantId;
        this.isEC2IP = isEC2IP;
        this.mddiThreshold = DEFAULT_MDDI_THRESHOLD;
        MIN_WIDTH = this.currentMddiImageSize.getWidth();
        MIN_HEIGHT = this.currentMddiImageSize.getHeight();

        if (!this.checkConfigurationParameters()) {
            throw new ConfigurationException(this.exceptionMessage);
        }

        Log.d(TAG + "_INITIALISED", "Client service is initialised");
    }

    /**
     * Set the Mddi threshold value.
     */
    public ClientService updateMddiThreshold(float mddiThreshold) {
        this.mddiThreshold = mddiThreshold;
        return this;
    }

    public MddiImageSize getMddiImageSize() {
        return this.currentMddiImageSize;
    }

    /**
     * Update the Mddi image size to either 480*640 or 512*512
     */
    public ClientService updateMddiImageSize(MddiImageSize mddiImageSize) {
        this.currentMddiImageSize = mddiImageSize;
        MIN_WIDTH = this.currentMddiImageSize.getWidth();
        MIN_HEIGHT = this.currentMddiImageSize.getHeight();
        return this;
    }

    /**
     * Get the Mddi threshold value.
     */
    public float getMddiThreshold() {
        return this.mddiThreshold;
    }

    public String getHost() {
        return host;
    }

    public String getTenantId() {
        return tenantId;
    }

    public InstanceType getInstanceType() {
        return instanceType;
    }

    public boolean isDefaultEC2IPSelected() {
        return this.isEC2IP;
    }

    /**
     * Create a new session for the MDDI client.
     * To make sure that closure of a grpc channel of one session does not affect other sessions.
     */
    private ClientService createNewSession() {
        return new ClientService(this.host, this.port, this.userName, this.password, this.userID,
                this.hostName, this.caCert, this.instanceType, this.tenantId, this.isEC2IP)
                .updateMddiImageSize(this.currentMddiImageSize)
                .updateMddiThreshold(this.mddiThreshold);
    }

    /**
     * Create the gRPC channel.
     */
    private ManagedChannel createGrpcChannel() {
        return OkHttpChannelBuilder.forAddress(this.host, this.port).
                keepAliveTime(20000, TimeUnit.MILLISECONDS).
                keepAliveTimeout(20000, TimeUnit.MILLISECONDS).
                overrideAuthority(this.hostName).sslSocketFactory(createSslFactoryContext(this.caCert)).
                build();
    }

    /**
     * Add the metadata for the blocking stub and the asynchronous stub.
     */
    private Metadata createMetaData() {
        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of("username", ASCII_STRING_MARSHALLER), this.userName);
        metadata.put(Metadata.Key.of("password", ASCII_STRING_MARSHALLER), this.password);
        metadata.put(Metadata.Key.of("userid", ASCII_STRING_MARSHALLER), this.userID);
        return metadata;
    }

    /**
     * Check the configuration.
     */
    boolean checkConfigurationParameters() {
        if (!IPv4_PATTERN.matcher(this.host).matches()) {
            this.exceptionMessage = "Wrong IP format";
            return false;
        }
        if (this.port != 443) {
            this.exceptionMessage = "Wrong port";
            return false;
        }
        //Finally check whether the certificate is in correct format
        createSslFactoryContext(this.caCert);
        return true;
    }

    /**
     * Check the ping of the MDDI backend with callback - AsyncTask.
     *
     * @param pingCallback is used to observe the ping response from MDDI backend.
     */
    public void checkConnection(Callback<PingResult> pingCallback) {
        ChannelConfiguration configuration = new ChannelConfiguration(this, ChannelStub.BLOCKING);
        PingTask pingTask = new PingTask(configuration.getClientService(),
                configuration.getManagedChannel(),
                configuration.getBlockingStub(), pingCallback);
        pingTask.execute();
    }

    /**
     * Task to get sample image of a particular collection from the MDDI backend with callback -
     * AsyncTask.
     *
     * @param cid               is the cid of the MDDI get sample request.
     * @param imageNeeded       set this to true when the image should be sent along with the response. If not needed, set it to false.
     * @param getSampleCallback is used to observe the get sample response from MDDI backend.
     */
    public void getSample(String cid, boolean imageNeeded, Callback<GetSampleResult> getSampleCallback) {
        requireNonNull(cid, getSampleCallback);
        ChannelConfiguration configuration = new ChannelConfiguration(this, ChannelStub.BLOCKING);
        GetSampleTask getSampleTask = new GetSampleTask(cid, imageNeeded, configuration.getClientService(),
                configuration.getManagedChannel(),
                configuration.getBlockingStub(), getSampleCallback);
        getSampleTask.execute();
    }

    /**
     * Task to delete a particular collection from the MDDI backend.
     *
     * @param cid            is the cid of the MDDI delete request.
     * @param deleteCallback is used to observe the delete response from MDDI backend.
     */
    public void deleteCollection(String cid, Callback<DeleteResult> deleteCallback) {
        requireNonNull(cid, deleteCallback);
        if (!this.isEC2IP) {
            deleteCallback.onError(CLIENT_EXCEPTION, new Exception("Delete collection call can only be performed on default EC2 instance"));
            return;
        }
        ChannelConfiguration configuration = new ChannelConfiguration(this, ChannelStub.BLOCKING);
        DeleteTask deleteTask = new DeleteTask(cid, configuration.getClientService(),
                configuration.getManagedChannel(),
                configuration.getBlockingStub(), deleteCallback);
        deleteTask.execute();
    }

    /**
     * Create the collection task for the given cid, sno and bitmap.
     *
     * @param image                  is the bitmap used to create the collection.
     * @param cid                    is the collection ID of the current MDDI request.
     * @param sno                    is the SNO of the current MDDI request.
     * @param collectionInfo         is the collection Info required to create a new collection.
     * @param collectionTaskCallback is used to observe the collection response from MDDI backend.
     */
    public void createCollection(Bitmap image, String cid, String sno, CollectionInfo collectionInfo,
                                 Callback<CollectionResult> collectionTaskCallback) {
        requireNonNull(image, cid, sno, collectionTaskCallback);
        if (!this.isEC2IP) {
            collectionTaskCallback.onError(CLIENT_EXCEPTION, new Exception("Create collection call can only be performed on default EC2 instance"));
            return;
        }
        ChannelConfiguration configuration = new ChannelConfiguration(this, ChannelStub.ASYNCHRONOUS);
        CollectionTask collectionTask = new CollectionTask(image, cid, sno, collectionInfo,
                configuration.getClientService(), configuration.getManagedChannel(),
                configuration.getAsyncStub(), collectionTaskCallback);
        collectionTask.execute();
    }

    /**
     * Task to add the image(in bitmap format) to the MDDI backend with given cid and sno
     *
     * @param image         is the image in bitmap format which is to be added to the MDDI
     *                      backend.
     * @param cid           is the cid of the current MDDI request.
     * @param sno           is the sno of the current MDDI request.
     * @param continuousAdd indicates whether the add is continuous or is finite.
     * @param addCallback   is used to observe the add response from MDDI backend.
     */
    public void addImage(Bitmap image, String cid, String sno, boolean continuousAdd,
                         AddCallback addCallback) {
        requireNonNull(image, cid, sno, addCallback);
        if (this.isAlreadyStopped()) {
            return;
        }
        this.continuousAdd = continuousAdd;
        if (this.instanceType == InstanceType.IVF && this.isDefaultEC2IPSelected()) {
            addCallback.onError(CLIENT_EXCEPTION, new Exception("Add call cannot be performed on default EC2 IP or IVF type of instance"));
            return;
        }
        if (continuousAdd && !this.addTaskCreated) {
            ChannelConfiguration configuration = new ChannelConfiguration(this, ChannelStub.ASYNCHRONOUS);
            this.addTask = new AddTask(configuration.getClientService(), true, configuration.getManagedChannel(),
                    configuration.getAsyncStub(), addCallback);
            this.addTaskCreated = true;
        }
        if (!continuousAdd) {
            ChannelConfiguration configuration = new ChannelConfiguration(this, ChannelStub.ASYNCHRONOUS);
            this.addTask = new AddTask(configuration.getClientService(), false, configuration.getManagedChannel(),
                    configuration.getAsyncStub(), addCallback);
            this.addTaskCreated = true;
        }
        this.addTask.execute(image, image.getWidth(), image.getHeight(), cid, sno);
    }

    /**
     * Task to search the image from the MDDI backend with given cid and sno.
     *
     * @param image            is the image in bitmap format which is to be searched from the
     *                         MDDI backend.
     * @param cid              is the cid of the current MDDI request.
     * @param sno              is the sno of the current MDDI request.
     * @param continuousSearch is set as 'true' makes the MDDI search continuous. The search
     *                         process will not
     *                         be stopped automatically(The connection will be open).'stopSearch'
     *                         should be
     *                         called to stop the MDDI search.
     *                         Set as 'false' makes the MDDI search finite. It will open the
     *                         connection for only one
     *                         MDDI request and then it will be closed automatically.
     * @param searchCallBack   is used to observe the search response from MDDI backend.
     */
    public void searchImage(Bitmap image, String cid, String sno, boolean continuousSearch,
                            SearchCallBack searchCallBack) {
        requireNonNull(image, cid, sno, searchCallBack);
        if (this.isAlreadyStopped()) {
            return;
        }
        this.continuousSearch = continuousSearch;
        if (this.instanceType == InstanceType.IVF && this.isDefaultEC2IPSelected()) {
            searchCallBack.onError(CLIENT_EXCEPTION, new Exception("Search call cannot be performed on default EC2 IP or IVF type of instance"));
            return;
        }
        if (continuousSearch && !this.searchTaskCreated) {
            ChannelConfiguration configuration = new ChannelConfiguration(this, ChannelStub.ASYNCHRONOUS);
            this.searchTask = new SearchTask(configuration.getClientService(), true, configuration.getManagedChannel(),
                    configuration.getAsyncStub(), searchCallBack);
            this.searchTaskCreated = true;
        }
        if (!continuousSearch) {
            ChannelConfiguration configuration = new ChannelConfiguration(this, ChannelStub.ASYNCHRONOUS);
            this.searchTask = new SearchTask(configuration.getClientService(), false, configuration.getManagedChannel(),
                    configuration.getAsyncStub(), searchCallBack);
            this.searchTaskCreated = true;
        }
        this.searchTask.execute(image, image.getWidth(), image.getHeight(), cid, sno);
    }

    /**
     * Task to check whether the image is suitable for adding to the MDDI backend.
     *
     * @param image                       is the image in bitmap format which is to be tested whether it is suitable for MDDI backend.
     * @param cid                         is the cid of the current MDDI request.
     * @param sno                         is the sno of the current MDDI request.
     * @param testMddiImageResultCallback is used to observe the test Mddi Image response from MDDI backend.
     */
    public void testMddiImage(Bitmap image, String cid, String sno, Callback<TestMddiImageResult> testMddiImageResultCallback) {
        requireNonNull(image, cid, sno, testMddiImageResultCallback);
        if (this.isAlreadyStopped()) {
            return;
        }
        if (this.instanceType == InstanceType.IVF || !this.isDefaultEC2IPSelected()) {
            String exception = this.instanceType == InstanceType.IVF ?
                    "Test mddi image call is currently not available for IVF type of instance" :
                    "Test mddi image call can only be performed on default EC2 instance";
            testMddiImageResultCallback.onError(CLIENT_EXCEPTION, new Exception(exception));
            return;
        }
        if (!this.testMddiTaskCreated) {
            ChannelConfiguration configuration = new ChannelConfiguration(this, ChannelStub.ASYNCHRONOUS);
            this.testMddiImageTask = new TestMddiImageTask(configuration.getClientService(), configuration.getManagedChannel(),
                    configuration.getAsyncStub(), testMddiImageResultCallback);
            this.testMddiTaskCreated = true;
        }
        this.testMddiImageTask.execute(image, image.getWidth(), image.getHeight(), cid, sno);
    }

    private boolean isAlreadyStopped() {
        return this.isAlreadyStopped;
    }

    public ClientService resetStopStatus() {
        this.isAlreadyStopped = false;
        return this;
    }

    /**
     * Stop the running search,add and test image MDDI tasks.
     */
    public void stopMddi() {
        if (this.searchTaskCreated) {
            this.searchTaskCreated = false;
            if (!continuousSearch) {
                return;
            }
            this.searchTask.stopSearch();
        }
        if (this.addTaskCreated) {
            this.addTaskCreated = false;
            if (!continuousAdd) {
                return;
            }
            this.addTask.stopAdd();
        }
        if (this.testMddiTaskCreated) {
            this.testMddiTaskCreated = false;
            this.testMddiImageTask.stopTestMddi();
        }
        this.isAlreadyStopped = true;
    }

    private static class ChannelConfiguration {
        private final ManagedChannel managedChannel;
        private final ClientService clientService;
        private final MddiTenantServiceGrpc.MddiTenantServiceStub asyncStub;
        private final MddiTenantServiceGrpc.MddiTenantServiceBlockingStub blockingStub;

        private ChannelConfiguration(ClientService clientService, ChannelStub channelStub) {
            ClientService clientServiceNew = clientService.createNewSession();
            ManagedChannel managedChannel = clientServiceNew.createGrpcChannel();
            Metadata metadata = clientServiceNew.createMetaData();
            this.managedChannel = managedChannel;
            this.clientService = clientServiceNew;
            this.blockingStub = channelStub == ChannelStub.BLOCKING ?
                    attachHeaders(newBlockingStub(this.managedChannel), metadata)
                            .withCompression("gzip").withDeadlineAfter(10,
                            TimeUnit.SECONDS) : null;
            this.asyncStub = channelStub == ChannelStub.ASYNCHRONOUS ?
                    attachHeaders(newStub(this.managedChannel), metadata)
                            .withCompression("gzip") : null;
        }

        private ManagedChannel getManagedChannel() {
            return this.managedChannel;
        }

        private ClientService getClientService() {
            return this.clientService;
        }

        private MddiTenantServiceGrpc.MddiTenantServiceStub getAsyncStub() {
            return asyncStub;
        }

        private MddiTenantServiceGrpc.MddiTenantServiceBlockingStub getBlockingStub() {
            return blockingStub;
        }
    }

    private enum ChannelStub {
        BLOCKING,
        ASYNCHRONOUS
    }
}
