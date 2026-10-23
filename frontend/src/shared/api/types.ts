import type { components } from './schema';

type Schemas = components['schemas'];

export type Page<T> = { content: T[]; page: number; size: number; totalElements: number; totalPages: number };
export type Role = Schemas['UserResponse']['role'];
export type MovementType = Schemas['StockMovementResponse']['type'];
export type User = Schemas['UserResponse'];
export type Supplier = Schemas['SupplierResponse'];
export type SupplierRequest = Schemas['SupplierRequest'];
export type Product = Schemas['ProductResponse'];
export type ProductRequest = Schemas['ProductRequest'];
export type StockMovement = Schemas['StockMovementResponse'];
export type RecordMovementRequest = Schemas['RecordMovementRequest'];
export type Dashboard = Schemas['DashboardResponse'];
export type TokenResponse = Schemas['TokenResponse'];
export type CreateUserRequest = Schemas['CreateUserRequest'];
export type UpdateUserRequest = Schemas['UpdateUserRequest'];
