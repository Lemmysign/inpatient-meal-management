import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { config } from '../../config/config';
import evercarelogo from '../../assets/icons/ec_logo.png';
import '../../styles/patient/patientdashboard.css';

interface FoodItem {
  foodItemId: string;
  name: string;
  description: string;
  mealType: 'BREAKFAST' | 'LUNCH' | 'DINNER' | 'EXTRA';
}

interface MenuCategory {
  menuGroupId: string;
  menuGroupName: string;
  description: string;
  breakfastOptions: FoodItem[];
  lunchOptions: FoodItem[];
  dinnerOptions: FoodItem[];
  extraOptions: FoodItem[];
}

interface MenuData {
  patientName: string;
  uhid: string;
  roomNumber: string;
  menuCategories: MenuCategory[];
}

interface MenuResponse {
  success: boolean;
  message: string;
  data: MenuData;
  timestamp: string;
}

interface PatientProfile {
  uhid: string;
  name: string;
  roomNumber: string;
}

interface ProfileResponse {
  success: boolean;
  message: string;
  data: PatientProfile;
  timestamp: string;
}

interface OrderMealItem {
  mealType: 'BREAKFAST' | 'LUNCH' | 'DINNER' | 'EXTRA';
  foodItemId: string;
}

interface OrderResponse {
  success: boolean;
  message: string;
  data: any;
  timestamp: string;
}

// ── Order History Types ──
interface HistoryMealItem {
  mealItemId: string;
  mealType: 'BREAKFAST' | 'LUNCH' | 'DINNER' | 'EXTRA';  // ✅ Add 'EXTRA'
  foodItemName: string;
  foodDescription: string;
  status: 'PENDING' | 'PROCESSED';
  orderedAt: string;
  processedAt: string | null;
  canModify: boolean;
}

interface TodayOrder {
  orderId: string;
  uhid: string;
  patientName: string;
  roomNumber: string;
  orderDate: string;
  meals: HistoryMealItem[];
  createdAt: string;
}

interface TodayOrderResponse {
  success: boolean;
  message: string;
  data: TodayOrder;
  timestamp: string;
}

// ── À La Carte Types ──
interface AlaCarteFoodItem {
  foodItemId: string;
  name: string;
  description: string;
  mealType: null;
}

interface AlaCarteResponse {
  success: boolean;
  message: string;
  data: AlaCarteFoodItem[];
  timestamp: string;
}

const MEAL_ORDER: HistoryMealItem['mealType'][] = ['BREAKFAST', 'LUNCH', 'DINNER', 'EXTRA'];

const STATUS_CONFIG: Record<HistoryMealItem['status'], { label: string; className: string }> = {
  PENDING: { label: 'Pending', className: 'status-pending' },
  PROCESSED: { label: 'Processed', className: 'status-processed' },
};

const formatOrderedAt = (iso: string): string => {
  try {
    const date = new Date(iso);
    return date.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: true,
    });
  } catch {
    return iso;
  }
};

const PatientDashboard: React.FC = () => {
  const navigate = useNavigate();
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [activeView, setActiveView] = useState<'diet' | 'alacarte' | 'history'>('diet');
  const [activeMealTime, setActiveMealTime] = useState<'breakfast' | 'lunch' | 'dinner'>('breakfast');
  const [patientProfile, setPatientProfile] = useState<PatientProfile | null>(null);
  const [menuData, setMenuData] = useState<MenuData | null>(null);
  const [isLoadingProfile, setIsLoadingProfile] = useState(true);
  const [isLoadingMenu, setIsLoadingMenu] = useState(true);
  const [isPlacingOrder, setIsPlacingOrder] = useState(false);
  const [showSuccessPopup, setShowSuccessPopup] = useState(false);
  const [showErrorPopup, setShowErrorPopup] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [hasNewOrder, setHasNewOrder] = useState(false);

  // Order History state
  const [todayOrder, setTodayOrder] = useState<TodayOrder | null>(null);
  const [isLoadingHistory, setIsLoadingHistory] = useState(false);
  const [historyError, setHistoryError] = useState<string | null>(null);

  // À La Carte state
  const [alaCarteMenu, setAlaCarteMenu] = useState<AlaCarteFoodItem[]>([]);
  const [isLoadingAlaCarteMenu, setIsLoadingAlaCarteMenu] = useState(false);

  // Time-based ordering state
  const [currentTime, setCurrentTime] = useState(new Date());
  const [requiredMealTypes, setRequiredMealTypes] = useState<string[]>([]);
  const [orderingMessage, setOrderingMessage] = useState('');
  const [isOrderingAllowed, setIsOrderingAllowed] = useState(true);

  // Selections
  const [dietSelections, setDietSelections] = useState<Record<string, string | null>>({
    breakfast: null,
    lunch: null,
    dinner: null,
  });

  const [alaCarteSelections, setAlaCarteSelections] = useState<string[]>([]);

  const showError = (message: string) => {
    setErrorMessage(message);
    setShowErrorPopup(true);
    setTimeout(() => setShowErrorPopup(false), 3000);
  };

  // Update current time every minute
  useEffect(() => {
    const timer = setInterval(() => setCurrentTime(new Date()), 60000);
    return () => clearInterval(timer);
  }, []);

  // Calculate required meal types based on current time
  useEffect(() => {
    const hour = currentTime.getHours();
    const minute = currentTime.getMinutes();
    const timeInMinutes = hour * 60 + minute;

    const morningStart = 21 * 60;
    const morningEnd = 10 * 60;
    const lunchStart = 10 * 60;
    const lunchEnd = 15 * 60;
    const dinnerStart = 17 * 60;
    const dinnerEnd = 19 * 60;

    const isInMorningWindow = timeInMinutes >= morningStart || timeInMinutes < morningEnd;
    const isInLunchWindow = timeInMinutes >= lunchStart && timeInMinutes < lunchEnd;
    const isInDinnerWindow = timeInMinutes >= dinnerStart && timeInMinutes < dinnerEnd;

    if (isInMorningWindow) {
      setRequiredMealTypes(['BREAKFAST', 'LUNCH', 'DINNER']);
      setOrderingMessage('Morning ordering period (9:00 PM - 10:00 AM). You must order Breakfast, Lunch, and Dinner.');
      setIsOrderingAllowed(true);
    } else if (isInLunchWindow) {
      setRequiredMealTypes(['LUNCH', 'DINNER']);
      setOrderingMessage('Lunch ordering period (10:00 AM - 3:00 PM). You must order Lunch and Dinner.');
      setIsOrderingAllowed(true);
    } else if (isInDinnerWindow) {
      setRequiredMealTypes(['DINNER']);
      setOrderingMessage('Dinner ordering period (5:00 PM - 7:00 PM). You must order Dinner only.');
      setIsOrderingAllowed(true);
    } else {
      setRequiredMealTypes([]);
      setOrderingMessage('Ordering is currently closed. Please order during: Morning (9:00 PM - 10:00 AM), Lunch (10:00 AM - 3:00 PM), or Dinner (5:00 PM - 7:00 PM).');
      setIsOrderingAllowed(false);
    }
  }, [currentTime]);

  // Auto-select first available meal tab
  useEffect(() => {
    if (requiredMealTypes.length === 0) return;

    const firstAvailable = requiredMealTypes[0].toLowerCase() as 'breakfast' | 'lunch' | 'dinner';
    
    if (isMealTimeDisabled(activeMealTime)) {
      setActiveMealTime(firstAvailable);
    }
  }, [requiredMealTypes]);

  const isMealTimeDisabled = (mealTime: string): boolean =>
    !requiredMealTypes.includes(mealTime.toUpperCase());

  // Fetch patient profile
  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const sessionToken = localStorage.getItem('sessionToken');
        if (!sessionToken) { navigate('/patient/login'); return; }

        const response = await axios.get<ProfileResponse>(config.patientProfileUrl, {
          headers: { 'Authorization': `Bearer ${sessionToken}`, 'Content-Type': 'application/json' },
        });

        if (response.data.success) {
          setPatientProfile(response.data.data);
        }
      } catch (err: any) {
        console.error('Error fetching profile:', err);
        if (err.response?.status === 401 || err.response?.status === 403) {
          localStorage.removeItem('sessionToken');
          localStorage.removeItem('patientInfo');
          localStorage.removeItem('patientId');
          navigate('/patient/login');
        }
      } finally {
        setIsLoadingProfile(false);
      }
    };
    fetchProfile();
  }, [navigate]);

  // Fetch menu data
  useEffect(() => {
    const fetchMenu = async () => {
      try {
        const sessionToken = localStorage.getItem('sessionToken');
        if (!sessionToken) { navigate('/patient/login'); return; }

        const response = await axios.get<MenuResponse>(config.patientMenuUrl, {
          headers: { 'Authorization': `Bearer ${sessionToken}`, 'Content-Type': 'application/json' },
        });

        if (response.data.success) setMenuData(response.data.data);
      } catch (err: any) {
        console.error('Error fetching menu:', err);
        if (err.response?.status === 401 || err.response?.status === 403) {
          localStorage.removeItem('sessionToken');
          localStorage.removeItem('patientInfo');
          navigate('/patient/login');
        }
      } finally {
        setIsLoadingMenu(false);
      }
    };
    fetchMenu();
  }, [navigate]);

  // Fetch today's order history when History tab is activated
  useEffect(() => {
    if (activeView !== 'history') return;

    const fetchTodayOrder = async () => {
      setIsLoadingHistory(true);
      setHistoryError(null);

      try {
        const sessionToken = localStorage.getItem('sessionToken');
        if (!sessionToken) { navigate('/patient/login'); return; }

        const response = await axios.get<TodayOrderResponse>(
          config.patientTodayOrderUrl,
          {
            headers: {
              'Authorization': `Bearer ${sessionToken}`,
              'Content-Type': 'application/json',
            },
          }
        );

        if (response.data.success) {
          setTodayOrder(response.data.data);
        } else {
          setTodayOrder(null);
          setHistoryError(response.data.message || 'No order found for today.');
        }
      } catch (err: any) {
        console.error('Error fetching order history:', err);
        if (err.response?.status === 401 || err.response?.status === 403) {
          localStorage.removeItem('sessionToken');
          localStorage.removeItem('patientInfo');
          localStorage.removeItem('patientId');
          navigate('/patient/login');
        } else if (err.response?.status === 404) {
          setTodayOrder(null);
          setHistoryError('No order placed yet for today.');
        } else {
          setHistoryError('Failed to load order history. Please try again.');
        }
      } finally {
        setIsLoadingHistory(false);
      }
    };

    fetchTodayOrder();
  }, [activeView, navigate]);

  // Fetch à la carte menu when À La Carte tab is activated
  useEffect(() => {
    if (activeView !== 'alacarte') return;

    const fetchAlaCarteMenu = async () => {
      setIsLoadingAlaCarteMenu(true);

      try {
        const sessionToken = localStorage.getItem('sessionToken');
        if (!sessionToken) { navigate('/patient/login'); return; }

        const response = await axios.get<AlaCarteResponse>(
          config.patientAlaCarteUrl,
          {
            headers: {
              'Authorization': `Bearer ${sessionToken}`,
              'Content-Type': 'application/json',
            },
          }
        );

        if (response.data.success) {
          setAlaCarteMenu(response.data.data);
        }
      } catch (err: any) {
        console.error('Error fetching à la carte menu:', err);
        if (err.response?.status === 401 || err.response?.status === 403) {
          localStorage.removeItem('sessionToken');
          localStorage.removeItem('patientInfo');
          localStorage.removeItem('patientId');
          navigate('/patient/login');
        }
      } finally {
        setIsLoadingAlaCarteMenu(false);
      }
    };

    fetchAlaCarteMenu();
  }, [activeView, navigate]);

  const getCurrentMealOptions = (): FoodItem[] => {
    if (!menuData || activeView !== 'diet') return [];
    const allMeals: FoodItem[] = [];
    menuData.menuCategories.forEach(category => {
      let options: FoodItem[] = [];
      switch (activeMealTime) {
        case 'breakfast': options = category.breakfastOptions; break;
        case 'lunch':     options = category.lunchOptions;     break;
        case 'dinner':    options = category.dinnerOptions;    break;
      }
      allMeals.push(...options);
    });
    return allMeals;
  };

  const toggleMealSelection = (foodItemId: string) => {
    if (activeView === 'diet') {
      if (isMealTimeDisabled(activeMealTime)) {
        showError(`${activeMealTime.charAt(0).toUpperCase() + activeMealTime.slice(1)} ordering is not available at this time.`);
        return;
      }
      setDietSelections(prev => ({
        ...prev,
        [activeMealTime]: prev[activeMealTime] === foodItemId ? null : foodItemId,
      }));
    } else {
      setAlaCarteSelections(prev =>
        prev.includes(foodItemId)
          ? prev.filter(id => id !== foodItemId)
          : [...prev, foodItemId]
      );
    }
  };

  const isSelected = (foodItemId: string): boolean => {
    if (activeView === 'diet') return dietSelections[activeMealTime] === foodItemId;
    return alaCarteSelections.includes(foodItemId);
  };

  const selectedCount = (): number => {
    if (activeView === 'diet') return Object.values(dietSelections).filter(v => v !== null).length;
    return alaCarteSelections.length;
  };

const handlePlaceOrder = async () => {
  if (activeView === 'diet') {
    // Diet Plan ordering logic
    if (!isOrderingAllowed) { 
      showError(orderingMessage); 
      return; 
    }

    const missingMeals: string[] = [];
    requiredMealTypes.forEach(mealType => {
      const key = mealType.toLowerCase() as 'breakfast' | 'lunch' | 'dinner';
      if (!dietSelections[key]) missingMeals.push(mealType);
    });

    if (missingMeals.length > 0) {
      showError(`Please select meals for: ${missingMeals.join(', ')}`);
      return;
    }

    const meals: OrderMealItem[] = requiredMealTypes.map(mealType => {
      const key = mealType.toLowerCase() as 'breakfast' | 'lunch' | 'dinner';
      return { 
        mealType: mealType as 'BREAKFAST' | 'LUNCH' | 'DINNER', 
        foodItemId: dietSelections[key]! 
      };
    });

    setIsPlacingOrder(true);
    try {
      const sessionToken = localStorage.getItem('sessionToken');
      if (!sessionToken) { navigate('/patient/login'); return; }

      const response = await axios.post<OrderResponse>(
        config.patientOrderUrl,
        { meals },
        { headers: { 'Authorization': `Bearer ${sessionToken}`, 'Content-Type': 'application/json' } }
      );

      if (response.data.success) {
        setShowSuccessPopup(true);
        setTimeout(() => setShowSuccessPopup(false), 2000);
        setDietSelections({ breakfast: null, lunch: null, dinner: null });
        
        // Set notification badge
        setHasNewOrder(true);
      }
    } catch (err: any) {
      console.error('Error placing order:', err);
      showError(err.response?.data?.message
        ? `Order failed: ${err.response.data.message}`
        : 'Failed to place order. Please try again.');
    } finally {
      setIsPlacingOrder(false);
    }
 } else if (activeView === 'alacarte') {
    // À La Carte ordering logic
    if (selectedCount() === 0) { 
      showError('Please select at least one item'); 
      return; 
    }

    setIsPlacingOrder(true);
    try {
      const sessionToken = localStorage.getItem('sessionToken');
      if (!sessionToken) { navigate('/patient/login'); return; }

      const response = await axios.post<OrderResponse>(
        config.patientAlacarteOrderUrl,
        { foodItemIds: alaCarteSelections },  // ✅ Changed from { meals } to { foodItemIds }
        { headers: { 'Authorization': `Bearer ${sessionToken}`, 'Content-Type': 'application/json' } }
      );

      if (response.data.success) {
        setShowSuccessPopup(true);
        setTimeout(() => setShowSuccessPopup(false), 2000);
        setAlaCarteSelections([]);
        
        // Set notification badge
        setHasNewOrder(true);
      }
    } catch (err: any) {
      console.error('Error placing à la carte order:', err);
      showError(err.response?.data?.message
        ? `Order failed: ${err.response.data.message}`
        : 'Failed to place order. Please try again.');
    } finally {
      setIsPlacingOrder(false);
    }
  }
};

  const handleLogout = async () => {
    try {
      const sessionToken = localStorage.getItem('sessionToken');
      
      if (sessionToken) {
        await axios.post(
          config.patientLogoutUrl,
          {},
          {
            headers: {
              'Authorization': `Bearer ${sessionToken}`,
              'Content-Type': 'application/json',
            },
          }
        );
      }
    } catch (err: any) {
      console.error('Logout error:', err);
    } finally {
      localStorage.removeItem('sessionToken');
      localStorage.removeItem('patientInfo');
      localStorage.removeItem('patientId');
      navigate('/patient/login');
    }
  };

  const handleNavigation = (view: 'diet' | 'alacarte' | 'history') => {
    setActiveView(view);
    setIsSidebarOpen(false);
    
    // Clear notification badge when navigating to Order History
    if (view === 'history') {
      setHasNewOrder(false);
    }
  };

  const currentMeals = activeView === 'diet' ? getCurrentMealOptions() : [];

  if (isLoadingProfile || isLoadingMenu) {
    return (
      <div className="loading-container">
        <div className="loading-content">
          <div className="loading-spinner-large"></div>
          <h2 className="loading-title">Loading Your Menu</h2>
          <p className="loading-subtitle">Please wait while we fetch your meal options...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard-container">
      {/* ── Sidebar ── */}
      <aside className={`dashboard-sidebar ${isSidebarOpen ? 'open' : ''}`}>
        <div className="sidebar-header">
          <img src={evercarelogo} alt="Evercare Hospital" className="sidebar-logo" />
        </div>

        <nav className="sidebar-nav">
          <button className={`nav-item ${activeView === 'diet' ? 'active' : ''}`} onClick={() => handleNavigation('diet')}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M3 2v7c0 1.1.9 2 2 2h4a2 2 0 0 0 2-2V2" />
              <path d="M7 2v20" />
              <path d="M21 15V2v0a5 5 0 0 0-5 5v6c0 1.1.9 2 2 2h3Zm0 0v7" />
            </svg>
            <span>Diet Plan</span>
          </button>

          <button className={`nav-item ${activeView === 'alacarte' ? 'active' : ''}`} onClick={() => handleNavigation('alacarte')}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <circle cx="8" cy="21" r="1" />
              <circle cx="19" cy="21" r="1" />
              <path d="M2.05 2.05h2l2.66 12.42a2 2 0 0 0 2 1.58h9.78a2 2 0 0 0 1.95-1.57l1.65-7.43H5.12" />
            </svg>
            <span>À La Carte</span>
          </button>

          <button className={`nav-item ${activeView === 'history' ? 'active' : ''}`} onClick={() => handleNavigation('history')}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M3 3v5h5" />
              <path d="M3.05 13A9 9 0 1 0 6 5.3L3 8" />
              <path d="M12 7v5l4 2" />
            </svg>
            <span>Order History</span>
            {hasNewOrder && <span className="notification-badge">1</span>}
          </button>

          <button className="nav-item logout" onClick={handleLogout}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
              <polyline points="16 17 21 12 16 7" />
              <line x1="21" y1="12" x2="9" y2="12" />
            </svg>
            <span>Logout</span>
          </button>
        </nav>
      </aside>

      {isSidebarOpen && <div className="sidebar-overlay" onClick={() => setIsSidebarOpen(false)} />}

      {/* ── Main ── */}
      <main className="dashboard-main">
        <div className="mobile-header">
          <button className="hamburger-btn" onClick={() => setIsSidebarOpen(true)}>
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <line x1="3" y1="12" x2="21" y2="12" />
              <line x1="3" y1="6" x2="21" y2="6" />
              <line x1="3" y1="18" x2="21" y2="18" />
            </svg>
          </button>
          <img src={evercarelogo} alt="Evercare" className="mobile-logo" />
        </div>

        <div className="welcome-section">
          <h1 className="welcome-title">
            Welcome, {patientProfile?.name || menuData?.patientName || 'Patient'}
          </h1>
          <p className="welcome-subtitle">
            UHID: {patientProfile?.uhid || menuData?.uhid || 'N/A'} • Room: {patientProfile?.roomNumber || menuData?.roomNumber || 'N/A'}
          </p>
        </div>

        {activeView === 'diet' && (
          <div className={`ordering-notice ${!isOrderingAllowed ? 'closed' : ''}`}>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <circle cx="12" cy="12" r="10" />
              <polyline points="12 6 12 12 16 14" />
            </svg>
            <span>{orderingMessage}</span>
          </div>
        )}

        <div className="content-area">

          {/* ── Diet view ── */}
          {activeView === 'diet' && (
            <>
              <div className="meal-tabs">
                {(['breakfast', 'lunch', 'dinner'] as const).map(meal => (
                  <button
                    key={meal}
                    className={`meal-tab ${activeMealTime === meal ? 'active' : ''} ${isMealTimeDisabled(meal) ? 'disabled' : ''}`}
                    onClick={() => !isMealTimeDisabled(meal) && setActiveMealTime(meal)}
                    disabled={isMealTimeDisabled(meal)}
                  >
                    {meal.charAt(0).toUpperCase() + meal.slice(1)}
                    {isMealTimeDisabled(meal) && <span className="tab-lock">🔒</span>}
                  </button>
                ))}
              </div>
              <div className="section-subtitle">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M22 12h-4l-3 9L9 3l-3 9H2" />
                </svg>
                <span>Your Diet Plan - Prescribed by Dietician</span>
              </div>
            </>
          )}

          {/* ── A la carte view ── */}
          {activeView === 'alacarte' && (
            <div className="section-subtitle">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="8" cy="21" r="1" />
                <circle cx="19" cy="21" r="1" />
                <path d="M2.05 2.05h2l2.66 12.42a2 2 0 0 0 2 1.58h9.78a2 2 0 0 0 1.95-1.57l1.65-7.43H5.12" />
              </svg>
              <span>À La Carte Menu - Optional Add-ons</span>
            </div>
          )}

          {/* ── Order History view ── */}
          {activeView === 'history' && (
            <div className="order-history">
              <div className="history-header">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M3 3v5h5" />
                  <path d="M3.05 13A9 9 0 1 0 6 5.3L3 8" />
                  <path d="M12 7v5l4 2" />
                </svg>
                <div>
                  <h2 className="history-title">Today's Order</h2>
                  <p className="history-date">
                    {new Date().toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
                  </p>
                </div>
              </div>

              {isLoadingHistory && (
                <div className="history-loading">
                  <div className="history-spinner" />
                  <p>Loading your order...</p>
                </div>
              )}

              {!isLoadingHistory && historyError && !todayOrder && (
                <div className="history-empty">
                  <div className="history-empty-icon">🍽️</div>
                  <p className="history-empty-title">No Order Yet</p>
                  <p className="history-empty-sub">{historyError}</p>
                </div>
              )}

       {!isLoadingHistory && todayOrder && (
                <div className="history-meal-list">
                  {MEAL_ORDER.flatMap(mealType => {
                    // Get ALL meals of this type (not just first)
                    const mealsOfType = todayOrder.meals.filter(m => m.mealType === mealType);
                    
                    return mealsOfType.map(meal => {
                      const statusCfg = STATUS_CONFIG[meal.status] ?? { 
                        label: meal.status, 
                        className: 'status-pending' 
                      };

                      return (
                        <div key={meal.mealItemId} className="history-meal-card">
                          <div className={`history-meal-accent ${statusCfg.className}`} />

                          <div className="history-meal-body">
                            <div className="history-meal-top">
                              <div className="history-meal-type">
                                <span className="history-meal-type-label">
                                  {mealType === 'EXTRA' ? 'À LA CARTE' : mealType}
                                </span>
                              </div>
                              <span className={`history-status-badge ${statusCfg.className}`}>
                                {statusCfg.label}
                              </span>
                            </div>

                            <p className="history-food-name">{meal.foodItemName}</p>
                            <p className="history-food-desc">{meal.foodDescription}</p>

                            <div className="history-meal-footer">
                              <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                <circle cx="12" cy="12" r="10" />
                                <polyline points="12 6 12 12 16 14" />
                              </svg>
                              <span>Ordered at {formatOrderedAt(meal.orderedAt)}</span>

                              {meal.processedAt && (
                                <>
                                  <span className="history-footer-sep">·</span>
                                  <span>Processed at {formatOrderedAt(meal.processedAt)}</span>
                                </>
                              )}
                            </div>
                          </div>
                        </div>
                      );
                    });
                  })}
                </div>
              )}
            </div>  
          )} 

          {/* ── Meal cards for diet / a la carte ── */}
          {activeView !== 'history' && (
            <>
              {activeView === 'alacarte' && isLoadingAlaCarteMenu ? (
                <div className="empty-state" style={{ textAlign: 'center', padding: '60px 20px' }}>
                  <div className="history-spinner" style={{ margin: '0 auto 20px' }} />
                  <p style={{ fontSize: '15px', color: 'var(--text-light)' }}>Loading à la carte menu...</p>
                </div>
              ) : activeView === 'diet' && (!menuData || menuData.menuCategories.length === 0) ? (
                <div className="empty-state" style={{ textAlign: 'center', padding: '60px 20px' }}>
                  <div style={{ fontSize: '64px', marginBottom: '16px', opacity: 0.5 }}>🍽️</div>
                  <h3 style={{ fontSize: '20px', fontWeight: 600, color: 'var(--text-dark)', marginBottom: '8px' }}>No Menu Available</h3>
                  <p style={{ fontSize: '15px', color: 'var(--text-light)' }}>There is currently no menu assigned to you. Please contact your dietician.</p>
                </div>
              ) : activeView === 'alacarte' && alaCarteMenu.length === 0 ? (
                <div className="empty-state" style={{ textAlign: 'center', padding: '60px 20px' }}>
                  <div style={{ fontSize: '64px', marginBottom: '16px', opacity: 0.5 }}>🍽️</div>
                  <h3 style={{ fontSize: '20px', fontWeight: 600, color: 'var(--text-dark)', marginBottom: '8px' }}>No À La Carte Items</h3>
                  <p style={{ fontSize: '15px', color: 'var(--text-light)' }}>There are currently no à la carte items available.</p>
                </div>
              ) : activeView === 'diet' && currentMeals.length === 0 ? (
                <div className="empty-state" style={{ textAlign: 'center', padding: '40px 20px' }}>
                  <p>No meals available for this selection.</p>
                </div>
              ) : (
                <div className="meal-list">
                  {activeView === 'alacarte' 
                    ? alaCarteMenu.map(meal => (
                        <div
                          key={meal.foodItemId}
                          className={`meal-card ${alaCarteSelections.includes(meal.foodItemId) ? 'selected' : ''}`}
                          onClick={() => toggleMealSelection(meal.foodItemId)}
                        >
                          <div className="meal-image-placeholder">🍽️</div>
                          <div className="meal-info">
                            <h3 className="meal-name">{meal.name}</h3>
                          </div>
                          {alaCarteSelections.includes(meal.foodItemId) && (
                            <div className="selected-checkmark">
                              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
                                <polyline points="20 6 9 17 4 12" />
                              </svg>
                            </div>
                          )}
                        </div>
                      ))
                    : currentMeals.map(meal => (
                        <div
                          key={meal.foodItemId}
                          className={`meal-card ${isSelected(meal.foodItemId) ? 'selected' : ''} ${activeView === 'diet' && isMealTimeDisabled(activeMealTime) ? 'disabled' : ''}`}
                          onClick={() => toggleMealSelection(meal.foodItemId)}
                        >
                          <div className="meal-image-placeholder">🍽️</div>
                          <div className="meal-info">
                            <h3 className="meal-name">{meal.name}</h3>
                            <p className="meal-description">{meal.description}</p>
                          </div>
                          {isSelected(meal.foodItemId) && (
                            <div className="selected-checkmark">
                              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round">
                                <polyline points="20 6 9 17 4 12" />
                              </svg>
                            </div>
                          )}
                        </div>
                      ))
                  }
                </div>
              )}
            </>
          )}
        </div>

   {/* ── Bottom bar (hidden on history) ── */}
{activeView !== 'history' && (
  <div className="bottom-bar">
    <div className="selected-count">
      <span className="count-label">Selected Items</span>
      <span className="count-value">{selectedCount()} {selectedCount() === 1 ? 'Item' : 'Items'}</span>
    </div>
    <button
      className="place-order-btn"
      onClick={handlePlaceOrder}
      disabled={(activeView === 'diet' && !isOrderingAllowed) || isPlacingOrder || selectedCount() === 0}
    >
      {isPlacingOrder ? (
        <><span className="spinner-small" /><span>Placing Order...</span></>
      ) : (
        <>
          <span>Place Order</span>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <line x1="5" y1="12" x2="19" y2="12" />
            <polyline points="12 5 19 12 12 19" />
          </svg>
        </>
      )}
    </button>
  </div>
)}
      </main>

      {/* Success Popup */}
      {showSuccessPopup && (
        <div className="notification-overlay">
          <div className="notification-popup success">
            <div className="notification-icon">✓</div>
            <p className="notification-message">Order Placed Successfully!</p>
          </div>
        </div>
      )}

      {/* Error Popup */}
      {showErrorPopup && (
        <div className="notification-overlay">
          <div className="notification-popup error">
            <div className="notification-icon">⚠</div>
            <p className="notification-message">{errorMessage}</p>
          </div>
        </div>
      )}
    </div>
  );
};

export default PatientDashboard;