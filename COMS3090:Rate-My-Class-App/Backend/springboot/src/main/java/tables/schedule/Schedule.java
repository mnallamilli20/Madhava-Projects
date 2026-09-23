package tables.schedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import tables.user.User;
import tables.university.University;
import tables.course.Course;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    @JsonProperty("schedule_id")
    private Long scheduleId;

    @Column(nullable = false)
    @JsonProperty("name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @JsonProperty("user")
    private User user;

    @ManyToOne
    @JoinColumn(name = "university_id", nullable = false)
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @JsonProperty("university")
    private University university;

    @ManyToMany
    @JoinTable(
            name = "schedule_courses",
            joinColumns = @JoinColumn(
                    name = "schedule_id",
                    foreignKey = @ForeignKey(
                            name = "FK_schedule_courses_schedule",
                            foreignKeyDefinition = "FOREIGN KEY (schedule_id) REFERENCES schedules(schedule_id) ON DELETE CASCADE"
                    )
            ),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    @JsonProperty("courses")
    private Set<Course> courses = new HashSet<>();

    @Column(name = "created_on")
    @JsonProperty("created_on")
    private LocalDateTime createdOn;

    public Schedule() {}

    @PrePersist
    protected void onCreate() {
        this.createdOn = LocalDateTime.now();
    }
}