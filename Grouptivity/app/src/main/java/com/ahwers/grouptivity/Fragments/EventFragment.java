package com.ahwers.grouptivity.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ahwers.grouptivity.Activities.SaveEventActivity;
import com.ahwers.grouptivity.Models.DocumentSchemas.EventSchema;
import com.ahwers.grouptivity.Models.Event;
import com.ahwers.grouptivity.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EventFragment extends Fragment {

    private static final String ARG_EVENT = "event";

    private TextView mTitleTextView;
    private TextView mGroupAndTypeTextView;
    private TextView mDateRangeTextView;
    private TextView mTimeRangeTextView;
    private TextView mLocationTextView;
    private RecyclerView mAttributesRecyclerView;
    private MenuItem mEditEventMenuItem;
    private FirebaseAuth mAuth;

    private Event mEvent;
    private boolean mUserIsOwner;


    public static Fragment newInstance(Event event) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_EVENT, event);

        EventFragment fragment = new EventFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mEvent = getArguments().getParcelable(ARG_EVENT);

        mAuth = FirebaseAuth.getInstance();

        for (String userId : mEvent.getGroupOwners()) {
            if (mAuth.getCurrentUser().getUid().equals(userId)) {
                mUserIsOwner = true;
                break;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_event, container, false);

        mTitleTextView = v.findViewById(R.id.event_title);
        mTitleTextView.setText(mEvent.getTitle());

        mGroupAndTypeTextView = v.findViewById(R.id.event_type);
        if (mEvent.getType() != null) {
            mGroupAndTypeTextView.setText(mEvent.getGroupName() + " - " + mEvent.getType());
        } else {
            mGroupAndTypeTextView.setText(mEvent.getGroupName());
        }

        mDateRangeTextView = v.findViewById(R.id.event_date_range);
        mTimeRangeTextView = v.findViewById(R.id.event_time_range);
        updateDateAndTimeViews();

        mLocationTextView = v.findViewById(R.id.event_location);
        mLocationTextView.setText(mEvent.getLocation());

        mAttributesRecyclerView = v.findViewById(R.id.attributes_recycler_view);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_event, menu);

        mEditEventMenuItem = menu.findItem(R.id.edit_event);
        mEditEventMenuItem.setVisible(mUserIsOwner);
        mEditEventMenuItem.setEnabled(mUserIsOwner);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_event:
               Intent intent = SaveEventActivity.newIntent(getActivity(), mEvent.getId(), mEvent.getGroupId());
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateDateAndTimeViews() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(EventSchema.EventCollection.Formats.DATE_FORMAT);
        SimpleDateFormat timeFormat = new SimpleDateFormat(EventSchema.EventCollection.Formats.TIME_FORMAT);

        if (mEvent.getStartDateTime() != null && mEvent.getEndDateTime() != null) {
            Date startDate = null;
            Date startTime = null;
            Date endDate = null;
            Date endTime = null;
            try {
                startDate = dateFormat.parse(dateFormat.format(mEvent.getStartDateTime()));
                startTime = timeFormat.parse(timeFormat.format(mEvent.getStartDateTime()));
                endDate = dateFormat.parse(dateFormat.format(mEvent.getEndDateTime()));
                endTime = timeFormat.parse(timeFormat.format(mEvent.getEndDateTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (endDate.after(startDate)) {
                mDateRangeTextView.setText(dateFormat.format(startDate) + " - " + dateFormat.format(endDate));
            } else {
                mDateRangeTextView.setText(dateFormat.format(startDate));
            }
 
            if (endTime.after(startTime)) {
                mTimeRangeTextView.setText(timeFormat.format(startTime) + " - " + timeFormat.format(endTime));
            } else {
                mTimeRangeTextView.setText(timeFormat.format(startTime));
            }

        } else {
            if (mEvent.getOpenDate().userVoted(mEvent.getId())) {
                mDateRangeTextView.setText("Date not decided.\n" +
                        "Open date from " + dateFormat.format(mEvent.getOpenDate().getStartDate()) +
                        " to " + dateFormat.format(mEvent.getOpenDate().getEndDate())
                );
                mTimeRangeTextView.setText("Click here to change your availability");
            } else {
                mDateRangeTextView.setText("Date not decided.\n" +
                        "Open date from " + dateFormat.format(mEvent.getOpenDate().getStartDate()) +
                        " to " + dateFormat.format(mEvent.getOpenDate().getEndDate())
                );
                mTimeRangeTextView.setText("Click here enter your availability");
            }

        }
    }


}
