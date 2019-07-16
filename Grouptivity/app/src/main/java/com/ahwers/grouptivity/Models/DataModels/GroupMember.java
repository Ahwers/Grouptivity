package com.ahwers.grouptivity.Models.DataModels;

public class GroupMember {

    private String mId;
    private String mDisplayName;
    private String mTitle;
    private boolean mOwner;

    public GroupMember(String id, String displayName, String title, boolean owner) {
        mId = id;
        mDisplayName = displayName;
        mTitle = title;
        mOwner = owner;
    }

    public GroupMember() {

    }

    public boolean isOwner() {
        return mOwner;
    }

    public void setOwner(boolean owner) {
        mOwner = owner;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public void setDisplayName(String displayName) {
        mDisplayName = displayName;
    }
}
