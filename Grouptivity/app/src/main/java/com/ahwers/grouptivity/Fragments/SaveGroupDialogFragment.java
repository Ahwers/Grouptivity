package com.ahwers.grouptivity.Fragments;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.ahwers.grouptivity.Models.DataModels.Group;
import com.ahwers.grouptivity.Models.ViewModels.SaveGroupViewModel;
import com.ahwers.grouptivity.R;

public class SaveGroupDialogFragment extends DialogFragment {

    private static final String TAG = "SaveGroupDialogFragment";

    private static final String ARG_GROUP_NAME = "group_name";
    private static final String ARG_GROUP_TYPE = "group_type";
    private static final String ARG_GROUP_INFO = "group_info";

    private static final String EXTRA_GROUP_NAME = "com.ahwers.grouptivity.group_name";
    private static final String EXTRA_GROUP_TYPE = "com.ahwers.grouptivity.group_type";
    private static final String EXTRA_GROUP_INFO = "com.ahwers.grouptivity.group_info";

    private EditText mNameEditText;
    private EditText mTypeEditText;
    private EditText mInfoEditText;
    private Button mSaveButton;
    private Button mCancelButton;

    private SaveGroupViewModel mSaveGroupViewModel;

    public static SaveGroupDialogFragment newInstance(String groupName, String groupType, String groupInfo) {
        Bundle args = new Bundle();
        args.putString(ARG_GROUP_NAME, groupName);
        args.putString(ARG_GROUP_TYPE, groupType);
        args.putString(ARG_GROUP_INFO, groupInfo);

        SaveGroupDialogFragment fragment = new SaveGroupDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public static String getGroupName(Intent data) {
        return data.getStringExtra(EXTRA_GROUP_NAME);
    }

    public static String getGroupType(Intent data) {
        return data.getStringExtra(EXTRA_GROUP_TYPE);
    }

    public static String getGroupInfo(Intent data) {
        return data.getStringExtra(EXTRA_GROUP_INFO);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_save_group_dialog, container, false);

        mNameEditText = view.findViewById(R.id.group_name);
        mNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSaveGroupViewModel.getGroup().getValue().setName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mTypeEditText = view.findViewById(R.id.group_type);
        mTypeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSaveGroupViewModel.getGroup().getValue().setType(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mInfoEditText = view.findViewById(R.id.group_info);
        mInfoEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSaveGroupViewModel.getGroup().getValue().setInfo(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mSaveButton = view.findViewById(R.id.save_button);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResult(Activity.RESULT_OK);
            }
        });

        mCancelButton = view.findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResult(Activity.RESULT_CANCELED);
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSaveGroupViewModel = ViewModelProviders.of(this).get(SaveGroupViewModel.class);
        mSaveGroupViewModel.init(new Group(
                (String) getArguments().get(ARG_GROUP_NAME),
                (String) getArguments().get(ARG_GROUP_TYPE),
                (String) getArguments().get(ARG_GROUP_INFO)
        ));

        updateUi(mSaveGroupViewModel.getGroup().getValue());
    }

    public void updateUi(Group group) {
        mNameEditText.setText(group.getName());
        mTypeEditText.setText(group.getType());
        mInfoEditText.setText(group.getInfo());
    }

    private void sendResult(int resultCode) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_GROUP_NAME,  mSaveGroupViewModel.getGroup().getValue().getName());
        intent.putExtra(EXTRA_GROUP_TYPE, mSaveGroupViewModel.getGroup().getValue().getType());
        intent.putExtra(EXTRA_GROUP_INFO, mSaveGroupViewModel.getGroup().getValue().getInfo());

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
        dismiss();
    }


}
