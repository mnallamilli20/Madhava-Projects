package tables.review_vote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewVoteRepository extends JpaRepository<ReviewVote, Long> {
    List<ReviewVote> findByReview_ReviewId(Long reviewId);
    Optional<ReviewVote> findByReview_ReviewIdAndUser_UserId(Long reviewId, Long userId);
}