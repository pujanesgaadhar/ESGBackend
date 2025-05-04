package com.esgframework.repositories;

import com.esgframework.models.Notification;
import com.esgframework.models.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    Optional<Notification> findByIdAndUser(Long id, User user);
    List<Notification> findByUserAndReadOrderByCreatedAtDesc(User user, boolean read);
    List<Notification> findBySubmissionId(Long submissionId);
    List<Notification> findByUserAndSubmissionId(User user, Long submissionId);
    
    /**
     * Find notifications by user ID
     * @param userId The user ID
     * @param sort The sort order
     * @return List of notifications
     */
    List<Notification> findByUserId(Long userId, Sort sort);
    
    /**
     * Find notifications by user ID and read status
     * @param userId The user ID
     * @param read The read status
     * @return List of notifications
     */
    List<Notification> findByUserIdAndRead(Long userId, boolean read);
    
    /**
     * Count notifications by user ID and read status
     * @param userId The user ID
     * @param read The read status
     * @return Count of notifications
     */
    long countByUserIdAndRead(Long userId, boolean read);
    
    /**
     * Find the latest notifications for a user
     * @param userId The user ID
     * @param limit The maximum number of notifications to return
     * @return List of notifications
     */
    @Query(value = "SELECT n FROM Notification n WHERE n.user.id = :userId ORDER BY n.createdAt DESC")
    List<Notification> findLatestByUserId(@Param("userId") Long userId);
    
    /**
     * Find the latest notifications for a user with limit
     * @param userId The user ID
     * @param limit The maximum number of notifications to return
     * @return List of notifications
     */
    @Query(value = "SELECT * FROM notifications WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
    List<Notification> findTopNByUserId(@Param("userId") Long userId, @Param("limit") int limit);
}
