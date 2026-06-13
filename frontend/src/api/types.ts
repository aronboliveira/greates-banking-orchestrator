export type UserRole = 'SUPER_ADMIN' | 'ADMIN' | 'USER';

export type UserProfile = {
  userId: number;
  username: string;
  displayName: string;
  email: string;
  role: UserRole;
  avatarId: string;
  notificationsEnabled: boolean;
  updatedAt: string;
};

export type LoginResponse = {
  accessToken: string;
  tokenType: 'Bearer';
  expiresAt: string;
  user: UserProfile;
};

export type AccountResponse = {
  accountId: number;
  documentNumber: string;
};

export type TransactionResponse = {
  transactionId: number;
  accountId: number;
  operationTypeId: number;
  amount: number;
};

export type AvatarOption = {
  id: string;
  label: string;
  src: string;
};

export type ApiError = {
  status: number;
  error: string;
  message: string;
};
