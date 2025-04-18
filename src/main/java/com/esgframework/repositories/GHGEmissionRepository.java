package com.esgframework.repositories;

import com.esgframework.models.GHGEmission;
import com.esgframework.models.EmissionScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GHGEmissionRepository extends JpaRepository<GHGEmission, Long> {
    List<GHGEmission> findByCompanyId(Long companyId);
    List<GHGEmission> findByCompanyIdAndScope(Long companyId, EmissionScope scope);
    List<GHGEmission> findByCompanyIdAndStartDateBetween(Long companyId, LocalDateTime startDate, LocalDateTime endDate);
}
