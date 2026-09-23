package com.example.androidexample;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

//added imports
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
//import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONException;
import org.json.JSONObject;

//FOR VALIDATION OF EMAIL
import android.util.Patterns;

//NEEDED FOR UNIVERSITY
import android.net.Uri;

//test
import com.android.volley.toolbox.StringRequest;
import com.android.volley.VolleyError;
import java.nio.charset.StandardCharsets;

public class SignupActivity extends AppCompatActivity {

    // URL, CHANGE LATER TO TALK TO BACKEND
    private static final String BASE_URL = "http://coms-3090-018.class.las.iastate.edu:8080"; //COMS3090:8080

    //private static final String BASE_URL = "http://10.0.2.2:3000";
    private static final String USERS_ENDPOINT = "/users";
    private static final String UNIVERSITIES_ENDPOINT = "/university";

    // variables for class
    private EditText etUsername, etEmail, etPassHash;
    //comment out if needed
    //private EditText etUniversityId;
    private Button btnSignup, btnHome, btnLogin;
    private RadioGroup rgAccountType;
    private RadioButton rbUser, rbUniversity;

    //University vars
    private EditText etUniversityName, etUniversityLocation, etUniversityWebsite, etUniversityDescription, etUniversityLogoUrl;


    /**
     *  Initializes the signup screen, binds UI elements, sets listeners,
     *  and determines whether user is creating a regular user account or school account.
     *
     * @param savedInstanceState contains the activity's previous saved state if there is one
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //IDS
        rgAccountType = findViewById(R.id.rgAccountType);
        rbUser = findViewById(R.id.rbUser);
        rbUniversity = findViewById(R.id.rbUniversity);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassHash = findViewById(R.id.etPassHash);
        //etUniversityId = findViewById(R.id.etUniversityId);
        btnSignup = findViewById(R.id.btnSignup);
        btnHome = findViewById(R.id.btnHome);
        btnLogin = findViewById(R.id.btnLogin);

        etUniversityName = findViewById(R.id.etUniversityName);
        etUniversityLocation = findViewById(R.id.etUniversityLocation);
        etUniversityWebsite = findViewById(R.id.etUniversityWebsite);
        etUniversityDescription = findViewById(R.id.etUniversityDescription);
        etUniversityLogoUrl = findViewById(R.id.etUniversityLogoUrl);

        updateUniversityFieldVis();

        rgAccountType.setOnCheckedChangeListener((group, checkedId) -> updateUniversityFieldVis());

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            Intent intent1 = new Intent(SignupActivity.this, LoginPage.class);
            startActivity(intent1);
        });

        //listens for button click (SIGNUP)
        btnSignup.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String passHash = etPassHash.getText().toString().trim();
            //String uniStr = etUniversityId.getText().toString().trim();

            //checks if fields are empty
            //if(username.isEmpty() || email.isEmpty() || passHash.isEmpty() || uniStr.isEmpty()) {
            if (username.isEmpty() || email.isEmpty() || passHash.isEmpty()) {

                Toast.makeText(this, "Please fill in missing fields", Toast.LENGTH_SHORT).show();
                return;
            }

            //EMAIL VALIDATION
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Please enter valid email address");
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
                return;
            }

            String role = rbUniversity.isChecked() ? "SCHOOL" : "USER";

            if (rbUniversity.isChecked()) {
                String uniName = etUniversityName.getText().toString().trim();
                String location = etUniversityLocation.getText().toString().trim();
                String website = etUniversityWebsite.getText().toString().trim();
                String description = etUniversityDescription.getText().toString().trim();
                String logoUrl = etUniversityLogoUrl.getText().toString().trim();

                if (uniName.isEmpty()) {
                    Toast.makeText(this, "Enter university name", Toast.LENGTH_SHORT).show();
                    return;
                }

                signupSchoolUser(username, email, passHash, role, uniName, location, website, description, logoUrl);
            } else {
                signupNormalUser(username, email, passHash, role);
            }
        });
    }


    /**
     * Shows or hides the university-only input fields depending on
     * whether the school account radio button has been selected
     */
    private void updateUniversityFieldVis() {
        int vis = rbUniversity.isChecked() ? View.VISIBLE : View.GONE;

        etUniversityName.setVisibility(vis);
        etUniversityLocation.setVisibility(vis);
        etUniversityWebsite.setVisibility(vis);
        etUniversityDescription.setVisibility(vis);
        etUniversityLogoUrl.setVisibility(vis);
    }


    /**
     * Sends a signup request for a normal user account and stores the user
     * information that is returned in SharedPreferences.
     *
     * @param username the username entered by the user
     * @param email the email entered by the user
     * @param passHash the password entered by the user
     * @param role the selected role, typically USER
     */
    private void signupNormalUser(String username, String email, String passHash, String role) {
        String url = BASE_URL + USERS_ENDPOINT;

        JSONObject body = new JSONObject();
        try {
            body.put("username", username);
            body.put("email", email);
            body.put("pass_hash", passHash);
            body.put("role", role);
            //body.put("university_id", universityId);

//            JSONObject universityObj = new JSONObject();
//            universityObj.put("university_id", universityId);
//
//            body.put("university", universityObj);
        }
        catch(JSONException e) {
            Toast.makeText(this, "JSON build error", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    //returns user id from backend
                    long userId;
                    try {
                        userId = response.getLong("user_id");
                    }
                    catch (JSONException e) {
                        Toast.makeText(this, "Missing user_id", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
                    prefs.edit()
                            .putLong("USER_ID", userId)
                            .putString("USERNAME", response.optString("username", username))
                            .putString("EMAIL", response.optString("email", email))
                            //.putInt("UNIVERSITY_ID", response.optInt("university_id", universityId))
                            .putString("ROLE", response.optString("role", role))
                            .apply();

                    Toast.makeText(this, "Signup is successful! user_id=" + userId, Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(this, ProfileActivity.class));
                    finish();
                },
                error -> Toast.makeText(this, "Signup failed: " + error.toString(), Toast.LENGTH_SHORT).show()

        );
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void signupSchoolUser(String username, String email, String passHash,
                                  String role, String uniName, String location,
                                  String website, String description, String logoUrl) {

        String url = BASE_URL + UNIVERSITIES_ENDPOINT + "/signup"; // ← changed

        JSONObject body = new JSONObject();
        try {
            JSONObject userObj = new JSONObject();
            userObj.put("username", username);
            userObj.put("email", email);
            userObj.put("pass_hash", passHash);
            userObj.put("role", role);

            JSONObject uniObj = new JSONObject();
            uniObj.put("name", uniName);
            uniObj.put("location", location);
            uniObj.put("website", website);
            uniObj.put("description", description);
            uniObj.put("logo_url", logoUrl);

            body.put("user", userObj);       // ← matches UniversitySignupRequest
            body.put("university", uniObj);  // ← matches UniversitySignupRequest

        } catch (JSONException e) {
            Toast.makeText(this, "JSON build error", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest req = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    if (response != null && response.contains("success")) {
                        fetchUniversityAfterCreated(0, username, email, uniName);
                        // userId 0 for now — or update backend to return it
                    } else {
                        Toast.makeText(this, "Signup failed: " + response, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Signup failed: " + error.toString(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public byte[] getBody() {
                return body.toString().getBytes(StandardCharsets.UTF_8);
            }
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }



    /**
     * Fetches the newly created university by name so the returned university
     * ID can be stored. After successfully looking up, the method saves user
     * and university data in SharedPreferences and redirects the user to
     * the university owner screen.
     *
     * @param userId the ID of the school user
     * @param username the username of the school user
     * @param email the email of the school user
     * @param uniName the name of the university that was created
     */
    private void fetchUniversityAfterCreated(long userId, String username, String email, String uniName) {
        String url = BASE_URL + UNIVERSITIES_ENDPOINT + "/name/" + Uri.encode(uniName);

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    //for testing if get response fails
                    Toast.makeText(this, "Response: " + response.toString(), Toast.LENGTH_SHORT).show();

                    long universityId;
                    try {
                        universityId = response.getLong("university_id");
                    } catch (JSONException e) {
                        Toast.makeText(this, "University created, missing university_id", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
                    prefs.edit()
                            .putLong("USER_ID", userId)
                            .putString("USERNAME", username)
                            .putString("EMAIL", email)
                            .putString("ROLE", "SCHOOL")
                            .putLong("UNIVERSITY_ID", universityId)
                            .putString("UNIVERSITY_NAME", uniName)
                            .apply();

                    Toast.makeText(this, "University account created: " + universityId, Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(this, UniversityOwnerActivity.class);
                    intent.putExtra("UNIVERSITY_ID", universityId);
                    intent.putExtra("UNIVERSITY_NAME", uniName);
                    intent.putExtra("USERNAME", username);
                    startActivity(intent);
                    finish();
                },
                error -> Toast.makeText(this, "University created, failed to fetch info: " + error.toString(), Toast.LENGTH_SHORT).show()

        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }


}