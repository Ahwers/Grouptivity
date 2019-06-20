package com.ahwers.grouptivity.Fragments;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ahwers.grouptivity.Activities.SignInActivity;
import com.ahwers.grouptivity.Models.Group;
import com.ahwers.grouptivity.Models.ViewModels.GroupViewModel;
import com.ahwers.grouptivity.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class GroupFragment extends Fragment {

    private static final String TAG = "GroupFragment";

    private static final String ARG_GROUP_ID = "group_id" ;

    private static final int REQUEST_EDIT_GROUP = 1;

    private static final String DIALOG_EDIT_GROUP = "edit_group_dialog";

    private TextView mNameTextView;
    private TextView mInfoTextView;
    private ConstraintLayout mScreen;
    private ProgressBar mProgressBar;

    private ViewPager mViewPager;
    private GroupFragmentPagerAdapter mGroupFragmentAdapater;
    private TabLayout mTabLayout;

    private GroupViewModel mGroupViewModel;
    private FirebaseAuth mAuth;

    public static GroupFragment newInstance(String groupId) {
        Log.d(TAG, "newInstance: ");

        Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID, groupId);

        GroupFragment fragment = new GroupFragment();
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
        View view = inflater.inflate(R.layout.fragment_group, container, false);
        Log.d(TAG, "onCreateView: ");

        mNameTextView = view.findViewById(R.id.group_title);
        mInfoTextView = view.findViewById(R.id.group_info);

        mViewPager = view.findViewById(R.id.view_pager);
        mTabLayout = view.findViewById(R.id.tab_layout);

        mScreen = view.findViewById(R.id.screen);

        mProgressBar = view.findViewById(R.id.progress_bar);
        mProgressBar.setIndeterminate(true);

        setLoading(true);

        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated: ");

        mGroupViewModel = ViewModelProviders.of(this).get(GroupViewModel.class);
        mGroupViewModel.init((String) getArguments().get(ARG_GROUP_ID));

        mGroupViewModel.getGroup().observe(this, mGroup -> {
            updateUi(mGroup);
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

//        mGroupFragmentAdapater = null;
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
        inflater.inflate(R.menu.fragment_group, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        Log.d(TAG, "onPrepareOptionsMenu: ");

        mGroupViewModel.getGroup().observe(this, mGroup -> {
            if (mAuth.getCurrentUser() != null) {
                if (!mGroup.isUserOwner(mAuth.getCurrentUser().getUid())) {
                    menu.findItem(R.id.edit_group).setVisible(false).setEnabled(false);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                setLoading(true);
                mGroupViewModel.refreshGroup().observe(this, mGroup -> {
//                    Maybe refresh members list from cache here?
                    updateUi(mGroup);
                });
                refreshChildFragments();
                return true;
            case R.id.edit_group:
                FragmentManager fm = getFragmentManager();
                SaveGroupDialogFragment dialog = SaveGroupDialogFragment.newInstance(
                        mGroupViewModel.getGroup().getValue().getName(),
                        mGroupViewModel.getGroup().getValue().getType(),
                        mGroupViewModel.getGroup().getValue().getInfo()
                );

                dialog.setTargetFragment(GroupFragment.this, REQUEST_EDIT_GROUP);
                dialog.show(fm, DIALOG_EDIT_GROUP);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case REQUEST_EDIT_GROUP:
                mGroupViewModel.getGroup().getValue().setName(SaveGroupDialogFragment.getGroupName(data));
                mGroupViewModel.getGroup().getValue().setType(SaveGroupDialogFragment.getGroupType(data));
                mGroupViewModel.getGroup().getValue().setInfo(SaveGroupDialogFragment.getGroupInfo(data));

                saveGroup();

                break;
        }
    }

    private void saveGroup() {
        setLoading(true);

        mGroupViewModel.saveGroup().observe(this, mGroupSaveState -> {
            if (mGroupSaveState != null) {
                String saveMessage = null;

                switch (mGroupSaveState) {
                    case -1:
                        saveMessage = "Implement loading wheel";
                        break;
                    case 0:
                        saveMessage = "Group save failed";
                        break;
                    case 1:
                        saveMessage = "Group saved";
                        break;
                    case -10:
                        saveMessage = "Group is invalid";
                        break;
                }
                Toast.makeText(getActivity(), saveMessage, Toast.LENGTH_SHORT)
                        .show();

                updateUi(mGroupViewModel.getGroup().getValue());
            }
        });
    }

    private void refreshChildFragments() {
        for (int i = 0; i < mGroupFragmentAdapater.mGroupFragments.size(); i++) {
            if (mGroupFragmentAdapater.getItem(i) instanceof EventListFragment) {
                EventListFragment fragment = (EventListFragment) mGroupFragmentAdapater.getItem(i);
                fragment.refreshList();
            } else if (mGroupFragmentAdapater.getItem(i) instanceof MembersListFragment) {
                MembersListFragment fragment = (MembersListFragment) mGroupFragmentAdapater.getItem(i);
                fragment.refreshList();
            }
        }
    }

    private void updateUi(Group group) {
        if (mGroupFragmentAdapater == null) {
            List<Fragment> fragmentList = new ArrayList<>();
            fragmentList.add(EventListFragment.newInstance(group.getId(), group.isUserOwner(mAuth.getCurrentUser().getUid())));
            fragmentList.add(MembersListFragment.newInstance(group.getId()));

            mGroupFragmentAdapater = new GroupFragmentPagerAdapter(getChildFragmentManager(), fragmentList);
            mViewPager.setAdapter(mGroupFragmentAdapater);
            mTabLayout.setupWithViewPager(mViewPager);
        } else if (mViewPager.getAdapter() == null) {
            mViewPager.setAdapter(mGroupFragmentAdapater);
            mTabLayout.setupWithViewPager(mViewPager);

        }

        mNameTextView.setText(group.getName());
        mInfoTextView.setText(group.getInfo());

        setLoading(false);
    }

    private void setLoading(boolean loading) {
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


    private class GroupFragmentPagerAdapter extends FragmentStatePagerAdapter {

        private List<Fragment> mGroupFragments;

        public GroupFragmentPagerAdapter(FragmentManager fm, List<Fragment> groupFragments) {
            super(fm);
            mGroupFragments = groupFragments;
        }

        @Override
        public Fragment getItem(int position) {
            return mGroupFragments.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String fragmentTitle = null;
            if (mGroupFragments.get(position) instanceof MembersListFragment) {
                fragmentTitle = "Members";
            } else {
                fragmentTitle = "Events";
            }

            return fragmentTitle;
        }

        @Override
        public int getCount() {
            return mGroupFragments.size();
        }
    }

}
