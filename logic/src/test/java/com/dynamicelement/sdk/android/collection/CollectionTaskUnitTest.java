package com.dynamicelement.sdk.android.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.graphics.Bitmap;

import com.dynamicelement.mddi.MddiTenantServiceGrpc;
import com.dynamicelement.sdk.android.Callback;
import com.dynamicelement.sdk.android.exceptions.ClientException;
import com.dynamicelement.sdk.android.exceptions.ExceptionType;
import com.dynamicelement.sdk.android.mddiclient.ClientService;
import com.dynamicelement.sdk.android.mddiclient.MddiVariables;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.grpc.ManagedChannel;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class CollectionTaskUnitTest {
    String cid = "1";
    String sno = "1";
    CollectionInfo collectionInfo = new CollectionInfo("PLI", "V0.0", "post1234");
    ClientService clientService;
    Bitmap bitmap;
    MddiTenantServiceGrpc.MddiTenantServiceStub asyncStub;
    ManagedChannel managedChannel;
    MddiVariables.MddiImageSize mddiImageSize;

    private static Object[] testValues() {
        return new Object[]{
                new Object[]{0, 900, "Width cannot be 0"},
                new Object[]{0, 0, "Width cannot be 0"},
                new Object[]{400, 0, "Height cannot be 0"},
                new Object[]{400, 680, "Width cannot be less than 480"},
//                new Object[]{480, 620, "Height cannot be less than 640"}
        };
    }

    @Before
    public void setUp() {
        this.clientService = mock(ClientService.class);
        this.bitmap = mock(Bitmap.class);
        this.mddiImageSize = mock(MddiVariables.MddiImageSize.class);
        this.asyncStub = mock(MddiTenantServiceGrpc.MddiTenantServiceStub.class);
        this.managedChannel = mock(ManagedChannel.class);
    }

    /**
     *
     */
    @Test
    @Parameters(method = "testValues")
    public void CollectionTask_TestWithDifferentWidthAndHeight(int width, int height, String expectedMessage) {
        when(this.bitmap.getWidth()).thenReturn(width);
        when(this.bitmap.getHeight()).thenReturn(height);
        when(this.clientService.getMddiImageSize()).thenReturn(mddiImageSize);
        new CollectionTask(this.bitmap, this.cid, this.sno, this.collectionInfo, this.clientService,
                this.managedChannel, this.asyncStub, new Callback<CollectionResult>() {
            @Override
            public void onResponse(CollectionResult response) {

            }

            @Override
            public void onError(ExceptionType exceptionType, Exception e) {
                assertTrue(e instanceof ClientException);
                assertEquals(expectedMessage, e.getMessage());
            }
        });
    }
}