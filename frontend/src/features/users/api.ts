import { http, query } from '../../shared/api/http';
import type { CreateUserRequest, Page, UpdateUserRequest, User } from '../../shared/api/types';

export const listUsers = (page: number, size = 20) =>
  http<Page<User>>(`/api/v1/users${query({ page, size, sort: 'email,asc' })}`);

export const createUser = (body: CreateUserRequest) =>
  http<User>('/api/v1/users', { method: 'POST', body: JSON.stringify(body) });

export const updateUser = (id: number, body: UpdateUserRequest) =>
  http<User>(`/api/v1/users/${id}`, { method: 'PATCH', body: JSON.stringify(body) });

export const setUserActive = (id: number, active: boolean) =>
  http<User>(`/api/v1/users/${id}/${active ? 'activate' : 'deactivate'}`, { method: 'POST' });
