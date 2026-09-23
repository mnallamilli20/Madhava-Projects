package com.example.androidexample;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FlagReviewActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://coms-3090-018.class.las.iastate.edu:8080";

    private Button btnBackFlag, btnSubmitFlag;
    private AutoCompleteTextView actvFlagReason;
    private EditText etFlagDescription;
    private TextView tvFlagReviewInfo;

    private long reviewId;
    private long currentUserId;

    private final String[] reasons = {
            "INAPPROPRIATE",
            "MISLEADING",
            "OTHER"
    };

    private static final Set<String> VALID_REASONS = new HashSet<>(
            Arrays.asList("INAPPROPRIATE", "MISLEADING", "OTHER")
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flag_review);

        btnBackFlag = findViewById(R.id.btnBackFlag);
        btnSubmitFlag = findViewById(R.id.btnSubmitFlag);
        actvFlagReason = findViewById(R.id.actvFlagReason);
        etFlagDescription = findViewById(R.id.etFlagDescription);
        tvFlagReviewInfo = findViewById(R.id.tvFlagReviewInfo);

        reviewId = getIntent().getLongExtra("REVIEW_ID", -1L);

        String username = getIntent().getStringExtra("REVIEW_USERNAME");
        String comment = getIntent().getStringExtra("REVIEW_COMMENT");

        if (username == null || username.trim().isEmpty()) {
            username = "Unknown user";
        }

        if (comment == null || comment.trim().isEmpty()) {
            comment = "No comment provided";
        }

        tvFlagReviewInfo.setText("Review by: " + username + "\n\n" + comment);

        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        currentUserId = prefs.getLong("USER_ID", -1L);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                reasons
        );

        actvFlagReason.setAdapter(adapter);
        actvFlagReason.setThreshold(0);

        // Prevent random typing but still allow dropdown selection
        actvFlagReason.setInputType(InputType.TYPE_NULL);
        actvFlagReason.setFocusable(false);

        actvFlagReason.setOnClickListener(v -> {
            actvFlagReason.showDropDown();
        });

        actvFlagReason.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                actvFlagReason.showDropDown();
            }
        });

        btnBackFlag.setOnClickListener(v -> finish());

        btnSubmitFlag.setOnClickListener(v -> submitFlag());
    }

    private void submitFlag() {
        String reason = actvFlagReason.getText().toString().trim();
        String description = etFlagDescription.getText().toString().trim();

        if (reviewId == -1L) {
            Toast.makeText(this, "Missing review id", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUserId == -1L) {
            Toast.makeText(this, "Please Login to Flag Review", Toast.LENGTH_SHORT).show();
            return;
        }

        if (reason.isEmpty()) {
            Toast.makeText(this, "Select a reason", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!VALID_REASONS.contains(reason)) {
            Toast.makeText(this, "Invalid reason selected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (description.isEmpty()) {
            Toast.makeText(this, "Enter a description", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmitFlag.setEnabled(false);
        btnSubmitFlag.setText("Submitting...");

        String url = BASE_URL + "/api/review-flags";

        JSONObject body = new JSONObject();

        try {
            JSONObject reviewObj = new JSONObject();
            reviewObj.put("review_id", reviewId);

            JSONObject flaggedByObj = new JSONObject();
            flaggedByObj.put("user_id", currentUserId);

            body.put("review", reviewObj);
            body.put("flagged_by", flaggedByObj);
            body.put("reason", reason);
            body.put("description", description);

        } catch (JSONException e) {
            btnSubmitFlag.setEnabled(true);
            btnSubmitFlag.setText("Submit Flag");
            Toast.makeText(this, "Error building request", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    Toast.makeText(this, "Review reported", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    btnSubmitFlag.setEnabled(true);
                    btnSubmitFlag.setText("Submit Flag");

                    if (error.networkResponse != null && error.networkResponse.statusCode == 409) {
                        Toast.makeText(this, "You already reported this review", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to report review", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
}