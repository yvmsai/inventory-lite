import type { Supplier } from '../../shared/api/types';
import { Button } from '../../shared/components/Button';
import { Table } from '../../shared/components/Table';
import { td, th } from '../../shared/components/styles';

interface SupplierTableProps {
  suppliers: Supplier[];
  canEdit: boolean;
  onEdit: (supplier: Supplier) => void;
  onDelete: (supplier: Supplier) => void;
}

export function SupplierTable({ suppliers, canEdit, onEdit, onDelete }: SupplierTableProps) {
  return (
    <Table
      caption="Suppliers"
      head={
        <tr>
          <th className={th}>Name</th>
          <th className={th}>Email</th>
          <th className={th}>Phone</th>
          {canEdit && <th className={`${th} text-right`}>Actions</th>}
        </tr>
      }
    >
      {suppliers.map((s) => (
        <tr key={s.id}>
          <td className={`${td} font-medium`}>{s.name}</td>
          <td className={td}>{s.email ?? '—'}</td>
          <td className={td}>{s.phone ?? '—'}</td>
          {canEdit && (
            <td className={`${td} text-right whitespace-nowrap`}>
              <Button variant="ghost" onClick={() => onEdit(s)} aria-label={`Edit ${s.name}`}>
                Edit
              </Button>
              <Button variant="ghost" onClick={() => onDelete(s)} aria-label={`Delete ${s.name}`}>
                Delete
              </Button>
            </td>
          )}
        </tr>
      ))}
    </Table>
  );
}
