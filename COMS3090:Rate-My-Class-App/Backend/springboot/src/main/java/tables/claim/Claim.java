package tables.claim;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import tables.user.User;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "claims")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "claim_id")
    @JsonProperty("claim_id")
    private Long claimId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JsonProperty("type")
    private ClaimType type;

    @Column(columnDefinition = "TEXT", nullable = false)
    @JsonProperty("description")
    private String description;

    @Column(nullable = false)
    @JsonProperty("status")
    private ClaimStatus status = ClaimStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @JsonProperty("user")
    private User user;

    @Column(name = "created_on")
    @JsonProperty("created_on")
    private LocalDateTime createdOn;

    public Claim() {}

    @PrePersist
    protected void onCreate() {
        this.createdOn = LocalDateTime.now();
    }
}