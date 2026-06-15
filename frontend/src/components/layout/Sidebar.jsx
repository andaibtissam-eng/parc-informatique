import clsx from "clsx";
import { Cpu, X } from "lucide-react";
import { NavLink } from "react-router-dom";
import { navigation } from "../../data/navigation.js";
import { useAuthStore } from "../../features/auth/auth.store.js";
import { hasAnyPermission, hasPermission } from "../../lib/access.js";

export const Sidebar = ({ open, onClose }) => {
  const user = useAuthStore((state) => state.user);
  const allowedItems = navigation.filter((item) => hasAnyPermission(user, item.permissions));
  const employeeWorkspace = hasPermission(user, "assignments.read.own") && !hasPermission(user, "assignments.manage");
  const groups = allowedItems.reduce((acc, item) => {
    acc[item.group] = [...(acc[item.group] || []), item];
    return acc;
  }, {});

  return (
    <>
      <button
        aria-label="Fermer la navigation"
        className={clsx("fixed inset-0 z-30 bg-ink/30 lg:hidden", open ? "block" : "hidden")}
        onClick={onClose}
        type="button"
      />
      <aside
        className={clsx(
          "fixed inset-y-0 left-0 z-40 flex w-72 flex-col border-r border-brand-100 bg-[#f7f9ff] transition-transform lg:sticky lg:top-0 lg:h-screen lg:translate-x-0",
          open ? "translate-x-0" : "-translate-x-full"
        )}
      >
        <div className="flex items-center justify-between border-b border-brand-100 bg-white/70 px-5 py-5">
          <div className="flex items-center gap-3">
            <div className="grid h-10 w-10 place-items-center rounded-xl bg-brand-600 text-white shadow-pop">
              <Cpu size={20} />
            </div>
            <div>
              <p className="font-display text-base font-bold text-ink">ParcFlow</p>
              <p className="text-xs font-medium text-muted">IT asset workspace</p>
            </div>
          </div>
          <button className="rounded-lg p-2 text-muted hover:bg-slate-100 lg:hidden" onClick={onClose} type="button">
            <X size={18} />
          </button>
        </div>

        <nav className="flex-1 overflow-y-auto px-3 py-4">
          {Object.entries(groups).map(([group, items]) => (
            <div className="mb-5" key={group}>
              <p className="mb-2 px-3 text-[11px] font-bold uppercase tracking-wider text-slate-400">{group}</p>
              <div className="space-y-1">
                {items.map(({ to, label, employeeLabel, icon: Icon }) => (
                  <NavLink
                    className={({ isActive }) =>
                      clsx(
                        "flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-semibold transition",
                        isActive ? "bg-brand-50 text-brand-700" : "text-slate-600 hover:bg-slate-100 hover:text-ink"
                      )
                    }
                    end={to === "/"}
                    key={to}
                    onClick={onClose}
                    to={to}
                  >
                    <Icon size={18} />
                    <span>{employeeWorkspace && employeeLabel ? employeeLabel : label}</span>
                  </NavLink>
                ))}
              </div>
            </div>
          ))}
        </nav>

        <div className="border-t border-brand-100 p-4">
          <div className="rounded-xl bg-gradient-to-br from-brand-100 via-violet-50 to-mintwash p-4">
            <p className="text-sm font-bold text-ink">Workspace organise</p>
            <p className="mt-1 text-xs leading-5 text-muted">Acces filtre selon votre role, vos permissions et votre statut de compte.</p>
          </div>
        </div>
      </aside>
    </>
  );
};
