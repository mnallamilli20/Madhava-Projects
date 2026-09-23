package com.example.androidexample;

/*
 * imports for espresso
 * find views, click buttons, type text, etc.
 */
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;

//added for more test classes
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.json.JSONObject;

import android.app.Activity;
import android.view.View;
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.test.espresso.matcher.ViewMatchers.Visibility;

import org.java_websocket.handshake.ServerHandshake;

import org.json.JSONObject;

import java.util.ArrayList;

//imports for launching activities
import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


/*
 * Tests frontend using espresso
 *
 * Tests cover the following:
 * SignUpActivity invalid email
 * SignUpActivity school account
 * ProfileActivity saved username display and delete dialog
 * ChatActivity missing course ID
 */
@RunWith(AndroidJUnit4.class)
public class EmrahTestClass1 {
    private Context context; //needed for sharedpreferences

    /*
     * resets apps user data before each test
     * runs before tests
     */
    @Before
    public void setup() {
        //get app context
        context = ApplicationProvider.getApplicationContext();

        //clear data
        clearUserPrefs();
    }

    /*
     * helper
     * clears USER_PREFS which includes
     * USER_ID, USERNAME, EMAIL, ROLE, UNIVERSITY_ID, and more
     */
    private void clearUserPrefs() {
        context.getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE).edit().clear().commit();
    }

    /*
     * Test # 1
     * tests signupactivity rejects invalid email
     *
     * steps:
     * 1. launch signupactivity
     * 2. enter username
     * 3. enter invalid email
     * 4. enter password
     * 5. click signup button
     * 6. check that email field shows error message
     */
    @Test
    public void signupInvalidEmail() {
         //creates intent to open signupactivity
        Intent intent = new Intent(context, SignupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //starts from app context

        //launches signupactivity
        /*
         * uses try (with) as it closes the scenario (activity) with each
         * test case that we run
         */
        try(ActivityScenario<SignupActivity> scenario = ActivityScenario.launch(intent)) {
            //finds username, then clears and tests with testuser456
            //closesoftkeyboard() is not needed for emulator but it can mess up espresso by
            //covering fields (edittexts) with the keyboard
            onView(withId(R.id.etUsername)).perform(clearText(), replaceText("testuser456"), closeSoftKeyboard());

            //finds email field (edittext) and types invalid email
            onView(withId(R.id.etEmail)).perform(clearText(), replaceText("notEmail"), closeSoftKeyboard());

            //finds password field, enters password
            onView(withId(R.id.etPassHash)).perform(clearText(), replaceText("123"), closeSoftKeyboard());

            //clicks signup button
            onView(withId(R.id.btnSignup)).perform(click());

            //checks for error message
            onView(withId(R.id.etEmail)).check(matches(hasErrorText("Please enter valid email address")));

            //if needed add a check for if user is still on signupactivity
        }
    }

    /*
     * Test # 2
     * tests school/university account signup
     *
     * steps:
     * 1. launch signupActivity
     * 2. click university/school account radio btn
     * 3. verify that university fields appear
     * 4. fill in normal user fields only
     * 5. leave university name empty
     * 6. click signup
     * 7. check that user stays on signupactivity
     */
    @Test
    public void schoolSignupUniversityNameMissing() {
        //create intent to signupactivity
        Intent intent = new Intent(context, SignupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        //launch signupactivity
        try(ActivityScenario<SignupActivity> scenario = ActivityScenario.launch(intent)) {
            //clicks university/school btn, should make university fields visible
            onView(withId(R.id.rbUniversity)).perform(click());

            //checks if university name field appears
            onView(withId(R.id.etUniversityName)).check(matches(isDisplayed()));

            //checks if university location field appears
            onView(withId(R.id.etUniversityLocation)).check(matches(isDisplayed()));

            //checks if university website field appears
            onView(withId(R.id.etUniversityWebsite)).check(matches(isDisplayed()));

            //fills in username with "schooluser456"
            onView(withId(R.id.etUsername)).perform(clearText(), replaceText("schooluser456"), closeSoftKeyboard());

            //fills in email
            onView(withId(R.id.etEmail)).perform(clearText(), replaceText("schooluser456@test.com"), closeSoftKeyboard());

            //fills in password
            onView(withId(R.id.etPassHash)).perform(clearText(), replaceText("123"), closeSoftKeyboard());

            //skips etUniversityName (test)

            //clicks signup
            onView(withId(R.id.btnSignup)).perform(click());

            //check that user is still on signup activity
            //could check toast message, but not supported easily from espresso, add if needed
            onView(withId(R.id.etUniversityName)).check(matches(isDisplayed()));


        }
    }

    /*
     * Test # 3
     * tests profileActivity behavior
     *
     * steps:
     * 1. save fake logged-in user in sharedpreferences
     * 2. launch profileactivity
     * 3. check that welcome message matches username (saved)
     * 4. click delete button
     * 5. check if delete confirmation appears
     * 6. click cancel
     */
    @Test
    public void profileUsernameDeleteDialog() {
        //saves fake login data
        context.getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
                .edit()
                .putLong("USER_ID", 1001L)
                .putString("USERNAME", "EmrahTest3")
                .putString("EMAIL", "emrah@test.com")
                .putString("ROLE", "USER")
                .commit();

        //creats intent to profileActivity
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        //launches profile activity
        try(ActivityScenario<ProfileActivity> scenario = ActivityScenario.launch(intent)) {
            //checks profileactivity reads username and displays it
            onView(withId(R.id.welcomeTxt)).check(matches(withText("Welcome, EmrahTest3!")));

            //clicks delete account btn
            onView(withId(R.id.deleteBtn)).perform(click());

            //verifies delete dialog shows up
            onView(withText("Delete Account?")).inRoot(isDialog()).check(matches(isDisplayed()));

            //checks delete dialog message shows up
            onView(withText("Are you sure you want to delete your account? This is permanent."))
                    .inRoot(isDialog()).check(matches(isDisplayed()));

            //clicks cancel
            onView(withText("Cancel")).inRoot(isDialog()).perform(click());
        }
    }

    /*
     * Test # 4
     * tests ChatActivity when opened without course ID
     *
     * steps:
     * 1. launch ChatActivity w/o passing COURSE_ID (this is just done by launching the chatactivity)
     * 2. ChatActivity should detect course id is missing
     * 3. Connection status text should show error message
     */
    @Test
    public void chatActivityMissingCourseId() {
        //creates intent to chatactivity
        Intent intent = new Intent(context, ChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        //launch chatactivity
        try(ActivityScenario<ChatActivity> scenario = ActivityScenario.launch(intent)) {
            //checks that connection status TextView shows missing course id error
            onView(withId(R.id.tvConnectionStatus)).check(matches(withText("Error: No course ID")));
        }
    }

    /*
     * Test # 5
     * tests adminclaim constructor and toString
     */
    @Test
    public void adminClaimT() {
        AdminClaim claim = new AdminClaim(
                10L,
                55L,
                "emrah",
                "PASSWORD_CHANGE",
                "password",
                "PENDING",
                "2026-05-06"
        );

        assertEquals(10L, claim.claim_id);
        assertEquals(55L, claim.user_id);
        assertEquals("emrah", claim.username);
        assertEquals("PASSWORD_CHANGE", claim.claim_type);
        assertEquals("password", claim.description);
        assertEquals("PENDING", claim.status);
        assertEquals("2026-05-06", claim.created_on);
        assertEquals("#10 | PASSWORD_CHANGE | PENDING", claim.toString());
    }

    /*
     * Test # 6
     * tests reviewflagitem constructor
     */
    @Test
    public void reviewFlagItemT() {
        ReviewFlagItem flag = new ReviewFlagItem(
                10L,
                20L,
                30L,
                "reportUser",
                "INAPPROPRIATE",
                "PENDING",
                "2026-05-06"
        );

        assertEquals(10L, flag.flag_id);
        assertEquals(20L, flag.review_id);
        assertEquals(30L, flag.flagged_by_id);
        assertEquals("reportUser", flag.flagged_by_username);
        assertEquals("INAPPROPRIATE", flag.reason);
        assertEquals("PENDING", flag.status);
        assertEquals("2026-05-06", flag.created_on);
    }

    /*
     * Test # 7
     * tests chatmessage constructor and toJson
     */
    @Test
    public void chatMessageT() throws Exception {
        ChatMessage msg = new ChatMessage(
                "chat",
                "emrah",
                "CS309",
                "hello",
                123L
        );

        String json = msg.toJson();
        JSONObject obj = new JSONObject(json);

        assertEquals("chat", obj.getString("type"));
        assertEquals("emrah", obj.getString("sender"));
        assertEquals("CS309", obj.getString("room"));
        assertEquals("hello", obj.getString("content"));
        assertEquals(123L, obj.getLong("timestamp"));
    }

    /*
     * Test # 8
     * tests filterresult constructor
     */
    @Test
    public void filterResultT() {
        FilterResult res = new FilterResult(
                10L,
                "COURSE",
                "COMS309",
                "Iowa State University | COMS309",
                5.0,
                "Overall Rating",
                10
        );

        assertEquals(10L, res.id);
        assertEquals("COURSE", res.type);
        assertEquals("COMS309", res.name);
        assertEquals("Iowa State University | COMS309", res.subtitle);
        assertEquals(5.0, res.rating, 0.001); //threshold 4.999 - 5.001
        assertEquals("Overall Rating", res.ratingLabel);
        assertEquals(10, res.reviewCount);
    }

    /*
     * Test # 11
     * test university constructor and toString
     */
    @Test
    public void universityT() {
        University uni = new University(
                1L,
                "Iowa State University",
                "Ames, IA",
                "http://test.edu",
                "University in Iowa",
                "logo",
                10,
                "2026-05-06"
        );

        assertEquals(1L, uni.university_id);
        assertEquals("Iowa State University", uni.name);
        assertEquals("Ames, IA", uni.location);
        assertEquals("http://test.edu", uni.website);
        assertEquals("University in Iowa", uni.description);
        assertEquals("logo", uni.logoUrl);
        assertEquals(10, uni.editor);
        assertEquals("2026-05-06", uni.createdOn);
        assertEquals("Iowa State University", uni.toString());
    }

    /*
     * Test # 12
     * tests chatwebsocketmanager disconnected state
     */
    @Test
    public void chatWebSocketManagerT() {
        ChatWebSocketManager m1 = ChatWebSocketManager.getInstance();
        ChatWebSocketManager m2 = ChatWebSocketManager.getInstance();

        assertSame(m1, m2);
        assertFalse(m1.isConnected());
    }

    /*
     * Test # 13
     * test signupactivity user account hides university fields
     */
    @Test
    public void signupActivityUserT() {
        Intent intent = new Intent(context, SignupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<SignupActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.etUniversityName)).check(matches(withEffectiveVisibility(Visibility.GONE)));

            onView(withId(R.id.etUniversityLocation)).check(matches(withEffectiveVisibility(Visibility.GONE)));

            onView(withId(R.id.etUniversityWebsite)).check(matches(withEffectiveVisibility(Visibility.GONE)));
        }
    }

    /*
     * Test # 14
     * tests university radio btn shows university fields
     */
    @Test
    public void signupUniFieldT() {
        Intent intent = new Intent(context, SignupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<SignupActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.rbUniversity)).perform(click());

            onView(withId(R.id.etUniversityName)).check(matches(isDisplayed()));

            onView(withId(R.id.etUniversityLocation)).check(matches(isDisplayed()));

            onView(withId(R.id.etUniversityWebsite)).check(matches(isDisplayed()));
        }
    }

    /*
     * Test # 15
     * tests claimrequestactivity fields
     */
    @Test
    public void claimRequestActivityFT() {
        Intent intent = new Intent(context, ClaimRequestActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<ClaimRequestActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.actvClaimType)).check(matches(withText("PASSWORD_CHANGE")));

            onView(withId(R.id.actvMyClaimsFilter)).check(matches(withText("ALL")));

            onView(withId(R.id.btnSubmitClaim)).check(matches(isDisplayed()));

            onView(withId(R.id.btnRefreshMyClaims)).check(matches(isDisplayed()));
        }
    }

    /*
     * Test # 16
     * tests searchfilteractivity btn visibility
     */
    @Test
    public void searchFilterBtns() {
        Intent intent = new Intent(context, SearchFilterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<SearchFilterActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.btnResetFilters)).check(matches(isDisplayed()));

            onView(withId(R.id.chipTopUniversities)).check(matches(isDisplayed()));

            onView(withId(R.id.chipHighestOverall)).check(matches(isDisplayed()));

            onView(withId(R.id.lvFilterResults)).check(matches(isDisplayed()));
        }
    }

    /*
     * Test # 17
     * tests searchfilteractivity btn disabling
     */
    @Test
    public void searchFilterBtnDisable() {
        Intent intent = new Intent(context, SearchFilterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<SearchFilterActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.chipHighestOverall)).perform(scrollTo(), click());

            onView(withId(R.id.chipTopUniversities)).check(matches(not(isEnabled())));

            onView(withId(R.id.chipLowUniversities)).check(matches(not(isEnabled())));

            onView(withId(R.id.btnResetFilters)).perform(click());

            onView(withId(R.id.chipTopUniversities)).check(matches(isEnabled()));

            onView(withId(R.id.chipHighestOverall)).check(matches(isEnabled()));
        }
    }

    /*
     * Test # 18
     * tests homeactivity views
     */
    @Test
    public void homeActivityT() {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<HomeActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.homeTitle)).check(matches(isDisplayed()));

            onView(withId(R.id.actvUniversitySelect)).check(matches(isDisplayed()));

            onView(withId(R.id.btnSearchFilter)).check(matches(isDisplayed()));
        }
    }

    /*
     * Test # 19
     * test chatactivity valid id, name, username
     */
    @Test
    public void chatActivityChatT() {
        context.getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
                .edit()
                .putString("USERNAME", "test")
                .commit();

        Intent intent = new Intent(context, ChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("COURSE_ID", 10L);
        intent.putExtra("COURSE_NAME", "COMS309");

        try (ActivityScenario<ChatActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.tvChatCourseName)).check(matches(withText("COMS309 Chat")));

            onView(withId(R.id.roomInfoTv)).check(matches(withText("Logged in as: test")));

            onView(withId(R.id.tvConnectionStatus)).check(matches(isDisplayed()));
        }
    }

    /*
     * Test # 20
     * tests filterresultadapter with green (high) rating
     */
    @Test
    public void filterResultAdapterGreenT() {
        Intent intent = new Intent(context, SignupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<SignupActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                ArrayList<FilterResult> results = new ArrayList<>();

                results.add(new FilterResult(
                        1L,
                        "COURSE",
                        "COMS309",
                        "Iowa State University",
                        5.0,
                        "Overall Rating",
                        10
                ));

                FilterResultAdapter adapter = new FilterResultAdapter(activity, results);
                FrameLayout parent = new FrameLayout(activity);

                View row = adapter.getView(0, null, parent);

                TextView rating = row.findViewById(R.id.tvResultRating);
                TextView outOf = row.findViewById(R.id.tvResultRatingOutOf);
                TextView name = row.findViewById(R.id.ResultName);
                TextView subtitle = row.findViewById(R.id.tvResultSubtitle);
                TextView label = row.findViewById(R.id.tvResultRatingLabel);

                assertEquals("5.0", rating.getText().toString());
                assertEquals("/5", outOf.getText().toString());
                assertEquals("COMS309", name.getText().toString());
                assertEquals("Iowa State University", subtitle.getText().toString());
                assertEquals("Overall Rating", label.getText().toString());
            });
        }
    }

    /*
     * Test # 21
     * test filterresultadapter with yellow (mid) rating
     */
    @Test
    public void filterResultAdapterYellowT() {
        Intent intent = new Intent(context, SignupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<SignupActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                ArrayList<FilterResult> results = new ArrayList<>();

                results.add(new FilterResult(
                        2L,
                        "COURSE",
                        "COMS309",
                        "Iowa State University",
                        3.5,
                        "Difficulty",
                        10
                ));

                FilterResultAdapter adapter = new FilterResultAdapter(activity, results);
                FrameLayout parent = new FrameLayout(activity);

                View row = adapter.getView(0, null, parent);

                TextView rating = row.findViewById(R.id.tvResultRating);
                TextView outOf = row.findViewById(R.id.tvResultRatingOutOf);
                TextView name = row.findViewById(R.id.ResultName);

                assertEquals("3.5", rating.getText().toString());
                assertEquals("/5", outOf.getText().toString());
                assertEquals("COMS309", name.getText().toString());
            });
        }
    }

    /*
     * Test # 21
     * tests filterresultadapter with orange (low) rating
     */
    @Test
    public void filterResultAdapterOrangeT() {
        Intent intent = new Intent(context, SignupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<SignupActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                ArrayList<FilterResult> results = new ArrayList<>();

                results.add(new FilterResult(
                        3L,
                        "COURSE",
                        "Hard Course",
                        "Lot of work",
                        2.5,
                        "Workload",
                        5
                ));

                FilterResultAdapter adapter = new FilterResultAdapter(activity, results);
                FrameLayout parent = new FrameLayout(activity);

                View row = adapter.getView(0, null, parent);

                TextView rating = row.findViewById(R.id.tvResultRating);
                TextView outOf = row.findViewById(R.id.tvResultRatingOutOf);

                assertEquals("2.5", rating.getText().toString());
                assertEquals("/5", outOf.getText().toString());
            });
        }
    }

    /*
     * Test # 22
     * test filterresultadapter with red (low or no) rating
     */
    @Test
    public void filterResultAdapterRedT() {
        Intent intent = new Intent(context, SignupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<SignupActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                ArrayList<FilterResult> results = new ArrayList<>();

                results.add(new FilterResult(
                        4L,
                        "UNIVERSITY",
                        "Iowa State University",
                        "No ratings yet",
                        0.0,
                        "Average Rating",
                        0
                ));

                FilterResultAdapter adapter = new FilterResultAdapter(activity, results);
                FrameLayout parent = new FrameLayout(activity);

                View row = adapter.getView(0, null, parent);

                TextView rating = row.findViewById(R.id.tvResultRating);
                TextView outOf = row.findViewById(R.id.tvResultRatingOutOf);
                TextView name = row.findViewById(R.id.ResultName);

                assertEquals("N/A", rating.getText().toString());
                assertEquals("", outOf.getText().toString());
                assertEquals("Iowa State University", name.getText().toString());
            });
        }
    }

    /*
     * Test # 23
     * test adminclaimadapter getView
     */
    @Test
    public void adminClaimAdapterT() {
        Intent intent = new Intent(context, SignupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<SignupActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                ArrayList<AdminClaim> claim = new ArrayList<>();

                claim.add(new AdminClaim(
                        1L,
                        2L,
                        "emrah",
                        "OTHER",
                        "test",
                        "PENDING",
                        "today"
                ));

                AdminClaimAdapter adapter = new AdminClaimAdapter(activity, claim);
                View view = new View(activity);
                FrameLayout parent = new FrameLayout(activity);

                View view2 = adapter.getView(0, view, parent);

                assertSame(view, view2);
            });
        }
    }

    /*
     * Test # 24
     * tests claimrequestactivity error
     */
    @Test
    public void claimRequestErrorT() {
        context.getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
                .edit()
                .putLong("USER_ID", 1L)
                .putString("USERNAME", "emrah")
                .putString("EMAIL", "emrah@test.edu")
                .putString("ROLE", "USER")
                .commit();

        Intent intent = new Intent(context, ClaimRequestActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<ClaimRequestActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.etDescription)).perform(clearText(), closeSoftKeyboard());

            onView(withId(R.id.btnSubmitClaim)).perform(click());

            onView(withId(R.id.etDescription)).check(matches(hasErrorText("Description is required")));
        }
    }

    /*
     * Test # 25
     * test claimRequestActivity missing claim type error
     */
    @Test
    public void claimRequestClaimtErrorT() {
        context.getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
                .edit()
                .putLong("USER_ID", 1L)
                .putString("USERNAME", "emrah")
                .putString("EMAIL", "emrah@test.edu")
                .putString("ROLE", "USER")
                .commit();

        Intent intent = new Intent(context, ClaimRequestActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<ClaimRequestActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.actvClaimType)).perform(clearText(), closeSoftKeyboard());

            onView(withId(R.id.etDescription)).perform(clearText(), replaceText("123"), closeSoftKeyboard());

            onView(withId(R.id.btnSubmitClaim)).perform(click());

            onView(withId(R.id.actvClaimType)).check(matches(hasErrorText("Select a claim type")));
        }
    }

    /*
     * Test # 26
     * tests claimrequestactivity with no user id
     */
    @Test
    public void claimRequestNoUserT() {
        Intent intent = new Intent(context, ClaimRequestActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<ClaimRequestActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.etDescription)).perform(clearText(), replaceText("123"), closeSoftKeyboard());

            onView(withId(R.id.btnSubmitClaim)).perform(click());

            onView(withId(R.id.btnSubmitClaim)).check(matches(isDisplayed()));

            //toast message so cant test
        }
    }

    /*
     * Test # 27
     * tests chatWebsocketmanager setting and removing listener
     */
    @Test
    public void chatWebsocketManagerLisT() {
        ChatWebSocketManager manager = ChatWebSocketManager.getInstance();

        ChatWebSocketListener lis = new ChatWebSocketListener() {
            @Override
            public void onWebSocketOpen(ServerHandshake handshakedata) {

            }

            @Override
            public void onWebSocketMessage(String message) {

            }

            @Override
            public void onWebSocketClose(int code, String reason, boolean remote) {

            }

            @Override
            public void onWebSocketError(Exception ex) {

            };
        };
        manager.setWebSocketListener(lis);
        assertFalse(manager.isConnected());

        manager.removeWebSocketListener();
        assertFalse(manager.isConnected());

        manager.disconnectWebSocket();
        assertFalse(manager.isConnected());
        }

    /*
     * Test # 28
     * tests universityowneractivity where university_id is missing, disables btns
     */
    @Test
    public void universityOwnerDisableBtnT() {
        context.getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
                .edit()
                .putString("ROLE", "SCHOOL")
                .commit();

        Intent intent = new Intent(context, UniversityOwnerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("UNIVERSITY_NAME", "test");

        try (ActivityScenario<UniversityOwnerActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.etUniversityName)).check(matches(withText("test")));

            onView(withId(R.id.btnUpdateUniversity)).check(matches(not(isEnabled())));

            onView(withId(R.id.btnDeleteUniversity)).check(matches(not(isEnabled())));
        }
    }

    /*
     * Test # 29
     * tests universityOwnerActivity website validation
     */
    @Test
    public void universityOwnerWebsiteT() {
        context.getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
                .edit()
                .putString("ROLE", "SCHOOL")
                .putLong("UNIVERSITY_ID", 10L)
                .commit();

        Intent intent = new Intent(context, UniversityOwnerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("UNIVERSITY_ID", 10L);

        try (ActivityScenario<UniversityOwnerActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.etUniversityWebsite)).perform(clearText(), replaceText("test"), closeSoftKeyboard());

            onView(withId(R.id.btnUpdateUniversity)).perform(click());

            onView(withId(R.id.etUniversityWebsite)).check(matches(withText("test")));
        }
    }

    /*
     * Test # 30
     * test chatActivity uses username fro intent
     */
    @Test
    public void chatActivityIntentT() {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("COURSE_ID", 10L);
        intent.putExtra("COURSE_NAME", "COMS309");
        intent.putExtra("username", "test");

        try (ActivityScenario<ChatActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.tvChatCourseName)).check(matches(withText("COMS309 Chat")));

            onView(withId(R.id.roomInfoTv)).check(matches(withText("Logged in as: test")));
        }
    }

    /*
     * Test # 31
     * test AdminClaimActivity admin btns
     */
    @Test
    public void adminClaimBtnT() {
        context.getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
                .edit()
                .putString("ROLE", "ADMIN")
                .putString("USERNAME", "Admin")
                .putString("EMAIL", "admin@test.edu")
                .commit();

        Intent intent = new Intent(context, AdminClaimActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<AdminClaimActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.btnTabClaims)).check(matches(isDisplayed()));

            onView(withId(R.id.btnTabFlags)).check(matches(isDisplayed()));

            onView(withId(R.id.etSearchClaims)).check(matches(isDisplayed()));

            onView(withId(R.id.actvStatusFilter)).check(matches(withText("ALL")));
        }
    }

    /*
     * Test # 32
     * tests adminclaimactivity flags tab
     */
    @Test
    public void adminClaimTabT() {
        context.getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
                .edit()
                .putString("ROLE", "ADMIN")
                .putString("USERNAME", "Admin")
                .putString("EMAIL", "admin@test.edu")
                .commit();

        Intent intent = new Intent(context, AdminClaimActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<AdminClaimActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.btnTabFlags)).perform(click());

            onView(withId(R.id.etSearchFlags)).check(matches(isDisplayed()));

            onView(withId(R.id.actvFlagStatusFilter)).check(matches(withText("ALL")));
        }
    }

    /*
     * Test # 33
     * tests profileactivity username (from intent)
     */
    @Test
    public void profileUsernameT() {
        context.getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
                .edit()
                .putLong("USER_ID", 1L)
                .putString("USERNAME", "test")
                .putString("EMAIL", "test@test.edu")
                .putString("ROLE", "USER")
                .commit();

        Intent intent = new Intent(context, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<ProfileActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.welcomeTxt)).check(matches(withText("Welcome, test!")));
        }
    }

    /*
     * Test # 34
     * test profileactivity guest takes to login
     */
    @Test
    public void profileGuestT() {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<ProfileActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.loginButton)).check(matches(isDisplayed()));

            onView(withId(R.id.usernameInput)).check(matches(isDisplayed()));

            onView(withId(R.id.passwordInput)).check(matches(isDisplayed()));
        }
    }

    /*
     * Test # 35
     * tests homeactivity search filter button opens searchfilteractivity
     */
    @Test
    public void homeActivitySearchFilterBtnT() {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<HomeActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.btnSearchFilter)).perform(click());

            onView(withId(R.id.btnResetFilters)).check(matches(isDisplayed()));
        }
    }

    /*
     * Test # 36
     * test universityowneractivity admin btn
     */
    @Test
    public void universityOwnerAdminT() {
        context.getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
                .edit()
                .putString("ROLE", "SCHOOL")
                .putLong("UNIVERSITY_ID", 10L)
                .commit();

        Intent intent = new Intent(context, UniversityOwnerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("UNIVERSITY_ID", 10L);
        intent.putExtra("UNIVERSITY_NAME", "test");

        try (ActivityScenario<UniversityOwnerActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.btnAdminHelp)).perform(click());

            onView(withId(R.id.btnSubmitClaim)).check(matches(isDisplayed()));
        }
    }

    /*
     * Test # 37
     * tests signup empty fields
     */
    @Test
    public void chatMessageJSONT() {
        Intent intent = new Intent(context, SignupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<SignupActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.etUsername)).perform(clearText(), closeSoftKeyboard());

            onView(withId(R.id.etEmail)).perform(clearText(), closeSoftKeyboard());

            onView(withId(R.id.etPassHash)).perform(clearText(), closeSoftKeyboard());

            onView(withId(R.id.btnSignup)).perform(click());

            onView(withId(R.id.btnSignup)).check(matches(isDisplayed()));
        }
    }

    /*
     * Test # 38
     * tests signupactivity switching university fields and user fields
     */
    @Test
    public void signupUserUniversityT() {
        Intent intent = new Intent(context, SignupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<SignupActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.rbUniversity)).perform(click());

            onView(withId(R.id.etUniversityName)).check(matches(isDisplayed()));

            onView(withId(R.id.etUniversityDescription)).check(matches(isDisplayed()));

            onView(withId(R.id.etUniversityLogoUrl)).check(matches(isDisplayed()));

            onView(withId(R.id.rbUser)).perform(click());

            onView(withId(R.id.etUniversityName)).check(matches(withEffectiveVisibility(Visibility.GONE)));
            onView(withId(R.id.etUniversityDescription)).check(matches(withEffectiveVisibility(Visibility.GONE)));
            onView(withId(R.id.etUniversityLogoUrl)).check(matches(withEffectiveVisibility(Visibility.GONE)));
        }
    }

    /*
     * Test # 39
     * tests switching between two course chips
     * searchfilteractivity
     */
    @Test
    public void searchFilterSwitchT() {
        Intent intent = new Intent(context, SearchFilterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<SearchFilterActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.chipHighestOverall)).perform(scrollTo(), click());
            onView(withId(R.id.chipTopUniversities)).check(matches(not(isEnabled())));

            onView(withId(R.id.chipLowestOverall)).perform(scrollTo(), click());
            onView(withId(R.id.chipTopUniversities)).check(matches(not(isDisplayed())));

            onView(withId(R.id.btnResetFilters)).perform(click());
            onView(withId(R.id.chipTopUniversities)).check(matches(isEnabled()));
        }
    }

    /*
     * Test # 40
     * tests
     */

    /*
     * Test # 41
     * tests chatMessage fromJSON parse
     */
    @Test
    public void chatMessagefromJSONT() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("type", "system");
        obj.put("sender", "server");
        obj.put("room", "COMS309");
        obj.put("content", "User joined");
        obj.put("timestamp", 999L);

        ChatMessage msg = ChatMessage.fromJSON(obj.toString());

        assertEquals("system", msg.type);
        assertEquals("server", msg.sender);
        assertEquals("COMS309", msg.room);
        assertEquals("User joined", msg.content);
        assertEquals(999L, msg.timestamp);
    }

    /*
     * Test # 42
     * tests chatMessage fromJson missing fields
     */
    @Test
    public void chatMessagefromJSONT1() throws Exception{
        ChatMessage msg = ChatMessage.fromJSON("{}");

        assertEquals("chat", msg.type);
        assertEquals("Unknown", msg.sender);
        assertEquals("general", msg.room);
        assertEquals("", msg.content);
    }

    /*
     * Test # 43
     * tests adminclaimactivity and clicking delete claim with no selected claim
     */
    @Test
    public void adminClaimDeleteT() {
        context.getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
                .edit()
                .putString("ROLE", "ADMIN")
                .putString("USERNAME", "Admin")
                .putString("EMAIL", "test@test.edu")
                .commit();

        Intent intent = new Intent(context, AdminClaimActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<AdminClaimActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.btnDeleteClaim)).perform(click());
            onView(withId(R.id.btnDeleteClaim)).check(matches(isDisplayed()));
        }
    }

    /*
     * Test # 44
     * tests adminclaimactivity typing in search bar
     */
    @Test
    public void adminClaimTypeT() {
        context.getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
                .edit()
                .putString("ROLE", "ADMIN")
                .putString("USERNAME", "Admin")
                .putString("EMAIL", "test@test.edu")
                .commit();

        Intent intent = new Intent(context, AdminClaimActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<AdminClaimActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.etSearchClaims)).perform(clearText(), replaceText("emrah"), closeSoftKeyboard());

            onView(withId(R.id.etSearchClaims)).check(matches(withText("emrah")));
            onView(withId(R.id.lvClaims)).check(matches(isDisplayed()));
        }
    }

    /*
     * Test # 45
     * tests adminclaimactivity and changing dropdown to pending
     */
    @Test
    public void adminClaimPendingT() {
        context.getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
                .edit()
                .putString("ROLE", "ADMIN")
                .putString("USERNAME", "Admin")
                .putString("EMAIL", "test@test.edu")
                .commit();

        Intent intent = new Intent(context, AdminClaimActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<AdminClaimActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.actvStatusFilter)).perform(clearText(), replaceText("PENDING"), closeSoftKeyboard());

            onView(withId(R.id.actvStatusFilter)).check(matches(withText("PENDING")));
            onView(withId(R.id.lvClaims)).check(matches(isDisplayed()));
        }
    }

    /*
     * Test # 46
     * tests adminclaimactivity and trying in search flags bar
     */
    @Test
    public void adminClaimFlagSearchT() {
        context.getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
                .edit()
                .putString("ROLE", "ADMIN")
                .putString("USERNAME", "Admin")
                .putString("EMAIL", "test@test.edu")
                .commit();

        Intent intent = new Intent(context, AdminClaimActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<AdminClaimActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.btnTabFlags)).perform(click());

            onView(withId(R.id.etSearchFlags)).perform(clearText(), replaceText("spam"), closeSoftKeyboard());

            onView(withId(R.id.etSearchFlags)).check(matches(withText("spam")));
            onView(withId(R.id.lvFlags)).check(matches(isDisplayed()));
        }
    }

    /*
     * Test # 47
     * tests adminclaimactivity dismiss flag and delete review are visible
     */
    @Test
    public void adminClaimFlagBtnsT() {
        context.getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE)
                .edit()
                .putString("ROLE", "ADMIN")
                .putString("USERNAME", "Admin")
                .putString("EMAIL", "test@test.edu")
                .commit();

        Intent intent = new Intent(context, AdminClaimActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<AdminClaimActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.btnTabFlags)).perform(click());

            onView(withId(R.id.btnDismissFlag)).check(matches(isDisplayed()));
            onView(withId(R.id.btnDeleteReview)).check(matches(isDisplayed()));
        }
    }

    /*
     * Test # 48
     * tests signupactivity hme btn
     */
    @Test
    public void signupHomebtnT() {
        Intent intent = new Intent(context, SignupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<SignupActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.btnHome)).perform(click());

            onView(withId(R.id.homeTitle)).check(matches(isDisplayed()));
        }
    }

    /*
     * Test # 49
     * tests signupactivity login btn
     */
    @Test
    public void signupLoginBtnT() {
        Intent intent = new Intent(context, SignupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<SignupActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.btnLogin)).perform(click());

            onView(withId(R.id.loginButton)).check(matches(isDisplayed()));
        }
    }

    /*
     * Test # 50
     * tests signupactivity submitting valid email but no username
     */
    @Test
    public void signupNoUserT() {
        Intent intent = new Intent(context, SignupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<SignupActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.etUsername)).perform(clearText(), closeSoftKeyboard());

            onView(withId(R.id.etEmail)).perform(clearText(), replaceText("test@test.edu"), closeSoftKeyboard());

            onView(withId(R.id.etPassHash)).perform(clearText(), replaceText("123"), closeSoftKeyboard());

            onView(withId(R.id.btnSignup)).perform(click());

            onView(withId(R.id.btnSignup)).check(matches(isDisplayed()));
        }
    }

    /*
     * Test # 51
     * tests homactivity university dropdown calls function
     */
    @Test
    public void homeActivityDT() {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try (ActivityScenario<HomeActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.actvUniversitySelect)).perform(click());

            onView(withId(R.id.actvUniversitySelect)).check(matches(isDisplayed()));

            onView(withId(R.id.actvUniversitySelect)).check(matches(isEnabled()));
        }
    }


}
