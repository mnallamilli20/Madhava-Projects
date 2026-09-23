package tables.claim;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByUser_UserId(Long userId);
    List<Claim> findByStatus(ClaimStatus status);
}
