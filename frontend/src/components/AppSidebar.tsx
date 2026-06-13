import { memo, useCallback, useMemo, useTransition } from 'react';
import {
  selectCanManageAccounts,
  selectSelectedPanel,
  setSelectedPanel,
  useAppDispatch,
  useAppSelector,
} from '../app/store';

type PanelId = 'overview' | 'accounts' | 'transactions';

export const AppSidebar = memo(function AppSidebar() {
  const dispatch = useAppDispatch();
  const selectedPanel = useAppSelector(selectSelectedPanel);
  const canManageAccounts = useAppSelector(selectCanManageAccounts);
  const [isPending, startTransition] = useTransition();

  const menuItems = useMemo(
    () => [
      { id: 'overview' as PanelId, label: 'Overview', icon: 'bi-speedometer2', visible: true },
      { id: 'accounts' as PanelId, label: 'Accounts', icon: 'bi-person-vcard', visible: canManageAccounts },
      { id: 'transactions' as PanelId, label: 'Transactions', icon: 'bi-cash-coin', visible: true },
    ],
    [canManageAccounts],
  );

  const selectPanel = useCallback(
    (panelId: PanelId) => {
      startTransition(() => {
        dispatch(setSelectedPanel(panelId));
      });
    },
    [dispatch],
  );

  return (
    <aside id="gbo-sidebar" className="gbo-sidebar" aria-label="Dashboard navigation" data-pending={isPending}>
      <nav id="gbo-sidebar-nav">
        {menuItems
          .filter((item) => item.visible)
          .map((item) => (
            <button
              key={item.id}
              id={`gbo-nav-${item.id}-button`}
              type="button"
              className={`gbo-nav-button ${selectedPanel === item.id ? 'is-selected' : ''}`}
              aria-current={selectedPanel === item.id ? 'page' : undefined}
              title={`Open ${item.label}`}
              onClick={() => selectPanel(item.id)}
            >
              <i className={`bi ${item.icon}`} aria-hidden="true" />
              <span>{item.label}</span>
            </button>
          ))}
      </nav>
    </aside>
  );
});
