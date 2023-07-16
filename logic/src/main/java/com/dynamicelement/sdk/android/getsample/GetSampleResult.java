package com.dynamicelement.sdk.android.getsample;

import android.graphics.Bitmap;

import com.dynamicelement.mddi.Image;

import java.util.ArrayList;
import java.util.List;

/**
 * Get Sample Result.
 */
public class GetSampleResult {
    protected String instanceType = "";
    protected Bitmap sampleImage;
    private GetSampleStatus status = GetSampleStatus.INVALID_RESPONSE;
    private String imageResponse = "";
    private List<Integer> pixelsList = new ArrayList<>();
    private String hostIP = "";
    private String portNumber = "";
    private Image mddiImage;

    /**
     * Status to indicate the get sample response.
     * Possible values are EXISTING_CID,NON_EXISTING_CID,INVALID_RESPONSE.
     */
    public GetSampleStatus getStatus() {
        return status;
    }

    /**
     * Set the status to indicate information about the given CID.
     * Possible values are EXISTING_CID,NON_EXISTING_CID,INVALID_RESPONSE.
     */
    protected void setStatus(GetSampleStatus status) {
        this.status = status;
    }

    /**
     * Image response for the given cid from the MDDI server.
     */
    public String getImageResponse() {
        return imageResponse;
    }

    /**
     * Set the server response for the given request from the MDDI server.
     */
    protected void setImageResponse(String imageResponse) {
        this.imageResponse = imageResponse;
    }

    /**
     * List of image pixels of the reference image of the given cid.
     */
    public List<Integer> getPixelsList() {
        return pixelsList;
    }

    /**
     * Set the received list of image pixels of the reference image of the given cid from the MDDI server.
     */
    protected void setPixelsList(List<Integer> pixelsList) {
        this.pixelsList = pixelsList;
    }

    /**
     * Host IP address of the backend.
     */
    public String getHostIP() {
        return hostIP;
    }

    /**
     * Set the host IP address of the backend.
     */
    protected void setHostIP(String hostIP) {
        this.hostIP = hostIP;
    }

    /**
     * Port number of the backend.
     */
    public String getPortNumber() {
        return portNumber;
    }

    /**
     * Set the port number of the backend.
     */
    protected void setPortNumber(String portNumber) {
        this.portNumber = portNumber;
    }

    /**
     * Instance type of the backend.
     */
    public String getInstanceType() {
        return instanceType;
    }

    /**
     * Set the instance type of the backend.
     */
    protected void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    /**
     * Represents the sample image of the requested CID.
     */
    public Bitmap getSampleImage() {
        return sampleImage;
    }

    /**
     * Set the actual sample image for the given CID request received from the MDDI server.
     */
    protected void setSampleImage(Bitmap sampleImage) {
        this.sampleImage = sampleImage;
    }

    /**
     * Get the MDDI image.
     */
    public Image getMddiImage() {
        return mddiImage;
    }

    /**
     * Set the MDDI image.
     */
    public void setMddiImage(Image mddiImage) {
        this.mddiImage = mddiImage;
    }

}
