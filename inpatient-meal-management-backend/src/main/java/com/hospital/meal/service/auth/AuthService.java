package com.hospital.meal.service.auth;

import com.hospital.meal.dto.auth.*;

public interface AuthService {

    /**
     * Authenticate staff (Admin, Dietician, Kitchen Staff) with email and password
     */
    LoginResponse login(LoginRequest request);

    /**
     * Authenticate patient with UHID, name, and room number (no password)
     */
    PatientSessionResponse patientLogin(PatientLoginRequest request, String ipAddress, String userAgent);

    /**
     * Set password for dietician using invite token
     */
    void setPassword(SetPasswordRequest request);

    /**
     * Validate patient session token
     */
    boolean validatePatientSession(String token);

    /**
     * Logout patient by invalidating session
     */
    void logoutPatient(String token);

    /**
     * Send password reset email to staff member
     */
    void forgotPassword(ForgotPasswordRequest request);

    /**
     * Reset password using token
     */
    void resetPassword(ResetPasswordRequest request);

    /**
     * Validate password reset token
     */
    boolean validateResetToken(String token);

}