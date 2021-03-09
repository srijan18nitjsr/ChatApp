package com.example.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView friends;
    DatabaseReference reference;
    FirebaseAuth auth;
    String current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);
        toolbar=findViewById(R.id.find_friend);
        toolbar.setTitle("Find Friends");
        friends=findViewById(R.id.friends_name);
        friends.setLayoutManager(new LinearLayoutManager(FindFriendActivity.this));
        reference= FirebaseDatabase.getInstance().getReference().child("Users");
        auth= FirebaseAuth.getInstance();

    }

    @Override
    protected void onStart() {


        FirebaseUser currentuser=auth.getCurrentUser();
        if(currentuser!=null){

           updateUserStatus("online");

        }
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(reference,Contacts.class).build();
        FirebaseRecyclerAdapter<Contacts,FindFriendViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull Contacts model) {
                holder.username.setText(model.getName());
                holder.userstatus.setText(model.getStatus());
                Picasso.get().load(model.getImage()).placeholder(R.drawable.profile).into(holder.photo);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id=getRef(position).getKey();
                        Intent intent=new Intent(FindFriendActivity.this,ProfileActivity.class);
                        intent.putExtra("visit_user_id",visit_user_id);
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                FindFriendViewHolder holder=new FindFriendViewHolder(view);
                return holder;
            }
        };
        friends.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FindFriendViewHolder extends RecyclerView.ViewHolder{

        TextView username,userstatus;
        CircleImageView photo;
        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.username);
            userstatus=itemView.findViewById(R.id.userstatus);
            photo=itemView.findViewById(R.id.view_image);
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
