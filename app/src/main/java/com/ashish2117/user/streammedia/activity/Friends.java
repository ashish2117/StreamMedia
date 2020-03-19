package com.ashish2117.user.streammedia.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.support.v7.widget.SearchView;
import android.widget.Toast;
import java.util.Iterator;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ashish2117.user.streammedia.adapter.FriendListAdapter;
import com.ashish2117.user.streammedia.util.ImageLoadTask;
import com.ashish2117.user.streammedia.service.NotificationService;
import com.ashish2117.user.streammedia.util.OnlineStatusHandler;
import com.ashish2117.user.streammedia.util.ProfileImageHandler;
import com.ashish2117.user.streammedia.R;
import com.ashish2117.user.streammedia.adapter.SearchAdapter;
import com.ashish2117.user.streammedia.util.UserDetails;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by user on 09-08-2018.
 */

public class Friends extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<String> usernames;
    ArrayList<String> names, friends;
    DatabaseReference reference;
    FirebaseAuth mAuth;
    SearchAdapter searchAdapter;
    ImageView imageView;
    CircleImageView profileImage;
    Button optionsButton;
    SearchView searchView;
    boolean backPressedOnce;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_user);
        recyclerView=findViewById(R.id.userList);
        profileImage = findViewById(R.id.profileImage);
        optionsButton = findViewById(R.id.optionsButton);
        searchView = findViewById(R.id.searchView);
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                recyclerView.removeAllViews();
                loadfriends();
                return false;
            }
        });

        backPressedOnce = false;

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(!s.trim().isEmpty()){
                    setAdapter(s);
                }
                return false;
            }


            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        OnlineStatusHandler.goingToBackGround = true;
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnlineStatusHandler.goingToBackGround = false;
                startActivity(new Intent(Friends.this, ProfileActivity.class));
            }
        });
        OnlineStatusHandler.setOnline();
        recyclerView.setHasFixedSize(true);
        usernames=new ArrayList<>();
        names=new ArrayList<>();
        friends=new ArrayList<>();
        Firebase.setAndroidContext(this);
        setTitle("Friends");
        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final PopupMenu popup = new PopupMenu(Friends.this, view);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.searchview, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId()==R.id.logout)
                        {
                            reference.child(mAuth.getCurrentUser().getDisplayName()).child("connection").
                                    child("connectedto").setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        OnlineStatusHandler.goingToBackGround = false;
                                        mAuth.signOut();
                                        Intent intent=new Intent(Friends.this, Login.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });


                        }
                        else if(item.getItemId()==R.id.requests)
                        {
                            OnlineStatusHandler.goingToBackGround = false;
                            Intent intent=new Intent(Friends.this, FriendRequestsActivity.class);
                            startActivity(intent);
                        }
                        else if(item.getItemId()==R.id.requests_sent)
                        {
                            OnlineStatusHandler.goingToBackGround = false;
                            Intent intent=new Intent(Friends.this, FriendRequestSentActivity.class);
                            startActivity(intent);
                        }

                        return  true;
                    }
                });
                popup.show();

            }
        });
        mAuth=FirebaseAuth.getInstance();
        startService(new Intent(this, NotificationService.class));
        //reference= new Firebase("https://stream-audio-527dd.firebaseio.com/");
        reference= FirebaseDatabase.getInstance().getReferenceFromUrl("https://stream-audio-527dd.firebaseio.com/users");
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        UserDetails.username=mAuth.getCurrentUser().getDisplayName();
        setProfilePicture();
        loadfriends();
    }

    private void setProfilePicture() {
        if(ProfileImageHandler.isImageExists(this,mAuth.getCurrentUser().getDisplayName() + ".jpg")){
            ProfileImageHandler.setImage(mAuth.getCurrentUser().getDisplayName() + ".jpg",profileImage,this);
            return;
        }
        FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getDisplayName())
                .child("profile_pic_uri")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue()!=null) {
                            ImageLoadTask loadTask = new ImageLoadTask(dataSnapshot.getValue().toString(), profileImage, Friends.this, mAuth.getCurrentUser().getDisplayName() + ".jpg");
                            loadTask.execute();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void loadfriends() {
        usernames=new ArrayList<>();
        names=new ArrayList<>();
        friends=new ArrayList<>();
        UserDetails.friendJustAdded=false;
        final ProgressDialog pd = new ProgressDialog(Friends.this);
        pd.setMessage("Loading...");
        pd.show();

        String url = "https://stream-audio-527dd.firebaseio.com/users/"+UserDetails.username+"/friends.json";
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONObject obj = new JSONObject(s);
                    Iterator<String> iterator= obj.keys();
                    while (iterator.hasNext())
                    {
                        friends.add(iterator.next());
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                FriendListAdapter adapter=new FriendListAdapter(Friends.this,friends);
                UserDetails.friends=friends;
                recyclerView.setAdapter(adapter);
                pd.dismiss();
            }

        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println("" + volleyError );
                pd.dismiss();
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(Friends.this);
        rQueue.add(request);
    }



    private void setAdapter(final String s) {
        usernames.clear();
        names.clear();
        recyclerView.removeAllViews();
        /*reference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            int counter=0;
            String username,name;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot d :dataSnapshot.getChildren())
                {
                    if(d.exists()) {
                        username = d.getKey();
                        name = d.child("name").getValue().toString();
                        if (username.equals(UserDetails.username))
                            continue;
                        if (username.contains(s) && !usernames.contains(username)) {
                            usernames.add(username);
                            names.add(name);
                            counter++;
                        }
                        if (counter == 15)
                            break;
                        searchAdapter = new SearchAdapter(Friends.this, usernames, names);
                        recyclerView.setAdapter(searchAdapter);
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });*/
        com.google.firebase.database.Query query=reference.orderByKey().startAt(s).endAt(s+"\uf88f");
        query.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                int counter=0;
                String username,name;
                for(com.google.firebase.database.DataSnapshot d: dataSnapshot.getChildren())
                {
                    username=d.getKey();
                    name = d.child("name").getValue().toString();
                    if (username.equals(UserDetails.username))
                        continue;
                    if (username.contains(s) && !usernames.contains(username)) {
                        usernames.add(username);
                        names.add(name);
                        counter++;
                    }
                    if (counter == 15)
                        break;
                }
                searchAdapter = new SearchAdapter(Friends.this, usernames, names);
                recyclerView.setAdapter(searchAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if(UserDetails.friendJustAdded)
            loadfriends();
        if(backPressedOnce)
          finish();
        else {
            backPressedOnce = true;
            Toast.makeText(this,"Please press BACK again to exit",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        OnlineStatusHandler.goingToBackGround =true;
        OnlineStatusHandler.setOnline();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(OnlineStatusHandler.goingToBackGround)
        OnlineStatusHandler.setOffline();
    }

    @Override
    protected void onResume() {
        super.onResume();
        backPressedOnce = false;
    }
}
