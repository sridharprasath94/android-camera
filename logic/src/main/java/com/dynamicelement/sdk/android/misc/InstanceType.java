package com.dynamicelement.sdk.android.misc;

public enum InstanceType {
    /**
     * DB-SNO instance is used for similar type of images (datasets).
     * Each image will be stored with a unique SNO(User  provided) in the database under the
     * given CID.
     */
    DB_SNO,

    /**
     * IVF instance is used for non-similar images(datasets).
     * There is no concept of SNO. All images be stored in the database under the given CID.
     */

    IVF
}