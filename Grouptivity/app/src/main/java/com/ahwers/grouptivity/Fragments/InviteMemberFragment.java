package com.ahwers.grouptivity.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.ahwers.grouptivity.R;


public class InviteMemberFragment extends DialogFragment {

    private static final String TAG = "InviteMemberFragment";

    private static final String ARG_GROUP_ID = "group_id";
    private static final String EXTRA_INVITE_RECIPIENT_ID = "com.ahwers.grouptivity.invite_recipient_id";

    private TextView mUserInviteCodeTextView;
    private Button mInviteMemberButton;
    private Button mCancelButton;

    public static InviteMemberFragment newInstance(String groupId) {
        Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID, groupId);
        InviteMemberFragment fragment = new InviteMemberFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public static String getRecipientUserId(Intent data) {
        String recipientUserId = data.getStringExtra(EXTRA_INVITE_RECIPIENT_ID);
        return recipientUserId;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_invite_member, container, false);

        mUserInviteCodeTextView = v.findViewById(R.id.user_invite_code_textview);

        mInviteMemberButton = v.findViewById(R.id.invite_member_button);
        mInviteMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResult(Activity.RESULT_OK, mUserInviteCodeTextView.getText().toString());
            }
        });

        mCancelButton = v.findViewById(R.id.cancel_invitation_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResult(Activity.RESULT_CANCELED, null);
            }
        });

        return v;
    }

    private void sendResult(int resultCode, String recipientUserId) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_INVITE_RECIPIENT_ID, recipientUserId);

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
        dismiss();
    }

}
