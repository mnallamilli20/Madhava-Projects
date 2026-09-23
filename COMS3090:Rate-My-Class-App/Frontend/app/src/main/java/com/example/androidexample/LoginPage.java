package com.example.androidexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginPage extends AppCompatActivity {

    private static final String BASE_URL = "http://coms-3090-018.class.las.iastate.edu:8080";

    EditText usernameInput, passwordInput;
    Button loginButton;
    TextView signUpButton, forgotPasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signUpButton);
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton);

        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginPage.this, "Enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = BASE_URL + "/users/login";
            RequestQueue queue = Volley.newRequestQueue(LoginPage.this);

            JSONObject requestBody = new JSONObject();

            try {
                requestBody.put("username", username);
                requestBody.put("pass_hash", password);
            } catch (JSONException e) {
                Toast.makeText(LoginPage.this, "JSON error", Toast.LENGTH_SHORT).show();
                return;
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    requestBody,
                    response -> {
                        try {
                            if (!response.has("user_id")) {
                                Toast.makeText(LoginPage.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            long userId = response.getLong("user_id");
                            String usernameFromResponse = response.optString("username", username);
                            String role = response.optString("role", "USER").trim().toUpperCase();
                            String email = response.optString("email", "");

                            SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
                            prefs.edit()
                                    .putLong("USER_ID", userId)
                                    .putString("USERNAME", usernameFromResponse)
                                    .putString("ROLE", role)
                                    .putString("EMAIL", email)
                                    .apply();

                            ((MyApp) getApplication()).startNotifications(userId);

                            Toast.makeText(LoginPage.this, "Login Successful!", Toast.LENGTH_SHORT).show();

                            if ("SCHOOL".equals(role)) {
                                fetchUniversityForSchoolUser(userId, usernameFromResponse, email);
                            } else {
                                Intent intent = new Intent(LoginPage.this, ProfileActivity.class);
                                intent.putExtra("USERNAME", usernameFromResponse);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }

                        } catch (JSONException e) {
                            Toast.makeText(
                                    LoginPage.this,
                                    "Response Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    },
                    error -> {
                        String message = "Login Failed";



                        Toast.makeText(LoginPage.this, message, Toast.LENGTH_LONG).show();
                    }
            );

            queue.add(jsonObjectRequest);
        });

        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginPage.this, SignupActivity.class);
            startActivity(intent);
        });

        forgotPasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginPage.this, HomeActivity.class);
            startActivity(intent);
        });
    }

    private void fetchUniversityForSchoolUser(long userId, String username, String email) {
        String url = BASE_URL + "/university";

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        long universityId = -1L;
                        String universityName = "";

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject universityObj = response.getJSONObject(i);

                            long editorId = getEditorId(universityObj);

                            android.util.Log.d("LOGIN_UNI", "Logged in userId: " + userId);
                            android.util.Log.d("LOGIN_UNI", "University JSON: " + universityObj.toString());
                            android.util.Log.d("LOGIN_UNI", "Found editorId: " + editorId);

                            if (editorId == userId) {
                                universityId = universityObj.optLong(
                                        "university_id",
                                        universityObj.optLong("universityId", -1L)
                                );

                                universityName = universityObj.optString("name", "");
                                break;
                            }
                        }

                        if (universityId == -1L) {
                            Toast.makeText(
                                    LoginPage.this,
                                    "Login successful, university information not found",
                                    Toast.LENGTH_LONG
                            ).show();

                            Intent intent = new Intent(LoginPage.this, HomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                            return;
                        }

                        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
                        prefs.edit()
                                .putLong("UNIVERSITY_ID", universityId)
                                .putString("UNIVERSITY_NAME", universityName)
                                .putString("ROLE", "SCHOOL")
                                .putString("USERNAME", username)
                                .putString("EMAIL", email)
                                .apply();

                        Intent intent = new Intent(LoginPage.this, UniversityOwnerActivity.class);
                        intent.putExtra("UNIVERSITY_ID", universityId);
                        intent.putExtra("UNIVERSITY_NAME", universityName);
                        intent.putExtra("USERNAME", username);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();

                    } catch (JSONException e) {
                        Toast.makeText(
                                LoginPage.this,
                                "University error: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                },
                error -> {
                    String message = "Failed to load university information";

                    if (error.networkResponse != null) {
                        message += " code: " + error.networkResponse.statusCode;
                    } else if (error.getMessage() != null) {
                        message += ": " + error.getMessage();
                    }

                    Toast.makeText(LoginPage.this, message, Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(LoginPage.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    private long getEditorId(JSONObject universityObj) {
        long editorId = -1L;

        try {
            JSONObject editorObj = universityObj.optJSONObject("editor");

            if (editorObj != null) {
                editorId = editorObj.optLong(
                        "user_id",
                        editorObj.optLong("userId", -1L)
                );
            }

            if (editorId == -1L) {
                editorId = universityObj.optLong("editor_id", -1L);
            }

            if (editorId == -1L) {
                editorId = universityObj.optLong("editorId", -1L);
            }

            if (editorId == -1L && universityObj.has("editor") && !universityObj.isNull("editor")) {
                Object editorValue = universityObj.get("editor");

                if (editorValue instanceof Number) {
                    editorId = universityObj.optLong("editor", -1L);
                }
            }

        } catch (JSONException e) {
            return -1L;
        }

        return editorId;
    }
}