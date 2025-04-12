package com.esgframework.models;


import org.hibernate.annotations.CreationTimestamp;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "esg_submissions")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class ESGSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne
    @JoinColumn(name = "submitted_by")
    private User submittedBy;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private SubmissionStatus status = SubmissionStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_comments")
    private String reviewComments;

    @Column(name = "submission_type")
    private String submissionType;

    @Column(name = "environmental_score")
    private Double environmentalScore;

    @Column(name = "social_score")
    private Double socialScore;

    @Column(name = "governance_score")
    private Double governanceScore;

    @Type(type = "jsonb")
    @Column(name = "environmental_metrics", columnDefinition = "jsonb")
    private String environmentalMetrics;

    @Type(type = "jsonb")
    @Column(name = "social_metrics", columnDefinition = "jsonb")
    private String socialMetrics;

    @Type(type = "jsonb")
    @Column(name = "governance_metrics", columnDefinition = "jsonb")
    private String governanceMetrics;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public User getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(User submittedBy) {
        this.submittedBy = submittedBy;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
    }

    public User getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(User reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public Double getEnvironmentalScore() {
        return environmentalScore;
    }

    public void setEnvironmentalScore(Double environmentalScore) {
        this.environmentalScore = environmentalScore;
    }

    public Double getSocialScore() {
        return socialScore;
    }

    public void setSocialScore(Double socialScore) {
        this.socialScore = socialScore;
    }

    public Double getGovernanceScore() {
        return governanceScore;
    }

    public void setGovernanceScore(Double governanceScore) {
        this.governanceScore = governanceScore;
    }

    public String getReviewComments() {
        return reviewComments;
    }

    public void setReviewComments(String reviewComments) {
        this.reviewComments = reviewComments;
    }

    public String getSubmissionType() {
        return submissionType;
    }

    public void setSubmissionType(String submissionType) {
        this.submissionType = submissionType;
    }

    public String getEnvironmentalMetrics() {
        return environmentalMetrics;
    }

    public void setEnvironmentalMetrics(String environmentalMetrics) {
        this.environmentalMetrics = environmentalMetrics;
    }

    public String getSocialMetrics() {
        return socialMetrics;
    }

    public void setSocialMetrics(String socialMetrics) {
        this.socialMetrics = socialMetrics;
    }

    public String getGovernanceMetrics() {
        return governanceMetrics;
    }

    public void setGovernanceMetrics(String governanceMetrics) {
        this.governanceMetrics = governanceMetrics;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
