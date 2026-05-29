package com.hospital.meal.security;

import com.hospital.meal.constant.ApiConstants;
import com.hospital.meal.constant.RoleConstants;
import com.hospital.meal.security.jwt.JwtAuthenticationFilter;
import com.hospital.meal.security.ratelimit.RateLimitFilter;
import com.hospital.meal.security.session.PatientSessionFilter;
import com.hospital.meal.security.userdetails.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final PatientSessionFilter patientSessionFilter;
    private final RateLimitFilter rateLimitFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configure(http))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                ApiConstants.AUTH_BASE + ApiConstants.AUTH_LOGIN,
                                ApiConstants.AUTH_BASE + ApiConstants.AUTH_PATIENT_LOGIN,
                                ApiConstants.AUTH_BASE + "/logout",
                                ApiConstants.WEB_PUSH_BASE + "/public-key",
                                ApiConstants.AUTH_BASE + ApiConstants.AUTH_SET_PASSWORD,
                                ApiConstants.AUTH_BASE + ApiConstants.AUTH_FORGOT_PASSWORD,      // ← ADD
                                ApiConstants.AUTH_BASE + ApiConstants.AUTH_RESET_PASSWORD,       // ← ADD
                                ApiConstants.AUTH_BASE + ApiConstants.AUTH_VALIDATE_RESET_TOKEN,
                                ApiConstants.AUTH_BASE + "/dev/hash",
                                ApiConstants.AUTH_BASE + "/his-mode",
                                "/actuator/health"
                        ).permitAll()

                        // Swagger/API Docs - ADMIN ONLY
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).hasRole(RoleConstants.ADMIN)

                        // Admin endpoints
                        .requestMatchers(ApiConstants.ADMIN_BASE + "/**")
                        .hasRole(RoleConstants.ADMIN)

                        // Dietician endpoints
                        .requestMatchers(ApiConstants.DIETICIAN_BASE + "/**")
                        .hasRole(RoleConstants.DIETICIAN)

                        // Kitchen endpoints
                        .requestMatchers(ApiConstants.KITCHEN_BASE + "/**")
                        .hasRole(RoleConstants.KITCHEN_STAFF)

                        // Notification endpoints
                        .requestMatchers(ApiConstants.WEB_PUSH_BASE + "/subscribe")
                        .authenticated()

                        .requestMatchers(ApiConstants.WEB_PUSH_BASE + "/unsubscribe")
                        .authenticated()

                        // Patient endpoints (handled by PatientSessionFilter)
                        .requestMatchers(ApiConstants.PATIENT_BASE + "/**")
                        .permitAll()

                        // All other requests must be authenticated
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider());

        // Add filters in order
        http.addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(patientSessionFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}