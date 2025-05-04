package com.esgframework.models;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "companies")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;

    @Column
    private String industry;

    @Column
    private String status = "active";

    @OneToMany(mappedBy = "company")
    @JsonManagedReference
    private List<User> users;

    @OneToMany(mappedBy = "company")
    @JsonIgnore
    private List<GHGEmission> ghgEmissions;
    
    @OneToMany(mappedBy = "company")
    @JsonIgnore
    private List<SocialMetric> socialMetrics;
    
    @OneToMany(mappedBy = "company")
    @JsonIgnore
    private List<GovernanceMetric> governanceMetrics;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<GHGEmission> getGhgEmissions() {
        return ghgEmissions;
    }

    public void setGhgEmissions(List<GHGEmission> ghgEmissions) {
        this.ghgEmissions = ghgEmissions;
    }
    
    public List<SocialMetric> getSocialMetrics() {
        return socialMetrics;
    }

    public void setSocialMetrics(List<SocialMetric> socialMetrics) {
        this.socialMetrics = socialMetrics;
    }
    
    public List<GovernanceMetric> getGovernanceMetrics() {
        return governanceMetrics;
    }

    public void setGovernanceMetrics(List<GovernanceMetric> governanceMetrics) {
        this.governanceMetrics = governanceMetrics;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
