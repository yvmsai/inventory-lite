import { http } from '../../shared/api/http';
import type { Dashboard } from '../../shared/api/types';

export const getDashboard = () => http<Dashboard>('/api/v1/dashboard');
