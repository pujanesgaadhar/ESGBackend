package com.esgframework.controllers;

import com.esgframework.models.GHGEmission;
import com.esgframework.models.EmissionScope;
import com.esgframework.services.GHGEmissionService;
import com.esgframework.services.CSVService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/ghg-emissions")
@CrossOrigin(origins = "*")
public class GHGEmissionController {
    @GetMapping("/company/pending")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('manager')")
    public org.springframework.http.ResponseEntity<java.util.List<com.esgframework.models.GHGEmission>> getPendingGHGEmissionsForManager() {
        java.util.List<com.esgframework.models.GHGEmission> emissions = ghgEmissionService.getPendingEmissionsForCompany();
        return org.springframework.http.ResponseEntity.ok(emissions);
    }
    
    @GetMapping("/company")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('manager') or hasRole('representative')")
    public ResponseEntity<List<GHGEmission>> getAllGHGEmissionsForCompany() {
        logger.info("Retrieving all GHG emissions for current company");
        List<GHGEmission> emissions = ghgEmissionService.getAllEmissionsForCompany();
        logger.info("Found {} total GHG emission records", emissions.size());
        return ResponseEntity.ok(emissions);
    }
    
    @GetMapping("/history")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('manager') or hasRole('representative')")
    public ResponseEntity<List<GHGEmission>> getGHGEmissionsHistory() {
        logger.info("Retrieving GHG emissions history");
        List<GHGEmission> emissions = ghgEmissionService.getEmissionsHistory();
        logger.info("Found {} GHG emission history records", emissions.size());
        return ResponseEntity.ok(emissions);
    }
    private static final Logger logger = LoggerFactory.getLogger(GHGEmissionController.class);
    @Autowired
    private GHGEmissionService ghgEmissionService;
    
    @Autowired
    private CSVService csvService;

    @PostMapping
    public ResponseEntity<?> submitEmission(@RequestBody GHGEmission emission) {
        try {
            logger.info("Received GHG emission submission - Scope: {}, Category: {}", 
                       emission.getScope(), emission.getCategory());
            
            // Validate company ID
            if (emission.getCompany() == null || emission.getCompany().getId() == null) {
                logger.error("Missing company ID in GHG emission submission");
                return ResponseEntity.badRequest().body(Map.of("error", "Company ID is required"));
            }
            
            logger.info("Company ID: {}", emission.getCompany().getId());
            logger.info("Emission details - StartDate: {}, EndDate: {}, Quantity: {}, Unit: {}", 
                       emission.getStartDate(), emission.getEndDate(), emission.getQuantity(), emission.getUnit());
            
            // Validate required fields
            if (emission.getScope() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Scope is required"));
            }
            if (emission.getCategory() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Category is required"));
            }
            if (emission.getStartDate() == null || emission.getEndDate() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Start and end dates are required"));
            }
            
            GHGEmission saved = ghgEmissionService.submitEmissionData(emission);
            logger.info("GHG emission successfully stored in database with ID: {}, Status: {}", 
                       saved.getId(), saved.getStatus());
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            logger.error("Error saving GHG emission data: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to save GHG emission data",
                "message", e.getMessage(),
                "details", e.getClass().getName()
            ));
        }
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<GHGEmission>> getCompanyEmissions(@PathVariable Long companyId) {
        logger.info("Retrieving all GHG emissions for company ID: {}", companyId);
        List<GHGEmission> emissions = ghgEmissionService.getCompanyEmissions(companyId);
        logger.info("Found {} GHG emission records for company {}", emissions.size(), companyId);
        return ResponseEntity.ok(emissions);
    }

    @GetMapping("/company/{companyId}/scope/{scope}")
    public ResponseEntity<List<GHGEmission>> getCompanyEmissionsByScope(
            @PathVariable Long companyId,
            @PathVariable EmissionScope scope) {
        logger.info("Retrieving GHG emissions for company ID: {} and scope: {}", companyId, scope);
        List<GHGEmission> emissions = ghgEmissionService.getCompanyEmissionsByScope(companyId, scope);
        logger.info("Found {} GHG emission records for company {} and scope {}", emissions.size(), companyId, scope);
        return ResponseEntity.ok(emissions);
    }

    @GetMapping("/company/{companyId}/date-range")
    public ResponseEntity<List<GHGEmission>> getCompanyEmissionsByDateRange(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        logger.info("Retrieving GHG emissions for company ID: {} between {} and {}", companyId, startDate, endDate);
        List<GHGEmission> emissions = ghgEmissionService.getCompanyEmissionsByDateRange(companyId, startDate, endDate);
        logger.info("Found {} GHG emission records for company {} in date range", emissions.size(), companyId);
        return ResponseEntity.ok(emissions);
    }
    
    @PutMapping("/{id}/status")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('manager')")
    public ResponseEntity<GHGEmission> updateEmissionStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest statusRequest) {
        logger.info("Updating GHG emission status for ID: {} to {}", id, statusRequest.getStatus());
        GHGEmission updated = ghgEmissionService.updateEmissionStatus(id, statusRequest.getStatus());
        logger.info("GHG emission status updated for ID: {}", id);
        return ResponseEntity.ok(updated);
    }
    
    // Status update request class
    public static class StatusUpdateRequest {
        private String status;
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
    }
    
    @PostMapping("/upload-csv")
    public ResponseEntity<?> uploadCSV(
            @RequestParam("file") MultipartFile file,
            @RequestParam("scope") String scopeStr,
            @RequestParam("companyId") Long companyId) {
        
        logger.info("Received CSV upload request for scope: {} and company ID: {}", scopeStr, companyId);
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Please upload a CSV file"));
        }
        
        if (!file.getOriginalFilename().endsWith(".csv")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Only CSV files are allowed"));
        }
        
        try {
            EmissionScope scope = EmissionScope.valueOf(scopeStr);
            int recordsProcessed = csvService.processCSVFile(file, scope, companyId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "CSV file processed successfully");
            response.put("recordsProcessed", recordsProcessed);
            response.put("scope", scope.name());
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid scope value: {}", scopeStr, e);
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid scope value"));
        } catch (Exception e) {
            logger.error("Error processing CSV file: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to process CSV file",
                "message", e.getMessage(),
                "details", e.getClass().getName()
            ));
        }
    }
}
