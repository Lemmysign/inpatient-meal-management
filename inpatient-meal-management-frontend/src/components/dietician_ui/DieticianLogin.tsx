import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Eye, EyeOff, Mail, Lock, AlertCircle, Loader2 } from 'lucide-react';
import { config } from '../../config/config';
import '../../styles/dietician/dietician.css';
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

const DieticianLogin: React.FC = () => {
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
      const response = await fetch(config.dieticianLoginUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: email.trim(), password: password.trim() }),
      });
      const data: LoginResponse = await response.json();
     if (response.ok && data.success) {
    // Store token only — no PII in localStorage
    localStorage.setItem('dietician_token', data.data.token);
    navigate('/dietician/dashboard');
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
    <div className="dc-login-page">

      {/* ── Left Hero ── */}
      <div className="dc-login-hero">
        <div className="dc-hero-shape-primary" />
        <div className="dc-hero-shape-secondary" />

        <div className="dc-hero-wrapper">
          <div className="dc-hero-header">
            <h1>Smart Nutrition Management for Better Patient Outcomes.</h1>
          </div>

          <div className="dc-hero-visual">
            <div className="dc-hero-image-container">
              <div className="dc-hero-illustration">
                <svg viewBox="0 0 400 260" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <ellipse cx="200" cy="210" rx="140" ry="22" fill="black" fillOpacity="0.1"/>
                  <circle cx="200" cy="155" r="115" fill="white" fillOpacity="0.12" stroke="white" strokeOpacity="0.25" strokeWidth="2"/>
                  <circle cx="200" cy="155" r="88" fill="white" fillOpacity="0.1"/>
                  <path d="M200 155 L200 70 A85 85 0 0 1 274 112 Z" fill="#4ade80" fillOpacity="0.75"/>
                  <path d="M200 155 L274 112 A85 85 0 0 1 274 198 Z" fill="#fb923c" fillOpacity="0.75"/>
                  <path d="M200 155 L274 198 A85 85 0 0 1 126 198 Z" fill="#60a5fa" fillOpacity="0.75"/>
                  <path d="M200 155 L126 198 A85 85 0 0 1 126 112 Z" fill="#f472b6" fillOpacity="0.75"/>
                  <path d="M200 155 L126 112 A85 85 0 0 1 200 70 Z"  fill="#facc15" fillOpacity="0.75"/>
                  <circle cx="200" cy="155" r="30" fill="white" fillOpacity="0.92"/>
                  <text x="200" y="152" textAnchor="middle" fontSize="9" fill="#1a6b4a" fontWeight="700">DIET</text>
                  <text x="200" y="163" textAnchor="middle" fontSize="9" fill="#1a6b4a" fontWeight="700">PLAN</text>
                  <line x1="88" y1="80" x2="88" y2="230" stroke="white" strokeOpacity="0.5" strokeWidth="3" strokeLinecap="round"/>
                  <path d="M80 80 Q80 125 88 140 Q96 125 96 80" stroke="white" strokeOpacity="0.5" strokeWidth="3" fill="none" strokeLinecap="round"/>
                  <line x1="312" y1="80" x2="312" y2="230" stroke="white" strokeOpacity="0.5" strokeWidth="3" strokeLinecap="round"/>
                  <path d="M304 80 Q312 90 312 130" stroke="white" strokeOpacity="0.6" strokeWidth="5" fill="none" strokeLinecap="round"/>
                </svg>
              </div>
            </div>
            <p className="dc-hero-description">
              Plan, monitor, and optimise patient meal plans with precision and care.
            </p>
          </div>
        </div>
      </div>

      {/* ── Right Panel ── */}
      <div className="dc-login-panel">
        <div className="dc-panel-content">

          <div className="dc-brand-section">
            <img src={evercarelogo} alt="Evercare Logo" className="dc-brand-logo" />
          </div>

          <div className="dc-form-card">
            <h2 className="dc-form-heading">Dietician Sign In</h2>
            <p className="dc-form-subheading">Enter your credentials to access the portal</p>

            <form onSubmit={handleSubmit}>
              {error && (
                <div className="dc-alert-error">
                  <AlertCircle size={18} />
                  <span>{error}</span>
                </div>
              )}

              {/* Email */}
              <div className="dc-field-group">
                <label className="dc-field-label">Email Address</label>
                <div className="dc-field-container">
                  <Mail className="dc-field-icon" size={18} />
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="Enter your email"
                    className="dc-field-input"
                    disabled={isLoading}
                    autoComplete="email"
                  />
                </div>
              </div>

              {/* Password */}
              <div className="dc-field-group">
                <label className="dc-field-label">Password</label>
                <div className="dc-field-container">
                  <Lock className="dc-field-icon" size={18} />
                  <input
                    type={showPassword ? 'text' : 'password'}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="Enter your password"
                    className="dc-field-input dc-field-password"
                    disabled={isLoading}
                    autoComplete="current-password"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="dc-password-toggle"
                    disabled={isLoading}
                  >
                    {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                  </button>
                </div>

                {/* Forgot password — below the input */}
                <Link to="/dietician/forgot-password" className="dc-forgot-link">
                  Forgot password?
                </Link>
              </div>

              <button type="submit" className="dc-submit-btn" disabled={isLoading}>
                {isLoading ? (
                  <>
                    <Loader2 className="dc-btn-spinner" size={18} />
                    <span>Signing In...</span>
                  </>
                ) : (
                  'Sign In'
                )}
              </button>
            </form>
          </div>

          <div className="dc-login-footer">
            <p>Copyright © 2026 – Evercare Hospital Lekki</p>
          </div>

        </div>
      </div>
    </div>
  );
};

export default DieticianLogin;