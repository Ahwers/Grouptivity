package com.ahwers.grouptivity.Activities;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ahwers.grouptivity.Fragments.GroupFragment;
import com.ahwers.grouptivity.Fragments.MyAccountFragment;
import com.ahwers.grouptivity.Fragments.EventListFragment;
import com.ahwers.grouptivity.Fragments.GroupsListFragment;
import com.ahwers.grouptivity.Models.Group;
import com.ahwers.grouptivity.Models.ViewModels.NavigationViewModel;
import com.ahwers.grouptivity.R;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "HomeActivity";
    
    private static final int REQUEST_SIGN_IN = 0;

    private static final String EXTRA_GROUP_ID = "com.ahwers.grouptivity.group_id";

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    private NavigationViewModel mNavigationViewModel;
    private FirebaseAuth mAuth;

    List<MenuItem> mGroupItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);
        Log.d(TAG, "onCreate: ");

        mAuth = FirebaseAuth.getInstance();

        mDrawerLayout = findViewById(R.id.drawer_layout);

        mNavigationView = findViewById(R.id.navigation);
        mNavigationView.setNavigationItemSelectedListener(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);

        mNavigationViewModel = ViewModelProviders.of(this).get(NavigationViewModel.class);

//        if a user is logged in, can load the user's events quicker
        if (mAuth.getCurrentUser() != null) {
            mNavigationViewModel.init(mAuth.getCurrentUser().getUid());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");

        if (mAuth.getCurrentUser() == null) {
            if (isConnected()) {
                signIn();
            } else {
                Intent intent = new Intent(this, SignInActivity.class);
                startActivity(intent);
            }
        } else {
//            if user just logged in, init the view model
            if (mNavigationViewModel.getUserId() == null) {
                mNavigationViewModel.init(mAuth.getCurrentUser().getUid());
//            if activity was stopped, start listening again
            } else if (!mNavigationViewModel.isListening()) {
                mNavigationViewModel.startListening();
            }
            navHeaderInit();
            addGroupsToNav();

            FragmentManager fm = getSupportFragmentManager();
            Fragment fragment = fm.findFragmentById(R.id.fragment_container);

            if (fragment == null) {
                fragment = EventListFragment.newInstance();
                fm.beginTransaction()
                        .add(R.id.fragment_container, fragment)
                        .commit();
                mNavigationView.setCheckedItem(R.id.my_events);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        mNavigationViewModel.stopListening();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        if (menuItem.isChecked()) {
            mDrawerLayout.closeDrawers();
            return true;
        }
        menuItem.setChecked(true);

        switch (menuItem.getItemId()) {
            case R.id.my_events:
                replaceFragment(EventListFragment.newInstance());
                break;
            case R.id.groups:
                replaceFragment(new GroupsListFragment());
                break;
            case R.id.my_account:
                replaceFragment(new MyAccountFragment());
                break;
            case R.id.sign_out:
                signOut();
                break;
            default:
                for (MenuItem item : mGroupItems) {
                    if (item.getItemId() == menuItem.getItemId()) {
                        replaceFragment(GroupFragment.newInstance(item.getIntent().getStringExtra(EXTRA_GROUP_ID)));
                        break;
                    }
                }
        }

        return true;
    }

    private void navHeaderInit() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        View headerView = mNavigationView.getHeaderView(0);
        TextView emailTextView = headerView.findViewById(R.id.email);
        TextView nameTextView = headerView.findViewById(R.id.name);

        emailTextView.setText(currentUser.getEmail());
        nameTextView.setText(currentUser.getDisplayName());
    }

    private void addGroupsToNav() {
        mNavigationViewModel.getGroups().observe(this, mGroups -> {
            updateUi(mGroups);
        });
    }

    private void updateUi(List<Group> groups) {
        Menu menu = mNavigationView.getMenu();
        MenuItem groupsMenuItem = menu.findItem(R.id.my_groups);
        Menu groupsMenu = groupsMenuItem.getSubMenu();

        groupsMenu.clear();

        List<MenuItem> groupNavItems = new ArrayList<>();
        for (Group group : groups) {
            groupNavItems.add(
                    groupsMenu.add(R.id.groups_menu_group, View.generateViewId(), 1, group.getName())
                            .setIcon(R.drawable.ic_group)
                            .setCheckable(true)
                            .setIntent(new Intent().putExtra(EXTRA_GROUP_ID, group.getId()))
            );
        }
        mGroupItems = groupNavItems;

        mNavigationView.invalidate();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                mNavigationViewModel.init(user.getUid());

                navHeaderInit();
                addGroupsToNav();
                mNavigationView.setCheckedItem(R.id.my_events);
                replaceFragment(EventListFragment.newInstance());
            }
        }
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }

    private void signIn() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build()
        );

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                REQUEST_SIGN_IN);
    }

    private void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        mDrawerLayout.closeDrawers();
                        mNavigationViewModel.reset();
                        signIn();
                    }
                });
    }
    
}
