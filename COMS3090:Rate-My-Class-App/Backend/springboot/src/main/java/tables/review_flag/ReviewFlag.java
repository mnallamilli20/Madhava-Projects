package tables.review_flag;

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
@Table(name = "review_flags", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"review_id", "user_id"})
})
@Schema(description = "Represents a flag submitted against a review")
public class ReviewFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flag_id")
    @JsonProperty("flag_id")
    @Schema(description = "Unique identifier", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long flagId;

    @ManyToOne
    @JoinColumn(name = "review_id", nullable = false)
    @JsonIgnoreProperties({"flags", "hibernateLazyInitializer", "handler"})
    @JsonProperty("review")
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @Schema(description = "The review being flagged")
    private Review review;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    @JsonIgnoreProperties({"flags", "hibernateLazyInitializer", "handler"})
    @JsonProperty("flagged_by")
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @Schema(description = "The user submitting the flag")
    private User flaggedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    @JsonProperty("reason")
    @Schema(description = "Reason for the flag", example = "INAPPROPRIATE", requiredMode = Schema.RequiredMode.REQUIRED)
    private FlagReason reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @JsonProperty("status")
    @Schema(description = "Current status of the flag", example = "PENDING", accessMode = Schema.AccessMode.READ_ONLY)
    private FlagStatus status = FlagStatus.PENDING;

    @JsonProperty("created_on")
    @Schema(description = "Timestamp of flag submission", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdOn;

    public ReviewFlag() {}

    @PrePersist
    protected void onCreate() {
        this.createdOn = LocalDateTime.now();
    }
}