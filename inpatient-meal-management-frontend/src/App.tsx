import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import './App.css';

import PatientLogin from './components/patient_ui/PatientLogin';
import PatientOnboarding from './components/patient_ui/PatientOnboarding';
import PatientDashboard from './components/patient_ui/PatientDashboard';
import DieticianLogin from './components/dietician_ui/DieticianLogin';
import DieticianDashboard from './components/dietician_ui/DieticianDashboard';
import DieticianPatients from './components/dietician_ui/DieticianPatients';
import MenuGroups from './components/dietician_ui/MenuGroups';
import FoodItems from './components/dietician_ui/FoodItems';
import SetPassword from './components/dietician_ui/SetPassword';
import KitchenStaffLogin from './components/kitchenstaff_ui/KitchenStaffLogin';
import KitchenDashboard from './components/kitchenstaff_ui/KitchenDashboard';
import KitchenBreakfast from './components/kitchenstaff_ui/KitchenBreakfast';
import KitchenLunch from './components/kitchenstaff_ui/KitchenLunch';
import KitchenDinner from './components/kitchenstaff_ui/KitchenDinner';
import KitchenAlaCarte from './components/kitchenstaff_ui/KitchenAlaCarte';
import AdminLogin from './components/admin_ui/AdminLogin';
import AdminDashboard from './components/admin_ui/AdminDashboard';
import AdminDieticians from './components/admin_ui/AdminDieticians';
import AdminServers from './components/admin_ui/AdminServers';
import AdminForgotPassword from './components/admin_ui/AdminForgotPassword';
import DieticianForgotPassword from './components/dietician_ui/DieticianForgotPassword';
import KitchenForgotPassword from './components/kitchenstaff_ui/KitchenForgotPassword';
import ResetPassword from './components/shared/ResetPassword';
import { KitchenQueueProvider } from './components/kitchenstaff_ui/KitchenQueueContext';
import ProtectedRoute from './components/ProtectedRoute';

const KitchenRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => (
  <ProtectedRoute type="kitchen">
    <KitchenQueueProvider>
      {children}
    </KitchenQueueProvider>
  </ProtectedRoute>
);

// ── Onboarding gate: shows onboarding on first visit, login on return visits ──
const PatientLoginRoute: React.FC = () => {
  const hasCompleted = localStorage.getItem('hasCompletedOnboarding');
  return hasCompleted ? <PatientLogin /> : <PatientOnboarding />;
};

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Default */}
        <Route path="/" element={<Navigate to="/patient/login" />} />

        {/* ── Patient ── */}
        <Route path="/patient/login" element={<PatientLoginRoute />} />
        <Route path="/patient/dashboard" element={
          <ProtectedRoute type="patient"><PatientDashboard /></ProtectedRoute>
        } />

        {/* ── Dietician ── */}
        <Route path="/dietician/login"           element={<DieticianLogin />} />
        <Route path="/dietician/forgot-password" element={<DieticianForgotPassword />} />
        <Route path="/set-password"              element={<SetPassword />} />
        <Route path="/dietician/dashboard" element={
          <ProtectedRoute type="dietician"><DieticianDashboard /></ProtectedRoute>
        } />
        <Route path="/dietician/patients" element={
          <ProtectedRoute type="dietician"><DieticianPatients /></ProtectedRoute>
        } />
        <Route path="/dietician/menu-groups" element={
          <ProtectedRoute type="dietician"><MenuGroups /></ProtectedRoute>
        } />
        <Route path="/dietician/food-items" element={
          <ProtectedRoute type="dietician"><FoodItems /></ProtectedRoute>
        } />

        {/* ── Kitchen ── */}
        <Route path="/kitchen/login"           element={<KitchenStaffLogin />} />
        <Route path="/kitchen/forgot-password" element={<KitchenForgotPassword />} />
        <Route path="/kitchen/dashboard" element={<KitchenRoute><KitchenDashboard /></KitchenRoute>} />
        <Route path="/kitchen/breakfast"  element={<KitchenRoute><KitchenBreakfast  /></KitchenRoute>} />
        <Route path="/kitchen/lunch"      element={<KitchenRoute><KitchenLunch      /></KitchenRoute>} />
        <Route path="/kitchen/dinner"     element={<KitchenRoute><KitchenDinner     /></KitchenRoute>} />
        <Route path="/kitchen/alacarte"   element={<KitchenRoute><KitchenAlaCarte   /></KitchenRoute>} />

        {/* ── Admin ── */}
        <Route path="/admin/login"           element={<AdminLogin />} />
        <Route path="/admin/forgot-password" element={<AdminForgotPassword />} />
        <Route path="/admin/dashboard" element={
          <ProtectedRoute type="admin"><AdminDashboard /></ProtectedRoute>
        } />
        <Route path="/admin/dieticians" element={
          <ProtectedRoute type="admin"><AdminDieticians /></ProtectedRoute>
        } />
        <Route path="/admin/servers" element={
          <ProtectedRoute type="admin"><AdminServers /></ProtectedRoute>
        } />

        {/* ── Universal Password Reset ── */}
        <Route path="/reset-password" element={<ResetPassword />} />

      </Routes>
    </BrowserRouter>
  );
}

export default App;