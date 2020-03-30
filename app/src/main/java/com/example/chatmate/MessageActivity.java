package com.example.chatmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatmate.Adapter.MessageAdapter;
import com.example.chatmate.Model.ChatMessage;
import com.example.chatmate.Model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username,status;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    Intent intent;
    FloatingActionButton btnSend;
    EditText etMessage;
    User user;
    String userId;

    MessageAdapter messageAdapter;
    List<ChatMessage> chatList;
    RecyclerView recyclerView;
    ValueEventListener seenListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.tvUsername);
        btnSend = findViewById(R.id.btnSend);
        etMessage = findViewById(R.id.etMessage);
        status = findViewById(R.id.tvStatus);

        intent = getIntent();

         userId = intent.getStringExtra("userId");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        user = new User();

        etMessage.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(keyboardShown(etMessage.getRootView())){
                    showTyping("typing");
                }
                else{
                    showTyping("notTyping");
                }
            }
        });

        assert userId != null;
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user.getStatus().equals("online")) {
                    status.setVisibility(View.VISIBLE);
                    status.setText("ONLINE");

                    if(user.getTyping().equals("typing"))  {
                        status.setVisibility(View.VISIBLE);
                        status.setText("typing...");
                    }

                    else {
                        status.setText("ONLINE");
                    }
                }
                if(user.getStatus().equals("offline")) {
                    status.setVisibility(View.VISIBLE);
                    status.setText("OFFLINE");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());

                readMessages(firebaseUser.getUid(),userId);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        recyclerView = findViewById(R.id.rvMessage);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = etMessage.getText().toString();
                if(message != null){
                    sendMessage(firebaseUser.getUid(),userId,message);
                }
                else {Toast.makeText(MessageActivity.this, "Nothing to send", Toast.LENGTH_SHORT).show();}
                etMessage.setText("");
            }
        });

        seenMessage(userId);

    }



    private void sendMessage (String sender, String receiver, String message){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        hashMap.put("isSeen",false);

        reference.child("Chats").push().setValue(hashMap);

    }

    private void readMessages(final String myId, final String userId){
        chatList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
               for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                   ChatMessage chatMessage = snapshot.getValue(ChatMessage.class);
                   if(chatMessage.getReceiver().equals(myId) && chatMessage.getSender().equals(userId) || chatMessage.getReceiver().equals(userId) && chatMessage.getSender().equals(myId)){
                       chatList.add(chatMessage);
                   }

                   messageAdapter = new MessageAdapter(MessageActivity.this,chatList);
                   recyclerView.setAdapter(messageAdapter);
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("status",status);


        reference.updateChildren(hashMap);

    }

    private void showTyping(String typing){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("typing",typing);


        reference.updateChildren(hashMap);

    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
        reference.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    private boolean keyboardShown(View rootView) {

        final int softKeyboardHeight = 100;
        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        DisplayMetrics dm = rootView.getResources().getDisplayMetrics();
        int heightDiff = rootView.getBottom() - r.bottom;
        return heightDiff > softKeyboardHeight * dm.density;
    }

    private void seenMessage(final String userid){
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ChatMessage chatMessage = snapshot.getValue(ChatMessage.class);
                    if(chatMessage.getReceiver().equals(firebaseUser.getUid()) && chatMessage.getSender().equals(userid)){
                      HashMap<String,Object> hashMap = new HashMap<>();
                      hashMap.put("isSeen",true);
                      snapshot.getRef().updateChildren(hashMap);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
