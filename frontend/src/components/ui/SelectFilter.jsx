export const SelectFilter = ({ value, onChange, options, label }) => (
  <label className="flex min-w-44 flex-col gap-1 text-xs font-bold uppercase text-slate-500">
    {label}
    <select
      className="h-10 rounded-xl border border-line bg-white px-3 text-sm font-semibold normal-case text-ink outline-none focus:border-brand-500 focus:ring-4 focus:ring-brand-500/10"
      onChange={(event) => onChange(event.target.value)}
      value={value}
    >
      {options.map((option) => (
        <option key={option.value} value={option.value}>
          {option.label}
        </option>
      ))}
    </select>
  </label>
);
