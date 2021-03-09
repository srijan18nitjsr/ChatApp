package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneActivity extends AppCompatActivity {

    EditText phone,code;
    Button send,verify;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    FirebaseAuth auth;
    ProgressDialog loadingBar;
    String mVerificationId;
    PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);
        auth=FirebaseAuth.getInstance();
        phone=findViewById(R.id.phone);
        code=findViewById(R.id.verify);
        send=findViewById(R.id.send);
        verify=findViewById(R.id.verify_code);
        loadingBar=new ProgressDialog(this);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String number=phone.getText().toString();
                if(TextUtils.isEmpty(number)){
                    Toast.makeText(PhoneActivity.this,"Enter Phone Number",Toast.LENGTH_LONG);
                }
                else{

                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("WAIT...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            number,      // Phone number to verify
                            60, // Timeout and unit
                            TimeUnit.SECONDS ,
                            PhoneActivity.this,
                            callbacks


                    );


                }
            }
        });

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send.setVisibility(View.INVISIBLE);
                phone.setVisibility(View.INVISIBLE);
                String otp=code.getText().toString();
                if(TextUtils.isEmpty(otp)){
                    Toast.makeText(PhoneActivity.this,"Enter Verification Code...",Toast.LENGTH_LONG);

                }
                else{

                    loadingBar.setTitle("Code Verification");
                    loadingBar.setMessage("WAIT...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
                    signInWithPhoneAuthCredential(credential);
                }

            }
        });

        callbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                loadingBar.dismiss();
                Toast.makeText(PhoneActivity.this,"Invalid Phone Number.....",Toast.LENGTH_LONG).show();
                send.setVisibility(View.VISIBLE);
                phone.setVisibility(View.VISIBLE);
                code.setVisibility(View.INVISIBLE);
                verify.setVisibility(View.INVISIBLE);


            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                Toast.makeText(PhoneActivity.this,"Code Sent Successfully...",Toast.LENGTH_LONG);
                loadingBar.dismiss();
                send.setVisibility(View.INVISIBLE);
                phone.setVisibility(View.INVISIBLE);
                code.setVisibility(View.VISIBLE);
                verify.setVisibility(View.VISIBLE);



                // Save verification ID and resending token so we can use them later
                 mVerificationId = verificationId;
                mResendToken = token;

                // ...
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            startActivity(new Intent(PhoneActivity.this,MainActivity.class));

                        } else {
                            // Sign in failed, display a message and update the UI

                            Toast.makeText(PhoneActivity.this,task.getException().toString(),Toast.LENGTH_LONG);
                        }
                    }
                });
    }



}
