package tables.review_flag;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tables.websocket.notification.NotificationSocket;
import java.util.List;

@RestController
@RequestMapping("/api/review-flags")
@Tag(name = "Review Flags", description = "Endpoints for flagging and moderating reviews")
public class ReviewFlagController {

    private final ReviewFlagRepository reviewFlagRepository;

    public ReviewFlagController(ReviewFlagRepository reviewFlagRepository) {
        this.reviewFlagRepository = reviewFlagRepository;
    }

    @GetMapping
    @Operation(summary = "Get all flags")
    public ResponseEntity<List<ReviewFlag>> getAllFlags() {
        return ResponseEntity.ok(reviewFlagRepository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a flag by ID")
    public ResponseEntity<ReviewFlag> getFlagById(@PathVariable Long id) {
        return reviewFlagRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/review/{reviewId}")
    @Operation(summary = "Get all flags for a review")
    public ResponseEntity<List<ReviewFlag>> getFlagsByReview(@PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewFlagRepository.findByReview_ReviewId(reviewId));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get all flags by status — useful for admin moderation queue")
    public ResponseEntity<List<ReviewFlag>> getFlagsByStatus(@PathVariable FlagStatus status) {
        return ResponseEntity.ok(reviewFlagRepository.findByStatus(status));
    }

    @PostMapping
    @Operation(summary = "Submit a flag on a review")
    public ResponseEntity<?> submitFlag(@RequestBody ReviewFlag incomingFlag) {
        Long reviewId = incomingFlag.getReview().getReviewId();
        Long userId   = incomingFlag.getFlaggedBy().getUserId();

        boolean alreadyFlagged = reviewFlagRepository
                .findByReview_ReviewIdAndFlaggedBy_UserId(reviewId, userId)
                .isPresent();

        if (alreadyFlagged) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("You have already flagged this review.");
        }

        ReviewFlag saved = reviewFlagRepository.save(incomingFlag);

        NotificationSocket.sendFlagNotification(
                "A review has been flagged for: " + saved.getReason()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update the status of a flag — admin use")
    public ResponseEntity<ReviewFlag> updateFlagStatus(@PathVariable Long id, @RequestParam FlagStatus status) {
        return reviewFlagRepository.findById(id).map(flag -> {
            flag.setStatus(status);
            return ResponseEntity.ok(reviewFlagRepository.save(flag));
        }).orElse(ResponseEntity.notFound().build());
    }
}