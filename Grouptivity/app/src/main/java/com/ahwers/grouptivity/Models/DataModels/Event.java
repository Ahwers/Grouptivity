package com.ahwers.grouptivity.Models.DataModels;

import android.os.Parcel;
import android.os.Parcelable;

import com.ahwers.grouptivity.Models.DocumentSchemas.EventSchema;
import com.ahwers.grouptivity.Models.DocumentSchemas.EventSchema.EventCollection;
import com.ahwers.grouptivity.Models.DocumentSchemas.GroupSchema;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Event {

    // Shouldn't be here
    public static final String UPDATE_SOURCE_LOCAL = "local";
    public static final String UPDATE_SOURCE_SERVER = "server";

    private String mId;
    private String mTitle;
    private String mType;
    private String mLocation;
    private Date mStartDateTime;
    private Date mEndDateTime;
    private String mGroupId;
    private String mGroupName;
    private Date mLastUpdated;
    private Date mCreated;
    private String mCreatorId;
    private OpenDateTime mOpenDate;
    private List<String> mGroupOwners = new ArrayList<>();
    private List<GroupMember> mParticipants = new ArrayList<>();
    private List<ExtraAttribute> mExtraAttributes = new ArrayList<>();
    private String mUpdateSource;
    private boolean mEditing;

    public Event() {

    }

    public Event(DocumentSnapshot document) {
        mId = document.getId();
        mTitle = (String) document.get(EventCollection.Cols.TITLE);
        mType = (String) document.get(EventCollection.Cols.TYPE);
        mLocation = (String) document.get(EventCollection.Cols.LOCATION);
        mGroupId = (String) document.get(EventSchema.EventCollection.Cols.GROUP_ID);
        mGroupName = (String) document.get(EventCollection.Cols.GROUP_NAME);
        mGroupOwners = (List<String>) document.get(EventCollection.Cols.GROUP_OWNERS);

        Map<String, Object> participantsMap = (Map<String, Object>) document.get(EventCollection.Cols.PARTICIPANTS);

        for (String id : participantsMap.keySet()) {
            Map<String, Object> memberMap = (Map<String, Object>) participantsMap.get(id);

            GroupMember member = new GroupMember();
            member.setId((String) memberMap.get(GroupSchema.GroupCollection.Cols.Members.ID));
            member.setOwner((boolean) memberMap.get(GroupSchema.GroupCollection.Cols.Members.OWNER));
            member.setTitle((String) memberMap.get(GroupSchema.GroupCollection.Cols.Members.TITLE));
            member.setDisplayName((String) memberMap.get(GroupSchema.GroupCollection.Cols.Members.DISPLAY_NAME));

            mParticipants.add(member);
        }

        Timestamp startDateTime = (Timestamp) document.get(EventSchema.EventCollection.Cols.START_DATE_TIME);
        Timestamp endDateTime = (Timestamp) document.get(EventCollection.Cols.END_DATE_TIME);
        if (startDateTime != null && endDateTime != null) {
            mStartDateTime = startDateTime.toDate();
            mEndDateTime = endDateTime.toDate();
        } else {
            mStartDateTime = null;
            mEndDateTime = null;
        }

        Map<String, Object> flexDateMap = (Map<String, Object>) document.get(EventCollection.Cols.FlexDateTime.NAME);
        Timestamp flexDateStart = null;
        Timestamp flexDateEnd = null;
        if (flexDateMap != null) {
            flexDateStart = (Timestamp) flexDateMap.get(EventSchema.EventCollection.Cols.FlexDateTime.PERIOD_START);
            flexDateEnd = (Timestamp) flexDateMap.get(EventSchema.EventCollection.Cols.FlexDateTime.PERIOD_END);

            if (flexDateStart != null && flexDateEnd != null) {
                mOpenDate = new OpenDateTime(
                        flexDateStart.toDate(),
                        flexDateEnd.toDate(),
                        (List<String>) flexDateMap.get(EventCollection.Cols.FlexDateTime.VOTERS)
                );
            }
        }
//        } else {
//            mOpenDate = new OpenDateTime();
//        }

        setExtraAttributes((HashMap<String, String>) document.get(EventSchema.EventCollection.Cols.EXTRA_ATTRIBUTES));

        mEditing = (boolean) document.get("editing");

    }

    public Map<String, Object> getEventDocumentMap() {
        Map<String, Object> eventMap = new HashMap<>();

        eventMap.put(EventSchema.EventCollection.Cols.TITLE, mTitle);
        eventMap.put(EventSchema.EventCollection.Cols.TYPE, mType);
        eventMap.put(EventCollection.Cols.LOCATION, mLocation);
        eventMap.put(EventCollection.Cols.START_DATE_TIME, mStartDateTime);
        eventMap.put(EventSchema.EventCollection.Cols.END_DATE_TIME, mEndDateTime);
        eventMap.put(EventCollection.Cols.GROUP_ID, mGroupId);
        eventMap.put(EventSchema.EventCollection.Cols.GROUP_NAME, mGroupName);
        eventMap.put(EventCollection.Cols.GROUP_OWNERS, mGroupOwners);

        Map<String, Object>  participantsMap = new HashMap<>();

        List<String> participantIds = new ArrayList<>();
        for (GroupMember member : mParticipants) {
            Map<String, Object> participantMap = new HashMap<>();
            participantMap.put(GroupSchema.GroupCollection.Cols.Members.ID, member.getId());
            participantMap.put(GroupSchema.GroupCollection.Cols.Members.OWNER, member.isOwner());
            participantMap.put(GroupSchema.GroupCollection.Cols.Members.TITLE, member.getTitle());
            participantMap.put(GroupSchema.GroupCollection.Cols.Members.DISPLAY_NAME, member.getDisplayName());

            participantsMap.put(member.getId(), participantMap);

            participantIds.add(member.getId());
        }

        eventMap.put(EventCollection.Cols.PARTICIPANT_IDS, participantIds);

        eventMap.put(EventCollection.Cols.PARTICIPANTS, participantsMap);

        Map<String, Object> extraAttributesMap = new HashMap<>();
        for (ExtraAttribute attribute : mExtraAttributes) {
            extraAttributesMap.put(attribute.getTitle(), attribute.getValue());
        }
        eventMap.put(EventSchema.EventCollection.Cols.EXTRA_ATTRIBUTES, extraAttributesMap);

        if (getOpenDate() != null) {
            Map<String, Object> flexDateTimeMap = getOpenDate().getDocumentMap();
            eventMap.put(EventCollection.Cols.FlexDateTime.NAME, flexDateTimeMap);
        }

        eventMap.put(EventSchema.EventCollection.Cols.CREATED, mCreated);
        eventMap.put(EventSchema.EventCollection.Cols.CREATOR, mCreatorId);
        eventMap.put(EventCollection.Cols.LAST_UPDATED, new Date());

        eventMap.put("editing" , mEditing);

        return eventMap;

    }

    public void setGroupValues(Group group) {
        mGroupName = group.getName();
        mGroupId = group.getId();
        mGroupOwners = group.getOwners();
        mParticipants = group.getMembers();
    }

    public boolean isValid() {
        if (mTitle == null
                || mTitle == ""
                || (mOpenDate == null && mStartDateTime == null)) {
            return false;
        }
        return true;
    }

    public boolean isUserOwner(String userId) {
        for (GroupMember member : mParticipants) {
            if (member.getId().equals(userId) && member.isOwner()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSetDate() {
        return mStartDateTime != null;
    }

    public boolean dateIsSet() {
        return mStartDateTime != null ? true : false;
    }

    public String getGroupId() {
        return mGroupId;
    }

    public void setGroupId(String groupId) {
        mGroupId = groupId;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public String getId() {
        return mId;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String location) {
        mLocation = location;
    }

    public List<ExtraAttribute> getExtraAttributes() {
        return mExtraAttributes;
    }

    private List<String> getExtraAttributeTitles() {
        List<String> titles = new ArrayList<>();
        for (ExtraAttribute attribute : mExtraAttributes) {
            titles.add(attribute.getTitle());
        }
        return titles;
    }

    private List<String> getExtraAttributeValues() {
        List<String> values = new ArrayList<>();
        for (ExtraAttribute attribute : mExtraAttributes) {
            values.add(attribute.getValue());
        }
        return values;
    }

    public void setExtraAttributes(List<ExtraAttribute> extraAttributes) {
        mExtraAttributes = extraAttributes;
    }

    public void setExtraAttributes(HashMap<String, String> attributes) {
        try {
            for (String i : attributes.keySet()) {
                mExtraAttributes.add(new ExtraAttribute(i, attributes.get(i)));
            }
        } catch (NullPointerException e) {

        }
    }

    public void setExtraAttributes(List<String> titles, List<String> values) {
        int size = titles.size() > values.size() ? titles.size() : values.size();
        List<ExtraAttribute> attributes = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            attributes.add(new ExtraAttribute(titles.get(i), values.get(i)));
        }
        mExtraAttributes = attributes;
    }

    public void addExtraAttribute(ExtraAttribute attribute) {
        mExtraAttributes.add(attribute);
    }

    public void updateAttributeBy(int position, ExtraAttribute attribute) {
        mExtraAttributes.remove(position);
        mExtraAttributes.add(position, attribute);
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public Date getStartDateTime() {
        return mStartDateTime;
    }

    public void setStartDateTime(Date startDateTime) {
        mStartDateTime = startDateTime;
    }

    public Date getEndDateTime() {
        return mEndDateTime;
    }

    public void setEndDateTime(Date endDateTime) {
        mEndDateTime = endDateTime;
    }

    public String getDateToString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(EventSchema.EventCollection.Formats.DATE_FORMAT);

        Date startDate;
        Date endDate;

        String dateOutput = "";

        if (hasSetDate()) {
            startDate = getStartDateTime();
            endDate = getEndDateTime();
        } else {
            startDate = getOpenDate().getStartDate();
            endDate = getOpenDate().getEndDate();
            dateOutput = dateOutput + "Between: ";
        }

        dateOutput = dateOutput + dateFormat.format(startDate) + " ";

        if (endDate.after(startDate)) {
            dateOutput = dateOutput + ("to " + dateFormat.format(endDate));
        }

        return dateOutput;
    }

    public String getTimeToString() {
        SimpleDateFormat timeFormat = new SimpleDateFormat(EventSchema.EventCollection.Formats.TIME_FORMAT);

        Date startDate;
        Date endDate;

        String timeOutput = "";

        if (hasSetDate()) {
            startDate = getStartDateTime();
            endDate = getEndDateTime();

            timeOutput = timeOutput + timeFormat.format(startDate) + " ";

            if (endDate.after(startDate)) {
                timeOutput = timeOutput + ("- " + timeFormat.format(endDate));
            }
        } else {
            timeOutput = "Undecided";
        }

        return timeOutput;
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

    public String getCreatorId() {
        return mCreatorId;
    }

    public void setCreatorId(String creatorId) {
        mCreatorId = creatorId;
    }

//    protected Event(Parcel in) {
//        mId = in.readString();
//        mTitle = in.readString();
//        mType = in.readString();
//        mLocation = in.readString();
//        mStartDateTime = (Date) in.readSerializable();
//        mEndDateTime = (Date) in.readSerializable();
//        mOpenDate = new OpenDateTime();
//        mOpenDate.setStartDate((Date) in.readSerializable());
//        mOpenDate.setEndDate((Date) in.readSerializable());
//        List<String> voters = new ArrayList<>();
//        in.readStringList(voters);
//        mOpenDate.setVoters(voters);
//        List<String> titles = new ArrayList<>();
//        in.readStringList(titles);
//        List<String> values = new ArrayList<>();
//        in.readStringList(values);
//        setExtraAttributes(titles, values);
//
//        mGroupId = in.readString();
//        mGroupName = in.readString();
//        in.readStringList(mGroupOwners);
//
//        mCreated = (Date) in.readSerializable();
//        mLastUpdated = (Date) in.readSerializable();
//        mCreatorId = in.readString();
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeString(mId);
//        dest.writeString(mTitle);
//        dest.writeString(mType);
//        dest.writeString(mLocation);
//        dest.writeSerializable(mStartDateTime);
//        dest.writeSerializable(mEndDateTime);
//        dest.writeSerializable(mOpenDate.getStartDate());
//        dest.writeSerializable(mOpenDate.getEndDate());
//        dest.writeStringList(mOpenDate.getVoters());
//        dest.writeStringList(getExtraAttributeTitles());
//        dest.writeStringList(getExtraAttributeValues());
//
//        dest.writeString(mGroupId);
//        dest.writeString(mGroupName);
//        dest.writeStringList(mGroupOwners);
//
//        dest.writeSerializable(mCreated);
//        dest.writeSerializable(mLastUpdated);
//        dest.writeString(mCreatorId);
//    }
//
//    public static final Creator<Event> CREATOR = new Creator<Event>() {
//        @Override
//        public Event createFromParcel(Parcel in) {
//            return new Event(in);
//        }
//
//        @Override
//        public Event[] newArray(int size) {
//            return new Event[size];
//        }
//    };
//
//    @Override
//    public int describeContents() {
//        return 0;
//    }

    public String getGroupName() {
        return mGroupName;
    }

    public void setGroupName(String groupName) {
        mGroupName = groupName;
    }

    public List<String> getGroupOwners() {
        return mGroupOwners;
    }

    public void setGroupOwners(List<String> groupOwners) {
        mGroupOwners = groupOwners;
    }

    public OpenDateTime getOpenDate() {
        return mOpenDate;
    }

    public void setOpenDate(OpenDateTime openDate) {
        mOpenDate = openDate;
    }

    public List<GroupMember> getParticipants() {
        return mParticipants;
    }

    public void setParticipants(List<GroupMember> participants) {
        mParticipants = participants;
    }

    public String getUpdateSource() {
        return mUpdateSource;
    }

    public void setUpdateSource(String updateSource) {
        mUpdateSource = updateSource;
    }

    public boolean isEditing() {
        return mEditing;
    }

    public void setEditing(boolean editing) {
        mEditing = editing;
    }
}
