package com.esgframework.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.esgframework.models.Company;
import com.esgframework.models.GovernanceMetric;
import com.esgframework.models.SubmissionStatus;
import com.esgframework.models.User;
import com.esgframework.repositories.GovernanceMetricRepository;
import com.esgframework.repositories.UserRepository;

@Service
public class GovernanceMetricService {

    @Autowired
    private GovernanceMetricRepository governanceMetricRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // CompanyRepository not needed
    
    @Autowired
    private NotificationService notificationService;
    
    public GovernanceMetric submitGovernanceMetric(GovernanceMetric governanceMetric) {
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUsername)
            .orElseThrow(() -> new RuntimeException("User not found: " + currentUsername));
        
        // Set the company from the authenticated user
        Company company = currentUser.getCompany();
        governanceMetric.setCompany(company);
        
        // Set the submitter
        governanceMetric.setSubmittedBy(currentUser);
        
        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        governanceMetric.setCreatedAt(now);
        governanceMetric.setUpdatedAt(now);
        
        // Set initial status
        governanceMetric.setStatus(SubmissionStatus.PENDING);
        
        // Ensure category is set if it's null but subtype is provided
        if (governanceMetric.getCategory() == null && governanceMetric.getSubtype() != null) {
            governanceMetric.setCategory(governanceMetric.getSubtype().toString());
            System.out.println("Setting category from subtype: " + governanceMetric.getSubtype().toString());
        }
        
        // Ensure subtype is set if it's null but category is provided
        if (governanceMetric.getSubtype() == null && governanceMetric.getCategory() != null) {
            try {
                GovernanceMetric.GovernanceSubtype subtype = GovernanceMetric.GovernanceSubtype.valueOf(governanceMetric.getCategory());
                governanceMetric.setSubtype(subtype);
                System.out.println("Setting subtype from category: " + governanceMetric.getCategory());
            } catch (IllegalArgumentException e) {
                System.out.println("Could not convert category to subtype: " + governanceMetric.getCategory());
            }
        }
        
        System.out.println("Saving governance metric with category: " + governanceMetric.getCategory() + 
                          " and subtype: " + governanceMetric.getSubtype());
        
        // Save the governance metric
        GovernanceMetric savedMetric = governanceMetricRepository.save(governanceMetric);
        
        // Create notification for managers
        notificationService.createGovernanceMetricSubmissionNotification(savedMetric);
        
        return savedMetric;
    }
    
    public List<GovernanceMetric> getGovernanceMetricsByCompany() {
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUsername)
            .orElseThrow(() -> new RuntimeException("User not found: " + currentUsername));
        
        // Get the company from the authenticated user
        Company company = currentUser.getCompany();
        
        return governanceMetricRepository.findByCompany(company);
    }
    
    public List<GovernanceMetric> getGovernanceMetricsHistory() {
        // Get the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByEmail(currentUsername)
            .orElseThrow(() -> new RuntimeException("User not found: " + currentUsername));
        
        // Get the company from the authenticated user
        Company company = currentUser.getCompany();
        
        // Get all approved and denied governance metrics
        List<GovernanceMetric> approvedMetrics = governanceMetricRepository.findByCompanyAndStatus(company, SubmissionStatus.APPROVED);
        List<GovernanceMetric> deniedMetrics = governanceMetricRepository.findByCompanyAndStatus(company, SubmissionStatus.DENIED);
        
        // Combine the lists
        approvedMetrics.addAll(deniedMetrics);
        
        // Sort by created date (newest first)
        approvedMetrics.sort((m1, m2) -> m2.getCreatedAt().compareTo(m1.getCreatedAt()));
        
        return approvedMetrics;
    }
    
    public GovernanceMetric updateGovernanceMetricStatus(Long id, SubmissionStatus status) {
        GovernanceMetric governanceMetric = governanceMetricRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Governance metric not found with id: " + id));
        
        governanceMetric.setStatus(status);
        governanceMetric.setUpdatedAt(LocalDateTime.now());
        
        GovernanceMetric updatedMetric = governanceMetricRepository.save(governanceMetric);
        
        // Create notification for the submitter
        notificationService.createGovernanceMetricStatusUpdateNotification(updatedMetric);
        
        return updatedMetric;
    }
    
    public GovernanceMetric getGovernanceMetricById(Long id) {
        return governanceMetricRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Governance metric not found with id: " + id));
    }
}
