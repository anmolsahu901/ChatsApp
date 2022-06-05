package com.example.chatsapp.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatsapp.Modals.MessageModal;
import com.example.chatsapp.Modals.User;
import com.example.chatsapp.R;
import com.example.chatsapp.databinding.RecieveRvItemBinding;
import com.example.chatsapp.databinding.RecieveRvItemGrpBinding;
import com.example.chatsapp.databinding.SentRvItemBinding;
import com.example.chatsapp.databinding.SentRvItemGrpBinding;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupMessagesAdapter extends RecyclerView.Adapter{

    Context context;
    ArrayList<MessageModal> messages;

    final int ITEM_SENT = 1,ITEM_RECIEVE=2;


    public GroupMessagesAdapter() {

    }

    public GroupMessagesAdapter(Context context, ArrayList<MessageModal> messages) {
        this.context = context;
        this.messages = messages;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==ITEM_SENT){
            View view = LayoutInflater.from(context).inflate(R.layout.sent_rv_item_grp,parent,false);
            return new SendViewHolder(view);
        }
        else{
            View view = LayoutInflater.from(context).inflate(R.layout.recieve_rv_item_grp,parent,false);
            return new RecieverViewHolder(view);
        }

    }


    // more than 1 view
    //return the view type of the item at position for the purposes of the view recyclinG
    @Override
    public int getItemViewType(int position) {
        MessageModal message = messages.get(position);
        //IF SENDER ID == CURRENT USER ID THAT MEANS MESSAGE IS SENT BY USER.....ELSE RECIEVE
        if(FirebaseAuth.getInstance().getUid().equals(message.getSenderId())){
            return ITEM_SENT;
        }
        else{
            return ITEM_RECIEVE;
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageModal messageModal = messages.get(position);


        //reactions-----------
//        step1 : creating the object for icons or feelings
        int reactions[] = new int[]{
                R.drawable.ic_like,
                R.drawable.ic_love,
                R.drawable.ic_laugh,
                R.drawable.ic_smile,
                R.drawable.ic_cry,
                R.drawable.ic_angry
        };

        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();
        ///-------step1 ends

        //step2:just copy paste the function
        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {

            if(pos<0)
                return false;

            //step 4:-----displaying the feeling selected
            if (holder.getClass() == SendViewHolder.class) {
                //display feeling in feelingimageview for sender view when ontouch of chat sent
                //here visiblity is gone bydefault so set up visible also
                SendViewHolder viewHolder = (SendViewHolder) holder;
                viewHolder.binding.feeling.setImageResource(reactions[pos]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }
            else
            {
                //display feeling in feelingimageview for receiver view when ontouch of chat receive
                //here visiblity is gone bydefault so set up visible also
                RecieverViewHolder viewHolder = (RecieverViewHolder) holder;
                viewHolder.binding.feeling.setImageResource(reactions[pos]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);

            }

            messageModal.setFeeling(pos);

            //step 5: storing the feeling in database
            //updating firebase database with feelings value in chat->both-rooms->messages->messageid->(feelings)
            FirebaseDatabase.getInstance().getReference()
                    .child("public")
                    .child(messageModal.getMessageId())
                    .setValue(messageModal);

            return true; // true is closing popup, false is requesting a new selection
        });


        //here ... we are setting up the values....
        if(holder.getClass()==SendViewHolder.class){
            SendViewHolder viewHolder = (SendViewHolder)holder;
            viewHolder.binding.message.setText(messageModal.getMessage());
            //here i can also update message timing but first create TV for time display in rv_items
            //then here you can set time but first change milliseconds to HH:MM

            if(messageModal.getMessage().equals("photo")){
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                Glide.with(context).load(messageModal.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(viewHolder.binding.image);
            }

            FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(messageModal.getSenderId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                User user = snapshot.getValue(User.class);
                                viewHolder.binding.name.setText(user.getName());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            //step 6: if already their is feeling for a message diplay it
            //from this all other message feelings will be displayed which were already setup by some feelings before
            if(messageModal.getFeeling()>= 0){
                viewHolder.binding.feeling.setImageResource(reactions[messageModal.getFeeling()]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }
            else {
                viewHolder.binding.feeling.setVisibility(View.GONE);
            }


            //sender bss reciever k messages ko reactions dega ...agar khud k message
            //me bhi reactions dene h toh neeche ka uncomment kardo
           //step3: setontouch show feelings option in chats
          viewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
              @SuppressLint("ClickableViewAccessibility")
              @Override
              public boolean onTouch(View view, MotionEvent motionEvent) {
                  popup.onTouch(view,motionEvent);
                  return false;
              }
          });

            // setontouch show feelings option in image
            viewHolder.binding.image.setOnTouchListener(new View.OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    popup.onTouch(view,motionEvent);
                    return false;
                }
            });
        }
        else{
            RecieverViewHolder viewHolder = (RecieverViewHolder)holder;
            viewHolder.binding.message.setText(messageModal.getMessage());


            if(messageModal.getMessage().equals("photo")){
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                Glide.with(context).load(messageModal.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(viewHolder.binding.image);
            }

            FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(messageModal.getSenderId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                User user = snapshot.getValue(User.class);
                                viewHolder.binding.name.setText(user.getName());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
            //step 6: if already their is feeling for a message diplay it
            //from this all other message feelings will be displayed which were already setup by some feelings before
            if(messageModal.getFeeling()>= 0){
                viewHolder.binding.feeling.setImageResource(reactions[messageModal.getFeeling()]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }
            else {
                viewHolder.binding.feeling.setVisibility(View.GONE);
            }

            //step3: setontouch show feelings option in chats
            viewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    popup.onTouch(view, motionEvent);

                    return false;
                }
            });

            // setontouch show feelings option in image
            viewHolder.binding.image.setOnTouchListener(new View.OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    popup.onTouch(view,motionEvent);
                    return false;
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class SendViewHolder extends RecyclerView.ViewHolder{
        SentRvItemGrpBinding binding;

        public SendViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = SentRvItemGrpBinding.bind(itemView);
        }
    }

    public class RecieverViewHolder extends RecyclerView.ViewHolder{

        RecieveRvItemGrpBinding binding;

        public RecieverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RecieveRvItemGrpBinding.bind(itemView);
        }
    }

}
