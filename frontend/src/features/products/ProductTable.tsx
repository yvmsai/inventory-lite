import { Link } from 'react-router-dom';
import type { Product } from '../../shared/api/types';
import { Badge } from '../../shared/components/Badge';
import { Button } from '../../shared/components/Button';
import { Table } from '../../shared/components/Table';
import { td, th } from '../../shared/components/styles';
import { formatMoney, formatNumber } from '../../shared/lib/format';

interface ProductTableProps {
  products: Product[];
  canEdit: boolean;
  onEdit: (product: Product) => void;
  onDelete: (product: Product) => void;
}

export function ProductTable({ products, canEdit, onEdit, onDelete }: ProductTableProps) {
  return (
    <Table
      caption="Products"
      head={
        <tr>
          <th className={th}>SKU</th>
          <th className={th}>Name</th>
          <th className={th}>Supplier</th>
          <th className={`${th} text-right`}>Qty</th>
          <th className={`${th} text-right`}>Unit price</th>
          {canEdit && <th className={`${th} text-right`}>Actions</th>}
        </tr>
      }
    >
      {products.map((p) => (
        <tr key={p.id}>
          <td className={`${td} font-mono text-xs`}>{p.sku}</td>
          <td className={td}>
            <Link to={`/products/${p.id}`} className="font-medium text-indigo-700 hover:underline">
              {p.name}
            </Link>
          </td>
          <td className={td}>{p.supplierName}</td>
          <td className={`${td} text-right whitespace-nowrap`}>
            {p.lowStock && (
              <span className="mr-2">
                <Badge tone="warning">Low stock</Badge>
              </span>
            )}
            {formatNumber(p.quantity)}
          </td>
          <td className={`${td} text-right`}>{formatMoney(p.unitPrice)}</td>
          {canEdit && (
            <td className={`${td} text-right whitespace-nowrap`}>
              <Button variant="ghost" onClick={() => onEdit(p)} aria-label={`Edit ${p.sku}`}>
                Edit
              </Button>
              <Button variant="ghost" onClick={() => onDelete(p)} aria-label={`Delete ${p.sku}`}>
                Delete
              </Button>
            </td>
          )}
        </tr>
      ))}
    </Table>
  );
}
