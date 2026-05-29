import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  LayoutDashboard, Coffee, Soup, Moon, ShoppingBag, Printer,
  LogOut, Menu, X, AlertTriangle, ChefHat, Search, Eye, CheckCircle2
} from 'lucide-react';
import { config } from '../../config/config';
import { useKitchenQueueCounts } from './KitchenQueueContext';
import '../../styles/kitchenstaff/kitchenbreakfast.css';
import evercarelogo from '../../assets/icons/ec_logo.png';
import { usePushNotifications } from '../../hooks/usePushNotifications';

interface KitchenUser {
  userId: string;
  email: string;
  name: string;
  role: string;
}

interface MealItem {
  mealItemId: string;
  uhid: string;
  patientName: string;
  roomNumber: string;
  mealType: string;
  foodItemName: string;
  status: 'PENDING' | 'PROCESSED';
  orderedAt: string;
  queuePosition: number;
  dieticianNotes?: string;
  processedAt?: string;
}

interface PagedResponse {
  content: MealItem[];
  pageable: any;
  last: boolean;
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  empty: boolean;
}

interface ApiResponse {
  success: boolean;
  message: string;
  data: PagedResponse;
  timestamp: string;
}

interface ProcessResponse {
  success: boolean;
  message: string;
  data: {
    mealItemId: string;
    uhid: string;
    patientName: string;
    roomNumber: string;
    mealType: string;
    foodItemName: string;
    status: string;
    orderedAt: string;
    processedAt: string;
    processedByStaffName: string;
  };
  timestamp: string;
}

interface PrintResponse {
  success: boolean;
  message: string;
  data: {
    uhid: string;
    patientName: string;
    roomNumber: string;
    mealType: string;
    foodItemName: string;
    status: string;
    processedAt: string;
    labelHtml: string;
  };
  timestamp: string;
}

const NAV_ITEMS = [
  { label: 'Dashboard',     icon: LayoutDashboard, path: '/kitchen/dashboard'     },
  { label: 'Breakfast',     icon: Coffee,          path: '/kitchen/breakfast'     },
  { label: 'Lunch',         icon: Soup,            path: '/kitchen/lunch'         },
  { label: 'Dinner',        icon: Moon,            path: '/kitchen/dinner'        },
  { label: 'À la carte',    icon: ShoppingBag,     path: '/kitchen/alacarte'      },
  { label: 'Print History', icon: Printer,         path: '/kitchen/print-history' },
];

const getInitials = (name: string) =>
  name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);

const formatDateTime = (iso: string): string => {
  try {
    const date = new Date(iso);
    return date.toLocaleString('en-US', {
      month: 'short', day: 'numeric', hour: 'numeric', minute: '2-digit', hour12: true,
    });
  } catch { return iso; }
};

const KitchenBreakfast: React.FC = () => {
  usePushNotifications('kitchen_token'); 
  const navigate = useNavigate();
  const [isSidebarOpen, setIsSidebarOpen]     = useState(false);
  const [user, setUser]                       = useState<KitchenUser | null>(null);
  const [mealItems, setMealItems]             = useState<MealItem[]>([]);
  const [currentPage]                         = useState(0);
  const [, setTotalPages]                     = useState(0);
  const [totalElements, setTotalElements]     = useState(0);
  const [isLoading, setIsLoading]             = useState(true);
  const [error, setError]                     = useState<string | null>(null);
  const [searchQuery, setSearchQuery]         = useState('');
  const [statusFilter, setStatusFilter]       = useState<string>('ALL');
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [isLoggingOut, setIsLoggingOut]       = useState(false);
  const [showNotesModal, setShowNotesModal]   = useState(false);
  const [selectedNotes, setSelectedNotes]     = useState<string>('');
  const [processingIds, setProcessingIds]     = useState<Set<string>>(new Set());
  const [printingIds, setPrintingIds]         = useState<Set<string>>(new Set());

  // ── Live sidebar counts shared across all kitchen pages ──────────────────
  const { counts, decrementCount } = useKitchenQueueCounts();
  const badgeMap: Record<string, number> = {
    '/kitchen/breakfast': counts.breakfast,
    '/kitchen/lunch':     counts.lunch,
    '/kitchen/dinner':    counts.dinner,
    '/kitchen/alacarte':  counts.alacarte,
  };

  const pageSize = 20;

  useEffect(() => {
    const raw = localStorage.getItem('kitchen_user');
    if (raw) setUser(JSON.parse(raw));
  }, []);

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

  const fetchBreakfastQueue = useCallback(async () => {
    try {
      setIsLoading(true);
      const token = getToken();
      if (!token) return;

      const params = new URLSearchParams();
      params.append('page', currentPage.toString());
      params.append('size', pageSize.toString());
      if (statusFilter !== 'ALL') params.append('status', statusFilter);
      if (searchQuery.trim()) params.append('search', searchQuery.trim());

      const response = await fetch(`${config.kitchenBreakfastQueueUrl}?${params.toString()}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (response.status === 401) { handle401(); return; }

      const result: ApiResponse = await response.json();

      if (result.success) {
        const sorted = [...result.data.content].sort((a, b) => a.queuePosition - b.queuePosition);
        setMealItems(sorted);
        setTotalPages(result.data.totalPages);
        setTotalElements(result.data.totalElements);
        setError(null);
      } else {
        setError(result.message || 'Failed to load breakfast queue');
      }
    } catch {
      setError('Failed to load breakfast queue. Please try again.');
    } finally {
      setIsLoading(false);
    }
  }, [currentPage, searchQuery, statusFilter]);

  useEffect(() => { fetchBreakfastQueue(); }, [fetchBreakfastQueue]);

  // Queue auto-refresh every 2 minutes
  useEffect(() => {
    const id = setInterval(fetchBreakfastQueue, 120000);
    return () => clearInterval(id);
  }, [fetchBreakfastQueue]);

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

  const handleViewNotes = (notes?: string) => {
    setSelectedNotes(notes || 'No notes available');
    setShowNotesModal(true);
  };

  const handleProcessMeal = async (mealItemId: string) => {
    const token = getToken();
    if (!token) return;
    setProcessingIds(prev => new Set(prev).add(mealItemId));
    try {
      const response = await fetch(config.kitchenProcessMealUrl, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
        body: JSON.stringify({ mealItemId }),
      });
      if (response.status === 401) { handle401(); return; }
      const result: ProcessResponse = await response.json();
      if (result.success) {
        setMealItems(prev =>
          prev.map(item =>
            item.mealItemId === mealItemId
              ? { ...item, status: 'PROCESSED', processedAt: result.data.processedAt }
              : item
          )
        );
        decrementCount('breakfast');
      } else {
        setError(result.message || 'Failed to process meal');
      }
    } catch {
      setError('Failed to process meal. Please try again.');
    } finally {
      setProcessingIds(prev => { const n = new Set(prev); n.delete(mealItemId); return n; });
    }
  };

  const handlePrintMeal = async (mealItemId: string) => {
    const token = getToken();
    if (!token) return;
    setPrintingIds(prev => new Set(prev).add(mealItemId));
    try {
      const response = await fetch(config.kitchenPrintLabelUrl(mealItemId), {
        method: 'GET',
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
      });
      if (response.status === 401) { handle401(); return; }
      const result: PrintResponse = await response.json();
      if (result.success && result.data.labelHtml) {
        const printWindow = window.open('', '_blank');
        if (printWindow) {
          printWindow.document.open();
          printWindow.document.write(result.data.labelHtml);
          printWindow.document.close();
          printWindow.onload = () => {
            printWindow.focus();
            printWindow.print();
            printWindow.onafterprint = () => printWindow.close();
          };
        }
      } else {
        setError(result.message || 'Failed to generate print label');
      }
    } catch {
      setError('Failed to fetch print label. Please try again.');
    } finally {
      setPrintingIds(prev => { const n = new Set(prev); n.delete(mealItemId); return n; });
    }
  };

  const getStatusBadgeClass = (status: string) => {
    switch (status) {
      case 'PENDING':   return 'status-pending';
      case 'PROCESSED': return 'status-processed';
      default:          return '';
    }
  };

  return (
    <div className="ks-dashboard">

      {/* Logout Modal */}
      {showLogoutModal && (
        <div className="ks-modal-overlay">
          <div className="ks-modal">
            <div className="ks-modal-icon"><AlertTriangle size={40} /></div>
            <h3 className="ks-modal-title">Confirm Logout</h3>
            <p className="ks-modal-message">Are you sure you want to logout? You will need to sign in again.</p>
            <div className="ks-modal-actions">
              <button className="ks-modal-cancel" onClick={() => setShowLogoutModal(false)} disabled={isLoggingOut}>Cancel</button>
              <button className="ks-modal-confirm" onClick={handleConfirmLogout} disabled={isLoggingOut}>
                {isLoggingOut ? 'Logging out...' : 'Yes, Logout'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Notes Modal */}
      {showNotesModal && (
        <div className="ks-modal-overlay" onClick={() => setShowNotesModal(false)}>
          <div className="ks-modal" onClick={(e) => e.stopPropagation()}>
            <h3 className="ks-modal-title">Dietician Notes</h3>
            <p className="ks-modal-message" style={{ textAlign: 'left', whiteSpace: 'pre-wrap' }}>{selectedNotes}</p>
            <div className="ks-modal-actions">
              <button className="ks-modal-cancel" onClick={() => setShowNotesModal(false)} style={{ flex: 'none', width: '100%' }}>Close</button>
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
        <button className="ks-sidebar-close" onClick={() => setIsSidebarOpen(false)}><X size={20} /></button>
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
            <LogOut size={16} /><span>Logout</span>
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="ks-main">
        <div className="ks-content-wrapper">

          <div className="ks-welcome">
            <div className="ks-page-header">
              <div>
                <h1 className="ks-welcome-title">Breakfast Queue</h1>
                <p className="ks-welcome-subtitle">
                  <span className="ks-role-badge">BREAKFAST</span>
                  &nbsp;·&nbsp;{totalElements} total orders
                </p>
              </div>
            </div>
          </div>

          {error && (
            <div className="ks-alert-error">
              <AlertTriangle size={18} />
              <span>{error}</span>
              <button onClick={() => setError(null)} className="ks-alert-close">×</button>
            </div>
          )}

          {/* Filters */}
          <div className="filters-section">
            <div className="search-bar">
              <Search size={20} />
              <input
                type="text"
                placeholder="Search by UHID, patient name, or food item..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="search-input"
              />
            </div>
            <div className="status-filters">
              {['ALL', 'PENDING', 'PROCESSED'].map(status => (
                <button
                  key={status}
                  className={`status-filter-btn ${statusFilter === status ? 'active' : ''}`}
                  onClick={() => setStatusFilter(status)}
                >
                  {status}
                </button>
              ))}
            </div>
          </div>

          {/* Queue Table */}
          <div className="ks-card">
            {isLoading ? (
              <div className="ks-table-loading">
                {[...Array(5)].map((_, i) => (
                  <div key={i} className="ks-skeleton-row">
                    <div className="ks-skeleton ks-skeleton-cell" />
                    <div className="ks-skeleton ks-skeleton-cell" />
                    <div className="ks-skeleton ks-skeleton-cell" />
                  </div>
                ))}
              </div>
            ) : mealItems.length === 0 ? (
              <div className="ks-empty-state">
                <Coffee size={48} />
                <p>No breakfast orders found</p>
              </div>
            ) : (
              <>
                {/* Desktop Table */}
                <div className="queue-table-wrap">
                  <table className="queue-table">
                    <thead>
                      <tr>
                        <th>#</th>
                        <th>UHID</th>
                        <th>Patient Name</th>
                        <th>Room</th>
                        <th>Food Item</th>
                        <th>Status</th>
                        <th>Ordered At</th>
                        <th>Processed At</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {mealItems.map((item) => (
                        <tr key={item.mealItemId}>
                          <td className="queue-position">{item.queuePosition}</td>
                          <td className="uhid-cell">{item.uhid}</td>
                          <td className="patient-name">{item.patientName}</td>
                          <td className="room-cell">{item.roomNumber}</td>
                          <td className="food-item-cell">{item.foodItemName}</td>
                          <td>
                            <span className={`status-badge ${getStatusBadgeClass(item.status)}`}>
                              {item.status}
                            </span>
                          </td>
                          <td className="time-cell">{formatDateTime(item.orderedAt)}</td>
                          <td className="time-cell">
                            {item.status === 'PROCESSED' && item.processedAt
                              ? formatDateTime(item.processedAt)
                              : '—'}
                          </td>
                          <td className="actions-cell">
                            <button
                              className="action-label-btn notes-btn"
                              onClick={() => handleViewNotes(item.dieticianNotes)}
                            >
                              <Eye size={14} /> Notes
                            </button>
                            {item.status === 'PENDING' && (
                              <button
                                className="action-label-btn process-btn"
                                onClick={() => handleProcessMeal(item.mealItemId)}
                                disabled={processingIds.has(item.mealItemId)}
                              >
                                <CheckCircle2 size={14} />
                                {processingIds.has(item.mealItemId) ? 'Serving...' : 'Serve'}
                              </button>
                            )}
                            <button
                              className="action-label-btn print-btn"
                              onClick={() => handlePrintMeal(item.mealItemId)}
                              disabled={printingIds.has(item.mealItemId)}
                            >
                              <Printer size={14} />
                              {printingIds.has(item.mealItemId) ? 'Printing...' : 'Print'}
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                {/* Mobile Cards */}
                <div className="queue-cards">
                  {mealItems.map((item) => (
                    <div key={item.mealItemId} className="queue-card">
                      <div className="queue-card-header">
                        <span className="queue-number">#{item.queuePosition}</span>
                        <span className={`status-badge ${getStatusBadgeClass(item.status)}`}>{item.status}</span>
                      </div>
                      <div className="queue-card-patient">
                        <h3>{item.patientName}</h3>
                        <p>UHID: {item.uhid} · Room {item.roomNumber}</p>
                      </div>
                      <div className="queue-card-food"><strong>{item.foodItemName}</strong></div>
                      <div className="queue-card-time">{formatDateTime(item.orderedAt)}</div>
                      {item.status === 'PROCESSED' && item.processedAt && (
                        <div className="queue-card-processed">Served: {formatDateTime(item.processedAt)}</div>
                      )}
                      <div className="queue-card-actions">
                        <button className="action-btn view-btn" onClick={() => handleViewNotes(item.dieticianNotes)}>
                          <Eye size={16} /> Notes
                        </button>
                        {item.status === 'PENDING' && (
                          <button
                            className="action-btn process-full-btn"
                            onClick={() => handleProcessMeal(item.mealItemId)}
                            disabled={processingIds.has(item.mealItemId)}
                          >
                            <CheckCircle2 size={16} />
                            {processingIds.has(item.mealItemId) ? 'Serving...' : 'Serve'}
                          </button>
                        )}
                        <button
                          className="action-btn print-full-btn"
                          onClick={() => handlePrintMeal(item.mealItemId)}
                          disabled={printingIds.has(item.mealItemId)}
                        >
                          <Printer size={16} />
                          {printingIds.has(item.mealItemId) ? 'Printing...' : 'Print'}
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </>
            )}
          </div>

        </div>
      </main>
    </div>
  );
};

export default KitchenBreakfast;