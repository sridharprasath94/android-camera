package com.dynamicelement.sdk.android.mddiclient;

import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.appendMessages;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.buildCollectionID;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.buildCollectionRequest;
import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.calculateTimeIn24HourFormat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.dynamicelement.mddi.CollectionId;
import com.dynamicelement.mddi.Image;
import com.dynamicelement.mddi.RequestCollection;
import com.dynamicelement.sdk.android.collection.CollectionInfo;

import org.junit.Test;

public class MddiParametersUnitTest {
    /**
     * Build the collection Id method with certain arguments
     * Check whether it has the correct arguments
     */
    @Test
    public void MddiParameters_buildCollectionIDTest() {
        CollectionId collectionId = buildCollectionID("1", true, true, "pli");
        assertEquals("1", collectionId.getCid());
        assertEquals("V1.0", collectionId.getVersionId());
        assertTrue(collectionId.getSampleImgFlag());
        assertTrue(collectionId.getOwnerOtherInformationFlag());
        assertTrue(collectionId.getOwnerIdFlag());
        assertTrue(collectionId.getOwnerNameFlag());
        assertTrue(collectionId.getOwnerAddressFlag());
        assertNotNull(collectionId.getTimestamp());

        collectionId = buildCollectionID("1", false, false, "pli");
        assertFalse(collectionId.getSampleImgFlag());
        assertFalse(collectionId.getOwnerOtherInformationFlag());
        assertFalse(collectionId.getOwnerIdFlag());
        assertFalse(collectionId.getOwnerNameFlag());
        assertFalse(collectionId.getOwnerAddressFlag());
    }

    /**
     * Build the collection request method with certain arguments
     * Check whether it has the correct arguments
     */
    @Test
    public void MddiParameters_buildCollectionRequestTest() {
        CollectionInfo collectionInfo = new CollectionInfo("PLI", "V0.0", "post1234", "test", "test");
        Image image = Image.newBuilder().setImageName("Test").setImageWidth(480).
                setImageHeight(640).setSno("1").setImageFormat(0).setImageExt("jpeg").build();
        RequestCollection requestCollection = buildCollectionRequest(collectionInfo, "1", image, "pli");
        assertEquals("1", requestCollection.getCid());
        assertEquals("V1.0", requestCollection.getVersionId());
        assertEquals("1", requestCollection.getImage().getSno());
        assertEquals("jpeg", requestCollection.getImage().getImageExt());
        assertEquals(0, requestCollection.getImage().getImageFormat());
        assertEquals(480, requestCollection.getImage().getImageWidth());
        assertEquals(640, requestCollection.getImage().getImageHeight());
        assertEquals("PLI", requestCollection.getCollectionName());
        assertEquals("V0.0", requestCollection.getCollectionVersionId());
        assertEquals("post1234", requestCollection.getCollectionOwnerId());
        assertEquals("test", requestCollection.getCollectionDescriptionLong());
        assertEquals("test", requestCollection.getCollectionDescriptionShort());
    }

    /**
     * Check calculate time method
     * Allow current thread to sleep for some time
     * And then calculate the difference between the start time and end end time
     * Convert the time difference to 24 hour format
     */
    @Test
    public void MddiParameters_calculateTimeIn24HourFormatTest() {
        long startTime = System.currentTimeMillis();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        assertEquals("00:00:01", calculateTimeIn24HourFormat(startTime, endTime));
        assertEquals("00:00:00", calculateTimeIn24HourFormat(1623554, 1623544));
        assertEquals("00:00:00", calculateTimeIn24HourFormat(1623554, 1623584));
        assertEquals("00:00:00", calculateTimeIn24HourFormat(10, 10));
        long startTime1 = System.currentTimeMillis();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTime1 = System.currentTimeMillis();
        assertEquals("00:00:02", calculateTimeIn24HourFormat(startTime1, endTime1));
    }

    /**
     * Testing this appendMessages method with and without object parameters
     * To increase the test coverage
     */
    @Test
    public void MddiParameters_appendMessageTest() {
        StringBuffer stringBuffer1 = new StringBuffer();
        StringBuffer stringBuffer2 = new StringBuffer();
        appendMessages(stringBuffer1, "test{0} , test{1}", 1, 2);
        assertEquals("test1 , test2" + System.lineSeparator(), stringBuffer1.toString());
        appendMessages(stringBuffer2, "test");
        assertEquals("test" + System.lineSeparator(), stringBuffer2.toString());
    }
}