package com.ahwers.grouptivity.Fragments;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.ahwers.grouptivity.Models.DataModels.Event;
import com.ahwers.grouptivity.Models.DataModels.ExtraAttribute;
import com.ahwers.grouptivity.Models.DataModels.OpenDateTime;
import com.ahwers.grouptivity.Models.Presenters.EventPresenter;
import com.ahwers.grouptivity.Models.ViewModels.SaveEventViewModel;
import com.ahwers.grouptivity.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class SaveEventFragment extends LoadableFragment implements View.OnClickListener {

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
    private Button mAddAttributeButton;
    private FloatingActionButton mEditEventButton;
    private FloatingActionButton mStopEditingButton;
    private FloatingActionButton mSaveEventButton;

    private FirebaseAuth mAuth;
    private SaveEventViewModel mSaveEventViewModel;
    private int mLastAttributeUpdated = -1;
    private boolean mEditing;
    private boolean mEditable = false;
    private boolean mNewEvent = false;

    // Inject as singleton
    private EventPresenter mEventPresenter;

    public static SaveEventFragment newInstance(String eventId, String groupId) {
        Log.d(TAG, "newInstance: ");

        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_GROUP_ID, groupId);

        SaveEventFragment fragment = new SaveEventFragment();
        fragment.setArguments(args);

        // Inject this
        fragment.setEventPresenter(new EventPresenter());

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");

        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        mSaveEventViewModel = ViewModelProviders.of(this).get(SaveEventViewModel.class);

        String eventId = null;
        String groupId = null;
        if (getArguments() != null) {
            eventId = (String) getArguments().get(ARG_EVENT_ID);
            groupId = (String) getArguments().get(ARG_GROUP_ID);
        }

        mSaveEventViewModel.init(eventId, groupId);

        if (eventId == null) {
            mNewEvent = true;
            mEditable = true;
        } else {
            initEventObserver();
        }
    }

    private void initEventObserver() {
        mSaveEventViewModel.getEvent().observe(this, mEvent -> {
            try {
                updateUi(mEvent);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                // Create snackbar that alerts user of failed fetch and add try again button
                // Try again button sets the screen to loading
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");

        View view = inflater.inflate(R.layout.fragment_save_event, container, false);

        createLoadingViews(view);
        setLoading(true);

        mSnackbarContext = view.findViewById(R.id.snackbar_context);

        mTitleEditText = view.findViewById(R.id.event_title_edit_text);
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

        mSetDateAndTimeToggle = view.findViewById(R.id.set_date);
        mSetDateAndTimeToggle.setOnClickListener(v -> {
            FragmentManager fm = getFragmentManager();
            DateTimePickerFragment dialog = DateTimePickerFragment.newInstance(
                    mSaveEventViewModel.getEventStartDateTime(),
                    mSaveEventViewModel.getEventEndDateTime()
            );

            dialog.setTargetFragment(SaveEventFragment.this, REQUEST_SET_DATETIME);
            if (fm != null) {
                dialog.show(fm, DIALOG_DATETIME);
            }
        });

        mOpenDateAndTimeToggle = view.findViewById(R.id.flex_date);
        mOpenDateAndTimeToggle.setOnClickListener(v -> {
            FragmentManager fm = getFragmentManager();
            DateTimePickerFragment dialog = DateTimePickerFragment.newInstance(new Date(), new Date());

            dialog.setTargetFragment(SaveEventFragment.this, REQUEST_OPEN_DATETIME);
            if (fm != null) {
                dialog.show(fm, DIALOG_DATETIME);
            }
        });

        mDateAndTimeEditText = view.findViewById(R.id.date_edit_text);
        mDateAndTimeEditText.setFocusable(false);

        mLocationEditText = view.findViewById(R.id.event_location);
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

        mAttributeRecyclerView = view.findViewById(R.id.extra_attributes_recycler_view);
        mAttributeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAddAttributeButton = view.findViewById(R.id.add_attribute_button);
        mAddAttributeButton.setOnClickListener(v -> {
            mLastAttributeUpdated = -1;

            FragmentManager fm = getFragmentManager();
            AttributeFragment dialog = AttributeFragment.newInstance(new ExtraAttribute(), true);

            dialog.setTargetFragment(SaveEventFragment.this, REQUEST_NEW_ATTRIBUTE);
            if (fm != null) {
                dialog.show(fm, DIALOG_ATTRIBUTE);
            }
        });
        mAddAttributeButton.setVisibility(View.GONE);
        mAddAttributeButton.setEnabled(false);

        mEditEventButton = view.findViewById(R.id.edit_event_button);
        mEditEventButton.setOnClickListener(this);

        mStopEditingButton = view.findViewById(R.id.stop_editing_buton);
        mStopEditingButton.setOnClickListener(this);

        mSaveEventButton = view.findViewById(R.id.save_event_button);
        mSaveEventButton.setOnClickListener(this);

        setEditing(mNewEvent);
        setLoading(!mNewEvent);

        return view;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: ");

        super.onStart();

        // Starts the event listener if an existing event is being displayed and is not already being listened to
        if (!mSaveEventViewModel.isListening() && !mNewEvent) {
            mSaveEventViewModel.startListening();
        }
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: ");

        super.onStop();

        // Stops event listener if it is listening
        if (mSaveEventViewModel.isListening()) {
            mSaveEventViewModel.stopListening();
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.edit_event_button:
                if (!mSaveEventViewModel.isEventBeingEdited()) {
                    setEditing(true);
                } else {
                    // Snackbar telling the user to wait until their group mate has finished editing the event
                }
                break;
            case R.id.stop_editing_buton:
                setEditing(false);
                break;
            case R.id.save_event_button:
                saveEvent();
                break;
        }
    }

    private void makeASnack(String snackMessage, View.OnClickListener snackAction, String snackActionName) {
        if (snackMessage == null) {
            throw new IllegalArgumentException("A snack needs a message.");
        } else if (snackAction != null && snackActionName == null) {
            throw new IllegalArgumentException("A snack action requires a name.");
        } else if (snackAction == null && snackActionName != null) {
            throw new IllegalArgumentException("A snack action name requires an action.");
        }

        Snackbar snack = Snackbar.make(mSnackbarContext, snackMessage, Snackbar.LENGTH_LONG);
        if (snackAction != null) {
            snack.setAction(snackActionName, snackAction);
        }
        snack.show();
    }

    public void makeASnack(String snackMessage) {
        if (snackMessage == null) {
            throw new IllegalArgumentException("A snack needs a message.");
        }

        Snackbar.make(mSnackbarContext, snackMessage, Snackbar.LENGTH_LONG)
                .show();
    }

    /**
     * Sets the event's editing state and updates the UI accordingly.
     *
     * @param editing The new editing state of the UI.
     */
    private void setEditing(boolean editing) {
        Log.d(TAG, "setEditing: ");

        mEditing = editing;

        // Don't update the editing state if the user has just loaded the event
        if (!isLoading()) {
            mSaveEventViewModel.updateEventEditingState(mEditing);
        }

        if (mEditable) {
            if (mNewEvent) {
                mEditEventButton.hide();
                mStopEditingButton.hide();
                mSaveEventButton.show();
            } else if (mEditing) {
                mEditEventButton.hide();
                mStopEditingButton.show();
                mSaveEventButton.show();
            } else {
                mEditEventButton.show();
                mStopEditingButton.hide();
                mSaveEventButton.hide();
            }
        } else {
            mEditEventButton.hide();
            mStopEditingButton.hide();
            mSaveEventButton.hide();
        }

        mSetDateAndTimeToggle.setVisibility(mEditing ? View.VISIBLE : View.GONE);
        mOpenDateAndTimeToggle.setVisibility(mEditing ? View.VISIBLE : View.GONE);
        mAddAttributeButton.setEnabled(mEditing);
        mAddAttributeButton.setVisibility(mEditing ? View.VISIBLE : View.INVISIBLE);
        setEditTextEditable(mTitleEditText, mEditing);
        setEditTextEditable(mLocationEditText, mEditing);
    }

    /**
     * Updates the fragment's views and editable state based off of the updated event data.
     *
     * @param event The event data to update the UI with.
     */
    private void updateUi(Event event) throws IllegalArgumentException {
        if (event == null) {
            throw new IllegalArgumentException("Event : event must not be null.");
        }

        Log.d(TAG, "updateUi: source " + event.getUpdateSource());

        // If the trigger is simply the user editing the event, no need to update the whole UI unless this is the first call
        if (mEditing == mSaveEventViewModel.getEvent().getValue().isEditing()
                && event.getUpdateSource().equals(Event.UPDATE_SOURCE_LOCAL)
                && !isLoading()) {
            return;
        }

        if (event.isEditing() && !mEditing) {
            // Display a message saying that a member is editing
            // Maybe say who it is
        }

        // Updates the editable state of the UI based on the event data
        updateEditState(event);

        // Displays a message to the user if another user has updated the event
        if (!mNewEvent
                && event.getUpdateSource().equals(Event.UPDATE_SOURCE_SERVER)
                && !isLoading()) {

            // Maybe say which user updated the event
            makeASnack("Event has just been updated");

            // Highlight the views of the data that was updated
        }

        setUiValues(event);

        setLoading(false);
    }

    /**
     * Updates the UI's editable state based on the event's data.
     *
     * @param event
     * The event that is being displayed on the UI.
     */
    private void updateEditState(Event event) {
        // If the user is an owner
        if (event.isUserOwner(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())) {
            mEditable = true;
            setEditing(mEditing);
        // Otherwise the user can only view the event's details
        } else {
            mEditable = false;
            mEditing = false;
            setEditing(false);
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

        mDateAndTimeEditText.setText(mEventPresenter.formatDate(event));

        if (!event.hasSetDate()) {
            Toast.makeText(getActivity(), "Implement submit availability button and inform user that it's date has not been decided yet.", Toast.LENGTH_LONG)
                    .show();
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

        // Should not be here
        // Move to the ViewModel
        if (mSaveEventViewModel.getGroup().getValue() != null) {

            // Should not be here
            // Move to the ViewModel where it checks if these values are null and subsequently adds them if they are
            if (mNewEvent) {
                Objects.requireNonNull(mSaveEventViewModel.getEvent().getValue()).setGroupValues(mSaveEventViewModel.getGroup().getValue());
                mSaveEventViewModel.getEvent().getValue().setCreated(new Date());
                mSaveEventViewModel.getEvent().getValue().setCreatorId(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
            }

            mSaveEventViewModel.saveEvent().observe(this, mEventSaveState -> {

                // Change the case values to static variables
                switch (mEventSaveState) {
                    case 0:
                        Snackbar.make(mSnackbarContext, "Event saved failed", Snackbar.LENGTH_LONG)
                                .setAction("Retry", v -> saveEvent())
                                .show();
                        break;
                    case 1:
                        if (mNewEvent) {
                            Snackbar.make(mSnackbarContext, "Event saved", Snackbar.LENGTH_LONG)
                                    .show();
                            mNewEvent = false;
                            mSaveEventViewModel.startListening();
                            initEventObserver();
                        } else {
                            Snackbar.make(mSnackbarContext, "Event saved", Snackbar.LENGTH_LONG)
//                                    .setAction("Undo", v -> rollbackSave())
                                    .show();
                        }
                        setEditing(false);
                        break;
                    case -10:
                        Toast.makeText(getActivity(), "Event invalid - Implement something that show's what's invalid", Toast.LENGTH_SHORT)
                                .show();
                        break;
                }

            });
        }

    }

//    /**
//     * Reverses the changes made to the event to it's previous state.
//     */
//    private void rollbackSave() {
//        mSaveEventViewModel.rollbackSave().observe(this, mRollbackSaveState -> {
//
//            switch (mRollbackSaveState) {
//                case 0:
//                    Snackbar.make(mSnackbarContext, "Undo failed", Snackbar.LENGTH_LONG)
//                            .setAction("Retry", v -> rollbackSave())
//                            .show();
//                    break;
//                case 1:
//                    Snackbar.make(mSnackbarContext, "Save undone", Snackbar.LENGTH_SHORT)
//                            .show();
//                    break;
//            }
//
//        });
//    }

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

                Objects.requireNonNull(mSaveEventViewModel.getEvent().getValue()).setOpenDate(null);

                mSaveEventViewModel.getEvent().getValue().setStartDateTime(DateTimePickerFragment.getStartDate(data));
                mSaveEventViewModel.getEvent().getValue().setEndDateTime(DateTimePickerFragment.getEndDate(data));
                break;
            case REQUEST_OPEN_DATETIME:
                if (Objects.requireNonNull(mSaveEventViewModel.getEvent().getValue()).getOpenDate() == null) {
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
        setUiValues(mSaveEventViewModel.getEvent().getValue());
    }

//    /**
//     * Informs the user of real time changes to the event by other members, and updates the rollback event
//     * state if the current user wishes to undo any changes they make.
//     *
//     * @param event
//     * The event the user is viewing.
//     * Holds information about the source of the update.
//     */
//    private void displayDataUpdatedMessage(Event event) {
//        // (If the event is already displayed AND the user has not already been informed of the data change)
//        if (!mTitleEditText.getText().toString().isEmpty()
//                && !mStaleData) {
//
//
//        }
//    }

    private void setEditTextEditable(EditText editText, boolean editable) {
        editText.setFocusable(editable);
        editText.setFocusableInTouchMode(editable);
    }

    private void setEventPresenter(EventPresenter eventPresenter) {
        this.mEventPresenter = eventPresenter;
    }


    private class AttributeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<ExtraAttribute> mAttributes;

        AttributeAdapter(List<ExtraAttribute> attributes) {
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

        void setAttributesList(List<ExtraAttribute> attributes) {
            mAttributes = attributes;
        }
    }

    private class EditAttributeHolder extends AttributeHolder implements View.OnClickListener {

        private ImageButton mEditAttribute;

        EditAttributeHolder(LayoutInflater inflater, ViewGroup parent, int layoutId) {
            super(inflater, parent, layoutId);

            mEditAttribute = itemView.findViewById(R.id.edit_attribute);
            mEditAttribute.setOnClickListener(this);

            mAttributeValue.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mLastAttributeUpdated = getAdapterPosition();

            FragmentManager fm = getFragmentManager();
            AttributeFragment dialog = AttributeFragment.newInstance(getAttribute(), false);
            dialog.setTargetFragment(SaveEventFragment.this, REQUEST_UPDATE_ATTRIBUTE);
            if (fm != null) {
                dialog.show(fm, DIALOG_ATTRIBUTE);
            }
        }
    }

    private class AttributeHolder extends RecyclerView.ViewHolder {

        TextView mAttributeTitle;
        EditText mAttributeValue;

        private ExtraAttribute mAttribute;

        AttributeHolder(LayoutInflater inflater, ViewGroup parent, int layoutId) {
            super(inflater.inflate(layoutId, parent, false));

            mAttributeTitle = itemView.findViewById(R.id.attribute_title);
            mAttributeValue = itemView.findViewById(R.id.attribute_value);
            mAttributeValue.setFocusable(false);
        }

        void bind(ExtraAttribute attribute) {
            mAttribute = attribute;
            mAttributeTitle.setText(mAttribute.getTitle());
            mAttributeValue.setText(mAttribute.getValue());
        }

        ExtraAttribute getAttribute() {
            return mAttribute;
        }

    }

}
