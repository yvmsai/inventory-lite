import { useQueryClient } from '@tanstack/react-query';
import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react';
import { setAccessToken, setUnauthorizedHandler } from '../../shared/api/http';
import type { User } from '../../shared/api/types';
import { fetchMe, login as loginRequest } from './api';
import { AuthContext } from './AuthContext';

/** The access token lives only in memory (ADR 0002): a page reload means logging in again. */
export function AuthProvider({ children, initialUser = null }: { children: ReactNode; initialUser?: User | null }) {
  const [user, setUser] = useState<User | null>(initialUser);
  const queryClient = useQueryClient();

  const logout = useCallback(() => {
    setAccessToken(null);
    setUser(null);
    queryClient.clear();
  }, [queryClient]);

  useEffect(() => setUnauthorizedHandler(logout), [logout]);

  const login = useCallback(async (email: string, password: string) => {
    const token = await loginRequest(email, password);
    setAccessToken(token.accessToken);
    try {
      setUser(await fetchMe());
    } catch (e) {
      setAccessToken(null);
      throw e;
    }
  }, []);

  const value = useMemo(() => ({ user, login, logout }), [user, login, logout]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
