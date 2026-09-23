package com.example.androidexample;

public class Course {
    public final long course_id;
    public final String name;
    public final String course_code;
    public final String subject;
    public final String university;
    public final String description;
    public final double avg_overall;
    public final double avg_difficulty;
    public final double avg_workload;
    public final int review_Count;

    //university info (parsed from JSON in /courses)
    public long universityId;
    public String universityName;

    public Course(long course_id, String name, String course_code, String subject, String university, String description,
                  double avg_overall, double avg_difficulty, double avg_workload, int review_Count) {
        this.course_id = course_id;
        this.name = name;
        this.course_code = course_code;
        this.subject = subject;
        this.university = university;
        this.description= description;
        this.avg_overall = avg_overall;
        this.avg_difficulty = avg_difficulty;
        this.avg_workload = avg_workload;
        this.review_Count = review_Count;
        this.universityId = -1L;
        this.universityName = "";

    }

    @Override
    public String toString() {
        return name;
    }
}
