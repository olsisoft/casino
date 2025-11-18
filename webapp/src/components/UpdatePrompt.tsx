import React from 'react';
import { motion } from 'framer-motion';

interface UpdatePromptProps {
  onUpdate: () => void;
  onDismiss: () => void;
}

export const UpdatePrompt: React.FC<UpdatePromptProps> = ({ onUpdate, onDismiss }) => {
  return (
    <motion.div
      initial={{ opacity: 0, y: -100 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -100 }}
      className="fixed top-4 left-4 right-4 md:left-auto md:right-4 md:max-w-md z-50"
    >
      <div className="bg-secondary-500 text-white rounded-xl shadow-2xl p-4">
        <div className="flex items-center gap-3">
          <div className="flex-shrink-0 text-2xl">
            🔄
          </div>
          <div className="flex-1">
            <p className="font-semibold mb-1">Update Available</p>
            <p className="text-sm opacity-90">
              A new version is ready. Reload to update.
            </p>
          </div>
          <div className="flex gap-2">
            <button
              onClick={onUpdate}
              className="bg-white text-secondary-500 font-semibold px-4 py-2 rounded-lg text-sm hover:bg-gray-100 transition-colors"
            >
              Reload
            </button>
            <button
              onClick={onDismiss}
              className="text-white hover:bg-secondary-600 px-3 py-2 rounded-lg text-sm transition-colors"
            >
              ✕
            </button>
          </div>
        </div>
      </div>
    </motion.div>
  );
};
