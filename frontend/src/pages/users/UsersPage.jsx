import { useDeferredValue, useEffect, useState } from "react";
import { Activity, CheckCircle2, Clock3, ShieldCheck, UserCheck, UserCog, UserMinus, UserX, XCircle } from "lucide-react";
import { http } from "../../api/http.js";
import { Button } from "../../components/ui/Button.jsx";
import { DataTable } from "../../components/ui/DataTable.jsx";
import { Loader } from "../../components/ui/Loader.jsx";
import { PageHeader } from "../../components/ui/PageHeader.jsx";
import { SearchInput } from "../../components/ui/SearchInput.jsx";
import { SectionCard } from "../../components/ui/SectionCard.jsx";
import { SelectFilter } from "../../components/ui/SelectFilter.jsx";
import { StatCard } from "../../components/ui/StatCard.jsx";
import { StatusBadge } from "../../components/ui/StatusBadge.jsx";
import { roleLabel } from "../../lib/access.js";
import { formatDateTime, initials, pageContent, unwrap } from "../../lib/format.js";

const statusOptions = [
  { value: "ALL", label: "Tous les statuts" },
  { value: "PENDING_APPROVAL", label: "En attente" },
  { value: "ACTIVE", label: "Actifs" },
  { value: "SUSPENDED", label: "Suspendus" },
  { value: "INACTIVE", label: "Inactifs" },
  { value: "REJECTED", label: "Rejetes" },
  { value: "LOCKED", label: "Verrouilles" }
];

const roleOptions = [
  { value: "ALL", label: "Tous les roles" },
  { value: "RESPONSABLE_INFORMATIQUE", label: "Responsable informatique" },
  { value: "TECHNICIEN", label: "Technicien" },
  { value: "EMPLOYE", label: "Employe / beneficiaire" }
];

const actionPalette = {
  approve: { label: "Approuver", variant: "primary" },
  reject: { label: "Rejeter", variant: "danger" },
  suspend: { label: "Suspendre", variant: "secondary" },
  activate: { label: "Activer", variant: "secondary" },
  deactivate: { label: "Desactiver", variant: "subtle" }
};

export const UsersPage = () => {
  const [usersPayload, setUsersPayload] = useState({ content: [], page: 0, totalPages: 0, totalElements: 0 });
  const [overview, setOverview] = useState(null);
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [roleFilter, setRoleFilter] = useState("ALL");
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [busyAction, setBusyAction] = useState("");
  const [roleDrafts, setRoleDrafts] = useState({});
  const deferredSearch = useDeferredValue(search);

  const loadUsers = async () => {
    setLoading(true);
    try {
      const [usersResponse, overviewResponse] = await Promise.all([
        http.get("/users", {
          params: {
            search: deferredSearch,
            status: statusFilter === "ALL" ? undefined : statusFilter,
            role: roleFilter === "ALL" ? undefined : roleFilter,
            page,
            size: 10
          }
        }),
        http.get("/users/admin-overview")
      ]);

      const nextUsers = unwrap(usersResponse);
      const nextOverview = unwrap(overviewResponse);
      setUsersPayload(nextUsers);
      setOverview(nextOverview);
      setRoleDrafts(
        Object.fromEntries(
          pageContent(nextUsers).map((user) => [user.id, Array.isArray(user.roles) && user.roles.length ? user.roles[0] : "EMPLOYE"])
        )
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUsers();
  }, [deferredSearch, statusFilter, roleFilter, page]);

  const runAction = async (userId, endpoint, body = null) => {
    setBusyAction(`${userId}:${endpoint}`);
    try {
      if (body) {
        await http.patch(`/users/${userId}/${endpoint}`, body);
      } else {
        await http.patch(`/users/${userId}/${endpoint}`);
      }
      await loadUsers();
    } finally {
      setBusyAction("");
    }
  };

  const rows = pageContent(usersPayload);

  const renderActionButton = (user, endpoint) => {
    const config = actionPalette[endpoint];
    return (
      <Button
        className="h-9 rounded-lg px-3 text-xs"
        disabled={busyAction === `${user.id}:${endpoint}`}
        onClick={() => runAction(user.id, endpoint)}
        type="button"
        variant={config.variant}
      >
        {busyAction === `${user.id}:${endpoint}` ? "..." : config.label}
      </Button>
    );
  };

  return (
    <div className="min-w-0">
      <PageHeader
        description="Validation des inscriptions, changement de role, suspension, activation et supervision des comptes dans une experience d'administration claire."
        eyebrow="Administration"
        title="Gestion des utilisateurs"
      />

      {overview ? (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-5">
          <StatCard helper="Comptes references dans la plateforme" icon={UserCog} label="Total comptes" value={overview.totalUsers} />
          <StatCard helper="A valider par l'administrateur" icon={Clock3} label="En attente" tone="amber" value={overview.pendingUsers} />
          <StatCard helper="Comptes actuellement autorises" icon={CheckCircle2} label="Actifs" tone="green" value={overview.activeUsers} />
          <StatCard helper="Suspensions, blocages et inactivites" icon={UserMinus} label="Sous controle" tone="red" value={overview.suspendedUsers + overview.inactiveUsers} />
          <StatCard helper="Responsables, techniciens et employes verifies" icon={ShieldCheck} label="Emails verifies" tone="violet" value={overview.verifiedUsers} />
        </div>
      ) : (
        <Loader label="Chargement du cockpit admin" />
      )}

      <div className="mt-5 space-y-5">
        <SectionCard
          actions={
            <>
              <SearchInput onChange={(value) => { setPage(0); setSearch(value); }} value={search} />
              <SelectFilter label="Statut" onChange={(value) => { setPage(0); setStatusFilter(value); }} options={statusOptions} value={statusFilter} />
              <SelectFilter label="Role" onChange={(value) => { setPage(0); setRoleFilter(value); }} options={roleOptions} value={roleFilter} />
            </>
          }
          subtitle="Recherche multi-critere, role filters et moderation des demandes d'acces."
          title="Comptes et approbations"
        >
          {loading ? (
            <Loader label="Chargement des utilisateurs" />
          ) : (
            <>
              <DataTable
                columns={[
                  {
                    key: "fullName",
                    label: "Utilisateur",
                    render: (row) => (
                      <div className="flex items-center gap-3">
                        <div className="grid h-11 w-11 place-items-center rounded-xl bg-violet-100 text-xs font-bold text-violet-600">{initials(row.fullName)}</div>
                        <div className="min-w-0">
                          <p className="font-semibold text-ink">{row.fullName}</p>
                          <p className="truncate text-xs text-muted">{row.email}</p>
                          {row.systemAccount && <p className="text-[11px] font-bold uppercase tracking-wide text-brand-600">Compte systeme protege</p>}
                        </div>
                      </div>
                    )
                  },
                  {
                    key: "roles",
                    label: "Role courant",
                    render: (row) => (
                      <div className="space-y-2">
                        <p className="text-xs font-semibold text-slate-600">{Array.from(row.roles || []).map(roleLabel).join(", ") || "-"}</p>
                        {!row.systemAccount && (
                          <div className="flex items-center gap-2">
                            <select
                              className="h-9 rounded-lg border border-line bg-white px-3 text-xs font-semibold text-ink outline-none"
                              onChange={(event) => setRoleDrafts((current) => ({ ...current, [row.id]: event.target.value }))}
                              value={roleDrafts[row.id] || "EMPLOYE"}
                            >
                              {roleOptions.filter((option) => option.value !== "ALL").map((option) => (
                                <option key={option.value} value={option.value}>
                                  {option.label}
                                </option>
                              ))}
                            </select>
                            <Button
                              className="h-9 rounded-lg px-3 text-xs"
                              disabled={busyAction === `${row.id}:roles` || roleDrafts[row.id] === (Array.isArray(row.roles) && row.roles.length ? row.roles[0] : "EMPLOYE")}
                              onClick={() => runAction(row.id, "roles", { roles: [roleDrafts[row.id]] })}
                              type="button"
                              variant="secondary"
                            >
                              Appliquer
                            </Button>
                          </div>
                        )}
                      </div>
                    )
                  },
                  { key: "department", label: "Departement" },
                  {
                    key: "status",
                    label: "Statut",
                    render: (row) => (
                      <div className="space-y-1">
                        <StatusBadge value={row.status} />
                        <p className="text-[11px] text-slate-500">{row.emailVerified ? "Email verifie" : "Email en attente"}</p>
                      </div>
                    )
                  },
                  {
                    key: "lastLoginAt",
                    label: "Activite",
                    render: (row) => (
                      <div>
                        <p className="text-xs font-semibold text-ink">{formatDateTime(row.lastLoginAt)}</p>
                        <p className="text-[11px] text-slate-500">{row.failedLoginAttempts || 0} tentative(s) ratee(s)</p>
                      </div>
                    )
                  },
                  {
                    key: "actions",
                    label: "Actions",
                    render: (row) => (
                      <div className="flex flex-wrap gap-2">
                        {row.systemAccount ? (
                          <span className="rounded-full bg-brand-50 px-2.5 py-1 text-[11px] font-bold text-brand-700">Protege</span>
                        ) : (
                          <>
                            {(row.status === "PENDING_APPROVAL" || row.status === "PENDING_VERIFICATION") && (
                              <>
                                {renderActionButton(row, "approve")}
                                {renderActionButton(row, "reject")}
                              </>
                            )}
                            {row.status === "ACTIVE" && (
                              <>
                                {renderActionButton(row, "suspend")}
                                {renderActionButton(row, "deactivate")}
                              </>
                            )}
                            {(row.status === "SUSPENDED" || row.status === "INACTIVE" || row.status === "REJECTED" || row.status === "LOCKED") &&
                              renderActionButton(row, "activate")}
                          </>
                        )}
                      </div>
                    )
                  }
                ]}
                emptyLabel="Aucun utilisateur trouve"
                minWidth={1280}
                rows={rows}
              />

              <div className="mt-4 flex items-center justify-between gap-3">
                <p className="text-sm text-muted">
                  {usersPayload.totalElements || 0} compte(s) trouves, page {(usersPayload.page || 0) + 1} / {Math.max(usersPayload.totalPages || 1, 1)}
                </p>
                <div className="flex gap-2">
                  <Button className="h-9 rounded-lg px-3 text-xs" disabled={(usersPayload.page || 0) <= 0} onClick={() => setPage((current) => Math.max(current - 1, 0))} type="button" variant="secondary">
                    Precedent
                  </Button>
                  <Button
                    className="h-9 rounded-lg px-3 text-xs"
                    disabled={(usersPayload.page || 0) >= Math.max((usersPayload.totalPages || 1) - 1, 0)}
                    onClick={() => setPage((current) => current + 1)}
                    type="button"
                    variant="secondary"
                  >
                    Suivant
                  </Button>
                </div>
              </div>
            </>
          )}
        </SectionCard>

        <div className="grid gap-5 xl:grid-cols-2">
          <SectionCard subtitle="Les derniers comptes soumis au circuit de validation." title="Inscriptions recentes">
            {overview ? (
              <DataTable
                columns={[
                  {
                    key: "fullName",
                    label: "Utilisateur",
                    render: (row) => (
                      <div>
                        <p className="font-semibold text-ink">{row.fullName}</p>
                        <p className="text-xs text-muted">{row.email}</p>
                      </div>
                    )
                  },
                  { key: "roles", label: "Role", render: (row) => Array.from(row.roles || []).map(roleLabel).join(", ") },
                  { key: "createdAt", label: "Inscrit le", render: (row) => formatDateTime(row.createdAt) }
                ]}
                emptyLabel="Aucune inscription recente"
                minWidth={680}
                rows={overview.recentRegistrations}
              />
            ) : (
              <Loader label="Chargement des inscriptions" />
            )}
          </SectionCard>

          <SectionCard subtitle="Evenements recents relies aux comptes, connexions et changements d'etat." title="Activite comptes">
            {overview ? (
              <DataTable
                columns={[
                  { key: "actor", label: "Acteur" },
                  { key: "description", label: "Evenement" },
                  { key: "occurredAt", label: "Date", render: (row) => formatDateTime(row.occurredAt) }
                ]}
                emptyLabel="Aucune activite recente"
                minWidth={680}
                rows={overview.recentAccountActivity}
              />
            ) : (
              <Loader label="Chargement des logs" />
            )}
          </SectionCard>
        </div>
      </div>

      {overview && (
        <div className="mt-5 grid gap-4 md:grid-cols-3">
          <SectionCard className="h-full" subtitle="Vue synthese des profils operationnels." title="Repartition des roles">
            <div className="space-y-3">
              <div className="flex items-center justify-between rounded-xl bg-slate-50 px-4 py-3">
                <span className="font-semibold text-ink">Responsables IT</span>
                <span className="text-sm font-bold text-ink">{overview.responsableCount}</span>
              </div>
              <div className="flex items-center justify-between rounded-xl bg-slate-50 px-4 py-3">
                <span className="font-semibold text-ink">Techniciens</span>
                <span className="text-sm font-bold text-ink">{overview.technicianCount}</span>
              </div>
              <div className="flex items-center justify-between rounded-xl bg-slate-50 px-4 py-3">
                <span className="font-semibold text-ink">Employes</span>
                <span className="text-sm font-bold text-ink">{overview.employeeCount}</span>
              </div>
            </div>
          </SectionCard>

          <SectionCard className="h-full" subtitle="Lecture rapide de la posture de securite des comptes." title="Hygiene d'acces">
            <div className="space-y-3 text-sm">
              <div className="flex items-center gap-3 rounded-xl bg-emerald-50 px-4 py-3 text-emerald-700">
                <UserCheck size={18} />
                <span>{overview.activeUsers} compte(s) actifs et autorises.</span>
              </div>
              <div className="flex items-center gap-3 rounded-xl bg-amber-50 px-4 py-3 text-amber-700">
                <Clock3 size={18} />
                <span>{overview.pendingUsers} demande(s) en attente d'approbation.</span>
              </div>
              <div className="flex items-center gap-3 rounded-xl bg-red-50 px-4 py-3 text-red-700">
                <UserX size={18} />
                <span>{overview.suspendedUsers + overview.inactiveUsers} compte(s) limites ou bloques.</span>
              </div>
            </div>
          </SectionCard>

          <SectionCard className="h-full" subtitle="Rappels d'administration pour garder une gouvernance saine." title="Bonnes pratiques">
            <div className="space-y-3 text-sm text-slate-600">
              <div className="flex items-start gap-3 rounded-xl bg-slate-50 px-4 py-3">
                <CheckCircle2 size={18} className="mt-0.5 text-brand-600" />
                <span>Valider seulement les comptes dont l'email est verifie et le role justifie.</span>
              </div>
              <div className="flex items-start gap-3 rounded-xl bg-slate-50 px-4 py-3">
                <XCircle size={18} className="mt-0.5 text-red-600" />
                <span>Le compte administrateur principal reste unique, systeme et non duplicable.</span>
              </div>
              <div className="flex items-start gap-3 rounded-xl bg-slate-50 px-4 py-3">
                <Activity size={18} className="mt-0.5 text-violet-600" />
                <span>Chaque changement de statut ou de role entraine une revocation des sessions actives.</span>
              </div>
            </div>
          </SectionCard>
        </div>
      )}
    </div>
  );
};
