package com.hospital.meal.service.service_impl.patient;

import com.hospital.meal.constant.MealStatusConstants;
import com.hospital.meal.constant.MealTypeConstants;
import com.hospital.meal.dto.patient.*;
import com.hospital.meal.exception.*;
import com.hospital.meal.mapper.MealOrderMapper;
import com.hospital.meal.model.menu.FoodItem;
import com.hospital.meal.model.menu.MenuGroup;
import com.hospital.meal.model.menu.PatientMenu;
import com.hospital.meal.model.order.MealOrder;
import com.hospital.meal.model.order.MealOrderItem;
import com.hospital.meal.model.order.OrderModificationLog;
import com.hospital.meal.model.status.MealStatus;
import com.hospital.meal.model.user.Patient;
import com.hospital.meal.repository.*;
import com.hospital.meal.service.patient.MealOrderService;
import com.hospital.meal.service.patient.PatientService;
import com.hospital.meal.util.DateUtil;
import com.hospital.meal.util.IdempotencyKeyGenerator;
import com.hospital.meal.util.MealOrderingTimeValidator;
import com.hospital.meal.util.MealTimeValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import com.hospital.meal.service.notification.WebPushService;

@Service
@RequiredArgsConstructor
@Slf4j
public class MealOrderServiceImpl implements MealOrderService {

    private final PatientService patientService;
    private final PatientMenuRepository patientMenuRepository;
    private final FoodItemRepository foodItemRepository;
    private final MealOrderRepository mealOrderRepository;
    private final MealOrderItemRepository mealOrderItemRepository;
    private final MealStatusRepository mealStatusRepository;
    private final OrderModificationLogRepository orderModificationLogRepository;
    private final MealOrderMapper mealOrderMapper;
    private final MealTimeValidator mealTimeValidator;
    private final MealOrderingTimeValidator mealOrderingTimeValidator;
    private final MenuGroupRepository menuGroupRepository;
    private final WebPushService webPushService;

    @Override
    @Transactional(readOnly = true)
    public AvailableMenuResponse getAvailableMenu(String uhid, LocalDate date) {
        log.info("Getting available menu for UHID: {} on date: {}", uhid, date);

        // Get patient
        Patient patient = patientService.getPatientByUhid(uhid)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "UHID", uhid));

        // Get active menus for patient on this date
        List<PatientMenu> activeMenus = patientMenuRepository
                .findActiveMenusByUhidAndDate(uhid, date);

        if (activeMenus.isEmpty()) {
            throw new ResourceNotFoundException("No active menu found for patient: " + uhid);
        }

        // Get all menu groups (patient's assigned menus)
        List<UUID> menuGroupIds = activeMenus.stream()
                .map(pm -> pm.getMenuGroup().getId())
                .collect(Collectors.toList());

        // ========== ADD À LA CARTE MENU GROUP ==========
        menuGroupRepository.findAlacarteMenuGroup()
                .ifPresent(mg -> menuGroupIds.add(mg.getId()));
        // ================================================

        // Get all food items for these menu groups
        List<FoodItem> foodItems = foodItemRepository.findByMenuGroupIds(menuGroupIds);

        // Group food items by menu group
        Map<UUID, List<FoodItem>> foodItemsByMenuGroup = foodItems.stream()
                .collect(Collectors.groupingBy(fi -> fi.getMenuGroup().getId()));

        // Build response
        List<AvailableMenuResponse.MenuCategory> categories = new ArrayList<>();

        // Process patient's assigned menus
        for (PatientMenu menu : activeMenus) {
            MenuGroup menuGroup = menu.getMenuGroup();
            List<FoodItem> menuFoodItems = foodItemsByMenuGroup.getOrDefault(menuGroup.getId(), new ArrayList<>());

            // Group by meal type
            List<AvailableMenuResponse.FoodOption> breakfastOptions = menuFoodItems.stream()
                    .filter(fi -> fi.getMealType() == null || MealTypeConstants.BREAKFAST.equals(fi.getMealType()))
                    .map(this::toFoodOption)
                    .collect(Collectors.toList());

            List<AvailableMenuResponse.FoodOption> lunchOptions = menuFoodItems.stream()
                    .filter(fi -> fi.getMealType() == null || MealTypeConstants.LUNCH.equals(fi.getMealType()))
                    .map(this::toFoodOption)
                    .collect(Collectors.toList());

            List<AvailableMenuResponse.FoodOption> dinnerOptions = menuFoodItems.stream()
                    .filter(fi -> fi.getMealType() == null || MealTypeConstants.DINNER.equals(fi.getMealType()))
                    .map(this::toFoodOption)
                    .collect(Collectors.toList());

            List<AvailableMenuResponse.FoodOption> extraOptions = menuFoodItems.stream()
                    .filter(fi -> MealTypeConstants.EXTRA.equals(fi.getMealType()))
                    .map(this::toFoodOption)
                    .collect(Collectors.toList());

            AvailableMenuResponse.MenuCategory category = AvailableMenuResponse.MenuCategory.builder()
                    .menuGroupId(menuGroup.getId())
                    .menuGroupName(menuGroup.getName())
                    .description(menuGroup.getDescription())
                    .breakfastOptions(breakfastOptions)
                    .lunchOptions(lunchOptions)
                    .dinnerOptions(dinnerOptions)
                    .extraOptions(extraOptions)
                    .build();

            categories.add(category);
        }

        // ========== ADD À LA CARTE AS SEPARATE CATEGORY ==========
        menuGroupRepository.findAlacarteMenuGroup().ifPresent(alacarteMenuGroup -> {
            List<FoodItem> alacarteItems = foodItemsByMenuGroup.getOrDefault(
                    alacarteMenuGroup.getId(), new ArrayList<>());

            if (!alacarteItems.isEmpty()) {
                List<AvailableMenuResponse.FoodOption> alacarteOptions = alacarteItems.stream()
                        .map(this::toFoodOption)
                        .collect(Collectors.toList());

                AvailableMenuResponse.MenuCategory alacarteCategory = AvailableMenuResponse.MenuCategory.builder()
                        .menuGroupId(alacarteMenuGroup.getId())
                        .menuGroupName("À la carte")
                        .description("Special items available outside your diet plan - order anytime!")
                        .breakfastOptions(new ArrayList<>())
                        .lunchOptions(new ArrayList<>())
                        .dinnerOptions(new ArrayList<>())
                        .extraOptions(alacarteOptions)  // All à la carte items in "extra"
                        .build();

                categories.add(alacarteCategory);
                log.info("Added {} à la carte items to menu", alacarteOptions.size());
            }
        });
        // ==========================================================

        return AvailableMenuResponse.builder()
                .patientName(patient.getName())
                .uhid(patient.getUhid())
                .roomNumber(patient.getRoomNumber())
                .menuCategories(categories)
                .build();
    }

    @Override
    @Transactional
    public MealOrderResponse createMealOrder(String uhid, CreateMealOrderRequest request) {
        log.info("Creating meal order for UHID: {}", uhid);

        // Get patient
        Patient patient = patientService.getPatientByUhid(uhid)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "UHID", uhid));

        LocalDate today = DateUtil.getCurrentDate();
        LocalTime currentTime = LocalTime.now();

        // ========== TIME-BASED VALIDATION ==========

        if (!mealOrderingTimeValidator.isOrderingAllowed(currentTime)) {
            String message = mealOrderingTimeValidator.getOrderingWindowMessage(currentTime);
            String nextWindow = mealOrderingTimeValidator.getNextOrderingWindow(currentTime);
            throw new OrderingNotAllowedException(message, nextWindow);
        }

        List<String> requiredMealTypes = mealOrderingTimeValidator.getRequiredMealTypes(currentTime);

        if (requiredMealTypes.isEmpty()) {
            String message = mealOrderingTimeValidator.getOrderingWindowMessage(currentTime);
            String nextWindow = mealOrderingTimeValidator.getNextOrderingWindow(currentTime);
            throw new OrderingNotAllowedException(message, nextWindow);
        }

        List<String> providedMealTypes = request.getMeals().stream()
                .map(MealSelectionRequest::getMealType)
                .distinct()
                .collect(Collectors.toList());

        if (!providedMealTypes.containsAll(requiredMealTypes)) {
            log.warn("Patient did not provide all required meals. Required: {}, Provided: {}",
                    requiredMealTypes, providedMealTypes);
            throw new InvalidMealSelectionException(requiredMealTypes, providedMealTypes);
        }

        for (String providedMeal : providedMealTypes) {
            if (!requiredMealTypes.contains(providedMeal)) {
                throw new InvalidMealSelectionException(
                        String.format("You cannot order %s at this time. Only allowed: %s",
                                providedMeal, String.join(", ", requiredMealTypes))
                );
            }
        }

        log.info("Meal selection validated. Required: {}, Provided: {}",
                requiredMealTypes, providedMealTypes);

        // ========== FIXED: CHECK FOR EXISTING ORDER TODAY ==========

        // Check if order already exists for TODAY (proper date filtering)
        // Check if DIET PLAN order already exists for TODAY
// (À la carte orders should not block diet plan orders)
        Optional<MealOrder> existingOrder = mealOrderRepository.findByUhidAndOrderDate(uhid, today);

        if (existingOrder.isPresent()) {
            // Check if the existing order has any BREAKFAST, LUNCH, or DINNER items
            boolean hasDietPlanMeals = existingOrder.get().getItems().stream()
                    .anyMatch(item ->
                            item.getMealType().equals(MealTypeConstants.BREAKFAST) ||
                                    item.getMealType().equals(MealTypeConstants.LUNCH) ||
                                    item.getMealType().equals(MealTypeConstants.DINNER)
                    );

            if (hasDietPlanMeals) {
                log.warn("Diet plan order already exists for UHID {} on {}", uhid, today);
                throw new DuplicateResourceException("You have already placed a diet plan order for today. You can modify your existing order if needed.");
            }

            // If order exists but only has EXTRA items, continue and add diet plan meals to it
            log.info("Existing order has only à la carte items. Adding diet plan meals to order.");
        }

        // Generate idempotency key
        String idempotencyKey = IdempotencyKeyGenerator.generateOrderKey(uhid, today);

        // ========== END FIX ==========

        // Validate patient has active menu
        List<PatientMenu> activeMenus = patientMenuRepository
                .findActiveMenusByUhidAndDate(uhid, today);

        if (activeMenus.isEmpty()) {
            throw new InvalidRequestException("No active menu assigned. Please contact dietician.");
        }

        // Get valid food item IDs from active menus
        Set<UUID> validFoodItemIds = foodItemRepository
                .findByMenuGroupIds(activeMenus.stream()
                        .map(pm -> pm.getMenuGroup().getId())
                        .collect(Collectors.toList()))
                .stream()
                .map(FoodItem::getId)
                .collect(Collectors.toSet());

        // Validate all selected food items are in patient's menu
        for (MealSelectionRequest selection : request.getMeals()) {
            if (!validFoodItemIds.contains(selection.getFoodItemId())) {
                throw new InvalidRequestException(
                        "Food item not available in your assigned menu: " + selection.getFoodItemId());
            }
        }

        // Get PENDING status
        MealStatus pendingStatus = mealStatusRepository.findByCode(MealStatusConstants.PENDING)
                .orElseThrow(() -> new IllegalStateException("PENDING status not found in database"));

        // Get or use existing meal order
        MealOrder mealOrder = existingOrder.orElseGet(() -> {
            // Create new order if none exists
            MealOrder newOrder = MealOrder.builder()
                    .patient(patient)
                    .uhid(patient.getUhid())
                    .orderDate(today)
                    .idempotencyKey(idempotencyKey)
                    .items(new ArrayList<>())
                    .build();
            return mealOrderRepository.save(newOrder);
        });

        mealOrder = mealOrderRepository.save(mealOrder);

        // Create meal order items
        LocalDateTime now = DateUtil.getCurrentDateTime();
        MealOrder finalMealOrder = mealOrder;

        for (MealSelectionRequest selection : request.getMeals()) {
            FoodItem foodItem = foodItemRepository.findById(selection.getFoodItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Food Item", "id", selection.getFoodItemId()));

            MealOrderItem item = MealOrderItem.builder()
                    .mealOrder(finalMealOrder)
                    .foodItem(foodItem)
                    .mealType(selection.getMealType())
                    .mealStatus(pendingStatus)
                    .orderedAt(now)
                    .build();

            mealOrder.getItems().add(item);
        }

        mealOrderItemRepository.saveAll(mealOrder.getItems());

        log.info("Meal order created successfully with {} items", mealOrder.getItems().size());

// ── Notify kitchen staff of new diet plan order ──────────────────────────
        webPushService.sendNotificationToUserType(
                "KITCHEN_STAFF",
                "🍽️ New Diet Plan Order",
                "Patient " + patient.getName() + " (Room " + patient.getRoomNumber() + ") has placed a meal order."
        );
        return mealOrderMapper.toResponse(mealOrder);
    }



    @Override
    @Transactional(readOnly = true)
    public MealOrderResponse getTodaysMealOrder(String uhid) {
        log.info("Getting today's meal order for UHID: {}", uhid);

        LocalDate today = DateUtil.getCurrentDate();

        MealOrder order = mealOrderRepository.findByUhidAndOrderDate(uhid, today)
                .orElseThrow(() -> new ResourceNotFoundException("No meal order found for today"));

        return mealOrderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MealOrderResponse> getMealOrderHistory(String uhid) {
        log.info("Getting meal order history for UHID: {}", uhid);

        List<MealOrder> orders = mealOrderRepository.findByUhid(uhid);

        return orders.stream()
                .map(mealOrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MealOrderResponse modifyMeal(String uhid, String mealType, UUID newFoodItemId, String reason) {
        log.info("Modifying {} for UHID: {} to food item: {}", mealType, uhid, newFoodItemId);

        // Get patient
        Patient patient = patientService.getPatientByUhid(uhid)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "UHID", uhid));

        LocalDate today = DateUtil.getCurrentDate();

        // Get today's order
        MealOrder order = mealOrderRepository.findByUhidAndOrderDate(uhid, today)
                .orElseThrow(() -> new ResourceNotFoundException("No meal order found for today"));

        // Check cutoff time
        if (!mealTimeValidator.canModifyMeal(mealType)) {
            throw new MealModificationNotAllowedException(mealType,
                    mealTimeValidator.getClass().toString());
        }

        // Check if any meal in order has been processed
        boolean anyProcessed = order.getItems().stream()
                .anyMatch(item -> MealStatusConstants.PROCESSED.equals(item.getMealStatus().getCode()));

        if (anyProcessed) {
            throw new MealModificationNotAllowedException(
                    "Cannot modify order - one or more meals have been processed");
        }

        // Find meal items of this type
        List<MealOrderItem> mealItems = mealOrderItemRepository
                .findByMealOrderIdAndMealType(order.getId(), mealType);

        if (mealItems.isEmpty()) {
            throw new ResourceNotFoundException("Meal item not found for type: " + mealType);
        }

        // Get new food item
        FoodItem newFoodItem = foodItemRepository.findById(newFoodItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Food Item", "id", newFoodItemId));

        // Modify the first meal item of this type
        MealOrderItem oldItem = mealItems.get(0);
        FoodItem oldFoodItem = oldItem.getFoodItem();

        // Create modification log
        OrderModificationLog modLog = OrderModificationLog.builder()
                .patient(patient)
                .mealOrder(order)
                .mealOrderItem(oldItem)
                .mealType(mealType)
                .oldFoodItem(oldFoodItem)
                .newFoodItem(newFoodItem)
                .modifiedAt(DateUtil.getCurrentDateTime())
                .modificationReason(reason)
                .build();

        orderModificationLogRepository.save(modLog);

        // Update meal item
        oldItem.setFoodItem(newFoodItem);
        mealOrderItemRepository.save(oldItem);

        log.info("Meal modified successfully");

        return mealOrderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canModifyMeal(String uhid, String mealType) {
        LocalDate today = DateUtil.getCurrentDate();

        Optional<MealOrder> orderOpt = mealOrderRepository.findByUhidAndOrderDate(uhid, today);
        if (orderOpt.isEmpty()) {
            return false;
        }

        MealOrder order = orderOpt.get();

        // Check if any meal processed
        boolean anyProcessed = order.getItems().stream()
                .anyMatch(item -> MealStatusConstants.PROCESSED.equals(item.getMealStatus().getCode()));

        if (anyProcessed) {
            return false;
        }

        // Check cutoff time
        return mealTimeValidator.canModifyMeal(mealType);
    }

    private AvailableMenuResponse.FoodOption toFoodOption(FoodItem foodItem) {
        return AvailableMenuResponse.FoodOption.builder()
                .foodItemId(foodItem.getId())
                .name(foodItem.getName())
                .description(foodItem.getDescription())
                .mealType(foodItem.getMealType())
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public List<AvailableMenuResponse.FoodOption> getAlacarteMenu() {
        log.info("Fetching à la carte menu items");

        // Get à la carte menu group
        MenuGroup alacarteMenuGroup = menuGroupRepository.findAlacarteMenuGroup()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "À la carte menu not configured. Please contact administrator."));

        // Get all food items from à la carte menu
        List<FoodItem> foodItems = foodItemRepository.findByMenuGroupId(alacarteMenuGroup.getId());

        if (foodItems.isEmpty()) {
            log.warn("À la carte menu has no items");
        }

        return foodItems.stream()
                .map(this::toFoodOption)
                .collect(Collectors.toList());
    }



    @Override
    @Transactional
    public MealOrderResponse createAlacarteOrder(String uhid, AlacarteOrderRequest request) {
        log.info("Creating à la carte order for UHID: {} with {} items", uhid, request.getFoodItemIds().size());

        // Get patient
        Patient patient = patientService.getPatientByUhid(uhid)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "UHID", uhid));

        LocalDate today = DateUtil.getCurrentDate();

        // ========== NO TIME RESTRICTIONS FOR À LA CARTE ==========
        // ========== NO MEAL TYPE REQUIREMENTS ==========

        // Get à la carte menu group
        MenuGroup alacarteMenuGroup = menuGroupRepository.findAlacarteMenuGroup()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "À la carte menu not available. Please contact administrator."));

        // Validate all selected items are from à la carte menu
        List<FoodItem> alacarteMenuItems = foodItemRepository.findByMenuGroupId(alacarteMenuGroup.getId());
        Set<UUID> validAlacarteIds = alacarteMenuItems.stream()
                .map(FoodItem::getId)
                .collect(Collectors.toSet());

        for (UUID foodItemId : request.getFoodItemIds()) {
            if (!validAlacarteIds.contains(foodItemId)) {
                throw new InvalidRequestException(
                        "Food item not found in à la carte menu: " + foodItemId);
            }
        }

        // Get or create today's meal order
        MealOrder mealOrder = mealOrderRepository.findByUhidAndOrderDate(uhid, today)
                .orElseGet(() -> {
                    log.info("No existing order for today, creating new order for UHID: {}", uhid);
                    // Create new order if none exists
                    String idempotencyKey = IdempotencyKeyGenerator.generateOrderKey(uhid, today);
                    MealOrder newOrder = MealOrder.builder()
                            .patient(patient)
                            .uhid(patient.getUhid())
                            .orderDate(today)
                            .idempotencyKey(idempotencyKey)
                            .items(new ArrayList<>())
                            .build();
                    return mealOrderRepository.save(newOrder);
                });

        // Get PENDING status
        MealStatus pendingStatus = mealStatusRepository.findByCode(MealStatusConstants.PENDING)
                .orElseThrow(() -> new IllegalStateException("PENDING status not found in database"));

        // Create meal order items for à la carte selections
        LocalDateTime now = DateUtil.getCurrentDateTime();
        List<MealOrderItem> newItems = new ArrayList<>();

        for (UUID foodItemId : request.getFoodItemIds()) {
            FoodItem foodItem = foodItemRepository.findById(foodItemId)
                    .orElseThrow(() -> new ResourceNotFoundException("Food Item", "id", foodItemId));

            MealOrderItem item = MealOrderItem.builder()
                    .mealOrder(mealOrder)
                    .foodItem(foodItem)
                    .mealType(MealTypeConstants.EXTRA)  // All à la carte items are EXTRA
                    .mealStatus(pendingStatus)
                    .orderedAt(now)
                    .build();

            newItems.add(item);
        }

        // Save new items
        mealOrderItemRepository.saveAll(newItems);

        // Add to order's item list
        mealOrder.getItems().addAll(newItems);

        log.info("À la carte order created successfully with {} items for UHID: {}", newItems.size(), uhid);

// ── Notify kitchen staff of new à la carte order ─────────────────────────
        webPushService.sendNotificationToUserType(
                "KITCHEN_STAFF",
                "🛒 New À La Carte Order",
                "Patient " + patient.getName() + " (Room " + patient.getRoomNumber() + ") ordered à la carte items."
        );

        return mealOrderMapper.toResponse(mealOrder);
    }


}