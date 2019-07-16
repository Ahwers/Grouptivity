package com.ahwers.grouptivity.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.ahwers.grouptivity.Fragments.EventFragment;
import com.ahwers.grouptivity.Models.DataModels.Event;
import com.ahwers.grouptivity.R;

import java.util.ArrayList;
import java.util.List;

public class EventPagerActivity extends AppCompatActivity {

    private static final String EXTRA_EVENT_ID = "com.ahwers.grouptivity.event_id";
    private static final String EXTRA_EVENTS = "com.ahwers.grouptivity.events";

    private ViewPager mEventViewPager;
    private EventPagerAdapter mEventPagerAdapter;

    private List<Event> mEvents = new ArrayList<>();
    private String mCurrentEventId;

    public static Intent newIntent(Context packageContext, String currentEventId, List<Event> events) {
        Intent intent = new Intent(packageContext, EventPagerActivity.class);
        intent.putExtra(EXTRA_EVENT_ID, currentEventId);
        intent.putParcelableArrayListExtra(EXTRA_EVENTS, (ArrayList<Event>) events);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_pager);

        mCurrentEventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        mEvents = getIntent().getParcelableArrayListExtra(EXTRA_EVENTS);

        mEventViewPager = findViewById(R.id.event_view_pager);

        FragmentManager fm = getSupportFragmentManager();
        mEventPagerAdapter = new EventPagerAdapter(fm);
        mEventViewPager.setAdapter(mEventPagerAdapter);

        for (int i = 0; i < mEvents.size(); i++) {
            if (mEvents.get(i).getId().equals(mCurrentEventId)) {
                mEventViewPager.setCurrentItem(i);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private class EventPagerAdapter extends FragmentStatePagerAdapter {

        public EventPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Event event = mEvents.get(position);
            return EventFragment.newInstance(event);
        }

        @Override
        public int getCount() {
            return mEvents.size();
        }

    }

}
