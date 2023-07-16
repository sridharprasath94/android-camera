package com.dynamicelement.sdk.android.ping;

import static com.dynamicelement.sdk.android.misc.MddiConstants.GET_SAMPLE_ERROR_INVALID_CID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dynamicelement.mddi.CollectionIdData;
import com.dynamicelement.mddi.MddiTenantServiceGrpc;
import com.dynamicelement.mddi.SampleImageResponse;
import com.dynamicelement.mddi.mddiError;
import com.dynamicelement.sdk.android.Callback;
import com.dynamicelement.sdk.android.exceptions.ServerException;
import com.dynamicelement.sdk.android.mddiclient.ClientService;
import com.dynamicelement.sdk.android.ping.PingTask.PingTaskRunnable;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.grpc.ManagedChannel;
import junitparams.JUnitParamsRunner;

@RunWith(JUnitParamsRunner.class)
public class PingTaskUnitTest {
    PingTaskRunnable pingTaskRunnable;
    ClientService clientService;
    MddiTenantServiceGrpc.MddiTenantServiceBlockingStub blockingStub;
    ManagedChannel managedChannel;
    PingTask pingTask;
    Callback<PingResult> pingResultCallback;

    @Before
    public void setUp() {
        this.clientService = mock(ClientService.class);
        this.blockingStub = mock(MddiTenantServiceGrpc.MddiTenantServiceBlockingStub.class);
        this.pingTaskRunnable = new PingTaskRunnable(this.clientService, this.blockingStub);
        String tenantID = "dnedev";
        when(this.clientService.getTenantId()).thenReturn(tenantID);
        this.managedChannel = mock(ManagedChannel.class);
        this.pingResultCallback = mock(Callback.class);
        this.pingTask = new PingTask(this.clientService, this.managedChannel, this.blockingStub, this.pingResultCallback);
    }

    /**
     * Test the ping task runnable.
     * Mock all the possible server responses.
     * Mock the CollectionIDData( when defaultEC2 IP is selected).
     * Test the PingResult.
     */
    @Test
    public void testPingTaskRunnableWithCollectionIdData() throws ServerException {
        mockCollectionIdData();

        PingResult pingResultExp1 = this.pingTaskRunnable.run();
        PingResult pingResultExp2 = this.pingTaskRunnable.run();
        PingResult pingResultExp3 = this.pingTaskRunnable.run();
        PingResult pingResultExp4 = this.pingTaskRunnable.run();
        PingResult pingResultExp5 = this.pingTaskRunnable.run();

        checkPingResults(pingResultExp1, pingResultExp2, pingResultExp3, pingResultExp4, pingResultExp5);
    }

    /**
     * Test the ping task runnable.
     * Mock all the possible server responses.
     * Mock the SampleImageResponse( when defaultEC2 IP is not selected).
     * Test the PingResult.
     */
    @Test
    public void testPingTaskRunnableWithSampleImageResponse() throws ServerException {
        mockSampleImageResponse();

        PingResult pingResultExp1 = this.pingTaskRunnable.run();
        PingResult pingResultExp2 = this.pingTaskRunnable.run();
        PingResult pingResultExp3 = this.pingTaskRunnable.run();
        PingResult pingResultExp4 = this.pingTaskRunnable.run();
        PingResult pingResultExp5 = this.pingTaskRunnable.run();

        checkPingResults(pingResultExp1, pingResultExp2, pingResultExp3, pingResultExp4, pingResultExp5);
    }

    /**
     * Test the ping task.
     * Mock all the possible server responses.
     * Mock the CollectionIDData( when defaultEC2 IP is selected).
     * Test the PingResult.
     */
    @Test
    public void testPingTaskWithCollectionIdData() {
        mockCollectionIdData();

        PingResult pingResultExp1 = this.pingTask.doInBackground();
        PingResult pingResultExp2 = this.pingTask.doInBackground();
        PingResult pingResultExp3 = this.pingTask.doInBackground();
        PingResult pingResultExp4 = this.pingTask.doInBackground();
        PingResult pingResultExp5 = this.pingTask.doInBackground();

        checkPingResults(pingResultExp1, pingResultExp2, pingResultExp3, pingResultExp4, pingResultExp5);
    }

    /**
     * Test the ping task.
     * Mock all the possible server responses.
     * Mock the SampleImageResponse( when defaultEC2 IP is not selected).
     * Test the PingResult.
     */
    @Test
    public void testPingTaskWithSampleImageResponse() {
        mockSampleImageResponse();

        PingResult pingResultExp1 = this.pingTask.doInBackground();
        PingResult pingResultExp2 = this.pingTask.doInBackground();
        PingResult pingResultExp3 = this.pingTask.doInBackground();
        PingResult pingResultExp4 = this.pingTask.doInBackground();
        PingResult pingResultExp5 = this.pingTask.doInBackground();

        checkPingResults(pingResultExp1, pingResultExp2, pingResultExp3, pingResultExp4, pingResultExp5);
    }

    /**
     * Mock the CollectionIDData( when defaultEC2 IP is selected).
     */
    private void mockCollectionIdData() {
        when(this.clientService.isDefaultEC2IPSelected())
                .thenReturn(true);
        CollectionIdData collectionIdData_1 = CollectionIdData.newBuilder().setAck("Test 1").build();
        CollectionIdData collectionIdData_2 = CollectionIdData.newBuilder().setAck("Test 2").
                setError(mddiError.newBuilder().setErrorCode(1000).setErrorCategory("GET DATA RPC ERROR").setErrorMessage("Invalid credentials").build()).build();
        CollectionIdData collectionIdData_3 = CollectionIdData.newBuilder().setAck("Test 3").
                setError(mddiError.newBuilder().setErrorCode(1001).setErrorCategory("GET DATA RPC ERROR").setErrorMessage("Invalid client timestamp attribute").build()).build();
        CollectionIdData collectionIdData_4 = CollectionIdData.newBuilder().setAck("Test 4").build();
        CollectionIdData collectionIdData_5 = CollectionIdData.newBuilder().setAck("Test 5").
                setError(mddiError.newBuilder().setErrorCode(GET_SAMPLE_ERROR_INVALID_CID).setErrorCategory("GET DATA RPC ERROR").setErrorMessage("Invalid CID").build()).build();

        when(this.blockingStub.getCollectionData(any()))
                .thenReturn(collectionIdData_1)
                .thenReturn(collectionIdData_2)
                .thenReturn(collectionIdData_3)
                .thenReturn(collectionIdData_4)
                .thenReturn(collectionIdData_5);
    }

    /**
     * Mock the SampleImageResponse( when defaultEC2 IP is not selected).
     */
    private void mockSampleImageResponse() {
        when(this.clientService.isDefaultEC2IPSelected())
                .thenReturn(false);
        SampleImageResponse sampleImageResponse_1 = SampleImageResponse.newBuilder().setAck("Test 1").build();
        SampleImageResponse sampleImageResponse_2 = SampleImageResponse.newBuilder().setAck("Test 2").
                setError(mddiError.newBuilder().setErrorCode(1000).setErrorCategory("GET DATA RPC ERROR").setErrorMessage("Invalid credentials").build()).build();
        SampleImageResponse sampleImageResponse_3 = SampleImageResponse.newBuilder().setAck("Test 3").
                setError(mddiError.newBuilder().setErrorCode(1001).setErrorCategory("GET DATA RPC ERROR").setErrorMessage("Invalid client timestamp attribute").build()).build();
        SampleImageResponse sampleImageResponse_4 = SampleImageResponse.newBuilder().setAck("Test 4").build();
        SampleImageResponse sampleImageResponse_5 = SampleImageResponse.newBuilder().setAck("Test 5").
                setError(mddiError.newBuilder().setErrorCode(GET_SAMPLE_ERROR_INVALID_CID).setErrorCategory("GET DATA RPC ERROR").setErrorMessage("Invalid CID").build()).build();

        when(this.blockingStub.getSampleImage(any()))
                .thenReturn(sampleImageResponse_1)
                .thenReturn(sampleImageResponse_2)
                .thenReturn(sampleImageResponse_3)
                .thenReturn(sampleImageResponse_4)
                .thenReturn(sampleImageResponse_5);
    }


    /**
     * Assert the PingResult.
     */
    private void checkPingResults(PingResult pingResultExp1, PingResult pingResultExp2, PingResult pingResultExp3, PingResult pingResultExp4, PingResult pingResultExp5) {
        Assert.assertTrue(pingResultExp1.isConnected());
        Assert.assertEquals("Test 1", pingResultExp1.getPingResponse());

        Assert.assertFalse(pingResultExp2.isConnected());
        Assert.assertEquals("GET DATA RPC ERROR:1000 - Invalid credentials", pingResultExp2.getPingResponse());

        Assert.assertFalse(pingResultExp3.isConnected());
        Assert.assertEquals("GET DATA RPC ERROR:1001 - Invalid client timestamp attribute", pingResultExp3.getPingResponse());

        Assert.assertTrue(pingResultExp4.isConnected());
        Assert.assertEquals("Test 4", pingResultExp4.getPingResponse());

        Assert.assertTrue(pingResultExp5.isConnected());
        Assert.assertEquals("Test 5", pingResultExp5.getPingResponse());
    }
}
