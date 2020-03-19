package com.ashish2117.user.streammedia.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ashish2117.user.streammedia.util.OnlineStatusHandler;
import com.ashish2117.user.streammedia.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 01-07-2018.
 */

public class Launcher extends AppCompatActivity {
    EditText urltext,artistText,titleText,albumText;
    Button fetch,send;
    MediaMetadataRetriever m;
    String sendToFriend;
    DatabaseReference freindRef;
    FirebaseAuth mAuth;
    boolean problem;
    String malbum,mtitle,martist,murl,mduration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launch);
        mAuth=FirebaseAuth.getInstance();
        urltext=(EditText) findViewById(R.id.urltext);
        artistText=(EditText)findViewById(R.id.martist);
        albumText=(EditText)findViewById(R.id.malbum);
        titleText=(EditText)findViewById(R.id.mtitle);
        send=findViewById(R.id.sendreq);
        OnlineStatusHandler.goingToBackGround = true;
        send.setVisibility(View.INVISIBLE);
        fetch=(Button)findViewById(R.id.play);
        Bundle b=getIntent().getExtras();
        sendToFriend=b.getString("friend");
        setTitle("Send to "+sendToFriend);
        if(sendToFriend.compareTo(mAuth.getCurrentUser().getDisplayName())>0)
            freindRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://stream-audio-527dd.firebaseio.com/songs/" + mAuth
                    .getCurrentUser().getDisplayName() + "_" + sendToFriend);
        else
            freindRef = FirebaseDatabase.getInstance().getReferenceFromUrl("https://stream-audio-527dd.firebaseio.com/songs/" + sendToFriend + "_" + mAuth
                    .getCurrentUser().getDisplayName());

        fetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                murl = urltext.getText().toString();
                if(android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.N && !urltext.getText().toString().endsWith(".mp3"))
                {
                    AlertDialog.Builder alertBuilder=new AlertDialog.Builder(Launcher.this);
                    alertBuilder.setMessage("Can't fetch the details of this song because you are not using " +
                            "Andoid N or above. Try using a url that ends with .mp3")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });
                    AlertDialog dialog=alertBuilder.create();
                    dialog.setTitle("Error!");
                    dialog.show();
                    problem=true;
                }
                else if(urltext.getText().toString().isEmpty())
                {
                    AlertDialog.Builder alertBuilder=new AlertDialog.Builder(Launcher.this);
                    alertBuilder.setMessage("Oops! URL Cant't Be Epty!")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });
                    AlertDialog dialog=alertBuilder.create();
                    dialog.setTitle("Error!");
                    dialog.show();
                    problem=true;
                }
                else
                  new MediaData().execute();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Map<String,String> map=new HashMap<>();
                mtitle=titleText.getText().toString();
                martist=artistText.getText().toString();
                malbum=albumText.getText().toString();
                map.put("url",murl);
                map.put("title",TextUtils.isEmpty(mtitle)?"Unknown":mtitle);
                map.put("album",TextUtils.isEmpty(malbum)?"Unknown":malbum);
                map.put("artist",TextUtils.isEmpty(martist)?"Unknown":martist);
                map.put("duration",TextUtils.isEmpty(mduration)?"0000":mduration);
                map.put("user",mAuth.getCurrentUser().getDisplayName());
                map.put("timestamp",String.valueOf(System.currentTimeMillis()));
                map.put("status","sent");
                freindRef.push().setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            DatabaseReference ref=FirebaseDatabase.getInstance().getReference();
                            ref.child("song_sent").child(mAuth.getCurrentUser().getDisplayName()).child(sendToFriend).
                                    child("title").setValue(map.get("title")).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        Toast.makeText(Launcher.this, "Request Sent to " + sendToFriend,
                                                Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(Launcher.this, SentOrRecievedActivity.class);
                                        intent.putExtra("friend", sendToFriend);
                                        OnlineStatusHandler.goingToBackGround = false;
                                        startActivity(intent);
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

    private class MediaData extends AsyncTask<String,Void,Void>
    {
        private ProgressDialog p;
        @Override
        protected Void doInBackground(String... strings) {
            m=new MediaMetadataRetriever();
            m = new MediaMetadataRetriever();
            if (Build.VERSION.SDK_INT >= 14)
                m.setDataSource(murl, new HashMap<String, String>());
            else
                m.setDataSource(murl);
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p=new ProgressDialog(Launcher.this);
            p.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            p.setMessage("loading..");
            p.setCancelable(false);
            p.setIndeterminate(true);
            p.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mtitle=m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            malbum=m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            martist=m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            mduration=m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            murl=urltext.getText().toString();
            titleText.setText(mtitle);
            albumText.setText(malbum);
            artistText.setText(martist);
            send.setVisibility(View.VISIBLE);
           /* Intent i=new Intent(Launcher.this,MainActivity.class);
            i.putExtra("URL",urltext.getText().toString());
            i.putExtra("Title",m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            i.putExtra("Album",m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
            i.putExtra("Artist",m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            i.putExtra("Duration",m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));*/
            p.dismiss();
            //startActivity(i);

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        OnlineStatusHandler.goingToBackGround = false;
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