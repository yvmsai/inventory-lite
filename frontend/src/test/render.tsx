import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { render } from '@testing-library/react';
import type { ReactElement } from 'react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { AuthProvider } from '../features/auth/AuthProvider';
import type { User } from '../shared/api/types';

interface Options {
  /** The URL to start at. */
  route?: string;
  /** The route pattern the element is mounted at, for pages that read params. */
  path?: string;
  user?: User | null;
}

export function renderWithProviders(ui: ReactElement, { route = '/', path = '*', user = null }: Options = {}) {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false }, mutations: { retry: false } } });
  return render(
    <QueryClientProvider client={client}>
      <AuthProvider initialUser={user}>
        <MemoryRouter initialEntries={[route]}>
          <Routes>
            <Route path={path} element={ui} />
            {path !== '/login' && <Route path="/login" element={<p>Login screen</p>} />}
            {path !== '*' && path !== '/' && <Route path="/" element={<p>Home screen</p>} />}
          </Routes>
        </MemoryRouter>
      </AuthProvider>
    </QueryClientProvider>,
  );
}
