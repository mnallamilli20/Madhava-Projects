package tables.review_flag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewFlagRepository extends JpaRepository<ReviewFlag, Long> {
    List<ReviewFlag> findByReview_ReviewId(Long reviewId);
    List<ReviewFlag> findByStatus(FlagStatus status);
    Optional<ReviewFlag> findByReview_ReviewIdAndFlaggedBy_UserId(Long reviewId, Long userId);
}