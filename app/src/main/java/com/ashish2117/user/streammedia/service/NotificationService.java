package com.ashish2117.user.streammedia.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import android.os.Build;
import android.app.NotificationChannel;

import com.ashish2117.user.streammedia.broadcastrec.ConnectionReciever;
import com.ashish2117.user.streammedia.util.Constants;
import com.ashish2117.user.streammedia.R;
import com.ashish2117.user.streammedia.util.UserDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class NotificationService extends Service {
    FirebaseAuth mAuth;
    DatabaseReference myRef,reference1;
    Notification notification;
    final String CHANNEL_ID="01";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Restart","Service startde");
        createNotificationChannel();
        mAuth=FirebaseAuth.getInstance();
        myRef= FirebaseDatabase.getInstance().getReferenceFromUrl("https://stream-audio-527dd.firebaseio.com/users/"+
               mAuth.getCurrentUser().getDisplayName());
        myRef.child("connection").child("connectedto").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue().toString().isEmpty()&&!UserDetails.connectedTo.isEmpty())
                {
                    UserDetails.connectedTo="";
                    Toast.makeText(NotificationService.this,"Disconnected",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        myRef.child("play_request").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final Map<String,String> map=new HashMap<>();
                Log.d("Added","inside child added");
                String key=dataSnapshot.getKey();
                Log.d("Added","key="+key);
                  for(DataSnapshot d:dataSnapshot.getChildren())
                  {
                      map.put(d.getKey(),d.getValue().toString());
                  }
                  Log.d("Added","Map Created");
                {
                      Log.d("Added","inside frirnd");
                      myRef.child("play_request").child(key).removeValue()
                              .addOnCompleteListener(new OnCompleteListener<Void>() {
                                  @Override
                                  public void onComplete(@NonNull Task<Void> task) {
                                      if(task.isSuccessful())
                                      {
                                          Log.d("Added","inside task");
                                          //Intent i = new Intent(NotificationService.this, GroupPlayerActivity.class);
                                          Intent i = new Intent(NotificationService.this, RemotePlayerService.class);
                                          i.putExtra("URL", map.get("url"));
                                          i.putExtra("Title", map.get("title"));
                                          i.putExtra("Album", map.get("album"));
                                          i.putExtra("Artist", map.get("artist"));
                                          i.putExtra("Duration", map.get("duration"));
                                          i.putExtra("friend", map.get("friend"));
                                          i.putExtra("instantbuf","false");
                                          i.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
                                          startService(i);
                                      }
                                  }
                              });
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

       myRef.child("connection_requests").limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String fromFriend=dataSnapshot.child("username").getValue().toString();
                Intent intent=new Intent(NotificationService.this, ConnectionReciever.class);
                intent.putExtra("username",fromFriend);
                intent.putExtra("requestID",dataSnapshot.getKey());
                PendingIntent pendingIntent = PendingIntent.getBroadcast(NotificationService.this,
                        0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                notification = new NotificationCompat.Builder(NotificationService.this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.appicon)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.dp))
                        .setContentTitle("New Connection Request")
                        .setContentText(fromFriend+" sent you a connction request")
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .addAction(R.drawable.ic_person_add_black_24dp,"CONNECT",pendingIntent)
                        .setColor(ContextCompat.getColor(getApplicationContext(),R.color.colorPrimary))
                        .build();
                NotificationManagerCompat notificationManagerCompat=NotificationManagerCompat.from(NotificationService.this);
                notificationManagerCompat.notify(1,notification);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Restart","service destroyed");
        sendBroadcast(new Intent("YouWillNeverKillMe"));
    }
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "notification channel";
            String description = "channel description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
