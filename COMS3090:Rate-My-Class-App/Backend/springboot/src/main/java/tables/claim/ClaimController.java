package tables.claim;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping(path = "/claims")
@Tag(name = "Claim Controller", description = "Endpoints for user profile requests and admin moderation")
public class ClaimController {

    @Autowired
    private ClaimRepository claimRepository;

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    @Operation(summary = "List all claims", description = "Returns all submitted user requests")
    @GetMapping
    public List<Claim> getAllClaims() {
        return claimRepository.findAll();
    }

    @Operation(summary = "Get claim by ID")
    @GetMapping(path = "/{id}")
    public Claim getClaimById(@PathVariable Long id) {
        return claimRepository.findById(id).orElse(null);
    }

    @Operation(summary = "Get claims by user ID")
    @GetMapping(path = "/user/{userId}")
    public List<Claim> getClaimsByUserId(@PathVariable Long userId) {
        return claimRepository.findByUser_UserId(userId);
    }

    @Operation(summary = "Get claims by status")
    @GetMapping(path = "/status/{status}")
    public List<Claim> getClaimsByStatus(@PathVariable ClaimStatus status) {
        return claimRepository.findByStatus(status);
    }

    @Operation(summary = "Create a new claim")
    @PostMapping
    public String createClaim(@RequestBody Claim claim) {
        try {
            claimRepository.save(claim);
            return success;
        } catch (Exception e) {
            return failure;
        }
    }

    @Operation(summary = "Update claim status")
    @PutMapping(path = "/{id}")
    public String updateClaimStatus(@PathVariable Long id, @RequestBody Claim claimDetails) {
        try {
            Claim existingClaim = claimRepository.findById(id).orElse(null);
            if (existingClaim == null) {
                return failure;
            }
            existingClaim.setStatus(claimDetails.getStatus());
            claimRepository.save(existingClaim);
            return success;
        } catch (Exception e) {
            return failure;
        }
    }

    @Operation(summary = "Delete a claim")
    @DeleteMapping(path = "/{id}")
    public String deleteClaim(@PathVariable Long id) {
        try {
            claimRepository.deleteById(id);
            return success;
        } catch (Exception e) {
            return failure;
        }
    }
}