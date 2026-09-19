/* EPV service worker: offline shell via stale-while-revalidate for same-origin GETs. */
const CACHE = 'epv-v1';
const SHELL = ['./', 'index.html', 'css/style.css?v=1', 'js/data.js?v=1', 'js/store.js?v=1', 'js/ui.js?v=1', 'js/engine.js?v=1',
  'js/screens-setup.js?v=1', 'js/screens-timer.js?v=1', 'js/screens-plans.js?v=1', 'js/screens-more.js?v=1', 'js/app.js?v=1',
  'manifest.webmanifest', 'icons/icon-192.png', 'icons/icon-512.png'];

self.addEventListener('install', (e) => {
  e.waitUntil(caches.open(CACHE).then((c) => c.addAll(SHELL)).then(() => self.skipWaiting()));
});
self.addEventListener('activate', (e) => {
  e.waitUntil(caches.keys().then((keys) => Promise.all(keys.filter((k) => k !== CACHE).map((k) => caches.delete(k)))).then(() => self.clients.claim()));
});
self.addEventListener('fetch', (e) => {
  const req = e.request;
  if (req.method !== 'GET' || new URL(req.url).origin !== self.location.origin) return;
  e.respondWith(caches.open(CACHE).then(async (cache) => {
    const cached = await cache.match(req, { ignoreSearch: false });
    const network = fetch(req).then((res) => { if (res && res.ok) cache.put(req, res.clone()); return res; }).catch(() => cached);
    return cached || network;
  }));
});
self.addEventListener('notificationclick', (e) => {
  e.notification.close();
  e.waitUntil(self.clients.matchAll({ type: 'window' }).then((list) => (list.length ? list[0].focus() : self.clients.openWindow('./'))));
});
