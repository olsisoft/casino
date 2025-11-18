/**
 * Spacing System
 * Consistent spacing scale for layouts, padding, margins
 */

// Base spacing unit (4px)
const BASE_UNIT = 4;

// Spacing Scale (4px increments)
export const spacing = {
  0: 0,
  1: BASE_UNIT * 1, // 4px
  2: BASE_UNIT * 2, // 8px
  3: BASE_UNIT * 3, // 12px
  4: BASE_UNIT * 4, // 16px
  5: BASE_UNIT * 5, // 20px
  6: BASE_UNIT * 6, // 24px
  7: BASE_UNIT * 7, // 28px
  8: BASE_UNIT * 8, // 32px
  10: BASE_UNIT * 10, // 40px
  12: BASE_UNIT * 12, // 48px
  14: BASE_UNIT * 14, // 56px
  16: BASE_UNIT * 16, // 64px
  20: BASE_UNIT * 20, // 80px
  24: BASE_UNIT * 24, // 96px
  28: BASE_UNIT * 28, // 112px
  32: BASE_UNIT * 32, // 128px
};

// Border Radius
export const borderRadius = {
  none: 0,
  sm: 4,
  base: 8,
  md: 12,
  lg: 16,
  xl: 20,
  '2xl': 24,
  '3xl': 32,
  full: 9999,
};

// Border Width
export const borderWidth = {
  none: 0,
  thin: 1,
  base: 2,
  thick: 3,
  heavy: 4,
};

// Icon Sizes
export const iconSize = {
  xs: 16,
  sm: 20,
  base: 24,
  md: 28,
  lg: 32,
  xl: 40,
  '2xl': 48,
  '3xl': 64,
};

// Avatar Sizes
export const avatarSize = {
  xs: 24,
  sm: 32,
  base: 40,
  md: 48,
  lg: 64,
  xl: 80,
  '2xl': 96,
  '3xl': 128,
};

// Container Widths
export const containerWidth = {
  sm: 640,
  md: 768,
  lg: 1024,
  xl: 1280,
  '2xl': 1536,
};

// Layout Spacing (Semantic)
export const layout = {
  screenPadding: spacing[4], // 16px
  sectionGap: spacing[6], // 24px
  cardPadding: spacing[4], // 16px
  listItemPadding: spacing[4], // 16px
  inputPadding: spacing[3], // 12px
  buttonPadding: spacing[4], // 16px
  modalPadding: spacing[6], // 24px
};
