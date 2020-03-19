package com.ashish2117.user.streammedia.activity;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.ashish2117.user.streammedia.util.OnlineStatusHandler;
import com.ashish2117.user.streammedia.R;
import com.ashish2117.user.streammedia.util.UserDetails;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SentOrRecievedActivity extends AppCompatActivity {
    String friend;
    MenuItem connectbutton;
    Button addNewSongButton;
    DatabaseReference friendRef,myRef;
    Firebase songsRef;
    LinearLayout layout;
    ScrollView scrollView;
    FirebaseAuth mAuth;
    final int CONNECT=1;
    final int DISCONNECT=2;
    final int CONNECTING=3;
    View.OnClickListener songClickListener;
    private Map<String, Map<String,String>> songsList;
    boolean initialLoaded;
    View.OnLongClickListener songLongClickListener;
    int status;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.songs_activity_menu,menu);
        connectbutton = menu.findItem(R.id.action_bar_connect_button);
        MenuItem item=menu.findItem(R.id.search_view);
        SearchView searchView=(SearchView) item.getActionView();
        searchView.setIconified(true);
        toggleConnectButton(CONNECT);
        if(UserDetails.connectedTo.equals(friend))
            toggleConnectButton(DISCONNECT);
        return  super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()){
            case R.id.action_bar_connect_button:
                connection();
                break;
            case android.R.id.home: {
                OnlineStatusHandler.goingToBackGround = false;
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sent_or_recieved);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        scrollView = (ScrollView) findViewById(R.id.scrollViewSongs);
        layout = findViewById(R.id.layout1);
        OnlineStatusHandler.goingToBackGround = true;
        addNewSongButton =(Button) findViewById(R.id.addNewSongButton);
        addNewSongButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(SentOrRecievedActivity.this, Launcher.class);
                intent.putExtra("friend",friend);
                OnlineStatusHandler.goingToBackGround = false;
                startActivity(intent);
            }
        });
        Bundle b=getIntent().getExtras();
        friend=b.getString("friend");
        setTitle(friend);
        initialLoaded = false;
        songsList = new HashMap<>();
        mAuth=FirebaseAuth.getInstance();
        friendRef= FirebaseDatabase.getInstance().getReferenceFromUrl("https://stream-audio-527dd.firebaseio.com/users/"+friend);
        myRef=FirebaseDatabase.getInstance().getReferenceFromUrl("https://stream-audio-527dd.firebaseio.com/users/"+
               mAuth.getCurrentUser().getDisplayName());
        if(friend.compareTo(mAuth.getCurrentUser().getDisplayName())>0)
            songsRef = new Firebase("https://stream-audio-527dd.firebaseio.com/songs/" + mAuth
                    .getCurrentUser().getDisplayName() + "_" + friend);
        else
            songsRef = new Firebase("https://stream-audio-527dd.firebaseio.com/songs/" + friend + "_" + mAuth
                    .getCurrentUser().getDisplayName());

        friendRef.child("connection").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        if(dataSnapshot.toString().contains(mAuth.getCurrentUser().getDisplayName()))
                        {
                            Log.d("Chhh", "inside if with frirnd ");
                            if( UserDetails.connectedTo.equals("")) {
                                Log.d("Chhh", "inside if with empty ");
                                 myRef.child("connection").child("connectedto").setValue(friend)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        UserDetails.connectedTo = friend;
                                                                        Toast.makeText(SentOrRecievedActivity.this,
                                                                                "connected to " + friend, Toast.LENGTH_SHORT).show();
                                                                        toggleConnectButton(DISCONNECT);
                                                                    }
                                                                }
                                                            });
                            }
                            else
                            {
                                toggleConnectButton(DISCONNECT);
                            }
                        }
                        else
                        {
                            toggleConnectButton(CONNECT);
                            Toast.makeText(SentOrRecievedActivity.this,"Disconnected",Toast.LENGTH_SHORT).show();
                            UserDetails.connectedTo="";
                        }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d("Dtat",dataSnapshot.toString());

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        songsRef.addChildEventListener(new com.firebase.client.ChildEventListener() {
            @Override
            public void onChildAdded(com.firebase.client.DataSnapshot dataSnapshot, String s) {
                String id = dataSnapshot.getKey();
                Map map = dataSnapshot.getValue(Map.class);
                String user = map.get("user").toString();
                songsList.put(id,map);
                if(user.equals(mAuth.getCurrentUser().getDisplayName()))
                    addSongToLayout(1,map,id);
                else if(user.equals(friend))
                    addSongToLayout(2,map,id);
            }

            @Override
            public void onChildChanged(com.firebase.client.DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);
                String key = dataSnapshot.getKey();
                String userName = map.get("user").toString();
                String status = map.get("status").toString();
                if(initialLoaded && status.equals("read") && userName.equals(mAuth.getCurrentUser().getDisplayName())){
                    for(int i=layout.getChildCount()-1;i>=0;i--){
                        LinearLayout l=(LinearLayout) layout.getChildAt(i);
                        if(((TextView)l.findViewById(R.id.messageId)).getText().toString().equals(key)){
                            ((ImageView)l.findViewById(R.id.status_sent)).setImageResource(R.drawable.ic_done_all_grey_24dp);
                            break;
                        }

                    }
                }
            }

            @Override
            public void onChildRemoved(com.firebase.client.DataSnapshot dataSnapshot) {
                Map map = dataSnapshot.getValue(Map.class);
                String key = dataSnapshot.getKey();
                for(int i=layout.getChildCount()-1;i>=0;i--){
                    LinearLayout l=(LinearLayout) layout.getChildAt(i);
                    if(((TextView)l.findViewById(R.id.messageId)).getText().toString().equals(key)){
                        layout.removeView(l);
                    }

                }

            }

            @Override
            public void onChildMoved(com.firebase.client.DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        songClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout clickedSongLayout = (LinearLayout)view;
                Map<String, String> map = songsList.get(((TextView)clickedSongLayout
                        .findViewById(R.id.messageId))
                        .getText().toString());
                Intent i=new Intent(SentOrRecievedActivity.this, MainActivity.class);
                i.putExtra("URL",map.get("url"));
                i.putExtra("Title",map.get("title"));
                i.putExtra("Album",map.get("album"));
                i.putExtra("Artist",map.get("artist"));
                i.putExtra("Duration",map.get("duration"));
                i.putExtra("friend",friend);
                OnlineStatusHandler.goingToBackGround = false;
                startActivity(i);
            }
        };


        songLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final LinearLayout clickedSongLayout = (LinearLayout)view;
                final Map<String, String> map = songsList.get(((TextView)clickedSongLayout
                        .findViewById(R.id.messageId))
                        .getText().toString());
                PopupMenu popup = new PopupMenu(SentOrRecievedActivity.this, view);
                popup.getMenuInflater().inflate(R.menu.longpress_inbox_item_menu, popup.getMenu());
                ImageView imageView = clickedSongLayout.findViewById(R.id.status_sent);
                if(imageView == null)
                    popup.getMenu().getItem(0).setVisible(false);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId()==R.id.copy)
                        {
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("url", map.get("url"));
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(SentOrRecievedActivity.this,"URL copied",Toast.LENGTH_SHORT).show();
                        }
                        else if(item.getItemId()==R.id.download) {
                            Uri uri = Uri.parse(map.get("url")); // missing 'http://' will cause crashed
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            OnlineStatusHandler.goingToBackGround = false;
                            startActivity(intent);
                        }
                        else if(item.getItemId()==R.id.details)
                        {
                            Dialog dialog=new Dialog(SentOrRecievedActivity.this);
                            int secs=Integer.parseInt(map.get("duration"))/1000;
                            int min=secs/60;
                            int sec=secs%60;
                            dialog.setContentView(R.layout.dialogue_details_layout);
                            TextView title=(TextView)dialog.findViewById(R.id.dialogue_title);
                            TextView album=(TextView)dialog.findViewById(R.id.dialogue_album);
                            TextView artist=(TextView)dialog.findViewById(R.id.dialogue_artist);
                            TextView duration=(TextView)dialog.findViewById(R.id.dialogue_duration);
                            title.setText("Tilte     : "+map.get("title"));
                            album.setText("Album     : "+map.get("album"));
                            artist.setText("Artist(s) : "+map.get("artist"));
                            duration.setText("Duration  : "+min+" mins "+sec+" secs");
                            dialog.show();
                        }else if(item.getItemId() == R.id.delete_song){
                            {
                                new AlertDialog.Builder(SentOrRecievedActivity.this)
                                        .setMessage("Delete this message?")
                                        .setCancelable(true)
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                String messageId = ((TextView)clickedSongLayout.findViewById(R.id.messageId)).getText().toString();
                                                songsRef.child(messageId).removeValue(new Firebase.CompletionListener() {
                                                    @Override
                                                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                                        if(firebaseError == null)
                                                            layout.removeView(clickedSongLayout);
                                                        else
                                                            Toast.makeText(SentOrRecievedActivity.this,"Error Occured",Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.cancel();
                                            }
                                        }).create().show();
                            }
                        }

                        return true;
                    }
                });
                popup.show();
                return true;
            }
        };
    }

    private void addSongToLayout(int type, Map map,String key) {
        String album = map.get("album").toString();
        String artist = map.get("artist").toString();
        String duration = map.get("duration").toString();
        String title = map.get("title").toString();
        String url = map.get("url").toString();
        String status = map.get("status").toString();
        String time = getTimeIn12HourFormat(map.get("timestamp").toString());
        Log.d("Statuss",status);
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.weight = 1.0f;
        LinearLayout linearLayout=null;
        if(type == 2) {
            linearLayout = (LinearLayout) inflater.inflate(R.layout.song_bubble_left, null, false);
            lp2.gravity = Gravity.LEFT;
            lp2.setMargins(0,0,150,20);
            if(status.equals("sent"))
                songsRef.child(key).child("status").setValue("read");
        }else if(type==1){
            linearLayout = (LinearLayout) inflater.inflate(R.layout.song_bubble_right, null, false);
            ImageView statusSent = linearLayout.findViewById(R.id.status_sent);
            if(status.equals("sent"))
                statusSent.setImageResource(R.drawable.ic_done_grey_24dp);
            else if(status.equals("read"))
                statusSent.setImageResource(R.drawable.ic_done_all_grey_24dp);
            lp2.gravity = Gravity.RIGHT;
            lp2.setMargins(150,0,0,20);
        }
        TextView textView = linearLayout.findViewById(R.id.song_bubble_title);
        TextView timeStampTextView = linearLayout.findViewById(R.id.timeStampTextView);
        TextView songDuration = linearLayout.findViewById(R.id.songDurationTextView);
        TextView messageId = linearLayout.findViewById(R.id.messageId);
        messageId.setText(key);
        textView.setText(title);
        timeStampTextView.setText(time);
        songDuration.setText("03:04");
        linearLayout.setLayoutParams(lp2);
        linearLayout.setOnClickListener(songClickListener);
        linearLayout.setOnLongClickListener(songLongClickListener);
        layout.addView(linearLayout);
        scrollToBottom();
    }

    private void toggleConnectButton(int status)
    {
        this.status=status;
        if(status==CONNECT)
        {
            connectbutton.setIcon(R.drawable.connect);
        }
        else if(status==DISCONNECT)
        {
            connectbutton.setIcon(R.drawable.disconnect);
        }
        else if(status==CONNECTING)
        {
            Toast.makeText(this,"Trying to connect",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(connectbutton!=null) {
            if (UserDetails.connectedTo.equals(friend))
                toggleConnectButton(DISCONNECT);
            else if (UserDetails.connectedTo.isEmpty())
                toggleConnectButton(CONNECT);
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        OnlineStatusHandler.setOnline();
        OnlineStatusHandler.goingToBackGround = true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        OnlineStatusHandler.goingToBackGround = false;
    }

    private void connection(){
        if (status == CONNECT) {
            if(UserDetails.connectedTo.isEmpty())
            {
                Query query = friendRef.child("connection").orderByKey();
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.child("connectedto").getValue().toString().isEmpty()) {
                            Toast.makeText(SentOrRecievedActivity.this, friend + " is busy", Toast.LENGTH_SHORT).show();
                        } else {
                            Map<String, String> map = new HashMap<>();
                            map.put("id", mAuth.getCurrentUser().getUid());
                            map.put("username", mAuth.getCurrentUser().getDisplayName());
                            friendRef.child("connection_requests").push().setValue(map).
                                    addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                toggleConnectButton(CONNECTING);
                                            }
                                        }
                                    });
                        }

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                });
            }else
            {
                Toast.makeText(SentOrRecievedActivity.this,"Connected to "+
                        UserDetails.connectedTo+", Disconnect first", Toast.LENGTH_SHORT).show();
            }


        }
        else if(status==DISCONNECT)
        {
            Query query = friendRef.child("connection").orderByKey();
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if( dataSnapshot.child("connectedto").getValue().equals(mAuth.getCurrentUser().getDisplayName()))
                    {

                        friendRef.child("connection").child("connectedto").setValue("")
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {

                                            myRef.child("connection").child("connectedto")
                                                    .setValue("")
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful())
                                                            {
                                                                UserDetails.connectedTo="";
                                                                Toast.makeText(SentOrRecievedActivity.this,
                                                                        "disconnected",Toast.LENGTH_SHORT)
                                                                        .show();
                                                                toggleConnectButton(CONNECT);
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
    private String getTimeIn12HourFormat(String timpestamp){
        Long time = Long.parseLong(timpestamp);
        Timestamp timestamp = new Timestamp(time);
        Date date = new Date(timestamp.getTime());
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        return dateFormat.format(date);
    }

    private void scrollToBottom() {
        new Thread(){
            public void run(){
                try {
                    Thread.sleep(50);
                    View lastChild = scrollView.getChildAt(scrollView.getChildCount() - 1);
                    int bottom = lastChild.getBottom() + scrollView.getPaddingBottom();
                    int sy = scrollView.getScrollY();
                    int sh = scrollView.getHeight();
                    int delta = bottom - (sy + sh);
                    scrollView.smoothScrollBy(0, delta);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(OnlineStatusHandler.goingToBackGround)
        OnlineStatusHandler.setOffline();
    }
}
