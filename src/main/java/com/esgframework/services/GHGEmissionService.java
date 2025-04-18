package com.esgframework.services;

import com.esgframework.models.GHGEmission;
import com.esgframework.models.EmissionScope;
import com.esgframework.repositories.GHGEmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class GHGEmissionService {
    @Autowired
    private GHGEmissionRepository ghgEmissionRepository;

    public GHGEmission submitEmissionData(GHGEmission emission) {
        emission.setSubmissionDate(LocalDateTime.now());
        return ghgEmissionRepository.save(emission);
    }

    public List<GHGEmission> getCompanyEmissions(Long companyId) {
        return ghgEmissionRepository.findByCompanyId(companyId);
    }

    public List<GHGEmission> getCompanyEmissionsByScope(Long companyId, EmissionScope scope) {
        return ghgEmissionRepository.findByCompanyIdAndScope(companyId, scope);
    }

    public List<GHGEmission> getCompanyEmissionsByDateRange(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        return ghgEmissionRepository.findByCompanyIdAndStartDateBetween(companyId, startDate, endDate);
    }
}
