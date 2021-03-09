package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;

    FirebaseAuth auth;
    DatabaseReference reference;
    String current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar=findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("MyChatApp");

        myViewPager=findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdapter=new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);

        myTabLayout=findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);
        reference= FirebaseDatabase.getInstance().getReference();
        auth=FirebaseAuth.getInstance();

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentuser=auth.getCurrentUser();
      if(currentuser==null){
           startActivity(new Intent(MainActivity.this,LoginActivity.class));
       }
      else{
          updateUserStatus("online");
          verifyUser();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentuser=auth.getCurrentUser();
        if(currentuser!=null){
            updateUserStatus("offline");
        }
    }

    private void verifyUser() {
        String current=auth.getCurrentUser().getUid();
        reference.child("Users").child(current).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if((snapshot.child("name").exists())){
                    Toast.makeText(MainActivity.this,"Welcome",Toast.LENGTH_SHORT).show();
                }
                else{
                    Intent intent=new Intent(MainActivity.this,SettingsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);

         getMenuInflater().inflate(R.menu.menu_options,menu);

         return  true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);

         if(item.getItemId()==R.id.logout){
             updateUserStatus("offline");
             auth.signOut();
             startActivity(new Intent(MainActivity.this,LoginActivity.class));

         }

        if(item.getItemId()==R.id.settings){
            startActivity(new Intent(MainActivity.this,SettingsActivity.class));

        }

        if(item.getItemId()==R.id.friends){
            startActivity(new Intent(MainActivity.this,FindFriendActivity.class));

        }

        if(item.getItemId()==R.id.group){
            createGroup();

        }


         return true;
    }

    private void createGroup() {
        AlertDialog.Builder ad=new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog).setTitle("Enter Group Name : ");
        final EditText gr=new EditText(MainActivity.this);
        gr.setHint("e.g. My Group");
        ad.setView(gr);

        ad.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String grname=gr.getText().toString();
                if(TextUtils.isEmpty(grname)){
                    Toast.makeText(MainActivity.this,"Enter group name",Toast.LENGTH_SHORT).show();
                }
                else{
                    reference.child("Group").child(grname).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(MainActivity.this,grname+" group is created",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
        ad.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        ad.show();
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
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}
