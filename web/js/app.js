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

  // 2. Configure PDF.js Worker
  if (window.pdfjsLib) {
    let workerSrc = '/js/pdf.worker.min.js';
    if (!window.location.origin.includes('doctoolkit.in')) {
      const path = window.location.pathname;
      if (path.includes('/tools/')) workerSrc = '../../js/pdf.worker.min.js';
      else if (path.match(/\/(about|contact|privacy|terms)\//)) workerSrc = '../js/pdf.worker.min.js';
      else workerSrc = 'js/pdf.worker.min.js';
    }
    pdfjsLib.GlobalWorkerOptions.workerSrc = workerSrc;
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
    version: '2.6.0',
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
    // Standardized UI Result & Feedback Component
    showProcessing(containerId, actionText, subText) {
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
      if (btn && onRetry) btn.onclick = onRetry;
    },
    showResult(containerId, options = {}) {
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
    });
    return dbPromise;
  };

  window.saveDocumentToDB = async function(doc) {
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
      if (!db) return [];
      const tx = db.transaction('documents', 'readonly');
      const store = tx.objectStore('documents');
      const req = store.getAll();
      return new Promise((resolve) => {
        req.onsuccess = () => resolve(req.result || []);
        req.onerror = () => resolve([]);
      });
    } catch (e) {
      return [];
    }
  };

  window.deleteDocumentFromDB = async function(id) {
    try {
      const db = await openDocDB();
      if (!db) return false;
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
      img.onload = res;
      img.onerror = () => rej(new Error('Failed to decode image file. File may be corrupted or unsupported.'));
      img.src = objUrl;
    });
    URL.revokeObjectURL(objUrl);

    onProgress(35, 'Analyzing image dimensions...');

    const origWidth = img.naturalWidth || img.width;
    const origHeight = img.naturalHeight || img.height;
    if (!origWidth || !origHeight) {
      throw new Error('Invalid image dimensions.');
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
      if (mode === 'target' && targetBytes && finalBlob && finalBlob.size > targetBytes) {
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
      // Target File Size Mode: Iterative Binary Search
      onProgress(50, 'Calibrating quality to reach target size...');

      if (targetBytes >= origSize) {
        finalBlob = await new Promise(r => canvas.toBlob(r, targetMime, 0.94));
        achieved = true;
        statusMessage = `Target size (${formatBytes(targetBytes)}) is greater than or equal to original size. File saved at maximum quality.`;
      } else {
        // Test lowest quality floor
        const floorBlob = await new Promise(r => canvas.toBlob(r, targetMime, 0.08));
        if (floorBlob && floorBlob.size > targetBytes) {
          finalBlob = floorBlob;
          achieved = false;
          statusMessage = `Target of ${formatBytes(targetBytes)} could not be reached without altering image resolution. Output reached quality floor (${formatBytes(floorBlob.size)}).`;
        } else {
          let lowQ = 0.08;
          let highQ = 0.96;
          let bestBlob = floorBlob;

          for (let iter = 0; iter < 6; iter++) {
            const midQ = (lowQ + highQ) / 2;
            const testBlob = await new Promise(r => canvas.toBlob(r, targetMime, midQ));
            if (!testBlob) break;

            if (testBlob.size <= targetBytes) {
              bestBlob = testBlob;
              if (testBlob.size >= targetBytes * 0.94) {
                break;
              }
              lowQ = midQ;
            } else {
              highQ = midQ;
            }
          }
          finalBlob = bestBlob;
          achieved = true;
          statusMessage = `Target achieved: Output (${formatBytes(finalBlob.size)}) is within your ${formatBytes(targetBytes)} goal.`;
        }
      }
    }

    if (!finalBlob) {
      throw new Error('Failed to generate compressed image.');
    }

    onProgress(100, 'Optimization complete!');
    const newSize = finalBlob.size;
    const savedPct = Math.round(((origSize - newSize) / origSize) * 100);

    return {
      blob: finalBlob,
      ext,
      origSize,
      newSize,
      savedPct,
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

    onProgress(10, 'Loading PDF document...');

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
        const perPageBudget = Math.max(1500, (targetBytes - 3500) / Math.max(1, numPages));

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

        // Calibrate scale and quality according to measured empirical sample:
        // Aim for ~90% of budget to prevent overshoot while maximizing crispness
        const budgetTarget = perPageBudget * 0.90;
        const ratio = budgetTarget / Math.max(1000, sampleBytes);

        if (ratio >= 2.5) {
          // Abundant budget headroom: maximize resolution and crisp text
          scaleFactor = 1.80; // total viewport scale = 2.7 (~195 DPI)
          qualityFactor = 0.92;
        } else if (ratio >= 1.4) {
          // Healthy budget headroom: high quality
          scaleFactor = 1.55; // total viewport scale = 2.3 (~165 DPI)
          qualityFactor = 0.86;
        } else if (ratio >= 0.85) {
          // Balanced budget: standard clear quality
          scaleFactor = 1.25; // total viewport scale = 1.88 (~135 DPI)
          qualityFactor = 0.78;
        } else if (ratio >= 0.50) {
          // Tight budget: moderate compression
          scaleFactor = 1.05; // total viewport scale = 1.58 (~113 DPI)
          qualityFactor = 0.62;
        } else if (ratio >= 0.28) {
          // Severe budget: compact compression
          scaleFactor = 0.88; // total viewport scale = 1.32 (~95 DPI)
          qualityFactor = 0.44;
        } else {
          // Readability floor: preserve text legibility
          scaleFactor = 0.72; // total viewport scale = 1.08 (~78 DPI)
          qualityFactor = 0.26;
        }
      }
    }

    const outPdf = await PDFLib.PDFDocument.create();

    for (let i = 1; i <= numPages; i++) {
      const prog = Math.round(20 + (i / numPages) * 70);
      onProgress(prog, `Optimizing page ${i} of ${numPages}...`);

      const page = await pdfDoc.getPage(i);
      const viewport = page.getViewport({ scale: scaleFactor * 1.5 });

      const canvas = document.createElement('canvas');
      canvas.width = viewport.width;
      canvas.height = viewport.height;
      const ctx = canvas.getContext('2d');
      ctx.fillStyle = '#FFFFFF';
      ctx.fillRect(0, 0, canvas.width, canvas.height);

      await page.render({ canvasContext: ctx, viewport }).promise;

      const imgDataUrl = canvas.toDataURL('image/jpeg', qualityFactor);
      const embeddedImg = await outPdf.embedJpg(imgDataUrl);

      const newPage = outPdf.addPage([viewport.width, viewport.height]);
      newPage.drawImage(embeddedImg, {
        x: 0,
        y: 0,
        width: viewport.width,
        height: viewport.height
      });
    }

    onProgress(95, 'Finalizing PDF output...');
    const pdfBytes = await outPdf.save();
    const finalBlob = new Blob([pdfBytes], { type: 'application/pdf' });
    const newSize = finalBlob.size;
    const savedPct = Math.round(((origSize - newSize) / origSize) * 100);

    if (mode === 'target') {
      if (newSize <= targetBytes) {
        achieved = true;
        statusMessage = `Target achieved: Output (${formatBytes(newSize)}) is within your ${formatBytes(targetBytes)} target with maximum clarity preserved.`;
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
