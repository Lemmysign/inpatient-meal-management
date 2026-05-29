package com.hospital.meal.service.notification;

import java.util.UUID;

public interface EmailService {



    /**
     * Send dietician invitation email
     */
    void sendDieticianInvite(String email, String name, String inviteToken);

    /**
     * Send meal order notification to dietician
     */
    void sendMealOrderNotification(UUID dieticianId, String patientName, String uhid);

    /**
     * Send password reset email
     */
    void sendPasswordResetEmail(String email, String userName, String resetLink);
}