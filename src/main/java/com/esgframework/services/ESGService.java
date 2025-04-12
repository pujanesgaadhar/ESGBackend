package com.esgframework.services;

import com.esgframework.models.ESGSubmission;
import com.esgframework.models.User;
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

        // Create notification for managers
        List<User> managers = userRepository.findByCompanyAndRole(currentUser.getCompany(), "ROLE_manager");
        for (User manager : managers) {
            Notification notification = new Notification();
            notification.setUser(manager);
            notification.setTitle("New ESG Submission requires review");
            notification.setMessage("A new ESG submission from " + currentUser.getName() + " requires your review.");
            notification.setCreatedAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }

        return esgSubmissionRepository.save(submission);
    }

    public List<ESGSubmission> getESGSubmissions() {
        User currentUser = getCurrentUser();
        if (currentUser.getRole().equals("ROLE_manager")) {
            return esgSubmissionRepository.findByCompany(currentUser.getCompany());
        }
        // Admin can see all submissions
        return esgSubmissionRepository.findAll();
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

        // Verify that the manager belongs to the same company as the submission
        if (currentUser.getRole().equals("ROLE_manager") && 
            !submission.getCompany().getId().equals(currentUser.getCompany().getId())) {
            throw new EntityNotFoundException("Submission not found with id: " + id);
        }

        // Validate the status
        if (!SubmissionStatus.APPROVED.equals(review.getStatus()) && 
            !SubmissionStatus.DENIED.equals(review.getStatus())) {
            throw new IllegalArgumentException("Invalid status. Must be APPROVED or DENIED");
        }

        submission.setStatus(review.getStatus());
        submission.setReviewComments(review.getComments());
        submission.setReviewedBy(currentUser);
        submission.setReviewedAt(LocalDateTime.now());

        // Create notification for the submitter
        Notification notification = new Notification();
        notification.setUser(submission.getSubmittedBy());
        notification.setTitle("Your submission was " + review.getStatus().toString().toLowerCase());
        String message = "Your ESG submission has been " + review.getStatus().toString().toLowerCase();
        if (review.getComments() != null && !review.getComments().isEmpty()) {
            message += ". Comments: " + review.getComments();
        }
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
        return esgSubmissionRepository.save(submission);
    }

    public List<ESGSubmission> getSubmissionHistory() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return esgSubmissionRepository.findBySubmittedByOrderByCreatedAtDesc(currentUser);
    }

    public Map<String, Object> getChartData() {
        List<ESGSubmission> approvedSubmissions = esgSubmissionRepository.findByStatus(SubmissionStatus.APPROVED);

        // Sort submissions by date
        approvedSubmissions.sort(Comparator.comparing(ESGSubmission::getCreatedAt));

        // Extract data for chart
        List<String> labels = new ArrayList<>();
        List<Double> environmentalScores = new ArrayList<>();
        List<Double> socialScores = new ArrayList<>();
        List<Double> governanceScores = new ArrayList<>();

        for (ESGSubmission submission : approvedSubmissions) {
            labels.add(submission.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd")));
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
