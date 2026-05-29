// ============================================================
// usePushNotifications.ts — Evercare Hospital
// Place this file in: src/hooks/usePushNotifications.ts
// ============================================================

import { useEffect, useRef } from 'react';
import axios from 'axios';
import { config } from '../config/config';

// Your VAPID public key from application.properties
const VAPID_PUBLIC_KEY =
  'BLNCeTzGxo77Hfhflt8h24Pzk68fYUtxZNpiC2sAbOu-9WZ-l7vaYfQ_QCAqmyy1F4FQ1aH2AfcclJMx4tARVNo';

export const usePushNotifications = (tokenKey: string): void => {
  const subscribed = useRef(false);

  useEffect(() => {
    if (subscribed.current) return;
    subscribed.current = true;
    registerAndSubscribe(tokenKey);
  }, [tokenKey]);
};

const registerAndSubscribe = async (tokenKey: string): Promise<void> => {
  try {
    if (!('serviceWorker' in navigator) || !('PushManager' in window)) {
      console.warn('[Push] Web push not supported in this browser.');
      return;
    }

    const registration = await navigator.serviceWorker.register('/sw.js', { scope: '/' });
    await navigator.serviceWorker.ready;

    const permission = await Notification.requestPermission();
    if (permission !== 'granted') {
      console.warn('[Push] Notification permission denied.');
      return;
    }

    const existingSubscription = await registration.pushManager.getSubscription();
    if (existingSubscription) {
      await sendSubscriptionToBackend(existingSubscription, tokenKey);
      return;
    }

    // Pass VAPID key as plain string — browser handles decoding natively
    const subscription = await registration.pushManager.subscribe({
      userVisibleOnly:      true,
      applicationServerKey: VAPID_PUBLIC_KEY,
    });

    await sendSubscriptionToBackend(subscription, tokenKey);
    console.log('[Push] Successfully subscribed to push notifications.');
  } catch (err) {
    console.error('[Push] Failed to subscribe:', err);
  }
};

const sendSubscriptionToBackend = async (
  subscription: PushSubscription,
  tokenKey: string
): Promise<void> => {
  const token = localStorage.getItem(tokenKey);
  if (!token) return;

  const p256dhKey = btoa(
    String.fromCharCode(...new Uint8Array(subscription.getKey('p256dh')!))
  );
  const authKey = btoa(
    String.fromCharCode(...new Uint8Array(subscription.getKey('auth')!))
  );

  await axios.post(
    `${config.apiUrl}/notifications/subscribe`,
    { endpoint: subscription.endpoint, p256dhKey, authKey },
    { headers: { Authorization: `Bearer ${token}` } }
  );
};