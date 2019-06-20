package com.ahwers.grouptivity.Models;

import com.ahwers.grouptivity.Models.DocumentSchemas.EventSchema.EventCollection;
import com.ahwers.grouptivity.Models.DocumentSchemas.GroupSchema;
import com.ahwers.grouptivity.Models.DocumentSchemas.GroupSchema.GroupCollection;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Group {

    private String mId;
    private String mName;
    private String mType;
    private String mInfo;
    private List<GroupMember> mMembers = new ArrayList<>();
    private String mCreator;
    private Date mCreated;
    private Date mLastUpdated;

    public Group() {}

    public Group(String groupName, String groupType, String groupInfo) {
        mName = groupName;
        mType = groupType;
        mInfo = groupInfo;
    }

    public Group(GroupMember groupCreator) {
        mMembers.add(groupCreator);
    }

    public Group(DocumentSnapshot document) {
        mId = document.getId();
        mName = (String) document.get(GroupSchema.GroupCollection.Cols.GROUP_NAME);
        mType = (String) document.get(GroupSchema.GroupCollection.Cols.TYPE);
        mInfo = (String) document.get(GroupCollection.Cols.INFO);

        Map<String, Object> groupMembersMap = (Map<String, Object>) document.get(GroupSchema.GroupCollection.Cols.Members.MapName);

        for (String id : groupMembersMap.keySet()) {
            Map<String, Object> memberMap = (Map<String, Object>) groupMembersMap.get(id);

            GroupMember member = new GroupMember();
            member.setId((String) memberMap.get(GroupSchema.GroupCollection.Cols.Members.ID));
            member.setOwner((boolean) memberMap.get(GroupSchema.GroupCollection.Cols.Members.OWNER));
            member.setTitle((String) memberMap.get(GroupCollection.Cols.Members.TITLE));
            member.setDisplayName((String) memberMap.get(GroupSchema.GroupCollection.Cols.Members.DISPLAY_NAME));

            mMembers.add(member);
        }

        Timestamp created = (Timestamp) document.get(GroupSchema.GroupCollection.Cols.CREATED);
        mCreated = created.toDate();

        Timestamp lastUpdated = (Timestamp) document.get(GroupSchema.GroupCollection.Cols.LAST_UPDATED);
        mLastUpdated = lastUpdated.toDate();

        mCreator = (String) document.get(GroupCollection.Cols.CREATOR);
    }

    public boolean isValid() {
        if (mName != null) {
            return true;
        }
        return false;
    }

    public boolean isUserOwner(String userId) {
        for (GroupMember member : mMembers) {
            if (member.getId().equals(userId) && member.isOwner()) {
                return true;
            }
        }
        return false;
    }

    public boolean isUserMember(String userId) {
        for (GroupMember member : mMembers) {
            if (member.getId().equals(userId)) {
                return true;
            }
        }
        return false;
    }

    public Map<String, Object> getGroupDocumentMap() {
        Map<String, Object> eventMap = new HashMap<>();

        eventMap.put(GroupSchema.GroupCollection.Cols.GROUP_NAME, mName);
        eventMap.put(GroupSchema.GroupCollection.Cols.TYPE, mType);
        eventMap.put(GroupSchema.GroupCollection.Cols.INFO, mInfo);

        Map<String, Object>  groupMembersMap = new HashMap<>();

        for (GroupMember member : mMembers) {
            Map<String, Object> memberMap = new HashMap<>();
            memberMap.put(GroupSchema.GroupCollection.Cols.Members.ID, member.getId());
            memberMap.put(GroupCollection.Cols.Members.OWNER, member.isOwner());
            memberMap.put(GroupCollection.Cols.Members.TITLE, member.getTitle());
            memberMap.put(GroupSchema.GroupCollection.Cols.Members.DISPLAY_NAME, member.getDisplayName());

            groupMembersMap.put(member.getId(), memberMap);
        }

        eventMap.put(GroupSchema.GroupCollection.Cols.Members.MapName, groupMembersMap);

        eventMap.put(EventCollection.Cols.CREATOR, mCreator);
        eventMap.put(EventCollection.Cols.CREATED, mCreated);
        eventMap.put(EventCollection.Cols.LAST_UPDATED, new Date());

        return eventMap;
    }

    public List<String> getOwners() {
        List<String> owners = new ArrayList<>();

        for (GroupMember member : mMembers) {
            if (member.isOwner()) {
                owners.add(member.getId());
            }
        }

        return owners;
    }

    public List<String> getMemberIds() {
        List<String> members = new ArrayList<>();

        for (GroupMember groupMember : mMembers) {
            members.add(groupMember.getId());
        }

        return members;
    }

    public List<GroupMember> getMembers() {
        return mMembers;
    }

    public void setMembers(List<GroupMember> members) {
        mMembers = members;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getInfo() {
        return mInfo;
    }

    public void setInfo(String info) {
        mInfo = info;
    }

    public Date getLastUpdated() {
        return mLastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        mLastUpdated = lastUpdated;
    }

    public Date getCreated() {
        return mCreated;
    }

    public void setCreated(Date created) {
        mCreated = created;
    }

    public String getCreator() {
        return mCreator;
    }

    public void setCreator(String creator) {
        mCreator = creator;
    }
}
