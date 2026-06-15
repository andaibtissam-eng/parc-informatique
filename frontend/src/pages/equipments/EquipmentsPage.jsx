import { useDeferredValue, useEffect, useMemo, useState } from "react";
import { ImageUp, FileText, Pencil, Plus, RefreshCcw } from "lucide-react";
import { http } from "../../api/http.js";
import { Button } from "../../components/ui/Button.jsx";
import { DataTable } from "../../components/ui/DataTable.jsx";
import { Loader } from "../../components/ui/Loader.jsx";
import { PageHeader } from "../../components/ui/PageHeader.jsx";
import { SearchInput } from "../../components/ui/SearchInput.jsx";
import { SectionCard } from "../../components/ui/SectionCard.jsx";
import { StatusBadge } from "../../components/ui/StatusBadge.jsx";
import { getEquipmentPhoto } from "../../data/equipmentPhotos.js";
import { useAuthStore } from "../../features/auth/auth.store.js";
import { hasPermission } from "../../lib/access.js";
import { formatDate, pageContent, unwrap } from "../../lib/format.js";

const emptyForm = {
  name: "",
  brand: "",
  model: "",
  serialNumber: "",
  assetTag: "",
  operatingSystem: "",
  memoryGb: "",
  storageGb: "",
  processor: "",
  purchaseDate: "",
  warrantyEndDate: "",
  acquisitionCost: "",
  status: "AVAILABLE",
  notes: "",
  categoryId: "",
  supplierId: "",
  locationId: "",
  departmentId: ""
};

export const EquipmentsPage = () => {
  const user = useAuthStore((state) => state.user);
  const canManage = hasPermission(user, "equipments.manage");
  const [equipments, setEquipments] = useState([]);
  const [references, setReferences] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [search, setSearch] = useState("");
  const [feedback, setFeedback] = useState({ type: "", message: "" });
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState("");
  const [imageFile, setImageFile] = useState(null);
  const [documentFile, setDocumentFile] = useState(null);
  const deferredSearch = useDeferredValue(search);

  const load = async () => {
    setLoading(true);
    try {
      const requests = [http.get("/equipments", { params: { search: deferredSearch, size: 20 } })];
      if (canManage) {
        requests.push(http.get("/references"));
      }
      const responses = await Promise.all(requests);
      setEquipments(pageContent(unwrap(responses[0])));
      setReferences(canManage ? unwrap(responses[1]) : null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, [deferredSearch, canManage]);

  const availableStatuses = useMemo(
    () => [
      { value: "AVAILABLE", label: "Disponible" },
      { value: "ASSIGNED", label: "Affecte" },
      { value: "MAINTENANCE", label: "En maintenance" },
      { value: "RESERVED", label: "Reserve" },
      { value: "LOST", label: "Perdu" },
      { value: "RETIRED", label: "Retire" }
    ],
    []
  );

  const resetForm = () => {
    setForm(emptyForm);
    setEditingId("");
    setImageFile(null);
    setDocumentFile(null);
  };

  const submit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setFeedback({ type: "", message: "" });

    const payload = {
      ...form,
      memoryGb: form.memoryGb ? Number(form.memoryGb) : null,
      storageGb: form.storageGb ? Number(form.storageGb) : null,
      acquisitionCost: form.acquisitionCost ? Number(form.acquisitionCost) : null,
      purchaseDate: form.purchaseDate || null,
      warrantyEndDate: form.warrantyEndDate || null,
      categoryId: form.categoryId || null,
      supplierId: form.supplierId || null,
      locationId: form.locationId || null,
      departmentId: form.departmentId || null,
      imageUrl: null,
      documentUrl: null
    };

    try {
      const response = editingId
        ? await http.put(`/equipments/${editingId}`, payload)
        : await http.post("/equipments", payload);
      let saved = unwrap(response);

      if (imageFile) {
        const imageData = new FormData();
        imageData.append("file", imageFile);
        saved = unwrap(
          await http.post(`/equipments/${saved.id}/image`, imageData, {
            headers: { "Content-Type": "multipart/form-data" }
          })
        );
      }

      if (documentFile) {
        const documentData = new FormData();
        documentData.append("file", documentFile);
        saved = unwrap(
          await http.post(`/equipments/${saved.id}/document`, documentData, {
            headers: { "Content-Type": "multipart/form-data" }
          })
        );
      }

      setFeedback({
        type: "success",
        message: editingId
          ? `Equipement ${saved.inventoryCode} mis a jour avec succes.`
          : `Equipement ${saved.inventoryCode} ajoute avec succes.`
      });
      resetForm();
      await load();
    } catch (error) {
      setFeedback({ type: "error", message: error.response?.data?.message || "Impossible d'enregistrer l'equipement." });
    } finally {
      setSaving(false);
    }
  };

  const editEquipment = async (equipmentId) => {
    setSaving(true);
    setFeedback({ type: "", message: "" });
    try {
      const response = await http.get(`/equipments/${equipmentId}`);
      const item = unwrap(response);
      setEditingId(item.id);
      setForm({
        name: item.name || "",
        brand: item.brand || "",
        model: item.model || "",
        serialNumber: item.serialNumber || "",
        assetTag: item.assetTag || "",
        operatingSystem: item.operatingSystem || "",
        memoryGb: item.memoryGb ?? "",
        storageGb: item.storageGb ?? "",
        processor: item.processor || "",
        purchaseDate: item.purchaseDate || "",
        warrantyEndDate: item.warrantyEndDate || "",
        acquisitionCost: item.acquisitionCost ?? "",
        status: item.status || "AVAILABLE",
        notes: item.notes || "",
        categoryId: item.categoryId || "",
        supplierId: item.supplierId || "",
        locationId: item.locationId || "",
        departmentId: item.departmentId || ""
      });
      setImageFile(null);
      setDocumentFile(null);
      document.getElementById("equipment-form")?.scrollIntoView({ behavior: "smooth", block: "start" });
    } catch (error) {
      setFeedback({ type: "error", message: error.response?.data?.message || "Impossible de charger cet equipement." });
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="min-w-0">
      <PageHeader
        actions={
          canManage ? (
            <Button onClick={() => document.getElementById("equipment-form")?.scrollIntoView({ behavior: "smooth", block: "start" })} type="button">
              <Plus size={17} /> Nouvel equipement
            </Button>
          ) : null
        }
        description="Un inventaire lisible avec statut, localisation, garantie, photos et code inventaire."
        eyebrow="Inventaire"
        title="Equipements"
      />

      {feedback.message && (
        <div className={`mb-5 rounded-2xl px-4 py-3 text-sm font-semibold ${feedback.type === "success" ? "bg-emerald-50 text-emerald-700" : "bg-red-50 text-danger"}`}>
          {feedback.message}
        </div>
      )}

      <div className="grid min-w-0 gap-5 2xl:grid-cols-[minmax(0,1.15fr)_minmax(430px,0.85fr)]">
        <SectionCard
          actions={<SearchInput onChange={setSearch} placeholder="Rechercher par nom, marque, code..." value={search} />}
          title="Liste des materiels"
          subtitle="Tableau organise pour scanner rapidement l'etat du parc."
        >
          {loading ? (
            <Loader label="Chargement des equipements" />
          ) : (
            <DataTable
              columns={[
                { key: "inventoryCode", label: "Code" },
                {
                  key: "name",
                  label: "Materiel",
                  render: (row, index) => (
                    <div className="flex items-center gap-3">
                      <div className="grid h-14 w-16 place-items-center overflow-hidden rounded-xl bg-slate-100 ring-1 ring-line">
                        <img
                          alt={`${row.name || "Materiel"} photo`}
                          className="h-full w-full object-cover"
                          loading="lazy"
                          src={getEquipmentPhoto(row, index)}
                        />
                      </div>
                      <div>
                        <p className="font-semibold text-ink">{row.name}</p>
                        <p className="text-xs text-muted">{[row.brand, row.model].filter(Boolean).join(" ") || "Details non renseignes"}</p>
                        {row.currentAssignee && <p className="text-[11px] font-semibold text-violet-600">Affecte a {row.currentAssignee}</p>}
                      </div>
                    </div>
                  )
                },
                { key: "category", label: "Categorie" },
                { key: "location", label: "Localisation" },
                { key: "status", label: "Statut", render: (row) => <StatusBadge value={row.status} /> },
                { key: "warrantyEndDate", label: "Garantie", render: (row) => formatDate(row.warrantyEndDate) },
                canManage
                  ? {
                      key: "actions",
                      label: "Actions",
                      render: (row) => (
                        <Button className="h-9 rounded-lg px-3 text-xs" onClick={() => editEquipment(row.id)} type="button" variant="secondary">
                          <Pencil size={14} /> Modifier
                        </Button>
                      )
                    }
                  : null
              ].filter(Boolean)}
              emptyLabel="Aucun equipement trouve"
              minWidth={980}
              rows={equipments}
            />
          )}
        </SectionCard>

        {canManage && (
          <SectionCard
            subtitle="Ajout et mise a jour reels via Spring Boot, avec photo et document PDF televerses apres creation."
            title={editingId ? "Modifier un equipement" : "Ajouter un equipement"}
          >
            <form className="space-y-3" onSubmit={submit}>
              <div id="equipment-form" />
              <div className="grid gap-3 sm:grid-cols-2">
                <label className="block">
                  <span className="text-sm font-bold text-ink">Nom</span>
                  <input className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none" onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))} required value={form.name} />
                </label>
                <label className="block">
                  <span className="text-sm font-bold text-ink">Statut</span>
                  <select className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none" onChange={(event) => setForm((current) => ({ ...current, status: event.target.value }))} value={form.status}>
                    {availableStatuses.map((item) => (
                      <option key={item.value} value={item.value}>
                        {item.label}
                      </option>
                    ))}
                  </select>
                </label>
              </div>

              <div className="grid gap-3 sm:grid-cols-2">
                <label className="block">
                  <span className="text-sm font-bold text-ink">Marque</span>
                  <input className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none" onChange={(event) => setForm((current) => ({ ...current, brand: event.target.value }))} value={form.brand} />
                </label>
                <label className="block">
                  <span className="text-sm font-bold text-ink">Modele</span>
                  <input className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none" onChange={(event) => setForm((current) => ({ ...current, model: event.target.value }))} value={form.model} />
                </label>
              </div>

              <div className="grid gap-3 sm:grid-cols-2">
                <label className="block">
                  <span className="text-sm font-bold text-ink">Numero de serie</span>
                  <input className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none" onChange={(event) => setForm((current) => ({ ...current, serialNumber: event.target.value }))} value={form.serialNumber} />
                </label>
                <label className="block">
                  <span className="text-sm font-bold text-ink">Asset tag</span>
                  <input className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none" onChange={(event) => setForm((current) => ({ ...current, assetTag: event.target.value }))} value={form.assetTag} />
                </label>
              </div>

              <div className="grid gap-3 sm:grid-cols-3">
                <label className="block">
                  <span className="text-sm font-bold text-ink">RAM (Go)</span>
                  <input className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none" min="0" onChange={(event) => setForm((current) => ({ ...current, memoryGb: event.target.value }))} type="number" value={form.memoryGb} />
                </label>
                <label className="block">
                  <span className="text-sm font-bold text-ink">Stockage (Go)</span>
                  <input className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none" min="0" onChange={(event) => setForm((current) => ({ ...current, storageGb: event.target.value }))} type="number" value={form.storageGb} />
                </label>
                <label className="block">
                  <span className="text-sm font-bold text-ink">Cout</span>
                  <input className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none" min="0" onChange={(event) => setForm((current) => ({ ...current, acquisitionCost: event.target.value }))} step="0.01" type="number" value={form.acquisitionCost} />
                </label>
              </div>

              <label className="block">
                <span className="text-sm font-bold text-ink">Systeme d'exploitation</span>
                <input className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none" onChange={(event) => setForm((current) => ({ ...current, operatingSystem: event.target.value }))} value={form.operatingSystem} />
              </label>

              <label className="block">
                <span className="text-sm font-bold text-ink">Processeur</span>
                <input className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none" onChange={(event) => setForm((current) => ({ ...current, processor: event.target.value }))} value={form.processor} />
              </label>

              <div className="grid gap-3 sm:grid-cols-2">
                <label className="block">
                  <span className="text-sm font-bold text-ink">Categorie</span>
                  <select className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none" onChange={(event) => setForm((current) => ({ ...current, categoryId: event.target.value }))} value={form.categoryId}>
                    <option value="">Selectionner</option>
                    {(references?.categories || []).map((item) => (
                      <option key={item.id} value={item.id}>
                        {item.label}
                      </option>
                    ))}
                  </select>
                </label>
                <label className="block">
                  <span className="text-sm font-bold text-ink">Fournisseur</span>
                  <select className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none" onChange={(event) => setForm((current) => ({ ...current, supplierId: event.target.value }))} value={form.supplierId}>
                    <option value="">Selectionner</option>
                    {(references?.suppliers || []).map((item) => (
                      <option key={item.id} value={item.id}>
                        {item.label}
                      </option>
                    ))}
                  </select>
                </label>
              </div>

              <div className="grid gap-3 sm:grid-cols-2">
                <label className="block">
                  <span className="text-sm font-bold text-ink">Localisation</span>
                  <select className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none" onChange={(event) => setForm((current) => ({ ...current, locationId: event.target.value }))} value={form.locationId}>
                    <option value="">Selectionner</option>
                    {(references?.locations || []).map((item) => (
                      <option key={item.id} value={item.id}>
                        {item.label}
                      </option>
                    ))}
                  </select>
                </label>
                <label className="block">
                  <span className="text-sm font-bold text-ink">Departement</span>
                  <select className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none" onChange={(event) => setForm((current) => ({ ...current, departmentId: event.target.value }))} value={form.departmentId}>
                    <option value="">Selectionner</option>
                    {(references?.departments || []).map((item) => (
                      <option key={item.id} value={item.id}>
                        {item.label}
                      </option>
                    ))}
                  </select>
                </label>
              </div>

              <div className="grid gap-3 sm:grid-cols-2">
                <label className="block">
                  <span className="text-sm font-bold text-ink">Date achat</span>
                  <input className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none" onChange={(event) => setForm((current) => ({ ...current, purchaseDate: event.target.value }))} type="date" value={form.purchaseDate} />
                </label>
                <label className="block">
                  <span className="text-sm font-bold text-ink">Fin garantie</span>
                  <input className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none" onChange={(event) => setForm((current) => ({ ...current, warrantyEndDate: event.target.value }))} type="date" value={form.warrantyEndDate} />
                </label>
              </div>

              <label className="block">
                <span className="text-sm font-bold text-ink">Notes</span>
                <textarea className="mt-1.5 min-h-24 w-full rounded-xl border border-line bg-slate-50 px-3 py-3 text-sm outline-none" onChange={(event) => setForm((current) => ({ ...current, notes: event.target.value }))} value={form.notes} />
              </label>

              <div className="grid gap-3 sm:grid-cols-2">
                <label className="block">
                  <span className="mb-1 inline-flex items-center gap-2 text-sm font-bold text-ink"><ImageUp size={15} /> Photo reelle</span>
                  <input accept="image/*" className="block w-full rounded-xl border border-dashed border-line bg-slate-50 px-3 py-3 text-sm text-slate-600" onChange={(event) => setImageFile(event.target.files?.[0] || null)} type="file" />
                </label>
                <label className="block">
                  <span className="mb-1 inline-flex items-center gap-2 text-sm font-bold text-ink"><FileText size={15} /> Document PDF</span>
                  <input accept="application/pdf" className="block w-full rounded-xl border border-dashed border-line bg-slate-50 px-3 py-3 text-sm text-slate-600" onChange={(event) => setDocumentFile(event.target.files?.[0] || null)} type="file" />
                </label>
              </div>

              <div className="flex flex-wrap gap-2">
                <Button className="flex-1" disabled={saving} type="submit">
                  {saving ? "Enregistrement..." : editingId ? "Mettre a jour l'equipement" : "Ajouter l'equipement"}
                </Button>
                <Button className="h-11 px-4" disabled={saving} onClick={resetForm} type="button" variant="secondary">
                  <RefreshCcw size={15} /> Reinitialiser
                </Button>
              </div>
            </form>
          </SectionCard>
        )}
      </div>
    </div>
  );
};
