package com.hospital.meal.service.service_impl.auth;
import com.hospital.meal.model.config.HISIntegrationSetting;
import com.hospital.meal.repository.HISIntegrationSettingRepository;
import com.hospital.meal.constant.RoleConstants;
import com.hospital.meal.dto.auth.*;
import com.hospital.meal.exception.InvalidRequestException;
import com.hospital.meal.exception.InviteExpiredException;
import com.hospital.meal.exception.ResourceNotFoundException;
import com.hospital.meal.exception.UnauthorizedException;
import com.hospital.meal.model.user.*;
import com.hospital.meal.repository.*;
import com.hospital.meal.security.jwt.JwtTokenProvider;
import com.hospital.meal.security.userdetails.UserPrincipal;
import com.hospital.meal.service.audit.AuditLogService;
import com.hospital.meal.service.auth.AuthService;
import com.hospital.meal.service.auth.PatientSessionService;
import com.hospital.meal.service.notification.EmailService;
import com.hospital.meal.service.patient.PatientInfoProvider;
import com.hospital.meal.service.patient.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospital.meal.service.patient.PatientInfoProvider;
import com.hospital.meal.dto.patient.PatientInfo;
import com.hospital.meal.util.ValidationUtil;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PatientService patientService;
    private final PatientSessionService patientSessionService;
    private final DieticianRepository dieticianRepository;
    private final DieticianInviteRepository dieticianInviteRepository;
    private final PasswordEncoder passwordEncoder;
    private  final AuditLogService auditLogService;
    private final KitchenStaffRepository kitchenStaffRepository;
    private final AdminRepository adminRepository;
    private final PatientRepository patientRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final PatientInfoProvider patientInfoProvider;
    private final HISIntegrationSettingRepository hisSettingRepository;


    @Value("${app.frontend-url:http://10.20.20.55:8086}")
    private String frontendBaseUrl;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        if (!userPrincipal.isActive()) {
            throw new UnauthorizedException("Account is disabled. Please contact administrator.");
        }

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(authentication);
        Date expiryDate = jwtTokenProvider.getExpirationDateFromToken(token);
        long expiresIn = expiryDate.getTime() - System.currentTimeMillis();

        // Log login
        auditLogService.logLogin(userPrincipal.getRole(), userPrincipal.getId(),
                userPrincipal.getName(), null);

        log.info("User {} logged in successfully with role {}", request.getEmail(), userPrincipal.getRole());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .userId(userPrincipal.getId())
                .name(userPrincipal.getName())
                .email(userPrincipal.getEmail())
                .role(userPrincipal.getRole())
                .build();
    }

    @Override
    @Transactional
    public PatientSessionResponse patientLogin(PatientLoginRequest request,
                                               String ipAddress, String userAgent) {
        log.info("Patient login attempt for UHID: {}", request.getUhid());

        String name;
        String roomNumber;

        // Check which mode is active
        boolean hisEnabled = hisSettingRepository.findById(1L)
                .map(HISIntegrationSetting::isEnabled)
                .orElse(false);

        if (hisEnabled) {
            // HIS mode — fetch from scraper, never fall back to manual
            log.info("HIS mode active — fetching patient info from HIS for UHID: {}",
                    request.getUhid());

            PatientInfo hisInfo = patientInfoProvider.getPatientInfo(request.getUhid());

            if (hisInfo == null) {
                // Patient not found in HIS — do NOT fall back to manual
                log.warn("Patient not found in HIS for UHID: {}", request.getUhid());
                throw new ResourceNotFoundException(
                        "Patient", "UHID", request.getUhid() +
                        ". Please confirm you are currently admitted and try again.");
            }

            if (hisInfo.getFullName() == null || hisInfo.getRoomNumber() == null) {
                log.error("HIS returned incomplete patient data for UHID: {}", request.getUhid());
                throw new InvalidRequestException(
                        "Patient information is incomplete in the hospital system. " +
                                "Please contact the front desk.");
            }

            name = hisInfo.getFullName();
            roomNumber = hisInfo.getRoomNumber();
            log.info("HIS data fetched for UHID: {} — name: {}, room: {}",
                    request.getUhid(), name, roomNumber);

        } else {
            // Manual mode — patient must provide their own details
            log.info("Manual mode active — using patient-provided info for UHID: {}",
                    request.getUhid());

            if (ValidationUtil.isBlank(request.getName())) {
                throw new InvalidRequestException("Patient name is required.");
            }
            if (ValidationUtil.isBlank(request.getRoomNumber())) {
                throw new InvalidRequestException("Room number is required.");
            }

            name = request.getName();
            roomNumber = request.getRoomNumber();
        }

        // Get or create patient
        Patient patient = patientService.getOrCreatePatient(
                request.getUhid(), name, roomNumber);

        // Check if patient has active menu
        boolean hasActiveMenu = patientService.hasActiveMenu(patient.getUhid());

        // Create session
        PatientSession session = patientSessionService.createSession(
                patient, ipAddress, userAgent);

        // Log patient login
        auditLogService.logLogin(RoleConstants.PATIENT, patient.getId(),
                patient.getName(), ipAddress);

        log.info("Patient {} logged in successfully", patient.getUhid());

        return PatientSessionResponse.builder()
                .sessionToken(session.getSessionToken())
                .patientId(patient.getId())
                .uhid(patient.getUhid())
                .name(patient.getName())
                .roomNumber(patient.getRoomNumber())
                .expiresAt(session.getExpiresAt())
                .hasActiveMenu(hasActiveMenu)
                .build();
    }

    @Override
    @Transactional
    public void setPassword(SetPasswordRequest request) {
        log.info("Setting password for invite token");

        // Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new InvalidRequestException("Passwords do not match");
        }

        // Find valid invite
        DieticianInvite invite = dieticianInviteRepository
                .findValidInvite(request.getToken(), LocalDateTime.now())
                .orElseThrow(() -> new InviteExpiredException("Invalid or expired invitation token"));

        // Get dietician
        Dietician dietician = invite.getDietician();

        // Set password
        dietician.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        dieticianRepository.save(dietician);

        // Mark invite as used
        invite.setUsedAt(LocalDateTime.now());
        dieticianInviteRepository.save(invite);

        log.info("Password set successfully for dietician: {}", dietician.getEmail());
    }

    @Override
    public boolean validatePatientSession(String token) {
        return patientSessionService.getValidSession(token).isPresent();
    }

    @Override
    @Transactional
    public void logoutPatient(String token) {
        log.info("Patient logout");
        patientSessionService.invalidateSession(token);
    }


    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        log.info("Password reset request for email: {}", request.getEmail());

        String email = request.getEmail();
        String userType = null;
        String userName = null;

        // Find user in Admin, Dietician, or Kitchen Staff tables
        Optional<Admin> admin = adminRepository.findByEmail(email);
        if (admin.isPresent()) {
            userType = RoleConstants.ADMIN;
            userName = admin.get().getName();
        } else {
            Optional<Dietician> dietician = dieticianRepository.findByEmail(email);
            if (dietician.isPresent()) {
                userType = RoleConstants.DIETICIAN;
                userName = dietician.get().getName();
            } else {
                Optional<KitchenStaff> staff = kitchenStaffRepository.findByEmail(email);
                if (staff.isPresent()) {
                    userType = RoleConstants.KITCHEN_STAFF;
                    userName = staff.get().getName();
                }
            }
        }

        // Don't reveal if user exists or not (security best practice)
        if (userType == null) {
            log.warn("Password reset requested for non-existent email: {}", email);
            // Still return success to prevent email enumeration
            return;
        }

        // Invalidate any existing tokens for this user
        passwordResetTokenRepository.invalidateAllTokensForUser(email, userType);

        // Generate new token (UUID)
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1); // 1 hour expiry

        // Save token
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .email(email)
                .userType(userType)
                .token(token)
                .expiresAt(expiresAt)
                .isValid(true)
                .build();

        passwordResetTokenRepository.save(resetToken);

        // Send email with reset link
        String resetLink = frontendBaseUrl + "/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(email, userName, resetLink);

        log.info("Password reset email sent to: {}", email);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Processing password reset");

        // Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new InvalidRequestException("Passwords do not match");
        }

        // Find and validate token
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findValidToken(request.getToken(), LocalDateTime.now())
                .orElseThrow(() -> new InvalidRequestException("Invalid or expired reset token"));

        // Update password based on user type
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        switch (resetToken.getUserType()) {
            case RoleConstants.ADMIN -> {
                Admin admin = adminRepository.findByEmail(resetToken.getEmail())
                        .orElseThrow(() -> new ResourceNotFoundException("Admin", "email", resetToken.getEmail()));
                admin.setPasswordHash(hashedPassword);
                adminRepository.save(admin);
            }
            case RoleConstants.DIETICIAN -> {
                Dietician dietician = dieticianRepository.findByEmail(resetToken.getEmail())
                        .orElseThrow(() -> new ResourceNotFoundException("Dietician", "email", resetToken.getEmail()));
                dietician.setPasswordHash(hashedPassword);
                dieticianRepository.save(dietician);
            }
            case RoleConstants.KITCHEN_STAFF -> {
                KitchenStaff staff = kitchenStaffRepository.findByEmail(resetToken.getEmail())
                        .orElseThrow(() -> new ResourceNotFoundException("Kitchen Staff", "email", resetToken.getEmail()));
                staff.setPasswordHash(hashedPassword);
                kitchenStaffRepository.save(staff);
            }
            default -> throw new InvalidRequestException("Invalid user type");
        }

        // Mark token as used
        resetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(resetToken);

        log.info("Password reset successful for: {}", resetToken.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateResetToken(String token) {
        return passwordResetTokenRepository
                .findValidToken(token, LocalDateTime.now())
                .isPresent();
    }


}

