import { z } from 'zod';

export const supplierSchema = z.object({
  name: z.string().trim().min(1, 'Name is required').max(150, 'At most 150 characters'),
  email: z.union([z.literal(''), z.email('Enter a valid email address').max(200, 'At most 200 characters')]),
  phone: z.string().trim().max(30, 'At most 30 characters'),
});

export type SupplierFormValues = z.infer<typeof supplierSchema>;
