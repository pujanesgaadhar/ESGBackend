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

import com.esgframework.models.SocialMetric;
import com.esgframework.models.SubmissionStatus;
import com.esgframework.services.SocialMetricService;

@RestController
@RequestMapping("/api/social-metrics")
public class SocialMetricController {

    @Autowired
    private SocialMetricService socialMetricService;
    
    @PostMapping
    @PreAuthorize("hasRole('representative')")
    public ResponseEntity<SocialMetric> submitSocialMetric(@RequestBody SocialMetric socialMetric) {
        try {
            SocialMetric submittedMetric = socialMetricService.submitSocialMetric(socialMetric);
            return ResponseEntity.status(HttpStatus.CREATED).body(submittedMetric);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('manager', 'representative')")
    public ResponseEntity<List<SocialMetric>> getSocialMetrics() {
        try {
            List<SocialMetric> metrics = socialMetricService.getSocialMetricsByCompany();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/company")
    @PreAuthorize("hasAnyRole('manager', 'representative')")
    public ResponseEntity<List<SocialMetric>> getAllSocialMetricsForCompany() {
        try {
            List<SocialMetric> metrics = socialMetricService.getSocialMetricsByCompany();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('manager', 'representative')")
    public ResponseEntity<List<SocialMetric>> getSocialMetricsHistory() {
        try {
            List<SocialMetric> metrics = socialMetricService.getSocialMetricsHistory();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('manager', 'representative')")
    public ResponseEntity<SocialMetric> getSocialMetricById(@PathVariable Long id) {
        try {
            SocialMetric metric = socialMetricService.getSocialMetricById(id);
            return ResponseEntity.ok(metric);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('manager')")
    public ResponseEntity<SocialMetric> updateSocialMetricStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest statusUpdate) {
        try {
            SubmissionStatus status = SubmissionStatus.valueOf(statusUpdate.getStatus());
            SocialMetric updatedMetric = socialMetricService.updateSocialMetricStatus(id, status);
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
