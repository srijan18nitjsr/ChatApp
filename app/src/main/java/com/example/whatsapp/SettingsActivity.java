package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    Button update;
    EditText uname,status;
    CircleImageView cview;
    Uri image_uri;
    StorageReference storageReference;
    String current;
    FirebaseAuth auth;
    DatabaseReference reference;
    private  static final int gallarypick=1;
    ProgressDialog loadingBar;
    Toolbar settingsToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        update=findViewById(R.id.update);
        settingsToolbar=findViewById(R.id.settings_toolbar);
        settingsToolbar.setTitle("My Profile");
        uname=findViewById(R.id.user);
        status=findViewById(R.id.status);
        cview=findViewById(R.id.profile_image);
        auth=FirebaseAuth.getInstance();
        current=auth.getCurrentUser().getUid();
        loadingBar=new ProgressDialog(this);
        reference= FirebaseDatabase.getInstance().getReference();
        storageReference= FirebaseStorage.getInstance().getReference().child("Profile");

        status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status.setCursorVisible(true);
            }
        });

        uname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uname.setCursorVisible(true);
            }
        });


        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String myname=uname.getText().toString();
                String mystatus=status.getText().toString();
                if(TextUtils.isEmpty(myname)){
                    Toast.makeText(SettingsActivity.this,"Set username",Toast.LENGTH_LONG).show();

                }
                if(TextUtils.isEmpty(mystatus)){
                    Toast.makeText(SettingsActivity.this,"Set status",Toast.LENGTH_LONG).show();

                }
                else{
                    HashMap<String,Object> map=new HashMap<>();
                    map.put("uid",current);
                    map.put("name",myname);
                    map.put("status",mystatus);
                    reference.child("Users").child(current).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Intent intent=new Intent(SettingsActivity.this,MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();;
                                Toast.makeText(SettingsActivity.this,"Uploaded Successfully",Toast.LENGTH_LONG).show();
                            }
                            else{
                                Toast.makeText(SettingsActivity.this,task.getException().toString(),Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }


            }
        });


        display();
        cview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,gallarypick);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==gallarypick && resultCode==RESULT_OK && data!=null){
            Uri images_selected=data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode==RESULT_OK){
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait..your profile image is updating..");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();
                 image_uri=result.getUri();
                final StorageReference image_ref=storageReference.child(current+".jpeg");
                image_ref.putFile(image_uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful()){
                            Toast.makeText(SettingsActivity.this,"Profile Image Set Successfully...",Toast.LENGTH_LONG);
                            final String profile=image_uri.toString();
                            image_ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    reference.child("Users").child(current).child("image").setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(SettingsActivity.this,"Successfull...",Toast.LENGTH_LONG).show();
                                                loadingBar.dismiss();
                                            }
                                            else{
                                                Toast.makeText(SettingsActivity.this,task.getException().toString(),Toast.LENGTH_LONG).show();
                                                loadingBar.dismiss();
                                            }

                                        }
                                    });

                                }
                            });


                        }
                        else{
                            Toast.makeText(SettingsActivity.this,task.getException().toString(),Toast.LENGTH_LONG).show();
                            loadingBar.dismiss();

                        }

                    }
                });

            }
        }




    }

    private void display() {
        reference.child("Users").child(current).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.hasChild("name") && snapshot.hasChild("status")&& snapshot.hasChild("image")){
                    String myname=snapshot.child("name").getValue().toString();
                    String ustatus=snapshot.child("status").getValue().toString();
                   String uimage=snapshot.child("image").getValue().toString();
                    uname.setText(myname);
                    status.setText(ustatus);
                    Picasso.get().load(uimage).into(cview);
                }
                else if(snapshot.exists() && snapshot.hasChild("name") && snapshot.hasChild("status")){
                    String myname=snapshot.child("name").getValue().toString();
                    String ustatus=snapshot.child("status").getValue().toString();
                    uname.setText(myname);
                    status.setText(ustatus);
                    Picasso.get().load(R.drawable.profile).into(cview);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void updateUserStatus(String state){
        String date,time;
        Calendar calendar=Calendar.getInstance();
        SimpleDateFormat current_date=new SimpleDateFormat("MMM dd, yyyy");
        date=current_date.format(calendar.getTime());

        SimpleDateFormat current_time=new SimpleDateFormat("hh:mm a");
        time=current_time.format(calendar.getTime());

        HashMap<String,Object> onlineState =new HashMap<>();
        onlineState.put("time",time);
        onlineState.put("date",date);
        onlineState.put("state",state);

        current=auth.getCurrentUser().getUid();
        reference.child("Users").child(current).child("userState").updateChildren(onlineState);
    }

   @Override
   protected void onStart() {
      super.onStart();

       FirebaseUser currentuser=auth.getCurrentUser();
       if(currentuser!=null){

          updateUserStatus("online");

       }
   }


    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUser currentuser = auth.getCurrentUser();
        if (currentuser != null) {
            updateUserStatus("offline");

        }
    }


}
