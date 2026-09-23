package com.example.androidexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;


/*
 * Filter logic:
 * chips are grouped into university and course categories
 * selecting a chip from one disables the other category
 * only one chip can be active at a time in a category
 * clicking the active chip deselects it and enables other chips
 */

public class SearchFilterActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String BASE_URL = "http://coms-3090-018.class.las.iastate.edu:8080";

    //drawer
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private ImageButton btnMenu;

    //ui elements
    private Button btnReset;
    private TextView tvActiveCategory, tvResultCount;
    private ListView lvResults;

    //university filter chips
    private Button chipTopUni, chipLowUni;

    //course filter chips
    private Button chipHighOverall, chipLowOverall, chipEasiest, chipHardest, chipLightWork, chipHeavyWork;

    //arrays for chip storage
    private final ArrayList<Button> universityChips = new ArrayList<>();
    private final ArrayList<Button> courseChips = new ArrayList<>();

    //courses from backend
    private final ArrayList<Course> allCourses = new ArrayList<>();

    //computed university data
    private final HashMap<Long, UniversityRating> universityRatings = new HashMap<>();

    //display results
    private final ArrayList<FilterResult> results = new ArrayList<>();
    private FilterResultAdapter resultAdapter;

    //current filter state
    private int activeChipId = -1; //id of selected chip
    private String activeCategory = null; //COURSE or UNIVERSITY or null

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_filter);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //drawer initialize
        drawerLayout = findViewById(R.id.drawerLayout);
        navView = findViewById(R.id.navView);
        btnMenu = findViewById(R.id.btnMenu);

        navView.setNavigationItemSelectedListener(this);

        //sets username in drawer header
        View headerView = navView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.navHeaderUsername);
        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        String username = prefs.getString("USERNAME", "Guest");
        navUsername.setText("Welcome, " + username + "!");

        String role = prefs.getString("ROLE", "GUEST");
        setupLogin(navView, role);

        //menu btn opens drawer
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        //filter ui setup
        btnReset = findViewById(R.id.btnResetFilters);
        tvActiveCategory = findViewById(R.id.tvActiveCategory);
        tvResultCount = findViewById(R.id.tvResultCount);
        lvResults = findViewById(R.id.lvFilterResults);

        //university chips
        chipTopUni = findViewById(R.id.chipTopUniversities);
        chipLowUni = findViewById(R.id.chipLowUniversities);
        universityChips.add(chipTopUni);
        universityChips.add(chipLowUni);

        //course chips
        chipHighOverall = findViewById(R.id.chipHighestOverall);
        chipLowOverall = findViewById(R.id.chipLowestOverall);
        chipEasiest = findViewById(R.id.chipEasiest);
        chipHardest = findViewById(R.id.chipHardest);
        chipLightWork = findViewById(R.id.chipLightWorkload);
        chipHeavyWork = findViewById(R.id.chipHeavyWorkload);
        courseChips.add(chipHighOverall);
        courseChips.add(chipLowOverall);
        courseChips.add(chipEasiest);
        courseChips.add(chipHardest);
        courseChips.add(chipLightWork);
        courseChips.add(chipHeavyWork);

        //result adapter
        resultAdapter = new FilterResultAdapter(this, results);
        lvResults.setAdapter(resultAdapter);

        //listeners for chips
        chipHighOverall.setOnClickListener(v -> onChipClicked(chipHighOverall, "COURSE"));
        chipLowOverall.setOnClickListener(v -> onChipClicked(chipLowOverall, "COURSE"));
        chipEasiest.setOnClickListener(v -> onChipClicked(chipEasiest, "COURSE"));
        chipHardest.setOnClickListener(v -> onChipClicked(chipHardest, "COURSE"));
        chipLightWork.setOnClickListener(v -> onChipClicked(chipLightWork, "COURSE"));
        chipHeavyWork.setOnClickListener(v -> onChipClicked(chipHeavyWork, "COURSE"));

        chipTopUni.setOnClickListener(v -> onChipClicked(chipTopUni, "UNIVERSITY"));
        chipLowUni.setOnClickListener(v -> onChipClicked(chipLowUni, "UNIVERSITY"));

        //result click navigates to detail screen
        lvResults.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= results.size()) {
                return;
            }
            FilterResult result = results.get(position);
            navigateToDetail(result);
        });

        //reset button
        btnReset.setOnClickListener(v -> resetAll());

        //fetch data
        fetchAllCourses();
    }

    //handles login/signup/logout visibility
    private void setupLogin(NavigationView navView, String role) {
        MenuItem loginItem = navView.getMenu().findItem(R.id.nav_login);
        MenuItem signupItem = navView.getMenu().findItem(R.id.nav_signup);
        MenuItem logoutItem = navView.getMenu().findItem(R.id.nav_logout);

        boolean isGuest = role == null || role.equals("GUEST");

        if(loginItem != null) {
            loginItem.setVisible(isGuest);
        }
        if(signupItem != null) {
            signupItem.setVisible(isGuest);
        }
        if(logoutItem != null) {
            logoutItem.setVisible(!isGuest);
        }
    }

    //functions

    //navigation drawer, routes to home, profile, signup, or back

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_login) {
            startActivity(new Intent(this, LoginPage.class));
        } else if (id == R.id.nav_signup) {
            startActivity(new Intent(this, SignupActivity.class));
        } else if (id == R.id.nav_back) {
            finish();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /*
     * Handles chips
     *
     * If this chip is already active, deselect, unlocks the rest
     * If a different chip is active in same category, switch to this chip
     * If no chip is active, select this chip, lock other category
     * If a chip from the other category is active, chip is blocked/disabled
     */

    private void onChipClicked(Button chip, String category) {
        int chipId = chip.getId();

        if (chipId == activeChipId) {
            //clicking active chip, deselect everything
            activeChipId = -1;
            activeCategory = null;
            updateChipVisuals();
            clearResults();
            tvActiveCategory.setText("Select a filter to begin");
            tvResultCount.setText("Select a filter to view results");
        } else {
            //select this chip
            activeChipId = chipId;
            activeCategory = category;
            updateChipVisuals();
            applyActiveFilter();
        }
    }

    /*
     * updates all chip backgrounds and enables different
     * states based on current selection
     */
    private void updateChipVisuals() {
        //update uni chip
        for (Button chip : universityChips) {
            if (chip.getId() == activeChipId) {
                chip.setBackgroundResource(R.drawable.chip_selected);
                chip.setTextColor(Color.WHITE);
                chip.setEnabled(true);
            } else if ("COURSE".equals(activeCategory)) {
                chip.setBackgroundResource(R.drawable.chip_disabled);
                chip.setTextColor(Color.GRAY);
                chip.setEnabled(false);
            } else {
                chip.setBackgroundResource(R.drawable.chip_unselected);
                chip.setTextColor(Color.DKGRAY);
                chip.setEnabled(true);
            }
        }

        //update course chip
        for (Button chip : courseChips) {
            if (chip.getId() == activeChipId) {
                chip.setBackgroundResource(R.drawable.chip_selected);
                chip.setTextColor(Color.WHITE);
                chip.setEnabled(true);
            } else if ("UNIVERSITY".equals(activeCategory)) {
                chip.setBackgroundResource(R.drawable.chip_disabled);
                chip.setTextColor(Color.GRAY);
                chip.setEnabled(false);
            } else {
                chip.setBackgroundResource(R.drawable.chip_unselected);
                chip.setTextColor(Color.DKGRAY);
                chip.setEnabled(true);
            }
        }
    }

    //applys filter
    private void applyActiveFilter() {
        if (activeChipId == -1) {
            return;
        }

        // university filters
        if (activeChipId == R.id.chipTopUniversities) {
            tvActiveCategory.setText("Showing: Universities (highest rated first)");
            sortUniversities(true);
        } else if (activeChipId == R.id.chipLowUniversities) {
            tvActiveCategory.setText("Showing: Universities (lowest rated first)");
            sortUniversities(false);
        }
        // course filters
        else if (activeChipId == R.id.chipHighestOverall) {
            tvActiveCategory.setText("Showing: Courses (best overall rating)");
            sortCourses("overall", false);
        } else if (activeChipId == R.id.chipLowestOverall) {
            tvActiveCategory.setText("Showing: Courses (worst overall rating)");
            sortCourses("overall", true);
        } else if (activeChipId == R.id.chipEasiest) {
            tvActiveCategory.setText("Showing: Courses (easiest first)");
            sortCourses("difficulty", true);
        } else if (activeChipId == R.id.chipHardest) {
            tvActiveCategory.setText("Showing: Courses (hardest first)");
            sortCourses("difficulty", false);
        } else if (activeChipId == R.id.chipLightWorkload) {
            tvActiveCategory.setText("Showing: Courses (lightest workload first)");
            sortCourses("workload", true);
        } else if (activeChipId == R.id.chipHeavyWorkload) {
            tvActiveCategory.setText("Showing: Courses (heaviest workload first)");
            sortCourses("workload", false);
        }
    }

    /*
     * sorts and displays courses base on rating
     */
    private void sortCourses(String field, boolean ascending) {
        results.clear();

        ArrayList<Course> sortable = new ArrayList<>(allCourses);

        Comparator<Course> comparator;
        String ratingLabel;

        switch (field) {
            case "difficulty":
                comparator = Comparator.comparingDouble(c -> c.avg_difficulty);
                ratingLabel = "Difficulty";
                break;
            case "workload":
                comparator = Comparator.comparingDouble(c -> c.avg_workload);
                ratingLabel = "Workload";
                break;
            default:
                comparator = Comparator.comparingDouble(c -> c.avg_overall);
                ratingLabel = "Overall";
                break;
        }

        if (!ascending) {
            comparator = comparator.reversed();
        }

        Collections.sort(sortable, comparator);

        for (Course c : sortable) {
            double rating;
            switch (field) {
                case "difficulty": rating = c.avg_difficulty; break;
                case "workload":   rating = c.avg_workload; break;
                default:           rating = c.avg_overall; break;
            }

            String subtitle = c.universityName != null && !c.universityName.isEmpty()
                    ? c.universityName : "Unknown University";
            if (c.course_code != null && !c.course_code.isEmpty()) {
                subtitle += " | " + c.course_code;
            }

            results.add(new FilterResult(
                    c.course_id,
                    "COURSE",
                    c.name,
                    subtitle,
                    rating,
                    ratingLabel + " Rating",
                    c.review_Count
            ));
        }

        resultAdapter.notifyDataSetChanged();
        tvResultCount.setText(results.size() + " courses found");
    }

    /*
     * sorts and displays universities based on average course rating
     */
    private void sortUniversities(boolean highestFirst) {
        results.clear();

        ArrayList<UniversityRating> sortable = new ArrayList<>(universityRatings.values());

        Comparator<UniversityRating> comparator = Comparator.comparingDouble(u -> u.totalRating);
        if (highestFirst) {
            comparator = comparator.reversed();
        }

        Collections.sort(sortable, comparator);

        for (UniversityRating ur : sortable) {
            String subtitle = String.format(
                    "Overall: %.1f | Difficulty: %.1f | Workload %.1f  (%d course%s)",
                    ur.avgOverall, ur.avgDifficulty, ur.avgWorkload, ur.courseCount,
                    ur.courseCount != 1 ? "s" : ""
            );

            results.add(new FilterResult(
                    ur.universityId,
                    "UNIVERSITY",
                    ur.universityName,
                    subtitle,
                    ur.totalRating,
                    "Avg Course Rating",
                    ur.courseCount
            ));
        }

        resultAdapter.notifyDataSetChanged();
        tvResultCount.setText(results.size() + " universities found");
    }

    /*
     * fetches courses from backend
     * many-to-one
     */
    private void fetchAllCourses() {
        String url = BASE_URL + "/courses";

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    allCourses.clear();
                    universityRatings.clear();
                    parseCourses(response);
                    computeUniversityRatings();

                    if (allCourses.isEmpty()) {
                        tvResultCount.setText("No courses found in database");
                    }
                },
                error -> Toast.makeText(this,
                        "Failed to load courses: " + error.toString(),
                        Toast.LENGTH_SHORT).show()
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    /*
     * parses JSON course array
     * gets course data and nested university data
     */
    private void parseCourses(JSONArray arr) {
        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject obj = arr.getJSONObject(i);

                long courseId = obj.getLong("course_id");
                String name = obj.optString("name", "Unknown");
                String courseCode = obj.optString("course_code", "");
                String subject = obj.optString("subject", "");
                String description = obj.optString("description", "");
                double avgOverall = obj.optDouble("avg_overall", 0.0);
                double avgDifficulty = obj.optDouble("avg_difficulty", 0.0);
                double avgWorkload = obj.optDouble("avg_workload", 0.0);
                int reviewCount = obj.optInt("review_count", 0);

                // extract nested university (demonstrates the relationship)
                long universityId = -1L;
                String universityName = "Unknown University";
                String universityStr = "";
                JSONObject uniObj = obj.optJSONObject("university");
                if (uniObj != null) {
                    universityId = uniObj.optLong("university_id", -1L);
                    universityName = uniObj.optString("name", "Unknown University");
                    universityStr = universityName;
                }

                Course course = new Course(courseId, name, courseCode, subject,
                        universityStr, description,
                        avgOverall, avgDifficulty, avgWorkload, reviewCount);
                course.universityId = universityId;
                course.universityName = universityName;

                allCourses.add(course);
            } catch (Exception ignored) {
            }
        }
    }

    /*
     * calculates average university rating based on avg_overall rating
     * of all of its courses
     */
    private void computeUniversityRatings() {
        HashMap<Long, ArrayList<Course>> grouped = new HashMap<>();
        HashMap<Long, String> nameMap = new HashMap<>();

        for (Course c : allCourses) {
            if (c.universityId <= 0) continue;
            grouped.putIfAbsent(c.universityId, new ArrayList<>());
            grouped.get(c.universityId).add(c);
            nameMap.put(c.universityId, c.universityName);
        }

        for (Map.Entry<Long, ArrayList<Course>> entry : grouped.entrySet()) {
            long uniId = entry.getKey();
            ArrayList<Course> courses = entry.getValue();

            double totalOverall = 0;
            double totalDifficulty = 0;
            double totalWorkload = 0;
            int ratedCount = 0;

            for (Course c : courses) {
                if (c.review_Count > 0) {
                    totalOverall += c.avg_overall;
                    totalDifficulty += c.avg_difficulty;
                    totalWorkload += c.avg_workload;
                    ratedCount++;
                }
            }

            double avgOverall = ratedCount > 0 ? totalOverall / ratedCount : 0.0;
            double avgDifficulty = ratedCount > 0 ? totalDifficulty / ratedCount : 0.0;
            double avgWorkload = ratedCount > 0 ? totalWorkload / ratedCount : 0.0;

            //averages all 3 ratings
            double totalRating = ratedCount > 0 ? (avgOverall + avgDifficulty +
                    avgWorkload) / 3.0 : 0.0;

            universityRatings.put(uniId, new UniversityRating(
                    uniId,
                    nameMap.getOrDefault(uniId, "Unknown"),
                    totalRating,
                    avgOverall,
                    avgDifficulty,
                    avgWorkload,
                    courses.size()
            ));
        }
    }

    /*
     * navigates to detail screen when result is clicked
     */
    private void navigateToDetail(FilterResult result) {
        if ("COURSE".equals(result.type)) {
            Intent intent = new Intent(this, CourseActivity.class);
            intent.putExtra("course_id", result.id);
            intent.putExtra("course_name", result.name);
            startActivity(intent);
        } else if ("UNIVERSITY".equals(result.type)) {
            Intent intent = new Intent(this, UniversityActivity.class);
            intent.putExtra("UNIVERSITY_ID", result.id);
            intent.putExtra("UNIVERSITY_NAME", result.name);
            startActivity(intent);
        }
    }

    //resets chip, results, etc.
    private void resetAll() {
        activeChipId = -1;
        activeCategory = null;
        updateChipVisuals();
        clearResults();
        tvActiveCategory.setText("Select a filter to get started");
        tvResultCount.setText("Select a filter to view results");
    }

    //clears results
    private void clearResults() {
        results.clear();
        resultAdapter.notifyDataSetChanged();
    }

    //helper
    private static class UniversityRating {
        final long universityId;
        final String universityName;
        final double totalRating;
        final double avgOverall;
        final double avgDifficulty;
        final double avgWorkload;
        final int courseCount;

        UniversityRating(long universityId, String universityName, double totalRating, double avgOverall,
                         double avgDifficulty, double avgWorkload, int courseCount) {
            this.universityId = universityId;
            this.universityName = universityName;
            this.totalRating = totalRating;
            this.avgOverall = avgOverall;
            this.avgDifficulty = avgDifficulty;
            this.avgWorkload = avgWorkload;
            this.courseCount = courseCount;
        }
    }
}