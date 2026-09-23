package com.example.androidexample;

/*
 * Tables: University -> Course -> Review
 * Shows single result in search filter screen
 */

public class FilterResult {
    public final long id; //course_id or university_id
    public final String type; //"COURSE" or "UNIVERSITY"
    public final String name;
    public final String subtitle;
    public final double rating;
    public final String ratingLabel; //rating measures, "Overall", "Difficulty"
    public final int reviewCount; //number of reviews

    public FilterResult(long id, String type, String name, String subtitle,
                        double rating, String ratingLabel, int reviewCount) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.subtitle = subtitle;
        this.rating = rating;
        this.ratingLabel = ratingLabel;
        this.reviewCount = reviewCount;
    }
}
