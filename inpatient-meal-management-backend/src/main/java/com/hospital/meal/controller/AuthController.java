package com.hospital.meal.controller;

import com.hospital.meal.constant.ApiConstants;
import com.hospital.meal.dto.auth.*;
import com.hospital.meal.dto.common.ApiResponse;
import com.hospital.meal.model.config.HISIntegrationSetting;
import com.hospital.meal.repository.HISIntegrationSettingRepository;
import com.hospital.meal.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.hospital.meal.constant.ApiConstants.AUTH_BASE;

@RestController
@RequestMapping(AUTH_BASE)
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Authentication", description = "Authentication endpoints for all user types")
public class AuthController {

    private final AuthService authService;
    private final HISIntegrationSettingRepository hisSettingRepository;

    // NOTE: PasswordEncoder and /dev/hash removed — exposed BCrypt publicly (security risk)

    @GetMapping("/his-mode")
    @Operation(
            summary = "HIS Mode",
            description = "Check if HIS integration is enabled"
    )
    public ResponseEntity<ApiResponse<Boolean>> getHISMode() {
        boolean enabled = hisSettingRepository.findById(1L)
                .map(HISIntegrationSetting::isEnabled)
                .orElse(false);
        return ResponseEntity.ok(ApiResponse.success(enabled));
    }

    /**
     * Login endpoint for staff (Admin, Dietician, Kitchen Staff)
     * Rate limiting handled transparently by RateLimitFilter
     */
    @PostMapping(ApiConstants.AUTH_LOGIN)
    @Operation(
            summary = "Staff Login",
            description = "Authenticate admin, dietician, or kitchen staff with email and password"
    )
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("Login request received for email: {}", request.getEmail());

        LoginResponse response = authService.login(request);

        log.info("User {} logged in successfully with role: {}",
                request.getEmail(), response.getRole());

        return ResponseEntity.ok(
                ApiResponse.success("Login successful", response)
        );
    }

    /**
     * Login endpoint for patients (no password required)
     * Rate limiting handled transparently by RateLimitFilter
     */
    @PostMapping(ApiConstants.AUTH_PATIENT_LOGIN)
    @Operation(
            summary = "Patient Login",
            description = "Authenticate patient using UHID, name, and room number (no password required)"
    )
    public ResponseEntity<ApiResponse<PatientSessionResponse>> patientLogin(
            @Valid @RequestBody PatientLoginRequest request,
            HttpServletRequest httpRequest) {

        log.info("Patient login request received for UHID: {}", request.getUhid());

        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        PatientSessionResponse response = authService.patientLogin(request, ipAddress, userAgent);

        log.info("Patient {} logged in successfully", request.getUhid());

        return ResponseEntity.ok(
                ApiResponse.success("Patient login successful", response)
        );
    }

    /**
     * Set password for dietician using invite token
     */
    @PostMapping(ApiConstants.AUTH_SET_PASSWORD)
    @Operation(
            summary = "Set Password",
            description = "Set password for dietician account using invitation token"
    )
    public ResponseEntity<ApiResponse<Void>> setPassword(
            @Valid @RequestBody SetPasswordRequest request) {

        log.info("Set password request received");

        authService.setPassword(request);

        log.info("Password set successfully");

        return ResponseEntity.ok(
                ApiResponse.success("Password set successfully. You can now log in.", null)
        );
    }

    /**
     * Logout endpoint
     */
    @PostMapping(ApiConstants.AUTH_LOGOUT)
    @Operation(
            summary = "Logout",
            description = "Logout and invalidate session"
    )
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = ApiConstants.AUTHORIZATION_HEADER, required = false) String authHeader) {

        log.info("Logout request received");

        if (authHeader != null && authHeader.startsWith(ApiConstants.BEARER_PREFIX)) {
            String token = authHeader.substring(ApiConstants.BEARER_PREFIX.length());
            authService.logoutPatient(token);
        }

        return ResponseEntity.ok(
                ApiResponse.success("Logged out successfully", null)
        );
    }

    /**
     * Validate session endpoint
     */
    @GetMapping("/validate-session")
    @Operation(
            summary = "Validate Session",
            description = "Check if the current session token is valid"
    )
    public ResponseEntity<ApiResponse<Boolean>> validateSession(
            @RequestHeader(value = ApiConstants.AUTHORIZATION_HEADER, required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith(ApiConstants.BEARER_PREFIX)) {
            return ResponseEntity.ok(ApiResponse.success("Session invalid", false));
        }

        String token = authHeader.substring(ApiConstants.BEARER_PREFIX.length());
        boolean isValid = authService.validatePatientSession(token);

        return ResponseEntity.ok(
                ApiResponse.success(isValid ? "Session valid" : "Session invalid", isValid)
        );
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    @Operation(
            summary = "Health Check",
            description = "Check if authentication service is running"
    )
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success("Authentication service is running", "OK")
        );
    }

    /**
     * Request password reset email
     * Rate limiting handled transparently by RateLimitFilter (3/min)
     */
    @PostMapping(ApiConstants.AUTH_FORGOT_PASSWORD)
    @Operation(
            summary = "Forgot Password",
            description = "Request password reset email for staff members"
    )
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        log.info("Forgot password request received for email: {}", request.getEmail());

        authService.forgotPassword(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "If an account exists with this email, you will receive password reset instructions.",
                        null
                )
        );
    }

    /**
     * Reset password using token
     * Rate limiting handled transparently by RateLimitFilter (5/min)
     */
    @PostMapping(ApiConstants.AUTH_RESET_PASSWORD)
    @Operation(
            summary = "Reset Password",
            description = "Reset password using token from email"
    )
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        log.info("Reset password request received");

        authService.resetPassword(request);

        return ResponseEntity.ok(
                ApiResponse.success("Password reset successful. You can now log in with your new password.", null)
        );
    }

    /**
     * Validate reset token
     */
    @GetMapping(ApiConstants.AUTH_VALIDATE_RESET_TOKEN)
    @Operation(
            summary = "Validate Reset Token",
            description = "Check if password reset token is valid"
    )
    public ResponseEntity<ApiResponse<Boolean>> validateResetToken(
            @RequestParam String token) {

        log.info("Validate reset token request");

        boolean isValid = authService.validateResetToken(token);

        return ResponseEntity.ok(
                ApiResponse.success(
                        isValid ? "Token is valid" : "Token is invalid or expired",
                        isValid
                )
        );
    }

    /**
     * Extract client IP — used for audit logging in patientLogin only.
     * Rate limiting uses RateLimitFilter's own IP extraction independently.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.contains(",") ? ip.split(",")[0].trim() : ip;
            }
        }

        return request.getRemoteAddr();
    }
}