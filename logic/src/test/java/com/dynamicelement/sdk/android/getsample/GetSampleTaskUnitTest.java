package com.dynamicelement.sdk.android.getsample;

import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.buildBitmapFromIntegerList;
import static com.dynamicelement.sdk.android.misc.MddiConstants.GET_SAMPLE_ERROR_INVALID_CID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

import com.dynamicelement.mddi.CollectionIdData;
import com.dynamicelement.mddi.Image;
import com.dynamicelement.mddi.MddiTenantServiceGrpc;
import com.dynamicelement.mddi.SampleImageResponse;
import com.dynamicelement.mddi.mddiError;
import com.dynamicelement.sdk.android.Callback;
import com.dynamicelement.sdk.android.exceptions.ServerException;
import com.dynamicelement.sdk.android.mddiclient.ClientService;
import com.dynamicelement.sdk.android.mddiclient.MddiParameters;
import com.dynamicelement.sdk.android.mddiclient.MddiVariables;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import io.grpc.ManagedChannel;
import junitparams.JUnitParamsRunner;

@RunWith(JUnitParamsRunner.class)
public class GetSampleTaskUnitTest {

    GetSampleTask.GetSampleTaskRunnable getSampleTaskRunnable;
    ClientService clientService;
    MddiTenantServiceGrpc.MddiTenantServiceBlockingStub blockingStub;
    Bitmap bitmap;
    Image image;
    ManagedChannel managedChannel;
    GetSampleTask getSampleTask;
    Callback<GetSampleResult> getSampleResultCallback;

    @SuppressLint("CheckResult")
    @Before
    public void setUp() {
        this.clientService = mock(ClientService.class);
        this.blockingStub = mock(MddiTenantServiceGrpc.MddiTenantServiceBlockingStub.class);
        this.getSampleTaskRunnable = new GetSampleTask.GetSampleTaskRunnable(this.clientService, this.blockingStub);
        List<Integer> pixelsList;
        int[] pixels = new int[480 * 640];
        pixelsList = new ArrayList<>(pixels.length);
        //Add the pixels to the pixelsList
        for (int j : pixels) {
            pixelsList.add(j);
        }
        //Build the Mddi image using the imagePixels from the Bitmap
        image = Image.newBuilder().addAllImagePixels(pixelsList).setImageName("1").setImageWidth(480).
                setImageHeight(640).setSno("1").setImageFormat(0).setImageExt("jpeg").build();

        this.bitmap = mock(Bitmap.class);
        String tenantID = "dnedev";
        when(this.clientService.getTenantId()).thenReturn(tenantID);
        when(this.clientService.getMddiImageSize()).thenReturn(MddiVariables.MddiImageSize.FORMAT_480X640);
        this.managedChannel = mock(ManagedChannel.class);
        this.getSampleResultCallback = mock(Callback.class);
        this.getSampleTask = new GetSampleTask("1", true, this.clientService, this.managedChannel, this.blockingStub, this.getSampleResultCallback);
    }

    /**
     * Test the GetSampleTaskRunnable.
     * Mock all the possible server responses.
     * Mock the CollectionIDData( when defaultEC2 IP is selected).
     * Test the GetSampleResult.
     */
    @Test
    public void testGetSampleTaskRunnableWithCollectionIdData() throws ServerException {
        try (MockedStatic<MddiParameters> mockedStatic = Mockito.mockStatic(MddiParameters.class)) {
            mockCollectionIdData();

            GetSampleResult getSampleResultExp1 = getSampleTaskRunnable.run("1", true);
            GetSampleResult getSampleResultExp2 = getSampleTaskRunnable.run("1", true);
            GetSampleResult getSampleResultExp3 = getSampleTaskRunnable.run("1", true);
            GetSampleResult getSampleResultExp4 = getSampleTaskRunnable.run("1", true);
            GetSampleResult getSampleResultExp5 = getSampleTaskRunnable.run("1", true);
            when(this.clientService.getMddiImageSize()).thenReturn(MddiVariables.MddiImageSize.FORMAT_512X512);
            GetSampleResult getSampleResultExp6 = getSampleTaskRunnable.run("1", true);
            GetSampleResult getSampleResultExp7 = getSampleTaskRunnable.run("1", false);

            checkGetSampleResults(getSampleResultExp1, getSampleResultExp2, getSampleResultExp3, getSampleResultExp4, getSampleResultExp5, getSampleResultExp6, getSampleResultExp7);
        }
    }

    /**
     * Test the GetSampleTaskRunnable.
     * Mock all the possible server responses.
     * Mock the SampleImageResponse( when defaultEC2 IP is not selected).
     * Test the GetSampleResult.
     */
    @Test
    public void testGetSampleTaskRunnableWithSampleImageResponse() throws ServerException {
        try (MockedStatic<MddiParameters> mockedStatic = Mockito.mockStatic(MddiParameters.class)) {
            mockSampleImageResponse();

            GetSampleResult getSampleResultExp1 = getSampleTaskRunnable.run("1", true);
            GetSampleResult getSampleResultExp2 = getSampleTaskRunnable.run("1", true);
            GetSampleResult getSampleResultExp3 = getSampleTaskRunnable.run("1", true);
            GetSampleResult getSampleResultExp4 = getSampleTaskRunnable.run("1", true);
            GetSampleResult getSampleResultExp5 = getSampleTaskRunnable.run("1", true);
            when(this.clientService.getMddiImageSize()).thenReturn(MddiVariables.MddiImageSize.FORMAT_512X512);
            GetSampleResult getSampleResultExp6 = getSampleTaskRunnable.run("1", true);
            GetSampleResult getSampleResultExp7 = getSampleTaskRunnable.run("1", false);

            checkGetSampleResults(getSampleResultExp1, getSampleResultExp2, getSampleResultExp3, getSampleResultExp4, getSampleResultExp5, getSampleResultExp6, getSampleResultExp7);
        }
    }

    /**
     * Test the GetSampleTask.
     * Mock all the possible server responses.
     * Mock the CollectionIDData( when defaultEC2 IP is selected).
     * Test the GetSampleResult.
     */
    @Test
    public void testGetSampleTaskWithCollectionIdData() {
        try (MockedStatic<MddiParameters> mockedStatic = Mockito.mockStatic(MddiParameters.class)) {
            mockCollectionIdData();

            GetSampleResult getSampleResultExp1 = getSampleTask.doInBackground();
            GetSampleResult getSampleResultExp2 = getSampleTask.doInBackground();
            GetSampleResult getSampleResultExp3 = getSampleTask.doInBackground();
            GetSampleResult getSampleResultExp4 = getSampleTask.doInBackground();
            GetSampleResult getSampleResultExp5 = getSampleTask.doInBackground();
            when(this.clientService.getMddiImageSize()).thenReturn(MddiVariables.MddiImageSize.FORMAT_512X512);
            GetSampleResult getSampleResultExp6 = getSampleTask.doInBackground();
            this.getSampleTask = new GetSampleTask("1", false, this.clientService, this.managedChannel, this.blockingStub, this.getSampleResultCallback);
            GetSampleResult getSampleResultExp7 = getSampleTask.doInBackground();

            checkGetSampleResults(getSampleResultExp1, getSampleResultExp2, getSampleResultExp3, getSampleResultExp4, getSampleResultExp5, getSampleResultExp6, getSampleResultExp7);
        }
    }

    /**
     * Test the GetSampleTask.
     * Mock all the possible server responses.
     * Mock the SampleImageResponse( when defaultEC2 IP is not selected).
     * Test the GetSampleResult.
     */
    @Test
    public void testGetSampleTaskWithSampleImageResponse() {
        try (MockedStatic<MddiParameters> mockedStatic = Mockito.mockStatic(MddiParameters.class)) {
            mockSampleImageResponse();

            GetSampleResult getSampleResultExp1 = getSampleTask.doInBackground();
            GetSampleResult getSampleResultExp2 = getSampleTask.doInBackground();
            GetSampleResult getSampleResultExp3 = getSampleTask.doInBackground();
            GetSampleResult getSampleResultExp4 = getSampleTask.doInBackground();
            GetSampleResult getSampleResultExp5 = getSampleTask.doInBackground();
            when(this.clientService.getMddiImageSize()).thenReturn(MddiVariables.MddiImageSize.FORMAT_512X512);
            GetSampleResult getSampleResultExp6 = getSampleTask.doInBackground();
            this.getSampleTask = new GetSampleTask("1", false, this.clientService, this.managedChannel, this.blockingStub, this.getSampleResultCallback);
            GetSampleResult getSampleResultExp7 = getSampleTask.doInBackground();

            checkGetSampleResults(getSampleResultExp1, getSampleResultExp2, getSampleResultExp3, getSampleResultExp4, getSampleResultExp5, getSampleResultExp6, getSampleResultExp7);
        }
    }


    /**
     * Mock the CollectionIDData( when defaultEC2 IP is selected).
     */
    private void mockCollectionIdData() {
        when(this.clientService.isDefaultEC2IPSelected())
                .thenReturn(true);

        CollectionIdData collectionIdData_1 = CollectionIdData.newBuilder().setAck("Test 1").setSampleImage(image)
                .setIp("111.111.111.111").setPort("443").setCidType("DBSNO").build();
        CollectionIdData collectionIdData_2 = CollectionIdData.newBuilder().setAck("Test 2").
                setError(mddiError.newBuilder().setErrorCode(1000).setErrorCategory("GET DATA RPC ERROR").setErrorMessage("Invalid credentials").build()).build();
        CollectionIdData collectionIdData_3 = CollectionIdData.newBuilder().setAck("Test 3").
                setError(mddiError.newBuilder().setErrorCode(1001).setErrorCategory("GET DATA RPC ERROR").setErrorMessage("Invalid client timestamp attribute").build()).build();
        CollectionIdData collectionIdData_4 = CollectionIdData.newBuilder().setAck("Test 4").build();
        CollectionIdData collectionIdData_5 = CollectionIdData.newBuilder().setAck("Test 5").
                setError(mddiError.newBuilder().setErrorCode(GET_SAMPLE_ERROR_INVALID_CID).setErrorCategory("GET DATA RPC ERROR").setErrorMessage("Invalid CID").build()).build();
        CollectionIdData collectionIdData_6 = CollectionIdData.newBuilder().setAck("Test 6").setSampleImage(image)
                .setIp("111.111.111.111").setPort("443").setCidType("DBSNO").build();
        //Build the Mddi image using the imagePixels from the Bitmap
        Image image = Image.newBuilder().setImageName("1").setImageWidth(480).
                setImageHeight(640).setSno("1").setImageFormat(0).setImageExt("jpeg").build();
        CollectionIdData collectionIdData_7 = CollectionIdData.newBuilder().setAck("Test 7").setSampleImage(image)
                .setIp("111.111.111.111").setPort("443").setCidType("DBSNO").build();


        when(this.blockingStub.getCollectionData(any()))
                .thenReturn(collectionIdData_1)
                .thenReturn(collectionIdData_2)
                .thenReturn(collectionIdData_3)
                .thenReturn(collectionIdData_4)
                .thenReturn(collectionIdData_5)
                .thenReturn(collectionIdData_6)
                .thenReturn(collectionIdData_7);

        when(buildBitmapFromIntegerList(anyList(), anyInt(), anyInt(), any())).thenReturn(bitmap);
    }

    /**
     * Mock the SampleImageResponse( when defaultEC2 IP is not selected).
     */
    private void mockSampleImageResponse() {
        when(this.clientService.isDefaultEC2IPSelected())
                .thenReturn(false);

        SampleImageResponse sampleImageResponse_1 = SampleImageResponse.newBuilder().setAck("Test 1").setSampleImage(image).build();
        SampleImageResponse sampleImageResponse_2 = SampleImageResponse.newBuilder().setAck("Test 2").
                setError(mddiError.newBuilder().setErrorCode(1000).setErrorCategory("GET DATA RPC ERROR").setErrorMessage("Invalid credentials").build()).build();
        SampleImageResponse sampleImageResponse_3 = SampleImageResponse.newBuilder().setAck("Test 3").
                setError(mddiError.newBuilder().setErrorCode(1001).setErrorCategory("GET DATA RPC ERROR").setErrorMessage("Invalid client timestamp attribute").build()).build();
        SampleImageResponse sampleImageResponse_4 = SampleImageResponse.newBuilder().setAck("Test 4").build();
        SampleImageResponse sampleImageResponse_5 = SampleImageResponse.newBuilder().setAck("Test 5").
                setError(mddiError.newBuilder().setErrorCode(GET_SAMPLE_ERROR_INVALID_CID).setErrorCategory("GET DATA RPC ERROR").setErrorMessage("Invalid CID").build()).build();
        SampleImageResponse sampleImageResponse_6 = SampleImageResponse.newBuilder().setAck("Test 6").setSampleImage(image).build();
        //Build the Mddi image using the imagePixels from the Bitmap
        Image image = Image.newBuilder().setImageName("1").setImageWidth(480).
                setImageHeight(640).setSno("1").setImageFormat(0).setImageExt("jpeg").build();
        SampleImageResponse sampleImageResponse_7 = SampleImageResponse.newBuilder().setAck("Test 7").setSampleImage(image).build();


        when(this.blockingStub.getSampleImage(any()))
                .thenReturn(sampleImageResponse_1)
                .thenReturn(sampleImageResponse_2)
                .thenReturn(sampleImageResponse_3)
                .thenReturn(sampleImageResponse_4)
                .thenReturn(sampleImageResponse_5)
                .thenReturn(sampleImageResponse_6)
                .thenReturn(sampleImageResponse_7);

        when(buildBitmapFromIntegerList(anyList(), anyInt(), anyInt(), any())).thenReturn(bitmap);
    }

    /**
     * Assert the GetSampleResult.
     */
    private void checkGetSampleResults(GetSampleResult getSampleResultExp1, GetSampleResult getSampleResultExp2, GetSampleResult getSampleResultExp3, GetSampleResult getSampleResultExp4,
                                       GetSampleResult getSampleResultExp5, GetSampleResult getSampleResultExp6, GetSampleResult getSampleResultExp7) {
        assertEquals(GetSampleStatus.EXISTING_CID, getSampleResultExp1.getStatus());
        assertEquals("Test 1", getSampleResultExp1.getImageResponse());
        assertEquals((480 * 640), getSampleResultExp1.getPixelsList().size());
        assertEquals(bitmap, getSampleResultExp1.getSampleImage());
        assertEquals(640, getSampleResultExp1.getMddiImage().getImageHeight());
        assertEquals(480, getSampleResultExp1.getMddiImage().getImageWidth());
        assertEquals(307200, getSampleResultExp1.getMddiImage().getImagePixelsList().size());


        assertEquals(GetSampleStatus.INVALID_RESPONSE, getSampleResultExp2.getStatus());
        assertEquals("GET DATA RPC ERROR:1000 - Invalid credentials", getSampleResultExp2.getImageResponse());
        assertNull(getSampleResultExp2.getPixelsList());


        assertEquals(GetSampleStatus.INVALID_RESPONSE, getSampleResultExp3.getStatus());
        assertEquals("GET DATA RPC ERROR:1001 - Invalid client timestamp attribute", getSampleResultExp3.getImageResponse());
        assertNull(getSampleResultExp3.getPixelsList());


        assertEquals(GetSampleStatus.INVALID_RESPONSE, getSampleResultExp4.getStatus());
        assertEquals("List of pixels for the given cid is null", getSampleResultExp4.getImageResponse());
        assertNull(getSampleResultExp4.getPixelsList());


        assertEquals(GetSampleStatus.NON_EXISTING_CID, getSampleResultExp5.getStatus());
        assertEquals("GET DATA RPC ERROR:" + GET_SAMPLE_ERROR_INVALID_CID + " - Invalid CID", getSampleResultExp5.getImageResponse());
        assertNull(getSampleResultExp5.getPixelsList());


        assertEquals(GetSampleStatus.INVALID_RESPONSE, getSampleResultExp6.getStatus());
        assertEquals("Client service mddi image width and height does not match the list of received pixels list from the server", getSampleResultExp6.getImageResponse());
        assertNull(getSampleResultExp6.getPixelsList());
        assertNull(getSampleResultExp6.getSampleImage());

        assertEquals(GetSampleStatus.EXISTING_CID, getSampleResultExp7.getStatus());
        assertEquals("Test 7", getSampleResultExp7.getImageResponse());
        assertNull(getSampleResultExp7.getPixelsList());
        assertNull(getSampleResultExp7.getSampleImage());
        assertEquals(640, getSampleResultExp7.getMddiImage().getImageHeight());
        assertEquals(480, getSampleResultExp7.getMddiImage().getImageWidth());
        assertEquals(0, getSampleResultExp7.getMddiImage().getImagePixelsList().size());
    }
}