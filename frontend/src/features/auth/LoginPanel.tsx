import { FormEvent, memo, useCallback, useDeferredValue, useMemo, useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { apiClient } from '../../api/client';
import { formValue, loginSchema } from '../../api/formSecurity';
import { setSession, useAppDispatch } from '../../app/store';

const MOCK_USERS = Object.freeze([
  { username: 'super-admin', password: 'orchestrate-all', role: 'super-admin' },
  { username: 'admin', password: 'approve-flow', role: 'admin' },
  { username: 'user', password: 'submit-flow', role: 'user' },
]);

type LoginPanelProps = {
  sessionMessage?: string;
};

export const LoginPanel = memo(function LoginPanel({ sessionMessage }: LoginPanelProps) {
  const dispatch = useAppDispatch();
  const [validationMessage, setValidationMessage] = useState('');
  const deferredValidationMessage = useDeferredValue(validationMessage);

  const loginMutation = useMutation({
    mutationFn: ({ username, password }: { username: string; password: string }) =>
      apiClient.login(username, password),
    onSuccess: (response) => {
      dispatch(setSession({ accessToken: response.accessToken, user: response.user }));
      toast.success(`Signed in as ${response.user.displayName}`);
      setValidationMessage('');
    },
    onError: (error) => {
      toast.error(error.message);
    },
  });

  const userOptions = useMemo(() => MOCK_USERS, []);

  const handleSubmit = useCallback(
    (event: FormEvent<HTMLFormElement>) => {
      event.preventDefault();
      const form = event.currentTarget;
      const formData = new FormData(form);
      const parsed = loginSchema.safeParse({
        username: formValue(formData, 'username'),
        password: formValue(formData, 'password'),
      });

      if (!parsed.success) {
        setValidationMessage(parsed.error.issues[0]?.message ?? 'Invalid login input.');
        form.dataset.validated = 'false';
        return;
      }

      form.dataset.validated = 'true';
      loginMutation.mutate(parsed.data);
    },
    [loginMutation],
  );

  return (
    <section id="gbo-login-section" className="gbo-login-section" aria-labelledby="gbo-login-title">
      <form id="gbo-login-form" className="gbo-card gbo-login-form" onSubmit={handleSubmit} noValidate>
        <fieldset id="gbo-login-fieldset" disabled={loginMutation.isPending}>
          <legend id="gbo-login-title">Sign in</legend>
          <div id="gbo-login-brand" className="gbo-login-brand">
            <img
              id="gbo-login-brand-logo"
              src="/assets/brand/logo-light-removebg-preview.png"
              alt=""
              aria-hidden="true"
            />
            <div id="gbo-login-brand-copy">
              <h1 id="gbo-login-app-title">The Greatest Banking Orchestrator</h1>
              <p id="gbo-login-subtitle">Use a seeded mock user to open the secure dashboard.</p>
            </div>
          </div>
          <div id="gbo-login-grid" className="gbo-form-stack">
            <label id="gbo-login-username-label" htmlFor="gbo-login-username-input">
              Username
              <input
                id="gbo-login-username-input"
                name="username"
                className="form-control"
                list="gbo-login-usernames"
                required
                minLength={4}
                maxLength={40}
                pattern="super-admin|admin|user"
                autoComplete="username"
                title="Choose one of the mock usernames"
                aria-describedby="gbo-login-help gbo-login-error"
                defaultValue="admin"
              />
            </label>
            <datalist id="gbo-login-usernames">
              {userOptions.map((user) => (
                <option key={user.username} value={user.username}>{`${user.role} / ${user.password}`}</option>
              ))}
            </datalist>

            <label id="gbo-login-password-label" htmlFor="gbo-login-password-input">
              Password
              <input
                id="gbo-login-password-input"
                name="password"
                className="form-control"
                type="password"
                required
                minLength={8}
                maxLength={80}
                autoComplete="current-password"
                title="Use the mock password documented on the page"
                aria-describedby="gbo-login-help gbo-login-error"
                defaultValue="approve-flow"
              />
            </label>
          </div>
          <p id="gbo-login-help" className="form-text">
            Mocks: super-admin/orchestrate-all, admin/approve-flow, user/submit-flow.
          </p>
          <p id="gbo-login-error" className="gbo-form-error" role="alert">
            {deferredValidationMessage}
          </p>
          <div id="gbo-login-actions" className="gbo-login-actions">
            <button
              id="gbo-login-submit-button"
              type="submit"
              className="btn btn-primary"
              title="Start mock JWT session"
              aria-busy={loginMutation.isPending}
            >
              <i className="bi bi-shield-lock" aria-hidden="true" /> {loginMutation.isPending ? 'Signing in...' : 'Sign in'}
            </button>
            {sessionMessage ? (
              <span id="gbo-login-session-status" className="gbo-state-pill is-warning" aria-live="polite">
                {sessionMessage}
              </span>
            ) : null}
          </div>
        </fieldset>
      </form>
    </section>
  );
});
