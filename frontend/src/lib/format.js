export const unwrap = (response) => response?.data?.data ?? response?.data;

export const pageContent = (payload) => payload?.content ?? payload?.items ?? [];

export const formatDate = (value) => {
  if (!value) return "-";
  return new Intl.DateTimeFormat("fr-FR", { dateStyle: "medium" }).format(new Date(value));
};

export const formatDateTime = (value) => {
  if (!value) return "-";
  return new Intl.DateTimeFormat("fr-FR", { dateStyle: "medium", timeStyle: "short" }).format(new Date(value));
};

export const initials = (name = "PF") =>
  name
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase())
    .join("") || "PF";

export const percent = (value, total) => {
  if (!total) return 0;
  return Math.round((value / total) * 100);
};
