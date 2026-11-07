import React from 'react';

interface AvatarProps {
  name?: string;
  url?: string;
  size?: 'sm' | 'md' | 'lg' | 'xl';
  className?: string;
}

export const Avatar: React.FC<AvatarProps> = ({ name = 'User', url, size = 'md', className = '' }) => {
  const sizeClasses = {
    sm: 'w-7 h-7 text-xs',
    md: 'w-9 h-9 text-sm',
    lg: 'w-12 h-12 text-base font-bold',
    xl: 'w-16 h-16 text-xl font-bold',
  };

  const getInitials = (n: string) => {
    return n
      .split(' ')
      .map((part) => part[0])
      .slice(0, 2)
      .join('')
      .toUpperCase();
  };

  if (url) {
    return (
      <img
        src={url}
        alt={name}
        className={`${sizeClasses[size]} rounded-full object-cover border border-slate-200 ${className}`}
      />
    );
  }

  // Consistent background color based on name
  const colors = [
    'bg-emerald-500',
    'bg-blue-500',
    'bg-indigo-500',
    'bg-violet-500',
    'bg-amber-500',
    'bg-rose-500',
    'bg-teal-500',
  ];
  const charCode = name.charCodeAt(0) || 0;
  const bgColor = colors[charCode % colors.length];

  return (
    <div
      className={`${sizeClasses[size]} ${bgColor} text-white font-semibold rounded-full flex items-center justify-center select-none shadow-sm ${className}`}
    >
      {getInitials(name)}
    </div>
  );
};

