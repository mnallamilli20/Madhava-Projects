package com.example.androidexample;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class CreateReviewActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://coms-3090-018.class.las.iastate.edu:8080";

    private final String[] gradeDisplayOptions = {
            "Select grade received",
            "A", "A-",
            "B+", "B", "B-",
            "C+", "C", "C-",
            "D+", "D", "D-",
            "F", "SF"
    };

    private final String[] gradeBackendOptions = {
            "",
            "A", "A_MINUS",
            "B_PLUS", "B", "B_MINUS",
            "C_PLUS", "C", "C_MINUS",
            "D_PLUS", "D", "D_MINUS",
            "F", "SF"
    };

    private TextView tvCreateReviewTitle, tvCreateReviewCourseName;
    private Spinner spinnerOverallRating, spinnerDifficultyRating,
            spinnerWorkloadRating, spinnerGradeReceived;
    private RadioGroup rgRecommendation;
    private EditText etComment;
    private Button btnSubmitReview, btnCancelReview;

    private long courseId = -1L;
    private String courseName = "Course";
    private long userId = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_review);

        tvCreateReviewTitle = findViewById(R.id.tvCreateReviewTitle);
        tvCreateReviewCourseName = findViewById(R.id.tvCreateReviewCourseName);

        spinnerOverallRating = findViewById(R.id.spinnerOverallRating);
        spinnerDifficultyRating = findViewById(R.id.spinnerDifficultyRating);
        spinnerWorkloadRating = findViewById(R.id.spinnerWorkloadRating);
        spinnerGradeReceived = findViewById(R.id.spinnerGradeReceived);

        rgRecommendation = findViewById(R.id.rgRecommendation);
        etComment = findViewById(R.id.etComment);

        btnSubmitReview = findViewById(R.id.btnSubmitReview);
        btnCancelReview = findViewById(R.id.btnCancelReview);

        courseId = getIntent().getLongExtra("COURSE_ID", -1L);
        courseName = getIntent().getStringExtra("COURSE_NAME");

        if (courseName == null || courseName.trim().isEmpty()) {
            courseName = "Course";
        }

        tvCreateReviewCourseName.setText(courseName);

        loadUserId();
        setupSpinners();

        btnCancelReview.setOnClickListener(v -> finish());
        btnSubmitReview.setOnClickListener(v -> submitReview());
    }

    private void loadUserId() {
        userId = getIntent().getLongExtra("USER_ID", -1L);

        if (userId == -1L) {
            SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
            userId = prefs.getLong("USER_ID", -1L);
        }
    }

    private void setupSpinners() {
        String[] overallOptions = {
                "Select overall rating",
                "1", "2", "3", "4", "5"
        };

        String[] difficultyOptions = {
                "Select difficulty rating",
                "1", "2", "3", "4", "5"
        };

        String[] workloadOptions = {
                "Select workload rating",
                "1", "2", "3", "4", "5"
        };

        ArrayAdapter<String> overallAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                overallOptions
        );
        overallAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOverallRating.setAdapter(overallAdapter);

        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                difficultyOptions
        );
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficultyRating.setAdapter(difficultyAdapter);

        ArrayAdapter<String> workloadAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                workloadOptions
        );
        workloadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWorkloadRating.setAdapter(workloadAdapter);

        ArrayAdapter<String> gradeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                gradeDisplayOptions
        );
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGradeReceived.setAdapter(gradeAdapter);
    }

    private void submitReview() {
        if (courseId == -1L) {
            Toast.makeText(this, "Missing course id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (userId == -1L) {
            Toast.makeText(this, "Missing user id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        int checkedId = rgRecommendation.getCheckedRadioButtonId();
        String comment = etComment.getText().toString().trim();

        boolean missingOverall = spinnerOverallRating.getSelectedItemPosition() == 0;
        boolean missingDifficulty = spinnerDifficultyRating.getSelectedItemPosition() == 0;
        boolean missingWorkload = spinnerWorkloadRating.getSelectedItemPosition() == 0;
        boolean missingGrade = spinnerGradeReceived.getSelectedItemPosition() == 0;
        boolean missingRecommendation = checkedId == -1;
        boolean missingComment = TextUtils.isEmpty(comment);

        if (missingOverall || missingDifficulty || missingWorkload || missingGrade
                || missingRecommendation || missingComment) {
            Toast.makeText(this, "Please fill out every field", Toast.LENGTH_SHORT).show();
            return;
        }

        int overallRating = Integer.parseInt(spinnerOverallRating.getSelectedItem().toString());
        int difficultyRating = Integer.parseInt(spinnerDifficultyRating.getSelectedItem().toString());
        int workloadRating = Integer.parseInt(spinnerWorkloadRating.getSelectedItem().toString());

        String gradeReceived =
                gradeBackendOptions[spinnerGradeReceived.getSelectedItemPosition()];

        boolean recommendation = checkedId == R.id.rbRecommendYes;

        String url = BASE_URL + "/api/reviews";

        JSONObject body = new JSONObject();

        try {
            JSONObject courseObject = new JSONObject();
            courseObject.put("course_id", courseId);

            JSONObject userObject = new JSONObject();
            userObject.put("user_id", userId);

            body.put("course", courseObject);
            body.put("user", userObject);
            body.put("overall_rating", overallRating);
            body.put("difficulty_rating", difficultyRating);
            body.put("workload_rating", workloadRating);
            body.put("recommendation", recommendation);
            body.put("grade_received", gradeReceived);
            body.put("comment", comment);

        } catch (JSONException e) {
            Toast.makeText(this, "Failed to build review data", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmitReview.setEnabled(false);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    Toast.makeText(this, "Review submitted successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                },
                error -> {
                    btnSubmitReview.setEnabled(true);
                    Toast.makeText(this, "Failed to submit review: " + error.toString(), Toast.LENGTH_LONG).show();
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }
}