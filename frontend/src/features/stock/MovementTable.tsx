import { Link } from 'react-router-dom';
import type { StockMovement } from '../../shared/api/types';
import { Badge } from '../../shared/components/Badge';
import { Table } from '../../shared/components/Table';
import { td, th } from '../../shared/components/styles';
import { formatDateTime, formatNumber } from '../../shared/lib/format';

interface MovementTableProps {
  movements: StockMovement[];
  caption: string;
  /** Show which product each movement belongs to (the dashboard's cross-product list). */
  showProduct?: boolean;
}

export function MovementTable({ movements, caption, showProduct = false }: MovementTableProps) {
  return (
    <Table
      caption={caption}
      head={
        <tr>
          <th className={th}>When</th>
          {showProduct && <th className={th}>Product</th>}
          <th className={th}>Type</th>
          <th className={`${th} text-right`}>Qty</th>
          <th className={`${th} text-right`}>Balance</th>
          <th className={th}>By</th>
          <th className={th}>Note</th>
        </tr>
      }
    >
      {movements.map((m) => (
        <tr key={m.id}>
          <td className={`${td} whitespace-nowrap`}>{formatDateTime(m.createdAt)}</td>
          {showProduct && (
            <td className={td}>
              <Link to={`/products/${m.productId}`} className="text-indigo-700 hover:underline">
                {m.productName}
              </Link>{' '}
              <span className="font-mono text-xs text-slate-500">{m.productSku}</span>
            </td>
          )}
          <td className={td}>
            <Badge tone={m.type === 'IN' ? 'success' : 'info'}>{m.type}</Badge>
          </td>
          <td className={`${td} text-right`}>
            {m.type === 'IN' ? '+' : '−'}
            {formatNumber(m.quantity)}
          </td>
          <td className={`${td} text-right`}>{formatNumber(m.quantityAfter)}</td>
          <td className={td}>{m.createdByName}</td>
          <td className={`${td} text-slate-600`}>{m.note ?? ''}</td>
        </tr>
      ))}
    </Table>
  );
}
