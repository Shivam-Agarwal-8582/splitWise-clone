import React, { useState } from 'react';
import { Outlet, Navigate } from 'react-router-dom';
import { useAuthStore } from '../../store/authStore';
import { Navbar } from './Navbar';
import { Sidebar } from './Sidebar';
import { CreateGroupModal } from '../../pages/groups/CreateGroupModal';

export const AppLayout: React.FC = () => {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  const [isCreateGroupOpen, setIsCreateGroupOpen] = useState(false);

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      <Navbar />
      <div className="flex flex-1">
        <Sidebar onOpenCreateGroup={() => setIsCreateGroupOpen(true)} />
        <main className="flex-1 p-4 sm:p-8 max-w-7xl mx-auto w-full">
          <Outlet />
        </main>
      </div>

      <CreateGroupModal
        isOpen={isCreateGroupOpen}
        onClose={() => setIsCreateGroupOpen(false)}
        onCreated={() => {
          setIsCreateGroupOpen(false);
          window.location.reload();
        }}
      />
    </div>
  );
};

