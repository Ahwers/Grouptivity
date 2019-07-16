package com.ahwers.grouptivity.Models.ViewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.ahwers.grouptivity.Models.DataModels.Group;
import com.ahwers.grouptivity.Models.Repositories.GroupRepository;

public class MembersListViewModel extends ViewModel {

    private LiveData<Integer> mGroupSaveState;
    private LiveData<Group> mGroup;
    private GroupRepository mGroupRepository;

    public MembersListViewModel() {
        mGroupRepository = new GroupRepository();
    }

    public void init(String groupId) {
        if (mGroup != null) {
            return;
        }
        mGroup = mGroupRepository.getGroup(groupId);
    }

    public LiveData<Group> getGroup() {
        return mGroup;
    }

    public LiveData<Integer> saveGroup() {
            Group group = mGroup.getValue();
            if (group.getId() == null) {
                mGroupSaveState = mGroupRepository.addGroup(group);
                return mGroupSaveState;
            } else {
                mGroupSaveState = mGroupRepository.setGroup(group);
                return mGroupSaveState;
            }
    }

    public LiveData<Group> refreshGroup() {
        mGroup = mGroupRepository.getGroup(mGroup.getValue().getId());
        return mGroup;
    }

    public void inviteMember(String recipientUserId) {
        mGroupRepository.inviteUserToGroup(mGroup.getValue().getId(), mGroup.getValue().getName(), recipientUserId);
    }

}
