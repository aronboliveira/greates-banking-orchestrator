import { memo, useCallback } from 'react';
import { createPortal } from 'react-dom';
import { setHelpOpen, useAppDispatch } from '../app/store';
import styles from './FloatingWindow.module.scss';

export const FloatingHelp = memo(function FloatingHelp() {
  const dispatch = useAppDispatch();
  const onClose = useCallback(() => dispatch(setHelpOpen(false)), [dispatch]);
  const root = document.getElementById('floating-help-root');

  if (!root) return null;

  return createPortal(
    <section
      id="gbo-floating-help-window"
      className={styles.window}
      role="dialog"
      aria-modal="false"
      aria-labelledby="gbo-floating-help-title"
    >
      <div className={styles.header}>
        <h2 id="gbo-floating-help-title">Dashboard help</h2>
        <button id="gbo-floating-help-close" type="button" className="btn btn-sm btn-outline-secondary" onClick={onClose} title="Close help">
          <i className="bi bi-x-lg" aria-hidden="true" />
        </button>
      </div>
      <p id="gbo-floating-help-copy">
        Use the mock users in the login datalist. Admins can create accounts; all signed-in users can register
        transactions and update their demo profile.
      </p>
    </section>,
    root,
  );
});
