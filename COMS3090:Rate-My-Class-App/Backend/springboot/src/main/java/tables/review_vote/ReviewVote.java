package tables.review_vote;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import tables.review.Review;
import tables.user.User;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "review_votes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"review_id", "user_id"})
})
@Schema(description = "Represents a like or dislike vote on a review")
public class ReviewVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vote_id")
    @JsonProperty("vote_id")
    @Schema(description = "Unique identifier", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long voteId;

    @ManyToOne
    @JoinColumn(name = "review_id", nullable = false)
    @JsonIgnoreProperties({"votes", "hibernateLazyInitializer", "handler"})
    @JsonProperty("review")
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @Schema(description = "The review being voted on")
    private Review review;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    @JsonIgnoreProperties({"votes", "hibernateLazyInitializer", "handler"})
    @JsonProperty("user")
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @Schema(description = "The user casting the vote")
    private User user;

    @Column(name = "is_like", nullable = false)
    @JsonProperty("is_like")
    @Schema(description = "True for like, false for dislike", example = "true")
    private Boolean isLike;

    @JsonProperty("created_on")
    @Schema(description = "Timestamp of vote submission", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdOn;

    public ReviewVote() {}

    @PrePersist
    protected void onCreate() {
        this.createdOn = LocalDateTime.now();
    }

}