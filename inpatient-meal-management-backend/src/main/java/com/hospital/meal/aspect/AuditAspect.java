package com.hospital.meal.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.meal.constant.RoleConstants;
import com.hospital.meal.model.audit.AuditLog;
import com.hospital.meal.repository.AuditLogRepository;
import com.hospital.meal.security.userdetails.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * Pointcut for service methods that should be audited
     */
    @Pointcut("execution(* com.hospital.meal.service.service_impl..*.create*(..)) || " +
            "execution(* com.hospital.meal.service.service_impl..*.update*(..)) || " +
            "execution(* com.hospital.meal.service.service_impl..*.delete*(..)) || " +
            "execution(* com.hospital.meal.service.service_impl..*.assign*(..)) || " +
            "execution(* com.hospital.meal.service.service_impl..*.process*(..))")
    public void auditableMethods() {}

    /**
     * Log successful operations
     */
    @AfterReturning(pointcut = "auditableMethods()", returning = "result")
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        try {
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();

            log.debug("Auditing method: {}.{}", className, methodName);

            // Get current user info
            UserInfo userInfo = getCurrentUserInfo();

            AuditLog auditLog = AuditLog.builder()
                    .userType(userInfo.userType)
                    .userId(userInfo.userId)
                    .userName(userInfo.userName)
                    .action(getActionFromMethodName(methodName))
                    .entityType(getEntityTypeFromClassName(className))
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIp())
                    .build();

            // Try to serialize result for audit trail
            if (result != null) {
                try {
                    String jsonResult = objectMapper.writeValueAsString(result);
                    // Truncate if too long (PostgreSQL TEXT limit considerations)
                    String truncated = jsonResult.length() > 5000 ?
                            jsonResult.substring(0, 5000) + "... [TRUNCATED]" : jsonResult;
                    auditLog.setNewValue(truncated);
                } catch (Exception e) {
                    log.debug("Could not serialize result for audit log", e);
                    auditLog.setNewValue("Result: " + result.getClass().getSimpleName());
                }
            }

            // Build human-readable details
            String details = String.format("%s performed %s on %s",
                    userInfo.userName != null ? userInfo.userName : "Unknown User",
                    methodName,
                    className);
            auditLog.setDetails(details);

            auditLogRepository.save(auditLog);

            log.debug("Audit log created successfully");

        } catch (Exception e) {
            // Never let audit logging break the main transaction
            log.error("Error creating audit log (non-critical)", e);
        }
    }

    /**
     * Log failed operations
     */
    @AfterThrowing(pointcut = "auditableMethods()", throwing = "exception")
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        try {
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();

            log.debug("Auditing failed method: {}.{}", className, methodName);

            UserInfo userInfo = getCurrentUserInfo();

            AuditLog auditLog = AuditLog.builder()
                    .userType(userInfo.userType)
                    .userId(userInfo.userId)
                    .userName(userInfo.userName)
                    .action(getActionFromMethodName(methodName) + "_FAILED")
                    .entityType(getEntityTypeFromClassName(className))
                    .timestamp(LocalDateTime.now())
                    .ipAddress(getClientIp())
                    .newValue("ERROR: " + exception.getClass().getSimpleName())
                    .details(String.format("Failed: %s.%s - Error: %s",
                            className, methodName, exception.getMessage()))
                    .build();

            auditLogRepository.save(auditLog);

        } catch (Exception e) {
            log.error("Error creating audit log for exception (non-critical)", e);
        }
    }

    /**
     * Extract action from method name
     */
    private String getActionFromMethodName(String methodName) {
        if (methodName.startsWith("create")) return "CREATE";
        if (methodName.startsWith("update")) return "UPDATE";
        if (methodName.startsWith("delete")) return "DELETE";
        if (methodName.startsWith("assign")) return "ASSIGN";
        if (methodName.startsWith("process")) return "PROCESS";
        if (methodName.startsWith("toggle")) return "TOGGLE_STATUS";
        if (methodName.startsWith("deactivate")) return "DEACTIVATE";
        return "OPERATION";
    }

    /**
     * Extract entity type from class name
     */
    private String getEntityTypeFromClassName(String className) {
        return className
                .replace("ServiceImpl", "")
                .replace("Service", "")
                .toUpperCase();
    }

    /**
     * Get current authenticated user information
     */
    private UserInfo getCurrentUserInfo() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated() &&
                    !"anonymousUser".equals(authentication.getPrincipal())) {

                Object principal = authentication.getPrincipal();

                if (principal instanceof UserPrincipal userPrincipal) {
                    return new UserInfo(
                            userPrincipal.getRole(),
                            userPrincipal.getId(),
                            userPrincipal.getName()
                    );
                }

                // Fallback for patient sessions or other custom principals
                return new UserInfo(
                        "UNKNOWN",
                        null,
                        authentication.getName()
                );
            }
        } catch (Exception e) {
            log.debug("Could not extract user info from security context", e);
        }

        return new UserInfo("SYSTEM", null, "SYSTEM");
    }

    /**
     * Get client IP address from request
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                // Check X-Forwarded-For header (for proxied requests)
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }

                // Check X-Real-IP header
                String xRealIp = request.getHeader("X-Real-IP");
                if (xRealIp != null && !xRealIp.isEmpty()) {
                    return xRealIp;
                }

                // Fallback to remote address
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.debug("Could not extract IP address", e);
        }

        return "UNKNOWN";
    }

    /**
     * Inner class to hold user information
     */
    private record UserInfo(String userType, UUID userId, String userName) {}
}