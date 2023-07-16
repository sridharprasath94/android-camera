package com.dynamicelement.sdk.android.delete;

/**
 * Delete Status
 */
public enum DeleteStatus {
    /**
     * Returns this status if the given cid(including the data of that cid) is deleted.
     */
    DELETED,
    /**
     * Returns this status if there is any error in deleting the given cid.
     */
    ERROR_DELETING,
    /**
     * Returns this status if the given cid does not exist.
     */
    CID_NOT_EXISTS
}
