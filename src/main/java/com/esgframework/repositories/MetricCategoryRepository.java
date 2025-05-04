package com.esgframework.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.esgframework.models.MetricCategory;

@Repository
public interface MetricCategoryRepository extends JpaRepository<MetricCategory, Long> {
    
    List<MetricCategory> findByMetricType(String metricType);
    
    Optional<MetricCategory> findByCategoryCodeAndMetricType(String categoryCode, String metricType);
    
    List<MetricCategory> findByMetricTypeOrderByDisplayOrderAsc(String metricType);
}
