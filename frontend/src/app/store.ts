import { configureStore, createSelector, createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { TypedUseSelectorHook } from 'react-redux';
import { useDispatch, useSelector } from 'react-redux';
import type { UserProfile } from '../api/types';

type SessionState = {
  accessToken: string | null;
  user: UserProfile | null;
};

type UiState = {
  theme: 'light' | 'dark';
  selectedPanel: 'overview' | 'accounts' | 'transactions';
  profileOpen: boolean;
  helpOpen: boolean;
};

const initialSessionState: SessionState = {
  accessToken: localStorage.getItem('gbo.accessToken'),
  user: null,
};

const sessionSlice = createSlice({
  name: 'session',
  initialState: initialSessionState,
  reducers: {
    setSession(state, action: PayloadAction<SessionState>) {
      state.accessToken = action.payload.accessToken;
      state.user = action.payload.user;
      if (action.payload.accessToken) {
        localStorage.setItem('gbo.accessToken', action.payload.accessToken);
      }
    },
    setUser(state, action: PayloadAction<UserProfile>) {
      state.user = action.payload;
    },
    logout(state) {
      state.accessToken = null;
      state.user = null;
      localStorage.removeItem('gbo.accessToken');
    },
  },
});

const initialUiState: UiState = {
  theme: (localStorage.getItem('gbo.theme') as UiState['theme']) || 'light',
  selectedPanel: 'overview',
  profileOpen: false,
  helpOpen: false,
};

const uiSlice = createSlice({
  name: 'ui',
  initialState: initialUiState,
  reducers: {
    setSelectedPanel(state, action: PayloadAction<UiState['selectedPanel']>) {
      state.selectedPanel = action.payload;
    },
    toggleTheme(state) {
      state.theme = state.theme === 'light' ? 'dark' : 'light';
      localStorage.setItem('gbo.theme', state.theme);
    },
    setProfileOpen(state, action: PayloadAction<boolean>) {
      state.profileOpen = action.payload;
    },
    setHelpOpen(state, action: PayloadAction<boolean>) {
      state.helpOpen = action.payload;
    },
  },
});

export const store = configureStore({
  reducer: {
    session: sessionSlice.reducer,
    ui: uiSlice.reducer,
  },
});

export const { setSession, setUser, logout } = sessionSlice.actions;
export const { setSelectedPanel, toggleTheme, setProfileOpen, setHelpOpen } = uiSlice.actions;

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
export const useAppDispatch = () => useDispatch<AppDispatch>();
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector;

export const selectToken = (state: RootState) => state.session.accessToken;
export const selectCurrentUser = (state: RootState) => state.session.user;
export const selectTheme = (state: RootState) => state.ui.theme;
export const selectSelectedPanel = (state: RootState) => state.ui.selectedPanel;
export const selectProfileOpen = (state: RootState) => state.ui.profileOpen;
export const selectHelpOpen = (state: RootState) => state.ui.helpOpen;

export const selectCanManageAccounts = createSelector(selectCurrentUser, (user) =>
  user?.role === 'SUPER_ADMIN' || user?.role === 'ADMIN',
);

export const selectSessionBadge = createSelector(selectCurrentUser, (user) =>
  user ? `${user.displayName} (${user.role.toLowerCase().replace('_', '-')})` : 'Signed out',
);
