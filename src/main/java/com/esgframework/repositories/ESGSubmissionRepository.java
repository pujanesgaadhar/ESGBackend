package com.esgframework.repositories;

import com.esgframework.models.ESGSubmission;
import com.esgframework.models.Company;
import com.esgframework.models.SubmissionStatus;
import com.esgframework.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ESGSubmissionRepository extends JpaRepository<ESGSubmission, Long> {
    List<ESGSubmission> findByStatus(SubmissionStatus status);
    List<ESGSubmission> findBySubmittedByOrderByCreatedAtDesc(User submittedBy);
    List<ESGSubmission> findByCompany(Company company);
    List<ESGSubmission> findByCompanyAndStatus(Company company, SubmissionStatus status);
}
