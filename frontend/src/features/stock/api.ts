import { http, query } from '../../shared/api/http';
import type { Page, RecordMovementRequest, StockMovement } from '../../shared/api/types';

export const listMovements = (productId: number, page: number, size = 20) =>
  http<Page<StockMovement>>(`/api/v1/products/${productId}/movements${query({ page, size })}`);

export const recordMovement = (productId: number, body: RecordMovementRequest) =>
  http<StockMovement>(`/api/v1/products/${productId}/movements`, { method: 'POST', body: JSON.stringify(body) });
