import { createContext } from 'react';
import type { User } from '../../shared/api/types';

export interface AuthState {
  user: User | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

export const AuthContext = createContext<AuthState | null>(null);
