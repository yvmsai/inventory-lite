import type { ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import type { Role } from '../../shared/api/types';
import { useAuth } from './useAuth';

interface RequireAuthProps {
  children: ReactNode;
  role?: Role;
}

/** Redirects to /login when signed out, and home when the user lacks the role. The API enforces both anyway. */
export function RequireAuth({ children, role }: RequireAuthProps) {
  const { user } = useAuth();
  const location = useLocation();
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  if (role && user.role !== role) return <Navigate to="/" replace />;
  return <>{children}</>;
}
