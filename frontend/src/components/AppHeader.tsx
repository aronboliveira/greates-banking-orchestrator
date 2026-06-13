import { memo, useCallback } from 'react';
import toast from 'react-hot-toast';
import {
  logout,
  selectCurrentUser,
  selectSessionBadge,
  selectTheme,
  setHelpOpen,
  setProfileOpen,
  toggleTheme,
  useAppDispatch,
  useAppSelector,
} from '../app/store';

export const AppHeader = memo(function AppHeader() {
  const dispatch = useAppDispatch();
  const theme = useAppSelector(selectTheme);
  const user = useAppSelector(selectCurrentUser);
  const sessionBadge = useAppSelector(selectSessionBadge);

  const onToggleTheme = useCallback(() => dispatch(toggleTheme()), [dispatch]);
  const onOpenProfile = useCallback(() => {
    dispatch(setProfileOpen(true));
    toast('Profile editor opened');
  }, [dispatch]);
  const onOpenHelp = useCallback(() => dispatch(setHelpOpen(true)), [dispatch]);
  const onLogout = useCallback(() => {
    dispatch(logout());
    toast.success('Signed out');
  }, [dispatch]);

  return (
    <header id="gbo-app-header" className="gbo-app-header">
      <div id="gbo-brand-lockup" className="gbo-brand-lockup">
        <img id="gbo-brand-logo" src="/assets/brand/logo-light-removebg-preview.png" alt="" aria-hidden="true" />
        <div>
          <h1 id="gbo-app-title">The Greatest Banking Orchestrator</h1>
          <span id="gbo-session-badge" className="text-muted">{sessionBadge}</span>
        </div>
      </div>
      <div id="gbo-header-actions" className="gbo-header-actions" role="group" aria-label="Dashboard controls">
        <button
          id="gbo-theme-toggle-button"
          type="button"
          className="btn btn-outline-secondary btn-sm"
          aria-pressed={theme === 'dark'}
          aria-label="Toggle light and dark mode"
          title="Toggle light and dark mode"
          onClick={onToggleTheme}
        >
          <i className={`bi ${theme === 'dark' ? 'bi-moon-stars-fill' : 'bi-brightness-high-fill'}`} aria-hidden="true" />
        </button>
        <button
          id="gbo-help-button"
          type="button"
          className="btn btn-outline-info btn-sm"
          aria-label="Open help"
          title="Open help"
          onClick={onOpenHelp}
        >
          <i className="bi bi-question-lg" aria-hidden="true" />
        </button>
        {user ? (
          <>
            <button
              id="gbo-profile-button"
              type="button"
              className="btn btn-outline-primary btn-sm"
              aria-label="Edit profile"
              title="Edit profile"
              onClick={onOpenProfile}
            >
              <i className="bi bi-person-gear" aria-hidden="true" />
              <span className="gbo-action-text">Profile</span>
            </button>
            <button
              id="gbo-logout-button"
              type="button"
              className="btn btn-outline-danger btn-sm"
              aria-label="Sign out"
              title="Sign out"
              onClick={onLogout}
            >
              <i className="bi bi-box-arrow-right" aria-hidden="true" />
              <span className="gbo-action-text">Sign out</span>
            </button>
          </>
        ) : null}
      </div>
    </header>
  );
});
