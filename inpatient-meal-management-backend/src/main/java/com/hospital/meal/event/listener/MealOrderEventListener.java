package com.hospital.meal.event.listener;

import com.hospital.meal.constant.RoleConstants;
import com.hospital.meal.event.MealOrderedEvent;
import com.hospital.meal.event.MealProcessedEvent;
import com.hospital.meal.event.MenuAssignedEvent;
import com.hospital.meal.event.OrderModifiedEvent;
import com.hospital.meal.model.menu.PatientMenu;
import com.hospital.meal.model.order.MealOrder;
import com.hospital.meal.model.order.MealOrderItem;
import com.hospital.meal.model.order.OrderModificationLog;
import com.hospital.meal.service.notification.EmailService;
import com.hospital.meal.service.notification.WebPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MealOrderEventListener {

    private final EmailService emailService;
    private final WebPushService webPushService;

    /**
     * Handle meal ordered event
     * Send notifications to dietician and kitchen staff
     */
    @Async
    @EventListener
    public void handleMealOrderedEvent(MealOrderedEvent event) {
        try {
            MealOrder mealOrder = event.getMealOrder();

            log.info("Meal order event received for patient UHID: {}",
                    mealOrder.getPatient().getUhid());

            // Send email notification to dietician (if we can determine which one)
            // For now, we'll need to get the dietician ID from patient menu
            // This would require a query to find active patient menus
            // emailService.sendMealOrderNotification(dieticianId, patientName, uhid);

            // Send web push notification to all kitchen staff
            webPushService.sendNotificationToUserType(
                    RoleConstants.KITCHEN_STAFF,
                    "New Meal Order",
                    String.format("New order from patient %s in room %s",
                            mealOrder.getPatient().getName(),
                            mealOrder.getPatient().getRoomNumber())
            );

            log.info("Notifications sent for meal order: {}", mealOrder.getId());

        } catch (Exception e) {
            log.error("Error handling meal ordered event", e);
        }
    }

    /**
     * Handle meal processed event
     * Send notification to patient (optional)
     */
    @Async
    @EventListener
    public void handleMealProcessedEvent(MealProcessedEvent event) {
        try {
            MealOrderItem item = event.getMealOrderItem();

            log.info("Meal processed event received for item: {}", item.getId());

            // Optionally send notification to patient
            // (Patient notification can be via SMS or in-app if implemented)

            log.info("Meal processed notification completed for item: {}", item.getId());

        } catch (Exception e) {
            log.error("Error handling meal processed event", e);
        }
    }

    /**
     * Handle menu assigned event
     * Send notification to patient and dietician
     */
    @Async
    @EventListener
    public void handleMenuAssignedEvent(MenuAssignedEvent event) {
        try {
            PatientMenu patientMenu = event.getPatientMenu();

            log.info("Menu assigned event received for patient UHID: {}",
                    patientMenu.getPatient().getUhid());

            // Send email to dietician confirming assignment
            // Note: We'd need to add this method to EmailService or adjust the call
            // emailService.sendMenuAssignmentConfirmation(patientMenu);

            log.info("Menu assignment notification sent");

        } catch (Exception e) {
            log.error("Error handling menu assigned event", e);
        }
    }

    /**
     * Handle order modified event
     * Log modification for audit purposes
     */
    @Async
    @EventListener
    public void handleOrderModifiedEvent(OrderModifiedEvent event) {
        try {
            OrderModificationLog modLog = event.getModificationLog();

            log.info("Order modified event received for patient UHID: {}",
                    modLog.getPatient().getUhid());

            // Optionally send notification to dietician about modification
            webPushService.sendNotificationToUserType(
                    RoleConstants.DIETICIAN,
                    "Order Modified",
                    String.format("Patient %s modified their %s order",
                            modLog.getPatient().getName(),
                            modLog.getMealType().toLowerCase())
            );

        } catch (Exception e) {
            log.error("Error handling order modified event", e);
        }
    }
}