package tables.university;

import tables.user.User;

public class UniversitySignupRequest {
    private University university;
    private User user;

    public University getUniversity() { return university; }
    public void setUniversity(University university) { this.university = university; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}