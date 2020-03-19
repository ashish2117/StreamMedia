package com.ashish2117.user.streammedia.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.ashish2117.user.streammedia.adapter.InboxAdapter;
import com.ashish2117.user.streammedia.util.OnlineStatusHandler;
import com.ashish2117.user.streammedia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class InboxActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    DatabaseReference databaseReference;
    String friend;
    ProgressDialog p;
    FirebaseAuth mAuth;
    ArrayList<String> titles;
    ArrayList<String> artists;
    ArrayList<String> albums;
    ArrayList<String> durations;
    ArrayList<String> urls;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_user);
        recyclerView=findViewById(R.id.userList);
        recyclerView.setHasFixedSize(true);
        OnlineStatusHandler.goingToBackGround = true;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        titles=new ArrayList<>();
        albums=new ArrayList<>();
        urls=new ArrayList<>();
        durations=new ArrayList<>();
        artists=new ArrayList<>();
        Bundle bundle=getIntent().getExtras();
        friend=bundle.getString("friend");
        mAuth=FirebaseAuth.getInstance();
        this.setTitle("Requests From "+friend);
        databaseReference= FirebaseDatabase.getInstance().getReferenceFromUrl("https://stream-audio-527dd.firebaseio.com/users/"+
        mAuth.getCurrentUser().getDisplayName()+"/friends/"+friend+"/inbox");
        p=new ProgressDialog(this);
        p.setMessage("Loading..");
        p.show();
        Query query=databaseReference.orderByKey();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot d:dataSnapshot.getChildren())
                {
                    artists.add(d.child("artist").getValue().toString());
                    albums.add(d.child("album").getValue().toString());
                    durations.add(d.child("duration").getValue().toString());
                    urls.add(d.child("url").getValue().toString());
                    titles.add(d.child("title").getValue().toString());
                }
                InboxAdapter inboxAdapter=new InboxAdapter(friend,titles,artists,albums,durations,urls,InboxActivity.this);
                recyclerView.setAdapter(inboxAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        p.dismiss();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        OnlineStatusHandler.goingToBackGround = false;
    }
}
