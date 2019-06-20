package com.ahwers.grouptivity.Fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ahwers.grouptivity.Activities.SignInActivity;
import com.ahwers.grouptivity.Models.Group;
import com.ahwers.grouptivity.Models.GroupMember;
import com.ahwers.grouptivity.Models.ViewModels.SaveGroupViewModel;
import com.ahwers.grouptivity.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Date;

public class SaveGroupFragment extends Fragment {

    private static final String TAG = "SaveGroupFragment";

    private static final String ARG_GROUP_ID = "group_id";

    private EditText mNameEditText;
    private EditText mTypeEditText;
    private EditText mInfoEditText;
    private Menu mMenu;
    private ProgressBar mProgressBar;

    private FirebaseAuth mAuth;
    private SaveGroupViewModel mGroupViewModel;

    public static SaveGroupFragment newInstance(String groupId) {
        Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID, groupId);

        SaveGroupFragment fragment = new SaveGroupFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_save_group, container, false);

        mNameEditText = view.findViewById(R.id.group_name);
        mNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mGroupViewModel.set(SaveGroupViewModel.NAME, s.toString());
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
                mGroupViewModel.set(SaveGroupViewModel.TYPE, s.toString());
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
                mGroupViewModel.set(SaveGroupViewModel.INFO, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mProgressBar = view.findViewById(R.id.progress_bar);
        mProgressBar.setIndeterminate(true);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String groupId = (String) getArguments().get(ARG_GROUP_ID);

        mGroupViewModel = ViewModelProviders.of(this).get(SaveGroupViewModel.class);

        if (groupId != null) {
            mGroupViewModel.init(groupId);
        } else {
            Group group = new Group();
            mGroupViewModel.init(group);
        }

        mGroupViewModel.getGroup().observe(this, mGroup -> {
            updateUi(mGroup);
        });

    }

    @Override
    public void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_save_group, menu);
        mMenu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_group:
                if (isConnected()) {
                    saveGroup();
                } else {
                    Toast.makeText(getActivity(), "Connect to the internet to create a new group", Toast.LENGTH_SHORT)
                            .show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateUi(Group group) {
        mNameEditText.setText(group.getName());
        mInfoEditText.setText(group.getInfo());
        mTypeEditText.setText(group.getType());
        mProgressBar.setVisibility(View.GONE);
    }

    private void saveGroup() {
        setSaveButtonEnabled(false);

        if (mGroupViewModel.getGroup().getValue().getId() == null) {
            GroupMember creator = new GroupMember(
                    mAuth.getCurrentUser().getUid(),
                    mAuth.getCurrentUser().getDisplayName(),
                    "Creator",
                    true
            );
            mGroupViewModel.getGroup().getValue().getMembers().add(creator);

            mGroupViewModel.getGroup().getValue().setCreator(mAuth.getCurrentUser().getUid());
            mGroupViewModel.getGroup().getValue().setCreated(new Date());
        }

        mGroupViewModel.saveGroup().observe(this, mGroupSaveState -> {
            String saveMessage = null;

            if (mGroupSaveState == -1) {
//                Toast.makeText(getActivity(), "Implement loading wheel", Toast.LENGTH_SHORT)
//                        .show();
            } else {
                switch (mGroupSaveState) {
                    case 0:
                        saveMessage = "Group Save Failed";
                        break;
                    case 1:
                        saveMessage = "Group Saved";
                        break;
                    case -10:
                        saveMessage = "Group is invalid";
                        break;
                }
                setSaveButtonEnabled(true);
                Toast.makeText(getActivity(), saveMessage, Toast.LENGTH_SHORT)
                        .show();
            }

        });

    }

    private void setSaveButtonEnabled(boolean enabled) {
        MenuItem item = mMenu.findItem(R.id.save_group);
        item.setEnabled(enabled);
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager)getActivity().getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

}
