package com.ahwers.grouptivity.Fragments;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ahwers.grouptivity.Models.DocumentSchemas.EventSchema;
import com.ahwers.grouptivity.Models.Event;
import com.ahwers.grouptivity.Models.ExtraAttribute;
import com.ahwers.grouptivity.Models.OpenDateTime;
import com.ahwers.grouptivity.Models.ViewModels.SaveEventViewModel;
import com.ahwers.grouptivity.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SaveEventFragment extends LoadableFragment {

    private static final String TAG = "SaveEventFragment";

    private static final String ARG_EVENT_ID = "event_id";
    private static final String ARG_GROUP_ID = "group_id";

    private static final int REQUEST_NEW_ATTRIBUTE = 0;
    private static final int REQUEST_UPDATE_ATTRIBUTE = 1;
    private static final int REQUEST_SET_DATETIME = 2;
    private static final int REQUEST_OPEN_DATETIME = 3;

    private static final String DIALOG_ATTRIBUTE = "attribute_dialog";
    private static final String DIALOG_DATETIME = "datetime_dialog";

    private EditText mTitleEditText;
    private ToggleButton mSetDateAndTimeToggle;
    private ToggleButton mOpenDateAndTimeToggle;
    private EditText mDateAndTimeEditText;
    private EditText mLocationEditText;
    private CoordinatorLayout mSnackbarContext;

    private RecyclerView mAttributeRecyclerView;
    private AttributeAdapter mAttributeAdapter;
    private FloatingActionButton mAddAttributeButton;

    private int mLastAttributeUpdated = -1;
    private boolean mEditing = false;
    private boolean mEditable = false;
    private boolean mStaleData = false;
    private boolean mNewEvent = false;

    private FirebaseAuth mAuth;
    private SaveEventViewModel mSaveEventViewModel;

    public static SaveEventFragment newInstance(String eventId, String groupId) {
        Log.d(TAG, "newInstance: ");

        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_GROUP_ID, groupId);

        SaveEventFragment fragment = new SaveEventFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mAuth = FirebaseAuth.getInstance();

        mSaveEventViewModel = ViewModelProviders.of(this).get(SaveEventViewModel.class);

        String eventId = (String) getArguments().get(ARG_EVENT_ID);
        String groupId = (String) getArguments().get(ARG_GROUP_ID);
        mSaveEventViewModel.init(eventId, groupId);

        if (eventId == null) {
            mNewEvent = true;
        }

        mSaveEventViewModel.getEvent().observe(this, mEvent -> {
            updateUi(mEvent);
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");

        View view = inflater.inflate(R.layout.fragment_save_event, container, false);

        createLoadingViews(view);
        setLoading(true);

        mSnackbarContext = view.findViewById(R.id.snackbar_context);

        mTitleEditText = view.findViewById(R.id.event_title);
        mTitleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSaveEventViewModel.setEventTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mSetDateAndTimeToggle = (ToggleButton) view.findViewById(R.id.set_date);
        mSetDateAndTimeToggle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                DateTimePickerFragment dialog = DateTimePickerFragment.newInstance(
                        mSaveEventViewModel.getEventStartDateTime(),
                        mSaveEventViewModel.getEventEndDateTime()
                );

                dialog.setTargetFragment(SaveEventFragment.this, REQUEST_SET_DATETIME);
                dialog.show(fm, DIALOG_DATETIME);
            }
        });

        mOpenDateAndTimeToggle = (ToggleButton) view.findViewById(R.id.flex_date);
        mOpenDateAndTimeToggle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                DateTimePickerFragment dialog = DateTimePickerFragment.newInstance(new Date(), new Date());

                dialog.setTargetFragment(SaveEventFragment.this, REQUEST_OPEN_DATETIME);
                dialog.show(fm, DIALOG_DATETIME);
            }
        });

        mDateAndTimeEditText = (EditText) view.findViewById(R.id.date_and_time_text_view);
        mDateAndTimeEditText.setFocusable(false);

        mLocationEditText = (EditText) view.findViewById(R.id.event_location);
        mLocationEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSaveEventViewModel.setEventLocation(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mAttributeRecyclerView = (RecyclerView) view.findViewById(R.id.extra_attributes_recycler_view);
        mAttributeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAddAttributeButton = (FloatingActionButton) view.findViewById(R.id.add_attribute_button);
        mAddAttributeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mLastAttributeUpdated = -1;

                FragmentManager fm = getFragmentManager();
                AttributeFragment dialog = AttributeFragment.newInstance(new ExtraAttribute(), true);

                dialog.setTargetFragment(SaveEventFragment.this, REQUEST_NEW_ATTRIBUTE);
                dialog.show(fm, DIALOG_ATTRIBUTE);
            }
        });
        mAddAttributeButton.hide();

        return view;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: ");

        super.onStart();

        if (!mSaveEventViewModel.isListening() && !mNewEvent) {
            mSaveEventViewModel.startListening();
        }
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: ");

        super.onStop();

        if (mSaveEventViewModel.isListening()) {
            mSaveEventViewModel.stopListening();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.d(TAG, "onCreateOptionsMenu: ");

        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_save_event, menu);

        hideMenuItem(menu.findItem(R.id.edit_event));
        hideMenuItem(menu.findItem(R.id.save_event));
        hideMenuItem(menu.findItem(R.id.back_button));
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu: ");

        if (mEditable) {
            if (mEditing) {
                showMenuItem(menu.findItem(R.id.save_event));
                hideMenuItem(menu.findItem(R.id.edit_event));
                showMenuItem(menu.findItem(R.id.back_button));
            } else {
                showMenuItem(menu.findItem(R.id.edit_event));
                hideMenuItem(menu.findItem(R.id.save_event));
                hideMenuItem(menu.findItem(R.id.back_button));
            }
        }

        if (mNewEvent) {
            hideMenuItem(menu.findItem(R.id.back_button));
        }
    }

    private void hideMenuItem(MenuItem menuItem) {
        menuItem.setVisible(false).setEnabled(false);
    }

    private void showMenuItem(MenuItem menuItem) {
        menuItem.setVisible(true).setEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: ");

        switch (item.getItemId()) {
            case R.id.save_event:
                if (isConnected()) {
                    saveEvent();
                    setEditing(false);
                } else {
                    displayConnectionNotice();
                }
                return true;
            case R.id.edit_event:
                if (isConnected()) {
                    setEditing(true);
                } else {
                   displayConnectionNotice();
                }
                return true;
            case R.id.back_button:
                setEditing(false);
//              Used to update the event data if another user updated it while the current one was updating it but cancelled the changes
                if (mStaleData) {
                    updateUi(mSaveEventViewModel.getEvent().getValue());
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void displayConnectionNotice() {
        Toast.makeText(getActivity(), "Connect to the internet to save an event", Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: ");

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        String attributeTitle = AttributeFragment.getAttributeTitle(data);
        String attributeValue = AttributeFragment.getExtraAttributeValue(data);
        boolean deleteAttribute = AttributeFragment.getDeleteAttribute(data);

        switch (requestCode) {
            case REQUEST_NEW_ATTRIBUTE:
                if (attributeTitle != null && attributeValue != null) {
                    mSaveEventViewModel.getEventCustomAttributes().add(new ExtraAttribute(attributeTitle, attributeValue));
                    mAttributeAdapter.setAttributesList(mSaveEventViewModel.getEventCustomAttributes());
                    mAttributeAdapter.notifyDataSetChanged();
                }
                break;
            case REQUEST_UPDATE_ATTRIBUTE:
                if (deleteAttribute) {
                    mSaveEventViewModel.getEventCustomAttributes().remove(mLastAttributeUpdated);
                } else {
                    mSaveEventViewModel.getEventCustomAttributes().remove(mLastAttributeUpdated);
                    mSaveEventViewModel.getEventCustomAttributes().add(mLastAttributeUpdated, new ExtraAttribute(attributeTitle, attributeValue));
                }
                break;
            case REQUEST_SET_DATETIME:
                mSetDateAndTimeToggle.setChecked(true);
                mOpenDateAndTimeToggle.setChecked(false);

                mSaveEventViewModel.getEvent().getValue().setOpenDate(null);

                mSaveEventViewModel.getEvent().getValue().setStartDateTime(DateTimePickerFragment.getStartDate(data));
                mSaveEventViewModel.getEvent().getValue().setEndDateTime(DateTimePickerFragment.getEndDate(data));
                break;
            case REQUEST_OPEN_DATETIME:
                if (mSaveEventViewModel.getEvent().getValue().getOpenDate() == null) {
                    mSaveEventViewModel.getEvent().getValue().setOpenDate(new OpenDateTime());
                }

                mOpenDateAndTimeToggle.setChecked(true);
                mSetDateAndTimeToggle.setChecked(false);

                mSaveEventViewModel.getEvent().getValue().setStartDateTime(null);
                mSaveEventViewModel.getEvent().getValue().setEndDateTime(null);

                mSaveEventViewModel.getEvent().getValue().getOpenDate().setStartDate(DateTimePickerFragment.getStartDate(data));
                mSaveEventViewModel.getEvent().getValue().getOpenDate().setEndDate(DateTimePickerFragment.getEndDate(data));
                break;
        }
        updateUi(mSaveEventViewModel.getEvent().getValue());
    }

    private void updateUi(Event event) {
        Log.d(TAG, "updateUi: source " + event.getUpdateSource());

//      Updates the editable state of the UI based on the event data
        updateEditState(event);

//      Displays a message to the user if another user has updated the event
        if (!mNewEvent && event.getUpdateSource().equals(Event.UPDATE_SOURCE_SERVER)) {
            displayDataUpdatedMessage(event);
        }

//      Only update the data if the current user isn't currently modifying the data
//      mStaleData allows the system to update to the updated data if the current user cancels their update
        if (!mEditing) {
            setUiValues(event);
            mStaleData = false;
        } else {
            mStaleData = true;
        }

//      Update the toolbar state based on new editable states
        getActivity().invalidateOptionsMenu();

        setLoading(false);
    }

    /**
     * Updates the UI's editable state based on the event's data.
     *
     * @param event
     * The event that is being displayed on the UI.
     */
    private void updateEditState(Event event) {
//      If the event is new, it is editing by default and the "Stop editing" button is disabled until it is saved (using mNewEvent)
        if (event.getId() == null) {
            mEditable = true;
            mEditing = true;
            mNewEvent = true;
            setEditing(true);
//      If the current user is an owner of the event they can edit it if they wish
        } else if (event.isUserOwner(mAuth.getCurrentUser().getUid())) {
            mEditable = true;
            setEditing(mEditing);
//      Otherwise the user can only view the event's details
        } else {
            mEditable = false;
            mEditing = false;
            setEditing(false);
        }
    }

    /**
     * Informs the user of real time changes to the event by other members, and updates the rollback event
     * state if the current user wishes to undo any changes they make.
     *
     * @param event
     * The event the user is viewing.
     * Holds information about the source of the update.
     */
    private void displayDataUpdatedMessage(Event event) {
//      (If the event is already displayed AND the user has not already been informed of the data change)
        if (!mTitleEditText.getText().toString().isEmpty()
                && !mStaleData) {
            mSaveEventViewModel.setEventRollback(event);
//          Maybe say which user updated the event
            Snackbar.make(mSnackbarContext, "Event has just been updated", Snackbar.LENGTH_LONG)
                    .show();

//          Highlight the views of the data that was updated
        }
    }

    /**
     * Updates the UI views based on the values of the parsed event.
     *
     * @param event
     * The event who's details to display.
     */
    private void setUiValues(Event event) {
        mTitleEditText.setText(event.getTitle());
        mLocationEditText.setText(event.getLocation());

        if ((event.getStartDateTime() != null && event.getEndDateTime() != null)
                || event.getOpenDate() != null) {
            updateDateDisplay(event);
        }

        if (mAttributeAdapter == null) {
            mAttributeAdapter = new AttributeAdapter(event.getExtraAttributes());
            mAttributeRecyclerView.setAdapter(mAttributeAdapter);
        } else {
            mAttributeAdapter.setAttributesList(event.getExtraAttributes());
            mAttributeAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Saves the event to the database, committing any changes the user has made.
     * Displays snackbar messages that inform the user of the status of the save.
     * Provides actions to retry if it failed, and undo if they wish to rollback the changes they made.
     */
    private void saveEvent() {
        Log.d(TAG, "saveEvent: ");

        if (mSaveEventViewModel.getGroup().getValue() != null) {

            if (mNewEvent) {
                mSaveEventViewModel.getEvent().getValue().setGroupValues(mSaveEventViewModel.getGroup().getValue());
                mSaveEventViewModel.getEvent().getValue().setCreated(new Date());
                mSaveEventViewModel.getEvent().getValue().setCreatorId(mAuth.getCurrentUser().getUid());
                mNewEvent = false;
            }

            mSaveEventViewModel.saveEvent().observe(this, mEventSaveState -> {

                switch (mEventSaveState) {
                    case 0:
                        Snackbar.make(mSnackbarContext, "Event saved failed", Snackbar.LENGTH_LONG)
                                .setAction("Retry", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        saveEvent();
                                    }
                                })
                                .show();
                        break;
                    case 1:
                        Snackbar.make(mSnackbarContext, "Event saved", Snackbar.LENGTH_LONG)
                                .setAction("Undo", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        rollbackSave();
                                    }
                                })
                                .show();
                        break;
                    case -10:
                        Toast.makeText(getActivity(), "Event invalid - Implement something that show's what's invalid", Toast.LENGTH_SHORT)
                                .show();
                        break;
                }

            });
        }

    }

    /**
     * Reverses the changes made to the event to it's previous state.
     */
    private void rollbackSave() {
        mSaveEventViewModel.rollbackSave().observe(this, mRollbackSaveState -> {

            switch (mRollbackSaveState) {
                case 0:
                    Snackbar.make(mSnackbarContext, "Undo failed", Snackbar.LENGTH_LONG)
                            .setAction("Retry", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    rollbackSave();
                                }
                            })
                            .show();
                    break;
                case 1:
                    Snackbar.make(mSnackbarContext, "Save undone", Snackbar.LENGTH_SHORT)
                            .show();
                    break;
            }

        });
    }

    private void updateDateDisplay(Event event) {
        Log.d(TAG, "updateDateDisplay: ");

        SimpleDateFormat dateFormat = new SimpleDateFormat(EventSchema.EventCollection.Formats.DATE_FORMAT);
        SimpleDateFormat timeFormat = new SimpleDateFormat(EventSchema.EventCollection.Formats.TIME_FORMAT);

        Date startDate = null;
        Date startTime = null;
        Date endDate = null;
        Date endTime = null;

        if (event.getStartDateTime() != null && event.getEndDateTime() != null) {
            try {
                startDate = dateFormat.parse(dateFormat.format(event.getStartDateTime()));
                startTime = timeFormat.parse(timeFormat.format(event.getStartDateTime()));
                endDate = dateFormat.parse(dateFormat.format(event.getEndDateTime()));
                endTime = timeFormat.parse(timeFormat.format(event.getEndDateTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            mSetDateAndTimeToggle.setChecked(true);

            if (endDate.after(startDate)) {
                mDateAndTimeEditText.setText(timeFormat.format(startTime) + " " + dateFormat.format(startDate) + " - "
                        + timeFormat.format(endTime) + " " + dateFormat.format(endDate));
            } else {
                mDateAndTimeEditText.setText(dateFormat.format(startDate) + " " + timeFormat.format(startTime) + " - "
                        + timeFormat.format(endTime));
            }
        } else {
            try {
                startDate = dateFormat.parse(dateFormat.format(event.getOpenDate().getStartDate()));
                startTime = timeFormat.parse(timeFormat.format(event.getOpenDate().getStartDate()));
                endDate = dateFormat.parse(dateFormat.format(event.getOpenDate().getEndDate()));
                endTime = timeFormat.parse(timeFormat.format(event.getOpenDate().getEndDate()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            mOpenDateAndTimeToggle.setChecked(true);

            mDateAndTimeEditText.setText(dateFormat.format(startDate) + "  - " + dateFormat.format(endDate) + "\n"
                    + timeFormat.format(startTime) + " - " + timeFormat.format(endTime));
        }

    }

    private void setEditing(boolean editing) {
        Log.d(TAG, "setEditing: ");

        mEditing = editing;

        if (!mEditing) {
            mAddAttributeButton.hide();
        } else {
            mAddAttributeButton.show();
        }
        mSetDateAndTimeToggle.setVisibility(mEditing ? View.VISIBLE : View.GONE);
        mOpenDateAndTimeToggle.setVisibility(mEditing ? View.VISIBLE : View.GONE);
        mAddAttributeButton.setEnabled(mEditing);
        setEditTextEditable(mTitleEditText, mEditing);
        setEditTextEditable(mLocationEditText, mEditing);

        getActivity().invalidateOptionsMenu();
    }

    private void setEditTextEditable(EditText editText, boolean editable) {
        editText.setFocusable(editable);
        editText.setFocusableInTouchMode(editable);
    }

    private boolean isConnected() {
        Log.d(TAG, "isConnected: ");

        ConnectivityManager cm = (ConnectivityManager)getActivity().getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    private class AttributeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<ExtraAttribute> mAttributes;

        public AttributeAdapter(List<ExtraAttribute> attributes) {
            Log.d(TAG, "AttributeAdapter: ");
            mAttributes = attributes;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Log.d(TAG, "onCreateViewHolder: ");

            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            RecyclerView.ViewHolder holder;

            if (mEditing) {
                holder = new EditAttributeHolder(layoutInflater, parent, R.layout.list_item_edit_attribute);
            } else {
                holder = new AttributeHolder(layoutInflater, parent, R.layout.list_item_attribute);
            }

            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Log.d(TAG, "onBindViewHolder: ");
            ExtraAttribute attribute = mAttributes.get(position);

            AttributeHolder attrHolder = (AttributeHolder) holder;
            attrHolder.bind(attribute);
        }

        @Override
        public int getItemCount() {
            return mAttributes.size();
        }

        public void setAttributesList(List<ExtraAttribute> attributes) {
            mAttributes = attributes;
        }
    }

    private class EditAttributeHolder extends AttributeHolder implements View.OnClickListener {

        private ImageButton mEditAttribute;

        public EditAttributeHolder(LayoutInflater inflater, ViewGroup parent, int layoutId) {
            super(inflater, parent, layoutId);

            mEditAttribute = (ImageButton) itemView.findViewById(R.id.edit_attribute);
            mEditAttribute.setOnClickListener(this);

            mAttributeValue.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mLastAttributeUpdated = getAdapterPosition();

            FragmentManager fm = getFragmentManager();
            AttributeFragment dialog = AttributeFragment.newInstance(getAttribute(), false);
            dialog.setTargetFragment(SaveEventFragment.this, REQUEST_UPDATE_ATTRIBUTE);
            dialog.show(fm, DIALOG_ATTRIBUTE);
        }
    }

    private class AttributeHolder extends RecyclerView.ViewHolder {

        protected TextView mAttributeTitle;
        protected EditText mAttributeValue;

        private ExtraAttribute mAttribute;

        public AttributeHolder(LayoutInflater inflater, ViewGroup parent, int layoutId) {
            super(inflater.inflate(layoutId, parent, false));

            mAttributeTitle = (TextView) itemView.findViewById(R.id.attribute_title);
            mAttributeValue = (EditText) itemView.findViewById(R.id.attribute_value);
            mAttributeValue.setFocusable(false);
        }

        public void bind(ExtraAttribute attribute) {
            mAttribute = attribute;
            mAttributeTitle.setText(mAttribute.getTitle());
            mAttributeValue.setText(mAttribute.getValue());
        }

        protected ExtraAttribute getAttribute() {
            return mAttribute;
        }

    }

}
