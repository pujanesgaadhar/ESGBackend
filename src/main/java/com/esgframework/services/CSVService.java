package com.esgframework.services;

import com.esgframework.models.*;
import com.esgframework.repositories.CompanyRepository;
import com.esgframework.repositories.GHGEmissionRepository;
import com.esgframework.repositories.UserRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private UserRepository userRepository;
    
    @Autowired
    private CompanyRepository companyRepository;

    public int processCSVFile(MultipartFile file, EmissionScope scope, Long companyId) throws IOException {
        logger.info("Processing CSV file for scope: {} and company ID: {}", scope, companyId);
        logger.info("File name: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());
        
        // Validate company exists
        Company company;
        try {
            company = companyRepository.findById(companyId)
                    .orElseThrow(() -> new RuntimeException("Company not found with ID: " + companyId));
            logger.info("Found company: {}", company.getName());
        } catch (Exception e) {
            logger.error("Error finding company with ID {}: {}", companyId, e.getMessage());
            throw new RuntimeException("Company not found or database error", e);
        }
        
        // Get current user
        User currentUser;
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
            logger.info("Current user: {} (ID: {})", currentUser.getEmail(), currentUser.getId());
        } catch (Exception e) {
            logger.error("Error getting current user: {}", e.getMessage());
            throw new RuntimeException("Failed to get current user", e);
        }
        
        List<GHGEmission> emissions = new ArrayList<>();
        int recordCount = 0;
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            // Create CSV parser with header record
            CSVParser csvParser = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withIgnoreHeaderCase(true)
                    .withTrim()
                    .parse(reader);
            
            // Log headers for debugging
            Map<String, Integer> headerMap = csvParser.getHeaderMap();
            logger.info("CSV headers found: {}", headerMap.keySet());
            
            // Check required columns
            String[] requiredColumns = {"Start Date", "End Date", "Category", "Quantity", "Unit"};
            for (String column : requiredColumns) {
                boolean found = false;
                for (String header : headerMap.keySet()) {
                    if (header.equalsIgnoreCase(column)) {
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    String error = "Missing required column: " + column;
                    logger.error(error);
                    throw new IllegalArgumentException(error);
                }
            }
            
            // Process each record
            for (CSVRecord record : csvParser) {
                recordCount++;
                logger.debug("Processing record #{}", recordCount);
                
                try {
                    Map<String, String> recordMap = record.toMap();
                    logger.debug("Record data: {}", recordMap);
                    
                    GHGEmission emission = createEmissionFromCSV(recordMap, scope, company, currentUser);
                    
                    if (emission != null) {
                        emissions.add(emission);
                        logger.debug("Added emission to list, current count: {}", emissions.size());
                    } else {
                        logger.warn("Skipped record #{} due to null emission", recordCount);
                    }
                } catch (Exception e) {
                    logger.error("Error processing CSV record #{}: {}", recordCount, e.getMessage(), e);
                    // Continue processing other records
                }
            }
            
            logger.info("Processed {} records, saving {} valid emissions", recordCount, emissions.size());
            
            if (!emissions.isEmpty()) {
                try {
                    ghgEmissionRepository.saveAll(emissions);
                    logger.info("Successfully saved {} emissions to database", emissions.size());
                } catch (Exception e) {
                    logger.error("Error saving emissions to database: {}", e.getMessage(), e);
                    throw new RuntimeException("Failed to save emissions to database", e);
                }
            } else {
                logger.warn("No valid emissions found in CSV file");
            }
            
            return emissions.size();
        } catch (Exception e) {
            logger.error("Error processing CSV file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process CSV file: " + e.getMessage(), e);
        }
    }
    
    private GHGEmission createEmissionFromCSV(Map<String, String> record, EmissionScope scope, Company company, User currentUser) {
        try {
            logger.info("Creating emission from CSV record for scope: {}", scope);
            logger.debug("CSV record contents: {}", record);
            
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
            String startDateStr = record.get("Start Date");
            String endDateStr = record.get("End Date");
            
            logger.debug("Processing date fields - Start Date: {}, End Date: {}", startDateStr, endDateStr);
            
            LocalDate startDate = parseDate(startDateStr);
            LocalDate endDate = parseDate(endDateStr);
            
            if (startDate != null && endDate != null) {
                emission.setStartDate(startDate.atStartOfDay());
                emission.setEndDate(endDate.atTime(LocalTime.MAX));
                logger.debug("Set date range: {} to {}", emission.getStartDate(), emission.getEndDate());
            } else {
                logger.error("Invalid date format in CSV record - Start Date: {}, End Date: {}", startDateStr, endDateStr);
                return null;
            }
            
            // Set quantity and unit
            try {
                String quantityStr = record.get("Quantity");
                logger.debug("Processing quantity: {}", quantityStr);
                
                if (quantityStr != null && !quantityStr.isEmpty()) {
                    // Remove any commas, spaces or other non-numeric characters except decimal point
                    quantityStr = quantityStr.replaceAll("[^\\d.\\-]", "").trim();
                    if (!quantityStr.isEmpty()) {
                        emission.setQuantity(Double.parseDouble(quantityStr));
                        logger.debug("Set quantity to: {}", emission.getQuantity());
                    } else {
                        logger.warn("Quantity is empty after cleaning");
                        emission.setQuantity(0.0); // Default to 0
                    }
                } else {
                    logger.warn("Quantity field is missing or empty");
                    emission.setQuantity(0.0); // Default to 0
                }
            } catch (NumberFormatException e) {
                logger.error("Invalid quantity format in CSV record: {}", e.getMessage());
                emission.setQuantity(0.0); // Default to 0 on error
            }
            
            // Set unit with default if missing
            String unit = record.get("Unit");
            if (unit != null && !unit.trim().isEmpty()) {
                emission.setUnit(unit.trim());
            } else {
                logger.warn("Unit field is missing or empty, using default");
                emission.setUnit("unknown"); // Default unit
            }
            
            // Set category based on scope
            setCategory(emission, record, scope);
            
            // Set other fields if they exist in the CSV
            if (record.containsKey("Source")) {
                String source = record.get("Source");
                if (source != null && !source.trim().isEmpty()) {
                    emission.setSource(source.trim());
                }
            }
            
            if (record.containsKey("Activity")) {
                String activity = record.get("Activity");
                if (activity != null && !activity.trim().isEmpty()) {
                    emission.setActivity(activity.trim());
                }
            }
            
            if (record.containsKey("Calculation Method")) {
                String method = record.get("Calculation Method");
                if (method != null && !method.trim().isEmpty()) {
                    emission.setCalculationMethod(method.trim());
                }
            }
            
            // Process emission factor
            if (record.containsKey("Emission Factor")) {
                try {
                    String efStr = record.get("Emission Factor");
                    logger.debug("Processing emission factor: {}", efStr);
                    
                    if (efStr != null && !efStr.isEmpty()) {
                        // Remove any commas, spaces or other non-numeric characters except decimal point
                        efStr = efStr.replaceAll("[^\\d.\\-]", "").trim();
                        if (!efStr.isEmpty()) {
                            emission.setEmissionFactor(Double.parseDouble(efStr));
                            logger.debug("Set emission factor to: {}", emission.getEmissionFactor());
                        } else {
                            logger.warn("Emission factor is empty after cleaning");
                            emission.setEmissionFactor(0.0); // Default to 0
                        }
                    } else {
                        logger.warn("Emission factor field is missing or empty");
                        emission.setEmissionFactor(0.0); // Default to 0
                    }
                } catch (NumberFormatException e) {
                    logger.error("Invalid emission factor format in CSV record: {}", e.getMessage());
                    emission.setEmissionFactor(0.0); // Default to 0 on error
                }
            }
            
            // Set emission factor unit with default if missing
            if (record.containsKey("Emission Factor Unit")) {
                String efUnit = record.get("Emission Factor Unit");
                if (efUnit != null && !efUnit.trim().isEmpty()) {
                    emission.setEmissionFactorUnit(efUnit.trim());
                } else {
                    logger.warn("Emission factor unit is missing or empty, using default");
                    emission.setEmissionFactorUnit("kg CO2e"); // Default unit
                }
            }
            
            // Set notes if available
            if (record.containsKey("Notes")) {
                String notes = record.get("Notes");
                if (notes != null && !notes.trim().isEmpty()) {
                    emission.setNotes(notes.trim());
                }
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
            logger.warn("No Category field found in CSV record");
            // Set a default category based on scope
            switch (scope) {
                case SCOPE_1:
                    emission.setCategory(EmissionCategory.STATIONARY_COMBUSTION);
                    logger.info("Using default category STATIONARY_COMBUSTION for Scope 1");
                    break;
                case SCOPE_2:
                    emission.setCategory(EmissionCategory.PURCHASED_ELECTRICITY);
                    logger.info("Using default category PURCHASED_ELECTRICITY for Scope 2");
                    break;
                case SCOPE_3:
                    emission.setCategory(EmissionCategory.TRANSPORTATION_DISTRIBUTION);
                    logger.info("Using default category TRANSPORTATION_DISTRIBUTION for Scope 3");
                    break;
                case SOLVENT:
                    emission.setCategory(EmissionCategory.SOLVENT_CONSUMPTION);
                    logger.info("Using default category SOLVENT_CONSUMPTION for Solvent");
                    break;
                case SINK:
                    emission.setCategory(EmissionCategory.REFORESTATION);
                    logger.info("Using default category REFORESTATION for Sink");
                    break;
            }
            return;
        }
        
        // Normalize category string by removing spaces and converting to uppercase
        String normalizedCategory = categoryStr.trim().toUpperCase().replace(" ", "_");
        logger.debug("Normalized category: {} from original: {}", normalizedCategory, categoryStr);
        
        try {
            switch (scope) {
                case SCOPE_1:
                    if (categoryStr.equalsIgnoreCase("Stationary Combustion") || 
                        normalizedCategory.contains("STATIONARY")) {
                        emission.setCategory(EmissionCategory.STATIONARY_COMBUSTION);
                        logger.debug("Mapped to STATIONARY_COMBUSTION");
                    } else if (categoryStr.equalsIgnoreCase("Mobile Combustion") || 
                               normalizedCategory.contains("MOBILE")) {
                        emission.setCategory(EmissionCategory.MOBILE_COMBUSTION);
                        logger.debug("Mapped to MOBILE_COMBUSTION");
                    } else if (categoryStr.equalsIgnoreCase("Process Emissions") || 
                               normalizedCategory.contains("PROCESS")) {
                        emission.setCategory(EmissionCategory.PROCESS_EMISSIONS);
                        logger.debug("Mapped to PROCESS_EMISSIONS");
                    } else if (categoryStr.equalsIgnoreCase("Fugitive Emissions") || 
                               normalizedCategory.contains("FUGITIVE")) {
                        emission.setCategory(EmissionCategory.FUGITIVE_EMISSIONS);
                        logger.debug("Mapped to FUGITIVE_EMISSIONS");
                    } else {
                        // Default for unrecognized Scope 1 categories
                        emission.setCategory(EmissionCategory.STATIONARY_COMBUSTION);
                        logger.warn("Unrecognized Scope 1 category: {}, defaulting to STATIONARY_COMBUSTION", categoryStr);
                    }
                    break;
                    
                case SCOPE_2:
                    if (categoryStr.equalsIgnoreCase("Purchased Electricity") || 
                        normalizedCategory.contains("ELECTRICITY")) {
                        emission.setCategory(EmissionCategory.PURCHASED_ELECTRICITY);
                        logger.debug("Mapped to PURCHASED_ELECTRICITY");
                    } else if (categoryStr.equalsIgnoreCase("Purchased Heating") || 
                               normalizedCategory.contains("HEATING")) {
                        emission.setCategory(EmissionCategory.PURCHASED_HEATING);
                        logger.debug("Mapped to PURCHASED_HEATING");
                    } else if (categoryStr.equalsIgnoreCase("Purchased Cooling") || 
                               normalizedCategory.contains("COOLING")) {
                        emission.setCategory(EmissionCategory.PURCHASED_COOLING);
                        logger.debug("Mapped to PURCHASED_COOLING");
                    } else if (categoryStr.equalsIgnoreCase("Purchased Steam") || 
                               normalizedCategory.contains("STEAM")) {
                        emission.setCategory(EmissionCategory.PURCHASED_STEAM);
                        logger.debug("Mapped to PURCHASED_STEAM");
                    } else {
                        // Default for unrecognized Scope 2 categories
                        emission.setCategory(EmissionCategory.PURCHASED_ELECTRICITY);
                        logger.warn("Unrecognized Scope 2 category: {}, defaulting to PURCHASED_ELECTRICITY", categoryStr);
                    }
                    break;
                    
                case SCOPE_3:
                    if (categoryStr.equalsIgnoreCase("Purchased Goods and Services") || 
                        normalizedCategory.contains("PURCHASED_GOODS") || normalizedCategory.contains("GOODS_AND_SERVICES")) {
                        emission.setCategory(EmissionCategory.PURCHASED_GOODS_SERVICES);
                        logger.debug("Mapped to PURCHASED_GOODS_SERVICES");
                    } else if (categoryStr.equalsIgnoreCase("Capital Goods") || 
                               normalizedCategory.contains("CAPITAL")) {
                        emission.setCategory(EmissionCategory.CAPITAL_GOODS);
                        logger.debug("Mapped to CAPITAL_GOODS");
                    } else if (categoryStr.equalsIgnoreCase("Fuel and Energy Related Activities") || 
                               normalizedCategory.contains("FUEL") || normalizedCategory.contains("ENERGY_RELATED")) {
                        emission.setCategory(EmissionCategory.FUEL_ENERGY_ACTIVITIES);
                        logger.debug("Mapped to FUEL_ENERGY_ACTIVITIES");
                    } else if (categoryStr.equalsIgnoreCase("Transportation and Distribution") || 
                               normalizedCategory.contains("TRANSPORTATION") || normalizedCategory.contains("DISTRIBUTION")) {
                        emission.setCategory(EmissionCategory.TRANSPORTATION_DISTRIBUTION);
                        logger.debug("Mapped to TRANSPORTATION_DISTRIBUTION");
                    } else if (categoryStr.equalsIgnoreCase("Waste Generated") || 
                               normalizedCategory.contains("WASTE")) {
                        emission.setCategory(EmissionCategory.WASTE_GENERATED);
                        logger.debug("Mapped to WASTE_GENERATED");
                    } else if (categoryStr.equalsIgnoreCase("Business Travel") || 
                               normalizedCategory.contains("TRAVEL") || normalizedCategory.contains("BUSINESS_TRAVEL")) {
                        emission.setCategory(EmissionCategory.BUSINESS_TRAVEL);
                        logger.debug("Mapped to BUSINESS_TRAVEL");
                    } else if (categoryStr.equalsIgnoreCase("Employee Commuting") || 
                               normalizedCategory.contains("COMMUTING") || normalizedCategory.contains("EMPLOYEE")) {
                        emission.setCategory(EmissionCategory.EMPLOYEE_COMMUTING);
                        logger.debug("Mapped to EMPLOYEE_COMMUTING");
                    } else if (categoryStr.equalsIgnoreCase("Leased Assets") || 
                               normalizedCategory.contains("LEASED") || normalizedCategory.contains("ASSETS")) {
                        emission.setCategory(EmissionCategory.LEASED_ASSETS);
                        logger.debug("Mapped to LEASED_ASSETS");
                    } else if (categoryStr.equalsIgnoreCase("Investments") || 
                               normalizedCategory.contains("INVESTMENT")) {
                        emission.setCategory(EmissionCategory.INVESTMENTS);
                        logger.debug("Mapped to INVESTMENTS");
                    } else if (categoryStr.equalsIgnoreCase("Downstream Transportation") || 
                               normalizedCategory.contains("DOWNSTREAM")) {
                        emission.setCategory(EmissionCategory.DOWNSTREAM_TRANSPORTATION);
                        logger.debug("Mapped to DOWNSTREAM_TRANSPORTATION");
                    } else if (categoryStr.equalsIgnoreCase("Processing of Sold Products") || 
                               normalizedCategory.contains("PROCESSING") || normalizedCategory.contains("SOLD_PRODUCTS")) {
                        emission.setCategory(EmissionCategory.PROCESSING_SOLD_PRODUCTS);
                        logger.debug("Mapped to PROCESSING_SOLD_PRODUCTS");
                    } else if (categoryStr.equalsIgnoreCase("Use of Sold Products") || 
                               normalizedCategory.contains("USE_OF")) {
                        emission.setCategory(EmissionCategory.USE_OF_SOLD_PRODUCTS);
                        logger.debug("Mapped to USE_OF_SOLD_PRODUCTS");
                    } else if (categoryStr.equalsIgnoreCase("End of Life Treatment of Products") || 
                               normalizedCategory.contains("END_OF_LIFE") || normalizedCategory.contains("EOL")) {
                        emission.setCategory(EmissionCategory.END_OF_LIFE_PRODUCTS);
                        logger.debug("Mapped to END_OF_LIFE_PRODUCTS");
                    } else if (categoryStr.equalsIgnoreCase("Franchises") || 
                               normalizedCategory.contains("FRANCHISE")) {
                        emission.setCategory(EmissionCategory.FRANCHISES);
                        logger.debug("Mapped to FRANCHISES");
                    } else {
                        // Default for unrecognized Scope 3 categories
                        emission.setCategory(EmissionCategory.TRANSPORTATION_DISTRIBUTION);
                        logger.warn("Unrecognized Scope 3 category: {}, defaulting to TRANSPORTATION_DISTRIBUTION", categoryStr);
                    }
                    break;
                    
                case SOLVENT:
                    if (categoryStr.equalsIgnoreCase("Solvent Consumption") || 
                        normalizedCategory.contains("CONSUMPTION")) {
                        emission.setCategory(EmissionCategory.SOLVENT_CONSUMPTION);
                        logger.debug("Mapped to SOLVENT_CONSUMPTION");
                    } else if (categoryStr.equalsIgnoreCase("Solvent Recovery") || 
                               normalizedCategory.contains("RECOVERY")) {
                        emission.setCategory(EmissionCategory.SOLVENT_RECOVERY);
                        logger.debug("Mapped to SOLVENT_RECOVERY");
                    } else if (categoryStr.equalsIgnoreCase("Solvent Loss") || 
                               normalizedCategory.contains("LOSS")) {
                        emission.setCategory(EmissionCategory.SOLVENT_LOSS);
                        logger.debug("Mapped to SOLVENT_LOSS");
                    } else {
                        // Default for unrecognized Solvent categories
                        emission.setCategory(EmissionCategory.SOLVENT_CONSUMPTION);
                        logger.warn("Unrecognized Solvent category: {}, defaulting to SOLVENT_CONSUMPTION", categoryStr);
                    }
                    break;
                    
                case SINK:
                    if (categoryStr.equalsIgnoreCase("Reforestation") || 
                        normalizedCategory.contains("REFOREST")) {
                        emission.setCategory(EmissionCategory.REFORESTATION);
                        logger.debug("Mapped to REFORESTATION");
                    } else if (categoryStr.equalsIgnoreCase("Afforestation") || 
                               normalizedCategory.contains("AFFOREST")) {
                        emission.setCategory(EmissionCategory.AFFORESTATION);
                        logger.debug("Mapped to AFFORESTATION");
                    } else if (categoryStr.equalsIgnoreCase("Soil Carbon Sequestration") || 
                               normalizedCategory.contains("SOIL") || normalizedCategory.contains("SEQUESTRATION")) {
                        emission.setCategory(EmissionCategory.SOIL_CARBON_SEQUESTRATION);
                        logger.debug("Mapped to SOIL_CARBON_SEQUESTRATION");
                    } else {
                        // Default for unrecognized Sink categories
                        emission.setCategory(EmissionCategory.REFORESTATION);
                        logger.warn("Unrecognized Sink category: {}, defaulting to REFORESTATION", categoryStr);
                    }
                    break;
            }
        } catch (Exception e) {
            logger.error("Error setting category from CSV: {}", e.getMessage(), e);
        }
    }
    
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            logger.warn("Empty date string received");
            return null;
        }
        
        // Trim the date string to remove any whitespace
        dateStr = dateStr.trim();
        logger.debug("Attempting to parse date: {}", dateStr);
        
        // Try different date formats
        DateTimeFormatter[] formatters = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
        };
        
        for (DateTimeFormatter formatter : formatters) {
            try {
                LocalDate date = LocalDate.parse(dateStr, formatter);
                logger.debug("Successfully parsed date {} with pattern {}", dateStr, formatter);
                return date;
            } catch (DateTimeParseException e) {
                // Try next format
                logger.debug("Failed to parse date with pattern {}: {}", formatter, e.getMessage());
            }
        }
        
        // If all formatters fail, try a more lenient approach
        try {
            // Try to parse as ISO date (yyyy-MM-dd)
            if (dateStr.contains("-")) {
                String[] parts = dateStr.split("-");
                if (parts.length == 3) {
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    int day = Integer.parseInt(parts[2]);
                    if (year > 1900 && month >= 1 && month <= 12 && day >= 1 && day <= 31) {
                        LocalDate date = LocalDate.of(year, month, day);
                        logger.debug("Parsed date using manual approach: {}", date);
                        return date;
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Manual date parsing failed: {}", e.getMessage());
        }
        
        logger.error("Could not parse date: {}", dateStr);
        return null;
    }
}
