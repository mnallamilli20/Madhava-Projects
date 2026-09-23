package com.example.androidexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class CourseActivity extends AppCompatActivity {

    // Comement
    private static final String BASE_URL = "http://coms-3090-018.class.las.iastate.edu:8080";

    private Button btnBack, btnEditCourse, btnDeleteCourse, btnViewReviews, btnJoinChat;

    private TextView tvCourseTitle, tvCourseCode, tvCourseSubject, tvCourseDescription,
            tvAverageOverall, tvAverageDifficulty, tvAverageWorkload;

    private long course_id = -1L;
    private String currentName = "";
    private String currentCourseCode = "";
    private String currentSubject = "";
    private String currentDescription = "";

    private boolean isSchoolUser = false;
    private boolean isAdmin = false;

    private long loggedInUniversityId = -1L;
    private long courseUniversityId = -1L;
    private boolean canManageThisCourse = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        course_id = getIntent().getLongExtra("course_id", -1L);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        btnBack = findViewById(R.id.btnBack);
        btnEditCourse = findViewById(R.id.btnEditCourse);
        btnDeleteCourse = findViewById(R.id.btnDeleteCourse);
        btnViewReviews = findViewById(R.id.btnViewReviews);
        btnJoinChat = findViewById(R.id.btnJoinChat);

        tvAverageOverall = findViewById(R.id.tvAverageOverall);
        tvAverageDifficulty = findViewById(R.id.tvAverageDifficulty);
        tvAverageWorkload = findViewById(R.id.tvAverageWorkload);

        tvCourseTitle = findViewById(R.id.tvCourseTitle);
        tvCourseCode = findViewById(R.id.tvCourseCode);
        tvCourseSubject = findViewById(R.id.tvCourseSubject);
        tvCourseDescription = findViewById(R.id.tvCourseDescription);

        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);

        String role = prefs.getString("ROLE", "GUEST");
        loggedInUniversityId = prefs.getLong("UNIVERSITY_ID", -1L);

        isSchoolUser = "SCHOOL".equals(role);
        isAdmin = "ADMIN".equals(role);

        btnEditCourse.setVisibility(View.GONE);
        btnDeleteCourse.setVisibility(View.GONE);

        btnBack.setOnClickListener(v -> finish());

        btnEditCourse.setOnClickListener(v -> {
            if (!canManageThisCourse) {
                Toast.makeText(this, "You can only edit courses from your own university", Toast.LENGTH_SHORT).show();
                return;
            }

            showEditDialog();
        });

        btnDeleteCourse.setOnClickListener(v -> {
            if (!canManageThisCourse) {
                Toast.makeText(this, "You can only delete courses from your own university", Toast.LENGTH_SHORT).show();
                return;
            }

            showDeleteDialog();
        });

        btnViewReviews.setOnClickListener(v -> {
            Intent intent = new Intent(CourseActivity.this, CourseReviewActivity.class);
            intent.putExtra("COURSE_ID", course_id);
            intent.putExtra("COURSE_NAME", currentName);
            startActivity(intent);
        });

        btnJoinChat.setOnClickListener(v -> {
            String username = prefs.getString("USERNAME", null);

            if (username == null || username.trim().isEmpty()) {
                Toast.makeText(this, "Please log in before joining chat", Toast.LENGTH_SHORT).show();
                return;
            }

            if (course_id == -1L) {
                Toast.makeText(this, "Invalid course", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(CourseActivity.this, ChatActivity.class);
            intent.putExtra("COURSE_ID", course_id);
            intent.putExtra("COURSE_NAME", currentName);
            startActivity(intent);
        });

        tvCourseTitle.setText("Loading...");
        tvCourseCode.setText("");
        tvCourseSubject.setText("");
        tvCourseDescription.setText("");

        if (course_id == -1L) {
            tvCourseTitle.setText("Invalid course");
            Toast.makeText(this, "Missing course id", Toast.LENGTH_LONG).show();
            return;
        }

        fetchCourse(course_id);
    }

    private void fetchCourse(long course_id) {
        String url = BASE_URL + "/courses/" + course_id;

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                this::displayCourse,
                this::showVolleyError
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void displayCourse(JSONObject course) {
        course_id = course.optLong("course_id", -1L);
        currentName = course.optString("name", "Course");
        currentCourseCode = course.optString("course_code", "UNKNOWN");
        currentSubject = course.optString("subject", "No subject available");
        currentDescription = course.optString("description", "No description available.");

        JSONObject universityObj = course.optJSONObject("university");

        if (universityObj != null) {
            courseUniversityId = universityObj.optLong("university_id", -1L);
        }

        canManageThisCourse =
                isAdmin ||
                        (isSchoolUser && loggedInUniversityId == courseUniversityId);

        if (canManageThisCourse) {
            btnEditCourse.setVisibility(View.VISIBLE);
            btnDeleteCourse.setVisibility(View.VISIBLE);
        } else {
            btnEditCourse.setVisibility(View.GONE);
            btnDeleteCourse.setVisibility(View.GONE);
        }

        double avgOverall = course.optDouble("avg_overall", 0.0);
        double avgDifficulty = course.optDouble("avg_difficulty", 0.0);
        double avgWorkload = course.optDouble("avg_workload", 0.0);

        tvCourseTitle.setText(currentName);
        tvCourseCode.setText("Course Code: " + currentCourseCode);
        tvCourseSubject.setText("Subject: " + currentSubject);
        tvCourseDescription.setText(currentDescription);

        tvAverageOverall.setText(String.format("Average Overall: %.1f / 5", avgOverall));
        tvAverageDifficulty.setText(String.format("Average Difficulty: %.1f / 5", avgDifficulty));
        tvAverageWorkload.setText(String.format("Average Workload: %.1f / 5", avgWorkload));
    }

    private void showEditDialog() {
        if (!canManageThisCourse) {
            Toast.makeText(this, "You can only edit courses from your own university", Toast.LENGTH_SHORT).show();
            return;
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        EditText etName = new EditText(this);
        etName.setHint("Course name");
        etName.setText(currentName);
        layout.addView(etName);

        EditText etCourseCode = new EditText(this);
        etCourseCode.setHint("Course code");
        etCourseCode.setText(currentCourseCode);
        layout.addView(etCourseCode);

        EditText etSubject = new EditText(this);
        etSubject.setHint("Subject");
        etSubject.setText(currentSubject);
        layout.addView(etSubject);

        EditText etDescription = new EditText(this);
        etDescription.setHint("Description");
        etDescription.setMinLines(3);
        etDescription.setText(currentDescription);
        layout.addView(etDescription);

        new AlertDialog.Builder(this)
                .setTitle("Edit Course")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = etName.getText().toString().trim();
                    String newCourseCode = etCourseCode.getText().toString().trim();
                    String newSubject = etSubject.getText().toString().trim();
                    String newDescription = etDescription.getText().toString().trim();

                    if (newName.isEmpty()) {
                        Toast.makeText(this, "Course name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (newCourseCode.isEmpty()) {
                        Toast.makeText(this, "Course code cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    updateCourse(newName, newCourseCode, newSubject, newDescription);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateCourse(String newName, String newCourseCode, String newSubject, String newDescription) {
        if (!canManageThisCourse) {
            Toast.makeText(this, "You can only edit courses from your own university", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/courses/" + course_id;

        JSONObject body = new JSONObject();

        try {
            body.put("name", newName);
            body.put("course_code", newCourseCode);
            body.put("subject", newSubject);
            body.put("description", newDescription);

        } catch (JSONException e) {
            Toast.makeText(this, "Failed to build request body", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest req = new StringRequest(
                Request.Method.PUT,
                url,
                response -> {
                    Toast.makeText(this, "Course updated successfully", Toast.LENGTH_SHORT).show();

                    currentName = newName;
                    currentCourseCode = newCourseCode;
                    currentSubject = newSubject;
                    currentDescription = newDescription;

                    tvCourseTitle.setText(currentName);
                    tvCourseCode.setText("Course Code: " + currentCourseCode);
                    tvCourseSubject.setText("Subject: " + currentSubject);
                    tvCourseDescription.setText(currentDescription);

                    fetchCourse(course_id);
                },
                error -> showVolleyError(error)
        ) {
            @Override
            public byte[] getBody() {
                return body.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void showDeleteDialog() {
        if (!canManageThisCourse) {
            Toast.makeText(this, "You can only delete courses from your own university", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Course")
                .setMessage("Are you sure you want to delete this course?")
                .setPositiveButton("Delete", (dialog, which) -> deleteCourse())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteCourse() {
        if (!canManageThisCourse) {
            Toast.makeText(this, "You can only delete courses from your own university", Toast.LENGTH_SHORT).show();
            return;
        }

        if (course_id == -1L) {
            Toast.makeText(this, "Invalid course_id", Toast.LENGTH_LONG).show();
            return;
        }

        String url = BASE_URL + "/courses/" + course_id;

        StringRequest req = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    Toast.makeText(this, "Course deleted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                },
                this::showVolleyError
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private void showVolleyError(VolleyError error) {
        String msg = error.getMessage();

        if (msg == null || msg.trim().isEmpty()) {
            msg = error.toString();
        }

        Toast.makeText(this, "Error: " + msg, Toast.LENGTH_LONG).show();
    }
}