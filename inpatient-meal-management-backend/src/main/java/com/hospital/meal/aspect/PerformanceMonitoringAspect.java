package com.hospital.meal.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class PerformanceMonitoringAspect {

    /**
     * Pointcut for all service methods
     */
    @Pointcut("execution(* com.hospital.meal.service.service_impl..*.*(..))")
    public void serviceMethods() {}

    /**
     * Pointcut for all controller methods
     */
    @Pointcut("execution(* com.hospital.meal.controller..*.*(..))")
    public void controllerMethods() {}

    /**
     * Monitor execution time of service methods
     */
    @Around("serviceMethods()")
    public Object monitorServicePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        String methodName = joinPoint.getSignature().toShortString();

        try {
            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - startTime;

            if (executionTime > 1000) {
                log.warn("Slow service method detected: {} took {}ms", methodName, executionTime);
            } else {
                log.debug("Service method {} took {}ms", methodName, executionTime);
            }

            return result;

        } catch (Throwable throwable) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Service method {} failed after {}ms", methodName, executionTime, throwable);
            throw throwable;
        }
    }

    /**
     * Monitor execution time of controller methods
     */
    @Around("controllerMethods()")
    public Object monitorControllerPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        String methodName = joinPoint.getSignature().toShortString();

        try {
            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - startTime;

            if (executionTime > 3000) {
                log.warn("Slow API endpoint detected: {} took {}ms", methodName, executionTime);
            } else {
                log.debug("API endpoint {} took {}ms", methodName, executionTime);
            }

            return result;

        } catch (Throwable throwable) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("API endpoint {} failed after {}ms", methodName, executionTime, throwable);
            throw throwable;
        }
    }
}