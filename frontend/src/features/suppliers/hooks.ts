import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import type { SupplierRequest } from '../../shared/api/types';
import { createSupplier, deleteSupplier, listSuppliers, updateSupplier, type SupplierListParams } from './api';

export const supplierKeys = {
  all: ['suppliers'] as const,
  list: (params: SupplierListParams) => [...supplierKeys.all, 'list', params] as const,
};

export function useSuppliers(params: SupplierListParams) {
  return useQuery({ queryKey: supplierKeys.list(params), queryFn: () => listSuppliers(params), placeholderData: keepPreviousData });
}

export function useSaveSupplier() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, body }: { id?: number; body: SupplierRequest }) =>
      id === undefined ? createSupplier(body) : updateSupplier(id, body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: supplierKeys.all });
      // Product rows show the supplier name.
      qc.invalidateQueries({ queryKey: ['products'] });
    },
  });
}

export function useDeleteSupplier() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: deleteSupplier,
    onSuccess: () => qc.invalidateQueries({ queryKey: supplierKeys.all }),
  });
}
