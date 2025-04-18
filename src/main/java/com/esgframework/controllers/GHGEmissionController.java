package com.esgframework.controllers;

import com.esgframework.models.GHGEmission;
import com.esgframework.models.EmissionScope;
import com.esgframework.services.GHGEmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/ghg-emissions")
@CrossOrigin(origins = "*")
public class GHGEmissionController {
    private static final Logger logger = LoggerFactory.getLogger(GHGEmissionController.class);
    @Autowired
    private GHGEmissionService ghgEmissionService;

    @PostMapping
    public ResponseEntity<GHGEmission> submitEmission(@RequestBody GHGEmission emission) {
        logger.info("Received GHG emission submission: {}", emission);
        GHGEmission saved = ghgEmissionService.submitEmissionData(emission);
        logger.info("GHG emission stored in database with ID: {}", saved.getId());
        return ResponseEntity.ok(saved);
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
}
