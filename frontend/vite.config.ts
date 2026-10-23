/// <reference types="vitest/config" />
import tailwindcss from '@tailwindcss/vite';
import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';

// `npm run dev` proxies /api to the backend; override with API_PROXY_TARGET if it runs on another port.
const apiTarget = process.env.API_PROXY_TARGET ?? 'http://localhost:8080';

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: { proxy: { '/api': apiTarget } },
  test: {
    environment: 'jsdom',
    setupFiles: './src/test/setup.ts',
    globals: true,
    env: { VITE_API_BASE_URL: 'http://api.test' },
    css: false,
    coverage: { provider: 'v8', include: ['src/**/*.{ts,tsx}'], exclude: ['src/test/**', 'src/**/*.test.tsx', 'src/shared/api/schema.d.ts', 'src/main.tsx'] },
  },
});
