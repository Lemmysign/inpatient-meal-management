const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:9096/api/v1';

export const config = {
  apiUrl: API_BASE_URL,

  // Patient Authentication
  patientLoginUrl:         `${API_BASE_URL}/auth/patient/login`,
  patientLogoutUrl:        `${API_BASE_URL}/auth/logout`,
  patientDetailsUrl:       `${API_BASE_URL}/auth/patient/me`,
  patientProfileUrl:       `${API_BASE_URL}/patient/profile`,
  patientMenuUrl:          `${API_BASE_URL}/patient/menu`,
  patientOrderUrl:         `${API_BASE_URL}/patient/orders`,
  patientAlacarteOrderUrl: `${API_BASE_URL}/patient/orders/alacarte`,
  patientTodayOrderUrl:    `${API_BASE_URL}/patient/orders/today`,
  patientAlaCarteUrl:      `${API_BASE_URL}/patient/alacarte`,
  hisStatusUrl: `${API_BASE_URL}/auth/his-mode`,

  // Dietician Authentication
  dieticianLoginUrl:          `${API_BASE_URL}/auth/login`,
  dieticianSetPasswordUrl:    `${API_BASE_URL}/auth/set-password`,
  dieticianDashboardUrl:      `${API_BASE_URL}/dietician/dashboard`,
  dieticianTotalFoodItemsUrl: `${API_BASE_URL}/dietician/dashboard/total-food-items`,
  dieticianPatientsUrl:       `${API_BASE_URL}/dietician/main-patient-menus`,
  dieticianLogoutUrl:         `${API_BASE_URL}/auth/logout`,

  // Dietician Patient Menu
  dieticianPatientMenuUrl: (patientMenuId: string) =>
    `${API_BASE_URL}/dietician/patient-menus/${patientMenuId}`,

  // Dietician Assign Menu
  dieticianAssignMenuUrl: `${API_BASE_URL}/dietician/patient-menus/assign`,

  // Dietician Menu Groups
  dieticianMenuGroupsPagedUrl: `${API_BASE_URL}/dietician/menu-groups/paged`,
  dieticianMenuGroupsUrl:      `${API_BASE_URL}/dietician/menu-groups`,
  dieticianMenuGroupUrl: (menuGroupId: string) =>
    `${API_BASE_URL}/dietician/menu-groups/${menuGroupId}`,

  // Dietician Food Items
  dieticianFoodItemsUrl:      `${API_BASE_URL}/dietician/food-items`,
  dieticianFoodItemSearchUrl: `${API_BASE_URL}/dietician/food-items/search`,
  dieticianFoodItemUrl: (foodItemId: string) =>
    `${API_BASE_URL}/dietician/food-items/${foodItemId}`,

  // Dietician Filters
  dieticianMenuGroupFiltersUrl: `${API_BASE_URL}/dietician/filters/menu-groups`,

  // Kitchen Staff Authentication
  kitchenLoginUrl:          `${API_BASE_URL}/auth/login`,
  kitchenLogoutUrl:         `${API_BASE_URL}/auth/logout`,
  kitchenDashboardUrl:      `${API_BASE_URL}/kitchen/dashboard`,
  kitchenBreakfastQueueUrl: `${API_BASE_URL}/kitchen/queue/breakfast/paged`,
  kitchenLunchQueueUrl:     `${API_BASE_URL}/kitchen/queue/lunch/paged`,
  kitchenDinnerQueueUrl:    `${API_BASE_URL}/kitchen/queue/dinner/paged`,
  kitchenAlaCarteQueueUrl:  `${API_BASE_URL}/kitchen/queue/EXTRA/paged`,
  kitchenProcessMealUrl:    `${API_BASE_URL}/kitchen/process`,
  kitchenPrintLabelUrl: (mealItemId: string) =>
    `${API_BASE_URL}/kitchen/print/${mealItemId}`,

  // Admin Authentication
  adminLoginUrl:              `${API_BASE_URL}/auth/login`,
  adminLogoutUrl:             `${API_BASE_URL}/auth/logout`,
  adminDashboardTodayUrl:     `${API_BASE_URL}/admin/dashboard/today`,
  adminDashboardWeeklyUrl:    `${API_BASE_URL}/admin/dashboard/weekly`,
  adminDashboardMonthlyUrl:   `${API_BASE_URL}/admin/dashboard/monthly`,
  adminDashboardRangeUrl:     `${API_BASE_URL}/admin/dashboard/range`,

  // Admin — Dieticians
  adminDieticiansUrl:      `${API_BASE_URL}/admin/dieticians`,
  adminDieticianSearchUrl: `${API_BASE_URL}/admin/dieticians/search`,
  adminDieticianStatusUrl: (id: string) =>
    `${API_BASE_URL}/admin/dieticians/${id}/status`,
  adminResendInviteUrl: (id: string) =>
    `${API_BASE_URL}/admin/dieticians/${id}/resend-invite`,

  // Admin — Servers (Kitchen Staff)
  adminServersUrl:       `${API_BASE_URL}/admin/kitchen-staff`,
  adminServerSearchUrl:  `${API_BASE_URL}/admin/kitchen-staff/search`,
  adminServerStatusUrl: (id: string) =>
    `${API_BASE_URL}/admin/kitchen-staff/${id}/status`,

  // Password Reset (shared across Admin, Dietician & Kitchen Staff)
  forgotPasswordUrl: `${API_BASE_URL}/auth/forgot-password`,
  resetPasswordUrl:  `${API_BASE_URL}/auth/reset-password`,
};