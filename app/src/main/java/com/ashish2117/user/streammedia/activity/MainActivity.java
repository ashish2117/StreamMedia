package com.ashish2117.user.streammedia.activity;

import java.io.IOException;
import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ashish2117.user.streammedia.util.OnlineStatusHandler;
import com.ashish2117.user.streammedia.R;


public class MainActivity extends Activity {
    //DatabaseReference reference;
   // FirebaseAuth mAuth;
    Button buttonStop,buttonStart ;
    String AudioURL;
    short playing;
    MediaPlayer mediaplayer;
    private int mProgressStatus;
    ProgressBar pBar;
    private Handler mHandler;
    private ProgressDialog p;
    int duration,minn,seccc;
    TextView title,album,artist,mCurDuration,mMaxDuration;
    String mTitle,mAlbum,mArtist,mmin,msec,friend;
    Thread progress;
    ImageView albumArt;

    private class FetchMetaData extends AsyncTask<Void,Void,Void>{
        MediaMetadataRetriever mediaMetadataRetriever;

        @Override
        protected Void doInBackground(Void... voids) {
            mediaMetadataRetriever=new MediaMetadataRetriever();
            mediaMetadataRetriever = new MediaMetadataRetriever();
            if (Build.VERSION.SDK_INT >= 14)
                mediaMetadataRetriever.setDataSource(AudioURL, new HashMap<String, String>());
            else
                mediaMetadataRetriever.setDataSource(AudioURL);
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i("Musicc","FetchMetaDataCalled");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.i("Musicc","FetchMetaDataDone");
            super.onPostExecute(aVoid);
            byte[] art = mediaMetadataRetriever.getEmbeddedPicture();

            if( art != null ){
                albumArt.setImageBitmap( BitmapFactory.decodeByteArray(art, 0, art.length));

            }
            else{
                albumArt.setImageResource(R.drawable.music);
            }


        }
    }
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
            p=new ProgressDialog(MainActivity.this);
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
            mediaplayer.start();
            progress.start();
            playing=1;
            buttonStart.setText("Pause");
            buttonStart.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_pause_black_24dp,0,0,0);
            new FetchMetaData().execute();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        title=findViewById(R.id.title);
        album=findViewById(R.id.album);
        artist=findViewById(R.id.artist);
        albumArt = (ImageView)findViewById(R.id.album_art);
        pBar=(ProgressBar)findViewById(R.id.progressbar);
        buttonStart = (Button)findViewById(R.id.button1);
        buttonStop = (Button)findViewById(R.id.button2);
        mCurDuration=findViewById(R.id.curtime);
        mMaxDuration=findViewById(R.id.maxtime);
        OnlineStatusHandler.goingToBackGround = true;
        Bundle b=getIntent().getExtras();
        AudioURL=b.getString("URL");
        mTitle=b.getString("Title");
        mAlbum=b.getString("Album");
        msec=b.getString("Duration");
        mArtist=b.getString("Artist");
        friend=b.getString("friend");
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
                if(playing==0) {
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

                else if(playing==1)
                {
                    mediaplayer.pause();
                    playing = 2;
                    buttonStart.setText("Play");
                    buttonStart.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_play_arrow_black_24dp,
                            0,0,0);
                }
                else
                {
                    mediaplayer.start();
                    playing=1;
                    buttonStart.setText("Pause");
                    buttonStart.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_pause_black_24dp,
                            0,0,0);
                }
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaplayer.stop();
                MainActivity.super.onBackPressed();
                OnlineStatusHandler.goingToBackGround = false;
                finish();
             }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaplayer.stop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mediaplayer.stop();
        MainActivity.this.finish();
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
