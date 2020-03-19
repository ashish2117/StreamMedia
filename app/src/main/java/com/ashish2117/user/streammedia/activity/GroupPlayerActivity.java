package com.ashish2117.user.streammedia.activity;

import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ashish2117.user.streammedia.util.OnlineStatusHandler;
import com.ashish2117.user.streammedia.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class GroupPlayerActivity extends Activity {

    DatabaseReference myRef,myFriendRef;
    FirebaseAuth mAuth;
    String myStatus,friendStatus;
    Button buttonStop,buttonStart ;
    String AudioURL;
    short playing;
    String instantbuf;
    MediaPlayer mediaplayer;
    private int mProgressStatus;
    ProgressBar pBar;
    private Handler mHandler;
    private ProgressDialog p;
    int duration,minn,seccc;
    TextView title,album,artist,mCurDuration,mMaxDuration;
    String mTitle,mAlbum,mArtist,mmin,msec,friend;
    Thread progress;
    boolean stopFlag;

    private class fetchData extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                mediaplayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p=new ProgressDialog(GroupPlayerActivity.this);
            p.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            p.setMessage("loading..");
            p.setCancelable(false);
            p.setIndeterminate(true);
            p.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            p.dismiss();
            Log.d("bufff","buffered");
            myRef.setValue("buffered").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        myStatus="buffered";
                        Log.d("bufff","buffered stus set");
                      if(friendStatus.equals("buffered")) {
                         myRef.setValue("playing").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    mediaplayer.start();
                                    progress.start();
                                    myStatus="playing";
                                    playing = 1;
                                    buttonStart.setText("Pause");
                                    buttonStart.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_pause_black_24dp, 0, 0, 0);
                                }
                            }
                        });
                    }
                    }
                }
            });

        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_group);
        OnlineStatusHandler.goingToBackGround = true;
        initActivity();
        Log.d("instanrbuf",""+instantbuf);
        if(instantbuf.equals("true"))
           initBuffering();
        else
            moveTaskToBack(true);

        myFriendRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("onDataChhh",dataSnapshot.toString());
                if(dataSnapshot.getValue()!=null)
                toggleFriendStatus(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mediaplayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
                switch (i)
                {
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                        playing=2;
                        break;
                    case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                        playing=1;
                        break;
                }
                return false;
            }
        });

        buttonStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(playing==1)
                {
                    myFriendRef.setValue("paused").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                myRef.setValue("paused").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            myStatus = "paused";
                                            friendStatus = "paused";
                                            mediaplayer.pause();
                                            playing = 2;
                                            buttonStart.setText("Play");
                                            buttonStart.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_play_arrow_black_24dp,
                                                    0, 0, 0);
                                        }
                                    }
                                });
                            }
                        }
                    });

                }
                else if(playing==2)
                {
                    myFriendRef.setValue("playing").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                myRef.setValue("playing").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            myStatus="playing";
                                            friendStatus="playing";
                                            mediaplayer.start();
                                            playing=1;
                                            buttonStart.setText("Pause");
                                            buttonStart.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_pause_black_24dp,
                                                    0,0,0);
                                        }
                                    }
                                });
                            }
                        }
                    });

                }
            }
        });
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myRef.setValue("off").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                       if(task.isSuccessful())
                       {
                           myFriendRef.setValue("off").addOnCompleteListener(new OnCompleteListener<Void>() {
                               @Override
                               public void onComplete(@NonNull Task<Void> task) {
                                  if(task.isSuccessful())
                                  {
                                      mediaplayer.stop();
                                      OnlineStatusHandler.goingToBackGround = false;
                                      finish();
                                  }
                               }
                           });
                       }
                    }
                });

            }
        });
    }


    ///////////No change part/////////////////////////////////
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaplayer.stop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        GroupPlayerActivity.this.finish();
        OnlineStatusHandler.goingToBackGround = false;
    }

    private static String calDuration(int sec)
    {
        int min,secc;
        min=sec/60;
        secc=sec%60;
        if(min<10&&secc<10)
            return "0"+min+":0"+secc;
        if(min<10)
            return "0"+min+":"+secc;
        if(secc<10)
            return min+":0"+sec;
        return min+":"+secc;
    }

    private void initActivity()
    {
        stopFlag=false;
        mAuth=FirebaseAuth.getInstance();
        title=findViewById(R.id.gtitle);
        album=findViewById(R.id.galbum);
        artist=findViewById(R.id.gartist);
        pBar=(ProgressBar)findViewById(R.id.gprogressbar);
        buttonStart = (Button)findViewById(R.id.gbutton1);
        buttonStop = (Button)findViewById(R.id.gbutton2);
        mCurDuration=findViewById(R.id.gcurtime);
        mMaxDuration=findViewById(R.id.gmaxtime);
        Bundle b=getIntent().getExtras();
        AudioURL=b.getString("URL");
        mTitle=b.getString("Title");
        mAlbum=b.getString("Album");
        msec=b.getString("Duration");
        mArtist=b.getString("Artist");
        friend=b.getString("friend");
        instantbuf=b.getString("instantbuf");
        title.setText(mTitle);
        album.setText(mAlbum);
        artist.setText(mArtist);
        mProgressStatus = 0;
        mHandler=new Handler();
        mediaplayer = new MediaPlayer();
        mediaplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        playing=0;
        duration= Integer.parseInt(msec)/1000+1;
        pBar.setMax(duration);
        minn=duration/60;
        seccc=duration%60;
        if(minn<10)
            mmin="0"+minn;
        else
            mmin=""+minn;
        if(seccc<10)
            msec="0"+seccc;
        else
            msec=""+seccc;
        mMaxDuration.setText(mmin+":"+msec);
        mCurDuration.setText("00:00");
        Log.d("MyName",mAuth.getCurrentUser().getDisplayName());
        Log.d("FriendName",friend);
        myRef=FirebaseDatabase.getInstance().getReferenceFromUrl("https://stream-audio-527dd.firebaseio.com/users/" +
                        friend+"/friends/"+mAuth.getCurrentUser().getDisplayName()+"/status");

        myFriendRef=FirebaseDatabase.getInstance().getReferenceFromUrl("https://stream-audio-527dd.firebaseio.com/users/" +
                mAuth.getCurrentUser().getDisplayName()+ "/friends/" + friend + "/status");

        myStatus="off";
        friendStatus="off";
    }

    private void initBuffering()
    {
        try {

            mediaplayer.setDataSource(AudioURL);
            new fetchData().execute();


        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        progress= new Thread(new Runnable() {
            @Override
            public void run() {
                while (mProgressStatus < duration){
                    if(playing==1||playing==0)
                        mProgressStatus++;
                    android.os.SystemClock.sleep(1000);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            pBar.setProgress(mProgressStatus);
                            mCurDuration.setText(calDuration(mProgressStatus));
                        }
                    });
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        playing=1;
                    }
                });
            }
        });
    }

    private void toggleFriendStatus(String status)
    {
        if(status.equals("buffered")) {
            friendStatus = "buffered";
            Toast.makeText(this, "friend buffering done", Toast.LENGTH_SHORT).show();
            if (instantbuf.equals("true") && myStatus.equals("buffered")) {
                myRef.setValue("playing").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mediaplayer.start();
                            progress.start();
                            myStatus = "playing";
                            playing = 1;
                            buttonStart.setText("Pause");
                            buttonStart.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_pause_black_24dp, 0, 0, 0);
                        }
                    }
                });
            } else
                initBuffering();
        }
        else if(status.equals("paused"))
        {
            mediaplayer.pause();
            myStatus="paused";
            playing = 2;
            buttonStart.setText("Play");
            buttonStart.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_play_arrow_black_24dp,
                    0,0,0);
        }
        else if(status.equals("playing"))
        {
            mediaplayer.start();
            myStatus="playing";
            playing=1;
            buttonStart.setText("Pause");
            buttonStart.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_pause_black_24dp,
                    0,0,0);
        }
        else if(status.equals("off")&&stopFlag)
        {
            mediaplayer.stop();
            finish();
        }
        else if(!stopFlag)
        {
            stopFlag=true;
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        OnlineStatusHandler.setOnline();
        OnlineStatusHandler.goingToBackGround = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(OnlineStatusHandler.goingToBackGround)
        OnlineStatusHandler.setOffline();
    }

}



