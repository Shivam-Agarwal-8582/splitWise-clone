import React from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Users, User, PlusCircle } from 'lucide-react';

interface SidebarProps {
  onOpenCreateGroup?: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ onOpenCreateGroup }) => {
  const links = [
    { name: 'Dashboard', to: '/dashboard', icon: LayoutDashboard },
    { name: 'Groups', to: '/groups', icon: Users },
    { name: 'Profile', to: '/profile', icon: User },
  ];

  return (
    <aside className="w-64 border-r border-slate-200 bg-white min-h-[calc(100vh-4rem)] p-4 flex flex-col justify-between hidden md:flex">
      <div className="space-y-6">
        {onOpenCreateGroup && (
          <button
            onClick={onOpenCreateGroup}
            className="w-full flex items-center justify-center gap-2 px-4 py-2.5 rounded-xl bg-brand-50 text-brand-600 hover:bg-brand-100 font-semibold text-sm transition-colors border border-brand-200/60"
          >
            <PlusCircle className="w-4 h-4 text-brand-500" />
            Create Group
          </button>
        )}

        <nav className="space-y-1">
          {links.map((link) => {
            const Icon = link.icon;
            return (
              <NavLink
                key={link.to}
                to={link.to}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm font-semibold transition-colors ${
                    isActive
                      ? 'bg-brand-500 text-white shadow-xs'
                      : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
                  }`
                }
              >
                <Icon className="w-4 h-4" />
                {link.name}
              </NavLink>
            );
          })}
        </nav>
      </div>

      <div className="p-3 bg-slate-50 rounded-xl border border-slate-100 text-xs text-slate-500">
        <p className="font-semibold text-slate-700">Splitwise Production</p>
        <p className="mt-0.5">Real-time STOMP & Min-Cash-Flow</p>
      </div>
    </aside>
  );
};

