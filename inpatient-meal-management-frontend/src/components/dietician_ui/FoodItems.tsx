import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  LayoutDashboard, Users, Layers, UtensilsCrossed,
  LogOut, Menu, X, AlertTriangle, ChefHat, ChevronLeft, ChevronRight,
  CheckCircle, XCircle, Search, Filter
} from 'lucide-react';
import { config } from '../../config/config';
import '../../styles/dietician/fooditems.css';
import evercarelogo     from '../../assets/icons/ec_logo.png';
import evercareLogoFull from '../../assets/icons/EvercareLogo.png';

interface FoodItem {
  id: string;
  name: string;
  description: string;
  mealType?: 'BREAKFAST' | 'LUNCH' | 'DINNER';
  menuGroupId: string;
  menuGroupName: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

interface MenuGroup {
  id: string;
  name: string;
}

interface PagedResponse {
  content: FoodItem[];
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

const FoodItems: React.FC = () => {
  const navigate = useNavigate();
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [user, setUser] = useState<DieticianUser | null>(null);
  const [foodItems, setFoodItems] = useState<FoodItem[]>([]);
  const [menuGroups, setMenuGroups] = useState<MenuGroup[]>([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Filters
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedMealType, setSelectedMealType] = useState<string>('');
  const [selectedMenuGroup, setSelectedMenuGroup] = useState<string>('');
  const [showFilters, setShowFilters] = useState(false);

  // Create modal state
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [createName, setCreateName] = useState('');
  const [createDescription, setCreateDescription] = useState('');
  const [createMealType, setCreateMealType] = useState<string>('');
  const [createMenuGroup, setCreateMenuGroup] = useState<string>('');
  const [isCreating, setIsCreating] = useState(false);

  // Edit modal state
  const [showEditModal, setShowEditModal] = useState(false);
  const [editingItem, setEditingItem] = useState<FoodItem | null>(null);
  const [editName, setEditName] = useState('');
  const [editDescription, setEditDescription] = useState('');
  const [editMealType, setEditMealType] = useState<string>('');
  const [editMenuGroup, setEditMenuGroup] = useState<string>('');
  const [isSaving, setIsSaving] = useState(false);

  // Modals
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deletingItem, setDeletingItem] = useState<{ id: string; name: string } | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  // Toast notifications
  const [toasts, setToasts] = useState<Toast[]>([]);

  const pageSize = 10;

  useEffect(() => {
    const raw = localStorage.getItem('dietician_user');
    if (raw) setUser(JSON.parse(raw));
  }, []);

  useEffect(() => {
    fetchFoodItems(currentPage);
  }, [currentPage, searchQuery, selectedMealType, selectedMenuGroup]);

  useEffect(() => {
    fetchMenuGroups();
  }, []);

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

  const fetchMenuGroups = async () => {
    try {
      const token = getToken();
      if (!token) return;
      
      const response = await fetch(config.dieticianMenuGroupFiltersUrl, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (response.ok) {
        const result = await response.json();
        if (result.success && result.data) {
          setMenuGroups(result.data.map((mg: any) => ({ id: mg.id, name: mg.name })));
        }
      }
    } catch (err) {
      console.error('Error fetching menu groups:', err);
    }
  };

  const fetchFoodItems = async (page: number) => {
    try {
      setLoading(true);
      const token = getToken();
      if (!token) return;

      let url = '';
      const params = new URLSearchParams();
      params.append('page', page.toString());
      params.append('size', pageSize.toString());

      if (searchQuery.trim()) {
        url = `${config.dieticianFoodItemSearchUrl}?query=${encodeURIComponent(searchQuery)}&${params.toString()}`;
      } else {
        if (selectedMealType) params.append('mealType', selectedMealType);
        if (selectedMenuGroup) params.append('menuGroupId', selectedMenuGroup);
        url = `${config.dieticianFoodItemsUrl}?${params.toString()}`;
      }
      
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
        throw new Error('Failed to fetch food items');
      }

      const result = await response.json();
      const pagedData: PagedResponse = result.data;
      
      setFoodItems(pagedData.content || []);
      setTotalPages(pagedData.totalPages);
      setTotalElements(pagedData.totalElements);
      setCurrentPage(pagedData.page);
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
      console.error('Error fetching food items:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (value: string) => {
    setSearchQuery(value);
    setCurrentPage(0);
  };

  const handleMealTypeFilter = (value: string) => {
    setSelectedMealType(value);
    setCurrentPage(0);
  };

  const handleMenuGroupFilter = (value: string) => {
    setSelectedMenuGroup(value);
    setCurrentPage(0);
  };

  const handleClearFilters = () => {
    setSearchQuery('');
    setSelectedMealType('');
    setSelectedMenuGroup('');
    setCurrentPage(0);
  };

  const handleCreateClick = () => {
    setCreateName('');
    setCreateDescription('');
    setCreateMealType('');
    setCreateMenuGroup('');
    setShowCreateModal(true);
  };

  const handleCancelCreate = () => {
    setShowCreateModal(false);
    setCreateName('');
    setCreateDescription('');
    setCreateMealType('');
    setCreateMenuGroup('');
  };

  const handleSaveCreate = async () => {
    if (!createName.trim() || !createMenuGroup) {
      showToast('error', 'Name and menu group are required');
      return;
    }

    try {
      setIsCreating(true);
      const token = getToken();
      if (!token) return;

      const body: any = {
        name: createName.trim(),
        description: createDescription.trim(),
        menuGroupId: createMenuGroup,
      };

      if (createMealType) {
        body.mealType = createMealType;
      }
      
      const response = await fetch(config.dieticianFoodItemsUrl, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(body),
      });

      if (response.status === 401) {
        handle401();
        return;
      }

      if (!response.ok) {
        throw new Error('Failed to create food item');
      }

      const result = await response.json();
      
      if (result.success) {
        await fetchFoodItems(0);
        setCurrentPage(0);
        handleCancelCreate();
        showToast('success', 'Food item created successfully');
      } else {
        throw new Error(result.message || 'Failed to create food item');
      }
    } catch (err) {
      showToast('error', err instanceof Error ? err.message : 'Failed to create food item');
      console.error('Error creating food item:', err);
    } finally {
      setIsCreating(false);
    }
  };

  const handleEdit = (item: FoodItem) => {
    setEditingItem(item);
    setEditName(item.name);
    setEditDescription(item.description);
    setEditMealType(item.mealType || '');
    setEditMenuGroup(item.menuGroupId);
    setShowEditModal(true);
  };

  const handleCancelEdit = () => {
    setShowEditModal(false);
    setEditingItem(null);
    setEditName('');
    setEditDescription('');
    setEditMealType('');
    setEditMenuGroup('');
  };

  const handleSaveEdit = async () => {
    if (!editingItem) return;
    
    if (!editName.trim() || !editMenuGroup) {
      showToast('error', 'Name and menu group are required');
      return;
    }

    try {
      setIsSaving(true);
      const token = getToken();
      if (!token) return;

      const body: any = {
        name: editName.trim(),
        description: editDescription.trim(),
        menuGroupId: editMenuGroup,
      };

      if (editMealType) {
        body.mealType = editMealType;
      }
      
      const response = await fetch(config.dieticianFoodItemUrl(editingItem.id), {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(body),
      });

      if (response.status === 401) {
        handle401();
        return;
      }

      if (!response.ok) {
        throw new Error('Failed to update food item');
      }

      const result = await response.json();
      
      if (result.success) {
        await fetchFoodItems(currentPage);
        handleCancelEdit();
        showToast('success', 'Food item updated successfully');
      } else {
        throw new Error(result.message || 'Failed to update food item');
      }
    } catch (err) {
      showToast('error', err instanceof Error ? err.message : 'Failed to update food item');
      console.error('Error updating food item:', err);
    } finally {
      setIsSaving(false);
    }
  };

  const handleDeleteClick = (foodItemId: string, foodItemName: string) => {
    setDeletingItem({ id: foodItemId, name: foodItemName });
    setShowDeleteModal(true);
  };

  const handleCancelDelete = () => {
    setShowDeleteModal(false);
    setDeletingItem(null);
  };

  const handleConfirmDelete = async () => {
    if (!deletingItem) return;

    try {
      setIsDeleting(true);
      const token = getToken();
      if (!token) return;
      
      const response = await fetch(config.dieticianFoodItemUrl(deletingItem.id), {
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
        throw new Error('Failed to delete food item');
      }

      await fetchFoodItems(currentPage);
      handleCancelDelete();
      showToast('success', 'Food item deleted successfully');
    } catch (err) {
      showToast('error', err instanceof Error ? err.message : 'Failed to delete food item');
      console.error('Error deleting food item:', err);
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

  const getMealTypeBadgeClass = (mealType?: string) => {
    if (!mealType) return 'meal-badge-none';
    switch (mealType) {
      case 'BREAKFAST': return 'meal-badge-breakfast';
      case 'LUNCH': return 'meal-badge-lunch';
      case 'DINNER': return 'meal-badge-dinner';
      default: return 'meal-badge-none';
    }
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
          <div className="dc-modal food-modal">
            <h3 className="dc-modal-title">Create Food Item</h3>
            
            <div className="edit-form">
              <div className="form-group">
                <label className="form-label">Name *</label>
                <input
                  type="text"
                  className="form-input"
                  value={createName}
                  onChange={(e) => setCreateName(e.target.value)}
                  placeholder="Enter food item name"
                  disabled={isCreating}
                />
              </div>
              
              <div className="form-group">
                <label className="form-label">Description</label>
                <textarea
                  className="form-textarea"
                  value={createDescription}
                  onChange={(e) => setCreateDescription(e.target.value)}
                  placeholder="Enter description (optional)"
                  rows={3}
                  disabled={isCreating}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Menu Group *</label>
                <select
                  className="form-select"
                  value={createMenuGroup}
                  onChange={(e) => setCreateMenuGroup(e.target.value)}
                  disabled={isCreating}
                >
                  <option value="">Select menu group</option>
                  {menuGroups.map(mg => (
                    <option key={mg.id} value={mg.id}>{mg.name}</option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Meal Type</label>
                <select
                  className="form-select"
                  value={createMealType}
                  onChange={(e) => setCreateMealType(e.target.value)}
                  disabled={isCreating}
                >
                  <option value="">None (À la carte)</option>
                  <option value="BREAKFAST">Breakfast</option>
                  <option value="LUNCH">Lunch</option>
                  <option value="DINNER">Dinner</option>
                </select>
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
                {isCreating ? 'Creating...' : 'Create Food Item'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── Edit Modal ── */}
      {showEditModal && editingItem && (
        <div className="dc-modal-overlay">
          <div className="dc-modal food-modal">
            <h3 className="dc-modal-title">Edit Food Item</h3>
            
            <div className="edit-form">
              <div className="form-group">
                <label className="form-label">Name *</label>
                <input
                  type="text"
                  className="form-input"
                  value={editName}
                  onChange={(e) => setEditName(e.target.value)}
                  placeholder="Enter food item name"
                  disabled={isSaving}
                />
              </div>
              
              <div className="form-group">
                <label className="form-label">Description</label>
                <textarea
                  className="form-textarea"
                  value={editDescription}
                  onChange={(e) => setEditDescription(e.target.value)}
                  placeholder="Enter description (optional)"
                  rows={3}
                  disabled={isSaving}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Menu Group *</label>
                <select
                  className="form-select"
                  value={editMenuGroup}
                  onChange={(e) => setEditMenuGroup(e.target.value)}
                  disabled={isSaving}
                >
                  <option value="">Select menu group</option>
                  {menuGroups.map(mg => (
                    <option key={mg.id} value={mg.id}>{mg.name}</option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Meal Type</label>
                <select
                  className="form-select"
                  value={editMealType}
                  onChange={(e) => setEditMealType(e.target.value)}
                  disabled={isSaving}
                >
                  <option value="">None (À la carte)</option>
                  <option value="BREAKFAST">Breakfast</option>
                  <option value="LUNCH">Lunch</option>
                  <option value="DINNER">Dinner</option>
                </select>
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

      {/* ── Delete Confirmation Modal ── */}
      {showDeleteModal && deletingItem && (
        <div className="dc-modal-overlay">
          <div className="dc-modal">
            <div className="dc-modal-icon delete-icon">
              <AlertTriangle size={40} />
            </div>
            <h3 className="dc-modal-title">Delete Food Item</h3>
            <p className="dc-modal-message">
              Are you sure you want to delete "<strong>{deletingItem.name}</strong>"? This action cannot be undone.
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
                <h1 className="dc-welcome-title">Food Items</h1>
                <p className="dc-welcome-subtitle">
                  Manage food items and meal options
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
                Create Food Item
              </button>
            </div>
          </div>

          {/* Filters */}
          <div className="filters-section">
            <div className="search-bar">
              <Search size={20} />
              <input
                type="text"
                placeholder="Search food items..."
                value={searchQuery}
                onChange={(e) => handleSearch(e.target.value)}
                className="search-input"
              />
            </div>
            
            <button 
              className="filter-toggle-btn"
              onClick={() => setShowFilters(!showFilters)}
            >
              <Filter size={18} />
              Filters
              {(selectedMealType || selectedMenuGroup) && <span className="filter-badge">•</span>}
            </button>

            {(searchQuery || selectedMealType || selectedMenuGroup) && (
              <button className="clear-filters-btn" onClick={handleClearFilters}>
                Clear All
              </button>
            )}
          </div>

          {showFilters && (
            <div className="filters-panel">
              <div className="filter-group">
                <label className="filter-label">Meal Type</label>
                <select 
                  className="filter-select"
                  value={selectedMealType}
                  onChange={(e) => handleMealTypeFilter(e.target.value)}
                >
                  <option value="">All Meal Types</option>
                  <option value="BREAKFAST">Breakfast</option>
                  <option value="LUNCH">Lunch</option>
                  <option value="DINNER">Dinner</option>
                </select>
              </div>

              <div className="filter-group">
                <label className="filter-label">Menu Group</label>
                <select 
                  className="filter-select"
                  value={selectedMenuGroup}
                  onChange={(e) => handleMenuGroupFilter(e.target.value)}
                >
                  <option value="">All Menu Groups</option>
                  {menuGroups.map(mg => (
                    <option key={mg.id} value={mg.id}>{mg.name}</option>
                  ))}
                </select>
              </div>
            </div>
          )}

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

            {!loading && foodItems.length === 0 && (
              <div className="dc-empty-state">
                <UtensilsCrossed size={48} />
                <p>No food items found</p>
              </div>
            )}

            {!loading && foodItems.length > 0 && (
              <>
                {/* Desktop Table */}
                <div className="dc-table-wrap">
                  <table className="dc-table">
                    <thead>
                      <tr>
                        <th>Name</th>
                        <th>Description</th>
                        <th>Meal Type</th>
                        <th>Menu Group</th>
                        <th>Created At</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {foodItems.map((item) => (
                        <tr key={item.id}>
                          <td className="cell-name">{item.name}</td>
                          <td className="cell-description">{item.description}</td>
                          <td>
                            {item.mealType ? (
                              <span className={`meal-badge ${getMealTypeBadgeClass(item.mealType)}`}>
                                {item.mealType}
                              </span>
                            ) : (
                              <span className="cell-none">—</span>
                            )}
                          </td>
                          <td className="cell-menu-group">{item.menuGroupName}</td>
                          <td className="cell-date">{formatDate(item.createdAt)}</td>
                          <td className="cell-actions">
                            <button 
                              className="action-btn edit-btn"
                              onClick={() => handleEdit(item)}
                            >
                              Edit
                            </button>
                            <button 
                              className="action-btn delete-btn"
                              onClick={() => handleDeleteClick(item.id, item.name)}
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
                <div className="food-item-cards">
                  {foodItems.map((item) => (
                    <div key={item.id} className="food-item-card">
                      <div className="food-card-header">
                        <h3 className="food-card-name">{item.name}</h3>
                        {item.mealType && (
                          <span className={`meal-badge ${getMealTypeBadgeClass(item.mealType)}`}>
                            {item.mealType}
                          </span>
                        )}
                      </div>
                      <p className="food-card-description">{item.description}</p>
                      <div className="food-card-meta">
                        <span className="food-card-group">{item.menuGroupName}</span>
                        <span className="food-card-date">{formatDate(item.createdAt)}</span>
                      </div>
                      <div className="food-card-actions">
                        <button 
                          className="action-btn edit-btn"
                          onClick={() => handleEdit(item)}
                        >
                          Edit
                        </button>
                        <button 
                          className="action-btn delete-btn"
                          onClick={() => handleDeleteClick(item.id, item.name)}
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

export default FoodItems;