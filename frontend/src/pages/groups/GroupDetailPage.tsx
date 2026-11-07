import React, { useEffect, useState, useCallback, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { apiClient } from '../../lib/api';
import { Group, Expense, GroupBalanceSummary, SimplifiedDebt } from '../../types';
import { useAuthStore } from '../../store/authStore';
import { Button } from '../../components/ui/Button';
import { Avatar } from '../../components/ui/Avatar';
import { MoneyDisplay } from '../../components/ui/MoneyDisplay';
import { AddExpenseModal } from '../../components/expenses/AddExpenseModal';
import { SettleUpModal } from '../../components/settlements/SettleUpModal';
import { GroupChatPane } from '../../components/chat/GroupChatPane';
import {
  Plus,
  ArrowRight,
  Copy,
  Check,
  Receipt,
  Scale,
  MessageSquare,
  BarChart3,
  Trash2,
  Search,
  Download,
} from 'lucide-react';

export const GroupDetailPage: React.FC = () => {
  const { groupId } = useParams<{ groupId: string }>();
  const currentUser = useAuthStore((s) => s.user);
  const navigate = useNavigate();

  const [group, setGroup] = useState<Group | null>(null);
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [balances, setBalances] = useState<GroupBalanceSummary | null>(null);
  const [analytics, setAnalytics] = useState<any | null>(null);

  const [activeTab, setActiveTab] = useState<'expenses' | 'balances' | 'chat' | 'analytics'>('expenses');
  const [isAddExpenseOpen, setIsAddExpenseOpen] = useState(false);
  const [isSettleUpOpen, setIsSettleUpOpen] = useState(false);
  const [selectedDebt, setSelectedDebt] = useState<SimplifiedDebt | null>(null);

  const [searchTerm, setSearchTerm] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('ALL');

  const [copiedCode, setCopiedCode] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  const loadAllGroupData = useCallback(async () => {
    if (!groupId) return;
    try {
      const [groupRes, expRes, balRes, analyticsRes]: any = await Promise.all([
        apiClient.get(`/groups/${groupId}`),
        apiClient.get(`/groups/${groupId}/expenses`),
        apiClient.get(`/groups/${groupId}/balances`),
        apiClient.get(`/groups/${groupId}/analytics`),
      ]);

      if (groupRes?.data) setGroup(groupRes.data);
      if (expRes?.data) setExpenses(expRes.data);
      if (balRes?.data) setBalances(balRes.data);
      if (analyticsRes?.data) setAnalytics(analyticsRes.data);
    } catch (err) {
      console.error('Failed to load group details', err);
    } finally {
      setIsLoading(false);
    }
  }, [groupId]);

  useEffect(() => {
    loadAllGroupData();
  }, [loadAllGroupData]);

  const handleCopyInviteCode = () => {
    if (group?.inviteCode) {
      navigator.clipboard.writeText(group.inviteCode);
      setCopiedCode(true);
      setTimeout(() => setCopiedCode(false), 2000);
    }
  };

  const handleDeleteExpense = async (expenseId: number) => {
    if (!confirm('Are you sure you want to delete this expense?')) return;
    try {
      await apiClient.delete(`/groups/${groupId}/expenses/${expenseId}`);
      loadAllGroupData();
    } catch (err: any) {
      alert(err?.message || 'Failed to delete expense');
    }
  };

  // Filtered expenses based on search & category
  const filteredExpenses = useMemo(() => {
    return expenses.filter((e) => {
      const matchesSearch =
        !searchTerm.trim() ||
        e.description.toLowerCase().includes(searchTerm.toLowerCase().trim()) ||
        e.payers.some((p) => p.user?.fullName.toLowerCase().includes(searchTerm.toLowerCase().trim()));
      const matchesCategory = categoryFilter === 'ALL' || e.category === categoryFilter;
      return matchesSearch && matchesCategory;
    });
  }, [expenses, searchTerm, categoryFilter]);

  // Quick CSV Export
  const exportExpensesCSV = () => {
    if (expenses.length === 0) return;
    const headers = ['Date', 'Description', 'Category', 'Amount', 'Currency', 'SplitType', 'PaidBy'];
    const rows = expenses.map((e) => [
      e.expenseDate,
      `"${e.description.replace(/"/g, '""')}"`,
      e.category,
      e.totalAmount,
      e.currency,
      e.splitType,
      `"${e.payers.map((p) => p.user?.fullName).join(', ')}"`,
    ]);
    const csvContent = 'data:text/csv;charset=utf-8,' + [headers.join(','), ...rows.map((r) => r.join(','))].join('\n');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `${group?.name || 'group'}_expenses.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  if (isLoading) {
    return (
      <div className="space-y-4 animate-pulse">
        <div className="h-40 bg-white dark:bg-slate-900 rounded-3xl" />
        <div className="h-96 bg-white dark:bg-slate-900 rounded-3xl" />
      </div>
    );
  }

  if (!group) {
    return (
      <div className="text-center py-12">
        <p className="text-slate-500">Group not found or you do not have permission to view it.</p>
        <Button onClick={() => navigate('/groups')} className="mt-4" size="sm">
          Back to Groups
        </Button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Top Banner Card */}
      <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 sm:p-8 border border-slate-200/80 dark:border-slate-800 shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-6 transition-colors">
        <div>
          <div className="flex items-center gap-2 mb-2">
            <span className="text-xs font-bold px-2.5 py-1 rounded-full bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300 uppercase">
              {group.groupType}
            </span>
            <span className="text-xs font-semibold text-slate-400 dark:text-slate-500">
              Currency: {group.currency} {group.currency === 'INR' ? '(Dual USD)' : ''}
            </span>
          </div>

          <h1 className="text-2xl sm:text-3xl font-black text-slate-900 dark:text-slate-100 tracking-tight">
            {group.name}
          </h1>
          {group.description && (
            <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">{group.description}</p>
          )}

          {/* Members Avatars & Invite Code */}
          <div className="flex flex-wrap items-center gap-4 mt-4 pt-4 border-t border-slate-100 dark:border-slate-800">
            <div className="flex -space-x-2">
              {group.members.map((m) => (
                <Avatar
                  key={m.user.id}
                  name={m.user.fullName}
                  url={m.user.avatarUrl}
                  size="sm"
                  className="ring-2 ring-white dark:ring-slate-900"
                />
              ))}
            </div>

            {group.inviteCode && (
              <button
                onClick={handleCopyInviteCode}
                className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 text-xs font-mono font-semibold text-slate-700 dark:text-slate-300 transition"
                title="Click to copy invite code"
              >
                {copiedCode ? <Check className="w-3.5 h-3.5 text-emerald-600" /> : <Copy className="w-3.5 h-3.5" />}
                Invite: {group.inviteCode}
              </button>
            )}
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex items-center gap-3">
          <Button
            onClick={() => {
              setSelectedDebt(null);
              setIsSettleUpOpen(true);
            }}
            variant="outline"
          >
            Settle Up
          </Button>
          <Button onClick={() => setIsAddExpenseOpen(true)}>
            <Plus className="w-4 h-4 mr-1" />
            Add Expense
          </Button>
        </div>
      </div>

      {/* Tabs navigation */}
      <div className="flex border-b border-slate-200 dark:border-slate-800 gap-2 sm:gap-6 text-sm font-bold text-slate-500 dark:text-slate-400 overflow-x-auto">
        <button
          onClick={() => setActiveTab('expenses')}
          className={`pb-3 px-1 flex items-center gap-2 border-b-2 transition whitespace-nowrap ${
            activeTab === 'expenses'
              ? 'border-brand-500 text-brand-600 dark:text-brand-400'
              : 'border-transparent hover:text-slate-800 dark:hover:text-slate-200'
          }`}
        >
          <Receipt className="w-4 h-4" />
          Expenses ({expenses.length})
        </button>

        <button
          onClick={() => setActiveTab('balances')}
          className={`pb-3 px-1 flex items-center gap-2 border-b-2 transition whitespace-nowrap ${
            activeTab === 'balances'
              ? 'border-brand-500 text-brand-600 dark:text-brand-400'
              : 'border-transparent hover:text-slate-800 dark:hover:text-slate-200'
          }`}
        >
          <Scale className="w-4 h-4" />
          Balances & Simplification
        </button>

        <button
          onClick={() => setActiveTab('chat')}
          className={`pb-3 px-1 flex items-center gap-2 border-b-2 transition whitespace-nowrap ${
            activeTab === 'chat'
              ? 'border-brand-500 text-brand-600 dark:text-brand-400'
              : 'border-transparent hover:text-slate-800 dark:hover:text-slate-200'
          }`}
        >
          <MessageSquare className="w-4 h-4" />
          Group Chat
        </button>

        <button
          onClick={() => setActiveTab('analytics')}
          className={`pb-3 px-1 flex items-center gap-2 border-b-2 transition whitespace-nowrap ${
            activeTab === 'analytics'
              ? 'border-brand-500 text-brand-600 dark:text-brand-400'
              : 'border-transparent hover:text-slate-800 dark:hover:text-slate-200'
          }`}
        >
          <BarChart3 className="w-4 h-4" />
          Analytics
        </button>
      </div>

      {/* Tab 1: Expenses List with Search, Filter & CSV Export */}
      {activeTab === 'expenses' && (
        <div className="space-y-4">
          <div className="flex flex-col sm:flex-row items-center justify-between gap-3 bg-white dark:bg-slate-900 p-3 rounded-2xl border border-slate-200/80 dark:border-slate-800">
            <div className="relative w-full sm:w-72">
              <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
              <input
                type="text"
                placeholder="search_expense_or_payer"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-9 pr-3 py-1.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-800 text-xs text-slate-900 dark:text-slate-100 outline-none focus:border-brand-500"
              />
            </div>

            <div className="flex items-center gap-2 w-full sm:w-auto justify-end">
              <select
                value={categoryFilter}
                onChange={(e) => setCategoryFilter(e.target.value)}
                className="px-3 py-1.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-800 text-xs text-slate-700 dark:text-slate-200 outline-none"
              >
                <option value="ALL">All Categories</option>
                <option value="FOOD">Food</option>
                <option value="RENT">Rent</option>
                <option value="TRANSPORTATION">Transportation</option>
                <option value="UTILITIES">Utilities</option>
                <option value="ENTERTAINMENT">Entertainment</option>
                <option value="SHOPPING">Shopping</option>
                <option value="OTHER">Other</option>
              </select>

              <button
                onClick={exportExpensesCSV}
                className="p-2 text-slate-500 hover:text-slate-700 dark:text-slate-400 dark:hover:text-slate-200 rounded-xl hover:bg-slate-100 dark:hover:bg-slate-800 transition"
                title="Export CSV"
              >
                <Download className="w-4 h-4" />
              </button>
            </div>
          </div>

          {filteredExpenses.length === 0 ? (
            <div className="text-center py-16 bg-white dark:bg-slate-900 rounded-3xl border border-dashed border-slate-300 dark:border-slate-800 p-8">
              <Receipt className="w-10 h-10 text-slate-300 dark:text-slate-600 mx-auto mb-2" />
              <h4 className="text-sm font-bold text-slate-800 dark:text-slate-200">No expenses found</h4>
              <p className="text-xs text-slate-500 dark:text-slate-400 mt-1 mb-4">
                Click the "Add Expense" button to split your bill.
              </p>
              <Button onClick={() => setIsAddExpenseOpen(true)} size="sm">
                <Plus className="w-4 h-4 mr-1" />
                Add Expense
              </Button>
            </div>
          ) : (
            filteredExpenses.map((expense) => {
              const primaryPayer = expense.payers[0];
              const userSplit = expense.splits.find((s) => s.userId === currentUser?.id);
              const userPaid = expense.payers.find((p) => p.userId === currentUser?.id)?.amountPaid || 0;
              const netEffect = userPaid - (userSplit?.owedAmount || 0);

              return (
                <div
                  key={expense.id}
                  className="bg-white dark:bg-slate-900 p-4 sm:p-5 rounded-2xl border border-slate-200/80 dark:border-slate-800 shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4 hover:border-slate-300 dark:hover:border-slate-700 transition"
                >
                  <div className="flex items-start gap-4">
                    <div className="w-12 h-12 rounded-2xl bg-brand-50 dark:bg-brand-950/40 text-brand-600 dark:text-brand-400 flex flex-col items-center justify-center font-bold text-xs shrink-0">
                      <span>{new Date(expense.expenseDate).getDate()}</span>
                      <span className="text-[10px] text-brand-400 uppercase">
                        {new Date(expense.expenseDate).toLocaleString('default', { month: 'short' })}
                      </span>
                    </div>

                    <div>
                      <h4 className="text-base font-bold text-slate-900 dark:text-slate-100">{expense.description}</h4>
                      <div className="text-xs text-slate-500 dark:text-slate-400 mt-0.5 flex items-baseline gap-1.5 flex-wrap">
                        <span>
                          {expense.payers.length === 1
                            ? `${primaryPayer.user?.fullName} paid`
                            : `${expense.payers.length} people paid`}
                        </span>
                        <MoneyDisplay
                          amount={expense.totalAmount}
                          currency={group.currency}
                          className="font-bold text-slate-800 dark:text-slate-200"
                        />
                        <span>·</span>
                        <span className="font-semibold text-slate-600 dark:text-slate-300">{expense.splitType} split</span>
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center justify-between sm:justify-end gap-6 pt-3 sm:pt-0 border-t sm:border-t-0 border-slate-100 dark:border-slate-800">
                    <div className="text-right">
                      <p className="text-xs text-slate-400 font-medium">Your balance</p>
                      <div
                        className={`text-sm font-extrabold ${
                          netEffect > 0.01
                            ? 'text-emerald-600 dark:text-emerald-400'
                            : netEffect < -0.01
                            ? 'text-rose-500 dark:text-rose-400'
                            : 'text-slate-500'
                        }`}
                      >
                        {Math.abs(netEffect) > 0.01 ? (
                          <MoneyDisplay
                            amount={netEffect}
                            currency={group.currency}
                            showSign={true}
                          />
                        ) : (
                          'not involved'
                        )}
                      </div>
                    </div>

                    <button
                      onClick={() => handleDeleteExpense(expense.id)}
                      className="p-2 text-slate-300 dark:text-slate-600 hover:text-rose-500 dark:hover:text-rose-400 rounded-xl hover:bg-rose-50 dark:hover:bg-rose-950/40 transition"
                      title="Delete expense"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              );
            })
          )}
        </div>
      )}

      {/* Tab 2: Balances & Debt Simplification */}
      {activeTab === 'balances' && balances && (
        <div className="space-y-6">
          {/* Member Net Balances */}
          <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200/80 dark:border-slate-800 shadow-xs transition-colors">
            <h3 className="text-base font-bold text-slate-900 dark:text-slate-100 mb-4">Member Net Balances</h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
              {balances.memberBalances.map((mb) => (
                <div
                  key={mb.user.id}
                  className="p-4 rounded-2xl border border-slate-100 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-850/50 flex items-center justify-between"
                >
                  <div className="flex items-center gap-3">
                    <Avatar name={mb.user.fullName} url={mb.user.avatarUrl} size="sm" />
                    <div>
                      <p className="text-sm font-bold text-slate-800 dark:text-slate-200 leading-tight">
                        {mb.user.fullName}
                      </p>
                      <div className="text-[11px] text-slate-400 flex items-center gap-1 mt-0.5">
                        <span>Paid:</span>
                        <MoneyDisplay amount={mb.totalPaid} currency={group.currency} />
                      </div>
                    </div>
                  </div>

                  <div
                    className={`text-sm font-black ${
                      mb.netBalance > 0.01
                        ? 'text-emerald-600 dark:text-emerald-400'
                        : mb.netBalance < -0.01
                        ? 'text-rose-500 dark:text-rose-400'
                        : 'text-slate-400'
                    }`}
                  >
                    {Math.abs(mb.netBalance) > 0.01 ? (
                      <MoneyDisplay amount={mb.netBalance} currency={group.currency} showSign={true} />
                    ) : (
                      'settled'
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Min-Cash-Flow Simplified Debts */}
          <div className="bg-white dark:bg-slate-900 rounded-3xl p-6 border border-slate-200/80 dark:border-slate-800 shadow-xs transition-colors">
            <div className="flex items-center justify-between mb-4">
              <div>
                <h3 className="text-base font-bold text-slate-900 dark:text-slate-100">
                  Debt Simplification (Min-Cash-Flow)
                </h3>
                <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
                  Greedy priority queue reduction: resolves all group debts in minimal transactions.
                </p>
              </div>
            </div>

            {balances.simplifiedDebts.length === 0 ? (
              <div className="text-center py-8 text-slate-400 dark:text-slate-500 text-sm">
                🎉 All balances are completely settled! No outstanding debts.
              </div>
            ) : (
              <div className="space-y-3">
                {balances.simplifiedDebts.map((debt, idx) => (
                  <div
                    key={idx}
                    className="p-4 rounded-2xl border border-slate-200/80 dark:border-slate-800 bg-white dark:bg-slate-900 flex flex-col sm:flex-row sm:items-center justify-between gap-4"
                  >
                    <div className="flex items-center gap-3">
                      <Avatar name={debt.debtor.fullName} size="sm" />
                      <span className="font-bold text-sm text-slate-900 dark:text-slate-100">{debt.debtor.fullName}</span>
                      <span className="text-xs text-slate-400 flex items-center gap-1 font-semibold">
                        owes <ArrowRight className="w-3.5 h-3.5 text-brand-500" />
                      </span>
                      <Avatar name={debt.creditor.fullName} size="sm" />
                      <span className="font-bold text-sm text-slate-900 dark:text-slate-100">{debt.creditor.fullName}</span>
                    </div>

                    <div className="flex items-center justify-between sm:justify-end gap-4">
                      <div className="text-base font-black text-slate-900 dark:text-slate-100">
                        <MoneyDisplay amount={debt.amount} currency={group.currency} />
                      </div>

                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => {
                          setSelectedDebt(debt);
                          setIsSettleUpOpen(true);
                        }}
                      >
                        Settle Up
                      </Button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}

      {/* Tab 3: Real-time STOMP Group Chat */}
      {activeTab === 'chat' && <GroupChatPane groupId={Number(groupId)} />}

      {/* Tab 4: Analytics */}
      {activeTab === 'analytics' && analytics && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="bg-white dark:bg-slate-900 p-6 rounded-3xl border border-slate-200/80 dark:border-slate-800 shadow-xs">
            <h3 className="text-base font-bold text-slate-900 dark:text-slate-100 mb-4">Spending by Category</h3>
            <div className="space-y-3">
              {analytics.categoryBreakdown?.length === 0 ? (
                <p className="text-xs text-slate-400">No expenses recorded yet.</p>
              ) : (
                analytics.categoryBreakdown?.map((cat: any) => (
                  <div key={cat.category} className="space-y-1">
                    <div className="flex justify-between text-xs font-semibold">
                      <span className="text-slate-700 dark:text-slate-300">{cat.category}</span>
                      <div className="text-slate-900 dark:text-slate-100 flex items-center gap-1">
                        <MoneyDisplay amount={cat.amount} currency={group.currency} />
                        <span>({cat.percentage}%)</span>
                      </div>
                    </div>
                    <div className="w-full bg-slate-100 dark:bg-slate-800 h-2.5 rounded-full overflow-hidden">
                      <div
                        className="bg-brand-500 h-full rounded-full transition-all"
                        style={{ width: `${cat.percentage}%` }}
                      />
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

          <div className="bg-white dark:bg-slate-900 p-6 rounded-3xl border border-slate-200/80 dark:border-slate-800 shadow-xs">
            <h3 className="text-base font-bold text-slate-900 dark:text-slate-100 mb-4">Monthly Spending Trends</h3>
            <div className="space-y-3">
              {analytics.monthlyTrends?.length === 0 ? (
                <p className="text-xs text-slate-400">No monthly data available.</p>
              ) : (
                analytics.monthlyTrends?.map((m: any) => (
                  <div
                    key={m.month}
                    className="flex justify-between items-center p-3 rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-100 dark:border-slate-800 text-xs font-semibold"
                  >
                    <span className="text-slate-600 dark:text-slate-300">{m.month}</span>
                    <MoneyDisplay
                      amount={m.totalAmount}
                      currency={group.currency}
                      className="font-bold text-slate-900 dark:text-slate-100"
                    />
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      )}

      {/* Add Expense Modal */}
      <AddExpenseModal
        isOpen={isAddExpenseOpen}
        onClose={() => setIsAddExpenseOpen(false)}
        group={group}
        onExpenseAdded={loadAllGroupData}
      />

      {/* Settle Up Modal */}
      <SettleUpModal
        isOpen={isSettleUpOpen}
        onClose={() => setIsSettleUpOpen(false)}
        group={group}
        defaultDebtor={selectedDebt?.debtor}
        defaultCreditor={selectedDebt?.creditor}
        defaultAmount={selectedDebt?.amount}
        onSettled={loadAllGroupData}
      />
    </div>
  );
};
