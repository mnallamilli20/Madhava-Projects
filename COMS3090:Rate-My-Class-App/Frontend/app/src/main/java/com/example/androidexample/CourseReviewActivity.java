package com.example.androidexample;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CourseReviewActivity extends AppCompatActivity implements
        ReviewAdapter.OnReactionClickListener {

    private static final String BASE_URL = "http://coms-3090-018.class.las.iastate.edu:8080";

    private Button btnBackReviews, btnWriteReview;
    private TextView tvReviewCourseTitle;
    private RecyclerView recyclerReviews;

    private final ArrayList<Review> reviewList = new ArrayList<>();
    private ReviewAdapter adapter;

    private long courseId = -1L;
    private String courseName = "";
    private long currentUserId = -1L;
    private String currentRole = "GUEST";
    private boolean userAlreadyReviewed = false;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_review);

        btnBackReviews = findViewById(R.id.btnBackReviews);
        btnWriteReview = findViewById(R.id.btnWriteReview);
        tvReviewCourseTitle = findViewById(R.id.tvReviewCourseTitle);
        recyclerReviews = findViewById(R.id.recyclerReviews);

        courseId = getIntent().getLongExtra("COURSE_ID", -1L);
        courseName = getIntent().getStringExtra("COURSE_NAME");

        if (courseName == null || courseName.trim().isEmpty()) {
            courseName = "Course";
        }

        tvReviewCourseTitle.setText(courseName + " Reviews");

        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        currentUserId = prefs.getLong("USER_ID", -1L);
        currentRole = prefs.getString("ROLE", "GUEST");

        adapter = new ReviewAdapter(reviewList, currentUserId, this);
        recyclerReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerReviews.setAdapter(adapter);

        btnBackReviews.setOnClickListener(v -> finish());

        btnWriteReview.setOnClickListener(v -> {
            boolean canWriteReview = currentUserId != -1L &&
                    ("USER".equals(currentRole) || "ADMIN".equals(currentRole));

            if (!canWriteReview) {
                Toast.makeText(this, "Please log in as a user or admin to write a review", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userAlreadyReviewed) {
                Toast.makeText(this, "You already reviewed this course", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(CourseReviewActivity.this, CreateReviewActivity.class);
            intent.putExtra("COURSE_ID", courseId);
            intent.putExtra("COURSE_NAME", courseName);
            startActivity(intent);
        });

        updateWriteReviewButton();

        if (courseId == -1L) {
            Toast.makeText(this, "Missing course id", Toast.LENGTH_SHORT).show();
            return;
        }

        NotificationHelper.createChannel(this);
        requestNotificationPermissionIfNeeded();

        fetchReviews(courseId);
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void fetchReviews(long courseId) {
        String url = BASE_URL + "/api/reviews/course/" + courseId;

        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    reviewList.clear();
                    userAlreadyReviewed = false;

                    for (int i = 0; i < response.length(); i++) {
                        JSONObject obj = response.optJSONObject(i);
                        if (obj == null) continue;

                        long reviewId = obj.optLong("review_id", obj.optLong("reviewId", -1L));

                        if (reviewId == -1L) {
                            Log.d("REVIEWS", "Missing review id in JSON: " + obj.toString());
                            continue;
                        }

                        int overall = obj.optInt("overall_rating", 0);
                        int difficulty = obj.optInt("difficulty_rating", 0);
                        int workload = obj.optInt("workload_rating", 0);
                        boolean recommendation = obj.optBoolean("recommendation", false);

                        String backendGrade = obj.optString("grade_received", "N/A");
                        String grade = formatGrade(backendGrade);

                        String comment = obj.optString("comment", "No comment");
                        int likeCount = obj.optInt("like_count", 0);
                        int dislikeCount = obj.optInt("dislike_count", 0);
                        String createdOn = obj.optString("created_on", "Unknown");

                        String username = "Unknown User";
                        long reviewUserId = -1L;

                        JSONObject userObj = obj.optJSONObject("user");
                        if (userObj != null) {
                            username = userObj.optString("username", "Unknown User");
                            reviewUserId = userObj.optLong("user_id", userObj.optLong("userId", -1L));
                        }

                        if (reviewUserId == currentUserId) {
                            userAlreadyReviewed = true;
                        }

                        reviewList.add(new Review(
                                reviewId,
                                reviewUserId,
                                username,
                                overall,
                                difficulty,
                                workload,
                                recommendation,
                                grade,
                                comment,
                                likeCount,
                                dislikeCount,
                                createdOn
                        ));
                    }

                    adapter.notifyDataSetChanged();
                    updateWriteReviewButton();
                },
                error -> Toast.makeText(
                        this,
                        "Failed to load reviews: " + error.toString(),
                        Toast.LENGTH_SHORT
                ).show()
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(req);
    }

    private void updateWriteReviewButton() {
        boolean canWriteReview = currentUserId != -1L &&
                ("USER".equals(currentRole) || "ADMIN".equals(currentRole));

        if (!canWriteReview) {
            btnWriteReview.setVisibility(View.GONE);
            return;
        }

        btnWriteReview.setVisibility(View.VISIBLE);

        if (userAlreadyReviewed) {
            btnWriteReview.setEnabled(false);
            btnWriteReview.setText("Review Already Submitted");
        } else {
            btnWriteReview.setEnabled(true);
            btnWriteReview.setText("Write Review");
        }
    }

    private String formatGrade(String backendGrade) {
        switch (backendGrade) {
            case "A_PLUS":
                return "A+";
            case "A_MINUS":
                return "A-";
            case "B_PLUS":
                return "B+";
            case "B_MINUS":
                return "B-";
            case "C_PLUS":
                return "C+";
            case "C_MINUS":
                return "C-";
            case "D_PLUS":
                return "D+";
            case "D_MINUS":
                return "D-";
            default:
                return backendGrade;
        }
    }

    @Override
    public void onLikeClicked(int position) {
        sendReaction(position, "LIKE");
    }

    @Override
    public void onDislikeClicked(int position) {
        sendReaction(position, "DISLIKE");
    }

    @Override
    public void onFlagClicked(int position) {
        if (position < 0 || position >= reviewList.size()) {
            return;
        }

        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        long userId = prefs.getLong("USER_ID", -1L);

        if (userId == -1L) {
            Toast.makeText(this, "Please Login to Flag Review", Toast.LENGTH_SHORT).show();
            return;
        }

        Review review = reviewList.get(position);

        Intent intent = new Intent(CourseReviewActivity.this, FlagReviewActivity.class);
        intent.putExtra("REVIEW_ID", review.review_id);
        intent.putExtra("REVIEW_USERNAME", review.username);
        intent.putExtra("REVIEW_COMMENT", review.comment);
        startActivity(intent);
    }

    @Override
    public void onDeleteClicked(int position) {
        if (position < 0 || position >= reviewList.size()) {
            return;
        }

        Review review = reviewList.get(position);

        if (review.user_id != currentUserId) {
            Toast.makeText(this, "You can only delete your own review", Toast.LENGTH_SHORT).show();
            return;
        }

        if (review.review_id == -1L) {
            Toast.makeText(this, "Invalid review id", Toast.LENGTH_SHORT).show();
            return;
        }

        deleteReview(review.review_id);
    }

    private void sendReaction(int position, String reactionType) {
        if (position < 0 || position >= reviewList.size()) {
            return;
        }

        SharedPreferences prefs = getSharedPreferences("USER_PREFS", MODE_PRIVATE);
        long userId = prefs.getLong("USER_ID", -1L);

        if (userId == -1L) {
            Toast.makeText(this, "Please Login to Like/Dislike", Toast.LENGTH_SHORT).show();
            return;
        }

        Review review = reviewList.get(position);
        String url = BASE_URL + "/api/review-votes";

        JSONObject body = new JSONObject();

        try {
            JSONObject reviewObj = new JSONObject();
            reviewObj.put("review_id", review.review_id);

            JSONObject userObj = new JSONObject();
            userObj.put("user_id", userId);

            body.put("review", reviewObj);
            body.put("user", userObj);
            body.put("is_like", reactionType.equals("LIKE"));

        } catch (JSONException e) {
            Toast.makeText(this, "Error building request", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> fetchReviews(courseId),
                error -> {
                    if (error.networkResponse != null && error.networkResponse.statusCode == 204) {
                        fetchReviews(courseId);
                    } else {
                        Toast.makeText(this, "Vote failed", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    //Delete Review was properly implemeted in a previous push
    private void deleteReview(long reviewId) {
        Log.d("DELETE_REVIEW", "Deleting review id: " + reviewId);

        String url = BASE_URL + "/api/reviews/" + reviewId;
        Log.d("DELETE_REVIEW", "DELETE URL: " + url);

        StringRequest request = new StringRequest(
                Request.Method.DELETE,
                url,
                response -> {
                    Toast.makeText(this, "Review deleted", Toast.LENGTH_SHORT).show();

                    userAlreadyReviewed = false;
                    updateWriteReviewButton();
                    fetchReviews(courseId);
                },
                error -> {
                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;

                        if (statusCode == 204 || statusCode == 200) {
                            Toast.makeText(this, "Review deleted", Toast.LENGTH_SHORT).show();

                            userAlreadyReviewed = false;
                            updateWriteReviewButton();
                            fetchReviews(courseId);
                        } else if (statusCode == 404) {
                            Toast.makeText(this, "Review not found", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to delete review: " + statusCode, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to delete review", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    private void refreshReviews() {
        if (courseId != -1L) {
            fetchReviews(courseId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshReviews();
    }
}