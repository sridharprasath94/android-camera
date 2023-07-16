package com.dynamicelement.sdk.android.search;

import android.graphics.Bitmap;

import com.dynamicelement.sdk.android.misc.PingStates;

/**
 * Search Result contains information like ping, image count, total count and log messages.
 */

public class SearchResult {
    private PingStates ping = PingStates.PING_GOOD;
    private int imageCount = 0;
    private String imageLogMessage = "";
    private Bitmap mddiImage = null;
    private String uid = "";
    private String cid = "";
    private String sno = "";
    private float score = 0;
    private int rating = 0;

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
     * Set the log message from the add response.
     */
    protected void setImageLogMessage(String imageLogMessage) {
        this.imageLogMessage = imageLogMessage;
    }

    /**
     * Set the current requested MDDI image.
     */
    protected void setMddiImage(Bitmap mddiImage) {
        this.mddiImage = mddiImage;
    }

    /**
     * Set the UID of the MDDI image from the server response.
     */
    protected void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * Set the CID of the MDDI image from the server response.
     */
    protected void setCid(String cid) {
        this.cid = cid;
    }

    /**
     * Set the SNO of the MDDI image from the server response.
     */
    protected void setSno(String sno) {
        this.sno = sno;
    }

    /**
     * Set the score of the MDDI image from the server response.
     */
    protected void setScore(float score) {
        this.score = score;
    }

    /**
     * Set the calculated rating value based on Score of the MDDI image.
     */
    protected void setRating(int rating) {
        this.rating = rating;
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
     * Log messages from the search response.
     */
    public String getImageLogMessage() {
        return imageLogMessage;
    }

    /**
     * Represents the current requested MDDI image.
     */
    public Bitmap getMddiImage() {
        return mddiImage;
    }

    /**
     * UID of the MDDI image from the server response.
     */
    public String getUid() {
        return uid;
    }

    /**
     * CID of the MDDI image from the server response.
     */
    public String getCid() {
        return cid;
    }

    /**
     * SNO of the MDDI image from the server response.
     */
    public String getSno() {
        return sno;
    }

    /**
     * Score of the MDDI image from the server response.
     */
    public float getScore() {
        return score;
    }

    /**
     * Calculated rating based on Score of the MDDI image.
     */
    public int getRating() {
        return rating;
    }
}
