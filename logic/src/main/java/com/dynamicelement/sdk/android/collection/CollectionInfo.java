package com.dynamicelement.sdk.android.collection;

import static com.dynamicelement.sdk.android.mddiclient.MddiParameters.requireNonNull;

/**
 * Collection Info - Needed to be provided before creating a CID in the MDDI database
 */
public class CollectionInfo {
    private String name;
    private String ownerId;
    private String versionId;
    private String shortDescription;
    private String longDescription;

    public CollectionInfo(String name, String versionId, String ownerId, String shortDescription, String longDescription) {
        requireNonNull(name, versionId, ownerId, shortDescription, longDescription);
        this.name = name;
        this.ownerId = ownerId;
        this.versionId = versionId;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
    }

    public CollectionInfo(String name, String versionId, String ownerId) {
        requireNonNull(name, versionId, ownerId);
        this.name = name;
        this.ownerId = ownerId;
        this.versionId = versionId;
        this.shortDescription = "";
        this.longDescription = "";
    }

    public CollectionInfo() {
        this.name = "";
        this.ownerId = "";
        this.versionId = "";
        this.shortDescription = "";
        this.longDescription = "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getName() {
        return name;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getVersionId() {
        return versionId;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }
}
