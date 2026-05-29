package com.hospital.meal.service.service_impl.dietician;

import com.hospital.meal.dto.dietician.DieticianFilterOption;
import com.hospital.meal.dto.dietician.MenuGroupFilterOption;
import com.hospital.meal.exception.ResourceNotFoundException;
import com.hospital.meal.model.menu.MenuGroup;
import com.hospital.meal.model.user.Dietician;
import com.hospital.meal.repository.DieticianRepository;
import com.hospital.meal.repository.FoodItemRepository;
import com.hospital.meal.repository.MenuGroupRepository;
import com.hospital.meal.service.dietician.DieticianService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DieticianServiceImpl implements DieticianService {

    private final DieticianRepository dieticianRepository;
    private final FoodItemRepository foodItemRepository;
    private final MenuGroupRepository menuGroupRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Dietician> getDieticianById(UUID dieticianId) {
        return dieticianRepository.findById(dieticianId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Dietician> getDieticianByEmail(String email) {
        return dieticianRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Dietician> getDieticianByStaffId(String staffId) {
        return dieticianRepository.findByStaffId(staffId);
    }


    @Override
    @Transactional(readOnly = true)
    public Long getTotalFoodItemsCount() {
        log.info("Fetching total food items count");
        return foodItemRepository.countActiveFoodItems();
    }




    @Override
    @Transactional(readOnly = true)
    public boolean isActive(UUID dieticianId) {
        return dieticianRepository.findById(dieticianId)
                .map(Dietician::getIsActive)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPassword(UUID dieticianId) {
        return dieticianRepository.findById(dieticianId)
                .map(d -> d.getPasswordHash() != null)
                .orElse(false);
    }

    @Override
    @Transactional
    public Dietician updateProfile(UUID dieticianId, String name, String phoneNumber) {
        log.info("Updating profile for dietician: {}", dieticianId);

        Dietician dietician = dieticianRepository.findById(dieticianId)
                .orElseThrow(() -> new ResourceNotFoundException("Dietician", "id", dieticianId));

        if (name != null && !name.trim().isEmpty()) {
            dietician.setName(name);
        }

        if (phoneNumber != null) {
            dietician.setPhoneNumber(phoneNumber);
        }

        dietician = dieticianRepository.save(dietician);

        log.info("Dietician profile updated successfully");

        return dietician;
    }


    @Override
    @Transactional(readOnly = true)
    public List<DieticianFilterOption> getAllDieticianFilterOptions() {
        log.info("Fetching all dieticians for filter options");

        List<Dietician> dieticians = dieticianRepository.findAllActiveForFilter();

        return dieticians.stream()
                .map(d -> DieticianFilterOption.builder()
                        .id(d.getId())
                        .name(d.getName())
                        .email(d.getEmail())
                        .isActive(d.getIsActive())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuGroupFilterOption> getAllMenuGroupFilterOptions() {
        log.info("Fetching all menu groups for filter options");

        List<Object[]> results = menuGroupRepository.findAllActiveWithPatientCount();

        return results.stream()
                .map(row -> {
                    MenuGroup mg = (MenuGroup) row[0];
                    Long count = (Long) row[1];

                    return MenuGroupFilterOption.builder()
                            .id(mg.getId())
                            .name(mg.getName())
                            .description(mg.getDescription())
                            .isActive(mg.getIsActive())
                            .assignedPatientsCount(count.intValue())
                            .build();
                })
                .collect(Collectors.toList());
    }



}