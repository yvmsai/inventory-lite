import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { AuthProvider } from '../features/auth/AuthProvider';
import { ApiError } from '../shared/api/http';
import { routes } from './routes';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 15_000,
      // Don't retry client errors (401/403/404); retry transient failures once.
      retry: (count, error) => !(error instanceof ApiError && error.status < 500) && count < 1,
    },
  },
});

const router = createBrowserRouter(routes);

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <RouterProvider router={router} />
      </AuthProvider>
    </QueryClientProvider>
  );
}
