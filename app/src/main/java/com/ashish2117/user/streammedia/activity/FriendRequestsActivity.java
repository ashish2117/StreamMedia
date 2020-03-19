package com.ashish2117.user.streammedia.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.ashish2117.user.streammedia.adapter.FreindRequestsRecievedAdapter;
import com.ashish2117.user.streammedia.util.OnlineStatusHandler;
import com.ashish2117.user.streammedia.R;
import com.ashish2117.user.streammedia.util.UserDetails;
import com.firebase.client.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FriendRequestsActivity extends AppCompatActivity {
    DatabaseReference friendRequests;
    FirebaseAuth mAuth;
    ArrayList<String> usernames,userIds;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);
        this.setTitle("Friend Request");
        Firebase.setAndroidContext(this);
        mAuth=FirebaseAuth.getInstance();
        userIds=new ArrayList<>();
        usernames=new ArrayList<>();
        OnlineStatusHandler.goingToBackGround = true;
        recyclerView=findViewById(R.id.requestList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        friendRequests=FirebaseDatabase.getInstance().getReference();
        Query query=friendRequests.child("friend_requests").child("received").orderByKey().equalTo(mAuth.getCurrentUser().getDisplayName());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot d:dataSnapshot.getChildren())
                {
                    Log.d("Requests",d.getValue().toString());
                    for(DataSnapshot dd:d.getChildren()){
                        usernames.add(dd.getKey());
                    }
                   // usernames.add(d.getValue().toString().split("=")[0].substring(1));
                }
                FreindRequestsRecievedAdapter freindRequestsRecievedAdapter=new FreindRequestsRecievedAdapter(usernames,
                        userIds,FriendRequestsActivity.this);
                recyclerView.setAdapter(freindRequestsRecievedAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        OnlineStatusHandler.goingToBackGround = false;
        if(UserDetails.friendJustAdded) {
            {
                OnlineStatusHandler.goingToBackGround = false;
                Intent intent=new Intent(this, Friends.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }
        else
           super.onBackPressed();

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
