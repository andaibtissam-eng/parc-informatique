import clsx from "clsx";

const variants = {
  primary: "bg-gradient-to-r from-brand-600 to-violet-600 text-white shadow-pop hover:brightness-105",
  secondary: "border border-line bg-white text-ink shadow-sm hover:bg-slate-50",
  subtle: "bg-slate-100 text-slate-700 hover:bg-slate-200",
  danger: "bg-red-600 text-white hover:bg-red-700"
};

export const Button = ({ as: Component = "button", variant = "primary", className, children, ...props }) => (
  <Component
    className={clsx(
      "inline-flex h-11 items-center justify-center gap-2 rounded-xl px-4 text-sm font-bold transition focus:outline-none focus:ring-4 focus:ring-brand-500/15",
      variants[variant] || variants.primary,
      className
    )}
    {...props}
  >
    {children}
  </Component>
);
