import { useEffect, useMemo, useState } from "react";
import { CheckCircle2, ClipboardCheck, PackagePlus, Plus, RotateCcw, Send } from "lucide-react";
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
import { formatDate, pageContent, unwrap } from "../../lib/format.js";

const emptyCreateForm = {
  equipmentId: "",
  beneficiaryId: "",
  startDate: new Date().toISOString().slice(0, 10),
  expectedReturnDate: "",
  digitalSignature: "",
  notes: ""
};

const emptyReturnForm = {
  assignmentId: "",
  conditionStatus: "GOOD",
  report: "",
  reassignable: true
};

const emptyMaterialRequestForm = {
  materialType: "",
  preferredModel: "",
  priority: "MEDIUM",
  justification: ""
};

export const AssignmentsPage = () => {
  const user = useAuthStore((state) => state.user);
  const canManage = hasPermission(user, "assignments.manage");
  const canRequestMaterial = !canManage && hasPermission(user, "assignments.read.own");
  const canViewMaterialRequests = canManage || canRequestMaterial;

  const [assignments, setAssignments] = useState([]);
  const [materialRequests, setMaterialRequests] = useState([]);
  const [references, setReferences] = useState(null);
  const [equipments, setEquipments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [createForm, setCreateForm] = useState(emptyCreateForm);
  const [returnForm, setReturnForm] = useState(emptyReturnForm);
  const [materialRequestForm, setMaterialRequestForm] = useState(emptyMaterialRequestForm);
  const [feedback, setFeedback] = useState({ type: "", message: "" });

  const load = async () => {
    setLoading(true);
    try {
      const [assignmentsResponse, referencesResponse, equipmentsResponse, materialRequestsResponse] = await Promise.all([
        http.get("/assignments", { params: { size: 50 } }),
        canManage ? http.get("/references") : Promise.resolve(null),
        canManage ? http.get("/equipments", { params: { size: 100 } }) : Promise.resolve(null),
        canViewMaterialRequests ? http.get("/material-requests") : Promise.resolve(null)
      ]);
      const assignmentsPayload = unwrap(assignmentsResponse);
      setAssignments(pageContent(assignmentsPayload));

      if (canManage) {
        setReferences(unwrap(referencesResponse));
        setEquipments(pageContent(unwrap(equipmentsResponse)));
      } else {
        setReferences(null);
        setEquipments([]);
      }
      setMaterialRequests(canViewMaterialRequests ? unwrap(materialRequestsResponse) || [] : []);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, [canManage, canViewMaterialRequests]);

  const availableEquipments = useMemo(
    () => equipments.filter((item) => item.status === "AVAILABLE" || item.status === "IN_STOCK"),
    [equipments]
  );
  const beneficiaries = references?.beneficiaries || [];
  const canCreateAssignment = canManage && availableEquipments.length > 0 && beneficiaries.length > 0;

  const openAssignments = assignments.filter((item) => item.status === "ACTIVE" || item.status === "PENDING" || item.status === "OVERDUE");
  const pendingMaterialRequests = materialRequests.filter((item) => item.status === "PENDING");

  const submitCreate = async (event) => {
    event.preventDefault();
    setSaving(true);
    setFeedback({ type: "", message: "" });
    try {
      await http.post("/assignments", {
        ...createForm,
        expectedReturnDate: createForm.expectedReturnDate || null
      });
      setCreateForm(emptyCreateForm);
      setFeedback({ type: "success", message: "Affectation creee avec succes." });
      await load();
    } catch (error) {
      setFeedback({ type: "error", message: error.response?.data?.message || "Impossible de creer l'affectation." });
    } finally {
      setSaving(false);
    }
  };

  const approveAssignment = async (assignmentId) => {
    setSaving(true);
    setFeedback({ type: "", message: "" });
    try {
      await http.patch(`/assignments/${assignmentId}/approve`);
      setFeedback({ type: "success", message: "Affectation validee." });
      await load();
    } catch (error) {
      setFeedback({ type: "error", message: error.response?.data?.message || "Impossible de valider l'affectation." });
    } finally {
      setSaving(false);
    }
  };

  const submitReturn = async (event) => {
    event.preventDefault();
    setSaving(true);
    setFeedback({ type: "", message: "" });
    try {
      await http.post(`/assignments/${returnForm.assignmentId}/return`, {
        returnedById: user.id,
        returnedAt: new Date().toISOString().slice(0, 10),
        conditionStatus: returnForm.conditionStatus,
        report: returnForm.report,
        reassignable: returnForm.reassignable
      });
      setReturnForm(emptyReturnForm);
      setFeedback({ type: "success", message: "Retour materiel enregistre." });
      await load();
    } catch (error) {
      setFeedback({ type: "error", message: error.response?.data?.message || "Impossible d'enregistrer le retour." });
    } finally {
      setSaving(false);
    }
  };

  const submitMaterialRequest = async (event) => {
    event.preventDefault();
    setSaving(true);
    setFeedback({ type: "", message: "" });
    try {
      await http.post("/material-requests", materialRequestForm);
      setMaterialRequestForm(emptyMaterialRequestForm);
      setFeedback({ type: "success", message: "Demande de materiel envoyee a l'equipe IT." });
      await load();
    } catch (error) {
      setFeedback({ type: "error", message: error.response?.data?.message || "Impossible d'envoyer la demande de materiel." });
    } finally {
      setSaving(false);
    }
  };

  const reviewMaterialRequest = async (requestId, status) => {
    setSaving(true);
    setFeedback({ type: "", message: "" });
    try {
      await http.patch(`/material-requests/${requestId}/review`, {
        status,
        reviewComment: status === "APPROVED" ? "Demande acceptee par l'equipe IT." : "Demande refusee par l'equipe IT."
      });
      setFeedback({ type: "success", message: "Demande de materiel traitee." });
      await load();
    } catch (error) {
      setFeedback({ type: "error", message: error.response?.data?.message || "Impossible de traiter la demande." });
    } finally {
      setSaving(false);
    }
  };

  return (
    <div>
      <PageHeader
        actions={
          canManage ? (
            <Button onClick={() => document.getElementById("assignment-create-form")?.scrollIntoView({ behavior: "smooth", block: "start" })} type="button">
              <Plus size={17} /> Affectation active
            </Button>
          ) : canRequestMaterial ? (
            <Button onClick={() => document.getElementById("material-request-form")?.scrollIntoView({ behavior: "smooth", block: "start" })} type="button">
              <PackagePlus size={17} /> Demander un materiel
            </Button>
          ) : null
        }
        description={canManage ? "Creation, validation, restitution et demandes de materiel sont reliees aux APIs." : "Consultez vos materiels affectes et demandez un nouveau materiel si besoin."}
        eyebrow="Operations"
        title={canManage ? "Affectations" : "Mes materiels"}
      />

      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <StatCard helper="Toutes les affectations visibles selon votre role" icon={ClipboardCheck} label="Total" value={assignments.length} />
        <StatCard helper="Affectations actuellement en circulation" icon={CheckCircle2} label="Actives" tone="violet" value={assignments.filter((item) => item.status === "ACTIVE").length} />
        <StatCard helper="Dossiers en attente de traitement" icon={RotateCcw} label="A retourner" tone="amber" value={assignments.filter((item) => item.status === "OVERDUE" || item.status === "ACTIVE").length} />
        <StatCard helper={canManage ? "Demandes employees a traiter" : "Demandes envoyees a l'equipe IT"} icon={PackagePlus} label="Demandes" tone="green" value={canManage ? pendingMaterialRequests.length : materialRequests.length} />
      </div>

      {feedback.message && (
        <div className={`mt-5 rounded-2xl px-4 py-3 text-sm font-semibold ${feedback.type === "success" ? "bg-emerald-50 text-emerald-700" : "bg-red-50 text-danger"}`}>
          {feedback.message}
        </div>
      )}

      <div className={`mt-5 grid min-w-0 gap-5 ${canManage ? "xl:grid-cols-[minmax(0,1.15fr)_minmax(380px,0.85fr)]" : "xl:grid-cols-[minmax(390px,0.85fr)_minmax(0,1.15fr)]"}`}>
        <SectionCard className={!canManage ? "order-2" : ""} subtitle="Suivi propre des dotations, validations, signatures et retours prevus." title="Affectations en cours">
          {loading ? (
            <Loader label="Chargement des affectations" />
          ) : (
            <DataTable
              columns={[
                { key: "equipmentName", label: "Equipement" },
                { key: "inventoryCode", label: "Code" },
                { key: "beneficiaryName", label: "Employe beneficiaire" },
                { key: "status", label: "Statut", render: (row) => <StatusBadge value={row.status} /> },
                { key: "approved", label: "Validation", render: (row) => (row.approved ? "Validee" : "En attente") },
                { key: "expectedReturnDate", label: "Retour prevu", render: (row) => formatDate(row.expectedReturnDate) },
                canManage
                  ? {
                      key: "actions",
                      label: "Actions",
                      render: (row) => (
                        <div className="flex flex-wrap gap-2">
                          {!row.approved && (
                            <Button className="h-9 rounded-lg px-3 text-xs" disabled={saving} onClick={() => approveAssignment(row.id)} type="button" variant="secondary">
                              Valider
                            </Button>
                          )}
                          {(row.status === "ACTIVE" || row.status === "OVERDUE") && (
                            <Button
                              className="h-9 rounded-lg px-3 text-xs"
                              disabled={saving}
                              onClick={() => setReturnForm((current) => ({ ...current, assignmentId: row.id }))}
                              type="button"
                              variant="subtle"
                            >
                              Preparer retour
                            </Button>
                          )}
                        </div>
                      )
                    }
                  : null
              ].filter(Boolean)}
              emptyLabel="Aucune affectation"
              minWidth={canManage ? 920 : 680}
              rows={assignments}
            />
          )}
        </SectionCard>

        <div className={`space-y-5 ${!canManage ? "order-1" : ""}`}>
          {canManage ? (
            <>
            <SectionCard subtitle="Demandes deposees par les employes beneficiaires avant affectation." title="Demandes de materiel">
              {loading ? (
                <Loader label="Chargement des demandes" />
              ) : (
                <DataTable
                  columns={[
                    { key: "requesterName", label: "Demandeur" },
                    { key: "materialType", label: "Materiel" },
                    { key: "preferredModel", label: "Preference" },
                    { key: "priority", label: "Priorite", render: (row) => <StatusBadge value={row.priority} /> },
                    { key: "status", label: "Statut", render: (row) => <StatusBadge value={row.status} /> },
                    {
                      key: "actions",
                      label: "Actions",
                      render: (row) =>
                        row.status === "PENDING" ? (
                          <div className="flex flex-wrap gap-2">
                            <Button className="h-9 rounded-lg px-3 text-xs" disabled={saving} onClick={() => reviewMaterialRequest(row.id, "APPROVED")} type="button" variant="secondary">
                              Valider
                            </Button>
                            <Button className="h-9 rounded-lg px-3 text-xs" disabled={saving} onClick={() => reviewMaterialRequest(row.id, "REJECTED")} type="button" variant="subtle">
                              Refuser
                            </Button>
                          </div>
                        ) : (
                          row.reviewedByName || "-"
                        )
                    }
                  ]}
                  emptyLabel="Aucune demande de materiel"
                  minWidth={820}
                  rows={materialRequests}
                />
              )}
            </SectionCard>

            <SectionCard subtitle="Creation reservee aux profils avec gestion des affectations." title="Nouvelle affectation">
              {!canCreateAssignment && (
                <div className="mb-4 rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800">
                  {availableEquipments.length === 0
                    ? "Aucun equipement disponible n'est actuellement reaffectable. Liberez ou retournez un materiel pour creer une nouvelle affectation."
                    : "Aucun employe beneficiaire approuve n'est disponible. Creez ou approuvez d'abord un utilisateur employe pour pouvoir affecter du materiel."}
                </div>
              )}
              <form className="space-y-3" onSubmit={submitCreate}>
                <div id="assignment-create-form" />
                <label className="block">
                  <span className="text-sm font-bold text-ink">Equipement</span>
                  <select
                    className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none"
                    disabled={!canCreateAssignment}
                    onChange={(event) => setCreateForm((current) => ({ ...current, equipmentId: event.target.value }))}
                    required
                    value={createForm.equipmentId}
                  >
                    <option value="">Selectionner</option>
                    {availableEquipments.map((item) => (
                      <option key={item.id} value={item.id}>
                        {item.inventoryCode} - {item.name}
                      </option>
                    ))}
                  </select>
                </label>
                <label className="block">
                  <span className="text-sm font-bold text-ink">Employe beneficiaire</span>
                  <select
                    className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none"
                    disabled={!canCreateAssignment}
                    onChange={(event) => setCreateForm((current) => ({ ...current, beneficiaryId: event.target.value }))}
                    required
                    value={createForm.beneficiaryId}
                  >
                    <option value="">Selectionner</option>
                    {beneficiaries.map((item) => (
                      <option key={item.id} value={item.id}>
                        {item.label}
                      </option>
                    ))}
                  </select>
                </label>
                <div className="grid gap-3 sm:grid-cols-2">
                  <label className="block">
                    <span className="text-sm font-bold text-ink">Date debut</span>
                    <input
                      className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none"
                      disabled={!canCreateAssignment}
                      onChange={(event) => setCreateForm((current) => ({ ...current, startDate: event.target.value }))}
                      required
                      type="date"
                      value={createForm.startDate}
                    />
                  </label>
                  <label className="block">
                    <span className="text-sm font-bold text-ink">Retour prevu</span>
                    <input
                      className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none"
                      disabled={!canCreateAssignment}
                      onChange={(event) => setCreateForm((current) => ({ ...current, expectedReturnDate: event.target.value }))}
                      type="date"
                      value={createForm.expectedReturnDate}
                    />
                  </label>
                </div>
                <label className="block">
                  <span className="text-sm font-bold text-ink">Signature numerique</span>
                  <input
                    className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none"
                    disabled={!canCreateAssignment}
                    onChange={(event) => setCreateForm((current) => ({ ...current, digitalSignature: event.target.value }))}
                    placeholder="Nom, code ou reference de signature"
                    value={createForm.digitalSignature}
                  />
                </label>
                <label className="block">
                  <span className="text-sm font-bold text-ink">Notes</span>
                  <textarea
                    className="mt-1.5 min-h-24 w-full rounded-xl border border-line bg-slate-50 px-3 py-3 text-sm outline-none"
                    disabled={!canCreateAssignment}
                    onChange={(event) => setCreateForm((current) => ({ ...current, notes: event.target.value }))}
                    value={createForm.notes}
                  />
                </label>
                <Button className="w-full" disabled={saving || !canCreateAssignment} type="submit">
                  {saving ? "Enregistrement..." : "Creer l'affectation"}
                </Button>
              </form>
            </SectionCard>
            </>
          ) : (
            <SectionCard subtitle="Precisez le besoin, la priorite et la justification. L'equipe IT validera ensuite la demande." title="Demander un materiel">
              <form className="space-y-3" onSubmit={submitMaterialRequest}>
                <div id="material-request-form" />
                <label className="block">
                  <span className="text-sm font-bold text-ink">Type de materiel</span>
                  <select
                    className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none"
                    onChange={(event) => setMaterialRequestForm((current) => ({ ...current, materialType: event.target.value }))}
                    required
                    value={materialRequestForm.materialType}
                  >
                    <option value="">Selectionner</option>
                    <option value="Ordinateur portable">Ordinateur portable</option>
                    <option value="Ordinateur bureau">Ordinateur bureau</option>
                    <option value="Ecran">Ecran</option>
                    <option value="Clavier / souris">Clavier / souris</option>
                    <option value="Casque">Casque</option>
                    <option value="Autre materiel">Autre materiel</option>
                  </select>
                </label>
                <label className="block">
                  <span className="text-sm font-bold text-ink">Modele souhaite</span>
                  <input
                    className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none"
                    onChange={(event) => setMaterialRequestForm((current) => ({ ...current, preferredModel: event.target.value }))}
                    placeholder="Ex: PC portable 16 Go RAM"
                    value={materialRequestForm.preferredModel}
                  />
                </label>
                <label className="block">
                  <span className="text-sm font-bold text-ink">Priorite</span>
                  <select
                    className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none"
                    onChange={(event) => setMaterialRequestForm((current) => ({ ...current, priority: event.target.value }))}
                    value={materialRequestForm.priority}
                  >
                    <option value="LOW">Basse</option>
                    <option value="MEDIUM">Moyenne</option>
                    <option value="HIGH">Haute</option>
                    <option value="CRITICAL">Critique</option>
                  </select>
                </label>
                <label className="block">
                  <span className="text-sm font-bold text-ink">Justification</span>
                  <textarea
                    className="mt-1.5 min-h-28 w-full rounded-xl border border-line bg-slate-50 px-3 py-3 text-sm outline-none"
                    onChange={(event) => setMaterialRequestForm((current) => ({ ...current, justification: event.target.value }))}
                    placeholder="Expliquez pourquoi ce materiel est necessaire."
                    required
                    value={materialRequestForm.justification}
                  />
                </label>
                <Button className="w-full" disabled={saving} type="submit">
                  <Send size={17} /> {saving ? "Envoi..." : "Envoyer la demande"}
                </Button>
              </form>
            </SectionCard>
          )}

          {!canManage && (
            <SectionCard subtitle="Suivez l'etat de vos demandes envoyees a l'equipe IT." title="Mes demandes de materiel">
              {loading ? (
                <Loader label="Chargement de vos demandes" />
              ) : (
                <DataTable
                  columns={[
                    { key: "materialType", label: "Materiel" },
                    { key: "preferredModel", label: "Preference" },
                    { key: "priority", label: "Priorite", render: (row) => <StatusBadge value={row.priority} /> },
                    { key: "status", label: "Statut", render: (row) => <StatusBadge value={row.status} /> },
                    { key: "reviewComment", label: "Commentaire" }
                  ]}
                  emptyLabel="Aucune demande envoyee"
                  minWidth={640}
                  rows={materialRequests}
                />
              )}
            </SectionCard>
          )}

          {canManage && (
            <SectionCard subtitle="Selectionnez une affectation active pour cloturer le retour." title="Enregistrer un retour">
              <form className="space-y-3" onSubmit={submitReturn}>
                <label className="block">
                  <span className="text-sm font-bold text-ink">Affectation</span>
                  <select
                    className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none"
                    onChange={(event) => setReturnForm((current) => ({ ...current, assignmentId: event.target.value }))}
                    required
                    value={returnForm.assignmentId}
                  >
                    <option value="">Selectionner</option>
                    {openAssignments.map((item) => (
                      <option key={item.id} value={item.id}>
                        {item.inventoryCode} - {item.equipmentName} - {item.beneficiaryName}
                      </option>
                    ))}
                  </select>
                </label>
                <label className="block">
                  <span className="text-sm font-bold text-ink">Etat au retour</span>
                  <select
                    className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none"
                    onChange={(event) => setReturnForm((current) => ({ ...current, conditionStatus: event.target.value }))}
                    value={returnForm.conditionStatus}
                  >
                    <option value="EXCELLENT">Excellent</option>
                    <option value="GOOD">Bon</option>
                    <option value="FAIR">Moyen</option>
                    <option value="DAMAGED">Endommage</option>
                  </select>
                </label>
                <label className="block">
                  <span className="text-sm font-bold text-ink">Rapport</span>
                  <textarea
                    className="mt-1.5 min-h-24 w-full rounded-xl border border-line bg-slate-50 px-3 py-3 text-sm outline-none"
                    onChange={(event) => setReturnForm((current) => ({ ...current, report: event.target.value }))}
                    value={returnForm.report}
                  />
                </label>
                <label className="flex items-center gap-3 rounded-xl bg-slate-50 px-3 py-3 text-sm font-semibold text-ink">
                  <input
                    checked={returnForm.reassignable}
                    onChange={(event) => setReturnForm((current) => ({ ...current, reassignable: event.target.checked }))}
                    type="checkbox"
                  />
                  Materiel reaffectable immediatement
                </label>
                <Button className="w-full" disabled={saving} type="submit" variant="secondary">
                  {saving ? "Traitement..." : "Enregistrer le retour"}
                </Button>
              </form>
            </SectionCard>
          )}
        </div>
      </div>
    </div>
  );
};
