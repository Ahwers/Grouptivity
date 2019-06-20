package com.ahwers.grouptivity.Models.ViewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.ahwers.grouptivity.Models.Event;
import com.ahwers.grouptivity.Models.Repositories.EventRepository;

import java.util.List;

public class EventListViewModel extends ViewModel {

    private static final String TAG = "EventListViewModel";

    private String mUserId;
    private String mGroupId;
    private LiveData<List<Event>> mEventsList;
    private EventRepository mEventRepository;

    public EventListViewModel() { mEventRepository = new EventRepository(); }

    public void init(String userId, String groupId){
        if (mEventsList != null) {
            return;
        }
        mGroupId = groupId;
        mUserId = userId;

        if (mGroupId != null) {
            mEventsList = mEventRepository.getGroupEvents(mGroupId);
        } else {
            mEventsList = mEventRepository.getUserEvents(mUserId);
        }
    }

    public LiveData<List<Event>> getEventsList() {
        return mEventsList;
    }

    public LiveData<List<Event>> refreshEventsList() {
        if (mGroupId != null) {
            mEventsList = mEventRepository.getGroupEvents(mGroupId);
        } else {
            mEventsList = mEventRepository.getUserEvents(mUserId);
        }
        return mEventsList;
    }

    public String getGroupId() {
        return mGroupId;
    }

}
