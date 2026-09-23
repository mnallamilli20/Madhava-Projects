package tables.websocket.chat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import tables.course.Course;
import tables.user.User;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "messages")
@Schema(description = "Represents a chat message in a course chat")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    @JsonProperty("message_id")
    @Schema(description = "Unique identifier", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long messageId;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    @JsonIgnoreProperties({"messages", "hibernateLazyInitializer", "handler"})
    @JsonProperty("course")
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @Schema(description = "The course this message belongs to")
    private Course course;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"messages", "hibernateLazyInitializer", "handler"})
    @JsonProperty("user")
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @Schema(description = "The user who sent the message")
    private User user;

    @Column(columnDefinition = "TEXT", nullable = false)
    @JsonProperty("content")
    @Schema(description = "The message content")
    private String content;

    @Column(name = "sent_on")
    @JsonProperty("sent_on")
    @Schema(description = "Timestamp of when the message was sent", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime sentOn;

    public Message() {}

    public Message(User user, String content, Course course) {
        this.user = user;
        this.content = content;
        this.course = course;
    }

    @PrePersist
    protected void onCreate() {
        this.sentOn = LocalDateTime.now();
    }
}