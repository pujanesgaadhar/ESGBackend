package com.esgframework.services;

import com.esgframework.models.ESGSubmission;
import com.esgframework.models.User;
import com.esgframework.models.Company;
import com.esgframework.dto.ReviewRequest;
import com.esgframework.models.SubmissionStatus;
import com.esgframework.models.Notification;
import com.esgframework.repositories.ESGSubmissionRepository;
import com.esgframework.repositories.UserRepository;
import com.esgframework.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ESGService {

    @Autowired
    private ESGSubmissionRepository esgSubmissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    public ESGSubmission submitESGData(ESGSubmission submission) {
        User currentUser = getCurrentUser();
        submission.setCompany(currentUser.getCompany());
        submission.setSubmittedBy(currentUser);
        submission.setStatus(SubmissionStatus.PENDING);

        // Create notification for managers from the same company
        Company submitterCompany = currentUser.getCompany();
        if (submitterCompany != null) {
            List<User> managers = userRepository.findByCompanyAndRole(submitterCompany, "manager");
            for (User manager : managers) {
                Notification notification = new Notification();
                notification.setUser(manager);
                LocalDateTime now = LocalDateTime.now();
                String formattedTime = now.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"));
                notification.setTitle("New ESG Submission requires review");
                notification.setMessage(String.format(
                    "New ESG submission requires your review\n" +
                    "Submitter: %s (%s)\n" +
                    "Company: %s\n" +
                    "Submitted on: %s",
                    currentUser.getName(),
                    currentUser.getEmail(),
                    submitterCompany.getName(),
                    formattedTime
                ));
                notification.setCreatedAt(now);
                notificationRepository.save(notification);
            }
        }

        return esgSubmissionRepository.save(submission);
    }

    public List<ESGSubmission> getESGSubmissions() {
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().equalsIgnoreCase("manager")) {
            throw new SecurityException("Only managers can view ESG submissions");
        }
        // Fetch only PENDING submissions for manager review
        List<ESGSubmission> pending = esgSubmissionRepository.findByCompanyAndStatus(currentUser.getCompany(), SubmissionStatus.PENDING);
        pending.sort(Comparator.comparing(ESGSubmission::getCreatedAt).reversed());
        return pending;
    }

    public ESGSubmission getSubmissionDetails(Long id) {
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().equalsIgnoreCase("manager")) {
            throw new SecurityException("Only managers can view ESG submission details");
        }

        ESGSubmission submission = esgSubmissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found with id: " + id));

        // Verify the manager belongs to the same company as the submission
        if (!submission.getCompany().getId().equals(currentUser.getCompany().getId())) {
            throw new SecurityException("You can only view submissions from your own company");
        }

        return submission;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }



    public ESGSubmission reviewSubmission(Long id, ReviewRequest review) {
        User currentUser = getCurrentUser();
        ESGSubmission submission = esgSubmissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found with id: " + id));

        // Verify reviewer's role and company access
        String role = currentUser.getRole();
        if (!role.equalsIgnoreCase("manager")) {
            throw new SecurityException("Only managers can review submissions");
        }
        
        // Verify they belong to the same company as the submission
        Company managerCompany = currentUser.getCompany();
        Company submissionCompany = submission.getCompany();
        
        if (managerCompany == null || submissionCompany == null || 
            !managerCompany.getId().equals(submissionCompany.getId())) {
            throw new SecurityException("You can only review submissions from your own company");
        }

        // Validate the status
        if (!SubmissionStatus.APPROVED.equals(review.getStatus()) && 
            !SubmissionStatus.DENIED.equals(review.getStatus())) {
            throw new IllegalArgumentException("Invalid status. Must be APPROVED or DENIED");
        }

        LocalDateTime now = LocalDateTime.now();
        String formattedTime = now.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a"));
        
        submission.setStatus(review.getStatus());
        submission.setReviewComments(review.getComments());
        submission.setReviewedBy(currentUser);
        submission.setReviewedAt(now);

        // Create notification for the submitter
        Notification notification = new Notification();
        notification.setUser(submission.getSubmittedBy());
        notification.setTitle("Your submission was " + review.getStatus().toString().toLowerCase());
        
        String message = String.format(
            "Your ESG submission has been %s\n" +
            "Reviewed by: %s (%s)\n" +
            "Review date: %s",
            review.getStatus().toString().toLowerCase(),
            currentUser.getName(),
            currentUser.getEmail(),
            formattedTime
        );
        
        if (review.getComments() != null && !review.getComments().isEmpty()) {
            message += "\nComments: " + review.getComments();
        }
        
        notification.setMessage(message);
        notification.setCreatedAt(now);

        notificationRepository.save(notification);
        return esgSubmissionRepository.save(submission);
    }

    public List<ESGSubmission> getSubmissionHistory() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return esgSubmissionRepository.findBySubmittedByOrderByCreatedAtDesc(currentUser);
    }

    public Map<String, Object> getChartData() {
        User currentUser = getCurrentUser();
        Company userCompany = currentUser.getCompany();
        
        // Only include APPROVED submissions for the user's company
        List<ESGSubmission> approvedSubmissions = esgSubmissionRepository.findByCompanyAndStatus(
            userCompany, 
            SubmissionStatus.APPROVED
        );
        approvedSubmissions.sort(Comparator.comparing(ESGSubmission::getCreatedAt));

        // Extract data for chart
        List<String> labels = new ArrayList<>();
        List<Double> environmentalScores = new ArrayList<>();
        List<Double> socialScores = new ArrayList<>();
        List<Double> governanceScores = new ArrayList<>();

        for (ESGSubmission submission : approvedSubmissions) {
            String dateLabel = submission.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd"));
            if (submission.getSubmittedBy() != null) {
                dateLabel += " (" + submission.getSubmittedBy().getName() + ")";
            }
            labels.add(dateLabel);
            environmentalScores.add(submission.getEnvironmentalScore());
            socialScores.add(submission.getSocialScore());
            governanceScores.add(submission.getGovernanceScore());
        }

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("labels", labels);
        chartData.put("environmentalScores", environmentalScores);
        chartData.put("socialScores", socialScores);
        chartData.put("governanceScores", governanceScores);

        return chartData;
    }
}
