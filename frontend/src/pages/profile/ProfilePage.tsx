import React, { useState } from 'react';
import { useAuthStore } from '../../store/authStore';
import { apiClient } from '../../lib/api';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { Avatar } from '../../components/ui/Avatar';
import { Check, User, Mail, Phone, DollarSign } from 'lucide-react';

export const ProfilePage: React.FC = () => {
  const user = useAuthStore((s) => s.user);
  const updateUser = useAuthStore((s) => s.updateUser);

  const [fullName, setFullName] = useState(user?.fullName || '');
  const [phoneNumber, setPhoneNumber] = useState(user?.phoneNumber || '+91 ');
  const [defaultCurrency, setDefaultCurrency] = useState(user?.defaultCurrency || 'INR');
  const [isLoading, setIsLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleCurrencyChange = (newCurrency: string) => {
    setDefaultCurrency(newCurrency);
    if (newCurrency === 'INR' && (!phoneNumber || phoneNumber === '+1 ')) {
      setPhoneNumber('+91 ');
    } else if (newCurrency === 'USD' && (!phoneNumber || phoneNumber === '+91 ')) {
      setPhoneNumber('+1 ');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);
    setSuccess(false);

    try {
      const response: any = await apiClient.put('/users/me', {
        fullName: fullName.trim(),
        phoneNumber: phoneNumber.trim() || undefined,
        defaultCurrency,
      });

      if (response?.data) {
        updateUser(response.data);
        setSuccess(true);
        setTimeout(() => setSuccess(false), 3000);
      }
    } catch (err: any) {
      setError(err?.message || 'Failed to update profile');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="max-w-xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-black text-slate-900 dark:text-slate-100 tracking-tight">Account Profile</h1>
        <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">Manage your identity and personal preferences</p>
      </div>

      <div className="bg-white dark:bg-slate-900 p-6 sm:p-8 rounded-3xl border border-slate-200/80 dark:border-slate-800 shadow-xs transition-colors">
        <div className="flex items-center gap-4 pb-6 border-b border-slate-100 dark:border-slate-800 mb-6">
          <Avatar name={user?.fullName} url={user?.avatarUrl} size="lg" />
          <div>
            <h3 className="text-lg font-bold text-slate-900 dark:text-slate-100">{user?.fullName}</h3>
            <p className="text-xs text-slate-400">{user?.email}</p>
          </div>
        </div>

        {error && (
          <div className="p-3 mb-4 rounded-xl bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-900 text-rose-600 dark:text-rose-400 text-xs font-medium">
            {error}
          </div>
        )}

        {success && (
          <div className="p-3 mb-4 rounded-xl bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-900 text-emerald-700 dark:text-emerald-300 text-xs font-medium flex items-center gap-1.5">
            <Check className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
            Profile updated successfully!
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <Input
            label="Username / Full Name"
            placeholder="username"
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
            required
          />

          <div>
            <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1">Email address</label>
            <input
              type="email"
              disabled
              value={user?.email || ''}
              className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-800 text-slate-400 dark:text-slate-500 text-sm outline-none cursor-not-allowed"
            />
            <p className="text-[11px] text-slate-400 mt-1">Email address cannot be changed.</p>
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1">Default Currency</label>
            <select
              value={defaultCurrency}
              onChange={(e) => handleCurrencyChange(e.target.value)}
              className="w-full px-3.5 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100 text-sm outline-none focus:border-brand-500 font-semibold"
            >
              <option value="INR">INR (₹) — Dual currency displays USD ($)</option>
              <option value="USD">USD ($)</option>
              <option value="EUR">EUR (€)</option>
              <option value="GBP">GBP (£)</option>
            </select>
          </div>

          <Input
            label="Phone Number"
            type="tel"
            placeholder={defaultCurrency === 'INR' ? '+91 98765 43210' : '+1 555 123 4567'}
            value={phoneNumber}
            onChange={(e) => setPhoneNumber(e.target.value)}
          />

          <div className="pt-4 border-t border-slate-100 dark:border-slate-800 flex justify-end">
            <Button type="submit" isLoading={isLoading}>
              Save Changes
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};
