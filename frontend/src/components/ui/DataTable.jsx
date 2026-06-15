export const DataTable = ({ columns, rows, emptyLabel = "Aucune donnee disponible", minWidth = 720 }) => {
  if (!rows?.length) {
    return (
      <div className="rounded-2xl border border-dashed border-line bg-slate-50 px-6 py-12 text-center">
        <p className="text-sm font-semibold text-ink">{emptyLabel}</p>
        <p className="mt-1 text-sm text-muted">Les donnees apparaitront ici des qu'elles seront disponibles.</p>
      </div>
    );
  }

  return (
    <div className="min-w-0 max-w-full overflow-hidden rounded-2xl border border-line bg-white">
      <div className="max-w-full overflow-x-auto overscroll-x-contain">
        <table className="w-full table-auto text-left text-sm" style={{ minWidth: `${minWidth}px` }}>
          <thead className="bg-slate-50 text-xs uppercase text-slate-500">
            <tr>
              {columns.map((column) => (
                <th key={column.key} className="whitespace-nowrap px-4 py-3 font-bold">
                  {column.label}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-line">
            {rows.map((row, index) => (
              <tr key={row.id || index} className="transition hover:bg-slate-50">
                {columns.map((column) => (
                  <td key={column.key} className="max-w-[320px] px-4 py-3 align-middle text-slate-700">
                    {column.render ? column.render(row, index) : <span className="block truncate">{row[column.key] ?? "-"}</span>}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
