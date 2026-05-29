import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  LayoutDashboard, Users, Layers, UtensilsCrossed,
  LogOut, Menu, X, AlertTriangle, ChefHat, ChevronLeft, ChevronRight,
  CheckCircle, XCircle
} from 'lucide-react';
import { config } from '../../config/config';
import '../../styles/dietician/menugroups.css';
import evercarelogo     from '../../assets/icons/ec_logo.png';
import evercareLogoFull from '../../assets/icons/EvercareLogo.png';

interface MenuGroup {
  id: string;
  name: string;
  description: string;
  isPredefined: boolean;
  createdByDieticianName?: string;
  createdByDieticianId?: string;
  isActive: boolean;
  foodItemCount: number;
  assignedPatientsCount: number;
  createdAt: string;
  updatedAt: string;
}

interface PagedResponse {
  content: MenuGroup[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

interface DieticianUser {
  userId: string;
  email: string;
  name: string;
  role: string;
}

interface Toast {
  id: number;
  type: 'success' | 'error';
  message: string;
}

const NAV_ITEMS = [
  { label: 'Dashboard',   icon: LayoutDashboard, path: '/dietician/dashboard'   },
  { label: 'Patients',    icon: Users,           path: '/dietician/patients'    },
  { label: 'Menu Groups', icon: Layers,          path: '/dietician/menu-groups' },
  { label: 'Food Items',  icon: UtensilsCrossed, path: '/dietician/food-items'  },
];

const getInitials = (name: string) =>
  name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);

const MenuGroups: React.FC = () => {
  const navigate = useNavigate();
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [user, setUser] = useState<DieticianUser | null>(null);
  const [menuGroups, setMenuGroups] = useState<MenuGroup[]>([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  
  // Create modal state
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [createName, setCreateName] = useState('');
  const [createDescription, setCreateDescription] = useState('');
  const [isCreating, setIsCreating] = useState(false);

  // Edit modal state
  const [showEditModal, setShowEditModal] = useState(false);
  const [editingGroup, setEditingGroup] = useState<MenuGroup | null>(null);
  const [editName, setEditName] = useState('');
  const [editDescription, setEditDescription] = useState('');
  const [isSaving, setIsSaving] = useState(false);

  // Delete confirmation state
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deletingGroup, setDeletingGroup] = useState<{ id: string; name: string } | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  // Toast notifications
  const [toasts, setToasts] = useState<Toast[]>([]);

  const pageSize = 10;

  useEffect(() => {
    const raw = localStorage.getItem('dietician_user');
    if (raw) setUser(JSON.parse(raw));
  }, []);

  useEffect(() => {
    fetchMenuGroups(currentPage);
  }, [currentPage]);

  const showToast = (type: 'success' | 'error', message: string) => {
    const id = Date.now();
    setToasts(prev => [...prev, { id, type, message }]);
    setTimeout(() => {
      setToasts(prev => prev.filter(t => t.id !== id));
    }, 4000);
  };

  const removeToast = (id: number) => {
    setToasts(prev => prev.filter(t => t.id !== id));
  };

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

  const fetchMenuGroups = async (page: number) => {
    try {
      setLoading(true);
      const token = getToken();
      if (!token) return;
      
      const url = `${config.dieticianMenuGroupsPagedUrl}?page=${page}&size=${pageSize}`;
      const response = await fetch(url, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (response.status === 401) {
        handle401();
        return;
      }

      if (!response.ok) {
        throw new Error('Failed to fetch menu groups');
      }

      const result = await response.json();
      const pagedData: PagedResponse = result.data;
      
      setMenuGroups(pagedData.content || []);
      setTotalPages(pagedData.totalPages);
      setTotalElements(pagedData.totalElements);
      setCurrentPage(pagedData.page);
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
      console.error('Error fetching menu groups:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateClick = () => {
    setCreateName('');
    setCreateDescription('');
    setShowCreateModal(true);
  };

  const handleCancelCreate = () => {
    setShowCreateModal(false);
    setCreateName('');
    setCreateDescription('');
  };

  const handleSaveCreate = async () => {
    if (!createName.trim() || !createDescription.trim()) {
      showToast('error', 'Name and description are required');
      return;
    }

    try {
      setIsCreating(true);
      const token = getToken();
      if (!token) return;
      
      const response = await fetch(config.dieticianMenuGroupsUrl, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          name: createName.trim(),
          description: createDescription.trim(),
        }),
      });

      if (response.status === 401) {
        handle401();
        return;
      }

      if (!response.ok) {
        throw new Error('Failed to create menu group');
      }

      const result = await response.json();
      
      if (result.success) {
        await fetchMenuGroups(0); // Go to first page to see new item
        setCurrentPage(0);
        handleCancelCreate();
        showToast('success', 'Menu group created successfully');
      } else {
        throw new Error(result.message || 'Failed to create menu group');
      }
    } catch (err) {
      showToast('error', err instanceof Error ? err.message : 'Failed to create menu group');
      console.error('Error creating menu group:', err);
    } finally {
      setIsCreating(false);
    }
  };

  const handleEdit = (group: MenuGroup) => {
    setEditingGroup(group);
    setEditName(group.name);
    setEditDescription(group.description);
    setShowEditModal(true);
  };

  const handleCancelEdit = () => {
    setShowEditModal(false);
    setEditingGroup(null);
    setEditName('');
    setEditDescription('');
  };

  const handleSaveEdit = async () => {
    if (!editingGroup) return;
    
    if (!editName.trim() || !editDescription.trim()) {
      showToast('error', 'Name and description are required');
      return;
    }

    try {
      setIsSaving(true);
      const token = getToken();
      if (!token) return;
      
      const response = await fetch(config.dieticianMenuGroupUrl(editingGroup.id), {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          name: editName.trim(),
          description: editDescription.trim(),
        }),
      });

      if (response.status === 401) {
        handle401();
        return;
      }

      if (!response.ok) {
        throw new Error('Failed to update menu group');
      }

      const result = await response.json();
      
      if (result.success) {
        await fetchMenuGroups(currentPage);
        handleCancelEdit();
        showToast('success', 'Menu group updated successfully');
      } else {
        throw new Error(result.message || 'Failed to update menu group');
      }
    } catch (err) {
      showToast('error', err instanceof Error ? err.message : 'Failed to update menu group');
      console.error('Error updating menu group:', err);
    } finally {
      setIsSaving(false);
    }
  };

  const handleDeleteClick = (menuGroupId: string, menuGroupName: string) => {
    setDeletingGroup({ id: menuGroupId, name: menuGroupName });
    setShowDeleteModal(true);
  };

  const handleCancelDelete = () => {
    setShowDeleteModal(false);
    setDeletingGroup(null);
  };

  const handleConfirmDelete = async () => {
    if (!deletingGroup) return;

    try {
      setIsDeleting(true);
      const token = getToken();
      if (!token) return;
      
      const response = await fetch(config.dieticianMenuGroupUrl(deletingGroup.id), {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (response.status === 401) {
        handle401();
        return;
      }

      if (!response.ok) {
        throw new Error('Menu group is currently assigned to patients, cannot be deleted');
      }

      await fetchMenuGroups(currentPage);
      handleCancelDelete();
      showToast('success', 'Menu group deleted successfully');
    } catch (err) {
      showToast('error', err instanceof Error ? err.message : 'Failed to delete menu group');
      console.error('Error deleting menu group:', err);
    } finally {
      setIsDeleting(false);
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

  const handlePreviousPage = () => {
    if (currentPage > 0) {
      setCurrentPage(currentPage - 1);
    }
  };

  const handleNextPage = () => {
    if (currentPage < totalPages - 1) {
      setCurrentPage(currentPage + 1);
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { 
      year: 'numeric', 
      month: 'short', 
      day: 'numeric' 
    });
  };

  return (
    <div className="dc-dashboard">
      
      {/* ── Toast Notifications ── */}
      <div className="toast-container">
        {toasts.map(toast => (
          <div key={toast.id} className={`toast toast-${toast.type}`}>
            {toast.type === 'success' ? <CheckCircle size={20} /> : <XCircle size={20} />}
            <span>{toast.message}</span>
            <button className="toast-close" onClick={() => removeToast(toast.id)}>×</button>
          </div>
        ))}
      </div>

      {/* ── Create Modal ── */}
      {showCreateModal && (
        <div className="dc-modal-overlay">
          <div className="dc-modal edit-modal">
            <h3 className="dc-modal-title">Create Menu Group</h3>
            
            <div className="edit-form">
              <div className="form-group">
                <label className="form-label">Name</label>
                <input
                  type="text"
                  className="form-input"
                  value={createName}
                  onChange={(e) => setCreateName(e.target.value)}
                  placeholder="Enter menu group name"
                  disabled={isCreating}
                />
              </div>
              
              <div className="form-group">
                <label className="form-label">Description</label>
                <textarea
                  className="form-textarea"
                  value={createDescription}
                  onChange={(e) => setCreateDescription(e.target.value)}
                  placeholder="Enter menu group description"
                  rows={4}
                  disabled={isCreating}
                />
              </div>
            </div>

            <div className="dc-modal-actions">
              <button 
                className="dc-modal-cancel" 
                onClick={handleCancelCreate} 
                disabled={isCreating}
              >
                Cancel
              </button>
              <button 
                className="dc-modal-confirm" 
                onClick={handleSaveCreate} 
                disabled={isCreating}
              >
                {isCreating ? 'Creating...' : 'Create Menu Group'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── Delete Confirmation Modal ── */}
      {showDeleteModal && deletingGroup && (
        <div className="dc-modal-overlay">
          <div className="dc-modal">
            <div className="dc-modal-icon delete-icon">
              <AlertTriangle size={40} />
            </div>
            <h3 className="dc-modal-title">Delete Menu Group</h3>
            <p className="dc-modal-message">
              Are you sure you want to delete "<strong>{deletingGroup.name}</strong>"? This action cannot be undone.
            </p>
            <div className="dc-modal-actions">
              <button className="dc-modal-cancel" onClick={handleCancelDelete} disabled={isDeleting}>
                Cancel
              </button>
              <button className="dc-modal-confirm delete-confirm" onClick={handleConfirmDelete} disabled={isDeleting}>
                {isDeleting ? 'Deleting...' : 'Yes, Delete'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── Edit Modal ── */}
      {showEditModal && editingGroup && (
        <div className="dc-modal-overlay">
          <div className="dc-modal edit-modal">
            <h3 className="dc-modal-title">Edit Menu Group</h3>
            
            <div className="edit-form">
              <div className="form-group">
                <label className="form-label">Name</label>
                <input
                  type="text"
                  className="form-input"
                  value={editName}
                  onChange={(e) => setEditName(e.target.value)}
                  placeholder="Enter menu group name"
                  disabled={isSaving}
                />
              </div>
              
              <div className="form-group">
                <label className="form-label">Description</label>
                <textarea
                  className="form-textarea"
                  value={editDescription}
                  onChange={(e) => setEditDescription(e.target.value)}
                  placeholder="Enter menu group description"
                  rows={4}
                  disabled={isSaving}
                />
              </div>
            </div>

            <div className="dc-modal-actions">
              <button 
                className="dc-modal-cancel" 
                onClick={handleCancelEdit} 
                disabled={isSaving}
              >
                Cancel
              </button>
              <button 
                className="dc-modal-confirm" 
                onClick={handleSaveEdit} 
                disabled={isSaving}
              >
                {isSaving ? 'Saving...' : 'Save Changes'}
              </button>
            </div>
          </div>
        </div>
      )}

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

          {/* Page Header */}
          <div className="dc-welcome">
            <div className="dc-welcome-content">
              <div>
                <h1 className="dc-welcome-title">Menu Groups</h1>
                <p className="dc-welcome-subtitle">
                  Manage dietary menu groups and meal plans
                  {totalElements > 0 && (
                    <span className="dc-role-badge">{totalElements} total</span>
                  )}
                </p>
              </div>
              <button className="create-btn" onClick={handleCreateClick}>
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <line x1="12" y1="5" x2="12" y2="19" />
                  <line x1="5" y1="12" x2="19" y2="12" />
                </svg>
                Create Menu
              </button>
            </div>
          </div>

          {/* Error */}
          {error && (
            <div className="dc-alert-error">
              <AlertTriangle size={18} />
              <span>{error}</span>
              <button onClick={() => setError(null)} className="dc-alert-close">×</button>
            </div>
          )}

          {/* Content Card */}
          <div className="dc-card">
            {loading && (
              <div className="dc-table-loading">
                {[...Array(4)].map((_, i) => (
                  <div key={i} className="dc-skeleton-row">
                    <div className="dc-skeleton dc-skeleton-cell" />
                    <div className="dc-skeleton dc-skeleton-cell" />
                    <div className="dc-skeleton dc-skeleton-cell" />
                  </div>
                ))}
              </div>
            )}

            {!loading && menuGroups.length === 0 && (
              <div className="dc-empty-state">
                <Layers size={48} />
                <p>No menu groups found</p>
              </div>
            )}

            {!loading && menuGroups.length > 0 && (
              <>
                {/* Desktop Table */}
                <div className="dc-table-wrap">
                  <table className="dc-table">
                    <thead>
                      <tr>
                        <th>Name</th>
                        <th>Description</th>
                        <th>Food Items</th>
                        <th>Assigned Patients</th>
                        <th>Last Updated</th>
                        <th>Created By</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {menuGroups.map((group) => (
                        <tr key={group.id}>
                          <td className="cell-name">{group.name}</td>
                          <td className="cell-description">{group.description}</td>
                          <td className="cell-count">{group.foodItemCount}</td>
                          <td className="cell-count">{group.assignedPatientsCount}</td>
                          <td className="cell-date">{formatDate(group.updatedAt)}</td>
                          <td className="cell-dietician">{group.createdByDieticianName || '—'}</td>
                          <td className="cell-actions">
                            <button 
                              className="action-btn edit-btn"
                              onClick={() => handleEdit(group)}
                            >
                              Edit
                            </button>
                            <button 
                              className="action-btn delete-btn"
                              onClick={() => handleDeleteClick(group.id, group.name)}
                            >
                              Delete
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                {/* Mobile Cards */}
                <div className="menu-group-cards">
                  {menuGroups.map((group) => (
                    <div key={group.id} className="menu-group-card">
                      <div className="menu-card-header">
                        <h3 className="menu-card-name">{group.name}</h3>
                        <div className="menu-card-badges">
                          <span className="menu-card-count">{group.foodItemCount} items</span>
                          <span className="menu-card-patients">{group.assignedPatientsCount} patients</span>
                        </div>
                      </div>
                      <p className="menu-card-description">{group.description}</p>
                      <div className="menu-card-meta">
                        <span className="menu-card-date">{formatDate(group.updatedAt)}</span>
                        {group.createdByDieticianName && (
                          <span className="menu-card-creator">by {group.createdByDieticianName}</span>
                        )}
                      </div>
                      <div className="menu-card-actions">
                        <button 
                          className="action-btn edit-btn"
                          onClick={() => handleEdit(group)}
                        >
                          Edit
                        </button>
                        <button 
                          className="action-btn delete-btn"
                          onClick={() => handleDeleteClick(group.id, group.name)}
                        >
                          Delete
                        </button>
                      </div>
                    </div>
                  ))}
                </div>

                {/* Pagination */}
                {totalPages > 1 && (
                  <div className="dc-pagination">
                    <div className="dc-pagination-info">
                      Showing {currentPage * pageSize + 1} to {Math.min((currentPage + 1) * pageSize, totalElements)} of {totalElements} entries
                    </div>
                    <div className="dc-pagination-controls">
                      <button 
                        className="dc-pagination-btn"
                        onClick={handlePreviousPage}
                        disabled={currentPage === 0}
                      >
                        <ChevronLeft size={18} />
                        Previous
                      </button>
                      <div className="dc-pagination-pages">
                        {[...Array(totalPages)].map((_, i) => (
                          <button
                            key={i}
                            className={`dc-pagination-page ${i === currentPage ? 'active' : ''}`}
                            onClick={() => setCurrentPage(i)}
                          >
                            {i + 1}
                          </button>
                        ))}
                      </div>
                      <button 
                        className="dc-pagination-btn"
                        onClick={handleNextPage}
                        disabled={currentPage === totalPages - 1}
                      >
                        Next
                        <ChevronRight size={18} />
                      </button>
                    </div>
                  </div>
                )}
              </>
            )}
          </div>

        </div>
      </main>
    </div>
  );
};

export default MenuGroups;