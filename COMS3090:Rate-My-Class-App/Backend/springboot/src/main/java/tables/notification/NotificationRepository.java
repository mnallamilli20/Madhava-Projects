package tables.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipient_UserId(Long userId);
    List<Notification> findByRecipient_UserIdAndIsRead(Long userId, Boolean isRead);
}