package com.example.chatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Adapter;
import android.widget.Toast;

import com.example.chatsapp.Adapter.FriendsAdapter;
import com.example.chatsapp.Adapter.StatusAdapter;
import com.example.chatsapp.Modals.StatusModal;
import com.example.chatsapp.Modals.User;
import com.example.chatsapp.Modals.UserStatusModal;
import com.example.chatsapp.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    //retriving data from database and display in recycleview
    //////////////#Step1:
    FirebaseDatabase database;


    ArrayList<User> list;
    FriendsAdapter adapter;

    //Statuses
    ArrayList<UserStatusModal> userStatusModalArrayList;
    StatusAdapter statusAdapter;
    ProgressDialog dialog;
    User user;

    ProgressDialog dialog2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog2 = new ProgressDialog(this);
        dialog2.setMessage("Loading...");
        dialog2.setCancelable(false);
        dialog2.show();

        /////////////#step2:
        database = FirebaseDatabase.getInstance();

        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String token) {
                        HashMap<String,Object> maps = new HashMap<>();
                        maps.put("token",token);

                        database.getReference().child("Users")
                                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                                .updateChildren(maps);
//                        Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();
                    }
                });



        userStatusModalArrayList = new ArrayList<>();
        list = new ArrayList<>();

        //RV of status and friends list
        statusAdapter = new StatusAdapter(this,userStatusModalArrayList);
        adapter = new FriendsAdapter(this,list);
//        binding.friendsRV.setLayoutManager(new LinearLayoutManager(this));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,RecyclerView.HORIZONTAL,false);
        binding.StatusRV.setLayoutManager(layoutManager);

        binding.friendsRV.setAdapter(adapter);
        binding.StatusRV.setAdapter(statusAdapter);

        //when uploading status
        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading status...");
        dialog.setCancelable(false);

        //getting current user name,profile image from database for status
        database.getReference().child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        ////////////#step3:
        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    User user = snapshot1.getValue(User.class);

                    if(!user.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                    list.add(user);

                }

                adapter.notifyDataSetChanged();
                dialog2.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //status coding...........
        //########Step 1
        //Onclicking the status option on navbar open gallery and display images only
        binding.bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.status) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent,75);
                }
                return false;
            }
        });

        database.getReference().child("Stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    userStatusModalArrayList.clear();
                    for (DataSnapshot dataSnapshot1:snapshot.getChildren()){
                        UserStatusModal userStatusModal = new UserStatusModal();
                        userStatusModal.setProfileImg(dataSnapshot1.child("profileImg").getValue(String.class));
                        userStatusModal.setName(dataSnapshot1.child("name").getValue(String.class));
                        userStatusModal.setLastUpdated(dataSnapshot1.child("lastUpdated").getValue(Long.class));

                        ArrayList<StatusModal> statuses = new ArrayList<>();
                        for(DataSnapshot statusSnapshot: dataSnapshot1.child("statuses").getChildren())
                        {
                            StatusModal statusModal = statusSnapshot.getValue(StatusModal.class);
                            statuses.add(statusModal);
                        }

                        userStatusModal.setStatuses(statuses);
                        userStatusModalArrayList.add(userStatusModal);
//                        Toast.makeText(MainActivity.this, "status updated", Toast.LENGTH_SHORT).show();
                    }

                    statusAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    //show person online or not
//    @Override
//    protected void onResume() {
//        super.onResume();
//        String currentId = FirebaseAuth.getInstance().getUid();
//        database.getReference().child("Presence").child(currentId).setValue("Online");
//
//    }

//        @Override
//    protected void onStop() {
//        super.onStop();
//        String currentId = FirebaseAuth.getInstance().getUid();
//        database.getReference().child("Presence").child(currentId).setValue("Offline");
//    }


    @Override
    protected void onPause() {
        super.onPause();
        String currentId = FirebaseAuth.getInstance().getUid();
        database.getReference().child("Presence").child(currentId).setValue("Offline");
    }

    //############step2
    //when image is selected from gallery that image is going to be stored in storage
    // and then from that storage image is going to be stored in database so that
    //data can be fetched through snapshots and can be displayed in StatusRV
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null){
            if(data.getData()!= null){
                dialog.show();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                Date date = new Date();
                StorageReference reference = storage.getReference().child("Stories")
                        .child(date.getTime()+""+user.getName());
                reference.putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    UserStatusModal userStatusModal = new UserStatusModal();
                                    userStatusModal.setProfileImg(user.getProfileimg());
                                    userStatusModal.setName(user.getName());
                                    userStatusModal.setLastUpdated(date.getTime());

                                    HashMap<String,Object> obj = new HashMap<>();
                                    obj.put("name",userStatusModal.getName());
                                    obj.put("profileImg",userStatusModal.getProfileImg());
                                    obj.put("lastUpdated",userStatusModal.getLastUpdated());

                                    String imgUrl = uri.toString();
                                    StatusModal statusModal = new StatusModal(imgUrl,userStatusModal.getLastUpdated());


                                    database.getReference().child("Stories")
                                            .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                                            .updateChildren(obj);

                                    database.getReference().child("Stories")
                                            .child(FirebaseAuth.getInstance().getUid())
                                            .child("statuses")
                                            .push()
                                            .setValue(statusModal);


                                    dialog.dismiss();

                                }
                            });
                        }
                    }
                });
            }
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.groups: {
                Intent intent = new Intent(MainActivity.this,GroupChatActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.search:
                Toast.makeText(this, "search clicked", Toast.LENGTH_SHORT).show();break;
            case R.id.settings:
                Toast.makeText(this, "settings clicked", Toast.LENGTH_SHORT).show();break;

        }
        return super.onOptionsItemSelected(item);
    }

    //just by this this function top menu is added step1> create menu file i.e. topmenu then
    //2>step-----
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu,menu);
        return super.onCreateOptionsMenu(menu);
    }
}