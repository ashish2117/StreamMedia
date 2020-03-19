package com.ashish2117.user.streammedia.util;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class OnlineStatusHandler {
    private static FirebaseAuth mAuth;
    private static DatabaseReference onlineStatusRef;
    public static boolean goingToBackGround;
    static {
        mAuth = FirebaseAuth.getInstance();
        onlineStatusRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(mAuth.getCurrentUser().getDisplayName()).child("online_status");
        goingToBackGround = true;
    }
    public static void setOnline(){
        onlineStatusRef.setValue("online");
    }

    public static void setOffline(){
        onlineStatusRef.setValue(String.valueOf(System.currentTimeMillis()));
    }
}
