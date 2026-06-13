import { FormEvent, memo, useCallback, useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { apiClient } from '../../api/client';
import { formValue, transactionSchema } from '../../api/formSecurity';
import { selectToken, useAppSelector } from '../../app/store';
import type { TransactionResponse } from '../../api/types';

const OPERATIONS = Object.freeze([
  { id: 1, label: 'PURCHASE', sign: 'debit' },
  { id: 2, label: 'INSTALLMENT PURCHASE', sign: 'debit' },
  { id: 3, label: 'WITHDRAWAL', sign: 'debit' },
  { id: 4, label: 'PAYMENT', sign: 'credit' },
]);

function TransactionsPanel() {
  const token = useAppSelector(selectToken);
  const [message, setMessage] = useState('');
  const [result, setResult] = useState<TransactionResponse | null>(null);

  const createMutation = useMutation({
    mutationFn: (payload: { accountId: number; operationTypeId: number; amount: number }) =>
      apiClient.createTransaction(token!, payload.accountId, payload.operationTypeId, payload.amount),
    onSuccess: (transaction) => {
      setResult(transaction);
      setMessage('');
      toast.success(`Transaction ${transaction.transactionId} created`);
    },
    onError: (error) => toast.error(error.message),
  });

  const handleSubmit = useCallback(
    (event: FormEvent<HTMLFormElement>) => {
      event.preventDefault();
      if (!token) {
        setMessage('Sign in before creating transactions.');
        return;
      }
      const form = event.currentTarget;
      const formData = new FormData(form);
      const parsed = transactionSchema.safeParse({
        accountId: formValue(formData, 'accountId'),
        operationTypeId: formValue(formData, 'operationTypeId'),
        amount: formValue(formData, 'amount'),
      });
      if (!parsed.success) {
        form.dataset.validated = 'false';
        setMessage(parsed.error.issues[0]?.message ?? 'Invalid transaction input.');
        return;
      }
      form.dataset.validated = 'true';
      createMutation.mutate(parsed.data);
    },
    [createMutation, token],
  );

  return (
    <section id="gbo-transactions-panel" className="gbo-panel" aria-labelledby="gbo-transactions-title">
      <div id="gbo-transactions-heading-row" className="gbo-panel-heading">
        <h2 id="gbo-transactions-title">Transactions</h2>
        <span id="gbo-transactions-state" className="gbo-state-pill" aria-live="polite">
          {createMutation.isPending ? 'Submitting' : 'Ready'}
        </span>
      </div>

      <form id="gbo-create-transaction-form" className="gbo-card" onSubmit={handleSubmit} noValidate>
        <fieldset id="gbo-transaction-account-fieldset" disabled={createMutation.isPending}>
          <legend>Account and operation</legend>
          <div id="gbo-transaction-first-row" className="gbo-form-grid">
            <label id="gbo-transaction-account-label" htmlFor="gbo-transaction-account-input">
              Account id
              <input
                id="gbo-transaction-account-input"
                name="accountId"
                className="form-control"
                type="number"
                min={1}
                step={1}
                list="gbo-transaction-account-datalist"
                required
                title="Use an existing account id"
              />
            </label>
            <datalist id="gbo-transaction-account-datalist">
              <option value="1">Common demo account</option>
              {result ? <option value={result.accountId}>Last transaction account</option> : null}
            </datalist>

            <label id="gbo-operation-type-label" htmlFor="gbo-operation-type-input">
              Operation type
              <input
                id="gbo-operation-type-input"
                name="operationTypeId"
                className="form-control"
                type="number"
                min={1}
                max={4}
                step={1}
                list="gbo-operation-type-datalist"
                required
                title="1 purchase, 2 installment purchase, 3 withdrawal, 4 payment"
              />
            </label>
            <datalist id="gbo-operation-type-datalist">
              {OPERATIONS.map((operation) => (
                <option key={operation.id} value={operation.id}>{`${operation.label} (${operation.sign})`}</option>
              ))}
            </datalist>
          </div>
        </fieldset>

        <fieldset id="gbo-transaction-amount-fieldset" disabled={createMutation.isPending}>
          <legend>Amount</legend>
          <label id="gbo-transaction-amount-label" htmlFor="gbo-transaction-amount-input">
            Positive amount
            <input
              id="gbo-transaction-amount-input"
              name="amount"
              className="form-control"
              type="number"
              inputMode="decimal"
              min={0.01}
              step={0.01}
              required
              title="The API applies debit or credit sign from operation type"
              aria-describedby="gbo-transaction-message"
            />
          </label>
        </fieldset>

        <button id="gbo-create-transaction-button" className="btn btn-success btn-sm" type="submit" title="Create transaction">
          <i className="bi bi-send-check" aria-hidden="true" /> Submit transaction
        </button>
      </form>

      <output id="gbo-transaction-message" className="gbo-form-error" aria-live="polite">{message}</output>
      {result ? (
        <section id="gbo-transaction-result-section" className="gbo-result" aria-label="Transaction result">
          Transaction #{result.transactionId}: account {result.accountId}, operation {result.operationTypeId}, stored amount {result.amount}
        </section>
      ) : null}
    </section>
  );
}

export default memo(TransactionsPanel);
