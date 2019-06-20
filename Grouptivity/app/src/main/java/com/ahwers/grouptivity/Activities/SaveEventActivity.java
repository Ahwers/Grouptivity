package com.ahwers.grouptivity.Activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import com.ahwers.grouptivity.Fragments.SaveEventFragment;

public class SaveEventActivity extends SingleFragmentActivity {

    private static final String EXTRA_EVENT_ID = "com.ahwers.grouptivity.event_id";
    private static final String EXTRA_GROUP_ID = "com.ahwers.grouptivity.group_id";

    public static Intent newIntent(Context packageContext, String eventId, String groupId) {
        Intent intent = new Intent(packageContext, SaveEventActivity.class);
        intent.putExtra(EXTRA_EVENT_ID, eventId);
        intent.putExtra(EXTRA_GROUP_ID, groupId);

        return intent;
    }

    @Override
    public Fragment createFragment() {
        return SaveEventFragment.newInstance(
                getIntent().getStringExtra(EXTRA_EVENT_ID),
                getIntent().getStringExtra(EXTRA_GROUP_ID)
        );
    }
}
