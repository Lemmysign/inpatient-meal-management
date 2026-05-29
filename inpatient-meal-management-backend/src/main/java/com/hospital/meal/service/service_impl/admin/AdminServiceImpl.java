package com.hospital.meal.service.service_impl.admin;

import com.hospital.meal.dto.admin.CreateDieticianRequest;
import com.hospital.meal.dto.admin.CreateKitchenStaffRequest;
import com.hospital.meal.dto.admin.DieticianResponse;
import com.hospital.meal.dto.admin.KitchenStaffResponse;
import com.hospital.meal.dto.common.PageResponse;
import com.hospital.meal.exception.DuplicateResourceException;
import com.hospital.meal.exception.ResourceNotFoundException;
import com.hospital.meal.mapper.AdminMapper;
import com.hospital.meal.model.user.Dietician;
import com.hospital.meal.model.user.DieticianInvite;
import com.hospital.meal.model.user.KitchenStaff;
import com.hospital.meal.repository.DieticianInviteRepository;
import com.hospital.meal.repository.DieticianRepository;
import com.hospital.meal.repository.KitchenStaffRepository;
import com.hospital.meal.service.admin.AdminService;
import com.hospital.meal.service.notification.EmailService;
import com.hospital.meal.util.IdempotencyKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final DieticianRepository dieticianRepository;
    private final KitchenStaffRepository kitchenStaffRepository;
    private final DieticianInviteRepository dieticianInviteRepository;
    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final long INVITE_EXPIRATION_MINUTES = 15; // 3 days

    @Override
    @Transactional
    public DieticianResponse createDietician(CreateDieticianRequest request) {
        log.info("Creating dietician with email: {}", request.getEmail());

        // Check for duplicates
        if (dieticianRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Dietician", "email", request.getEmail());
        }

        if (request.getStaffId() != null && dieticianRepository.existsByStaffId(request.getStaffId())) {
            throw new DuplicateResourceException("Dietician", "staffId", request.getStaffId());
        }

        // Create dietician (without password initially)
        Dietician dietician = Dietician.builder()
                .name(request.getName())
                .staffId(request.getStaffId())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .passwordHash(null) // Will be set when they accept invite
                .isActive(true)
                .build();

        dietician = dieticianRepository.save(dietician);

        // Create invite token
        String inviteToken = IdempotencyKeyGenerator.generateInviteToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(INVITE_EXPIRATION_MINUTES);

        DieticianInvite invite = DieticianInvite.builder()
                .dietician(dietician)
                .token(inviteToken)
                .expiresAt(expiresAt)
                .build();

        dieticianInviteRepository.save(invite);

        // Send invite email
        emailService.sendDieticianInvite(dietician.getEmail(), dietician.getName(), inviteToken);

        log.info("Dietician created successfully with ID: {}", dietician.getId());

        return adminMapper.toDieticianResponse(dietician);
    }

    @Override
    @Transactional
    public KitchenStaffResponse createKitchenStaff(CreateKitchenStaffRequest request) {
        log.info("Creating kitchen staff with email: {}", request.getEmail());

        // Check for duplicates
        if (kitchenStaffRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Kitchen Staff", "email", request.getEmail());
        }

        // Create kitchen staff with password
        KitchenStaff kitchenStaff = KitchenStaff.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .sessionDurationHours(24) // Default 24-hour session
                .build();

        kitchenStaff = kitchenStaffRepository.save(kitchenStaff);

        log.info("Kitchen staff created successfully with ID: {}", kitchenStaff.getId());

        return adminMapper.toKitchenStaffResponse(kitchenStaff);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DieticianResponse> getAllDieticians(Pageable pageable) {
        Page<Dietician> dieticianPage = dieticianRepository.findAll(pageable);

        return PageResponse.<DieticianResponse>builder()
                .content(dieticianPage.getContent().stream()
                        .map(adminMapper::toDieticianResponse)
                        .toList())
                .page(dieticianPage.getNumber())
                .size(dieticianPage.getSize())
                .totalElements(dieticianPage.getTotalElements())
                .totalPages(dieticianPage.getTotalPages())
                .first(dieticianPage.isFirst())
                .last(dieticianPage.isLast())
                .empty(dieticianPage.isEmpty())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<KitchenStaffResponse> getAllKitchenStaff(Pageable pageable) {
        Page<KitchenStaff> staffPage = kitchenStaffRepository.findAll(pageable);

        return PageResponse.<KitchenStaffResponse>builder()
                .content(staffPage.getContent().stream()
                        .map(adminMapper::toKitchenStaffResponse)
                        .toList())
                .page(staffPage.getNumber())
                .size(staffPage.getSize())
                .totalElements(staffPage.getTotalElements())
                .totalPages(staffPage.getTotalPages())
                .first(staffPage.isFirst())
                .last(staffPage.isLast())
                .empty(staffPage.isEmpty())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DieticianResponse getDieticianById(UUID dieticianId) {
        Dietician dietician = dieticianRepository.findById(dieticianId)
                .orElseThrow(() -> new ResourceNotFoundException("Dietician", "id", dieticianId));

        return adminMapper.toDieticianResponse(dietician);
    }

    @Override
    @Transactional(readOnly = true)
    public KitchenStaffResponse getKitchenStaffById(UUID staffId) {
        KitchenStaff staff = kitchenStaffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Kitchen Staff", "id", staffId));

        return adminMapper.toKitchenStaffResponse(staff);
    }

    @Override
    @Transactional
    public void toggleDieticianStatus(UUID dieticianId, boolean isActive) {
        log.info("Toggling dietician status to {} for ID: {}", isActive, dieticianId);

        Dietician dietician = dieticianRepository.findById(dieticianId)
                .orElseThrow(() -> new ResourceNotFoundException("Dietician", "id", dieticianId));

        dietician.setIsActive(isActive);
        dieticianRepository.save(dietician);

        log.info("Dietician status updated successfully");
    }

    @Override
    @Transactional
    public void toggleKitchenStaffStatus(UUID staffId, boolean isActive) {
        log.info("Toggling kitchen staff status to {} for ID: {}", isActive, staffId);

        KitchenStaff staff = kitchenStaffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Kitchen Staff", "id", staffId));

        staff.setIsActive(isActive);
        kitchenStaffRepository.save(staff);

        log.info("Kitchen staff status updated successfully");
    }

    @Override
    @Transactional
    public void resendDieticianInvite(UUID dieticianId) {
        log.info("Resending invite for dietician ID: {}", dieticianId);

        Dietician dietician = dieticianRepository.findById(dieticianId)
                .orElseThrow(() -> new ResourceNotFoundException("Dietician", "id", dieticianId));

        // Check if password already set
        if (dietician.getPasswordHash() != null) {
            throw new IllegalStateException("Dietician has already set their password");
        }

        // Invalidate old invites and create new one
        String inviteToken = IdempotencyKeyGenerator.generateInviteToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(INVITE_EXPIRATION_MINUTES);

        DieticianInvite invite = DieticianInvite.builder()
                .dietician(dietician)
                .token(inviteToken)
                .expiresAt(expiresAt)
                .build();

        dieticianInviteRepository.save(invite);

        // Send invite email
        emailService.sendDieticianInvite(dietician.getEmail(), dietician.getName(), inviteToken);

        log.info("Invite resent successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DieticianResponse> searchDieticians(String search, Pageable pageable) {
        Page<Dietician> dieticianPage = dieticianRepository.searchDieticians(search, pageable);

        return PageResponse.<DieticianResponse>builder()
                .content(dieticianPage.getContent().stream()
                        .map(adminMapper::toDieticianResponse)
                        .toList())
                .page(dieticianPage.getNumber())
                .size(dieticianPage.getSize())
                .totalElements(dieticianPage.getTotalElements())
                .totalPages(dieticianPage.getTotalPages())
                .first(dieticianPage.isFirst())
                .last(dieticianPage.isLast())
                .empty(dieticianPage.isEmpty())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<KitchenStaffResponse> searchKitchenStaff(String search, Pageable pageable) {
        Page<KitchenStaff> staffPage = kitchenStaffRepository.searchKitchenStaff(search, pageable);

        return PageResponse.<KitchenStaffResponse>builder()
                .content(staffPage.getContent().stream()
                        .map(adminMapper::toKitchenStaffResponse)
                        .toList())
                .page(staffPage.getNumber())
                .size(staffPage.getSize())
                .totalElements(staffPage.getTotalElements())
                .totalPages(staffPage.getTotalPages())
                .first(staffPage.isFirst())
                .last(staffPage.isLast())
                .empty(staffPage.isEmpty())
                .build();
    }
}