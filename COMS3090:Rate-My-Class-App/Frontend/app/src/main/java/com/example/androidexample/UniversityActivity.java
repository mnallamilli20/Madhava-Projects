package com.example.androidexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UniversityActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://coms-3090-018.class.las.iastate.edu:8080";
    private static final String COURSES_ENDPOINT = "/courses/university/";

    private Button btnBack, btnAddCourse, btnScheduleDifficulty;
    private TextView tvUniversity;
    private AutoCompleteTextView actvCourses;

    private final ArrayList<Course> courses = new ArrayList<>();
    private ArrayAdapter<Course> courseAdapter;

    private long university_id = -1L;

    private long loggedInUniversityId = -1L;
    private boolean isSchoolUser = false;
    private boolean isAdmin = false;
    private boolean canManageThisUniversity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_university);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnBack = findViewById(R.id.btnBack);
        tvUniversity = findViewById(R.id.tvUniversity);
        actvCourses = findViewById(R.id.actvCourseSelect);
        btnAddCourse = findViewById(R.id.btnAddCourse);
        btnScheduleDifficulty = findViewById(R.id.btnScheduleDifficulty);

        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);

        String role = prefs.getString("ROLE", "GUEST");
        loggedInUniversityId = prefs.getLong("UNIVERSITY_ID", -1L);

        isSchoolUser = "SCHOOL".equals(role);
        isAdmin = "ADMIN".equals(role);

        university_id = getIntent().getLongExtra("UNIVERSITY_ID", -1L);
        String uName = getIntent().getStringExtra("UNIVERSITY_NAME");

        canManageThisUniversity =
                isAdmin ||
                        (isSchoolUser && loggedInUniversityId == university_id);

        if (canManageThisUniversity) {
            btnAddCourse.setVisibility(View.VISIBLE);
        } else {
            btnAddCourse.setVisibility(View.GONE);
        }

        btnBack.setOnClickListener(v -> finish());

        btnAddCourse.setOnClickListener(v -> {
            if (!canManageThisUniversity) {
                Toast.makeText(this, "You can only add courses to your own university", Toast.LENGTH_SHORT).show();
                return;
            }

            showAddDialog();
        });

        btnScheduleDifficulty.setOnClickListener(v -> {
            Intent intent = new Intent(UniversityActivity.this, CreateScheduleActivity.class);
            intent.putExtra("UNIVERSITY_ID", university_id);
            intent.putExtra("UNIVERSITY_NAME", tvUniversity.getText().toString());
            startActivity(intent);
        });

        if (uName == null || uName.trim().isEmpty()) {
            uName = "University";
        }

        tvUniversity.setText(uName);

        courseAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                courses
        );

        actvCourses.setAdapter(courseAdapter);
        actvCourses.setThreshold(0);
        actvCourses.setDropDownHeight(dp_to_px(4 * 56));
        actvCourses.setOnClickListener(v -> actvCourses.showDropDown());

        actvCourses.setOnItemClickListener((parent, view, position, id) -> {
            Course selectedCourse = courseAdapter.getItem(position);

            if (selectedCourse == null) {
                Toast.makeText(this, "Invalid course selection", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(UniversityActivity.this, CourseActivity.class);
            intent.putExtra("course_id", selectedCourse.course_id);
            intent.putExtra("course_name", selectedCourse.name);
            startActivity(intent);
        });

        fetchCourses(university_id);
    }



    private void fetchCourses(long uId) {
        String url = BASE_URL + COURSES_ENDPOINT + uId;

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    courses.clear();
                    parseCourses(response);
                    courseAdapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(
                        this,
                        "Failed to load courses: " + error.toString(),
                        Toast.LENGTH_SHORT
                ).show()
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    private void parseCourses(JSONArray arr) {
        for (int i = 0; i < arr.length(); ++i) {
            try {
                JSONObject obj = arr.getJSONObject(i);

                long id = obj.getLong("course_id");
                String name = obj.getString("name");
                String courseCode = obj.getString("course_code");
                String subject = obj.getString("subject");

                JSONObject universityObj = obj.optJSONObject("university");
                String university = "";

                if (universityObj != null) {
                    university = universityObj.optString("name", "");
                }

                String description = obj.getString("description");
                double avgOverall = obj.getDouble("avg_overall");
                double avgDifficulty = obj.getDouble("avg_difficulty");
                double avgWorkload = obj.getDouble("avg_workload");
                int reviewCount = obj.optInt("review_count", 0);

                courses.add(new Course(
                        id,
                        name,
                        courseCode,
                        subject,
                        university,
                        description,
                        avgOverall,
                        avgDifficulty,
                        avgWorkload,
                        reviewCount
                ));

            } catch (Exception ignored) {

            }
        }
    }

    private void showAddDialog() {
        if (!canManageThisUniversity) {
            Toast.makeText(this, "You can only add courses to your own university", Toast.LENGTH_SHORT).show();
            return;
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        EditText etName = new EditText(this);
        etName.setHint("Course name");
        layout.addView(etName);

        EditText etCourseCode = new EditText(this);
        etCourseCode.setHint("Course code");
        layout.addView(etCourseCode);

        EditText etSubject = new EditText(this);
        etSubject.setHint("Subject");
        layout.addView(etSubject);

        EditText etDescription = new EditText(this);
        etDescription.setHint("Description");
        etDescription.setMinLines(3);
        layout.addView(etDescription);

        new AlertDialog.Builder(this)
                .setTitle("Add Course")
                .setView(layout)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String courseCode = etCourseCode.getText().toString().trim();
                    String subject = etSubject.getText().toString().trim();
                    String description = etDescription.getText().toString().trim();

                    if (name.isEmpty() || courseCode.isEmpty()) {
                        Toast.makeText(this, "Course name and code are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    addCourse(name, courseCode, subject, description);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addCourse(String name, String courseCode, String subject, String description) {
        if (!canManageThisUniversity) {
            Toast.makeText(this, "You can only add courses to your own university", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/courses";

        JSONObject body = new JSONObject();

        try {
            JSONObject universityObj = new JSONObject();
            universityObj.put("university_id", university_id);

            body.put("university", universityObj);
            body.put("name", name);
            body.put("course_code", courseCode);
            body.put("subject", subject);
            body.put("description", description);

        } catch (JSONException e) {
            Toast.makeText(this, "JSON build error", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    Toast.makeText(this, "Course added successfully", Toast.LENGTH_SHORT).show();
                    fetchCourses(university_id);
                },
                error -> Toast.makeText(
                        this,
                        "Failed to add course: " + error.toString(),
                        Toast.LENGTH_SHORT
                ).show()
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    private int dp_to_px(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}