// DocToolKit Service Worker for Offline PWA - v2.4.0
const CACHE_NAME = 'doctoolkit-v2.4.0';
const ASSETS_TO_CACHE = [
  './',
  './index.html',
  './404.html',
  './manifest.json',
  './favicon.ico',
  './favicon.svg',
  './apple-touch-icon.png',
  './icons/apple-touch-icon.png',
  './icons/icon-192.png',
  './icons/icon-512.png',
  './icons/favicon-16x16.png',
  './icons/favicon-32x32.png',
  './icons/doctoolkit-logo-horizontal.png',
  './icons/doctoolkit-logo-horizontal.webp',
  './icons/doctoolkit-icon-master.png',
  './icons/og-image.png',
  './icons/icon-192.webp',
  './icons/icon-512.webp',
  './css/style.css',
  './js/app.js',
  './js/pdf-lib.min.js',
  './js/pdf.min.js',
  './js/pdf.worker.min.js'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      // Force cache reload from network during installation to prevent caching stale HTTP cache
      const requests = ASSETS_TO_CACHE.map((url) => new Request(url, { cache: 'reload' }));
      return cache.addAll(requests);
    }).then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) => {
      return Promise.all(
        keys.map((key) => {
          if (key !== CACHE_NAME) {
            return caches.delete(key);
          }
        })
      );
    }).then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', (event) => {
  if (event.request.method !== 'GET') return;
  const url = event.request.url;

  // Never intercept or cache blob/data URLs or search-engine metadata
  if (url.startsWith('blob:') || url.startsWith('data:')) return;
  if (url.includes('sitemap.xml') || url.includes('robots.txt')) return;

  // Network-First for HTML navigation requests (ensures fresh metadata & SEO tags when online)
  if (event.request.mode === 'navigate') {
    event.respondWith(
      fetch(event.request)
        .then((networkResponse) => {
          if (networkResponse && networkResponse.status === 200) {
            const responseToCache = networkResponse.clone();
            caches.open(CACHE_NAME).then((cache) => {
              cache.put(event.request, responseToCache);
            });
          }
          return networkResponse;
        })
        .catch(() => {
          return caches.match(event.request).then((cached) => {
            return cached || caches.match('./index.html');
          });
        })
    );
    return;
  }

  // Cache-First for static assets (CSS, JS, images, icons, fonts)
  event.respondWith(
    caches.match(event.request).then((cachedResponse) => {
      if (cachedResponse) {
        return cachedResponse;
      }
      return fetch(event.request).then((networkResponse) => {
        if (!networkResponse || networkResponse.status !== 200 || networkResponse.type !== 'basic') {
          return networkResponse;
        }
        const responseToCache = networkResponse.clone();
        caches.open(CACHE_NAME).then((cache) => {
          cache.put(event.request, responseToCache);
        });
        return networkResponse;
      });
    })
  );
});
