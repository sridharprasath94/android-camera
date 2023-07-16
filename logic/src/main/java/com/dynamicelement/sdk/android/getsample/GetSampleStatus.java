package com.dynamicelement.sdk.android.getsample;

/**
 * Get sample status.
 */
public enum GetSampleStatus {
    /**
     * Returns this status if the Collection ID already exists.
     */
    EXISTING_CID,
    /**
     * Returns this status if the Collection ID does not exist.
     */
    NON_EXISTING_CID,
    /**
     * Returns this status if there is any error.
     */
    INVALID_RESPONSE
}
