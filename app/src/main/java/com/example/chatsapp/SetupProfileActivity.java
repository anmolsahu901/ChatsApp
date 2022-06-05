package com.example.chatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.example.chatsapp.Modals.User;
import com.example.chatsapp.databinding.ActivitySetupProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class SetupProfileActivity extends AppCompatActivity {

    ActivitySetupProfileBinding binding;

    //STEP 1:(COMMON STEP FOR STORAGE AND DATABASE)
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;

    Uri selectedimg;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetupProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        dialog = new ProgressDialog(this);
        dialog.setMessage("Updating profile... ");
        dialog.setCancelable(false);


        //STEP 2: GET INSTANCE
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        //openning gallery at clicking
        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //through this a gallery will be opened
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
               //and after selecting image for profile a reference of that image will be return to activity
                startActivityForResult(intent, 45);
                //Now look at function onActivityResult
            }
        });



        //
        binding.continueprofilebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = binding.namebox.getText().toString();
                if (name.isEmpty()) {
                    binding.namebox.setError("please type your name");
                    return;
                }
                //if image is selected
                dialog.show();
                if (selectedimg != null) {
                    //#step3:(STORAGE)
                    //firebase storage reference : Profile -> userId(filename)
                    StorageReference reference = storage.getReference().child("Profiles")
                            .child(auth.getUid()+name);

                    //#step4:(STORAGE)
                    // putFile() :profileimage ko FIREBASE STORAGE mein store kardega iss reference me
                    // addOnCompleteListener : agar task complete hota h toh
                    reference.putFile(selectedimg).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                // toh uss store image ka url lelo taaki database mein add kar sake
                                //addOnSuccessListener : agar success h toh iske andar ka code chalega
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        //uri : link h profile image ka
                                        String imageUrl = uri.toString();

                                        String uid = auth.getUid();
                                        String phonenumber = auth.getCurrentUser().getPhoneNumber();
                                        String name = binding.namebox.getText().toString();

                                        //NOW TO ADD ALL THIS USER DATA TO FIREBASE DATABASE
                                        User user = new User(uid, name, phonenumber, imageUrl);

                                        //STEP 3: (DATABASE)
                                        //storing data in database and if successful then open MainActivity(chatting)
                                        database.getReference().child("Users")
                                                .child(uid)
                                                .setValue(user)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        dialog.dismiss();
                                                        Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                });

                                    }
                                });
                            }
                        }
                    });


                }
                // if image is not selected from gallery
                else {
                    String uid = auth.getUid();
                    String phonenumber = auth.getCurrentUser().getPhoneNumber();
                    //NOW TO ADD ALL THIS USER DATA TO FIREBASE DATABASE
                    User user = new User(uid, name, phonenumber, "No Image");

                    database.getReference().child("Users").child(uid)
                            .setValue(user)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    dialog.dismiss();
                                    Intent intent = new Intent(SetupProfileActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                }
            }
        });
    }


    //on selecting an image from gallery the reference will be returned so
    //setting that image to profile and saving the uri to a variable for further use in Database
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (data.getData() != null) {
                binding.imageView.setImageURI(data.getData());
                selectedimg = data.getData();
            }
        }
    }
}