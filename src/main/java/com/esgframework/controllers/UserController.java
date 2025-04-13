package com.esgframework.controllers;

import com.esgframework.models.User;
import com.esgframework.models.Notification;
import com.esgframework.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<List<User>> getUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok().body(users);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> addUser(@Valid @RequestBody User user) {
        try {
            System.out.println("Received user data: " + user);
            User createdUser = userService.createUser(user);
            System.out.println("Successfully created user: " + createdUser);
            return ResponseEntity.ok(createdUser);
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error creating user: " + e.getMessage());
        }
    }

    @GetMapping("/notifications")
    @PreAuthorize("hasAnyRole('manager', 'representative')")
    public ResponseEntity<List<Notification>> getNotifications() {
        try {
            List<Notification> notifications = userService.getUserNotifications();
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/notifications/{id}")
    @PreAuthorize("hasAnyRole('manager', 'representative')")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        try {
            userService.deleteNotification(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/notifications/{id}/read")
    @PreAuthorize("hasAnyRole('manager', 'representative')")
    public ResponseEntity<?> markNotificationRead(@PathVariable Long id) {
        try {
            userService.markNotificationAsRead(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/notifications/submission/{submissionId}")
    @PreAuthorize("hasAnyRole('manager', 'representative')")
    public ResponseEntity<?> deleteNotificationBySubmissionId(@PathVariable Long submissionId) {
        try {
            userService.deleteNotificationBySubmissionId(submissionId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }


}
