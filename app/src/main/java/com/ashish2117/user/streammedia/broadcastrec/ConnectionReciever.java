package com.ashish2117.user.streammedia.broadcastrec;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import com.ashish2117.user.streammedia.util.UserDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ConnectionReciever extends BroadcastReceiver {
    String requestID;
    String username;
    DatabaseReference databaseReference;
    FirebaseAuth mAuth;
    @Override
    public void onReceive(final Context context, Intent intent) {
        NotificationManagerCompat notificationManagerCompat= NotificationManagerCompat.from(context);
        notificationManagerCompat.cancel(1);
        mAuth= FirebaseAuth.getInstance();
        databaseReference= FirebaseDatabase.getInstance().getReferenceFromUrl("https://stream-audio-527dd.firebaseio.com/users/");
        requestID=intent.getStringExtra("requestID");
        username=intent.getStringExtra("username");
        databaseReference.child(mAuth.getCurrentUser().getDisplayName()).child("connection_requests")
                .child(requestID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {

                                databaseReference.child(mAuth.getCurrentUser().getDisplayName()).child("connection")
                                        .child("connectedto").setValue(username).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            UserDetails.connectedTo = username;
                                            Toast.makeText(context,"Connected to "+username,
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                }
            }
        });
    }
}
