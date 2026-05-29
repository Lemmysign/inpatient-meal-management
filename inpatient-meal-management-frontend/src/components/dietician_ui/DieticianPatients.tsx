import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  LayoutDashboard, Users, Layers, UtensilsCrossed, LogOut, Menu, X,
  Search, Eye, AlertTriangle, ChefHat, ChevronLeft, ChevronRight,
  ClipboardList, Filter, Pencil, CheckCircle, ChevronDown,
} from 'lucide-react';
import { config } from '../../config/config';
import '../../styles/dietician/dieticianpatients.css';
import evercarelogo     from '../../assets/icons/ec_logo.png';
import evercareLogoFull from '../../assets/icons/EvercareLogo.png';

// ── Types ──────────────────────────────────────────────────
interface DieticianUser {
  userId: string;
  email: string;
  name: string;
  role: string;
}

interface PatientMenu {
  id: string;
  patientId: string;
  patientUhid: string;
  patientName: string;
  patientRoomNumber: string;
  menuGroupId: string;
  menuGroupName: string;
  menuGroupDescription: string;
  assignedByDieticianId: string;
  assignedByDieticianName: string;
  notes: string;
  assignedAt: string;
  validFrom: string;
  validUntil: string;
  isActive: boolean;
}

interface PatientsResponse {
  success: boolean;
  message: string;
  data: {
    content: PatientMenu[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    first: boolean;
    last: boolean;
    empty: boolean;
  };
  timestamp: string;
}

interface MenuGroupOption {
  id: string;
  name: string;
  description: string;
  isActive: boolean;
  assignedPatientsCount: number;
}

interface DieticianOption {
  id: string;
  name: string;
  email: string;
  isActive: boolean;
}

interface FilterOptions {
  success: boolean;
  data: MenuGroupOption[] | DieticianOption[];
}

interface EditFormData {
  menuGroupId: string;
  validFrom: string;
  validUntil: string;
  isActive: boolean;
  notes: string;
}

interface AssignFormData {
  uhid: string;
  menuGroupIds: string[];
  validFrom: string;
  validUntil: string;
  notes: string;
}

interface ToastData {
  id: number;
  message: string;
  type: 'success' | 'error';
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

const toDateInputValue = (iso: string): string => {
  if (!iso) return '';
  return iso.slice(0, 10);
};

const getInitials = (name: string) =>
  name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);

// ── Toast Component ────────────────────────────────────────
const ToastContainer: React.FC<{ toasts: ToastData[]; onRemove: (id: number) => void }> = ({ toasts, onRemove }) => (
  <div className="dc-toast-container">
    {toasts.map(toast => (
      <div key={toast.id} className={`dc-toast dc-toast-${toast.type}`}>
        <div className="dc-toast-icon">
          {toast.type === 'success' ? <CheckCircle size={18} /> : <AlertTriangle size={18} />}
        </div>
        <span className="dc-toast-message">{toast.message}</span>
        <button className="dc-toast-close" onClick={() => onRemove(toast.id)}>
          <X size={14} />
        </button>
      </div>
    ))}
  </div>
);

// ── Component ──────────────────────────────────────────────
const DieticianPatients: React.FC = () => {
  const navigate = useNavigate();
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [user, setUser] = useState<DieticianUser | null>(null);
  const [patients, setPatients] = useState<PatientMenu[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  // Toast notifications
  const [toasts, setToasts] = useState<ToastData[]>([]);
  const toastIdRef = useRef(0);

  const showToast = (message: string, type: 'success' | 'error' = 'success') => {
    const id = ++toastIdRef.current;
    setToasts(prev => [...prev, { id, message, type }]);
    setTimeout(() => setToasts(prev => prev.filter(t => t.id !== id)), 4000);
  };

  const removeToast = (id: number) => setToasts(prev => prev.filter(t => t.id !== id));

  // Pagination
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const pageSize = 10;

  // Filters
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<'all' | 'active' | 'inactive'>('all');
  const [menuGroupFilter, setMenuGroupFilter] = useState<string[]>([]);
  const [dieticianFilter, setDieticianFilter] = useState<string[]>([]);
  const [dateFrom, setDateFrom] = useState<string>('');
  const [dateTo, setDateTo] = useState<string>('');

  // Filter options from backend (shared between filter bar & assign modal)
  const [menuGroupOptions, setMenuGroupOptions] = useState<MenuGroupOption[]>([]);
  const [dieticianOptions, setDieticianOptions] = useState<DieticianOption[]>([]);
  const [isLoadingFilters, setIsLoadingFilters] = useState(true);

  // Notes modal
  const [showNotesModal, setShowNotesModal] = useState(false);
  const [selectedNotes, setSelectedNotes] = useState<{ patient: string; notes: string } | null>(null);

  // Edit modal
  const [showEditModal, setShowEditModal] = useState(false);
  const [editingPatient, setEditingPatient] = useState<PatientMenu | null>(null);
  const [editForm, setEditForm] = useState<EditFormData>({
    menuGroupId: '', validFrom: '', validUntil: '', isActive: true, notes: '',
  });
  const [isSaving, setIsSaving] = useState(false);
  const [editError, setEditError] = useState<string | null>(null);
  const [editSuccess, setEditSuccess] = useState(false);

  // Assign menu modal
  const [showAssignModal, setShowAssignModal] = useState(false);
  const [assignForm, setAssignForm] = useState<AssignFormData>({
    uhid: '', menuGroupIds: [], validFrom: '', validUntil: '', notes: '',
  });
  const [isAssigning, setIsAssigning] = useState(false);
  const [assignError, setAssignError] = useState<string | null>(null);
  const [showAssignMenuGroupDropdown, setShowAssignMenuGroupDropdown] = useState(false);
  const assignDropdownRef = useRef<HTMLDivElement>(null);

  // Filter bar custom dropdowns
  const [showMenuGroupDropdown, setShowMenuGroupDropdown] = useState(false);
  const [showDieticianDropdown, setShowDieticianDropdown] = useState(false);

  // Close all dropdowns on outside click
  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      const target = e.target as HTMLElement;
      if (!target.closest('.dc-custom-dropdown')) {
        setShowMenuGroupDropdown(false);
        setShowDieticianDropdown(false);
      }
      if (assignDropdownRef.current && !assignDropdownRef.current.contains(target)) {
        setShowAssignMenuGroupDropdown(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // ── Filter helpers ────────────────────────────────────────
  const toggleMenuGroupFilter = (id: string) =>
    setMenuGroupFilter(prev => prev.includes(id) ? prev.filter(i => i !== id) : [...prev, id]);

  const toggleDieticianFilter = (id: string) =>
    setDieticianFilter(prev => prev.includes(id) ? prev.filter(i => i !== id) : [...prev, id]);

  const getSelectedMenuGroupNames = (): string => {
    if (menuGroupFilter.length === 0) return 'All Menu Groups';
    if (menuGroupFilter.length === 1) return menuGroupOptions.find(g => g.id === menuGroupFilter[0])?.name || 'Selected';
    return `${menuGroupFilter.length} selected`;
  };

  const getSelectedDieticianNames = (): string => {
    if (dieticianFilter.length === 0) return 'All Dieticians';
    if (dieticianFilter.length === 1) return dieticianOptions.find(d => d.id === dieticianFilter[0])?.name || 'Selected';
    return `${dieticianFilter.length} selected`;
  };

  // ── Assign modal menu group label ─────────────────────────
  const selectedAssignMenuGroupLabel =
    assignForm.menuGroupIds.length === 0
      ? 'Select menu groups...'
      : assignForm.menuGroupIds.length === 1
        ? menuGroupOptions.find(g => g.id === assignForm.menuGroupIds[0])?.name || 'Selected'
        : `${assignForm.menuGroupIds.length} selected`;

  // ── Bootstrap ──────────────────────────────────────────────
  useEffect(() => {
    const raw = localStorage.getItem('dietician_user');
    if (raw) setUser(JSON.parse(raw));
  }, []);

  useEffect(() => { loadFilterOptions(); }, []);

  const [debouncedSearchTerm, setDebouncedSearchTerm] = useState('');
  useEffect(() => {
    const timer = setTimeout(() => setDebouncedSearchTerm(searchTerm), 500);
    return () => clearTimeout(timer);
  }, [searchTerm]);

  useEffect(() => {
    fetchPatients();
  }, [currentPage, debouncedSearchTerm, statusFilter, menuGroupFilter, dieticianFilter, dateFrom, dateTo]);

  // ── Auth helpers ──────────────────────────────────────────
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

  // ── Data fetching ─────────────────────────────────────────
  const loadFilterOptions = async () => {
    setIsLoadingFilters(true);
    try {
      const token = getToken();
      if (!token) return;
      const [menuGroupRes, dieticianRes] = await Promise.all([
        fetch(`${config.apiUrl}/dietician/filters/menu-groups`, { headers: { 'Authorization': `Bearer ${token}` } }),
        fetch(`${config.apiUrl}/dietician/filters/dieticians`,  { headers: { 'Authorization': `Bearer ${token}` } }),
      ]);
      if (menuGroupRes.ok) {
        const data: FilterOptions = await menuGroupRes.json();
        setMenuGroupOptions(data.data as MenuGroupOption[]);
      }
      if (dieticianRes.ok) {
        const data: FilterOptions = await dieticianRes.json();
        setDieticianOptions(data.data as DieticianOption[]);
      }
    } catch (err) {
      console.error('Failed to load filter options:', err);
    } finally {
      setIsLoadingFilters(false);
    }
  };

  const buildQueryParams = (): string => {
    const params = new URLSearchParams();
    params.append('page', String(currentPage));
    params.append('size', String(pageSize));
    menuGroupFilter.forEach(id => params.append('menuGroupIds', id));
    dieticianFilter.forEach(id => params.append('dieticianIds', id));
    if (debouncedSearchTerm.trim()) params.append('searchTerm', debouncedSearchTerm.trim());
    if (statusFilter === 'active') params.append('isActive', 'true');
    if (statusFilter === 'inactive') params.append('isActive', 'false');
    if (dateFrom) params.append('dateFrom', dateFrom);
    if (dateTo) params.append('dateTo', dateTo);
    return params.toString();
  };

  const fetchPatients = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const token = getToken();
      if (!token) return;
      const res = await fetch(`${config.dieticianPatientsUrl}?${buildQueryParams()}`, {
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
      });
      if (res.status === 401) { handle401(); return; }
      const data: PatientsResponse = await res.json();
      if (data.success) {
        setPatients(data.data.content);
        setTotalPages(data.data.totalPages);
        setTotalElements(data.data.totalElements);
      } else {
        setError(data.message || 'Failed to load patients');
      }
    } catch {
      setError('Failed to load patients. Please check your connection.');
    } finally {
      setIsLoading(false);
    }
  };

  // ── Handlers ──────────────────────────────────────────────
  const handleClearFilters = () => {
    setSearchTerm(''); setStatusFilter('all'); setMenuGroupFilter([]);
    setDieticianFilter([]); setDateFrom(''); setDateTo(''); setCurrentPage(0);
  };

  const handleNavigation = (path: string) => { setIsSidebarOpen(false); navigate(path); };

  const handleConfirmLogout = async () => {
    setIsLoggingOut(true);
    try {
      const token = localStorage.getItem('dietician_token');
      if (token) await fetch(config.dieticianLogoutUrl, { method: 'POST', headers: { 'Authorization': `Bearer ${token}` } });
    } catch (err) { console.error('Logout error:', err); }
    finally {
      localStorage.removeItem('dietician_token');
      localStorage.removeItem('dietician_user');
      setIsLoggingOut(false); setShowLogoutModal(false); navigate('/dietician/login');
    }
  };

  const handleViewNotes = (patient: PatientMenu) => {
    setSelectedNotes({ patient: `${patient.patientName} (${patient.patientUhid})`, notes: patient.notes || 'No notes available.' });
    setShowNotesModal(true);
  };

  // ── Edit handlers ──────────────────────────────────────────
  const handleEditClick = (patient: PatientMenu) => {
    setEditingPatient(patient);
    setEditForm({
      menuGroupId: patient.menuGroupId,
      validFrom: toDateInputValue(patient.validFrom),
      validUntil: toDateInputValue(patient.validUntil),
      isActive: patient.isActive,
      notes: patient.notes || '',
    });
    setEditError(null); setEditSuccess(false); setShowEditModal(true);
  };

  const handleEditClose = () => {
    if (isSaving) return;
    setShowEditModal(false); setEditingPatient(null); setEditError(null); setEditSuccess(false);
  };

  const handleEditSave = async () => {
    if (!editingPatient) return;
    if (!editForm.menuGroupId) { setEditError('Please select a menu group.'); return; }
    if (!editForm.validFrom || !editForm.validUntil) { setEditError('Valid From and Valid Until dates are required.'); return; }
    if (editForm.validFrom > editForm.validUntil) { setEditError('"Valid From" cannot be after "Valid Until".'); return; }
    setIsSaving(true); setEditError(null);
    try {
      const token = getToken();
      if (!token) return;
      const res = await fetch(config.dieticianPatientMenuUrl(editingPatient.id), {
        method: 'PUT',
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
        body: JSON.stringify({
          menuGroupId: editForm.menuGroupId,
          validFrom: editForm.validFrom,
          validUntil: editForm.validUntil,
          isActive: editForm.isActive,
          notes: editForm.notes,
        }),
      });
      if (res.status === 401) { handle401(); return; }
      const data = await res.json();
      if (data.success) {
        setEditSuccess(true);
        setPatients(prev => prev.map(p => p.id === editingPatient.id ? { ...p, ...data.data } : p));
        setTimeout(() => { setShowEditModal(false); setEditingPatient(null); setEditSuccess(false); }, 1200);
      } else {
        setEditError(data.message || 'Failed to update patient menu.');
      }
    } catch {
      setEditError('Failed to save. Please check your connection.');
    } finally {
      setIsSaving(false);
    }
  };

  // ── Assign Menu handlers ───────────────────────────────────
  const handleOpenAssignModal = () => {
    setAssignForm({ uhid: '', menuGroupIds: [], validFrom: '', validUntil: '', notes: '' });
    setAssignError(null);
    setShowAssignModal(true);
  };

  const handleAssignClose = () => {
    if (isAssigning) return;
    setShowAssignModal(false);
    setAssignError(null);
    setShowAssignMenuGroupDropdown(false);
  };

  const toggleAssignMenuGroup = (id: string) => {
    setAssignForm(f => ({
      ...f,
      menuGroupIds: f.menuGroupIds.includes(id)
        ? f.menuGroupIds.filter(i => i !== id)
        : [...f.menuGroupIds, id],
    }));
  };

  const handleAssignSubmit = async () => {
    if (!assignForm.uhid.trim()) { setAssignError('Patient UHID is required.'); return; }
    if (assignForm.menuGroupIds.length === 0) { setAssignError('Please select at least one menu group.'); return; }
    if (!assignForm.validFrom || !assignForm.validUntil) { setAssignError('Both date fields are required.'); return; }
    if (assignForm.validFrom > assignForm.validUntil) { setAssignError('"Valid From" cannot be after "Valid Until".'); return; }

    setIsAssigning(true);
    setAssignError(null);
    try {
      const token = getToken();
      if (!token) return;

      const res = await fetch(config.dieticianAssignMenuUrl, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
        body: JSON.stringify({
          uhid: assignForm.uhid.trim(),
          menuGroupIds: assignForm.menuGroupIds,
          validFrom: assignForm.validFrom,
          validUntil: assignForm.validUntil,
          notes: assignForm.notes,
        }),
      });

      if (res.status === 401) { handle401(); return; }

      const data = await res.json();

      if (data.success) {
        const assigned = Array.isArray(data.data) ? data.data[0] : data.data;
        const uhid = assigned?.patientUhid || assignForm.uhid.trim();
        const patientName = assigned?.patientName || '';
        const menuLabel =
          assignForm.menuGroupIds.length === 1
            ? (assigned?.menuGroupName || menuGroupOptions.find(g => g.id === assignForm.menuGroupIds[0])?.name || 'Menu')
            : `${assignForm.menuGroupIds.length} menus`;

        showToast(
          patientName
            ? `${menuLabel} assigned to ${patientName} (UHID: ${uhid})`
            : `${menuLabel} assigned to patient with UHID: ${uhid}`,
          'success'
        );

        setShowAssignModal(false);
        setCurrentPage(0);
      } else {
        setAssignError(data.message || 'Failed to assign menu.');
      }
    } catch {
      setAssignError('Failed to assign menu. Please check your connection.');
    } finally {
      setIsAssigning(false);
    }
  };

  const hasActiveFilters =
    searchTerm.trim() !== '' || statusFilter !== 'all' ||
    menuGroupFilter.length > 0 || dieticianFilter.length > 0 ||
    dateFrom !== '' || dateTo !== '';

  return (
    <div className="dc-dashboard">

      {/* ── Toast Notifications ── */}
      <ToastContainer toasts={toasts} onRemove={removeToast} />

      {/* ── Logout Modal ── */}
      {showLogoutModal && (
        <div className="dc-modal-overlay">
          <div className="dc-modal">
            <div className="dc-modal-icon"><AlertTriangle size={40} /></div>
            <h3 className="dc-modal-title">Confirm Logout</h3>
            <p className="dc-modal-message">Are you sure you want to logout? You will need to sign in again.</p>
            <div className="dc-modal-actions">
              <button className="dc-modal-cancel" onClick={() => setShowLogoutModal(false)} disabled={isLoggingOut}>Cancel</button>
              <button className="dc-modal-confirm" onClick={handleConfirmLogout} disabled={isLoggingOut}>
                {isLoggingOut ? 'Logging out...' : 'Yes, Logout'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── Notes Modal ── */}
      {showNotesModal && selectedNotes && (
        <div className="dc-modal-overlay" onClick={() => setShowNotesModal(false)}>
          <div className="dc-notes-modal" onClick={e => e.stopPropagation()}>
            <div className="dc-notes-header">
              <h3>Patient Notes</h3>
              <button className="dc-notes-close" onClick={() => setShowNotesModal(false)}><X size={20} /></button>
            </div>
            <div className="dc-notes-body">
              <p className="dc-notes-patient">{selectedNotes.patient}</p>
              <div className="dc-notes-content">{selectedNotes.notes}</div>
            </div>
          </div>
        </div>
      )}

      {/* ── Edit Modal ── */}
      {showEditModal && editingPatient && (
        <div className="dc-modal-overlay" onClick={handleEditClose}>
          <div className="dc-edit-modal" onClick={e => e.stopPropagation()}>
            <div className="dc-edit-modal-header">
              <div>
                <h3 className="dc-edit-modal-title">Edit Menu Assignment</h3>
                <p className="dc-edit-modal-subtitle">
                  {editingPatient.patientName}&nbsp;·&nbsp;
                  <span className="dc-uhid">{editingPatient.patientUhid}</span>
                </p>
              </div>
              <button className="dc-notes-close" onClick={handleEditClose} disabled={isSaving}><X size={20} /></button>
            </div>

            <div className="dc-edit-modal-body">
              {editSuccess && (
                <div className="dc-edit-success"><CheckCircle size={18} /><span>Updated successfully!</span></div>
              )}
              {editError && (
                <div className="dc-edit-error"><AlertTriangle size={16} /><span>{editError}</span></div>
              )}

              <div className="dc-edit-field">
                <label className="dc-edit-label">Menu Group</label>
                <select
                  className="dc-filter-select"
                  value={editForm.menuGroupId}
                  onChange={e => setEditForm(f => ({ ...f, menuGroupId: e.target.value }))}
                  disabled={isSaving || isLoadingFilters}
                >
                  {isLoadingFilters ? (
                    <option value="">Loading...</option>
                  ) : (
                    menuGroupOptions.map(group => (
                      <option key={group.id} value={group.id}>
                        {group.name}
                      </option>
                    ))
                  )}
                </select>
              </div>

              <div className="dc-edit-field">
                <label className="dc-edit-label">Valid From</label>
                <input type="date" className="dc-edit-date-input" value={editForm.validFrom}
                  onChange={e => setEditForm(f => ({ ...f, validFrom: e.target.value }))} disabled={isSaving} />
              </div>

              <div className="dc-edit-field">
                <label className="dc-edit-label">Valid Until</label>
                <input type="date" className="dc-edit-date-input" value={editForm.validUntil}
                  onChange={e => setEditForm(f => ({ ...f, validUntil: e.target.value }))} disabled={isSaving} />
              </div>

              <div className="dc-edit-field dc-edit-field-row">
                <label className="dc-edit-label">Active Status</label>
                <button type="button"
                  className={`dc-toggle ${editForm.isActive ? 'dc-toggle-on' : 'dc-toggle-off'}`}
                  onClick={() => setEditForm(f => ({ ...f, isActive: !f.isActive }))}
                  disabled={isSaving} aria-pressed={editForm.isActive}>
                  <span className="dc-toggle-knob" />
                  <span className="dc-toggle-label">{editForm.isActive ? 'Active' : 'Inactive'}</span>
                </button>
              </div>

              <div className="dc-edit-field">
                <label className="dc-edit-label">Notes</label>
                <textarea className="dc-edit-notes-input" placeholder="Enter any dietary notes or instructions..."
                  value={editForm.notes} onChange={e => setEditForm(f => ({ ...f, notes: e.target.value }))}
                  rows={4} disabled={isSaving} />
              </div>
            </div>

            <div className="dc-edit-modal-footer">
              <button className="dc-modal-cancel" onClick={handleEditClose} disabled={isSaving}>Cancel</button>
              <button className="dc-modal-confirm" onClick={handleEditSave} disabled={isSaving || editSuccess}>
                {isSaving ? 'Saving...' : 'Save Changes'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── Assign Menu Modal ── */}
      {showAssignModal && (
        <div className="dc-modal-overlay" onClick={handleAssignClose}>
          <div className="dc-assign-modal" onClick={e => e.stopPropagation()}>

            {/* Header */}
            <div className="dc-assign-modal-header">
              <div>
                <h3 className="dc-assign-modal-title">Assign Menu to Patient</h3>
                <p className="dc-assign-modal-subtitle">Search by UHID and select one or more menu groups</p>
              </div>
              <button className="dc-notes-close" onClick={handleAssignClose} disabled={isAssigning}>
                <X size={20} />
              </button>
            </div>

            {/* Body */}
            <div className="dc-assign-modal-body">

              {assignError && (
                <div className="dc-edit-error">
                  <AlertTriangle size={16} />
                  <span>{assignError}</span>
                </div>
              )}

              {/* UHID */}
              <div className="dc-edit-field">
                <label className="dc-edit-label">Patient UHID <span className="dc-required">*</span></label>
                <input
                  type="text"
                  className="dc-assign-text-input"
                  placeholder="e.g. 546008"
                  value={assignForm.uhid}
                  onChange={e => setAssignForm(f => ({ ...f, uhid: e.target.value }))}
                  disabled={isAssigning}
                />
              </div>

              {/* Menu Group Multi-Select Dropdown */}
              <div className="dc-edit-field">
                <label className="dc-edit-label">
                  Menu Groups <span className="dc-required">*</span>
                  {assignForm.menuGroupIds.length > 0 && (
                    <span className="dc-filter-count">{assignForm.menuGroupIds.length}</span>
                  )}
                </label>
                <div className="dc-assign-dropdown-wrap" ref={assignDropdownRef}>
                  <button
                    type="button"
                    className={`dc-assign-dropdown-trigger ${assignForm.menuGroupIds.length > 0 ? 'dc-assign-dropdown-selected' : ''}`}
                    onClick={() => setShowAssignMenuGroupDropdown(v => !v)}
                    disabled={isAssigning}
                  >
                    <span className="dc-assign-dropdown-value">{selectedAssignMenuGroupLabel}</span>
                    <ChevronDown size={16} className={`dc-assign-chevron ${showAssignMenuGroupDropdown ? 'dc-assign-chevron-open' : ''}`} />
                  </button>

                  {showAssignMenuGroupDropdown && (
                    <div className="dc-assign-dropdown-menu">
                      {isLoadingFilters ? (
                        <div className="dc-dropdown-loading">Loading menu groups...</div>
                      ) : menuGroupOptions.length === 0 ? (
                        <div className="dc-dropdown-empty">No menu groups available</div>
                      ) : (
                        <>
                          <button
                            type="button"
                            className="dc-dropdown-item dc-dropdown-clear"
                            onClick={() => setAssignForm(f => ({ ...f, menuGroupIds: [] }))}
                          >
                            Clear Selection
                          </button>
                          {menuGroupOptions.map(group => (
                            <label key={group.id} className="dc-assign-dropdown-item">
                              <input
                                type="checkbox"
                                checked={assignForm.menuGroupIds.includes(group.id)}
                                onChange={() => toggleAssignMenuGroup(group.id)}
                                disabled={isAssigning}
                              />
                              <div className="dc-assign-dropdown-item-main">
                                <span className="dc-assign-dropdown-item-name">{group.name}</span>
                                {group.assignedPatientsCount > 0 && (
                                  <span className="dc-assign-dropdown-item-count">
                                    {group.assignedPatientsCount} patient{group.assignedPatientsCount !== 1 ? 's' : ''}
                                  </span>
                                )}
                              </div>
                              {group.description && (
                                <span className="dc-assign-dropdown-item-desc">{group.description}</span>
                              )}
                            </label>
                          ))}
                        </>
                      )}
                    </div>
                  )}
                </div>
              </div>

              {/* Dates on the same line */}
              <div className="dc-assign-dates-row">
                <div className="dc-edit-field">
                  <label className="dc-edit-label">Valid From <span className="dc-required">*</span></label>
                  <input
                    type="date"
                    className="dc-edit-date-input"
                    value={assignForm.validFrom}
                    onChange={e => setAssignForm(f => ({ ...f, validFrom: e.target.value }))}
                    disabled={isAssigning}
                  />
                </div>
                <div className="dc-edit-field">
                  <label className="dc-edit-label">Valid Until <span className="dc-required">*</span></label>
                  <input
                    type="date"
                    className="dc-edit-date-input"
                    value={assignForm.validUntil}
                    onChange={e => setAssignForm(f => ({ ...f, validUntil: e.target.value }))}
                    disabled={isAssigning}
                  />
                </div>
              </div>

              {/* Notes */}
              <div className="dc-edit-field">
                <label className="dc-edit-label">Notes</label>
                <textarea
                  className="dc-edit-notes-input"
                  placeholder="e.g. Patient is allergic to garlic..."
                  value={assignForm.notes}
                  onChange={e => setAssignForm(f => ({ ...f, notes: e.target.value }))}
                  rows={3}
                  disabled={isAssigning}
                />
              </div>
            </div>

            {/* Footer */}
            <div className="dc-edit-modal-footer">
              <button className="dc-modal-cancel" onClick={handleAssignClose} disabled={isAssigning}>
                Cancel
              </button>
              <button className="dc-assign-submit-btn" onClick={handleAssignSubmit} disabled={isAssigning}>
                <ClipboardList size={16} />
                <span>{isAssigning ? 'Assigning...' : 'Assign Menu to Patient'}</span>
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
      {isSidebarOpen && <div className="dc-overlay" onClick={() => setIsSidebarOpen(false)} />}

      {/* ── Sidebar ── */}
      <aside className={`dc-sidebar ${isSidebarOpen ? 'dc-sidebar-open' : ''}`}>
        <button className="dc-sidebar-close" onClick={() => setIsSidebarOpen(false)}><X size={20} /></button>

        <div className="dc-sidebar-brand">
          <img src={evercareLogoFull} alt="Evercare" className="dc-sidebar-logo-collapsed" />
          <img src={evercarelogo}     alt="Evercare" className="dc-sidebar-logo-expanded"  />
        </div>

        <nav className="dc-nav">
          {NAV_ITEMS.map(item => {
            const Icon = item.icon;
            const isActive = window.location.pathname === item.path;
            return (
              <button key={item.label} className={`dc-nav-item ${isActive ? 'active' : ''}`}
                onClick={() => handleNavigation(item.path)}>
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
            <LogOut size={16} /><span>Logout</span>
          </button>
        </div>
      </aside>

      {/* ── Main Content ── */}
      <main className="dc-main">
        <div className="dc-content-wrapper">

          {/* Page Header */}
          <div className="dc-page-header">
            <div>
              <h1 className="dc-page-title">Patients</h1>
              <p className="dc-page-subtitle">Manage patient menu assignments and dietary notes</p>
            </div>
            <button className="dc-assign-menu-btn" onClick={handleOpenAssignModal}>
              <ClipboardList size={18} />
              <span>Assign Menu</span>
            </button>
          </div>

          {/* Error */}
          {error && (
            <div className="dc-alert-error">
              <AlertTriangle size={18} />
              <span>{error}</span>
              <button onClick={() => setError(null)} className="dc-alert-close">×</button>
            </div>
          )}

          {/* Filters Bar */}
          <div className="dc-filters-card">
            <div className="dc-filters-header">
              <div className="dc-filters-title">
                <Filter size={18} />
                <span>Filters</span>
                {hasActiveFilters && (
                  <span className="dc-filter-count">
                    {[searchTerm, statusFilter !== 'all', menuGroupFilter.length > 0, dieticianFilter.length > 0, dateFrom, dateTo].filter(Boolean).length}
                  </span>
                )}
              </div>
              {hasActiveFilters && (
                <button className="dc-clear-filters-btn" onClick={handleClearFilters}>Clear All</button>
              )}
            </div>

            <div className="dc-filters-grid">
              {/* Search */}
              <div className="dc-filter-group">
                <label className="dc-filter-label">Search</label>
                <div className="dc-search-box">
                  <Search size={16} className="dc-search-icon" />
                  <input type="text" placeholder="Name or UHID" value={searchTerm}
                    onChange={e => setSearchTerm(e.target.value)} className="dc-search-input" />
                </div>
              </div>

              {/* Status Filter */}
              <div className="dc-filter-group">
                <label className="dc-filter-label">Status</label>
                <select value={statusFilter} onChange={e => setStatusFilter(e.target.value as any)} className="dc-filter-select">
                  <option value="all">All Status</option>
                  <option value="active">Active</option>
                  <option value="inactive">Inactive</option>
                </select>
              </div>

              {/* Menu Group Filter */}
              <div className="dc-filter-group">
                <label className="dc-filter-label">
                  Menu Groups {menuGroupFilter.length > 0 && `(${menuGroupFilter.length})`}
                </label>
                <div className="dc-custom-dropdown">
                  <button type="button" className="dc-dropdown-trigger"
                    onClick={() => setShowMenuGroupDropdown(!showMenuGroupDropdown)}>
                    <span>{getSelectedMenuGroupNames()}</span>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <polyline points="6 9 12 15 18 9" />
                    </svg>
                  </button>
                  {showMenuGroupDropdown && (
                    <div className="dc-dropdown-menu">
                      {isLoadingFilters ? <div className="dc-dropdown-loading">Loading...</div>
                        : menuGroupOptions.length === 0 ? <div className="dc-dropdown-empty">No menu groups available</div>
                        : (
                          <>
                            <button type="button" className="dc-dropdown-item dc-dropdown-clear"
                              onClick={() => setMenuGroupFilter([])}>Clear Selection</button>
                            {menuGroupOptions.map(group => (
                              <label key={group.id} className="dc-dropdown-item">
                                <input type="checkbox" checked={menuGroupFilter.includes(group.id)}
                                  onChange={() => toggleMenuGroupFilter(group.id)} />
                                <span className="dc-dropdown-text">{group.name}</span>
                                <span className="dc-dropdown-count">({group.assignedPatientsCount})</span>
                              </label>
                            ))}
                          </>
                        )}
                    </div>
                  )}
                </div>
              </div>

              {/* Dietician Filter */}
              <div className="dc-filter-group">
                <label className="dc-filter-label">
                  Dieticians {dieticianFilter.length > 0 && `(${dieticianFilter.length})`}
                </label>
                <div className="dc-custom-dropdown">
                  <button type="button" className="dc-dropdown-trigger"
                    onClick={() => setShowDieticianDropdown(!showDieticianDropdown)}>
                    <span>{getSelectedDieticianNames()}</span>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <polyline points="6 9 12 15 18 9" />
                    </svg>
                  </button>
                  {showDieticianDropdown && (
                    <div className="dc-dropdown-menu">
                      {isLoadingFilters ? <div className="dc-dropdown-loading">Loading...</div>
                        : dieticianOptions.length === 0 ? <div className="dc-dropdown-empty">No dieticians available</div>
                        : (
                          <>
                            <button type="button" className="dc-dropdown-item dc-dropdown-clear"
                              onClick={() => setDieticianFilter([])}>Clear Selection</button>
                            {dieticianOptions.map(dietician => (
                              <label key={dietician.id} className="dc-dropdown-item">
                                <input type="checkbox" checked={dieticianFilter.includes(dietician.id)}
                                  onChange={() => toggleDieticianFilter(dietician.id)} />
                                <span className="dc-dropdown-text">{dietician.name}</span>
                              </label>
                            ))}
                          </>
                        )}
                    </div>
                  )}
                </div>
              </div>

              {/* Date From */}
              <div className="dc-filter-group">
                <label className="dc-filter-label">Valid From</label>
                <input type="date" value={dateFrom} onChange={e => setDateFrom(e.target.value)} className="dc-date-input" />
              </div>

              {/* Date To */}
              <div className="dc-filter-group">
                <label className="dc-filter-label">Valid Until</label>
                <input type="date" value={dateTo} onChange={e => setDateTo(e.target.value)} className="dc-date-input" />
              </div>
            </div>
          </div>

          {/* Results count */}
          <div className="dc-results-info">
            Showing <strong>{patients.length}</strong> of <strong>{totalElements}</strong> patient assignments
            {hasActiveFilters && <span className="dc-filtered-text"> (filtered)</span>}
          </div>

          {/* Table */}
          <div className="dc-card">
            {isLoading ? (
              <div className="dc-table-loading">
                {[...Array(5)].map((_, i) => (
                  <div key={i} className="dc-skeleton-row">
                    <div className="dc-skeleton dc-skeleton-cell" />
                    <div className="dc-skeleton dc-skeleton-cell" />
                    <div className="dc-skeleton dc-skeleton-cell" />
                    <div className="dc-skeleton dc-skeleton-cell" />
                  </div>
                ))}
              </div>
            ) : patients.length > 0 ? (
              <>
                <div className="dc-table-wrap">
                  <table className="dc-patients-table">
                    <thead>
                      <tr>
                        <th>#</th>
                        <th>UHID</th>
                        <th>Patient Name</th>
                        <th>Room</th>
                        <th>Menu Group</th>
                        <th>Assigned By</th>
                        <th>Valid From</th>
                        <th>Valid Until</th>
                        <th>Status</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {patients.map((patient, index) => (
                        <tr key={patient.id}>
                          <td className="dc-row-number">{currentPage * pageSize + index + 1}</td>
                          <td><span className="dc-uhid">{patient.patientUhid}</span></td>
                          <td className="dc-patient-name-cell">{patient.patientName}</td>
                          <td>{patient.patientRoomNumber}</td>
                          <td className="dc-menu-cell">{patient.menuGroupName}</td>
                          <td className="dc-dietician-cell">{patient.assignedByDieticianName}</td>
                          <td>{formatDate(patient.validFrom)}</td>
                          <td>{formatDate(patient.validUntil)}</td>
                          <td>
                            <span className={`dc-status-badge ${patient.isActive ? 'active' : 'inactive'}`}>
                              {patient.isActive ? 'Active' : 'Inactive'}
                            </span>
                          </td>
                          <td>
                            <div className="dc-action-buttons">
                              <button className="dc-edit-btn" onClick={() => handleEditClick(patient)}>
                                <Pencil size={16} /><span>Edit</span>
                              </button>
                              <button className="dc-view-notes-btn" onClick={() => handleViewNotes(patient)}>
                                <Eye size={16} /><span>Notes</span>
                              </button>
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                {/* Mobile Cards */}
                <div className="dc-patient-cards">
                  {patients.map(patient => (
                    <div key={patient.id} className="dc-patient-card">
                      <div className="dc-patient-card-header">
                        <div>
                          <p className="dc-patient-card-name">{patient.patientName}</p>
                          <p className="dc-patient-card-meta">
                            <span className="dc-uhid">{patient.patientUhid}</span> · Room {patient.patientRoomNumber}
                          </p>
                        </div>
                        <span className={`dc-status-badge ${patient.isActive ? 'active' : 'inactive'}`}>
                          {patient.isActive ? 'Active' : 'Inactive'}
                        </span>
                      </div>
                      <div className="dc-patient-card-body">
                        <div className="dc-patient-card-row">
                          <span className="dc-label">Menu:</span>
                          <span className="dc-value">{patient.menuGroupName}</span>
                        </div>
                        <div className="dc-patient-card-row">
                          <span className="dc-label">Assigned by:</span>
                          <span className="dc-value">{patient.assignedByDieticianName}</span>
                        </div>
                        <div className="dc-patient-card-row">
                          <span className="dc-label">Valid:</span>
                          <span className="dc-value">{formatDate(patient.validFrom)} – {formatDate(patient.validUntil)}</span>
                        </div>
                      </div>
                      <div className="dc-patient-card-actions">
                        <button className="dc-edit-btn-mobile" onClick={() => handleEditClick(patient)}>
                          <Pencil size={16} /><span>Edit</span>
                        </button>
                        <button className="dc-view-notes-btn-mobile" onClick={() => handleViewNotes(patient)}>
                          <Eye size={16} /><span>View Notes</span>
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </>
            ) : (
              <div className="dc-empty-state">
                <Users size={48} />
                <p>No patients found</p>
                {hasActiveFilters && (
                  <button className="dc-clear-filters-link" onClick={handleClearFilters}>Clear filters</button>
                )}
              </div>
            )}
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="dc-pagination">
              <button className="dc-page-btn"
                onClick={() => setCurrentPage(p => Math.max(0, p - 1))} disabled={currentPage === 0}>
                <ChevronLeft size={18} /><span>Previous</span>
              </button>
              <span className="dc-page-info">Page {currentPage + 1} of {totalPages}</span>
              <button className="dc-page-btn"
                onClick={() => setCurrentPage(p => Math.min(totalPages - 1, p + 1))} disabled={currentPage === totalPages - 1}>
                <span>Next</span><ChevronRight size={18} />
              </button>
            </div>
          )}

        </div>
      </main>
    </div>
  );
};

export default DieticianPatients;