package com.ahwers.grouptivity.Models.DocumentSchemas;

public class EventSchema {

    public static final class EventCollection {
        public static final String NAME = "events";

        public static final class Formats {
            public static final String DATE_FORMAT = "dd MMMM yyyy";
            public static final String TIME_FORMAT = "HH:mm";
        }

        public static final class Cols {
            public static final String TITLE = "title";
            public static final String TYPE = "type";
            public static final String LOCATION = "location";
            public static final String START_DATE_TIME = "start_date_time";
            public static final String END_DATE_TIME = "end_date_time";
            public static final String EXTRA_ATTRIBUTES = "extra_attributes";
            public static final String CREATED = "created";
            public static final String LAST_UPDATED = "last_updated";
            public static final String CREATOR = "creator";
            public static final String GROUP_ID = "group_id";
            public static final String GROUP_NAME = "group_name";
            public static final String GROUP_OWNERS = "group_owners";
            public static final String PARTICIPANTS = "participants";
            public static final String PARTICIPANT_IDS = "participant_ids";

            public static final class FlexDateTime {
                public static final String NAME = "flex_date_time";
                public static final String PERIOD_START = "period_start";
                public static final String PERIOD_END = "period_end";
                public static final String VOTERS = "voters";
            }

        }
    }
}
