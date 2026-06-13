import DOMPurify from 'dompurify';
import { z } from 'zod';

export const loginSchema = z.object({
  username: z.enum(['super-admin', 'admin', 'user']),
  password: z.string().min(8).max(80),
});

export const accountSchema = z.object({
  documentNumber: z.string().regex(/^[0-9]{11}$/, 'Document number must have exactly 11 digits.'),
});

export const transactionSchema = z.object({
  accountId: z.coerce.number().int().positive(),
  operationTypeId: z.coerce.number().int().min(1).max(4),
  amount: z.coerce.number().positive().finite(),
});

export const profileSchema = z.object({
  displayName: z.string().trim().min(2).max(80),
  email: z.string().trim().email().max(160),
  avatarId: z.string().trim().min(2).max(80),
  notificationsEnabled: z.coerce.boolean(),
});

export function sanitizeText(value: FormDataEntryValue | null): string {
  return DOMPurify.sanitize(String(value ?? ''), { ALLOWED_TAGS: [], ALLOWED_ATTR: [] }).trim();
}

export function formValue(formData: FormData, key: string): string {
  return sanitizeText(formData.get(key));
}
