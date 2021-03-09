package com.example.whatsapp;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {

    View groupview;
    ListView listview;
    DatabaseReference reference;
    ArrayAdapter<String> aad;
    ArrayList<String> arr =new ArrayList<>();



    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        groupview= inflater.inflate(R.layout.fragment_groups, container, false);

        reference= FirebaseDatabase.getInstance().getReference().child("Group");
        listview=groupview.findViewById(R.id.lview);
        displayGroup();
        aad=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,arr);
        listview.setAdapter(aad);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String currentGroup=parent.getItemAtPosition(position).toString();
                Intent intent=new Intent(getContext(),GroupChatActivity.class);
                intent.putExtra("groupname",currentGroup);
                startActivity(intent);
            }
        });
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {

                CharSequence[] sequences=new CharSequence[]
                        {
                                "Remove",
                                "Cancel"
                        };

                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                builder.setTitle("Do you want to remove this group");
                builder.setItems(sequences, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        if(which==0){

                            String currentGroup=parent.getItemAtPosition(position).toString();
                            reference.child(currentGroup).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                }
                            });
                        }
                        else if(which==1){

                        }
                    }
                });
                builder.show();

                return true;
            }
        });

        return  groupview;
    }

    private void displayGroup() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> set=new HashSet<>();
                Iterator it=snapshot.getChildren().iterator();
                while(it.hasNext()){
                    set.add(((DataSnapshot)it.next()).getKey());
                    Log.i("jh","hello");


                }
                arr.clear();
                arr.addAll(set);

                aad.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }





}
