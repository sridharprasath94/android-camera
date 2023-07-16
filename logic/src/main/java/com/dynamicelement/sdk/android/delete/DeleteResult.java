package com.dynamicelement.sdk.android.delete;

/**
 * Delete Result
 */
public class DeleteResult {
    private DeleteStatus deleteStatus = DeleteStatus.ERROR_DELETING;
    private String response = "";

    /**
     * Set the status based on the server response from the MDDI server.
     * Possible values are DELETED,ERROR_DELETING,CID_NOT_EXISTS.
     */
    protected void setDeleteStatus(DeleteStatus deleteStatus) {
        this.deleteStatus = deleteStatus;
    }

    /**
     * Set the server response for the given request from the MDDI server.
     */
    protected void setResponse(String response) {
        this.response = response;
    }

    /**
     * Status to indicate the delete response.
     * Possible values are DELETED,ERROR_DELETING,CID_NOT_EXISTS.
     */
    public DeleteStatus getDeleteStatus() {
        return deleteStatus;
    }

    /**
     * Response for the given delete request from the MDDI server.
     */
    public String getResponse() {
        return response;
    }
}
