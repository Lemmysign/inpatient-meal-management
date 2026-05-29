package com.hospital.meal.security.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.meal.constant.ApiConstants;
import com.hospital.meal.dto.common.ErrorResponse;
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
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final RateLimitProperties rateLimitProperties;
    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Skip rate limiting entirely if disabled
        if (!rateLimitProperties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        String method = request.getMethod();
        String ipAddress = getClientIp(request);

        // 1. Global rate limit — applies to every endpoint
        if (!rateLimitService.isGlobalAllowed(ipAddress)) {
            sendRateLimitError(response, "Too many requests. Please slow down.");
            return;
        }

        // 2. Staff login rate limit
        if (path.equals(ApiConstants.AUTH_BASE + ApiConstants.AUTH_LOGIN)
                && "POST".equals(method)) {
            if (!rateLimitService.isStaffLoginAllowed(ipAddress)) {
                sendRateLimitError(response, "Too many login attempts. Please try again later.");
                return;
            }
        }

        // 3. Patient login rate limit
        if (path.equals(ApiConstants.AUTH_BASE + ApiConstants.AUTH_PATIENT_LOGIN)
                && "POST".equals(method)) {
            if (!rateLimitService.isLoginAllowed(ipAddress)) {
                sendRateLimitError(response, "Too many login attempts. Please try again later.");
                return;
            }
        }

        // 4. Patient order rate limit — extract UHID from JWT
        if (path.contains(ApiConstants.ORDER_BASE) && "POST".equals(method)) {
            String uhid = extractUhidFromToken(request);
            if (uhid != null && !rateLimitService.isOrderAllowed(uhid)) {
                sendRateLimitError(response, "Too many order requests. Please try again later.");
                return;
            }
        }

        // 5. Forgot password rate limit — prevents email spam abuse
        if (path.equals(ApiConstants.AUTH_BASE + ApiConstants.AUTH_FORGOT_PASSWORD)
                && "POST".equals(method)) {
            if (!rateLimitService.isForgotPasswordAllowed(ipAddress)) {
                sendRateLimitError(response, "Too many password reset requests. Please try again later.");
                return;
            }
        }

        // 6. Reset password rate limit
        if (path.equals(ApiConstants.AUTH_BASE + ApiConstants.AUTH_RESET_PASSWORD)
                && "POST".equals(method)) {
            if (!rateLimitService.isResetPasswordAllowed(ipAddress)) {
                sendRateLimitError(response, "Too many reset attempts. Please try again later.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractUhidFromToken(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader(ApiConstants.AUTHORIZATION_HEADER);
            if (authHeader != null && authHeader.startsWith(ApiConstants.BEARER_PREFIX)) {
                String token = authHeader.substring(ApiConstants.BEARER_PREFIX.length());
                return jwtTokenProvider.getUhidFromToken(token);
            }
        } catch (Exception e) {
            log.debug("Could not extract UHID from token for rate limiting: {}", e.getMessage());
        }
        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_CLIENT_IP"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.contains(",") ? ip.split(",")[0].trim() : ip;
            }
        }

        return request.getRemoteAddr();
    }

    private void sendRateLimitError(HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .message(message)
                .build();

        objectMapper.writeValue(response.getWriter(), error);
    }
}