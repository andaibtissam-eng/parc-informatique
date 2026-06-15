export const Loader = ({ label = "Chargement", fullScreen = false }) => (
  <div className={fullScreen ? "grid min-h-screen place-items-center bg-canvas" : "grid min-h-48 place-items-center"}>
    <div className="flex items-center gap-3 rounded-xl border border-line bg-white px-4 py-3 shadow-soft">
      <span className="h-4 w-4 animate-spin rounded-full border-2 border-brand-600 border-t-transparent" />
      <span className="text-sm font-semibold text-muted">{label}</span>
    </div>
  </div>
);
