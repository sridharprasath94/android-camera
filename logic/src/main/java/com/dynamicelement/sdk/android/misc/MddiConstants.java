package com.dynamicelement.sdk.android.misc;

public interface MddiConstants {
    /**
     * Time out value until the collection can be created.
     */
    int COLLECTION_TIMEOUT = 15000;
    /**
     * Time delay between each successive requests for add and search tasks.
     */
    int TIME_DELAY = 500;
    /**
     * Version ID for the MDDI requests.
     */
    String VERSION_ID = "V1.0";
    /**
     * Mddi server response without any errors
     */
    int RESPONSE_NO_ERROR = 0;
    /**
     * Search error code for invalid Sno.
     */
    int SEARCH_ERROR_INVALID_SNO = 3003;
    /**
     * Search error code for invalid image.
     */
    int SEARCH_ERROR_INVALID_IMAGE = 3004;
    /**
     * Add error code for duplicate image.
     */
    int ADD_ERROR_DUPLICATE_IMAGE = 2053;
    /**
     * Add error code for invalid image.
     */
    int ADD_ERROR_INVALID_IMAGE = 2054;
    /**
     * Delete error code for invalid cid.
     */
    int DELETE_ERROR_INVALID_CID = 2002;
    /**
     * Get sample error code for invalid cid.
     */
    int GET_SAMPLE_ERROR_INVALID_CID = 1002;
    /**
     * Test Mddi image code for invalid image.
     */
    int TEST_MDDI_INVALID_IMAGE = 3503;
    /**
     * Default MDDI threshold value. The server score will be compared with this value.
     *
     * There is option to update this threshold in client service.
     * Call updateMddiThreshold() to update this value.
     */
    float DEFAULT_MDDI_THRESHOLD = 0.6f;
}
