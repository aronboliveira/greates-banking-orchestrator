import { useQuery } from '@tanstack/react-query';
import { memo } from 'react';
import { apiClient } from '../../api/client';
import { selectCurrentUser, useAppSelector } from '../../app/store';

function OverviewPanel() {
  const user = useAppSelector(selectCurrentUser);
  const healthQuery = useQuery({
    queryKey: ['api-health'],
    queryFn: apiClient.health,
    retry: false,
    refetchOnWindowFocus: false,
    refetchInterval: (query) => (query.state.data?.ok ? 30_000 : false),
  });

  return (
    <section id="gbo-overview-panel" className="gbo-panel" aria-labelledby="gbo-overview-title">
      <div id="gbo-overview-heading-row" className="gbo-panel-heading">
        <h2 id="gbo-overview-title">Dashboard overview</h2>
        <span
          id="gbo-api-health-status"
          className={`gbo-state-pill ${healthQuery.data?.ok ? 'is-success' : 'is-warning'}`}
          title="Backend health status"
        >
          API {healthQuery.isLoading ? 'checking' : healthQuery.data?.ok ? 'online' : 'unavailable'}
        </span>
      </div>
      <div id="gbo-overview-content" className="gbo-overview-grid">
        <article id="gbo-overview-auth-card" className="gbo-card">
          <h3>Security demo</h3>
          <p>Local JWT auth protects the account, transaction, and profile APIs. Current role: {user?.role ?? 'none'}.</p>
        </article>
        <article id="gbo-overview-forms-card" className="gbo-card">
          <h3>Controlled submissions</h3>
          <p>Forms use HTML constraints, datalists, Zod validation, and sanitized string values before API calls.</p>
        </article>
        <article id="gbo-overview-render-card" className="gbo-card">
          <h3>Render ready</h3>
          <p>The backend accepts Render Postgres URLs and the frontend reads `VITE_API_BASE_URL` at build time.</p>
        </article>
      </div>
    </section>
  );
}

export default memo(OverviewPanel);
