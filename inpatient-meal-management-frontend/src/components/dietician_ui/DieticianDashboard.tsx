import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  LayoutDashboard, Users, ClipboardList, Layers, UtensilsCrossed,
  LogOut, Menu, X, AlertTriangle, TrendingUp, ChefHat
} from 'lucide-react';
import { config } from '../../config/config';
import '../../styles/dietician/dieticiandashboard.css';
import evercarelogo     from '../../assets/icons/ec_logo.png';
import evercareLogoFull from '../../assets/icons/EvercareLogo.png';

// ── Types ──────────────────────────────────────────────────
interface DieticianUser {
  userId: string;
  email: string;
  name: string;
  role: string;
}

interface RecentOrder {
  uhid: string;
  patientName: string;
  roomNumber: string;
  orderDate: string;
  totalMeals: number;
}

interface DashboardData {
  date: string;
  totalPatientsAssigned: number;
  totalMenuGroupsCreated: number;
  totalOrdersToday: number;
  recentOrders: RecentOrder[];
}

interface DashboardResponse {
  success: boolean;
  message: string;
  data: DashboardData;
  timestamp: string;
}

interface FoodItemsResponse {
  success: boolean;
  message: string;
  data: {
    totalFoodItems: number;
  };
  timestamp: string;
}

// ── Nav items ──────────────────────────────────────────────
const NAV_ITEMS = [
  { label: 'Dashboard',   icon: LayoutDashboard, path: '/dietician/dashboard'   },
  { label: 'Patients',    icon: Users,           path: '/dietician/patients'    },
  { label: 'Menu Groups', icon: Layers,          path: '/dietician/menu-groups' },
  { label: 'Food Items',  icon: UtensilsCrossed, path: '/dietician/food-items'  },
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

// ── Component ──────────────────────────────────────────────
const DieticianDashboard: React.FC = () => {
  const navigate = useNavigate();
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [user, setUser] = useState<DieticianUser | null>(null);
  const [dashboardData, setDashboardData] = useState<DashboardData | null>(null);
  const [totalFoodItems, setTotalFoodItems] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [isLoggingOut, setIsLoggingOut] = useState(false);

useEffect(() => {
    const raw = localStorage.getItem('dietician_user');
    if (raw) setUser(JSON.parse(raw));
}, []);

useEffect(() => {
    fetchAll();
}, []);

  const getToken = (): string | null => {
    const token = localStorage.getItem('dietician_token');
    if (!token) { navigate('/dietician/login'); return null; }
    return token;
  };

  const handle401 = () => {
    localStorage.removeItem('dietician_token');
    localStorage.removeItem('dietician_user');
    navigate('/dietician/login');
  };

  const fetchAll = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const token = getToken();
      if (!token) return;

      const headers = {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      };

      const [dashRes, foodRes] = await Promise.all([
        fetch(config.dieticianDashboardUrl,      { headers }),
        fetch(config.dieticianTotalFoodItemsUrl, { headers }),
      ]);

      if (dashRes.status === 401 || foodRes.status === 401) {
        handle401(); return;
      }

      const [dashJson, foodJson]: [DashboardResponse, FoodItemsResponse] =
        await Promise.all([dashRes.json(), foodRes.json()]);

      if (dashJson.success) {
        setDashboardData(dashJson.data);
      } else {
        setError(dashJson.message || 'Failed to load dashboard');
      }

      if (foodJson.success) {
        setTotalFoodItems(foodJson.data.totalFoodItems);
      }
    } catch (err) {
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
      const token = localStorage.getItem('dietician_token');
      if (token) {
        await fetch(config.dieticianLogoutUrl, {
          method: 'POST',
          headers: { 'Authorization': `Bearer ${token}` },
        });
      }
    } catch (err) {
      console.error('Logout error:', err);
    } finally {
      localStorage.removeItem('dietician_token');
      localStorage.removeItem('dietician_user');
      setIsLoggingOut(false);
      setShowLogoutModal(false);
      navigate('/dietician/login');
    }
  };

  const statCards = [
    { label: 'Patients Assigned',    value: dashboardData?.totalPatientsAssigned  ?? 0, icon: Users,          color: 'teal'  },
    { label: 'Menu Groups Created',  value: dashboardData?.totalMenuGroupsCreated ?? 0, icon: Layers,         color: 'green' },
    { label: "Today's Orders",       value: dashboardData?.totalOrdersToday       ?? 0, icon: TrendingUp,     color: 'blue'  },
    { label: 'Total Food Items',     value: totalFoodItems                        ?? 0, icon: UtensilsCrossed, color: 'amber' },
  ];

  return (
    <div className="dc-dashboard">

      {/* ── Logout Modal ── */}
      {showLogoutModal && (
        <div className="dc-modal-overlay">
          <div className="dc-modal">
            <div className="dc-modal-icon">
              <AlertTriangle size={40} />
            </div>
            <h3 className="dc-modal-title">Confirm Logout</h3>
            <p className="dc-modal-message">
              Are you sure you want to logout? You will need to sign in again.
            </p>
            <div className="dc-modal-actions">
              <button className="dc-modal-cancel" onClick={() => setShowLogoutModal(false)} disabled={isLoggingOut}>
                Cancel
              </button>
              <button className="dc-modal-confirm" onClick={handleConfirmLogout} disabled={isLoggingOut}>
                {isLoggingOut ? 'Logging out...' : 'Yes, Logout'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── Mobile Header ── */}
      <div className="dc-mobile-header">
        <div className="dc-mobile-brand">
          <img src={evercarelogo} alt="Evercare" className="dc-mobile-logo" />
        </div>
        <button className="dc-hamburger" onClick={() => setIsSidebarOpen(true)}>
          <Menu size={24} />
        </button>
      </div>

      {/* ── Sidebar Overlay ── */}
      {isSidebarOpen && (
        <div className="dc-overlay" onClick={() => setIsSidebarOpen(false)} />
      )}

      {/* ── Sidebar ── */}
      <aside className={`dc-sidebar ${isSidebarOpen ? 'dc-sidebar-open' : ''}`}>
        <button className="dc-sidebar-close" onClick={() => setIsSidebarOpen(false)}>
          <X size={20} />
        </button>

        <div className="dc-sidebar-brand">
          <img src={evercareLogoFull} alt="Evercare" className="dc-sidebar-logo-collapsed" />
          <img src={evercarelogo}     alt="Evercare" className="dc-sidebar-logo-expanded"  />
        </div>

        <nav className="dc-nav">
          {NAV_ITEMS.map(item => {
            const Icon = item.icon;
            const isActive = window.location.pathname === item.path;
            return (
              <button
                key={item.label}
                className={`dc-nav-item ${isActive ? 'active' : ''}`}
                onClick={() => handleNavigation(item.path)}
              >
                <Icon size={20} />
                <span>{item.label}</span>
              </button>
            );
          })}
        </nav>

        <div className="dc-sidebar-footer">
          <div className="dc-user-info">
            <div className="dc-user-avatar">
              {user?.name ? getInitials(user.name) : <ChefHat size={18} />}
            </div>
            <div className="dc-user-text">
              <p className="dc-user-name">{user?.name || 'Dietician'}</p>
              <p className="dc-user-role">{user?.role || 'DIETICIAN'}</p>
            </div>
          </div>
          <button className="dc-logout-btn" onClick={() => setShowLogoutModal(true)} disabled={isLoggingOut}>
            <LogOut size={16} />
            <span>Logout</span>
          </button>
        </div>
      </aside>

      {/* ── Main Content ── */}
      <main className="dc-main">
        <div className="dc-content-wrapper">

          {/* Welcome */}
          <div className="dc-welcome">
            <h1 className="dc-welcome-title">
              Welcome, {user?.name?.split(' ')[0] || 'Dietician'}
            </h1>
            <p className="dc-welcome-subtitle">
              <span className="dc-role-badge">{user?.role || 'DIETICIAN'}</span>
              &nbsp;·&nbsp;
              {dashboardData?.date
                ? formatDate(dashboardData.date)
                : new Date().toLocaleDateString('en-US', {
                    weekday: 'long', month: 'long', day: 'numeric', year: 'numeric',
                  })}
            </p>
          </div>

          {/* Error */}
          {error && (
            <div className="dc-alert-error">
              <AlertTriangle size={18} />
              <span>{error}</span>
              <button onClick={() => setError(null)} className="dc-alert-close">×</button>
            </div>
          )}

          {/* Stat Cards */}
          <div className="dc-stats-grid">
            {isLoading
              ? [...Array(4)].map((_, i) => (
                  <div key={i} className="dc-stat-card skeleton-card">
                    <div className="dc-skeleton dc-skeleton-icon" />
                    <div className="dc-skeleton dc-skeleton-label" />
                    <div className="dc-skeleton dc-skeleton-value" />
                  </div>
                ))
              : statCards.map((card, i) => {
                  const Icon = card.icon;
                  return (
                    <div key={i} className={`dc-stat-card dc-stat-${card.color}`}>
                      <div className="dc-stat-icon-wrap">
                        <Icon size={22} />
                      </div>
                      <p className="dc-stat-label">{card.label}</p>
                      <p className="dc-stat-value">{card.value}</p>
                    </div>
                  );
                })}
          </div>

          {/* Recent Orders */}
          <div className="dc-card">
            <div className="dc-card-header">
              <h2 className="dc-card-title">Recent Orders</h2>
              <span className="dc-card-badge">
                {dashboardData?.recentOrders?.length ?? 0} entries
              </span>
            </div>

            {isLoading ? (
              <div className="dc-table-loading">
                {[...Array(4)].map((_, i) => (
                  <div key={i} className="dc-skeleton-row">
                    <div className="dc-skeleton dc-skeleton-avatar" />
                    <div className="dc-skeleton-info">
                      <div className="dc-skeleton dc-skeleton-name" />
                      <div className="dc-skeleton dc-skeleton-sub" />
                    </div>
                    <div className="dc-skeleton dc-skeleton-chip" />
                    <div className="dc-skeleton dc-skeleton-chip" />
                  </div>
                ))}
              </div>
            ) : dashboardData?.recentOrders && dashboardData.recentOrders.length > 0 ? (
              <>
                {/* Desktop table */}
                <div className="dc-table-wrap">
                  <table className="dc-table">
                    <thead>
                      <tr>
                        <th>Patient</th>
                        <th>UHID</th>
                        <th>Room</th>
                        <th>Order Date</th>
                        <th>Total Meals</th>
                      </tr>
                    </thead>
                    <tbody>
                      {dashboardData.recentOrders.map((order, i) => (
                        <tr key={i}>
                          <td>
                            <div className="dc-patient-cell">
                              <div className="dc-table-avatar">
                                {getInitials(order.patientName)}
                              </div>
                              <span>{order.patientName}</span>
                            </div>
                          </td>
                          <td><span className="dc-uhid">{order.uhid}</span></td>
                          <td>{order.roomNumber}</td>
                          <td>{formatDate(order.orderDate)}</td>
                          <td>
                            <span className={`dc-meals-badge ${order.totalMeals > 0 ? 'active' : 'empty'}`}>
                              {order.totalMeals} meals
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                {/* Mobile cards */}
                <div className="dc-order-cards">
                  {dashboardData.recentOrders.map((order, i) => (
                    <div key={i} className="dc-order-card">
                      <div className="dc-order-card-top">
                        <div className="dc-order-avatar">
                          {getInitials(order.patientName)}
                        </div>
                        <div className="dc-order-info">
                          <p className="dc-order-name">{order.patientName}</p>
                          <p className="dc-order-meta">UHID: {order.uhid} · Room {order.roomNumber}</p>
                        </div>
                        <span className={`dc-meals-badge ${order.totalMeals > 0 ? 'active' : 'empty'}`}>
                          {order.totalMeals} meals
                        </span>
                      </div>
                      <p className="dc-order-date">{formatDate(order.orderDate)}</p>
                    </div>
                  ))}
                </div>
              </>
            ) : (
              <div className="dc-empty-state">
                <ClipboardList size={48} />
                <p>No recent orders found</p>
              </div>
            )}
          </div>

        </div>
      </main>
    </div>
  );
};

export default DieticianDashboard;