package com.example.androidexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class CreateScheduleActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://coms-3090-018.class.las.iastate.edu:8080";

    private TextView tvScheduleDifficultyResult, tvScheduleDifficultyTitle;
    private AutoCompleteTextView actvCoursePicker;
    private EditText etScheduleName;
    private Button btnAddSelectedCourse, btnCalculateScheduleDifficulty, btnBackScheduleDifficulty,
            btnSaveSchedule, btnViewSavedSchedules;
    private ListView listSelectedCourses;

    private final ArrayList<Course> allCourses = new ArrayList<>();
    private final ArrayList<Course> selectedCourses = new ArrayList<>();

    private ArrayAdapter<Course> allCoursesAdapter;
    private ArrayAdapter<Course> selectedCoursesAdapter;

    private Course pendingSelectedCourse = null;

    private long university_id = -1L;
    private String universityName = "University";
    private long user_id = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_difficulty);

        tvScheduleDifficultyTitle = findViewById(R.id.tvScheduleDifficultyTitle);
        tvScheduleDifficultyResult = findViewById(R.id.tvScheduleDifficultyResult);
        actvCoursePicker = findViewById(R.id.actvCoursePicker);
        etScheduleName = findViewById(R.id.etScheduleName);
        btnAddSelectedCourse = findViewById(R.id.btnAddSelectedCourse);
        btnCalculateScheduleDifficulty = findViewById(R.id.btnCalculateScheduleDifficulty);
        btnBackScheduleDifficulty = findViewById(R.id.btnBackScheduleDifficulty);
        btnSaveSchedule = findViewById(R.id.btnSaveSchedule);
        btnViewSavedSchedules = findViewById(R.id.btnViewSavedSchedules);
        listSelectedCourses = findViewById(R.id.listSelectedCourses);

        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        user_id = prefs.getLong("USER_ID", -1L);

        if (user_id == -1L) {
            Toast.makeText(this, "Please log in to create schedules", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        allCoursesAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                allCourses
        );

        selectedCoursesAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                selectedCourses
        );

        actvCoursePicker.setAdapter(allCoursesAdapter);
        actvCoursePicker.setThreshold(0);

        actvCoursePicker.setOnClickListener(v -> {
            actvCoursePicker.requestFocus();
            actvCoursePicker.showDropDown();
        });

        actvCoursePicker.setOnItemClickListener((parent, view, position, id) -> {
            pendingSelectedCourse = allCoursesAdapter.getItem(position);
        });

        listSelectedCourses.setAdapter(selectedCoursesAdapter);

        btnBackScheduleDifficulty.setOnClickListener(v -> finish());

        btnAddSelectedCourse.setOnClickListener(v -> {
            Course selectedCourse = pendingSelectedCourse;

            if (selectedCourse == null) {
                Toast.makeText(this, "Please select a valid course from the dropdown", Toast.LENGTH_SHORT).show();
                return;
            }

            if (alreadySelected(selectedCourse)) {
                Toast.makeText(this, "Course already added", Toast.LENGTH_SHORT).show();
                return;
            }

            selectedCourses.add(selectedCourse);
            selectedCoursesAdapter.notifyDataSetChanged();

            pendingSelectedCourse = null;
            actvCoursePicker.setText("");

            Toast.makeText(this, "Course added", Toast.LENGTH_SHORT).show();
        });

        btnCalculateScheduleDifficulty.setOnClickListener(v -> showDifficultyResult());

        btnSaveSchedule.setOnClickListener(v -> saveSchedule());

        btnViewSavedSchedules.setOnClickListener(v -> {
            Intent intent = new Intent(CreateScheduleActivity.this, SavedSchedulesActivity.class);
            startActivity(intent);
        });

        listSelectedCourses.setOnItemLongClickListener((parent, view, position, id) -> {
            selectedCourses.remove(position);
            selectedCoursesAdapter.notifyDataSetChanged();

            Toast.makeText(this, "Course removed", Toast.LENGTH_SHORT).show();
            return true;
        });

        university_id = getIntent().getLongExtra("UNIVERSITY_ID", -1L);
        universityName = getIntent().getStringExtra("UNIVERSITY_NAME");

        if (universityName == null || universityName.trim().isEmpty()) {
            universityName = "University";
        }

        tvScheduleDifficultyTitle.setText(universityName + " Schedule Builder");

        if (university_id == -1L) {
            Toast.makeText(this, "Missing university id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchCourses(university_id);

        tvScheduleDifficultyResult.setText("Average Schedule Difficulty: No courses selected");
    }

    private void fetchCourses(long universityId) {
        String url = BASE_URL + "/courses/university/" + universityId;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    allCourses.clear();
                    parseCourses(response);
                    allCoursesAdapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(
                        this,
                        "Failed to load courses: " + getVolleyErrorMessage(error),
                        Toast.LENGTH_LONG
                ).show()
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void parseCourses(JSONArray arr) {
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.optJSONObject(i);
            if (obj == null) continue;

            long id = obj.optLong("course_id", -1L);
            String name = obj.optString("name", "");
            String courseCode = obj.optString("course_code", "");
            String subject = obj.optString("subject", "");
            String description = obj.optString("description", "");
            double avgOverall = obj.optDouble("avg_overall", 0.0);
            double avgDifficulty = obj.optDouble("avg_difficulty", 0.0);
            double avgWorkload = obj.optDouble("avg_workload", 0.0);
            int reviewCount = obj.optInt("review_count", 0);

            String university = "";
            JSONObject universityObj = obj.optJSONObject("university");
            if (universityObj != null) {
                university = universityObj.optString("name", "");
            }

            allCourses.add(new Course(
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
        }
    }

    private boolean alreadySelected(Course course) {
        for (Course c : selectedCourses) {
            if (c.course_id == course.course_id) {
                return true;
            }
        }
        return false;
    }

    private double calculateScheduleDifficulty() {
        double total = 0.0;
        int count = 0;

        for (Course course : selectedCourses) {
            if (course.avg_difficulty > 0.0) {
                total += course.avg_difficulty;
                count++;
            }
        }

        if (count == 0) {
            return 0.0;
        }

        return total / count;
    }

    private String getDifficultyLabel(double avg) {
        if (avg == 0.0) return "No rated courses";
        if (avg <= 2.0) return "Light";
        if (avg <= 3.5) return "Moderate";
        return "Heavy";
    }

    private void showDifficultyResult() {
        if (selectedCourses.isEmpty()) {
            tvScheduleDifficultyResult.setText("Average Schedule Difficulty: No courses selected");
            return;
        }

        double average = calculateScheduleDifficulty();
        String label = getDifficultyLabel(average);

        tvScheduleDifficultyResult.setText(
                String.format("Average Schedule Difficulty: %.2f / 5 (%s)", average, label)
        );
    }

    private void saveSchedule() {
        String scheduleName = etScheduleName.getText().toString().trim();

        if (scheduleName.isEmpty()) {
            Toast.makeText(this, "Enter a schedule name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCourses.isEmpty()) {
            Toast.makeText(this, "Add at least one course", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject body = new JSONObject();
            body.put("name", scheduleName);

            JSONObject userObj = new JSONObject();
            userObj.put("user_id", user_id);
            body.put("user", userObj);

            JSONObject universityObj = new JSONObject();
            universityObj.put("university_id", university_id);
            body.put("university", universityObj);

            JSONArray coursesArray = new JSONArray();
            for (Course c : selectedCourses) {
                JSONObject courseObj = new JSONObject();
                courseObj.put("course_id", c.course_id);
                coursesArray.put(courseObj);
            }

            body.put("courses", coursesArray);

            String url = BASE_URL + "/api/schedules";

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    response -> {
                        Toast.makeText(this, "Schedule saved", Toast.LENGTH_SHORT).show();
                        finish();
                    },
                    error -> Toast.makeText(
                            this,
                            "Failed to save schedule: " + getVolleyErrorMessage(error),
                            Toast.LENGTH_LONG
                    ).show()
            );

            VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);

        } catch (Exception e) {
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
        }
    }

    private String getVolleyErrorMessage(com.android.volley.VolleyError error) {
        if (error.networkResponse != null) {
            int statusCode = error.networkResponse.statusCode;
            String body = "";

            if (error.networkResponse.data != null) {
                body = new String(error.networkResponse.data);
            }

            return "Status: " + statusCode + " Body: " + body;
        }

        if (error.getMessage() != null) {
            return error.getMessage();
        }

        return error.toString();
    }
}