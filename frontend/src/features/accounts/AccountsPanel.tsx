import { FormEvent, memo, useCallback, useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { apiClient } from '../../api/client';
import { accountSchema, formValue } from '../../api/formSecurity';
import { selectToken, useAppSelector } from '../../app/store';
import type { AccountResponse } from '../../api/types';

function AccountsPanel() {
  const token = useAppSelector(selectToken);
  const [result, setResult] = useState<AccountResponse | null>(null);
  const [message, setMessage] = useState('');

  const createMutation = useMutation({
    mutationFn: (documentNumber: string) => apiClient.createAccount(token!, documentNumber),
    onSuccess: (account) => {
      setResult(account);
      setMessage('');
      toast.success(`Account ${account.accountId} created`);
    },
    onError: (error) => toast.error(error.message),
  });

  const lookupMutation = useMutation({
    mutationFn: (accountId: number) => apiClient.getAccount(token!, accountId),
    onSuccess: (account) => {
      setResult(account);
      toast.success(`Account ${account.accountId} loaded`);
    },
    onError: (error) => toast.error(error.message),
  });

  const handleCreate = useCallback(
    (event: FormEvent<HTMLFormElement>) => {
      event.preventDefault();
      if (!token) {
        setMessage('Sign in before creating accounts.');
        return;
      }
      const form = event.currentTarget;
      const parsed = accountSchema.safeParse({
        documentNumber: formValue(new FormData(form), 'documentNumber'),
      });
      if (!parsed.success) {
        form.dataset.validated = 'false';
        setMessage(parsed.error.issues[0]?.message ?? 'Invalid account input.');
        return;
      }
      form.dataset.validated = 'true';
      createMutation.mutate(parsed.data.documentNumber);
    },
    [createMutation, token],
  );

  const handleLookup = useCallback(
    (event: FormEvent<HTMLFormElement>) => {
      event.preventDefault();
      if (!token) {
        setMessage('Sign in before looking up accounts.');
        return;
      }
      const accountId = Number(formValue(new FormData(event.currentTarget), 'accountId'));
      if (!Number.isInteger(accountId) || accountId <= 0) {
        setMessage('Account id must be a positive integer.');
        return;
      }
      lookupMutation.mutate(accountId);
    },
    [lookupMutation, token],
  );

  return (
    <section id="gbo-accounts-panel" className="gbo-panel" aria-labelledby="gbo-accounts-title">
      <div id="gbo-accounts-heading-row" className="gbo-panel-heading">
        <h2 id="gbo-accounts-title">Accounts</h2>
        <span id="gbo-accounts-state" className="gbo-state-pill" aria-live="polite">
          {createMutation.isPending || lookupMutation.isPending ? 'Working' : 'Ready'}
        </span>
      </div>

      <div id="gbo-accounts-workspace" className="gbo-two-column">
        <form id="gbo-create-account-form" className="gbo-card" onSubmit={handleCreate} noValidate>
          <fieldset id="gbo-create-account-fieldset" disabled={createMutation.isPending}>
            <legend>Create account</legend>
            <label id="gbo-document-number-label" htmlFor="gbo-document-number-input">
              Document number
              <input
                id="gbo-document-number-input"
                name="documentNumber"
                className="form-control"
                list="gbo-document-number-datalist"
                inputMode="numeric"
                required
                minLength={11}
                maxLength={11}
                pattern="[0-9]{11}"
                title="Enter exactly 11 digits"
                aria-describedby="gbo-account-message"
                defaultValue="12345678900"
              />
            </label>
            <datalist id="gbo-document-number-datalist">
              <option value="12345678900">Primary demo document</option>
              <option value="23456789012">Secondary demo document</option>
              <option value="34567890123">QA demo document</option>
            </datalist>
            <button id="gbo-create-account-button" className="btn btn-success btn-sm" type="submit" title="Create account">
              <i className="bi bi-person-plus" aria-hidden="true" /> Create
            </button>
          </fieldset>
        </form>

        <form id="gbo-find-account-form" className="gbo-card" onSubmit={handleLookup} noValidate>
          <fieldset id="gbo-find-account-fieldset" disabled={lookupMutation.isPending}>
            <legend>Find account</legend>
            <label id="gbo-account-id-label" htmlFor="gbo-account-id-input">
              Account id
              <input
                id="gbo-account-id-input"
                name="accountId"
                className="form-control"
                type="number"
                min={1}
                step={1}
                list="gbo-known-account-id-datalist"
                required
                title="Enter a known account id"
                aria-describedby="gbo-account-message"
              />
            </label>
            <datalist id="gbo-known-account-id-datalist">
              {result ? <option value={result.accountId}>Last returned account</option> : null}
              <option value="1">Typical first account</option>
            </datalist>
            <button id="gbo-find-account-button" className="btn btn-outline-primary btn-sm" type="submit" title="Find account">
              <i className="bi bi-search" aria-hidden="true" /> Find
            </button>
          </fieldset>
        </form>
      </div>

      <output id="gbo-account-message" className="gbo-form-error" aria-live="polite">{message}</output>
      {result ? (
        <section id="gbo-account-result-section" className="gbo-result" aria-label="Account result">
          Account #{result.accountId} / document {result.documentNumber}
        </section>
      ) : null}
    </section>
  );
}

export default memo(AccountsPanel);
