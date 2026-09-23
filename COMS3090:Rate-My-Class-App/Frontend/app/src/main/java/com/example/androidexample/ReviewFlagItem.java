package com.example.androidexample;

public class ReviewFlagItem {
    public long flag_id;
    public long review_id;
    public long flagged_by_id;
    public String flagged_by_username;
    public String reason;
    public String status;
    public String created_on;

    public ReviewFlagItem(long flag_id, long review_id, long flagged_by_id,
                          String flagged_by_username, String reason, String status,
                          String created_on) {
        this.flag_id = flag_id;
        this.review_id = review_id;
        this.flagged_by_id = flagged_by_id;
        this.flagged_by_username = flagged_by_username;
        this.reason = reason;
        this.status = status;
        this.created_on = created_on;
    }

    @Override
    public String toString() {
        return "Flag #" + flag_id + " | Review #" + review_id + " | " + reason + " | " + status;
    }
}
