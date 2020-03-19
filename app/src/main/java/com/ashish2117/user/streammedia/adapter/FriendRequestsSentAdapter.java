package com.ashish2117.user.streammedia.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ashish2117.user.streammedia.R;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;

public class FriendRequestsSentAdapter extends RecyclerView.Adapter<FriendRequestsSentAdapter.ViewHolder> {

    ArrayList<String> usernames,userIds;
    Context context;
    Firebase reference,reference1;
    FirebaseAuth mAuth;
    DatabaseReference databaseReference,inboxRef;
    public FriendRequestsSentAdapter(ArrayList<String> usernames, ArrayList<String> userIds, Context context) {
        Collections.reverse(userIds);
        Collections.reverse(usernames);
        this.usernames = usernames;
        this.userIds=userIds;
        this.context = context;
        Firebase.setAndroidContext(context);
        databaseReference= FirebaseDatabase.getInstance()
                .getReferenceFromUrl("https://stream-audio-527dd.firebaseio.com/friend_requests/");
        mAuth=FirebaseAuth.getInstance();

    }

    @NonNull
    @Override
    public FriendRequestsSentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_request_item,parent,false);
        return new FriendRequestsSentAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FriendRequestsSentAdapter.ViewHolder holder, final int position) {
        holder.username.setText(usernames.get(position));
        holder.addTextView.setText("Cancel");
        holder.addTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReference.child("sent").child(mAuth.getCurrentUser().getDisplayName()
                ).child(usernames.get(position)).child("request_type").removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    databaseReference.child("received").child(usernames.get(position))
                                            .child(mAuth.getCurrentUser().getDisplayName()).child("request_type").removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()) {
                                                        Toast.makeText(context, "Request Cancelled", Toast.LENGTH_LONG).show();
                                                        holder.addTextView.setText("Cancelled");
                                                        holder.addTextView.setEnabled(false);
                                                    }
                                                }
                                            });
                                }
                            }
                        });
            }
        });
    }

    @Override
    public int getItemCount() {
        return usernames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView username,addTextView;
        public ViewHolder(View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.usernamee);
            addTextView=itemView.findViewById(R.id.addTextView);
        }
    }
}
