package tables.notification;

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
@Table(name = "notifications")
@Schema(description = "Represents a notification sent to a user or admin")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    @JsonProperty("notification_id")
    @Schema(description = "Unique identifier", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long notificationId;

    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    @JsonIgnoreProperties({"notifications", "hibernateLazyInitializer", "handler"})
    @JsonProperty("recipient")
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @Schema(description = "The user receiving the notification")
    private User recipient;

    @ManyToOne
    @JoinColumn(name = "triggered_by_id", nullable = false)
    @JsonIgnoreProperties({"notifications", "hibernateLazyInitializer", "handler"})
    @JsonProperty("triggered_by")
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @Schema(description = "The user who triggered the notification")
    private User triggeredBy;

    @ManyToOne
    @JoinColumn(name = "review_id", nullable = false)
    @JsonIgnoreProperties({"notifications", "hibernateLazyInitializer", "handler"})
    @JsonProperty("review")
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @Schema(description = "The review this notification is related to")
    private Review review;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    @JsonProperty("type")
    @Schema(description = "Type of notification", example = "VOTE_RECEIVED", accessMode = Schema.AccessMode.READ_ONLY)
    private NotificationType type;

    @Column(name = "is_read", nullable = false)
    @JsonProperty("is_read")
    @Schema(description = "Whether the notification has been read", example = "false")
    private Boolean isRead = false;

    @JsonProperty("created_on")
    @Schema(description = "Timestamp of notification creation", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdOn;

    public Notification() {}

    @PrePersist
    protected void onCreate() {
        this.createdOn = LocalDateTime.now();
    }
}