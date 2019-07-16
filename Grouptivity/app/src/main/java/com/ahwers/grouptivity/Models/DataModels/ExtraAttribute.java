package com.ahwers.grouptivity.Models.DataModels;

public class ExtraAttribute {


    private String mTitle;
    private String mValue;

    public ExtraAttribute() {

    }

    public ExtraAttribute(String title, String value) {
        mTitle = title;
        mValue = value;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        mValue = value;
    }

}
