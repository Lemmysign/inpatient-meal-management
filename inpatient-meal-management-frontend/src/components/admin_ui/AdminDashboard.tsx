import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  LayoutDashboard, Users, ChefHat, LogOut, Menu, X, AlertTriangle,
  TrendingUp, Clock, CheckCircle, UtensilsCrossed, Calendar,
  ChevronDown, ShieldCheck, BarChart2, RefreshCw
} from 'lucide-react';
import { config } from '../../config/config';
import '../../styles/admin/admindashboard.css';
import evercarelogo from '../../assets/icons/ec_logo.png';

/* ─── Types ──────────────────────────────────────────── */
interface PeakHour      { hour: number;           orderCount: number; }
interface DieticianStat { dieticianName: string;  orderCount: number; }
interface MenuGroupStat { menuGroupName: string;  usageCount: number; }
interface FoodItemStat  { foodItemName: string;   orderCount: number; }
interface TrendPoint    { date: string;           orderCount: number; }

interface DashboardData {
  date: string;
  totalMealsOrdered: number;
  ordersByMealType:  Record<string, number>;
  ordersByStatus:    Record<string, number>;
  averageProcessingTimeMinutes: number;
  peakOrderingHours:   PeakHour[];
  ordersPerDietician:  DieticianStat[];
  mostUsedMenuGroups:  MenuGroupStat[];
  mostOrderedFoodItems: FoodItemStat[];
  orderVolumeTrends:   TrendPoint[];
}

type FilterMode = 'today' | 'weekly' | 'monthly' | 'range';

interface AdminUser { userId: string; email: string; name: string; role: string; }

const NAV_ITEMS = [
  { label: 'Dashboard',  icon: LayoutDashboard, path: '/admin/dashboard'  },
  { label: 'Dieticians', icon: Users,           path: '/admin/dieticians' },
  { label: 'Servers',    icon: ChefHat,         path: '/admin/servers'    },
];

const MEAL_COLORS: Record<string, string> = {
  BREAKFAST: '#f59e0b',
  LUNCH:     '#3b82f6',
  DINNER:    '#8b5cf6',
  EXTRA:     '#10b981',
};

const STATUS_COLORS = { PROCESSED: '#10b981', PENDING: '#f59e0b' };

const formatHour = (h: number) => {
  if (h === 0)  return '12 AM';
  if (h < 12)   return `${h} AM`;
  if (h === 12) return '12 PM';
  return `${h - 12} PM`;
};

const formatDate = (d: string) =>
  new Date(d).toLocaleDateString('en-US', { month: 'short', day: 'numeric' });

const getInitials = (name: string) =>
  name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);

/* ─── Mini SVG Pie Chart ─────────────────────────────── */
const PieChart: React.FC<{ data: { label: string; value: number; color: string }[]; size?: number }> = ({
  data, size = 160,
}) => {
  const total = data.reduce((s, d) => s + d.value, 0);
  if (total === 0) return (
    <div className="ad-pie-empty">No data</div>
  );

  let cumAngle = -90;
  const cx = size / 2, cy = size / 2, r = size / 2 - 8;

  const slices = data.map(d => {
    const angle  = (d.value / total) * 360;
    const start  = cumAngle;
    cumAngle    += angle;
    return { ...d, startAngle: start, endAngle: cumAngle, angle };
  });

  const polarToXY = (angleDeg: number, radius: number) => ({
    x: cx + radius * Math.cos((angleDeg * Math.PI) / 180),
    y: cy + radius * Math.sin((angleDeg * Math.PI) / 180),
  });

  return (
    <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`}>
      {slices.map((s, i) => {
        const p1  = polarToXY(s.startAngle, r);
        const p2  = polarToXY(s.endAngle,   r);
        const large = s.angle > 180 ? 1 : 0;
        return (
          <path
            key={i}
            d={`M${cx},${cy} L${p1.x},${p1.y} A${r},${r} 0 ${large},1 ${p2.x},${p2.y} Z`}
            fill={s.color}
            opacity={0.9}
            stroke="white"
            strokeWidth="2"
          />
        );
      })}
      <circle cx={cx} cy={cy} r={r * 0.52} fill="white"/>
      <text x={cx} y={cy - 6}  textAnchor="middle" fontSize="13" fontWeight="700" fill="#0f172a">{total}</text>
      <text x={cx} y={cy + 10} textAnchor="middle" fontSize="9"  fill="#64748b">total</text>
    </svg>
  );
};

/* ─── Bar Chart (SVG) ────────────────────────────────── */
const BarChart: React.FC<{ data: { label: string; value: number }[]; color?: string }> = ({
  data, color = '#3b82f6',
}) => {
  if (!data.length) return <div className="ad-pie-empty">No data</div>;

  const barCount     = data.length;
  const rotateLabels = barCount > 10;
  const labelStep    = barCount > 20 ? 3 : barCount > 10 ? 2 : 1;
  const padBottom    = rotateLabels ? 60 : 36;

  const W        = 560;
  const H        = rotateLabels ? 230 : 200;
  const padTop   = 20;
  const padLeft  = 32;
  const padRight = 8;
  const chartH   = H - padTop - padBottom;
  const chartW   = W - padLeft - padRight;

  const max    = Math.max(...data.map(d => d.value), 1);
  const groupW = chartW / barCount;
  const barW   = Math.max(6, Math.min(44, groupW * 0.65));
  const gap    = (groupW - barW) / 2;

  return (
    <svg
      width="100%"
      height="100%"
      viewBox={`0 0 ${W} ${H}`}
      preserveAspectRatio="xMidYMid meet"
      style={{ display: 'block' }}
    >
      {/* Horizontal grid lines + Y-axis labels */}
      {[0, 0.25, 0.5, 0.75, 1].map((frac, i) => {
        const y = padTop + chartH * (1 - frac);
        return (
          <g key={i}>
            <line x1={padLeft} y1={y} x2={W - padRight} y2={y}
              stroke="#e2e8f0" strokeWidth="1"
              strokeDasharray={frac === 0 ? '0' : '4 3'}/>
            <text x={padLeft - 4} y={y + 4}
              fontSize="10" fill="#94a3b8" textAnchor="end">
              {Math.round(max * frac)}
            </text>
          </g>
        );
      })}

      {/* Bars + labels */}
      {data.map((d, i) => {
        const barH   = Math.max(2, (d.value / max) * chartH);
        const x      = padLeft + i * groupW + gap;
        const y      = padTop + chartH - barH;
        const labelX = padLeft + i * groupW + groupW / 2;
        const labelY = padTop + chartH + 8;
        const showLabel = i % labelStep === 0;

        return (
          <g key={i}>
            {/* Bar */}
            <rect
              x={x} y={y}
              width={barW} height={barH}
              fill={color} rx="3" ry="3"
              opacity="0.88"
            />
            {/* Value above bar — only when tall enough and not too many bars */}
            {barH > 18 && barCount <= 15 && (
              <text x={x + barW / 2} y={y - 4}
                textAnchor="middle" fontSize="10" fontWeight="600" fill={color}>
                {d.value}
              </text>
            )}
            {/* X-axis label — rotated when many bars */}
            {showLabel && (
              rotateLabels ? (
                <text
                  x={labelX} y={labelY}
                  fontSize="10" fill="#94a3b8"
                  textAnchor="end"
                  transform={`rotate(-40, ${labelX}, ${labelY})`}
                >
                  {d.label}
                </text>
              ) : (
                <text
                  x={labelX} y={H - 6}
                  textAnchor="middle"
                  fontSize="10" fill="#94a3b8"
                >
                  {d.label}
                </text>
              )
            )}
          </g>
        );
      })}
    </svg>
  );
};

/* ─── Component ──────────────────────────────────────── */
const AdminDashboard: React.FC = () => {
  const navigate = useNavigate();
  const [isSidebarOpen,   setIsSidebarOpen]   = useState(false);
  const [user,            setUser]            = useState<AdminUser | null>(null);
  const [data,            setData]            = useState<DashboardData | null>(null);
  const [isLoading,       setIsLoading]       = useState(true);
  const [error,           setError]           = useState<string | null>(null);
  const [filterMode,      setFilterMode]      = useState<FilterMode>('today');
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [isLoggingOut,    setIsLoggingOut]    = useState(false);
  const [startDate,       setStartDate]       = useState('');
  const [endDate,         setEndDate]         = useState('');
  const [showDatePicker,  setShowDatePicker]  = useState(false);

  useEffect(() => {
    const raw = localStorage.getItem('admin_user');
    if (raw) setUser(JSON.parse(raw));
  }, []);

  const getToken = () => {
    const t = localStorage.getItem('admin_token');
    if (!t) { navigate('/admin/login'); return null; }
    return t;
  };

  const handle401 = () => {
    localStorage.removeItem('admin_token');
    localStorage.removeItem('admin_user');
    navigate('/admin/login');
  };

  const fetchDashboard = useCallback(async (
    mode: FilterMode = filterMode,
    sd: string = startDate,
    ed: string = endDate,
  ) => {
    setIsLoading(true);
    setError(null);
    try {
      const token = getToken();
      if (!token) return;

      let url = '';
      if (mode === 'today')   url = config.adminDashboardTodayUrl;
      else if (mode === 'weekly')  url = config.adminDashboardWeeklyUrl;
      else if (mode === 'monthly') url = config.adminDashboardMonthlyUrl;
      else url = `${config.adminDashboardRangeUrl}?startDate=${sd}&endDate=${ed}`;

      const res = await fetch(url, {
        headers: { 'Authorization': `Bearer ${token}` },
      });
      if (res.status === 401) { handle401(); return; }

      const json = await res.json();
      if (json.success) setData(json.data);
      else setError(json.message || 'Failed to load dashboard');
    } catch {
      setError('Failed to load dashboard. Check your connection.');
    } finally {
      setIsLoading(false);
    }
  }, [filterMode, startDate, endDate]);

  useEffect(() => {
    if (filterMode !== 'range') fetchDashboard(filterMode);
  }, [filterMode]);

  const handleApplyRange = () => {
    if (!startDate || !endDate) return;
    setShowDatePicker(false);
    fetchDashboard('range', startDate, endDate);
  };

  const handleNavigation = (path: string) => {
    setIsSidebarOpen(false);
    navigate(path);
  };

  const handleConfirmLogout = async () => {
    setIsLoggingOut(true);
    try {
      const token = localStorage.getItem('admin_token');
      if (token) await fetch(config.adminLogoutUrl, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` },
      });
    } catch {}
    finally {
      localStorage.removeItem('admin_token');
      localStorage.removeItem('admin_user');
      setIsLoggingOut(false);
      setShowLogoutModal(false);
      navigate('/admin/login');
    }
  };

  /* Derived data */
  const mealTypeData = data
    ? Object.entries(data.ordersByMealType).map(([label, value]) => ({
        label, value, color: MEAL_COLORS[label] ?? '#94a3b8',
      }))
    : [];

  const statusData = data
    ? Object.entries(data.ordersByStatus).map(([label, value]) => ({
        label, value, color: STATUS_COLORS[label as keyof typeof STATUS_COLORS] ?? '#94a3b8',
      }))
    : [];

  const trendBarData = (data?.orderVolumeTrends ?? []).map(t => ({
    label: formatDate(t.date),
    value: t.orderCount,
  }));

  const peakBarData = (data?.peakOrderingHours ?? []).map(p => ({
    label: formatHour(p.hour),
    value: p.orderCount,
  }));

  const filterLabel: Record<FilterMode, string> = {
    today:   "Today",
    weekly:  "This Week",
    monthly: "This Month",
    range:   startDate && endDate ? `${formatDate(startDate)} – ${formatDate(endDate)}` : "Date Range",
  };

  return (
    <div className="ad-dashboard">

      {/* Logout Modal */}
      {showLogoutModal && (
        <div className="ad-modal-overlay">
          <div className="ad-modal">
            <div className="ad-modal-icon"><AlertTriangle size={38}/></div>
            <h3 className="ad-modal-title">Confirm Logout</h3>
            <p className="ad-modal-msg">Are you sure you want to logout?</p>
            <div className="ad-modal-actions">
              <button className="ad-modal-cancel" onClick={() => setShowLogoutModal(false)} disabled={isLoggingOut}>Cancel</button>
              <button className="ad-modal-confirm" onClick={handleConfirmLogout} disabled={isLoggingOut}>
                {isLoggingOut ? 'Logging out…' : 'Yes, Logout'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Mobile Header */}
      <div className="ad-mobile-header">
        <img src={evercarelogo} alt="Evercare" className="ad-mobile-logo"/>
        <button className="ad-hamburger" onClick={() => setIsSidebarOpen(true)}>
          <Menu size={24}/>
        </button>
      </div>

      {isSidebarOpen && <div className="ad-overlay" onClick={() => setIsSidebarOpen(false)}/>}

      {/* Sidebar */}
      <aside className={`ad-sidebar ${isSidebarOpen ? 'ad-sidebar-open' : ''}`}>
        <button className="ad-sidebar-close" onClick={() => setIsSidebarOpen(false)}><X size={20}/></button>
        <div className="ad-sidebar-brand">
          <img src={evercarelogo} alt="Evercare" className="ad-sidebar-logo"/>
        </div>
        <div className="ad-sidebar-badge">
          <ShieldCheck size={13}/> Admin Portal
        </div>
        <nav className="ad-nav">
          {NAV_ITEMS.map(item => {
            const Icon    = item.icon;
            const isActive = window.location.pathname === item.path;
            return (
              <button key={item.label}
                className={`ad-nav-item ${isActive ? 'active' : ''}`}
                onClick={() => handleNavigation(item.path)}>
                <Icon size={18}/>
                <span>{item.label}</span>
              </button>
            );
          })}
        </nav>
        <div className="ad-sidebar-footer">
          <div className="ad-user-info">
            <div className="ad-user-avatar">{user?.name ? getInitials(user.name) : '?'}</div>
            <div className="ad-user-text">
              <p className="ad-user-name">{user?.name || 'Admin'}</p>
              <p className="ad-user-role">{user?.role || 'ADMIN'}</p>
            </div>
          </div>
          <button className="ad-logout-btn" onClick={() => setShowLogoutModal(true)}>
            <LogOut size={15}/><span>Logout</span>
          </button>
        </div>
      </aside>

      {/* Main */}
      <main className="ad-main">
        <div className="ad-content">

          {/* Page Header */}
          <div className="ad-page-header">
            <div>
              <h1 className="ad-page-title">Admin Dashboard</h1>
              <p className="ad-page-sub">Hospital nutrition operations overview</p>
            </div>

            {/* Filter Bar */}
            <div className="ad-filter-bar">
              {(['today','weekly','monthly'] as FilterMode[]).map(m => (
                <button key={m}
                  className={`ad-filter-btn ${filterMode === m ? 'active' : ''}`}
                  onClick={() => setFilterMode(m)}>
                  {m === 'today' ? 'Today' : m === 'weekly' ? 'Week' : 'Month'}
                </button>
              ))}
              <div className="ad-range-wrap">
                <button
                  className={`ad-filter-btn ad-range-btn ${filterMode === 'range' ? 'active' : ''}`}
                  onClick={() => { setShowDatePicker(!showDatePicker); setFilterMode('range'); }}>
                  <Calendar size={14}/>
                  {filterMode === 'range' && startDate && endDate
                    ? `${formatDate(startDate)} – ${formatDate(endDate)}`
                    : 'Range'}
                  <ChevronDown size={13}/>
                </button>
                {showDatePicker && (
                  <div className="ad-date-picker">
                    <div className="ad-date-row">
                      <label>From</label>
                      <input type="date" value={startDate} onChange={e => setStartDate(e.target.value)}/>
                    </div>
                    <div className="ad-date-row">
                      <label>To</label>
                      <input type="date" value={endDate} onChange={e => setEndDate(e.target.value)}/>
                    </div>
                    <button className="ad-date-apply" onClick={handleApplyRange}
                      disabled={!startDate || !endDate}>Apply</button>
                  </div>
                )}
              </div>
              <button className="ad-refresh-btn" onClick={() => fetchDashboard()} title="Refresh">
                <RefreshCw size={15}/>
              </button>
            </div>
          </div>

          {/* Active filter label */}
          <div className="ad-active-filter">
            <span className="ad-filter-pill">{filterLabel[filterMode]}</span>
            {data?.date && <span className="ad-filter-date">as of {formatDate(data.date)}</span>}
          </div>

          {error && (
            <div className="ad-alert-error">
              <AlertTriangle size={16}/><span>{error}</span>
              <button onClick={() => setError(null)}>×</button>
            </div>
          )}

          {/* ── Stat Cards ── */}
          <div className="ad-stat-grid">
            {[
              { label: 'Total Meals Ordered', value: data?.totalMealsOrdered ?? 0,      icon: UtensilsCrossed, color: 'blue'  },
              { label: 'Processed',           value: data?.ordersByStatus?.PROCESSED ?? 0, icon: CheckCircle,  color: 'green' },
              { label: 'Pending',             value: data?.ordersByStatus?.PENDING   ?? 0, icon: Clock,        color: 'amber' },
              { label: 'Avg. Processing Time',
                value: data ? `${data.averageProcessingTimeMinutes.toFixed(1)} min` : '—',
                icon: TrendingUp, color: 'purple' },
            ].map((card, i) => {
              const Icon = card.icon;
              return isLoading ? (
                <div key={i} className="ad-stat-card ad-skeleton"/>
              ) : (
                <div key={i} className={`ad-stat-card ad-stat-${card.color}`}>
                  <div className="ad-stat-icon"><Icon size={20}/></div>
                  <div className="ad-stat-body">
                    <p className="ad-stat-label">{card.label}</p>
                    <p className="ad-stat-value">{card.value}</p>
                  </div>
                </div>
              );
            })}
          </div>

          {/* ── Row 1: Order Volume Trend + Meal Type Pie ── */}
          <div className="ad-row-2">
            <div className="ad-card ad-card-wide">
              <div className="ad-card-header">
                <h2 className="ad-card-title"><BarChart2 size={16}/> Order Volume Trend</h2>
              </div>
              {isLoading ? <div className="ad-chart-skeleton"/> : (
                trendBarData.every(d => d.value === 0)
                  ? <div className="ad-empty">No orders in this period</div>
                  : <div className="ad-bar-wrap">
                      <BarChart data={trendBarData} color="#3b82f6"/>
                    </div>
              )}
            </div>

            <div className="ad-card">
              <div className="ad-card-header">
                <h2 className="ad-card-title">Orders by Meal Type</h2>
              </div>
              {isLoading ? <div className="ad-chart-skeleton"/> : (
                <div className="ad-pie-section">
                  <PieChart data={mealTypeData} size={150}/>
                  <div className="ad-legend">
                    {mealTypeData.map((d, i) => (
                      <div key={i} className="ad-legend-item">
                        <span className="ad-legend-dot" style={{ background: d.color }}/>
                        <span className="ad-legend-label">{d.label}</span>
                        <span className="ad-legend-val">{d.value}</span>
                      </div>
                    ))}
                    {mealTypeData.length === 0 && <p className="ad-empty-sm">No data</p>}
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* ── Row 2: Status Pie + Peak Hours ── */}
          <div className="ad-row-2">
            <div className="ad-card">
              <div className="ad-card-header">
                <h2 className="ad-card-title">Order Status</h2>
              </div>
              {isLoading ? <div className="ad-chart-skeleton"/> : (
                <div className="ad-pie-section">
                  <PieChart data={statusData} size={150}/>
                  <div className="ad-legend">
                    {statusData.map((d, i) => (
                      <div key={i} className="ad-legend-item">
                        <span className="ad-legend-dot" style={{ background: d.color }}/>
                        <span className="ad-legend-label">{d.label}</span>
                        <span className="ad-legend-val">{d.value}</span>
                      </div>
                    ))}
                    {statusData.length === 0 && <p className="ad-empty-sm">No data</p>}
                  </div>
                </div>
              )}
            </div>

            <div className="ad-card ad-card-wide">
              <div className="ad-card-header">
                <h2 className="ad-card-title"><BarChart2 size={16}/> Peak Ordering Hours</h2>
              </div>
              {isLoading ? <div className="ad-chart-skeleton"/> : (
                peakBarData.length === 0
                  ? <div className="ad-empty">No peak hour data</div>
                  : <div className="ad-bar-wrap">
                      <BarChart data={peakBarData} color="#8b5cf6"/>
                    </div>
              )}
            </div>
          </div>

          {/* ── Row 3: Food Items + Menu Groups + Dieticians ── */}
          <div className="ad-row-3">

            <div className="ad-card">
              <div className="ad-card-header">
                <h2 className="ad-card-title">Top Food Items</h2>
              </div>
              {isLoading ? <div className="ad-list-skeleton"/> : (
                data?.mostOrderedFoodItems.length === 0
                  ? <div className="ad-empty">No data</div>
                  : <ul className="ad-rank-list">
                      {data?.mostOrderedFoodItems.map((f, i) => (
                        <li key={i} className="ad-rank-item">
                          <span className="ad-rank-num">{i + 1}</span>
                          <span className="ad-rank-name">{f.foodItemName}</span>
                          <span className="ad-rank-badge">{f.orderCount}</span>
                        </li>
                      ))}
                    </ul>
              )}
            </div>

            <div className="ad-card">
              <div className="ad-card-header">
                <h2 className="ad-card-title">Top Menu Groups</h2>
              </div>
              {isLoading ? <div className="ad-list-skeleton"/> : (
                data?.mostUsedMenuGroups.length === 0
                  ? <div className="ad-empty">No data</div>
                  : <ul className="ad-rank-list">
                      {data?.mostUsedMenuGroups.map((m, i) => (
                        <li key={i} className="ad-rank-item">
                          <span className="ad-rank-num">{i + 1}</span>
                          <span className="ad-rank-name">{m.menuGroupName}</span>
                          <span className="ad-rank-badge ad-rank-teal">{m.usageCount}</span>
                        </li>
                      ))}
                    </ul>
              )}
            </div>

            <div className="ad-card">
              <div className="ad-card-header">
                <h2 className="ad-card-title">Orders per Dietician</h2>
              </div>
              {isLoading ? <div className="ad-list-skeleton"/> : (
                data?.ordersPerDietician.length === 0
                  ? <div className="ad-empty">No data</div>
                  : <ul className="ad-rank-list">
                      {data?.ordersPerDietician.map((d, i) => (
                        <li key={i} className="ad-rank-item">
                          <div className="ad-rank-avatar">{getInitials(d.dieticianName)}</div>
                          <span className="ad-rank-name">{d.dieticianName}</span>
                          <span className="ad-rank-badge ad-rank-purple">{d.orderCount}</span>
                        </li>
                      ))}
                    </ul>
              )}
            </div>

          </div>
        </div>
      </main>
    </div>
  );
};

export default AdminDashboard;