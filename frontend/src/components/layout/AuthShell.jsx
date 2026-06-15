import { Boxes, CheckCircle2, Cpu, HardDrive, Layers3, ShieldCheck, Wrench } from "lucide-react";

const previewRows = [
  { name: "MacBook Pro M3", status: "Affecte", color: "bg-brand-50 text-brand-700" },
  { name: "Dell Latitude 7450", status: "Disponible", color: "bg-emerald-50 text-emerald-700" },
  { name: "ThinkPad X1", status: "Maintenance", color: "bg-amber-50 text-amber-700" }
];

export const AuthShell = ({
  badge = "Controlled enterprise access",
  title,
  description,
  children
}) => (
  <main className="auth-canvas relative min-h-screen overflow-y-auto px-4 py-4 sm:px-6 lg:px-8">
    <div className="relative mx-auto flex min-h-[calc(100vh-2rem)] max-w-7xl flex-col">
      <header className="flex items-center justify-between rounded-2xl border border-white/70 bg-white/75 px-4 py-3 shadow-soft backdrop-blur">
        <div className="flex items-center gap-3">
          <div className="grid h-10 w-10 place-items-center rounded-xl bg-gradient-to-br from-brand-600 to-violet-600 text-white shadow-pop">
            <Boxes size={20} />
          </div>
          <div>
            <p className="font-display text-base font-bold text-ink">ParcFlow</p>
            <p className="text-xs font-semibold text-muted">IT asset workspace</p>
          </div>
        </div>
        <div className="hidden items-center gap-2 rounded-full bg-brand-50 px-3 py-2 text-xs font-bold text-brand-700 sm:flex">
          <CheckCircle2 size={15} />
          Workflow securise
        </div>
      </header>

      <section className="grid flex-1 items-center gap-5 py-4 md:grid-cols-[minmax(0,1fr)_minmax(360px,430px)] xl:grid-cols-[minmax(0,0.95fr)_430px] xl:gap-8">
        <div className="min-w-0">
          <div className="max-w-xl">
            <div className="inline-flex items-center gap-2 rounded-full border border-brand-100 bg-white/80 px-3 py-2 text-xs font-bold uppercase tracking-wider text-brand-700 shadow-sm">
              <Layers3 size={15} />
              {badge}
            </div>
            <h1 className="mt-4 max-w-xl font-display text-2xl font-bold leading-[1.08] tracking-normal text-ink sm:text-3xl xl:text-4xl">
              {title}
            </h1>
            <p className="mt-3 max-w-lg text-sm leading-7 text-slate-600">{description}</p>
          </div>

          <div className="mt-5 hidden gap-3 md:grid md:grid-cols-3">
            <div className="rounded-2xl border border-white/80 bg-white/75 p-3 shadow-soft backdrop-blur">
              <Cpu className="text-brand-600" size={20} />
              <p className="mt-2 text-xl font-bold text-ink">RBAC</p>
              <p className="text-xs text-muted">Roles et permissions</p>
            </div>
            <div className="rounded-2xl border border-white/80 bg-white/75 p-3 shadow-soft backdrop-blur">
              <Wrench className="text-warning" size={20} />
              <p className="mt-2 text-xl font-bold text-ink">Admin</p>
              <p className="text-xs text-muted">Validation centralisee</p>
            </div>
            <div className="rounded-2xl border border-white/80 bg-white/75 p-3 shadow-soft backdrop-blur">
              <ShieldCheck className="text-success" size={20} />
              <p className="mt-2 text-xl font-bold text-ink">Audit</p>
              <p className="text-xs text-muted">Tracabilite securisee</p>
            </div>
          </div>

          <div className="mt-4 hidden overflow-hidden rounded-3xl border border-white/80 soft-panel xl:block">
            <div className="flex items-center justify-between border-b border-line/80 px-5 py-3">
              <div>
                <p className="text-sm font-bold text-ink">Inventaire recent</p>
                <p className="text-xs text-muted">Vue rapide des materiels critiques</p>
              </div>
              <span className="rounded-full bg-violet-50 px-3 py-1 text-xs font-bold text-violet-600">Live</span>
            </div>
            <div className="grid gap-2 p-3">
              {previewRows.map((row) => (
                <div className="flex items-center justify-between rounded-2xl bg-white/80 px-4 py-3" key={row.name}>
                  <div className="flex items-center gap-3">
                    <div className="grid h-9 w-9 place-items-center rounded-xl bg-gradient-to-br from-brand-100 to-violet-100 text-brand-700">
                      <HardDrive size={17} />
                    </div>
                    <div>
                      <p className="text-sm font-bold text-ink">{row.name}</p>
                      <p className="text-xs text-muted">Inventaire / DSI</p>
                    </div>
                  </div>
                  <span className={`rounded-full px-3 py-1 text-xs font-bold ${row.color}`}>{row.status}</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="order-first flex justify-center md:order-none md:justify-end">{children}</div>
      </section>
    </div>
  </main>
);
