package com.hospital.meal.service.service_impl.kitchen;

import com.hospital.meal.constant.MealStatusConstants;
import com.hospital.meal.constant.MealTypeConstants;
import com.hospital.meal.dto.kitchen.KitchenQueueResponse;
import com.hospital.meal.dto.kitchen.MealQueueItemResponse;
import com.hospital.meal.mapper.KitchenStaffMapper;
import com.hospital.meal.model.menu.PatientMenu;
import com.hospital.meal.model.order.MealOrderItem;
import com.hospital.meal.repository.MealOrderItemRepository;
import com.hospital.meal.service.kitchen.KitchenQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KitchenQueueServiceImpl implements KitchenQueueService {

    private final MealOrderItemRepository mealOrderItemRepository;
    private final KitchenStaffMapper kitchenStaffMapper;

    @Override
    @Transactional(readOnly = true)
    public KitchenQueueResponse getQueueByMealType(String mealType, String status) {
        log.debug("Getting kitchen queue for meal type: {} with status: {}", mealType, status);

        if (!MealTypeConstants.isValidMealType(mealType)) {
            throw new IllegalArgumentException("Invalid meal type: " + mealType);
        }

        String targetStatus = status != null ? status : MealStatusConstants.PENDING;

        List<MealOrderItem> mealItems = mealOrderItemRepository
                .findQueueByMealType(targetStatus, mealType);

        List<MealQueueItemResponse> queueItems = new ArrayList<>();
        for (int i = 0; i < mealItems.size(); i++) {
            MealOrderItem item = mealItems.get(i);
            Integer queuePosition = i + 1;
            MealQueueItemResponse response = kitchenStaffMapper
                    .toMealQueueItemResponse(item, queuePosition);
            queueItems.add(response);
        }

        return KitchenQueueResponse.builder()
                .mealType(mealType)
                .status(targetStatus)
                .totalCount(queueItems.size())
                .items(queueItems)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<KitchenQueueResponse> getAllPendingQueues() {
        log.debug("Getting all pending meal queues");

        List<KitchenQueueResponse> allQueues = new ArrayList<>();

        for (String mealType : MealTypeConstants.MAIN_MEAL_TYPES) {
            KitchenQueueResponse queue = getQueueByMealType(mealType, MealStatusConstants.PENDING);
            allQueues.add(queue);
        }

        KitchenQueueResponse extraQueue = getQueueByMealType(
                MealTypeConstants.EXTRA,
                MealStatusConstants.PENDING
        );
        allQueues.add(extraQueue);

        log.debug("Retrieved {} meal queues", allQueues.size());

        return allQueues;
    }

    @Override
    @Transactional(readOnly = true)
    public KitchenQueueResponse getQueueByMealTypePaged(String mealType, String status, Pageable pageable) {
        log.debug("Getting kitchen queue (paginated) for meal type: {} with status: {}", mealType, status);

        if (!MealTypeConstants.isValidMealType(mealType)) {
            throw new IllegalArgumentException("Invalid meal type: " + mealType);
        }

        String targetStatus = status != null ? status : MealStatusConstants.PENDING;

        Page<MealOrderItem> mealItemsPage = mealOrderItemRepository
                .findQueueByMealTypePaged(targetStatus, mealType, pageable);

        int positionOffset = pageable.getPageNumber() * pageable.getPageSize();

        List<MealQueueItemResponse> queueItems = new ArrayList<>();
        List<MealOrderItem> items = mealItemsPage.getContent();

        for (int i = 0; i < items.size(); i++) {
            MealOrderItem item = items.get(i);
            Integer queuePosition = positionOffset + i + 1;
            MealQueueItemResponse response = kitchenStaffMapper
                    .toMealQueueItemResponse(item, queuePosition);
            queueItems.add(response);
        }

        return KitchenQueueResponse.builder()
                .mealType(mealType)
                .status(targetStatus)
                .totalCount((int) mealItemsPage.getTotalElements())
                .items(queueItems)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MealQueueItemResponse> getQueuePaginated(String mealType, String statusCode, Pageable pageable) {
        log.info("Fetching paginated queue for meal type: {}, status: {}, page: {}, size: {}",
                mealType, statusCode, pageable.getPageNumber(), pageable.getPageSize());

        LocalDate today = LocalDate.now();

        Page<MealOrderItem> mealOrderItems = mealOrderItemRepository
                .findQueueByStatusAndMealTypeAndDatePaged(statusCode, mealType, today, pageable);

        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();

        List<MealQueueItemResponse> mappedContent = new ArrayList<>();
        List<MealOrderItem> items = mealOrderItems.getContent();
        for (int i = 0; i < items.size(); i++) {
            int position = pageNumber * pageSize + i + 1;
            mappedContent.add(mapToQueueItem(items.get(i), position));
        }

        return new PageImpl<>(mappedContent, pageable, mealOrderItems.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Page<MealQueueItemResponse>> getAllQueuesPaginated(String statusCode, Pageable pageable) {
        log.info("Fetching paginated queues for all meal types, status: {}, page: {}, size: {}",
                statusCode, pageable.getPageNumber(), pageable.getPageSize());

        Map<String, Page<MealQueueItemResponse>> allQueues = new HashMap<>();

        String[] mealTypes = {"BREAKFAST", "LUNCH", "DINNER", "EXTRA"};
        for (String mealType : mealTypes) {
            Page<MealQueueItemResponse> queue = getQueuePaginated(mealType, statusCode, pageable);
            allQueues.put(mealType, queue);
        }

        return allQueues;
    }

    @Transactional(readOnly = true)
    public KitchenQueueResponse getBreakfastQueue() {
        return getQueueByMealType(MealTypeConstants.BREAKFAST, MealStatusConstants.PENDING);
    }

    @Transactional(readOnly = true)
    public KitchenQueueResponse getLunchQueue() {
        return getQueueByMealType(MealTypeConstants.LUNCH, MealStatusConstants.PENDING);
    }

    @Transactional(readOnly = true)
    public KitchenQueueResponse getDinnerQueue() {
        return getQueueByMealType(MealTypeConstants.DINNER, MealStatusConstants.PENDING);
    }

    @Transactional(readOnly = true)
    public KitchenQueueResponse getExtraQueue() {
        return getQueueByMealType(MealTypeConstants.EXTRA, MealStatusConstants.PENDING);
    }

    @Transactional(readOnly = true)
    public KitchenQueueResponse getProcessedMeals(String mealType) {
        return getQueueByMealType(mealType, MealStatusConstants.PROCESSED);
    }

    @Transactional(readOnly = true)
    public List<KitchenQueueResponse> getAllProcessedMeals() {
        log.debug("Getting all processed meal queues");

        List<KitchenQueueResponse> allQueues = new ArrayList<>();

        for (String mealType : MealTypeConstants.ALL_MEAL_TYPES) {
            KitchenQueueResponse queue = getQueueByMealType(mealType, MealStatusConstants.PROCESSED);
            if (queue.getTotalCount() > 0) {
                allQueues.add(queue);
            }
        }

        return allQueues;
    }

    @Transactional(readOnly = true)
    public int getPendingMealCount(String mealType) {
        List<MealOrderItem> items = mealOrderItemRepository
                .findQueueByMealType(MealStatusConstants.PENDING, mealType);
        return items.size();
    }

    @Transactional(readOnly = true)
    public int getTotalPendingMealCount() {
        int total = 0;
        for (String mealType : MealTypeConstants.ALL_MEAL_TYPES) {
            total += getPendingMealCount(mealType);
        }
        return total;
    }

    private MealQueueItemResponse mapToQueueItem(MealOrderItem item, int position) {
        LocalDate today = LocalDate.now();
        String notes = item.getMealOrder().getPatient().getPatientMenus().stream()
                .filter(pm -> pm.getIsActive())
                .filter(pm -> !today.isBefore(pm.getValidFrom()))
                .filter(pm -> pm.getValidUntil() == null || !today.isAfter(pm.getValidUntil()))
                .map(PatientMenu::getNotes)
                .filter(n -> n != null && !n.trim().isEmpty())
                .collect(Collectors.joining(" | "));

        return MealQueueItemResponse.builder()
                .mealItemId(item.getId())
                .uhid(item.getMealOrder().getPatient().getUhid())
                .patientName(item.getMealOrder().getPatient().getName())
                .roomNumber(item.getMealOrder().getPatient().getRoomNumber())
                .mealType(item.getMealType())
                .foodItemName(item.getFoodItem().getName())
                .status(item.getMealStatus().getCode())
                .orderedAt(item.getOrderedAt())
                .processedAt(item.getProcessedAt())
                .processedByStaffName(item.getProcessedByStaff() != null ?
                        item.getProcessedByStaff().getName() : null)
                .queuePosition(position)
                .dieticianNotes(notes.isEmpty() ? null : notes)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MealQueueItemResponse> getQueuePaginatedWithSearch(
            String mealType,
            String statusCode,
            String searchTerm,
            Pageable pageable) {

        log.info("Fetching paginated queue with search - mealType: {}, status: {}, search: {}, page: {}, size: {}",
                mealType, statusCode, searchTerm, pageable.getPageNumber(), pageable.getPageSize());

        LocalDate today = LocalDate.now();

        Page<MealOrderItem> mealOrderItems;

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            mealOrderItems = mealOrderItemRepository.searchQueueByStatusAndMealTypeAndDate(
                    statusCode, mealType, today, searchTerm.trim(), pageable);
        } else {
            mealOrderItems = mealOrderItemRepository.findQueueByStatusAndMealTypeAndDatePaged(
                    statusCode, mealType, today, pageable);
        }

        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();

        List<MealQueueItemResponse> mappedContent = new ArrayList<>();
        List<MealOrderItem> items = mealOrderItems.getContent();
        for (int i = 0; i < items.size(); i++) {
            int position = pageNumber * pageSize + i + 1;
            mappedContent.add(mapToQueueItem(items.get(i), position));
        }

        return new PageImpl<>(mappedContent, pageable, mealOrderItems.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Page<MealQueueItemResponse>> getAllQueuesPaginatedWithSearch(
            String statusCode,
            String searchTerm,
            Pageable pageable) {

        log.info("Fetching all paginated queues with search - status: {}, search: {}, page: {}, size: {}",
                statusCode, searchTerm, pageable.getPageNumber(), pageable.getPageSize());

        Map<String, Page<MealQueueItemResponse>> allQueues = new HashMap<>();

        String[] mealTypes = {"BREAKFAST", "LUNCH", "DINNER", "EXTRA"};
        for (String mealType : mealTypes) {
            Page<MealQueueItemResponse> queue = getQueuePaginatedWithSearch(
                    mealType, statusCode, searchTerm, pageable);
            allQueues.put(mealType, queue);
        }

        return allQueues;
    }
}