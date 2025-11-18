/**
 * Typography System
 * Professional, scalable type system with excellent readability
 */

import { Platform } from 'react-native';

// Font Families
export const fontFamily = {
  // Primary Font (Modern, Clean)
  primary: {
    thin: Platform.select({
      ios: 'System',
      android: 'Roboto-Thin',
      default: 'System',
    }),
    light: Platform.select({
      ios: 'System',
      android: 'Roboto-Light',
      default: 'System',
    }),
    regular: Platform.select({
      ios: 'System',
      android: 'Roboto-Regular',
      default: 'System',
    }),
    medium: Platform.select({
      ios: 'System',
      android: 'Roboto-Medium',
      default: 'System',
    }),
    semibold: Platform.select({
      ios: 'System',
      android: 'Roboto-Bold',
      default: 'System',
    }),
    bold: Platform.select({
      ios: 'System',
      android: 'Roboto-Bold',
      default: 'System',
    }),
    black: Platform.select({
      ios: 'System',
      android: 'Roboto-Black',
      default: 'System',
    }),
  },

  // Display Font (Headings, Impact)
  display: {
    regular: Platform.select({
      ios: 'System',
      android: 'sans-serif',
      default: 'System',
    }),
    bold: Platform.select({
      ios: 'System',
      android: 'sans-serif',
      default: 'System',
    }),
  },

  // Monospace (Numbers, Codes)
  mono: Platform.select({
    ios: 'Menlo',
    android: 'monospace',
    default: 'monospace',
  }),
};

// Font Sizes (Scalable)
export const fontSize = {
  xs: 12,
  sm: 14,
  base: 16,
  lg: 18,
  xl: 20,
  '2xl': 24,
  '3xl': 30,
  '4xl': 36,
  '5xl': 48,
  '6xl': 60,
  '7xl': 72,
};

// Line Heights
export const lineHeight = {
  tight: 1.2,
  snug: 1.375,
  normal: 1.5,
  relaxed: 1.625,
  loose: 2,
};

// Letter Spacing
export const letterSpacing = {
  tighter: -0.05,
  tight: -0.025,
  normal: 0,
  wide: 0.025,
  wider: 0.05,
  widest: 0.1,
};

// Font Weights
export const fontWeight = {
  thin: '100',
  extralight: '200',
  light: '300',
  normal: '400',
  medium: '500',
  semibold: '600',
  bold: '700',
  extrabold: '800',
  black: '900',
} as const;

// Typography Styles (Pre-defined combinations)
export const typography = {
  // Display Styles (Large, Impact)
  displayLarge: {
    fontFamily: fontFamily.display.bold,
    fontSize: fontSize['6xl'],
    lineHeight: fontSize['6xl'] * lineHeight.tight,
    fontWeight: fontWeight.bold,
    letterSpacing: letterSpacing.tight,
  },
  displayMedium: {
    fontFamily: fontFamily.display.bold,
    fontSize: fontSize['5xl'],
    lineHeight: fontSize['5xl'] * lineHeight.tight,
    fontWeight: fontWeight.bold,
    letterSpacing: letterSpacing.tight,
  },
  displaySmall: {
    fontFamily: fontFamily.display.bold,
    fontSize: fontSize['4xl'],
    lineHeight: fontSize['4xl'] * lineHeight.tight,
    fontWeight: fontWeight.bold,
    letterSpacing: letterSpacing.normal,
  },

  // Heading Styles
  h1: {
    fontFamily: fontFamily.primary.bold,
    fontSize: fontSize['3xl'],
    lineHeight: fontSize['3xl'] * lineHeight.snug,
    fontWeight: fontWeight.bold,
    letterSpacing: letterSpacing.tight,
  },
  h2: {
    fontFamily: fontFamily.primary.bold,
    fontSize: fontSize['2xl'],
    lineHeight: fontSize['2xl'] * lineHeight.snug,
    fontWeight: fontWeight.bold,
    letterSpacing: letterSpacing.normal,
  },
  h3: {
    fontFamily: fontFamily.primary.semibold,
    fontSize: fontSize.xl,
    lineHeight: fontSize.xl * lineHeight.snug,
    fontWeight: fontWeight.semibold,
    letterSpacing: letterSpacing.normal,
  },
  h4: {
    fontFamily: fontFamily.primary.semibold,
    fontSize: fontSize.lg,
    lineHeight: fontSize.lg * lineHeight.normal,
    fontWeight: fontWeight.semibold,
    letterSpacing: letterSpacing.normal,
  },

  // Body Styles
  bodyLarge: {
    fontFamily: fontFamily.primary.regular,
    fontSize: fontSize.lg,
    lineHeight: fontSize.lg * lineHeight.relaxed,
    fontWeight: fontWeight.normal,
    letterSpacing: letterSpacing.normal,
  },
  body: {
    fontFamily: fontFamily.primary.regular,
    fontSize: fontSize.base,
    lineHeight: fontSize.base * lineHeight.normal,
    fontWeight: fontWeight.normal,
    letterSpacing: letterSpacing.normal,
  },
  bodySmall: {
    fontFamily: fontFamily.primary.regular,
    fontSize: fontSize.sm,
    lineHeight: fontSize.sm * lineHeight.normal,
    fontWeight: fontWeight.normal,
    letterSpacing: letterSpacing.normal,
  },

  // Label Styles
  labelLarge: {
    fontFamily: fontFamily.primary.medium,
    fontSize: fontSize.base,
    lineHeight: fontSize.base * lineHeight.normal,
    fontWeight: fontWeight.medium,
    letterSpacing: letterSpacing.wide,
  },
  label: {
    fontFamily: fontFamily.primary.medium,
    fontSize: fontSize.sm,
    lineHeight: fontSize.sm * lineHeight.normal,
    fontWeight: fontWeight.medium,
    letterSpacing: letterSpacing.wide,
  },
  labelSmall: {
    fontFamily: fontFamily.primary.medium,
    fontSize: fontSize.xs,
    lineHeight: fontSize.xs * lineHeight.normal,
    fontWeight: fontWeight.medium,
    letterSpacing: letterSpacing.wider,
  },

  // Button Styles
  buttonLarge: {
    fontFamily: fontFamily.primary.semibold,
    fontSize: fontSize.lg,
    lineHeight: fontSize.lg * lineHeight.normal,
    fontWeight: fontWeight.semibold,
    letterSpacing: letterSpacing.wide,
  },
  button: {
    fontFamily: fontFamily.primary.semibold,
    fontSize: fontSize.base,
    lineHeight: fontSize.base * lineHeight.normal,
    fontWeight: fontWeight.semibold,
    letterSpacing: letterSpacing.wide,
  },
  buttonSmall: {
    fontFamily: fontFamily.primary.semibold,
    fontSize: fontSize.sm,
    lineHeight: fontSize.sm * lineHeight.normal,
    fontWeight: fontWeight.semibold,
    letterSpacing: letterSpacing.wider,
  },

  // Caption/Helper Text
  caption: {
    fontFamily: fontFamily.primary.regular,
    fontSize: fontSize.xs,
    lineHeight: fontSize.xs * lineHeight.normal,
    fontWeight: fontWeight.normal,
    letterSpacing: letterSpacing.normal,
  },

  // Overline (All caps labels)
  overline: {
    fontFamily: fontFamily.primary.medium,
    fontSize: fontSize.xs,
    lineHeight: fontSize.xs * lineHeight.normal,
    fontWeight: fontWeight.medium,
    letterSpacing: letterSpacing.widest,
    textTransform: 'uppercase' as const,
  },

  // Monospace (Numbers, Balances)
  mono: {
    fontFamily: fontFamily.mono,
    fontSize: fontSize.base,
    lineHeight: fontSize.base * lineHeight.normal,
    fontWeight: fontWeight.medium,
    letterSpacing: letterSpacing.normal,
  },
  monoLarge: {
    fontFamily: fontFamily.mono,
    fontSize: fontSize.xl,
    lineHeight: fontSize.xl * lineHeight.normal,
    fontWeight: fontWeight.bold,
    letterSpacing: letterSpacing.normal,
  },
};
