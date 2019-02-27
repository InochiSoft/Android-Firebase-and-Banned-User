package com.inochi.firebase.util;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.inochi.firebase.helper.Constants;
import com.inochi.firebase.listener.FirestoreDatabaseListener;
import com.inochi.firebase.helper.BundleSetting;
import com.inochi.firebase.item.UserItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class FirestoreDatabase {
    private FirebaseFirestore database;
    private FirestoreDatabaseListener firestoreDatabaseListener;
    private Context context;

    public FirestoreDatabase(Context context){
        this.context = context;
        database = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .build();
        database.setFirestoreSettings(settings);
    }

    public void getUser(UserItem item){
        final ArrayList<UserItem> result = new ArrayList<>();
        database.collection(Constants.Firestore.Collection.USER)
                .document(item.getUserId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            if (task.getResult() != null){
                                boolean exist = task.getResult().exists();
                                if (exist){
                                    UserItem userItem = new UserItem();
                                    Map<String, Object> data = task.getResult().getData();
                                    if (data != null){
                                        for (Map.Entry<String, Object> entry : data.entrySet()) {
                                            String key = entry.getKey();
                                            Object value = entry.getValue();

                                            switch (key) {
                                                case "userId": {
                                                    userItem.setUserId(value.toString());
                                                    break;
                                                }
                                                case "userName": {
                                                    userItem.setUserName(value.toString());
                                                    break;
                                                }
                                                case "userEmail": {
                                                    userItem.setUserEmail(value.toString());
                                                    break;
                                                }
                                                case "userPhoto": {
                                                    userItem.setUserPhoto(value.toString());
                                                    break;
                                                }
                                                case "userDate": {
                                                    long newValue = (Long) value;
                                                    userItem.setUserDate(newValue);
                                                    break;
                                                }
                                                case "userType": {
                                                    long newValue = (Long) value;
                                                    userItem.setUserType((int) newValue);
                                                    break;
                                                }
                                                case "appVer": {
                                                    userItem.setAppVer(value.toString());
                                                    break;
                                                }
                                                case "appName": {
                                                    context.getPackageName();
                                                    break;
                                                }
                                            }
                                        }

                                        result.add(userItem);
                                    }
                                }
                            }
                        }
                        if (firestoreDatabaseListener != null){
                            firestoreDatabaseListener.onFirestoreDataComplete(
                                    Constants.Firestore.Data.GET_USER, result);
                        }
                    }
                })
        ;
    }

    public void createUser(final UserItem item){
        Map<String, Object> data = new HashMap<>();
        data.put("userId", item.getUserId());
        data.put("userName", item.getUserName());
        data.put("userEmail", item.getUserEmail());
        data.put("userDate", item.getUserDate());
        data.put("userPhoto", item.getUserPhoto());
        data.put("userType", item.getUserType());
        data.put("appName", context.getPackageName());
        data.put("appVer", item.getAppVer());

        database.collection(Constants.Firestore.Collection.USER)
                .document(item.getUserId())
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (firestoreDatabaseListener != null){
                            firestoreDatabaseListener.onFirestoreDataSuccess(
                                    Constants.Firestore.Data.CREATE_USER, item);
                        }
                    }
                })
        ;
    }

    public void createUserListener(UserItem item){
        DocumentReference documentReference = database.collection(Constants.Firestore.Collection.USER)
                .document(item.getUserId());

        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                Map<String, Object> data;

                if (documentSnapshot != null) {
                    data = documentSnapshot.getData();
                    if (data != null){
                        UserItem userItem = new UserItem();

                        for (Map.Entry<String, Object> entry : data.entrySet()) {
                            String key = entry.getKey();
                            Object value = entry.getValue();

                            switch (key) {
                                case "userId": {
                                    userItem.setUserId(value.toString());
                                    break;
                                }
                                case "userName": {
                                    userItem.setUserName(value.toString());
                                    break;
                                }
                                case "userEmail": {
                                    userItem.setUserEmail(value.toString());
                                    break;
                                }
                                case "userPhoto": {
                                    userItem.setUserPhoto(value.toString());
                                    break;
                                }
                                case "userDate": {
                                    long newValue = (Long) value;
                                    userItem.setUserDate(newValue);
                                    break;
                                }
                                case "userType": {
                                    long newValue = (Long) value;
                                    userItem.setUserType((int) newValue);
                                    break;
                                }
                                case "appVer": {
                                    userItem.setAppVer(value.toString());
                                    break;
                                }
                            }
                        }

                        BundleSetting bundleSetting = new BundleSetting(context);
                        bundleSetting.setUserItemLast(userItem);
                    }
                }
            }
        });
    }

    public void setFirestoreDatabaseListener(FirestoreDatabaseListener firestoreDatabaseListener) {
        this.firestoreDatabaseListener = firestoreDatabaseListener;
    }
}
