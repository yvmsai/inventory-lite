import { Link } from 'react-router-dom';
import type { Product } from '../../shared/api/types';
import { EmptyState } from '../../shared/components/EmptyState';
import { ErrorState } from '../../shared/components/ErrorState';
import { PageHeader } from '../../shared/components/PageHeader';
import { Spinner } from '../../shared/components/Spinner';
import { Table } from '../../shared/components/Table';
import { td, th } from '../../shared/components/styles';
import { formatMoney, formatNumber } from '../../shared/lib/format';
import { MovementTable } from '../stock';
import { useDashboard } from './hooks';

export function DashboardPage() {
  const { data, isPending, isError, refetch } = useDashboard();

  if (isPending) return <Spinner label="Loading dashboard" />;
  if (isError) return <ErrorState message="Could not load the dashboard." onRetry={refetch} />;

  const { totals, lowStock, lowStockCount, recentMovements } = data;
  return (
    <>
      <PageHeader title="Dashboard" />
      <dl className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <Stat label="Products" value={formatNumber(totals.productCount)} />
        <Stat label="Suppliers" value={formatNumber(totals.supplierCount)} />
        <Stat label="Total stock value" value={formatMoney(totals.totalStockValue)} />
      </dl>

      <section aria-labelledby="low-stock" className="mt-8">
        <h2 id="low-stock" className="mb-3 text-lg font-semibold">
          Low stock {lowStockCount > 0 && <span className="text-slate-500">({formatNumber(lowStockCount)})</span>}
        </h2>
        {lowStock.length === 0 ? (
          <EmptyState title="Nothing is low on stock" description="Every product is above its reorder level." />
        ) : (
          <LowStockTable products={lowStock} />
        )}
        {lowStockCount > lowStock.length && (
          <p className="mt-2 text-sm text-slate-600">
            Showing the {lowStock.length} lowest of {formatNumber(lowStockCount)}.
          </p>
        )}
      </section>

      <section aria-labelledby="recent-movements" className="mt-8">
        <h2 id="recent-movements" className="mb-3 text-lg font-semibold">
          Recent movements
        </h2>
        {recentMovements.length === 0 ? (
          <EmptyState title="No stock movements yet" description="Open a product to record stock in or out." />
        ) : (
          <MovementTable movements={recentMovements} caption="The 10 most recent stock movements" showProduct />
        )}
      </section>
    </>
  );
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-lg border border-slate-200 bg-white p-5">
      <dt className="text-sm text-slate-500">{label}</dt>
      <dd className="mt-1 text-2xl font-semibold">{value}</dd>
    </div>
  );
}

function LowStockTable({ products }: { products: Product[] }) {
  return (
    <Table
      caption="Products at or below their reorder level, lowest quantity first"
      head={
        <tr>
          <th className={th}>Product</th>
          <th className={th}>SKU</th>
          <th className={th}>Supplier</th>
          <th className={`${th} text-right`}>Qty</th>
          <th className={`${th} text-right`}>Reorder level</th>
        </tr>
      }
    >
      {products.map((p) => (
        <tr key={p.id}>
          <td className={td}>
            <Link to={`/products/${p.id}`} className="font-medium text-indigo-700 hover:underline">
              {p.name}
            </Link>
          </td>
          <td className={`${td} font-mono text-xs`}>{p.sku}</td>
          <td className={td}>{p.supplierName}</td>
          <td className={`${td} text-right font-semibold text-amber-800`}>{formatNumber(p.quantity)}</td>
          <td className={`${td} text-right`}>{formatNumber(p.reorderLevel)}</td>
        </tr>
      ))}
    </Table>
  );
}
