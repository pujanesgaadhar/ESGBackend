package com.esgframework.models;

import javax.persistence.*;
import javax.persistence.Index;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "ghg_emissions", indexes = {
    @Index(name = "idx_ghg_company", columnList = "company_id"),
    @Index(name = "idx_ghg_status", columnList = "status"),
    @Index(name = "idx_ghg_scope", columnList = "scope"),
    @Index(name = "idx_ghg_dates", columnList = "start_date, end_date"),
    @Index(name = "idx_ghg_submitter", columnList = "submitted_by_id")
})
public class GHGEmission {
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubmissionStatus status = SubmissionStatus.PENDING;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne
    @JoinColumn(name = "submitted_by_id")
    private User submittedBy;

    @Enumerated(EnumType.STRING)
    private EmissionScope scope;

    @Enumerated(EnumType.STRING)
    private EmissionCategory category;

    @Enumerated(EnumType.STRING)
    private TimeFrame timeFrame;

    @Column(name = "start_date")
    private LocalDateTime startDate;
    
    @Column(name = "end_date")
    private LocalDateTime endDate;
    private Double quantity;
    private String unit;
    private String source;
    private String activity;
    private String calculationMethod;
    private Double emissionFactor;
    private String emissionFactorUnit;
    private LocalDateTime submissionDate;
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne
    @JoinColumn(name = "last_modified_by_id")
    private User lastModifiedBy;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    public EmissionScope getScope() { return scope; }
    public void setScope(EmissionScope scope) { this.scope = scope; }

    public EmissionCategory getCategory() { return category; }
    public void setCategory(EmissionCategory category) { this.category = category; }

    public TimeFrame getTimeFrame() { return timeFrame; }
    public void setTimeFrame(TimeFrame timeFrame) { this.timeFrame = timeFrame; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getActivity() { return activity; }
    public void setActivity(String activity) { this.activity = activity; }

    public String getCalculationMethod() { return calculationMethod; }
    public void setCalculationMethod(String calculationMethod) { this.calculationMethod = calculationMethod; }

    public Double getEmissionFactor() { return emissionFactor; }
    public void setEmissionFactor(Double emissionFactor) { this.emissionFactor = emissionFactor; }

    public String getEmissionFactorUnit() { return emissionFactorUnit; }
    public void setEmissionFactorUnit(String emissionFactorUnit) { this.emissionFactorUnit = emissionFactorUnit; }

    public LocalDateTime getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(LocalDateTime submissionDate) { this.submissionDate = submissionDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public SubmissionStatus getStatus() {
        return status;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
    }
    
    public User getSubmittedBy() {
        return submittedBy;
    }
    
    public void setSubmittedBy(User submittedBy) {
        this.submittedBy = submittedBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public User getLastModifiedBy() {
        return lastModifiedBy;
    }
    
    public void setLastModifiedBy(User lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }
}
