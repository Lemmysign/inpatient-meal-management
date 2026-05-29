package com.hospital.meal.service.service_impl.dietician;

import com.hospital.meal.dto.common.PageResponse;
import com.hospital.meal.dto.dietician.AssignPatientMenuRequest;
import com.hospital.meal.dto.dietician.PatientMenuResponse;
import com.hospital.meal.dto.dietician.UpdateMenuValidityRequest;
import com.hospital.meal.dto.dietician.UpdatePatientMenuRequest;
import com.hospital.meal.exception.InvalidRequestException;
import com.hospital.meal.exception.ResourceNotFoundException;
import com.hospital.meal.mapper.PatientMenuMapper;
import com.hospital.meal.model.menu.MenuGroup;
import com.hospital.meal.model.menu.PatientMenu;
import com.hospital.meal.model.user.Dietician;
import com.hospital.meal.model.user.Patient;
import com.hospital.meal.repository.*;
import com.hospital.meal.service.dietician.PatientMenuService;
import com.hospital.meal.service.patient.PatientService;
import com.hospital.meal.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientMenuServiceImpl implements PatientMenuService {

    private final PatientMenuRepository patientMenuRepository;
    private final MenuGroupRepository menuGroupRepository;
    private final DieticianRepository dieticianRepository;
    private final PatientService patientService;
    private final PatientMenuMapper patientMenuMapper;
    private final PatientRepository patientRepository;

    // PatientMenuServiceImpl.java - Simplified assignMenuToPatient method

    @Override
    @Transactional
    public List<PatientMenuResponse> assignMenuToPatient(AssignPatientMenuRequest request, UUID dieticianId) {
        log.info("Assigning menu groups to patient UHID: {} by dietician: {}", request.getUhid(), dieticianId);

        // Validate request
        if (request.getMenuGroupIds() == null || request.getMenuGroupIds().isEmpty()) {
            throw new InvalidRequestException("At least one menu group must be selected");
        }

        if (request.getValidFrom() == null) {
            throw new InvalidRequestException("Valid from date is required");
        }

        // Get or create patient (only UHID is saved if patient doesn't exist)
        Patient patient = patientService.getPatientByUhid(request.getUhid())
                .orElseGet(() -> {
                    log.info("Patient with UHID {} does not exist. Creating patient record with UHID only.", request.getUhid());

                    Patient newPatient = Patient.builder()
                            .uhid(request.getUhid())
                            .isActive(true)
                            .build();

                    return patientRepository.save(newPatient);
                });

        // Get dietician
        Dietician dietician = dieticianRepository.findById(dieticianId)
                .orElseThrow(() -> new ResourceNotFoundException("Dietician", "id", dieticianId));

        // Validate all menu groups exist and are active
        List<MenuGroup> menuGroups = new ArrayList<>();
        for (UUID menuGroupId : request.getMenuGroupIds()) {
            MenuGroup menuGroup = menuGroupRepository.findById(menuGroupId)
                    .orElseThrow(() -> new ResourceNotFoundException("Menu Group", "id", menuGroupId));

            if (!menuGroup.getIsActive()) {
                throw new InvalidRequestException("Menu group is inactive: " + menuGroup.getName());
            }

            menuGroups.add(menuGroup);
        }

        // Check for duplicate assignments
        for (MenuGroup menuGroup : menuGroups) {
            long duplicateCount = patientMenuRepository.countDuplicateAssignment(
                    patient.getId(),
                    menuGroup.getId(),
                    request.getValidFrom()
            );

            if (duplicateCount > 0) {
                log.warn("Duplicate menu assignment detected for patient {} and menu group {}",
                        request.getUhid(), menuGroup.getName());
                throw new InvalidRequestException(
                        "Menu group '" + menuGroup.getName() + "' is already assigned for this date");
            }
        }

        // Create patient menu assignments
        LocalDateTime now = DateUtil.getCurrentDateTime();
        List<PatientMenu> patientMenus = new ArrayList<>();

        for (MenuGroup menuGroup : menuGroups) {
            PatientMenu patientMenu = PatientMenu.builder()
                    .patient(patient)
                    .menuGroup(menuGroup)
                    .menuGroupName(menuGroup.getName())
                    .uhid(patient.getUhid())
                    .assignedByDietician(dietician)
                    .assignedAt(now)
                    .validFrom(request.getValidFrom())
                    .validUntil(request.getValidUntil())
                    .notes(request.getNotes())
                    .isActive(true)
                    .build();

            patientMenus.add(patientMenu);
        }

        patientMenus = patientMenuRepository.saveAll(patientMenus);

        log.info("Successfully assigned {} menu group(s) to patient {}", patientMenus.size(), request.getUhid());

        return patientMenus.stream()
                .map(patientMenuMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientMenuResponse> getActivePatientMenus(String uhid, LocalDate date) {
        log.debug("Getting active menus for patient UHID: {} on date: {}", uhid, date);

        // Validate patient exists
        Patient patient = patientService.getPatientByUhid(uhid)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "UHID", uhid));

        List<PatientMenu> patientMenus = patientMenuRepository
                .findActiveMenusByUhidAndDate(uhid, date);

        return patientMenus.stream()
                .map(patientMenuMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientMenuResponse> getPatientMenuHistory(String uhid) {
        log.debug("Getting menu history for patient UHID: {}", uhid);

        // Validate patient exists
        Patient patient = patientService.getPatientByUhid(uhid)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "UHID", uhid));

        List<PatientMenu> patientMenus = patientMenuRepository
                .findByPatientId(patient.getId());

        return patientMenus.stream()
                .map(patientMenuMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PatientMenuResponse> getMenusByDietician(UUID dieticianId, Pageable pageable) {
        log.debug("Getting menus assigned by dietician: {}", dieticianId);

        // Validate dietician exists
        if (!dieticianRepository.existsById(dieticianId)) {
            throw new ResourceNotFoundException("Dietician", "id", dieticianId);
        }

        Page<PatientMenu> patientMenuPage = patientMenuRepository
                .findByDieticianId(dieticianId, pageable);

        return PageResponse.<PatientMenuResponse>builder()
                .content(patientMenuPage.getContent().stream()
                        .map(patientMenuMapper::toResponse)
                        .collect(Collectors.toList()))
                .page(patientMenuPage.getNumber())
                .size(patientMenuPage.getSize())
                .totalElements(patientMenuPage.getTotalElements())
                .totalPages(patientMenuPage.getTotalPages())
                .first(patientMenuPage.isFirst())
                .last(patientMenuPage.isLast())
                .empty(patientMenuPage.isEmpty())
                .build();
    }

    @Override
    @Transactional
    public void deactivatePatientMenu(UUID patientMenuId, UUID dieticianId) {
        log.info("Deactivating patient menu: {} by dietician: {}", patientMenuId, dieticianId);

        // Get patient menu
        PatientMenu patientMenu = patientMenuRepository.findById(patientMenuId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient Menu", "id", patientMenuId));

        /* Verify dietician is the one who assigned it
        if (!patientMenu.getAssignedByDietician().getId().equals(dieticianId)) {
            throw new IllegalStateException("Only the assigning dietician can deactivate this menu");
        }
*/
        // Deactivate
        patientMenu.setIsActive(false);
        patientMenuRepository.save(patientMenu);

        log.info("Patient menu deactivated successfully");
    }

    @Override
    @Transactional
    public PatientMenuResponse updateMenuValidity(UUID patientMenuId,
                                                  UpdateMenuValidityRequest request,
                                                  UUID dieticianId) {
        log.info("Updating menu validity for patient menu: {} by dietician: {}", patientMenuId, dieticianId);

        // Get patient menu
        PatientMenu patientMenu = patientMenuRepository.findByIdWithDetails(patientMenuId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient Menu", "id", patientMenuId));

    /* Verify dietician is the one who assigned it
    if (!patientMenu.getAssignedByDietician().getId().equals(dieticianId)) {
        throw new IllegalStateException("Only the assigning dietician can update this menu");
    }
    */

        // Validate dates
        if (request.getValidFrom() == null) {
            throw new InvalidRequestException("Valid from date is required");
        }

        if (request.getValidUntil() != null && request.getValidUntil().isBefore(request.getValidFrom())) {
            throw new InvalidRequestException("Valid until date cannot be before valid from date");
        }

        // ============================================================
        // MENU GROUP CHANGE LOGIC (NEW)
        // ============================================================
        if (request.getMenuGroupId() != null &&
                !request.getMenuGroupId().equals(patientMenu.getMenuGroup().getId())) {

            log.info("Changing menu group from {} to {}",
                    patientMenu.getMenuGroup().getId(), request.getMenuGroupId());

            // Get new menu group
            MenuGroup newMenuGroup = menuGroupRepository.findById(request.getMenuGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu Group", "id", request.getMenuGroupId()));

            // Validate menu group is active
            if (!newMenuGroup.getIsActive()) {
                throw new InvalidRequestException("Cannot assign inactive menu group: " + newMenuGroup.getName());
            }

            // Check for duplicate assignment EXCLUDING current assignment
            long duplicateCount = patientMenuRepository.countDuplicateAssignmentExcludingCurrent(
                    patientMenu.getPatient().getId(),
                    request.getMenuGroupId(),
                    request.getValidFrom(),
                    patientMenuId
            );

            if (duplicateCount > 0) {
                throw new InvalidRequestException(
                        "Menu group '" + newMenuGroup.getName() + "' is already assigned for this date range");
            }

            // Update menu group
            patientMenu.setMenuGroup(newMenuGroup);
            patientMenu.setMenuGroupName(newMenuGroup.getName());

            log.info("Menu group changed to: {}", newMenuGroup.getName());
        }

        // ============================================================
        // UPDATE OTHER FIELDS
        // ============================================================

        // Update validity dates
        patientMenu.setValidFrom(request.getValidFrom());
        patientMenu.setValidUntil(request.getValidUntil());

        // Update isActive if provided
        if (request.getIsActive() != null) {
            patientMenu.setIsActive(request.getIsActive());
        }

        // Update notes if provided
        if (request.getNotes() != null) {
            patientMenu.setNotes(request.getNotes());
        }

        patientMenu = patientMenuRepository.save(patientMenu);

        log.info("Menu validity updated successfully");

        return patientMenuMapper.toResponse(patientMenu);
    }


    @Override
    @Transactional(readOnly = true)
    public PageResponse<PatientMenuResponse> searchPatientMenus(
            List<UUID> menuGroupIds,
            List<UUID> dieticianIds,
            String searchTerm,
            LocalDate dateFrom,
            LocalDate dateTo,
            Boolean isActive,
            Pageable pageable) {

        log.info("Searching patient menus - menuGroups: {}, dieticians: {}, search: {}, dateFrom: {}, dateTo: {}, isActive: {}",
                menuGroupIds, dieticianIds, searchTerm, dateFrom, dateTo, isActive);

        // Normalize inputs before passing
        List<UUID> filteredMenuGroupIds = (menuGroupIds != null && !menuGroupIds.isEmpty()) ? menuGroupIds : null;
        List<UUID> filteredDieticianIds = (dieticianIds != null && !dieticianIds.isEmpty()) ? dieticianIds : null;
        String filteredSearchTerm = (searchTerm != null && !searchTerm.isBlank()) ? searchTerm.trim() : null;

        Specification<PatientMenu> spec = PatientMenuSpecification.withFilters(
                filteredMenuGroupIds,
                filteredDieticianIds,
                filteredSearchTerm,
                dateFrom,
                dateTo,
                isActive
        );

        // Add explicit sort in case pageable doesn't have one
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSortOr(Sort.by(Sort.Direction.DESC, "assignedAt"))
        );

        Page<PatientMenu> patientMenuPage = patientMenuRepository.findAll(spec, sortedPageable);

        return PageResponse.<PatientMenuResponse>builder()
                .content(patientMenuPage.getContent().stream()
                        .map(patientMenuMapper::toResponse)
                        .collect(Collectors.toList()))
                .page(patientMenuPage.getNumber())
                .size(patientMenuPage.getSize())
                .totalElements(patientMenuPage.getTotalElements())
                .totalPages(patientMenuPage.getTotalPages())
                .first(patientMenuPage.isFirst())
                .last(patientMenuPage.isLast())
                .empty(patientMenuPage.isEmpty())
                .build();
    }

    @Override
    @Transactional
    public PatientMenuResponse updatePatientMenuAssignment(
            UUID patientMenuId,
            UpdatePatientMenuRequest request,
            UUID dieticianId) {

        log.info("Updating patient menu assignment: {} by dietician: {}", patientMenuId, dieticianId);

        PatientMenu patientMenu = patientMenuRepository.findById(patientMenuId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient Menu", "id", patientMenuId));

        // Validate dates
        if (request.getValidFrom() == null) {
            throw new InvalidRequestException("Valid from date is required");
        }

        if (request.getValidUntil() != null && request.getValidUntil().isBefore(request.getValidFrom())) {
            throw new InvalidRequestException("Valid until date cannot be before valid from date");
        }

        // ============================================================
        // MENU GROUP CHANGE LOGIC
        // ============================================================
        if (request.getMenuGroupId() != null &&
                !request.getMenuGroupId().equals(patientMenu.getMenuGroup().getId())) {

            log.info("Changing menu group from {} to {}",
                    patientMenu.getMenuGroup().getId(), request.getMenuGroupId());

            // Get new menu group
            MenuGroup newMenuGroup = menuGroupRepository.findById(request.getMenuGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu Group", "id", request.getMenuGroupId()));

            // Validate menu group is active
            if (!newMenuGroup.getIsActive()) {
                throw new InvalidRequestException("Cannot assign inactive menu group: " + newMenuGroup.getName());
            }

            // ✅ FIX: Check for duplicate assignment EXCLUDING current assignment
            long duplicateCount = patientMenuRepository.countDuplicateAssignmentExcludingCurrent(
                    patientMenu.getPatient().getId(),
                    request.getMenuGroupId(),
                    request.getValidFrom(),
                    patientMenuId  // ← Exclude this assignment
            );

            if (duplicateCount > 0) {
                throw new InvalidRequestException(
                        "Menu group '" + newMenuGroup.getName() + "' is already assigned for this date range");
            }

            // Update menu group
            patientMenu.setMenuGroup(newMenuGroup);
            patientMenu.setMenuGroupName(newMenuGroup.getName());

            log.info("Menu group changed to: {}", newMenuGroup.getName());
        }

        // ============================================================
        // UPDATE OTHER FIELDS
        // ============================================================

        // Update dates
        patientMenu.setValidFrom(request.getValidFrom());
        patientMenu.setValidUntil(request.getValidUntil());

        // Update active status
        if (request.getIsActive() != null) {
            patientMenu.setIsActive(request.getIsActive());
        }

        // Update notes
        if (request.getNotes() != null) {
            patientMenu.setNotes(request.getNotes());
        }

        patientMenu = patientMenuRepository.save(patientMenu);

        log.info("Patient menu assignment updated successfully");

        return patientMenuMapper.toResponse(patientMenu);
    }

    @Override
    @Transactional
    public void removeMenuFromPatient(UUID patientMenuId, UUID dieticianId) {
        log.info("Removing menu from patient: {} by dietician: {}", patientMenuId, dieticianId);

        PatientMenu patientMenu = patientMenuRepository.findById(patientMenuId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient Menu", "id", patientMenuId));

        // Deactivate
        patientMenu.setIsActive(false);
        patientMenuRepository.save(patientMenu);

        log.info("Menu removed from patient successfully");
    }




}