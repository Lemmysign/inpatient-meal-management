package com.hospital.meal.service.service_impl.notification;

import com.hospital.meal.model.user.Dietician;
import com.hospital.meal.repository.DieticianRepository;
import com.hospital.meal.service.notification.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;


import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final DieticianRepository dieticianRepository;

    @Value("${spring.mail.username:noreply@evercare.ng}")
    private String fromEmail;

    @Value("${app.frontend-url:http://10.20.20.55:8086}")
    private String frontendUrl;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;



    @Override
    @Async
    public void sendDieticianInvite(String email, String name, String inviteToken) {
        log.info("Sending dietician invite to: {}", email);

        if (!emailEnabled) {
            log.warn("Email service is disabled. Skipping invite email to: {}", email);
            return;
        }

        try {
            String inviteLink = frontendUrl + "/set-password?token=" + inviteToken;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Welcome to Hospital Meal Ordering System - Set Your Password");
            helper.setText(buildDieticianInviteEmail(name, inviteLink), true);

            mailSender.send(message);
            log.info("Dietician invite email sent successfully to: {}", email);

        } catch (MessagingException e) {
            log.error("Failed to send dietician invite email to: {}", email, e);
        } catch (Exception e) {
            log.error("Unexpected error sending invite email to: {}", email, e);
        }
    }

    @Override
    @Async
    public void sendMealOrderNotification(UUID dieticianId, String patientName, String uhid) {
        log.info("Sending meal order notification for dietician: {}", dieticianId);

        if (!emailEnabled) {
            log.warn("Email service is disabled. Skipping meal order notification for dietician: {}", dieticianId);
            return;
        }

        try {
            Dietician dietician = dieticianRepository.findById(dieticianId).orElse(null);
            if (dietician == null) {
                log.warn("Dietician not found with id: {}", dieticianId);
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(dietician.getEmail());
            helper.setSubject("New Meal Order Placed - " + patientName);
            helper.setText(buildMealOrderNotificationEmail(dietician.getName(), patientName, uhid), true);

            mailSender.send(message);
            log.info("Meal order notification sent successfully to: {}", dietician.getEmail());

        } catch (MessagingException e) {
            log.error("Failed to send meal order notification to dietician: {}", dieticianId, e);
        } catch (Exception e) {
            log.error("Unexpected error sending meal order notification to dietician: {}", dieticianId, e);
        }
    }


    @Async
    public void sendSimpleEmail(String to, String subject, String text) {
        log.info("Sending simple email to: {}", to);

        if (!emailEnabled) {
            log.warn("Email service is disabled. Skipping simple email to: {}", to);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("Simple email sent successfully to: {}", to);

        } catch (Exception e) {
            log.error("Failed to send simple email to: {}", to, e);
        }
    }

    // ============================================
    // EMAIL TEMPLATES
    // ============================================

    private String buildDieticianInviteEmail(String name, String inviteLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .button { display: inline-block; padding: 12px 24px; background-color: #4CAF50;
                              color: white; text-decoration: none; border-radius: 4px; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; color: #777; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Welcome to Hospital Meal Ordering System</h1>
                    </div>
                    <div class="content">
                        <p>Hello %s,</p>
                        <p>You have been invited to join the Hospital Meal Ordering System as a Dietician.</p>
                        <p>Please click the button below to set your password and activate your account:</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Set Password</a>
                        </p>
                        <p>Or copy and paste this link in your browser:</p>
                        <p style="word-break: break-all; color: #4CAF50;">%s</p>
                        <p><strong>This link will expire in 15 minutes.</strong></p>
                        <p>If you did not expect this invitation, please ignore this email.</p>
                        <p>Best regards,<br>Hospital Administration</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated message. Please do not reply to this email.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(name, inviteLink, inviteLink);
    }

    private String buildMealOrderNotificationEmail(String dieticianName, String patientName, String uhid) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #2196F3; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .info-box { background-color: white; border-left: 4px solid #2196F3; padding: 15px; margin: 15px 0; }
                    .footer { text-align: center; padding: 20px; color: #777; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>New Meal Order Notification</h1>
                    </div>
                    <div class="content">
                        <p>Hello %s,</p>
                        <p>A patient under your care has placed a new meal order.</p>
                        <div class="info-box">
                            <p><strong>Patient Name:</strong> %s</p>
                            <p><strong>UHID:</strong> %s</p>
                            <p><strong>Order Time:</strong> Just now</p>
                        </div>
                        <p>You can view the complete order details in the system dashboard.</p>
                        <p>Best regards,<br>Hospital Meal Ordering System</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated notification. Please do not reply to this email.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(dieticianName, patientName, uhid);
    }


    @Async
    @Override
    public void sendPasswordResetEmail(String toEmail, String userName, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request - Hospital Meal Ordering System");
            helper.setFrom(fromEmail);

            // Inline HTML template
            String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Password Reset</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #2563eb; color: white; padding: 20px; text-align: center; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 8px; margin: 20px 0; }
                    .button { display: inline-block; padding: 12px 30px; background: #2563eb; color: white; 
                              text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; color: #6b7280; font-size: 14px; margin-top: 30px; }
                    .warning { background: #fef3c7; padding: 15px; border-left: 4px solid #f59e0b; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Password Reset Request</h1>
                    </div>
                    
                    <div class="content">
                        <p>Hello <strong>%s</strong>,</p>
                        
                        <p>We received a request to reset your password for your Hospital Meal Ordering System account.</p>
                        
                        <p>Click the button below to reset your password:</p>
                        
                        <a href="%s" class="button">Reset Password</a>
                        
                        <div class="warning">
                            <strong>⚠️ Important:</strong>
                            <ul>
                                <li>This link will expire in 1 hour</li>
                                <li>If you didn't request this, you can safely ignore this email</li>
                                <li>Your password will remain unchanged until you access the link above</li>
                            </ul>
                        </div>
                        
                        <p>If the button doesn't work, copy and paste this link into your browser:</p>
                        <p style="word-break: break-all; color: #2563eb;">%s</p>
                    </div>
                    
                    <div class="footer">
                        <p>Hospital Meal Ordering System</p>
                        <p>This is an automated email. Please do not reply.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName, resetLink, resetLink);

            helper.setText(htmlContent, true);
            mailSender.send(message);

            log.info("Password reset email sent to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }







    private String buildPasswordResetEmail(String resetLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #FF9800; color: white; padding: 20px; text-align: center; }
                    .content { padding: 20px; background-color: #f9f9f9; }
                    .button { display: inline-block; padding: 12px 24px; background-color: #FF9800;
                              color: white; text-decoration: none; border-radius: 4px; margin: 20px 0; }
                    .warning { background-color: #fff3cd; border-left: 4px solid #FF9800; padding: 15px; margin: 15px 0; }
                    .footer { text-align: center; padding: 20px; color: #777; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Password Reset Request</h1>
                    </div>
                    <div class="content">
                        <p>Hello,</p>
                        <p>We received a request to reset your password for the Hospital Meal Ordering System.</p>
                        <p>Click the button below to reset your password:</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Reset Password</a>
                        </p>
                        <p>Or copy and paste this link in your browser:</p>
                        <p style="word-break: break-all; color: #FF9800;">%s</p>
                        <div class="warning">
                            <p><strong>⚠️ Security Notice:</strong></p>
                            <p>This link will expire in 1 hour.</p>
                            <p>If you did not request a password reset, please ignore this email and your password will remain unchanged.</p>
                        </div>
                        <p>Best regards,<br>Hospital Administration</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated message. Please do not reply to this email.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(resetLink, resetLink);
    }
}