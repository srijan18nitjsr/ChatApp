package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    String receive_user_id, current_state, current;
    TextView name, status;
    Button send, cancel_request;
    CircleImageView profile;
    DatabaseReference reference, chat, contacts_ref, notification_ref;
    FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        contacts_ref = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notification_ref = FirebaseDatabase.getInstance().getReference().child("Notifications");
        chat = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        auth = FirebaseAuth.getInstance();


        receive_user_id = getIntent().getExtras().get("visit_user_id").toString();
        current = auth.getCurrentUser().getUid();
        name = findViewById(R.id.visit_username);

        status = findViewById(R.id.visit_status);
        cancel_request = findViewById(R.id.request);
        profile = findViewById(R.id.visit_profile);
        send = findViewById(R.id.send_message);
        current_state = "new";

        ActivityCompat.requestPermissions(ProfileActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        ActivityCompat.requestPermissions(ProfileActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder1 = new AlertDialog.Builder(ProfileActivity.this);
                builder1.setTitle("Save Image to gallary");
                builder1.setMessage("Are you sure you want to save image");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                saveToGallary();


                            }
                        });

                builder1.setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();

                            }
                        });


                builder1.show();
            }
        });
        reference.child(receive_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("image")) {
                    String userimage = snapshot.child("image").getValue().toString();
                    String username = snapshot.child("name").getValue().toString();
                    String userstatus = snapshot.child("status").getValue().toString();

                    Picasso.get().load(userimage).placeholder(R.drawable.profile).into(profile);
                    name.setText(username);
                    status.setText(userstatus);
                    manageChatRequest();
                } else {

                    String username = snapshot.child("name").getValue().toString();
                    String userstatus = snapshot.child("status").getValue().toString();
                    name.setText(username);
                    status.setText(userstatus);
                    manageChatRequest();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }





    private void manageChatRequest() {

        chat.child(current).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.hasChild(receive_user_id)) {
                    String request = snapshot.child(receive_user_id).child("request_type").getValue().toString();
                    if (request.equals("sent")) {
                        current_state = "request_sent";
                        send.setText("Cancel Chat Request");
                    } else if (request.equals("received")) {
                        current_state = "request_received";
                        send.setText("Accept Chat Request");
                        cancel_request.setVisibility(View.VISIBLE);
                        cancel_request.setEnabled(true);
                        cancel_request.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelChatRequest();
                            }
                        });
                    }
                } else {
                    contacts_ref.child(current).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(receive_user_id)) {
                                current_state = "friends";
                                send.setText("Remove This Friend");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (!receive_user_id.equals(current)) {

            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    send.setEnabled(false);

                    if (current_state.equals("new")) {
                        sendChatRequest();
                    }
                    if (current_state.equals("request_sent")) {
                        cancelChatRequest();
                    }
                    if (current_state.equals("request_received")) {
                        acceptChatRequest();
                    }
                    if (current_state.equals("friends")) {
                        removeFriend();
                    }


                }
            });

        } else {
            send.setVisibility(View.INVISIBLE);
        }
    }

    private void removeFriend() {

        contacts_ref.child(current).child(receive_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    contacts_ref.child(receive_user_id).child(current).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                send.setEnabled(true);
                                send.setText("Send Message");
                                current_state = "new";
                                cancel_request.setVisibility(View.INVISIBLE);
                                cancel_request.setEnabled(false);

                            }
                        }
                    });
                }
            }
        });


    }

    private void saveToGallary(){
        BitmapDrawable draw = (BitmapDrawable) profile.getDrawable();
        Bitmap bitmap = draw.getBitmap();

        FileOutputStream outStream = null;
        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/YourFolderName");
        dir.mkdirs();
        String fileName = String.format("%d.jpg", System.currentTimeMillis());
        File outFile = new File(dir, fileName);
        try {
            outStream = new FileOutputStream(outFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
        Toast.makeText(ProfileActivity.this,"Image saved to gallary!!!!",Toast.LENGTH_SHORT).show();
        try {
            outStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(outFile));
        sendBroadcast(intent);
    }

    private void acceptChatRequest() {

        contacts_ref.child(current).child(receive_user_id).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    contacts_ref.child(receive_user_id).child(current).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                chat.child(current).child(receive_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            chat.child(receive_user_id).child(current).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    send.setEnabled(true);
                                                    current_state = "friends";
                                                    send.setText("Remove This Friend");
                                                    cancel_request.setVisibility(View.INVISIBLE);
                                                    cancel_request.setEnabled(false);

                                                }
                                            });
                                        }

                                    }
                                });
                            }

                        }
                    });
                }
            }
        });
    }

    private void cancelChatRequest() {

        chat.child(current).child(receive_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    chat.child(receive_user_id).child(current).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                send.setEnabled(true);
                                send.setText("Send Message");
                                current_state = "new";
                                cancel_request.setVisibility(View.INVISIBLE);
                                cancel_request.setEnabled(false);

                            }
                        }
                    });
                }
            }
        });
    }

    private void sendChatRequest() {

        chat.child(current).child(receive_user_id).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    chat.child(receive_user_id).child(current).child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                HashMap<String, String> chatNotification = new HashMap<>();
                                chatNotification.put("from", current);
                                chatNotification.put("type", "request");
                                notification_ref.child(receive_user_id).push().setValue(chatNotification).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            send.setEnabled(true);
                                            current_state = "request_sent";
                                            send.setText("Cancel Chat Request");

                                        }
                                    }
                                });

                            }
                        }
                    });
                }
            }
        });
    }

    public void updateUserStatus(String state) {
        String date, time;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat current_date = new SimpleDateFormat("MMM dd, yyyy");
        date = current_date.format(calendar.getTime());

        SimpleDateFormat current_time = new SimpleDateFormat("hh:mm a");
        time = current_time.format(calendar.getTime());

        HashMap<String, Object> onlineState = new HashMap<>();
        onlineState.put("time", time);
        onlineState.put("date", date);
        onlineState.put("state", state);

        current = auth.getCurrentUser().getUid();
        reference.child(current).child("userState").updateChildren(onlineState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentuser = auth.getCurrentUser();
        if (currentuser != null) {

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

