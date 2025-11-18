import React, { useEffect } from 'react';
import {
  View,
  Text,
  FlatList,
  TouchableOpacity,
  StyleSheet,
  Image,
} from 'react-native';
import { useDispatch, useSelector } from 'react-redux';
import { fetchGames } from '../store/gameSlice';
import { fetchBalance } from '../store/userSlice';
import { AppDispatch, RootState } from '../store';

export default function HomeScreen({ navigation }: any) {
  const dispatch = useDispatch<AppDispatch>();
  const { games, isLoading } = useSelector((state: RootState) => state.game);
  const { balance } = useSelector((state: RootState) => state.user);

  useEffect(() => {
    dispatch(fetchGames());
    dispatch(fetchBalance());
  }, []);

  const renderGame = ({ item }: any) => (
    <TouchableOpacity
      style={styles.gameCard}
      onPress={() => navigation.navigate('Game', { game: item })}
    >
      <View style={styles.gameInfo}>
        <Text style={styles.gameName}>{item.gameName}</Text>
        <Text style={styles.gameType}>{item.gameType}</Text>
        <Text style={styles.gamePlayers}>{item.activePlayers} players</Text>
      </View>
    </TouchableOpacity>
  );

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Casino Games</Text>
        <View style={styles.balanceContainer}>
          <Text style={styles.balanceLabel}>Balance:</Text>
          <Text style={styles.balanceAmount}>
            ${balance?.realBalance || 0}
          </Text>
        </View>
      </View>

      <FlatList
        data={games}
        renderItem={renderGame}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.gameList}
        refreshing={isLoading}
        onRefresh={() => {
          dispatch(fetchGames());
          dispatch(fetchBalance());
        }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1a1a2e',
  },
  header: {
    padding: 20,
    backgroundColor: '#16213e',
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#fff',
    marginBottom: 10,
  },
  balanceContainer: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  balanceLabel: {
    color: '#999',
    fontSize: 16,
    marginRight: 10,
  },
  balanceAmount: {
    color: '#4ecca3',
    fontSize: 20,
    fontWeight: 'bold',
  },
  gameList: {
    padding: 20,
  },
  gameCard: {
    backgroundColor: '#16213e',
    borderRadius: 15,
    padding: 20,
    marginBottom: 15,
    flexDirection: 'row',
    alignItems: 'center',
  },
  gameInfo: {
    flex: 1,
  },
  gameName: {
    color: '#fff',
    fontSize: 20,
    fontWeight: 'bold',
    marginBottom: 5,
  },
  gameType: {
    color: '#999',
    fontSize: 14,
    marginBottom: 5,
  },
  gamePlayers: {
    color: '#4ecca3',
    fontSize: 12,
  },
});
