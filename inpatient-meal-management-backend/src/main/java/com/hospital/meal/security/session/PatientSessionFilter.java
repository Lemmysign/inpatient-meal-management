package com.hospital.meal.security.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.meal.constant.ApiConstants;
import com.hospital.meal.constant.RoleConstants;
import com.hospital.meal.dto.common.ErrorResponse;
import com.hospital.meal.model.user.PatientSession;
import com.hospital.meal.repository.PatientSessionRepository;
import com.hospital.meal.security.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PatientSessionFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final PatientSessionRepository patientSessionRepository;
    private final DeviceFingerprintUtil deviceFingerprintUtil;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Only apply to patient endpoints
        String path = request.getRequestURI();
        if (!path.startsWith(ApiConstants.PATIENT_BASE) || path.contains("/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = getJwtFromRequest(request);

            if (!StringUtils.hasText(jwt)) {
                sendErrorResponse(response, "Missing session token", HttpStatus.UNAUTHORIZED);
                return;
            }

            if (!jwtTokenProvider.validateToken(jwt)) {
                sendErrorResponse(response, "Invalid or expired session token", HttpStatus.UNAUTHORIZED);
                return;
            }

            String role = jwtTokenProvider.getRoleFromToken(jwt);
            if (!RoleConstants.PATIENT.equals(role)) {
                sendErrorResponse(response, "Invalid session type", HttpStatus.FORBIDDEN);
                return;
            }

            // Validate session exists in database
            Optional<PatientSession> sessionOpt = patientSessionRepository.findBySessionToken(jwt);

            if (sessionOpt.isEmpty()) {
                sendErrorResponse(response, "Session not found", HttpStatus.UNAUTHORIZED);
                return;
            }

            PatientSession session = sessionOpt.get();

            // Check if session is active
            if (!session.getIsActive()) {
                sendErrorResponse(response, "Session has been deactivated", HttpStatus.UNAUTHORIZED);
                return;
            }

            // Check if session is expired
            if (session.isExpired()) {
                session.setIsActive(false);
                patientSessionRepository.save(session);
                sendErrorResponse(response, "Session has expired", HttpStatus.UNAUTHORIZED);
                return;
            }

            // Validate device fingerprint (optional but recommended)
          /*  String currentFingerprint = deviceFingerprintUtil.generate(request);
            if (session.getDeviceFingerprint() != null &&
                    !session.getDeviceFingerprint().equals(currentFingerprint)) {
                log.warn("Device fingerprint mismatch for UHID: {}", session.getPatient().getUhid());
                // Optionally invalidate session or just log
                // sendErrorResponse(response, "Device mismatch detected", HttpStatus.FORBIDDEN);
                // return;
            }
            */
            // Update last activity
            session.setLastActivityAt(LocalDateTime.now());
            patientSessionRepository.save(session);

            // Set patient info in request attributes for controllers
            request.setAttribute("patientId", session.getPatient().getId());
            request.setAttribute("uhid", session.getPatient().getUhid());
            request.setAttribute("patientName", session.getPatient().getName());
            request.setAttribute("roomNumber", session.getPatient().getRoomNumber());
            request.setAttribute("sessionId", session.getId());

        } catch (Exception ex) {
            log.error("Error validating patient session", ex);
            sendErrorResponse(response, "Session validation failed", HttpStatus.INTERNAL_SERVER_ERROR);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from request header
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(ApiConstants.AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(ApiConstants.BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }

        return null;
    }

    /**
     * Send error response as JSON
     */
    private void sendErrorResponse(HttpServletResponse response, String message, HttpStatus status) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .message(message)
                .error(status.getReasonPhrase())
                .status(status.value())
                .timestamp(LocalDateTime.now())
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}