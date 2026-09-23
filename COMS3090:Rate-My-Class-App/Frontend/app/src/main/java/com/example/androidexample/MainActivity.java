package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Declare button variables
    private Button strBtn, homeBtn, courseBtn; //imgBtn;

    //ADDED BUTTONS FOR PROFILE AND SIGNUP
    private Button signupBtn, profileBtn, adminClaimsBtn;

    //ADDED BUTTONS FOR LOGIN
    private Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Set the UI layout for the activity

        // Initialize buttons by finding them using their IDs from XML layout
        strBtn = findViewById(R.id.btnStringRequest);
        //changed
        homeBtn = findViewById(R.id.btnHome);
        courseBtn = findViewById(R.id.btnCourse);
        //imgBtn = findViewById(R.id.btnImageRequest);

        //ADDED
        signupBtn = findViewById(R.id.btnSignup);
        profileBtn = findViewById(R.id.btnProfile);

        //ADDED
        loginBtn = findViewById(R.id.btnLogin);

        adminClaimsBtn = findViewById(R.id.btnAdminClaims);

        /* Set click listeners for each button */
        strBtn.setOnClickListener(this);
        homeBtn.setOnClickListener(this);
        courseBtn.setOnClickListener(this);
        //imgBtn.setOnClickListener(this);

        //ADDED
        signupBtn.setOnClickListener(this);
        profileBtn.setOnClickListener(this);

        //ADDED
        loginBtn.setOnClickListener(this);
        adminClaimsBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId(); // Get the ID of the clicked button

        // Check which button was clicked and start the corresponding activity
        if (id == R.id.btnStringRequest) {
            startActivity(new Intent(MainActivity.this, StringReqActivity.class));
        } else if (id == R.id.btnHome) {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
        } else if (id == R.id.btnCourse) {
            startActivity(new Intent(MainActivity.this, CourseActivity.class));
        } //else if (id == R.id.btnImageRequest) {
        // startActivity(new Intent(MainActivity.this, ImageReqActivity.class));
        //}
        //ADDED
        else if (id == R.id.btnSignup) {
            startActivity(new Intent(MainActivity.this, SignupActivity.class));
        }
        else if (id == R.id.btnProfile) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        }
        //ADDED
        else if (id == R.id.btnLogin) {
            startActivity(new Intent(MainActivity.this,LoginPage.class));
        }
        else if(id == R.id.btnAdminClaims) {
            SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
            prefs.edit()
                    .putLong("USER_ID", 999L)
                    .putString("USERNAME", "demo_admini")
                    .putString("EMAIL", "admin@test.com")
                    .putString("ROLE", "ADMIN")
                    .apply();

            startActivity(new Intent(MainActivity.this, AdminClaimActivity.class));
        }
    }
}
