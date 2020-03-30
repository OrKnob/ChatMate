package com.example.chatmate.Fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.chatmate.Adapter.UserAdapter;
import com.example.chatmate.Model.ChatMessage;
import com.example.chatmate.Model.User;
import com.example.chatmate.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import java.util.Iterator;
import java.util.List;

public class ChatsFragment extends Fragment {
    RecyclerView rvRecentChats;
    FirebaseUser firebaseUser;
    List<User> mUsers;
    UserAdapter userAdapter;
    DatabaseReference reference;
    List<String> userList;
    Iterator<User> iterator;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        rvRecentChats = view.findViewById(R.id.rvRecentChats);
        rvRecentChats.setHasFixedSize(true);
        rvRecentChats.setLayoutManager(new LinearLayoutManager(getContext()));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        userList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ChatMessage chatMessage = snapshot.getValue(ChatMessage.class);
                    if(chatMessage.getSender().equals(firebaseUser.getUid())){
                        userList.add(chatMessage.getReceiver());
                    }
                    if(chatMessage.getReceiver().equals(firebaseUser.getUid())){
                        userList.add(chatMessage.getSender());
                    }

                }


                readChats();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        return view;
    }

    private void readChats() {

        mUsers = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();

               for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                   User user = snapshot.getValue(User.class);

                   for(String id: userList){

                       if(user.getId().equals(id)){
                           if(mUsers.size()!=0){
                               if(!mUsers.contains(user)){
                                   mUsers.add(user);
                               }

                               }
                           else{
                               mUsers.add(user);
                           }
                           }

                       }

                           /*if (user.getId().equals(id)) {
                               if (mUsers.size() != 0) {

                                   for(int i = 0;i<mUsers.size();i++){
                                       User user1 = mUsers.get(i);
                                     if(!user.getId().equals(user1.getId())){
                                         mUsers.add(user);
                                     }
                                   }

                                   *//*for(User user1 : mUsers){
                                       if (!user.getId().equals(user1.getId())) {
                                           mUsers.add(user);
                                       }
                                   }*//*
                               } else {
                                   mUsers.add(user);
                               }
                           }*/



               }
               userAdapter = new UserAdapter(getContext(),mUsers,true);
               rvRecentChats.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
