package com.dynamicelement.sdk.android.search;

import static com.dynamicelement.sdk.android.search.SearchTask.getRating;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.graphics.Bitmap;

import com.dynamicelement.mddi.MddiTenantServiceGrpc;
import com.dynamicelement.mddi.SearchStreamResponse;
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
 * Test the execute method of the SearchBitmapTask class with invalid parameters
 */
@RunWith(JUnitParamsRunner.class)
public class SearchTaskUnitTest {
    Bitmap bitmap;
    ClientService clientService;
    MddiTenantServiceGrpc.MddiTenantServiceStub asyncStub;
    ManagedChannel managedChannel;

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

    private static Object[] testValues() {
        return new Object[]{
                new Object[]{0, 900, "Width cannot be 0"},
                new Object[]{0, 0, "Width cannot be 0"},
                new Object[]{400, 0, "Height cannot be 0"},
                new Object[]{400, 680, "Width cannot be less than 480"},
//                new Object[]{480, 620, "Height cannot be less than 640"}
        };
    }

    @Test
    @Parameters(method = "testValues")
    public void SearchTask_executeTestDifferentValues(int width, int height, String expectedMessage) {
        SearchTask searchTask = new SearchTask(this.clientService, false, this.managedChannel,this.asyncStub, new SearchCallBack() {
            @Override
            public void onNegativeResponse(SearchStreamResponse searchStreamResponse, SearchResult searchResult) {

            }

            @Override
            public void onPositiveResponse(SearchStreamResponse searchStreamResponse, SearchResult searchResult) {

            }

            @Override
            public void onSearchCompleted(String elapsedTime, String summaryMessage) {

            }

            @Override
            public void onError(ExceptionType exceptionType, Exception e) {
                assertTrue(e instanceof ClientException);
                assertEquals(expectedMessage, e.getMessage());
            }
        });
        searchTask.execute(this.bitmap, width, height, "1", "1");
    }

    /**
     * Check get rating method by checking with different values of search score
     */
    @Test
    public void MddiParameters_getRatingTest() {
        assertEquals(1, getRating(0.60));
        assertEquals(1, getRating(0.63));
        assertEquals(1, getRating(0.70));
        assertEquals(2, getRating(0.71));
        assertEquals(2, getRating(0.75));
        assertEquals(2, getRating(0.80));
        assertEquals(3, getRating(0.81));
        assertEquals(3, getRating(0.87));
        assertEquals(3, getRating(0.90));
        assertEquals(4, getRating(0.91));
        assertEquals(4, getRating(0.93));
        assertEquals(4, getRating(0.95));
        assertEquals(5, getRating(0.96));
        assertEquals(5, getRating(0.99));
        assertEquals(5, getRating(1.00));
        assertEquals(0, getRating(0.59));
        assertEquals(0, getRating(0.44));
        assertEquals(0, getRating(0.00));
        assertEquals(0, getRating(1.01));
    }
}