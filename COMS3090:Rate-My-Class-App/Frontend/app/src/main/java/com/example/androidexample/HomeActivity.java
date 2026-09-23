package com.example.androidexample;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

//ADDED IMPORTS
import android.content.Intent;
import android.view.Gravity;
import android.widget.ArrayAdapter; //drop down menu
import android.widget.AutoCompleteTextView; //searchbar for dropdown
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

import android.widget.Button;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

import android.content.SharedPreferences;
import android.view.MenuItem;
import android.view.View;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //URL AND ENDPOINT
    private static final String BASE_URL = "http://coms-3090-018.class.las.iastate.edu:8080";
    private static final String UNIVERSITIES_ENDPOINT = "/university"; //CHANGE IF NEEDED

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    //FOR TESTING WITH MOCKOON
    //private static final String BASE_URL = "http://10.0.2.2:3000";

    //title, dropdown, array, dropdown
    private TextView tvTitle;
    private AutoCompleteTextView actvUniversity;
    private final ArrayList<University> universities = new ArrayList<>();
    private ArrayAdapter<University> adapter;

    private Button searchFilterBtn;


    /**
     * Initializes the home screen, configures the university dropdown,
     * handles university selection, and starts loading universities from
     * the backend.
     *
     * @param savedInstanceState contains the activity's previously saved data if any exists
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_view);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        updateNavHeader();

        tvTitle = findViewById(R.id.homeTitle);
        actvUniversity = findViewById(R.id.actvUniversitySelect);
        adapter = new ArrayAdapter<>( //adapter to display university
                this, //uses this activity
                android.R.layout.simple_dropdown_item_1line, //drop down
                universities
            );
        actvUniversity.setAdapter(adapter); //connects adapter to dropdown

        actvUniversity.setThreshold(0); //shows suggestions when no chars typed
        actvUniversity.setDropDownHeight(dp_to_px(4*56)); //dropdown height set to 4 rows
        actvUniversity.setOnClickListener(v -> actvUniversity.showDropDown()); //dropdown opens on click

        //when user selects university
        actvUniversity.setOnItemClickListener((parent, view, position, id) -> {
            //gets university
            University selectedUniversity = adapter.getItem(position);

            //error message for no/invalid selection
            if(selectedUniversity == null) {
                Toast.makeText(this, "Invalid University Selection", Toast.LENGTH_SHORT).show();
                return;
            }
            //intent for moving to university activity
            Intent intent = new Intent(HomeActivity.this, UniversityActivity.class);

            //passes university id to university page
            intent.putExtra("UNIVERSITY_ID", selectedUniversity.university_id);
            //passes university name to university page
            intent.putExtra("UNIVERSITY_NAME", selectedUniversity.name);
            //moves to university page
            startActivity(intent);
        });
        //grabs universities from server
        fetchUniversities();

        searchFilterBtn = findViewById(R.id.btnSearchFilter);
        Intent intentSearch = new Intent(HomeActivity.this, SearchFilterActivity.class);
        searchFilterBtn.setOnClickListener(v ->
                startActivity(intentSearch));
    }

    //FUNCTIONS

    private void updateNavHeader() {
        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        String username = prefs.getString("USERNAME", null);
        String email = prefs.getString("EMAIL", "");
        String role = prefs.getString("ROLE", "GUEST");

        //hide login/signup when user logged in
        setupLogin(navigationView, role);

        View headerView = navigationView.getHeaderView(0);
        TextView tvNavUsername = headerView.findViewById(R.id.tvNavUsername);
        TextView tvNavEmail = headerView.findViewById(R.id.tvNavEmail);

        if(username != null && !username.isEmpty()) {
            tvNavUsername.setText(username);
            tvNavEmail.setText(email);
        }
        else {
            tvNavUsername.setText("Guest");
            tvNavEmail.setText("Not logged in");
        }

        //shows manage claims only if admin
        MenuItem adminItem = navigationView.getMenu().findItem(R.id.nav_admin);

//        if(adminItem != null) {
//            adminItem.setVisible("ADMIN".equals(role));
//        }
        if(adminItem != null) {
            if("ADMIN".equals(role)) {
                adminItem.setVisible(true);
            }
            else {
                adminItem.setVisible(false);
            }
        }
        else {
            Toast.makeText(this, "nav_admin not found", Toast.LENGTH_SHORT).show();
        }


        //shows manage university only if SCHOOL or ADMIN
        MenuItem uniOwnerItem = navigationView.getMenu().findItem(R.id.nav_university_owner);
//        if(uniOwnerItem != null) {
//            uniOwnerItem.setVisible("SCHOOL".equals(role) || "ADMIN".equals(role));
//        }
//        if(uniOwnerItem != null) {
//            if("SCHOOL".equals(role) || "ADMIN".equals(role)) {
//                uniOwnerItem.setVisible(true);
//            }
//            else {
//                uniOwnerItem.setVisible(false);
//            }
//        }
        if(uniOwnerItem != null) {
            uniOwnerItem.setVisible("SCHOOL".equals(role));
        }

        MenuItem profileItem = navigationView.getMenu().findItem(R.id.nav_profile);
        if(profileItem != null) {
            profileItem.setVisible(!"SCHOOL".equals(role) && !"GUEST".equals(role));
        }
    }

    //handles login/signup/logout visibility
    private void setupLogin(NavigationView navigationView, String role) {
        MenuItem loginItem = navigationView.getMenu().findItem(R.id.nav_login);
        MenuItem signupItem = navigationView.getMenu().findItem(R.id.nav_signup);
        MenuItem logoutItem = navigationView.getMenu().findItem(R.id.nav_logout);
        MenuItem claimsItem = navigationView.getMenu().findItem(R.id.nav_claims);

        boolean isGuest = role == null || role.equals("GUEST");

        if(claimsItem != null) {
            claimsItem.setVisible(!"ADMIN".equals(role) && !isGuest);
        }

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

    //handles what happens when sidebar menu item is clicked
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.nav_home) {
            //nothing since already at home
        }
        else if(id == R.id.nav_profile) {
            //startActivity(new Intent(this, ProfileActivity.class));
            SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
            String role = prefs.getString("ROLE", "GUEST");

            if("SCHOOL".equals(role)) {
                Toast.makeText(this, "University accounts cannot access Profile", Toast.LENGTH_SHORT).show();
            }
            else {
                startActivity(new Intent(this, ProfileActivity.class));
            }
        }
        else if(id == R.id.nav_login) {
            startActivity(new Intent(this, LoginPage.class));
        }
        else if(id == R.id.nav_signup) {
            startActivity(new Intent(this, SignupActivity.class));
        }
        else if(id == R.id.nav_claims) {
            startActivity(new Intent(this, ClaimRequestActivity.class));
        }
        else if(id == R.id.nav_admin) {
            startActivity(new Intent(this, AdminClaimActivity.class));
        }
        else if(id == R.id.nav_university_owner) {
            SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
            long uniId = prefs.getLong("UNIVERSITY_ID", -1L);
            String role = prefs.getString("ROLE", "GUEST");
            String uniName = prefs.getString("UNIVERSITY_NAME", "");

            if(!"SCHOOL".equals(role)) {
                Toast.makeText(this, "University accounts can only manage", Toast.LENGTH_SHORT).show();
                return true;
            }
            if(uniId == -1L) {
                Toast.makeText(this, "University information missing", Toast.LENGTH_SHORT).show();
                return true;
            }
            Intent intent = new Intent(this, UniversityOwnerActivity.class);
            intent.putExtra("UNIVERSITY_ID", uniId);
            intent.putExtra("UNIVERSITY_NAME", uniName);
            startActivity(intent);
        }
        else if(id == R.id.nav_logout) {
            //clears data and refreshes
            SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
            prefs.edit().clear().apply();
            updateNavHeader();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
        }

        //closes drawer
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    //refresh drawer header every time user comes back
    @Override
    protected void onResume() {
        super.onResume();
        updateNavHeader();
    }



    /**
     * Sends an HTTP GET request to retrieve the list of universities from
     * the backend. When successful, the existing list is cleared, parsed, and
     * the dropdown adapter is refreshed.
     */
    private void fetchUniversities() {
        String URL = BASE_URL + UNIVERSITIES_ENDPOINT;

        //volley request
        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET, //HTTP GET
                URL,
                null,
                response -> {
                        //clear old university list
                        universities.clear();
                        //JSON to university obj
                        parseUniversities(response);
                        //refresh dropdown
                        adapter.notifyDataSetChanged();
                },
                error -> {
                    Toast.makeText(this, "Failed to load universities: " + error.toString(), Toast.LENGTH_SHORT).show();
                }
        );

        //sends volley request
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    /**
     * Parses a JSON array of university objects returned by the backend and converts
     * each JSON object into a University model instance for display use in the
     * dropdown.
     *
     * @param arr the JSON array containing university data
     */
    private void parseUniversities(JSONArray arr) {
        for(int i = 0; i < arr.length(); ++i) {
            try {
                //get json obj
                JSONObject obj = arr.getJSONObject(i);

                //gets fields
                long id = obj.getLong("university_id"); //changed from int
                String name = obj.getString("name");

                String location = obj.optString("location", "");
                String website = obj.optString("website", "");
                String description = obj.optString("description", "");
                String logoUrl = obj.optString("logo_url", "");
                int editor = obj.optInt("editor", -1);
                String createdOn = obj.optString("created_on", "");

                //creates university obj, adds
                //FIX??
                //universities.add(new University(id, name));
                universities.add(new University(id, name, location, website, description, logoUrl, editor, createdOn));
            }
            catch (Exception ignored) {
                //ignores
            }
        }
    }

    /**
     * Converts a density independent pixel (dp) value to equivalent
     * pixel using the current density of the screen.
     *
     * @param dp the density independent pixel value
     * @return the converted pixel value
     */
    private int dp_to_px(int dp) {
        //gets screen density (how many pixels there are)
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density); //converts dp to px
    }
}