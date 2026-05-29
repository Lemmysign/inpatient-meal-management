package com.hospital.meal.security.userdetails;

import com.hospital.meal.constant.RoleConstants;
import com.hospital.meal.model.user.Admin;
import com.hospital.meal.model.user.Dietician;
import com.hospital.meal.model.user.KitchenStaff;
import com.hospital.meal.repository.AdminRepository;
import com.hospital.meal.repository.DieticianRepository;
import com.hospital.meal.repository.KitchenStaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;
    private final DieticianRepository dieticianRepository;
    private final KitchenStaffRepository kitchenStaffRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // Try Admin
        Optional<Admin> admin = adminRepository.findByEmail(email);
        if (admin.isPresent()) {
            Admin a = admin.get();
            return UserPrincipal.builder()
                    .id(a.getId())
                    .email(a.getEmail())
                    .password(a.getPasswordHash())
                    .role(RoleConstants.ADMIN)
                    .name(a.getName())
                    .isActive(a.getIsActive())
                    .build();
        }

        // Try Dietician
        Optional<Dietician> dietician = dieticianRepository.findByEmail(email);
        if (dietician.isPresent()) {
            Dietician d = dietician.get();
            if (d.getPasswordHash() == null) {
                throw new UsernameNotFoundException("Password not set for dietician: " + email);
            }
            return UserPrincipal.builder()
                    .id(d.getId())
                    .email(d.getEmail())
                    .password(d.getPasswordHash())
                    .role(RoleConstants.DIETICIAN)
                    .name(d.getName())
                    .isActive(d.getIsActive())
                    .build();
        }

        // Try Kitchen Staff
        Optional<KitchenStaff> kitchenStaff = kitchenStaffRepository.findByEmail(email);
        if (kitchenStaff.isPresent()) {
            KitchenStaff k = kitchenStaff.get();
            return UserPrincipal.builder()
                    .id(k.getId())
                    .email(k.getEmail())
                    .password(k.getPasswordHash())
                    .role(RoleConstants.KITCHEN_STAFF)
                    .name(k.getName())
                    .isActive(k.getIsActive())
                    .build();
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }

    /**
     * Load user by email and role (for more specific queries)
     */
    public UserDetails loadUserByEmailAndRole(String email, String role) throws UsernameNotFoundException {
        return switch (role) {
            case RoleConstants.ADMIN -> {
                Admin admin = adminRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("Admin not found: " + email));
                yield UserPrincipal.builder()
                        .id(admin.getId())
                        .email(admin.getEmail())
                        .password(admin.getPasswordHash())
                        .role(RoleConstants.ADMIN)
                        .name(admin.getName())
                        .isActive(admin.getIsActive())
                        .build();
            }
            case RoleConstants.DIETICIAN -> {
                Dietician dietician = dieticianRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("Dietician not found: " + email));
                yield UserPrincipal.builder()
                        .id(dietician.getId())
                        .email(dietician.getEmail())
                        .password(dietician.getPasswordHash())
                        .role(RoleConstants.DIETICIAN)
                        .name(dietician.getName())
                        .isActive(dietician.getIsActive())
                        .build();
            }
            case RoleConstants.KITCHEN_STAFF -> {
                KitchenStaff staff = kitchenStaffRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("Kitchen staff not found: " + email));
                yield UserPrincipal.builder()
                        .id(staff.getId())
                        .email(staff.getEmail())
                        .password(staff.getPasswordHash())
                        .role(RoleConstants.KITCHEN_STAFF)
                        .name(staff.getName())
                        .isActive(staff.getIsActive())
                        .build();
            }
            default -> throw new UsernameNotFoundException("Invalid role: " + role);
        };
    }
}