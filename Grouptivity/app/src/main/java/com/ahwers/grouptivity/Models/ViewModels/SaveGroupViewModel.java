package com.ahwers.grouptivity.Models.ViewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.ahwers.grouptivity.Models.DataModels.Group;
import com.ahwers.grouptivity.Models.Repositories.GroupRepository;

public class SaveGroupViewModel extends ViewModel {

    public static final int NAME = 0;
    public static final int TYPE = 1;
    public static final int INFO = 2;

    private LiveData<Integer> mGroupSaveState;
    private LiveData<Group> mGroup;
    private GroupRepository mGroupRepository;

    public SaveGroupViewModel() {
        mGroupRepository = new GroupRepository();
    }

    public void init(String groupId) {
        if (mGroup != null) {
            return;
        }
        mGroup = mGroupRepository.getGroup(groupId);
    }

    public void init(Group group) {
        final MutableLiveData<Group> data = new MutableLiveData<>();
        data.setValue(group);
        mGroup = data;
    }

    public LiveData<Group> getGroup() {
        return mGroup;
    }

    public void set(int field, String value) {
        switch (field) {
            case NAME:
                mGroup.getValue().setName(value);
                break;
            case TYPE:
                mGroup.getValue().setType(value);
                break;
            case INFO:
                mGroup.getValue().setInfo(value);
                break;
        }
    }

    public LiveData<Integer> saveGroup() {
        Group group = mGroup.getValue();
        if (group.isValid()) {
            if (group.getId() == null) {
                mGroupSaveState = mGroupRepository.addGroup(group);
                return mGroupSaveState;
            } else {
                mGroupSaveState = mGroupRepository.setGroup(group);
                return mGroupSaveState;
            }
        } else {
            final MutableLiveData<Integer> data = new MutableLiveData<>();
            data.setValue(-10);
            return data;
        }
    }

}
