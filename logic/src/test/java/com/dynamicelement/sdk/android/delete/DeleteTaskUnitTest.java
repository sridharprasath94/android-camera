package com.dynamicelement.sdk.android.delete;

import static com.dynamicelement.sdk.android.misc.MddiConstants.DELETE_ERROR_INVALID_CID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dynamicelement.mddi.MddiTenantServiceGrpc;
import com.dynamicelement.mddi.ResponseDeleteCollection;
import com.dynamicelement.mddi.ResponseDeleteTenantCollection;
import com.dynamicelement.mddi.mddiError;
import com.dynamicelement.sdk.android.Callback;
import com.dynamicelement.sdk.android.delete.DeleteTask.DeleteTaskRunnable;
import com.dynamicelement.sdk.android.exceptions.ServerException;
import com.dynamicelement.sdk.android.mddiclient.ClientService;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.grpc.ManagedChannel;
import junitparams.JUnitParamsRunner;

@RunWith(JUnitParamsRunner.class)
public class DeleteTaskUnitTest {
    DeleteTaskRunnable deleteTaskRunnable;
    ClientService clientService;
    MddiTenantServiceGrpc.MddiTenantServiceBlockingStub blockingStub;
    ManagedChannel managedChannel;
    DeleteTask deleteTask;
    Callback<DeleteResult> deleteResultCallback;

    @Before
    public void setUp() {
        this.clientService = mock(ClientService.class);
        this.blockingStub = mock(MddiTenantServiceGrpc.MddiTenantServiceBlockingStub.class);
        this.deleteTaskRunnable = new DeleteTaskRunnable(this.clientService, this.blockingStub);
        String tenantID = "dnedev";
        when(this.clientService.getTenantId()).thenReturn(tenantID);
        this.managedChannel = mock(ManagedChannel.class);
        this.deleteResultCallback = mock(Callback.class);
        this.deleteTask = new DeleteTask("1", this.clientService, this.managedChannel, this.blockingStub, this.deleteResultCallback);
    }

    /**
     * Test the DeleteTaskRunnable.
     * Mock all the possible server responses.
     * Mock the ResponseDeleteTenantCollection( when defaultEC2 IP is selected).
     * Test the DeleteResult.
     */
    @Test
    public void testDeleteTaskRunnableWithResponseDeleteTenantCollection() throws ServerException {
        mockResponseDeleteTenantCollection();

        DeleteResult deleteResultExp1 = this.deleteTaskRunnable.run("1");
        DeleteResult deleteResultExp2 = this.deleteTaskRunnable.run("1");
        DeleteResult deleteResultExp3 = this.deleteTaskRunnable.run("1");
        DeleteResult deleteResultExp4 = this.deleteTaskRunnable.run("1");
        DeleteResult deleteResultExp5 = this.deleteTaskRunnable.run("1");

        checkDeleteResults(deleteResultExp1, deleteResultExp2, deleteResultExp3, deleteResultExp4, deleteResultExp5);
    }

    /**
     * Test the DeleteTaskRunnable.
     * Mock all the possible server responses.
     * Mock the ResponseDeleteCollection( when defaultEC2 IP is not selected).
     * Test the DeleteResult.
     */
    @Test
    public void testDeleteTaskRunnableWithResponseDeleteCollection() throws ServerException {
        mockResponseCollection();

        DeleteResult deleteResultExp1 = this.deleteTaskRunnable.run("1");
        DeleteResult deleteResultExp2 = this.deleteTaskRunnable.run("1");
        DeleteResult deleteResultExp3 = this.deleteTaskRunnable.run("1");
        DeleteResult deleteResultExp4 = this.deleteTaskRunnable.run("1");
        DeleteResult deleteResultExp5 = this.deleteTaskRunnable.run("1");

        checkDeleteResults(deleteResultExp1, deleteResultExp2, deleteResultExp3, deleteResultExp4, deleteResultExp5);
    }

    /**
     * Test the DeleteTaskRunnable.
     * Mock all the possible server responses.
     * Mock the ResponseDeleteTenantCollection( when defaultEC2 IP is selected).
     * Test the DeleteResult.
     */
    @Test
    public void testDeleteTaskWithResponseDeleteTenantCollection() {
        mockResponseDeleteTenantCollection();

        DeleteResult deleteResultExp1 = this.deleteTask.doInBackground();
        DeleteResult deleteResultExp2 = this.deleteTask.doInBackground();
        DeleteResult deleteResultExp3 = this.deleteTask.doInBackground();
        DeleteResult deleteResultExp4 = this.deleteTask.doInBackground();
        DeleteResult deleteResultExp5 = this.deleteTask.doInBackground();

        checkDeleteResults(deleteResultExp1, deleteResultExp2, deleteResultExp3, deleteResultExp4, deleteResultExp5);
    }


    /**
     * Test the DeleteTaskRunnable.
     * Mock all the possible server responses.
     * Mock the ResponseDeleteCollection( when defaultEC2 IP is not selected).
     * Test the DeleteResult.
     */
    @Test
    public void testDeleteTaskWithResponseDeleteCollection() {
        mockResponseCollection();

        DeleteResult deleteResultExp1 = this.deleteTask.doInBackground();
        DeleteResult deleteResultExp2 = this.deleteTask.doInBackground();
        DeleteResult deleteResultExp3 = this.deleteTask.doInBackground();
        DeleteResult deleteResultExp4 = this.deleteTask.doInBackground();
        DeleteResult deleteResultExp5 = this.deleteTask.doInBackground();

        checkDeleteResults(deleteResultExp1, deleteResultExp2, deleteResultExp3, deleteResultExp4, deleteResultExp5);
    }

    /**
     * Mock the ResponseDeleteTenantCollection( when defaultEC2 IP is selected).
     */
    private void mockResponseDeleteTenantCollection() {
        when(this.clientService.isDefaultEC2IPSelected())
                .thenReturn(true);
        ResponseDeleteTenantCollection responseDeleteTenantCollection_1 = ResponseDeleteTenantCollection.newBuilder().setAck("Test 1").build();
        ResponseDeleteTenantCollection responseDeleteTenantCollection_2 = ResponseDeleteTenantCollection.newBuilder().setAck("Test 2").
                setError(mddiError.newBuilder().setErrorCode(1000).setErrorCategory("DELETE RPC ERROR").setErrorMessage("Invalid credentials").build()).build();
        ResponseDeleteTenantCollection responseDeleteTenantCollection_3 = ResponseDeleteTenantCollection.newBuilder().setAck("Test 3").
                setError(mddiError.newBuilder().setErrorCode(1001).setErrorCategory("DELETE RPC ERROR").setErrorMessage("Invalid client timestamp attribute").build()).build();
        ResponseDeleteTenantCollection responseDeleteTenantCollection_4 = ResponseDeleteTenantCollection.newBuilder().setAck("Test 4").build();
        ResponseDeleteTenantCollection responseDeleteTenantCollection_5 = ResponseDeleteTenantCollection.newBuilder().setAck("Test 5").
                setError(mddiError.newBuilder().setErrorCode(DELETE_ERROR_INVALID_CID).setErrorCategory("DELETE RPC ERROR").setErrorMessage("Invalid CID").build()).build();

        when(this.blockingStub.deleteTenantCollection(any()))
                .thenReturn(responseDeleteTenantCollection_1)
                .thenReturn(responseDeleteTenantCollection_2)
                .thenReturn(responseDeleteTenantCollection_3)
                .thenReturn(responseDeleteTenantCollection_4)
                .thenReturn(responseDeleteTenantCollection_5);
    }

    /**
     * Mock the ResponseDeleteCollection( when defaultEC2 IP is not selected).
     */
    private void mockResponseCollection() {
        when(this.clientService.isDefaultEC2IPSelected())
                .thenReturn(false);
        ResponseDeleteCollection responseDeleteCollection_1 = ResponseDeleteCollection.newBuilder().setAck("Test 1").build();
        ResponseDeleteCollection responseDeleteCollection_2 = ResponseDeleteCollection.newBuilder().setAck("Test 2").
                setError(mddiError.newBuilder().setErrorCode(1000).setErrorCategory("DELETE RPC ERROR").setErrorMessage("Invalid credentials").build()).build();
        ResponseDeleteCollection responseDeleteCollection_3 = ResponseDeleteCollection.newBuilder().setAck("Test 3").
                setError(mddiError.newBuilder().setErrorCode(1001).setErrorCategory("DELETE RPC ERROR").setErrorMessage("Invalid client timestamp attribute").build()).build();
        ResponseDeleteCollection responseDeleteCollection_4 = ResponseDeleteCollection.newBuilder().setAck("Test 4").build();
        ResponseDeleteCollection responseDeleteCollection_5 = ResponseDeleteCollection.newBuilder().setAck("Test 5").
                setError(mddiError.newBuilder().setErrorCode(DELETE_ERROR_INVALID_CID).setErrorCategory("DELETE RPC ERROR").setErrorMessage("Invalid CID").build()).build();

        when(this.blockingStub.deleteCollection(any()))
                .thenReturn(responseDeleteCollection_1)
                .thenReturn(responseDeleteCollection_2)
                .thenReturn(responseDeleteCollection_3)
                .thenReturn(responseDeleteCollection_4)
                .thenReturn(responseDeleteCollection_5);
    }

    /**
     * Assert the DeleteResult.
     */
    private void checkDeleteResults(DeleteResult deleteResultExp1, DeleteResult deleteResultExp2, DeleteResult deleteResultExp3, DeleteResult deleteResultExp4, DeleteResult deleteResultExp5) {
        Assert.assertEquals(DeleteStatus.DELETED, deleteResultExp1.getDeleteStatus());
        Assert.assertEquals("Test 1", deleteResultExp1.getResponse());

        Assert.assertEquals(DeleteStatus.ERROR_DELETING, deleteResultExp2.getDeleteStatus());
        Assert.assertEquals("DELETE RPC ERROR:1000 - Invalid credentials", deleteResultExp2.getResponse());

        Assert.assertEquals(DeleteStatus.ERROR_DELETING, deleteResultExp3.getDeleteStatus());
        Assert.assertEquals("DELETE RPC ERROR:1001 - Invalid client timestamp attribute", deleteResultExp3.getResponse());

        Assert.assertEquals(DeleteStatus.DELETED, deleteResultExp4.getDeleteStatus());
        Assert.assertEquals("Test 4", deleteResultExp4.getResponse());

        Assert.assertEquals(DeleteStatus.CID_NOT_EXISTS, deleteResultExp5.getDeleteStatus());
        Assert.assertEquals("DELETE RPC ERROR:" + DELETE_ERROR_INVALID_CID + " - Invalid CID", deleteResultExp5.getResponse());
    }
}
