package tables.university;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import tables.course.Course;
import tables.user.User;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing a University record.
 */
@Entity
@Getter
@Setter
@Table(name = "universities")
@Schema(description = "Information about a registered university")
public class University {

    /** Unique internal ID for the university. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "university_id")
    @JsonProperty("university_id")
    @Schema(description = "Unique ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long universityId;

    /** Legal name of the university. Must be unique. */
    @NotBlank
    @Column(nullable = false, unique = true)
    @JsonProperty("name")
    @Schema(description = "Unique name of the institution", example = "Iowa State University", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /** Physical address or city/state location. */
    @JsonProperty("location")
    @Schema(description = "Physical location", example = "Ames, IA")
    private String location;

    /** Official university web address. */
    @JsonProperty("website")
    @Schema(description = "Official URL", example = "https://www.iastate.edu")
    private String website;

    /** Detailed profile or history of the university. */
    @Column(columnDefinition = "TEXT")
    @JsonProperty("description")
    @Schema(description = "Long-form institutional description")
    private String description;

    /** Link to the university's branding logo. */
    @JsonProperty("logo_url")
    @Schema(description = "URL path to the university logo image", example = "http://storage.com/logo.png")
    private String logo_url;

    /** Auto-generated timestamp of when the record was added. */
    @JsonProperty("created_on")
    @Schema(description = "Timestamp of registration", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime created_on;

    /** The user account with permissions to edit this university's data. */
    @OneToOne
    @JoinColumn(name = "editor_id", referencedColumnName = "user_id")
    @JsonIgnoreProperties({"university", "hibernateLazyInitializer", "handler"})
    @JsonProperty("editor")
    @Schema(description = "The assigned editor for this university")
    private User editor;

    /** * List of all courses with this university.
     * cascade = CascadeType.REMOVE, deleting this university
     * will automatically delete all linked course records.
     */
    @OneToMany(mappedBy = "university", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    @Schema(
            description = "Collection of courses belonging to this institution. Managed via cascade delete.",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private List<Course> courses;

    public University() {
    }

    /** Sets the creation timestamp before the record is persisted. */
    @PrePersist
    protected void onCreate() {
        this.created_on = LocalDateTime.now();
    }
}