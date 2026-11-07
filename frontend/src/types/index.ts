export type SplitType = 'EQUAL' | 'EXACT' | 'PERCENTAGE' | 'SHARE';

export type ExpenseCategory =
  | 'FOOD'
  | 'RENT'
  | 'TRANSPORTATION'
  | 'UTILITIES'
  | 'ENTERTAINMENT'
  | 'SHOPPING'
  | 'OTHER';

export type GroupRole = 'ADMIN' | 'MEMBER';
export type GroupType = 'HOME' | 'TRIP' | 'COUPLE' | 'OTHER';

export interface User {
  id: number;
  email: string;
  fullName: string;
  phoneNumber?: string;
  avatarUrl?: string;
  defaultCurrency: string;
}

export interface GroupMember {
  id: number;
  user: User;
  role: GroupRole;
  joinedAt: string;
}

export interface Group {
  id: number;
  name: string;
  description?: string;
  groupType: GroupType;
  currency: string;
  avatarUrl?: string;
  simplifyDebtsDefault: boolean;
  inviteCode?: string;
  createdBy: User;
  members: GroupMember[];
  createdAt: string;
}

export interface ExpensePayer {
  id?: number;
  userId: number;
  user?: User;
  amountPaid: number;
}

export interface ExpenseSplit {
  id?: number;
  userId: number;
  user?: User;
  owedAmount: number;
  splitValue?: number;
}

export interface Expense {
  id: number;
  groupId: number;
  description: string;
  category: ExpenseCategory;
  totalAmount: number;
  currency: string;
  splitType: SplitType;
  expenseDate: string;
  createdBy: User;
  payers: ExpensePayer[];
  splits: ExpenseSplit[];
  createdAt: string;
  updatedAt: string;
}

export interface Settlement {
  id: number;
  groupId: number;
  payer: User;
  payee: User;
  amount: number;
  currency: string;
  settlementDate: string;
  notes?: string;
}

export interface MemberBalance {
  user: User;
  totalPaid: number;
  totalOwed: number;
  netBalance: number; // Positive = to receive, Negative = owes
}

export interface SimplifiedDebt {
  debtor: User;
  creditor: User;
  amount: number;
  currency: string;
}

export interface GroupBalanceSummary {
  groupId: number;
  currency: string;
  totalGroupSpending: number;
  memberBalances: MemberBalance[];
  simplifiedDebts: SimplifiedDebt[];
}

export interface ChatMessage {
  id: number;
  groupId: number;
  sender: User;
  messageType: 'TEXT' | 'IMAGE' | 'SYSTEM';
  content: string;
  mediaUrl?: string;
  isEdited: boolean;
  createdAt: string;
  readByUserIds: number[];
}

export interface TypingIndicator {
  groupId: number;
  userId: number;
  userName: string;
  isTyping: boolean;
}

export interface SpendingByCategory {
  category: ExpenseCategory;
  amount: number;
  percentage: number;
}

export interface MonthlySpending {
  month: string;
  totalAmount: number;
}

export interface GroupAnalytics {
  groupId: number;
  totalSpending: number;
  categoryBreakdown: SpendingByCategory[];
  monthlyTrends: MonthlySpending[];
}

