import { useEffect } from "react";
import { Outlet } from "react-router-dom";
import { useAuthStore } from "../features/auth/auth.store.js";
import { Loader } from "../components/ui/Loader.jsx";

export const AppShell = () => {
  const { bootstrapped, bootstrapSession } = useAuthStore();

  useEffect(() => {
    bootstrapSession();
  }, [bootstrapSession]);

  if (!bootstrapped) {
    return <Loader fullScreen label="Initialisation de l'application" />;
  }

  return <Outlet />;
};
