package com.ahwers.grouptivity.Fragments;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.ahwers.grouptivity.R;

public class LoadableFragment extends Fragment {

    private ConstraintLayout mScreen;
    private ProgressBar mProgressBar;

    protected void createLoadingViews(View view) {
        mProgressBar = view.findViewById(R.id.progress_bar);
        mProgressBar.setIndeterminate(true);

        mScreen = view.findViewById(R.id.screen);
        mScreen.setVisibility(View.GONE);
    }

    protected void setLoading(boolean loading) {
        mProgressBar.setEnabled(loading);

        mScreen.setVisibility(loading ? View.GONE : View.VISIBLE);
        mProgressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    protected boolean isLoading() {
        return mProgressBar.getVisibility() == View.GONE ? false : true;
    }

}
