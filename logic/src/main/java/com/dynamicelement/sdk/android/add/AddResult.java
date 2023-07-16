package com.dynamicelement.sdk.android.add;

import android.graphics.Bitmap;

import com.dynamicelement.sdk.android.misc.PingStates;

/**
 * Add Result contains information like add status, ping, image count and total count.
 */

public class AddResult {
    private AddImageStatus addImageStatus = AddImageStatus.ERROR;
    private PingStates ping = PingStates.PING_GOOD;
    private int imageCount = 0;
    private Bitmap mddiImage = null;
    private String imageLogMessage = "";

    /**
     * Set the AddImageStatus based on the received server response - SUCCESS, DUPLICATE or ERROR.
     */
    protected void setAddImageStatus(AddImageStatus addImageStatus) {
        this.addImageStatus = addImageStatus;
    }

    /**
     * Calculate and set the ping based on the single trip time from the server response.
     * Possible values are PING_GOOD, PING_AVERAGE, PING_BAD, PING_POOR.
     */
    protected void setPing(PingStates ping) {
        this.ping = ping;
    }

    /**
     * Set the current image count.
     */
    protected void setImageCount(int imageCount) {
        this.imageCount = imageCount;
    }

    /**
     * Set the current requested MDDI image.
     */
    protected void setMddiImage(Bitmap mddiImage) {
        this.mddiImage = mddiImage;
    }

    /**
     * Set the log message from the search response.
     */
    protected void setImageLogMessage(String imageLogMessage) {
        this.imageLogMessage = imageLogMessage;
    }

    /**
     * AddImageStatus defines whether the image is SUCCESS, DUPLICATE or ERROR.
     */
    public AddImageStatus getAddImageStatus() {
        return addImageStatus;
    }

    /**
     * Ping states is for defining the speed of the MDDI service connection.
     * Possible values are PING_GOOD, PING_AVERAGE, PING_BAD, PING_POOR.
     * It is calculated from the single trip time between the request and the response.
     */
    public PingStates getPing() {
        return ping;
    }

    /**
     * It is the current image count.
     */
    public int getImageCount() {
        return imageCount;
    }

    /**
     * Represents the current requested MDDI image.
     */
    public Bitmap getMddiImage() {
        return mddiImage;
    }

    /**
     * Log messages from the search response.
     */
    public String getImageLogMessage() {
        return imageLogMessage;
    }
}
