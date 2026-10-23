import { http } from '../../shared/api/http';
import type { TokenResponse, User } from '../../shared/api/types';

export const login = (email: string, password: string) =>
  http<TokenResponse>('/api/v1/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) });

export const fetchMe = () => http<User>('/api/v1/auth/me');
