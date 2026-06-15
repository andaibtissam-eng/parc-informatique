import axios from "axios";
import { useAuthStore } from "../features/auth/auth.store.js";

const baseURL = import.meta.env.VITE_API_BASE_URL || "/api";

export const http = axios.create({
  baseURL,
  withCredentials: true
});

http.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().clearSession();
    }

    return Promise.reject(error);
  }
);
