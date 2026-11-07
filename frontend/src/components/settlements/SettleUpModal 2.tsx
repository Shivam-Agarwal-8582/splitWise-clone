import React, { useState } from 'react';
import { Modal } from '../ui/Modal';
import { Button } from '../ui/Button';
import { apiClient } from '../../lib/api';
import { Group, User } from '../../types';
import { useAuthStore } from '../../store/authStore';
import { ArrowRight, CheckCircle2 } from 'lucide-react';

interface SettleUpModalProps {
  isOpen: boolean;
  onClose: () => void;
  group: Group;
  defaultDebtor?: User;
  defaultCreditor?: User;
  defaultAmount?: number;
  onSettled: () => void;
}

export const SettleUpModal: React.FC<SettleUpModalProps> = ({
  isOpen,
  onClose,
  group,
  defaultDebtor,
  defaultCreditor,
  defaultAmount,
  onSettled,
}) => {
  const currentUser = useAuthStore((s) => s.user);

  const [payerId, setPayerId] = useState<number>(defaultDebtor?.id || currentUser?.id || 0);
  const [payeeId, setPayeeId] = useState<number>(defaultCreditor?.id || 0);
  const [amount, setAmount] = useState<string>(defaultAmount ? defaultAmount.toFixed(2) : '');
  const [notes, setNotes] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Update defaults when modal opens with props
  React.useEffect(() => {
    if (isOpen) {
      if (defaultDebtor) setPayerId(defaultDebtor.id);
      else if (currentUser) setPayerId(currentUser.id);

      if (defaultCreditor) setPayeeId(defaultCreditor.id);
      else {
        const other = group?.members.find((m) => m.user.id !== (defaultDebtor?.id || currentUser?.id));
        if (other) setPayeeId(other.user.id);
      }

      if (defaultAmount) setAmount(defaultAmount.toFixed(2));
      setNotes('');
      setError(null);
    }
  }, [isOpen, defaultDebtor, defaultCreditor, defaultAmount, group, currentUser]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const parsedAmount = parseFloat(amount);
    if (isNaN(parsedAmount) || parsedAmount <= 0) {
      setError('Please enter a valid settlement amount');
      return;
    }
    if (payerId === payeeId) {
      setError('Payer and recipient cannot be the same person');
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      await apiClient.post(`/groups/${group.id}/settlements`, {
        payerId,
        payeeId,
        amount: parsedAmount,
        currency: group.currency,
        notes: notes.trim() || undefined,
      });

      onSettled();
      onClose();
    } catch (err: any) {
      setError(err?.message || 'Failed to record settlement');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Record a Settlement" description="Record a cash or bank transfer payment made between members">
      {error && (
        <div className="p-3 mb-4 rounded-xl bg-rose-50 border border-rose-200 text-rose-600 text-xs font-medium">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="flex items-center justify-between p-4 bg-slate-50 rounded-2xl border border-slate-200/80 gap-3">
          <div className="flex-1">
            <label className="block text-xs font-bold text-slate-500 uppercase mb-1">Payer</label>
            <select
              value={payerId}
              onChange={(e) => setPayerId(Number(e.target.value))}
              className="w-full bg-white px-3 py-2 rounded-xl border border-slate-200 text-sm font-semibold outline-none focus:border-brand-500"
            >
              {group?.members.map((m) => (
                <option key={m.user.id} value={m.user.id}>
                  {m.user.fullName} {m.user.id === currentUser?.id ? '(You)' : ''}
                </option>
              ))}
            </select>
          </div>

          <div className="pt-5 text-slate-400">
            <ArrowRight className="w-5 h-5" />
          </div>

          <div className="flex-1">
            <label className="block text-xs font-bold text-slate-500 uppercase mb-1">Recipient</label>
            <select
              value={payeeId}
              onChange={(e) => setPayeeId(Number(e.target.value))}
              className="w-full bg-white px-3 py-2 rounded-xl border border-slate-200 text-sm font-semibold outline-none focus:border-brand-500"
            >
              {group?.members.map((m) => (
                <option key={m.user.id} value={m.user.id}>
                  {m.user.fullName} {m.user.id === currentUser?.id ? '(You)' : ''}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-slate-700 mb-1">Amount ({group.currency})</label>
          <div className="relative">
            <span className="absolute left-3.5 top-2.5 text-slate-400 font-bold text-sm">$</span>
            <input
              type="number"
              step="0.01"
              min="0.01"
              required
              placeholder="0.00"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              className="w-full pl-8 pr-3.5 py-2.5 rounded-xl border border-slate-200 font-bold text-slate-900 text-sm outline-none focus:border-brand-500"
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-slate-700 mb-1">Notes (Optional)</label>
          <input
            type="text"
            placeholder="e.g. Paid via Venmo / Cash"
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 text-sm outline-none focus:border-brand-500"
          />
        </div>

        <div className="flex justify-end gap-3 pt-4 border-t border-slate-100 mt-6">
          <Button type="button" variant="outline" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" isLoading={isLoading}>
            <CheckCircle2 className="w-4 h-4 mr-1" />
            Record Payment
          </Button>
        </div>
      </form>
    </Modal>
  );
};

