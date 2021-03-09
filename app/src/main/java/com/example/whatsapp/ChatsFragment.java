package com.example.whatsapp;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    View PrivateChatView;
    RecyclerView chat_list;
    DatabaseReference chats_ref, contacts_ref, users_ref;
    String current;
    FirebaseAuth auth;
    FirebaseUser currentuser;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatView = inflater.inflate(R.layout.fragment_chats, container, false);
        auth = FirebaseAuth.getInstance();
        currentuser = auth.getCurrentUser();
        contacts_ref = FirebaseDatabase.getInstance().getReference().child("Contacts");

        if (currentuser != null) {
            current = auth.getCurrentUser().getUid();

            chats_ref = FirebaseDatabase.getInstance().getReference().child("Contacts").child(current);

        } else

            chats_ref = FirebaseDatabase.getInstance().getReference().child("Contacts");
        users_ref = FirebaseDatabase.getInstance().getReference().child("Users");
        chat_list = PrivateChatView.findViewById(R.id.chat_list);
        chat_list.setLayoutManager(new LinearLayoutManager(getContext()));

        return PrivateChatView;
    }

    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(chats_ref, Contacts.class).build();
        FirebaseRecyclerAdapter<Contacts, ChatHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ChatHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatHolder holder, int position, @NonNull Contacts model) {

                final String user_id = getRef(position).getKey();
                final String[] profileimage = {"default"};
                final String list_user_id = getRef(position).getKey();
                users_ref.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            if (snapshot.hasChild("image")) {
                                profileimage[0] = snapshot.child("image").getValue().toString();
                                Picasso.get().load(profileimage[0]).placeholder(R.drawable.profile).into(holder.photo);
                            }

                            final String profilename = snapshot.child("name").getValue().toString();
                            String profilestatus = snapshot.child("status").getValue().toString();

                            holder.username.setText(profilename);

                            if (snapshot.child("userState").hasChild("state")) {

                                String state = snapshot.child("userState").child("state").getValue().toString();
                                String date = snapshot.child("userState").child("date").getValue().toString();
                                String time = snapshot.child("userState").child("time").getValue().toString();

                                if (state.equals("online")) {
                                    holder.userstatus.setText("online");
                                    holder.online.setVisibility(View.VISIBLE);
                                } else if (state.equals("offline")) {
                                    holder.userstatus.setText("Last Seen: " + date + "  " + time);
                                    holder.online.setVisibility(View.INVISIBLE);
                                }
                            } else {
                                holder.userstatus.setText("offline");
                                holder.online.setVisibility(View.INVISIBLE);
                            }

                            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {

                                    CharSequence[] sequences = new CharSequence[]
                                            {
                                                    "View Profile",
                                                    "Remove this friend",
                                                    "Cancel"
                                            };

                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    //builder.setTitle("Do you want to remove " + profilename);
                                    builder.setItems(sequences, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            if(which==0){
                                                Intent intent = new Intent(getContext(), ProfileActivity.class);
                                                intent.putExtra("visit_user_id", user_id);
                                                startActivity(intent);
                                            }

                                            if (which == 1) {

                                                contacts_ref.child(current).child(list_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            contacts_ref.child(list_user_id).child(current).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {

                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });

                                            } else if (which == 2) {

                                            }
                                        }
                                    });
                                    builder.show();
                                    return true;
                                }
                            });

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getContext(), ChatActivity.class);
                                    intent.putExtra("visit_user_id", user_id);
                                    intent.putExtra("visit_user_name", profilename);
                                    intent.putExtra("visit_user_image", profileimage[0]);
                                    startActivity(intent);
                                }
                            });


                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @NonNull
            @Override
            public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                return new ChatHolder(view);
            }
        };
        chat_list.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatHolder extends RecyclerView.ViewHolder {
        TextView username, userstatus;
        CircleImageView photo;
        ImageView online;

        public ChatHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            userstatus = itemView.findViewById(R.id.userstatus);
            photo = itemView.findViewById(R.id.view_image);
            online=itemView.findViewById(R.id.onlineState);
        }
    }


}
