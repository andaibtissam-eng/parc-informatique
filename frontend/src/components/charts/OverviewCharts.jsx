import { ArcElement, BarElement, CategoryScale, Chart as ChartJS, Legend, LinearScale, Tooltip } from "chart.js";
import { Bar, Doughnut } from "react-chartjs-2";
import { SectionCard } from "../ui/SectionCard.jsx";

ChartJS.register(ArcElement, BarElement, CategoryScale, LinearScale, Tooltip, Legend);

export const OverviewCharts = ({ labels = [], values = [] }) => {
  const chartLabels = labels.length ? labels : ["Disponibles", "Affectes", "Maintenance"];
  const chartValues = values.length ? values : [0, 0, 0];

  return (
    <div className="grid gap-5 xl:grid-cols-[0.9fr_1.1fr]">
      <SectionCard title="Etat du parc" subtitle="Repartition claire des equipements selon leur disponibilite.">
        <div className="mx-auto max-w-xs">
          <Doughnut
            data={{
              labels: chartLabels,
              datasets: [
                {
                  data: chartValues,
                  backgroundColor: ["#2f6fed", "#7c5cff", "#16a34a", "#d97706"],
                  borderWidth: 0
                }
              ]
            }}
            options={{
              cutout: "68%",
              plugins: {
                legend: { position: "bottom", labels: { boxWidth: 10, color: "#667085", font: { family: "Inter" } } }
              }
            }}
          />
        </div>
      </SectionCard>

      <SectionCard title="Vue operationnelle" subtitle="Comparaison rapide pour lire les tendances sans surcharger le dashboard.">
        <Bar
          data={{
            labels: chartLabels,
            datasets: [
              {
                label: "Equipements",
                data: chartValues,
                backgroundColor: "#2f6fed",
                borderRadius: 8,
                maxBarThickness: 44
              }
            ]
          }}
          options={{
            responsive: true,
            plugins: {
              legend: { display: false }
            },
            scales: {
              x: { grid: { display: false }, ticks: { color: "#667085", font: { family: "Inter" } } },
              y: { grid: { color: "#eef1f5" }, ticks: { color: "#667085", precision: 0, font: { family: "Inter" } } }
            }
          }}
        />
      </SectionCard>
    </div>
  );
};
