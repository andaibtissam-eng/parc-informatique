import { createBrowserRouter, Navigate } from "react-router-dom";
import { AppShell } from "./App.jsx";
import { AppLayout } from "../components/layout/AppLayout.jsx";
import { Loader } from "../components/ui/Loader.jsx";
import { useAuthStore } from "../features/auth/auth.store.js";
import { LoginPage } from "../pages/auth/LoginPage.jsx";
import { ForgotPasswordPage, ResetPasswordPage, VerifyEmailPage } from "../pages/auth/AuthSupportPages.jsx";
import { DashboardPage } from "../pages/dashboard/DashboardPage.jsx";
import { EquipmentsPage } from "../pages/equipments/EquipmentsPage.jsx";
import { UsersPage } from "../pages/users/UsersPage.jsx";
import { AssignmentsPage } from "../pages/assignments/AssignmentsPage.jsx";
import { MaintenancePage } from "../pages/maintenance/MaintenancePage.jsx";
import { NotificationsPage } from "../pages/notifications/NotificationsPage.jsx";
import { ReportsPage } from "../pages/reports/ReportsPage.jsx";
import { hasAnyPermission } from "../lib/access.js";
import {
  AnalyticsPage,
  CalendarPage,
  CategoriesPage,
  HistoryPage,
  ProfilePage,
  ReturnsPage,
  SettingsPage,
  TicketsPage
} from "../pages/workspace/WorkspacePages.jsx";

const ProtectedRoute = ({ children }) => {
  const { bootstrapped, accessToken } = useAuthStore();

  if (!bootstrapped) {
    return <Loader fullScreen label="Chargement" />;
  }

  if (!accessToken) {
    return <Navigate to="/login" replace />;
  }

  return children;
};

const PublicRoute = ({ children }) => {
  const { accessToken } = useAuthStore();

  if (accessToken) {
    return <Navigate to="/" replace />;
  }

  return children;
};

const PermissionRoute = ({ children, permissions = [] }) => {
  const { bootstrapped, accessToken, user } = useAuthStore();

  if (!bootstrapped) {
    return <Loader fullScreen label="Chargement" />;
  }

  if (!accessToken) {
    return <Navigate to="/login" replace />;
  }

  if (!hasAnyPermission(user, permissions)) {
    return <Navigate to="/" replace />;
  }

  return children;
};

export const router = createBrowserRouter([
  {
    path: "/",
    element: <AppShell />,
    children: [
      {
        path: "/login",
        element: (
          <PublicRoute>
            <LoginPage />
          </PublicRoute>
        )
      },
      {
        path: "/register",
        element: (
          <PublicRoute>
            <LoginPage initialMode="register" />
          </PublicRoute>
        )
      },
      {
        path: "/forgot-password",
        element: (
          <PublicRoute>
            <ForgotPasswordPage />
          </PublicRoute>
        )
      },
      {
        path: "/reset-password",
        element: (
          <PublicRoute>
            <ResetPasswordPage />
          </PublicRoute>
        )
      },
      {
        path: "/verify-email",
        element: (
          <PublicRoute>
            <VerifyEmailPage />
          </PublicRoute>
        )
      },
      {
        path: "/",
        element: (
          <ProtectedRoute>
            <AppLayout />
          </ProtectedRoute>
        ),
        children: [
          { index: true, element: <PermissionRoute permissions={["dashboard.read"]}><DashboardPage /></PermissionRoute> },
          { path: "dashboard", element: <PermissionRoute permissions={["dashboard.read"]}><DashboardPage /></PermissionRoute> },
          { path: "analytics", element: <PermissionRoute permissions={["analytics.read"]}><AnalyticsPage /></PermissionRoute> },
          { path: "equipments", element: <PermissionRoute permissions={["equipments.read", "equipments.manage"]}><EquipmentsPage /></PermissionRoute> },
          { path: "categories", element: <PermissionRoute permissions={["equipments.manage"]}><CategoriesPage /></PermissionRoute> },
          { path: "users", element: <PermissionRoute permissions={["users.manage"]}><UsersPage /></PermissionRoute> },
          { path: "assignments", element: <PermissionRoute permissions={["assignments.manage", "assignments.read.own"]}><AssignmentsPage /></PermissionRoute> },
          { path: "returns", element: <PermissionRoute permissions={["assignments.manage", "maintenances.manage"]}><ReturnsPage /></PermissionRoute> },
          { path: "maintenance", element: <PermissionRoute permissions={["maintenances.read", "maintenances.manage", "maintenances.request"]}><MaintenancePage /></PermissionRoute> },
          { path: "maintenances", element: <Navigate replace to="/maintenance" /> },
          { path: "board", element: <PermissionRoute permissions={["maintenances.read", "maintenances.manage", "maintenances.request"]}><MaintenancePage /></PermissionRoute> },
          { path: "tickets", element: <PermissionRoute permissions={["maintenances.read", "maintenances.manage"]}><TicketsPage /></PermissionRoute> },
          { path: "calendar", element: <PermissionRoute permissions={["maintenances.read", "maintenances.manage"]}><CalendarPage /></PermissionRoute> },
          { path: "notifications", element: <PermissionRoute permissions={["notifications.read.own"]}><NotificationsPage /></PermissionRoute> },
          { path: "reports", element: <PermissionRoute permissions={["reports.read"]}><ReportsPage /></PermissionRoute> },
          { path: "history", element: <PermissionRoute permissions={["audit.read"]}><HistoryPage /></PermissionRoute> },
          { path: "profile", element: <PermissionRoute permissions={["profile.manage.own"]}><ProfilePage /></PermissionRoute> },
          { path: "settings", element: <PermissionRoute permissions={["system.manage"]}><SettingsPage /></PermissionRoute> }
        ]
      }
    ]
  }
]);
