import clsx from "clsx";

const palette = {
  AVAILABLE: "bg-emerald-50 text-emerald-700 ring-emerald-200",
  ACTIVE: "bg-emerald-50 text-emerald-700 ring-emerald-200",
  PENDING: "bg-amber-50 text-amber-700 ring-amber-200",
  PENDING_APPROVAL: "bg-amber-50 text-amber-700 ring-amber-200",
  PENDING_VERIFICATION: "bg-sky-50 text-sky-700 ring-sky-200",
  SUSPENDED: "bg-red-50 text-red-700 ring-red-200",
  REJECTED: "bg-rose-50 text-rose-700 ring-rose-200",
  INACTIVE: "bg-slate-100 text-slate-700 ring-slate-200",
  LOCKED: "bg-red-50 text-red-700 ring-red-200",
  ASSIGNED: "bg-blue-50 text-blue-700 ring-blue-200",
  IN_STOCK: "bg-emerald-50 text-emerald-700 ring-emerald-200",
  IN_PROGRESS: "bg-blue-50 text-blue-700 ring-blue-200",
  OPEN: "bg-amber-50 text-amber-700 ring-amber-200",
  ON_HOLD: "bg-violet-50 text-violet-700 ring-violet-200",
  RESERVED: "bg-violet-50 text-violet-700 ring-violet-200",
  MAINTENANCE: "bg-amber-50 text-amber-700 ring-amber-200",
  CRITICAL: "bg-red-50 text-red-700 ring-red-200",
  HIGH: "bg-orange-50 text-orange-700 ring-orange-200",
  MEDIUM: "bg-sky-50 text-sky-700 ring-sky-200",
  LOW: "bg-slate-100 text-slate-700 ring-slate-200",
  APPROVED: "bg-emerald-50 text-emerald-700 ring-emerald-200",
  RESOLVED: "bg-emerald-50 text-emerald-700 ring-emerald-200",
  FULFILLED: "bg-emerald-50 text-emerald-700 ring-emerald-200",
  CLOSED: "bg-slate-100 text-slate-700 ring-slate-200",
  CANCELLED: "bg-slate-100 text-slate-700 ring-slate-200",
  RETIRED: "bg-slate-100 text-slate-700 ring-slate-200",
  LOST: "bg-red-50 text-red-700 ring-red-200"
};

export const StatusBadge = ({ value }) => (
  <span className={clsx("inline-flex rounded-full px-2.5 py-1 text-xs font-bold ring-1", palette[value] || "bg-slate-100 text-slate-700 ring-slate-200")}>
    {value || "-"}
  </span>
);
