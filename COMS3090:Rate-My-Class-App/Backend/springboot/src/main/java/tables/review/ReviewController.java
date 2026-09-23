package tables.review;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Reviews", description = "Endpoints for managing course reviews")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;

    public ReviewController(ReviewRepository reviewRepository, ReviewService reviewService) {
        this.reviewRepository = reviewRepository;
        this.reviewService = reviewService;
    }

    @GetMapping
    @Operation(summary = "Get all reviews")
    public ResponseEntity<List<Review>> getAllReviews() {
        return ResponseEntity.ok(reviewRepository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a review by ID")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        Optional<Review> review = reviewRepository.findById(id);
        return review.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Get all reviews for a course")
    public ResponseEntity<List<Review>> getReviewsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(reviewRepository.findByCourse_CourseId(courseId));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all reviews by a user")
    public ResponseEntity<List<Review>> getReviewsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(reviewRepository.findByUser_UserId(userId));
    }

    @PostMapping
    @Operation(summary = "Create a new review")
    public ResponseEntity<Review> createReview(@RequestBody Review review) {
        Review savedReview = reviewService.saveReviewAndUpdateCourse(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedReview);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a review")
    public ResponseEntity<Review> updateReview(@PathVariable Long id, @RequestBody Review updated) {
        return reviewRepository.findById(id).map(existing -> {
            existing.setOverallRating(updated.getOverallRating());
            existing.setDifficultyRating(updated.getDifficultyRating());
            existing.setWorkloadRating(updated.getWorkloadRating());
            existing.setRecommendation(updated.getRecommendation());
            existing.setGradeReceived(updated.getGradeReceived());
            existing.setComment(updated.getComment());

            Review saved = reviewService.saveReviewAndUpdateCourse(existing);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a review")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        try {
            reviewService.deleteReviewAndUpdateCourse(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/reaction")
    @Operation(summary = "Handle likes and dislikes")
    public ResponseEntity<Review> handleReaction(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Object> payload) {

        String type = (String) payload.get("reaction_type");

        return reviewRepository.findById(id).map(review -> {
            if ("LIKE".equalsIgnoreCase(type)) {
                review.setLikeCount(review.getLikeCount() + 1);
            } else if ("DISLIKE".equalsIgnoreCase(type)) {
                review.setDislikeCount(review.getDislikeCount() + 1);
            }

            Review updatedReview = reviewRepository.save(review);

            return ResponseEntity.ok(updatedReview);
        }).orElse(ResponseEntity.notFound().build());
    }



}