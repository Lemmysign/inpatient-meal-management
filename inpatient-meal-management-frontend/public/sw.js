// ============================================================
// Service Worker — Evercare Hospital Meal Ordering
// Place this file in: public/sw.js
// ============================================================

self.addEventListener('push', (event) => {
  let data = {};
  try {
    data = event.data?.json() ?? {};
  } catch {
    data = { title: 'New Notification', body: event.data?.text() ?? '' };
  }

  event.waitUntil(
    self.registration.showNotification(data.title || 'New Order', {
      body:             data.body || 'A new meal order has been placed.',
      icon:             '/ec_logo.png',
      badge:            '/ec_logo.png',
      vibrate:          [200, 100, 200, 100, 200],
      tag:              'meal-notification',
      requireInteraction: true,
      data:             { url: data.data?.url || '/kitchen/dashboard' },
    })
  );
});

self.addEventListener('notificationclick', (event) => {
  event.notification.close();
  const targetUrl = event.notification.data?.url || '/kitchen/dashboard';

  event.waitUntil(
    clients.matchAll({ type: 'window', includeUncontrolled: true }).then((clientList) => {
      // If a kitchen tab is already open, focus it
      for (const client of clientList) {
        if (client.url.includes('/kitchen') && 'focus' in client) {
          return client.focus();
        }
      }
      // Otherwise open a new tab
      if (clients.openWindow) {
        return clients.openWindow(targetUrl);
      }
    })
  );
});

self.addEventListener('install', () => self.skipWaiting());
self.addEventListener('activate', (event) => {
  event.waitUntil(clients.claim());
});