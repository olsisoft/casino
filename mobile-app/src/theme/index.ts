/**
 * Theme Index
 * Central export for all theme tokens
 */

import { colors, gradients, shadows } from './colors';
import { typography, fontFamily, fontSize, lineHeight, letterSpacing, fontWeight } from './typography';
import { spacing, borderRadius, borderWidth, iconSize, avatarSize, layout } from './spacing';

export const theme = {
  colors,
  gradients,
  shadows,
  typography,
  fontFamily,
  fontSize,
  lineHeight,
  letterSpacing,
  fontWeight,
  spacing,
  borderRadius,
  borderWidth,
  iconSize,
  avatarSize,
  layout,

  // Animation Durations
  animation: {
    fastest: 100,
    faster: 150,
    fast: 200,
    normal: 300,
    slow: 400,
    slower: 500,
    slowest: 600,
  },

  // Z-Index Scale
  zIndex: {
    background: -1,
    base: 0,
    dropdown: 1000,
    sticky: 1100,
    overlay: 1200,
    modal: 1300,
    popover: 1400,
    tooltip: 1500,
    toast: 1600,
  },

  // Opacity Scale
  opacity: {
    disabled: 0.4,
    inactive: 0.6,
    active: 1,
  },
};

export type Theme = typeof theme;

export default theme;
