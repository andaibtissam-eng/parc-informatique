import { useEffect, useMemo, useState } from "react";
import { AlertTriangle, Plus, Wrench } from "lucide-react";
import { http } from "../../api/http.js";
import { Button } from "../../components/ui/Button.jsx";
import { DataTable } from "../../components/ui/DataTable.jsx";
import { Loader } from "../../components/ui/Loader.jsx";
import { PageHeader } from "../../components/ui/PageHeader.jsx";
import { SectionCard } from "../../components/ui/SectionCard.jsx";
import { StatCard } from "../../components/ui/StatCard.jsx";
import { StatusBadge } from "../../components/ui/StatusBadge.jsx";
import { useAuthStore } from "../../features/auth/auth.store.js";
import { hasPermission } from "../../lib/access.js";
import { formatDateTime, pageContent, unwrap } from "../../lib/format.js";

const emptyForm = {
  equipmentId: "",
  technicianId: "",
  title: "",
  description: "",
  priority: "MEDIUM",
  status: "OPEN",
  slaDeadline: "",
  cost: "",
  partsReplaced: "",
  rootCause: "",
  notes: ""
};

export const MaintenancePage = () => {
  const user = useAuthStore((state) => state.user);
  const canManage = hasPermission(user, "maintenances.manage");
  const canRequest = hasPermission(user, "maintenances.request");

  const [maintenances, setMaintenances] = useState([]);
  const [board, setBoard] = useState([]);
  const [references, setReferences] = useState(null);
  const [equipments, setEquipments] = useState([]);
  const [ownAssignments, setOwnAssignments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [feedback, setFeedback] = useState({ type: "", message: "" });
  const [form, setForm] = useState(emptyForm);

  const load = async () => {
    setLoading(true);
    try {
      if (canManage) {
        const requests = [http.get("/maintenances"), http.get("/maintenances/board"), http.get("/equipments", { params: { size: 100 } }), http.get("/references")];
        const responses = await Promise.all(requests);
        setMaintenances(unwrap(responses[0]) || []);
        setBoard(unwrap(responses[1]) || []);
        setEquipments(pageContent(unwrap(responses[2])));
        setReferences(unwrap(responses[3]));
        setOwnAssignments([]);
      } else if (canRequest) {
        const responses = await Promise.all([http.get("/assignments", { params: { size: 50 } }), http.get("/references")]);
        const assignments = pageContent(unwrap(responses[0]));
        setOwnAssignments(assignments);
        setEquipments(
          assignments
            .filter((item) => item.status === "ACTIVE" || item.status === "OVERDUE" || item.status === "PENDING")
            .map((item) => ({
              id: item.equipmentId,
              inventoryCode: item.inventoryCode,
              name: item.equipmentName,
              status: item.status
            }))
        );
        setReferences(unwrap(responses[1]));
        setMaintenances([]);
        setBoard([]);
      } else {
        setMaintenances([]);
        setBoard([]);
        setEquipments([]);
        setOwnAssignments([]);
        setReferences(null);
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, [canManage, canRequest]);

  const columns = ["OPEN", "IN_PROGRESS", "ON_HOLD", "RESOLVED", "CLOSED"];

  const statusCounts = useMemo(
    () =>
      columns.reduce((accumulator, status) => {
        accumulator[status] = board.filter((item) => item.status === status).length;
        return accumulator;
      }, {}),
    [board]
  );

  const availableEquipments = useMemo(() => equipments.filter((item) => item.status !== "RETIRED"), [equipments]);

  const submit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setFeedback({ type: "", message: "" });
    try {
      await http.post("/maintenances", {
        ...form,
        technicianId: canManage ? form.technicianId || null : null,
        status: canManage ? form.status : "OPEN",
        slaDeadline: form.slaDeadline || null,
        cost: form.cost ? Number(form.cost) : null
      });
      setForm(emptyForm);
      setFeedback({ type: "success", message: "Intervention creee avec succes." });
      await load();
    } catch (error) {
      setFeedback({ type: "error", message: error.response?.data?.message || "Impossible de creer l'intervention." });
    } finally {
      setSaving(false);
    }
  };

  const updateStatus = async (maintenanceId, status) => {
    setSaving(true);
    setFeedback({ type: "", message: "" });
    try {
      await http.patch(`/maintenances/${maintenanceId}/status`, { status });
      setFeedback({ type: "success", message: "Statut mis a jour." });
      await load();
    } catch (error) {
      setFeedback({ type: "error", message: error.response?.data?.message || "Impossible de mettre a jour le statut." });
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="min-w-0">
      <PageHeader
        actions={
          canManage || canRequest ? (
            <Button onClick={() => document.getElementById("maintenance-create-form")?.scrollIntoView({ behavior: "smooth", block: "start" })} type="button">
              <Plus size={17} /> Nouvelle intervention
            </Button>
          ) : null
        }
        description="Le kanban est relie aux tickets de maintenance et la creation d'intervention est maintenant fonctionnelle selon le role."
        eyebrow="Maintenance"
        title="Kanban maintenance"
      />

      <div className="grid gap-4 md:grid-cols-4">
        <StatCard helper="Tickets en attente" icon={AlertTriangle} label="Ouverts" value={statusCounts.OPEN || 0} />
        <StatCard helper="Interventions en cours d'execution" icon={Wrench} label="En cours" tone="violet" value={statusCounts.IN_PROGRESS || 0} />
        <StatCard helper="Tickets a risque eleve" icon={AlertTriangle} label="Critiques" tone="red" value={maintenances.filter((item) => item.priority === "CRITICAL").length} />
        <StatCard helper={canManage ? "Dossiers termines ou clos" : "Materiels qui peuvent faire l'objet d'une demande"} icon={Plus} label={canManage ? "Traites" : "Mes equipements"} tone="green" value={canManage ? (statusCounts.RESOLVED || 0) + (statusCounts.CLOSED || 0) : availableEquipments.length} />
      </div>

      {feedback.message && (
        <div className={`mt-5 rounded-2xl px-4 py-3 text-sm font-semibold ${feedback.type === "success" ? "bg-emerald-50 text-emerald-700" : "bg-red-50 text-danger"}`}>
          {feedback.message}
        </div>
      )}

      <div className="mt-5 space-y-5">
        <div className="min-w-0 space-y-5">
          {canManage ? (
            <>
              <div className="max-w-full overflow-x-auto overscroll-x-contain rounded-2xl border border-line bg-white/70 p-3 pb-4">
                <div className="grid min-w-[940px] grid-cols-[repeat(5,minmax(170px,1fr))] gap-3">
                  {columns.map((status) => (
                    <section className="min-w-0 rounded-2xl border border-line bg-white p-3 shadow-soft" key={status}>
                      <div className="mb-3 flex items-center justify-between gap-2">
                        <h2 className="truncate text-sm font-bold text-ink">{status.replaceAll("_", " ")}</h2>
                        <span className="rounded-full bg-slate-100 px-2 py-1 text-xs font-bold text-muted">{statusCounts[status] || 0}</span>
                      </div>
                      <div className="space-y-3">
                        {board
                          .filter((item) => item.status === status)
                          .slice(0, 4)
                          .map((item) => (
                            <article className="rounded-xl border border-line bg-slate-50 p-3" key={item.id}>
                              <div className="flex items-start justify-between gap-2">
                                <p className="truncate text-sm font-bold text-ink">{item.reference}</p>
                                <StatusBadge value={item.priority} />
                              </div>
                              <p className="mt-2 line-clamp-2 text-sm text-slate-700">{item.equipmentName}</p>
                              <p className="mt-1 truncate text-xs text-muted">{item.technicianName || "Non assigne"}</p>
                              <div className="mt-3 flex flex-wrap gap-2">
                                {status !== "IN_PROGRESS" && (
                                  <Button className="h-8 rounded-lg px-2.5 text-[11px]" disabled={saving} onClick={() => updateStatus(item.id, "IN_PROGRESS")} type="button" variant="secondary">
                                    Lancer
                                  </Button>
                                )}
                                {status !== "RESOLVED" && (
                                  <Button className="h-8 rounded-lg px-2.5 text-[11px]" disabled={saving} onClick={() => updateStatus(item.id, "RESOLVED")} type="button" variant="subtle">
                                    Resoudre
                                  </Button>
                                )}
                              </div>
                            </article>
                          ))}
                        {board.filter((item) => item.status === status).length === 0 && (
                          <div className="rounded-xl border border-dashed border-line bg-slate-50 px-3 py-6 text-center text-xs font-semibold text-muted">
                            Aucune carte
                          </div>
                        )}
                      </div>
                    </section>
                  ))}
                </div>
              </div>

              <SectionCard subtitle="Tableau detaille pour le suivi operationnel." title="Registre des maintenances">
                {loading ? (
                  <Loader label="Chargement des maintenances" />
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
                    emptyLabel="Aucune maintenance"
                    rows={maintenances}
                  />
                )}
              </SectionCard>
            </>
          ) : (
            <SectionCard subtitle="Les employes voient ici les equipements qui leur sont rattaches pour deposer une demande de support utile." title="Mes equipements affectes">
              {loading ? (
                <Loader label="Chargement de vos equipements" />
              ) : (
                <DataTable
                  columns={[
                    { key: "equipmentName", label: "Equipement" },
                    { key: "inventoryCode", label: "Code inventaire" },
                    { key: "status", label: "Statut affectation", render: (row) => <StatusBadge value={row.status} /> },
                    { key: "expectedReturnDate", label: "Retour prevu", render: (row) => formatDateTime(row.expectedReturnDate) }
                  ]}
                  emptyLabel="Aucun equipement affecte pour le moment"
                  rows={ownAssignments}
                />
              )}
            </SectionCard>
          )}
        </div>

        <SectionCard className="min-w-0" subtitle={canManage ? "Creation complete pour admin, responsable et technicien." : "Les employes peuvent declarer une panne. Le reste du cycle est gere par l'equipe IT."} title="Nouvelle intervention">
          {canManage || canRequest ? (
            <form className="grid gap-3 md:grid-cols-2" onSubmit={submit}>
              <div className="sr-only" id="maintenance-create-form" />
              <label className="block">
                <span className="text-sm font-bold text-ink">Equipement</span>
                <select
                  className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none"
                  onChange={(event) => setForm((current) => ({ ...current, equipmentId: event.target.value }))}
                  required
                  value={form.equipmentId}
                >
                  <option value="">Selectionner</option>
                  {availableEquipments.map((item) => (
                    <option key={item.id} value={item.id}>
                      {item.inventoryCode} - {item.name}
                    </option>
                  ))}
                </select>
              </label>
              {canManage && (
                <label className="block">
                  <span className="text-sm font-bold text-ink">Technicien</span>
                  <select
                    className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none"
                    onChange={(event) => setForm((current) => ({ ...current, technicianId: event.target.value }))}
                    value={form.technicianId}
                  >
                    <option value="">Assignation ulterieure</option>
                    {(references?.technicians || []).map((item) => (
                      <option key={item.id} value={item.id}>
                        {item.label}
                      </option>
                    ))}
                  </select>
                </label>
              )}
              <label className="block">
                <span className="text-sm font-bold text-ink">Titre</span>
                <input
                  className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none"
                  onChange={(event) => setForm((current) => ({ ...current, title: event.target.value }))}
                  required
                  value={form.title}
                />
              </label>
              <label className="block md:col-span-2">
                <span className="text-sm font-bold text-ink">Description</span>
                <textarea
                  className="mt-1.5 min-h-24 w-full rounded-xl border border-line bg-slate-50 px-3 py-3 text-sm outline-none"
                  onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))}
                  required
                  value={form.description}
                />
              </label>
              <div className="grid gap-3 sm:grid-cols-2 md:col-span-2">
                <label className="block">
                  <span className="text-sm font-bold text-ink">Priorite</span>
                  <select
                    className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none"
                    onChange={(event) => setForm((current) => ({ ...current, priority: event.target.value }))}
                    value={form.priority}
                  >
                    <option value="LOW">Basse</option>
                    <option value="MEDIUM">Moyenne</option>
                    <option value="HIGH">Haute</option>
                    <option value="CRITICAL">Critique</option>
                  </select>
                </label>
                <label className="block">
                  <span className="text-sm font-bold text-ink">Statut</span>
                  <select
                    className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none"
                    disabled={!canManage}
                    onChange={(event) => setForm((current) => ({ ...current, status: event.target.value }))}
                    value={canManage ? form.status : "OPEN"}
                  >
                    <option value="OPEN">OPEN</option>
                    <option value="IN_PROGRESS">IN PROGRESS</option>
                    <option value="ON_HOLD">ON HOLD</option>
                    <option value="RESOLVED">RESOLVED</option>
                    <option value="CLOSED">CLOSED</option>
                  </select>
                </label>
              </div>
              <div className="grid gap-3 sm:grid-cols-2 md:col-span-2">
                <label className="block">
                  <span className="text-sm font-bold text-ink">Deadline SLA</span>
                  <input
                    className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none"
                    onChange={(event) => setForm((current) => ({ ...current, slaDeadline: event.target.value }))}
                    type="datetime-local"
                    value={form.slaDeadline}
                  />
                </label>
                <label className="block">
                  <span className="text-sm font-bold text-ink">Cout</span>
                  <input
                    className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none"
                    min="0"
                    onChange={(event) => setForm((current) => ({ ...current, cost: event.target.value }))}
                    step="0.01"
                    type="number"
                    value={form.cost}
                  />
                </label>
              </div>
              <label className="block">
                <span className="text-sm font-bold text-ink">Pieces remplacees</span>
                <input
                  className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none"
                  onChange={(event) => setForm((current) => ({ ...current, partsReplaced: event.target.value }))}
                  value={form.partsReplaced}
                />
              </label>
              <label className="block">
                <span className="text-sm font-bold text-ink">Cause racine</span>
                <input
                  className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none"
                  onChange={(event) => setForm((current) => ({ ...current, rootCause: event.target.value }))}
                  value={form.rootCause}
                />
              </label>
              <label className="block md:col-span-2">
                <span className="text-sm font-bold text-ink">Notes</span>
                <textarea
                  className="mt-1.5 min-h-20 w-full rounded-xl border border-line bg-slate-50 px-3 py-3 text-sm outline-none"
                  onChange={(event) => setForm((current) => ({ ...current, notes: event.target.value }))}
                  value={form.notes}
                />
              </label>
              <Button className="w-full md:col-span-2" disabled={saving} type="submit">
                {saving ? "Enregistrement..." : canManage ? "Creer l'intervention" : "Declarer la panne"}
              </Button>
            </form>
          ) : (
            <p className="text-sm leading-6 text-muted">
              Votre compte n'a pas de permission sur les maintenances. Un employe peut declarer une panne, tandis que l'equipe IT gere l'assignation et les statuts.
            </p>
          )}
        </SectionCard>
      </div>
    </div>
  );
};
