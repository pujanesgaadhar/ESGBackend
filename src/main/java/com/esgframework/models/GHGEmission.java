package com.esgframework.models;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ghg_emissions")
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

    @Enumerated(EnumType.STRING)
    private EmissionScope scope;

    @Enumerated(EnumType.STRING)
    private EmissionCategory category;

    @Enumerated(EnumType.STRING)
    private TimeFrame timeFrame;

    private LocalDateTime startDate;
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
}
