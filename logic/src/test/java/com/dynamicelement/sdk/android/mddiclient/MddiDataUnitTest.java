package com.dynamicelement.sdk.android.mddiclient;

import static com.dynamicelement.sdk.android.mddiclient.MddiData.MddiDataCallback;
import static com.dynamicelement.sdk.android.mddiclient.MddiData.getBarcodeCid;
import static com.dynamicelement.sdk.android.mddiclient.MddiData.getBarcodeSno;
import static com.dynamicelement.sdk.android.mddiclient.MddiData.isThisTheCorrectFormat;
import static com.dynamicelement.sdk.android.misc.InstanceType.DB_SNO;
import static com.dynamicelement.sdk.android.misc.InstanceType.IVF;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import android.graphics.Bitmap;
import android.media.Image;
import android.util.Log;

import com.dynamicelement.sdk.android.exceptions.ClientException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

/**
 * Test different methods of the class MddiData class
 */
@RunWith(JUnitParamsRunner.class)
public class MddiDataUnitTest {
    Bitmap bitmap;
    Image image;
    MddiDataCallback mddiDataCallback;
    MockedStatic<Log> logMockedStatic;


    private static Object[] testValuesGetBarcode() {
        return new Object[]{
                new Object[]{"http://d2.vc/bm4/1234567890",
                        null, "http://d2.vc/bm4/1234567890"},
                new Object[]{null, "http://d2.vc/bm4/1234567890",
                        "http://d2.vc/bm4/1234567890"},
                new Object[]{"http://d2.vc/bm4/1234567890",
                        "http://d2.vc/bm4/1234567890", "http://d2.vc/bm4/1234567890"},
                new Object[]{null, null, null},
        };
    }

    private static Object[] testValuesNegativeMddiTest() {
        return new Object[]{
                new Object[]{400, 400, 3000, 4000, "Bitmap width should be atleast 480"},
//                new Object[]{300, 400, 4000, 4000, "Crop height and width are not in the ratio 4:3"},
//                new Object[]{300, 400, 3000, 4000, "Bitmap width should be atleast 480"},
//                new Object[]{480, 640, 300, 400, "Crop width should be atleast 480"},
                new Object[]{0, 640, 300, 400, "Bitmap height and width are less than the ratio 4:3"},
                new Object[]{480, 0, 0, 400, "Crop height and width are less than the ratio 4:3"},
                new Object[]{0, 0, 0, 400, "Crop height and width are less than the ratio 4:3"},
//                new Object[]{480, 640, 0, 400, "Crop height and width are not in the ratio 4:3"},
        };
    }

    private static Object[] testValuesPositiveMddiTest() {
        return new Object[]{
                new Object[]{DB_SNO, true, "http://d2.vc/bm4/1234567890", "bm4", "1234567890"},
                new Object[]{DB_SNO, false, null, "1", "1"},
                new Object[]{IVF, true, null, "1", "1"},
                new Object[]{IVF, false, null, "1", "1"},
        };
    }

    @Before
    public void setUp() {
        this.bitmap = mock(Bitmap.class);
        this.image = mock(Image.class);
        this.mddiDataCallback = mock(MddiDataCallback.class);
        this.logMockedStatic = mockStatic(Log.class);
        this.mddiDataCallback = mock(MddiDataCallback.class);
    }

    @After
    public void close() {
        this.logMockedStatic.close();
    }

    /**
     * Test the methods with different inputs
     * Check if a string is 27 alpha-numeric characters long
     * It has to start the with the prefix "http://d2.vc/"
     */
    @Test
    public void MddiData_isThisTheCorrectFormatTest() {
        assertTrue(isThisTheCorrectFormat("http://d2.vc/bm4/1234567890"));
        assertTrue(isThisTheCorrectFormat("http://d2.vc/bm4/1234567899"));
        assertFalse(isThisTheCorrectFormat("http://d2.vc/bm4/123456789999"));
        assertFalse(isThisTheCorrectFormat("http://d2.vc44/123456789999"));
        assertFalse(isThisTheCorrectFormat("httt://d2.vc/bm4/1234567890"));
        assertFalse(isThisTheCorrectFormat(null));
    }

    /**
     * Extract the indices 13 to 16 of the given string(Should be 27 indices)
     */
    @Test
    public void MddiData_getBarcodeCidTest() {
        assertEquals("bm4", getBarcodeCid("http://d2.vc/bm4/1234567890"));
        assertEquals("bi4", getBarcodeCid("http://d2.vc/bi4/1234567887"));
        assertNotEquals("bm4", getBarcodeCid("http://d2.vc/bi4/1234567890"));
        assertNotEquals("bi4", getBarcodeCid("http://d2.vc/bm4/2234567890"));
    }

    /**
     * Extract the indices 17 to 27 of the given string(Should be 27 indices)
     */
    @Test
    public void MddiData_getBarcodeSnoTest() {
        assertEquals("1234567890", getBarcodeSno("http://d2.vc/bm4/1234567890"));
        assertEquals("1234567887", getBarcodeSno("http://d2.vc/bi4/1234567887"));
        assertNotEquals("12345678ui", getBarcodeSno("http://d2.vc/bi4/1234567890"));
        assertNotEquals("2134567890", getBarcodeSno("http://d2.vc/bm4/2234567890"));
    }

    /**
     * This MddiData class has bitmap and openCV dependencies.
     * These dependent methods are mocked to return the desired response for those methods
     * Then the class under test will be tested based on the mocked responses
     */
    @Test
    @Parameters(method = "testValuesNegativeMddiTest")
    public void MddiData_negativeMddiDataTest(int bitmapWidth,
                                              int bitmapHeight,
                                              int cropWidth,
                                              int cropHeight,
                                              String expectedResult) {
        try {
            when(bitmap.getWidth()).thenReturn(bitmapWidth);
            when(bitmap.getHeight()).thenReturn(bitmapHeight);
            MddiData.checkBitmap(bitmap, cropWidth, cropHeight);
        } catch (ClientException exception) {
            assertEquals(expectedResult, exception.getMessage());
        }
    }
    //TODO - Temporarily commented out since it has java openCV dependencies

//    /**
//     * This MddiData class has bitmap and openCV dependencies.
//     * These dependent methods are mocked to return the desired response for those methods
//     * Then the class under test will be tested based on the mocked responses
//     */
//    @Test
//    @Parameters(method = "testValuesPositiveMddiTest")
//    public void MddiData_positiveMddiDataTest(InstanceType instanceType,
//                                              boolean barcodeMode,
//                                              String expectedBarcodeResult,
//                                              String expectedCid,
//                                              String expectedSno) throws Exception {
//        try (MockedStatic<MddiParameters> mddiParametersMockedStatic = Mockito.mockStatic(MddiParameters.class)) {
//            try (MockedStatic<Imgcodecs> imgcodecsMockedStatic = Mockito.mockStatic(Imgcodecs.class)) {
//                try (MockedStatic<OpenCVLoader> openCVLoaderMockedStatic = Mockito.mockStatic(OpenCVLoader.class)) {
//                    Mat mat = mock(Mat.class);
//                    byte[] bytes = "test".getBytes();
//                    when(bitmap.getWidth()).thenReturn(2100);
//                    when(bitmap.getHeight()).thenReturn(2800);
//
//                    mddiParametersMockedStatic.when(() -> centerCropBitmap(any(Bitmap.class), any(Integer.class), any(Integer.class))).thenReturn(bitmap);
//                    mddiParametersMockedStatic.when(() -> getBytesFromBitmap(any(Bitmap.class))).thenReturn(bytes);
//                    mddiParametersMockedStatic.when(() -> buildBarcodeBitmapFromBytes(any(byte[].class), any(Integer.class))).thenReturn(bitmap);
//                    mddiParametersMockedStatic.when(() -> zbarScanning(any(Bitmap.class))).thenReturn("http://d2.vc/bm4/1234567890");
//                    mddiParametersMockedStatic.when(() -> zxingScanning(any(Bitmap.class))).thenReturn(null);
//                    mddiParametersMockedStatic.when(() -> getStreamImageFromBitmap(any(Bitmap.class), any(Integer.class), any(Integer.class),
//                            any(String.class),
//                            any(String.class),
//                            any(String.class),
//                            any(InstanceType.class))).thenReturn(StreamImage.newBuilder().build());
//                    CountDownLatch latch = new CountDownLatch(1);
//                    AtomicReference<String> expectedBarcodeReference = new AtomicReference<>(null);
//                    AtomicReference<String> expectedCidReference = new AtomicReference<>(null);
//                    AtomicReference<String> expectedSnoReference = new AtomicReference<>(null);
//                    //Db Sno with barcode mode true
//                    MddiData.builder().bitmap(bitmap).cropWidth(1500).cropHeight(2000).
//                            instanceType(instanceType).cid("1").sno("1").barcodeMode(barcodeMode).mddiDataCallback(new MddiData.MddiDataCallback() {
//                        @Override
//                        public void onDBSNO(String barcodeResult, String mddiCid, String mddiSno, StreamImage mddiImage, Bitmap originalBitmap) {
//                            expectedBarcodeReference.set(barcodeResult);
//                            expectedCidReference.set(mddiCid);
//                            expectedSnoReference.set(mddiSno);
//                            latch.countDown();
//                        }
//
//                        @Override
//                        public void onIVF(String mddiCid, String mddiSno, StreamImage mddiImage, Bitmap originalBitmap) {
//                            expectedBarcodeReference.set(null);
//                            expectedCidReference.set(mddiCid);
//                            expectedSnoReference.set(mddiSno);
//                            latch.countDown();
//                        }
//                    }).build();
//
//                    if (latch.await(5000, TimeUnit.MILLISECONDS)) {
//                        assertEquals(expectedBarcodeResult, expectedBarcodeReference.get());
//                        assertEquals(expectedCid, expectedCidReference.get());
//                        assertEquals(expectedSno, expectedSnoReference.get());
//                    } else {
//                        throw new ClientException("Timeout error");
//                    }
//                }
//            }
//        }
//    }
}