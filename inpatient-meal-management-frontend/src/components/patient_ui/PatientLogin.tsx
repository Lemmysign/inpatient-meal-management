import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { config } from '../../config/config';
import evercarelogo from '../../assets/icons/EVERCARE_LOGO.png';
import '../../styles/patient/patient.css';

interface FormData {
  uhid: string;
  fullName: string;
  roomNumber: string;
}

interface FormErrors {
  uhid?: string;
  fullName?: string;
  roomNumber?: string;
  general?: string;
}

interface LoginResponse {
  success: boolean;
  message: string;
  data: {
    sessionToken: string;
    patientId: string;
    uhid: string;
    name: string;
    roomNumber: string;
    expiresAt: string;
    hasActiveMenu: boolean;
  };
  timestamp: string;
}

const PatientLogin: React.FC = () => {
  const navigate = useNavigate();

  const [formData, setFormData] = useState<FormData>({
    uhid: '',
    fullName: '',
    roomNumber: '',
  });

  const [errors, setErrors] = useState<FormErrors>({});
  const [isLoading, setIsLoading] = useState(false);
  const [focusedField, setFocusedField] = useState<string | null>(null);
  const [showSuccessPopup, setShowSuccessPopup] = useState(false);
  const [hisEnabled, setHisEnabled] = useState<boolean | null>(null);
  const [hisStatusLoading, setHisStatusLoading] = useState(true);

  // Fetch HIS mode on page load
  useEffect(() => {
    const fetchHISStatus = async () => {
      try {
        const response = await axios.get(config.hisStatusUrl);
        setHisEnabled(response.data.data);
      } catch (err) {
        console.error('Could not fetch HIS status:', err);
        setHisEnabled(false);
      } finally {
        setHisStatusLoading(false);
      }
    };

    fetchHISStatus();
  }, []);

  const validate = (): boolean => {
    const newErrors: FormErrors = {};

    if (!formData.uhid.trim()) {
      newErrors.uhid = 'UHID is required';
    }

    if (!hisEnabled) {
      if (!formData.fullName.trim()) {
        newErrors.fullName = 'Full name is required';
      }
      if (!formData.roomNumber.trim()) {
        newErrors.roomNumber = 'Room number is required';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (errors[name as keyof FormErrors]) {
      setErrors(prev => ({ ...prev, [name]: undefined }));
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validate()) return;

    setIsLoading(true);
    setErrors({});

    try {
      const payload = hisEnabled
        ? { uhid: formData.uhid }
        : {
            uhid: formData.uhid,
            name: formData.fullName,
            roomNumber: formData.roomNumber,
          };

      const response = await axios.post<LoginResponse>(
        config.patientLoginUrl,
        payload,
        { headers: { 'Content-Type': 'application/json' } }
      );

      if (response.data.success) {
        localStorage.setItem('sessionToken', response.data.data.sessionToken);
        localStorage.setItem('patientId', response.data.data.patientId);
    
        setShowSuccessPopup(true);
        setTimeout(() => navigate('/patient/dashboard'), 1500);
      }
    } catch (err: any) {
      console.error('Login error:', err);
      const message = err.response?.data?.message;

      if (message) {
        if (hisEnabled) {
          setErrors({ uhid: message });
        } else {
          setErrors({ general: message });
        }
      } else {
        setErrors({ general: 'Login failed. Please check your details and try again.' });
      }
    } finally {
      setIsLoading(false);
    }
  };

  if (hisStatusLoading) {
    return (
      <div className="patient-login-container">
        <div className="patient-login-wrapper">
          <div className="patient-login-card">
            <div className="patient-logo-container">
              <img src={evercarelogo} alt="Evercare Logo" className="patient-logo" />
            </div>
            <div style={{ textAlign: 'center', padding: '2rem' }}>
              <span className="patient-submit-spinner" />
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="patient-login-container">
      <div className="patient-login-wrapper">
        <div className="patient-login-card">

          {/* Logo */}
          <div className="patient-logo-container">
            <img src={evercarelogo} alt="Evercare Logo" className="patient-logo" />
          </div>

          {/* Header */}
          <div className="patient-header">
            <h1 className="patient-title">Welcome to Evercare</h1>
            <p className="patient-subtitle">
              {hisEnabled
                ? 'Enter your UHID to access today\'s personalized menu.'
                : 'Please enter your patient details to view today\'s personalized menu.'}
            </p>
          </div>

          {/* Divider */}
          <div className="patient-divider">
            <span className="patient-divider-line" />
            <span className="patient-divider-icon">✦</span>
            <span className="patient-divider-line" />
          </div>

          {/* General error */}
          {errors.general && (
            <div className="patient-general-error">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none"
                stroke="currentColor" strokeWidth="2.5"
                strokeLinecap="round" strokeLinejoin="round">
                <circle cx="12" cy="12" r="10" />
                <line x1="12" y1="8" x2="12" y2="12" />
                <line x1="12" y1="16" x2="12.01" y2="16" />
              </svg>
              {errors.general}
            </div>
          )}

          {/* Form */}
          <form className="patient-form" onSubmit={handleSubmit} noValidate>

            {/* UHID — always shown */}
            <div className={`patient-form-field 
              ${focusedField === 'uhid' ? 'patient-form-field-focused' : ''} 
              ${errors.uhid ? 'patient-form-field-error' : ''} 
              ${formData.uhid ? 'patient-form-field-filled' : ''}`}>
              <label className="patient-form-label" htmlFor="uhid">
                <span className="patient-form-label-icon">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                    stroke="currentColor" strokeWidth="2"
                    strokeLinecap="round" strokeLinejoin="round">
                    <rect x="2" y="5" width="20" height="14" rx="2" />
                    <line x1="2" y1="10" x2="22" y2="10" />
                  </svg>
                </span>
                UHID
              </label>
              <input
                id="uhid"
                name="uhid"
                type="text"
                className="patient-form-input"
                placeholder="Enter your UHID"
                value={formData.uhid}
                onChange={handleChange}
                onFocus={() => setFocusedField('uhid')}
                onBlur={() => setFocusedField(null)}
                autoComplete="off"
              />
              <div className="patient-form-field-border" />
              {errors.uhid && (
                <span className="patient-form-error">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none"
                    stroke="currentColor" strokeWidth="2.5"
                    strokeLinecap="round" strokeLinejoin="round">
                    <circle cx="12" cy="12" r="10" />
                    <line x1="12" y1="8" x2="12" y2="12" />
                    <line x1="12" y1="16" x2="12.01" y2="16" />
                  </svg>
                  {errors.uhid}
                </span>
              )}
            </div>

            {/* Full Name — only in manual mode */}
            {!hisEnabled && (
              <div className={`patient-form-field 
                ${focusedField === 'fullName' ? 'patient-form-field-focused' : ''} 
                ${errors.fullName ? 'patient-form-field-error' : ''} 
                ${formData.fullName ? 'patient-form-field-filled' : ''}`}>
                <label className="patient-form-label" htmlFor="fullName">
                  <span className="patient-form-label-icon">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                      stroke="currentColor" strokeWidth="2"
                      strokeLinecap="round" strokeLinejoin="round">
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                      <circle cx="12" cy="7" r="4" />
                    </svg>
                  </span>
                  Full Name
                </label>
                <input
                  id="fullName"
                  name="fullName"
                  type="text"
                  className="patient-form-input"
                  placeholder="Enter your full name"
                  value={formData.fullName}
                  onChange={handleChange}
                  onFocus={() => setFocusedField('fullName')}
                  onBlur={() => setFocusedField(null)}
                  autoComplete="name"
                />
                <div className="patient-form-field-border" />
                {errors.fullName && (
                  <span className="patient-form-error">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none"
                      stroke="currentColor" strokeWidth="2.5"
                      strokeLinecap="round" strokeLinejoin="round">
                      <circle cx="12" cy="12" r="10" />
                      <line x1="12" y1="8" x2="12" y2="12" />
                      <line x1="12" y1="16" x2="12.01" y2="16" />
                    </svg>
                    {errors.fullName}
                  </span>
                )}
              </div>
            )}

            {/* Room Number — only in manual mode */}
            {!hisEnabled && (
              <div className={`patient-form-field 
                ${focusedField === 'roomNumber' ? 'patient-form-field-focused' : ''} 
                ${errors.roomNumber ? 'patient-form-field-error' : ''} 
                ${formData.roomNumber ? 'patient-form-field-filled' : ''}`}>
                <label className="patient-form-label" htmlFor="roomNumber">
                  <span className="patient-form-label-icon">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
                      stroke="currentColor" strokeWidth="2"
                      strokeLinecap="round" strokeLinejoin="round">
                      <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
                      <polyline points="9 22 9 12 15 12 15 22" />
                    </svg>
                  </span>
                  Room Number
                </label>
                <input
                  id="roomNumber"
                  name="roomNumber"
                  type="text"
                  className="patient-form-input"
                  placeholder="Enter your room number"
                  value={formData.roomNumber}
                  onChange={handleChange}
                  onFocus={() => setFocusedField('roomNumber')}
                  onBlur={() => setFocusedField(null)}
                  autoComplete="off"
                />
                <div className="patient-form-field-border" />
                {errors.roomNumber && (
                  <span className="patient-form-error">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none"
                      stroke="currentColor" strokeWidth="2.5"
                      strokeLinecap="round" strokeLinejoin="round">
                      <circle cx="12" cy="12" r="10" />
                      <line x1="12" y1="8" x2="12" y2="12" />
                      <line x1="12" y1="16" x2="12.01" y2="16" />
                    </svg>
                    {errors.roomNumber}
                  </span>
                )}
              </div>
            )}

            {/* Submit Button */}
            <button
              type="submit"
              className={`patient-submit-button ${isLoading ? 'patient-submit-button-loading' : ''}`}
              disabled={isLoading}
            >
              {isLoading ? (
                <span className="patient-submit-spinner" />
              ) : (
                <>
                  <span>View My Menu</span>
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
                    stroke="currentColor" strokeWidth="2"
                    strokeLinecap="round" strokeLinejoin="round">
                    <line x1="5" y1="12" x2="19" y2="12" />
                    <polyline points="12 5 19 12 12 19" />
                  </svg>
                </>
              )}
            </button>
            <br />
          </form>

          {/* Footer note */}
          <p className="patient-form-footer">
            Your information is kept private and secure.
          </p>
        </div>
      </div>

      {/* Success Popup */}
      {showSuccessPopup && (
        <div className="patient-success-popup">
          <div className="patient-success-content">
            <div className="patient-success-icon">✓</div>
            <p className="patient-success-message">Login Successful!</p>
          </div>
        </div>
      )}
    </div>
  );
};

export default PatientLogin;