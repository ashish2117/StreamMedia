package com.ashish2117.user.streammedia.activity;

/**
 * Created by user on 25-04-2018.
 */

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ashish2117.user.streammedia.App;
import com.ashish2117.user.streammedia.util.ImageLoadTask;
import com.ashish2117.user.streammedia.util.OnlineStatusHandler;
import com.ashish2117.user.streammedia.util.ProfileImageHandler;
import com.ashish2117.user.streammedia.R;
import com.ashish2117.user.streammedia.util.UserDetails;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class Chat extends AppCompatActivity {
    LinearLayout layout,bottomArea;
    ImageView sendButton;
    EditText messageArea;
    ScrollView scrollView;
    Firebase reference1;
    String friend;
    FirebaseAuth mAuth;
    boolean initialLoaded,chatOpen;
    ArrayList unreadList;
    OnChatBubbleTouchListener onChatBubbleTouchListener;
    View customView;
    URL url;
    CircleImageView emojiButton;
    LinearLayout typingStatus;
    LinearLayout typingStatusLayout;
    TypingThread typingThread;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        chatOpen=false;
        UserDetails.chatWith = "";
    }

    @Override
    protected void onPause() {
        super.onPause();
        chatOpen=false;
        UserDetails.chatWith ="";
        if(OnlineStatusHandler.goingToBackGround)
          OnlineStatusHandler.setOffline();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        OnlineStatusHandler.setOnline();
        OnlineStatusHandler.goingToBackGround = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        chatOpen=true;
        UserDetails.chatWith = friend;
        for(int i=0; i<unreadList.size();i++){
            if(isNetworkConnected()) {
                reference1.child(unreadList.get(i).toString()).child("status").setValue("read");
                unreadList.remove(i);
            }
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        OnlineStatusHandler.goingToBackGround = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.chat_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.clear_chat){
            new AlertDialog.Builder(Chat.this)
                    .setMessage("Clear entire chat?")
                    .setCancelable(true)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            reference1.removeValue(new Firebase.CompletionListener() {
                                @Override
                                public void onComplete(FirebaseError firebaseError, Firebase firebase) {

                                    layout.removeAllViews();
                                    Toast.makeText(Chat.this,"Chat Cleared",Toast.LENGTH_SHORT).show();
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
        }else if(item.getItemId() == android.R.id.home){
            finish();
        }else if(item.getItemId() == R.id.music_option){
            Intent intent=new Intent(Chat.this, SentOrRecievedActivity.class);
            intent.putExtra("friend",friend);
            OnlineStatusHandler.goingToBackGround = false;
            startActivity(intent);
        }
        return  super.onOptionsItemSelected(item);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.chat_action_bar);
        customView =getSupportActionBar().getCustomView();
        initialLoaded = false;
        chatOpen = true;
        OnlineStatusHandler.goingToBackGround = true;
        unreadList = new ArrayList<String>();
        layout = (LinearLayout) findViewById(R.id.layout1);
        sendButton = (ImageView)findViewById(R.id.sendButton);
        bottomArea = (LinearLayout) findViewById(R.id.bottomArea);
        sendButton.setEnabled(false);
        messageArea = (EditText)findViewById(R.id.messageArea);
        scrollView = (ScrollView)findViewById(R.id.scrollView);
        emojiButton = findViewById(R.id.emojiButton);
        typingStatus = findViewById(R.id.typingStatus);
        typingStatusLayout = findViewById(R.id.typingStatusLayout);
        typingStatusLayout.removeView(typingStatus);
        Bundle b=getIntent().getExtras();
        friend=b.getString("friend");
        try{
            if(b.getString("started_from").equals("notification_tap")){
                OnlineStatusHandler.setOnline();
            }
        }catch (Exception e){

        }
        ((TextView)customView.findViewById(R.id.usernameText)).setText(friend);
        ((Button)customView.findViewById(R.id.backButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnlineStatusHandler.goingToBackGround = false;
                finish();
            }
        });
        typingThread = new TypingThread();
        typingThread.start();
        UserDetails.chatWith = friend;
        onChatBubbleTouchListener = new OnChatBubbleTouchListener(this);
        Firebase.setAndroidContext(this);
        mAuth = FirebaseAuth.getInstance();
        if(friend.compareTo(mAuth.getCurrentUser().getDisplayName())>0)
            reference1 = new Firebase("https://stream-audio-527dd.firebaseio.com/messages/" + mAuth
                .getCurrentUser().getDisplayName() + "_" + friend);
        else
            reference1 = new Firebase("https://stream-audio-527dd.firebaseio.com/messages/" + friend + "_" + mAuth
        .getCurrentUser().getDisplayName());

        FirebaseDatabase.getInstance().getReference().child("users").child(friend).child("online_status").
                addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue().toString().equals("online"))
                         ((TextView)customView.findViewById(R.id.onlineStatusText)).setText("Online");
                        else{
                            String lastSeen = getTimeIn12HourFormat(dataSnapshot.getValue().toString());
                            ((TextView)customView.findViewById(R.id.onlineStatusText)).setText("Last Seen " + lastSeen);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        setProfilePicture();

        scrollView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                scrollToBottom();
            }
        });

        FirebaseDatabase.getInstance().getReference().child("users")
                .child(friend).child("profile_pic_uri")
                .addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                SharedPreferences preferences = Chat.this.getSharedPreferences("ProfilePicUris",Context.MODE_PRIVATE);
                String oldurl = preferences.getString(friend + ".jpg","null");
                if(oldurl.equals(dataSnapshot.getValue().toString()))
                    return;
                else {
                      new ImageLoadTask(dataSnapshot.getValue().toString(), (ImageView) customView.findViewById(R.id.profileImage),
                            getApplicationContext(), friend + ".jpg")
                            .execute();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        messageArea.addTextChangedListener(new TextWatcher() {
            int count = 0;
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(count ==0){
                    FirebaseDatabase.getInstance().getReference().child("users").child(friend)
                            .child("friends").child(mAuth.getCurrentUser().getDisplayName()).child("typing")
                            .setValue("true");
                    typingThread.count = 0;
                    count++;
                }else if(count == 3)
                    count = 0;
                else
                    count++;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageArea.getText().toString();
                if(!messageText.equals("")){
                    final Map<String, String> map = new HashMap();
                    map.put("message", messageText);
                    map.put("user", mAuth.getCurrentUser().getDisplayName());
                    map.put("status","sent");
                    Long time = System.currentTimeMillis();
                    map.put("timestamp",String.valueOf(time));
                    LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lp2.weight = 1.0f;
                    LayoutInflater inflater = LayoutInflater.from(Chat.this);
                    final LinearLayout linearLayout =(LinearLayout) inflater.inflate(R.layout.chat_bubble_right,null,false) ;
                        TextView messageTextView = linearLayout.findViewById(R.id.messageTextView);
                        TextView timeStamTextView = linearLayout.findViewById(R.id.timeStampTextView);
                        timeStamTextView.setText(getTimeIn12HourFormat(String.valueOf(time)));
                        messageTextView.setText(messageText);
                        messageTextView.setPadding(20,20,20,20);
                        messageTextView.setTextColor(Color.parseColor("#ffffff"));
                        lp2.gravity = Gravity.RIGHT;
                        lp2.setMargins(100,0,0,20);
                        linearLayout.setBackgroundResource(R.drawable.roundleft);
                    linearLayout.setLayoutParams(lp2);
                    linearLayout.setOnTouchListener(onChatBubbleTouchListener);
                    layout.addView(linearLayout);
                    reference1.push().setValue(map, new Firebase.CompletionListener(){
                        @Override

                                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                    ImageView imageView = linearLayout.findViewById(R.id.messageStatusImageView);
                                    imageView.setImageResource(R.drawable.ic_done_grey_24dp);
                                }
                    });
                    messageArea.setText("");
                    FirebaseDatabase.getInstance().getReference().child("users").child(friend)
                            .child("friends").child(mAuth.getCurrentUser().getDisplayName()).child("typing")
                            .setValue("false");
                }


            }
        });

        FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getDisplayName())
                .child("friends").child(friend).child("typing")
                .addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                 if(dataSnapshot.getValue() != null) {
                     if (dataSnapshot.getValue().toString().equals("true")) {
                         typingStatusLayout.addView(typingStatus);
                     } else if (dataSnapshot.getValue().toString().equals("false")) {
                         typingStatusLayout.removeView(typingStatus);
                     }
                 }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        customView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnlineStatusHandler.goingToBackGround = false;
                Intent intent=new Intent(Chat.this, FriendProfileActivity.class);
                intent.putExtra("friend",friend);
                startActivity(intent);
            }
        });

        reference1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String id = dataSnapshot.getKey();
                Map map = dataSnapshot.getValue(Map.class);
                String message = map.get("message").toString();
                String userName = map.get("user").toString();
                String status = map.get("status").toString();
                String time = getTimeIn12HourFormat(map.get("timestamp").toString());
                if(!userName.equals(mAuth.getCurrentUser().getDisplayName())){
                    addMessageBox(message, status, 2,id,time);
                }else if(!initialLoaded)
                    addMessageBox(message,status,1,id,time);
                else {
                    for (int i = layout.getChildCount()-1;i>=0;i--){
                        LinearLayout l= (LinearLayout)layout.getChildAt(i);
                        TextView view = l.findViewById(R.id.messageId);
                        if(view.getText().toString().isEmpty()){
                            view.setText(id);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);
                String key = dataSnapshot.getKey();
                String userName = map.get("user").toString();
                String status = map.get("status").toString();
                 if(initialLoaded && status.equals("read") && userName.equals(mAuth.getCurrentUser().getDisplayName())){
                     for(int i=layout.getChildCount()-1;i>=0;i--){
                    LinearLayout l=(LinearLayout) layout.getChildAt(i);
                    if(((TextView)l.findViewById(R.id.messageId)).getText().toString().equals(key)){
                        ((ImageView)l.findViewById(R.id.messageStatusImageView)).setImageResource(R.drawable.ic_done_all_grey_24dp);
                        break;
                    }

                }
                 }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Map map = dataSnapshot.getValue(Map.class);
                String key = dataSnapshot.getKey();
                String userName = map.get("user").toString();
                String status = map.get("status").toString();
                for(int i=layout.getChildCount()-1;i>=0;i--){
                    LinearLayout l=(LinearLayout) layout.getChildAt(i);
                    if(((TextView)l.findViewById(R.id.messageId)).getText().toString().equals(key)){
                        layout.removeView(l);
                    }

                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        reference1.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!initialLoaded) {
                    sendButton.setEnabled(true);
                    initialLoaded = true;
                    Log.d("chhh",dataSnapshot.toString());
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void setProfilePicture() {
        final CircleImageView profileImage = customView.findViewById(R.id.profileImage);
        if(ProfileImageHandler.isImageExists(Chat.this,friend + ".jpg")){
            ProfileImageHandler.setImage(friend + ".jpg",profileImage,Chat.this);
            return;
        }
        FirebaseDatabase.getInstance().getReference().child("users").child(friend).child("profile_pic_uri")
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue()!=null) {
                            ImageLoadTask loadTask = new ImageLoadTask(dataSnapshot.getValue().toString(), profileImage,Chat.this,friend + ".jpg");
                            loadTask.execute();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void scrollToBottom() {
        View lastChild = scrollView.getChildAt(scrollView.getChildCount() - 1);
        int bottom = lastChild.getBottom() + scrollView.getPaddingBottom();
        int sy = scrollView.getScrollY();
        int sh = scrollView.getHeight();
        int delta = bottom - (sy + sh);
        scrollView.smoothScrollBy(0, delta);
    }

    public void addMessageBox(String message, String status, int type, String id,String timestamp){
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.weight = 1.0f;
        LinearLayout linearLayout=null;
        if(type == 2) {
           linearLayout =(LinearLayout) inflater.inflate(R.layout.chat_bubble_left,null,false) ;
            TextView textView = linearLayout.findViewById(R.id.messageTextView);
            TextView timeStampTextView = linearLayout.findViewById(R.id.timeStampTextView);
            timeStampTextView.setText(timestamp);
            textView.setText(message);
            textView.setPadding(20,20,20,20);
            textView.setTextColor(Color.parseColor("#ffffff"));
            lp2.gravity = Gravity.LEFT;
            lp2.setMargins(0,0,100,20);
            linearLayout.setBackgroundResource(R.drawable.roundright);
            if(status.equals("sent")) {
                if(chatOpen)
                  reference1.child(id).child("status").setValue("read");
                else
                    unreadList.add(id);
            }
        }
        else if(!initialLoaded){
            linearLayout =(LinearLayout) inflater.inflate(R.layout.chat_bubble_right,null,false) ;
            TextView textView = linearLayout.findViewById(R.id.messageTextView);
            TextView timeStampTextView = linearLayout.findViewById(R.id.timeStampTextView);
            timeStampTextView.setText(timestamp);
            textView.setText(message);
            textView.setPadding(20,20,20,20);
            textView.setTextColor(Color.parseColor("#ffffff"));
            ImageView imageView = linearLayout.findViewById(R.id.messageStatusImageView);
            lp2.gravity = Gravity.RIGHT;
            if(status.equals("sent")) {
                imageView.setImageResource(R.drawable.ic_done_grey_24dp);
            }
            else if(status.equals("read"))
                imageView.setImageResource(R.drawable.ic_done_all_grey_24dp);
            lp2.setMargins(100,0,0,20);
            linearLayout.setBackgroundResource(R.drawable.roundleft);
        }else
            return;
        TextView messageId = linearLayout.findViewById(R.id.messageId);
        messageId.setText(id);
        linearLayout.setLayoutParams(lp2);
        linearLayout.setOnTouchListener(onChatBubbleTouchListener);
        layout.addView(linearLayout);
    }
    public void createNotification(String title, String chat,String text)
    {
        NotificationCompat.Builder b=new NotificationCompat.Builder(this, App.CHANNEL_ID);
        Intent intent=new Intent(this,Chat.class);
       friend=title;
        friend = chat;
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
        b.setContentIntent(pIntent);
        b.setContentTitle(chat);
        b.setSmallIcon(R.drawable.abc);
        b.setContentText(text);
        b.setAutoCancel(true);
        NotificationManager n=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        n.notify(0,b.build());
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }



    private class OnChatBubbleTouchListener implements View.OnTouchListener {

        private final GestureDetector gestureDetector;
        LinearLayout bubble;
        Context context;
        public OnChatBubbleTouchListener(Context ctx){
            gestureDetector = new GestureDetector(ctx, new GestureListener());
            context = ctx;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            bubble = (LinearLayout) v;
            return gestureDetector.onTouchEvent(event);
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = 50;
            private static final int SWIPE_VELOCITY_THRESHOLD = 50;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                ImageView imageView = bubble.findViewById(R.id.messageStatusImageView);
                if(imageView != null) {
                    new AlertDialog.Builder(Chat.this)
                            .setMessage("Delete this message?")
                            .setCancelable(true)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String messageId = ((TextView)bubble.findViewById(R.id.messageId)).getText().toString();
                                    reference1.child(messageId).removeValue(new Firebase.CompletionListener() {
                                        @Override
                                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                            if(firebaseError == null)
                                                layout.removeView(bubble);
                                            else
                                                Toast.makeText(context,"Error Occured",Toast.LENGTH_SHORT).show();
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
                return super.onDoubleTap(e);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight(diffX);
                            } else {
                                onSwipeLeft(diffX);
                            }
                            result = true;
                        }
                    }
                    else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeBottom();
                        } else {
                            onSwipeTop();
                        }
                        result = true;
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return result;
            }
        }

        public void onSwipeRight(float diffX) {
            TextView textView = bubble.findViewById(R.id.messageTextView);
            Toast.makeText(context, "Swipe to reply is coming soon", Toast.LENGTH_SHORT).show();
        }

        public void onSwipeLeft(float diffX) {

        }

        public void onSwipeTop() {
        }

        public void onSwipeBottom() {
        }
    }
    private String getTimeIn12HourFormat(String timpestamp){
        Long time = Long.parseLong(timpestamp);
        Timestamp timestamp = new Timestamp(time);
        Date date = new Date(timestamp.getTime());
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        return dateFormat.format(date);
    }

   private  class TypingThread extends Thread{
        int count = 0;
       @Override
       public void run() {
           while (true) {
               try {
                   Thread.sleep(100);
                   count++;
                   if(count == 20){
                       FirebaseDatabase.getInstance().getReference().child("users").child(friend)
                               .child("friends").child(mAuth.getCurrentUser().getDisplayName()).child("typing")
                               .setValue("false");
                       count = 0;
                   }
               } catch (InterruptedException e) {

               }
           }

       }
   }

}
