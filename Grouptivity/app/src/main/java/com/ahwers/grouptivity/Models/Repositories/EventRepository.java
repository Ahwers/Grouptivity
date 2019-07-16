package com.ahwers.grouptivity.Models.Repositories;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ahwers.grouptivity.Models.DocumentSchemas.AvailabilitySchema;
import com.ahwers.grouptivity.Models.DocumentSchemas.EventSchema;
import com.ahwers.grouptivity.Models.DocumentSchemas.EventSchema.EventCollection;
import com.ahwers.grouptivity.Models.DataModels.Event;
import com.ahwers.grouptivity.Models.DataModels.OpenDateAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class EventRepository {

    private static final String TAG = "EventRepository";

    public static final Integer SAVE_IN_PROGRESS = -1;
    public static final Integer SAVE_FAILED = 0;
    public static final Integer SAVE_SUCCESS = 1;

    protected String mNewEventId;

    // I think the listeners prevent this from being a singleton.
    // Think about this and refactor
    private ListenerRegistration mEventListener;

    private FirebaseFirestore mDb;

    public LiveData<Event> getEvent(String eventId) {
        mDb = FirebaseFirestore.getInstance();

        final MutableLiveData<Event> data = new MutableLiveData<>();

        mDb.collection(EventCollection.NAME)
                .document(eventId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                        data.setValue(new Event(document));

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

//    Gets all of a groups events
    public LiveData<List<Event>> getGroupEvents(String groupId) {
        mDb = FirebaseFirestore.getInstance();

        final MutableLiveData<List<Event>> data = new MutableLiveData<>();

        Task setEventsTask = mDb.collection(EventCollection.NAME)
                .whereEqualTo(EventCollection.Cols.GROUP_ID, groupId)
                .whereGreaterThanOrEqualTo(EventCollection.Cols.START_DATE_TIME, new Date())
                .orderBy(EventCollection.Cols.START_DATE_TIME, Query.Direction.ASCENDING)
                .limit(10)
                .get();

        Task unsetEventsTask = mDb.collection(EventCollection.NAME)
                .whereEqualTo(EventCollection.Cols.GROUP_ID, groupId)
                .whereEqualTo(EventCollection.Cols.START_DATE_TIME, null)
                .orderBy(EventCollection.Cols.CREATED, Query.Direction.ASCENDING)
                .get();

        Task combinedTask = Tasks.whenAllSuccess(setEventsTask, unsetEventsTask)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: ");
                        data.setValue(new ArrayList<>());
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<List<Object>>() {
                    @Override
                    public void onSuccess(List<Object> results) {
                        Log.d(TAG, "onSuccess: ");
                        List<Event> eventsList = new ArrayList<>();
                        for (int i = 0; i < results.size(); i++) {
                            QuerySnapshot snapshot = (QuerySnapshot) results.get(i);

                            for (DocumentSnapshot document : snapshot.getDocuments()) {
                                eventsList.add(new Event(document));
                            }
                        }
                        data.setValue(eventsList);
                    }
                });

        return data;
    }

//    Gets all events that the parsed user is a participant of
    public LiveData<List<Event>> getUserEvents(String userId) {
        mDb = FirebaseFirestore.getInstance();

        final MutableLiveData<List<Event>> data = new MutableLiveData<>();

        Task setEventsTask = mDb.collection(EventCollection.NAME)
                .whereArrayContains(EventCollection.Cols.PARTICIPANT_IDS, userId)
                .whereGreaterThanOrEqualTo(EventCollection.Cols.START_DATE_TIME, new Date())
                .orderBy(EventCollection.Cols.START_DATE_TIME, Query.Direction.ASCENDING)
                .limit(10)
                .get();

        Task unsetEventsTask = mDb.collection(EventCollection.NAME)
                .whereArrayContains(EventCollection.Cols.PARTICIPANT_IDS, userId)
                .whereEqualTo(EventCollection.Cols.START_DATE_TIME, null)
                .get();

        Task combinedTask = Tasks.whenAllSuccess(setEventsTask, unsetEventsTask)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: ");
                        data.setValue(new ArrayList<>());
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<List<Object>>() {
                    @Override
                    public void onSuccess(List<Object> results) {
                        Log.d(TAG, "onSuccess: ");
                        List<Event> eventsList = new ArrayList<>();
                        for (int i = 0; i < results.size(); i++) {
                            QuerySnapshot snapshot = (QuerySnapshot) results.get(i);

                            for (DocumentSnapshot document : snapshot.getDocuments()) {
                                eventsList.add(new Event(document));
                            }
                        }
                        data.setValue(eventsList);
                    }
                });

        return data;
    }

    public LiveData<Event> getEventListener(String eventId) {
        mDb = FirebaseFirestore.getInstance();

        final MutableLiveData<Event> data = new MutableLiveData<>();

        final DocumentReference eventDocRef = mDb.collection(EventCollection.NAME).document(eventId);

        eventDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot document, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (document != null && document.exists()) {
                    Log.d(TAG, "Current data: " + document.getData());
                    Event event = new Event(document);
                    String source = document.getMetadata().hasPendingWrites()
                            ? Event.UPDATE_SOURCE_LOCAL : Event.UPDATE_SOURCE_SERVER;
                    event.setUpdateSource(source);
                    data.setValue(event);
                } else {
                    Log.d(TAG, "Current data: null");
                }

            }
        });

        return data;
    }

    public void stopListening() {
        if (mEventListener != null) {
            mEventListener.remove();
        }
    }

    public LiveData<Integer> setEvent(Event event) {
        mDb = FirebaseFirestore.getInstance();

        final MutableLiveData<Integer> data = new MutableLiveData<>();
        data.setValue(-1);

        mDb.collection(EventCollection.NAME)
                .document(event.getId())
                .set(event.getEventDocumentMap())
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

    public LiveData<Integer> addEvent(Event event) {
        mDb = FirebaseFirestore.getInstance();

        final MutableLiveData<Integer> data = new MutableLiveData<>();
        data.setValue(-1);

        mDb.collection(EventCollection.NAME)
                .add(event.getEventDocumentMap())
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        mNewEventId = documentReference.getId();
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

    public String getNewEventId() {
        return mNewEventId;
    }

    public LiveData<Integer> setAvailability(List<OpenDateAvailability> availabilityList, String userId, String eventId) {
        mDb = FirebaseFirestore.getInstance();

        MutableLiveData<Integer> data = new MutableLiveData<>();

        WriteBatch batch = mDb.batch();

        Map<String, Object> availabilityMap = new HashMap<>();

        int i = 0;
        for (OpenDateAvailability availability : availabilityList) {
            Map<String, Object> dateMap = new HashMap<>();
            dateMap.put(AvailabilitySchema.AvailabilityDocument.Cols.DATE, availability.getDate());
            dateMap.put(AvailabilitySchema.AvailabilityDocument.Cols.AVAILABLE, availability.isAvailable());
            dateMap.put(AvailabilitySchema.AvailabilityDocument.Cols.START_TIME, availability.getStartTime());
            dateMap.put(AvailabilitySchema.AvailabilityDocument.Cols.END_TIME, availability.getEndTime());

            availabilityMap.put(AvailabilitySchema.AvailabilityDocument.NAME + i, dateMap);
            i++;
        }

        DocumentReference availabilityRef = mDb.collection(EventSchema.EventCollection.NAME)
                .document(eventId)
                .collection(AvailabilitySchema.AvailabilityDocument.NAME)
                .document(userId);
        batch.set(availabilityRef, availabilityMap);

        DocumentReference eventRef = mDb.collection(EventSchema.EventCollection.NAME)
                        .document(eventId);
        batch.update(
                eventRef,
                EventSchema.EventCollection.Cols.FlexDateTime.NAME + "." + EventSchema.EventCollection.Cols.FlexDateTime.VOTERS,
                FieldValue.arrayUnion(userId)
        );

        batch.commit()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
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

    public LiveData<List<OpenDateAvailability>> getAvailability(String userId, String eventId) {
        mDb = FirebaseFirestore.getInstance();

        final MutableLiveData<List<OpenDateAvailability>> data = new MutableLiveData<>();

        mDb.collection(EventCollection.NAME)
                .document(eventId)
                .collection(AvailabilitySchema.AvailabilityDocument.NAME)
                .document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        List<OpenDateAvailability> availability = new ArrayList<>();

                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                                Map<String, Object> data = document.getData();

                                for (int i = 0; i < data.size(); i++) {
                                    availability.add(new OpenDateAvailability((HashMap) data.get(AvailabilitySchema.AvailabilityDocument.NAME + String.valueOf(i))));
                                }

                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }

                        data.setValue(availability);
                    }
                });

        return data;
    }

    public void updateEditingState(String eventId, boolean editing) {
        mDb = FirebaseFirestore.getInstance();

        DocumentReference eventDoc = mDb.collection(EventCollection.NAME).document(eventId);

        eventDoc
                .update("editing", editing)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
    }

}
