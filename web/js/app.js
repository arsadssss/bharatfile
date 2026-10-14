// DocToolKit Client-Side Engine v2.1.0
// 100% In-Browser Privacy • WebAssembly & Canvas Processing

(function() {
  'use strict';

  // 1. Service Worker Registration
  if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
      let swPath = '/sw.js';
      if (!window.location.origin.includes('doctoolkit.in')) {
        const path = window.location.pathname;
        if (path.includes('/tools/')) swPath = '../../sw.js';
        else if (path.match(/\/(about|contact|privacy|terms)\//)) swPath = '../sw.js';
        else swPath = './sw.js';
      }
      navigator.serviceWorker.register(swPath)
        .then(reg => console.log('DocToolKit PWA active, scope:', reg.scope))
        .catch(err => console.log('SW registration note:', err));
    });
  }

  // 2. Dynamic PDF Libraries Loader (load on-demand only when tools require it)
  function initPdfWorker() {
    if (window.pdfjsLib && (!pdfjsLib.GlobalWorkerOptions || !pdfjsLib.GlobalWorkerOptions.workerSrc)) {
      let workerSrc = '/js/pdf.worker.min.js';
      if (!window.location.origin.includes('doctoolkit.in')) {
        const path = window.location.pathname;
        if (path.includes('/tools/')) workerSrc = '../../js/pdf.worker.min.js';
        else if (path.match(/\/(about|contact|privacy|terms)\//)) workerSrc = '../js/pdf.worker.min.js';
        else workerSrc = 'js/pdf.worker.min.js';
      }
      pdfjsLib.GlobalWorkerOptions.workerSrc = workerSrc;
    }
  }

  window.loadPdfLibraries = function() {
    if (window._pdfLibPromise) return window._pdfLibPromise;
    window._pdfLibPromise = new Promise((resolve, reject) => {
      if (window.PDFLib && window.pdfjsLib) {
        initPdfWorker();
        return resolve();
      }

      let basePath = 'js/';
      const path = window.location.pathname;
      if (path.includes('/tools/')) basePath = '../../js/';
      else if (path.match(/\/(about|contact|privacy|terms)\//)) basePath = '../js/';

      function loadScript(src) {
        return new Promise((res, rej) => {
          const existing = document.querySelector(`script[src*="${src}"]`);
          if (existing) {
            if (existing.getAttribute('data-loaded') === 'true' || window.PDFLib || window.pdfjsLib) return res();
            existing.addEventListener('load', () => res());
            existing.addEventListener('error', rej);
            return;
          }
          const s = document.createElement('script');
          s.src = basePath + src;
          s.defer = true;
          s.onload = () => { s.setAttribute('data-loaded', 'true'); res(); };
          s.onerror = (e) => rej(new Error('Failed to load dependency: ' + src));
          document.head.appendChild(s);
        });
      }

      Promise.all([
        loadScript('pdf-lib.min.js'),
        loadScript('pdf.min.js')
      ]).then(() => {
        initPdfWorker();
        resolve();
      }).catch(reject);
    });
    return window._pdfLibPromise;
  };

  if (window.pdfjsLib) {
    initPdfWorker();
  }

  // 3. User Preferences & Storage
  function getPref(key, def) {
    return localStorage.getItem('doctoolkit_' + key) || localStorage.getItem('bharatfile_' + key) || def;
  }
  function setPref(key, val) {
    localStorage.setItem('doctoolkit_' + key, val);
  }

  // Recent Activity Local Storage Manager
  const recentActivityManager = {
    get() {
      try {
        return JSON.parse(localStorage.getItem('doctoolkit_recent_activity') || '[]');
      } catch (e) {
        return [];
      }
    },
    add(item) {
      try {
        const list = recentActivityManager.get();
        list.unshift({
          id: Date.now(),
          name: item.name,
          origSize: item.origSize,
          newSize: item.newSize,
          savedPct: item.savedPct,
          type: item.type || 'file',
          targetAchieved: item.targetAchieved !== false,
          targetSize: item.targetSize || null,
          timestamp: new Date().toISOString()
        });
        localStorage.setItem('doctoolkit_recent_activity', JSON.stringify(list.slice(0, 10)));
      } catch (e) {
        console.warn('Recent activity store note:', e);
      }
    },
    clear() {
      localStorage.removeItem('doctoolkit_recent_activity');
    }
  };

  function parseTargetBytes(val, unit) {
    const num = parseFloat(val);
    if (isNaN(num) || num <= 0) return null;
    if ((unit || '').toUpperCase() === 'MB') return Math.round(num * 1024 * 1024);
    return Math.round(num * 1024);
  }

  function timeAgo(dateInput) {
    if (!dateInput) return 'Recently';
    const d = new Date(dateInput);
    const secs = Math.floor((Date.now() - d.getTime()) / 1000);
    if (secs < 60) return 'Just now';
    const mins = Math.floor(secs / 60);
    if (mins < 60) return mins + 'm ago';
    const hrs = Math.floor(mins / 60);
    if (hrs < 24) return hrs + 'h ago';
    const days = Math.floor(hrs / 24);
    return days + 'd ago';
  }

  function pluralize(count, singular, plural) {
    const num = Math.abs(count || 0);
    const word = num === 1 ? singular : (plural || singular + 's');
    return `${count} ${word}`;
  }

  function stripExtension(filename) {
    if (!filename) return '';
    const lastDot = filename.lastIndexOf('.');
    if (lastDot <= 0) return filename;
    return filename.substring(0, lastDot);
  }

  window.DocToolKit = {
    version: '2.8.0',
    getPref,
    setPref,
    parseTargetBytes,
    timeAgo,
    pluralize,
    stripExtension,
    recentActivity: recentActivityManager,
    userProfile: {
      name: getPref('username', 'User'),
      defaultFormat: getPref('format', 'jpeg'),
      defaultComp: getPref('comp', '60')
    },
    // Google Analytics 4 (GA4) Tracking Integration (Measurement ID: G-FQQFMTFKSF)
    trackEvent(eventName, params = {}) {
      try {
        if (!eventName) return;
        const toolName = params.tool_name || DocToolKit.getCurrentToolName();
        const dedupKey = `${eventName}:${toolName}`;
        const now = Date.now();
        DocToolKit._lastEvents = DocToolKit._lastEvents || {};
        if (now - (DocToolKit._lastEvents[dedupKey] || 0) < 400) return;
        DocToolKit._lastEvents[dedupKey] = now;

        if (typeof window.gtag === 'function') {
          window.gtag('event', eventName, { tool_name: toolName });
        }
      } catch (err) {}
    },
    getCurrentToolName() {
      try {
        const path = window.location.pathname;
        const match = path.match(/\/tools\/([^\/]+)/);
        if (match && match[1]) return match[1];
        if (path === '/' || path.endsWith('/index.html') || !path) return 'homepage-quick-mode';
        return path.replace(/^\/|\/$/g, '').replace(/\//g, '_') || 'home';
      } catch (e) {
        return 'doctoolkit';
      }
    },
    trackFileSelected(toolName) {
      DocToolKit.trackEvent('tool_file_selected', { tool_name: toolName || DocToolKit.getCurrentToolName() });
    },
    trackProcessingStarted(toolName) {
      DocToolKit.trackEvent('tool_processing_started', { tool_name: toolName || DocToolKit.getCurrentToolName() });
    },
    trackProcessingCompleted(toolName) {
      DocToolKit.trackEvent('tool_processing_completed', { tool_name: toolName || DocToolKit.getCurrentToolName() });
    },
    trackProcessingFailed(toolName) {
      DocToolKit.trackEvent('tool_processing_failed', { tool_name: toolName || DocToolKit.getCurrentToolName() });
    },
    trackFileDownloaded(toolName) {
      DocToolKit.trackEvent('tool_file_downloaded', { tool_name: toolName || DocToolKit.getCurrentToolName() });
    },
    // Standardized UI Result & Feedback Component
    showProcessing(containerId, actionText, subText) {
      DocToolKit.trackProcessingStarted();
      const container = typeof containerId === 'string' ? document.getElementById(containerId) : containerId;
      if (!container) return;
      container.className = 'result-feedback-card';
      container.style.display = 'flex';
      container.innerHTML = `
        <div class="result-header-row">
          <div class="result-icon-circle processing">⚡</div>
          <div class="result-title-wrap">
            <h4>${actionText || 'Processing...'}</h4>
            <p>${subText || 'Executing client-side in browser memory...'}</p>
          </div>
        </div>
        <div class="progress-bar-wrap"><div class="progress-bar-fill" style="width: 75%; animation: pulse 1s infinite alternate;"></div></div>
      `;
    },
    showError(containerId, title, message, onRetry) {
      DocToolKit.trackProcessingFailed();
      const container = typeof containerId === 'string' ? document.getElementById(containerId) : containerId;
      if (!container) return;
      container.className = 'result-feedback-card';
      container.style.display = 'flex';
      container.innerHTML = `
        <div class="result-header-row">
          <div class="result-icon-circle error">⚠️</div>
          <div class="result-title-wrap">
            <h4>${title || 'Processing Error'}</h4>
            <p>${message || 'An error occurred while processing the file.'}</p>
          </div>
        </div>
        <div class="result-actions-row">
          <button class="btn-secondary-action" type="button" id="btnRetryFeedbackOp">🔄 Try Again</button>
        </div>
      `;
      const btn = container.querySelector('#btnRetryFeedbackOp');
      if (btn) {
        btn.onclick = () => {
          if (onRetry) onRetry();
          else DocToolKit.hideResult(containerId);
        };
      }
    },
    showResult(containerId, options = {}) {
      DocToolKit.trackProcessingCompleted();
      const container = typeof containerId === 'string' ? document.getElementById(containerId) : containerId;
      if (!container) return;
      const title = options.title || 'Operation Completed Successfully';
      const subtitle = options.subtitle || options.subTitle || 'Output generated locally. Click download below.';
      const filename = options.filename || options.defaultFilename || 'download';
      const blob = options.blob;
      const origSize = options.origSize;
      const newSize = options.newSize || (blob ? blob.size : 0);
      const savedPct = typeof options.savedPct === 'number' ? options.savedPct : (origSize && newSize ? Math.round(((origSize - newSize) / origSize) * 100) : 0);
      const meta = options.meta || [];
      const ctaLabel = options.ctaLabel || 'Download Output';
      const onReset = options.onReset;
      const resetLabel = options.resetLabel || 'Process Another File';

      let metaItemsHtml = '';
      if (origSize) {
        metaItemsHtml += `
          <div class="result-meta-item">
            <span class="result-meta-label">Original</span>
            <span class="result-meta-value">${formatBytes(origSize)}</span>
          </div>
        `;
      }
      if (newSize) {
        metaItemsHtml += `
          <div class="result-meta-item">
            <span class="result-meta-label">Output</span>
            <span class="result-meta-value" style="color:var(--success);">${formatBytes(newSize)}</span>
          </div>
        `;
      }
      if (typeof savedPct === 'number' && savedPct > 0) {
        metaItemsHtml += `
          <div class="result-meta-item">
            <span class="result-meta-label">Saved</span>
            <span class="result-meta-value" style="color:var(--success);">${savedPct}%</span>
          </div>
        `;
      }
      if (Array.isArray(meta)) {
        meta.forEach(m => {
          metaItemsHtml += `
            <div class="result-meta-item">
              <span class="result-meta-label">${m.label}</span>
              <span class="result-meta-value">${m.value}</span>
            </div>
          `;
        });
      }

      container.className = 'result-feedback-card';
      container.style.display = 'flex';
      container.innerHTML = `
        <div class="result-header-row">
          <div class="result-icon-circle">✓</div>
          <div class="result-title-wrap">
            <h4>${title}</h4>
            <p>${subtitle}</p>
          </div>
        </div>

        ${metaItemsHtml ? `<div class="result-meta-grid">${metaItemsHtml}</div>` : ''}

        <div style="width:100%;">
          <label style="font-size:12px; font-weight:700; color:var(--text-muted); display:block; margin-bottom:4px;">Customize Filename:</label>
          <input type="text" class="rename-input" id="resultFeedbackNameInput" value="${filename || 'download'}">
        </div>

        <div class="result-actions-row">
          <button class="btn-download-primary" type="button" id="btnFeedbackDownloadPrimary">
            <span>⬇</span>
            <span>${ctaLabel}</span>
          </button>
          <button class="btn-secondary-action" type="button" id="btnFeedbackResetAction">
            <span>🔄</span>
            <span>${resetLabel}</span>
          </button>
        </div>
        <div class="result-download-confirm" id="feedbackDownloadConfirm">✓ Download started! Check your browser downloads.</div>
      `;

      const btnDl = container.querySelector('#btnFeedbackDownloadPrimary');
      const btnReset = container.querySelector('#btnFeedbackResetAction');
      const confirmMsg = container.querySelector('#feedbackDownloadConfirm');
      const nameInput = container.querySelector('#resultFeedbackNameInput');

      if (btnDl && blob) {
        btnDl.onclick = () => {
          const finalName = (nameInput ? nameInput.value.trim() : '') || filename || 'download';
          triggerDownload(blob, finalName);
          if (confirmMsg) {
            confirmMsg.style.display = 'inline-flex';
            setTimeout(() => { if (confirmMsg) confirmMsg.style.display = 'none'; }, 4000);
          }
        };
      }
      if (btnReset && onReset) {
        btnReset.onclick = onReset;
      }
    },
    hideResult(containerId) {
      const container = typeof containerId === 'string' ? document.getElementById(containerId) : containerId;
      if (!container) return;
      container.style.display = 'none';
      container.innerHTML = '';
    }
  };

  // Expose global helpers
  window.pluralize = pluralize;
  window.stripExtension = stripExtension;

  // 4. Theme Management
  window.initTheme = function() {
    const saved = getPref('theme', null);
    const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
    const isDark = saved === 'dark' || (!saved && prefersDark);
    if (isDark) {
      document.body.classList.add('dark-mode');
      document.documentElement.setAttribute('data-theme', 'dark');
    } else {
      document.body.classList.remove('dark-mode');
      document.documentElement.setAttribute('data-theme', 'light');
    }
    const btns = document.querySelectorAll('.theme-toggle-btn');
    btns.forEach(b => {
      b.textContent = isDark ? '☀️' : '🌙';
      b.setAttribute('aria-label', isDark ? 'Switch to Light Mode' : 'Switch to Dark Mode');
      b.setAttribute('title', isDark ? 'Switch to Light Mode' : 'Switch to Dark Mode');
    });
  };

  window.toggleTheme = function() {
    document.body.classList.toggle('dark-mode');
    const isDark = document.body.classList.contains('dark-mode');
    document.documentElement.setAttribute('data-theme', isDark ? 'dark' : 'light');
    setPref('theme', isDark ? 'dark' : 'light');
    const btns = document.querySelectorAll('.theme-toggle-btn');
    btns.forEach(b => {
      b.textContent = isDark ? '☀️' : '🌙';
      b.setAttribute('aria-label', isDark ? 'Switch to Light Mode' : 'Switch to Dark Mode');
      b.setAttribute('title', isDark ? 'Switch to Light Mode' : 'Switch to Dark Mode');
    });
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
    let base = stripExtension(origName);
    base = base.replace(/[^a-zA-Z0-9_-]/g, '_');
    const cleanExt = (ext || 'pdf').replace(/^\./, '');
    return `${base}_${cleanProfile}_${actionSuffix}.${cleanExt}`;
  };

  // 7. IndexedDB Document Organizer Storage (DocToolKitDB) with Safety Timeout
  let dbPromise = null;
  window.openDocDB = function() {
    if (dbPromise) return dbPromise;
    dbPromise = new Promise((resolve) => {
      let settled = false;
      const timeout = setTimeout(() => {
        if (!settled) {
          settled = true;
          console.warn('DocToolKitDB open timed out; falling back gracefully.');
          resolve(null);
        }
      }, 2500);

      try {
        if (!window.indexedDB) {
          clearTimeout(timeout);
          settled = true;
          return resolve(null);
        }
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
        req.onsuccess = (e) => {
          if (!settled) {
            settled = true;
            clearTimeout(timeout);
            resolve(e.target.result);
          }
        };
        req.onerror = (e) => {
          if (!settled) {
            settled = true;
            clearTimeout(timeout);
            console.warn('IndexedDB open error:', e.target.error);
            resolve(null);
          }
        };
      } catch (err) {
        if (!settled) {
          settled = true;
          clearTimeout(timeout);
          resolve(null);
        }
      }
    }).then(db => {
      if (!db) dbPromise = null;
      return db;
    });
    return dbPromise;
  };

  function saveToSessionFallback(doc) {
    try {
      const items = JSON.parse(sessionStorage.getItem('doctoolkit_session_docs') || '[]');
      items.unshift({
        id: doc.id || Date.now(),
        name: doc.name,
        origSize: doc.origSize,
        size: doc.size,
        type: doc.type,
        date: doc.date || new Date().toISOString()
      });
      sessionStorage.setItem('doctoolkit_session_docs', JSON.stringify(items.slice(0, 25)));
    } catch (e) {}
  }

  window.saveDocumentToDB = async function(doc) {
    saveToSessionFallback(doc);
    try {
      const db = await openDocDB();
      if (!db) return false;
      const tx = db.transaction('documents', 'readwrite');
      const store = tx.objectStore('documents');
      doc.date = doc.date || new Date().toISOString();
      store.add(doc);
      return new Promise((resolve) => {
        tx.oncomplete = () => resolve(true);
        tx.onerror = () => resolve(false);
      });
    } catch (e) {
      console.warn('Document storage notice:', e);
      return false;
    }
  };

  window.getAllDocumentsFromDB = async function() {
    try {
      const db = await openDocDB();
      if (db) {
        const tx = db.transaction('documents', 'readonly');
        const store = tx.objectStore('documents');
        const req = store.getAll();
        const docs = await new Promise((resolve) => {
          req.onsuccess = () => resolve(req.result || []);
          req.onerror = () => resolve(null);
        });
        if (docs !== null) return docs;
      }
    } catch (e) {}

    try {
      const sessionDocs = JSON.parse(sessionStorage.getItem('doctoolkit_session_docs') || '[]');
      return sessionDocs;
    } catch (e) {
      return [];
    }
  };

  window.deleteDocumentFromDB = async function(id) {
    try {
      const items = JSON.parse(sessionStorage.getItem('doctoolkit_session_docs') || '[]');
      const filtered = items.filter(d => d.id !== id);
      sessionStorage.setItem('doctoolkit_session_docs', JSON.stringify(filtered));
    } catch (e) {}

    try {
      const db = await openDocDB();
      if (!db) return true;
      const tx = db.transaction('documents', 'readwrite');
      tx.objectStore('documents').delete(id);
      return new Promise(resolve => {
        tx.oncomplete = () => resolve(true);
        tx.onerror = () => resolve(false);
      });
    } catch (e) {
      return false;
    }
  };

  // 8. Download Helper
  window.triggerDownload = function(blob, filename) {
    if (window.DocToolKit && typeof DocToolKit.trackFileDownloaded === 'function') {
      DocToolKit.trackFileDownloaded();
    }
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

  // 9. Client-Side Image Compression Engine (Target Size & Level Modes)
  window.compressImageEngine = async function(file, opts) {
    if (!file) throw new Error('No file provided for compression.');

    const fileNameLower = (file.name || '').toLowerCase();
    const fileType = (file.type || '').toLowerCase();

    // Check for PDF input
    if (fileType === 'application/pdf' || fileNameLower.endsWith('.pdf')) {
      throw new Error('PDF files cannot be compressed as images. Please use the Compress PDF tool.');
    }

    // Check for valid image extensions or MIME types
    const validImageExts = ['.jpg', '.jpeg', '.png', '.webp', '.bmp', '.gif', '.svg'];
    const hasValidExt = validImageExts.some(ext => fileNameLower.endsWith(ext));
    if (fileType && !fileType.startsWith('image/') && !hasValidExt) {
      throw new Error('Unsupported file format. Please upload a valid image file (JPEG, PNG, WebP, GIF, or BMP).');
    }

    // Inspect first 8 bytes for PDF magic bytes (%PDF)
    try {
      const slice = file.slice(0, 8);
      const buffer = await slice.arrayBuffer();
      const bytes = new Uint8Array(buffer);
      if (bytes.length >= 4 && bytes[0] === 0x25 && bytes[1] === 0x50 && bytes[2] === 0x44 && bytes[3] === 0x46) {
        throw new Error('PDF files cannot be compressed as images. Please use the Compress PDF tool.');
      }
    } catch (e) {
      if (e.message && e.message.includes('PDF files cannot be compressed')) throw e;
    }

    const origSize = file.size;
    const mode = (opts && opts.mode) || 'target'; // 'target' or 'slider'
    const targetBytes = opts && opts.targetBytes ? opts.targetBytes : null;
    const sliderPct = opts && typeof opts.sliderPct === 'number' ? opts.sliderPct : 60;
    const formatPref = (opts && opts.format) || 'original'; // 'original', 'jpeg', 'png', 'webp'
    const onProgress = (opts && opts.onProgress) || (() => {});

    onProgress(15, 'Reading image file...');

    const img = new Image();
    const objUrl = URL.createObjectURL(file);
    await new Promise((res, rej) => {
      let settled = false;
      const timeout = setTimeout(() => {
        if (!settled) {
          settled = true;
          rej(new Error('Image decoding timed out. The file may be corrupted or in an unsupported format.'));
        }
      }, 8000);
      img.onload = () => {
        if (!settled) { settled = true; clearTimeout(timeout); res(); }
      };
      img.onerror = () => {
        if (!settled) { settled = true; clearTimeout(timeout); rej(new Error('Failed to decode image file. File may be corrupted or unsupported.')); }
      };
      img.src = objUrl;
    });
    URL.revokeObjectURL(objUrl);

    onProgress(35, 'Analyzing image dimensions...');

    const origWidth = img.naturalWidth || img.width;
    const origHeight = img.naturalHeight || img.height;
    if (!origWidth || !origHeight) {
      throw new Error('Invalid image dimensions.');
    }
    if (origWidth > 16384 || origHeight > 16384 || (origWidth * origHeight > 100000000)) {
      throw new Error('Image dimensions exceed browser processing limits (100 MP). Please select a smaller image.');
    }

    // Determine target format strictly based on user selection or original file
    let targetMime = 'image/jpeg';
    let ext = 'jpg';

    if (formatPref === 'png') {
      targetMime = 'image/png';
      ext = 'png';
    } else if (formatPref === 'webp') {
      targetMime = 'image/webp';
      ext = 'webp';
    } else if (formatPref === 'jpeg' || formatPref === 'jpg') {
      targetMime = 'image/jpeg';
      ext = 'jpg';
    } else {
      // 'original' or auto
      if (file.type === 'image/webp') {
        targetMime = 'image/webp';
        ext = 'webp';
      } else if (file.type === 'image/png') {
        targetMime = 'image/png';
        ext = 'png';
      } else {
        targetMime = 'image/jpeg';
        ext = 'jpg';
      }
    }

    const canvas = document.createElement('canvas');
    canvas.width = origWidth;
    canvas.height = origHeight;
    const ctx = canvas.getContext('2d');

    // If saving as JPEG, fill white background behind transparency
    if (targetMime === 'image/jpeg') {
      ctx.fillStyle = '#FFFFFF';
      ctx.fillRect(0, 0, origWidth, origHeight);
    }
    ctx.drawImage(img, 0, 0, origWidth, origHeight);

    let finalBlob = null;
    let achieved = true;
    let statusMessage = '';

    if (targetMime === 'image/png') {
      // PNG is lossless in standard canvas.toBlob
      onProgress(60, 'Encoding lossless PNG...');
      finalBlob = await new Promise(r => canvas.toBlob(r, 'image/png'));
      if (finalBlob && finalBlob.size > origSize) {
        achieved = false;
        statusMessage = `PNG format is lossless. Re-encoded output (${formatBytes(finalBlob.size)}) is larger than original file (${formatBytes(origSize)}). Consider converting to WebP or JPEG for smaller file size.`;
      } else if (mode === 'target' && targetBytes && finalBlob && finalBlob.size > targetBytes) {
        achieved = false;
        statusMessage = `PNG format is lossless. Standard PNG compression reached ${formatBytes(finalBlob.size)}. To achieve smaller target file sizes (such as ${formatBytes(targetBytes)}), consider selecting WebP or JPEG format.`;
      } else {
        achieved = true;
        statusMessage = `Saved as PNG with lossless quality and transparency preserved (${formatBytes(finalBlob.size)}).`;
      }
    } else if (mode === 'slider') {
      onProgress(60, 'Applying compression curves...');
      const quality = Math.max(0.08, Math.min(0.96, (100 - sliderPct * 0.90) / 100));
      finalBlob = await new Promise(r => canvas.toBlob(r, targetMime, quality));
      achieved = true;
      statusMessage = `Compressed at ${sliderPct}% compression level in ${ext.toUpperCase()} format.`;
    } else {
      // Target File Size Mode: Controlled Precision Search for Highest Quality <= Target
      onProgress(45, 'Calibrating quality to achieve requested target size...');

      if (targetBytes >= origSize) {
        const candidate = await new Promise(r => canvas.toBlob(r, targetMime, 0.95));
        if (candidate && candidate.size <= targetBytes) {
          finalBlob = candidate;
          achieved = true;
          statusMessage = `Target size (${formatBytes(targetBytes)}) is greater than or equal to original size. File saved at maximum quality (${formatBytes(finalBlob.size)}).`;
        }
      }

      if (!finalBlob) {
        let workingCanvas = canvas;
        let workingW = origWidth;
        let workingH = origHeight;

        // Test lowest quality floor at 100% resolution
        const floorBlob = await new Promise(r => workingCanvas.toBlob(r, targetMime, 0.05));

        // If even at lowest quality floor (0.05) the full-resolution image exceeds target,
        // pre-scale dimensions smoothly so the file can physically fit inside the target budget
        if (floorBlob && floorBlob.size > targetBytes) {
          onProgress(50, 'Scaling resolution to satisfy target file size...');
          const scale = Math.max(0.20, Math.min(0.95, Math.sqrt((targetBytes * 0.95) / floorBlob.size)));
          workingW = Math.max(80, Math.round(origWidth * scale));
          workingH = Math.max(80, Math.round(origHeight * scale));
          workingCanvas = document.createElement('canvas');
          workingCanvas.width = workingW;
          workingCanvas.height = workingH;
          const wCtx = workingCanvas.getContext('2d');
          if (targetMime === 'image/jpeg') {
            wCtx.fillStyle = '#FFFFFF';
            wCtx.fillRect(0, 0, workingW, workingH);
          }
          wCtx.drawImage(img, 0, 0, workingW, workingH);
        }

        // Precision 9-Iteration Binary Search for Highest Quality <= targetBytes
        let lowQ = 0.05;
        let highQ = 0.98;
        let bestBlob = null;
        let bestQuality = 0.05;

        // Check if highQ already produces output <= targetBytes
        const highCandidate = await new Promise(r => workingCanvas.toBlob(r, targetMime, highQ));
        if (highCandidate && highCandidate.size <= targetBytes) {
          bestBlob = highCandidate;
          bestQuality = highQ;
        } else {
          // Binary search bounded within [lowQ, highQ]
          for (let iter = 0; iter < 9; iter++) {
            const midQ = (lowQ + highQ) / 2;
            const testBlob = await new Promise(r => workingCanvas.toBlob(r, targetMime, midQ));
            if (!testBlob) break;

            if (testBlob.size <= targetBytes) {
              bestBlob = testBlob;
              bestQuality = midQ;
              // If within 2KB of target, exceptionally close
              if (testBlob.size >= targetBytes - 2048) {
                lowQ = midQ;
                break;
              }
              lowQ = midQ;
            } else {
              highQ = midQ;
            }
          }
        }

        // Fallback to lowest floor candidate if needed
        if (!bestBlob) {
          bestBlob = await new Promise(r => workingCanvas.toBlob(r, targetMime, 0.05));
        }

        finalBlob = bestBlob;
        if (finalBlob && finalBlob.size <= targetBytes) {
          achieved = true;
          const shortfall = targetBytes - finalBlob.size;
          if (shortfall <= 10240) {
            statusMessage = `✓ Target Achieved: Output (${formatBytes(finalBlob.size)}) is within 10 KB of your ${formatBytes(targetBytes)} target at ${Math.round(bestQuality * 100)}% quality.`;
          } else {
            statusMessage = `Target met: Output (${formatBytes(finalBlob.size)}) is within your ${formatBytes(targetBytes)} limit with highest visual clarity.`;
          }
        } else {
          achieved = false;
          statusMessage = `Target of ${formatBytes(targetBytes)} reached physical compression floor (${formatBytes(finalBlob ? finalBlob.size : 0)}).`;
        }
      }
    }

    if (!finalBlob) {
      throw new Error('Failed to generate compressed image.');
    }

    onProgress(100, 'Optimization complete!');
    const newSize = finalBlob.size;
    const isLarger = newSize > origSize;
    const savedPct = Math.round(((origSize - newSize) / origSize) * 100);

    return {
      blob: finalBlob,
      ext,
      origSize,
      newSize,
      savedPct,
      isLarger,
      achieved,
      statusMessage,
      width: origWidth,
      height: origHeight,
      mime: targetMime
    };
  };

  // 10. Client-Side PDF Compression Engine (Calibrated Target Size & Level Modes)
  window.compressPdfEngine = async function(file, opts) {
    const origSize = file.size;
    const mode = (opts && opts.mode) || 'target';
    const targetBytes = opts && opts.targetBytes ? opts.targetBytes : null;
    const sliderPct = opts && typeof opts.sliderPct === 'number' ? opts.sliderPct : 60;
    const onProgress = (opts && opts.onProgress) || (() => {});

    onProgress(10, 'Loading PDF document & engine...');
    await window.loadPdfLibraries();

    const arrayBuffer = await file.arrayBuffer();
    const pdfDoc = await pdfjsLib.getDocument({ data: arrayBuffer }).promise;
    const numPages = pdfDoc.numPages;
    if (numPages <= 0) {
      throw new Error('The selected PDF contains no readable pages.');
    }

    let qualityFactor = 0.65;
    let scaleFactor = 0.95;
    let achieved = true;
    let statusMessage = '';

    if (mode === 'slider') {
      qualityFactor = Math.max(0.15, (100 - sliderPct * 0.85) / 100);
      scaleFactor = Math.max(0.68, (100 - sliderPct * 0.35) / 100);
      achieved = true;
      statusMessage = `Compressed at ${sliderPct}% compression level.`;
    } else {
      // Target File Size Mode: Adaptive Empirical Calibration
      if (targetBytes >= origSize) {
        qualityFactor = 0.92;
        scaleFactor = 1.80;
        achieved = true;
        statusMessage = `Target size (${formatBytes(targetBytes)}) is greater than or equal to original. File saved at maximum quality.`;
      } else {
        onProgress(15, 'Evaluating document complexity & clarity...');
        const pdfOverhead = Math.round(1800 + numPages * 1200);
        const netBudget = Math.max(2000, targetBytes - pdfOverhead);
        const perPageBudget = netBudget / numPages;

        // Sample Page 1 at reference scale (1.5) and quality 0.82
        let sampleBytes = 35000;
        try {
          const samplePage = await pdfDoc.getPage(1);
          const sampleViewport = samplePage.getViewport({ scale: 1.5 });
          const sampleCanvas = document.createElement('canvas');
          sampleCanvas.width = sampleViewport.width;
          sampleCanvas.height = sampleViewport.height;
          const sampleCtx = sampleCanvas.getContext('2d');
          sampleCtx.fillStyle = '#FFFFFF';
          sampleCtx.fillRect(0, 0, sampleCanvas.width, sampleCanvas.height);
          await samplePage.render({ canvasContext: sampleCtx, viewport: sampleViewport }).promise;
          const sampleDataUrl = sampleCanvas.toDataURL('image/jpeg', 0.82);
          sampleBytes = Math.round((sampleDataUrl.length - 22) * 0.75);
        } catch (e) {
          console.warn('PDF sampling notice:', e);
        }

        // Aim for ~97% of budget to prevent overshoot while maximizing crispness
        const budgetTarget = perPageBudget * 0.97;
        const ratio = budgetTarget / Math.max(1000, sampleBytes);

        // Continuous power curve calibrated around reference sample (scale 1.5, quality 0.82):
        scaleFactor = Math.max(0.60, Math.min(1.65, 1.0 * Math.pow(ratio, 0.45)));
        qualityFactor = Math.max(0.18, Math.min(0.90, 0.76 * Math.pow(ratio, 0.30)));
      }
    }

    async function encodePdfDoc(scaleF, qualityF, reportProg = true) {
      const outPdf = await PDFLib.PDFDocument.create();
      for (let i = 1; i <= numPages; i++) {
        if (reportProg) {
          const prog = Math.round(20 + (i / numPages) * 70);
          onProgress(prog, `Optimizing page ${i} of ${numPages}...`);
        }
        const page = await pdfDoc.getPage(i);
        const viewport = page.getViewport({ scale: scaleF * 1.5 });
        const canvas = document.createElement('canvas');
        canvas.width = viewport.width;
        canvas.height = viewport.height;
        const ctx = canvas.getContext('2d');
        ctx.fillStyle = '#FFFFFF';
        ctx.fillRect(0, 0, canvas.width, canvas.height);
        await page.render({ canvasContext: ctx, viewport }).promise;
        const imgDataUrl = canvas.toDataURL('image/jpeg', qualityF);
        const embeddedImg = await outPdf.embedJpg(imgDataUrl);
        const newPage = outPdf.addPage([viewport.width, viewport.height]);
        newPage.drawImage(embeddedImg, {
          x: 0,
          y: 0,
          width: viewport.width,
          height: viewport.height
        });
      }
      const pdfBytes = await outPdf.save();
      return new Blob([pdfBytes], { type: 'application/pdf' });
    }

    let finalBlob = await encodePdfDoc(scaleFactor, qualityFactor, true);

    // Iterative refinement if output exceeds target OR significantly undershoots
    if (mode === 'target' && targetBytes < origSize) {
      let currentScale = scaleFactor;
      let currentQuality = qualityFactor;

      // While output exceeds target, apply smooth mathematical reduction (up to 2 passes)
      for (let pass = 0; pass < 2 && finalBlob.size > targetBytes; pass++) {
        onProgress(85 + pass * 5, 'Fine-tuning to guarantee output meets target size...');
        const correction = (targetBytes * 0.94) / finalBlob.size;
        currentScale = Math.max(0.50, currentScale * Math.sqrt(correction));
        currentQuality = Math.max(0.15, currentQuality * Math.pow(correction, 0.35));
        const refinedBlob = await encodePdfDoc(currentScale, currentQuality, false);
        if (refinedBlob && refinedBlob.size < finalBlob.size) {
          finalBlob = refinedBlob;
        }
      }

      // If output undershot target significantly (> 10 KB headroom) on a document with <= 6 pages:
      if (finalBlob.size <= targetBytes && (targetBytes - finalBlob.size > 10240) && numPages <= 6) {
        const headroom = (targetBytes * 0.98) / finalBlob.size;
        if (headroom > 1.05) {
          onProgress(92, 'Maximizing visual clarity within target budget...');
          const refinedScale = Math.min(1.65, currentScale * Math.min(1.25, Math.sqrt(headroom)));
          const refinedQuality = Math.min(0.90, currentQuality * Math.min(1.25, Math.pow(headroom, 0.28)));
          const refinedBlob = await encodePdfDoc(refinedScale, refinedQuality, false);
          if (refinedBlob && refinedBlob.size <= targetBytes && refinedBlob.size > finalBlob.size) {
            finalBlob = refinedBlob;
          }
        }
      }
    }

    onProgress(95, 'Finalizing PDF output...');
    const newSize = finalBlob.size;
    const savedPct = Math.round(((origSize - newSize) / origSize) * 100);

    if (mode === 'target') {
      if (newSize <= targetBytes) {
        achieved = true;
        const shortfall = targetBytes - newSize;
        if (shortfall <= 10240) {
          statusMessage = `✓ Target Achieved: Output (${formatBytes(newSize)}) is within 10 KB of your ${formatBytes(targetBytes)} target with maximum clarity.`;
        } else {
          statusMessage = `Target met: Output (${formatBytes(newSize)}) is within your ${formatBytes(targetBytes)} target with maximum clarity preserved.`;
        }
      } else {
        achieved = false;
        statusMessage = `Target of ${formatBytes(targetBytes)} reached readability floor (${formatBytes(newSize)}). Minimum legible quality was preserved.`;
      }
    }

    onProgress(100, 'Optimization complete!');

    return {
      blob: finalBlob,
      ext: 'pdf',
      origSize,
      newSize,
      savedPct,
      achieved,
      statusMessage,
      numPages
    };
  };

  // Initialize Theme & Navigation on DOM Ready
  document.addEventListener('DOMContentLoaded', () => {
    initTheme();
    // Bind all theme toggles
    document.querySelectorAll('.theme-toggle-btn').forEach(btn => {
      if (!btn.hasAttribute('onclick')) {
        btn.addEventListener('click', toggleTheme);
      }
    });

    const closeBtn = document.getElementById('closeDrawerBtn');
    if (closeBtn) closeBtn.addEventListener('click', closeDrawer);
    const backdrop = document.getElementById('drawerBackdrop');
    if (backdrop) backdrop.addEventListener('click', closeDrawer);
    const hb = document.getElementById('hamburgerBtn');
    if (hb) hb.addEventListener('click', openDrawer);

    // Auto-highlight active navigation link
    try {
      const currentPath = window.location.pathname.replace(/\/$/, '');
      document.querySelectorAll('.desktop-menu .menu-btn, .mobile-drawer .drawer-item').forEach(link => {
        const href = link.getAttribute('href');
        if (href) {
          const resolvedPath = new URL(href, window.location.href).pathname.replace(/\/$/, '');
          if (resolvedPath === currentPath || (currentPath === '' && resolvedPath.endsWith('index.html'))) {
            link.classList.add('active');
            link.setAttribute('aria-current', 'page');
          }
        }
      });
    } catch (e) {}
  });

  // Global file selection & drop listener for GA4 tool_file_selected event
  document.addEventListener('change', function(e) {
    if (e.target && e.target.tagName === 'INPUT' && e.target.type === 'file') {
      if (e.target.files && e.target.files.length > 0) {
        if (window.DocToolKit && typeof DocToolKit.trackFileSelected === 'function') {
          DocToolKit.trackFileSelected();
        }
      }
    }
  }, true);

  document.addEventListener('drop', function(e) {
    if (e.dataTransfer && e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      if (window.DocToolKit && typeof DocToolKit.trackFileSelected === 'function') {
        DocToolKit.trackFileSelected();
      }
    }
  }, true);

})();
