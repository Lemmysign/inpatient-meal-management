import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Mail, AlertCircle, CheckCircle, Loader2, ArrowLeft } from 'lucide-react';
import { config } from '../../config/config';
import '../../styles/admin/admin.css';
import evercarelogo from '../../assets/icons/ec_logo.png';

const AdminForgotPassword: React.FC = () => {
  const [email, setEmail]           = useState('');
  const [isLoading, setIsLoading]   = useState(false);
  const [error, setError]           = useState('');
  const [sent, setSent]             = useState(false);
  const [successMsg, setSuccessMsg] = useState('');

  // ── Tag this browser session as "admin" so ResetPassword
  //    knows which login page to redirect to after reset ──
  useEffect(() => {
    localStorage.setItem('reset_portal', 'admin');
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!email.trim()) return setError('Email address is required.');
    if (!/\S+@\S+\.\S+/.test(email)) return setError('Please enter a valid email address.');

    setIsLoading(true);
    try {
      const res = await fetch(config.forgotPasswordUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: email.trim() }),
      });
      const data = await res.json();
      if (res.ok && data.success) {
        setSent(true);
        setSuccessMsg(data.message);
      } else {
        setError(data.message || 'Something went wrong. Please try again.');
      }
    } catch {
      setError('Network error. Please check your connection and try again.');
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
            <span className="al-hero-eyebrow">Account Recovery</span>
            <h1>Reset Your Admin Password Securely.</h1>
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
                  <rect x="140" y="130" width="120" height="95" rx="12"
                    fill="white" fillOpacity="0.12" stroke="white" strokeOpacity="0.25" strokeWidth="1.5"/>
                  <path d="M165 130 L165 105 Q165 75 200 75 Q235 75 235 105 L235 130"
                    stroke="white" strokeOpacity="0.5" strokeWidth="8" strokeLinecap="round" fill="none"/>
                  <circle cx="200" cy="175" r="18"
                    fill="white" fillOpacity="0.18" stroke="white" strokeOpacity="0.35" strokeWidth="1.5"/>
                  <rect x="196" y="178" width="8" height="20" rx="4" fill="white" fillOpacity="0.4"/>
                  <circle cx="316" cy="75" r="22"
                    fill="none" stroke="#34d399" strokeOpacity="0.6" strokeWidth="2"/>
                  <path d="M306 75 L313 82 L326 68"
                    stroke="#34d399" strokeOpacity="0.9" strokeWidth="2.5"
                    strokeLinecap="round" strokeLinejoin="round"/>
                  <rect x="48" y="170" width="56" height="40" rx="6"
                    fill="white" fillOpacity="0.08" stroke="white" strokeOpacity="0.18" strokeWidth="1"/>
                  <path d="M48 178 L76 196 L104 178"
                    stroke="white" strokeOpacity="0.35" strokeWidth="1.5"
                    strokeLinecap="round" strokeLinejoin="round" fill="none"/>
                  <circle cx="105" cy="90"  r="3" fill="#60a5fa" fillOpacity="0.5"/>
                  <circle cx="310" cy="140" r="3" fill="#34d399"  fillOpacity="0.5"/>
                  <circle cx="80"  cy="140" r="2" fill="white"    fillOpacity="0.3"/>
                  <circle cx="330" cy="190" r="2" fill="white"    fillOpacity="0.3"/>
                </svg>
              </div>
            </div>
            <p className="al-hero-description">
              Enter your admin email and we'll send a secure reset link to your inbox.
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

            <h2 className="al-form-heading">Forgot Password?</h2>
            <p className="al-form-subheading">
              We'll send a reset link to your registered admin email address.
            </p>

            {sent ? (
              <div className="al-fp-success">
                <div className="al-fp-success-icon">
                  <CheckCircle size={40} strokeWidth={1.5} />
                </div>
                <p className="al-fp-success-msg">{successMsg}</p>
                <p className="al-fp-success-hint">
                  Check your inbox and click the link to reset your password.
                  The link expires in <strong>1 hour</strong>.
                </p>
              </div>
            ) : (
              <form onSubmit={handleSubmit}>
                {error && (
                  <div className="al-alert-error">
                    <AlertCircle size={18} />
                    <span>{error}</span>
                  </div>
                )}

                <div className="al-field-group">
                  <label className="al-field-label">Admin Email Address</label>
                  <div className="al-field-container">
                    <Mail className="al-field-icon" size={18} />
                    <input
                      type="email"
                      value={email}
                      onChange={e => setEmail(e.target.value)}
                      placeholder="Enter your admin email"
                      className="al-field-input"
                      disabled={isLoading}
                      autoComplete="email"
                    />
                  </div>
                </div>

                <button type="submit" className="al-submit-btn" disabled={isLoading}>
                  {isLoading ? (
                    <><Loader2 className="al-btn-spinner" size={18} /><span>Sending Reset Link...</span></>
                  ) : (
                    <><Mail size={16} /><span>Send Reset Link</span></>
                  )}
                </button>
              </form>
            )}

            <div className="al-fp-back">
              <ArrowLeft size={15} />
              <Link to="/admin/login">Back to Login</Link>
            </div>
          </div>

          <div className="al-login-footer">
            <p>Copyright © 2026 – Evercare Hospital Lekki</p>
          </div>

        </div>
      </div>
    </div>
  );
};

export default AdminForgotPassword;