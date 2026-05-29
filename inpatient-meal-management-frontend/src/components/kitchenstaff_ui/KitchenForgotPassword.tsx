import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Mail, AlertCircle, CheckCircle, Loader2, ArrowLeft } from 'lucide-react';
import { config } from '../../config/config';
import '../../styles/kitchenstaff/kitchenstafflogin.css';
import evercarelogo from '../../assets/icons/ec_logo.png';

const KitchenForgotPassword: React.FC = () => {
  const [email, setEmail]           = useState('');
  const [isLoading, setIsLoading]   = useState(false);
  const [error, setError]           = useState('');
  const [sent, setSent]             = useState(false);
  const [successMsg, setSuccessMsg] = useState('');

  // ── Tag this browser session as "kitchen" so ResetPassword
  //    knows which login page to redirect to after reset ──
  useEffect(() => {
    localStorage.setItem('reset_portal', 'kitchen');
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
    <div className="ks-login-page">

      {/* ── Left Hero ── */}
      <div className="ks-login-hero">
        <div className="ks-hero-shape-primary" />
        <div className="ks-hero-shape-secondary" />

        <div className="ks-hero-wrapper">
          <div className="ks-hero-header">
            <h1>Secure Account Recovery for Kitchen Portal.</h1>
          </div>

          <div className="ks-hero-visual">
            <div className="ks-hero-image-container">
              <div className="ks-hero-illustration">
                <svg viewBox="0 0 400 260" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect x="140" y="130" width="120" height="95" rx="12"
                    fill="white" fillOpacity="0.12" stroke="white" strokeOpacity="0.25" strokeWidth="1.5"/>
                  <path d="M165 130 L165 105 Q165 75 200 75 Q235 75 235 105 L235 130"
                    stroke="white" strokeOpacity="0.5" strokeWidth="8" strokeLinecap="round" fill="none"/>
                  <circle cx="200" cy="175" r="18"
                    fill="white" fillOpacity="0.18" stroke="white" strokeOpacity="0.35" strokeWidth="1.5"/>
                  <rect x="196" y="178" width="8" height="20" rx="4" fill="white" fillOpacity="0.4"/>
                  <g transform="translate(55, 60)">
                    <rect x="10" y="35" width="50" height="12" rx="3" fill="white" fillOpacity="0.25"/>
                    <path d="M15 35 Q15 10 35 10 Q55 10 55 35"
                      fill="white" fillOpacity="0.2" stroke="white" strokeOpacity="0.35" strokeWidth="1.5"/>
                    <circle cx="35" cy="12" r="8"
                      fill="white" fillOpacity="0.15" stroke="white" strokeOpacity="0.3" strokeWidth="1"/>
                  </g>
                  <rect x="285" y="155" width="70" height="50" rx="7"
                    fill="white" fillOpacity="0.1" stroke="white" strokeOpacity="0.2" strokeWidth="1.5"/>
                  <path d="M285 163 L320 183 L355 163"
                    stroke="white" strokeOpacity="0.4" strokeWidth="1.5"
                    strokeLinecap="round" strokeLinejoin="round" fill="none"/>
                  <circle cx="310" cy="80" r="22"
                    fill="none" stroke="white" strokeOpacity="0.3" strokeWidth="1.5"/>
                  <path d="M300 80 L307 87 L320 73"
                    stroke="white" strokeOpacity="0.7" strokeWidth="2.5"
                    strokeLinecap="round" strokeLinejoin="round"/>
                  <circle cx="90"  cy="150" r="3" fill="white" fillOpacity="0.3"/>
                  <circle cx="340" cy="120" r="3" fill="white" fillOpacity="0.3"/>
                  <circle cx="75"  cy="195" r="2" fill="white" fillOpacity="0.25"/>
                  <circle cx="355" cy="80"  r="2" fill="white" fillOpacity="0.25"/>
                </svg>
              </div>
            </div>
            <p className="ks-hero-description">
              Enter your registered email and we'll send a secure reset link to your inbox.
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
            <h2 className="ks-form-heading">Forgot Password?</h2>
            <p className="ks-form-subheading">
              We'll send a reset link to your registered kitchen staff email address.
            </p>

            {sent ? (
              <div className="ks-fp-success">
                <div className="ks-fp-success-icon">
                  <CheckCircle size={40} strokeWidth={1.5} />
                </div>
                <p className="ks-fp-success-msg">{successMsg}</p>
                <p className="ks-fp-success-hint">
                  Check your inbox and click the link to reset your password.
                  The link expires in <strong>1 hour</strong>.
                </p>
              </div>
            ) : (
              <form onSubmit={handleSubmit}>
                {error && (
                  <div className="ks-alert-error">
                    <AlertCircle size={18} />
                    <span>{error}</span>
                  </div>
                )}

                <div className="ks-field-group">
                  <label className="ks-field-label">Email Address</label>
                  <div className="ks-field-container">
                    <Mail className="ks-field-icon" size={18} />
                    <input
                      type="email"
                      value={email}
                      onChange={e => setEmail(e.target.value)}
                      placeholder="Enter your email"
                      className="ks-field-input"
                      disabled={isLoading}
                      autoComplete="email"
                    />
                  </div>
                </div>

                <button type="submit" className="ks-submit-btn" disabled={isLoading}>
                  {isLoading ? (
                    <><Loader2 className="ks-btn-spinner" size={18} /><span>Sending Reset Link...</span></>
                  ) : (
                    <><Mail size={16} /><span>Send Reset Link</span></>
                  )}
                </button>
              </form>
            )}

            <div className="ks-fp-back">
              <ArrowLeft size={15} />
              <Link to="/kitchen/login">Back to Login</Link>
            </div>
          </div>

          <div className="ks-login-footer">
            <p>Copyright © 2026 – Evercare Hospital Lekki</p>
          </div>

        </div>
      </div>
    </div>
  );
};

export default KitchenForgotPassword;