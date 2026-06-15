import { Bell, LogOut, Menu, Search } from "lucide-react";
import { useLocation } from "react-router-dom";
import { navigation } from "../../data/navigation.js";
import { useAuthStore } from "../../features/auth/auth.store.js";
import { useNotificationStore } from "../../features/notifications/notification.store.js";
import { useUiStore } from "../../features/ui/ui.store.js";
import { hasAnyPermission, hasPermission, roleLabel } from "../../lib/access.js";
import { initials } from "../../lib/format.js";

export const Topbar = () => {
  const user = useAuthStore((state) => state.user);
  const logout = useAuthStore((state) => state.logout);
  const unreadCount = useNotificationStore((state) => state.unreadCount);
  const toggleSidebar = useUiStore((state) => state.toggleSidebar);
  const { pathname } = useLocation();
  const visibleNavigation = navigation.filter((item) => hasAnyPermission(user, item.permissions));
  const current = visibleNavigation.find((item) => item.to === pathname) || visibleNavigation.find((item) => item.to === "/") || navigation.find((item) => item.to === "/");
  const employeeWorkspace = hasPermission(user, "assignments.read.own") && !hasPermission(user, "assignments.manage");
  const currentLabel = employeeWorkspace && current?.employeeLabel ? current.employeeLabel : current?.label;
  const displayName = user?.fullName || user?.email || "Equipe IT";
  const currentRole = Array.isArray(user?.roles) && user.roles.length > 0 ? roleLabel(user.roles[0]) : "Utilisateur";

  return (
    <header className="sticky top-0 z-20 border-b border-brand-100 bg-skywash/90 backdrop-blur">
      <div className="mx-auto flex h-16 max-w-[1440px] items-center justify-between gap-4 px-4 sm:px-6 lg:px-8">
        <div className="flex min-w-0 items-center gap-3">
          <button className="rounded-xl border border-line p-2 text-slate-600 lg:hidden" onClick={toggleSidebar} type="button">
            <Menu size={20} />
          </button>
          <div className="min-w-0">
            <p className="truncate text-sm font-semibold text-ink">{currentLabel || "Dashboard"}</p>
            <p className="hidden text-xs text-muted sm:block">Gestion claire du parc informatique</p>
          </div>
        </div>

        <div className="hidden min-w-[280px] max-w-md flex-1 items-center gap-2 rounded-xl border border-brand-100 bg-white/80 px-3 py-2 lg:flex">
          <Search size={17} className="text-slate-400" />
          <input className="w-full bg-transparent text-sm outline-none placeholder:text-slate-400" placeholder="Rechercher un materiel, utilisateur, ticket..." />
        </div>

        <div className="flex items-center gap-2">
          <button className="relative rounded-xl border border-line bg-white p-2.5 text-slate-600 hover:bg-slate-50" type="button">
            <Bell size={18} />
            {unreadCount > 0 && <span className="absolute -right-1 -top-1 grid h-5 min-w-5 place-items-center rounded-full bg-danger px-1 text-[10px] font-bold text-white">{unreadCount}</span>}
          </button>
          <div className="hidden items-center gap-3 rounded-xl border border-line bg-white px-3 py-2 sm:flex">
            <div className="grid h-8 w-8 place-items-center rounded-lg bg-violet-100 text-xs font-bold text-violet-600">{initials(displayName)}</div>
            <div className="max-w-36">
              <p className="truncate text-sm font-semibold text-ink">{displayName}</p>
              <p className="text-xs text-muted">{currentRole}</p>
            </div>
          </div>
          <button className="rounded-xl border border-line bg-white p-2.5 text-slate-600 hover:bg-slate-50" onClick={logout} type="button">
            <LogOut size={18} />
          </button>
        </div>
      </div>
    </header>
  );
};
