package com.abdallah8198.healthcaresystemdoctor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.abdallah8198.healthcaresystemdoctor.models.ModelUsers;
import com.abdallah8198.healthcaresystemdoctor.notifications.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class DashboardActivity extends AppCompatActivity {

    //firebase auth
    FirebaseAuth firebaseAuth;

    ActionBar actionBar;
    //views

    String mUID;
    private String TAG = "DashboardActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //ActionBar and its title
        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");

        //inti
        firebaseAuth= FirebaseAuth.getInstance();

        //bottom navigation
        BottomNavigationView navigationView = (BottomNavigationView) findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);
        // default fragment on start
        actionBar.setTitle("Home");
        HomeFragment fragment1= new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content ,fragment1 ,"");
        ft1.commit();

        checkUserStatus();


    }
    public void  updateToken(String token){
        DatabaseReference ref  = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(mUID).setValue(mToken);
    }
    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    //handle item select
                    switch (menuItem.getItemId()){
                        case R.id.nav_home:
                            //home fragment selection
                            actionBar.setTitle("Home");
                            HomeFragment fragment1= new HomeFragment();
                            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                            ft1.replace(R.id.content ,fragment1 ,"");
                            ft1.commit();
                            return  true;
                        case R.id.nav_profile:
                            //profile fragment selection
                            actionBar.setTitle("Profile");
                            profileFragment fragment2= new profileFragment();
                            FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                            ft2.replace(R.id.content ,fragment2 ,"");
                            ft2.commit();
                            return  true;
                        case R.id.nav_users:
                            //user fragment selection
                            //profile fragment selection
                            actionBar.setTitle("Users");
                            UsersFragment fragment3= new UsersFragment();
                            FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                            ft3.replace(R.id.content ,fragment3 ,"");
                            ft3.commit();
                            return  true;

                    }
                    return false;
                }
            };

    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here
            //set email of logged in user
            mUID = user.getUid();

            //save uid of currently signed in user in shared preferences
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();

            //update token
            updateToken(FirebaseInstanceId.getInstance().getToken());

        } else {
            //user is not signed ,go to MainActicity
            startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();


    }
    @Override
    protected void onStart() {
        // check on start app
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        final FirebaseAuth firebaseAuth1 = FirebaseAuth.getInstance();
        Log.e(TAG, "onStart: " + firebaseAuth1.getCurrentUser());
        if (fUser != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(fUser.getUid());
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ModelUsers user = dataSnapshot.getValue(ModelUsers.class);


                    Log.e(TAG, "onDataChange: " + dataSnapshot.getValue());
                    Log.e(TAG, "onDataChange: " + user.getKind());
                    if (user.getKind() != null) {
                        if (user.getKind().equals("Doctor")) {

                            checkUserStatus();


                        } else {
                            Toast.makeText(DashboardActivity.this, "Please sign with the email of Doctor", Toast.LENGTH_SHORT).show();
                            firebaseAuth1.signOut();
                            startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
                        }
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        super.onStart();
    }

    @Override
    protected void onResume() {
        // check on start app
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

        final FirebaseAuth firebaseAuth1 = FirebaseAuth.getInstance();
        Log.e(TAG, "onStart: " + firebaseAuth1.getCurrentUser());
        if (fUser != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(fUser.getUid());
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ModelUsers user = dataSnapshot.getValue(ModelUsers.class);


                    Log.e(TAG, "onDataChange: " + dataSnapshot.getValue());
                    Log.e(TAG, "onDataChange: " + user.getKind());
                    if (user.getKind() != null) {
                        if (user.getKind().equals("Doctor")) {
                            checkUserStatus();


                        } else {

                            Toast.makeText(DashboardActivity.this, "Please sign with the email of Doctor", Toast.LENGTH_SHORT).show();
                            firebaseAuth1.signOut();
                            startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
                        }
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            super.onResume();
        }
    }
}


