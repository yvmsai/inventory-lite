import { useState, type ReactNode } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ApiError } from '../../shared/api/http';
import { Badge } from '../../shared/components/Badge';
import { Button } from '../../shared/components/Button';
import { EmptyState } from '../../shared/components/EmptyState';
import { ErrorState } from '../../shared/components/ErrorState';
import { PageHeader } from '../../shared/components/PageHeader';
import { Spinner } from '../../shared/components/Spinner';
import { formatMoney, formatNumber } from '../../shared/lib/format';
import { useIsAdmin } from '../auth/useAuth';
import { MovementHistory, RecordMovementForm } from '../stock';
import { ProductFormDialog } from './ProductFormDialog';
import { useProduct } from './hooks';

export function ProductDetailPage() {
  const id = Number(useParams().id);
  const isAdmin = useIsAdmin();
  const { data: product, isPending, isError, error, refetch } = useProduct(id);
  const [editing, setEditing] = useState(false);

  if (isPending) return <Spinner label="Loading product" />;
  if (isError) {
    if (error instanceof ApiError && error.status === 404)
      return <EmptyState title="Product not found" action={<Link to="/products">Back to products</Link>} />;
    return <ErrorState message="Could not load the product." onRetry={refetch} />;
  }

  return (
    <>
      <p className="mb-2 text-sm">
        <Link to="/products" className="text-indigo-700 hover:underline">
          ← Products
        </Link>
      </p>
      <PageHeader title={product.name} actions={isAdmin && <Button onClick={() => setEditing(true)}>Edit product</Button>} />
      <div className="grid gap-6 lg:grid-cols-3">
        <section aria-labelledby="product-info" className="rounded-lg border border-slate-200 bg-white p-5 lg:col-span-2">
          <h2 id="product-info" className="sr-only">
            Product details
          </h2>
          <dl className="grid grid-cols-2 gap-4 text-sm sm:grid-cols-3">
            <Detail label="SKU" value={<span className="font-mono">{product.sku}</span>} />
            <Detail label="Supplier" value={product.supplierName} />
            <Detail label="Unit price" value={formatMoney(product.unitPrice)} />
            <Detail
              label="Quantity"
              value={
                <span className="flex items-center gap-2 text-lg font-semibold">
                  {formatNumber(product.quantity)}
                  {product.lowStock && <Badge tone="warning">Low stock</Badge>}
                </span>
              }
            />
            <Detail label="Reorder level" value={formatNumber(product.reorderLevel)} />
          </dl>
        </section>
        <section aria-labelledby="record-movement" className="rounded-lg border border-slate-200 bg-white p-5">
          <h2 id="record-movement" className="mb-3 font-semibold">
            Record movement
          </h2>
          <RecordMovementForm productId={product.id} available={product.quantity} />
        </section>
      </div>
      <section aria-labelledby="movement-history" className="mt-8">
        <h2 id="movement-history" className="mb-3 text-lg font-semibold">
          Movement history
        </h2>
        <MovementHistory productId={product.id} />
      </section>
      {editing && <ProductFormDialog product={product} onClose={() => setEditing(false)} />}
    </>
  );
}

function Detail({ label, value }: { label: string; value: ReactNode }) {
  return (
    <div>
      <dt className="text-slate-500">{label}</dt>
      <dd className="mt-1">{value}</dd>
    </div>
  );
}
