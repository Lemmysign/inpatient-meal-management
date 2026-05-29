package com.hospital.meal.security.jwt;

import com.hospital.meal.constant.ApiConstants;
import com.hospital.meal.constant.RoleConstants;
import com.hospital.meal.security.userdetails.CustomUserDetailsService;
import com.hospital.meal.security.userdetails.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService; // ADD THIS

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {

                String role = jwtTokenProvider.getRoleFromToken(jwt);
                String requestPath = request.getRequestURI();

                if (RoleConstants.PATIENT.equals(role) &&
                        requestPath.startsWith(ApiConstants.PATIENT_BASE)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                String email = jwtTokenProvider.getEmailFromToken(jwt);
                UUID userId = jwtTokenProvider.getUserIdFromToken(jwt);

                // Load full UserPrincipal instead of just storing email string
                UserPrincipal userPrincipal = (UserPrincipal) customUserDetailsService
                        .loadUserByEmailAndRole(email, role);

                request.setAttribute("userId", userId);
                request.setAttribute("userEmail", email);
                request.setAttribute("userRole", role);
                request.setAttribute("userType", role);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userPrincipal, // ← UserPrincipal as principal, not email string
                                null,
                                userPrincipal.getAuthorities()
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Set authentication for user: {} with role: {}", email, role);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(ApiConstants.AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(ApiConstants.BEARER_PREFIX)) {
            return bearerToken.substring(ApiConstants.BEARER_PREFIX.length());
        }
        return null;
    }
}