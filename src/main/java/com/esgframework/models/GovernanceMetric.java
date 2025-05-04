package com.esgframework.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "governance_metrics", indexes = {
    @Index(name = "idx_gov_company", columnList = "company_id"),
    @Index(name = "idx_gov_status", columnList = "status"),
    @Index(name = "idx_gov_subtype", columnList = "subtype"),
    @Index(name = "idx_gov_category", columnList = "category"),
    @Index(name = "idx_gov_dates", columnList = "start_date, end_date"),
    @Index(name = "idx_gov_submitter", columnList = "submitted_by")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceMetric {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    private GovernanceSubtype subtype;
    
    @Column(nullable = false)
    private String category;
    
    @Column(nullable = false)
    private String metric;
    
    @Column(nullable = false)
    private Double value;
    
    @Column(nullable = false)
    private String unit;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    private String description;
    
    private Boolean policyExists;
    
    private String policyUrl;
    
    private String reviewFrequency;
    
    private String responsibleParty;
    
    private String documentationUrl;
    
    @Enumerated(EnumType.STRING)
    private SubmissionStatus status;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
    
    @ManyToOne
    @JoinColumn(name = "submitted_by")
    private User submittedBy;
    
    public enum GovernanceSubtype {
        CORPORATE, ETHICS, RISK
    }
}
