package com.ashish2117.user.streammedia.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.ashish2117.user.streammedia.broadcastrec.FirebaseMessagingStarterBroadcastRec;
import com.ashish2117.user.streammedia.util.ProfileImageHandler;
import com.ashish2117.user.streammedia.R;
import com.ashish2117.user.streammedia.util.UserDetails;
import com.ashish2117.user.streammedia.activity.Chat;
import com.ashish2117.user.streammedia.activity.InboxActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyMessagingService extends FirebaseMessagingService {
    FirebaseAuth mAuth;
    final String CHANNEL_ID="02";
    Notification notification;
    String title;

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        createNotificationChannel();
        mAuth=FirebaseAuth.getInstance();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference();
        Intent intent=null;
        if(remoteMessage.getData().get("click_action").equals("Recieved_Songs"))
        {
            reference.child(remoteMessage.getData().get("sent_from")).child(mAuth.getCurrentUser().getDisplayName())
                   .removeValue();
            intent=new Intent(MyMessagingService.this, InboxActivity.class);
            intent.putExtra("friend", remoteMessage.getData().get("sent_from"));
            title="New Song Received";
        }
        else if(remoteMessage.getData().get("click_action").equals("RequestRecieved")){
            intent = new Intent(remoteMessage.getData().get("click_action"));
            title="New Friend Request";
        }else if (remoteMessage.getData().get("click_action").equals("Recieved_Message")){
            Log.d("Messagee",remoteMessage.getData().get("body"));
            if(remoteMessage.getData().get("sent_from").equals(UserDetails.chatWith))
                return;
            intent=new Intent(MyMessagingService.this, Chat.class);
            intent.putExtra("friend", remoteMessage.getData().get("sent_from"));
            intent.putExtra("started_from", "notification_tap");
            title = remoteMessage.getData().get("sent_from");
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);
        notification=new NotificationCompat.Builder(this,CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(remoteMessage.getData().get("body"))
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.appicon)
                .setPriority(Notification.PRIORITY_HIGH)
                .setLargeIcon(ProfileImageHandler.getProfileImageBitmap(title + ".jpg", getApplicationContext()))
                .setOnlyAlertOnce(true)
                .setColor(ContextCompat.getColor(getApplicationContext(),R.color.colorPrimary))
                .setContentIntent(pendingIntent).build();
        NotificationManagerCompat notificationManager=NotificationManagerCompat.from(MyMessagingService.this);
        notificationManager.notify(0,notification);
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

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent intent = new Intent(this, FirebaseMessagingStarterBroadcastRec.class);
        intent.setAction("restartFirebaseMessage");
        sendBroadcast(intent);
    }
}
