package tables.claim;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Categories for claim statuses")
public enum ClaimStatus {
    PENDING,
    RESOLVED
}
