import { z } from 'zod';

export const productSchema = z.object({
  sku: z
    .string()
    .trim()
    .min(1, 'SKU is required')
    .max(64, 'At most 64 characters')
    .regex(/^[A-Z0-9-]+$/, 'Use only uppercase letters, digits and hyphens'),
  name: z.string().trim().min(1, 'Name is required').max(200, 'At most 200 characters'),
  unitPrice: z.string().trim().regex(/^\d+(\.\d{1,2})?$/, 'Enter a price of 0 or more, like 19.99'),
  reorderLevel: z.string().trim().regex(/^\d+$/, 'Enter a whole number of 0 or more'),
  supplierId: z.string().min(1, 'Choose a supplier'),
});

export type ProductFormValues = z.infer<typeof productSchema>;
