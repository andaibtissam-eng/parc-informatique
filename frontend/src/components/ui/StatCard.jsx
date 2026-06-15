import clsx from "clsx";

const toneClasses = {
  blue: "bg-brand-50 text-brand-700",
  violet: "bg-violet-50 text-violet-600",
  green: "bg-emerald-50 text-success",
  amber: "bg-amber-50 text-warning",
  red: "bg-red-50 text-danger",
  slate: "bg-slate-100 text-slate-700"
};

export const StatCard = ({ label, value, helper, icon: Icon, tone = "blue" }) => (
  <article className="rounded-2xl border border-line bg-white p-5 shadow-soft">
    <div className="flex items-start justify-between gap-4">
      <div>
        <p className="text-sm font-semibold text-muted">{label}</p>
        <p className="mt-3 font-display text-3xl font-bold tracking-normal text-ink">{value}</p>
      </div>
      {Icon && (
        <div className={clsx("grid h-10 w-10 place-items-center rounded-xl", toneClasses[tone] || toneClasses.blue)}>
          <Icon size={20} />
        </div>
      )}
    </div>
    {helper && <p className="mt-3 text-sm leading-5 text-muted">{helper}</p>}
  </article>
);
