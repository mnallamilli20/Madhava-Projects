package tables.course;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import tables.university.University;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "courses")
@Schema(description = "Represents a university course offering")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    @JsonProperty("course_id")
    @Schema(description = "Unique identifier", example = "101", accessMode = Schema.AccessMode.READ_ONLY)
    private Long courseId;

    @ManyToOne
    @JoinColumn(name = "university_id", nullable = false)
    @JsonIgnoreProperties({"courses", "hibernateLazyInitializer", "handler"})
    @JsonProperty("university")
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @Schema(description = "The university offering this course")
    private University university;

    @Column(nullable = false)
    @JsonProperty("name")
    @Schema(description = "Full title of the course", example = "Introduction to Computer Science", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Column(nullable = false, name = "course_code")
    @JsonProperty("course_code")
    @Schema(description = "Unique academic code", example = "CS101", requiredMode = Schema.RequiredMode.REQUIRED)
    private String courseCode;

    @JsonProperty("subject")
    @Schema(description = "Broad academic category", example = "Mathematics")
    private String subject;

    @Column(columnDefinition = "TEXT")
    @JsonProperty("description")
    @Schema(description = "Detailed course syllabus or summary")
    private String description;

    @JsonProperty("created_on")
    @Schema(description = "Timestamp of record creation", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime created_on;

    @Column(name = "avg_overall")
    @JsonProperty("avg_overall")
    @Schema(description = "Average overall rating", example = "4.5")
    private Double avgOverall = 0.0;//Initialized to zero, but will change

    @Column(name = "avg_difficulty")
    @JsonProperty("avg_difficulty")
    @Schema(description = "Average difficulty rating", example = "2.3")
    private Double avgDifficulty = 0.0;//Initialized to zero, but will change

    @Column(name = "avg_workload")
    @JsonProperty("avg_workload")
    @Schema(description = "Average workload rating", example = "2.3")
    private Double avgWorkload = 0.0;//Initialized to zero, but will change

    @Column(name = "review_count")
    @JsonProperty("review_count")
    @Schema(description = "Amount of reviews for the course(needed for average calculations", example = "10")
    private Integer reviewCount = 0;//Initialized to zero, but will change


    public Course() {}

    @PrePersist
    protected void onCreate() {
        this.created_on = LocalDateTime.now();
    }
}