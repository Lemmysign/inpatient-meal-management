import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  LayoutDashboard, Users, ChefHat, LogOut, Menu, X,
  AlertTriangle, ShieldCheck, Search, Plus, CheckCircle, XCircle, Loader2
} from 'lucide-react';
import { config } from '../../config/config';
import '../../styles/admin/admindashboard.css';
import '../../styles/admin/admindieticians.css';
import evercarelogo from '../../assets/icons/ec_logo.png';

/* ─── Types ─────────────────────────────────────── */
interface Dietician {
  id: string;
  name: string;
  staffId: string;
  email: string;
  phoneNumber: string;
  isActive: boolean;
  hasPassword: boolean;
  createdAt: string;
  updatedAt: string;
}


interface AdminUser { userId: string; email: string; name: string; role: string; }

interface Toast { id: number; type: 'success' | 'error'; message: string; }

interface CreateForm {
  name: string;
  email: string;
  staffId: string;
  phoneNumber: string;
}

const EMPTY_FORM: CreateForm = { name: '', email: '', staffId: '', phoneNumber: '' };

const NAV_ITEMS = [
  { label: 'Dashboard',  icon: LayoutDashboard, path: '/admin/dashboard'  },
  { label: 'Dieticians', icon: Users,           path: '/admin/dieticians' },
  { label: 'Servers',    icon: ChefHat,         path: '/admin/servers'    },
];

const getInitials = (name: string) =>
  name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);

const formatDate = (iso: string) =>
  new Date(iso).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });

/* ─── Component ─────────────────────────────────── */
const AdminDieticians: React.FC = () => {
  const navigate = useNavigate();
  const [isSidebarOpen,   setIsSidebarOpen]   = useState(false);
  const [user,            setUser]            = useState<AdminUser | null>(null);
  const [dieticians,      setDieticians]      = useState<Dietician[]>([]);
  const [isLoading,       setIsLoading]       = useState(true);
  const [error,           setError]           = useState<string | null>(null);
  const [searchQuery,     setSearchQuery]     = useState('');
  const [filterStatus,    setFilterStatus]    = useState<'ALL' | 'ACTIVE' | 'INACTIVE'>('ALL');
  const [togglingId,      setTogglingId]      = useState<string | null>(null);
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [isLoggingOut,    setIsLoggingOut]    = useState(false);
  const [toasts,          setToasts]          = useState<Toast[]>([]);
  const [totalElements,   setTotalElements]   = useState(0);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [createForm,      setCreateForm]      = useState<CreateForm>(EMPTY_FORM);
  const [createErrors,    setCreateErrors]    = useState<Partial<CreateForm>>({});
  const [isCreating,      setIsCreating]      = useState(false);
  const searchTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

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

  const showToast = (type: 'success' | 'error', message: string) => {
    const id = Date.now();
    setToasts(prev => [...prev, { id, type, message }]);
    setTimeout(() => setToasts(prev => prev.filter(t => t.id !== id)), 4000);
  };

  const fetchDieticians = useCallback(async (query = '') => {
    setIsLoading(true);
    setError(null);
    try {
      const token = getToken();
      if (!token) return;

      const url = query.trim()
        ? `${config.adminDieticianSearchUrl}?query=${encodeURIComponent(query.trim())}`
        : config.adminDieticiansUrl;

      const res = await fetch(url, {
        headers: { 'Authorization': `Bearer ${token}` },
      });
      if (res.status === 401) { handle401(); return; }

      const json = await res.json();
      if (json.success) {
        setDieticians(json.data.content ?? []);
        setTotalElements(json.data.totalElements ?? 0);
      } else {
        setError(json.message || 'Failed to load dieticians');
      }
    } catch {
      setError('Failed to load dieticians. Check your connection.');
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => { fetchDieticians(); }, [fetchDieticians]);

  /* Debounced search */
  const handleSearchChange = (value: string) => {
    setSearchQuery(value);
    if (searchTimer.current) clearTimeout(searchTimer.current);
    searchTimer.current = setTimeout(() => fetchDieticians(value), 400);
  };

  const handleToggleStatus = async (dietician: Dietician) => {
    const token = getToken();
    if (!token) return;

    setTogglingId(dietician.id);
    const newStatus = !dietician.isActive;
    try {
      const res = await fetch(
        `${config.adminDieticianStatusUrl(dietician.id)}?isActive=${newStatus}`,
        { method: 'PATCH', headers: { 'Authorization': `Bearer ${token}` } }
      );
      if (res.status === 401) { handle401(); return; }

      const json = await res.json();
      if (json.success) {
        setDieticians(prev =>
          prev.map(d => d.id === dietician.id ? { ...d, isActive: newStatus } : d)
        );
        showToast('success', `${dietician.name} marked as ${newStatus ? 'Active' : 'Inactive'}`);
      } else {
        showToast('error', json.message || 'Failed to update status');
      }
    } catch {
      showToast('error', 'Failed to update status. Try again.');
    } finally {
      setTogglingId(null);
    }
  };

  const validateCreate = (): boolean => {
    const errs: Partial<CreateForm> = {};
    if (!createForm.name.trim())        errs.name        = 'Name is required';
    if (!createForm.email.trim())       errs.email       = 'Email is required';
    else if (!/\S+@\S+\.\S+/.test(createForm.email)) errs.email = 'Invalid email address';
    if (!createForm.staffId.trim())     errs.staffId     = 'Staff ID is required';
    if (!createForm.phoneNumber.trim()) errs.phoneNumber = 'Phone number is required';
    setCreateErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleCreate = async () => {
    if (!validateCreate()) return;
    const token = getToken();
    if (!token) return;
    setIsCreating(true);
    try {
      const res = await fetch(config.adminDieticiansUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify({
          name:        createForm.name.trim(),
          email:       createForm.email.trim(),
          staffId:     createForm.staffId.trim(),
          phoneNumber: createForm.phoneNumber.trim(),
        }),
      });
      if (res.status === 401) { handle401(); return; }
      const json = await res.json();
      if (json.success) {
        showToast('success', json.message || 'Dietician created successfully');
        setShowCreateModal(false);
        setCreateForm(EMPTY_FORM);
        setCreateErrors({});
        fetchDieticians(searchQuery);
      } else {
        showToast('error', json.message || 'Failed to create dietician');
      }
    } catch {
      showToast('error', 'Failed to create dietician. Check your connection.');
    } finally {
      setIsCreating(false);
    }
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

  /* Client-side status filter on top of API results */
  const filtered = dieticians.filter(d => {
    if (filterStatus === 'ACTIVE')   return d.isActive;
    if (filterStatus === 'INACTIVE') return !d.isActive;
    return true;
  });

  return (
    <div className="ad-dashboard">

      {/* ── Toasts ── */}
      <div className="ad-toast-container">
        {toasts.map(t => (
          <div key={t.id} className={`ad-toast ad-toast-${t.type}`}>
            {t.type === 'success' ? <CheckCircle size={18}/> : <XCircle size={18}/>}
            <span>{t.message}</span>
            <button onClick={() => setToasts(prev => prev.filter(x => x.id !== t.id))}>×</button>
          </div>
        ))}
      </div>

      {/* ── Create Dietician Modal ── */}
      {showCreateModal && (
        <div className="ad-modal-overlay" onClick={e => { if (e.target === e.currentTarget) { setShowCreateModal(false); } }}>
          <div className="ad-modal ad-create-modal">
            <div className="ad-create-modal-header">
              <h3 className="ad-modal-title">Create Dietician</h3>
              <button className="ad-modal-close" onClick={() => setShowCreateModal(false)} disabled={isCreating}>
                <X size={18}/>
              </button>
            </div>
            <p className="ad-modal-msg">Fill in the details below. An invitation email will be sent to the dietician.</p>

            <div className="ad-create-form">
              <div className="ad-form-field">
                <label className="ad-form-label">Full Name <span>*</span></label>
                <input
                  className={`ad-form-input ${createErrors.name ? 'error' : ''}`}
                  type="text"
                  placeholder="e.g. Dr. Jane Smith"
                  value={createForm.name}
                  onChange={e => { setCreateForm(p => ({ ...p, name: e.target.value })); setCreateErrors(p => ({ ...p, name: '' })); }}
                  disabled={isCreating}
                />
                {createErrors.name && <p className="ad-form-error">{createErrors.name}</p>}
              </div>

              <div className="ad-form-field">
                <label className="ad-form-label">Email Address <span>*</span></label>
                <input
                  className={`ad-form-input ${createErrors.email ? 'error' : ''}`}
                  type="email"
                  placeholder="e.g. jane.smith@hospital.com"
                  value={createForm.email}
                  onChange={e => { setCreateForm(p => ({ ...p, email: e.target.value })); setCreateErrors(p => ({ ...p, email: '' })); }}
                  disabled={isCreating}
                />
                {createErrors.email && <p className="ad-form-error">{createErrors.email}</p>}
              </div>

              <div className="ad-form-row">
                <div className="ad-form-field">
                  <label className="ad-form-label">Staff ID <span>*</span></label>
                  <input
                    className={`ad-form-input ${createErrors.staffId ? 'error' : ''}`}
                    type="text"
                    placeholder="e.g. DT-005"
                    value={createForm.staffId}
                    onChange={e => { setCreateForm(p => ({ ...p, staffId: e.target.value })); setCreateErrors(p => ({ ...p, staffId: '' })); }}
                    disabled={isCreating}
                  />
                  {createErrors.staffId && <p className="ad-form-error">{createErrors.staffId}</p>}
                </div>

                <div className="ad-form-field">
                  <label className="ad-form-label">Phone Number <span>*</span></label>
                  <input
                    className={`ad-form-input ${createErrors.phoneNumber ? 'error' : ''}`}
                    type="tel"
                    placeholder="e.g. +2348012345678"
                    value={createForm.phoneNumber}
                    onChange={e => { setCreateForm(p => ({ ...p, phoneNumber: e.target.value })); setCreateErrors(p => ({ ...p, phoneNumber: '' })); }}
                    disabled={isCreating}
                  />
                  {createErrors.phoneNumber && <p className="ad-form-error">{createErrors.phoneNumber}</p>}
                </div>
              </div>
            </div>

            <div className="ad-modal-actions">
              <button className="ad-modal-cancel" onClick={() => setShowCreateModal(false)} disabled={isCreating}>
                Cancel
              </button>
              <button className="ad-modal-confirm ad-modal-create" onClick={handleCreate} disabled={isCreating}>
                {isCreating ? <><Loader2 size={15} className="ad-btn-spinner"/> Creating…</> : <><Plus size={15}/> Create Dietician</>}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── Logout Modal ── */}
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

      {/* ── Mobile Header ── */}
      <div className="ad-mobile-header">
        <img src={evercarelogo} alt="Evercare" className="ad-mobile-logo"/>
        <button className="ad-hamburger" onClick={() => setIsSidebarOpen(true)}>
          <Menu size={24}/>
        </button>
      </div>

      {isSidebarOpen && <div className="ad-overlay" onClick={() => setIsSidebarOpen(false)}/>}

      {/* ── Sidebar ── */}
      <aside className={`ad-sidebar ${isSidebarOpen ? 'ad-sidebar-open' : ''}`}>
        <button className="ad-sidebar-close" onClick={() => setIsSidebarOpen(false)}><X size={20}/></button>
        <div className="ad-sidebar-brand">
          <img src={evercarelogo} alt="Evercare" className="ad-sidebar-logo"/>
        </div>
        <div className="ad-sidebar-badge"><ShieldCheck size={13}/> Admin Portal</div>
        <nav className="ad-nav">
          {NAV_ITEMS.map(item => {
            const Icon = item.icon;
            const isActive = window.location.pathname === item.path;
            return (
              <button key={item.label}
                className={`ad-nav-item ${isActive ? 'active' : ''}`}
                onClick={() => handleNavigation(item.path)}>
                <Icon size={18}/><span>{item.label}</span>
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

      {/* ── Main ── */}
      <main className="ad-main">
        <div className="ad-content">

          {/* Page Header */}
          <div className="ad-page-header">
            <div>
              <h1 className="ad-page-title">Dieticians</h1>
              <p className="ad-page-sub">
                Manage dietician accounts and access
                {totalElements > 0 && <span className="ad-count-badge">{totalElements} total</span>}
              </p>
            </div>
            <button className="ad-create-btn" onClick={() => { setShowCreateModal(true); setCreateForm(EMPTY_FORM); setCreateErrors({}); }}>
              <Plus size={16}/> Create Dietician
            </button>
          </div>

          {/* Search + Filter Bar */}
          <div className="ad-toolbar">
            <div className="ad-search-bar">
              <Search size={17} className="ad-search-icon"/>
              <input
                type="text"
                placeholder="Search by name, email or staff ID..."
                value={searchQuery}
                onChange={e => handleSearchChange(e.target.value)}
                className="ad-search-input"
              />
              {searchQuery && (
                <button className="ad-search-clear" onClick={() => { setSearchQuery(''); fetchDieticians(''); }}>
                  <X size={14}/>
                </button>
              )}
            </div>
            <div className="ad-status-filters">
              {(['ALL', 'ACTIVE', 'INACTIVE'] as const).map(s => (
                <button key={s}
                  className={`ad-filter-pill-btn ${filterStatus === s ? 'active' : ''}`}
                  onClick={() => setFilterStatus(s)}>
                  {s === 'ALL' ? 'All' : s === 'ACTIVE' ? 'Active' : 'Inactive'}
                </button>
              ))}
            </div>
          </div>

          {error && (
            <div className="ad-alert-error">
              <AlertTriangle size={16}/><span>{error}</span>
              <button onClick={() => setError(null)}>×</button>
            </div>
          )}

          {/* ── Table Card ── */}
          <div className="ad-card">
            {isLoading ? (
              <div className="ad-table-skeleton">
                {[...Array(5)].map((_, i) => (
                  <div key={i} className="ad-skeleton-row"/>
                ))}
              </div>
            ) : filtered.length === 0 ? (
              <div className="ad-empty-state">
                <Users size={44}/>
                <p>{searchQuery ? 'No dieticians match your search' : 'No dieticians found'}</p>
              </div>
            ) : (
              <>
                {/* Desktop Table */}
                <div className="ad-table-wrap">
                  <table className="ad-table">
                    <thead>
                      <tr>
                        <th>Name</th>
                        <th>Staff ID</th>
                        <th>Email</th>
                        <th>Phone</th>
                        <th>Created</th>
                        <th>Status</th>
                        <th>Action</th>
                      </tr>
                    </thead>
                    <tbody>
                      {filtered.map(d => (
                        <tr key={d.id}>
                          <td>
                            <div className="ad-name-cell">
                              <div className="ad-avatar">{getInitials(d.name)}</div>
                              <div>
                                <p className="ad-name">{d.name}</p>
                                {!d.hasPassword && <span className="ad-no-pwd">No password set</span>}
                              </div>
                            </div>
                          </td>
                          <td><span className="ad-staff-id">{d.staffId}</span></td>
                          <td className="ad-email">{d.email}</td>
                          <td className="ad-phone">{d.phoneNumber}</td>
                          <td className="ad-date">{formatDate(d.createdAt)}</td>
                          <td>
                            <span className={`ad-status-badge ${d.isActive ? 'active' : 'inactive'}`}>
                              {d.isActive ? 'Active' : 'Inactive'}
                            </span>
                          </td>
                          <td>
                            <button
                              className={`ad-toggle-btn ${d.isActive ? 'deactivate' : 'activate'}`}
                              onClick={() => handleToggleStatus(d)}
                              disabled={togglingId === d.id}
                            >
                              {togglingId === d.id
                                ? 'Updating…'
                                : d.isActive ? 'Deactivate' : 'Activate'}
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                {/* Mobile Cards */}
                <div className="ad-mobile-cards">
                  {filtered.map(d => (
                    <div key={d.id} className="ad-dietician-card">
                      <div className="ad-dc-header">
                        <div className="ad-dc-identity">
                          <div className="ad-avatar ad-avatar-lg">{getInitials(d.name)}</div>
                          <div>
                            <p className="ad-name">{d.name}</p>
                            <span className="ad-staff-id">{d.staffId}</span>
                          </div>
                        </div>
                        <span className={`ad-status-badge ${d.isActive ? 'active' : 'inactive'}`}>
                          {d.isActive ? 'Active' : 'Inactive'}
                        </span>
                      </div>
                      <div className="ad-dc-details">
                        <p>{d.email}</p>
                        <p>{d.phoneNumber}</p>
                        <p className="ad-date">{formatDate(d.createdAt)}</p>
                      </div>
                      <button
                        className={`ad-toggle-btn full-width ${d.isActive ? 'deactivate' : 'activate'}`}
                        onClick={() => handleToggleStatus(d)}
                        disabled={togglingId === d.id}
                      >
                        {togglingId === d.id ? 'Updating…' : d.isActive ? 'Deactivate' : 'Activate'}
                      </button>
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

export default AdminDieticians;