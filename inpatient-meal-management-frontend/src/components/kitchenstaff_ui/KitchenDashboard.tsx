import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  LayoutDashboard, Coffee, Soup, Moon, ShoppingBag, Printer,
  LogOut, Menu, X, AlertTriangle, TrendingUp, ChefHat, Clock, CheckCircle
} from 'lucide-react';
import { config } from '../../config/config';
import { useKitchenQueueCounts } from './KitchenQueueContext';
import '../../styles/kitchenstaff/kitchendashboard.css';
import evercarelogo from '../../assets/icons/ec_logo.png';
import { usePushNotifications } from '../../hooks/usePushNotifications';

interface KitchenUser {
  userId: string;
  email: string;
  name: string;
  role: string;
}

interface DashboardData {
  date: string;
  totalOrdersToday: number;
  pendingBreakfast: number;
  pendingLunch: number;
  pendingDinner: number;
  processedMeals: number;
  averageProcessingTimeMinutes: number;
  mealsByStatus: {
    PROCESSING: number;
    PROCESSED: number;
    PENDING: number;
  };
  mealsByType: {
    DINNER: number;
    BREAKFAST: number;
    EXTRA: number;
    LUNCH: number;
  };
}

interface DashboardResponse {
  success: boolean;
  message: string;
  data: DashboardData;
  timestamp: string;
}

// NAV_ITEMS — no countKey needed, badges come from context
const NAV_ITEMS = [
  { label: 'Dashboard',     icon: LayoutDashboard, path: '/kitchen/dashboard'    },
  { label: 'Breakfast',     icon: Coffee,          path: '/kitchen/breakfast'    },
  { label: 'Lunch',         icon: Soup,            path: '/kitchen/lunch'        },
  { label: 'Dinner',        icon: Moon,            path: '/kitchen/dinner'       },
  { label: 'À la carte',    icon: ShoppingBag,     path: '/kitchen/alacarte'     },
  { label: 'Print History', icon: Printer,         path: '/kitchen/print-history'},
];

const formatDate = (iso: string): string => {
  try {
    return new Date(iso).toLocaleDateString('en-US', {
      month: 'short', day: 'numeric', year: 'numeric',
    });
  } catch { return iso; }
};

const getInitials = (name: string) =>
  name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);

const KitchenDashboard: React.FC = () => {
  usePushNotifications('kitchen_token'); 
  const navigate = useNavigate();
  const [isSidebarOpen, setIsSidebarOpen]     = useState(false);
  const [user, setUser]                       = useState<KitchenUser | null>(null);
  const [dashboardData, setDashboardData]     = useState<DashboardData | null>(null);
  const [isLoading, setIsLoading]             = useState(true);
  const [error, setError]                     = useState<string | null>(null);
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [isLoggingOut, setIsLoggingOut]       = useState(false);

  // ── Shared live counts from context (refreshes every 5 min) ──────────────
  const { counts } = useKitchenQueueCounts();

  // Badge map — used in sidebar for ALL pages consistently
  const badgeMap: Record<string, number> = {
    '/kitchen/breakfast': counts.breakfast,
    '/kitchen/lunch':     counts.lunch,
    '/kitchen/dinner':    counts.dinner,
    '/kitchen/alacarte':  counts.alacarte,
  };

  useEffect(() => {
    const raw = localStorage.getItem('kitchen_user');
    if (raw) setUser(JSON.parse(raw));
  }, []);

  useEffect(() => { fetchDashboard(); }, []);

  const getToken = (): string | null => {
    const token = localStorage.getItem('kitchen_token');
    if (!token) { navigate('/kitchen/login'); return null; }
    return token;
  };

  const handle401 = () => {
    localStorage.removeItem('kitchen_token');
    localStorage.removeItem('kitchen_user');
    navigate('/kitchen/login');
  };

  const fetchDashboard = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const token = getToken();
      if (!token) return;

      const response = await fetch(config.kitchenDashboardUrl, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (response.status === 401) { handle401(); return; }

      const data: DashboardResponse = await response.json();

      if (data.success) {
        setDashboardData(data.data);
      } else {
        setError(data.message || 'Failed to load dashboard');
      }
    } catch {
      setError('Failed to load dashboard. Logout and Re-Login.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleNavigation = (path: string) => {
    setIsSidebarOpen(false);
    navigate(path);
  };

  const handleConfirmLogout = async () => {
    setIsLoggingOut(true);
    try {
      const token = localStorage.getItem('kitchen_token');
      if (token) {
        await fetch(config.kitchenLogoutUrl, {
          method: 'POST',
          headers: { 'Authorization': `Bearer ${token}` },
        });
      }
    } catch (err) {
      console.error('Logout error:', err);
    } finally {
      localStorage.removeItem('kitchen_token');
      localStorage.removeItem('kitchen_user');
      setIsLoggingOut(false);
      setShowLogoutModal(false);
      navigate('/kitchen/login');
    }
  };

  const statCards = [
    {
      label: "Today's Orders",
      value: dashboardData?.totalOrdersToday ?? 0,
      icon: LayoutDashboard,
      color: 'purple',
      subtext: 'Total orders received',
    },
    {
      label: 'Pending Meals',
      value: dashboardData?.mealsByStatus.PENDING ?? 0,
      icon: Clock,
      color: 'amber',
      subtext: 'Awaiting preparation',
    },
    {
      label: 'Processing',
      value: dashboardData?.mealsByStatus.PROCESSING ?? 0,
      icon: TrendingUp,
      color: 'blue',
      subtext: 'Currently cooking',
    },
    {
      label: 'Completed',
      value: dashboardData?.processedMeals ?? 0,
      icon: CheckCircle,
      color: 'green',
      subtext: 'Meals processed today',
    },
  ];

  // Meal type cards use context counts for live "pending" badges
  const mealTypeCards = [
    {
      label:   'Breakfast Orders',
      count:   dashboardData?.mealsByType.BREAKFAST ?? 0,
      pending: counts.breakfast,
      icon:    Coffee,
      color:   'orange',
      path:    '/kitchen/breakfast',
    },
    {
      label:   'Lunch Orders',
      count:   dashboardData?.mealsByType.LUNCH ?? 0,
      pending: counts.lunch,
      icon:    Soup,
      color:   'green',
      path:    '/kitchen/lunch',
    },
    {
      label:   'Dinner Orders',
      count:   dashboardData?.mealsByType.DINNER ?? 0,
      pending: counts.dinner,
      icon:    Moon,
      color:   'indigo',
      path:    '/kitchen/dinner',
    },
    {
      label:   'À la carte',
      count:   dashboardData?.mealsByType.EXTRA ?? 0,
      pending: counts.alacarte,
      icon:    ShoppingBag,
      color:   'pink',
      path:    '/kitchen/alacarte',
    },
  ];

  return (
    <div className="ks-dashboard">

      {/* Logout Modal */}
      {showLogoutModal && (
        <div className="ks-modal-overlay">
          <div className="ks-modal">
            <div className="ks-modal-icon"><AlertTriangle size={40} /></div>
            <h3 className="ks-modal-title">Confirm Logout</h3>
            <p className="ks-modal-message">
              Are you sure you want to logout? You will need to sign in again.
            </p>
            <div className="ks-modal-actions">
              <button className="ks-modal-cancel" onClick={() => setShowLogoutModal(false)} disabled={isLoggingOut}>
                Cancel
              </button>
              <button className="ks-modal-confirm" onClick={handleConfirmLogout} disabled={isLoggingOut}>
                {isLoggingOut ? 'Logging out...' : 'Yes, Logout'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Mobile Header */}
      <div className="ks-mobile-header">
        <div className="ks-mobile-brand">
          <img src={evercarelogo} alt="Evercare" className="ks-mobile-logo" />
        </div>
        <button className="ks-hamburger" onClick={() => setIsSidebarOpen(true)}>
          <Menu size={24} />
        </button>
      </div>

      {isSidebarOpen && <div className="ks-overlay" onClick={() => setIsSidebarOpen(false)} />}

      {/* Sidebar */}
      <aside className={`ks-sidebar ${isSidebarOpen ? 'ks-sidebar-open' : ''}`}>
        <button className="ks-sidebar-close" onClick={() => setIsSidebarOpen(false)}>
          <X size={20} />
        </button>
        <div className="ks-sidebar-brand">
          <img src={evercarelogo} alt="Evercare" className="ks-sidebar-logo-collapsed" />
        </div>
        <nav className="ks-nav">
          {NAV_ITEMS.map(item => {
            const Icon = item.icon;
            const isActive   = window.location.pathname === item.path;
            const badgeCount = badgeMap[item.path] ?? 0;
            return (
              <button
                key={item.label}
                className={`ks-nav-item ${isActive ? 'active' : ''}`}
                onClick={() => handleNavigation(item.path)}
              >
                <Icon size={20} />
                <span>{item.label}</span>
                {badgeCount > 0 && <span className="ks-nav-badge">{badgeCount}</span>}
              </button>
            );
          })}
        </nav>
        <div className="ks-sidebar-footer">
          <div className="ks-user-info">
            <div className="ks-user-avatar">
              {user?.name ? getInitials(user.name) : <ChefHat size={18} />}
            </div>
            <div className="ks-user-text">
              <p className="ks-user-name">{user?.name || 'Kitchen Staff'}</p>
              <p className="ks-user-role">{user?.role || 'KITCHEN_STAFF'}</p>
            </div>
          </div>
          <button className="ks-logout-btn" onClick={() => setShowLogoutModal(true)} disabled={isLoggingOut}>
            <LogOut size={16} />
            <span>Logout</span>
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="ks-main">
        <div className="ks-content-wrapper">

          <div className="ks-welcome">
            <h1 className="ks-welcome-title">Kitchen Dashboard</h1>
            <p className="ks-welcome-subtitle">
              <span className="ks-role-badge">{user?.role || 'KITCHEN_STAFF'}</span>
              &nbsp;·&nbsp;
              {dashboardData?.date
                ? formatDate(dashboardData.date)
                : new Date().toLocaleDateString('en-US', {
                    weekday: 'long', month: 'long', day: 'numeric', year: 'numeric',
                  })}
            </p>
          </div>

          {error && (
            <div className="ks-alert-error">
              <AlertTriangle size={18} />
              <span>{error}</span>
              <button onClick={() => setError(null)} className="ks-alert-close">×</button>
            </div>
          )}

          {/* Stat Cards */}
          <div className="ks-stats-grid">
            {isLoading
              ? [...Array(4)].map((_, i) => (
                  <div key={i} className="ks-stat-card skeleton-card">
                    <div className="ks-skeleton ks-skeleton-icon" />
                    <div className="ks-skeleton ks-skeleton-label" />
                    <div className="ks-skeleton ks-skeleton-value" />
                  </div>
                ))
              : statCards.map((card, i) => {
                  const Icon = card.icon;
                  return (
                    <div key={i} className={`ks-stat-card ks-stat-${card.color}`}>
                      <div className="ks-stat-icon-wrap"><Icon size={22} /></div>
                      <p className="ks-stat-label">{card.label}</p>
                      <p className="ks-stat-value">{card.value}</p>
                      <p className="ks-stat-subtext">{card.subtext}</p>
                    </div>
                  );
                })}
          </div>

          {/* Meal Type Breakdown */}
          <div className="ks-card">
            <div className="ks-card-header">
              <h2 className="ks-card-title">Orders by Meal Type</h2>
              {dashboardData && (
                <span className="ks-card-info">
                  Avg. Processing Time: <strong>{dashboardData.averageProcessingTimeMinutes.toFixed(1)} min</strong>
                </span>
              )}
            </div>

            {isLoading ? (
              <div className="ks-meal-grid">
                {[...Array(4)].map((_, i) => (
                  <div key={i} className="ks-meal-card skeleton-card">
                    <div className="ks-skeleton ks-skeleton-meal-icon" />
                    <div className="ks-skeleton ks-skeleton-meal-label" />
                    <div className="ks-skeleton ks-skeleton-meal-count" />
                  </div>
                ))}
              </div>
            ) : (
              <div className="ks-meal-grid">
                {mealTypeCards.map((meal, i) => {
                  const Icon = meal.icon;
                  return (
                    <div
                      key={i}
                      className={`ks-meal-card ks-meal-${meal.color}`}
                      onClick={() => handleNavigation(meal.path)}
                    >
                      <div className="ks-meal-icon-wrap"><Icon size={28} /></div>
                      <p className="ks-meal-label">{meal.label}</p>
                      <p className="ks-meal-count">{meal.count}</p>
                      {meal.pending > 0 && (
                        <span className="ks-meal-pending">{meal.pending} pending</span>
                      )}
                    </div>
                  );
                })}
              </div>
            )}
          </div>

          {/* Quick Stats */}
          <div className="ks-quick-stats">
            <div className="ks-quick-stat">
              <p className="ks-quick-label">Processing</p>
              <p className="ks-quick-value">{dashboardData?.mealsByStatus.PROCESSING ?? 0}</p>
            </div>
            <div className="ks-quick-stat">
              <p className="ks-quick-label">Processed</p>
              <p className="ks-quick-value">{dashboardData?.mealsByStatus.PROCESSED ?? 0}</p>
            </div>
            <div className="ks-quick-stat">
              <p className="ks-quick-label">Pending</p>
              <p className="ks-quick-value">{dashboardData?.mealsByStatus.PENDING ?? 0}</p>
            </div>
          </div>

        </div>
      </main>
    </div>
  );
};

export default KitchenDashboard;