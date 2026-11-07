import React from 'react';
import { useAuthStore } from '../../store/authStore';
import { Avatar } from '../ui/Avatar';
import { ThemeToggle } from '../ui/ThemeToggle';
import { LogOut, Bell } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const Navbar: React.FC = () => {
  const user = useAuthStore((s) => s.user);
  const logout = useAuthStore((s) => s.logout);
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="h-16 border-b border-slate-200 dark:border-slate-800 bg-white/95 dark:bg-slate-900/95 backdrop-blur-sm sticky top-0 z-30 flex items-center justify-between px-4 sm:px-8 shadow-xs transition-colors">
      <div className="flex items-center gap-3 cursor-pointer" onClick={() => navigate('/dashboard')}>
        <div className="w-9 h-9 rounded-xl bg-brand-500 text-white flex items-center justify-center font-black text-xl shadow-sm shadow-brand-500/40">
          S
        </div>
        <span className="font-extrabold text-xl tracking-tight text-slate-900 dark:text-slate-100 hidden sm:inline">
          Split<span className="text-brand-500">Wise</span>
        </span>
      </div>

      <div className="flex items-center gap-3 sm:gap-4">
        {/* Dark/Light mode toggle */}
        <ThemeToggle />

        <button
          className="p-2 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 rounded-xl hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          title="Notifications"
        >
          <Bell className="w-5 h-5" />
        </button>

        <div
          onClick={() => navigate('/profile')}
          className="flex items-center gap-3 cursor-pointer p-1.5 rounded-xl hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
        >
          <Avatar name={user?.fullName} url={user?.avatarUrl} size="sm" />
          <div className="text-left hidden md:block">
            <p className="text-xs font-semibold text-slate-900 dark:text-slate-100 leading-none">{user?.fullName}</p>
            <p className="text-[10px] text-slate-500 dark:text-slate-400 mt-0.5">{user?.email}</p>
          </div>
        </div>

        <button
          onClick={handleLogout}
          className="p-2 text-slate-400 hover:text-rose-500 dark:hover:text-rose-400 rounded-xl hover:bg-rose-50 dark:hover:bg-rose-950/40 transition-colors"
          title="Sign out"
        >
          <LogOut className="w-5 h-5" />
        </button>
      </div>
    </header>
  );
};
