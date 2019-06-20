package com.ahwers.grouptivity.Models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BacklogActivity {

    private String mId;
    private String mActivityType;
    private String mActivityDescription;
    private Date mActiviyDateTime;
    private String mReference;
    private boolean mComplete;

    public BacklogActivity() { }

    public BacklogActivity(DocumentSnapshot document) {
        mId = document.getId();
        mActivityType = (String) document.get("type");
        mActivityDescription = (String) document.get("description");
        Timestamp datetime = (Timestamp) document.get("datetime");
        mActiviyDateTime = datetime.toDate();
        mReference = (String) document.get("reference");
        mComplete = (boolean) document.get("complete");
    }

    public Map<String, Object> getDocumentMap() {
        Map<String, Object> activityDocumentMap = new HashMap<>();

        activityDocumentMap.put("type", mActivityType);
        activityDocumentMap.put("description", mActivityDescription);
        activityDocumentMap.put("datetime", mActiviyDateTime);
        activityDocumentMap.put("reference", mReference);
        activityDocumentMap.put("complete", mComplete);

        return activityDocumentMap;
    }

    public String getActivityType() {
        return mActivityType;
    }

    public void setActivityType(String activityType) {
        mActivityType = activityType;
    }

    public String getActivityDescription() {
        return mActivityDescription;
    }

    public void setActivityDescription(String activityDescription) {
        mActivityDescription = activityDescription;
    }

    public Date getActiviyDateTime() {
        return mActiviyDateTime;
    }

    public void setActiviyDateTime(Date activiyDateTime) {
        mActiviyDateTime = activiyDateTime;
    }


    public String getReference() {
        return mReference;
    }

    public void setReference(String reference) {
        mReference = reference;
    }

    public boolean isComplete() {
        return mComplete;
    }

    public void setComplete(boolean complete) {
        mComplete = complete;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }
}
