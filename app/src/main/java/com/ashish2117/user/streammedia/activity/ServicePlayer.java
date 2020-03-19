package com.ashish2117.user.streammedia.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ashish2117.user.streammedia.util.OnlineStatusHandler;
import com.ashish2117.user.streammedia.service.PlayerService;
import com.ashish2117.user.streammedia.R;

import java.util.HashMap;

public class ServicePlayer extends AppCompatActivity {
    EditText serviceUrl;
    Button startMusic;
    String mtitle,malbum,murl,mduration,martist;
    MediaMetadataRetriever m;
    private boolean goingToBackGround;

    @Override
    protected void onRestart() {
        super.onRestart();
        OnlineStatusHandler.setOnline();
        goingToBackGround = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_player);
        serviceUrl=(EditText)findViewById(R.id.serviceurl);
        startMusic=findViewById(R.id.startservice);
        PlayerService.playing=0;
        goingToBackGround = true;
        startMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(PlayerService.playing==0) {
                    murl = serviceUrl.getText().toString();
                    Log.d("ulr", "URL" + murl);
                    new MediaData().execute(murl);
                    startMusic.setText("PAUSE");
                }
                else if(PlayerService.playing==1) {
                    PlayerService.pausePlayer();
                    startMusic.setText("PAUSE");
                }
                else if(PlayerService.playing==2)
                {
                    PlayerService.resumePlayer();
                    startMusic.setText("RESUME");
                }

            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goingToBackGround = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(goingToBackGround)
            OnlineStatusHandler.setOffline();
    }

    private class MediaData extends AsyncTask<String,Void,Void>
    {
        private ProgressDialog p;
        @Override
        protected Void doInBackground(String... strings) {
            Log.d("ulr","in doinback");
            m=new MediaMetadataRetriever();
            m = new MediaMetadataRetriever();
            if (Build.VERSION.SDK_INT >= 14)
                m.setDataSource(strings[0], new HashMap<String, String>());
            else
                m.setDataSource(strings[0]);
            Log.d("ulr","source set");
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("ulr","in preexec");
            p=new ProgressDialog(ServicePlayer.this);
            p.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            p.setMessage("loading..");
            p.setCancelable(false);
            p.setIndeterminate(true);
            p.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d("ulr","in post exex");
            mtitle=m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            malbum=m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            martist=m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            mduration=m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            murl=serviceUrl.getText().toString();
            Intent intent=new Intent(ServicePlayer.this,PlayerService.class);
            intent.putExtra("URL",murl);
            intent.putExtra("Artist",martist);
            intent.putExtra("Album",malbum);
            intent.putExtra("Title",mtitle);
            intent.putExtra("Duration",mduration);
            Log.d("ulr","Extras set");
            startService(intent);
            p.dismiss();

        }
    }

}
