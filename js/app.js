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

  window.DocToolKit = {
    getPref,
    setPref,
    parseTargetBytes,
    timeAgo,
    recentActivity: recentActivityManager,
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

  // 9. Client-Side Image Compression Engine (Target Size & Level Modes)
  window.compressImageEngine = async function(file, opts) {
    const origSize = file.size;
    const mode = (opts && opts.mode) || 'target'; // 'target' or 'slider'
    const targetBytes = opts && opts.targetBytes ? opts.targetBytes : null;
    const sliderPct = opts && typeof opts.sliderPct === 'number' ? opts.sliderPct : 60;
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

    // Determine target format
    let targetMime = 'image/jpeg';
    let ext = 'jpg';
    if (file.type === 'image/webp') {
      targetMime = 'image/webp';
      ext = 'webp';
    } else if (file.type === 'image/png') {
      if (mode === 'slider' && sliderPct < 40) {
        targetMime = 'image/png';
        ext = 'png';
      } else {
        targetMime = 'image/webp';
        ext = 'webp';
      }
    }

    const canvas = document.createElement('canvas');
    canvas.width = origWidth;
    canvas.height = origHeight;
    const ctx = canvas.getContext('2d');
    ctx.drawImage(img, 0, 0, origWidth, origHeight);

    let finalBlob = null;
    let achieved = true;
    let statusMessage = '';

    if (mode === 'slider') {
      onProgress(60, 'Applying compression curves...');
      const quality = Math.max(0.05, Math.min(0.98, (100 - sliderPct * 0.92) / 100));
      finalBlob = await new Promise(r => canvas.toBlob(r, targetMime, quality));
      achieved = true;
      statusMessage = `Compressed at ${sliderPct}% compression level.`;
    } else {
      // Target File Size Mode: Iterative Binary Search
      onProgress(50, 'Calibrating quality to reach target size...');

      if (targetBytes >= origSize) {
        finalBlob = await new Promise(r => canvas.toBlob(r, targetMime, 0.92));
        achieved = true;
        statusMessage = `Target size (${formatBytes(targetBytes)}) is greater than or equal to original size. File saved at maximum quality.`;
      } else {
        const floorBlob = await new Promise(r => canvas.toBlob(r, targetMime, 0.05));
        if (floorBlob && floorBlob.size > targetBytes) {
          finalBlob = floorBlob;
          achieved = false;
          statusMessage = `Target of ${formatBytes(targetBytes)} could not be reached without altering image dimensions. Reduced as much as quality settings allow (${formatBytes(floorBlob.size)}).`;
        } else {
          let lowQ = 0.05;
          let highQ = 0.95;
          let bestBlob = floorBlob;

          for (let iter = 0; iter < 5; iter++) {
            const midQ = (lowQ + highQ) / 2;
            const testBlob = await new Promise(r => canvas.toBlob(r, targetMime, midQ));
            if (!testBlob) break;

            if (testBlob.size <= targetBytes) {
              bestBlob = testBlob;
              if (testBlob.size >= targetBytes * 0.92) {
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

  // 10. Client-Side PDF Compression Engine (Target Size & Level Modes)
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

    let qualityFactor = 0.60;
    let scaleFactor = 0.90;
    let achieved = true;
    let statusMessage = '';

    if (mode === 'slider') {
      qualityFactor = Math.max(0.12, (100 - sliderPct * 0.85) / 100);
      scaleFactor = Math.max(0.65, (100 - sliderPct * 0.35) / 100);
      achieved = true;
      statusMessage = `Compressed at ${sliderPct}% compression level.`;
    } else {
      // Target File Size Mode
      if (targetBytes >= origSize) {
        qualityFactor = 0.85;
        scaleFactor = 1.25;
        achieved = true;
        statusMessage = `Target size (${formatBytes(targetBytes)}) is greater than or equal to original. File saved at maximum quality.`;
      } else {
        const ratio = targetBytes / origSize;
        if (ratio >= 0.85) {
          scaleFactor = 1.25; qualityFactor = 0.85;
        } else if (ratio >= 0.60) {
          scaleFactor = 1.10; qualityFactor = 0.75;
        } else if (ratio >= 0.35) {
          scaleFactor = 0.95; qualityFactor = 0.60;
        } else if (ratio >= 0.18) {
          scaleFactor = 0.80; qualityFactor = 0.42;
        } else if (ratio >= 0.08) {
          scaleFactor = 0.68; qualityFactor = 0.28;
        } else {
          scaleFactor = 0.58; qualityFactor = 0.18;
        }

        const perPageBudget = (targetBytes - 6000) / Math.max(1, numPages);
        if (perPageBudget < 25000 && scaleFactor > 0.75) {
          scaleFactor = Math.max(0.58, scaleFactor * 0.85);
          qualityFactor = Math.max(0.18, qualityFactor * 0.85);
        }
      }
    }

    const outPdf = await PDFLib.PDFDocument.create();

    for (let i = 1; i <= numPages; i++) {
      const prog = Math.round(15 + (i / numPages) * 75);
      onProgress(prog, `Optimizing page ${i} of ${numPages}...`);

      const page = await pdfDoc.getPage(i);
      const viewport = page.getViewport({ scale: scaleFactor * 1.5 });

      const canvas = document.createElement('canvas');
      canvas.width = viewport.width;
      canvas.height = viewport.height;
      const ctx = canvas.getContext('2d');

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
        statusMessage = `Target achieved: Output (${formatBytes(newSize)}) is within your ${formatBytes(targetBytes)} goal.`;
      } else {
        achieved = false;
        statusMessage = `Target of ${formatBytes(targetBytes)} could not be reached without making text unreadable. Reduced as much as allowable (${formatBytes(newSize)}).`;
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
