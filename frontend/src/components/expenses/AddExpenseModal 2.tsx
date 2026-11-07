import React, { useState, useEffect } from 'react';
import { Modal } from '../ui/Modal';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';
import { apiClient } from '../../lib/api';
import { Group, SplitType, ExpenseCategory } from '../../types';
import { useAuthStore } from '../../store/authStore';
import { DollarSign, PieChart, Users, Hash, Receipt } from 'lucide-react';

interface AddExpenseModalProps {
  isOpen: boolean;
  onClose: () => void;
  group: Group;
  onExpenseAdded: () => void;
}

export const AddExpenseModal: React.FC<AddExpenseModalProps> = ({
  isOpen,
  onClose,
  group,
  onExpenseAdded,
}) => {
  const currentUser = useAuthStore((s) => s.user);

  const [description, setDescription] = useState('');
  const [totalAmount, setTotalAmount] = useState('');
  const [category, setCategory] = useState<ExpenseCategory>('FOOD');
  const [splitType, setSplitType] = useState<SplitType>('EQUAL');
  const [expenseDate, setExpenseDate] = useState(new Date().toISOString().split('T')[0]);

  // Multiple Payers state (Map: userId -> amountPaid)
  const [payerAmounts, setPayerAmounts] = useState<Record<number, string>>({});

  // Split participants selection & values
  // For EQUAL: set of included userIds
  // For EXACT / PERCENTAGE / SHARE: map of userId -> splitValue
  const [includedUsers, setIncludedUsers] = useState<Record<number, boolean>>({});
  const [splitValues, setSplitValues] = useState<Record<number, string>>({});

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Initialize defaults on open
  useEffect(() => {
    if (isOpen && group?.members) {
      setDescription('');
      setTotalAmount('');
      setCategory('FOOD');
      setSplitType('EQUAL');
      setError(null);

      // Default: current user is the sole payer
      const initialPayers: Record<number, string> = {};
      group.members.forEach((m) => {
        initialPayers[m.user.id] = m.user.id === currentUser?.id ? '' : '0';
      });
      setPayerAmounts(initialPayers);

      // Default: all members included in split
      const initialIncluded: Record<number, boolean> = {};
      const initialSplits: Record<number, string> = {};
      group.members.forEach((m) => {
        initialIncluded[m.user.id] = true;
        initialSplits[m.user.id] = '1'; // 1 share default or empty
      });
      setIncludedUsers(initialIncluded);
      setSplitValues(initialSplits);
    }
  }, [isOpen, group, currentUser]);

  const handleTotalAmountChange = (val: string) => {
    setTotalAmount(val);
    // Auto-fill sole payer amount
    if (currentUser) {
      setPayerAmounts({
        [currentUser.id]: val,
      });
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    const parsedTotal = parseFloat(totalAmount);
    if (isNaN(parsedTotal) || parsedTotal <= 0) {
      setError('Please enter a valid expense total amount');
      return;
    }

    // Build payers list
    const payersList: Array<{ userId: number; amountPaid: number }> = [];
    let sumPaid = 0;
    for (const member of group.members) {
      const amtStr = payerAmounts[member.user.id];
      const amt = parseFloat(amtStr || '0');
      if (!isNaN(amt) && amt > 0) {
        payersList.push({ userId: member.user.id, amountPaid: amt });
        sumPaid += amt;
      }
    }

    if (payersList.length === 0) {
      setError('At least one person must pay for this expense');
      return;
    }

    if (Math.abs(sumPaid - parsedTotal) > 0.01) {
      setError(`Sum of payers (${sumPaid.toFixed(2)}) must equal total amount (${parsedTotal.toFixed(2)})`);
      return;
    }

    // Build splits list
    const splitsList: Array<{ userId: number; splitValue?: number }> = [];
    const activeParticipants = group.members.filter((m) => includedUsers[m.user.id]);

    if (activeParticipants.length === 0) {
      setError('At least one member must be included in the split');
      return;
    }

    if (splitType === 'EQUAL') {
      activeParticipants.forEach((m) => {
        splitsList.push({ userId: m.user.id });
      });
    } else {
      for (const m of activeParticipants) {
        const val = parseFloat(splitValues[m.user.id] || '0');
        if (isNaN(val) || val <= 0) {
          setError(`Please provide a positive split value for ${m.user.fullName}`);
          return;
        }
        splitsList.push({ userId: m.user.id, splitValue: val });
      }

      // Check sum validations
      if (splitType === 'PERCENTAGE') {
        const sumPct = splitsList.reduce((acc, curr) => acc + (curr.splitValue || 0), 0);
        if (Math.abs(sumPct - 100) > 0.05) {
          setError(`Percentages must sum to 100% (currently ${sumPct.toFixed(1)}%)`);
          return;
        }
      } else if (splitType === 'EXACT') {
        const sumExact = splitsList.reduce((acc, curr) => acc + (curr.splitValue || 0), 0);
        if (Math.abs(sumExact - parsedTotal) > 0.01) {
          setError(`Exact split amounts must sum to ${parsedTotal.toFixed(2)} (currently ${sumExact.toFixed(2)})`);
          return;
        }
      }
    }

    setIsLoading(true);

    try {
      await apiClient.post(`/groups/${group.id}/expenses`, {
        description: description.trim(),
        totalAmount: parsedTotal,
        currency: group.currency,
        category,
        splitType,
        expenseDate,
        payers: payersList,
        splits: splitsList,
      });

      onExpenseAdded();
      onClose();
    } catch (err: any) {
      setError(err?.message || 'Failed to create expense');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Add an Expense" maxWidth="lg">
      {error && (
        <div className="p-3 mb-4 rounded-xl bg-rose-50 border border-rose-200 text-rose-600 text-xs font-medium">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <Input
            label="Description"
            placeholder="e.g. Dinner, Grocery run, Flight tickets"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            required
          />

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Total Amount ({group.currency})</label>
            <div className="relative">
              <span className="absolute left-3.5 top-2.5 text-slate-400 font-bold text-sm">$</span>
              <input
                type="number"
                step="0.01"
                min="0.01"
                required
                placeholder="0.00"
                value={totalAmount}
                onChange={(e) => handleTotalAmountChange(e.target.value)}
                className="w-full pl-8 pr-3.5 py-2.5 rounded-xl border border-slate-200 font-bold text-slate-900 text-sm outline-none focus:border-brand-500"
              />
            </div>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Category</label>
            <select
              value={category}
              onChange={(e) => setCategory(e.target.value as ExpenseCategory)}
              className="w-full px-3 py-2.5 rounded-xl border border-slate-200 text-sm outline-none bg-white focus:border-brand-500"
            >
              <option value="FOOD">Food & Drinks</option>
              <option value="RENT">Rent & Housing</option>
              <option value="TRANSPORTATION">Transportation</option>
              <option value="UTILITIES">Utilities</option>
              <option value="ENTERTAINMENT">Entertainment</option>
              <option value="SHOPPING">Shopping</option>
              <option value="OTHER">Other</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Date</label>
            <input
              type="date"
              value={expenseDate}
              onChange={(e) => setExpenseDate(e.target.value)}
              className="w-full px-3 py-2.5 rounded-xl border border-slate-200 text-sm outline-none bg-white focus:border-brand-500"
            />
          </div>
        </div>

        {/* Paid by section */}
        <div className="pt-2 border-t border-slate-100">
          <label className="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">
            Who paid?
          </label>
          <div className="space-y-2 max-h-36 overflow-y-auto pr-1">
            {group.members.map((m) => (
              <div key={m.user.id} className="flex items-center justify-between text-sm">
                <span className="text-slate-700 font-medium">{m.user.fullName}</span>
                <div className="flex items-center gap-1.5 w-32">
                  <span className="text-xs text-slate-400 font-semibold">{group.currency}</span>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    placeholder="0.00"
                    value={payerAmounts[m.user.id] || ''}
                    onChange={(e) =>
                      setPayerAmounts({ ...payerAmounts, [m.user.id]: e.target.value })
                    }
                    className="w-full px-2.5 py-1 rounded-lg border border-slate-200 text-right text-xs outline-none focus:border-brand-500"
                  />
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Split Strategy Tabs */}
        <div className="pt-2 border-t border-slate-100">
          <label className="block text-xs font-bold text-slate-500 uppercase tracking-wider mb-2">
            Split Strategy
          </label>
          <div className="grid grid-cols-4 gap-1.5 p-1 bg-slate-100 rounded-xl mb-3 text-xs font-bold text-slate-600">
            <button
              type="button"
              onClick={() => setSplitType('EQUAL')}
              className={`py-2 rounded-lg flex items-center justify-center gap-1 transition ${
                splitType === 'EQUAL' ? 'bg-white text-brand-600 shadow-xs' : 'hover:text-slate-900'
              }`}
            >
              <Users className="w-3.5 h-3.5" />
              Equal
            </button>
            <button
              type="button"
              onClick={() => setSplitType('EXACT')}
              className={`py-2 rounded-lg flex items-center justify-center gap-1 transition ${
                splitType === 'EXACT' ? 'bg-white text-brand-600 shadow-xs' : 'hover:text-slate-900'
              }`}
            >
              <DollarSign className="w-3.5 h-3.5" />
              Exact
            </button>
            <button
              type="button"
              onClick={() => setSplitType('PERCENTAGE')}
              className={`py-2 rounded-lg flex items-center justify-center gap-1 transition ${
                splitType === 'PERCENTAGE' ? 'bg-white text-brand-600 shadow-xs' : 'hover:text-slate-900'
              }`}
            >
              <PieChart className="w-3.5 h-3.5" />
              Percent
            </button>
            <button
              type="button"
              onClick={() => setSplitType('SHARE')}
              className={`py-2 rounded-lg flex items-center justify-center gap-1 transition ${
                splitType === 'SHARE' ? 'bg-white text-brand-600 shadow-xs' : 'hover:text-slate-900'
              }`}
            >
              <Hash className="w-3.5 h-3.5" />
              Shares
            </button>
          </div>

          {/* Dynamic input rows per participant based on split type */}
          <div className="space-y-2 max-h-40 overflow-y-auto pr-1">
            {group.members.map((m) => {
              const isIncluded = includedUsers[m.user.id] ?? true;
              return (
                <div key={m.user.id} className="flex items-center justify-between text-sm py-1">
                  <div className="flex items-center gap-2">
                    <input
                      type="checkbox"
                      id={`member-${m.user.id}`}
                      checked={isIncluded}
                      onChange={(e) =>
                        setIncludedUsers({ ...includedUsers, [m.user.id]: e.target.checked })
                      }
                      className="w-4 h-4 text-brand-500 rounded border-slate-300 focus:ring-brand-500"
                    />
                    <label htmlFor={`member-${m.user.id}`} className="text-slate-700 font-medium">
                      {m.user.fullName}
                    </label>
                  </div>

                  {splitType !== 'EQUAL' && isIncluded && (
                    <div className="flex items-center gap-1.5 w-28">
                      <input
                        type="number"
                        step={splitType === 'SHARE' ? '1' : '0.01'}
                        min="0"
                        placeholder={
                          splitType === 'PERCENTAGE' ? '%' : splitType === 'SHARE' ? 'shares' : '0.00'
                        }
                        value={splitValues[m.user.id] || ''}
                        onChange={(e) =>
                          setSplitValues({ ...splitValues, [m.user.id]: e.target.value })
                        }
                        className="w-full px-2 py-1 rounded-lg border border-slate-200 text-right text-xs outline-none focus:border-brand-500"
                      />
                      <span className="text-xs text-slate-400 font-semibold">
                        {splitType === 'PERCENTAGE' ? '%' : splitType === 'SHARE' ? 'sh' : group.currency}
                      </span>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </div>

        <div className="flex justify-end gap-3 pt-4 border-t border-slate-100 mt-6">
          <Button type="button" variant="outline" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" isLoading={isLoading}>
            Save Expense
          </Button>
        </div>
      </form>
    </Modal>
  );
};

