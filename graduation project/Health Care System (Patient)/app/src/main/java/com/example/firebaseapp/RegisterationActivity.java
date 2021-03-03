package com.example.firebaseapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterationActivity extends AppCompatActivity {
    // view
    EditText EmailEt, passeordEt,passeord2Et ,nameEt,phoneEt ,ageEt;
    Button registerBtn;
    TextView HaveAccount;

    // Progress to Diaplay while register user
    ProgressDialog prossDialog;

    //Declare an instance of FirebaseAuth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration);


        // init
        EmailEt = (EditText)findViewById(R.id.EmailEt);
        passeordEt = (EditText)findViewById(R.id.passeordEt);
        passeord2Et = findViewById(R.id.conpasseordEt);
        nameEt = findViewById(R.id.NameEt);
        ageEt = findViewById(R.id.AgeEt);
        phoneEt = findViewById(R.id.PhoneEt);
        registerBtn = (Button)findViewById(R.id.registerBtn);
        HaveAccount = (TextView) findViewById(R.id.Have_Account);
        //In the onCreate() method, initialize the FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        prossDialog = new ProgressDialog(this);
        prossDialog.setMessage("Registering User ...");
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input email,password
                String email = EmailEt.getText().toString().trim();
                String password = passeordEt.getText().toString().trim();
                // valdit
                if(nameEt.length()==0){
                    //set error and focus on Email EditText
                    nameEt.setError("Please Enter Your Name");
                    nameEt.setFocusable(true);
                } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    //set error and focus on Email EditText
                    EmailEt.setError("invalid Email");
                    EmailEt.setFocusable(true);
                } else if (passeordEt.length() < 6) {
                    //set error and focus on Password EditText
                    passeordEt.setError("Password invalid at least 6 characters");
                    passeordEt.setFocusable(true);
                }else if (passeord2Et.length() !=passeordEt.length()) {
                    //set error and focus on Password EditText
                    passeord2Et.setError(" the two Passwords don't match ");
                    passeord2Et.setFocusable(true);
                }else if (phoneEt.length()!=11) {
                    //set error and focus on Password EditText
                    phoneEt.setError("Please Enter Your Phone Number");
                    phoneEt.setFocusable(true);
                } else if (ageEt.length()==0) {
                    //set error and focus on Password EditText
                    ageEt.setError("Please Enter Your Age");
                    ageEt.setFocusable(true);
                }   else {
                    // register the user
                    registerUser(email, password);

                }
            }
         });
        //handle login textview click listener
        HaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterationActivity.this,LoginActivity.class));
                finish();
            }
        });
        }

    private void registerUser(String email, String password) {

            //email and password pattern is valid ,show progress dialoge and start registering user
            prossDialog.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success,dismiss dialoge and startregister activity
                                prossDialog.dismiss();

                                FirebaseUser user = mAuth.getCurrentUser();
                                //get email and uid from auth
                                String email= user.getEmail();
                                String uid = user.getUid();
                                //when user is register store user info in firebase realtime database too
                                //get thecurrent time and stord in database
                                String timeStamp=String.valueOf(System.currentTimeMillis());
                                String name = nameEt.getText().toString();
                                String phone = phoneEt.getText().toString();
                                String age = ageEt.getText().toString();
                                //using hashmap

                                HashMap<Object,String> hashMap = new HashMap<>();

                                //put info in hash map
                                hashMap.put("email" , email);
                                hashMap.put("uid" , uid);
                                hashMap.put("name" , name);
                                hashMap.put("phone" , phone);
                                hashMap.put("onlineStatus" , timeStamp);
                                hashMap.put("typingTo" , "noOne");
                                hashMap.put("image" , "");
                                hashMap.put("cover" , "");
                                hashMap.put("kind" , "Doctor");
                                hashMap.put("active" , "yes");
                                hashMap.put("age" , age);

                                //firebase database instance
                                FirebaseDatabase database =  FirebaseDatabase.getInstance();
                                //path to store user data named "User"
                                DatabaseReference reference =database.getReference("Users");
                                //put datawithin hashmap in database
                                reference.child(uid).setValue(hashMap);

                                Toast.makeText(RegisterationActivity.this, " Registered ..\n."+user.getEmail(), Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterationActivity.this, DashboardActivity.class));
                                finish();
                            } else {
                                // If sign in fails, display a message to the user.
                                prossDialog.dismiss();
                                Toast.makeText(RegisterationActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // error ,dismiss progress dialogeand get and show the error message
                    prossDialog.dismiss();
                    Toast.makeText(RegisterationActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }


            });
        }


    public  boolean onSupportNavigateUp() {
        onBackPressed();    //go previous activity
        return super.onSupportNavigateUp();

    }
}





