package com.ashish2117.user.streammedia.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.ashish2117.user.streammedia.App;
import com.ashish2117.user.streammedia.util.Constants;
import com.ashish2117.user.streammedia.R;
import com.ashish2117.user.streammedia.activity.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;

public class RemotePlayerService extends Service  {
    MediaPlayer mediaPlayer;
    String url,title,album,artist,msec,friend;
    int duration;
    short playing;
    FirebaseAuth mAuth;
    String username;
    DatabaseReference myFriendRef, myRef, myFriendReff;
    String friendStatus;
    String ownStatus;
    Notification status;
    private final String LOG_TAG = "NotificationService";
    Bundle bundle;

    @Override
    public void onCreate() {
        super.onCreate();
        mAuth = FirebaseAuth.getInstance();
        username = mAuth.getCurrentUser().getDisplayName();
        ownStatus = "off";
        friendStatus = "off";
    }

    private class FetchPlayerData extends Thread{
        @Override
        public void run() {
            try {
                if(!mediaPlayer.isPlaying()){
                    mediaPlayer.setDataSource(url);
                    mediaPlayer.prepare();
                    if (friendStatus.equals("buffered")) {
                        mediaPlayer.start();
                        ownStatus = "playing";
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i("Musiccc","Rem Start");
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        /////////////////////////////////

        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            mediaPlayer=new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            bundle=intent.getExtras();
            url=bundle.getString("URL");
            title=bundle.getString("Title");
            album=bundle.getString("Album");
            msec=bundle.getString("Duration");
            artist=bundle.getString("Artist");
            friend = bundle.getString("friend");
            showNotification();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    Log.d("media","Prepared");
                    ownStatus = "buffered";
                    myRef.setValue("buffered");
                }
            });
            if(!mediaPlayer.isPlaying()){
                try {
                    mediaPlayer.setDataSource(url);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (friendStatus.equals("buffered")) {
                    mediaPlayer.start();
                    ownStatus = "playing";
                }
            }
            Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();

        } else if (intent.getAction().equals(Constants.ACTION.PREV_ACTION)) {
            Toast.makeText(this, "Clicked Previous", Toast.LENGTH_SHORT).show();
            Log.i(LOG_TAG, "Clicked Previous");
        } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
            if(ownStatus.equals("playing")) {
                mediaPlayer.pause();
                toggleFriendStatus("paused");
                Toast.makeText(this, "Clicked Play"+ownStatus, Toast.LENGTH_SHORT).show();
                ownStatus = "paused";

            }
            else {
                mediaPlayer.start();
                toggleFriendStatus("playing");
                Toast.makeText(this, "Clicked Play"+ownStatus, Toast.LENGTH_SHORT).show();
                ownStatus = "playing";
            }


            Log.i(LOG_TAG, "Clicked Play");
        } else if (intent.getAction().equals(Constants.ACTION.NEXT_ACTION)) {
            Toast.makeText(this, "Clicked Next", Toast.LENGTH_SHORT).show();
            Log.i(LOG_TAG, "Clicked Next");
        } else if (intent.getAction().equals(
                Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            Toast.makeText(this, "Service Stoped", Toast.LENGTH_SHORT).show();
            stopForeground(true);
            stopSelf();
        }

        ////////////////////////////////
        /*mediaPlayer=new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                Log.d("media","Prepared");
                ownStatus = "buffered";
                myRef.setValue("buffered");
            }
        });*/

        myFriendRef=FirebaseDatabase.getInstance().getReferenceFromUrl("https://stream-audio-527dd.firebaseio.com/users/" +
                mAuth.getCurrentUser().getDisplayName()+ "/friends/" + friend + "/status");

        myFriendReff=FirebaseDatabase.getInstance().getReferenceFromUrl("https://stream-audio-527dd.firebaseio.com/users/" +
                mAuth.getCurrentUser().getDisplayName()+ "/friends/" + friend);

        myRef=FirebaseDatabase.getInstance().getReferenceFromUrl("https://stream-audio-527dd.firebaseio.com/users/" +
                friend+"/friends/"+mAuth.getCurrentUser().getDisplayName()+"/status");




        myFriendReff.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d("onDataChildChhh",dataSnapshot.toString());
                if(dataSnapshot.getValue()!=null)
                    toggleFriendStatus(dataSnapshot.getValue().toString());
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
        Log.d("ulr title alb dur art",url+title+album+msec+artist);



        return START_NOT_STICKY;
    }

    private void toggleFriendStatus(String status) {
        if (status.equals("buffered")) {
            friendStatus = "buffered";
            if (ownStatus.equals("buffered")) {
                Toast.makeText(this, "friend buffering done", Toast.LENGTH_SHORT).show();
                mediaPlayer.start();
            }
        }

        else if(status.equals("paused"))
        {
            mediaPlayer.pause();
            ownStatus="paused";
        }
        else if(status.equals("playing"))
        {
            mediaPlayer.start();
            ownStatus="playing";
        }
        else if(status.equals("off"))
        {
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                stopSelf();
            }
        }

    }

    private void showNotification() {
// Using RemoteViews to bind custom layouts into Notification
        RemoteViews views = new RemoteViews(getPackageName(),
                R.layout.status_bar);
        RemoteViews bigViews = new RemoteViews(getPackageName(),
                R.layout.status_bar_expanded);

// showing default album image
        bigViews.setImageViewBitmap(R.id.status_bar_album_art,
                Constants.getDefaultAlbumArt(this));

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Intent previousIntent = new Intent(this, RemotePlayerService.class);
        //previousIntent.putExtras(bundle);
        previousIntent.setAction(Constants.ACTION.PREV_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);

        Intent playIntent = new Intent(this, RemotePlayerService.class);
        //playIntent.putExtras(bundle);
        playIntent.setAction(Constants.ACTION.PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent nextIntent = new Intent(this, RemotePlayerService.class);
        nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
        //nextIntent.putExtras(bundle);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);

        Intent closeIntent = new Intent(this, RemotePlayerService.class);
        closeIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        //closeIntent.putExtras(bundle);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, 0);

        views.setOnClickPendingIntent(R.id.status_bar_playy, pplayIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_playy, pplayIntent);

        views.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);

        views.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent);

        views.setOnClickPendingIntent(R.id.status_bar_small, pcloseIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);


        views.setTextViewText(R.id.status_bar_track_name, "This is the title");
        bigViews.setTextViewText(R.id.status_bar_track_name, "This is the title");



        views.setTextViewText(R.id.status_bar_artist_name, "This is the artist");
        bigViews.setTextViewText(R.id.status_bar_artist_name,"This is the artist");

        bigViews.setTextViewText(R.id.status_bar_album_name, "This is the album");

        status = new NotificationCompat.Builder(this, App.CHANNEL_ID).build();
        status.contentView = views;
        status.bigContentView = bigViews;
        status.flags = Notification.FLAG_ONGOING_EVENT;
        status.icon = R.drawable.ic_launcher;
        status.contentIntent = pendingIntent;
        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
    }

}
