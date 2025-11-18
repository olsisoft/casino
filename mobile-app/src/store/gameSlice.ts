import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import apiClient from '../config/api';

interface GameState {
  games: any[];
  currentSession: any | null;
  isLoading: boolean;
  error: string | null;
}

const initialState: GameState = {
  games: [],
  currentSession: null,
  isLoading: false,
  error: null,
};

export const fetchGames = createAsyncThunk('game/fetchGames', async () => {
  const response = await apiClient.get('/games');
  return response.data;
});

export const startSession = createAsyncThunk(
  'game/startSession',
  async (data: { gameCode: string; startingBalance: number; balanceType: string }) => {
    const response = await apiClient.post('/games/sessions/start', data);
    return response.data;
  }
);

export const playRound = createAsyncThunk(
  'game/playRound',
  async (data: { sessionId: string; betAmount: number }) => {
    const response = await apiClient.post('/games/play', data);
    return response.data;
  }
);

const gameSlice = createSlice({
  name: 'game',
  initialState,
  reducers: {
    clearSession: (state) => {
      state.currentSession = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchGames.pending, (state) => {
        state.isLoading = true;
      })
      .addCase(fetchGames.fulfilled, (state, action) => {
        state.isLoading = false;
        state.games = action.payload;
      })
      .addCase(fetchGames.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.error.message || 'Failed to fetch games';
      })
      .addCase(startSession.fulfilled, (state, action) => {
        state.currentSession = action.payload;
      });
  },
});

export const { clearSession } = gameSlice.actions;
export default gameSlice.reducer;
