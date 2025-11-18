/**
 * Game Lobby Screen
 * Professional game selection with categories and search
 */

import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  SafeAreaView,
  FlatList,
  TouchableOpacity,
  Image,
} from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import { theme } from '../../theme';
import { Card } from '../../components/Card';
import { Input } from '../../components/Input';

interface Game {
  id: string;
  name: string;
  category: string;
  image: string;
  players: number;
  minBet: number;
  maxBet: number;
  rtp: number;
}

const MOCK_GAMES: Game[] = [
  {
    id: '1',
    name: 'Blackjack',
    category: 'Table Games',
    image: '🃏',
    players: 1247,
    minBet: 1,
    maxBet: 1000,
    rtp: 99.5,
  },
  {
    id: '2',
    name: 'Roulette',
    category: 'Table Games',
    image: '🎰',
    players: 892,
    minBet: 1,
    maxBet: 500,
    rtp: 97.3,
  },
  {
    id: '3',
    name: 'Video Poker',
    category: 'Video Poker',
    image: '🎴',
    players: 543,
    minBet: 0.5,
    maxBet: 100,
    rtp: 98.9,
  },
  {
    id: '4',
    name: 'Dice',
    category: 'Dice Games',
    image: '🎲',
    players: 324,
    minBet: 0.1,
    maxBet: 1000,
    rtp: 99.0,
  },
  {
    id: '5',
    name: 'Mines',
    category: 'Strategy',
    image: '💣',
    players: 678,
    minBet: 1,
    maxBet: 100,
    rtp: 97.0,
  },
  {
    id: '6',
    name: 'Crash',
    category: 'Multiplier',
    image: '🚀',
    players: 1543,
    minBet: 1,
    maxBet: 500,
    rtp: 99.0,
  },
];

const CATEGORIES = ['All', 'Table Games', 'Slots', 'Video Poker', 'Dice Games', 'Strategy', 'Multiplier'];

export const GameLobbyScreen: React.FC = () => {
  const [selectedCategory, setSelectedCategory] = useState('All');
  const [searchQuery, setSearchQuery] = useState('');

  const filteredGames = MOCK_GAMES.filter((game) => {
    const matchesCategory = selectedCategory === 'All' || game.category === selectedCategory;
    const matchesSearch = game.name.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesCategory && matchesSearch;
  });

  const renderGameCard = ({ item }: { item: Game }) => (
    <TouchableOpacity activeOpacity={0.9} style={styles.gameCardWrapper}>
      <Card variant="glass" padding="none" style={styles.gameCard}>
        <LinearGradient
          colors={['rgba(255, 119, 0, 0.2)', 'rgba(0, 128, 255, 0.2)']}
          start={{ x: 0, y: 0 }}
          end={{ x: 1, y: 1 }}
          style={styles.gameCardGradient}
        >
          <View style={styles.gameImageContainer}>
            <Text style={styles.gameImage}>{item.image}</Text>
            <View style={styles.playersBadge}>
              <Text style={styles.playersText}>👥 {item.players}</Text>
            </View>
          </View>

          <View style={styles.gameInfo}>
            <Text style={styles.gameName}>{item.name}</Text>
            <Text style={styles.gameCategory}>{item.category}</Text>

            <View style={styles.gameStats}>
              <View style={styles.statItem}>
                <Text style={styles.statLabel}>Min</Text>
                <Text style={styles.statValue}>${item.minBet}</Text>
              </View>
              <View style={styles.statDivider} />
              <View style={styles.statItem}>
                <Text style={styles.statLabel}>Max</Text>
                <Text style={styles.statValue}>${item.maxBet}</Text>
              </View>
              <View style={styles.statDivider} />
              <View style={styles.statItem}>
                <Text style={styles.statLabel}>RTP</Text>
                <Text style={styles.statValue}>{item.rtp}%</Text>
              </View>
            </View>
          </View>

          <View style={styles.playButton}>
            <LinearGradient
              colors={theme.gradients.primary}
              start={{ x: 0, y: 0 }}
              end={{ x: 1, y: 0 }}
              style={styles.playButtonGradient}
            >
              <Text style={styles.playButtonText}>Play</Text>
            </LinearGradient>
          </View>
        </LinearGradient>
      </Card>
    </TouchableOpacity>
  );

  return (
    <LinearGradient
      colors={[theme.colors.background.primary, theme.colors.background.secondary]}
      style={styles.gradient}
    >
      <SafeAreaView style={styles.container}>
        {/* Header */}
        <View style={styles.header}>
          <Text style={styles.headerTitle}>Game Lobby</Text>
          <TouchableOpacity style={styles.filterButton}>
            <Text style={styles.filterIcon}>⚙️</Text>
          </TouchableOpacity>
        </View>

        {/* Search */}
        <View style={styles.searchContainer}>
          <Input
            placeholder="Search games..."
            value={searchQuery}
            onChangeText={setSearchQuery}
            containerStyle={styles.searchInput}
          />
        </View>

        {/* Categories */}
        <ScrollView
          horizontal
          showsHorizontalScrollIndicator={false}
          contentContainerStyle={styles.categoriesContainer}
        >
          {CATEGORIES.map((category) => (
            <TouchableOpacity
              key={category}
              onPress={() => setSelectedCategory(category)}
              activeOpacity={0.7}
            >
              <View
                style={[
                  styles.categoryChip,
                  selectedCategory === category && styles.categoryChipActive,
                ]}
              >
                {selectedCategory === category && (
                  <LinearGradient
                    colors={theme.gradients.primary}
                    start={{ x: 0, y: 0 }}
                    end={{ x: 1, y: 0 }}
                    style={StyleSheet.absoluteFillObject}
                  />
                )}
                <Text
                  style={[
                    styles.categoryText,
                    selectedCategory === category && styles.categoryTextActive,
                  ]}
                >
                  {category}
                </Text>
              </View>
            </TouchableOpacity>
          ))}
        </ScrollView>

        {/* Games Grid */}
        <FlatList
          data={filteredGames}
          renderItem={renderGameCard}
          keyExtractor={(item) => item.id}
          numColumns={2}
          contentContainerStyle={styles.gamesGrid}
          showsVerticalScrollIndicator={false}
          columnWrapperStyle={styles.gameRow}
        />
      </SafeAreaView>
    </LinearGradient>
  );
};

const styles = StyleSheet.create({
  gradient: {
    flex: 1,
  },
  container: {
    flex: 1,
  },

  // Header
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: theme.spacing[6],
    paddingVertical: theme.spacing[4],
  },
  headerTitle: {
    ...theme.typography.h1,
    color: theme.colors.text.primary,
  },
  filterButton: {
    width: 40,
    height: 40,
    borderRadius: theme.borderRadius.lg,
    backgroundColor: theme.colors.background.card,
    alignItems: 'center',
    justifyContent: 'center',
  },
  filterIcon: {
    fontSize: 20,
  },

  // Search
  searchContainer: {
    paddingHorizontal: theme.spacing[6],
    marginBottom: theme.spacing[4],
  },
  searchInput: {
    marginBottom: 0,
  },

  // Categories
  categoriesContainer: {
    paddingHorizontal: theme.spacing[6],
    paddingBottom: theme.spacing[4],
  },
  categoryChip: {
    paddingHorizontal: theme.spacing[4],
    paddingVertical: theme.spacing[2],
    borderRadius: theme.borderRadius.full,
    marginRight: theme.spacing[2],
    backgroundColor: theme.colors.background.card,
    borderWidth: 1,
    borderColor: theme.colors.neutral[700],
    overflow: 'hidden',
  },
  categoryChipActive: {
    borderColor: theme.colors.primary[500],
  },
  categoryText: {
    ...theme.typography.label,
    color: theme.colors.text.secondary,
  },
  categoryTextActive: {
    color: theme.colors.text.primary,
    fontWeight: '600',
  },

  // Games Grid
  gamesGrid: {
    padding: theme.spacing[4],
    paddingBottom: theme.spacing[20],
  },
  gameRow: {
    justifyContent: 'space-between',
  },
  gameCardWrapper: {
    flex: 0.48,
    marginBottom: theme.spacing[4],
  },
  gameCard: {
    overflow: 'hidden',
  },
  gameCardGradient: {
    borderRadius: theme.borderRadius.xl,
  },
  gameImageContainer: {
    height: 120,
    alignItems: 'center',
    justifyContent: 'center',
    position: 'relative',
  },
  gameImage: {
    fontSize: 60,
  },
  playersBadge: {
    position: 'absolute',
    top: theme.spacing[2],
    right: theme.spacing[2],
    backgroundColor: 'rgba(0, 0, 0, 0.6)',
    paddingHorizontal: theme.spacing[2],
    paddingVertical: theme.spacing[1],
    borderRadius: theme.borderRadius.base,
  },
  playersText: {
    ...theme.typography.caption,
    color: theme.colors.text.primary,
  },
  gameInfo: {
    padding: theme.spacing[3],
  },
  gameName: {
    ...theme.typography.h4,
    color: theme.colors.text.primary,
    marginBottom: theme.spacing[1],
  },
  gameCategory: {
    ...theme.typography.caption,
    color: theme.colors.text.tertiary,
    marginBottom: theme.spacing[3],
  },
  gameStats: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  statItem: {
    flex: 1,
    alignItems: 'center',
  },
  statLabel: {
    ...theme.typography.caption,
    color: theme.colors.text.tertiary,
    marginBottom: theme.spacing[1],
  },
  statValue: {
    ...theme.typography.labelSmall,
    color: theme.colors.primary[500],
    fontWeight: '600',
  },
  statDivider: {
    width: 1,
    height: 24,
    backgroundColor: theme.colors.neutral[700],
  },
  playButton: {
    margin: theme.spacing[3],
    marginTop: 0,
    borderRadius: theme.borderRadius.lg,
    overflow: 'hidden',
  },
  playButtonGradient: {
    paddingVertical: theme.spacing[3],
    alignItems: 'center',
  },
  playButtonText: {
    ...theme.typography.button,
    color: theme.colors.text.primary,
  },
});
