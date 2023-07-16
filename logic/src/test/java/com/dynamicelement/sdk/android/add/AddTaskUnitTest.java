package com.dynamicelement.sdk.android.add;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.graphics.Bitmap;

import com.dynamicelement.mddi.AddStreamResponse;
import com.dynamicelement.mddi.MddiTenantServiceGrpc;
import com.dynamicelement.sdk.android.exceptions.ClientException;
import com.dynamicelement.sdk.android.exceptions.ExceptionType;
import com.dynamicelement.sdk.android.mddiclient.ClientService;
import com.dynamicelement.sdk.android.mddiclient.MddiVariables;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import io.grpc.ManagedChannel;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

/**
 * Test the execute method of the AddBitmapTask class with invalid parameters
 */
@RunWith(JUnitParamsRunner.class)
public class AddTaskUnitTest {
    Bitmap bitmap;
    ClientService clientService;
    ManagedChannel managedChannel;
    MddiTenantServiceGrpc.MddiTenantServiceStub asyncStub;

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
        this.bitmap = mock(Bitmap.class);
        this.clientService = mock(ClientService.class);
        this.asyncStub = mock(MddiTenantServiceGrpc.MddiTenantServiceStub.class);
        this.managedChannel = mock(ManagedChannel.class);
        MddiVariables.MddiImageSize mddiImageSize = mock(MddiVariables.MddiImageSize.class);
        when(this.clientService.getMddiImageSize()).thenReturn(mddiImageSize);
        when(mddiImageSize.getWidth()).thenReturn(480);
        when(mddiImageSize.getHeight()).thenReturn(640);
    }

    @Test
    @Parameters(method = "testValues")
    public void AddTask_executeTestDifferentValues(int width, int height, String expectedMessage) {
        AddTask addTask = new AddTask(this.clientService, false, this.managedChannel, this.asyncStub, new AddCallback() {
            @Override
            public void onNextResponse(AddStreamResponse addStreamResponse, AddResult result) {

            }

            @Override
            public void onCompleted(String elapsedTime, String summaryMessage) {

            }

            @Override
            public void onError(ExceptionType exceptionType, Exception e) {
                assertTrue(e instanceof ClientException);
                assertEquals(expectedMessage, e.getMessage());
            }
        });
        addTask.execute(this.bitmap, width, height, "1", "1");
    }
}