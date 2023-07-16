package com.dynamicelement.sdk.android.add;

public enum AddImageStatus {
    /**
     * When the image gets added to the MDDI backend successfully, it will be considered as a
     * valid and successful image.
     * This status will be assigned for the current image.
     */
    SUCCESS,
    /**
     * When there is already a matching image in the MDDI backend, it will be considered as
     * duplicate.
     * This status will be then be assigned for the current image.
     */
    DUPLICATE,
    /**
     * When the image does not have enough required features for the MDDI, it will be considered
     * an invalid image.
     * This status will be then be assigned for the current image.
     */
    ERROR
}
