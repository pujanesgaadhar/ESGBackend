package com.esgframework.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esgframework.models.GovernanceMetric;
import com.esgframework.models.SubmissionStatus;
import com.esgframework.services.GovernanceMetricService;

@RestController
@RequestMapping("/api/governance-metrics")
public class GovernanceMetricController {

    @Autowired
    private GovernanceMetricService governanceMetricService;
    
    @PostMapping
    @PreAuthorize("hasRole('representative')")
    public ResponseEntity<GovernanceMetric> submitGovernanceMetric(@RequestBody GovernanceMetric governanceMetric) {
        try {
            GovernanceMetric submittedMetric = governanceMetricService.submitGovernanceMetric(governanceMetric);
            return ResponseEntity.status(HttpStatus.CREATED).body(submittedMetric);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('manager', 'representative')")
    public ResponseEntity<List<GovernanceMetric>> getGovernanceMetrics() {
        try {
            List<GovernanceMetric> metrics = governanceMetricService.getGovernanceMetricsByCompany();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/company")
    @PreAuthorize("hasAnyRole('manager', 'representative')")
    public ResponseEntity<List<GovernanceMetric>> getAllGovernanceMetricsForCompany() {
        try {
            List<GovernanceMetric> metrics = governanceMetricService.getGovernanceMetricsByCompany();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('manager', 'representative')")
    public ResponseEntity<List<GovernanceMetric>> getGovernanceMetricsHistory() {
        try {
            List<GovernanceMetric> metrics = governanceMetricService.getGovernanceMetricsHistory();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('manager', 'representative')")
    public ResponseEntity<GovernanceMetric> getGovernanceMetricById(@PathVariable Long id) {
        try {
            GovernanceMetric metric = governanceMetricService.getGovernanceMetricById(id);
            return ResponseEntity.ok(metric);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('manager')")
    public ResponseEntity<GovernanceMetric> updateGovernanceMetricStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest statusUpdate) {
        try {
            SubmissionStatus status = SubmissionStatus.valueOf(statusUpdate.getStatus());
            GovernanceMetric updatedMetric = governanceMetricService.updateGovernanceMetricStatus(id, status);
            return ResponseEntity.ok(updatedMetric);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Inner class for status update requests
    public static class StatusUpdateRequest {
        private String status;
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
    }
}
