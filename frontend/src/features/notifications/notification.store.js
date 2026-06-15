import { create } from "zustand";

export const useNotificationStore = create((set) => ({
  items: [],
  unreadCount: 0,
  setItems: (items) =>
    set({
      items,
      unreadCount: items.filter((item) => !(item.isRead ?? item.read)).length
    }),
  prependItem: (item) =>
    set((state) => ({
      items: [item, ...state.items],
      unreadCount: state.unreadCount + ((item.isRead ?? item.read) ? 0 : 1)
    })),
  markAllReadLocal: () =>
    set((state) => ({
      items: state.items.map((item) => ({ ...item, isRead: true, read: true })),
      unreadCount: 0
    }))
}));
