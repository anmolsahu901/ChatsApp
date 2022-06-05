package com.example.chatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.chatsapp.databinding.ActivityOTPBinding;
import com.example.chatsapp.databinding.ActivityPhoneNumberBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class OTPActivity extends AppCompatActivity {

    ActivityOTPBinding binding;
    // #steps for firebase  phone authentication --
    //#step 1 : create objects
    FirebaseAuth auth;
    String verificationId;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOTPBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String phonenumber = getIntent().getStringExtra("phonenumber");

        binding.phoneTV.setText("Verify " + phonenumber);

        getSupportActionBar().hide();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending OTP... ");
        dialog.setCancelable(false);
        dialog.show();

        //#step 2 : getInstance
        auth = FirebaseAuth.getInstance();

        //#Step 3: create PhoneAuthOptions variable
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth) //pass auth
                .setPhoneNumber(phonenumber) // number whose authentication to be set
                .setTimeout(60L, TimeUnit.SECONDS)   // otp code is valid till 60s
                .setActivity(OTPActivity.this) //working in which activity
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() { //ya to verification hogi ya nhi hogi
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(OTPActivity.this, "Invalid Phone number", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();

                    }

                    // this method is overide manually
                    @Override
                    public void onCodeSent(@NonNull String verifyId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verifyId, forceResendingToken);
                        dialog.dismiss();
                        verificationId = verifyId;
                        //on verification set otp  automatically
//                        Toast.makeText(OTPActivity.this, "otp : 123456", Toast.LENGTH_SHORT).show();
//                        binding.otpview.setText("123456");
                    }
                }).build();

        //#step 4: phoneAuthProvider:
        //ye line options ko follow karte hue ek code ko send kardegi humare phonenumber
        PhoneAuthProvider.verifyPhoneNumber(options);

        //jese hi otp completely enter hojaye click continue
        binding.continueOTPbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String otp = binding.otpview.getText().toString();

                if (otp.isEmpty()) {
                    binding.otpview.setError("Enter the OTP");
                    return;
                }

                //#step 5: create variable for PhoneAuthCredential and pass the verificationId
                //that we get from firebase and otp entered by user
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);

                // #step 6:auth.signInWithCredential() pass credential and addOnCompleteListener
                auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //if task is successfull then logged in
                        if (task.isSuccessful()) {
                            Toast.makeText(OTPActivity.this, "Logged in", Toast.LENGTH_SHORT).show();
                            Intent intent1 = new Intent(OTPActivity.this, SetupProfileActivity.class);
                            startActivity(intent1);
                            finishAffinity(); // jitni bhi pehle ki activities h unhe band kardo
                            // but finish() ye bss recent activity ko band karega
                        } else {
                            Toast.makeText(OTPActivity.this, "failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        });
    }
}