# Casino Platform - Progressive Web App (PWA)

## Overview

Professional Progressive Web App for the Casino Platform, providing a native app-like experience across all devices with offline capabilities, push notifications, and installability.

## Features

### ✨ PWA Capabilities

- **📱 Installable** - Add to home screen on mobile and desktop
- **🔄 Offline Support** - Service worker with intelligent caching
- **⚡ Fast Loading** - Optimized bundle splitting and lazy loading
- **🔔 Push Notifications** - Real-time updates (future implementation)
- **📊 Background Sync** - Queue actions while offline
- **🎨 Responsive Design** - Works on all screen sizes
- **🚀 App-like Experience** - Standalone mode, no browser chrome

### 🎯 Core Features

- **Authentication** - Login, Register, Social Auth
- **Game Lobby** - Browse and search games
- **Wallet** - Deposits, withdrawals, transaction history
- **Profile** - User settings, VIP status, KYC
- **Live Games** - Real-time game play
- **Chat** - Real-time messaging
- **Leaderboards** - Rankings and achievements

## Tech Stack

### Core
- **React 18** - UI library with hooks
- **TypeScript** - Type safety
- **Vite** - Fast build tool and dev server
- **React Router 6** - Client-side routing

### PWA
- **Vite PWA Plugin** - PWA configuration
- **Workbox** - Service worker library
- **Web App Manifest** - App metadata

### Styling
- **Tailwind CSS** - Utility-first CSS
- **Framer Motion** - Animations
- **Custom Design System** - Brand colors and components

### State Management
- **Zustand** - Lightweight state management
- **React Query** - Server state (future)

### HTTP
- **Axios** - API client
- **React Hot Toast** - Notifications

## Project Structure

```
webapp/
├── public/
│   ├── icons/              # App icons (various sizes)
│   ├── screenshots/        # App screenshots
│   ├── manifest.json       # Web app manifest
│   └── robots.txt
├── src/
│   ├── components/         # Reusable components
│   │   ├── InstallPrompt.tsx
│   │   ├── UpdatePrompt.tsx
│   │   ├── OfflineIndicator.tsx
│   │   └── ...
│   ├── screens/            # Page components
│   │   ├── auth/
│   │   │   ├── LoginScreen.tsx
│   │   │   └── RegisterScreen.tsx
│   │   ├── home/
│   │   ├── games/
│   │   ├── wallet/
│   │   └── profile/
│   ├── hooks/              # Custom React hooks
│   ├── services/           # API services
│   ├── store/              # State management
│   ├── utils/              # Helper functions
│   ├── types/              # TypeScript types
│   ├── App.tsx             # Root component
│   ├── main.tsx            # Entry point
│   └── index.css           # Global styles
├── vite.config.ts          # Vite configuration
├── tailwind.config.js      # Tailwind configuration
├── tsconfig.json           # TypeScript configuration
└── package.json

```

## Installation & Setup

### Prerequisites
- Node.js 18+ and npm/yarn

### Install Dependencies
```bash
cd webapp
npm install
```

### Development Server
```bash
npm run dev
```
Opens at `http://localhost:3000`

### Build for Production
```bash
npm run build
```
Outputs to `dist/` directory

### Preview Production Build
```bash
npm run preview
```

## PWA Configuration

### Manifest Configuration
Located in `public/manifest.json`:
- App name and description
- Theme colors
- Display mode (standalone)
- Icons (72x72 to 512x512)
- Shortcuts (quick actions)
- Screenshots

### Service Worker
Configured in `vite.config.ts` with Workbox:

**Caching Strategies:**
1. **API Calls** - NetworkFirst (24h cache)
2. **Static Assets** - CacheFirst (1 year)
3. **Images** - CacheFirst (30 days)
4. **Fonts** - CacheFirst (1 year)

**Workbox Features:**
- Runtime caching
- Precaching
- Cache expiration
- Offline fallbacks

### Icons

Required icon sizes:
- 72x72, 96x96, 128x128, 144x144
- 152x152, 192x192, 384x384, 512x512

**Generation:**
Use tools like https://realfavicongenerator.net/

## Offline Support

### Cached Resources
- HTML, CSS, JavaScript
- Images and icons
- Fonts
- API responses (24h)

### Offline Features
- View cached games
- Access profile
- Browse transaction history
- Queue deposit/withdraw requests

### Online Detection
`OfflineIndicator` component shows banner when offline.

## Install Prompt

### Desktop (Chrome/Edge)
- Automatic prompt after criteria met
- Custom prompt after 30 seconds
- "Install" button in omnibox

### Mobile (Android)
- Add to Home Screen prompt
- Mini-infobar
- Custom install prompt

### iOS (Safari)
- Share menu → Add to Home Screen
- No automatic prompt

### Install Criteria
1. Served over HTTPS
2. Has web app manifest
3. Has service worker
4. User engagement (30s)

## Update Flow

1. New version detected
2. `UpdatePrompt` shown
3. User clicks "Reload"
4. Service worker updates
5. App reloads with new version

## Performance Optimization

### Bundle Splitting
- React vendor bundle
- UI vendor bundle (Framer Motion)
- Route-based code splitting

### Image Optimization
- WebP format
- Lazy loading
- Responsive images

### Caching
- Service worker caching
- HTTP caching headers
- CDN caching

### Metrics (Target)
- **FCP** - < 1.5s
- **LCP** - < 2.5s
- **TTI** - < 3.5s
- **CLS** - < 0.1

## Design System

### Colors
- **Primary** - #FF7700 (Orange)
- **Secondary** - #0080FF (Blue)
- **Success** - #00FF80 (Green)
- **Error** - #FF0000 (Red)
- **Dark** - #0A0D14 to #1F2937

### Typography
- **Font Family** - Inter (body), Poppins (headings)
- **Sizes** - xs (12px) to 6xl (60px)
- **Weights** - 400, 500, 600, 700

### Spacing
- **Scale** - 0.25rem to 8rem (4px to 128px)
- **Grid** - 8px base unit

### Components
- Buttons (Primary, Secondary, Outline, Ghost)
- Cards (Default, Elevated, Glass)
- Inputs (Text, Email, Password, Number)
- Modals, Toasts, Badges, etc.

## Responsive Breakpoints

```css
sm: 640px   /* Small devices */
md: 768px   /* Medium devices */
lg: 1024px  /* Large devices */
xl: 1280px  /* Extra large */
2xl: 1536px /* 2X Extra large */
```

## Browser Support

### Desktop
- ✅ Chrome 90+
- ✅ Edge 90+
- ✅ Firefox 88+
- ✅ Safari 14+

### Mobile
- ✅ Chrome Android 90+
- ✅ Safari iOS 14+
- ✅ Samsung Internet 15+

## Testing

### Lighthouse Audit
```bash
npm run build
npm run preview
# Run Lighthouse in Chrome DevTools
```

**Target Scores:**
- Performance: 90+
- Accessibility: 95+
- Best Practices: 95+
- SEO: 90+
- PWA: 100

### PWA Checklist
- [x] HTTPS
- [x] Service Worker
- [x] Web App Manifest
- [x] Offline fallback
- [x] Mobile responsive
- [x] Fast load times
- [x] Install prompt
- [x] Update mechanism

## Deployment

### Build
```bash
npm run build
```

### Deploy to Netlify
```bash
# Install Netlify CLI
npm install -g netlify-cli

# Deploy
netlify deploy --prod --dir=dist
```

### Deploy to Vercel
```bash
# Install Vercel CLI
npm install -g vercel

# Deploy
vercel --prod
```

### Deploy to Firebase
```bash
# Install Firebase CLI
npm install -g firebase-tools

# Initialize
firebase init hosting

# Deploy
firebase deploy
```

### Environment Variables
Create `.env` file:
```
VITE_API_URL=https://api.casino.com
VITE_WS_URL=wss://api.casino.com/ws
VITE_VAPID_PUBLIC_KEY=your_vapid_key
```

## Push Notifications (Future)

### Setup
1. Generate VAPID keys
2. Configure service worker
3. Request permission
4. Subscribe user
5. Send notifications from backend

## SEO

### Meta Tags
- Title, description
- Open Graph tags
- Twitter Card tags
- Canonical URL

### Sitemap
Located at `/sitemap.xml`

### Robots.txt
Located at `/robots.txt`

## Analytics (Future)

- Google Analytics 4
- Custom event tracking
- User behavior analytics
- Performance monitoring

## Security

- HTTPS only
- Content Security Policy
- XSS protection
- CORS configuration
- Secure cookies
- JWT token storage

## Accessibility

- ARIA labels
- Keyboard navigation
- Screen reader support
- Color contrast (WCAG AA)
- Focus indicators
- Alt text for images

## Best Practices

1. **Performance** - Optimize images, lazy load, code split
2. **SEO** - Meta tags, sitemap, semantic HTML
3. **Accessibility** - ARIA, keyboard nav, contrast
4. **Security** - HTTPS, CSP, sanitize inputs
5. **UX** - Loading states, error handling, offline support

## Future Enhancements

- [ ] Push notifications
- [ ] Background sync
- [ ] Web Share API
- [ ] Payment Request API
- [ ] Credential Management API
- [ ] Geolocation API
- [ ] Camera API (for KYC)
- [ ] Biometric authentication

## Resources

- [PWA Documentation](https://web.dev/progressive-web-apps/)
- [Workbox](https://developers.google.com/web/tools/workbox)
- [Vite PWA Plugin](https://vite-pwa-org.netlify.app/)
- [Web App Manifest](https://developer.mozilla.org/en-US/docs/Web/Manifest)

## License

Proprietary - All rights reserved

## Support

For issues and support, contact: support@casino.com

---

**Last Updated**: 2025-01-18
**Version**: 1.0.0
