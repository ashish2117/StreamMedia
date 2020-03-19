package com.ashish2117.user.streammedia.adapter;

import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ClipboardManager;

import android.net.Uri;

import com.ashish2117.user.streammedia.listener.ItemClickListener;
import com.ashish2117.user.streammedia.util.OnlineStatusHandler;
import com.ashish2117.user.streammedia.R;
import com.ashish2117.user.streammedia.util.UserDetails;
import com.ashish2117.user.streammedia.activity.GroupPlayerActivity;
import com.ashish2117.user.streammedia.activity.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InboxAdapter  extends RecyclerView.Adapter<InboxAdapter.ViewHolder> {

    ArrayList<String> titles;
    ArrayList<String> artists;
    ArrayList<String> albums;
    ArrayList<String> durations;
    ArrayList<String> urls;
    Context context;
    String friend;
    FirebaseAuth mAuth;
    DatabaseReference databaseReference;

    public InboxAdapter(String friend,ArrayList<String> titles, ArrayList<String> artists,
                        ArrayList<String> albums, ArrayList<String> durations,
                        ArrayList<String> urls, Context context) {
        Collections.reverse(titles);
        Collections.reverse(albums);
        Collections.reverse(durations);
        Collections.reverse(urls);
        Collections.reverse(artists);
        this.friend=friend;
        this.titles = titles;
        this.artists = artists;
        this.albums = albums;
        this.durations = durations;
        this.urls = urls;
        this.context = context;
        mAuth=FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.inbox_item, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.mtitle=titles.get(position);
        holder.ttitle.setText(holder.mtitle);
        holder.martist=artists.get(position);
        holder.malbum=albums.get(position);
        holder.murl=urls.get(position);
        holder.mduration=durations.get(position);

        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                Intent i=new Intent(context, MainActivity.class);
                i.putExtra("URL",holder.murl);
                i.putExtra("Title",holder.mtitle);
                i.putExtra("Album",holder.malbum);
                i.putExtra("Artist",holder.martist);
                i.putExtra("Duration",holder.mduration);
                i.putExtra("friend",friend);
                OnlineStatusHandler.goingToBackGround = false;
                context.startActivity(i);
            }

            @Override
            public void onLongPress(View view, final int position) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(context, view);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.longpress_inbox_item_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId()==R.id.copy)
                        {
                            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("url", urls.get(position));
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(context,"URL copied",Toast.LENGTH_SHORT).show();
                        }
                        else if(item.getItemId()==R.id.download) {
                            Uri uri = Uri.parse(urls.get(position)); // missing 'http://' will cause crashed
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            OnlineStatusHandler.goingToBackGround = false;
                            context.startActivity(intent);
                        }
                        else if(item.getItemId()==R.id.details)
                        {
                            Dialog dialog=new Dialog(context);
                            int secs=Integer.parseInt(durations.get(position))/1000;
                            int min=secs/60;
                            int sec=secs%60;
                            dialog.setContentView(R.layout.dialogue_details_layout);
                            TextView title=(TextView)dialog.findViewById(R.id.dialogue_title);
                            TextView album=(TextView)dialog.findViewById(R.id.dialogue_album);
                            TextView artist=(TextView)dialog.findViewById(R.id.dialogue_artist);
                            TextView duration=(TextView)dialog.findViewById(R.id.dialogue_duration);
                            title.setText("Tilte     : "+titles.get(position));
                            album.setText("Album     : "+albums.get(position));
                            artist.setText("Artist(s) : "+artists.get(position));
                            duration.setText("Duration  : "+min+" mins "+sec+" secs");
                            dialog.show();
                        }

                        return true;
                    }
                });
                popup.show();
            }
        });

        holder.groupPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if(UserDetails.connectedTo.equals(friend)) {

                   Map<String, String> map = new HashMap<>();
                   map.put("url", holder.murl);
                   map.put("title", holder.mtitle);
                   map.put("album", holder.malbum);
                   map.put("artist", holder.martist);
                   map.put("duration", holder.mduration);
                   map.put("friend", mAuth.getCurrentUser().getDisplayName());
                   databaseReference = FirebaseDatabase.getInstance().
                           getReferenceFromUrl("https://stream-audio-527dd.firebaseio.com/users");
                   databaseReference.child(friend).child("play_request")
                           .push().setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                       @Override
                       public void onComplete(@NonNull Task<Void> task) {
                           String instantbuf="true";
                           Intent i = new Intent(context, GroupPlayerActivity.class);
                           i.putExtra("URL", holder.murl);
                           i.putExtra("Title", holder.mtitle);
                           i.putExtra("Album", holder.malbum);
                           i.putExtra("Artist", holder.martist);
                           i.putExtra("Duration", holder.mduration);
                           i.putExtra("friend", friend);
                           i.putExtra("instantbuf",instantbuf);
                           OnlineStatusHandler.goingToBackGround = false;
                           context.startActivity(i);
                       }
                   });

               }
               else
               {
                   Toast.makeText(context,"Not Connected to "+friend,Toast.LENGTH_SHORT).show();
               }
            }
        });
    }

    @Override
    public int getItemCount() {
        return urls.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener,
                   View.OnClickListener{
        String mtitle,malbum,mduration,martist,murl;
        ImageView groupPlay;
        TextView ttitle;
        private ItemClickListener itemClickListener;
        public ViewHolder(View itemView) {
            super(itemView);
            ttitle=itemView.findViewById(R.id.title);
            groupPlay=itemView.findViewById(R.id.group_play);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            itemClickListener.onClick(view,getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            itemClickListener.onLongPress(view,getAdapterPosition());
            return true;
        }

        public void setItemClickListener(ItemClickListener itemClickListener)
        {
            this.itemClickListener=itemClickListener;
        }
    }
}
