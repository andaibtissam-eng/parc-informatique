import { useEffect, useState } from "react";
import { Bell, Boxes, ClipboardCheck, LifeBuoy, Plus, Wrench } from "lucide-react";
import { http } from "../../api/http.js";
import { OverviewCharts } from "../../components/charts/OverviewCharts.jsx";
import { Button } from "../../components/ui/Button.jsx";
import { DataTable } from "../../components/ui/DataTable.jsx";
import { Loader } from "../../components/ui/Loader.jsx";
import { PageHeader } from "../../components/ui/PageHeader.jsx";
import { SectionCard } from "../../components/ui/SectionCard.jsx";
import { StatCard } from "../../components/ui/StatCard.jsx";
import { StatusBadge } from "../../components/ui/StatusBadge.jsx";
import { formatDateTime, unwrap } from "../../lib/format.js";

export const DashboardPage = () => {
  const [data, setData] = useState(null);

  useEffect(() => {
    http.get("/dashboard/summary").then((response) => setData(unwrap(response)));
  }, []);

  if (!data) {
    return <Loader label="Chargement du dashboard" />;
  }

  const recentRows = (data.recentActivity || []).slice(0, 5).map((item) => ({
    id: item.id,
    actor: item.actor,
    description: item.description,
    date: formatDateTime(item.occurredAt)
  }));

  return (
    <div>
      <PageHeader
        actions={
          <>
            <Button as="a" href="/equipments" variant="secondary">
              <Boxes size={17} /> Inventaire
            </Button>
            <Button as="a" href="/maintenance">
              <Plus size={17} /> Intervention
            </Button>
          </>
        }
        description="Une vue calme et organisee pour suivre le stock, les affectations, la maintenance et les alertes importantes."
        eyebrow="Dashboard"
        title="Vue d'ensemble du parc"
      />

      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <StatCard helper="Tous les actifs suivis" icon={Boxes} label="Equipements" value={data.totalEquipments} />
        <StatCard helper="Prets a etre affectes" icon={ClipboardCheck} label="Disponibles" tone="green" value={data.availableEquipments} />
        <StatCard helper="Materiels en circulation" icon={ClipboardCheck} label="Affectations actives" tone="violet" value={data.activeAssignments} />
        <StatCard helper="Demandes a traiter" icon={LifeBuoy} label="Tickets ouverts" tone="amber" value={data.openTickets} />
      </div>

      <div className="mt-5">
        <OverviewCharts labels={data.equipmentChartLabels} values={data.equipmentChartValues} />
      </div>

      <div className="mt-5 grid gap-5 xl:grid-cols-[1.2fr_0.8fr]">
        <SectionCard title="Activite recente" subtitle="Les dernieres operations importantes du parc.">
          <DataTable
            columns={[
              { key: "actor", label: "Acteur" },
              { key: "description", label: "Action" },
              { key: "date", label: "Date" }
            ]}
            emptyLabel="Aucune activite recente"
            rows={recentRows}
          />
        </SectionCard>

        <SectionCard title="Maintenance overview" subtitle="Un resume simple pour garder les priorites visibles.">
          <div className="space-y-3">
            <div className="flex items-center justify-between rounded-xl bg-slate-50 p-4">
              <div className="flex items-center gap-3">
                <Wrench className="text-warning" size={20} />
                <span className="font-semibold text-ink">Equipements en maintenance</span>
              </div>
              <StatusBadge value={String(data.maintenanceEquipments)} />
            </div>
            <div className="flex items-center justify-between rounded-xl bg-slate-50 p-4">
              <div className="flex items-center gap-3">
                <Bell className="text-brand-600" size={20} />
                <span className="font-semibold text-ink">Notifications non lues</span>
              </div>
              <span className="font-bold text-ink">{data.unreadNotifications}</span>
            </div>
          </div>
        </SectionCard>
      </div>
    </div>
  );
};
