package com.esgframework.repositories;

import com.esgframework.models.Notification;
import com.esgframework.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    Optional<Notification> findByIdAndUser(Long id, User user);
    List<Notification> findByUserAndReadOrderByCreatedAtDesc(User user, boolean read);
}
