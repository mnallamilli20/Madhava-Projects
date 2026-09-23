package com.example.androidexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class SavedSchedulesActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://coms-3090-018.class.las.iastate.edu:8080";

    private ListView listSchedules;
    private Button btnBackSchedules;

    private final ArrayList<Schedule> schedules = new ArrayList<>();
    private ArrayAdapter<Schedule> schedulesAdapter;

    private long user_id = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_schedules);

        listSchedules = findViewById(R.id.listSchedules);
        btnBackSchedules = findViewById(R.id.btnBackSchedules);

        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        user_id = prefs.getLong("USER_ID", -1L);

        if (user_id == -1L) {
            Toast.makeText(this, "Please log in to view saved schedules", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        schedulesAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                schedules
        );

        listSchedules.setAdapter(schedulesAdapter);

        btnBackSchedules.setOnClickListener(v -> finish());

        /*
         * Normal click opens schedule details.
         */
        listSchedules.setOnItemClickListener((parent, view, position, id) -> {
            Schedule s = schedules.get(position);

            Intent intent = new Intent(this, ScheduleDetailsActivity.class);
            intent.putExtra("SCHEDULE_ID", s.schedule_id);
            intent.putExtra("SCHEDULE_NAME", s.name);
            startActivity(intent);
        });

        /*
         * Long click deletes a saved schedule.
         */
        listSchedules.setOnItemLongClickListener((parent, view, position, id) -> {
            Schedule s = schedules.get(position);

            new AlertDialog.Builder(this)
                    .setTitle("Delete Schedule?")
                    .setMessage("Delete \"" + s.name + "\"?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteSchedule(s.schedule_id, position))
                    .setNegativeButton("Cancel", null)
                    .show();

            return true;
        });

        fetchSchedules();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (user_id != -1L) {
            fetchSchedules();
        }
    }

    private void fetchSchedules() {
        String url = BASE_URL + "/api/schedules/user/" + user_id;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    schedules.clear();
                    parseSchedules(response);
                    schedulesAdapter.notifyDataSetChanged();
                },
                error -> Toast.makeText(
                        this,
                        "Failed to load schedules: " + error.toString(),
                        Toast.LENGTH_LONG
                ).show()
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void deleteSchedule(long scheduleId, int position) {
        String url = BASE_URL + "/api/schedules/" + scheduleId;

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    removeScheduleFromList(position);
                    Toast.makeText(this, "Schedule deleted", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    /*
                     * Some DELETE endpoints return 204 No Content.
                     * Volley may treat an empty 204 response strangely, so handle it here too.
                     */
                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;

                        if (statusCode == 200 || statusCode == 204) {
                            removeScheduleFromList(position);
                            Toast.makeText(this, "Schedule deleted", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Toast.makeText(
                                this,
                                "Failed to delete schedule: " + statusCode,
                                Toast.LENGTH_SHORT
                        ).show();
                    } else {
                        Toast.makeText(
                                this,
                                "Failed to delete schedule",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void removeScheduleFromList(int position) {
        if (position >= 0 && position < schedules.size()) {
            schedules.remove(position);
            schedulesAdapter.notifyDataSetChanged();
        }
    }

    private void parseSchedules(JSONArray arr) {
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.optJSONObject(i);
            if (obj == null) continue;

            long scheduleId = obj.optLong("schedule_id", -1L);
            String name = obj.optString("name", "Untitled Schedule");

            JSONObject userObj = obj.optJSONObject("user");
            long userId = -1L;

            if (userObj != null) {
                userId = userObj.optLong("user_id", -1L);
            }

            JSONObject universityObj = obj.optJSONObject("university");
            long universityId = -1L;
            String universityName = "";

            if (universityObj != null) {
                universityId = universityObj.optLong("university_id", -1L);
                universityName = universityObj.optString("name", "");
            }

            double avgDifficulty = calculateAverageDifficulty(obj.optJSONArray("courses"));

            schedules.add(new Schedule(
                    scheduleId,
                    userId,
                    name,
                    universityId,
                    universityName,
                    avgDifficulty
            ));
        }
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