package com.esgframework.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esgframework.models.Notification;
import com.esgframework.models.User;
import com.esgframework.services.NotificationService;
import com.esgframework.services.UserService;

@RestController
@RequestMapping("/api/users/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Get all notifications for the authenticated user
     * @return List of notifications
     */
    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        List<Notification> notifications = notificationService.getNotificationsByUserId(currentUser.getId());
        return ResponseEntity.ok(notifications);
    }
    
    /**
     * Get the count of unread notifications for the authenticated user
     * @return Count of unread notifications
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadNotificationCount() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        long count = notificationService.getUnreadNotificationCount(currentUser.getId());
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Mark a notification as read
     * @param id The ID of the notification
     * @return Success or error response
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markNotificationAsRead(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        boolean success = notificationService.markNotificationAsRead(id, currentUser.getId());
        Map<String, Object> response = new HashMap<>();
        
        if (success) {
            response.put("success", true);
            response.put("message", "Notification marked as read");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Failed to mark notification as read");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    /**
     * Mark all notifications for the authenticated user as read
     * @return Success response with count of notifications marked as read
     */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllNotificationsAsRead() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        int count = notificationService.markAllNotificationsAsRead(currentUser.getId());
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", count);
        response.put("message", count + " notification(s) marked as read");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete a notification
     * @param id The ID of the notification
     * @return Success or error response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        boolean success = notificationService.deleteNotification(id, currentUser.getId());
        Map<String, Object> response = new HashMap<>();
        
        if (success) {
            response.put("success", true);
            response.put("message", "Notification deleted successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Failed to delete notification");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    /**
     * Delete all notifications related to a specific submission
     * @param submissionId The ID of the submission
     * @return Success response with count of notifications deleted
     */
    @DeleteMapping("/submission/{submissionId}")
    public ResponseEntity<Map<String, Object>> deleteNotificationsBySubmissionId(@PathVariable Long submissionId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        int count = notificationService.deleteNotificationsBySubmissionId(submissionId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", count);
        response.put("message", count + " notification(s) deleted");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Helper method to get the current authenticated user
     * @return The current user or null if not authenticated
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        String email = authentication.getName();
        return userService.findByEmail(email);
    }
}
