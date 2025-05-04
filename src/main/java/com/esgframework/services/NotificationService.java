package com.esgframework.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esgframework.models.GHGEmission;
import com.esgframework.models.GovernanceMetric;
import com.esgframework.models.Notification;
import com.esgframework.models.SocialMetric;
import com.esgframework.models.User;
import com.esgframework.repositories.NotificationRepository;
import com.esgframework.repositories.UserRepository;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get all notifications for a user
     * @param userId The ID of the user
     * @return List of notifications
     */
    public List<Notification> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserId(userId, 
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }
    
    /**
     * Get the count of unread notifications for a user
     * @param userId The ID of the user
     * @return Count of unread notifications
     */
    public long getUnreadNotificationCount(Long userId) {
        return notificationRepository.countByUserIdAndRead(userId, false);
    }
    
    /**
     * Mark a notification as read
     * @param notificationId The ID of the notification
     * @param userId The ID of the user (for security check)
     * @return true if successful, false otherwise
     */
    @Transactional
    public boolean markNotificationAsRead(Long notificationId, Long userId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            
            // Security check - make sure the notification belongs to the user
            if (!notification.getUser().getId().equals(userId)) {
                logger.warn("User {} attempted to mark notification {} as read, but it belongs to user {}", 
                        userId, notificationId, notification.getUser().getId());
                return false;
            }
            
            notification.setRead(true);
            notificationRepository.save(notification);
            return true;
        }
        
        return false;
    }
    
    /**
     * Mark all notifications for a user as read
     * @param userId The ID of the user
     * @return Number of notifications marked as read
     */
    @Transactional
    public int markAllNotificationsAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndRead(userId, false);
        
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
        
        return unreadNotifications.size();
    }
    
    /**
     * Delete a notification
     * @param notificationId The ID of the notification
     * @param userId The ID of the user (for security check)
     * @return true if successful, false otherwise
     */
    @Transactional
    public boolean deleteNotification(Long notificationId, Long userId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            
            // Security check - make sure the notification belongs to the user
            if (!notification.getUser().getId().equals(userId)) {
                logger.warn("User {} attempted to delete notification {}, but it belongs to user {}", 
                        userId, notificationId, notification.getUser().getId());
                return false;
            }
            
            notificationRepository.delete(notification);
            return true;
        }
        
        return false;
    }
    
    /**
     * Delete all notifications related to a specific submission
     * @param submissionId The ID of the submission
     * @return Number of notifications deleted
     */
    @Transactional
    public int deleteNotificationsBySubmissionId(Long submissionId) {
        List<Notification> notifications = notificationRepository.findBySubmissionId(submissionId);
        notificationRepository.deleteAll(notifications);
        return notifications.size();
    }
    
    // GHG Emission notifications
    public void createGHGEmissionSubmissionNotification(GHGEmission emission) {
        // Find all managers in the company
        List<User> managers = userRepository.findByCompanyAndRole(emission.getCompany(), "manager");
        
        for (User manager : managers) {
            Notification notification = new Notification();
            notification.setUser(manager);
            notification.setTitle("New GHG Emission Submission");
            notification.setMessage("A new GHG emission has been submitted for " + emission.getScope() + 
                    " with quantity " + emission.getQuantity() + " " + emission.getUnit());
            notification.setType("GHG_SUBMISSION");
            notification.setRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setSubmissionId(emission.getId());
            
            notificationRepository.save(notification);
        }
    }
    
    public void createGHGEmissionStatusUpdateNotification(GHGEmission emission) {
        User submitter = emission.getSubmittedBy();
        if (submitter == null) {
            logger.warn("Cannot create status update notification: submitter is null for emission ID {}", emission.getId());
            return;
        }
        
        Notification notification = new Notification();
        notification.setUser(submitter);
        notification.setTitle("GHG Emission Status Update");
        notification.setMessage("Your GHG emission submission for " + emission.getScope() + 
                " has been " + emission.getStatus().toString().toLowerCase());
        notification.setType("GHG_STATUS_UPDATE");
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setSubmissionId(emission.getId());
        
        notificationRepository.save(notification);
    }
    
    // Social Metric notifications
    public void createSocialMetricSubmissionNotification(SocialMetric socialMetric) {
        // Find all managers in the company
        List<User> managers = userRepository.findByCompanyAndRole(socialMetric.getCompany(), "manager");
        
        for (User manager : managers) {
            Notification notification = new Notification();
            notification.setUser(manager);
            notification.setTitle("New Social Metric Submission");
            notification.setMessage("A new social metric has been submitted for " + 
                    socialMetric.getSubtype().toString().toLowerCase() + ": " + 
                    socialMetric.getMetric() + " with value " + socialMetric.getValue() + 
                    " " + socialMetric.getUnit());
            notification.setType("SOCIAL_SUBMISSION");
            notification.setRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setSubmissionId(socialMetric.getId());
            
            notificationRepository.save(notification);
        }
    }
    
    public void createSocialMetricStatusUpdateNotification(SocialMetric socialMetric) {
        User submitter = socialMetric.getSubmittedBy();
        
        Notification notification = new Notification();
        notification.setUser(submitter);
        notification.setTitle("Social Metric Status Update");
        notification.setMessage("Your social metric submission for " + 
                socialMetric.getMetric() + " has been " + 
                socialMetric.getStatus().toString().toLowerCase());
        notification.setType("SOCIAL_STATUS_UPDATE");
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setSubmissionId(socialMetric.getId());
        
        notificationRepository.save(notification);
    }
    
    // Governance Metric notifications
    public void createGovernanceMetricSubmissionNotification(GovernanceMetric governanceMetric) {
        // Find all managers in the company
        List<User> managers = userRepository.findByCompanyAndRole(governanceMetric.getCompany(), "manager");
        
        for (User manager : managers) {
            Notification notification = new Notification();
            notification.setUser(manager);
            notification.setTitle("New Governance Metric Submission");
            notification.setMessage("A new governance metric has been submitted for " + 
                    governanceMetric.getSubtype().toString().toLowerCase() + ": " + 
                    governanceMetric.getMetric() + " with value " + governanceMetric.getValue() + 
                    " " + governanceMetric.getUnit());
            notification.setType("GOVERNANCE_SUBMISSION");
            notification.setRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setSubmissionId(governanceMetric.getId());
            
            notificationRepository.save(notification);
        }
    }
    
    public void createGovernanceMetricStatusUpdateNotification(GovernanceMetric governanceMetric) {
        User submitter = governanceMetric.getSubmittedBy();
        
        Notification notification = new Notification();
        notification.setUser(submitter);
        notification.setTitle("Governance Metric Status Update");
        notification.setMessage("Your governance metric submission for " + 
                governanceMetric.getMetric() + " has been " + 
                governanceMetric.getStatus().toString().toLowerCase());
        notification.setType("GOVERNANCE_STATUS_UPDATE");
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setSubmissionId(governanceMetric.getId());
        
        notificationRepository.save(notification);
    }
}
