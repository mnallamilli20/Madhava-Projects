package tables.review;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import tables.course.Course;
import tables.user.User;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "reviews")
@Schema(description = "Represents a student review for a university course")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    @Schema(description = "Unique identifier", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long reviewId;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    @JsonIgnoreProperties({"reviews", "hibernateLazyInitializer", "handler"})
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @JsonProperty("course")
    @Schema(description = "The course being reviewed")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "user_id",referencedColumnName = "user_id", nullable = false)
    @JsonIgnoreProperties({"reviews", "hibernateLazyInitializer", "handler"})
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @JsonProperty("user")
    @Schema(description = "The user submitting the review")
    private User user;

    @Min(1)
    @Max(5)
    @Column(nullable = false, name = "overall_rating")
    @JsonProperty("overall_rating")
    @Schema(description = "Overall course rating from 1 to 5", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer overallRating;

    @Min(1)
    @Max(5)
    @Column(nullable = false, name = "difficulty_rating")
    @JsonProperty("difficulty_rating")
    @Schema(description = "Difficulty rating from 1 to 5", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer difficultyRating;

    @Min(1)
    @Max(5)
    @Column(nullable = false, name = "workload_rating")
    @JsonProperty("workload_rating")
    @Schema(description = "Workload rating from 1 to 5", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer workloadRating;

    @Column(nullable = false)
    @JsonProperty("recommendation")
    @Schema(description = "Whether the student recommends this course", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean recommendation;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade_received")
    @JsonProperty("grade_received")
    @Schema(description = "Grade received in the course", example = "A_MINUS")
    private Grade gradeReceived;

    @Column(columnDefinition = "TEXT")
    @JsonProperty("comment")
    @Schema(description = "Written review or feedback about the course")
    private String comment;

    @Column(name = "like_count", nullable = false)
    @JsonProperty("like_count")
    @Schema(description = "Cached count of likes", example = "12", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer likeCount = 0;

    @Column(name = "dislike_count", nullable = false)
    @JsonProperty("dislike_count")
    @Schema(description = "Cached count of dislikes", example = "2", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer dislikeCount = 0;

    @JsonProperty("created_on")
    @Schema(description = "Timestamp of review submission", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdOn;

    public Review() {}

    @PrePersist
    protected void onCreate() {
        this.createdOn = LocalDateTime.now();
    }

    @JsonProperty("review_id")
    public Long getReviewId() {
        return reviewId;
    }

    @JsonProperty("review_id")
    public void setReviewId(Long reviewId) {
        this.reviewId = reviewId;
    }
}