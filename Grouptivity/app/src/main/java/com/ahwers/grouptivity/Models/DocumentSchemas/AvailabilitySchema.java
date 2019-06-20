package com.ahwers.grouptivity.Models.DocumentSchemas;

public class AvailabilitySchema {

    public static final class AvailabilityDocument {
        public static final String NAME = "availabilities";

        public static final class Cols {
            public static final String DATE = "date";
            public static final String AVAILABLE = "available";
            public static final String START_TIME = "start_time";
            public static final String END_TIME = "end_time";
        }

    }

}
