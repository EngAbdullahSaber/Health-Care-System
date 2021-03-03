package com.example.firebaseapp;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebaseapp.adapters.AdapterUsers;
import com.example.firebaseapp.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {

    //firebase auth
    FirebaseAuth firebaseAuth;

    RecyclerView recyclerView;
    AdapterUsers adapterUsers;
    List<ModelUsers> usersList;

    public UsersFragment() {
        // Required empty public constructor
    }



    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_users, container, false);

        //inti
        firebaseAuth= FirebaseAuth.getInstance();

        //init recycleview
        recyclerView = view.findViewById(R.id.users_recyclerView);
        //set it's properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //init user list
        usersList=new ArrayList<>();

        //get all users
        getAllUers();

        return view;
    }

    private void getAllUers() {
        //get current user
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        //get path of database named "Users" containg users info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //get alldata from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot ds :dataSnapshot.getChildren()){
                    ModelUsers modelUsers = ds.getValue(ModelUsers.class);
                            //get all users except currently signed in user
                    if ( !modelUsers.getUid().equals(fUser.getUid()) ) {

                        if (modelUsers.getKind().length() !=0  ) {
                            usersList.add(modelUsers);
                        }
                    }
                    // adapter
                    adapterUsers = new AdapterUsers(getActivity(), usersList);
                    //set adapter to recycler view
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void searchUsers(final String query) {
        //get current user
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        //get path of database named "Users" containg users info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //get alldata from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot ds :dataSnapshot.getChildren()){
                    ModelUsers modelUsers = ds.getValue(ModelUsers.class);
                    /*condition to fulfill search:
                    1) user is not currnet users
                    2)the user mail or name contains text entered in searchview (case insensetive )
                    * */

                    //get all searched  users except currently signed in user
                    if ( !modelUsers.getUid().equals(fUser.getUid())) {
                        if (modelUsers.getName().toLowerCase().contains(query.toLowerCase()) ||
                                modelUsers.getEmail().toLowerCase().contains(query.toLowerCase())){
                            usersList.add(modelUsers);
                        }


                    }
                    // adapter
                    adapterUsers = new AdapterUsers(getActivity(), usersList);
                    // refresh adapter
                    adapterUsers.notifyDataSetChanged();
                    //set adapter to recycler view
                    recyclerView.setAdapter(adapterUsers);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkUserStatus(){
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user!=null){
            //user is signed in stay here
            //set email of logged in user


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

        //hide addpost Icon from this fragment
        menu.findItem(R.id.action_add_post).setVisible(false);

        //search view
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView =(SearchView) MenuItemCompat.getActionView(item);

        //search listener

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //called when user press search button from keyboard
                //if search query is not empty then search
                if (!TextUtils.isEmpty(query.trim())){
                    // search text contains text , search it
                    searchUsers(query);
                }
                else {
                    // search is empty get all users
                    getAllUers();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                //if search query is not empty then search
                if (!TextUtils.isEmpty(query.trim())){
                    // search text contains text , search it
                    searchUsers(query);
                }
                else {
                    // search is empty get all users
                    getAllUers();
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
