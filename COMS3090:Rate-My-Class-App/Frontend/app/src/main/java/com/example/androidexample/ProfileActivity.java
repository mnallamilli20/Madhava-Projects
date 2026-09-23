package com.example.androidexample;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

//ADDED
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import androidx.appcompat.app.AlertDialog;
//this is a test for main frontend CI/CD
public class ProfileActivity extends AppCompatActivity {

    //variables
    private static final String BASE_URL = "http://coms-3090-018.class.las.iastate.edu:8080";
    private static final String USERS_ENDPOINT = "/users";
    private View root;
    private TextView welcomeTxt;
    private Button deleteBtn, editBtn, claimBtn, homeBtn;

    private SharedPreferences prefs;
    private String username;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);

        //get username
        username = prefs.getString("USERNAME", null);
        userId = prefs.getLong("USER_ID", -1);
        String role = prefs.getString("ROLE", "GUEST");

        //tries to get from intent
        if (username == null || username.trim().isEmpty()) {
            username = getIntent().getStringExtra("USERNAME");
        }

        if (isGuestUser(username, userId)) {
            Toast.makeText(this, "Guest cannot access this page. Please login or sign up.", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(ProfileActivity.this, LoginPage.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        }
        if("SCHOOL".equals(role)) {
            Toast.makeText(this, "Universities cannot access profile", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(ProfileActivity.this, UniversityOwnerActivity.class);
            intent.putExtra("UNIVERSITY_ID", prefs.getLong("UNIVERSITY_ID", -1l));
            intent.putExtra("UNIVERSITY_NAME", prefs.getString("UNIVERSITY_NAME", ""));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        }


        root = findViewById(R.id.root);
        welcomeTxt = findViewById(R.id.welcomeTxt);
        editBtn = findViewById(R.id.editBtn);
        homeBtn = findViewById(R.id.homeBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
        claimBtn = findViewById(R.id.claimBtn);

        //admin cant see requestadminclaims button
        if("ADMIN".equals(role)) {
            claimBtn.setVisibility(View.GONE);
        }
        else {
            claimBtn.setVisibility(View.VISIBLE);
        }

        welcomeTxt.setText("Welcome, " + username + "!");


        //edit button
        editBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        //home button
        homeBtn.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
        });

        //delete button
        deleteBtn.setOnClickListener(v -> deleteConfirm());

        claimBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ClaimRequestActivity.class);
            startActivity(intent);
        });
    }

    //method helps to block guest users
    private boolean isGuestUser(String username, long userId) {
        return userId == -1 || username == null ||
                username.trim().isEmpty() || username.equalsIgnoreCase("Guest");
    }


    //confirms that you want to delete your account
    private void deleteConfirm() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account?")
                .setMessage("Are you sure you want to delete your account? This is permanent.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    //delete account
    private void deleteAccount() {
        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        long userId = prefs.getLong("USER_ID", -1);

        if(userId == -1) {
            Toast.makeText(this, "No user id found", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = BASE_URL + USERS_ENDPOINT + "/" + userId; // /users/{id}

        JsonObjectRequest deleteReq = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                response -> {
                    prefs.edit().clear().apply();

                    ((MyApp) getApplication()).stopNotifications();

                    Toast.makeText(this, "Account deleted: user_id=" + userId, Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(ProfileActivity.this, SignupActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                },
                error -> {
                    Toast.makeText(this, "Account deletion failed: " + error.toString(), Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(deleteReq);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        String username = prefs.getString("USERNAME", "Guest");
        welcomeTxt.setText("Welcome, " + username + "!");
    }
}