package com.example.firebaseapp.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseapp.AddPostActivity;
import com.example.firebaseapp.PostDetailActivity;
import com.example.firebaseapp.ThereProfileActivity;
import com.example.firebaseapp.models.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.R;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;


import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder> {

    // view holder class

    Context context;
    List<ModelPost> list;
    String myUid;

    private DatabaseReference likeRef;
    private DatabaseReference postRef;

    boolean mProcessLike = false;

    public AdapterPosts(Context context, List<ModelPost> list) {
        this.context = context;
        this.list = list;

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        likeRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(com.example.firebaseapp.R.layout.row_posts,viewGroup,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder myHolder, final int i) {

        //get date
        final String uid = list.get(i).getUid();
        String uEmail = list.get(i).getuEmail();
        String uName = list.get(i).getuName();
        String uDp = list.get(i).getuDp();
        final String pId = list.get(i).getpId();
        String pTitle = list.get(i).getpTitle();
        String pDescription = list.get(i).getpDescr();
        final String pImage = list.get(i).getpImage();
        String pTimeStamp = list.get(i).getpTime();
        String pLikes = list.get(i).getpLikes();
        String pComments = list.get(i).getpComments();

        //convert timesatamp to dd/mm/YYYY hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime = DateFormat.format("dd/mm/yyyy hh:mm aa",calendar).toString();

        myHolder.pDescriptionTv.setText(pDescription);
        myHolder.pTitleTv.setText(pTitle);
        myHolder.pTimeTv.setText(pTime);
        myHolder.uNameTv.setText(uName);
        myHolder.pLikesTv.setText(pLikes+" Likes");
        myHolder.pCommentsTv.setText(pComments+" Comments");

        setLikes(myHolder,pId);
        //set user picture
        try {
            Picasso.get().load(uDp).placeholder(com.example.firebaseapp.R.drawable.ic_defult_img).into(myHolder.uPictureIv);
        }catch (Exception e){
        }
        //set post image
        if (pImage.equals("noImage")){
            myHolder.pImageIv.setVisibility(View.GONE);
        }else {

            myHolder.pImageIv.setVisibility(View.VISIBLE);
            try {
                Picasso.get().load(pImage).into(myHolder.pImageIv);
            }catch (Exception e){
            }
        }

        myHolder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {

                showMoreOptions(myHolder.moreBtn,uid,myUid,pId,pImage);
            }
        });
        myHolder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int pLikes = Integer.parseInt(list.get(i).getpLikes());
                ///////
                mProcessLike = true;
                final String podtId = list.get(i).getpId();
                likeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (mProcessLike)
                        {
                            if (dataSnapshot.child(podtId).hasChild(myUid))
                            {
                                postRef.child(podtId).child("pLikes").setValue(""+(pLikes-1));
                                likeRef.child(podtId).child(myUid).removeValue();
                                mProcessLike=false;
                            }else
                            {
                                postRef.child(podtId).child("pLikes").setValue(""+(pLikes+1));
                                likeRef.child(podtId).child(myUid).setValue("Liked");
                                mProcessLike=false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        myHolder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start activity post detail

                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId",pId);
                context.startActivity(intent);
            }
        });
        myHolder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //implement dialog later
                Toast.makeText(context, "share is clicked", Toast.LENGTH_SHORT).show();
            }
        });
        myHolder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, ThereProfileActivity.class);
                intent.putExtra("uid",uid);
                context.startActivity(intent);
            }
        });
    }

    private void setLikes(final MyHolder holder, final String postKey) {
        likeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postKey).hasChild(myUid))
                {
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(com.example.firebaseapp.R.drawable.ic_liked,0,0,0);
                    holder.likeBtn.setText("Liked");
                }else
                {
                    holder.likeBtn.setCompoundDrawablesWithIntrinsicBounds(com.example.firebaseapp.R.drawable.ic_like_black,0,0,0);
                    holder.likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, final String pId, final String pImage) {
        //crate popup menu
        PopupMenu popupMenu = new PopupMenu(context,moreBtn, Gravity.END);
        //show only options for sign up
        if (uid.equals(myUid)){
            //add item in menu
            popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");
            popupMenu.getMenu().add(Menu.NONE,1,0,"Edit");
        }
        popupMenu.getMenu().add(Menu.NONE,2,0,"View Details");
        //item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                int id = menuItem.getItemId();
                if (id==0)
                {
                    //delete post
                    beingDelete(pId,pImage);
                }else if (id==1)
                {
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key","editPost");
                    intent.putExtra("editPostId",pId);
                    context.startActivity(intent);
                }
                else if (id==2)
                {
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId",pId);
                    context.startActivity(intent);
                }
                return false;
            }
        });
        //show popup
        popupMenu.show();
    }

    private void beingDelete(String pId, String pImage) {
        if (pImage.equals("noImage"))
        {
            deleteWithoutImage(pId);
        }else {
            deleteWithImage(pId,pImage);
        }
    }

    private void deleteWithImage(final String pId, String pImage) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting..");

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Query fQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren())
                        {
                            ds.getRef().removeValue();
                        }
                        Toast.makeText(context, "deleted successfully..", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteWithoutImage(String pId) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Deleting..");

        Query fQuery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    ds.getRef().removeValue();
                }
                Toast.makeText(context, "deleted successfully..", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyHolder extends RecyclerView.ViewHolder
    {
        ImageView uPictureIv,pImageIv;
        TextView uNameTv,pTitleTv,pDescriptionTv,pTimeTv,pLikesTv,pCommentsTv;
        Button likeBtn,commentBtn,shareBtn;
        ImageButton moreBtn;
        LinearLayout profileLayout;
        public MyHolder(@NonNull View itemView) {
            super(itemView);

            uPictureIv = (ImageView) itemView.findViewById(com.example.firebaseapp.R.id.uPictureIv);
            pImageIv = (ImageView) itemView.findViewById(com.example.firebaseapp.R.id.pImageIv);
            uNameTv = (TextView) itemView.findViewById(com.example.firebaseapp.R.id.uNameTv);
            pTimeTv = (TextView) itemView.findViewById(com.example.firebaseapp.R.id.pTimeTv);
            pTitleTv = (TextView) itemView.findViewById(com.example.firebaseapp.R.id.pTitleTv);
            pDescriptionTv = (TextView) itemView.findViewById(com.example.firebaseapp.R.id.pDescriptionTv);
            pLikesTv = (TextView) itemView.findViewById(com.example.firebaseapp.R.id.pLikesTv);
            pCommentsTv = (TextView) itemView.findViewById(com.example.firebaseapp.R.id.pCommentsTv);
            likeBtn = (Button) itemView.findViewById(com.example.firebaseapp.R.id.likeBtn);
            commentBtn = (Button) itemView.findViewById(com.example.firebaseapp.R.id.commentBtn);
            shareBtn = (Button) itemView.findViewById(com.example.firebaseapp.R.id.shareBtn);
            moreBtn = (ImageButton) itemView.findViewById(com.example.firebaseapp.R.id.moreBtn);
            profileLayout = (LinearLayout) itemView.findViewById(com.example.firebaseapp.R.id.profileLayout);
        }
    }
}

