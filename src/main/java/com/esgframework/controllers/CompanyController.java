package com.esgframework.controllers;

import com.esgframework.models.Company;
import com.esgframework.services.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @GetMapping
    public ResponseEntity<?> getCompanies() {
        try {
            List<Company> companies = companyService.getAllCompanies();
            return ResponseEntity.ok().body(companies);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error fetching companies: " + e.getMessage());
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public Company addCompany(@Valid @RequestBody Company company) {
        System.out.println("Company added: " + company);
        return companyService.createCompany(company);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Company> updateCompany(@PathVariable Long id, @Valid @RequestBody Company company) {
        return ResponseEntity.ok(companyService.updateCompany(id, company));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/metrics")
    public ResponseEntity<?> getCompanyMetrics(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getCompanyMetrics(id));
    }
}
