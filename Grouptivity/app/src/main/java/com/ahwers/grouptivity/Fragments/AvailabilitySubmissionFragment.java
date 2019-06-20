package com.ahwers.grouptivity.Fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ahwers.grouptivity.Models.DocumentSchemas.AvailabilitySchema.AvailabilityDocument;
import com.ahwers.grouptivity.Models.DocumentSchemas.EventSchema;
import com.ahwers.grouptivity.Models.OpenDateAvailability;
import com.ahwers.grouptivity.Models.Event;
import com.ahwers.grouptivity.Models.ViewModels.AvailabilitySubmissionViewModel;
import com.ahwers.grouptivity.Models.ViewModels.EventListViewModel;
import com.ahwers.grouptivity.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AvailabilitySubmissionFragment extends Fragment {

    private static final String TAG = "AvailabilitySubmissionF";

    private static final String ARG_START_DATE = "start_date";
    private static final String ARG_END_DATE = "end_date";
    private static final String ARG_EVENT_ID = "event_id";

    private RecyclerView mDatesRecyclerView;
    private DateAdapter mDatesAdapter;
    private Button mPositiveButton;
    private Button mNegativeButton;
    private ConstraintLayout mScreen;
    private ProgressBar mProgressBar;

    private FirebaseAuth mAuth;
    private AvailabilitySubmissionViewModel mViewModel;

    public static AvailabilitySubmissionFragment newInstance(String eventId, Date startDate, Date endDate, boolean memberSubmitted) {
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putSerializable(ARG_START_DATE, startDate);
        args.putSerializable(ARG_END_DATE, endDate);

        AvailabilitySubmissionFragment fragment = new AvailabilitySubmissionFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String eventId = (String) getArguments().get(ARG_EVENT_ID);

        mViewModel = ViewModelProviders.of(this).get(AvailabilitySubmissionViewModel.class);
        mViewModel.init(mAuth.getCurrentUser().getUid(), eventId);

        mViewModel.getAvailability().observe(this, mAvailability -> {
            updateUi(mAvailability);
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_availability_submission, container, false);

        mScreen = v.findViewById(R.id.screen);
        mScreen.setVisibility(View.GONE);

        mProgressBar = v.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);

        mDatesRecyclerView = v.findViewById(R.id.dates_recycler_view);
        mDatesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mPositiveButton = v.findViewById(R.id.positve_button);
        mPositiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAvailability();
            }
        });

        mNegativeButton = v.findViewById(R.id.negative_button);
        mNegativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        return v;
    }

    public void updateUi(List<OpenDateAvailability> availability) {
        if (availability.size() == 0) {
            Date startDate = (Date) getArguments().get(ARG_START_DATE);
            Date endDate = (Date) getArguments().get(ARG_END_DATE);

            SimpleDateFormat dateFormat = new SimpleDateFormat(EventSchema.EventCollection.Formats.DATE_FORMAT);
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(startDate);

            availability.add(new OpenDateAvailability(calendar.getTime()));
            do {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                availability.add(new OpenDateAvailability(calendar.getTime()));
            } while (!dateFormat.format(calendar.getTime()).equals(dateFormat.format(endDate)));
        }

        mDatesAdapter = new DateAdapter(availability);
        mDatesRecyclerView.setAdapter(mDatesAdapter);

        showScreen();
    }

    private void showScreen() {
        mProgressBar.setVisibility(View.GONE);
        mScreen.setVisibility(View.VISIBLE);
    }

    private void saveAvailability() {

        mViewModel.saveAvailability().observe(this, mAvailabilitySaveState -> {
            String saveMessage = null;

            switch (mAvailabilitySaveState) {
                case 0:
                    saveMessage = "Availability submission failed";
                    break;
                case 1:
                    saveMessage = "Availability submitted";
                    getActivity().finish();
                    break;
            }

            Toast.makeText(getActivity(), saveMessage, Toast.LENGTH_SHORT)
                    .show();

        });

    }


    private class DateAdapter extends RecyclerView.Adapter<DateHolder> {

        private List<OpenDateAvailability> mAvailabilities;

        public DateAdapter(List<OpenDateAvailability> dates) {
            mAvailabilities = dates;
        }

        @NonNull
        @Override
        public DateHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            return new DateHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull DateHolder holder, int position) {
            holder.bind(mAvailabilities.get(position));
        }

        @Override
        public int getItemCount() {
            return mAvailabilities.size();
        }
    }


    private class DateHolder extends RecyclerView.ViewHolder {

        private CheckBox mDateCheckBox;

        private OpenDateAvailability mAvailability;

        public DateHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_availability_picker_date, parent, false));

            mDateCheckBox = itemView.findViewById(R.id.date_check_box);
            mDateCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mAvailability.setAvailable(isChecked);
                }
            });
        }

        public void bind(OpenDateAvailability dateAvailability) {
            SimpleDateFormat format = new SimpleDateFormat(EventSchema.EventCollection.Formats.DATE_FORMAT);

            mAvailability = dateAvailability;
            mDateCheckBox.setChecked(mAvailability.isAvailable());
            mDateCheckBox.setText(format.format(mAvailability.getDate()));
        }

    }

}
