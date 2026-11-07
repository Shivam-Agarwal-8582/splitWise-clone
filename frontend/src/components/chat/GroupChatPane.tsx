import React, { useState, useRef, useEffect } from 'react';
import { useGroupChat } from '../../hooks/useGroupChat';
import { useAuthStore } from '../../store/authStore';
import { Avatar } from '../ui/Avatar';
import { Button } from '../ui/Button';
import { Send, Image, CheckCheck, Loader2 } from 'lucide-react';
import { apiClient } from '../../lib/api';

interface GroupChatPaneProps {
  groupId: number;
}

export const GroupChatPane: React.FC<GroupChatPaneProps> = ({ groupId }) => {
  const currentUser = useAuthStore((s) => s.user);
  const [inputText, setInputText] = useState('');
  const [isUploading, setIsUploading] = useState(false);
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const messagesEndRef = useRef<HTMLDivElement | null>(null);

  const {
    messages,
    typingUsers,
    isConnected,
    isLoading,
    error,
    sendMessage,
    sendTyping,
    markAsRead,
  } = useGroupChat(groupId);

  // Auto-scroll to bottom on new messages
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, typingUsers]);

  // Mark latest incoming unread message as read
  useEffect(() => {
    if (messages.length > 0 && currentUser) {
      const lastMsg = messages[messages.length - 1];
      if (lastMsg.sender.id !== currentUser.id && !lastMsg.readByUserIds.includes(currentUser.id)) {
        markAsRead(lastMsg.id);
      }
    }
  }, [messages, currentUser, markAsRead]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setInputText(e.target.value);
    sendTyping(true);
  };

  const handleSend = (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputText.trim()) return;
    sendMessage(inputText.trim());
    setInputText('');
    sendTyping(false);
  };

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setIsUploading(true);
    const formData = new FormData();
    formData.append('file', file);

    try {
      const res: any = await apiClient.post('/media/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      if (res?.data?.fileUrl) {
        sendMessage(file.name, res.data.fileUrl);
      }
    } catch (err) {
      console.error('File upload failed', err);
    } finally {
      setIsUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  return (
    <div className="flex flex-col h-[600px] bg-white dark:bg-slate-900 rounded-3xl border border-slate-200/80 dark:border-slate-800 shadow-xs overflow-hidden transition-colors">
      {/* Chat header */}
      <div className="px-5 py-3.5 border-b border-slate-100 dark:border-slate-800 flex items-center justify-between bg-slate-50/50 dark:bg-slate-850/50">
        <div className="flex items-center gap-2.5">
          <div className="relative">
            <span
              className={`w-2.5 h-2.5 rounded-full inline-block ${
                isConnected ? 'bg-emerald-500 animate-pulse' : 'bg-slate-300 dark:bg-slate-700'
              }`}
            />
          </div>
          <div>
            <h4 className="text-sm font-bold text-slate-800 dark:text-slate-200">Group Discussion</h4>
            <p className="text-[11px] text-slate-500 dark:text-slate-400">
              {isConnected ? 'Real-time WebSocket active' : 'Connecting to STOMP broker...'}
            </p>
          </div>
        </div>

        {error && (
          <span className="text-xs text-rose-500 font-medium bg-rose-50 dark:bg-rose-950/40 px-2.5 py-1 rounded-lg">
            {error}
          </span>
        )}
      </div>

      {/* Message history */}
      <div className="flex-1 overflow-y-auto p-4 space-y-4">
        {isLoading ? (
          <div className="flex items-center justify-center h-full text-slate-400 text-sm gap-2">
            <Loader2 className="w-5 h-5 animate-spin text-brand-500" />
            Loading conversation...
          </div>
        ) : messages.length === 0 ? (
          <div className="text-center py-16 text-slate-400 dark:text-slate-500 text-xs">
            No messages in this group yet. Say hello or share an expense!
          </div>
        ) : (
          messages.map((msg) => {
            const isMe = msg.sender.id === currentUser?.id;
            return (
              <div
                key={msg.id}
                className={`flex items-end gap-2.5 ${isMe ? 'flex-row-reverse' : 'flex-row'}`}
              >
                {!isMe && <Avatar name={msg.sender.fullName} url={msg.sender.avatarUrl} size="sm" />}

                <div
                  className={`max-w-[75%] rounded-2xl p-3.5 shadow-xs ${
                    isMe
                      ? 'bg-brand-500 text-white rounded-br-xs'
                      : 'bg-slate-100 dark:bg-slate-800 text-slate-900 dark:text-slate-100 rounded-bl-xs'
                  }`}
                >
                  {!isMe && (
                    <p className="text-[11px] font-bold text-slate-600 dark:text-slate-300 mb-1 leading-none">
                      {msg.sender.fullName}
                    </p>
                  )}

                  {msg.messageType === 'IMAGE' && msg.mediaUrl ? (
                    <div className="mb-2 rounded-xl overflow-hidden border border-black/10 dark:border-white/10">
                      <img src={msg.mediaUrl} alt="Attachment" className="max-h-60 object-cover" />
                    </div>
                  ) : null}

                  <p className="text-sm leading-relaxed whitespace-pre-wrap">{msg.content}</p>

                  <div
                    className={`flex items-center justify-end gap-1 text-[10px] mt-1.5 ${
                      isMe ? 'text-brand-100' : 'text-slate-400 dark:text-slate-400'
                    }`}
                  >
                    <span>
                      {new Date(msg.createdAt).toLocaleTimeString([], {
                        hour: '2-digit',
                        minute: '2-digit',
                      })}
                    </span>
                    {isMe && (
                      <CheckCheck
                        className={`w-3.5 h-3.5 ${
                          msg.readByUserIds.length > 0 ? 'text-brand-200' : 'opacity-60'
                        }`}
                      />
                    )}
                  </div>
                </div>
              </div>
            );
          })
        )}

        {/* Typing indicator banner */}
        {typingUsers.length > 0 && (
          <div className="flex items-center gap-2 text-xs text-slate-400 dark:text-slate-500 italic py-1 animate-pulse">
            <span className="w-1.5 h-1.5 bg-brand-500 rounded-full animate-bounce" />
            <span>{typingUsers.join(', ')} is typing...</span>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* Input bar */}
      <form onSubmit={handleSend} className="p-3 border-t border-slate-100 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-850/50 flex items-center gap-2">
        <input
          type="file"
          ref={fileInputRef}
          onChange={handleFileUpload}
          accept="image/*"
          className="hidden"
        />

        <button
          type="button"
          onClick={() => fileInputRef.current?.click()}
          disabled={isUploading}
          className="p-2.5 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 rounded-xl hover:bg-slate-200/60 dark:hover:bg-slate-800 transition disabled:opacity-50"
          title="Share receipt or image"
        >
          {isUploading ? <Loader2 className="w-5 h-5 animate-spin" /> : <Image className="w-5 h-5" />}
        </button>

        <input
          type="text"
          placeholder="type_message"
          value={inputText}
          onChange={handleInputChange}
          className="flex-1 px-4 py-2.5 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100 text-sm outline-none focus:border-brand-500"
        />

        <Button type="submit" size="sm" disabled={!inputText.trim()}>
          <Send className="w-4 h-4" />
        </Button>
      </form>
    </div>
  );
};
