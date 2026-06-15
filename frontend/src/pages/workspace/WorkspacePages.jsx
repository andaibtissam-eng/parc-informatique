import { useEffect, useMemo, useState } from "react";
import {
  Bell,
  CalendarDays,
  CheckCircle2,
  Clock3,
  Download,
  FileSpreadsheet,
  FileText,
  FolderTree,
  History,
  LifeBuoy,
  Lock,
  MapPin,
  Repeat2,
  Settings,
  ShieldCheck,
  Tag,
  UserCircle,
  Users
} from "lucide-react";
import { Link } from "react-router-dom";
import { http } from "../../api/http.js";
import { OverviewCharts } from "../../components/charts/OverviewCharts.jsx";
import { Button } from "../../components/ui/Button.jsx";
import { DataTable } from "../../components/ui/DataTable.jsx";
import { Loader } from "../../components/ui/Loader.jsx";
import { PageHeader } from "../../components/ui/PageHeader.jsx";
import { SectionCard } from "../../components/ui/SectionCard.jsx";
import { StatCard } from "../../components/ui/StatCard.jsx";
import { StatusBadge } from "../../components/ui/StatusBadge.jsx";
import { useAuthStore } from "../../features/auth/auth.store.js";
import { hasPermission, roleLabel } from "../../lib/access.js";
import { formatDate, formatDateTime, pageContent, percent, unwrap } from "../../lib/format.js";

const useDashboardSummary = () => {
  const [state, setState] = useState({ loading: true, data: null });

  useEffect(() => {
    let active = true;
    http
      .get("/dashboard/summary")
      .then((response) => {
        if (active) {
          setState({ loading: false, data: unwrap(response) });
        }
      })
      .catch(() => {
        if (active) {
          setState({ loading: false, data: null });
        }
      });
    return () => {
      active = false;
    };
  }, []);

  return state;
};

const useReferenceData = () => {
  const [state, setState] = useState({ loading: true, data: null });

  useEffect(() => {
    let active = true;
    http
      .get("/references")
      .then((response) => {
        if (active) {
          setState({ loading: false, data: unwrap(response) });
        }
      })
      .catch(() => {
        if (active) {
          setState({ loading: false, data: null });
        }
      });
    return () => {
      active = false;
    };
  }, []);

  return state;
};

const useAssignmentsData = () => {
  const [state, setState] = useState({ loading: true, data: [] });

  useEffect(() => {
    let active = true;
    http
      .get("/assignments", { params: { size: 50 } })
      .then((response) => {
        if (active) {
          setState({ loading: false, data: pageContent(unwrap(response)) });
        }
      })
      .catch(() => {
        if (active) {
          setState({ loading: false, data: [] });
        }
      });
    return () => {
      active = false;
    };
  }, []);

  return state;
};

const useMaintenancesData = () => {
  const [state, setState] = useState({ loading: true, list: [], board: [] });

  useEffect(() => {
    let active = true;
    Promise.all([http.get("/maintenances"), http.get("/maintenances/board")])
      .then(([listResponse, boardResponse]) => {
        if (active) {
          setState({
            loading: false,
            list: unwrap(listResponse) || [],
            board: unwrap(boardResponse) || []
          });
        }
      })
      .catch(() => {
        if (active) {
          setState({ loading: false, list: [], board: [] });
        }
      });
    return () => {
      active = false;
    };
  }, []);

  return state;
};

const useNotificationsData = () => {
  const [state, setState] = useState({ loading: true, items: [] });

  useEffect(() => {
    let active = true;
    http
      .get("/notifications")
      .then((response) => {
        if (active) {
          setState({ loading: false, items: unwrap(response) || [] });
        }
      })
      .catch(() => {
        if (active) {
          setState({ loading: false, items: [] });
        }
      });
    return () => {
      active = false;
    };
  }, []);

  return state;
};

const useAdminOverview = (enabled) => {
  const [state, setState] = useState({ loading: enabled, data: null });

  useEffect(() => {
    if (!enabled) {
      setState({ loading: false, data: null });
      return;
    }

    let active = true;
    http
      .get("/users/admin-overview")
      .then((response) => {
        if (active) {
          setState({ loading: false, data: unwrap(response) });
        }
      })
      .catch(() => {
        if (active) {
          setState({ loading: false, data: null });
        }
      });
    return () => {
      active = false;
    };
  }, [enabled]);

  return state;
};

const EmptyPanel = ({ title }) => (
  <div className="rounded-2xl border border-dashed border-line bg-slate-50 px-6 py-12 text-center">
    <p className="text-sm font-semibold text-ink">{title}</p>
    <p className="mt-1 text-sm text-muted">Les donnees apparaitront ici des qu'elles seront disponibles.</p>
  </div>
);

export const CategoriesPage = () => {
  const { loading, data } = useReferenceData();

  if (loading) {
    return <Loader label="Chargement des referentiels" />;
  }

  return (
    <div>
      <PageHeader
        description="Les referentiels de categories, localisations, fournisseurs et departements sont relies au backend et utilisables dans les formulaires."
        eyebrow="Inventory"
        title="Categories et referentiels"
      />
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <StatCard helper="Familles de materiels actives" icon={FolderTree} label="Categories" value={data?.categories?.length || 0} />
        <StatCard helper="Zones de rattachement" icon={MapPin} label="Localisations" tone="violet" value={data?.locations?.length || 0} />
        <StatCard helper="Fournisseurs disponibles" icon={Tag} label="Fournisseurs" tone="green" value={data?.suppliers?.length || 0} />
        <StatCard helper="Structures metier reliees" icon={Users} label="Departements" tone="amber" value={data?.departments?.length || 0} />
      </div>
      <div className="mt-5 grid gap-5 xl:grid-cols-2">
        <SectionCard subtitle="Classification disponible pour les equipements." title="Catalogue categories">
          <DataTable
            columns={[
              { key: "label", label: "Categorie" },
              { key: "secondary", label: "Code / precision" }
            ]}
            emptyLabel="Aucune categorie"
            rows={data?.categories || []}
          />
        </SectionCard>
        <SectionCard subtitle="Points de livraison et d'affectation." title="Localisations">
          <DataTable
            columns={[
              { key: "label", label: "Localisation" },
              { key: "secondary", label: "Detail" }
            ]}
            emptyLabel="Aucune localisation"
            rows={data?.locations || []}
          />
        </SectionCard>
        <SectionCard subtitle="Structures organisationnelles du parc." title="Departements">
          <DataTable
            columns={[
              { key: "label", label: "Departement" },
              { key: "secondary", label: "Code" }
            ]}
            emptyLabel="Aucun departement"
            rows={data?.departments || []}
          />
        </SectionCard>
        <SectionCard subtitle="Partenaires de fourniture et support." title="Fournisseurs">
          <DataTable
            columns={[
              { key: "label", label: "Fournisseur" },
              { key: "secondary", label: "Contact" }
            ]}
            emptyLabel="Aucun fournisseur"
            rows={data?.suppliers || []}
          />
        </SectionCard>
      </div>
    </div>
  );
};

export const ReturnsPage = () => {
  const { loading, data } = useAssignmentsData();
  const returnable = data.filter((item) => item.status === "ACTIVE" || item.status === "OVERDUE");

  return (
    <div>
      <PageHeader
        description="Cette vue centralise les materiels en circulation, les retours attendus et les dossiers a cloturer."
        eyebrow="Operations"
        title="Restitutions"
      />
      <div className="grid gap-4 md:grid-cols-3">
        <StatCard helper="Materiels encore en circulation" icon={Repeat2} label="Retours attendus" value={returnable.length} />
        <StatCard
          helper="Taux de retour sur l'ensemble des affectations"
          icon={CheckCircle2}
          label="Retours clotures"
          tone="green"
          value={`${percent(data.filter((item) => item.status === "RETURNED").length, Math.max(data.length, 1))}%`}
        />
        <StatCard
          helper="A traiter rapidement pour eviter les retards"
          icon={Clock3}
          label="En retard"
          tone="amber"
          value={data.filter((item) => item.status === "OVERDUE").length}
        />
      </div>
      <div className="mt-5 grid gap-5 xl:grid-cols-[1.1fr_0.9fr]">
        <SectionCard subtitle="Les affectations actives ou en retard sont pretes pour une restitution." title="Retours a preparer">
          {loading ? (
            <Loader label="Chargement des retours" />
          ) : (
            <DataTable
              columns={[
                { key: "equipmentName", label: "Equipement" },
                { key: "beneficiaryName", label: "Utilisateur" },
                { key: "status", label: "Statut", render: (row) => <StatusBadge value={row.status} /> },
                { key: "expectedReturnDate", label: "Retour prevu", render: (row) => formatDate(row.expectedReturnDate) }
              ]}
              emptyLabel="Aucun retour en attente"
              rows={returnable}
            />
          )}
        </SectionCard>
        <SectionCard subtitle="Le module de restitution complet est relie a la page Affectations pour la validation et l'enregistrement du retour." title="Parcours de restitution">
          <div className="space-y-3 text-sm leading-6 text-muted">
            <p>1. Ouvrir la page Affectations.</p>
            <p>2. Selectionner une affectation active.</p>
            <p>3. Enregistrer l'etat de retour et le rapport.</p>
            <p>4. Le materiel repasse disponible ou est reoriente vers la maintenance.</p>
          </div>
          <Button as={Link} className="mt-5" to="/assignments">
            Ouvrir les affectations
          </Button>
        </SectionCard>
      </div>
    </div>
  );
};

export const TicketsPage = () => {
  const { loading, list } = useMaintenancesData();

  return (
    <div>
      <PageHeader
        description="Le registre des tickets est maintenant branche sur les maintenances reelles pour visualiser incidents, priorites et techniciens."
        eyebrow="Support"
        title="Tickets"
      />
      <div className="grid gap-4 md:grid-cols-4">
        <StatCard helper="Incidents ouverts" icon={LifeBuoy} label="Ouverts" value={list.filter((item) => item.status === "OPEN").length} />
        <StatCard helper="Interventions en cours" icon={Clock3} label="En cours" tone="violet" value={list.filter((item) => item.status === "IN_PROGRESS").length} />
        <StatCard helper="Tickets critiques" icon={ShieldCheck} label="Critiques" tone="red" value={list.filter((item) => item.priority === "CRITICAL").length} />
        <StatCard helper="Tickets resolus ou clos" icon={CheckCircle2} label="Traites" tone="green" value={list.filter((item) => item.status === "RESOLVED" || item.status === "CLOSED").length} />
      </div>
      <div className="mt-5">
        <SectionCard subtitle="Vue detaillee des tickets relies au module maintenance." title="Registre tickets">
          {loading ? (
            <Loader label="Chargement des tickets" />
          ) : (
            <DataTable
              columns={[
                { key: "reference", label: "Reference" },
                { key: "title", label: "Ticket" },
                { key: "equipmentName", label: "Equipement" },
                { key: "technicianName", label: "Technicien" },
                { key: "priority", label: "Priorite", render: (row) => <StatusBadge value={row.priority} /> },
                { key: "status", label: "Statut", render: (row) => <StatusBadge value={row.status} /> },
                { key: "slaDeadline", label: "SLA", render: (row) => formatDateTime(row.slaDeadline) }
              ]}
              emptyLabel="Aucun ticket"
              rows={list}
            />
          )}
        </SectionCard>
      </div>
    </div>
  );
};

export const HistoryPage = () => {
  const user = useAuthStore((state) => state.user);
  const { loading, data } = useDashboardSummary();
  const admin = useAdminOverview(hasPermission(user, "users.manage"));

  const accountActivity = admin.data?.recentAccountActivity || [];
  const activity = data?.recentActivity || [];

  return (
    <div>
      <PageHeader
        description="Cette page affiche maintenant les logs recents disponibles au lieu de cartes vides, avec les flux metier et les activites de compte."
        eyebrow="Audit"
        title="Historique"
      />
      <div className="grid gap-5 xl:grid-cols-2">
        <SectionCard subtitle="Mouvements recents du parc et actions metier." title="Activite operationnelle">
          {loading ? (
            <Loader label="Chargement de l'activite" />
          ) : (
            <DataTable
              columns={[
                { key: "actor", label: "Acteur" },
                { key: "description", label: "Action" },
                { key: "occurredAt", label: "Date", render: (row) => formatDateTime(row.occurredAt) }
              ]}
              emptyLabel="Aucun log operationnel"
              rows={activity}
            />
          )}
        </SectionCard>
        <SectionCard subtitle="Disponible pour les administrateurs afin de suivre inscriptions, activations et modifications de comptes." title="Activite comptes">
          {admin.loading ? (
            <Loader label="Chargement des logs comptes" />
          ) : hasPermission(user, "users.manage") ? (
            <DataTable
              columns={[
                { key: "actor", label: "Acteur" },
                { key: "description", label: "Evenement" },
                { key: "occurredAt", label: "Date", render: (row) => formatDateTime(row.occurredAt) }
              ]}
              emptyLabel="Aucun log compte"
              rows={accountActivity}
            />
          ) : (
            <EmptyPanel title="Les logs comptes sont reserves a l'administrateur." />
          )}
        </SectionCard>
      </div>
    </div>
  );
};

export const ProfilePage = () => {
  const user = useAuthStore((state) => state.user);
  const { items } = useNotificationsData();

  return (
    <div>
      <PageHeader
        description="Le profil utilise maintenant les vraies informations de session, roles et permissions renvoyes par le backend."
        eyebrow="Workspace"
        title="Profil utilisateur"
      />
      <div className="grid gap-5 xl:grid-cols-[0.9fr_1.1fr]">
        <SectionCard subtitle="Resume du compte connecte." title="Identite">
          <div className="flex items-start gap-4">
            <div className="grid h-16 w-16 place-items-center rounded-2xl bg-violet-100 text-lg font-bold text-violet-700">
              {(user?.fullName || "PF")
                .split(" ")
                .filter(Boolean)
                .slice(0, 2)
                .map((part) => part[0]?.toUpperCase())
                .join("")}
            </div>
            <div className="space-y-2">
              <p className="font-display text-2xl font-bold text-ink">{user?.fullName || "Utilisateur"}</p>
              <p className="text-sm text-muted">{user?.email}</p>
              <div className="flex flex-wrap gap-2">
                {(user?.roles || []).map((role) => (
                  <span className="rounded-full bg-brand-50 px-3 py-1 text-xs font-bold text-brand-700" key={role}>
                    {roleLabel(role)}
                  </span>
                ))}
                <StatusBadge value={user?.status || "ACTIVE"} />
              </div>
            </div>
          </div>
        </SectionCard>
        <SectionCard subtitle="Permissions et surface d'acces du compte courant." title="Autorisations">
          <div className="grid gap-3 sm:grid-cols-2">
            {(user?.permissions || []).map((permission) => (
              <div className="rounded-xl border border-line bg-slate-50 px-3 py-2 text-sm text-slate-700" key={permission}>
                {permission}
              </div>
            ))}
          </div>
        </SectionCard>
        <SectionCard subtitle="Raccourcis utiles pour votre espace." title="Actions rapides">
          <div className="flex flex-wrap gap-3">
            <Button as={Link} to="/notifications">
              <Bell size={17} /> Notifications
            </Button>
            <Button as={Link} to="/assignments" variant="secondary">
              <Repeat2 size={17} /> Mes affectations
            </Button>
            <Button as={Link} to="/maintenance" variant="secondary">
              <LifeBuoy size={17} /> Maintenance
            </Button>
          </div>
        </SectionCard>
        <SectionCard subtitle="Apercu des alertes recues par votre compte." title="Notifications recentes">
          <DataTable
            columns={[
              { key: "title", label: "Notification" },
              { key: "type", label: "Type", render: (row) => <StatusBadge value={row.type} /> },
              { key: "createdAt", label: "Date", render: (row) => formatDateTime(row.createdAt) }
            ]}
            emptyLabel="Aucune notification recente"
            rows={items.slice(0, 6)}
          />
        </SectionCard>
      </div>
    </div>
  );
};

export const SettingsPage = () => {
  const user = useAuthStore((state) => state.user);

  return (
    <div>
      <PageHeader
        description="Cette vue remplace les anciens visuels statiques par un panneau de configuration lisible, utile pour la soutenance et l'exploitation."
        eyebrow="Workspace"
        title="Parametres"
      />
      <div className="grid gap-5 xl:grid-cols-2">
        <SectionCard subtitle="Config applicative utile en demonstration." title="Configuration active">
          <div className="space-y-3 text-sm">
            <div className="flex items-center justify-between rounded-xl bg-slate-50 px-4 py-3">
              <span className="text-muted">Frontend servi par</span>
              <span className="font-semibold text-ink">React + Vite</span>
            </div>
            <div className="flex items-center justify-between rounded-xl bg-slate-50 px-4 py-3">
              <span className="text-muted">Backend</span>
              <span className="font-semibold text-ink">Spring Boot 3</span>
            </div>
            <div className="flex items-center justify-between rounded-xl bg-slate-50 px-4 py-3">
              <span className="text-muted">Base de donnees</span>
              <span className="font-semibold text-ink">PostgreSQL</span>
            </div>
            <div className="flex items-center justify-between rounded-xl bg-slate-50 px-4 py-3">
              <span className="text-muted">Authentification</span>
              <span className="font-semibold text-ink">JWT + RBAC</span>
            </div>
          </div>
        </SectionCard>
        <SectionCard subtitle="Pages et services utiles en soutenance." title="Liens utiles">
          <div className="flex flex-wrap gap-3">
            <Button as="a" href="/swagger-ui.html" target="_blank" variant="secondary">
              <Settings size={17} /> Swagger
            </Button>
            <Button as={Link} to="/reports">
              <Download size={17} /> Exports
            </Button>
            <Button as={Link} to="/users" variant="secondary">
              <Users size={17} /> Administration
            </Button>
          </div>
        </SectionCard>
        <SectionCard subtitle="Compte courant et securite." title="Securite session">
          <div className="space-y-3 text-sm">
            <div className="flex items-center gap-3 rounded-xl bg-slate-50 px-4 py-3">
              <Lock className="text-brand-600" size={18} />
              <div>
                <p className="font-semibold text-ink">Compte connecte</p>
                <p className="text-muted">{user?.email}</p>
              </div>
            </div>
            <div className="flex items-center gap-3 rounded-xl bg-slate-50 px-4 py-3">
              <ShieldCheck className="text-success" size={18} />
              <div>
                <p className="font-semibold text-ink">Role principal</p>
                <p className="text-muted">{roleLabel(user?.roles?.[0] || "EMPLOYE")}</p>
              </div>
            </div>
          </div>
        </SectionCard>
        <SectionCard subtitle="La gestion complete des permissions est disponible dans l'administration utilisateurs." title="Gouvernance acces">
          {hasPermission(user, "users.manage") ? (
            <Button as={Link} to="/users">
              Ouvrir la gestion des utilisateurs
            </Button>
          ) : (
            <EmptyPanel title="Le pilotage des comptes et permissions est reserve a l'administration." />
          )}
        </SectionCard>
      </div>
    </div>
  );
};

export const AnalyticsPage = () => {
  const { loading, data } = useDashboardSummary();

  if (loading) {
    return <Loader label="Chargement des analytics" />;
  }

  if (!data) {
    return <EmptyPanel title="Impossible de charger les analytics." />;
  }

  return (
    <div>
      <PageHeader
        description="Les analytics lisent maintenant les KPIs du backend au lieu d'afficher des cartes vides."
        eyebrow="Insights"
        title="Analytics"
      />
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <StatCard helper="Total des actifs suivis" icon={FolderTree} label="Equipements" value={data.totalEquipments} />
        <StatCard helper="Taux de disponibilite du parc" icon={CheckCircle2} label="Disponibilite" tone="green" value={`${percent(data.availableEquipments, Math.max(data.totalEquipments, 1))}%`} />
        <StatCard helper="Equipements actuellement affectes" icon={Users} label="Affectations actives" tone="violet" value={data.activeAssignments} />
        <StatCard helper="Maintenance et tickets ouverts" icon={LifeBuoy} label="Incidents ouverts" tone="amber" value={data.openTickets} />
      </div>
      <div className="mt-5">
        <OverviewCharts labels={data.equipmentChartLabels} values={data.equipmentChartValues} />
      </div>
      <div className="mt-5">
        <SectionCard subtitle="Activite recente recapitulant les faits marquants du parc." title="Flux recent">
          <DataTable
            columns={[
              { key: "actor", label: "Acteur" },
              { key: "description", label: "Evenement" },
              { key: "occurredAt", label: "Date", render: (row) => formatDateTime(row.occurredAt) }
            ]}
            emptyLabel="Aucune activite recente"
            rows={data.recentActivity || []}
          />
        </SectionCard>
      </div>
    </div>
  );
};

export const CalendarPage = () => {
  const assignments = useAssignmentsData();
  const maintenances = useMaintenancesData();

  const upcoming = useMemo(() => {
    const assignmentEvents = assignments.data
      .filter((item) => item.expectedReturnDate)
      .map((item) => ({
        id: item.id,
        type: "Retour prevu",
        label: `${item.equipmentName} - ${item.beneficiaryName}`,
        date: `${item.expectedReturnDate}T09:00:00`
      }));

    const maintenanceEvents = maintenances.list
      .filter((item) => item.slaDeadline)
      .map((item) => ({
        id: item.id,
        type: "SLA maintenance",
        label: `${item.reference} - ${item.equipmentName}`,
        date: item.slaDeadline
      }));

    return [...assignmentEvents, ...maintenanceEvents]
      .sort((a, b) => new Date(a.date) - new Date(b.date))
      .slice(0, 12);
  }, [assignments.data, maintenances.list]);

  return (
    <div>
      <PageHeader
        description="Le calendrier agrège maintenant les dates de retour et les echeances SLA disponibles dans le systeme."
        eyebrow="Planning"
        title="Calendrier"
      />
      <div className="grid gap-4 md:grid-cols-3">
        <StatCard helper="Dates de retour programmees" icon={Repeat2} label="Retours prevus" value={assignments.data.filter((item) => item.expectedReturnDate).length} />
        <StatCard helper="Maintenances avec SLA" icon={Clock3} label="Echeances SLA" tone="violet" value={maintenances.list.filter((item) => item.slaDeadline).length} />
        <StatCard helper="Vision centralisee des actions a venir" icon={CalendarDays} label="Evenements" tone="green" value={upcoming.length} />
      </div>
      <div className="mt-5">
        <SectionCard subtitle="Liste chronologique des prochaines dates importantes." title="Agenda des actions">
          {assignments.loading || maintenances.loading ? (
            <Loader label="Chargement du calendrier" />
          ) : (
            <DataTable
              columns={[
                { key: "type", label: "Type" },
                { key: "label", label: "Element" },
                { key: "date", label: "Date", render: (row) => formatDateTime(row.date) }
              ]}
              emptyLabel="Aucun evenement planifie"
              rows={upcoming}
            />
          )}
        </SectionCard>
      </div>
    </div>
  );
};
