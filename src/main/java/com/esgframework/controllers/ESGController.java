package com.esgframework.controllers;

import com.esgframework.models.ESGSubmission;
import com.esgframework.dto.ReviewRequest;
import com.esgframework.services.ESGService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/esg")
public class ESGController {

    @Autowired
    private ESGService esgService;

    @PostMapping("/submissions")
    @PreAuthorize("hasAnyRole('admin', 'manager', 'representative')")
    public ResponseEntity<ESGSubmission> submitESGData(@Valid @RequestBody ESGSubmission submission) {
        return ResponseEntity.ok(esgService.submitESGData(submission));
    }

    @GetMapping("/submissions")
    @PreAuthorize("hasRole('manager')")
    public ResponseEntity<List<ESGSubmission>> getESGSubmissions() {
        return ResponseEntity.ok(esgService.getESGSubmissions());
    }

    @GetMapping("/submissions/{id}")
    @PreAuthorize("hasRole('manager')")
    public ResponseEntity<ESGSubmission> getSubmissionDetails(@PathVariable Long id) {
        return ResponseEntity.ok(esgService.getSubmissionDetails(id));
    }

    @PutMapping("/submissions/{id}/review")
    @PreAuthorize("hasRole('manager')")
    public ResponseEntity<ESGSubmission> reviewSubmission(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest review) {
        return ResponseEntity.ok(esgService.reviewSubmission(id, review));
    }


    @GetMapping("/submissions/history")
    @PreAuthorize("hasAnyRole('manager', 'representative')")
    public ResponseEntity<List<ESGSubmission>> getSubmissionHistory() {
        return ResponseEntity.ok(esgService.getSubmissionHistory());
    }

    @GetMapping("/chart-data")
    @PreAuthorize("hasAnyRole('admin', 'manager')")
    public ResponseEntity<Map<String, Object>> getChartData() {
        return ResponseEntity.ok(esgService.getChartData());
    }
}
