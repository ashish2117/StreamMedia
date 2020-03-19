package com.ashish2117.user.streammedia.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ashish2117.user.streammedia.R;
import com.ashish2117.user.streammedia.util.UserDetails;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Created by user on 09-08-2018.
 */

public class SearchAdapter extends Adapter<SearchAdapter.SearchViewHolder> {
    Context context;
    ArrayList<String> userNames,names;
    Firebase reference;
    DatabaseReference addNewFriendRef,friendRequestRef;
    String addFriendUserId;
    FirebaseAuth mAuth;
    class SearchViewHolder extends RecyclerView.ViewHolder{
        TextView name,username;
        ImageView imageView;
        public SearchViewHolder(View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.name);
            username=itemView.findViewById(R.id.username);
            imageView=itemView.findViewById(R.id.add);
        }
    }
    public SearchAdapter(Context context, ArrayList<String> userNames, ArrayList<String> names) {
        this.context = context;
        this.userNames = userNames;
        this.names=names;
        mAuth=FirebaseAuth.getInstance();
        Firebase.setAndroidContext(context);
        reference=new Firebase("https://stream-audio-527dd.firebaseio.com/users/"+ UserDetails.username+"/friends");
    }

    @NonNull
    @Override
    public SearchAdapter.SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.search_item,parent,false);
        return new SearchAdapter.SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SearchViewHolder holder, final int position) {
        holder.name.setText(names.get(position));
        holder.username.setText(userNames.get(position));
        if(UserDetails.friends.contains(userNames.get(position)))
            holder.imageView.setImageResource(R.drawable.ic_check_circle_black_24dp);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!UserDetails.friends.contains(userNames.get(position))) {

                            friendRequestRef=FirebaseDatabase.getInstance().getReference().child("friend_requests");
                            friendRequestRef.child("sent").child(mAuth.getCurrentUser().getDisplayName()).child(userNames.get(position)).child("request_type")
                                    .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        friendRequestRef.child("received").child(userNames.get(position)).child(mAuth.getCurrentUser().getDisplayName()).child("request_type")
                                                .setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(context, "Request sent", Toast.LENGTH_SHORT).show();
                                                    holder.imageView.setImageResource(R.drawable.ic_check_circle_black_24dp);
                                                    holder.imageView.setEnabled(false);
                                                }

                                            }

                                        });
                                    }
                        }


                    });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return userNames.size();
    }
}
