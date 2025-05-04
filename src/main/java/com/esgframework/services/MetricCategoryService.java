package com.esgframework.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esgframework.models.MetricCategory;
import com.esgframework.repositories.MetricCategoryRepository;

@Service
public class MetricCategoryService {
    
    @Autowired
    private MetricCategoryRepository metricCategoryRepository;
    
    /**
     * Get all metric categories by type
     * @param metricType The type of metric (ENVIRONMENT, SOCIAL, GOVERNANCE)
     * @return List of metric categories
     */
    public List<MetricCategory> getCategoriesByType(String metricType) {
        return metricCategoryRepository.findByMetricTypeOrderByDisplayOrderAsc(metricType);
    }
    
    /**
     * Get a metric category by code and type
     * @param categoryCode The category code
     * @param metricType The type of metric
     * @return The metric category if found
     */
    public Optional<MetricCategory> getCategoryByCodeAndType(String categoryCode, String metricType) {
        return metricCategoryRepository.findByCategoryCodeAndMetricType(categoryCode, metricType);
    }
    
    /**
     * Create a new metric category
     * @param metricCategory The metric category to create
     * @return The created metric category
     */
    @Transactional
    public MetricCategory createCategory(MetricCategory metricCategory) {
        return metricCategoryRepository.save(metricCategory);
    }
    
    /**
     * Initialize default categories if they don't exist
     */
    @Transactional
    public void initializeDefaultCategories() {
        // Environment categories
        createCategoryIfNotExists("ENVIRONMENT", "SCOPE_1", "Scope 1", "Direct emissions from owned or controlled sources", 1);
        createCategoryIfNotExists("ENVIRONMENT", "SCOPE_2", "Scope 2", "Indirect emissions from purchased electricity, steam, heating, and cooling", 2);
        createCategoryIfNotExists("ENVIRONMENT", "SCOPE_3", "Scope 3", "All other indirect emissions in a company's value chain", 3);
        createCategoryIfNotExists("ENVIRONMENT", "SOLVENT", "Solvent", "Emissions from solvent use in industrial processes", 4);
        createCategoryIfNotExists("ENVIRONMENT", "SINK", "Sink", "Carbon removal activities", 5);
        
        // Social categories
        createCategoryIfNotExists("SOCIAL", "EMPLOYEE", "Employee", "Metrics related to employee welfare and development", 1);
        createCategoryIfNotExists("SOCIAL", "COMMUNITY", "Community", "Metrics related to community engagement and impact", 2);
        createCategoryIfNotExists("SOCIAL", "SUPPLY_CHAIN", "Supply Chain", "Metrics related to supply chain management and ethics", 3);
        
        // Governance categories
        createCategoryIfNotExists("GOVERNANCE", "CORPORATE", "Corporate", "Metrics related to corporate governance structure", 1);
        createCategoryIfNotExists("GOVERNANCE", "ETHICS", "Ethics", "Metrics related to business ethics and compliance", 2);
        createCategoryIfNotExists("GOVERNANCE", "RISK", "Risk", "Metrics related to risk management", 3);
    }
    
    /**
     * Helper method to create a category if it doesn't exist
     */
    private void createCategoryIfNotExists(String metricType, String categoryCode, String name, String description, int displayOrder) {
        if (!metricCategoryRepository.findByCategoryCodeAndMetricType(categoryCode, metricType).isPresent()) {
            MetricCategory category = new MetricCategory();
            category.setMetricType(metricType);
            category.setCategoryCode(categoryCode);
            category.setName(name);
            category.setDescription(description);
            category.setDisplayOrder(displayOrder);
            metricCategoryRepository.save(category);
        }
    }
}
