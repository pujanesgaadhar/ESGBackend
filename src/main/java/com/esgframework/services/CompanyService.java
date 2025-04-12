package com.esgframework.services;

import com.esgframework.models.Company;
import com.esgframework.repositories.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public Company createCompany(Company company) {
        return companyRepository.save(company);
    }

    public Company updateCompany(Long id, Company companyDetails) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + id));
        
        company.setName(companyDetails.getName());
        company.setDescription(companyDetails.getDescription());
        company.setIndustry(companyDetails.getIndustry());
        
        return companyRepository.save(company);
    }

    public void deleteCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + id));
        companyRepository.delete(company);
    }

    public Map<String, Object> getCompanyMetrics(Long id) {
        companyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + id));
        // TODO: Implement metrics calculation
        return Map.of("message", "Metrics calculation not implemented yet");
    }
}
