package com.ahwers.grouptivity.Activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.ahwers.grouptivity.Fragments.SaveGroupFragment;

public class SaveGroupActivity extends SingleFragmentActivity {

    public static final String EXTRA_GROUP_ID = "com.ahwers.grouptivity.group_id";

    public static Intent newIntent(Context packageContext, String groupId) {
        Intent intent = new Intent(packageContext, SaveGroupActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);

        return intent;
    }

    @Override
    public Fragment createFragment() {
        return SaveGroupFragment.newInstance(getIntent().getStringExtra(EXTRA_GROUP_ID));
    }


}
