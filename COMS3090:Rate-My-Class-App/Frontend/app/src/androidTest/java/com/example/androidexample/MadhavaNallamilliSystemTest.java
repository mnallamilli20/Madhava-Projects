package com.example.androidexample;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewAssertion;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MadhavaNallamilliSystemTest {

    private static final String SCHOOL_USERNAME = "Paper";
    private static final String SCHOOL_PASSWORD = "123";

    private static final String STUDENT10_USERNAME = "TestStudent";
    private static final String STUDENT10_PASSWORD = "123";

    private static final String STUDENT11_USERNAME = "TestStudent2";
    private static final String STUDENT11_PASSWORD = "password";

    private static final String UNIVERSITY_NAME_TO_SELECT = "Paper University";

    private static final String COURSE_ONE_NAME = "Software Development";
    private static final String COURSE_ONE_CODE = "SE 309";
    private static final String COURSE_ONE_SUBJECT = "Software Engineering";
    private static final String COURSE_ONE_DESCRIPTION =
            "Covers software design, development practices, teamwork, testing, and project implementation.";

    private static final String COURSE_ONE_EDITED_DESCRIPTION =
            "Edited course covering frontend, backend, teamwork, testing, and software project maintenance.";

    private static final String COURSE_TWO_NAME = "Introduction to Computer Science";
    private static final String COURSE_TWO_CODE = "COM S 127";
    private static final String COURSE_TWO_SUBJECT = "Computer Science";
    private static final String COURSE_TWO_DESCRIPTION =
            "Introduces programming fundamentals, problem solving, algorithms, and basic computer science concepts.";

    private static final String COURSE_TWO_EDITED_DESCRIPTION =
            "Edited course covering programming basics, algorithms, debugging, and introductory computer science ideas.";

    private static final String REVIEW_ONE_COMMENT =
            "Student10 review for Software Development test.";

    private static final String REVIEW_TWO_COMMENT =
            "Student10 review for Introduction to Computer Science test.";

    private static final String FLAG_DESCRIPTION =
            "Student11 is flagging this review for the flag review test.";

    private static final String SCHEDULE_NAME =
            "Student10 Test Schedule";

    @Before
    public void setup() {
        clearPrefs();
    }

    /*
     * Test 1:
     * Paper logs in, creates both courses, then edits both courses.
     */
    @Test
    public void test1() {
        ActivityScenario.launch(LoginPage.class);

        loginThroughUi(SCHOOL_USERNAME, SCHOOL_PASSWORD);

        waitFor(4000);

        openLakeUniversityCoursePageFromSavedLogin();

        waitFor(3000);

        addCourse(
                COURSE_ONE_NAME,
                COURSE_ONE_CODE,
                COURSE_ONE_SUBJECT,
                COURSE_ONE_DESCRIPTION
        );

        waitFor(2000);

        addCourse(
                COURSE_TWO_NAME,
                COURSE_TWO_CODE,
                COURSE_TWO_SUBJECT,
                COURSE_TWO_DESCRIPTION
        );

        waitFor(3000);

        openCourseFromDropdown(COURSE_ONE_NAME);

        waitFor(3000);

        editCourse(
                COURSE_ONE_NAME,
                COURSE_ONE_CODE,
                COURSE_ONE_SUBJECT,
                COURSE_ONE_EDITED_DESCRIPTION
        );

        waitFor(3000);

        openLakeUniversityCoursePageFromSavedLogin();

        waitFor(3000);

        openCourseFromDropdown(COURSE_TWO_NAME);

        waitFor(3000);

        editCourse(
                COURSE_TWO_NAME,
                COURSE_TWO_CODE,
                COURSE_TWO_SUBJECT,
                COURSE_TWO_EDITED_DESCRIPTION
        );

        waitFor(3000);
    }

    /*
     * Test 2:
     * TestStudent creates a review on Software Development and likes it.
     */
    @Test
    public void test2() {
        ActivityScenario.launch(LoginPage.class);

        loginStudent10AndGoHome();

        openUniversityFromHomeDropdown();

        waitFor(3000);

        openCourseFromDropdown(COURSE_ONE_NAME);

        waitFor(3000);

        openReviewsPage();

        createReview(REVIEW_ONE_COMMENT);

        waitFor(3000);

        clickReviewButtonByComment(REVIEW_ONE_COMMENT, R.id.btnLike);

        waitFor(2000);
    }

    /*
     * Test 3:
     * TestStudent creates a review on Introduction to Computer Science and dislikes it.
     */
    @Test
    public void test3() {
        ActivityScenario.launch(LoginPage.class);

        loginStudent10AndGoHome();

        openUniversityFromHomeDropdown();

        waitFor(3000);

        openCourseFromDropdown(COURSE_TWO_NAME);

        waitFor(3000);

        openReviewsPage();

        createReview(REVIEW_TWO_COMMENT);

        waitFor(3000);

        clickReviewButtonByComment(REVIEW_TWO_COMMENT, R.id.btnDislike);

        waitFor(2000);
    }

    /*
     * Test 4:
     * TestStudent calculates schedule difficulty, saves a schedule,
     * goes back into schedule builder, views saved schedules,
     * then deletes the saved schedule.
     */
    @Test
    public void test4() {
        ActivityScenario.launch(LoginPage.class);

        loginStudent10AndGoHome();

        openUniversityFromHomeDropdown();

        waitFor(3000);

        openScheduleBuilderFromUniversityPage();

        waitFor(2000);

        addCourseToSchedule(COURSE_ONE_NAME);
        addCourseToSchedule(COURSE_TWO_NAME);

        onView(withId(R.id.btnCalculateScheduleDifficulty))
                .perform(click());

        waitFor(1000);

        onView(withId(R.id.etScheduleName))
                .perform(typeText(SCHEDULE_NAME), closeSoftKeyboard());

        onView(withId(R.id.btnSaveSchedule))
                .perform(click());

        waitFor(3000);

        /*
         * After saving, your app moves back to the university page.
         * Go back into schedule builder before viewing saved schedules.
         */
        openScheduleBuilderFromUniversityPage();

        waitFor(2000);

        onView(withId(R.id.btnViewSavedSchedules))
                .perform(click());

        waitFor(3000);

        /*
         * Delete saved schedule from SavedSchedulesActivity.
         */
        deleteSavedScheduleFromList();

        waitFor(2000);

        /*
         * Leave saved schedules page cleanly.
         */
        Intent homeIntent = new Intent(
                ApplicationProvider.getApplicationContext(),
                HomeActivity.class
        );
        ActivityScenario.launch(homeIntent);

        waitFor(2000);
    }

    /*
     * Test 5:
     * TestStudent2 flags TestStudent's Software Development review.
     */
    @Test
    public void test5() {
        ActivityScenario.launch(LoginPage.class);

        loginStudent11AndGoHome();

        openUniversityFromHomeDropdown();

        waitFor(3000);

        openCourseFromDropdown(COURSE_ONE_NAME);

        waitFor(3000);

        openReviewsPage();

        waitFor(3000);

        clickReviewButtonByComment(REVIEW_ONE_COMMENT, R.id.btnFlagReview);

        waitFor(2000);

        selectFlagReason("MISLEADING");

        onView(withId(R.id.etFlagDescription))
                .perform(typeText(FLAG_DESCRIPTION), closeSoftKeyboard());

        onView(withId(R.id.btnSubmitFlag))
                .perform(click());

        waitFor(2000);
    }

    /*
     * Test 6:
     * TestStudent deletes the review that was not flagged.
     * The flagged review is Software Development, so delete the Intro CS review.
     */
    @Test
    public void test6() {
        ActivityScenario.launch(LoginPage.class);

        loginStudent10AndGoHome();

        openUniversityFromHomeDropdown();

        waitFor(3000);

        openCourseFromDropdown(COURSE_TWO_NAME);

        waitFor(3000);

        openReviewsPage();

        waitFor(3000);

        clickReviewButtonByComment(REVIEW_TWO_COMMENT, R.id.btnDeleteReview);

        waitFor(1000);

        /*
         * Some delete flows show a confirmation dialog.
         * Some delete immediately. This handles both.
         */
        tryClickDeleteDialogIfPresent();

        waitFor(1500);

        /*
         * After deleting the review, leave the review page so the test ends cleanly.
         */
        Intent homeIntent = new Intent(
                ApplicationProvider.getApplicationContext(),
                HomeActivity.class
        );
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ApplicationProvider.getApplicationContext().startActivity(homeIntent);

        waitFor(2000);
    }

    /*
     * Test 7:
     * Paper deletes both courses last.
     */
    @Test
    public void test7() {
        ActivityScenario.launch(LoginPage.class);

        loginThroughUi(SCHOOL_USERNAME, SCHOOL_PASSWORD);

        waitFor(4000);

        deleteCourseAndVerifyGone(COURSE_ONE_NAME);

        waitFor(2000);

        deleteCourseAndVerifyGone(COURSE_TWO_NAME);
    }

    private void loginThroughUi(String username, String password) {
        onView(withId(R.id.usernameInput))
                .perform(typeText(username), closeSoftKeyboard());

        onView(withId(R.id.passwordInput))
                .perform(typeText(password), closeSoftKeyboard());

        onView(withId(R.id.loginButton))
                .perform(click());
    }

    private void loginStudent10AndGoHome() {
        loginThroughUi(STUDENT10_USERNAME, STUDENT10_PASSWORD);

        waitFor(3000);

        onView(withId(R.id.homeBtn))
                .perform(click());

        waitFor(3000);
    }

    private void loginStudent11AndGoHome() {
        loginThroughUi(STUDENT11_USERNAME, STUDENT11_PASSWORD);

        waitFor(3000);

        onView(withId(R.id.homeBtn))
                .perform(click());

        waitFor(3000);
    }

    private void openUniversityFromHomeDropdown() {
        onView(withId(R.id.actvUniversitySelect))
                .perform(click(), typeText(UNIVERSITY_NAME_TO_SELECT), closeSoftKeyboard());

        waitFor(1500);

        onData(anything())
                .inRoot(isPlatformPopup())
                .atPosition(0)
                .perform(click());
    }

    private void openLakeUniversityCoursePageFromSavedLogin() {
        Context context = ApplicationProvider.getApplicationContext();

        SharedPreferences prefs = context.getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE);

        long universityId = prefs.getLong("UNIVERSITY_ID", -1L);
        String universityName = prefs.getString("UNIVERSITY_NAME", UNIVERSITY_NAME_TO_SELECT);

        if (universityId == -1L) {
            throw new RuntimeException("Paper login did not save UNIVERSITY_ID.");
        }

        Intent intent = new Intent(context, UniversityActivity.class);
        intent.putExtra("UNIVERSITY_ID", universityId);
        intent.putExtra("UNIVERSITY_NAME", universityName);

        ActivityScenario.launch(intent);
    }

    private void openScheduleBuilderFromUniversityPage() {
        onView(withId(R.id.btnScheduleDifficulty))
                .perform(click());
    }

    /*
     * Adds a course only. No dropdown verification afterward.
     */
    private void addCourse(String name, String code, String subject, String description) {
        onView(withId(R.id.btnAddCourse))
                .perform(click());

        waitFor(500);

        onView(withHint("Course name"))
                .inRoot(isDialog())
                .perform(typeText(name), closeSoftKeyboard());

        onView(withHint("Course code"))
                .inRoot(isDialog())
                .perform(typeText(code), closeSoftKeyboard());

        onView(withHint("Subject"))
                .inRoot(isDialog())
                .perform(typeText(subject), closeSoftKeyboard());

        onView(withHint("Description"))
                .inRoot(isDialog())
                .perform(typeText(description), closeSoftKeyboard());

        onView(withText("Create"))
                .inRoot(isDialog())
                .perform(click());

        waitFor(3000);
    }

    private void openCourseFromDropdown(String courseName) {
        onView(withId(R.id.actvCourseSelect))
                .perform(click(), typeText(courseName), closeSoftKeyboard());

        waitFor(1500);

        onData(anything())
                .inRoot(isPlatformPopup())
                .atPosition(0)
                .perform(click());
    }

    private void openReviewsPage() {
        onView(withId(R.id.btnViewReviews))
                .perform(click());

        waitFor(2000);
    }

    private void createReview(String comment) {
        onView(withId(R.id.btnWriteReview))
                .perform(click());

        waitFor(2000);

        selectSpinnerOption(R.id.spinnerOverallRating, "5");
        selectSpinnerOption(R.id.spinnerDifficultyRating, "3");
        selectSpinnerOption(R.id.spinnerWorkloadRating, "4");
        selectSpinnerOption(R.id.spinnerGradeReceived, "A");

        onView(withText("Yes"))
                .perform(click());

        onView(withId(R.id.etComment))
                .perform(typeText(comment), closeSoftKeyboard());

        onView(withId(R.id.btnSubmitReview))
                .perform(click());

        waitFor(3000);
    }

    private void selectSpinnerOption(int spinnerId, String optionText) {
        onView(withId(spinnerId))
                .perform(click());

        onData(allOf(is(instanceOf(String.class)), is(optionText)))
                .perform(click());

        waitFor(500);
    }

    private void selectFlagReason(String reason) {
        onView(withId(R.id.actvFlagReason))
                .perform(click());

        waitFor(500);

        onData(allOf(is(instanceOf(String.class)), is(reason)))
                .inRoot(isPlatformPopup())
                .perform(click());

        waitFor(500);
    }

    private void addCourseToSchedule(String courseName) {
        onView(withId(R.id.actvCoursePicker))
                .perform(click(), typeText(courseName), closeSoftKeyboard());

        waitFor(1500);

        onData(anything())
                .inRoot(isPlatformPopup())
                .atPosition(0)
                .perform(click());

        waitFor(500);

        onView(withId(R.id.btnAddSelectedCourse))
                .perform(click());

        waitFor(1000);
    }

    /*
     * Long-clicks the first saved schedule and confirms delete.
     * This assumes SavedSchedulesActivity has long-click delete enabled.
     */
    private void deleteSavedScheduleFromList() {
        onData(anything())
                .inAdapterView(withId(R.id.listSchedules))
                .atPosition(0)
                .perform(longClick());

        waitFor(1000);

        onView(withText("Delete"))
                .inRoot(isDialog())
                .perform(click());

        waitFor(2000);
    }

    private void editCourse(String name, String code, String subject, String description) {
        onView(withId(R.id.btnEditCourse))
                .perform(click());

        waitFor(500);

        onView(withHint("Course name"))
                .inRoot(isDialog())
                .perform(clearText(), typeText(name), closeSoftKeyboard());

        onView(withHint("Course code"))
                .inRoot(isDialog())
                .perform(clearText(), typeText(code), closeSoftKeyboard());

        onView(withHint("Subject"))
                .inRoot(isDialog())
                .perform(clearText(), typeText(subject), closeSoftKeyboard());

        onView(withHint("Description"))
                .inRoot(isDialog())
                .perform(clearText(), typeText(description), closeSoftKeyboard());

        onView(withText("Save"))
                .inRoot(isDialog())
                .perform(click());
    }

    private void deleteCurrentCourse() {
        onView(withId(R.id.btnDeleteCourse))
                .perform(click());

        waitFor(500);

        onView(withText("Delete"))
                .inRoot(isDialog())
                .perform(click());

        waitFor(3000);
    }

    private void deleteCourseAndVerifyGone(String courseName) {
        openLakeUniversityCoursePageFromSavedLogin();

        waitFor(3000);

        openCourseFromDropdown(courseName);

        waitFor(3000);

        deleteCurrentCourse();

        waitFor(3000);

        Intent homeIntent = new Intent(
                ApplicationProvider.getApplicationContext(),
                HomeActivity.class
        );
        ActivityScenario.launch(homeIntent);

        waitFor(4000);

        openUniversityFromHomeDropdown();

        waitFor(4000);

        /*
         * This verifies that after going back to home/search and re-entering
         * the university, the deleted course is no longer in the dropdown.
         */
        onView(withId(R.id.actvCourseSelect))
                .check(courseNotInDropdown(courseName));
    }

    private ViewAssertion courseNotInDropdown(String deletedCourseName) {
        return (view, noViewFoundException) -> {
            if (noViewFoundException != null) {
                throw noViewFoundException;
            }

            if (!(view instanceof AutoCompleteTextView)) {
                throw new AssertionError("View is not an AutoCompleteTextView");
            }

            AutoCompleteTextView dropdown = (AutoCompleteTextView) view;
            ListAdapter adapter = dropdown.getAdapter();

            if (adapter == null) {
                return;
            }

            for (int i = 0; i < adapter.getCount(); i++) {
                Object item = adapter.getItem(i);

                if (item != null && item.toString().equals(deletedCourseName)) {
                    throw new AssertionError(
                            "Deleted course still appears in dropdown: " + deletedCourseName
                    );
                }
            }
        };
    }

    private void clickReviewButtonByComment(String commentText, int childButtonId) {
        onView(withId(R.id.recyclerReviews))
                .perform(clickChildInRecyclerRowWithText(commentText, childButtonId));
    }

    private ViewAction clickChildInRecyclerRowWithText(String targetText, int childButtonId) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "Click child button in RecyclerView row containing text: " + targetText;
            }

            @Override
            public void perform(UiController uiController, View view) {
                if (!(view instanceof RecyclerView)) {
                    throw new AssertionError("View is not a RecyclerView.");
                }

                RecyclerView recyclerView = (RecyclerView) view;

                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    View row = recyclerView.getChildAt(i);

                    if (rowContainsText(row, targetText)) {
                        View button = row.findViewById(childButtonId);

                        if (button == null) {
                            throw new AssertionError(
                                    "Button with id " + childButtonId + " not found in matching review row."
                            );
                        }

                        button.performClick();
                        return;
                    }
                }

                throw new AssertionError(
                        "No visible review row found containing text: " + targetText
                );
            }
        };
    }

    private boolean rowContainsText(View view, String targetText) {
        if (view instanceof TextView) {
            CharSequence text = ((TextView) view).getText();

            return text != null && text.toString().contains(targetText);
        }

        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) view;

            for (int i = 0; i < group.getChildCount(); i++) {
                if (rowContainsText(group.getChildAt(i), targetText)) {
                    return true;
                }
            }
        }

        return false;
    }

    /*
     * Clicks the Delete confirmation dialog only if it appears.
     * If no dialog appears, it assumes the delete happened immediately.
     */
    private void tryClickDeleteDialogIfPresent() {
        try {
            onView(withText("Delete"))
                    .inRoot(isDialog())
                    .perform(click());
        } catch (Exception ignored) {
        }
    }

    private void clearPrefs() {
        Context context = ApplicationProvider.getApplicationContext();

        SharedPreferences prefs = context.getSharedPreferences("USER_PREFS", Context.MODE_PRIVATE);

        prefs.edit().clear().apply();
    }

    private void waitFor(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}