/**
 * Card Component
 * Professional card with glassmorphism effect
 */

import React from 'react';
import { View, StyleSheet, ViewStyle, TouchableOpacity } from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import { theme } from '../theme';

interface CardProps {
  children: React.ReactNode;
  variant?: 'default' | 'elevated' | 'glass' | 'gradient';
  padding?: 'none' | 'small' | 'medium' | 'large';
  onPress?: () => void;
  style?: ViewStyle;
}

export const Card: React.FC<CardProps> = ({
  children,
  variant = 'default',
  padding = 'medium',
  onPress,
  style,
}) => {
  const cardStyles = [
    styles.card,
    styles[variant],
    padding !== 'none' && styles[`padding_${padding}`],
    style,
  ];

  const content = <View style={cardStyles}>{children}</View>;

  if (variant === 'gradient') {
    const Container = onPress ? TouchableOpacity : View;
    return (
      <Container
        onPress={onPress}
        activeOpacity={onPress ? 0.8 : 1}
        style={[styles.card, padding !== 'none' && styles[`padding_${padding}`], style]}
      >
        <LinearGradient
          colors={['rgba(255, 119, 0, 0.1)', 'rgba(0, 128, 255, 0.1)']}
          start={{ x: 0, y: 0 }}
          end={{ x: 1, y: 1 }}
          style={styles.gradientBackground}
        >
          {children}
        </LinearGradient>
      </Container>
    );
  }

  if (onPress) {
    return (
      <TouchableOpacity onPress={onPress} activeOpacity={0.8} style={cardStyles}>
        {children}
      </TouchableOpacity>
    );
  }

  return content;
};

const styles = StyleSheet.create({
  card: {
    borderRadius: theme.borderRadius.xl,
    overflow: 'hidden',
  },

  // Variants
  default: {
    backgroundColor: theme.colors.background.card,
    borderWidth: 1,
    borderColor: theme.colors.neutral[800],
  },
  elevated: {
    backgroundColor: theme.colors.background.elevated,
    ...theme.shadows.large,
  },
  glass: {
    backgroundColor: 'rgba(26, 31, 46, 0.8)',
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.1)',
    ...theme.shadows.medium,
  },
  gradient: {
    backgroundColor: 'transparent',
  },

  // Padding
  padding_small: {
    padding: theme.spacing[3],
  },
  padding_medium: {
    padding: theme.spacing[4],
  },
  padding_large: {
    padding: theme.spacing[6],
  },

  // Gradient background
  gradientBackground: {
    flex: 1,
    borderRadius: theme.borderRadius.xl,
  },
});
