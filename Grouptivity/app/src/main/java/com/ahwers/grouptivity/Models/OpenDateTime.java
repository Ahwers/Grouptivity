package com.ahwers.grouptivity.Models;

import com.ahwers.grouptivity.Models.DocumentSchemas.EventSchema;
import com.ahwers.grouptivity.Models.DocumentSchemas.EventSchema.EventCollection;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenDateTime {

    private Date mStartDate;
    private Date mEndDate;
    private List<String> mVoters = new ArrayList<>();

    public OpenDateTime() {

    }

    public OpenDateTime(Date startDate, Date endDate, List<String> voters) {
        mStartDate = startDate;
        mEndDate = endDate;
        mVoters = voters;
    }

    public boolean userVoted(String userId) {
        for (String voters : mVoters) {
            if (voters.equals(userId)) {
                return true;
            }
        }
        return false;
    }

    public Date getStartDate() {
        return mStartDate;
    }

    public void setStartDate(Date startDate) {
        mStartDate = startDate;
    }

    public Date getEndDate() {
        return mEndDate;
    }

    public void setEndDate(Date endDate) {
        mEndDate = endDate;
    }

    public List<String> getVoters() {
        return mVoters;
    }

    public void setVoters(List<String> voters) {
        mVoters = voters;
    }

    public Map<String, Object> getDocumentMap() {
        Map<String, Object> documentMap = new HashMap<>();
        documentMap.put(EventSchema.EventCollection.Cols.FlexDateTime.PERIOD_START, mStartDate);
        documentMap.put(EventCollection.Cols.FlexDateTime.PERIOD_END, mEndDate);
        documentMap.put(EventCollection.Cols.FlexDateTime.VOTERS, mVoters);
        return documentMap;
    }

}
