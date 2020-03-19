package com.ashish2117.user.streammedia.service;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

public class PlayerService extends Service {
    static MediaPlayer mediaPlayer;
    static String url,title,album,artist,msec;
    public static int duration;
    public static short playing;

    public static MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public static String getUrl() {
        return url;
    }

    public static String getTitle() {
        return title;
    }

    public static String getAlbum() {
        return album;
    }

    public static String getArtist() {
        return artist;
    }

    public static String getMsec() {
        return msec;
    }

    public static int getDuration() {
        return duration;
    }

    public static short getPlaying() {
        return playing;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        mediaPlayer=new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        Bundle b=intent.getExtras();
        url=b.getString("URL");
        title=b.getString("Title");
        album=b.getString("Album");
        msec=b.getString("Duration");
        artist=b.getString("Artist");
        Log.d("ulr title alb dur art",url+title+album+msec+artist);
        playing=0;
        try {
            mediaPlayer.setDataSource(url);
            new FetchData().execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static void pausePlayer()
    {
       mediaPlayer.pause();
       playing=2;
    }

    public static void resumePlayer()
    {
        mediaPlayer.start();
        playing=1;
    }



    private class FetchData extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mediaPlayer.start();
            playing=1;
        }
    }
}
