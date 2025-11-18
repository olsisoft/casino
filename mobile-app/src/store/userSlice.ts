import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import apiClient from '../config/api';

interface UserState {
  profile: any | null;
  balance: any | null;
  isLoading: boolean;
  error: string | null;
}

const initialState: UserState = {
  profile: null,
  balance: null,
  isLoading: false,
  error: null,
};

export const fetchProfile = createAsyncThunk('user/fetchProfile', async () => {
  const response = await apiClient.get('/users/profile');
  return response.data;
});

export const fetchBalance = createAsyncThunk('user/fetchBalance', async () => {
  const response = await apiClient.get('/users/balance');
  return response.data;
});

const userSlice = createSlice({
  name: 'user',
  initialState,
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchProfile.fulfilled, (state, action) => {
        state.profile = action.payload;
      })
      .addCase(fetchBalance.fulfilled, (state, action) => {
        state.balance = action.payload;
      });
  },
});

export default userSlice.reducer;
