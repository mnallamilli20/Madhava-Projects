package com.example.androidexample;

public class Schedule {
    public long schedule_id;
    public long user_id;
    public String name;
    public long university_id;
    public String university_name;
    public double avg_difficulty;

    public Schedule(long schedule_id, long user_id, String name,
                    long university_id, String university_name,
                    double avg_difficulty) {
        this.schedule_id = schedule_id;
        this.user_id = user_id;
        this.name = name;
        this.university_id = university_id;
        this.university_name = university_name;
        this.avg_difficulty = avg_difficulty;
    }

    @Override
    public String toString() {
        return name + " (" + String.format("%.2f", avg_difficulty) + "/5)";
    }
}