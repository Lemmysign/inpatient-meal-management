import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Eye, EyeOff, Mail, Lock, AlertCircle, Loader2 } from 'lucide-react';
import { config } from '../../config/config';
import '../../styles/admin/admin.css';
import evercarelogo from '../../assets/icons/ec_logo.png';

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

const AdminLogin: React.FC = () => {
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
      const response = await fetch(config.adminLoginUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: email.trim(), password: password.trim() }),
      });
      const data: LoginResponse = await response.json();
   if (response.ok && data.success) {
    // Store token only — no PII in localStorage
    localStorage.setItem('admin_token', data.data.token);
    navigate('/admin/dashboard');
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
    <div className="al-login-page">

      {/* ── Left Hero ── */}
      <div className="al-login-hero">
        <div className="al-hero-shape-primary" />
        <div className="al-hero-shape-secondary" />
        <div className="al-hero-shape-tertiary" />

        <div className="al-hero-wrapper">
          <div className="al-hero-header">
            <span className="al-hero-eyebrow">System Administration</span>
            <h1>Full Control. Total Visibility. Zero Compromise.</h1>
          </div>

          <div className="al-hero-visual">
            <div className="al-hero-image-container">
              <div className="al-hero-illustration">
                <svg viewBox="0 0 400 260" fill="none" xmlns="http://www.w3.org/2000/svg">
                  {[...Array(6)].map((_, i) => (
                    <line key={`h${i}`} x1="40" y1={50 + i * 34} x2="360" y2={50 + i * 34}
                      stroke="white" strokeOpacity="0.06" strokeWidth="1"/>
                  ))}
                  {[...Array(9)].map((_, i) => (
                    <line key={`v${i}`} x1={40 + i * 40} y1="50" x2={40 + i * 40} y2="220"
                      stroke="white" strokeOpacity="0.06" strokeWidth="1"/>
                  ))}
                  <rect x="80" y="55" width="240" height="148" rx="10"
                    fill="white" fillOpacity="0.08" stroke="white" strokeOpacity="0.2" strokeWidth="1.5"/>
                  <rect x="88" y="63" width="224" height="132" rx="6" fill="white" fillOpacity="0.05"/>
                  <rect x="104" y="155" width="20" height="28" rx="3" fill="#34d399" fillOpacity="0.8"/>
                  <rect x="132" y="138" width="20" height="45" rx="3" fill="#34d399" fillOpacity="0.8"/>
                  <rect x="160" y="120" width="20" height="63" rx="3" fill="#34d399" fillOpacity="0.8"/>
                  <rect x="188" y="132" width="20" height="51" rx="3" fill="#60a5fa" fillOpacity="0.8"/>
                  <rect x="216" y="108" width="20" height="75" rx="3" fill="#60a5fa" fillOpacity="0.8"/>
                  <rect x="244" y="125" width="20" height="58" rx="3" fill="#f472b6" fillOpacity="0.8"/>
                  <rect x="272" y="115" width="20" height="68" rx="3" fill="#f472b6" fillOpacity="0.8"/>
                  <rect x="88" y="63" width="224" height="22" rx="6" fill="white" fillOpacity="0.08"/>
                  <circle cx="103" cy="74" r="4" fill="#f87171" fillOpacity="0.8"/>
                  <circle cx="117" cy="74" r="4" fill="#fbbf24" fillOpacity="0.8"/>
                  <circle cx="131" cy="74" r="4" fill="#34d399" fillOpacity="0.8"/>
                  <text x="200" y="79" textAnchor="middle" fontSize="8"
                    fill="white" fillOpacity="0.6" fontWeight="600">ADMIN DASHBOARD</text>
                  <rect x="188" y="203" width="24" height="12" rx="2" fill="white" fillOpacity="0.15"/>
                  <rect x="168" y="215" width="64" height="6" rx="3" fill="white" fillOpacity="0.12"/>
                  <g transform="translate(298, 58)">
                    <path d="M22 4 L34 9 L34 18 C34 25 28 30 22 32 C16 30 10 25 10 18 L10 9 Z"
                      fill="none" stroke="white" strokeOpacity="0.5" strokeWidth="1.5"/>
                    <path d="M18 17 L21 20 L27 14"
                      stroke="#34d399" strokeOpacity="0.9" strokeWidth="2"
                      strokeLinecap="round" strokeLinejoin="round"/>
                  </g>
                  <rect x="40" y="90" width="30" height="38" rx="6"
                    fill="white" fillOpacity="0.08" stroke="white" strokeOpacity="0.12" strokeWidth="1"/>
                  <text x="55" y="106" textAnchor="middle" fontSize="7" fill="white" fillOpacity="0.5">USERS</text>
                  <text x="55" y="120" textAnchor="middle" fontSize="11" fill="white" fillOpacity="0.9" fontWeight="700">248</text>
                  <rect x="330" y="90" width="30" height="38" rx="6"
                    fill="white" fillOpacity="0.08" stroke="white" strokeOpacity="0.12" strokeWidth="1"/>
                  <text x="345" y="106" textAnchor="middle" fontSize="7" fill="white" fillOpacity="0.5">ACTIVE</text>
                  <text x="345" y="120" textAnchor="middle" fontSize="11" fill="white" fillOpacity="0.9" fontWeight="700">94%</text>
                </svg>
              </div>
            </div>
            <p className="al-hero-description">
              Manage users and monitor activities from one place.
            </p>
          </div>
        </div>
      </div>

      {/* ── Right Panel ── */}
      <div className="al-login-panel">
        <div className="al-panel-content">

          <div className="al-brand-section">
            <img src={evercarelogo} alt="Evercare Logo" className="al-brand-logo" />
          </div>

          <div className="al-form-card">
            <div className="al-admin-badge">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
              </svg>
              Admin Portal
            </div>

            <h2 className="al-form-heading">Administrator Sign In</h2>
            <p className="al-form-subheading"></p>

            <form onSubmit={handleSubmit}>
              {error && (
                <div className="al-alert-error">
                  <AlertCircle size={18} />
                  <span>{error}</span>
                </div>
              )}

              {/* Email */}
              <div className="al-field-group">
                <label className="al-field-label">Email Address</label>
                <div className="al-field-container">
                  <Mail className="al-field-icon" size={18} />
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="Enter your email"
                    className="al-field-input"
                    disabled={isLoading}
                    autoComplete="email"
                  />
                </div>
              </div>

              {/* Password */}
              <div className="al-field-group">
                <label className="al-field-label">Password</label>
                <div className="al-field-container">
                  <Lock className="al-field-icon" size={18} />
                  <input
                    type={showPassword ? 'text' : 'password'}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="Enter your password"
                    className="al-field-input al-field-password"
                    disabled={isLoading}
                    autoComplete="current-password"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="al-password-toggle"
                    disabled={isLoading}
                  >
                    {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                  </button>
                </div>

                {/* Forgot password — below the input */}
                <Link to="/admin/forgot-password" className="al-forgot-link">
                  Forgot password?
                </Link>
              </div>

              <button type="submit" className="al-submit-btn" disabled={isLoading}>
                {isLoading ? (
                  <>
                    <Loader2 className="al-btn-spinner" size={18} />
                    <span>Authenticating...</span>
                  </>
                ) : (
                  <>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                      stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                      <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                    </svg>
                    Login
                  </>
                )}
              </button>
            </form>
          </div>

          <div className="al-login-footer">
            <p>Copyright © 2026 – Evercare Hospital Lekki</p>
          </div>

        </div>
      </div>
    </div>
  );
};

export default AdminLogin;