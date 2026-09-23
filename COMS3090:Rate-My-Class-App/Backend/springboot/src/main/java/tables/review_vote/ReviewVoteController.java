package tables.review_vote;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tables.notification.Notification;
import tables.notification.NotificationRepository;
import tables.notification.NotificationType;
import tables.review.ReviewRepository;
import tables.websocket.notification.NotificationSocket;
import java.util.Optional;

@RestController
@RequestMapping("/api/review-votes")
@Schema(name = "Review Votes", description = "Endpoints for liking and disliking reviews")
public class ReviewVoteController {

    private final ReviewVoteRepository reviewVoteRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationRepository notificationRepository;

    public ReviewVoteController(ReviewVoteRepository reviewVoteRepository,
                                ReviewRepository reviewRepository,
                                NotificationRepository notificationRepository) {
        this.reviewVoteRepository = reviewVoteRepository;
        this.reviewRepository = reviewRepository;
        this.notificationRepository = notificationRepository;
    }

    @PostMapping
    @Operation(summary = "Cast or switch a vote on a review")
    public ResponseEntity<ReviewVote> castVote(@RequestBody ReviewVote incomingVote) {
        Long reviewId = incomingVote.getReview().getReviewId();
        Long userId   = incomingVote.getUser().getUserId();
        Optional<ReviewVote> existing = reviewVoteRepository.findByReview_ReviewIdAndUser_UserId(reviewId, userId);

        if (existing.isPresent()) {
            ReviewVote current = existing.get();

            // Same vote — toggle off
            if (current.getIsLike().equals(incomingVote.getIsLike())) {
                updateCachedCount(reviewId, current.getIsLike(), -1);
                reviewVoteRepository.delete(current);
                return ResponseEntity.noContent().build();
            }

            // Different vote — switch it
            updateCachedCount(reviewId, current.getIsLike(), -1);
            current.setIsLike(incomingVote.getIsLike());
            updateCachedCount(reviewId, current.getIsLike(), +1);
            ReviewVote switched = reviewVoteRepository.save(current);

            String switchMessage = switched.getIsLike() ? "Someone liked your review!" : "Someone disliked your review!";
            saveNotification(switched, switchMessage);

            return ResponseEntity.ok(switched);
        }

        // No existing vote — create new
        updateCachedCount(reviewId, incomingVote.getIsLike(), +1);
        ReviewVote saved = reviewVoteRepository.save(incomingVote);

        String voteMessage = saved.getIsLike() ? "Someone liked your review!" : "Someone disliked your review!";
        saveNotification(saved, voteMessage);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    private void saveNotification(ReviewVote vote, String message) {
        try {
            reviewRepository.findById(vote.getReview().getReviewId()).ifPresent(fullReview -> {

                Notification notification = new Notification();
                notification.setRecipient(fullReview.getUser());
                notification.setTriggeredBy(vote.getUser());
                notification.setReview(fullReview);
                notification.setType(vote.getIsLike() ?
                        NotificationType.LIKE_RECEIVED :
                        NotificationType.DISLIKE_RECEIVED);
                notification.setIsRead(false);

                notificationRepository.save(notification);

                NotificationSocket.sendVoteNotification(fullReview.getUser().getUserId(), message);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/review/{reviewId}")
    @Operation(summary = "Get all votes for a review")
    public ResponseEntity<?> getVotesForReview(@PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewVoteRepository.findByReview_ReviewId(reviewId));
    }

    @GetMapping("/review/{reviewId}/user/{userId}")
    @Operation(summary = "Get a specific user's vote on a review")
    public ResponseEntity<ReviewVote> getUserVote(@PathVariable Long reviewId, @PathVariable Long userId) {
        return reviewVoteRepository.findByReview_ReviewIdAndUser_UserId(reviewId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private void updateCachedCount(Long reviewId, Boolean isLike, int delta) {
        reviewRepository.findById(reviewId).ifPresent(review -> {
            if (isLike) {
                review.setLikeCount(review.getLikeCount() + delta);
            } else {
                review.setDislikeCount(review.getDislikeCount() + delta);
            }
            reviewRepository.save(review);
        });
    }
}