package com.dynamicelement.sdk.android.testMddiImage;

public class TestMddiImageResult {
    private String response = "";
    private boolean validImage = false;

    /**
     * Set the server response for the given request from the MDDI server.
     */
    protected void setResponse(String response) {
        this.response = response;
    }

    /**
     * Set true if the image has enough features and suitable to add to MDDI backend. Otherwise set as false.
     */
    protected void setValidImage(boolean validImage) {
        this.validImage = validImage;
    }

    /**
     * The actual acknowledgment message from the MDDI server.
     */
    public String getResponse() {
        return response;
    }

    /**
     * Check if the image has enough features and suitable to add to MDDI backend.
     */
    public boolean isValidImage() {
        return validImage;
    }
}
