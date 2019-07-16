package com.ahwers.grouptivity.Models.Repositories;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ahwers.grouptivity.Models.DataModels.BacklogActivity;
import com.ahwers.grouptivity.Models.DocumentSchemas.GroupSchema;
import com.ahwers.grouptivity.Models.DocumentSchemas.GroupSchema.GroupCollection;
import com.ahwers.grouptivity.Models.DataModels.Group;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

@Singleton
public class GroupRepository {

    private static final String TAG = "GroupRepository";

    public static final Integer SAVE_IN_PROGRESS = -1;
    public static final Integer SAVE_FAILED = 0;
    public static final Integer SAVE_SUCCESS = 1;

    private ListenerRegistration mGroupsListener;
    private FirebaseFirestore mDb;

    public LiveData<Group> getGroup(String groupId) {
        mDb = FirebaseFirestore.getInstance();

        final MutableLiveData<Group> data = new MutableLiveData<>();

        mDb.collection(GroupSchema.GroupCollection.NAME)
                .document(groupId)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                        data.setValue(new Group(document));

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }

        });

        return data;
    }

    public LiveData<List<Group>> getUsersGroupsListener(String userId) {
        mDb = FirebaseFirestore.getInstance();

        final MutableLiveData<List<Group>> data = new MutableLiveData<>();

        mGroupsListener = mDb.collection(GroupSchema.GroupCollection.NAME)
//                .whereArrayContains(GroupCollection.Cols.MEMBERS, mAuth.getCurrentUser().getUid())
                .whereEqualTo(GroupSchema.GroupCollection.Cols.Members.MapName + "." + userId + "." + GroupCollection.Cols.Members.ID, userId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e);
                            return;
                        }

                        List<Group> groupsList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : value) {
                            groupsList.add(new Group(document));
                        }

                        data.setValue(groupsList);
                    }
                });

        return data;
    }

    public void stopListening() {
        if (mGroupsListener != null) {
            mGroupsListener.remove();
        }
    }

    public LiveData<Integer> setGroup(Group group) {
        mDb = FirebaseFirestore.getInstance();

        final MutableLiveData<Integer> data = new MutableLiveData<>();
        data.setValue(-1);

        Map<String, Object> groupMap = group.getGroupDocumentMap();

        mDb.collection(GroupCollection.NAME)
                .document(group.getId())
                .set(groupMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        data.setValue(1);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        data.setValue(0);
                    }
                });

        return data;
    }

    public LiveData<Integer> addGroup(Group group) {
        mDb = FirebaseFirestore.getInstance();

        final MutableLiveData<Integer> data = new MutableLiveData<>();
        data.setValue(-1);

        mDb.collection(GroupCollection.NAME)
                .add(group.getGroupDocumentMap())
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        data.setValue(1);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        data.setValue(0);
                    }
                });

        return data;
    }

    public void inviteUserToGroup(String groupId, String groupName, String recipientUserId) {
        BacklogActivity invite = new BacklogActivity();
        invite.setActivityType("group_invitation");
        invite.setActivityDescription("You have been invited to join the group: " + groupName);
        invite.setActiviyDateTime(new Date());
        invite.setReference(groupId);

        mDb.collection("users")
                .document(recipientUserId)
                .collection("invitations")
                .add(invite.getDocumentMap());
    }


}
