package com.ahwers.grouptivity.Fragments;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ahwers.grouptivity.Activities.SignInActivity;
import com.ahwers.grouptivity.Models.DataModels.Group;
import com.ahwers.grouptivity.Models.DataModels.GroupMember;
import com.ahwers.grouptivity.Models.ViewModels.MembersListViewModel;
import com.ahwers.grouptivity.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MembersListFragment extends Fragment {

    private static final String TAG = "GroupMemberListFragment";

    private static final String ARG_GROUP_ID = "group_id";

    private static final int REQUEST_INVITE_MEMBER = 1;

    private static final String DIALOG_INVITE_MEMBER = "invite_member_dialog";

    private RecyclerView mMembersRecyclerView;
    private MembersAdapter mMembersAdapter;
    private ConstraintLayout mScreen;
    private ProgressBar mProgressBar;
    private FloatingActionButton mInviteMembersButton;
    private FloatingActionButton mSaveMembersButton;

    private FirebaseAuth mAuth;
    private MembersListViewModel mMembersListViewModel;

    public static MembersListFragment newInstance(String groupId) {
        Log.d(TAG, "newInstance: ");

        Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID, groupId);

        MembersListFragment fragment = new MembersListFragment();
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
        View view = inflater.inflate(R.layout.fragment_members_list, container, false);
        Log.d(TAG, "onCreateView: ");

        mMembersRecyclerView = view.findViewById(R.id.group_members_recycler_view);
        mMembersRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mInviteMembersButton = view.findViewById(R.id.invite_members_button);
        mInviteMembersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                InviteMemberFragment dialog = InviteMemberFragment.newInstance(
                        mMembersListViewModel.getGroup().getValue().getId()
                );

                dialog.setTargetFragment(MembersListFragment.this, REQUEST_INVITE_MEMBER);
                dialog.show(fm, DIALOG_INVITE_MEMBER);
            }
        });

        mSaveMembersButton = view.findViewById(R.id.save_members_button);
        mSaveMembersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveGroup();
            }
        });

        mProgressBar = view.findViewById(R.id.progress_bar);
        mProgressBar.setIndeterminate(true);
        mScreen = view.findViewById(R.id.screen);

        setLoading(true);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated: ");

        String groupId = (String) getArguments().get(ARG_GROUP_ID);
        mMembersListViewModel = ViewModelProviders.of(this).get(MembersListViewModel.class);
        mMembersListViewModel.init(groupId);

        mMembersListViewModel.getGroup().observe(this, mGroup -> {
            updateUi(mGroup);
            if (!mMembersListViewModel.getGroup().getValue().isUserMember(mAuth.getCurrentUser().getUid())) {
                Toast.makeText(getActivity(), "You are not a member of this group.", Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");

        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            startActivity(intent);
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

    private void saveGroup() {
        mMembersListViewModel.saveGroup().observe(this, mGroupSaveState -> {
            if (mGroupSaveState != null) {
                String saveMessage = null;
                    switch (mGroupSaveState) {
                        case -1:
                            saveMessage = "Saving";
                            break;
                        case 0:
                            saveMessage = "Members update failed";
                            break;
                        case 1:
                            saveMessage = "Members updated";
                            break;
                        case -10:
                            saveMessage = "Group is invalid";
                            break;
                    }

                    Toast.makeText(getActivity(), saveMessage, Toast.LENGTH_SHORT)
                            .show();

            }
        });
    }

    private void updateUi(Group mGroup) {
        boolean userIsOwner = mGroup.isUserOwner(mAuth.getCurrentUser().getUid());
        if (mMembersAdapter == null) {
            mMembersAdapter = new MembersAdapter(mGroup.getMembers(), userIsOwner);
        } else {
            mMembersAdapter.setMembersList(mGroup.getMembers());
            mMembersAdapter.notifyDataSetChanged();
        }

        if (mMembersRecyclerView.getAdapter() == null) {
            mMembersRecyclerView.setAdapter(mMembersAdapter);
        }

        if (!mMembersListViewModel.getGroup().getValue().isUserOwner(mAuth.getCurrentUser().getUid())) {
            mInviteMembersButton.setEnabled(false);
            mInviteMembersButton.hide();

            mSaveMembersButton.setEnabled(false);
            mSaveMembersButton.hide();
        }

        setLoading(false);
    }

    public void refreshList() {
        setLoading(true);
        mMembersListViewModel.refreshGroup().observe(this, mGroup -> {
            updateUi(mGroup);
        });
    }

    public void setLoading(boolean loading) {
        if (loading) {
            mScreen.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setEnabled(true);
        } else {
            mScreen.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            mProgressBar.setEnabled(false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case REQUEST_INVITE_MEMBER:
                String inviteRecipientUserId = InviteMemberFragment.getRecipientUserId(data);

                mMembersListViewModel.inviteMember(inviteRecipientUserId);
                break;
        }
    }


    private class MembersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_MEMBER_VIEW_TYPE = 0;
        private static final int EDIT_MEMBER_VIEW_TYPE = 1;

        private boolean mUserIsOwner;
        private List<GroupMember> mMembersList;

        public MembersAdapter(List<GroupMember> membersList, boolean userIsOwner) {
            mMembersList = membersList;
            mUserIsOwner = userIsOwner;
        }

        @Override
        public int getItemViewType(int position) {
            if (mUserIsOwner) {
                return EDIT_MEMBER_VIEW_TYPE;
            }
            return VIEW_MEMBER_VIEW_TYPE;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            RecyclerView.ViewHolder holder = null;

            switch (viewType) {
                case EDIT_MEMBER_VIEW_TYPE:
                    holder = new EditMemberHolder(layoutInflater, parent);
                    break;
                case VIEW_MEMBER_VIEW_TYPE:
                    holder = new MemberHolder(layoutInflater, parent);
                    break;
            }

            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (mUserIsOwner) {
                EditMemberHolder editHolder = (EditMemberHolder) holder;
                editHolder.bind(mMembersList.get(position));
            } else {
                MemberHolder memberHolder = (MemberHolder) holder;
                memberHolder.bind(mMembersList.get(position));
            }
        }

        @Override
        public int getItemCount() {
            return mMembersList.size();
        }

        public void setMembersList(List<GroupMember> membersList) {
            mMembersList = membersList;
            notifyDataSetChanged();
        }
    }

    private class MemberHolder extends RecyclerView.ViewHolder {

        private TextView mUsernameTextView;
        private TextView mTitleTextView;
        private ImageView mOwnerImageView;

        private GroupMember mGroupMember;

        public MemberHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_member, parent, false));

            mUsernameTextView = itemView.findViewById(R.id.member_username_text_view);

            mTitleTextView = itemView.findViewById(R.id.member_title_text_view);

            mOwnerImageView = itemView.findViewById(R.id.owner_icon);
        }

        public void bind(GroupMember groupMember) {
            mGroupMember = groupMember;

            mUsernameTextView.setText(mGroupMember.getDisplayName());

            mTitleTextView.setText(mGroupMember.getTitle());

            mOwnerImageView.setVisibility(mGroupMember.isOwner() ? View.VISIBLE : View.GONE);
        }

    }

    private class EditMemberHolder extends RecyclerView.ViewHolder {

        private EditText mUsernameEditText;
        private EditText mTitleEditText;
        private CheckBox mOwnerCheckBox;

        private GroupMember mGroupMember;

        public EditMemberHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_edit_member, parent, false));

            mUsernameEditText = itemView.findViewById(R.id.member_username_edit_text);
            mUsernameEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    mGroupMember.setDisplayName(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            mTitleEditText = itemView.findViewById(R.id.member_title_edit_text);
            mTitleEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    mGroupMember.setTitle(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            mOwnerCheckBox = itemView.findViewById(R.id.owner_check_box);
            mOwnerCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mGroupMember.setOwner(isChecked);
                }
            });
        }

        public void bind(GroupMember groupMember) {
            mGroupMember = groupMember;

            mUsernameEditText.setText(mGroupMember.getDisplayName());

            mTitleEditText.setText(mGroupMember.getTitle());

            mOwnerCheckBox.setChecked(mGroupMember.isOwner() ? true : false);

        }
    }

}
