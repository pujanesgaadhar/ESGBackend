package com.esgframework.controllers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esgframework.models.MetricCategory;
import com.esgframework.services.MetricCategoryService;

@RestController
@RequestMapping("/api/categories")
public class MetricCategoryController {
    
    @Autowired
    private MetricCategoryService metricCategoryService;
    
    /**
     * Get all categories by metric type
     * @param type The metric type (environment, social, governance)
     * @return List of categories
     */
    @GetMapping("/{type}")
    public ResponseEntity<List<MetricCategory>> getCategoriesByType(@PathVariable String type) {
        // Convert to uppercase for consistency
        String metricType = type.toUpperCase();
        List<MetricCategory> categories = metricCategoryService.getCategoriesByType(metricType);
        return ResponseEntity.ok(categories);
    }
    
    /**
     * Get a category by code and type
     * @param type The metric type
     * @param code The category code
     * @return The category if found
     */
    @GetMapping("/{type}/{code}")
    public ResponseEntity<?> getCategoryByCodeAndType(
            @PathVariable String type, 
            @PathVariable String code) {
        
        // Convert to uppercase for consistency
        String metricType = type.toUpperCase();
        String categoryCode = code.toUpperCase();
        
        Optional<MetricCategory> category = metricCategoryService.getCategoryByCodeAndType(categoryCode, metricType);
        
        if (category.isPresent()) {
            return ResponseEntity.ok(category.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Category not found"));
        }
    }
    
    /**
     * Create a new category
     * @param metricCategory The category to create
     * @return The created category
     */
    @PostMapping
    public ResponseEntity<MetricCategory> createCategory(@RequestBody MetricCategory metricCategory) {
        // Convert to uppercase for consistency
        metricCategory.setMetricType(metricCategory.getMetricType().toUpperCase());
        metricCategory.setCategoryCode(metricCategory.getCategoryCode().toUpperCase());
        
        MetricCategory createdCategory = metricCategoryService.createCategory(metricCategory);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }
    
    /**
     * Initialize default categories
     * @return Success message
     */
    @PostMapping("/initialize")
    public ResponseEntity<Map<String, String>> initializeCategories() {
        metricCategoryService.initializeDefaultCategories();
        return ResponseEntity.ok(Map.of("message", "Default categories initialized"));
    }
}
