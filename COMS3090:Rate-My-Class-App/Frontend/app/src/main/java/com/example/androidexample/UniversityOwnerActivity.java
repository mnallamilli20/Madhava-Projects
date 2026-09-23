package com.example.androidexample;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

//added
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UniversityOwnerActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://coms-3090-018.class.las.iastate.edu:8080";
    // private static final String BASE_URL = "http://10.0.2.2:3000";

    private EditText etUniversityName, etUniversityLocation, etUniversityWebsite, etUniversityDescription, etUniversityLogoUrl;
    private Button btnUpdateUniversity, btnDeleteUniversity, btnAdminHelp, btnHome;

    private long universityId = -1L;


    /**
     * Initializes the university owner screen, validates user permissions,
     * loads existing university data if an ID is present, sets button
     * listeners for update and delete operations.
     *
     * @param savedInstanceState contains activity's previous saved state if exists
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_university_owner);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etUniversityName = findViewById(R.id.etUniversityName);
        etUniversityLocation = findViewById(R.id.etUniversityLocation);
        etUniversityWebsite = findViewById(R.id.etUniversityWebsite);
        etUniversityDescription = findViewById(R.id.etUniversityDescription);
        etUniversityLogoUrl = findViewById(R.id.etUniversityLogoUrl);

        btnUpdateUniversity = findViewById(R.id.btnUpdateUniversity);
        btnDeleteUniversity = findViewById(R.id.btnDeleteUniversity);
        btnAdminHelp = findViewById(R.id.btnAdminHelp);
        btnHome = findViewById(R.id.btnHome);

        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        universityId = getIntent().getLongExtra("UNIVERSITY_ID", prefs.getLong("UNIVERSITY_ID", -1L));
        String role = prefs.getString("ROLE", "GUEST");
        String universityName = getIntent().getStringExtra("UNIVERSITY_NAME");

        //no users without permission allowed
        //if(!(role.equals("SCHOOL") || role.equals("ADMIN"))) {
        if(!role.equals("SCHOOL")) {
            Toast.makeText(this, "No permission to continue, must be a university", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if(universityId == -1L) {
            etUniversityName.setText(universityName);
            Toast.makeText(this, "University create. Missing university id", Toast.LENGTH_SHORT).show();
            //finish();
            //return;
            btnUpdateUniversity.setEnabled(false);
            btnDeleteUniversity.setEnabled(false);
        } else{
            fetchUniversity(universityId);
        }

        // fetchUniversity(universityId);
        btnUpdateUniversity.setOnClickListener(v -> updateUniversity());
        btnDeleteUniversity.setOnClickListener(v -> deleteUniversity());
        btnAdminHelp.setOnClickListener(v -> {
            Intent intent = new Intent(UniversityOwnerActivity.this, ClaimRequestActivity.class);
            startActivity(intent);
        });
        btnHome.setOnClickListener(v -> {
            Intent intent1 = new Intent(UniversityOwnerActivity.this, HomeActivity.class);
            startActivity(intent1);
        });
    }

    /**
     * Fetches university details from backend by using the given university ID
     * and fills in editable text fields with the data returned.
     *
     * @param id the university ID to fetch
     */
    private void fetchUniversity(long id) {
        String url = BASE_URL + "/university/" + id;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response ->{
                    etUniversityName.setText(response.optString("name", ""));
                    etUniversityLocation.setText(response.optString("location", ""));
                    etUniversityWebsite.setText(response.optString("website", ""));
                    etUniversityDescription.setText(response.optString("description", ""));
                    etUniversityLogoUrl.setText(response.optString("logo_url", ""));
                },
                error -> Toast.makeText(this, "Failed to load university", Toast.LENGTH_SHORT).show()
        );
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    /**
     * Sends updated university information to the backend by using an
     * HTTP PUT request. Also validates that the website field beings with
     * http:// or https://.
     */
    private void updateUniversity() {
        String website = etUniversityWebsite.getText().toString().trim();
        //VALIDATION FOR URL
        if(!website.isEmpty() && !website.startsWith("http://") && !website.startsWith("https://")) {
            Toast.makeText(this, "Website must start with http:// or https://", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject body = new JSONObject();

        try {
            body.put("name", etUniversityName.getText().toString().trim());
            body.put("location", etUniversityLocation.getText().toString().trim());
            body.put("website", etUniversityWebsite.getText().toString().trim());
            body.put("description", etUniversityDescription.getText().toString().trim());
            body.put("logo_url", etUniversityLogoUrl.getText().toString().trim());
        }catch (JSONException e) {
            Toast.makeText(this, "JSON build error", Toast.LENGTH_SHORT).show();
            return;
        }

        //PUTS the changes in place (updates)
        String url = BASE_URL + "/university/" + universityId;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                body,
                response -> Toast.makeText(this, "University updated", Toast.LENGTH_SHORT).show(),
                error -> Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
        );
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    /**
     * Displays a confirmation dialog before deleting the university
     * account and information. If user confirms, delete is executed.
     */
    private void confirmDeleteUniversity() {
        new AlertDialog.Builder(this)
                .setTitle("Delete university?")
                .setMessage("Are you sure you want to delete this university? This is permanent.")
                .setPositiveButton("Delete", (dialog, which) -> deleteUniversity())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Deleted the current university from the backend using a HTTP DELETE
     * request. If successful, stored university data is removed from
     * SharedPreferences and user is redirected to home screen.
     */
    private void deleteUniversity() {
        String url = BASE_URL + "/university/" + universityId;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.DELETE,
                url,
                null,
                response -> {
                    SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
                    prefs.edit().remove("UNIVERSITY_ID").remove("UNIVERSITY_NAME").apply();

                    Toast.makeText(this, "University deleted", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                },
                error -> Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show()
        );
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }
}