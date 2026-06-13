import type {
  AccountResponse,
  ApiError,
  AvatarOption,
  LoginResponse,
  TransactionResponse,
  UserProfile,
} from './types';

type RawUserProfile = {
  user_id: number;
  username: string;
  display_name: string;
  email: string;
  role: UserProfile['role'];
  avatar_id: string;
  notifications_enabled: boolean;
  updated_at: string;
};

type RawLoginResponse = {
  access_token: string;
  token_type: 'Bearer';
  expires_at: string;
  user: RawUserProfile;
};

type RawAccountResponse = {
  account_id: number;
  document_number: string;
};

type RawTransactionResponse = {
  transaction_id: number;
  account_id: number;
  operation_type_id: number;
  amount: number;
};

const API_BASE_URL = Object.freeze({
  value: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080',
});

async function request<T>(path: string, options: RequestInit = {}, token?: string | null): Promise<T> {
  const headers = new Headers(options.headers);
  headers.set('Accept', 'application/json');
  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(`${API_BASE_URL.value}${path}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    let apiError: ApiError = {
      status: response.status,
      error: response.statusText,
      message: response.statusText,
    };
    try {
      apiError = await response.json();
    } catch {
      // Keep the HTTP fallback when the server returns no JSON body.
    }
    throw new Error(apiError.message || apiError.error);
  }

  return response.json() as Promise<T>;
}

export const apiClient = Object.freeze({
  login: (username: string, password: string) =>
    request<RawLoginResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    }).then(mapLogin),

  me: (token: string) => request<RawUserProfile>('/auth/me', {}, token).then(mapUser),

  updateProfile: (
    token: string,
    payload: Pick<UserProfile, 'displayName' | 'email' | 'avatarId' | 'notificationsEnabled'>,
  ) =>
    request<RawUserProfile>(
      '/users/me/profile',
      {
        method: 'PATCH',
        body: JSON.stringify({
          display_name: payload.displayName,
          email: payload.email,
          avatar_id: payload.avatarId,
          notifications_enabled: payload.notificationsEnabled,
        }),
      },
      token,
    ).then(mapUser),

  avatars: (token: string) => request<AvatarOption[]>('/profile-avatars', {}, token),

  createAccount: (token: string, documentNumber: string) =>
    request<RawAccountResponse>(
      '/accounts',
      {
        method: 'POST',
        body: JSON.stringify({ document_number: documentNumber }),
      },
      token,
    ).then(mapAccount),

  getAccount: (token: string, accountId: number) =>
    request<RawAccountResponse>(`/accounts/${accountId}`, {}, token).then(mapAccount),

  createTransaction: (token: string, accountId: number, operationTypeId: number, amount: number) =>
    request<RawTransactionResponse>(
      '/transactions',
      {
        method: 'POST',
        body: JSON.stringify({
          account_id: accountId,
          operation_type_id: operationTypeId,
          amount,
        }),
      },
      token,
    ).then(mapTransaction),

  health: async () => {
    const response = await fetch(`${API_BASE_URL.value}/actuator/health`);
    return {
      ok: response.ok,
      status: response.status,
    };
  },
});

function mapUser(raw: RawUserProfile): UserProfile {
  return {
    userId: raw.user_id,
    username: raw.username,
    displayName: raw.display_name,
    email: raw.email,
    role: raw.role,
    avatarId: raw.avatar_id,
    notificationsEnabled: raw.notifications_enabled,
    updatedAt: raw.updated_at,
  };
}

function mapLogin(raw: RawLoginResponse): LoginResponse {
  return {
    accessToken: raw.access_token,
    tokenType: raw.token_type,
    expiresAt: raw.expires_at,
    user: mapUser(raw.user),
  };
}

function mapAccount(raw: RawAccountResponse): AccountResponse {
  return {
    accountId: raw.account_id,
    documentNumber: raw.document_number,
  };
}

function mapTransaction(raw: RawTransactionResponse): TransactionResponse {
  return {
    transactionId: raw.transaction_id,
    accountId: raw.account_id,
    operationTypeId: raw.operation_type_id,
    amount: raw.amount,
  };
}
