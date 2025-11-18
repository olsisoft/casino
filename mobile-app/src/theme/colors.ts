/**
 * Professional Color Palette
 * Modern casino-inspired design with accessibility in mind
 */

export const colors = {
  // Primary Brand Colors
  primary: {
    50: '#FFF1E6',
    100: '#FFE4CC',
    200: '#FFC999',
    300: '#FFAD66',
    400: '#FF9233',
    500: '#FF7700', // Main brand color - vibrant orange
    600: '#CC5F00',
    700: '#994700',
    800: '#663000',
    900: '#331800',
  },

  // Secondary/Accent Colors
  secondary: {
    50: '#E6F5FF',
    100: '#CCE6FF',
    200: '#99CCFF',
    300: '#66B3FF',
    400: '#3399FF',
    500: '#0080FF', // Electric blue
    600: '#0066CC',
    700: '#004D99',
    800: '#003366',
    900: '#001A33',
  },

  // Success Colors
  success: {
    50: '#E6FFF2',
    100: '#CCFFE5',
    200: '#99FFCC',
    300: '#66FFB3',
    400: '#33FF99',
    500: '#00FF80', // Bright green
    600: '#00CC66',
    700: '#00994D',
    800: '#006633',
    900: '#003319',
  },

  // Error/Danger Colors
  error: {
    50: '#FFE6E6',
    100: '#FFCCCC',
    200: '#FF9999',
    300: '#FF6666',
    400: '#FF3333',
    500: '#FF0000', // Pure red
    600: '#CC0000',
    700: '#990000',
    800: '#660000',
    900: '#330000',
  },

  // Warning Colors
  warning: {
    50: '#FFFBE6',
    100: '#FFF7CC',
    200: '#FFEF99',
    300: '#FFE766',
    400: '#FFDF33',
    500: '#FFD700', // Gold
    600: '#CCAC00',
    700: '#998100',
    800: '#665600',
    900: '#332B00',
  },

  // Neutral/Grayscale
  neutral: {
    0: '#FFFFFF',
    50: '#F9FAFB',
    100: '#F3F4F6',
    200: '#E5E7EB',
    300: '#D1D5DB',
    400: '#9CA3AF',
    500: '#6B7280',
    600: '#4B5563',
    700: '#374151',
    800: '#1F2937',
    900: '#111827',
    950: '#0A0D14',
    1000: '#000000',
  },

  // Dark Theme Background
  background: {
    primary: '#0A0D14',
    secondary: '#111827',
    tertiary: '#1F2937',
    card: '#1A1F2E',
    elevated: '#252B3D',
  },

  // Text Colors
  text: {
    primary: '#FFFFFF',
    secondary: '#D1D5DB',
    tertiary: '#9CA3AF',
    disabled: '#6B7280',
    inverse: '#000000',
  },

  // Special Effects
  effects: {
    glow: '#FF7700',
    shimmer: '#FFD700',
    overlay: 'rgba(0, 0, 0, 0.6)',
    overlayLight: 'rgba(0, 0, 0, 0.3)',
    overlayHeavy: 'rgba(0, 0, 0, 0.8)',
    blur: 'rgba(10, 13, 20, 0.9)',
  },

  // Game-specific Colors
  game: {
    blackjack: '#1A472A',
    roulette: '#8B0000',
    slots: '#9B59B6',
    poker: '#2C3E50',
    dice: '#E74C3C',
    mines: '#34495E',
    crash: '#E67E22',
    coinflip: '#FFD700',
  },

  // Status Colors
  status: {
    online: '#00FF80',
    offline: '#6B7280',
    away: '#FFD700',
    busy: '#FF3333',
  },

  // VIP Tier Colors
  vip: {
    bronze: '#CD7F32',
    silver: '#C0C0C0',
    gold: '#FFD700',
    platinum: '#E5E4E2',
    diamond: '#B9F2FF',
  },
};

// Gradient Definitions
export const gradients = {
  primary: ['#FF7700', '#FF9233', '#FFAD66'],
  secondary: ['#0080FF', '#3399FF', '#66B3FF'],
  success: ['#00FF80', '#33FF99', '#66FFB3'],
  error: ['#FF0000', '#FF3333', '#FF6666'],
  dark: ['#0A0D14', '#111827', '#1F2937'],
  gold: ['#FFD700', '#FFDF33', '#FFE766'],

  // Special Gradients
  win: ['#00FF80', '#FFD700', '#FF7700'],
  lose: ['#FF0000', '#660000', '#330000'],
  jackpot: ['#FFD700', '#FF7700', '#FFD700'],
  vip: ['#9B59B6', '#E74C3C', '#FFD700'],
};

// Shadow Definitions
export const shadows = {
  small: {
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
  },
  medium: {
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.15,
    shadowRadius: 8,
    elevation: 4,
  },
  large: {
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.2,
    shadowRadius: 16,
    elevation: 8,
  },
  glow: {
    shadowColor: '#FF7700',
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 0.5,
    shadowRadius: 12,
    elevation: 6,
  },
};
