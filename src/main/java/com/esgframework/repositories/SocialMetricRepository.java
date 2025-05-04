package com.esgframework.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.esgframework.models.Company;
import com.esgframework.models.SocialMetric;
import com.esgframework.models.SubmissionStatus;

@Repository
public interface SocialMetricRepository extends JpaRepository<SocialMetric, Long> {
    
    List<SocialMetric> findByCompany(Company company);
    
    List<SocialMetric> findByCompanyAndStatus(Company company, SubmissionStatus status);
    
    List<SocialMetric> findByCompanyOrderByCreatedAtDesc(Company company);
}
