import React, { useEffect, useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';

export const OfflineIndicator: React.FC = () => {
  const [isOnline, setIsOnline] = useState(navigator.onLine);

  useEffect(() => {
    const handleOnline = () => setIsOnline(true);
    const handleOffline = () => setIsOnline(false);

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  return (
    <AnimatePresence>
      {!isOnline && (
        <motion.div
          initial={{ opacity: 0, y: -50 }}
          animate={{ opacity: 1, y: 0 }}
          exit={{ opacity: 0, y: -50 }}
          className="fixed top-0 left-0 right-0 z-50"
        >
          <div className="bg-error-500 text-white py-3 px-4 text-center">
            <div className="flex items-center justify-center gap-2">
              <span className="text-lg">📡</span>
              <span className="font-semibold">You are offline</span>
              <span className="text-sm opacity-90">- Some features may be limited</span>
            </div>
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  );
};
