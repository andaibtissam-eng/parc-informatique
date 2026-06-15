import { Search } from "lucide-react";

export const SearchInput = ({ value, onChange, placeholder = "Rechercher..." }) => (
  <div className="relative w-full sm:max-w-md">
    <Search className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={17} />
    <input
      className="h-10 w-full rounded-xl border border-line bg-white pl-10 pr-3 text-sm text-ink outline-none transition placeholder:text-slate-400 focus:border-brand-500 focus:ring-4 focus:ring-brand-500/10"
      onChange={(event) => onChange(event.target.value)}
      placeholder={placeholder}
      value={value}
    />
  </div>
);
