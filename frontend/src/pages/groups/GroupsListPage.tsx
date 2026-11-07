import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '../../lib/api';
import { Group } from '../../types';
import { Button } from '../../components/ui/Button';
import { CreateGroupModal } from './CreateGroupModal';
import { Plus, Users, ArrowRight, KeyRound, Sparkles } from 'lucide-react';

export const GroupsListPage: React.FC = () => {
  const [groups, setGroups] = useState<Group[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [inviteCode, setInviteCode] = useState('');
  const [isJoining, setIsJoining] = useState(false);
  const [joinError, setJoinError] = useState<string | null>(null);

  const navigate = useNavigate();

  const fetchGroups = async () => {
    try {
      const response: any = await apiClient.get('/groups');
      if (response?.data) {
        setGroups(response.data);
      }
    } catch (e) {
      console.error('Failed to fetch groups', e);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchGroups();
  }, []);

  const handleJoinByCode = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!inviteCode.trim()) return;

    setIsJoining(true);
    setJoinError(null);

    try {
      const response: any = await apiClient.post(`/groups/join/${inviteCode.trim().toUpperCase()}`);
      if (response?.data) {
        setInviteCode('');
        fetchGroups();
        navigate(`/groups/${response.data.id}`);
      }
    } catch (err: any) {
      setJoinError(err?.message || 'Invalid or expired invitation code');
    } finally {
      setIsJoining(false);
    }
  };

  return (
    <div className="space-y-8">
      {/* Header banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-gradient-to-r from-brand-600 to-emerald-700 dark:from-brand-700 dark:to-emerald-900 rounded-3xl p-6 sm:p-8 text-white shadow-lg shadow-brand-500/10">
        <div>
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-white/20 text-xs font-semibold backdrop-blur-md mb-2">
            <Sparkles className="w-3.5 h-3.5" />
            Shared finances simplified
          </span>
          <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight">Your Groups</h1>
          <p className="text-brand-100 text-sm mt-1 max-w-md">
            Manage trips, roommates, or shared projects with real-time balance calculations.
          </p>
        </div>
        <Button
          onClick={() => setIsCreateOpen(true)}
          className="bg-white text-emerald-800 hover:bg-brand-50 shadow-none border-none self-start sm:self-auto py-3 px-5 font-bold"
        >
          <Plus className="w-4 h-4 mr-1 text-emerald-600" />
          Create New Group
        </Button>
      </div>

      {/* Join group via invite code card */}
      <div className="bg-white dark:bg-slate-900 p-5 rounded-2xl border border-slate-200/80 dark:border-slate-800 shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4 transition-colors">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-brand-50 dark:bg-brand-950/40 flex items-center justify-center text-brand-600 dark:text-brand-400">
            <KeyRound className="w-5 h-5" />
          </div>
          <div>
            <h4 className="text-sm font-bold text-slate-900 dark:text-slate-100">Have an invitation code?</h4>
            <p className="text-xs text-slate-500 dark:text-slate-400">Enter code to instantly join an existing group</p>
          </div>
        </div>

        <form onSubmit={handleJoinByCode} className="flex items-center gap-2 w-full sm:w-auto">
          <input
            type="text"
            placeholder="INVITE_CODE"
            value={inviteCode}
            onChange={(e) => setInviteCode(e.target.value)}
            className="px-3.5 py-2 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100 text-sm outline-none focus:border-brand-500 uppercase tracking-widest font-mono text-center w-full sm:w-44"
          />
          <Button type="submit" size="sm" isLoading={isJoining} disabled={!inviteCode.trim()}>
            Join
          </Button>
        </form>
      </div>

      {joinError && (
        <div className="p-3 rounded-xl bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-900 text-rose-600 dark:text-rose-400 text-xs font-medium">
          {joinError}
        </div>
      )}

      {/* Group grid */}
      <div>
        <h3 className="text-base font-bold text-slate-800 dark:text-slate-200 mb-4">Active Groups ({groups.length})</h3>

        {isLoading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {[1, 2, 3].map((i) => (
              <div key={i} className="h-44 bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 animate-pulse p-6" />
            ))}
          </div>
        ) : groups.length === 0 ? (
          <div className="text-center py-12 bg-white dark:bg-slate-900 rounded-2xl border border-dashed border-slate-300 dark:border-slate-800 p-8">
            <div className="w-12 h-12 rounded-full bg-brand-50 dark:bg-brand-950/40 text-brand-500 flex items-center justify-center mx-auto mb-3">
              <Users className="w-6 h-6" />
            </div>
            <h4 className="text-base font-bold text-slate-800 dark:text-slate-200">No groups yet</h4>
            <p className="text-sm text-slate-500 dark:text-slate-400 max-w-sm mx-auto mt-1 mb-4">
              Create your first group or join one using an invitation code to start splitting expenses.
            </p>
            <Button onClick={() => setIsCreateOpen(true)} size="sm">
              <Plus className="w-4 h-4 mr-1" />
              Create Group
            </Button>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
            {groups.map((group) => (
              <div
                key={group.id}
                onClick={() => navigate(`/groups/${group.id}`)}
                className="bg-white dark:bg-slate-900 rounded-2xl p-6 border border-slate-200/80 dark:border-slate-800 hover:border-brand-300 dark:hover:border-brand-700 hover:shadow-md transition-all cursor-pointer flex flex-col justify-between group"
              >
                <div>
                  <div className="flex items-start justify-between mb-3">
                    <span className="text-[11px] font-bold px-2.5 py-1 rounded-full bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300 uppercase tracking-wider">
                      {group.groupType}
                    </span>
                    <span className="text-xs font-semibold text-slate-400 dark:text-slate-500">
                      {group.currency} {group.currency === 'INR' ? '(+USD)' : ''}
                    </span>
                  </div>

                  <h4 className="text-lg font-bold text-slate-900 dark:text-slate-100 group-hover:text-brand-600 dark:group-hover:text-brand-400 transition-colors">
                    {group.name}
                  </h4>
                  {group.description && (
                    <p className="text-xs text-slate-500 dark:text-slate-400 mt-1 line-clamp-2">{group.description}</p>
                  )}
                </div>

                <div className="pt-6 border-t border-slate-100 dark:border-slate-800 mt-4 flex items-center justify-between">
                  <div className="flex items-center gap-1.5 text-xs text-slate-500 dark:text-slate-400 font-medium">
                    <Users className="w-3.5 h-3.5 text-slate-400" />
                    {group.members?.length || 1} members
                  </div>

                  <span className="text-xs font-bold text-brand-600 dark:text-brand-400 flex items-center gap-1 group-hover:translate-x-0.5 transition-transform">
                    View <ArrowRight className="w-3.5 h-3.5" />
                  </span>
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
          fetchGroups();
        }}
      />
    </div>
  );
};
