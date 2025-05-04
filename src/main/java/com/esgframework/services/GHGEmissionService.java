package com.esgframework.services;

import com.esgframework.models.GHGEmission;
import com.esgframework.models.EmissionScope;
import com.esgframework.models.SubmissionStatus;
import com.esgframework.repositories.GHGEmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

import com.esgframework.models.User;
import com.esgframework.models.Company;
import com.esgframework.models.Notification;
import com.esgframework.repositories.NotificationRepository;
import com.esgframework.repositories.UserRepository;

@Service
public class GHGEmissionService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;

    private User getCurrentUser() {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new javax.persistence.EntityNotFoundException("User not found"));
    }

    public List<GHGEmission> getPendingEmissionsForCompany() {
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().equalsIgnoreCase("manager")) {
            throw new SecurityException("Only managers can view GHG submissions");
        }
        Company company = currentUser.getCompany();
        return ghgEmissionRepository.findByCompanyAndStatus(company, com.esgframework.models.SubmissionStatus.PENDING);
    }
    
    public List<GHGEmission> getAllEmissionsForCompany() {
        User currentUser = getCurrentUser();
        // Allow both managers and representatives to view emissions for their company
        if (!currentUser.getRole().equalsIgnoreCase("manager") && !currentUser.getRole().equalsIgnoreCase("representative")) {
            throw new SecurityException("Only managers and representatives can view GHG submissions");
        }
        Company company = currentUser.getCompany();
        return ghgEmissionRepository.findByCompany(company);
    }
    
    public List<GHGEmission> getEmissionsHistory() {
        User currentUser = getCurrentUser();
        // Allow both managers and representatives to view emissions history for their company
        if (!currentUser.getRole().equalsIgnoreCase("manager") && !currentUser.getRole().equalsIgnoreCase("representative")) {
            throw new SecurityException("Only managers and representatives can view GHG submission history");
        }
        Company company = currentUser.getCompany();
        
        // Get all approved and denied emissions
        List<GHGEmission> approvedEmissions = ghgEmissionRepository.findByCompanyAndStatus(company, SubmissionStatus.APPROVED);
        List<GHGEmission> deniedEmissions = ghgEmissionRepository.findByCompanyAndStatus(company, SubmissionStatus.DENIED);
        
        // Combine the lists
        approvedEmissions.addAll(deniedEmissions);
        
        // Sort by created date (newest first)
        approvedEmissions.sort((e1, e2) -> e2.getCreatedAt().compareTo(e1.getCreatedAt()));
        
        return approvedEmissions;
    }
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GHGEmissionService.class);
    @Autowired
    private GHGEmissionRepository ghgEmissionRepository;

    public GHGEmission submitEmissionData(GHGEmission emission) {
        logger.info("Submitting GHG emission data for company ID: {}", emission.getCompany().getId());
        
        // Set the current user as the submitter and last modifier
        User currentUser = getCurrentUser();
        emission.setSubmittedBy(currentUser);
        emission.setLastModifiedBy(currentUser);
        
        // Set submission date to current time
        emission.setSubmissionDate(LocalDateTime.now());
        
        // Explicitly set status to PENDING to ensure it appears in review
        emission.setStatus(SubmissionStatus.PENDING);
        
        // Save the emission data
        GHGEmission saved = ghgEmissionRepository.save(emission);
        logger.info("GHG emission saved with ID: {} submitted by user: {}", saved.getId(), currentUser.getName());
        
        // Create notifications for managers from the same company
        createNotificationsForManagers(saved);
        
        return saved;
    }

    public List<GHGEmission> getCompanyEmissions(Long companyId) {
    logger.info("Fetching all GHG emissions for company ID: {}", companyId);
        List<GHGEmission> emissions = ghgEmissionRepository.findByCompanyId(companyId);
    logger.info("Found {} GHG emission records", emissions.size());
    return emissions;
    }

    public List<GHGEmission> getCompanyEmissionsByScope(Long companyId, EmissionScope scope) {
    logger.info("Fetching GHG emissions for company ID: {} and scope: {}", companyId, scope);
        List<GHGEmission> emissions = ghgEmissionRepository.findByCompanyIdAndScope(companyId, scope);
    logger.info("Found {} GHG emission records for scope {}", emissions.size(), scope);
    return emissions;
    }

    public List<GHGEmission> getCompanyEmissionsByDateRange(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
    logger.info("Fetching GHG emissions for company ID: {} between {} and {}", companyId, startDate, endDate);
        List<GHGEmission> emissions = ghgEmissionRepository.findByCompanyIdAndStartDateBetween(companyId, startDate, endDate);
    logger.info("Found {} GHG emission records in date range", emissions.size());
    return emissions;
    }
    
    public GHGEmission updateEmissionStatus(Long id, String status) {
        logger.info("Updating status for GHG emission ID: {} to {}", id, status);
        
        // Find the emission by ID
        GHGEmission emission = ghgEmissionRepository.findById(id)
            .orElseThrow(() -> new javax.persistence.EntityNotFoundException("GHG Emission not found with ID: " + id));
        
        // Get current user for audit trail
        User currentUser = getCurrentUser();
        
        // Validate that the user has permission to update this emission
        if (!currentUser.getRole().equalsIgnoreCase("manager")) {
            throw new SecurityException("Only managers can update emission status");
        }
        
        // Validate that the emission belongs to the manager's company
        if (!emission.getCompany().getId().equals(currentUser.getCompany().getId())) {
            throw new SecurityException("You can only update emissions for your own company");
        }
        
        try {
            // Update the status
            emission.setStatus(com.esgframework.models.SubmissionStatus.valueOf(status));
            
            // Update audit fields
            emission.setLastModifiedBy(currentUser);
            // UpdateTimestamp will automatically update the updatedAt field
            
            // Save the updated emission
            GHGEmission updated = ghgEmissionRepository.save(emission);
            logger.info("Successfully updated status for GHG emission ID: {} to {}", id, status);
            
            // Create notification for the representative who submitted this emission
            createNotificationForRepresentative(updated, status);
            
            return updated;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid status value: {}", status, e);
            throw new IllegalArgumentException("Invalid status value: " + status);
        }
    }
    
    // Helper method to create notifications for managers when a new GHG emission is submitted
    private void createNotificationsForManagers(GHGEmission emission) {
        logger.info("Creating notifications for managers for GHG emission ID: {}", emission.getId());
        
        // Get all managers from the same company
        List<User> managers = userRepository.findByCompanyAndRole(
            emission.getCompany(), "manager");
        
        if (managers.isEmpty()) {
            logger.warn("No managers found for company ID: {}", emission.getCompany().getId());
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        String scopeDisplay = emission.getScope().toString()
            .replace("SCOPE_", "Scope ")
            .replace("SOLVENT", "Solvent")
            .replace("SINK", "Sink");
        
        // Create a notification for each manager
        for (User manager : managers) {
            // Don't notify the submitter if they are also a manager
            if (manager.getId().equals(emission.getSubmittedBy().getId())) {
                continue;
            }
            
            Notification notification = new Notification();
            notification.setUser(manager);
            notification.setSubmissionId(emission.getId());
            notification.setTitle("New " + scopeDisplay + " Submission");
            notification.setMessage(
                emission.getSubmittedBy().getName() + " has submitted new " + 
                scopeDisplay + " emissions data for review."
            );
            notification.setCreatedAt(now);
            notification.setRead(false);
            
            notificationRepository.save(notification);
            logger.info("Created notification for manager ID: {} for GHG emission ID: {}", 
                manager.getId(), emission.getId());
        }
    }
    
    // Helper method to create notification for representative when their submission is approved/denied
    private void createNotificationForRepresentative(GHGEmission emission, String status) {
        logger.info("Creating notification for representative for GHG emission ID: {}", emission.getId());
        
        // Get the representative who submitted this emission
        User representative = emission.getSubmittedBy();
        if (representative == null) {
            logger.warn("No submitter found for GHG emission ID: {}", emission.getId());
            return;
        }
        
        // Only create notification if the submitter is a representative
        if (!representative.getRole().equalsIgnoreCase("representative")) {
            logger.info("Submitter is not a representative, skipping notification for user ID: {}", representative.getId());
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        String scopeDisplay = emission.getScope().toString()
            .replace("SCOPE_", "Scope ")
            .replace("SOLVENT", "Solvent")
            .replace("SINK", "Sink");
        
        // Format status for display (APPROVED -> Approved)
        String statusDisplay = status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
        
        Notification notification = new Notification();
        notification.setUser(representative);
        notification.setSubmissionId(emission.getId());
        notification.setTitle("Your " + scopeDisplay + " submission was " + statusDisplay);
        notification.setMessage(
            "Your " + scopeDisplay + " emissions data submission from " + 
            emission.getStartDate().toLocalDate() + " to " + emission.getEndDate().toLocalDate() + 
            " has been " + statusDisplay.toLowerCase() + " by a manager."
        );
        notification.setCreatedAt(now);
        notification.setRead(false);
        
        notificationRepository.save(notification);
        logger.info("Created notification for representative ID: {} for GHG emission ID: {}", 
            representative.getId(), emission.getId());
    }
}
