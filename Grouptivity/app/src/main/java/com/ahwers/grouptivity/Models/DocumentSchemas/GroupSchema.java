package com.ahwers.grouptivity.Models.DocumentSchemas;

public class GroupSchema {

    public static final class GroupCollection {
        public static final String NAME = "groups";

        public static final class Cols {
            public static final String GROUP_NAME = "name";
            public static final String TYPE = "type";
            public static final String INFO = "info";
            public static final String CREATED = "created";
            public static final String LAST_UPDATED = "last_updated";
            public static final String CREATOR = "creator";

            public static final class Members {
                public static final String MapName = "members";

                public static final String DISPLAY_NAME = "display_name";
                public static final String ID = "id";
                public static final String TITLE = "title";
                public static final String OWNER = "owner";
            }

        }

    }

}
