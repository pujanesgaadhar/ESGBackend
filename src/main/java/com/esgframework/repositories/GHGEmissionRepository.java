package com.esgframework.repositories;

import com.esgframework.models.GHGEmission;
import com.esgframework.models.EmissionScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

import com.esgframework.models.Company;
import com.esgframework.models.SubmissionStatus;

@Repository
public interface GHGEmissionRepository extends JpaRepository<GHGEmission, Long> {
    List<GHGEmission> findByCompanyAndStatus(Company company, SubmissionStatus status);
    List<GHGEmission> findByCompany(Company company);
    List<GHGEmission> findByCompanyId(Long companyId);
    List<GHGEmission> findByCompanyIdAndScope(Long companyId, EmissionScope scope);
    List<GHGEmission> findByCompanyIdAndStartDateBetween(Long companyId, LocalDateTime startDate, LocalDateTime endDate);
}
