package com.ahwers.grouptivity.Models.ViewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.ahwers.grouptivity.Models.DataModels.Event;
import com.ahwers.grouptivity.Models.DataModels.ExtraAttribute;
import com.ahwers.grouptivity.Models.DataModels.Group;
import com.ahwers.grouptivity.Models.Presenters.EventPresenter;
import com.ahwers.grouptivity.Models.Repositories.EventRepository;
import com.ahwers.grouptivity.Models.Repositories.GroupRepository;

import java.util.Date;
import java.util.List;

public class SaveEventViewModel extends ViewModel {

    private EventRepository mEventRepository;
    private GroupRepository mGroupRepository;

    private LiveData<Integer> mEventSaveState;
    private LiveData<Integer> mRollbackState;

    private LiveData<Event> mEvent;
    private LiveData<Group> mGroup;

    private Event mEventRollback;
    private String mEventId;
    private boolean mListening;

    // Inject this shit dude
    public SaveEventViewModel() {
        mEventRepository = new EventRepository();
        mGroupRepository = new GroupRepository();
    }

    public void init(String eventId, String groupId) {
        if (mEvent != null) {
            return;
        }

        mEventId = eventId;
        if (mEventId == null) {
            // New event
            final MutableLiveData<Event> data = new MutableLiveData<>();
            data.setValue(new Event());
            mEvent = data;
        } else {
            // Existing event
            startListening();
        }

        mGroup = mGroupRepository.getGroup(groupId);
    }

    public LiveData<Event> getEvent() {
        return mEvent;
    }

    public LiveData<Group> getGroup() { return mGroup; }

    public void startListening() {
        mListening = true;
        if (mEventId == null) {
            mEventId = mEventRepository.getNewEventId();
        }

        mEvent = mEventRepository.getEventListener(mEventId);
    }

    public void stopListening() {
        mListening = false;
        mEventRepository.stopListening();
    }

    public boolean isListening() {
        return mListening;
    }

    public LiveData<Integer> saveEvent() {
        Event event = mEvent.getValue();
        if (event.isValid()) {
            if (event.getId() == null) {
                mEventSaveState = mEventRepository.addEvent(event);
                return mEventSaveState;
            } else {
                mEventSaveState = mEventRepository.setEvent(event);
                return mEventSaveState;
            }
        } else {
            final MutableLiveData<Integer> data = new MutableLiveData<>();
            data.setValue(-10);
            return data;
        }
    }

    public void updateEventEditingState(boolean editing) {
        Event event = mEvent.getValue();
        if (event != null) {
            // Don't attempt to update an inexistant event or update it's editing field to it's already current value
            if (event.getId() != null && event.isEditing() != editing) {
                event.setEditing(editing);
                mEventRepository.updateEditingState(event.getId(), editing);
            }
        }
    }

    public LiveData<Integer> rollbackSave() {
        mRollbackState = mEventRepository.setEvent(mEventRollback);
        return mRollbackState;
    }

    public void setEventTitle(String title) {
        mEvent.getValue().setTitle(title);
    }

    public void setEventLocation(String location) {
        mEvent.getValue().setLocation(location);
    }

    public Date getEventStartDateTime(){
        return mEvent.getValue().getStartDateTime();
    }

    public Date getEventEndDateTime() {
        return mEvent.getValue().getEndDateTime();
    }

    public void setEventRollback(Event eventRollback) {
        mEventRollback = eventRollback;
    }

    public List<ExtraAttribute> getEventCustomAttributes() {
        return mEvent.getValue().getExtraAttributes();
    }

}
