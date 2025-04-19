package com.esgframework.controllers;

import com.esgframework.models.User;
import com.esgframework.models.Company;
import com.esgframework.repositories.UserRepository;
import com.esgframework.repositories.CompanyRepository;

import com.esgframework.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            org.springframework.security.core.userdetails.UserDetails userDetails =
                    (org.springframework.security.core.userdetails.UserDetails) principal;

            // Assuming your username is the email or unique identifier
            String username = userDetails.getUsername();
            // Fetch your User entity from the database
            User user = userRepository.findByEmail(username)
                    .orElse(null);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            // Build a response map (customize as needed)
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("name", user.getName());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
        }
    }
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);


    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtTokenProvider tokenProvider;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;



    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            logger.debug("Received login request for user: {}", loginRequest.getEmail());
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);
            System.out.println(jwt + "     Pujan Patel");
            logger.debug("Successfully generated JWT token for user: {}", loginRequest.getEmail());
       
            User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Get role and ensure it's consistent
            String role = user.getRole();
            logger.info("User role from database: {}", role);
            
            // Create a user object with only necessary fields to avoid circular references
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("name", user.getName());
            userData.put("email", user.getEmail());
            userData.put("role", role);
            
            // Add company information if available
            if (user.getCompany() != null) {
                Map<String, Object> companyData = new HashMap<>();
                companyData.put("id", user.getCompany().getId());
                companyData.put("name", user.getCompany().getName());
                userData.put("company", companyData);
            }
            
            System.out.println("User logged in successfully: " + user.getEmail() + ", with role: " + role);
            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("user", userData);
            response.put("role", role); // Explicit role for easier client-side access
            
            logger.info("User logged in successfully: {}, with role: {}", user.getEmail(), role);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Login failed for user: {}, error: {}", loginRequest.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        try {
            logger.info("Processing signup request for user: {} with role: {}", 
                signUpRequest.getEmail(), signUpRequest.getRole());

            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                logger.warn("Signup failed: Email already exists: {}", signUpRequest.getEmail());
                return ResponseEntity.badRequest().body("Email is already taken!");            }

            // Validate company information for representatives
            if ("REPRESENTATIVE".equalsIgnoreCase(signUpRequest.getRole())) {
                if (signUpRequest.getCompanyName() == null || signUpRequest.getCompanyName().trim().isEmpty()) {
                    logger.warn("Signup failed: Company name is required for representatives");
                    return ResponseEntity.badRequest().body("Company name is required for representatives");
                }
            }

            User user = new User();
            user.setName(signUpRequest.getName());
            user.setEmail(signUpRequest.getEmail());
            user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
            user.setRole(signUpRequest.getRole());

            // Handle company creation and association for representatives
            if ("REPRESENTATIVE".equalsIgnoreCase(signUpRequest.getRole())) {
                logger.info("Processing company information for representative: {}", signUpRequest.getCompanyName());
                
                // Check if company exists
                Company company = companyRepository.findByName(signUpRequest.getCompanyName())
                    .orElseGet(() -> {
                        // Create new company if it doesn't exist
                        logger.info("Creating new company: {}", signUpRequest.getCompanyName());
                        Company newCompany = new Company();
                        newCompany.setName(signUpRequest.getCompanyName());
                        newCompany.setIndustry(signUpRequest.getCompanyIndustry());
                        newCompany.setStatus("active");
                        return companyRepository.save(newCompany);
                    });
                
                user.setCompany(company);
                logger.info("Associated user with company ID: {}", company.getId());
            }

            User result = userRepository.save(user);
            logger.info("User registered successfully with ID: {}", result.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            
            // Create a clean user object for response
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", result.getId());
            userData.put("name", result.getName());
            userData.put("email", result.getEmail());
            userData.put("role", result.getRole());
            
            if (result.getCompany() != null) {
                Map<String, Object> companyData = new HashMap<>();
                companyData.put("id", result.getCompany().getId());
                companyData.put("name", result.getCompany().getName());
                userData.put("company", companyData);
            }
            
            response.put("user", userData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error during user registration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error during registration: " + e.getMessage());
        }
    }
}
