// DocToolKit Client-Side Engine v2.1.0
// 100% In-Browser Privacy • WebAssembly & Canvas Processing

(function() {
  'use strict';

  // 1. Service Worker Registration
  if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
      const swPath = window.location.pathname.includes('/tools/') || window.location.pathname.includes('/privacy/') || window.location.pathname.includes('/terms/') ? '../../sw.js' : './sw.js';
      navigator.serviceWorker.register(swPath)
        .then(reg => console.log('DocToolKit PWA active, scope:', reg.scope))
        .catch(err => console.log('SW registration note:', err));
    });
  }

  // 2. Configure PDF.js Worker
  if (window.pdfjsLib) {
    const isSubdir = window.location.pathname.includes('/tools/') || window.location.pathname.includes('/privacy/') || window.location.pathname.includes('/terms/');
    pdfjsLib.GlobalWorkerOptions.workerSrc = isSubdir ? '../../js/pdf.worker.min.js' : 'js/pdf.worker.min.js';
  }

  // 3. User Preferences & Storage
  function getPref(key, def) {
    return localStorage.getItem('doctoolkit_' + key) || localStorage.getItem('bharatfile_' + key) || def;
  }
  function setPref(key, val) {
    localStorage.setItem('doctoolkit_' + key, val);
  }

  window.DocToolKit = {
    getPref,
    setPref,
    userProfile: {
      name: getPref('username', 'User'),
      defaultFormat: getPref('format', 'jpeg'),
      defaultComp: getPref('comp', '60')
    }
  };

  // 4. Theme Management
  window.initTheme = function() {
    const saved = getPref('theme', null);
    const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
    const isDark = saved === 'dark' || (!saved && prefersDark);
    if (isDark) {
      document.body.classList.add('dark-mode');
    } else {
      document.body.classList.remove('dark-mode');
    }
    const btns = document.querySelectorAll('.theme-toggle-btn');
    btns.forEach(b => b.textContent = isDark ? '☀️' : '🌙');
  };

  window.toggleTheme = function() {
    document.body.classList.toggle('dark-mode');
    const isDark = document.body.classList.contains('dark-mode');
    setPref('theme', isDark ? 'dark' : 'light');
    const btns = document.querySelectorAll('.theme-toggle-btn');
    btns.forEach(b => b.textContent = isDark ? '☀️' : '🌙');
  };

  // 5. Drawer Controls
  window.openDrawer = function() {
    const d = document.getElementById('drawer');
    const b = document.getElementById('drawerBackdrop');
    if (d) d.classList.add('open');
    if (b) b.classList.add('open');
  };

  window.closeDrawer = function() {
    const d = document.getElementById('drawer');
    const b = document.getElementById('drawerBackdrop');
    if (d) d.classList.remove('open');
    if (b) b.classList.remove('open');
  };

  // 6. Utility Functions
  window.formatBytes = function(bytes) {
    if (!bytes || bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
  };

  // Mandatory Smart Export Filename Rule: [Original]_[Profile]_[Action].[ext]
  window.generateExportFilename = function(origName, profileName, actionSuffix, ext) {
    const cleanProfile = (profileName || DocToolKit.userProfile.name || 'User').trim().replace(/[^a-zA-Z0-9_-]/g, '');
    let base = origName.substring(0, origName.lastIndexOf('.')) || origName;
    base = base.replace(/[^a-zA-Z0-9_-]/g, '_');
    const cleanExt = (ext || 'pdf').replace(/^\./, '');
    return `${base}_${cleanProfile}_${actionSuffix}.${cleanExt}`;
  };

  // 7. IndexedDB Document Organizer Storage (DocToolKitDB)
  let dbPromise = null;
  window.openDocDB = function() {
    if (dbPromise) return dbPromise;
    dbPromise = new Promise((resolve, reject) => {
      const req = indexedDB.open('DocToolKitDB', 1);
      req.onupgradeneeded = (e) => {
        const d = e.target.result;
        if (!d.objectStoreNames.contains('documents')) {
          const store = d.createObjectStore('documents', { keyPath: 'id', autoIncrement: true });
          store.createIndex('name', 'name', { unique: false });
          store.createIndex('date', 'date', { unique: false });
          store.createIndex('type', 'type', { unique: false });
        }
      };
      req.onsuccess = (e) => resolve(e.target.result);
      req.onerror = (e) => reject(e.target.error);
    });
    return dbPromise;
  };

  window.saveDocumentToDB = async function(doc) {
    try {
      const db = await openDocDB();
      const tx = db.transaction('documents', 'readwrite');
      const store = tx.objectStore('documents');
      doc.date = doc.date || new Date().toISOString();
      store.add(doc);
      return new Promise((resolve, reject) => {
        tx.oncomplete = () => resolve(true);
        tx.onerror = () => reject(tx.error);
      });
    } catch (e) {
      console.warn('Document storage notice:', e);
      return false;
    }
  };

  window.getAllDocumentsFromDB = async function() {
    try {
      const db = await openDocDB();
      const tx = db.transaction('documents', 'readonly');
      const store = tx.objectStore('documents');
      const req = store.getAll();
      return new Promise((resolve, reject) => {
        req.onsuccess = () => resolve(req.result || []);
        req.onerror = () => reject(req.error);
      });
    } catch (e) {
      return [];
    }
  };

  window.deleteDocumentFromDB = async function(id) {
    try {
      const db = await openDocDB();
      const tx = db.transaction('documents', 'readwrite');
      tx.objectStore('documents').delete(id);
      return new Promise(resolve => { tx.oncomplete = () => resolve(true); });
    } catch (e) {
      return false;
    }
  };

  // 8. Download Helper
  window.triggerDownload = function(blob, filename) {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    setTimeout(() => {
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    }, 2000);
  };

  // Initialize Theme on DOM Ready
  document.addEventListener('DOMContentLoaded', () => {
    initTheme();
    const closeBtn = document.getElementById('closeDrawerBtn');
    if (closeBtn) closeBtn.addEventListener('click', closeDrawer);
    const backdrop = document.getElementById('drawerBackdrop');
    if (backdrop) backdrop.addEventListener('click', closeDrawer);
    const hb = document.getElementById('hamburgerBtn');
    if (hb) hb.addEventListener('click', openDrawer);
  });

})();
