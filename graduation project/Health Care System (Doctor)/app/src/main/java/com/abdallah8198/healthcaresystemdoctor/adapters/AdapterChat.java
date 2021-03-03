package com.abdallah8198.healthcaresystemdoctor.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.abdallah8198.healthcaresystemdoctor.models.ModelChat;
import com.abdallah8198.healthcaresystemdoctor.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.zip.DataFormatException;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {



    private static final int MSG_TYPE_LEFT=0;
    private static final int MSG_TYPE_RIGHT=1;
    Context context;
    List<ModelChat> chatList;
    String imageUrl;

    FirebaseUser fUser;



    public AdapterChat(Context context, List<ModelChat> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i==MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right,viewGroup,false);
            return new MyHolder(view);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left,viewGroup,false);
            return new MyHolder(view);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, final int i) {
        //get data
        String message=chatList.get(i).getMessage();
        String timeStamp=chatList.get(i).getTimestamp();

        //convert time stamp to dd/mm/yyyy hh:mm am/pm
        Calendar cal=Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timeStamp));
        String dataTime = DateFormat.format("dd/mm/yyyy hh:mm aa" ,cal).toString();


        //set data
        myHolder.messageTv.setText(message);
        myHolder.timeTv.setText(dataTime);
        try {
            Picasso.get().load(imageUrl).into(myHolder.profileIv);
        }catch (Exception e){

        }

        //click to show delete dialog
        myHolder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show delete message confirm dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete Message");
                builder.setMessage("Are you sure to delete this message ? ");
                //delete button
                builder.setPositiveButton("Delete ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessage(i);
                    }
                });
                //cancel delete button
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss Dialog
                        dialog.dismiss();
                    }
                });
                //create and show dialoge
                builder.create().show();
            }
        });

        //set seen /deliveried status of message
        if(i==chatList.size()-1){
            if (chatList.get(i).isSeen()) {
                myHolder.isSeenTv.setText("Seen");
            }else {
                myHolder.isSeenTv.setText("Delivered");
            }
        }else {
            myHolder.isSeenTv.setVisibility(View.GONE);
        }
    }

    private void deleteMessage( int position ) {
        final String myUID =FirebaseAuth.getInstance().getCurrentUser().getUid();


        /**logic
         * get TimeStamp of Clicked Message
         * compare timestamp of clicked message with all message in Chats
         * where both value matches delete this message
         */
        String msTimeStamp =  chatList.get(position).getTimestamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = dbRef.orderByChild("timestamp").equalTo(msTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    /**
                     * if you want to allow sender to delete only this message then
                     * compare sender value with current user's uid
                     * if they match means its message of sender that is trying to delete
                     */
                    if (ds.child("sender").getValue().equals(myUID)) {


                        /**
                         * we can do one of two things here
                         * 1)Remove message from Chats
                         * 2)set the Value of message "this message was delted
                         * so do whatever you want "
                         */
                        // 1)remove the message from chats
                        ds.getRef().removeValue();
                        // 2)set the value of this message " this message was deleted.... "
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("message", "this message was deleted....");
                        ds.getRef().updateChildren(hashMap);
                        Toast.makeText(context, "message deleted......", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(context, "you can only delete your message ", Toast.LENGTH_SHORT).show();

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }



    @Override
    public int getItemViewType(int position) {
        //get currently signed in user
        fUser= FirebaseAuth.getInstance().getCurrentUser();
        if(chatList.get(position).getSender().equals(fUser.getUid())){
            return MSG_TYPE_RIGHT;
        }else
        {
            return MSG_TYPE_LEFT;
        }

    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder{

        //views
        ImageView profileIv;
        TextView messageTv, timeTv,isSeenTv;
        LinearLayout messageLayout;// for click listener to show delete
        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views
            profileIv =itemView.findViewById(R.id.profileTv);
            messageTv =itemView.findViewById(R.id.messageTv);
            timeTv =itemView.findViewById(R.id.timeTv);
            isSeenTv =itemView.findViewById(R.id.isSeenTv);
            messageLayout =itemView.findViewById(R.id.messageLayout);

        }
    }
}
