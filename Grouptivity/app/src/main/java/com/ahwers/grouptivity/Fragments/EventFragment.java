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
import com.ahwers.grouptivity.Models.ViewModels.SaveEventViewModel;
import com.ahwers.grouptivity.Models.ViewModels.SaveEventViewModel.EntityType;
import com.ahwers.grouptivity.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class EventFragment extends LoadableFragment {

    private static final String TAG = "EventFragment";

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
    private UiState mUiState;
    private int mLastAttributeUpdated = -1;

    enum UiState {
        EDIT_EVENT,
        NEW_EVENT,
        VIEW_EVENT
    }

    public static EventFragment newInstance(String eventId, String groupId) {
        Log.d(TAG, "newInstance: ");

        if (eventId == null && groupId == null) {
            throw new IllegalArgumentException(
                    "EventFragment requires either an Event ID in order to view and edit that event, or a Group ID in order to " +
                            "create a new event."
            );
        }

        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_GROUP_ID, groupId);

        EventFragment fragment = new EventFragment();
        fragment.setArguments(args);

        return fragment;
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

            dialog.setTargetFragment(EventFragment.this, REQUEST_SET_DATETIME);
            if (fm != null) {
                dialog.show(fm, DIALOG_DATETIME);
            }
        });

        mOpenDateAndTimeToggle = view.findViewById(R.id.flex_date);
        mOpenDateAndTimeToggle.setOnClickListener(v -> {
            FragmentManager fm = getFragmentManager();
            DateTimePickerFragment dialog = DateTimePickerFragment.newInstance(new Date(), new Date());

            dialog.setTargetFragment(EventFragment.this, REQUEST_OPEN_DATETIME);
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

            dialog.setTargetFragment(EventFragment.this, REQUEST_NEW_ATTRIBUTE);
            if (fm != null) {
                dialog.show(fm, DIALOG_ATTRIBUTE);
            }
        });

        mEditEventButton = view.findViewById(R.id.edit_event_button);
        mEditEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mSaveEventViewModel.isEventBeingEdited()) {
                    startEditingEvent();
                } else {
                    // IMPLEMENT THE MEMBER NAME
                    Toast.makeText(getActivity(), "[GROUP MEMBER NAME] is currently editing this event.", Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        mStopEditingButton = view.findViewById(R.id.stop_editing_buton);
        mStopEditingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopEditingEvent();
            }
        });

        mSaveEventButton = view.findViewById(R.id.save_event_button);
        mSaveEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEvent();
            }
        });

        if (mUiState == UiState.NEW_EVENT) {
            setLoading(false);
            setEditing(true);
        }
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");

        super.onCreate(savedInstanceState);

        if (getArguments() == null) {
            throw new IllegalArgumentException(
                    "EventFragment needs to be created through newInstance() in order to provide it with the necessary arguments."
            );
        }

        mAuth = FirebaseAuth.getInstance();

        mSaveEventViewModel = ViewModelProviders.of(this).get(SaveEventViewModel.class);

        String eventId = (String) getArguments().get(ARG_EVENT_ID);
        String groupId = (String) getArguments().get(ARG_GROUP_ID);

        if (eventId != null) {
            mSaveEventViewModel.init(EntityType.EVENT, eventId);
            mUiState = UiState.VIEW_EVENT;
            initEventObserver();
        } else {
            mSaveEventViewModel.init(EntityType.GROUP, groupId);
            mUiState = UiState.NEW_EVENT;
        }

    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: ");

        super.onStart();

        // Starts the event listener if an existing event is being displayed and is not already being listened to
        if (!mSaveEventViewModel.isListening() && mUiState != UiState.NEW_EVENT) {
            setLoading(true);
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

    private void initEventObserver() {
        mSaveEventViewModel.getEvent().observe(this, mEvent -> {
            updateUi(mEvent);
        });
    }

    private void updateUi(Event event) {
        // If user's ownership status has been revoked or it is the first call.
        if ((!event.isUserOwner(mAuth.getCurrentUser().getUid()) && mUiState == UiState.EDIT_EVENT)
                || mTitleEditText.getText().toString().isEmpty()) {

            // Don't use stopEditingEvent() here as it reverts the UI data to the rollback event.
            // This method updates the UI with the updated event passed in.
            setEditing(false);
        }

        setViewValues(event);

        if (isLoading()) {
            setLoading(false);
        }
    }

    private void startEditingEvent() {
        setEditing(true);

        // Update rollback event in case the user cancels their editing.
        mSaveEventViewModel.setRollbackEvent();

    }

    private void stopEditingEvent() {
        setEditing(false);

        // Revert Ui values to rollback event.
        setViewValues(mSaveEventViewModel.getRollbackEvent());
    }

    private void setEditing(boolean editing) {
        if (mUiState != UiState.NEW_EVENT) {
            mUiState = editing ? UiState.EDIT_EVENT : UiState.VIEW_EVENT;
            mSaveEventViewModel.updateEventEditingState(editing);
        }

        mSetDateAndTimeToggle.setVisibility(editing ? View.VISIBLE : View.GONE);
        mOpenDateAndTimeToggle.setVisibility(editing ? View.VISIBLE : View.GONE);
        mAddAttributeButton.setEnabled(editing);
        mAddAttributeButton.setVisibility(editing ? View.VISIBLE : View.INVISIBLE);
        setEditTextEditable(mTitleEditText, editing);
        setEditTextEditable(mLocationEditText, editing);

        switch (mUiState) {
            case NEW_EVENT:
                mEditEventButton.hide();
                mStopEditingButton.hide();
                mSaveEventButton.show();
                break;
            case EDIT_EVENT:
                mEditEventButton.hide();
                mStopEditingButton.show();
                mSaveEventButton.show();
                break;
            case VIEW_EVENT:
                if (mSaveEventViewModel.getEvent().getValue().isUserOwner(mAuth.getCurrentUser().getUid())) {
                    mEditEventButton.show();
                    mStopEditingButton.hide();
                    mSaveEventButton.hide();
                } else {
                    mEditEventButton.hide();
                    mStopEditingButton.hide();
                    mSaveEventButton.hide();
                }
                break;
        }
    }

    private void setViewValues(Event event) {
        mTitleEditText.setText(event.getTitle());
        mLocationEditText.setText(event.getLocation());

        mDateAndTimeEditText.setText("Implement");

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
        setViewValues(mSaveEventViewModel.getEvent().getValue());
    }

    /**
     * Saves the event to the database, committing any changes the user has made.
     * Displays snackbar messages that inform the user of the status of the save.
     * Provides actions to retry if it failed, and undo if they wish to rollback the changes they made.
     */
    private void saveEvent() {
        Log.d(TAG, "saveEvent: ");

        mSaveEventViewModel.saveEvent().observe(this, mEventSaveState -> {

            // Change the case values to static variables
            switch (mEventSaveState) {
                case 0:
                    Snackbar.make(mSnackbarContext, "Event saved failed", Snackbar.LENGTH_LONG)
                            .setAction("Retry", v -> saveEvent())
                            .show();
                    break;
                case 1:
                    if (mUiState == UiState.NEW_EVENT) {
                        Snackbar.make(mSnackbarContext, "Event saved", Snackbar.LENGTH_LONG)
                                .show();
                        mUiState = UiState.VIEW_EVENT;
                        mSaveEventViewModel.startListening();
                        initEventObserver();
                    } else {
                        Snackbar.make(mSnackbarContext, "Event saved", Snackbar.LENGTH_LONG)
                                .setAction("Undo", v -> rollbackSave())
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

    /**
     * Reverses the changes made to the event to it's previous state.
     */
    private void rollbackSave() {

        mSaveEventViewModel.rollbackSave().observe(this, mRollbackSaveState -> {
            switch (mRollbackSaveState) {
                case 0:
                    Snackbar.make(mSnackbarContext, "Undo failed", Snackbar.LENGTH_LONG)
                            .setAction("Retry", v -> rollbackSave())
                            .show();
                    break;
                case 1:
                    Snackbar.make(mSnackbarContext, "Save undone", Snackbar.LENGTH_SHORT)
                            .show();
                    break;
            }

        });
    }

    private void setEditTextEditable(EditText editText, boolean editable) {
        editText.setFocusable(editable);
        editText.setFocusableInTouchMode(editable);
    }

    /**
     * Helper class that creates and displays a snackbar message with an action button.
     *
     * @param snackMessage      The message for the snackbar to display.
     * @param snackAction       The snackbar action's onClick action.
     * @param snackActionName   The snackbar action's button text.
     */
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

    /**
     * Helper class that creates and displays a snackbar messgage;
     *
     * @param snackMessage  The message for the snackbar to display.
     */
    public void makeASnack(String snackMessage) {
        if (snackMessage == null) {
            throw new IllegalArgumentException("A snack needs a message.");
        }

        Snackbar.make(mSnackbarContext, snackMessage, Snackbar.LENGTH_LONG)
                .show();
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

            if (mUiState == UiState.EDIT_EVENT) {
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
            dialog.setTargetFragment(EventFragment.this, REQUEST_UPDATE_ATTRIBUTE);
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
