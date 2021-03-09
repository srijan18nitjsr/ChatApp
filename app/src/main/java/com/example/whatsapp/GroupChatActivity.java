package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;


public class GroupChatActivity extends AppCompatActivity {

    Toolbar mytoolbar;
    ScrollView scrollView;
    EditText editText;
    TextView textView;
    ImageButton imageButton;
    String currentGroup,current,current_name,current_date,current_time;
    FirebaseAuth auth;
    DatabaseReference reference,group_ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        auth=FirebaseAuth.getInstance();
        current=auth.getCurrentUser().getUid();
        reference= FirebaseDatabase.getInstance().getReference().child("Users");
        currentGroup=getIntent().getExtras().get("groupname").toString();
        mytoolbar=findViewById(R.id.chat);

        mytoolbar.setTitle(currentGroup);
        scrollView=findViewById(R.id.scroll);
        editText=findViewById(R.id.text);
        textView=findViewById(R.id.mychat);
        imageButton=findViewById(R.id.send);
        group_ref=FirebaseDatabase.getInstance().getReference().child("Group").child(currentGroup);

        getUser();

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message=editText.getText().toString();

                if(TextUtils.isEmpty(message)){
                    Toast.makeText(GroupChatActivity.this,"Enter message first",Toast.LENGTH_SHORT).show();
                }
                else{
                    Calendar cal_date =Calendar.getInstance();
                    SimpleDateFormat dateFormat=new SimpleDateFormat("MMM dd, yyyy");
                    current_date=dateFormat.format(cal_date.getTime());

                    Calendar cal_time =Calendar.getInstance();
                    SimpleDateFormat timeFormat=new SimpleDateFormat("hh:mm a");
                    current_time=timeFormat.format(cal_time.getTime());

                    HashMap<String,Object> map=new HashMap<>();
                    map.put("name",current_name);
                    map.put("message",message);
                    map.put("date",current_date);
                    map.put("time",current_time);
                    group_ref.push().setValue(map);
                    editText.setText("");
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            }
        });


    }

    private void getUser() {

        reference.child(current).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    current_name=snapshot.child("name").getValue().toString();
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


        FirebaseUser currentuser=auth.getCurrentUser();
        if(currentuser!=null){

           updateUserStatus("online");

        }
        group_ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    displayMessage(snapshot);
                }

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

    private void displayMessage(DataSnapshot snapshot) {
        Iterator it=snapshot.getChildren().iterator();

        while(it.hasNext()){
            String chatdate=(String)((DataSnapshot)it.next()).getValue();
            String chatmessage=(String)((DataSnapshot)it.next()).getValue();
            String chatname=(String)((DataSnapshot)it.next()).getValue();
            String chattime=(String)((DataSnapshot)it.next()).getValue();

            textView.append(chatname+" : \n"+chatmessage+"\n"+chattime+"      "+chatdate+"\n\n\n");
            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
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
        reference.child(current).child("userState").updateChildren(onlineState);
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
