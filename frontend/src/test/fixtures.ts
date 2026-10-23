import type { Dashboard, Page, Product, StockMovement, Supplier, User } from '../shared/api/types';

export const API = 'http://api.test/api/v1';

export const admin: User = {
  id: 1,
  email: 'admin@inventory.local',
  fullName: 'Alex Admin',
  role: 'ADMIN',
  active: true,
  createdAt: '2026-01-01T10:00:00Z',
  updatedAt: '2026-01-01T10:00:00Z',
};

export const staff: User = { ...admin, id: 2, email: 'staff@inventory.local', fullName: 'Sam Staff', role: 'STAFF' };

export const supplier: Supplier = {
  id: 10,
  name: 'Northwind Traders',
  email: 'orders@northwind.example',
  phone: null,
  version: 0,
  createdAt: '2026-01-01T10:00:00Z',
  updatedAt: '2026-01-01T10:00:00Z',
};

export const product: Product = {
  id: 100,
  sku: 'NW-COF-1KG',
  name: 'Coffee beans 1 kg',
  unitPrice: '18.50',
  quantity: 42,
  reorderLevel: 10,
  lowStock: false,
  supplierId: 10,
  supplierName: 'Northwind Traders',
  version: 1,
  createdAt: '2026-01-01T10:00:00Z',
  updatedAt: '2026-01-01T10:00:00Z',
};

export const lowProduct: Product = { ...product, id: 101, sku: 'NW-TEA-100', name: 'Green tea', quantity: 3, lowStock: true };

export const movement: StockMovement = {
  id: 1000,
  productId: 100,
  productSku: 'NW-COF-1KG',
  productName: 'Coffee beans 1 kg',
  type: 'IN',
  quantity: 42,
  quantityAfter: 42,
  note: 'Opening stock',
  createdById: 1,
  createdByName: 'Alex Admin',
  createdAt: '2026-01-02T09:30:00Z',
};

export const dashboard: Dashboard = {
  totals: { productCount: 2, supplierCount: 1, totalStockValue: '1679.08' },
  lowStockCount: 1,
  lowStock: [lowProduct],
  recentMovements: [movement],
};

export function page<T>(content: T[], overrides: Partial<Page<T>> = {}): Page<T> {
  return { content, page: 0, size: 20, totalElements: content.length, totalPages: content.length ? 1 : 0, ...overrides };
}

export const problem = (status: number, detail: string, errors?: { field: string; message: string }[]) => ({
  type: 'about:blank',
  title: status === 400 ? 'Validation failed' : 'Error',
  status,
  detail,
  ...(errors ? { errors } : {}),
});
