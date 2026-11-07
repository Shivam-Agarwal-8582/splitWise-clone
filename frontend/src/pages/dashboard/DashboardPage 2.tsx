import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { apiClient } from '../../lib/api';
import { Group, GroupBalanceSummary } from '../../types';
import { Button } from '../../components/ui/Button';
import { CreateGroupModal } from '../groups/CreateGroupModal';
import {
  TrendingUp,
  TrendingDown,
  Scale,
  Users,
  Plus,
  ArrowRight,
  ShieldCheck,
} from 'lucide-react';

export const DashboardPage: React.FC = () => {
  const currentUser = useAuthStore((s) => s.user);
  const [groups, setGroups] = useState<Group[]>([]);
  const [totalOwed, setTotalOwed] = useState(0); // You are owed
  const [totalOwe, setTotalOwe] = useState(0);   // You owe
  const [isLoading, setIsLoading] = useState(true);
  const [isCreateOpen, setIsCreateOpen] = useState(false);

  const navigate = useNavigate();

  const loadDashboard = async () => {
    try {
      const groupsRes: any = await apiClient.get('/groups');
      const loadedGroups: Group[] = groupsRes?.data || [];
      setGroups(loadedGroups);

      // Fetch balances across user's groups
      let owedSum = 0;
      let oweSum = 0;

      await Promise.all(
        loadedGroups.map(async (g) => {
          try {
            const balRes: any = await apiClient.get(`/groups/${g.id}/balances`);
            const summary: GroupBalanceSummary = balRes?.data;
            if (summary && currentUser) {
              const myBal = summary.memberBalances.find((mb) => mb.user.id === currentUser.id);
              if (myBal) {
                if (myBal.netBalance > 0) owedSum += myBal.netBalance;
                else if (myBal.netBalance < 0) oweSum += Math.abs(myBal.netBalance);
              }
            }
          } catch {}
        })
      );

      setTotalOwed(owedSum);
      setTotalOwe(oweSum);
    } catch (err) {
      console.error('Failed to load dashboard', err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadDashboard();
  }, [currentUser]);

  const netBalance = totalOwed - totalOwe;

  return (
    <div className="space-y-8">
      {/* Welcome Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            Dashboard
          </h1>
          <p className="text-slate-500 text-sm mt-0.5">
            Welcome back, <span className="font-semibold text-slate-700">{currentUser?.fullName}</span>
          </p>
        </div>

        <Button onClick={() => setIsCreateOpen(true)} className="self-start sm:self-auto">
          <Plus className="w-4 h-4 mr-1" />
          Create New Group
        </Button>
      </div>

      {/* 3 Metric Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        {/* Net Balance Card */}
        <div className="bg-white rounded-3xl p-6 border border-slate-200/80 shadow-xs flex flex-col justify-between">
          <div className="flex items-center justify-between text-slate-400">
            <span className="text-xs font-bold uppercase tracking-wider">Total Net Balance</span>
            <Scale className="w-5 h-5 text-slate-400" />
          </div>
          <div className="my-4">
            <h3
              className={`text-3xl font-black ${
                netBalance > 0.01
                  ? 'text-emerald-600'
                  : netBalance < -0.01
                  ? 'text-rose-500'
                  : 'text-slate-700'
              }`}
            >
              {netBalance > 0.01
                ? `+$${netBalance.toFixed(2)}`
                : netBalance < -0.01
                ? `-$${Math.abs(netBalance).toFixed(2)}`
                : '$0.00'}
            </h3>
            <p className="text-xs text-slate-400 mt-1">
              {netBalance >= 0 ? 'Overall you are in the green' : 'You have outstanding debts'}
            </p>
          </div>
          <div className="h-1.5 w-full bg-slate-100 rounded-full overflow-hidden">
            <div
              className={`h-full rounded-full ${
                netBalance >= 0 ? 'bg-emerald-500' : 'bg-rose-500'
              }`}
              style={{ width: '100%' }}
            />
          </div>
        </div>

        {/* You are owed Card */}
        <div className="bg-white rounded-3xl p-6 border border-slate-200/80 shadow-xs flex flex-col justify-between">
          <div className="flex items-center justify-between text-slate-400">
            <span className="text-xs font-bold uppercase tracking-wider">You are owed</span>
            <TrendingUp className="w-5 h-5 text-emerald-500" />
          </div>
          <div className="my-4">
            <h3 className="text-3xl font-black text-emerald-600">+${totalOwed.toFixed(2)}</h3>
            <p className="text-xs text-slate-400 mt-1">From friends across your active groups</p>
          </div>
          <div className="h-1.5 w-full bg-emerald-50 rounded-full overflow-hidden">
            <div className="h-full bg-emerald-500 rounded-full" style={{ width: '100%' }} />
          </div>
        </div>

        {/* You owe Card */}
        <div className="bg-white rounded-3xl p-6 border border-slate-200/80 shadow-xs flex flex-col justify-between">
          <div className="flex items-center justify-between text-slate-400">
            <span className="text-xs font-bold uppercase tracking-wider">You owe</span>
            <TrendingDown className="w-5 h-5 text-rose-500" />
          </div>
          <div className="my-4">
            <h3 className="text-3xl font-black text-rose-500">-${totalOwe.toFixed(2)}</h3>
            <p className="text-xs text-slate-400 mt-1">Ready to settle up with members</p>
          </div>
          <div className="h-1.5 w-full bg-rose-50 rounded-full overflow-hidden">
            <div className="h-full bg-rose-500 rounded-full" style={{ width: '100%' }} />
          </div>
        </div>
      </div>

      {/* Groups Section */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-bold text-slate-900">Your Active Groups</h3>
          <button
            onClick={() => navigate('/groups')}
            className="text-xs font-bold text-brand-600 hover:text-brand-700 flex items-center gap-1"
          >
            View all ({groups.length}) <ArrowRight className="w-3.5 h-3.5" />
          </button>
        </div>

        {groups.length === 0 ? (
          <div className="bg-white p-8 rounded-3xl border border-dashed border-slate-200 text-center">
            <Users className="w-10 h-10 text-slate-300 mx-auto mb-2" />
            <h4 className="text-sm font-bold text-slate-800">No active groups</h4>
            <p className="text-xs text-slate-500 mt-1 mb-4">
              Create a group or ask a friend for their invitation code.
            </p>
            <Button size="sm" onClick={() => setIsCreateOpen(true)}>
              <Plus className="w-4 h-4 mr-1" />
              Create Group
            </Button>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {groups.slice(0, 6).map((group) => (
              <div
                key={group.id}
                onClick={() => navigate(`/groups/${group.id}`)}
                className="bg-white p-5 rounded-2xl border border-slate-200/80 hover:border-brand-300 hover:shadow-md transition-all cursor-pointer flex items-center justify-between group"
              >
                <div>
                  <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-slate-100 text-slate-600 uppercase">
                    {group.groupType}
                  </span>
                  <h4 className="text-base font-bold text-slate-900 group-hover:text-brand-600 transition-colors mt-1">
                    {group.name}
                  </h4>
                  <p className="text-xs text-slate-400 mt-0.5">{group.members?.length || 1} members</p>
                </div>

                <div className="w-8 h-8 rounded-full bg-slate-50 flex items-center justify-center text-slate-400 group-hover:bg-brand-50 group-hover:text-brand-600 transition">
                  <ArrowRight className="w-4 h-4" />
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      <CreateGroupModal
        isOpen={isCreateOpen}
        onClose={() => setIsCreateOpen(false)}
        onCreated={() => {
          setIsCreateOpen(false);
          loadDashboard();
        }}
      />
    </div>
  );
};

