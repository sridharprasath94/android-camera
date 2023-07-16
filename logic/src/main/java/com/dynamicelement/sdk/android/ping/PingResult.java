package com.dynamicelement.sdk.android.ping;

/**
 * Ping Result.
 */
public class PingResult {
    private String pingResponse = "";
    private boolean isConnected = false;

    /**
     * Set the server response for the given request from the MDDI server.
     */
    protected void setPingResponse(String pingResponse) {
        this.pingResponse = pingResponse;
    }

    /**
     * Set true if the MDDI backend is connected. Otherwise set as false.
     */
    protected void setConnected(boolean connected) {
        isConnected = connected;
    }

    /**
     * The actual acknowledgment message from the MDDI server.
     */
    public String getPingResponse() {
        return pingResponse;
    }

    /**
     * Returns true if the MDDI backend is connected. Otherwise false(default value).
     */
    public boolean isConnected() {
        return isConnected;
    }
}
