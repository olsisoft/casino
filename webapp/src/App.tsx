import React, { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { useRegisterSW } from 'virtual:pwa-register/react';

// Screens
import { LoginScreen } from './screens/auth/LoginScreen';
import { RegisterScreen } from './screens/auth/RegisterScreen';
import { HomeScreen } from './screens/home/HomeScreen';
import { GameLobbyScreen } from './screens/games/GameLobbyScreen';
import { WalletScreen } from './screens/wallet/WalletScreen';
import { ProfileScreen } from './screens/profile/ProfileScreen';

// Components
import { InstallPrompt } from './components/InstallPrompt';
import { UpdatePrompt } from './components/UpdatePrompt';
import { OfflineIndicator } from './components/OfflineIndicator';

function App() {
  const {
    needRefresh: [needRefresh, setNeedRefresh],
    updateServiceWorker,
  } = useRegisterSW({
    onRegistered(r) {
      console.log('SW Registered: ' + r);
    },
    onRegisterError(error) {
      console.log('SW registration error', error);
    },
  });

  useEffect(() => {
    // Register service worker
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.register('/sw.js');
    }
  }, []);

  return (
    <Router>
      <div className="min-h-screen bg-dark-primary text-white">
        {/* Toasts for notifications */}
        <Toaster
          position="top-right"
          toastOptions={{
            duration: 4000,
            style: {
              background: '#1A1F2E',
              color: '#fff',
              border: '1px solid #374151',
            },
            success: {
              iconTheme: {
                primary: '#00FF80',
                secondary: '#1A1F2E',
              },
            },
            error: {
              iconTheme: {
                primary: '#FF0000',
                secondary: '#1A1F2E',
              },
            },
          }}
        />

        {/* PWA Components */}
        <InstallPrompt />
        <OfflineIndicator />
        {needRefresh && (
          <UpdatePrompt
            onUpdate={() => updateServiceWorker(true)}
            onDismiss={() => setNeedRefresh(false)}
          />
        )}

        {/* Routes */}
        <Routes>
          <Route path="/login" element={<LoginScreen />} />
          <Route path="/register" element={<RegisterScreen />} />
          <Route path="/" element={<HomeScreen />} />
          <Route path="/games" element={<GameLobbyScreen />} />
          <Route path="/wallet" element={<WalletScreen />} />
          <Route path="/profile" element={<ProfileScreen />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
