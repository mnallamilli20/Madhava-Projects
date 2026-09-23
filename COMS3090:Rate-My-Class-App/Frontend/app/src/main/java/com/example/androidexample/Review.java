package com.example.androidexample;

public class Review {
    public final long review_id;
    public final long user_id;
    public final String username;
    public final int overall_rating;
    public final int difficulty_rating;
    public final int workload_rating;
    public final boolean recommendation;
    public final String grade_received;
    public final String comment;
    public int like_count;
    public int dislike_count;
    public final String created_on;

    public Review(long review_id, long user_id, String username, int overall_rating, int difficulty_rating, int workload_rating,
                  boolean recommendation, String grade_received, String comment, int like_count, int dislike_count,
                  String created_on) {
        this.review_id = review_id;
        this.user_id = user_id;
        this.username = username;
        this.overall_rating = overall_rating;
        this.difficulty_rating = difficulty_rating;
        this.workload_rating = workload_rating;
        this.recommendation = recommendation;
        this.grade_received = grade_received;
        this.comment = comment;
        this.like_count = like_count;
        this.dislike_count = dislike_count;
        this.created_on = created_on;
    }
}