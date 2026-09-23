package com.example.androidexample;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class ScheduleDetailsActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://coms-3090-018.class.las.iastate.edu:8080";

    private TextView tvScheduleName, tvScheduleAverage;
    private Button btnBackScheduleDetails;
    private ListView listScheduleCourses;

    private final ArrayList<String> courseNames = new ArrayList<>();
    private ArrayAdapter<String> courseAdapter;

    private long schedule_id = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_details);

        tvScheduleName = findViewById(R.id.tvScheduleName);
        tvScheduleAverage = findViewById(R.id.tvScheduleAverage);
        btnBackScheduleDetails = findViewById(R.id.btnBackScheduleDetails);
        listScheduleCourses = findViewById(R.id.listScheduleCourses);

        courseAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                courseNames
        );

        listScheduleCourses.setAdapter(courseAdapter);

        btnBackScheduleDetails.setOnClickListener(v -> finish());

        schedule_id = getIntent().getLongExtra("SCHEDULE_ID", -1L);
        String scheduleName = getIntent().getStringExtra("SCHEDULE_NAME");

        tvScheduleName.setText(scheduleName == null ? "Schedule Details" : scheduleName);

        if (schedule_id == -1L) {
            Toast.makeText(this, "Missing schedule id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchScheduleDetails();
    }

    private void fetchScheduleDetails() {
        String url = BASE_URL + "/api/schedules/" + schedule_id;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    courseNames.clear();

                    JSONArray coursesArray = response.optJSONArray("courses");
                    double avg = calculateAverageDifficulty(coursesArray);

                    tvScheduleAverage.setText(
                            "Average Difficulty: " + String.format("%.2f", avg) + " / 5"
                    );

                    if (coursesArray != null) {
                        for (int i = 0; i < coursesArray.length(); i++) {
                            JSONObject obj = coursesArray.optJSONObject(i);
                            if (obj == null) continue;

                            String name = obj.optString("name", "");
                            String code = obj.optString("course_code", "");
                            double difficulty = obj.optDouble("avg_difficulty", 0.0);

                            courseNames.add(
                                    code + " - " + name + " (" +
                                            String.format("%.2f", difficulty) + "/5)"
                            );
                        }
                    }

                    courseAdapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(
                        this,
                        "Failed to load schedule details: " + error.toString(),
                        Toast.LENGTH_LONG
                ).show()
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private double calculateAverageDifficulty(JSONArray coursesArray) {
        if (coursesArray == null || coursesArray.length() == 0) {
            return 0.0;
        }

        double total = 0.0;
        int count = 0;

        for (int i = 0; i < coursesArray.length(); i++) {
            JSONObject courseObj = coursesArray.optJSONObject(i);
            if (courseObj == null) continue;

            total += courseObj.optDouble("avg_difficulty", 0.0);
            count++;
        }

        if (count == 0) {
            return 0.0;
        }

        return total / count;
    }
}