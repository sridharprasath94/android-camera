package com.dynamicelement.sdk.android.collection;

/**
 * Collection Result
 */
public class CollectionResult {
    private String response = "";
    private boolean collectionCreated = false;

    /**
     * Set the server response for the given request from the MDDI server.
     */
    protected void setResponse(String response) {
        this.response = response;
    }

    /**
     * Set true if the collection is created. Otherwise set as false.
     */
    protected void setCollectionCreated(boolean collectionCreated) {
        this.collectionCreated = collectionCreated;
    }

    /**
     * The actual acknowledgment message for the given request from the MDDI server.
     */
    public String getResponse() {
        return response;
    }

    /**
     * Returns true if the collection is created. Otherwise false(default value).
     */
    public boolean isCollectionCreated() {
        return collectionCreated;
    }
}
