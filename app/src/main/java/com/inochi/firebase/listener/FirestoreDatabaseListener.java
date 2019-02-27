package com.inochi.firebase.listener;

import java.util.ArrayList;

public interface FirestoreDatabaseListener {
    void onFirestoreDataSuccess(int dataType, Object value);
    void onFirestoreDataComplete(int dataType, ArrayList<?> result);
}
