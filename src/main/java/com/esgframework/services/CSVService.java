package com.esgframework.services;

import com.esgframework.models.*;
import com.esgframework.repositories.GHGEmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class CSVService {
    private static final Logger logger = LoggerFactory.getLogger(CSVService.class);
    
    @Autowired
    private GHGEmissionRepository ghgEmissionRepository;
    
    @Autowired
    private com.esgframework.repositories.UserRepository userRepository;
    
    @Autowired
    private com.esgframework.repositories.CompanyRepository companyRepository;

    public int processCSVFile(MultipartFile file, EmissionScope scope, Long companyId) throws IOException {
        logger.info("Processing CSV file for scope: {} and company ID: {}", scope, companyId);
        
        // Get current user
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
            .orElseThrow(() -> new javax.persistence.EntityNotFoundException("User not found"));
        
        // Get company
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new javax.persistence.EntityNotFoundException("Company not found"));
        
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            List<GHGEmission> emissions = new ArrayList<>();
            String line;
            String[] headers = null;
            int lineNumber = 0;
            
            while ((line = fileReader.readLine()) != null) {
                String[] values = line.split(",");
                
                if (lineNumber == 0) {
                    // This is the header row
                    headers = values;
                    lineNumber++;
                    continue;
                }
                
                try {
                    Map<String, String> record = new HashMap<>();
                    for (int i = 0; i < headers.length && i < values.length; i++) {
                        record.put(headers[i].trim(), values[i].trim());
                    }
                    
                    GHGEmission emission = createEmissionFromCSV(record, scope, company, currentUser);
                    if (emission != null) {
                        emissions.add(emission);
                    }
                } catch (Exception e) {
                    logger.error("Error processing CSV record at line {}: {}", lineNumber, e.getMessage(), e);
                }
                
                lineNumber++;
            }
            
            // Save all emissions
            if (!emissions.isEmpty()) {
                ghgEmissionRepository.saveAll(emissions);
                logger.info("Successfully processed {} emissions from CSV file", emissions.size());
                return emissions.size();
            }
            
            return 0;
        }
    }
    
    private GHGEmission createEmissionFromCSV(Map<String, String> record, EmissionScope scope, Company company, User currentUser) {
        try {
            GHGEmission emission = new GHGEmission();
            
            // Set common fields
            emission.setCompany(company);
            emission.setSubmittedBy(currentUser);
            emission.setLastModifiedBy(currentUser);
            emission.setScope(scope);
            emission.setStatus(SubmissionStatus.PENDING);
            emission.setSubmissionDate(LocalDateTime.now());
            emission.setTimeFrame(TimeFrame.CUSTOM);
            
            // Process dates
            LocalDate startDate = parseDate(record.get("Start Date"));
            LocalDate endDate = parseDate(record.get("End Date"));
            
            if (startDate != null && endDate != null) {
                emission.setStartDate(startDate.atStartOfDay());
                emission.setEndDate(endDate.atTime(LocalTime.MAX));
            } else {
                logger.error("Invalid date format in CSV record");
                return null;
            }
            
            // Set quantity and unit
            try {
                String quantityStr = record.get("Quantity");
                if (quantityStr != null && !quantityStr.isEmpty()) {
                    emission.setQuantity(Double.parseDouble(quantityStr));
                }
            } catch (NumberFormatException e) {
                logger.error("Invalid quantity format in CSV record: {}", e.getMessage());
            }
            
            emission.setUnit(record.get("Unit"));
            
            // Set category based on scope
            setCategory(emission, record, scope);
            
            // Set other fields if they exist in the CSV
            if (record.containsKey("Source")) {
                emission.setSource(record.get("Source"));
            }
            
            if (record.containsKey("Activity")) {
                emission.setActivity(record.get("Activity"));
            }
            
            if (record.containsKey("Calculation Method")) {
                emission.setCalculationMethod(record.get("Calculation Method"));
            }
            
            if (record.containsKey("Emission Factor")) {
                try {
                    String efStr = record.get("Emission Factor");
                    if (efStr != null && !efStr.isEmpty()) {
                        emission.setEmissionFactor(Double.parseDouble(efStr));
                    }
                } catch (NumberFormatException e) {
                    logger.error("Invalid emission factor format in CSV record: {}", e.getMessage());
                }
            }
            
            if (record.containsKey("Emission Factor Unit")) {
                emission.setEmissionFactorUnit(record.get("Emission Factor Unit"));
            }
            
            if (record.containsKey("Notes")) {
                emission.setNotes(record.get("Notes"));
            }
            
            return emission;
        } catch (Exception e) {
            logger.error("Error creating emission from CSV record: {}", e.getMessage(), e);
            return null;
        }
    }
    
    private void setCategory(GHGEmission emission, Map<String, String> record, EmissionScope scope) {
        String categoryStr = record.get("Category");
        if (categoryStr == null) {
            return;
        }
        
        try {
            switch (scope) {
                case SCOPE_1:
                    if (categoryStr.equalsIgnoreCase("Stationary Combustion")) {
                        emission.setCategory(EmissionCategory.STATIONARY_COMBUSTION);
                    } else if (categoryStr.equalsIgnoreCase("Mobile Combustion")) {
                        emission.setCategory(EmissionCategory.MOBILE_COMBUSTION);
                    } else if (categoryStr.equalsIgnoreCase("Process Emissions")) {
                        emission.setCategory(EmissionCategory.PROCESS_EMISSIONS);
                    } else if (categoryStr.equalsIgnoreCase("Fugitive Emissions")) {
                        emission.setCategory(EmissionCategory.FUGITIVE_EMISSIONS);
                    }
                    break;
                    
                case SCOPE_2:
                    if (categoryStr.equalsIgnoreCase("Purchased Electricity")) {
                        emission.setCategory(EmissionCategory.PURCHASED_ELECTRICITY);
                    } else if (categoryStr.equalsIgnoreCase("Purchased Heating")) {
                        emission.setCategory(EmissionCategory.PURCHASED_HEATING);
                    } else if (categoryStr.equalsIgnoreCase("Purchased Cooling")) {
                        emission.setCategory(EmissionCategory.PURCHASED_COOLING);
                    } else if (categoryStr.equalsIgnoreCase("Purchased Steam")) {
                        emission.setCategory(EmissionCategory.PURCHASED_STEAM);
                    }
                    break;
                    
                case SCOPE_3:
                    if (categoryStr.equalsIgnoreCase("Purchased Goods and Services")) {
                        emission.setCategory(EmissionCategory.PURCHASED_GOODS_SERVICES);
                    } else if (categoryStr.equalsIgnoreCase("Capital Goods")) {
                        emission.setCategory(EmissionCategory.CAPITAL_GOODS);
                    } else if (categoryStr.equalsIgnoreCase("Fuel and Energy Related Activities")) {
                        emission.setCategory(EmissionCategory.FUEL_ENERGY_ACTIVITIES);
                    } else if (categoryStr.equalsIgnoreCase("Transportation and Distribution")) {
                        emission.setCategory(EmissionCategory.TRANSPORTATION_DISTRIBUTION);
                    } else if (categoryStr.equalsIgnoreCase("Waste Generated")) {
                        emission.setCategory(EmissionCategory.WASTE_GENERATED);
                    } else if (categoryStr.equalsIgnoreCase("Business Travel")) {
                        emission.setCategory(EmissionCategory.BUSINESS_TRAVEL);
                    } else if (categoryStr.equalsIgnoreCase("Employee Commuting")) {
                        emission.setCategory(EmissionCategory.EMPLOYEE_COMMUTING);
                    } else if (categoryStr.equalsIgnoreCase("Leased Assets")) {
                        emission.setCategory(EmissionCategory.LEASED_ASSETS);
                    } else if (categoryStr.equalsIgnoreCase("Investments")) {
                        emission.setCategory(EmissionCategory.INVESTMENTS);
                    } else if (categoryStr.equalsIgnoreCase("Downstream Transportation")) {
                        emission.setCategory(EmissionCategory.DOWNSTREAM_TRANSPORTATION);
                    } else if (categoryStr.equalsIgnoreCase("Processing of Sold Products")) {
                        emission.setCategory(EmissionCategory.PROCESSING_SOLD_PRODUCTS);
                    } else if (categoryStr.equalsIgnoreCase("Use of Sold Products")) {
                        emission.setCategory(EmissionCategory.USE_OF_SOLD_PRODUCTS);
                    } else if (categoryStr.equalsIgnoreCase("End of Life Treatment of Products")) {
                        emission.setCategory(EmissionCategory.END_OF_LIFE_PRODUCTS);
                    } else if (categoryStr.equalsIgnoreCase("Franchises")) {
                        emission.setCategory(EmissionCategory.FRANCHISES);
                    }
                    break;
                    
                case SOLVENT:
                    if (categoryStr.equalsIgnoreCase("Solvent Consumption")) {
                        emission.setCategory(EmissionCategory.SOLVENT_CONSUMPTION);
                    } else if (categoryStr.equalsIgnoreCase("Solvent Recovery")) {
                        emission.setCategory(EmissionCategory.SOLVENT_RECOVERY);
                    } else if (categoryStr.equalsIgnoreCase("Solvent Loss")) {
                        emission.setCategory(EmissionCategory.SOLVENT_LOSS);
                    }
                    break;
                    
                case SINK:
                    if (categoryStr.equalsIgnoreCase("Reforestation")) {
                        emission.setCategory(EmissionCategory.REFORESTATION);
                    } else if (categoryStr.equalsIgnoreCase("Afforestation")) {
                        emission.setCategory(EmissionCategory.AFFORESTATION);
                    } else if (categoryStr.equalsIgnoreCase("Soil Carbon Sequestration")) {
                        emission.setCategory(EmissionCategory.SOIL_CARBON_SEQUESTRATION);
                    }
                    break;
            }
        } catch (Exception e) {
            logger.error("Error setting category from CSV: {}", e.getMessage(), e);
        }
    }
    
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        
        // Try different date formats
        DateTimeFormatter[] formatters = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
        };
        
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                // Try next format
            }
        }
        
        logger.error("Could not parse date: {}", dateStr);
        return null;
    }
}
