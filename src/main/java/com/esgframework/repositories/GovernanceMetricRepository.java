package com.esgframework.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.esgframework.models.Company;
import com.esgframework.models.GovernanceMetric;
import com.esgframework.models.SubmissionStatus;

@Repository
public interface GovernanceMetricRepository extends JpaRepository<GovernanceMetric, Long> {
    
    List<GovernanceMetric> findByCompany(Company company);
    
    List<GovernanceMetric> findByCompanyAndStatus(Company company, SubmissionStatus status);
    
    List<GovernanceMetric> findByCompanyOrderByCreatedAtDesc(Company company);
}
