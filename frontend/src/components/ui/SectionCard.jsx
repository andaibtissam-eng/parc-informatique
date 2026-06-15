export const SectionCard = ({ title, subtitle, actions, children, className = "" }) => (
  <section className={`min-w-0 overflow-hidden rounded-2xl border border-line bg-white/95 p-5 shadow-soft ${className}`}>
    <div className="-mx-5 -mt-5 mb-5 h-1.5 bg-gradient-to-r from-brand-500 via-violet-500 to-success" />
    {(title || subtitle || actions) && (
      <div className="mb-5 flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div>
          {title && <h2 className="font-display text-lg font-bold text-ink">{title}</h2>}
          {subtitle && <p className="mt-1 max-w-3xl text-sm leading-6 text-muted">{subtitle}</p>}
        </div>
        {actions && <div className="flex shrink-0 flex-wrap gap-2">{actions}</div>}
      </div>
    )}
    {children}
  </section>
);
