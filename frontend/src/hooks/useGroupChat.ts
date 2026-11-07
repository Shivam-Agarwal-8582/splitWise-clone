import { useEffect, useRef, useState, useCallback } from 'react';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import { ChatMessage, TypingIndicator } from '../types';
import { apiClient } from '../lib/api';
import { useAuthStore } from '../store/authStore';

export function useGroupChat(groupId: number | null) {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [typingUsers, setTypingUsers] = useState<Map<number, string>>(new Map());
  const [isConnected, setIsConnected] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const clientRef = useRef<Client | null>(null);
  const subscriptionsRef = useRef<StompSubscription[]>([]);
  const typingTimeoutRef = useRef<any>(null);
  const user = useAuthStore((s) => s.user);
  const token = useAuthStore((s) => s.token);

  // 1. Fetch initial message history from REST API
  const fetchHistory = useCallback(async () => {
    if (!groupId) return;
    setIsLoading(true);
    setError(null);
    try {
      const response: any = await apiClient.get(`/groups/${groupId}/chat/messages`);
      if (response?.data) {
        setMessages(response.data);
      }
    } catch (err: any) {
      setError(err?.message || 'Failed to load chat history');
    } finally {
      setIsLoading(false);
    }
  }, [groupId]);

  // 2. Set up STOMP client & subscriptions
  useEffect(() => {
    if (!groupId || !token) return;

    fetchHistory();

    const brokerUrl =
      window.location.protocol === 'https:'
        ? `wss://${window.location.host}/ws`
        : `ws://${window.location.host}/ws`;

    const client = new Client({
      brokerURL: brokerUrl,
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      debug: () => {},
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
    });

    client.onConnect = () => {
      setIsConnected(true);

      // Clean old subscriptions
      subscriptionsRef.current.forEach((sub) => sub.unsubscribe());
      subscriptionsRef.current = [];

      // A. Subscribe to group messages
      const chatSub = client.subscribe(`/topic/groups/${groupId}/chat`, (message: IMessage) => {
        try {
          const newMsg: ChatMessage = JSON.parse(message.body);
          setMessages((prev) => {
            // Deduplicate
            if (prev.some((m) => m.id === newMsg.id)) {
              return prev.map((m) => (m.id === newMsg.id ? newMsg : m));
            }
            return [...prev, newMsg];
          });
        } catch (e) {
          console.error('Error parsing chat message', e);
        }
      });
      subscriptionsRef.current.push(chatSub);

      // B. Subscribe to typing indicator
      const typingSub = client.subscribe(`/topic/groups/${groupId}/typing`, (message: IMessage) => {
        try {
          const typingDto: TypingIndicator = JSON.parse(message.body);
          if (typingDto.userId === user?.id) return; // ignore self

          setTypingUsers((prev) => {
            const next = new Map(prev);
            if (typingDto.isTyping) {
              next.set(typingDto.userId, typingDto.userName);
            } else {
              next.delete(typingDto.userId);
            }
            return next;
          });
        } catch (e) {
          console.error('Error parsing typing event', e);
        }
      });
      subscriptionsRef.current.push(typingSub);

      // C. Subscribe to read receipts
      const readSub = client.subscribe(`/topic/groups/${groupId}/read`, (message: IMessage) => {
        try {
          const receipt = JSON.parse(message.body);
          setMessages((prev) =>
            prev.map((m) => {
              if (m.id === receipt.messageId && !m.readByUserIds.includes(receipt.userId)) {
                return { ...m, readByUserIds: [...m.readByUserIds, receipt.userId] };
              }
              return m;
            })
          );
        } catch (e) {
          console.error('Error parsing read receipt', e);
        }
      });
      subscriptionsRef.current.push(readSub);
    };

    client.onDisconnect = () => {
      setIsConnected(false);
    };

    client.onStompError = (frame) => {
      console.error('Broker error:', frame.headers['message']);
      setError(frame.headers['message'] || 'Chat connection error');
    };

    client.activate();
    clientRef.current = client;

    return () => {
      subscriptionsRef.current.forEach((sub) => sub.unsubscribe());
      subscriptionsRef.current = [];
      if (client.active) {
        client.deactivate();
      }
      setIsConnected(false);
    };
  }, [groupId, token, fetchHistory, user?.id]);

  // 3. Actions
  const sendMessage = useCallback(
    (content: string, mediaUrl?: string) => {
      if (!clientRef.current || !clientRef.current.connected || !groupId || !content.trim()) return;

      clientRef.current.publish({
        destination: `/app/groups/${groupId}/chat`,
        body: JSON.stringify({
          content: content.trim(),
          messageType: mediaUrl ? 'IMAGE' : 'TEXT',
          mediaUrl,
        }),
      });
    },
    [groupId]
  );

  const sendTyping = useCallback(
    (isTyping: boolean) => {
      if (!clientRef.current || !clientRef.current.connected || !groupId) return;

      clientRef.current.publish({
        destination: `/app/groups/${groupId}/typing`,
        body: JSON.stringify({
          groupId,
          isTyping,
        }),
      });

      if (isTyping) {
        if (typingTimeoutRef.current) clearTimeout(typingTimeoutRef.current);
        typingTimeoutRef.current = setTimeout(() => {
          sendTyping(false);
        }, 3000);
      }
    },
    [groupId]
  );

  const markAsRead = useCallback(
    (messageId: number) => {
      if (!clientRef.current || !clientRef.current.connected || !groupId) return;

      clientRef.current.publish({
        destination: `/app/groups/${groupId}/read`,
        body: JSON.stringify(messageId),
      });
    },
    [groupId]
  );

  return {
    messages,
    typingUsers: Array.from(typingUsers.values()),
    isConnected,
    isLoading,
    error,
    sendMessage,
    sendTyping,
    markAsRead,
    reloadHistory: fetchHistory,
  };
}
