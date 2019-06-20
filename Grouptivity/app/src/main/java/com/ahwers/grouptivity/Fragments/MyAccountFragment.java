package com.ahwers.grouptivity.Fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ahwers.grouptivity.Models.BacklogActivity;
import com.ahwers.grouptivity.Models.DocumentSchemas.GroupSchema;
import com.ahwers.grouptivity.Models.DocumentSchemas.GroupSchema.GroupCollection;
import com.ahwers.grouptivity.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.CLIPBOARD_SERVICE;

public class MyAccountFragment extends Fragment {

    private static final String TAG = "MyAccountFragment";
    
    private TextView mMyInviteCodeTextField;
    private RecyclerView mInvitesRecyclerView;
    private InvitationsAdapter mInvitationsAdapter;

    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_account, container, false);

        mAuth = FirebaseAuth.getInstance();

        mMyInviteCodeTextField = view.findViewById(R.id.my_invite_code_textview);
        mMyInviteCodeTextField.setText(mAuth.getCurrentUser().getUid());
        mMyInviteCodeTextField.setFocusable(false);
        mMyInviteCodeTextField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final android.content.ClipboardManager clipboardManager = (ClipboardManager)getActivity().getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("My invite code", mMyInviteCodeTextField.getText());
                clipboardManager.setPrimaryClip(clipData);

                Toast.makeText(getActivity(), "Invite code copied to clipboard", Toast.LENGTH_SHORT)
                        .show();
            }
        });

        mInvitesRecyclerView = view.findViewById(R.id.invitations_recycler_view);
        mInvitesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        getInvitations();

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void getInvitations() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .collection("invitations")
                .whereEqualTo("complete", false)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<BacklogActivity> invitationsList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                invitationsList.add(new BacklogActivity(document));
                                updateUI(invitationsList);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void respondToInvitation(BacklogActivity invitation, boolean accepted) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .collection("invitations")
                .document(invitation.getId())
                .update("complete", true);

        if (accepted) {
            Map<String, Object> memberMap = new HashMap<>();
            memberMap.put(GroupSchema.GroupCollection.Cols.Members.DISPLAY_NAME, mAuth.getCurrentUser().getDisplayName());
            memberMap.put(GroupCollection.Cols.Members.ID, mAuth.getCurrentUser().getUid());
            memberMap.put(GroupSchema.GroupCollection.Cols.Members.OWNER, false);
            memberMap.put(GroupSchema.GroupCollection.Cols.Members.TITLE, null);

            db.collection(GroupSchema.GroupCollection.NAME)
                    .document(invitation.getReference())
                    .update(GroupCollection.Cols.Members.MapName + "." + mAuth.getCurrentUser().getUid(), memberMap);
        }

        String messageText = accepted == true ? "accepted" : "declined";
        Toast.makeText(getActivity(), "Invitation " + messageText + ".", Toast.LENGTH_SHORT)
                .show();

        getInvitations();

    }

    public void updateUI(List<BacklogActivity> invitationsList) {
        if (mInvitationsAdapter == null) {
            mInvitationsAdapter = new InvitationsAdapter(invitationsList);
        } else {
            mInvitationsAdapter.setInvitationsList(invitationsList);
            mInvitationsAdapter.notifyDataSetChanged();
        }

        if (mInvitesRecyclerView.getAdapter() == null) {
            mInvitesRecyclerView.setAdapter(mInvitationsAdapter);
        }
    }

    private class InvitationsAdapter extends RecyclerView.Adapter<InvitationHolder> {

        private List<BacklogActivity> mInvitationsList;

        public InvitationsAdapter(List<BacklogActivity> invitationsList) {
            mInvitationsList = invitationsList;
        }

        @NonNull
        @Override
        public InvitationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            InvitationHolder holder = new InvitationHolder(layoutInflater, parent);

            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull InvitationHolder holder, int position) {
            holder.bind(mInvitationsList.get(position));
        }

        @Override
        public int getItemCount() {
            return mInvitationsList.size();
        }

        public void setInvitationsList(List<BacklogActivity> invitationsList) {
            mInvitationsList = invitationsList;
            notifyDataSetChanged();
        }
    }

    private class InvitationHolder extends RecyclerView.ViewHolder {

        private TextView mInvitationMessageTextView;
        private Button mAcceptInvitationButton;
        private Button mDeclineInvitationButton;

        private BacklogActivity mInvitation;

        public InvitationHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_invitation, parent, false));

            mInvitationMessageTextView = itemView.findViewById(R.id.invitation_message_textview);

            mAcceptInvitationButton = itemView.findViewById(R.id.accept_invitation_button);
            mAcceptInvitationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    respondToInvitation(mInvitation, true);
                }
            });

            mDeclineInvitationButton = itemView.findViewById(R.id.decline_invitation_button);
            mDeclineInvitationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    respondToInvitation(mInvitation, false);
                }
            });
        }

        public void bind(BacklogActivity invitation) {
            mInvitation = invitation;

            mInvitationMessageTextView.setText(mInvitation.getActivityDescription());

        }

    }

}
