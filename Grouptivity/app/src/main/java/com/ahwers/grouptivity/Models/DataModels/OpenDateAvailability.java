package com.ahwers.grouptivity.Models.DataModels;

import com.ahwers.grouptivity.Models.DocumentSchemas.AvailabilitySchema;
import com.ahwers.grouptivity.Models.DocumentSchemas.AvailabilitySchema.AvailabilityDocument;
import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.HashMap;

public class OpenDateAvailability {

    private Date mDate;
    private boolean mAvailable;
    private Date mStartTime;
    private Date mEndTime;

    public OpenDateAvailability(Date date) {
        mDate = date;
        mAvailable = false;
    }

    public OpenDateAvailability(HashMap dateAvailability) {
        Timestamp timestamp;

        timestamp = (Timestamp) dateAvailability.get(AvailabilityDocument.Cols.DATE);
        mDate = timestamp.toDate();

        mAvailable = (boolean) dateAvailability.get(AvailabilityDocument.Cols.AVAILABLE);

        timestamp = (Timestamp) dateAvailability.get(AvailabilityDocument.Cols.START_TIME);
        if (timestamp != null) {
            mStartTime = timestamp.toDate();
        }

        timestamp = (Timestamp) dateAvailability.get(AvailabilityDocument.Cols.END_TIME);
        if (timestamp != null) {
            mEndTime = timestamp.toDate();
        }

    }

    public OpenDateAvailability() {}

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public boolean isAvailable() {
        return mAvailable;
    }

    public void setAvailable(boolean available) {
        mAvailable = available;
    }

    public Date getStartTime() {
        return mStartTime;
    }

    public void setStartTime(Date startTime) {
        mStartTime = startTime;
    }

    public Date getEndTime() {
        return mEndTime;
    }

    public void setEndTime(Date endTime) {
        mEndTime = endTime;
    }
}
