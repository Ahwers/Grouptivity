package com.ahwers.grouptivity.Models.ViewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.ahwers.grouptivity.Models.OpenDateAvailability;
import com.ahwers.grouptivity.Models.Repositories.EventRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AvailabilitySubmissionViewModel extends ViewModel {

    private String mUserId;
    private String mEventId;
    private boolean mUserVoted;
    private Date mStartDate;
    private Date mEndDate;
    private LiveData<Integer> mAvailabilitySaveState;
    private LiveData<List<OpenDateAvailability>> mAvailabilities;
    private EventRepository mEventRepository;

    public AvailabilitySubmissionViewModel() {
        mEventRepository = new EventRepository();
    }

    public void init(String userId, String eventId) {
        if (mAvailabilities != null) {
            return;
        }
        mUserId = userId;
        mEventId = eventId;
        mAvailabilities = mEventRepository.getAvailability(mUserId, mEventId);
    }

    public LiveData<List<OpenDateAvailability>> getAvailability() {
        return mAvailabilities;
    }

    public LiveData<Integer> saveAvailability() {
        List<OpenDateAvailability> availability = mAvailabilities.getValue();
        mAvailabilitySaveState = mEventRepository.setAvailability(availability, mUserId, mEventId);
        return mAvailabilitySaveState;
    }

}
