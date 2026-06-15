import { useEffect, useState } from "react";
import { CheckCheck } from "lucide-react";
import { http } from "../../api/http.js";
import { Button } from "../../components/ui/Button.jsx";
import { Loader } from "../../components/ui/Loader.jsx";
import { PageHeader } from "../../components/ui/PageHeader.jsx";
import { SectionCard } from "../../components/ui/SectionCard.jsx";
import { StatusBadge } from "../../components/ui/StatusBadge.jsx";
import { formatDateTime, unwrap } from "../../lib/format.js";

export const NotificationsPage = () => {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = () =>
    http
      .get("/notifications")
      .then((response) => setItems(unwrap(response) || []))
      .finally(() => setLoading(false));

  useEffect(() => {
    load();
  }, []);

  const markRead = async () => {
    await http.patch("/notifications/read-all");
    await load();
  };

  return (
    <div>
      <PageHeader
        actions={
          <Button onClick={markRead} variant="secondary">
            <CheckCheck size={17} /> Tout marquer lu
          </Button>
        }
        description="Alertes de maintenance, garanties, systeme et evenements importants."
        eyebrow="System"
        title="Notifications"
      />
      <SectionCard title="Centre de notifications" subtitle="Les messages sont classes de facon lisible et actionnable.">
        {loading ? (
          <Loader label="Chargement des notifications" />
        ) : (
          <div className="space-y-3">
            {items.length === 0 && <div className="rounded-2xl border border-dashed border-line bg-slate-50 p-8 text-center text-sm text-muted">Aucune notification recente.</div>}
            {items.map((item) => (
              <article className="flex flex-col gap-3 rounded-2xl border border-line bg-white p-4 sm:flex-row sm:items-start sm:justify-between" key={item.id}>
                <div>
                  <p className="font-bold text-ink">{item.title}</p>
                  <p className="mt-1 text-sm leading-6 text-muted">{item.message}</p>
                  <p className="mt-2 text-xs text-slate-400">{formatDateTime(item.createdAt)}</p>
                </div>
                <StatusBadge value={item.read ? "CLOSED" : item.type} />
              </article>
            ))}
          </div>
        )}
      </SectionCard>
    </div>
  );
};
