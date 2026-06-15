export const PageHeader = ({ eyebrow, title, description, actions }) => (
  <div className="mb-6 flex flex-col gap-4 rounded-2xl border border-brand-100 bg-[linear-gradient(135deg,#ffffff_0%,#eef4ff_48%,#f2ecff_100%)] p-5 shadow-soft lg:flex-row lg:items-end lg:justify-between">
    <div className="max-w-3xl">
      {eyebrow && <p className="text-xs font-bold uppercase tracking-wider text-brand-600">{eyebrow}</p>}
      <h1 className="mt-2 font-display text-2xl font-bold tracking-normal text-ink sm:text-3xl">{title}</h1>
      {description && <p className="mt-2 text-sm leading-6 text-muted">{description}</p>}
    </div>
    {actions && <div className="flex flex-wrap gap-2">{actions}</div>}
  </div>
);
