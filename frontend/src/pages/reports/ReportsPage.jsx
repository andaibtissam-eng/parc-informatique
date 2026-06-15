import { Download, FileSpreadsheet, FileText } from "lucide-react";
import { Button } from "../../components/ui/Button.jsx";
import { PageHeader } from "../../components/ui/PageHeader.jsx";
import { SectionCard } from "../../components/ui/SectionCard.jsx";

const reports = [
  {
    title: "Inventaire PDF",
    description: "Rapport propre pour impression, partage et presentation au management.",
    href: "/api/reports/inventory.pdf",
    icon: FileText
  },
  {
    title: "Export Excel",
    description: "Extraction structuree pour analyse, audit et rapprochement des donnees.",
    href: "/api/reports/inventory.xlsx",
    icon: FileSpreadsheet
  }
];

export const ReportsPage = () => (
  <div>
    <PageHeader
      description="Une page simple pour generer les exports utiles sans disperser les actions."
      eyebrow="Exports"
      title="Rapports"
    />
    <div className="grid gap-5 md:grid-cols-2">
      {reports.map(({ title, description, href, icon: Icon }) => (
        <SectionCard key={title}>
          <div className="flex items-start gap-4">
            <div className="grid h-11 w-11 place-items-center rounded-xl bg-brand-50 text-brand-600">
              <Icon size={21} />
            </div>
            <div>
              <h2 className="font-display text-lg font-bold text-ink">{title}</h2>
              <p className="mt-2 text-sm leading-6 text-muted">{description}</p>
              <Button as="a" className="mt-5" href={href}>
                <Download size={17} /> Telecharger
              </Button>
            </div>
          </div>
        </SectionCard>
      ))}
    </div>
  </div>
);
