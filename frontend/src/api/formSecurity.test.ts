import { accountSchema, loginSchema, profileSchema, sanitizeText, transactionSchema } from './formSecurity';

describe('form security helpers', () => {
  it('sanitizes submitted text before validation', () => {
    expect(sanitizeText('<img src=x onerror=alert(1)>admin')).toBe('admin');
  });

  it('validates predictable mock login credentials', () => {
    expect(loginSchema.safeParse({ username: 'admin', password: 'approve-flow' }).success).toBe(true);
    expect(loginSchema.safeParse({ username: 'guest', password: 'approve-flow' }).success).toBe(false);
  });

  it('enforces account and transaction constraints', () => {
    expect(accountSchema.safeParse({ documentNumber: '12345678900' }).success).toBe(true);
    expect(accountSchema.safeParse({ documentNumber: '123' }).success).toBe(false);
    expect(transactionSchema.safeParse({ accountId: '1', operationTypeId: '4', amount: '12.30' }).success).toBe(true);
    expect(transactionSchema.safeParse({ accountId: '1', operationTypeId: '9', amount: '-1' }).success).toBe(false);
  });

  it('validates editable profile data', () => {
    expect(
      profileSchema.safeParse({
        displayName: 'Portfolio User',
        email: 'user@example.test',
        avatarId: 'robot-operator',
        notificationsEnabled: true,
      }).success,
    ).toBe(true);
  });
});
