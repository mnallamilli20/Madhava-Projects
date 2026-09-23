package com.example.androidexample;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

//added
import android.content.SharedPreferences;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import android.view.View;
import android.widget.ListView;
import androidx.annotation.NonNull; //navigation needs this
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;
import org.json.JSONArray;
import java.util.ArrayList;
import com.android.volley.toolbox.StringRequest;

public class ClaimRequestActivity extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener{

    //private static final String BASE_URL = "http://10.0.2.2:3000";
    private static final String BASE_URL = "http://coms-3090-018.class.las.iastate.edu:8080";
    private static final String CLAIMS_ENDPOINT = "/claims";
    private static final String CLAIMS_URL = BASE_URL + CLAIMS_ENDPOINT;

    private DrawerLayout drawerLayout; //sidebar
    private NavigationView navView; //sidebar navigation
    private AutoCompleteTextView actvClaimType, actvMyClaimsFilter;
    private EditText etDescription;
    private Button btnSubmitClaim, btnRefreshMyClaims, btnDeleteMyClaim;
    private TextView tvClaimResult;
    private ListView lvMyClaims;
    private TextView tvMyClaimDetail;

    //stores claim data
    private final ArrayList<AdminClaim> allMyClaims = new ArrayList<>();

    //stores claims that match filter
    private final ArrayList<AdminClaim> filteredMyClaims = new ArrayList<>();

    //stores text in lv
    private final ArrayList<String> myDisplayList = new ArrayList<>();

    //myDisplaylist to lvmyclaim
    private ArrayAdapter<String> myListAdapter;
    private AdminClaim selectedMyClaim = null;

    //claim type
    private final String[] CLAIM_TYPES = {
            "PASSWORD_CHANGE",
            "ACCOUNT_DELETION",
            "OTHER"
    };

    //status filter options
    private final String[] MY_STATUS_OPTIONS = {
            "ALL",
            "PENDING",
            "RESOLVED"
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_claim_request);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);

        String username = prefs.getString("USERNAME", "Guest");
        String email = prefs.getString("EMAIL", "");
        String role = prefs.getString("ROLE", "USER");

        //sets up sidebar
        setupSidebar(username, email, role);

        actvClaimType = findViewById(R.id.actvClaimType);
        etDescription = findViewById(R.id.etDescription);
        btnSubmitClaim = findViewById(R.id.btnSubmitClaim);
        btnRefreshMyClaims = findViewById(R.id.btnRefreshMyClaims);
        tvClaimResult = findViewById(R.id.tvClaimResult);
        actvMyClaimsFilter = findViewById(R.id.actvMyClaimsFilter);
        lvMyClaims = findViewById(R.id.lvMyClaims);
        tvMyClaimDetail = findViewById(R.id.tvMyClaimDetail);
        btnDeleteMyClaim = findViewById(R.id.btnDeleteMyClaim);

        setupClaimTypeDropdown(); //sets claim dropdown up

        setupMyClaimsFilterDropdown();

        setupMyClaimsList();

        btnSubmitClaim.setOnClickListener(v -> submitClaim());
        btnRefreshMyClaims.setOnClickListener(v -> fetchMyClaims());
        btnDeleteMyClaim.setOnClickListener(v -> deleteClaim());

        fetchMyClaims();
    }

    /*
     * sets up toolbar and sidebar
     */
    private void setupSidebar(String username, String email, String role) {
        Toolbar toolbar = findViewById(R.id.toolbar);

        //makes toolbar the activity toolbar
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        navView = findViewById(R.id.navView);

        //activity handles sidebar item clicks
        navView.setNavigationItemSelectedListener(this);

        //button for closing/opening sidebar
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        //connect toggle to toolbar
        drawerLayout.addDrawerListener(toggle);

        //syncs state
        toggle.syncState();

        //makes icon white
        toggle.getDrawerArrowDrawable().setColor(
                getResources().getColor(android.R.color.white, null)
        );

        //side bar header view
        View header = navView.getHeaderView(0);

        //shows username/email in sidebar
        ((TextView) header.findViewById(R.id.tvNavUsername)).setText(username);
        ((TextView) header.findViewById(R.id.tvNavEmail)).setText(email);

        //doesn't show login/signup unless user is guest
        setupLogin(navView, role);

        //finds admin item
        MenuItem adminItem = navView.getMenu().findItem(R.id.nav_admin);

        //only shows admin item if user is admin
        if(adminItem != null) {
            if("ADMIN".equals(role)) {
                adminItem.setVisible(true);
            }
            else {
                adminItem.setVisible(false);
            }
        }

        //finds university owner item
        MenuItem uniItem = navView.getMenu().findItem(R.id.nav_university_owner);

        //change if admin access wanted on that page, but kindof useless and ugly
        //shows university item only for school
        if(uniItem != null) {
            if("SCHOOL".equals(role)) {
                uniItem.setVisible(true);
            }
            else {
                uniItem.setVisible(false);
            }
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

    /*
     * sets up claim type dropdown
     */
    private void setupClaimTypeDropdown() {
        //connects CLAIM_TYPES to dropdown
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                CLAIM_TYPES
        );

        //sets adapter on dropdown
        actvClaimType.setAdapter(typeAdapter);

        //sets default claim type
        actvClaimType.setText(CLAIM_TYPES[0], false);
    }

    /*
     * sets up status filter dropdown
     */
    private void setupMyClaimsFilterDropdown() {
        //connects MY_STATUS_OPTIONS to dropdown
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                MY_STATUS_OPTIONS
        );

        //set adapter on dropdown
        actvMyClaimsFilter.setAdapter(statusAdapter);

        //sets default filter
        actvMyClaimsFilter.setText("ALL", false);

        //updates claim list whenver filter selected
        actvMyClaimsFilter.setOnItemClickListener((parent, view, position, id) -> applyMyClaimsFilter());
    }

    /*
     * sets up listview for myClaims
     */
    private void setupMyClaimsList() {
        //connect myDisplayList to listview
        myListAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                myDisplayList
        );

        //set adapter on listview
        lvMyClaims.setAdapter(myListAdapter);

        //when claim clicked, show all details of that claim
        lvMyClaims.setOnItemClickListener((parent, view, position, id) -> {
//            AdminClaim selectedClaim = filteredMyClaims.get(position);
//            showMyClaimDetails(selectedClaim);
            selectedMyClaim = filteredMyClaims.get(position);
            showMyClaimDetails(selectedMyClaim);

        });
    }

    /*
     * submits a claim
     */
    private void submitClaim() {
        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);

        long userId = prefs.getLong("USER_ID", -1L);

        if(userId == -1L) {
            Toast.makeText(this, "No user ID found. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String claimType = actvClaimType.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if(claimType.isEmpty()) {
            actvClaimType.setError("Select a claim type");
            return;
        }

        if(description.isEmpty()) {
            etDescription.setError("Description is required");
            return;
        }

        JSONObject body = new JSONObject();
        try {
            JSONObject userObj = new JSONObject();
            userObj.put("user_id", userId);
            body.put("type", claimType);
            body.put("description", description);
            body.put("status", "PENDING");
            body.put("user", userObj);
        } catch(JSONException e) {
            Toast.makeText(this, "Failed to build request JSON", Toast.LENGTH_SHORT).show();
            return;
        }


        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                CLAIMS_URL,
                body,
                response -> {
                    //success result
                    tvClaimResult.setText("Claim was successfully submitted");

                    //clear description
                    etDescription.setText("");

                    //reset dropdown
                    actvClaimType.setText(CLAIM_TYPES[0], false);

                    Toast.makeText(this, "Claim submitted", Toast.LENGTH_SHORT).show();

                    fetchMyClaims();
                },
                error -> {
                    tvClaimResult.setText("Claim submission failed: " + error.toString());
                    Toast.makeText(this, "Claim submission failed", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    /*
     * fetches users claims
     */
    private void fetchMyClaims() {
        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);

        long userId = prefs.getLong("USER_ID", -1L);

        if(userId == -1L) {
            return;
        }

        String url = CLAIMS_URL + "/user/" + userId;

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    //clear old claims
                    allMyClaims.clear();

                    //JSON response to adminclaim obj
                    parseMyClaims(response);

                    //status filter
                    applyMyClaimsFilter();
                },
                error -> {
                    Toast.makeText(this, "Failed to load claims", Toast.LENGTH_SHORT).show();
                }
        );
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    /*
     * convert JSON array to adminclaim obj
     */
    private void parseMyClaims(JSONArray response) {
        for(int i = 0; i < response.length(); ++i) {
            try{
                JSONObject obj = response.getJSONObject(i); //get claim obj

                //gets nested user obj
                JSONObject user = obj.optJSONObject("user");

                //claim fields
                long claimId = obj.optLong("claim_id", -1L);
                String claimType = obj.optString("type", "UNKNOWN");
                String description = obj.optString("description", "");
                String status = obj.optString("status", "PENDING");
                String createOn = obj.optString("created_on", "");

                //default
                long uId = -1L;
                String uName = "";

                if(user != null) {
                    uId = user.optLong("user_id", -1L);
                    uName = user.optString("username", "");
                }

                //adminclaim obj
                AdminClaim claim = new AdminClaim(
                        claimId,
                        uId,
                        uName,
                        claimType,
                        description,
                        status,
                        createOn
                );

                //add to list
                allMyClaims.add(claim);
            }
            catch(Exception ignored) {
                //nothing skips
            }
        }
    }

    /*
     * filters user claims by selected
     */
    private void applyMyClaimsFilter() {
        //get filter
        String statusFilter = actvMyClaimsFilter.getText().toString().trim();

        //clear list
        filteredMyClaims.clear();

        for(AdminClaim c : allMyClaims) {
            if(!"ALL".equals(statusFilter) && !statusFilter.isEmpty() && !c.status.equalsIgnoreCase(statusFilter)) {
                continue;
            }
            //adds claim
            filteredMyClaims.add(c);
        }

        //clear old text
        myDisplayList.clear();

        //makes listview text for each claim
        for(AdminClaim c : filteredMyClaims) {
            String displayText = "#" + c.claim_id + " | " + c.claim_type +
                    " | " + c.status;
            myDisplayList.add(displayText);
        }

        //refresh listview
        myListAdapter.notifyDataSetChanged();

        //reset detail box
        selectedMyClaim = null;
        tvMyClaimDetail.setText("Select a claim to view details");
    }

    /*
     * shows details for claim
     */
    private void showMyClaimDetails(AdminClaim c) {
        tvMyClaimDetail.setText(
                "Claim ID: " + c.claim_id + "\n" + "Type: " + c.claim_type + "\n" +
                        "Status: " + c.status + "\n" + "Description: " + c.description +
                        "Created: " + c.created_on
        );
    }

    //deletes selected claim and removes from list
    private void deleteClaim() {
        if(selectedMyClaim == null) {
            Toast.makeText(this, "Select claim first", Toast.LENGTH_SHORT).show();
            return;
        }

        long claimIdDelete = selectedMyClaim.claim_id;
        String url = CLAIMS_URL + "/" + claimIdDelete;

        StringRequest req = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    allMyClaims.removeIf(c -> c.claim_id == claimIdDelete);
                    filteredMyClaims.removeIf(c -> c.claim_id == claimIdDelete);

                    selectedMyClaim = null;

                    applyMyClaimsFilter();

                    tvMyClaimDetail.setText("Select a claim to see details");
                    tvClaimResult.setText("Claim #" + claimIdDelete + " deleted");

                    Toast.makeText(this, "Claim deleted", Toast.LENGTH_SHORT).show();

                    fetchMyClaims();
                },
                error -> {
                    Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                }
        );
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    /*
     * sidebar menu clicks
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.nav_home) {
            startActivity(new Intent(this, HomeActivity.class));
        }
        else if(id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        }
        else if(id == R.id.nav_claims) {
            startActivity(new Intent(this, ClaimRequestActivity.class));
        }
        else if(id == R.id.nav_login) {
            startActivity(new Intent(this, LoginPage.class));
        }
        else if(id == R.id.nav_signup) {
            startActivity(new Intent(this, SignupActivity.class));
        }
        else if(id == R.id.nav_logout) {
            //clears login info
            getSharedPreferences("USER_PREFS", MODE_PRIVATE).edit().clear().apply();

            //sends user back to login page
            startActivity(new Intent(this, LoginPage.class));
        }
        //closes sidebar after item clicked
        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }
}