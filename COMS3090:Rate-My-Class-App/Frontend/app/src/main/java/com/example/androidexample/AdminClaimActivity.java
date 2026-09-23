package com.example.androidexample;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

//ADDED
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.content.SharedPreferences;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.navigation.NavigationView;

import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;



public class AdminClaimActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String BASE_URL = "http://coms-3090-018.class.las.iastate.edu:8080";
    //private static final String BASE_URL = "http://10.0.2.2:3000";
    private static final String CLAIMS_ENDPOINT = "/claims";
    private static final String FLAGS_ENDPOINT = "/api/review-flags";

    //panel views
    private LinearLayout panelClaims, panelFlags;
    private Button btnTabClaims, btnTabFlags;

    //sidebar views
    private DrawerLayout drawerLayout;
    private NavigationView navView;

    //claims ui
    private EditText etSearchClaims;
    private AutoCompleteTextView actvStatusFilter;
    private ListView lvClaims;
    private TextView tvSelectedClaimDetails, tvActionResult;
    private Button btnResolveClaim, btnDeleteClaim;

    //flags ui
    private EditText etSearchFlags;
    private AutoCompleteTextView actvFlagStatusFilter;
    private ListView lvFlags;
    private TextView tvSelectedFlagDetails, tvFlagActionResult;
    private Button btnDeleteReview, btnDismissFlag;

    //claims data
    private final ArrayList<AdminClaim> allClaims = new ArrayList<>();
    private final ArrayList<AdminClaim> filteredClaims = new ArrayList<>();
    private final ArrayList<String> claimDisplay = new ArrayList<>();
    private ArrayAdapter<String> claimAdapter;
    private AdminClaim selectedClaim;

    //flags data
    private final ArrayList<ReviewFlagItem> allFlags = new ArrayList<>();
    private final ArrayList<ReviewFlagItem> filteredFlags = new ArrayList<>();
    private final ArrayList<String> flagDisplay = new ArrayList<>();
    private ArrayAdapter<String> flagAdapter;
    private ReviewFlagItem selectedFlag = null;

    private static final String[] CLAIM_STATUSES = {"ALL", "PENDING", "RESOLVED"};
    private static final String[] FLAG_STATUSES = {"ALL", "PENDING", "RESOLVED", "DISMISSED"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //uncomment if needed, handled by toolbar
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_claim);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        String role = prefs.getString("ROLE", "GUEST");
        String username = prefs.getString("USERNAME", "Guest");
        String email = prefs.getString("EMAIL", "");

        if(!"ADMIN".equals(role)) {
            Toast.makeText(this, "ACCESS DENIED: Must be an admin to access", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        //toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        navView = findViewById(R.id.navView);
        navView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(android.R.color.white, null));

        View header = navView.getHeaderView(0);
        ((TextView) header.findViewById(R.id.tvNavUsername)).setText(username);
        ((TextView) header.findViewById(R.id.tvNavEmail)).setText(email);

        setupLogin(navView, role);

        MenuItem adminItem = navView.getMenu().findItem(R.id.nav_admin);

        if(adminItem != null) {
            adminItem.setVisible(true);
        }

        //tab panel
        panelClaims = findViewById(R.id.panelClaims);
        panelFlags = findViewById(R.id.panelFlags);
        btnTabClaims = findViewById(R.id.btnTabClaims);
        btnTabFlags = findViewById(R.id.btnTabFlags);
        btnTabClaims.setOnClickListener(v -> showTab(true));
        btnTabFlags.setOnClickListener(v -> showTab(false));

        //claims ui
        etSearchClaims = findViewById(R.id.etSearchClaims);
        actvStatusFilter = findViewById(R.id.actvStatusFilter);
        lvClaims = findViewById(R.id.lvClaims);
        tvSelectedClaimDetails = findViewById(R.id.tvSelectedClaimDetails);
        tvActionResult = findViewById(R.id.tvActionResult);
        btnResolveClaim = findViewById(R.id.btnResolveClaim);
        btnDeleteClaim = findViewById(R.id.btnDeleteClaim);

        claimAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, claimDisplay);
        lvClaims.setAdapter(claimAdapter);

        actvStatusFilter.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, CLAIM_STATUSES));
        actvStatusFilter.setText("ALL", false);

        actvStatusFilter.setOnItemClickListener((p, v, pos, id) ->
                applyClaimFilter(etSearchClaims.getText().toString()));

        //live search watcher
        etSearchClaims.addTextChangedListener(new SimpleWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int a, int b, int c) {
                applyClaimFilter(s.toString());
            }
        });

        //search results stay synced
        lvClaims.setOnItemClickListener((p, v, pos, id) -> {
            selectedClaim = filteredClaims.get(pos);
            renderClaimDetail();
        });

        //btn listeners for claim
        btnResolveClaim.setOnClickListener(v -> updateClaimStatus("RESOLVED"));
        btnDeleteClaim.setOnClickListener(v -> deleteClaim());

        //flags ui
        etSearchFlags = findViewById(R.id.etSearchFlags);
        actvFlagStatusFilter = findViewById(R.id.actvFlagStatusFilter);
        lvFlags = findViewById(R.id.lvFlags);
        tvSelectedFlagDetails = findViewById(R.id.tvSelectedFlagDetails);
        tvFlagActionResult = findViewById(R.id.tvFlagActionResult);
        btnDeleteReview = findViewById(R.id.btnDeleteReview);
        btnDismissFlag = findViewById(R.id.btnDismissFlag);

        flagAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, flagDisplay);

        lvFlags.setAdapter(flagAdapter);

        actvFlagStatusFilter.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, FLAG_STATUSES));
        actvFlagStatusFilter.setText("ALL", false);
        actvFlagStatusFilter.setOnItemClickListener((p, v, pos, id) ->
                applyFlagFilter(etSearchFlags.getText().toString()));

        etSearchFlags.addTextChangedListener(new SimpleWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int a, int b, int c) {
                applyFlagFilter(s.toString());
            }
        });

        lvFlags.setOnItemClickListener((p, v, pos, id) -> {
            selectedFlag = filteredFlags.get(pos);
            renderFlagDetail();
        });

        btnDeleteReview.setOnClickListener(v -> deleteReview());
        btnDismissFlag.setOnClickListener(v -> dismissFlag());

        fetchClaims();
        fetchFlags();
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

    //toggles which panel (claims, reviews) is clicked
    private void showTab(boolean claimsTab) {
        if(claimsTab) {
            //panels
            panelClaims.setVisibility(View.VISIBLE);
            panelFlags.setVisibility(View.GONE);
        }
        else {
            panelClaims.setVisibility(View.GONE);
            panelFlags.setVisibility(View.VISIBLE);
        }
    }

    private void fetchClaims() {
        String filter = actvStatusFilter.getText().toString().trim();
        String url;

        if(filter.isEmpty() || "ALL".equals(filter)) {
            url = BASE_URL + CLAIMS_ENDPOINT;
        } else{
            url = BASE_URL + CLAIMS_ENDPOINT + "/status/" + filter;
        }

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    //clear old claims
                    allClaims.clear();

                    //JSON response to adminclaim obj
                    parseClaims(response);

                    //applies filter
                    applyClaimFilter(etSearchClaims.getText().toString());

                    //clears selected claim after refreshing
                    selectedClaim = null;
                    tvSelectedClaimDetails.setText("Select a claim above to see details");
                    tvActionResult.setText("");
                },
                error -> {
                    Toast.makeText(this, "Failed to load claims", Toast.LENGTH_SHORT).show();
                }
        );
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    //converts json array to adminclaim object
    private void parseClaims(JSONArray arr) {
        for(int i = 0; i < arr.length(); ++i) {
            try {
                JSONObject obj = arr.getJSONObject(i);

                JSONObject user = obj.optJSONObject("user");

                long userId;
                String username;

                if(user != null) {
                    userId = user.optLong("user_id", -1);
                    username = user.optString("username", "unknown");
                }
                else {
                    userId = -1;
                    username = "unknown";
                }

                AdminClaim claim = new AdminClaim(
                        obj.optLong("claim_id", -1),
                        userId,
                        username,
                        obj.optString("type", "UNKNOWN"),
                        obj.optString("description", ""),
                        obj.optString("status", "PENDING"),
                        obj.optString("created_on", "")
                );

                allClaims.add(claim);
            } catch(Exception ignored) {
                //nothing happens skips
            }
        }
    }

    /*
     * filters claim using status dropdown and searchbar
     */
    private void applyClaimFilter(String query) {
        String sf = actvStatusFilter.getText().toString().trim();
        String q = query.toLowerCase().trim();

        filteredClaims.clear();

        for(AdminClaim c : allClaims) {
            //if dropdown is not ALL, keeps claims with matching status
            if(!"ALL".equals(sf) && !sf.isEmpty() && !c.status.equalsIgnoreCase(sf)) {
                continue;
            }

            //if search bar is not empty, checks claim matches searched text
            if(!q.isEmpty() && !c.username.toLowerCase().contains(q) &&
                    !c.claim_type.toLowerCase().contains(q) &&
                    !String.valueOf(c.claim_id).contains(q) &&
                    !c.description.toLowerCase().contains(q)) {
                continue;
            }

            filteredClaims.add(c);
        }

        claimDisplay.clear();

        //"builds" new text for each claim in lv
        for(AdminClaim c : filteredClaims) {
            claimDisplay.add("#" + c.claim_id + " | " + c.claim_type + " | " + c.username + " | " + c.status);
        }

        //refreshes lv
        claimAdapter.notifyDataSetChanged();
    }

    /*
     * shows full info for claim currently selected
     */
    private void renderClaimDetail() {
        if(selectedClaim == null) {
            tvSelectedClaimDetails.setText("Select a claim above to see details");
            return;
        }

        tvSelectedClaimDetails.setText(
                "Claim ID: " + selectedClaim.claim_id + "\n" +
                        "User: " + selectedClaim.username + "ID: " +
                        selectedClaim.user_id + "\n" + "Type: " +
                        selectedClaim.claim_type + "\n" + "Status: " +
                        selectedClaim.status + "\n" + "Description: " +
                        selectedClaim.description + "\n" + "Created: " +
                        selectedClaim.created_on
        );
    }

    /*
     * updates selected claim status
     */
    private void updateClaimStatus(String newStatus) {
        if(selectedClaim == null) {
            Toast.makeText(this, "Select a claim first", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + CLAIMS_ENDPOINT + "/" + selectedClaim.claim_id;

        JSONObject body = new JSONObject();

        try{
            body.put("status", newStatus);
        } catch(Exception ignored) {
            //does nothing
        }

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.PUT,
                url,
                body,
                response -> {
                    //update selected claim (LOCAL)
                    selectedClaim.status = newStatus;

                    //refresh list
                    applyClaimFilter(etSearchClaims.getText().toString());

                    //update details
                    renderClaimDetail();

                    tvActionResult.setText("Claim marked " + newStatus);
                },
                error -> {
                    tvActionResult.setText("Update failed: " + error.toString());
                    Toast.makeText(this, "Claim update failed", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    /*
     * deletes selected claim from backend, also removes from list
     */
    private void deleteClaim() {
        if(selectedClaim == null) {
            Toast.makeText(this, "Select a claim first", Toast.LENGTH_SHORT).show();
            return;
        }

        long claimIdToDelete = selectedClaim.claim_id;
        String url = BASE_URL + CLAIMS_ENDPOINT + "/" + selectedClaim.claim_id;

        StringRequest req = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    //test message
                    Toast.makeText(this, "Claim delete in progress", Toast.LENGTH_SHORT).show();

                    //removes deleted claim from list
                    //allClaims.remove(selectedClaim);
                    allClaims.removeIf(c -> c.claim_id == claimIdToDelete);
                    filteredClaims.removeIf(c -> c.claim_id == claimIdToDelete);

                    selectedClaim = null;

                    //refresh list
                    applyClaimFilter(etSearchClaims.getText().toString());

                    tvSelectedClaimDetails.setText("Select claim above to see details");
                    tvActionResult.setText("Claim #" + claimIdToDelete + " has been deleted");
                },
                error -> {
                    //tvActionResult.setText("Delete failed");
                    String message = "Delete failed";

                    //debug message
                    if(error.networkResponse != null) {
                        message += " | Status code: " + error.networkResponse.statusCode;
                    }
                    tvActionResult.setText(message);

                    Toast.makeText(this, "Failed to delete claim", Toast.LENGTH_SHORT).show();
                }
        );
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    //FLAGS SECTION BELOW

    /*
     * loads review flag from backend
     */
    private void fetchFlags() {
        String filter = actvFlagStatusFilter.getText().toString().trim();

        String url;

        if("ALL".equals(filter) || filter.isEmpty()) {
            url = BASE_URL + FLAGS_ENDPOINT;
        }
        else {
            url = BASE_URL + FLAGS_ENDPOINT + "/status/" + filter;
        }

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    //clears old flags
                    allFlags.clear();

                    //convert json response to reviewflagitem obj
                    parseFlags(response);

                    //applies current search or filter
                    applyFlagFilter(etSearchFlags.getText().toString());

                    //clears flag after refresh
                    selectedFlag = null;
                    tvSelectedFlagDetails.setText("Select a flag to see details");
                    tvFlagActionResult.setText("");
                },
                error -> {
                    Toast.makeText(this, "Failed to load flags", Toast.LENGTH_SHORT).show();
                }
        );
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    /*
     * convers json flag into reviewflagitem obj
     */
    private void parseFlags(JSONArray arr) {
        for(int i = 0; i < arr.length(); ++i) {
            try{
                JSONObject obj = arr.getJSONObject(i);

                //review obj has the review that was flagged
                JSONObject review = obj.optJSONObject("review");

                //flagged_by has user who reported review
                JSONObject by = obj.optJSONObject("flagged_by");

                long reviewId;
                long flaggedById;
                String flaggedByUsername;

                if(review != null) {
                    reviewId = review.optLong("review_id", -1);
                }
                else {
                    reviewId = -1;
                }

                if(by != null) {
                    flaggedById = by.optLong("user_id", -1);
                    flaggedByUsername = by.optString("username", "unknown");
                }
                else {
                    flaggedById = -1;
                    flaggedByUsername = "unknown";
                }

                ReviewFlagItem flag = new ReviewFlagItem(
                        obj.optLong("flag_id", -1),
                        reviewId,
                        flaggedById,
                        flaggedByUsername,
                        obj.optString("reason", "UNKNOWN"),
                        obj.optString("status", "PENDING"),
                        obj.optString("created_on", "")
                );

                allFlags.add(flag);
            }
            catch(Exception ignored) {
                //does nothing, skips flag that is 'bad'
            }
        }
    }

    /*
     * filters review flags using
     * dropdown, and search bar
     */
    private void applyFlagFilter(String query) {
        String sf = actvFlagStatusFilter.getText().toString().trim();
        String q = query.toLowerCase().trim();

        filteredFlags.clear();

        for(ReviewFlagItem f : allFlags) {
            //if dropdown is not ALL, keeps only matching flag status
            if(!"ALL".equals(sf) && !sf.isEmpty() && !f.status.equalsIgnoreCase(sf)) {
                continue;
            }

            //checks if search bar has text, and if flag matches search
            if(!q.isEmpty() && !f.flagged_by_username.toLowerCase().contains(q)
            && !f.reason.toLowerCase().contains(q) &&
            !String.valueOf(f.review_id).contains(q) &&
            !String.valueOf(f.flag_id).contains(q)) {
                continue;
            }

            filteredFlags.add(f); //adds flag
        }
        flagDisplay.clear();

        //builds text for each flag in listview
        for(ReviewFlagItem f : filteredFlags) {
            flagDisplay.add(
                    "Flag #" + f.flag_id + " | Review #" + f.review_id +
                            " | " + f.reason + " | " + f.flagged_by_username +
                            " | " + f.status
            );
        }

        //refresh lv
        flagAdapter.notifyDataSetChanged();
    }

    /*
     * shows info for selected review flag
     */
    private void renderFlagDetail() {
        if(selectedFlag == null) {
            tvSelectedFlagDetails.setText("Select a flag to see details");
            return;
        }

        tvSelectedFlagDetails.setText(
                "Flag ID: " + selectedFlag.flag_id + "\n" + "Review ID: " +
                        selectedFlag.review_id + "\n" + "Flagged by: " +
                        selectedFlag.flagged_by_username + "User ID: " +
                        selectedFlag.flagged_by_id + "\n" + "Reason: " +
                        selectedFlag.reason + "\n" + "Status: " +
                        selectedFlag.status + "\n" + "Created: " +
                        selectedFlag.created_on
        );
    }

    /*
     * deletes review that was flagged
     */
    private void deleteReview() {
        if(selectedFlag == null) {
            Toast.makeText(this, "Select a flag first", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "/api/reviews/" + selectedFlag.review_id;

        StringRequest req = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    long deleteReviewId = selectedFlag.review_id;

                    //removes all flags connected to the review
                    allFlags.removeIf(f -> f.review_id == deleteReviewId);

                    selectedFlag = null;

                    //refresh flag list
                    applyFlagFilter(etSearchFlags.getText().toString());

                    tvSelectedFlagDetails.setText("Select a flag to see details");
                    tvFlagActionResult.setText("Review deleted");
                    Toast.makeText(this, "Review deleted", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    tvFlagActionResult.setText("Delete failed");
                    Toast.makeText(this, "Failed to delete review", Toast.LENGTH_SHORT).show();
                }
        );
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    /*
     * Dismisses review flag
     */
    private void dismissFlag() {
        if(selectedFlag == null) {
            Toast.makeText(this, "Select a flag first", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + FLAGS_ENDPOINT + "/" + selectedFlag.flag_id + "/status?status=DISMISSED";

        StringRequest req = new StringRequest(
                Request.Method.PATCH,
                url,
                response -> {
                    //updates flag
                    selectedFlag.status = "DISMISSED";

                    //refresh list
                    applyFlagFilter(etSearchFlags.getText().toString());

                    //update details
                    renderFlagDetail();

                    tvFlagActionResult.setText("Flag dismissed");
                    Toast.makeText(this, "Flag dismissed", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    tvFlagActionResult.setText("Dismiss failed");
                    Toast.makeText(this, "Failed to dismiss flag", Toast.LENGTH_SHORT).show();
                }
        );
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    /*
     * sidebar navigation
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

    //helper for textwatcher
    private abstract static class SimpleWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //nothing
        }

        @Override
        public void afterTextChanged(Editable s) {
            //nothing
        }
    }
}