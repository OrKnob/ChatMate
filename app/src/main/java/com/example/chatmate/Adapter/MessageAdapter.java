package com.example.chatmate.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatmate.MessageActivity;
import com.example.chatmate.Model.ChatMessage;
import com.example.chatmate.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

private Context context;
private List<ChatMessage> chatList;
public static final int MSG_SEND = 1 ;
    public static final int MSG_RECEIVE = 0;
    FirebaseUser firebaseUser;
    AlertDialog alertDialog;


public MessageAdapter(Context context, List<ChatMessage> chatList){
        this.context = context;
        this.chatList = chatList;


        }

@NonNull
@Override
public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    if (viewType == MSG_SEND) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_send, parent, false);
        return new MessageAdapter.ViewHolder(view);
    }
    else {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_receive, parent, false);
    return new MessageAdapter.ViewHolder(view);
    }
}

@Override
public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    ChatMessage chatMessage = chatList.get(position);
    holder.showMessage.setText(chatMessage.getMessage());

    holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setMessage("Are you sure you want to delete the message?");
            alertDialog.setButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            alertDialog.setButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.cancel();
                }
            });
            return false;
        }
    });

    if(position == chatList.size()-1){
        if(chatMessage.isSeen()){
            holder.seenStatus.setText("seen");
        }
        else{
            holder.seenStatus.setText("Delivered");
        }
    }
    else{
        holder.seenStatus.setVisibility(View.INVISIBLE);
    }

        }

@Override
public int getItemCount() {
        return chatList.size();
        }

public class ViewHolder extends RecyclerView.ViewHolder{

    public TextView showMessage,seenStatus;



    public ViewHolder(@NonNull View itemView) {
        super(itemView);

        showMessage = itemView.findViewById(R.id.showMessage);
        seenStatus = itemView.findViewById(R.id.tvSeen);

    }
}

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(chatList.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_SEND;
        }
        else return MSG_RECEIVE;
    }

    }
