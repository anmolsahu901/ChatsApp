package com.example.chatsapp.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatsapp.ChatActivity;
import com.example.chatsapp.Modals.User;
import com.example.chatsapp.R;
import com.example.chatsapp.databinding.FriendsRvItemBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class FriendsAdapter extends  RecyclerView.Adapter<FriendsAdapter.ViewHolder>{


    Context context;
    ArrayList<User> list;

    public FriendsAdapter(){

    }

    public FriendsAdapter(Context context, ArrayList<User> list) {
        this.context = context;
        this.list = list;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friends_rv_item,parent,false);


        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = list.get(position);

        holder.binding.username.setText(user.getName());
        Glide.with(context).load(user.getProfileimg())  //context && image to be insert
                .placeholder(R.drawable.avatar)  //default image if no image url
                .into(holder.binding.profile);   // where to insert

        //this was done after the work in chat activity coding was finished so that we can get last message and time
        String senderId = FirebaseAuth.getInstance().getUid();

        String senderRoom = senderId+user.getUid();

        FirebaseDatabase.getInstance().getReference()
                .child("Chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){

                        String lastmsg = snapshot.child("lastMsg").getValue(String.class);
                        try{
                            long timeinmilliseconds = snapshot.child("lastMsgTime").getValue(Long.class);
                            @SuppressLint("SimpleDateFormat") DateFormat format = new SimpleDateFormat("HH:mm");
                            Calendar calender = Calendar.getInstance();
                            calender.setTimeInMillis(timeinmilliseconds);

                            holder.binding.messagetime.setText(""+format.format(calender.getTime()));


                        }
                        catch (Exception e){

                        }

                        holder.binding.lastmessage.setText(lastmsg);
                        }
                        else
                        {
                            holder.binding.lastmessage.setText("Tap to chat");

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        //on clicking any item in recyclerview
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("name",user.getName());
                intent.putExtra("image",user.getProfileimg());
                intent.putExtra("uid",user.getUid());
                intent.putExtra("token",user.getToken());

                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        FriendsRvItemBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = FriendsRvItemBinding.bind(itemView);

        }


    }
}
