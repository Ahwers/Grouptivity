package com.ahwers.grouptivity.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.ahwers.grouptivity.Models.DocumentSchemas.EventSchema;
import com.ahwers.grouptivity.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateTimePickerFragment extends DialogFragment {

    private static final String TAG = "DateTimePickerFragment";

    private static final String ARG_START_DATE = "start_date";
    private static final String ARG_END_DATE = "end_date";

    public static final String EXTRA_START_DATE_TIME = "com.ahwers.grouptivity.start_event_date_time";
    public static final String EXTRA_END_DATE_TIME = "com.ahwers.grouptivity.end_event_date_time";

    private DatePicker mStartDatePicker;
    private TimePicker mStartTimePicker;
    private DatePicker mEndDatePicker;
    private TimePicker mEndTimePicker;
    private Button mNextButton;
    private Button mBackButton;
    private TextView mTitleText;

    private Date mStartDate;
    private Date mEndDate;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat(EventSchema.EventCollection.Formats.DATE_FORMAT);
    private SimpleDateFormat mTimeFormat = new SimpleDateFormat(EventSchema.EventCollection.Formats.TIME_FORMAT);

    public static DateTimePickerFragment newInstance(Date startDate, Date endDate) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_START_DATE, startDate);
        args.putSerializable(ARG_END_DATE, endDate);

        DateTimePickerFragment fragment = new DateTimePickerFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public static Date getStartDate(Intent data) {
        return (Date) data.getSerializableExtra(EXTRA_START_DATE_TIME);
    }

    public static Date getEndDate(Intent data) {
        return (Date) data.getSerializableExtra(EXTRA_END_DATE_TIME);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_date_time_picker, container, false);

        Date argStartDate = (Date) getArguments().getSerializable(ARG_START_DATE);
        mStartDate = argStartDate != null ? argStartDate : new Date();

        Date argEndDate = (Date) getArguments().getSerializable(ARG_END_DATE);
        mEndDate = argEndDate != null ? argEndDate : new Date();

        Calendar startCalendar = new GregorianCalendar();
        startCalendar.setTime(mStartDate);
        int startYear = startCalendar.get(Calendar.YEAR);
        int startMonth = startCalendar.get(Calendar.MONTH);
        int startDay = startCalendar.get(Calendar.DAY_OF_MONTH);
        int startHour = startCalendar.get(Calendar.HOUR);
        int startMinute = startCalendar.get(Calendar.MINUTE);

        Calendar endCalendar = new GregorianCalendar();
        startCalendar.setTime(mEndDate);
        int endYear = startCalendar.get(Calendar.YEAR);
        int endMonth = startCalendar.get(Calendar.MONTH);
        int endDay = startCalendar.get(Calendar.DAY_OF_MONTH);
        int endHour = startCalendar.get(Calendar.HOUR);
        int endMinute = startCalendar.get(Calendar.MINUTE);

        mStartDatePicker = (DatePicker) v.findViewById(R.id.start_date_picker);
        mStartDatePicker.init(startYear, startMonth, startDay, null);

        mEndDatePicker = (DatePicker) v.findViewById(R.id.end_date_picker);
        mEndDatePicker.setVisibility(View.GONE);
        mEndDatePicker.init(endYear, endMonth, endDay, null);

        mStartTimePicker = (TimePicker) v.findViewById(R.id.start_time_picker);
        mStartTimePicker.setVisibility(View.GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mStartTimePicker.setHour(startHour);
            mStartTimePicker.setMinute(startMinute);
        } else {
            mStartTimePicker.setCurrentHour(startHour);
            mStartTimePicker.setCurrentMinute(startMinute);
        }

        mEndTimePicker = (TimePicker) v.findViewById(R.id.end_time_picker);
        mEndTimePicker.setVisibility(View.GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mEndTimePicker.setHour(endHour);
            mEndTimePicker.setMinute(endMinute);
        } else {
            mEndTimePicker.setCurrentHour(endHour);
            mEndTimePicker.setCurrentMinute(endMinute);
        }

        mNextButton = (Button) v.findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Could add all pickers to a list and make nextPage() and previousPage() methods that hide and show the appropriate pickers
                if (mStartDatePicker.getVisibility() == View.VISIBLE) {
                    mTitleText.setText("End Date");
                    mEndDatePicker.setVisibility(View.VISIBLE);
                    mStartDatePicker.setVisibility(View.GONE);
                } else if (mEndDatePicker.getVisibility() == View.VISIBLE) {
                    mTitleText.setText("Start Time");
                    mEndDatePicker.setVisibility(View.GONE);
                    mStartTimePicker.setVisibility(View.VISIBLE);
                } else if (mStartTimePicker.getVisibility() == View.VISIBLE) {
                    mTitleText.setText("End Time");
                    mStartTimePicker.setVisibility(View.GONE);
                    mEndTimePicker.setVisibility(View.VISIBLE);
                } else {
                    Date startDate = getStartDateTime();
                    Date endDate = getEndDateTime();
                    sendResult(Activity.RESULT_OK, startDate, endDate);
                }
            }
        });

        mBackButton = (Button) v.findViewById(R.id.back_button);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStartDatePicker.getVisibility() == View.VISIBLE) {
                    sendResult(Activity.RESULT_CANCELED, null, null);
                } else if (mEndTimePicker.getVisibility() == View.VISIBLE) {
                    mTitleText.setText("Start Date");
                    mEndDatePicker.setVisibility(View.GONE);
                    mStartDatePicker.setVisibility(View.VISIBLE);
                } else if (mStartTimePicker.getVisibility() == View.VISIBLE) {
                    mTitleText.setText("End Date");
                    mStartTimePicker.setVisibility(View.GONE);
                    mEndDatePicker.setVisibility(View.VISIBLE);
                } else {
                    mTitleText.setText("Start Time");
                    mEndDatePicker.setVisibility(View.GONE);
                    mStartTimePicker.setVisibility(View.VISIBLE);
                }
            }
        });

        mTitleText = (TextView) v.findViewById(R.id.dialog_title);
        mTitleText.setText("Start Date");

        return v;
    }

    private Date getStartDateTime() {
        int startYear = mStartDatePicker.getYear();
        int startMonth = mStartDatePicker.getMonth();
        int startDay = mStartDatePicker.getDayOfMonth();
        int startHour;
        int startMinute;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            startHour = mStartTimePicker.getHour();
            startMinute = mStartTimePicker.getMinute();
        } else {
            startHour = mStartTimePicker.getCurrentHour();
            startMinute = mStartTimePicker.getCurrentMinute();
        }

        return new GregorianCalendar(startYear, startMonth, startDay, startHour, startMinute).getTime();
    }

    private Date getEndDateTime() {
        int endYear = mEndDatePicker.getYear();
        int endMonth = mEndDatePicker.getMonth();
        int endDay = mEndDatePicker.getDayOfMonth();
        int endHour;
        int endMinute;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            endHour = mEndTimePicker.getHour();
            endMinute = mEndTimePicker.getMinute();
        } else {
            endHour = mEndTimePicker.getCurrentHour();
            endMinute = mEndTimePicker.getCurrentMinute();
        }

        return new GregorianCalendar(endYear, endMonth, endDay, endHour, endMinute).getTime();
    }

    private void sendResult(int resultCode, Date startDate, Date endDate) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_START_DATE_TIME, startDate);
        intent.putExtra(EXTRA_END_DATE_TIME, endDate);

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
        dismiss();
    }

}
