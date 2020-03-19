package com.ashish2117.user.streammedia.adapter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import android.content.Context;
import android.widget.Toast;

import com.ashish2117.user.streammedia.util.ImageLoadTask;
import com.ashish2117.user.streammedia.listener.ItemClickListener;
import com.ashish2117.user.streammedia.util.OnlineStatusHandler;
import com.ashish2117.user.streammedia.util.ProfileImageHandler;
import com.ashish2117.user.streammedia.R;
import com.ashish2117.user.streammedia.activity.Chat;
import com.ashish2117.user.streammedia.activity.FriendProfileActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.ViewHolder> {
    ArrayList<String> names;
    Context context;
    public FriendListAdapter(Context context, ArrayList<String> names) {
        this.names=names;
        this.context=context;
    }

    @NonNull
    @Override
    public FriendListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_item, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final  FriendListAdapter.ViewHolder holder, final int position) {
        holder.name.setText(names.get(position));
        holder.reference = FirebaseDatabase.getInstance().getReference().child("users")
                .child(names.get(position)).child("profile_pic_uri");

        if(ProfileImageHandler.isImageExists(context,names.get(position)+".jpg")) {
            ProfileImageHandler.setImage(names.get(position) + ".jpg", holder.imageView, context);
        }

        holder.reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SharedPreferences preferences = context.getSharedPreferences("ProfilePicUris",Context.MODE_PRIVATE);
                String oldurl = preferences.getString(names.get(position) + ".jpg","null");
                if(oldurl.equals(dataSnapshot.getValue().toString()))
                    return;
                else {
                    new ImageLoadTask(dataSnapshot.getValue().toString(), holder.imageView, context, names.get(position) + ".jpg")
                            .execute();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                OnlineStatusHandler.goingToBackGround = false;
                Intent intent=new Intent(context, Chat.class);
                intent.putExtra("friend",names.get(position));
                context.startActivity(intent);
            }

            @Override
            public void onLongPress(View view, int position) {
                Toast.makeText(context,"LongClicked position "+position,Toast.LENGTH_SHORT).show();
            }
        });

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnlineStatusHandler.goingToBackGround = false;
                Intent intent=new Intent(context, FriendProfileActivity.class);
                intent.putExtra("friend",names.get(position));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return names.size();

    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener ,
                                View.OnLongClickListener{
        TextView name,username;
        CircleImageView imageView;
        private  ItemClickListener itemClickListener;
        DatabaseReference reference;
        public ViewHolder(View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.name);
            imageView = itemView.findViewById(R.id.dpImageView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onClick(view,getAdapterPosition());
        }

        public void setItemClickListener(ItemClickListener itemClickListener)
        {
            this.itemClickListener=itemClickListener;
        }

        @Override
        public boolean onLongClick(View view) {
            itemClickListener.onLongPress(view,getAdapterPosition());
            return true;
        }
    }
}

