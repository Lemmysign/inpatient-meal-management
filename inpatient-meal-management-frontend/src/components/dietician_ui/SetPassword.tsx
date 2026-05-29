import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Eye, EyeOff, Lock, AlertCircle, Loader2, CheckCircle2, ShieldCheck } from 'lucide-react';
import { config } from '../../config/config';
import '../../styles/dietician/dietician.css';
import evercarelogo from '../../assets/icons/ec_logo.png';

interface SetPasswordResponse {
  success: boolean;
  message: string;
  timestamp: string;
}

const passwordRules = [
  { label: 'At least 8 characters', test: (p: string) => p.length >= 8 },
  { label: 'One uppercase letter',  test: (p: string) => /[A-Z]/.test(p) },
  { label: 'One lowercase letter',  test: (p: string) => /[a-z]/.test(p) },
  { label: 'One number',            test: (p: string) => /\d/.test(p) },
];

const SetPassword: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  const [password, setPassword]               = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword]       = useState(false);
  const [showConfirm, setShowConfirm]         = useState(false);
  const [isLoading, setIsLoading]             = useState(false);
  const [error, setError]                     = useState('');
  const [success, setSuccess]                 = useState(false);

  useEffect(() => {
    if (!token) {
      setError('Invalid or missing invitation link. Please request a new invite from your administrator.');
    }
  }, [token]);

  const allRulesPassed = passwordRules.every(r => r.test(password));

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!token) {
      setError('Invalid or missing invitation token.');
      return;
    }

    if (!allRulesPassed) {
      setError('Please meet all password requirements.');
      return;
    }

    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    setIsLoading(true);

    try {
      const response = await fetch(config.dieticianSetPasswordUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token, password, confirmPassword }),
      });

      const data: SetPasswordResponse = await response.json();

      if (response.ok && data.success) {
        setSuccess(true);
        setTimeout(() => navigate('/dietician/login'), 3000);
      } else {
        setError(data.message || 'Something went wrong. The link may have expired.');
      }
    } catch {
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
                  {/* Plate shadow */}
                  <ellipse cx="200" cy="210" rx="140" ry="22" fill="black" fillOpacity="0.1"/>
                  {/* Plate */}
                  <circle cx="200" cy="155" r="115" fill="white" fillOpacity="0.12" stroke="white" strokeOpacity="0.25" strokeWidth="2"/>
                  <circle cx="200" cy="155" r="88" fill="white" fillOpacity="0.1"/>
                  {/* Food segments */}
                  <path d="M200 155 L200 70 A85 85 0 0 1 274 112 Z" fill="#4ade80" fillOpacity="0.75"/>
                  <path d="M200 155 L274 112 A85 85 0 0 1 274 198 Z" fill="#fb923c" fillOpacity="0.75"/>
                  <path d="M200 155 L274 198 A85 85 0 0 1 126 198 Z" fill="#60a5fa" fillOpacity="0.75"/>
                  <path d="M200 155 L126 198 A85 85 0 0 1 126 112 Z" fill="#f472b6" fillOpacity="0.75"/>
                  <path d="M200 155 L126 112 A85 85 0 0 1 200 70 Z"  fill="#facc15" fillOpacity="0.75"/>
                  {/* Centre circle */}
                  <circle cx="200" cy="155" r="30" fill="white" fillOpacity="0.92"/>
                  <text x="200" y="152" textAnchor="middle" fontSize="9" fill="#1a6b4a" fontWeight="700">DIET</text>
                  <text x="200" y="163" textAnchor="middle" fontSize="9" fill="#1a6b4a" fontWeight="700">PLAN</text>
                  {/* Fork */}
                  <line x1="88"  y1="80"  x2="88"  y2="230" stroke="white" strokeOpacity="0.5" strokeWidth="3" strokeLinecap="round"/>
                  <path d="M80 80 Q80 125 88 140 Q96 125 96 80" stroke="white" strokeOpacity="0.5" strokeWidth="3" fill="none" strokeLinecap="round"/>
                  {/* Knife */}
                  <line x1="312" y1="80"  x2="312" y2="230" stroke="white" strokeOpacity="0.5" strokeWidth="3" strokeLinecap="round"/>
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

          {/* Logo */}
          <div className="dc-brand-section">
            <img src={evercarelogo} alt="Evercare Logo" className="dc-brand-logo" />
          </div>

          {/* Card */}
          <div className="dc-form-card">

            {success ? (
              /* ── Success State ── */
              <div className="sp-success">
                <div className="sp-success-icon">
                  <CheckCircle2 size={48} color="#16a34a" />
                </div>
                <h2 className="sp-success-title">Password Set!</h2>
                <p className="sp-success-msg">
                  Your account is now active. Redirecting you to the login page…
                </p>
                <div className="sp-success-bar">
                  <div className="sp-success-bar-fill" />
                </div>
              </div>
            ) : (
              <>
                <div className="sp-heading-row">
                  <ShieldCheck size={22} className="sp-heading-icon" />
                  <div>
                    <h2 className="dc-form-heading">Set Your Password</h2>
                    <p className="dc-form-subheading">Create a secure password for your account</p>
                  </div>
                </div>

                <form onSubmit={handleSubmit}>

                  {/* Error */}
                  {error && (
                    <div className="dc-alert-error">
                      <AlertCircle size={18} />
                      <span>{error}</span>
                    </div>
                  )}

                  {/* New Password */}
                  <div className="dc-field-group">
                    <label className="dc-field-label">New Password</label>
                    <div className="dc-field-container">
                      <Lock className="dc-field-icon" size={18} />
                      <input
                        type={showPassword ? 'text' : 'password'}
                        value={password}
                        onChange={e => setPassword(e.target.value)}
                        placeholder="Create a strong password"
                        className="dc-field-input dc-field-password"
                        disabled={isLoading || !token}
                        autoComplete="new-password"
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
                  </div>

                  {/* Password strength rules */}
                  {password.length > 0 && (
                    <div className="sp-rules">
                      {passwordRules.map(rule => (
                        <div
                          key={rule.label}
                          className={`sp-rule ${rule.test(password) ? 'sp-rule--pass' : 'sp-rule--fail'}`}
                        >
                          <span className="sp-rule-dot" />
                          {rule.label}
                        </div>
                      ))}
                    </div>
                  )}

                  {/* Confirm Password */}
                  <div className="dc-field-group">
                    <label className="dc-field-label">Confirm Password</label>
                    <div className="dc-field-container">
                      <Lock className="dc-field-icon" size={18} />
                      <input
                        type={showConfirm ? 'text' : 'password'}
                        value={confirmPassword}
                        onChange={e => setConfirmPassword(e.target.value)}
                        placeholder="Repeat your password"
                        className={`dc-field-input dc-field-password ${
                          confirmPassword && confirmPassword !== password ? 'sp-input--error' : ''
                        } ${
                          confirmPassword && confirmPassword === password ? 'sp-input--match' : ''
                        }`}
                        disabled={isLoading || !token}
                        autoComplete="new-password"
                      />
                      <button
                        type="button"
                        onClick={() => setShowConfirm(!showConfirm)}
                        className="dc-password-toggle"
                        disabled={isLoading}
                      >
                        {showConfirm ? <EyeOff size={18} /> : <Eye size={18} />}
                      </button>
                    </div>
                    {confirmPassword && confirmPassword !== password && (
                      <p className="sp-mismatch">Passwords do not match</p>
                    )}
                  </div>

                  {/* Submit */}
                  <button
                    type="submit"
                    className="dc-submit-btn"
                    disabled={isLoading || !token}
                  >
                    {isLoading ? (
                      <>
                        <Loader2 className="dc-btn-spinner" size={18} />
                        <span>Setting Password…</span>
                      </>
                    ) : (
                      'Set Password & Activate Account'
                    )}
                  </button>

                </form>
              </>
            )}
          </div>

          {/* Footer */}
          <div className="dc-login-footer">
            <p>Copyright © 2026 – Evercare Hospital Lekki</p>
          </div>

        </div>
      </div>
    </div>
  );
};

export default SetPassword;