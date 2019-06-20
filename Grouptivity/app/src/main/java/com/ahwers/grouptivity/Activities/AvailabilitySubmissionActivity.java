package com.ahwers.grouptivity.Activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import com.ahwers.grouptivity.Fragments.AvailabilitySubmissionFragment;
import com.ahwers.grouptivity.Models.Event;

import java.util.Date;


public class AvailabilitySubmissionActivity extends SingleFragmentActivity {

    private static final String EXTRA_START_DATE = "com.ahwers.grouptivity.start_date";
    private static final String EXTRA_END_DATE = "com.ahwers.grouptivity.end_date";
    private static final String EXTRA_EVENT_ID = "com.ahwers.grouptivity.event_id";
    private static final String EXTRA_MEMBER_SUBMITTED = "com.ahwers.grouptivity.user_submitted";

    public static Intent newIntent(Context packageContext, String eventId, Date startDate, Date endDate, boolean memberSubmitted) {
        Intent intent = new Intent(packageContext, AvailabilitySubmissionActivity.class);
        intent.putExtra(EXTRA_EVENT_ID, eventId);
        intent.putExtra(EXTRA_START_DATE, startDate);
        intent.putExtra(EXTRA_END_DATE, endDate);
        intent.putExtra(EXTRA_MEMBER_SUBMITTED, memberSubmitted);

        return intent;
    }

    @Override
    public Fragment createFragment() {

        return AvailabilitySubmissionFragment.newInstance(
                (String) getIntent().getStringExtra(EXTRA_EVENT_ID),
                (Date) getIntent().getSerializableExtra(EXTRA_START_DATE),
                (Date) getIntent().getSerializableExtra(EXTRA_END_DATE),
                (boolean) getIntent().getBooleanExtra(EXTRA_MEMBER_SUBMITTED, false)
        );
    }

}
