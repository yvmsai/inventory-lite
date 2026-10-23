import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import type { RecordMovementRequest } from '../../shared/api/types';
import { listMovements, recordMovement } from './api';

export const movementKeys = {
  all: ['movements'] as const,
  history: (productId: number, page: number) => [...movementKeys.all, productId, page] as const,
};

export function useMovementHistory(productId: number, page: number) {
  return useQuery({
    queryKey: movementKeys.history(productId, page),
    queryFn: () => listMovements(productId, page),
    placeholderData: keepPreviousData,
  });
}

export function useRecordMovement(productId: number) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: RecordMovementRequest) => recordMovement(productId, body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: movementKeys.all });
      // The product's quantity, low-stock flag and version changed, and so did the dashboard.
      qc.invalidateQueries({ queryKey: ['products'] });
      qc.invalidateQueries({ queryKey: ['dashboard'] });
    },
  });
}
