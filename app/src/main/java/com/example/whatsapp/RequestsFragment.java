package com.example.whatsapp;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
public class RequestsFragment extends Fragment {

    View RequestsFragmentView;
    RecyclerView myRequestList;
    DatabaseReference chat_req_ref,users_ref,contacts_ref;
    String current;
    FirebaseAuth auth;



    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestsFragmentView= inflater.inflate(R.layout.fragment_requests, container, false);
        chat_req_ref= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contacts_ref= FirebaseDatabase.getInstance().getReference().child("Contacts");
        users_ref= FirebaseDatabase.getInstance().getReference().child("Users");
        auth=FirebaseAuth.getInstance();
        current=auth.getCurrentUser().getUid();
        myRequestList=RequestsFragmentView.findViewById(R.id.request_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));
        return RequestsFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options=new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(chat_req_ref.child(current),Contacts.class).build();
        FirebaseRecyclerAdapter<Contacts,RequestsHolder> adapter=new FirebaseRecyclerAdapter<Contacts, RequestsHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestsHolder holder, int position, @NonNull Contacts model) {

                holder.itemView.findViewById(R.id.accept).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.remove).setVisibility(View.VISIBLE);
                final String list_user_id=getRef(position).getKey();
                DatabaseReference getTypeRef=getRef(position).child("request_type").getRef();

                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(snapshot.exists()){
                            String type=snapshot.getValue().toString();
                            if(type.equals("received")){

                                users_ref.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        if(snapshot.hasChild("image")) {
                                            String profileimage = snapshot.child("image").getValue().toString();
                                            Picasso.get().load(profileimage).placeholder(R.drawable.profile).into(holder.photo);
                                        }
                                            final String profilename=snapshot.child("name").getValue().toString();
                                            String profilestatus=snapshot.child("status").getValue().toString();

                                            holder.username.setText(profilename);
                                            holder.userstatus.setText("wants to connect with you.");

                                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    CharSequence[] sequences=new CharSequence[]
                                                            {
                                                                    "Accept",
                                                                    "Cancel"
                                                            };

                                                    AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                                    builder.setTitle(profilename+" Chat Request");
                                                    builder.setItems(sequences, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                            if(which==0){

                                                                contacts_ref.child(current).child(list_user_id).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                        if(task.isSuccessful()){

                                                                            contacts_ref.child(list_user_id).child(current).child("Contact").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    if(task.isSuccessful()){

                                                                                        chat_req_ref.child(current).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if(task.isSuccessful()){

                                                                                                    chat_req_ref.child(list_user_id).child(current).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if(task.isSuccessful()){

                                                                                                                Toast.makeText(getContext(),"New Friend Added",Toast.LENGTH_SHORT).show();

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

                                                                    }
                                                                });

                                                            }
                                                            if(which==1){

                                                                chat_req_ref.child(current).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful()){

                                                                            chat_req_ref.child(list_user_id).child(current).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful()){

                                                                                        Toast.makeText(getContext(),"Friend Removed",Toast.LENGTH_SHORT).show();

                                                                                    }

                                                                                }
                                                                            });

                                                                        }

                                                                    }
                                                                });

                                                            }
                                                        }
                                                    });
                                                    builder.show();
                                                }
                                            });




                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });


                            }

                            else if(type.equals("sent")){
                                Button sent_request=holder.itemView.findViewById(R.id.accept);
                                sent_request.setText("Req. Sent");
                                holder.itemView.findViewById(R.id.remove).setVisibility(View.INVISIBLE);

                                users_ref.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                                        if(snapshot.hasChild("image")) {
                                            String profileimage = snapshot.child("image").getValue().toString();
                                            Picasso.get().load(profileimage).placeholder(R.drawable.profile).into(holder.photo);
                                        }
                                        final String profilename=snapshot.child("name").getValue().toString();
                                        String profilestatus=snapshot.child("status").getValue().toString();

                                        holder.username.setText(profilename);
                                        holder.userstatus.setText("you sent a request to "+profilename);

                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                CharSequence[] sequences=new CharSequence[]
                                                        {
                                                                "Cancel Sent Request"
                                                        };

                                                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                                builder.setTitle(profilename+" Chat Request");
                                                builder.setItems(sequences, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {


                                                        if(which==0){

                                                            chat_req_ref.child(current).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()){

                                                                        chat_req_ref.child(list_user_id).child(current).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful()){

                                                                                    Toast.makeText(getContext(),"You cancelled the sent request..",Toast.LENGTH_SHORT).show();

                                                                                }

                                                                            }
                                                                        });

                                                                    }

                                                                }
                                                            });

                                                        }
                                                    }
                                                });
                                                builder.show();
                                            }
                                        });




                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @NonNull
            @Override
            public RequestsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                RequestsHolder holder=new RequestsHolder(view);
                return  holder;
            }
        };

        myRequestList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestsHolder extends  RecyclerView.ViewHolder{

        TextView username,userstatus;
        CircleImageView photo;
        Button accept,cancel;
        public RequestsHolder(@NonNull View itemView) {
            super(itemView);
            username=itemView.findViewById(R.id.username);
            userstatus=itemView.findViewById(R.id.userstatus);
            photo=itemView.findViewById(R.id.view_image);
            accept=itemView.findViewById(R.id.accept);
            cancel=itemView.findViewById(R.id.remove);
        }
    }
}
