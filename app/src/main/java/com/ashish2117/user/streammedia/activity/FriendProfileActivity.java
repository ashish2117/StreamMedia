package com.ashish2117.user.streammedia.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.ashish2117.user.streammedia.util.OnlineStatusHandler;
import com.ashish2117.user.streammedia.util.ProfileImageHandler;
import com.ashish2117.user.streammedia.R;

public class FriendProfileActivity extends AppCompatActivity {

    ImageView profileImage;
    Button backButton;
    String friend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);
        profileImage = findViewById(R.id.profileImage);
        backButton = findViewById(R.id.backButton);
        Bundle b=getIntent().getExtras();
        friend=b.getString("friend");
        OnlineStatusHandler.goingToBackGround = true;

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnlineStatusHandler.goingToBackGround = false;
                finish();
            }
        });

        ProfileImageHandler.setImage(friend + ".jpg" , profileImage , FriendProfileActivity.this);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(OnlineStatusHandler.goingToBackGround)
            OnlineStatusHandler.setOffline();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        OnlineStatusHandler.setOnline();
        OnlineStatusHandler.goingToBackGround = true;
    }
}
