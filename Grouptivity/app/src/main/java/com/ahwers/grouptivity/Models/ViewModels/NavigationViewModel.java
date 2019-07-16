package com.ahwers.grouptivity.Models.ViewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.ahwers.grouptivity.Models.DataModels.Group;
import com.ahwers.grouptivity.Models.Repositories.GroupRepository;

import java.util.List;

public class NavigationViewModel extends ViewModel {

    private String mUserId;
    private LiveData<List<Group>> mGroups;
    private GroupRepository mGroupRepository;
    private boolean mListening;

    public NavigationViewModel() {
        mGroupRepository = new GroupRepository();
    }

    public void init(String userId) {
        if (mGroups != null) {
            return;
        }
        mUserId = userId;
        startListening();
    }

    public void reset() {
        mUserId = null;
        mGroups = null;
    }

    public LiveData<List<Group>> getGroups() {
        return mGroups;
    }

    public String getUserId() {
        return mUserId;
    }

    public void startListening() {
        mListening = true;
        mGroups = mGroupRepository.getUsersGroupsListener(mUserId);
    }

    public void stopListening() {
        mListening = false;
        mGroupRepository.stopListening();
    }

    public boolean isListening() {
        return mListening;
    }
}
