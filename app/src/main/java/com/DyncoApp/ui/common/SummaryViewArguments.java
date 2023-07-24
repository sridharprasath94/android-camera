package com.DyncoApp.ui.common;

import android.os.Parcel;
import android.os.Parcelable;

public class SummaryViewArguments implements Parcelable {
    private boolean userMode;
    private boolean createCollection;
    private String mddiCid;
    private String mddiUid;
    private int mddiRating;
    private String mddiMode;
    private float mddiScore;
    private String mddiBase64Image;

    public SummaryViewArguments() {
        // Default constructor
    }

    protected SummaryViewArguments(Parcel in) {
        userMode = in.readByte() != 0;
        createCollection = in.readByte() != 0;
        mddiCid = in.readString();
        mddiUid = in.readString();
        mddiRating = in.readInt();
        mddiMode = in.readParcelable(MddiMode.class.getClassLoader());
        mddiScore = in.readFloat();
        mddiBase64Image = in.readString();
    }

    public static final Creator<SummaryViewArguments> CREATOR = new Creator<SummaryViewArguments>() {
        @Override
        public SummaryViewArguments createFromParcel(Parcel in) {
            return new SummaryViewArguments(in);
        }

        @Override
        public SummaryViewArguments[] newArray(int size) {
            return new SummaryViewArguments[size];
        }
    };

    public boolean isUserMode() {
        return userMode;
    }

    public void setUserMode(boolean userMode) {
        this.userMode = userMode;
    }

    public boolean isCreateCollection() {
        return createCollection;
    }

    public void setCreateCollection(boolean createCollection) {
        this.createCollection = createCollection;
    }

    public String getMddiCid() {
        return mddiCid;
    }

    public void setMddiCid(String mddiCid) {
        this.mddiCid = mddiCid;
    }


    public String getMddiUid() {
        return mddiUid;
    }

    public void setMddiUid(String mddiUid) {
        this.mddiUid = mddiUid;
    }

    public int getMddiRating() {
        return mddiRating;
    }

    public void setMddiRating(int mddiRating) {
        this.mddiRating = mddiRating;
    }

    public String getMddiMode() {
        return mddiMode;
    }

    public void setMddiMode(String mddiMode) {
        this.mddiMode = mddiMode;
    }

    public float getMddiScore() {
        return mddiScore;
    }

    public void setMddiScore(float mddiScore) {
        this.mddiScore = mddiScore;
    }

    public String getMddiBase64Image() {
        return mddiBase64Image;
    }

    public void setMddiBase64Image(String mddiBase64Image) {
        this.mddiBase64Image = mddiBase64Image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (userMode ? 1 : 0));
        dest.writeByte((byte) (createCollection ? 1 : 0));
        dest.writeString(mddiCid);
        dest.writeString(mddiUid);
        dest.writeInt(mddiRating);
        dest.writeString(mddiMode);
        dest.writeFloat(mddiScore);
        dest.writeString(mddiBase64Image);
    }
}
