import type { ComponentType } from 'react';
import { Navigate, type RouteObject } from 'react-router-dom';
import { LoginPage } from '../features/auth/LoginPage';
import { RequireAuth } from '../features/auth/RequireAuth';
import { AppShell } from './layout/AppShell';

// Pages load on demand so the login screen stays small.
const page = <M,>(load: () => Promise<M>, pick: (m: M) => ComponentType) => async () => ({
  Component: pick(await load()),
});

export const routes: RouteObject[] = [
  { path: '/login', element: <LoginPage /> },
  {
    element: (
      <RequireAuth>
        <AppShell />
      </RequireAuth>
    ),
    children: [
      { index: true, lazy: page(() => import('../features/dashboard/DashboardPage'), (m) => m.DashboardPage) },
      { path: 'products', lazy: page(() => import('../features/products/ProductListPage'), (m) => m.ProductListPage) },
      {
        path: 'products/:id',
        lazy: page(() => import('../features/products/ProductDetailPage'), (m) => m.ProductDetailPage),
      },
      { path: 'suppliers', lazy: page(() => import('../features/suppliers/SupplierListPage'), (m) => m.SupplierListPage) },
      {
        path: 'users',
        lazy: page(
          () => import('../features/users/UserListPage'),
          (m) => () => (
            <RequireAuth role="ADMIN">
              <m.UserListPage />
            </RequireAuth>
          ),
        ),
      },
      { path: '*', element: <Navigate to="/" replace /> },
    ],
  },
];
