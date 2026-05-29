package com.hospital.meal.service.service_impl.kitchen;

import com.hospital.meal.exception.ResourceNotFoundException;
import com.hospital.meal.model.user.KitchenStaff;
import com.hospital.meal.repository.KitchenStaffRepository;
import com.hospital.meal.service.kitchen.KitchenStaffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KitchenStaffServiceImpl implements KitchenStaffService {

    private final KitchenStaffRepository kitchenStaffRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<KitchenStaff> getKitchenStaffById(UUID staffId) {
        return kitchenStaffRepository.findById(staffId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<KitchenStaff> getKitchenStaffByEmail(String email) {
        return kitchenStaffRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isActive(UUID staffId) {
        return kitchenStaffRepository.findById(staffId)
                .map(KitchenStaff::getIsActive)
                .orElse(false);
    }

    @Override
    @Transactional
    public void updateSessionDuration(UUID staffId, Integer hours) {
        log.info("Updating session duration for kitchen staff: {} to {} hours", staffId, hours);

        KitchenStaff staff = kitchenStaffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Kitchen Staff", "id", staffId));

        if (hours < 1 || hours > 48) {
            throw new IllegalArgumentException("Session duration must be between 1 and 48 hours");
        }

        staff.setSessionDurationHours(hours);
        kitchenStaffRepository.save(staff);

        log.info("Session duration updated successfully");
    }
}