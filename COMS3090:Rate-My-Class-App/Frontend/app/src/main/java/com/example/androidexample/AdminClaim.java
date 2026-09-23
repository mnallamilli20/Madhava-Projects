package com.example.androidexample;

public class AdminClaim {
        public long claim_id;
        public long user_id;
        public String username;
        public String claim_type;
        public String description;
        public String status;
        public String created_on;

        public AdminClaim(long claim_id, long user_id, String username,
                          String claim_type, String description, String status, String created_on) {
            this.claim_id = claim_id;
            this.user_id = user_id;
            this.username = username;
            this.claim_type = claim_type;
            this.description = description;
            this.status = status;
            this.created_on = created_on;
        }
        @Override
        public String toString() {
            return "#" + claim_id + " | " + claim_type + " | " + status;
        }
}
