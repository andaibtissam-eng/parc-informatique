import { create } from "zustand";
import { persist } from "zustand/middleware";
import { http } from "../../api/http.js";
import { unwrap } from "../../lib/format.js";

export const useAuthStore = create(
  persist(
    (set, get) => ({
      accessToken: null,
      user: null,
      bootstrapped: false,
      setSession: ({ accessToken, user }) =>
        set({
          accessToken,
          user,
          bootstrapped: true
        }),
      clearSession: () =>
        set({
          accessToken: null,
          user: null,
          bootstrapped: true
        }),
      bootstrapSession: async () => {
        if (get().accessToken) {
          set({ bootstrapped: true });
          return;
        }

        set({
          accessToken: null,
          user: null,
          bootstrapped: true
        });
      },
      login: async (credentials) => {
        const response = await http.post("/auth/login", credentials);
        const data = unwrap(response);
        set({
          accessToken: data.accessToken,
          user: data.user,
          bootstrapped: true
        });
      },
      register: async (payload) => {
        const response = await http.post("/auth/register", payload);
        return unwrap(response);
      },
      forgotPassword: async (email) => {
        const response = await http.post("/auth/forgot-password", { email });
        return unwrap(response);
      },
      resetPassword: async ({ token, password }) => {
        const response = await http.post("/auth/reset-password", { token, password });
        return unwrap(response);
      },
      verifyEmail: async (token) => {
        const response = await http.post("/auth/verify-email", { token });
        return unwrap(response);
      },
      logout: async () => {
        try {
          await http.post("/auth/logout");
        } finally {
          set({
            accessToken: null,
            user: null,
            bootstrapped: true
          });
        }
      }
    }),
    {
      name: "parcflow-auth",
      partialize: (state) => ({
        accessToken: state.accessToken,
        user: state.user
      })
    }
  )
);
