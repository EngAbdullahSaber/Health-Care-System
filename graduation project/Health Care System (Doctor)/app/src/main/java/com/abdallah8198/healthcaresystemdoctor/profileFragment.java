package com.abdallah8198.healthcaresystemdoctor;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.usage.ExternalStorageStats;
import android.app.usage.StorageStats;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.abdallah8198.healthcaresystemdoctor.adapters.AdapterPosts;
import com.abdallah8198.healthcaresystemdoctor.models.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.security.Permissions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.firebase.storage.StorageReference;
import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;


public class profileFragment extends Fragment {

    // firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    //storage
    StorageReference storgeReference;

    //path where images of user profile and cover will be stored
    String sroragePath = "Users_Profile_Cover_Imgs/";

    //views
    ImageView avatertv ,coverIv;
    TextView nameTv , emailTv , phoneTv;
    FloatingActionButton fab ;
    RecyclerView postsRecyclerView;

    //progress dialoge
    ProgressDialog pd;

    //permission constant
    private  static  final  int CAMERA_REQUEST_CODE = 100;
    private  static  final  int STORAGE_REQUEST_CODE = 200;
    private  static  final  int IMAGE_PICK_GALLERY_CODE = 300;
    private  static  final  int IMAGE_PICK_CAMERA_CODE = 400;
    //arrays of permissiion s to be requseeted
    String cameraPermissions [];
    String storagePermissions[];

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;

    //uri of picked image
    Uri image_Uri;

    // for checking profile or cover photo
    String profileOrCoverPhot;
    public profileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference= firebaseDatabase.getReference("Users");
        storgeReference = getInstance().getReference();

        //init arrays of permission
        cameraPermissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //init views
        avatertv = view.findViewById(R.id.avaterIv);
        coverIv = view.findViewById(R.id.coverIv);
        nameTv = view.findViewById(R.id.nameTv);
        emailTv = view.findViewById(R.id.emailTv);
        phoneTv = view.findViewById(R.id.phoneTv);
        fab = view.findViewById(R.id.fab);
        postsRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_posts);

        //init progress dialoge
        pd = new ProgressDialog(getActivity());

        Query query= databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //check untill required data get
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    //get data
                    String name =""+ ds.child("name").getValue();
                    String email =""+ ds.child("email").getValue();
                    String phone =""+ ds.child("phone").getValue();
                    String image =""+ ds.child("image").getValue();
                    String cover =""+ ds.child("cover").getValue();

                    //set data
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    try {
                        // if image is received then set
                        Picasso.get().load(image).into(avatertv);
                    }catch ( Exception e){
                        //if there is exception  while getting image then set defult
                        Picasso.get().load(R.drawable.ic_defult_img_white).into(avatertv);
                    }
                    try {
                        // if image is received then set
                        Picasso.get().load(cover).into(coverIv);
                    }catch ( Exception e){
                        //if there is exception  while getting image then set defult

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //fab button click
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialoge();
            }
        });

        postList = new ArrayList<>();
        checkUserStatus();
        loadMyPosts();

        return view;
    }

    private void loadMyPosts() {
        //linearlayout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        postsRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    ModelPost modelPost = ds.getValue(ModelPost.class);
                    postList.add(modelPost);

                    adapterPosts = new AdapterPosts(getActivity(),postList);
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError e) {

                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void searchMyPosts(final String searchQuery) {
        //linearlayout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        postsRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    ModelPost modelPost = ds.getValue(ModelPost.class);
                    if (modelPost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelPost.getpDescr().toLowerCase().contains(searchQuery.toLowerCase()))
                    {
                        postList.add(modelPost);
                    }

                    adapterPosts = new AdapterPosts(getActivity(),postList);
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError e) {

                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    //for storage permission
    private  boolean checkStoragePermission(){
        // check if storage permission is enabled or not
        //return true if enabled
        //return false if not enabled
        boolean reslut= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return  reslut;
    }
    private void requestStoragePermission (){
        //request runtime storage permission
        requestPermissions(storagePermissions,STORAGE_REQUEST_CODE);
    }
    //for camera permission
    private  boolean checkCameraPermission(){
        // check if storage permission is enabled or not
        //return true if enabled
        //return false if not enabled
        boolean reslut = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA)
                ==(PackageManager.PERMISSION_GRANTED);
        boolean reslut1= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return  reslut && reslut1;
    }
    private void requestCameraPermission (){
        //request runtime storage permission
        requestPermissions(cameraPermissions,CAMERA_REQUEST_CODE);
    }

    private void showEditProfileDialoge() {
        // option to show dialoge
        String option[] ={"Edit Profile Picture","Edit Cover Photo"," Edit Name "," Edit Phone "};
        //alert dialoge
        AlertDialog.Builder bulider= new AlertDialog.Builder(getActivity());
        //set title
        bulider.setTitle("Choose Action ");
        bulider.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle dialoge item clicks
                if(which == 0){
                    // edit profile clicked
                    pd.setMessage("Updating Profile Picture");
                    profileOrCoverPhot="image";
                    showImagePictureDialoge();
                }else if (which == 1){
                    // edit cover clicked
                    pd.setMessage("Updating Cover photo");
                    profileOrCoverPhot = "cover";
                    showImagePictureDialoge();
                }else if (which == 2){
                    // edit name  clicked
                    pd.setMessage("Updating Name");
                    //calling method and press key "name" as parameter to update it's value in database
                    showNAmePhoneUpdateDialoge("name");
                }else if (which == 3){
                    // edit phone  clicked
                    pd.setMessage("Updating Phone");
                    showNAmePhoneUpdateDialoge("phone");
                }
            }
        });
        //create and show dialoge
        bulider.create().show();
    }
    private void showNAmePhoneUpdateDialoge(final String key) {
        /* parameter key will contain value
         * ethier "name" which is key in user's database which is used to update user's name
         * or  "phone" which is key in phone's database which is used to update user's phone  */
        //custom dialoge
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("update "+key);

        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        //add edit text
        final EditText editText = new EditText(getActivity());
        editText.setHint("Enter "+key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);


        //add button in dialoge
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input text from edit text
                final String  value = editText.getText().toString().trim();
                //validate if user have enter something or no
                if(!TextUtils.isEmpty(value)){
                    HashMap<String ,Object> result = new HashMap<>();
                    result.put(key , value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //update  ,dismiss progress
                                    pd.dismiss();
                                    Toast.makeText(getActivity() , "Updating....",Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //update  ,dismiss progress ,get and show error message
                                    pd.dismiss();
                                    Toast.makeText(getActivity() , ""+e.getMessage(),Toast.LENGTH_SHORT).show();

                                }
                            });
                    //if user edit his name also change it in post
                    if (key.equals("name")) {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    String child = ds.getKey();
                                    dataSnapshot.getRef().child(child).child("uName").setValue(value);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        // Update name in current users comments posts
                        ref.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren())
                                {
                                    String child = ds.getKey();
                                    if (dataSnapshot.child(child).hasChild("Comments"))
                                    {
                                        String child1 = ""+dataSnapshot.child(child).getKey();
                                        Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for (DataSnapshot ds : dataSnapshot.getChildren())
                                                {
                                                    String child = ds.getKey();
                                                    dataSnapshot.getRef().child(child).child("uName").setValue(value);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                }else {
                    Toast.makeText(getActivity(),"Please Enter" +key, Toast.LENGTH_SHORT).show();
                }
            }
        });
        //cancel button in dialoge
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        //create and show dialog
        builder.create().show();

    }
    private void showImagePictureDialoge() {
        String option[] ={"Camera ","Gallery"};
        //alert dialoge
        AlertDialog.Builder bulider= new AlertDialog.Builder(getActivity());
        //set title
        bulider.setTitle("Pick Image From ");
        bulider.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //handle dialoge item clicks
                if(which == 0){
                    // Camera clicked
                    if (!checkCameraPermission()){
                        requestCameraPermission();
                    }else {
                        pickFromCamera();
                    }

                }else if (which == 1){
                    // Gallery clicked
                    if (!checkStoragePermission()){
                        requestStoragePermission();
                    }else {
                        pickFromGallery();
                    }

                }
            }
        });
        //create and show dialoge
        bulider.create().show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        /*this method when user press allow or deny from permission request dialoge
         *we will handle permission case (Allow | Deny)
         */
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                //picking from camera first check if camera and storage permission allowed or not
                if (grantResults.length>0){
                    boolean cameraAccepted =grantResults[0]== PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageaAccepted =grantResults[1]== PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageaAccepted){
                        //permission accepted
                        pickFromCamera();
                    }else {
                        //permission denied
                        Toast.makeText(getActivity(),"Please enable Camera and Storage Permission  " ,Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
            case STORAGE_REQUEST_CODE:{

                //picking from gallery first check if camera and storage permission allowed or not
                if (grantResults.length>0){
                    boolean writeStorageaAccepted =grantResults[1]== PackageManager.PERMISSION_GRANTED;
                    if (  writeStorageaAccepted){
                        //permission accepted
                        pickFromGallery();
                    }else {
                        //permission denied
                        Toast.makeText(getActivity(),"Please enable  Storage Permission  " ,Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;

        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //this method  after picking image from camera or gallery
        if(resultCode ==RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                //image is picked from gallery , get uri of image
                image_Uri =data.getData();
                uploadProfileCoverPhoto(image_Uri);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE){
                //image is picked from camera  , get uri of image
                uploadProfileCoverPhoto(image_Uri);
            }


        }
    }
    private void uploadProfileCoverPhoto(Uri image_uri) {
        //show progress
        pd.show();
        /* instead of creating  function for profile picture and cover photo*/
        //path and name of image to be stored in firebase storge
        String filePathAndName = sroragePath+""+ profileOrCoverPhot+"_"+user.getUid();

        StorageReference storageReference2nd =storgeReference.child(filePathAndName);
        storageReference2nd.putFile(image_uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //image is upload to storage ,now get it's url and store in user's database
                        Task<Uri> uriTask =    taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        final Uri downLoadUri =uriTask.getResult();

                        //check if image is upload or not and url is received
                        if (uriTask.isSuccessful()){
                            //image is upload
                            //add | update in user's database
                            HashMap<String ,Object> results = new HashMap<>();
                            results.put(profileOrCoverPhot, downLoadUri.toString());
                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            //url in database of user is added successfully
                                            //dismiss progress bar
                                            pd.dismiss();
                                            Toast.makeText(getActivity(),"Image Updated...",Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // error adding url in database of user
                                    //dismiss progress bar
                                    pd.dismiss();
                                    Toast.makeText(getActivity(),"error  Updating image ...",Toast.LENGTH_SHORT).show();

                                }
                            });

                            //if user edit his name also change it in post
                            if (profileOrCoverPhot.equals("image")) {
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                                Query query = ref.orderByChild("uid").equalTo(uid);
                                query.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                            String child = ds.getKey();
                                            dataSnapshot.getRef().child(child).child("uDp").setValue(downLoadUri.toString());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                ///change current user image in comments
                                ref.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot ds : dataSnapshot.getChildren())
                                        {
                                            String child = ds.getKey();
                                            if (dataSnapshot.child(child).hasChild("Comments"))
                                            {
                                                String child1 = ""+dataSnapshot.child(child).getKey();
                                                Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                                child2.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        for (DataSnapshot ds : dataSnapshot.getChildren())
                                                        {
                                                            String child = ds.getKey();
                                                            dataSnapshot.getRef().child(child).child("uDp").setValue(downLoadUri.toString());
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                        else {
                            //error
                            pd.dismiss();
                            Toast.makeText(getActivity(),"Some error ocurred ", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //there were some error(s) , get and show error message ,dismiss progress dialoge
                pd.dismiss();
                Toast.makeText(getActivity(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void pickFromCamera() {
        //intent of picking image from device
        ContentValues values =new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");

        //put image uri
        image_Uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        //intent for start camera
        Intent cameraIntent = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_Uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);
    }
    private void pickFromGallery() {
        //pickfrom gallery
        Intent galleryIntent =  new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_CODE);
    }
    private void checkUserStatus(){
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user!=null){
            //user is signed in stay here
            //set email of logged in user

            uid = user.getUid();
        }else{
            //user is not signed ,go to MainActicity
            startActivity(new Intent(getActivity(),LoginActivity.class));
            getActivity().finish();
        }
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true); // to show menu option in fragment
        super.onCreate(savedInstanceState);
    }
    //inflate options menu
    @Override
    public void onCreateOptionsMenu(Menu menu , MenuInflater inflater) {
        //inflating enu
        inflater.inflate(R.menu.menu_main,menu);

        MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                if (!TextUtils.isEmpty(s))
                {
                    searchMyPosts(s);
                }else {
                    loadMyPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!TextUtils.isEmpty(s))
                {
                    searchMyPosts(s);
                }else {
                    loadMyPosts();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu ,inflater);
    }
    //handle menu item click
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id
        int id =item.getItemId();
        if(id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        if (id == R.id.action_add_post){
            startActivity(new Intent(getActivity(),AddPostActivity.class));

        }

        return super.onOptionsItemSelected(item);
    }

}
