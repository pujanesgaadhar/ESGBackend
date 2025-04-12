package com.esgframework.services;

import com.esgframework.models.User;
import com.esgframework.models.Notification;
import com.esgframework.repositories.UserRepository;
import com.esgframework.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public List<Notification> getUserNotifications() {
        User currentUser = getCurrentUser();
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(currentUser);

        // For representatives, only show review notifications
        if (currentUser.getRole().equals("ROLE_representative")) {
            return notifications.stream()
                .filter(n -> n.getTitle().startsWith("Your submission was"))
                .collect(Collectors.toList());
        }

        // For managers, only show submission notifications
        if (currentUser.getRole().equals("ROLE_manager")) {
            return notifications.stream()
                .filter(n -> n.getTitle().equals("New ESG Submission requires review"))
                .collect(Collectors.toList());
        }

        // Admins can see all notifications
        return notifications;
    }

    public Notification markNotificationAsRead(Long id) {
        User currentUser = getCurrentUser();
        Notification notification = notificationRepository.findByIdAndUser(id, currentUser)
            .orElseThrow(() -> new EntityNotFoundException("Notification not found"));
        
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }


    public User createUser(User user) {
        try {
            System.out.println("Creating user with data: " + user);

            // Validate required fields
            if (user.getName() == null || user.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Name is required");
            }
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("Email is required");
            }
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                throw new IllegalArgumentException("Password is required");
            }
            if (user.getRole() == null || user.getRole().trim().isEmpty()) {
                throw new IllegalArgumentException("Role is required");
            }

            // Check if email already exists
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }

            // Normalize role to lowercase
            user.setRole(user.getRole().toLowerCase());

            // Encode password
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // Save user
            User savedUser = userRepository.save(user);
            System.out.println("Successfully created user: " + savedUser);
            return savedUser;

        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error creating user: " + e.getMessage(), e);
        }
    }

    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        
        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }


}
