import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Eye, EyeOff, Mail, Lock, AlertCircle, Loader2 } from 'lucide-react';
import { config } from '../../config/config';
import '../../styles/kitchenstaff/kitchenstafflogin.css';
import evercarelogo from '../../assets/icons/ec_logo.png';
import { usePushNotifications } from '../../hooks/usePushNotifications';

interface LoginResponse {
  success: boolean;
  message: string;
  data: {
    token: string;
    tokenType: string;
    expiresIn: number;
    userId: string;
    name: string;
    email: string;
    role: string;
  };
  timestamp: string;
}

const KitchenStaffLogin: React.FC = () => {
  usePushNotifications('kitchen_token');
  const navigate = useNavigate();
  const [showPassword, setShowPassword] = useState(false);
  const [email, setEmail]               = useState('');
  const [password, setPassword]         = useState('');
  const [isLoading, setIsLoading]       = useState(false);
  const [error, setError]               = useState<string>('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    if (!email.trim()) { setError('Email is required'); return; }
    if (!password.trim()) { setError('Password is required'); return; }
    if (!/\S+@\S+\.\S+/.test(email)) { setError('Please enter a valid email address'); return; }

    setIsLoading(true);
    try {
      const response = await fetch(config.kitchenLoginUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: email.trim(), password: password.trim() }),
      });
      const data: LoginResponse = await response.json();
      if (response.ok && data.success) {
        // Store token only — no PII in localStorage
        localStorage.setItem('kitchen_token', data.data.token);
        navigate('/kitchen/dashboard');
      } else {
        setError(data.message || 'Invalid email or password');
      }
    } catch (err) {
      console.error('Login error:', err);
      setError('An error occurred. Please check your connection and try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="ks-login-page">

      {/* ── Left Hero ── */}
      <div className="ks-login-hero">
        <div className="ks-hero-shape-primary" />
        <div className="ks-hero-shape-secondary" />

        <div className="ks-hero-wrapper">
          <div className="ks-hero-header">
            <h1>Streamline Kitchen Operations for Timely Meal Delivery.</h1>
          </div>

          <div className="ks-hero-visual">
            <div className="ks-hero-image-container">
              <div className="ks-hero-illustration">
                <svg viewBox="0 0 400 300" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <circle cx="200" cy="150" r="120" fill="white" fillOpacity="0.05"/>
                  <circle cx="200" cy="150" r="90"  fill="white" fillOpacity="0.08"/>
                  <ellipse cx="200" cy="180" rx="70" ry="12" fill="white" fillOpacity="0.3"/>
                  <rect x="130" y="130" width="140" height="50" fill="white" fillOpacity="0.9"/>
                  <rect x="130" y="130" width="140" height="10" fill="white" fillOpacity="0.95"/>
                  <path d="M130 145 Q115 145 115 155 Q115 165 130 165" stroke="white" strokeWidth="8" strokeLinecap="round" fill="none" opacity="0.8"/>
                  <path d="M270 145 Q285 145 285 155 Q285 165 270 165" stroke="white" strokeWidth="8" strokeLinecap="round" fill="none" opacity="0.8"/>
                  <ellipse cx="200" cy="115" rx="75" ry="15" fill="white" fillOpacity="0.95"/>
                  <rect x="190" y="95" width="20" height="20" rx="3" fill="white" fillOpacity="0.9"/>
                  <circle cx="200" cy="105" r="8" fill="white" fillOpacity="0.85"/>
                  <path d="M170 90 Q165 70 170 50 Q175 30 170 10" stroke="white" strokeWidth="4" strokeLinecap="round" fill="none" opacity="0.6"/>
                  <path d="M200 85 Q195 65 200 45 Q205 25 200 5" stroke="white" strokeWidth="5" strokeLinecap="round" fill="none" opacity="0.7"/>
                  <path d="M230 90 Q235 70 230 50 Q225 30 230 10" stroke="white" strokeWidth="4" strokeLinecap="round" fill="none" opacity="0.6"/>
                </svg>
              </div>
            </div>
            <p className="ks-hero-description">
              Manage orders, track preparation, and ensure quality meal service.
            </p>
          </div>
        </div>
      </div>

      {/* ── Right Panel ── */}
      <div className="ks-login-panel">
        <div className="ks-panel-content">

          <div className="ks-brand-section">
            <img src={evercarelogo} alt="Evercare Logo" className="ks-brand-logo" />
          </div>

          <div className="ks-form-card">
            <h2 className="ks-form-heading">Kitchen Staff Sign In</h2>
            <p className="ks-form-subheading">Enter your credentials to access the kitchen portal</p>

            <form onSubmit={handleSubmit}>
              {error && (
                <div className="ks-alert-error">
                  <AlertCircle size={18} />
                  <span>{error}</span>
                </div>
              )}

              {/* Email */}
              <div className="ks-field-group">
                <label className="ks-field-label">Email Address</label>
                <div className="ks-field-container">
                  <Mail className="ks-field-icon" size={18} />
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="Enter your email"
                    className="ks-field-input"
                    disabled={isLoading}
                    autoComplete="email"
                  />
                </div>
              </div>

              {/* Password */}
              <div className="ks-field-group">
                <label className="ks-field-label">Password</label>
                <div className="ks-field-container">
                  <Lock className="ks-field-icon" size={18} />
                  <input
                    type={showPassword ? 'text' : 'password'}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="Enter your password"
                    className="ks-field-input ks-field-password"
                    disabled={isLoading}
                    autoComplete="current-password"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="ks-password-toggle"
                    disabled={isLoading}
                  >
                    {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                  </button>
                </div>

                <Link to="/kitchen/forgot-password" className="ks-forgot-link">
                  Forgot password?
                </Link>
              </div>

              <button type="submit" className="ks-submit-btn" disabled={isLoading}>
                {isLoading ? (
                  <>
                    <Loader2 className="ks-btn-spinner" size={18} />
                    <span>Signing In...</span>
                  </>
                ) : (
                  'Sign In'
                )}
              </button>
            </form>
          </div>

          <div className="ks-login-footer">
            <p>Copyright © 2026 – Evercare Hospital Lekki</p>
          </div>

        </div>
      </div>
    </div>
  );
};

export default KitchenStaffLogin;