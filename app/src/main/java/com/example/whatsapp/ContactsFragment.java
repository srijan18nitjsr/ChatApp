package com.example.whatsapp;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {

    View ContactsView;
    DatabaseReference contacts_ref,users_ref;
    private RecyclerView myContactList;
    FirebaseAuth auth;
    String current;


    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ContactsView= inflater.inflate(R.layout.fragment_contacts, container, false);
        myContactList=ContactsView.findViewById(R.id.contacts_list);
        myContactList.setLayoutManager(new LinearLayoutManager(getContext()));
        auth=FirebaseAuth.getInstance();
        current=auth.getCurrentUser().getUid();
        contacts_ref= FirebaseDatabase.getInstance().getReference().child("Contacts").child(current);
        users_ref= FirebaseDatabase.getInstance().getReference().child("Users");
        return  ContactsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options=new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(contacts_ref,Contacts.class).build();
        FirebaseRecyclerAdapter<Contacts,ContactsHolder> adapter=new FirebaseRecyclerAdapter<Contacts, ContactsHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsHolder holder, int position, @NonNull Contacts model) {
                String user_ids=getRef(position).getKey();
                users_ref.child(user_ids).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()&& snapshot.hasChild("image")){
                            String profileimage=snapshot.child("image").getValue().toString();
                            String profilename=snapshot.child("name").getValue().toString();
                            String profilestatus=snapshot.child("status").getValue().toString();

                            Picasso.get().load(profileimage).placeholder(R.drawable.profile).into(holder.photo);
                            holder.username.setText(profilename);
                            holder.userstatus.setText(profilestatus);

                        }
                        else {

                            String profilename=snapshot.child("name").getValue().toString();
                            String profilestatus=snapshot.child("status").getValue().toString();

                            holder.username.setText(profilename);
                            holder.userstatus.setText(profilestatus);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }

            @NonNull
            @Override
            public ContactsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                ContactsHolder holder=new ContactsHolder(view);
                return holder;

            }
        };
        myContactList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ContactsHolder extends  RecyclerView.ViewHolder{

        TextView username,userstatus;
        CircleImageView photo;
        public ContactsHolder(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.username);
            userstatus=itemView.findViewById(R.id.userstatus);
            photo=itemView.findViewById(R.id.view_image);
        }
    }
}
