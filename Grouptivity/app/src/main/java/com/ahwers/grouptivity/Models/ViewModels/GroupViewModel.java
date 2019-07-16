package com.ahwers.grouptivity.Models.ViewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.ahwers.grouptivity.Models.DataModels.Group;
import com.ahwers.grouptivity.Models.Repositories.GroupRepository;

public class GroupViewModel extends ViewModel {

    private static final String TAG = "GroupViewModel";

    private LiveData<Integer> mGroupSaveState;
    private LiveData<Group> mGroup;
    private GroupRepository mGroupRepository;

    public GroupViewModel() {
        mGroupRepository = new GroupRepository();
    }

    public void init(String groupid) {
        if (mGroup != null) {
            return;
        }
        mGroup = mGroupRepository.getGroup(groupid);
    }

    public LiveData<Integer> saveGroup() {
        Group group = mGroup.getValue();

        mGroupSaveState = mGroupRepository.setGroup(group);
        return mGroupSaveState;

    }

    public LiveData<Group> getGroup() {
        return mGroup;
    }

    public LiveData<Group> refreshGroup() {
        mGroup = mGroupRepository.getGroup(mGroup.getValue().getId());
        return mGroup;
    }

}
