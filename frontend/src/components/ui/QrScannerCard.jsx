import { useEffect, useId, useRef, useState } from "react";
import { ScanLine } from "lucide-react";
import { Html5QrcodeScanner } from "html5-qrcode";
import { Button } from "./Button.jsx";
import { SectionCard } from "./SectionCard.jsx";

export const QrScannerCard = ({ onDetected }) => {
  const regionId = `qr-${useId().replaceAll(":", "")}`;
  const scannerRef = useRef(null);
  const [manualCode, setManualCode] = useState("");
  const [active, setActive] = useState(false);

  useEffect(() => {
    if (!active) return undefined;

    scannerRef.current = new Html5QrcodeScanner(regionId, { fps: 8, qrbox: 220 }, false);
    scannerRef.current.render((decodedText) => {
      onDetected?.(decodedText);
      setActive(false);
    });

    return () => {
      scannerRef.current?.clear().catch(() => undefined);
      scannerRef.current = null;
    };
  }, [active, onDetected, regionId]);

  return (
    <SectionCard
      actions={
        <Button onClick={() => setActive((current) => !current)} type="button" variant="secondary">
          <ScanLine size={17} /> {active ? "Arreter" : "Scanner"}
        </Button>
      }
      subtitle="Lecture QR ou saisie manuelle pour retrouver rapidement une fiche materiel."
      title="Scanner QR"
    >
      {active && <div className="mb-4 overflow-hidden rounded-2xl border border-line bg-slate-50 p-3"><div id={regionId} /></div>}
      <div className="flex flex-col gap-3 sm:flex-row">
        <input
          className="h-10 flex-1 rounded-xl border border-line px-3 text-sm outline-none focus:border-brand-500 focus:ring-4 focus:ring-brand-500/10"
          onChange={(event) => setManualCode(event.target.value)}
          placeholder="Saisir un code inventaire ou QR"
          value={manualCode}
        />
        <Button onClick={() => onDetected?.(manualCode)} type="button">
          Rechercher
        </Button>
      </div>
    </SectionCard>
  );
};
