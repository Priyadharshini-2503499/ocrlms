package com.genc.omnichannel.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // enables @PreAuthorize / @PostAuthorize on controller methods
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public — login endpoint + static frontend files
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/index.html", "/login.html", "/login.js").permitAll()
                .requestMatchers("/assets/**").permitAll()
                .requestMatchers("/*.html", "/*.js", "/*.css").permitAll()
                .requestMatchers("/api/promotions/coupons/tier/**").permitAll()

                // Role-based access to API endpoints
                .requestMatchers("/products/**").hasAnyRole("ADMIN", "STORE_MANAGER", "MERCHANDISER")
                .requestMatchers("/api/orders/**").hasAnyRole("ADMIN", "STORE_MANAGER", "CUSTOMER_SERVICE")
                .requestMatchers("/api/customers/**").hasAnyRole("ADMIN", "CUSTOMER_SERVICE")
                // Promotions: read + apply allowed for ADMIN and MARKETING_MANAGER;
                // write operations (create/update/delete) are further restricted by @PreAuthorize
                .requestMatchers("/api/promotions/**").hasAnyRole("ADMIN", "MARKETING_MANAGER")
                .requestMatchers("/api/returns/**").hasAnyRole("ADMIN", "STORE_MANAGER", "CUSTOMER_SERVICE")
                .requestMatchers("/api/auth/users/**").hasRole("ADMIN")

                // Static pages for modules (HTML/JS/CSS served from static/)
                .requestMatchers("/productcatalog/**").permitAll()
                .requestMatchers("/order-management/**").permitAll()
                .requestMatchers("/loyalty/**").permitAll()
                .requestMatchers("/promotion/**", "/promotion").permitAll()
                .requestMatchers("/returns/**").permitAll()
                .requestMatchers("/admin/**").permitAll()

                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
