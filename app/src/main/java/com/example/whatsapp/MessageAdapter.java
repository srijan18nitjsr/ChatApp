package com.example.whatsapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    List<Messages> userMessageList;
    FirebaseAuth auth;
    DatabaseReference users_ref;

            public MessageAdapter(List<Messages> userMessageList )
            {
                this.userMessageList=userMessageList;
            }

    public  class  MessageViewHolder extends RecyclerView.ViewHolder{

        TextView sender,receiver;
        CircleImageView receiver_photo;
        ImageView sender_picture,receiver_picture;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            sender=itemView.findViewById(R.id.sender_text);
            receiver=itemView.findViewById(R.id.receive_text);
            receiver_photo=itemView.findViewById(R.id.message_profile_image);
            sender_picture=itemView.findViewById(R.id.sender_image);
            receiver_picture=itemView.findViewById(R.id.receiver_image);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout,parent,false);
        auth=FirebaseAuth.getInstance();
        return  new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {
                String sender_id=auth.getCurrentUser().getUid();
                Messages messages=userMessageList.get(position);
                String fromUserid=messages.getFrom();
                String fromMessageType=messages.getType();

                users_ref= FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserid);
                users_ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(snapshot.hasChild("image")) {
                            String profileimage = snapshot.child("image").getValue().toString();


                            Picasso.get().load(profileimage).placeholder(R.drawable.profile).into(holder.receiver_photo);
                        }

                        final String profilename=snapshot.child("name").getValue().toString();
                        String profilestatus=snapshot.child("status").getValue().toString();


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        holder.receiver_photo.setVisibility(View.GONE);
        holder.receiver.setVisibility(View.GONE);
        holder.sender.setVisibility(View.GONE);
        holder.receiver_picture.setVisibility(View.GONE);
        holder.sender_picture.setVisibility(View.GONE);




        if(fromMessageType.equals("text")){

                    if(fromUserid.equals(sender_id)){
                        holder.sender.setVisibility(View.VISIBLE);
                        holder.sender.setBackgroundResource(R.drawable.sender_messages_layout);
                        holder.sender.setText(messages.getMessage()+"\n \n"+messages.getDate()+" - "+messages.getTime());
                    }
                    else{

                        holder.receiver_photo.setVisibility(View.VISIBLE);
                        holder.receiver.setVisibility(View.VISIBLE);


                        holder.receiver.setBackgroundResource(R.drawable.receiver_messages_layout);
                        holder.receiver.setText(messages.getMessage()+"\n \n"+messages.getDate()+" - "+messages.getTime());

                    }
                }


                if(fromUserid.equals(sender_id)){
                    holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if(userMessageList.get(position).getType().equals("text")){

                                CharSequence[] options=new CharSequence[]{
                                        "Delete For Me",
                                        "Delete For Everyone",
                                        "Cancel"
                                };

                                AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                                builder.setTitle("Delete");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        if(which==0){

                                            deleteSentMessages(position,holder);
                                            Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                            holder.itemView.getContext().startActivity(intent);



                                        }
                                        else if(which==1){

                                            deleteMessagesForEveryone(position,holder);
                                            Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                            holder.itemView.getContext().startActivity(intent);

                                        }
                                        else if(which==2){

                                        }

                                    }
                                });
                                builder.show();
                            }
                            return false;
                        }
                    });
                }
                else{

                    holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if(userMessageList.get(position).getType().equals("text")){

                                CharSequence[] options=new CharSequence[]{
                                        "Delete For Me",
                                        "Cancel"
                                };

                                AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                                builder.setTitle("Delete");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        if(which==0){

                                            deleteReceivedMessages(position,holder);
                                            Intent intent=new Intent(holder.itemView.getContext(),MainActivity.class);
                                            holder.itemView.getContext().startActivity(intent);

                                        }
                                        else if(which==1){

                                        }


                                    }
                                });
                                builder.show();
                            }
                            return false;
                        }
                    });

                }

    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }


    private  void deleteSentMessages(final int position, final MessageViewHolder holder){

                DatabaseReference root_ref=FirebaseDatabase.getInstance().getReference();
                root_ref.child("Message").child(userMessageList.get(position).getFrom()).child(userMessageList.get(position).getTo()).child(userMessageList.get(position).getMessageID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                        }
                    }
                });

    }

    private  void deleteReceivedMessages(final int position, final MessageViewHolder holder) {

        DatabaseReference root_ref = FirebaseDatabase.getInstance().getReference();
        root_ref.child("Message").child(userMessageList.get(position).getTo()).child(userMessageList.get(position).getFrom()).child(userMessageList.get(position).getMessageID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                }
            }
        });


    }

    private  void deleteMessagesForEveryone(final int position, final MessageViewHolder holder) {

        final DatabaseReference root_ref = FirebaseDatabase.getInstance().getReference();
        root_ref.child("Message").child(userMessageList.get(position).getTo()).child(userMessageList.get(position).getFrom()).child(userMessageList.get(position).getMessageID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    root_ref.child("Message").child(userMessageList.get(position).getFrom()).child(userMessageList.get(position).getTo()).child(userMessageList.get(position).getMessageID()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                        }
                    });

                }
            }
        });
    }


    }
