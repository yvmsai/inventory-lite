import { http, query } from '../../shared/api/http';
import type { Page, Supplier, SupplierRequest } from '../../shared/api/types';

export interface SupplierListParams {
  q?: string;
  page: number;
  size?: number;
}

export const listSuppliers = ({ q, page, size = 20 }: SupplierListParams) =>
  http<Page<Supplier>>(`/api/v1/suppliers${query({ q, page, size, sort: 'name,asc' })}`);

export const createSupplier = (body: SupplierRequest) =>
  http<Supplier>('/api/v1/suppliers', { method: 'POST', body: JSON.stringify(body) });

export const updateSupplier = (id: number, body: SupplierRequest) =>
  http<Supplier>(`/api/v1/suppliers/${id}`, { method: 'PUT', body: JSON.stringify(body) });

export const deleteSupplier = (id: number) => http<void>(`/api/v1/suppliers/${id}`, { method: 'DELETE' });
