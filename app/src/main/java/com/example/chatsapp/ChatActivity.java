package com.example.chatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.chatsapp.Adapter.MessagesAdapter;
import com.example.chatsapp.Modals.MessageModal;
import com.example.chatsapp.Modals.User;
import com.example.chatsapp.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;

    ArrayList<MessageModal> messageModalArrayList;
    MessagesAdapter adapter;

    String senderRoom,recieverRoom;

    FirebaseDatabase database;
    FirebaseStorage storage;
    String senderUid,recieverUid;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String name = getIntent().getStringExtra("name");
        recieverUid = getIntent().getStringExtra("uid");
        String profileimg = getIntent().getStringExtra("image");
        String token = getIntent().getStringExtra("token");

//        if(token!=null)
//        Toast.makeText(this, token, Toast.LENGTH_SHORT).show();



        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();


        //-------TOOL BAR ----------------
        //setting up a custom toolbar but edit in style also create new style in theme with new name
        // and noactionbar and set it in this activity in manifest
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //the default title will get removed also remove back btn
        //create custom back button

//        Objects.requireNonNull(getSupportActionBar()).setTitle(name);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //displays backarrow in toolbar

         //setting name and profile in toolbar
        binding.name.setText(name);
        Glide.with(ChatActivity.this).load(profileimg)
                .placeholder(R.drawable.avatar)
                .into(binding.profileimg);

        //left arrow se back hojaye
        binding.backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // shows online / offline
        database.getReference().child("Presence").child(recieverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String s = snapshot.getValue(String.class);
                    if(!s.isEmpty()){
                        if(s.equals("Offline")){
                            binding.onlinestatus.setVisibility(View.GONE);
                        }
                        else{
                        binding.onlinestatus.setText(s);
                        binding.onlinestatus.setVisibility(View.VISIBLE);
                    }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //typing... status
        final Handler handler = new Handler();
        binding.messagebox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                database.getReference().child("Presence").child(senderUid).setValue("typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping,1000);
            }

            Runnable userStoppedTyping = new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("Presence").child(senderUid).setValue("Online");

                }
            };
        });


      //----------TOOLBAR ENDS

         senderUid = FirebaseAuth.getInstance().getUid();

        senderRoom = senderUid+recieverUid;
        recieverRoom = recieverUid+senderUid;




        messageModalArrayList = new ArrayList<>();
        adapter = new MessagesAdapter(this,messageModalArrayList,senderRoom,recieverRoom);
        binding.ChatRV.setLayoutManager(new LinearLayoutManager(this));
        binding.ChatRV.setAdapter(adapter);

        dialog = new ProgressDialog(this);
        dialog.setMessage("sending image...");
        dialog.setCancelable(false);


        //chatting steps
        //step 1 :typed message is stored to database in both of sender and reciever
        binding.sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messagetxt = binding.messagebox.getText().toString();

                if(messagetxt.equals("")){
                    return;
                }

                Date date = new Date();
                MessageModal message = new MessageModal(messagetxt,senderUid,date.getTime());

                //set messageboc empty after sending message
                binding.messagebox.setText("");

                String randomKey = database.getReference().push().getKey();


                database.getReference().child("Chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        database.getReference().child("Chats")
                                .child(recieverRoom)
                                .child("messages")
                                .child(randomKey)
                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                sendNotification(name,message.getMessage(),token);
                            }
                        });

                        //updating lastmessage and time in database to show in recent message on friendRV now further in
                        // friendsAdapter for setting up the data
                        HashMap<String,Object> lastMsgObj = new HashMap<>();
                        lastMsgObj.put("lastMsg",message.getMessage());
                        lastMsgObj.put("lastMsgTime",date.getTime());

                        database.getReference().child("Chats").child(senderRoom).updateChildren(lastMsgObj);
                        database.getReference().child("Chats").child(recieverRoom).updateChildren(lastMsgObj);
                    }
                });


            }
        });




        //chatting steps
        //step 2:
        //displaying both the message in sender and recievers view
        /*adding the messages from database to arrayList of messageModal type to display */
       //  in chats through MessageAdapter
        database.getReference().child("Chats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageModalArrayList.clear();

                        for(DataSnapshot snapshot1 : snapshot.getChildren()){
                            MessageModal messageModal = snapshot1.getValue(MessageModal.class);
                            messageModal.setMessageId(snapshot1.getKey());
                            messageModalArrayList.add(messageModal);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



        //sending images using attachment
        //step1 :
        binding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");  //"video/*"  //for both "*/*"
                startActivityForResult(intent,25);
            }
        });

    }

    //sending images using attachment
    //step 2:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==25) {
            if (data != null) {
                if(data.getData()!=null){
                    dialog.show();
                    Uri selectedimg = data.getData();
                    Calendar calendar = Calendar.getInstance();
                    StorageReference reference = storage.getReference().child("chats")
                            .child(calendar.getTimeInMillis() +"");

                    reference.putFile(selectedimg).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        dialog.dismiss();
                                        String imagepath = uri.toString();

                                        String messagetxt = binding.messagebox.getText().toString();

//                                        if(messagetxt.equals("")){
//                                            return;
//                                        }

                                        Date date = new Date();
                                        MessageModal message = new MessageModal(messagetxt,senderUid,date.getTime());

                                        //adding image to message modal
                                        message.setImageUrl(imagepath);
                                        message.setMessage("photo");

                                        //set messageboc empty after sending message
                                        binding.messagebox.setText("");

                                        String randomKey = database.getReference().push().getKey();


                                        database.getReference().child("Chats")
                                                .child(senderRoom)
                                                .child("messages")
                                                .child(randomKey)
                                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                database.getReference().child("Chats")
                                                        .child(recieverRoom)
                                                        .child("messages")
                                                        .child(randomKey)
                                                        .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                    }
                                                });

                                                //updating lastmessage and time in database to show in recent message on friendRV now further in
                                                // friendsAdapter for setting up the data
                                                HashMap<String,Object> lastMsgObj = new HashMap<>();
                                                lastMsgObj.put("lastMsg",message.getMessage());
                                                lastMsgObj.put("lastMsgTime",date.getTime());

                                                database.getReference().child("Chats").child(senderRoom).updateChildren(lastMsgObj);
                                                database.getReference().child("Chats").child(recieverRoom).updateChildren(lastMsgObj);
                                            }
                                        });


                                    }
                                });
                            }
                        }
                    });


                }
            }
        }

    }

    //back button on toolbar to back on last activity or u can use manifest   android:parentActivityName="."
    //if manifest wla don't work
//    @Override
//    public boolean onSupportNavigateUp() {
//        finish();
//        return super.onSupportNavigateUp();
//    }

    //show person online or not when in this activity here it will show online
    @Override
    protected void onResume() {
        super.onResume();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("Presence").child(currentId).setValue("Online");

    }

    @Override
    protected void onPause() {
        super.onPause();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("Presence").child(currentId).setValue("Offline");
    }


    void sendNotification(String name,String message,String token){
        try {
            RequestQueue queue = Volley.newRequestQueue(this);

            String url = "https://fcm.googleapis.com/fcm/send";

            JSONObject data = new JSONObject();
            data.put("title", name);
            data.put("body", message);

            JSONObject notificationData = new JSONObject();
            notificationData.put("notification",data);
            notificationData.put("to",token);

            JsonObjectRequest request = new JsonObjectRequest(url, notificationData, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
//                    Toast.makeText(ChatActivity.this, "success", Toast.LENGTH_SHORT).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
//                    Toast.makeText(ChatActivity.this, "error", Toast.LENGTH_SHORT).show();

                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String,String> map = new HashMap<>();
                    String key = "Key=AAAA6d__Plc:APA91bGRxpttezMVCvaCQBOxtrTXP3f77O_UNmtLFpRNZXhJjsrztB2ftSbLDpZMuZ7v0ClZ8KIjPlPGwEfh38SodCbU95keydgx891oOjfgCj4XmX02AtpLO4Qt7Nj29F29zGtZOIBx";
                    map.put("Authorization",key);
                    map.put("Content-Type","application/json");
                    return map;
                }
            };

            queue.add(request);
        }
        catch (Exception e){

        }

    }


        @Override
    protected void onStop() {
        super.onStop();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("Presence").child(currentId).setValue("Offline");
    }

}