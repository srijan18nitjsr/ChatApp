package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {
    EditText email,password;
    Button register;
    FirebaseAuth auth;
    String mail,pass;
    DatabaseReference reference;
    TextView exist;
    ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        auth=FirebaseAuth.getInstance();
        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
        exist=findViewById(R.id.exist);
        register=findViewById(R.id.register);
        loadingBar=new ProgressDialog(this);
        reference= FirebaseDatabase.getInstance().getReference();



        Log.i("ccv","vb");
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mail=email.getText().toString();
                pass =password.getText().toString();

                if(TextUtils.isEmpty(mail)){
                    Toast.makeText(RegisterActivity.this,"Enter your email..",Toast.LENGTH_SHORT).show();

                }
                else if(TextUtils.isEmpty(pass)){
                    Toast.makeText(RegisterActivity.this,"Enter your password..",Toast.LENGTH_SHORT).show();

                }
                else{
                    loadingBar.setTitle("Creating New Account");
                    loadingBar.setMessage("Please wait..while we are creating new account for you..");
                    loadingBar.setCanceledOnTouchOutside(true);
                    loadingBar.show();
                    auth.createUserWithEmailAndPassword(mail,pass).addOnCompleteListener(RegisterActivity.this,new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){

                                String deviceToken= FirebaseInstanceId.getInstance().getToken();
                                loadingBar.dismiss();
                                Toast.makeText(RegisterActivity.this,"Account create successfully...",Toast.LENGTH_SHORT).show();
                                String currentUserid=auth.getCurrentUser().getUid();
                                reference.child("Users").child(currentUserid).setValue("");
                                reference.child("Users").child(currentUserid).child("device_token").setValue(deviceToken);
                                Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();;
                            }
                            else{
                                Toast.makeText(RegisterActivity.this,task.getException().toString(),Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();

                            }
                        }
                    });

                }

            }
        });

        exist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
            }
        });

    }
    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}
