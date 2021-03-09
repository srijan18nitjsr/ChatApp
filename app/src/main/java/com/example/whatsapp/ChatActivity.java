package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.internal.Util;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    String message_receiver_id, message_receiver_name, message_receiver_image, current, checker = "", myUrl = "";
    RecyclerView user_messages_list;
    TextView username, lastseen;
    CircleImageView profile_image;
    Toolbar chatToolbar;
    EditText message;
    ImageButton send, send_files;
    String date, time;
    FirebaseAuth auth;
    DatabaseReference root_ref, reference;
    List<Messages> messagesList = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    MessageAdapter messageAdapter;
//    Uri fileUri;
//    StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        auth = FirebaseAuth.getInstance();
        current = auth.getCurrentUser().getUid();
        root_ref = FirebaseDatabase.getInstance().getReference();
        reference = FirebaseDatabase.getInstance().getReference();

        message_receiver_id = getIntent().getExtras().get("visit_user_id").toString();
        message_receiver_name = getIntent().getExtras().get("visit_user_name").toString();
        message_receiver_image = getIntent().getExtras().get("visit_user_image").toString();


        InitialiseControls();


        username.setText(message_receiver_name);
        Picasso.get().load(message_receiver_image).placeholder(R.drawable.profile).into(profile_image);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        displayLastSeen();

//        send_files.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                CharSequence[] options=new CharSequence[]{
//                        "Images",
//                        "PDF Files",
//                        "MS Word Files"
//                };
//
//                AlertDialog.Builder alert=new AlertDialog.Builder(ChatActivity.this);
//                alert.setTitle("Select the file");
//                alert.setItems(options, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                        if(which==0){
//                            checker="image";
//                            Intent intent=new Intent();
//                            intent.setAction(Intent.ACTION_GET_CONTENT);
//                            intent.setType("image/*");
//                            startActivityForResult(intent.createChooser(intent,"Select Image"),438);
//                            Log.i("aaaaaaaa","lllll");
//                        }
//
//                        if(which==1){
//                            checker="pdf";
//                        }
//
//                        if(which==2){
//                            checker="docs";
//                        }
//
//                    }
//                });
//                alert.show();
//            }
//        });
    }

    private void sendMessage() {

        String mymessage = message.getText().toString();
        if (TextUtils.isEmpty(mymessage)) {
            Toast.makeText(ChatActivity.this, "Enter message first", Toast.LENGTH_SHORT).show();
        } else {
            String sent_message = "Message/" + current + "/" + message_receiver_id;
            String received_message = "Message/" + message_receiver_id + "/" + current;

            DatabaseReference message_ref = root_ref.child("Message").child(current).child(message_receiver_id).push();
            String push_id = message_ref.getKey();
            Log.i("id", push_id);
            Map messagebody = new HashMap();
            messagebody.put("message", mymessage);
            messagebody.put("type", "text");
            messagebody.put("from", current);
            messagebody.put("to", message_receiver_id);
            messagebody.put("messageID", push_id);
            messagebody.put("date", date);
            messagebody.put("time", time);

            Map details = new HashMap();
            details.put(sent_message + "/" + push_id, messagebody);
            details.put(received_message + "/" + push_id, messagebody);

            root_ref.updateChildren(details).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if (task.isSuccessful()) {
                        Toast.makeText(ChatActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }

                    message.setText("");

                }
            });


        }
    }

    private void InitialiseControls() {


        chatToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolbar);
        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        Log.i("name", message_receiver_name);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);


        username = findViewById(R.id.profile_name);
        lastseen = findViewById(R.id.lastseen);
        profile_image = findViewById(R.id.profile);
        message = findViewById(R.id.input_message);
        send = findViewById(R.id.send_btn);

        messageAdapter = new MessageAdapter(messagesList);
        user_messages_list = findViewById(R.id.private_messages);
        linearLayoutManager = new LinearLayoutManager(this);
        user_messages_list.setLayoutManager(linearLayoutManager);
        user_messages_list.setAdapter(messageAdapter);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat current_date = new SimpleDateFormat("MMM dd, yyyy");
        date = current_date.format(calendar.getTime());

        SimpleDateFormat current_time = new SimpleDateFormat("hh:mm a");
        time = current_time.format(calendar.getTime());
    }

//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if(requestCode==438 && requestCode==RESULT_OK && data!=null && data.getData()!=null){
//
//            fileUri=data.getData();
//            if(!checker.equals("image")){
//
//            }
//            else if(checker.equals("image")){
//
//                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Image Files");
//
//                final String sent_message="Message/" +current+"/"+message_receiver_id;
//                final String received_message="Message/" +message_receiver_id+"/"+current;
//
//                DatabaseReference message_ref=root_ref.child("Messages").child(current).child(message_receiver_id).push();
//                final String push_id=message_ref.getKey();
//                final StorageReference filePath=storageReference.child(push_id+".jpg");
//                Log.i("aaaaaaaa","zzzzzzzzz");
//                uploadTask=filePath.putFile(fileUri);
//                uploadTask.continueWithTask(new Continuation() {
//                    @Override
//                    public Object then(@NonNull Task task) throws Exception {
//                        if(!task.isSuccessful()){
//                            throw task.getException();
//                        }
//                        Log.i("aaaaaaaa","bbbbbbbbbbbbbbbbcccc");
//                        return filePath.getDownloadUrl();
//                    }
//                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Uri> task) {
//                        if(task.isSuccessful()){
//                            Uri downloadUri=task.getResult();
//                            myUrl=downloadUri.toString();
//
//                            Map messagebody=new HashMap();
//                            messagebody.put("message",myUrl);
//                            messagebody.put("name",fileUri.getLastPathSegment());
//                            messagebody.put("type",checker);
//                            messagebody.put("from",current);
//                            messagebody.put("to",message_receiver_id);
//                            messagebody.put("messageID",push_id);
//                            messagebody.put("date",date);
//                            messagebody.put("time",time);
//
//                            Map details=new HashMap();
//                            details.put(sent_message+"/"+push_id,messagebody);
//                            details.put(received_message+"/"+push_id,messagebody);
//
//                            root_ref.updateChildren(details).addOnCompleteListener(new OnCompleteListener() {
//                                @Override
//                                public void onComplete(@NonNull Task task) {
//
//                                    if(task.isSuccessful()){
//                                        Toast.makeText(ChatActivity.this,"Message Sent",Toast.LENGTH_SHORT);
//                                        Log.i("oooooooooo","goooooooooooooooooddddddddd");
//                                    }
//                                    else{
//                                        Toast.makeText(ChatActivity.this,"Error",Toast.LENGTH_SHORT).show();
//                                    }
//
//                                    message.setText("");
//
//                                }
//                            });
//                        }
//                    }
//                });
//
//            }
//            else{
//
//                Toast.makeText(ChatActivity.this,"Error",Toast.LENGTH_SHORT).show();
//            }
//
//        }
//    }

    void displayLastSeen() {

        root_ref.child("Users").child(message_receiver_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child("userState").hasChild("state")) {

                    String state = snapshot.child("userState").child("state").getValue().toString();
                    String date = snapshot.child("userState").child("date").getValue().toString();
                    String time = snapshot.child("userState").child("time").getValue().toString();

                    if (state.equals("online")) {
                        lastseen.setText("online");
                    } else if (state.equals("offline")) {
                        lastseen.setText("Last Seen: " + date + " " + time);
                    }
                } else {
                    lastseen.setText("offline");
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentuser = auth.getCurrentUser();
        if (currentuser != null) {

            updateUserStatus("online");

        }

        root_ref.child("Message").child(current).child(message_receiver_id).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                Messages messages = snapshot.getValue(Messages.class);
                messagesList.add(messages);
                messageAdapter.notifyDataSetChanged();
                user_messages_list.smoothScrollToPosition(user_messages_list.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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
        reference.child("Users").child(current).child("userState").updateChildren(onlineState);
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
