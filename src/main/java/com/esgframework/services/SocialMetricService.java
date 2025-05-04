package com.esgframework.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.esgframework.models.Company;
import com.esgframework.models.SocialMetric;
import com.esgframework.models.SubmissionStatus;
import com.esgframework.models.User;
import com.esgframework.repositories.SocialMetricRepository;
import com.esgframework.repositories.UserRepository;

@Service
public class SocialMetricService {

    @Autowired
    private SocialMetricRepository socialMetricRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // CompanyRepository not needed
    
    @Autowired
    private NotificationService notificationService;
    
    public SocialMetric submitSocialMetric(SocialMetric socialMetric) {
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUsername)
            .orElseThrow(() -> new RuntimeException("User not found: " + currentUsername));
        
        // Set the company from the authenticated user
        Company company = currentUser.getCompany();
        socialMetric.setCompany(company);
        
        // Set the submitter
        socialMetric.setSubmittedBy(currentUser);
        
        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        socialMetric.setCreatedAt(now);
        socialMetric.setUpdatedAt(now);
        
        // Set initial status
        socialMetric.setStatus(SubmissionStatus.PENDING);
        
        // Ensure category is set if it's null but subtype is provided
        if (socialMetric.getCategory() == null && socialMetric.getSubtype() != null) {
            socialMetric.setCategory(socialMetric.getSubtype().toString());
            System.out.println("Setting category from subtype: " + socialMetric.getSubtype().toString());
        }
        
        // Ensure subtype is set if it's null but category is provided
        if (socialMetric.getSubtype() == null && socialMetric.getCategory() != null) {
            try {
                SocialMetric.SocialSubtype subtype = SocialMetric.SocialSubtype.valueOf(socialMetric.getCategory());
                socialMetric.setSubtype(subtype);
                System.out.println("Setting subtype from category: " + socialMetric.getCategory());
            } catch (IllegalArgumentException e) {
                System.out.println("Could not convert category to subtype: " + socialMetric.getCategory());
            }
        }
        
        System.out.println("Saving social metric with category: " + socialMetric.getCategory() + 
                          " and subtype: " + socialMetric.getSubtype());
        
        // Save the social metric
        SocialMetric savedMetric = socialMetricRepository.save(socialMetric);
        
        // Create notification for managers
        notificationService.createSocialMetricSubmissionNotification(savedMetric);
        
        return savedMetric;
    }
    
    public List<SocialMetric> getSocialMetricsByCompany() {
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUsername)
            .orElseThrow(() -> new RuntimeException("User not found: " + currentUsername));
        
        // Get the company from the authenticated user
        Company company = currentUser.getCompany();
        
        return socialMetricRepository.findByCompanyOrderByCreatedAtDesc(company);
    }
    
    public List<SocialMetric> getSocialMetricsHistory() {
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUsername)
            .orElseThrow(() -> new RuntimeException("User not found: " + currentUsername));
        
        // Get the company from the authenticated user
        Company company = currentUser.getCompany();
        
        // Get all approved and denied social metrics
        List<SocialMetric> approvedMetrics = socialMetricRepository.findByCompanyAndStatus(company, SubmissionStatus.APPROVED);
        List<SocialMetric> deniedMetrics = socialMetricRepository.findByCompanyAndStatus(company, SubmissionStatus.DENIED);
        
        // Combine the lists
        approvedMetrics.addAll(deniedMetrics);
        
        // Sort by created date (newest first)
        approvedMetrics.sort((m1, m2) -> m2.getCreatedAt().compareTo(m1.getCreatedAt()));
        
        return approvedMetrics;
    }
    
    public SocialMetric updateSocialMetricStatus(Long id, SubmissionStatus status) {
        SocialMetric socialMetric = socialMetricRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Social metric not found with id: " + id));
        
        socialMetric.setStatus(status);
        socialMetric.setUpdatedAt(LocalDateTime.now());
        
        SocialMetric updatedMetric = socialMetricRepository.save(socialMetric);
        
        // Create notification for the submitter
        notificationService.createSocialMetricStatusUpdateNotification(updatedMetric);
        
        return updatedMetric;
    }
    
    public SocialMetric getSocialMetricById(Long id) {
        return socialMetricRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Social metric not found with id: " + id));
    }
}
