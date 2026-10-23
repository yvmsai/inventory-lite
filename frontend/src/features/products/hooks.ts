import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import type { ProductRequest } from '../../shared/api/types';
import {
  createProduct,
  deleteProduct,
  getProduct,
  listProducts,
  listSupplierOptions,
  updateProduct,
  type ProductListParams,
} from './api';

export const productKeys = {
  all: ['products'] as const,
  list: (params: ProductListParams) => [...productKeys.all, 'list', params] as const,
  detail: (id: number) => [...productKeys.all, 'detail', id] as const,
};

export function useProducts(params: ProductListParams) {
  return useQuery({ queryKey: productKeys.list(params), queryFn: () => listProducts(params), placeholderData: keepPreviousData });
}

export function useProduct(id: number) {
  return useQuery({ queryKey: productKeys.detail(id), queryFn: () => getProduct(id) });
}

export function useSupplierOptions() {
  return useQuery({ queryKey: ['suppliers', 'options'], queryFn: listSupplierOptions, staleTime: 60_000 });
}

export function useSaveProduct() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, body }: { id?: number; body: ProductRequest }) =>
      id === undefined ? createProduct(body) : updateProduct(id, body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: productKeys.all });
      qc.invalidateQueries({ queryKey: ['dashboard'] });
    },
  });
}

export function useDeleteProduct() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: deleteProduct,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: productKeys.all });
      qc.invalidateQueries({ queryKey: ['dashboard'] });
    },
  });
}
