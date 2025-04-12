package com.esgframework.repositories;

import com.esgframework.models.User;
import com.esgframework.models.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByCompany(Company company);
    List<User> findByCompanyAndRole(Company company, String role);
}
