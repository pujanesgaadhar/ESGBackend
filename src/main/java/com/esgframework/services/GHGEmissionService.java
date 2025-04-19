package com.esgframework.services;

import com.esgframework.models.GHGEmission;
import com.esgframework.models.EmissionScope;
import com.esgframework.repositories.GHGEmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

import com.esgframework.models.User;
import com.esgframework.models.Company;

@Service
public class GHGEmissionService {
    @Autowired
    private com.esgframework.repositories.UserRepository userRepository;

    private User getCurrentUser() {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new javax.persistence.EntityNotFoundException("User not found"));
    }

    public List<GHGEmission> getPendingEmissionsForCompany() {
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().equalsIgnoreCase("manager")) {
            throw new SecurityException("Only managers can view GHG submissions");
        }
        Company company = currentUser.getCompany();
        return ghgEmissionRepository.findByCompanyAndStatus(company, com.esgframework.models.SubmissionStatus.PENDING);
    }
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GHGEmissionService.class);
    @Autowired
    private GHGEmissionRepository ghgEmissionRepository;

    public GHGEmission submitEmissionData(GHGEmission emission) {
    logger.info("Submitting GHG emission data for company ID: {}", emission.getCompany().getId());
        emission.setSubmissionDate(LocalDateTime.now());
        GHGEmission saved = ghgEmissionRepository.save(emission);
    logger.info("GHG emission saved with ID: {}", saved.getId());
    return saved;
    }

    public List<GHGEmission> getCompanyEmissions(Long companyId) {
    logger.info("Fetching all GHG emissions for company ID: {}", companyId);
        List<GHGEmission> emissions = ghgEmissionRepository.findByCompanyId(companyId);
    logger.info("Found {} GHG emission records", emissions.size());
    return emissions;
    }

    public List<GHGEmission> getCompanyEmissionsByScope(Long companyId, EmissionScope scope) {
    logger.info("Fetching GHG emissions for company ID: {} and scope: {}", companyId, scope);
        List<GHGEmission> emissions = ghgEmissionRepository.findByCompanyIdAndScope(companyId, scope);
    logger.info("Found {} GHG emission records for scope {}", emissions.size(), scope);
    return emissions;
    }

    public List<GHGEmission> getCompanyEmissionsByDateRange(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
    logger.info("Fetching GHG emissions for company ID: {} between {} and {}", companyId, startDate, endDate);
        List<GHGEmission> emissions = ghgEmissionRepository.findByCompanyIdAndStartDateBetween(companyId, startDate, endDate);
    logger.info("Found {} GHG emission records in date range", emissions.size());
    return emissions;
    }
}
