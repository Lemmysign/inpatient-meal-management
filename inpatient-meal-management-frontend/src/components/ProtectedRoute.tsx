import React from 'react';
import { Navigate } from 'react-router-dom';

interface ProtectedRouteProps {
  children: React.ReactNode;
  type: 'patient' | 'dietician' | 'kitchen' | 'admin';
}

const decodeAndValidateJWT = (token: string): boolean => {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    const now = Math.floor(Date.now() / 1000);
    return !(payload.exp && now > payload.exp);
  } catch {
    return false;
  }
};

const clearAndRedirect = (keys: string[], path: string) => {
  keys.forEach(key => localStorage.removeItem(key));
  return <Navigate to={path} replace />;
};

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, type }) => {

  if (type === 'patient') {
    const token = localStorage.getItem('sessionToken');
    if (!token) return <Navigate to="/patient/login" replace />;
    if (!decodeAndValidateJWT(token)) {
      return clearAndRedirect(['sessionToken', 'patientId'], '/patient/login');
    }
    return <>{children}</>;
  }

  if (type === 'dietician') {
    const token = localStorage.getItem('dietician_token');
    if (!token) return <Navigate to="/dietician/login" replace />;
    if (!decodeAndValidateJWT(token)) {
      return clearAndRedirect(['dietician_token'], '/dietician/login');
    }
    return <>{children}</>;
  }

  if (type === 'kitchen') {
    const token = localStorage.getItem('kitchen_token');
    if (!token) return <Navigate to="/kitchen/login" replace />;
    if (!decodeAndValidateJWT(token)) {
      return clearAndRedirect(['kitchen_token'], '/kitchen/login');
    }
    return <>{children}</>;
  }

  if (type === 'admin') {
    const token = localStorage.getItem('admin_token');
    if (!token) return <Navigate to="/admin/login" replace />;
    if (!decodeAndValidateJWT(token)) {
      return clearAndRedirect(['admin_token'], '/admin/login');
    }
    return <>{children}</>;
  }

  return <Navigate to="/" replace />;
};

export default ProtectedRoute;