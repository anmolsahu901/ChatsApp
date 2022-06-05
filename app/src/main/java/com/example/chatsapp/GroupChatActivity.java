package com.example.chatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatsapp.Adapter.GroupMessagesAdapter;
import com.example.chatsapp.Adapter.MessagesAdapter;
import com.example.chatsapp.Modals.MessageModal;
import  com.example.chatsapp.databinding.ActivityGroupChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class GroupChatActivity extends AppCompatActivity {


    ActivityGroupChatBinding binding;

    ArrayList<MessageModal> messageModalArrayList;
    GroupMessagesAdapter adapter;

    FirebaseDatabase database;
    FirebaseStorage storage;

    String senderUid;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setTitle("Group Chat");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        senderUid = FirebaseAuth.getInstance().getUid();

        dialog = new ProgressDialog(this);
        dialog.setMessage("sending image...");
        dialog.setCancelable(false);

        messageModalArrayList = new ArrayList<>();
        adapter = new GroupMessagesAdapter(this,messageModalArrayList);
        binding.ChatRV.setLayoutManager(new LinearLayoutManager(this));
        binding.ChatRV.setAdapter(adapter);

        //on clicking the message willbe stored in database
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

                database.getReference().child("public")
                        .push()
                        .setValue(message);
            }
        });


        //fetching data from database
        database.getReference().child("public")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messageModalArrayList.clear();
                        for(DataSnapshot snapshot1: snapshot.getChildren()){
                            MessageModal message = snapshot1.getValue(MessageModal.class);
                            message.setMessageId(snapshot1.getKey());
                            messageModalArrayList.add(message);
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

                                        Date date = new Date();
                                        MessageModal message = new MessageModal(messagetxt,senderUid,date.getTime());

                                        //adding image to message modal
                                        message.setImageUrl(imagepath);
                                        message.setMessage("photo");
                                        //set messageboc empty after sending message
                                        binding.messagebox.setText("");

                                        database.getReference().child("public")
                                                .push()
                                                .setValue(message);


                                    }
                                });
                            }
                        }
                    });


                }
            }
        }

    }




    //to get to back button in toolbar
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}