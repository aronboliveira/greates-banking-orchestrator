import { Suspense, lazy, useEffect, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Toaster } from 'react-hot-toast';
import { apiClient } from './api/client';
import {
  logout,
  selectCurrentUser,
  selectHelpOpen,
  selectProfileOpen,
  selectSelectedPanel,
  selectTheme,
  selectToken,
  setUser,
  useAppDispatch,
  useAppSelector,
} from './app/store';
import { AppHeader } from './components/AppHeader';
import { AppSidebar } from './components/AppSidebar';
import { FloatingHelp } from './components/FloatingHelp';
import { LoginPanel } from './features/auth/LoginPanel';
import { ProfileEditor } from './features/profile/ProfileEditor';

const OverviewPanel = lazy(() => import('./features/dashboard/OverviewPanel'));
const AccountsPanel = lazy(() => import('./features/accounts/AccountsPanel'));
const TransactionsPanel = lazy(() => import('./features/transactions/TransactionsPanel'));

export function App() {
  const dispatch = useAppDispatch();
  const theme = useAppSelector(selectTheme);
  const token = useAppSelector(selectToken);
  const currentUser = useAppSelector(selectCurrentUser);
  const selectedPanel = useAppSelector(selectSelectedPanel);
  const profileOpen = useAppSelector(selectProfileOpen);
  const helpOpen = useAppSelector(selectHelpOpen);

  useEffect(() => {
    document.documentElement.classList.toggle('gbo-theme-dark', theme === 'dark');
    document.documentElement.classList.toggle('gbo-theme-light', theme === 'light');
    document.documentElement.dataset.themeMounted = 'true';
  }, [theme]);

  const sessionQuery = useQuery({
    queryKey: ['current-user', token],
    queryFn: () => apiClient.me(token as string),
    enabled: Boolean(token && !currentUser),
    retry: false,
    staleTime: 60_000,
  });

  useEffect(() => {
    if (sessionQuery.data) {
      dispatch(setUser(sessionQuery.data));
    }
  }, [dispatch, sessionQuery.data]);

  useEffect(() => {
    if (sessionQuery.error) {
      dispatch(logout());
    }
  }, [dispatch, sessionQuery.error]);

  const panel = useMemo(() => {
    if (selectedPanel === 'accounts') return <AccountsPanel />;
    if (selectedPanel === 'transactions') return <TransactionsPanel />;
    return <OverviewPanel />;
  }, [selectedPanel]);

  if (!token || !currentUser) {
    return (
      <div id="gbo-app-shell" className="gbo-app-shell gbo-auth-shell">
        <main id="gbo-auth-main" className="gbo-auth-main" aria-labelledby="gbo-login-title">
          <LoginPanel sessionMessage={sessionQuery.isLoading ? 'Restoring secure session...' : undefined} />
        </main>
        <Toaster position="top-right" containerStyle={{ zIndex: 2500 }} />
      </div>
    );
  }

  return (
    <div id="gbo-app-shell" className="gbo-app-shell">
      <AppHeader />
      <div id="gbo-dashboard-layout" className="gbo-dashboard-layout">
        <AppSidebar />
        <main id="gbo-main-panel" className="gbo-main-panel" aria-live="polite">
          <Suspense fallback={<section id="gbo-panel-loading" className="panel-state">Loading panel...</section>}>
            {panel}
          </Suspense>
        </main>
      </div>
      {profileOpen ? <ProfileEditor /> : null}
      {helpOpen ? <FloatingHelp /> : null}
      <Toaster position="top-right" containerStyle={{ zIndex: 2500 }} />
    </div>
  );
}
