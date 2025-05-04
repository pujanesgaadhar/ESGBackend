package com.esgframework.models;

import javax.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "metric_categories", indexes = {
    @Index(name = "idx_category_type", columnList = "metric_type"),
    @Index(name = "idx_category_code", columnList = "category_code")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricCategory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "metric_type", nullable = false)
    private String metricType; // ENVIRONMENT, SOCIAL, GOVERNANCE
    
    @Column(name = "category_code", nullable = false)
    private String categoryCode; // SCOPE_1, EMPLOYEE, CORPORATE, etc.
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(name = "display_order")
    private Integer displayOrder;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
