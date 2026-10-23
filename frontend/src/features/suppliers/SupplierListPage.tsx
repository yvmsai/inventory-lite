import { useState } from 'react';
import { errorMessage } from '../../shared/api/http';
import type { Supplier } from '../../shared/api/types';
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
import { SupplierFormDialog } from './SupplierFormDialog';
import { SupplierTable } from './SupplierTable';
import { useDeleteSupplier, useSuppliers } from './hooks';

type Editing = { mode: 'create' } | { mode: 'edit'; supplier: Supplier } | null;

export function SupplierListPage() {
  const isAdmin = useIsAdmin();
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const q = useDebouncedValue(search.trim());
  const { data, isPending, isError, refetch } = useSuppliers({ q, page });
  const [editing, setEditing] = useState<Editing>(null);
  const [deleting, setDeleting] = useState<Supplier | null>(null);
  const remove = useDeleteSupplier();

  const confirmDelete = () =>
    deleting && remove.mutate(deleting.id, { onSuccess: () => setDeleting(null) });

  return (
    <>
      <PageHeader
        title="Suppliers"
        actions={isAdmin && <Button onClick={() => setEditing({ mode: 'create' })}>Add supplier</Button>}
      />
      <div className="mb-4 max-w-sm">
        <label htmlFor="supplier-search" className="sr-only">
          Search suppliers by name
        </label>
        <input
          id="supplier-search"
          type="search"
          placeholder="Search by name"
          className={inputClass}
          value={search}
          onChange={(e) => {
            setSearch(e.target.value);
            setPage(0);
          }}
        />
      </div>

      {isPending ? (
        <Spinner label="Loading suppliers" />
      ) : isError ? (
        <ErrorState message="Could not load suppliers." onRetry={refetch} />
      ) : data.content.length === 0 ? (
        <EmptyState
          title={q ? 'No suppliers match your search' : 'No suppliers yet'}
          description={q ? 'Try a different name.' : undefined}
          action={isAdmin && !q && <Button onClick={() => setEditing({ mode: 'create' })}>Add supplier</Button>}
        />
      ) : (
        <div className="space-y-4">
          <SupplierTable
            suppliers={data.content}
            canEdit={isAdmin}
            onEdit={(supplier) => setEditing({ mode: 'edit', supplier })}
            onDelete={(supplier) => {
              remove.reset();
              setDeleting(supplier);
            }}
          />
          <Pagination page={data.page} totalPages={data.totalPages} totalElements={data.totalElements} onChange={setPage} />
        </div>
      )}

      {editing && (
        <SupplierFormDialog
          supplier={editing.mode === 'edit' ? editing.supplier : undefined}
          onClose={() => setEditing(null)}
        />
      )}
      {deleting && (
        <ConfirmDialog
          title="Delete supplier"
          message={`Delete "${deleting.name}"? This cannot be undone.`}
          confirmLabel="Delete"
          pending={remove.isPending}
          error={remove.isError ? errorMessage(remove.error) : undefined}
          onConfirm={confirmDelete}
          onCancel={() => setDeleting(null)}
        />
      )}
    </>
  );
}
