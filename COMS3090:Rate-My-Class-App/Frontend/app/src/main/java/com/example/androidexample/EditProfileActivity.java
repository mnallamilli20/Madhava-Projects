package com.example.androidexample;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class EditProfileActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://coms-3090-018.class.las.iastate.edu:8080";
    private static final String USERS_ENDPOINT = "/users";

    private EditText usernameInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private Button saveBtn, cancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        saveBtn = findViewById(R.id.saveBtn);
        cancelBtn = findViewById(R.id.cancelBtn);

        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        long userId = prefs.getLong("USER_ID", -1L);

        usernameInput.setText(prefs.getString("USERNAME", ""));

        cancelBtn.setOnClickListener(v -> finish());

        saveBtn.setOnClickListener(v -> {
            if (userId == -1L) {
                Toast.makeText(this, "No user id found", Toast.LENGTH_SHORT).show();
                return;
            }

            String newUsername = usernameInput.getText().toString().trim();
            String newPassword = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            if (newUsername.isEmpty()) {
                usernameInput.setError("Username required");
                return;
            }

            if (!newPassword.isEmpty() || !confirmPassword.isEmpty()) {
                if (newPassword.isEmpty()) {
                    passwordInput.setError("Enter new password");
                    return;
                }

                if (confirmPassword.isEmpty()) {
                    confirmPasswordInput.setError("Confirm new password");
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    confirmPasswordInput.setError("Passwords do not match");
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (newPassword.length() < 4) {
                    passwordInput.setError("Password must be at least 4 characters");
                    return;
                }
            }

            updateProfile(userId, newUsername, newPassword);
        });
    }

    private void updateProfile(long userId, String newUsername, String newPassword) {
        String url = BASE_URL + USERS_ENDPOINT + "/" + userId;

        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);

        JSONObject body = new JSONObject();
        try {
            body.put("username", newUsername);
            body.put("email", prefs.getString("EMAIL", ""));

            String role = prefs.getString("ROLE", "USER");
            body.put("role", role);

            /*
             * Only send pass_hash if the user entered a new password.
             * This avoids accidentally overwriting the password with an empty string.
             */
            if (!newPassword.isEmpty()) {
                body.put("pass_hash", newPassword);
            }

            body.put("university", JSONObject.NULL);

        } catch (JSONException e) {
            Toast.makeText(this, "JSON error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                body,
                response -> {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("USERNAME", newUsername);

                    if (!newPassword.isEmpty()) {
                        editor.putString("PASSWORD", newPassword);
                    }

                    editor.apply();

                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    String message = "Update failed";

                    if (error.networkResponse != null) {
                        message += " code: " + error.networkResponse.statusCode;
                    } else if (error.getMessage() != null) {
                        message += ": " + error.getMessage();
                    }

                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }
}