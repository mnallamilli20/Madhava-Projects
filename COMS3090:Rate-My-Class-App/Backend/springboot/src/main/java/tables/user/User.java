package tables.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;
import tables.university.University;
import java.time.LocalDateTime;

/**
 * Entity representing a User account.
 */
@Entity
@Getter
@Setter
@Table(name = "users")
@Schema(description = "User account details and security roles")
public class User {

    /** Unique system identifier for the user. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    @JsonProperty("user_id")
    @Schema(description = "Unique User ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long userId;

    /** Unique handle used for login. */
    @Column(nullable = false, unique = true)
    @JsonProperty("username")
    @Schema(description = "Unique display name/login handle", example = "jdoe88", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    /** Unique email address. Validated for standard email format. */
    @Column(nullable = false, unique = true)
    @JsonProperty("email")
    @Email
    @Schema(description = "Primary contact email", example = "john.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    /** password string.
     * Marked WRITE_ONLY so it is never sent back in API responses.
     */
    @JsonProperty(value = "pass_hash", access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    @Schema(description = "not encrypted password", example = "plaintext password: password", requiredMode = Schema.RequiredMode.REQUIRED)
    private String passHash;

    /** The authorization level (e.g., USER, ADMIN, EDITOR). */
    @Enumerated(EnumType.STRING)
    @JsonProperty("role")
    @Column(nullable = false)
    @Schema(description = "User access level", example = "USER")
    private Role role;

    /** Timestamp of account creation. Cannot be updated. */
    @Column(nullable = false, updatable = false)
    @Schema(description = "Account creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdOn;

    /** The university the user is associated with. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "university_id")
    @JsonIgnoreProperties({"users", "hibernateLazyInitializer", "handler"})
    @JsonProperty("university")
    @Schema(description = "Associated university institution")
    private University university;

    public User() {}

    /** Initializes creation timestamp and sets default role to USER if null. */
    @PrePersist
    protected void onCreate() {
        this.createdOn = LocalDateTime.now();
        if (this.role == null) { this.role = Role.USER; }
    }
}