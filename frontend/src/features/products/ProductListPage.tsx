import { useState } from 'react';
import { errorMessage } from '../../shared/api/http';
import type { Product } from '../../shared/api/types';
import { Button } from '../../shared/components/Button';
import { ConfirmDialog } from '../../shared/components/ConfirmDialog';
import { EmptyState } from '../../shared/components/EmptyState';
import { ErrorState } from '../../shared/components/ErrorState';
import { PageHeader } from '../../shared/components/PageHeader';
import { Pagination } from '../../shared/components/Pagination';
import { Spinner } from '../../shared/components/Spinner';
import { inputClass } from '../../shared/components/styles';
import { useDebouncedValue } from '../../shared/lib/useDebouncedValue';
import { useIsAdmin } from '../auth/useAuth';
import { ProductFormDialog } from './ProductFormDialog';
import { ProductTable } from './ProductTable';
import { useDeleteProduct, useProducts, useSupplierOptions } from './hooks';

type Editing = { mode: 'create' } | { mode: 'edit'; product: Product } | null;

export function ProductListPage() {
  const isAdmin = useIsAdmin();
  const [search, setSearch] = useState('');
  const [supplierId, setSupplierId] = useState('');
  const [page, setPage] = useState(0);
  const q = useDebouncedValue(search.trim());
  const { data, isPending, isError, refetch } = useProducts({
    q,
    supplierId: supplierId ? Number(supplierId) : undefined,
    page,
  });
  const suppliers = useSupplierOptions();
  const [editing, setEditing] = useState<Editing>(null);
  const [deleting, setDeleting] = useState<Product | null>(null);
  const remove = useDeleteProduct();
  const filtered = Boolean(q || supplierId);

  return (
    <>
      <PageHeader
        title="Products"
        actions={isAdmin && <Button onClick={() => setEditing({ mode: 'create' })}>Add product</Button>}
      />
      <div className="mb-4 flex flex-wrap gap-3">
        <div className="w-full max-w-sm">
          <label htmlFor="product-search" className="sr-only">
            Search products by name or SKU
          </label>
          <input
            id="product-search"
            type="search"
            placeholder="Search by name or SKU"
            className={inputClass}
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(0);
            }}
          />
        </div>
        <div className="w-full max-w-xs">
          <label htmlFor="product-supplier-filter" className="sr-only">
            Filter by supplier
          </label>
          <select
            id="product-supplier-filter"
            className={inputClass}
            value={supplierId}
            onChange={(e) => {
              setSupplierId(e.target.value);
              setPage(0);
            }}
          >
            <option value="">All suppliers</option>
            {suppliers.data?.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>
        </div>
      </div>

      {isPending ? (
        <Spinner label="Loading products" />
      ) : isError ? (
        <ErrorState message="Could not load products." onRetry={refetch} />
      ) : data.content.length === 0 ? (
        <EmptyState
          title={filtered ? 'No products match your filters' : 'No products yet'}
          description={filtered ? 'Try a different search or supplier.' : undefined}
          action={isAdmin && !filtered && <Button onClick={() => setEditing({ mode: 'create' })}>Add product</Button>}
        />
      ) : (
        <div className="space-y-4">
          <ProductTable
            products={data.content}
            canEdit={isAdmin}
            onEdit={(product) => setEditing({ mode: 'edit', product })}
            onDelete={(product) => {
              remove.reset();
              setDeleting(product);
            }}
          />
          <Pagination page={data.page} totalPages={data.totalPages} totalElements={data.totalElements} onChange={setPage} />
        </div>
      )}

      {editing && (
        <ProductFormDialog product={editing.mode === 'edit' ? editing.product : undefined} onClose={() => setEditing(null)} />
      )}
      {deleting && (
        <ConfirmDialog
          title="Delete product"
          message={`Delete ${deleting.sku} "${deleting.name}"? This cannot be undone.`}
          confirmLabel="Delete"
          pending={remove.isPending}
          error={remove.isError ? errorMessage(remove.error) : undefined}
          onConfirm={() => remove.mutate(deleting.id, { onSuccess: () => setDeleting(null) })}
          onCancel={() => setDeleting(null)}
        />
      )}
    </>
  );
}
