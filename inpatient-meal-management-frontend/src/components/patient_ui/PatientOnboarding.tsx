import React, { useState, useRef } from 'react';
import '../../styles/patient/patientonboarding.css';
import evercarelogo from '../../assets/icons/ec_logo.png';

import slide1Img from '../../assets/images/onboardpic1.jpeg';
import slide2Img from '../../assets/images/onboardpic2.png';
import slide3Img from '../../assets/images/onboardpic3.jpeg';

const SLIDES = [
  {
    image: slide1Img,
    title: 'Welcome to Your Ward\nDining Service',
    subtitle: 'Order your meals with ease. Thoughtfully designed to support your recovery.',
    cta: 'Continue',
  },
  {
    image: slide2Img,
    title: 'Clinically Designed\nNutrition',
    subtitle: 'Your diet plans are carefully tailored by our certified dieticians.',
    cta: 'Continue',
  },
  {
    image: slide3Img,
    title: 'Refined Dining\nOptions',
    subtitle: 'Explore our À La Carte selection.',
    cta: 'Proceed to Login',
  },
];

const PatientOnboarding: React.FC = () => {
  const [current, setCurrent]         = useState(0);
  const [isAnimating, setIsAnimating] = useState(false);
  const touchStartX = useRef<number | null>(null);
  const touchEndX   = useRef<number | null>(null);

  const goTo = (index: number) => {
    if (isAnimating || index === current) return;
    setIsAnimating(true);
    setCurrent(index);
    setTimeout(() => setIsAnimating(false), 300);
  };

  const handleContinue = () => {
    if (current < SLIDES.length - 1) {
      goTo(current + 1);
    } else {
      localStorage.setItem('hasCompletedOnboarding', 'true');
      window.location.href = '/patient/login';
    }
  };

  const handleSkip = () => {
    localStorage.setItem('hasCompletedOnboarding', 'true');
    window.location.href = '/patient/login';
  };

  const handleTouchStart = (e: React.TouchEvent) => {
    touchStartX.current = e.touches[0].clientX;
  };

  const handleTouchEnd = (e: React.TouchEvent) => {
    touchEndX.current = e.changedTouches[0].clientX;
    const delta = (touchStartX.current ?? 0) - (touchEndX.current ?? 0);
    if (Math.abs(delta) > 50) {
      if (delta > 0 && current < SLIDES.length - 1) goTo(current + 1);
      if (delta < 0 && current > 0) goTo(current - 1);
    }
  };

  const slide = SLIDES[current];
  const isLast = current === SLIDES.length - 1;

  return (
    <div
      className="ob-root"
      onTouchStart={handleTouchStart}
      onTouchEnd={handleTouchEnd}
    >
      <div className="ob-logo">
        <img src={evercarelogo} alt="Evercare Hospital" />
      </div>

      <div className="ob-slide">
        <div className="ob-image-wrap">
          <div className="ob-image-blob" />
          <img
            src={slide.image}
            alt={`Slide ${current + 1}`}
            className="ob-image"
            draggable={false}
          />
        </div>

        <div className="ob-text">
          <h1 className="ob-title">
            {slide.title.split('\n').map((line, i) => (
              <React.Fragment key={i}>
                {line}
                {i < slide.title.split('\n').length - 1 && <br />}
              </React.Fragment>
            ))}
          </h1>
          <p className="ob-subtitle">{slide.subtitle}</p>
        </div>
      </div>

      <div className="ob-bottom">
        <div className="ob-dots">
          {SLIDES.map((_, i) => (
            <button
              key={i}
              className={`ob-dot ${i === current ? 'ob-dot--active' : ''}`}
              onClick={() => goTo(i)}
              aria-label={`Go to slide ${i + 1}`}
            />
          ))}
        </div>

        <button
          className={`ob-btn ${isLast ? 'ob-btn--last' : ''}`}
          onClick={handleContinue}
          disabled={isAnimating}
        >
          <span>{slide.cta}</span>
          {!isLast && (
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" strokeWidth="2.5"
              strokeLinecap="round" strokeLinejoin="round">
              <line x1="5" y1="12" x2="19" y2="12" />
              <polyline points="12 5 19 12 12 19" />
            </svg>
          )}
          {isLast && (
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
              stroke="currentColor" strokeWidth="2.5"
              strokeLinecap="round" strokeLinejoin="round">
              <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4" />
              <polyline points="10 17 15 12 10 7" />
              <line x1="15" y1="12" x2="3" y2="12" />
            </svg>
          )}
        </button>

        {/* Skip — below CTA, hidden on last slide */}
        {!isLast && (
          <button className="ob-skip" onClick={handleSkip}>
            Skip
          </button>
        )}
      </div>
    </div>
  );
};

export default PatientOnboarding;