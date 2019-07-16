package com.ahwers.grouptivity.Fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ahwers.grouptivity.Activities.AvailabilitySubmissionActivity;
import com.ahwers.grouptivity.Activities.SaveEventActivity;
import com.ahwers.grouptivity.Activities.SaveGroupActivity;
import com.ahwers.grouptivity.Activities.SignInActivity;
import com.ahwers.grouptivity.Models.DocumentSchemas.EventSchema;
import com.ahwers.grouptivity.Models.DocumentSchemas.EventSchema.EventCollection;
import com.ahwers.grouptivity.Models.DataModels.Event;
import com.ahwers.grouptivity.Models.ViewModels.EventListViewModel;
import com.ahwers.grouptivity.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventListFragment extends Fragment {

    private static final String TAG = "EventListFragment";

    private static final String ARG_GROUP_ID = "group_id";
    private static final String ARG_USER_IS_OWNER = "user_is_owner";

    private static final String ARG_PLACEHOLDER_VIEW_TYPE = "placeholder_view_type";

    private RecyclerView mEventsRecyclerView;
    private EventAdapter mEventsAdapter;
    private FloatingActionButton mNewEventButton;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ConstraintLayout mScreen;
    private ProgressBar mProgressBar;

    private FirebaseAuth mAuth;
    private EventListViewModel mEventListViewModel;

    private boolean mGroupEvents;

    public static EventListFragment newInstance(String groupId, boolean userIsOwner) {
        Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID, groupId);
        args.putBoolean(ARG_USER_IS_OWNER, userIsOwner);

        EventListFragment fragment = new EventListFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public static EventListFragment newInstance() {
        Bundle args = new Bundle();
        EventListFragment fragment = new EventListFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");

            setHasOptionsMenu(true);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);
        Log.d(TAG, "onCreateView: ");

        mEventsRecyclerView = (RecyclerView) view.findViewById(R.id.events_recycler_vew);
        mEventsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mNewEventButton = (FloatingActionButton) view.findViewById(R.id.new_event_button);
        mNewEventButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = SaveEventActivity.newIntent(getActivity(), null, mEventListViewModel.getGroupId());
                startActivity(intent);
            }
        });
        boolean userIsOwner = (boolean) getArguments().getBoolean(ARG_USER_IS_OWNER, false);
        if (!userIsOwner) {
            mNewEventButton.hide();
        }

        mSwipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }
        });

        mScreen = view.findViewById(R.id.screen);
        mScreen.setVisibility(View.GONE);

        mProgressBar = view.findViewById(R.id.progress_bar);
        mProgressBar.setIndeterminate(true);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated: ");
        
        if (mAuth.getCurrentUser() != null) {
            Log.d(TAG, "onActivityCreated: list fetched");

            String groupId = getArguments().getString(ARG_GROUP_ID, null);
            mGroupEvents = groupId == null;

            mEventListViewModel = ViewModelProviders.of(this).get(EventListViewModel.class);
            mEventListViewModel.init(mAuth.getCurrentUser().getUid(), groupId);

            mEventListViewModel.getEventsList().observe(this, mEventsList -> {
                updateUi(mEventsList);
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");

        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            startActivity(intent);
        } else {
            if (mEventListViewModel.getEventsList().getValue() != null) {
                Log.d(TAG, "onStart: list refreshed");
                mEventListViewModel.refreshEventsList().observe(this, mEventsList -> {
                    updateUi(mEventsList);
                });
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Log.d(TAG, "onCreateOptionsMenu: ");

        inflater.inflate(R.menu.fragment_event_list, menu);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (!mGroupEvents) {
            menu.findItem(R.id.refresh_list).setEnabled(false).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_list:
                mEventListViewModel.refreshEventsList().observe(this, mEventsList -> {
                    updateUi(mEventsList);
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateUi(List<Event> eventsList) {
        Log.d(TAG, "updateUi: ");

        List<Object> viewHolderValues = new ArrayList<>();
        if (eventsList.size() > 0) {
            viewHolderValues = createHolderValues(eventsList);
        } else {
            viewHolderValues.add(ARG_PLACEHOLDER_VIEW_TYPE);
        }

        if (mEventsAdapter == null) {
            mEventsAdapter = new EventAdapter(viewHolderValues, mEventListViewModel.getGroupId() != null ? true : false);
        } else {
            mEventsAdapter.setList(viewHolderValues);
            mEventsAdapter.notifyDataSetChanged();
        }

        if (mEventsRecyclerView.getAdapter() == null) {
            mEventsRecyclerView.setAdapter(mEventsAdapter);
        }

        mScreen.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mProgressBar.setEnabled(false);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void refreshList() {
        mSwipeRefreshLayout.setRefreshing(true);
        mEventListViewModel.refreshEventsList().observe(this, mEventsList -> {
            updateUi(mEventsList);
        });
    }

    private List<Object> createHolderValues(List<Event> eventsList) {
        // List of the events to be displayed passed in as a parameter

        // New list of objects that will hold the data for the recycler view to display
        List<Object> holderValues = new ArrayList<>();

        // New lists of objects to separate events with open dates that the signed in user
        // has and hasn't submitted their availability for. (The two types have different headers)
        List<Object> openSubmittedList = new ArrayList<>();
        List<Object> openAwaitingList = new ArrayList<>();

        // Add the list headers
        openSubmittedList.add("Awaiting member's availability");
        openAwaitingList.add("Awaiting your availability");

        // This first loop pulls the open date events into their respective lists
        // Loop through all the events in the events list
        for (int i = 0; i < eventsList.size(); i++) {

            // Check if the current event has an open date
            // (events with open dates do not have start dates set)
            if (eventsList.get(i).getStartDateTime() == null) {

                // Check if the signed in user has submitted their avilability to the open date event
                // and add it to the appropriate list
                if (eventsList.get(i).getOpenDate().userVoted(mAuth.getCurrentUser().getUid())) {
                    openSubmittedList.add(eventsList.get(i));
                } else {
                    openAwaitingList.add(eventsList.get(i));
                }
            } else {
                holderValues.add(eventsList.get(i));
            }
        }

        // This date format object formats date objects to remove time in order to determine whether
        // neighbouring events in the list start on the same day
        SimpleDateFormat dateFormat = new SimpleDateFormat(EventCollection.Formats.DATE_FORMAT);

        // Descending for loop from the last event in the list downwards
        for (int j = eventsList.size() - 1; j > -1; j--) {

            // Ignore open dated events
            if (eventsList.get(j).getStartDateTime() != null) {
                try {
                    if (j == 0) {

                        // Add the date of the first event in the list if on the last event
                        holderValues.add(j, eventsList.get(j).getStartDateTime());
                    } else {

                        // Find out if the current event occurs after the next event in the list
                        if (dateFormat.parse(dateFormat.format(
                                eventsList.get(j).getStartDateTime()))
                                .after(dateFormat.parse(dateFormat.format(
                                        eventsList.get(j - 1).getStartDateTime())))) {

                            // If it does, add that event's date to the list
                            holderValues.add(j, eventsList.get(j).getStartDateTime());
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        // Adds the list of open dated events that the signed in user has submitted availability
        // for if it is not empty
        if (openSubmittedList.size() > 1) {
            holderValues.addAll(0, openSubmittedList);
        }

        // Adds the list of open dated events that the signed in user has not submitted availability
        // for if it is not empty
        if (openAwaitingList.size() > 1) {
            holderValues.addAll(0, openAwaitingList);
        }

        return holderValues;
    }


    private class EventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int SUBTITLE_VIEW_TYPE = 0;
        private static final int SET_EVENT_VIEW_TYPE = 1;
        private static final int OPEN_EVENT_VIEW_TYPE = 3;
        private static final int PLACEHOLDER_VIEW_TYPE = 4;

        private List<Object> mList;
        private boolean mShowGroup;

        public EventAdapter(List<Object> events, boolean showGroup) {
            mList = events;
            mShowGroup = showGroup;
        }

        // Determines the list item view to display the current value's data on
        @Override
        public int getItemViewType(int position) {

            if (mList.get(position) instanceof Event) {
                Event event = (Event) mList.get(position);
                if (event.dateIsSet()) {

                    // If the value is an event object with a set date
                    // create a set event list item
                    return SET_EVENT_VIEW_TYPE;
                } else {

                    // If the value is an event object with an open date
                    // create an open date event list item
                    return OPEN_EVENT_VIEW_TYPE;
                }
            } else if (mList.get(position) instanceof String) {
                String i = (String) mList.get(position);

                // If the string in the list matches a predefined value matching a placeholder
                // create a placeholder list item
                if (i.equals(ARG_PLACEHOLDER_VIEW_TYPE)) {
                    return PLACEHOLDER_VIEW_TYPE;
                }
            }

            // Else create a subtitle list item with the string value
            return SUBTITLE_VIEW_TYPE;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            RecyclerView.ViewHolder holder = null;

            switch (viewType) {
                case SUBTITLE_VIEW_TYPE:
                    holder = new SubtitleHolder(layoutInflater, parent);
                    break;
                case SET_EVENT_VIEW_TYPE:
                    holder = new SetEventViewHolder(layoutInflater, parent, R.layout.list_item_set_event);
                    break;
                case OPEN_EVENT_VIEW_TYPE:
                    holder = new OpenEventViewHolder(layoutInflater, parent, R.layout.list_item_open_event);
                    break;
                case PLACEHOLDER_VIEW_TYPE:
                    holder = new PlaceholderViewHolder(layoutInflater, parent);
                    break;
            }

            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
            if (mList.get(position) instanceof Date) {
                SubtitleHolder holder = (SubtitleHolder) viewHolder;
                holder.bind((Date) mList.get(position));

            } else if (mList.get(position) instanceof String) {
                String i = (String) mList.get(position);
                if (i.equals(ARG_PLACEHOLDER_VIEW_TYPE)) {

                } else {
                    SubtitleHolder holder = (SubtitleHolder) viewHolder;
                    holder.bind((String) mList.get(position));
                }
            } else {
                Event event = (Event) mList.get(position);
                if (!event.dateIsSet()) {
                    OpenEventViewHolder holder = (OpenEventViewHolder) viewHolder;
                    holder.bind(event, mShowGroup);
                } else {
                    SetEventViewHolder holder = (SetEventViewHolder) viewHolder;
                    holder.bind(event, mShowGroup);
                }
            }
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        public void setList(List<Object> list) {
            mList = list;
        }
    }


    private abstract class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mTitleTextView;
        private TextView mGroupTextView;

        private Event mEvent;

        public EventViewHolder(LayoutInflater inflater, ViewGroup parent, int layoutId) {
            super(inflater.inflate(layoutId, parent, false));

            mTitleTextView = (TextView) itemView.findViewById(R.id.event_title_edit_text);
            mGroupTextView = (TextView) itemView.findViewById(R.id.event_group);

            itemView.setOnClickListener(this);
        }

        public void bind(Event event, boolean showGroup) {
            mEvent = event;

            mTitleTextView.setText(mEvent.getTitle());
            if (showGroup) {
                mGroupTextView.setText(mEvent.getGroupName());
            } else {
                if (mEvent.getType() != null) {
                    mGroupTextView.setText(mEvent.getType());
                }
            }
        }

        @Override
        public void onClick(View v) {

            Intent intent = SaveEventActivity.newIntent(getActivity(), mEvent.getId(), mEvent.getGroupId());
            startActivity(intent);
        }

        protected Event getEvent() {
            return mEvent;
        }

    }

    private class OpenEventViewHolder extends EventViewHolder {

        private Button mSubmitAvailabilityButton;

        public OpenEventViewHolder(LayoutInflater inflater, ViewGroup parent, int layoutId) {
            super(inflater, parent, layoutId);

            mSubmitAvailabilityButton = itemView.findViewById(R.id.submit_availability_button);
            mSubmitAvailabilityButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = AvailabilitySubmissionActivity.newIntent(
                            getActivity(),
                            getEvent().getId(),
                            getEvent().getOpenDate().getStartDate(),
                            getEvent().getOpenDate().getEndDate(),
                            getEvent().getOpenDate().userVoted(mAuth.getCurrentUser().getUid())
                    );
                    startActivity(intent);
                }
            });
        }

        @Override
        public void bind(Event event, boolean showGroup) {
            super.bind(event, showGroup);

            if (getEvent().getOpenDate().userVoted(mAuth.getCurrentUser().getUid())) {
                mSubmitAvailabilityButton.setText("Update Availability");
            } else {
                mSubmitAvailabilityButton.setText("Submit Availability");
            }
        }
    }

    private class SetEventViewHolder extends EventViewHolder {

        private TextView mTimeTextView;

        public SetEventViewHolder(LayoutInflater inflater, ViewGroup parent, int layoutId) {
            super(inflater, parent, layoutId);

            mTimeTextView = itemView.findViewById(R.id.event_time);
        }

        public void bind(Event event, boolean showGroup) {
            super.bind(event, showGroup);

            SimpleDateFormat timeFormat = new SimpleDateFormat(EventSchema.EventCollection.Formats.TIME_FORMAT);

            if (timeFormat.format(getEvent().getStartDateTime()).equals(timeFormat.format(getEvent().getEndDateTime()))) {
                mTimeTextView.setText(timeFormat.format(getEvent().getStartDateTime()));
            } else {
                mTimeTextView.setText(
                        timeFormat.format(getEvent().getStartDateTime())
                                + " - "
                                + timeFormat.format(getEvent().getEndDateTime()));
            }
        }

    }

    private class SubtitleHolder extends RecyclerView.ViewHolder {

        private TextView mMainTextView;
        private TextView mSubTextView;

        private Date mDate;

        public SubtitleHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_subtitle, parent, false));

            mMainTextView = (TextView) itemView.findViewById(R.id.event_title_edit_text);

            mSubTextView = (TextView) itemView.findViewById(R.id.subtitle_sub);
        }

        public void bind(Date date) {
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            SimpleDateFormat dateFormat = new SimpleDateFormat(EventCollection.Formats.DATE_FORMAT);

            mDate = date;

            if (dateFormat.format(date).equals(dateFormat.format(new Date()))) {
                mMainTextView.setText("Today");
            } else {
                mMainTextView.setText(dayFormat.format(date));
            }

            mSubTextView.setText(dateFormat.format(date));
        }

        public void bind(String subtitle) {
            mMainTextView.setText(subtitle);
            mSubTextView.setText("");
        }

    }


    private class PlaceholderViewHolder extends RecyclerView.ViewHolder {

        private Button mCreateGroupButton;

        public PlaceholderViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_event_list_placeholder, parent, false));

            mCreateGroupButton = itemView.findViewById(R.id.create_group_button);
            mCreateGroupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = SaveGroupActivity.newIntent(getActivity(), null);
                    startActivity(intent);
                }
            });
        }

    }

}
