import React, { useState } from 'react';
import { Modal } from '../../components/ui/Modal';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { apiClient } from '../../lib/api';
import { GroupType } from '../../types';

interface CreateGroupModalProps {
  isOpen: boolean;
  onClose: () => void;
  onCreated: () => void;
}

export const CreateGroupModal: React.FC<CreateGroupModalProps> = ({ isOpen, onClose, onCreated }) => {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [groupType, setGroupType] = useState<GroupType>('TRIP');
  const [currency, setCurrency] = useState('USD');
  const [simplifyDebtsDefault, setSimplifyDebtsDefault] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;

    setIsLoading(true);
    setError(null);

    try {
      await apiClient.post('/groups', {
        name: name.trim(),
        description: description.trim() || undefined,
        groupType,
        currency,
        simplifyDebtsDefault,
      });
      setName('');
      setDescription('');
      onCreated();
    } catch (err: any) {
      setError(err?.message || 'Failed to create group');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Create New Group" description="Start sharing expenses with your friends or roommates">
      {error && (
        <div className="p-3 mb-4 rounded-xl bg-rose-50 border border-rose-200 text-rose-600 text-xs font-medium">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <Input
          label="Group Name"
          placeholder="e.g. Summer Road Trip, Apartment 4B"
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
        />

        <Input
          label="Description (Optional)"
          placeholder="Brief description of the group"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />

        <div className="grid grid-cols-2 gap-3">
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Type</label>
            <select
              value={groupType}
              onChange={(e) => setGroupType(e.target.value as GroupType)}
              className="w-full px-3 py-2.5 rounded-xl border border-slate-200 text-sm outline-none bg-white focus:border-brand-500"
            >
              <option value="TRIP">Trip / Vacation</option>
              <option value="HOME">Home / Flatmates</option>
              <option value="COUPLE">Couple</option>
              <option value="OTHER">Other</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Currency</label>
            <select
              value={currency}
              onChange={(e) => setCurrency(e.target.value)}
              className="w-full px-3 py-2.5 rounded-xl border border-slate-200 text-sm outline-none bg-white focus:border-brand-500"
            >
              <option value="USD">USD ($)</option>
              <option value="EUR">EUR (€)</option>
              <option value="GBP">GBP (£)</option>
              <option value="INR">INR (₹)</option>
            </select>
          </div>
        </div>

        <div className="flex items-center gap-2 pt-2">
          <input
            type="checkbox"
            id="simplifyDebts"
            checked={simplifyDebtsDefault}
            onChange={(e) => setSimplifyDebtsDefault(e.target.checked)}
            className="w-4 h-4 text-brand-500 rounded border-slate-300 focus:ring-brand-500"
          />
          <label htmlFor="simplifyDebts" className="text-sm text-slate-700">
            Automatically simplify debts in this group
          </label>
        </div>

        <div className="flex justify-end gap-3 pt-4 border-t border-slate-100 mt-6">
          <Button type="button" variant="outline" onClick={onClose}>
            Cancel
          </Button>
          <Button type="submit" isLoading={isLoading}>
            Create Group
          </Button>
        </div>
      </form>
    </Modal>
  );
};

