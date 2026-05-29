import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { config } from '../../config/config';

export interface QueueCounts {
  breakfast: number;
  lunch:     number;
  dinner:    number;
  alacarte:  number;
}

export type QueueKey = keyof QueueCounts;

interface KitchenQueueContextValue {
  counts:         QueueCounts;
  refreshCounts:  () => void;
  decrementCount: (queue: QueueKey) => void;
}

const defaultCounts: QueueCounts = { breakfast: 0, lunch: 0, dinner: 0, alacarte: 0 };

const KitchenQueueContext = createContext<KitchenQueueContextValue>({
  counts:         defaultCounts,
  refreshCounts:  () => {},
  decrementCount: () => {},
});

export const useKitchenQueueCounts = () => useContext(KitchenQueueContext);

const fetchPendingCount = async (url: string, token: string): Promise<number> => {
  try {
    const params = new URLSearchParams({ page: '0', size: '200' });
    const res = await fetch(`${url}?${params}`, {
      headers: { 'Authorization': `Bearer ${token}` },
    });
    if (!res.ok) return 0;
    const json = await res.json();
    const content: Array<{ status: string }> = json?.data?.content ?? [];
    return content.filter(item => item.status === 'PENDING').length;
  } catch {
    return 0;
  }
};

export const KitchenQueueProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [counts, setCounts] = useState<QueueCounts>(defaultCounts);

  const refreshCounts = useCallback(async () => {
    const token = localStorage.getItem('kitchen_token');
    if (!token) return;

    const [breakfast, lunch, dinner, alacarte] = await Promise.all([
      fetchPendingCount(config.kitchenBreakfastQueueUrl, token),
      fetchPendingCount(config.kitchenLunchQueueUrl,     token),
      fetchPendingCount(config.kitchenDinnerQueueUrl,    token),
      fetchPendingCount(config.kitchenAlaCarteQueueUrl,  token),
    ]);

    setCounts({ breakfast, lunch, dinner, alacarte });
  }, []);

  // Instantly decrement a specific queue's badge — no API call needed
  const decrementCount = useCallback((queue: QueueKey) => {
    setCounts(prev => ({
      ...prev,
      [queue]: Math.max(0, prev[queue] - 1),
    }));
  }, []);

  useEffect(() => { refreshCounts(); }, [refreshCounts]);

  useEffect(() => {
    const id = setInterval(refreshCounts, 5 * 60 * 1000);
    return () => clearInterval(id);
  }, [refreshCounts]);

  return (
    <KitchenQueueContext.Provider value={{ counts, refreshCounts, decrementCount }}>
      {children}
    </KitchenQueueContext.Provider>
  );
};