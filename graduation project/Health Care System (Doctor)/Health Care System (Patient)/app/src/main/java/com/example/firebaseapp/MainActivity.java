package com.example.firebaseapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button logbtn , regbtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logbtn = (Button) findViewById(R.id.logibtn);
        regbtn = (Button) findViewById(R.id.regbtn);

        //handle Register button Click
        regbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            // Start Register Activity
                startActivity(new Intent(MainActivity.this,RegisterationActivity.class));

            }
        });

        //handle Login button Click
        logbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start Register Activity
                startActivity(new Intent(MainActivity.this,LoginActivity.class));

            }
        });
    }
}
