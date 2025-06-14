package com.esgframework.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.esgframework.security.JwtAuthenticationFilter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors().configurationSource(corsConfigurationSource()).and()
            .csrf().disable()
            .authorizeHttpRequests(requests -> requests
                .requestMatchers(new AntPathRequestMatcher("/api/auth/login")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/auth/signup")).hasAuthority("ROLE_admin")
                .requestMatchers(new AntPathRequestMatcher("/api/companies")).hasAnyAuthority("ROLE_admin", "ROLE_manager")
                .requestMatchers(new AntPathRequestMatcher("/api/companies/**")).hasAnyAuthority("ROLE_admin", "ROLE_manager")
                .requestMatchers(new AntPathRequestMatcher("/api/users/notifications")).hasAnyAuthority("ROLE_admin", "ROLE_manager", "ROLE_representative")
                .requestMatchers(new AntPathRequestMatcher("/api/users/notifications/**")).hasAnyAuthority("ROLE_admin", "ROLE_manager", "ROLE_representative")
                .requestMatchers(new AntPathRequestMatcher("/api/users/**")).hasAuthority("ROLE_admin")
                .requestMatchers(new AntPathRequestMatcher("/api/esg/submissions")).hasAnyAuthority("ROLE_admin", "ROLE_manager", "ROLE_representative")
                .requestMatchers(new AntPathRequestMatcher("/api/esg/**")).hasAnyAuthority("ROLE_admin", "ROLE_manager")
                .requestMatchers(new AntPathRequestMatcher("/api/ghg-emissions")).hasAnyAuthority("ROLE_admin", "ROLE_manager", "ROLE_representative")
                .requestMatchers(new AntPathRequestMatcher("/api/ghg-emissions/**")).hasAnyAuthority("ROLE_admin", "ROLE_manager", "ROLE_representative")
                // Swagger UI and OpenAPI endpoints
                .requestMatchers(new AntPathRequestMatcher("/")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api-docs/**")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/v3/api-docs/**")).permitAll()
                .anyRequest().authenticated())
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword.toString();
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return rawPassword.toString().equals(encodedPassword);
            }
        };
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Instead of wildcard, specify your frontend origins
        // For development, you might want to include multiple origins
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:5173",
            "http://frontend:3000",
            "http://esgaadhar-frontend:3000",
            "https://dashboard.esgaadhar.com",
            "https://api.esgaadhar.com"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "Accept", 
            "X-Requested-With", 
            "Cache-Control"
        ));
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Disposition"
        ));
        // Allow cookies and credentials
        configuration.setAllowCredentials(true);
        // Cache preflight requests for 1 hour
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
