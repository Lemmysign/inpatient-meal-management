import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import { Lock, Eye, EyeOff, AlertCircle, CheckCircle, Loader2, ArrowLeft, KeyRound, ShieldCheck } from 'lucide-react';
import { config } from '../../config/config';
import '../../styles/shared/reset-password.css';
import evercarelogo from '../../assets/icons/ec_logo.png';

/* ─────────────────────────────────────────── */
/*  Role → login page map                       */
/* ─────────────────────────────────────────── */
const ROLE_REDIRECT: Record<string, string> = {
  admin:     '/admin/login',
  dietician: '/dietician/login',
  kitchen:   '/kitchen/login',
};

const ROLE_LABEL: Record<string, string> = {
  admin:     'Admin',
  dietician: 'Dietician',
  kitchen:   'Kitchen Staff',
};

/* ─────────────────────────────────────────── */
/*  Detect which portal triggered the reset     */
/*                                              */
/*  Priority order:                             */
/*  1. ?role= in the URL  (if backend sends it) */
/*  2. reset_portal in localStorage             */
/*     (set when user visits forgot-password)   */
/*  3. Fall back to 'admin'                     */
/* ─────────────────────────────────────────── */
const detectPortal = (roleFromUrl: string): string => {
  const validRoles = Object.keys(ROLE_REDIRECT);

  // 1. URL param takes highest priority
  if (validRoles.includes(roleFromUrl)) return roleFromUrl;

  // 2. localStorage — set by the forgot-password page
  const stored = localStorage.getItem('reset_portal') ?? '';
  if (validRoles.includes(stored)) return stored;

  // 3. Safe fallback
  return 'admin';
};

/* ─────────────────────────────────────────── */
/*  Password strength helper                    */
/* ─────────────────────────────────────────── */
const getStrength = (p: string): number => {
  let s = 0;
  if (p.length >= 8)                         s++;
  if (/[A-Z]/.test(p))                       s++;
  if (/[0-9]/.test(p))                       s++;
  if (/[@$!%*?&#^()_\-+=]/.test(p))          s++;
  return s;
};
const STRENGTH_LABELS = ['', 'Weak', 'Fair', 'Good', 'Strong'];
const STRENGTH_COLORS = ['', '#ef4444', '#f59e0b', '#3b82f6', '#22c55e'];

/* ─────────────────────────────────────────── */
/*  Component                                   */
/* ─────────────────────────────────────────── */
const ResetPassword: React.FC = () => {
  const navigate       = useNavigate();
  const [searchParams] = useSearchParams();

  const tokenFromUrl = searchParams.get('token') ?? '';
  const roleFromUrl  = (searchParams.get('role') ?? '').toLowerCase();

  // Store role in state so it's locked in at mount time and never
  // lost due to re-renders or localStorage being cleared mid-flow
  const [role]      = useState<string>(() => detectPortal(roleFromUrl));
  const loginPath   = ROLE_REDIRECT[role];

  const [token,           setToken]           = useState(tokenFromUrl);
  const [password,        setPassword]        = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword,    setShowPassword]    = useState(false);
  const [showConfirm,     setShowConfirm]     = useState(false);
  const [isLoading,       setIsLoading]       = useState(false);
  const [error,           setError]           = useState('');
  const [done,            setDone]            = useState(false);
  const [countdown,       setCountdown]       = useState(2);

  /* Auto-redirect countdown after success.
     role is locked in useState so it's safe even after
     localStorage is cleared at the moment of navigation. */
  useEffect(() => {
    if (!done) return;
    const id = setInterval(() => {
      setCountdown(c => {
        if (c <= 1) {
          clearInterval(id);
          localStorage.removeItem('reset_portal');
          navigate(loginPath);
        }
        return c - 1;
      });
    }, 1000);
    return () => clearInterval(id);
  }, [done, loginPath, navigate]);

  const strength      = getStrength(password);
  const strengthLabel = STRENGTH_LABELS[strength];
  const strengthColor = STRENGTH_COLORS[strength];

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!token.trim())                return setError('Reset token is missing or invalid.');
    if (!password)                    return setError('Please enter a new password.');
    if (password.length < 8)          return setError('Password must be at least 8 characters.');
    if (strength < 3)                 return setError('Please choose a stronger password.');
    if (password !== confirmPassword) return setError('Passwords do not match.');

    setIsLoading(true);
    try {
      const res = await fetch(config.resetPasswordUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          token:           token.trim(),
          password,
          confirmPassword,
        }),
      });
      const data = await res.json();
      if (res.ok && data.success) {
        setDone(true);
      } else {
        setError(data.message || 'Failed to reset password. The link may have expired.');
      }
    } catch {
      setError('Network error. Please check your connection and try again.');
    } finally {
      setIsLoading(false);
    }
  };

  /* ── Hero colour matches the portal theme ── */
  const heroClass =
    role === 'kitchen'   ? 'rp-hero rp-hero--kitchen'   :
    role === 'dietician' ? 'rp-hero rp-hero--dietician' :
                           'rp-hero rp-hero--admin';

  return (
    <div className="rp-page">

      {/* ── Left Hero ── */}
      <div className={heroClass}>
        <div className="rp-hero-shape rp-hero-shape--1" />
        <div className="rp-hero-shape rp-hero-shape--2" />

        <div className="rp-hero-content">
          <div className="rp-hero-icon">
            <ShieldCheck size={48} strokeWidth={1.5} />
          </div>

          <h1 className="rp-hero-title">
            {done ? 'All Done!' : 'Create New Password'}
          </h1>

          <p className="rp-hero-desc">
            {done
              ? `Your ${ROLE_LABEL[role]} password has been updated. Redirecting to login in ${countdown}s…`
              : `Choose a strong, unique password for your ${ROLE_LABEL[role]} account. Your account security depends on it.`}
          </p>

          {!done && (
            <div className="rp-hero-tips">
              <p className="rp-hero-tips-title">Password must contain:</p>
              <ul>
                <li className={password.length >= 8                        ? 'rp-tip--pass' : ''}>At least 8 characters</li>
                <li className={/[A-Z]/.test(password)                      ? 'rp-tip--pass' : ''}>One uppercase letter</li>
                <li className={/[0-9]/.test(password)                      ? 'rp-tip--pass' : ''}>One number</li>
                <li className={/[@$!%*?&#^()_\-+=]/.test(password)         ? 'rp-tip--pass' : ''}>One special character</li>
              </ul>
            </div>
          )}
        </div>
      </div>

      {/* ── Right Panel ── */}
      <div className="rp-panel">
        <div className="rp-panel-content">

          <div className="rp-brand">
            <img src={evercarelogo} alt="Evercare Logo" className="rp-brand-logo" />
          </div>

          <div className="rp-card">

            {done ? (
              /* ── Success state ── */
              <div className="rp-done">
                <div className="rp-done-icon">
                  <CheckCircle size={44} strokeWidth={1.5} />
                </div>
                <h2 className="rp-card-title">Password Reset!</h2>
                <p className="rp-card-subtitle">
                  Your password has been updated successfully.
                </p>
                <p className="rp-done-redirect">
                  Redirecting to <strong>{ROLE_LABEL[role]} login</strong> in {countdown}s…
                </p>
                <div className="rp-done-bar">
                  <div className="rp-done-bar-fill" style={{ animationDuration: '2s' }} />
                </div>
                <button className="rp-btn" onClick={() => navigate(loginPath)}>
                  Go to Login Now
                </button>
              </div>
            ) : (
              /* ── Reset form ── */
              <>
                <div className="rp-card-icon">
                  <KeyRound size={22} />
                </div>
                <h2 className="rp-card-title">Set New Password</h2>
                <p className="rp-card-subtitle">
                  {ROLE_LABEL[role]} account · enter your new password below.
                </p>

                <form onSubmit={handleSubmit}>
                  {error && (
                    <div className="rp-alert rp-alert--error">
                      <AlertCircle size={17} />
                      <span>{error}</span>
                    </div>
                  )}

                  {/* Token field */}
                  <div className="rp-field">
                    <label className="rp-label">Reset Token</label>
                    <div className="rp-input-wrap">
                      <KeyRound className="rp-input-icon" size={17} />
                      <input
                        type="text"
                        value={token}
                        onChange={e => setToken(e.target.value)}
                        placeholder="Reset token"
                        className="rp-input"
                        disabled={isLoading}
                      />
                    </div>
                    {tokenFromUrl && (
                      <p className="rp-hint">✓ Token auto-filled from your reset link.</p>
                    )}
                  </div>

                  {/* New password */}
                  <div className="rp-field">
                    <label className="rp-label">New Password</label>
                    <div className="rp-input-wrap">
                      <Lock className="rp-input-icon" size={17} />
                      <input
                        type={showPassword ? 'text' : 'password'}
                        value={password}
                        onChange={e => setPassword(e.target.value)}
                        placeholder="Enter new password"
                        className="rp-input rp-input--padded"
                        disabled={isLoading}
                        autoComplete="new-password"
                      />
                      <button type="button" className="rp-toggle"
                        onClick={() => setShowPassword(!showPassword)} disabled={isLoading}>
                        {showPassword ? <EyeOff size={17} /> : <Eye size={17} />}
                      </button>
                    </div>

                    {password && (
                      <div className="rp-strength">
                        <div className="rp-strength-bars">
                          {[1, 2, 3, 4].map(i => (
                            <div key={i} className="rp-strength-bar"
                              style={{ backgroundColor: i <= strength ? strengthColor : '#e5e7eb' }} />
                          ))}
                        </div>
                        <span className="rp-strength-label" style={{ color: strengthColor }}>
                          {strengthLabel}
                        </span>
                      </div>
                    )}
                  </div>

                  {/* Confirm password */}
                  <div className="rp-field">
                    <label className="rp-label">Confirm Password</label>
                    <div className="rp-input-wrap">
                      <Lock className="rp-input-icon" size={17} />
                      <input
                        type={showConfirm ? 'text' : 'password'}
                        value={confirmPassword}
                        onChange={e => setConfirmPassword(e.target.value)}
                        placeholder="Confirm new password"
                        className={`rp-input rp-input--padded ${
                          confirmPassword && confirmPassword !== password ? 'rp-input--error' : ''
                        } ${
                          confirmPassword && confirmPassword === password ? 'rp-input--match' : ''
                        }`}
                        disabled={isLoading}
                        autoComplete="new-password"
                      />
                      <button type="button" className="rp-toggle"
                        onClick={() => setShowConfirm(!showConfirm)} disabled={isLoading}>
                        {showConfirm ? <EyeOff size={17} /> : <Eye size={17} />}
                      </button>
                    </div>
                    {confirmPassword && confirmPassword !== password && (
                      <p className="rp-field-error">Passwords do not match.</p>
                    )}
                    {confirmPassword && confirmPassword === password && (
                      <p className="rp-field-match">✓ Passwords match.</p>
                    )}
                  </div>

                  <button type="submit" className="rp-btn" disabled={isLoading}>
                    {isLoading ? (
                      <><Loader2 className="rp-btn-spinner" size={17} /><span>Resetting...</span></>
                    ) : (
                      <><ShieldCheck size={16} /><span>Reset Password</span></>
                    )}
                  </button>
                </form>

                <div className="rp-back">
                  <ArrowLeft size={15} />
                  <Link to={loginPath}>Back to {ROLE_LABEL[role]} Login</Link>
                </div>
              </>
            )}
          </div>

          <div className="rp-footer">
            <p>Copyright © 2026 – Evercare Hospital Lekki</p>
          </div>

        </div>
      </div>
    </div>
  );
};

export default ResetPassword;