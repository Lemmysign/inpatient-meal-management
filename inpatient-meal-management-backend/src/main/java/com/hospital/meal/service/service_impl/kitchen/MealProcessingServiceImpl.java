package com.hospital.meal.service.service_impl.kitchen;

import com.hospital.meal.constant.MealStatusConstants;
import com.hospital.meal.dto.kitchen.MealQueueItemResponse;
import com.hospital.meal.exception.ConcurrencyException;
import com.hospital.meal.exception.MealProcessingException;
import com.hospital.meal.exception.ResourceNotFoundException;
import com.hospital.meal.mapper.KitchenStaffMapper;
import com.hospital.meal.model.order.MealOrderItem;
import com.hospital.meal.model.status.MealStatus;
import com.hospital.meal.model.user.KitchenStaff;
import com.hospital.meal.repository.KitchenStaffRepository;
import com.hospital.meal.repository.MealOrderItemRepository;
import com.hospital.meal.repository.MealStatusRepository;
import com.hospital.meal.service.kitchen.MealProcessingService;
import com.hospital.meal.util.DateUtil;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MealProcessingServiceImpl implements MealProcessingService {

    private final MealOrderItemRepository mealOrderItemRepository;
    private final MealStatusRepository mealStatusRepository;
    private final KitchenStaffRepository kitchenStaffRepository;
    private final KitchenStaffMapper kitchenStaffMapper;

    @Override
    @Transactional
    public MealQueueItemResponse processMeal(UUID mealItemId, UUID kitchenStaffId) {
        log.info("Processing meal item: {} by staff: {}", mealItemId, kitchenStaffId);

        try {
            // Load meal item (optimistic lock will be checked on save)
            MealOrderItem item = mealOrderItemRepository.findById(mealItemId)
                    .orElseThrow(() -> new ResourceNotFoundException("Meal Item", "id", mealItemId));

            // Check current status
            String currentStatus = item.getMealStatus().getCode();
            if (!MealStatusConstants.PENDING.equals(currentStatus)) {
                throw new MealProcessingException(
                        "Meal cannot be processed. Current status: " + currentStatus);
            }

            // Get kitchen staff
            KitchenStaff staff = kitchenStaffRepository.findById(kitchenStaffId)
                    .orElseThrow(() -> new ResourceNotFoundException("Kitchen Staff", "id", kitchenStaffId));

            // Get PROCESSING status
            MealStatus processingStatus = mealStatusRepository.findByCode(MealStatusConstants.PROCESSING)
                    .orElseThrow(() -> new IllegalStateException("PROCESSING status not found"));

            // Transition to PROCESSING
            item.setMealStatus(processingStatus);
            item.setProcessedByStaff(staff);

            // Save with optimistic lock check
            item = mealOrderItemRepository.save(item);

            log.info("Meal item transitioned to PROCESSING");

            // Get PROCESSED status
            MealStatus processedStatus = mealStatusRepository.findByCode(MealStatusConstants.PROCESSED)
                    .orElseThrow(() -> new IllegalStateException("PROCESSED status not found"));

            // Transition to PROCESSED
            item.setMealStatus(processedStatus);
            item.setProcessedAt(DateUtil.getCurrentDateTime());

            // Save final state
            item = mealOrderItemRepository.save(item);

            log.info("Meal item processed successfully: {}", mealItemId);

            // TODO: Fire MealProcessedEvent

            return kitchenStaffMapper.toMealQueueItemResponse(item, null);

        } catch (OptimisticLockException e) {
            log.error("Concurrent modification detected for meal item: {}", mealItemId);
            throw new ConcurrencyException(
                    "This meal is being processed by another staff member. Please refresh the queue.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canProcessMeal(UUID mealItemId) {
        return mealOrderItemRepository.findById(mealItemId)
                .map(item -> MealStatusConstants.PENDING.equals(item.getMealStatus().getCode()))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public String getMealStatus(UUID mealItemId) {
        return mealOrderItemRepository.findById(mealItemId)
                .map(item -> item.getMealStatus().getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Meal Item", "id", mealItemId));
    }
}