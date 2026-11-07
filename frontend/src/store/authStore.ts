import { create } from 'zustand';
import { User } from '../types';
import { apiClient } from '../lib/api';

interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (userData: User, token: string, refreshToken: string) => void;
  logout: () => void;
  fetchCurrentUser: () => Promise<void>;
  updateUser: (user: User) => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: localStorage.getItem('user_data') ? JSON.parse(localStorage.getItem('user_data')!) : null,
  token: localStorage.getItem('access_token'),
  isAuthenticated: !!localStorage.getItem('access_token'),
  isLoading: false,

  login: (userData: User, token: string, refreshToken: string) => {
    localStorage.setItem('access_token', token);
    localStorage.setItem('refresh_token', refreshToken);
    localStorage.setItem('user_data', JSON.stringify(userData));
    set({ user: userData, token, isAuthenticated: true });
  },

  logout: () => {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('user_data');
    set({ user: null, token: null, isAuthenticated: false });
  },

  fetchCurrentUser: async () => {
    set({ isLoading: true });
    try {
      const response: any = await apiClient.get('/users/me');
      if (response?.data) {
        localStorage.setItem('user_data', JSON.stringify(response.data));
        set({ user: response.data, isAuthenticated: true, isLoading: false });
      }
    } catch {
      set({ user: null, token: null, isAuthenticated: false, isLoading: false });
    }
  },

  updateUser: (updatedUser: User) => {
    localStorage.setItem('user_data', JSON.stringify(updatedUser));
    set({ user: updatedUser });
  },
}));

