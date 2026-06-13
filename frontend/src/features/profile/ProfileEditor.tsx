import { FormEvent, memo, useCallback, useMemo } from 'react';
import { createPortal } from 'react-dom';
import { useMutation, useQuery } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { apiClient } from '../../api/client';
import { formValue, profileSchema } from '../../api/formSecurity';
import {
  selectCurrentUser,
  selectToken,
  setProfileOpen,
  setUser,
  useAppDispatch,
  useAppSelector,
} from '../../app/store';
import styles from './ProfileEditor.module.scss';

export const ProfileEditor = memo(function ProfileEditor() {
  const dispatch = useAppDispatch();
  const token = useAppSelector(selectToken);
  const user = useAppSelector(selectCurrentUser);

  const avatarsQuery = useQuery({
    queryKey: ['profile-avatars', token],
    queryFn: () => apiClient.avatars(token!),
    enabled: Boolean(token),
  });

  const avatarOptions = useMemo(() => avatarsQuery.data ?? [], [avatarsQuery.data]);
  const selectedAvatar = useMemo(
    () => avatarOptions.find((avatar) => avatar.id === user?.avatarId) ?? avatarOptions[0],
    [avatarOptions, user?.avatarId],
  );

  const updateMutation = useMutation({
    mutationFn: (payload: {
      displayName: string;
      email: string;
      avatarId: string;
      notificationsEnabled: boolean;
    }) => apiClient.updateProfile(token!, payload),
    onSuccess: (profile) => {
      dispatch(setUser(profile));
      toast.success('Profile updated');
    },
    onError: (error) => toast.error(error.message),
  });

  const close = useCallback(() => dispatch(setProfileOpen(false)), [dispatch]);

  const handleSubmit = useCallback(
    (event: FormEvent<HTMLFormElement>) => {
      event.preventDefault();
      const form = event.currentTarget;
      const formData = new FormData(form);
      const parsed = profileSchema.safeParse({
        displayName: formValue(formData, 'displayName'),
        email: formValue(formData, 'email'),
        avatarId: formValue(formData, 'avatarId'),
        notificationsEnabled: formData.get('notificationsEnabled') === 'on',
      });

      if (!parsed.success) {
        form.dataset.validated = 'false';
        toast.error(parsed.error.issues[0]?.message ?? 'Invalid profile input');
        return;
      }

      form.dataset.validated = 'true';
      updateMutation.mutate(parsed.data);
    },
    [updateMutation],
  );

  const root = document.getElementById('global-modal-root');
  if (!root || !user) return null;

  return createPortal(
    <div id="gbo-profile-editor-overlay" className={styles.overlay} role="presentation">
      <section
        id="gbo-profile-editor-window"
        className={styles.window}
        role="dialog"
        aria-modal="true"
        aria-labelledby="gbo-profile-editor-title"
      >
        <div className={styles.header}>
          <div className={styles.identity}>
            {selectedAvatar ? <img id="gbo-profile-editor-avatar-preview" src={selectedAvatar.src} alt="" aria-hidden="true" /> : null}
            <div>
              <h2 id="gbo-profile-editor-title">Edit profile</h2>
              <span id="gbo-profile-editor-role">{user.role.toLowerCase().replace('_', '-')}</span>
            </div>
          </div>
          <button id="gbo-profile-editor-close" type="button" className="btn btn-sm btn-outline-secondary" onClick={close} title="Close profile editor">
            <i className="bi bi-x-lg" aria-hidden="true" />
          </button>
        </div>

        <form id="gbo-profile-editor-form" onSubmit={handleSubmit} noValidate>
          <fieldset id="gbo-profile-identity-fieldset" disabled={updateMutation.isPending}>
            <legend>Identity</legend>
            <label id="gbo-profile-display-name-label" htmlFor="gbo-profile-display-name-input">
              Display name
              <input
                id="gbo-profile-display-name-input"
                name="displayName"
                className="form-control"
                required
                minLength={2}
                maxLength={80}
                title="Display name shown in the dashboard"
                defaultValue={user.displayName}
              />
            </label>
            <label id="gbo-profile-email-label" htmlFor="gbo-profile-email-input">
              Email
              <input
                id="gbo-profile-email-input"
                name="email"
                className="form-control"
                type="email"
                required
                maxLength={160}
                list="gbo-profile-email-datalist"
                autoComplete="email"
                title="Notification email"
                defaultValue={user.email}
              />
            </label>
            <datalist id="gbo-profile-email-datalist">
              <option value="aronprogamador@gmail.com">Real SMTP allowlist recipient</option>
              <option value="admin@example.test">Demo admin</option>
              <option value="user@example.test">Demo user</option>
            </datalist>
          </fieldset>

          <fieldset id="gbo-profile-preferences-fieldset" disabled={updateMutation.isPending}>
            <legend>Avatar and notifications</legend>
            <label id="gbo-profile-avatar-label" htmlFor="gbo-profile-avatar-input">
              Avatar id
              <input
                id="gbo-profile-avatar-input"
                name="avatarId"
                className="form-control"
                required
                list="gbo-profile-avatar-datalist"
                maxLength={80}
                title="Choose one of the available profile avatars"
                defaultValue={user.avatarId}
              />
            </label>
            <datalist id="gbo-profile-avatar-datalist">
              {avatarOptions.map((avatar) => (
                <option key={avatar.id} value={avatar.id}>{avatar.label}</option>
              ))}
            </datalist>
            <label id="gbo-profile-notifications-label" className="form-check">
              <input
                id="gbo-profile-notifications-input"
                name="notificationsEnabled"
                className="form-check-input"
                type="checkbox"
                defaultChecked={user.notificationsEnabled}
                title="Allow major portfolio notifications"
              />
              <span className="form-check-label">Enable major notifications</span>
            </label>
          </fieldset>

          <button
            id="gbo-profile-save-button"
            className="btn btn-primary"
            type="submit"
            aria-busy={updateMutation.isPending}
            title="Save profile"
          >
            <i className="bi bi-save2" aria-hidden="true" /> {updateMutation.isPending ? 'Saving...' : 'Save profile'}
          </button>
        </form>
      </section>
    </div>,
    root,
  );
});
