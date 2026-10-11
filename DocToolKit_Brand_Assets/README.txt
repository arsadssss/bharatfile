DocToolKit branding assets

Official spelling: DocToolKit
Official domain: https://doctoolkit.in/

Files:
- doctoolkit-logo-horizontal.png: transparent horizontal logo + wordmark for website header.
- doctoolkit-icon-master.png: high-resolution transparent icon master.
- favicon.ico: multi-size browser favicon.
- favicon.svg: lightweight vector favicon fallback.
- icons/favicon-16x16.png and icons/favicon-32x32.png: small favicon PNGs.
- icons/apple-touch-icon.png: 180x180 iOS home-screen icon.
- icons/icon-192.png: PWA icon, 192x192.
- icons/icon-512.png: PWA icon, 512x512.

Suggested HTML:
<link rel="icon" href="/favicon.ico" sizes="any">
<link rel="icon" type="image/svg+xml" href="/favicon.svg">
<link rel="apple-touch-icon" href="/icons/apple-touch-icon.png">
<link rel="manifest" href="/manifest.json">

Suggested manifest icon entries:
{ "src": "/icons/icon-192.png", "sizes": "192x192", "type": "image/png" }
{ "src": "/icons/icon-512.png", "sizes": "512x512", "type": "image/png" }

Note: These are visual brand assets. Antigravity should update references in index.html, manifest.json, and any SEO metadata, then verify every asset path after deployment.
