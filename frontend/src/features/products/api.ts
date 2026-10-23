import { http, query } from '../../shared/api/http';
import type { Page, Product, ProductRequest, Supplier } from '../../shared/api/types';

export interface ProductListParams {
  q?: string;
  supplierId?: number;
  page: number;
  size?: number;
}

export const listProducts = ({ q, supplierId, page, size = 20 }: ProductListParams) =>
  http<Page<Product>>(`/api/v1/products${query({ q, supplierId, page, size, sort: 'name,asc' })}`);

export const getProduct = (id: number) => http<Product>(`/api/v1/products/${id}`);

export const createProduct = (body: ProductRequest) =>
  http<Product>('/api/v1/products', { method: 'POST', body: JSON.stringify(body) });

export const updateProduct = (id: number, body: ProductRequest) =>
  http<Product>(`/api/v1/products/${id}`, { method: 'PUT', body: JSON.stringify(body) });

export const deleteProduct = (id: number) => http<void>(`/api/v1/products/${id}`, { method: 'DELETE' });

/** Supplier choices for the filter and form (a small business has well under 100 suppliers). */
export const listSupplierOptions = () =>
  http<Page<Supplier>>(`/api/v1/suppliers${query({ size: 100, sort: 'name,asc' })}`).then((p) => p.content);
