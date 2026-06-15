import { useState } from "react";
import { ArrowRight, LogIn, UserPlus } from "lucide-react";
import clsx from "clsx";
import { Link } from "react-router-dom";
import { AuthShell } from "../../components/layout/AuthShell.jsx";
import { Button } from "../../components/ui/Button.jsx";
import { useAuthStore } from "../../features/auth/auth.store.js";
import { registrationRoles, roleLabel } from "../../lib/access.js";

const emptyRegisterForm = {
  firstName: "",
  lastName: "",
  email: "",
  phone: "",
  departmentCode: "DSI",
  role: "EMPLOYE",
  password: ""
};

const TabButton = ({ active, children, icon: Icon, onClick }) => (
  <button
    className={clsx(
      "flex h-10 flex-1 items-center justify-center gap-2 rounded-xl text-sm font-bold transition",
      active ? "bg-white text-brand-700 shadow-sm" : "text-slate-500 hover:text-ink"
    )}
    onClick={onClick}
    type="button"
  >
    <Icon size={16} />
    {children}
  </button>
);

export const LoginPage = ({ initialMode = "login" }) => {
  const login = useAuthStore((state) => state.login);
  const register = useAuthStore((state) => state.register);
  const [mode, setMode] = useState(initialMode);
  const [loginForm, setLoginForm] = useState({ email: "admin@emsi.ma", password: "Admin123*" });
  const [registerForm, setRegisterForm] = useState(emptyRegisterForm);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const isRegister = mode === "register";

  const submit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");

    try {
      if (isRegister) {
        const registration = await register(registerForm);
        setSuccess(
          `Inscription enregistree pour ${registration.email}. Verifiez votre email puis attendez l'approbation de l'administrateur avant de vous connecter.`
        );
        setLoginForm((current) => ({ ...current, email: registerForm.email }));
        setRegisterForm(emptyRegisterForm);
        setMode("login");
      } else {
        await login(loginForm);
      }
    } catch (authError) {
      setError(authError.response?.data?.message || "Operation impossible");
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthShell
      badge="Controlled enterprise access"
      description="Chaque utilisateur rejoint le workspace avec un role metier precise, un circuit d'approbation et une gouvernance d'acces digne d'une vraie application SaaS."
      title="Une plateforme claire pour organiser le parc IT avec validation administrateur."
    >
      <div className="w-full max-w-[430px] rounded-[1.75rem] border border-white/80 bg-white/85 p-2 shadow-[0_30px_90px_rgba(47,111,237,0.20)] backdrop-blur">
        <div className="rounded-[1.35rem] border border-line/80 bg-white p-4">
          <div className="rounded-2xl bg-slate-100 p-1">
            <div className="flex gap-1">
              <TabButton active={!isRegister} icon={LogIn} onClick={() => setMode("login")}>
                Connexion
              </TabButton>
              <TabButton active={isRegister} icon={UserPlus} onClick={() => setMode("register")}>
                Inscription
              </TabButton>
            </div>
          </div>

          <div className="my-4 rounded-2xl bg-gradient-to-br from-brand-50 via-white to-violet-50 p-4">
            <p className="text-xs font-bold uppercase tracking-wider text-brand-600">{isRegister ? "Nouveau compte" : "Connexion"}</p>
            <h2 className="mt-1 font-display text-xl font-bold text-ink">
              {isRegister ? "Demander un acces metier" : "Acceder au workspace"}
            </h2>
            <p className="mt-1 text-sm leading-6 text-muted">
              {isRegister
                ? "Choisissez votre role metier. Le role employe correspond au beneficiaire final du materiel. Le role administrateur reste reserve au compte principal cree par le systeme."
                : "Connectez-vous avec un compte approuve et verifie."}
            </p>
          </div>

          <form className="space-y-3" onSubmit={submit}>
            {isRegister ? (
              <>
                <div className="grid gap-3 sm:grid-cols-2">
                  <label className="block">
                    <span className="text-sm font-bold text-ink">Prenom</span>
                    <input
                      className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none transition focus:border-brand-500 focus:bg-white focus:ring-4 focus:ring-brand-500/10"
                      onChange={(event) => setRegisterForm((current) => ({ ...current, firstName: event.target.value }))}
                      required
                      value={registerForm.firstName}
                    />
                  </label>
                  <label className="block">
                    <span className="text-sm font-bold text-ink">Nom</span>
                    <input
                      className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none transition focus:border-brand-500 focus:bg-white focus:ring-4 focus:ring-brand-500/10"
                      onChange={(event) => setRegisterForm((current) => ({ ...current, lastName: event.target.value }))}
                      required
                      value={registerForm.lastName}
                    />
                  </label>
                </div>
                <label className="block">
                  <span className="text-sm font-bold text-ink">Email</span>
                  <input
                    className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none transition focus:border-brand-500 focus:bg-white focus:ring-4 focus:ring-brand-500/10"
                    onChange={(event) => setRegisterForm((current) => ({ ...current, email: event.target.value }))}
                    required
                    type="email"
                    value={registerForm.email}
                  />
                </label>
                <div className="grid gap-3 sm:grid-cols-2">
                  <label className="block">
                    <span className="text-sm font-bold text-ink">Telephone</span>
                    <input
                      className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none transition focus:border-brand-500 focus:bg-white focus:ring-4 focus:ring-brand-500/10"
                      onChange={(event) => setRegisterForm((current) => ({ ...current, phone: event.target.value }))}
                      value={registerForm.phone}
                    />
                  </label>
                  <label className="block">
                    <span className="text-sm font-bold text-ink">Departement</span>
                    <input
                      className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none transition focus:border-brand-500 focus:bg-white focus:ring-4 focus:ring-brand-500/10"
                      onChange={(event) => setRegisterForm((current) => ({ ...current, departmentCode: event.target.value }))}
                      value={registerForm.departmentCode}
                    />
                  </label>
                </div>
                <label className="block">
                  <span className="text-sm font-bold text-ink">Role demande</span>
                  <select
                    className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none transition focus:border-brand-500 focus:bg-white focus:ring-4 focus:ring-brand-500/10"
                    onChange={(event) => setRegisterForm((current) => ({ ...current, role: event.target.value }))}
                    value={registerForm.role}
                  >
                    {registrationRoles.map((role) => (
                      <option key={role.value} value={role.value}>
                        {role.label}
                      </option>
                    ))}
                  </select>
                  <p className="mt-1 text-xs text-muted">Selection actuelle : {roleLabel(registerForm.role)}</p>
                </label>
                <label className="block">
                  <span className="text-sm font-bold text-ink">Mot de passe</span>
                  <input
                    className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none transition focus:border-brand-500 focus:bg-white focus:ring-4 focus:ring-brand-500/10"
                    minLength={8}
                    onChange={(event) => setRegisterForm((current) => ({ ...current, password: event.target.value }))}
                    required
                    type="password"
                    value={registerForm.password}
                  />
                </label>
              </>
            ) : (
              <>
                <label className="block">
                  <span className="text-sm font-bold text-ink">Email</span>
                  <input
                    className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none transition focus:border-brand-500 focus:bg-white focus:ring-4 focus:ring-brand-500/10"
                    onChange={(event) => setLoginForm((current) => ({ ...current, email: event.target.value }))}
                    type="email"
                    value={loginForm.email}
                  />
                </label>
                <label className="block">
                  <span className="text-sm font-bold text-ink">Mot de passe</span>
                  <input
                    className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none transition focus:border-brand-500 focus:bg-white focus:ring-4 focus:ring-brand-500/10"
                    onChange={(event) => setLoginForm((current) => ({ ...current, password: event.target.value }))}
                    type="password"
                    value={loginForm.password}
                  />
                </label>
                <div className="flex items-center justify-between gap-3 text-sm">
                  <Link className="font-semibold text-brand-700 hover:text-brand-600" to="/forgot-password">
                    Mot de passe oublie ?
                  </Link>
                  <button
                    className="font-semibold text-violet-600 transition hover:text-violet-500"
                    onClick={() => setMode("register")}
                    type="button"
                  >
                    Creer un compte
                  </button>
                </div>
              </>
            )}

            {success && <p className="rounded-xl bg-emerald-50 px-3 py-2 text-sm font-semibold text-emerald-700">{success}</p>}
            {error && <p className="rounded-xl bg-red-50 px-3 py-2 text-sm font-semibold text-danger">{error}</p>}

            <Button className="h-11 w-full rounded-xl" disabled={loading} type="submit">
              {loading ? "Traitement..." : isRegister ? "Soumettre la demande" : "Se connecter"}
              <ArrowRight size={17} />
            </Button>
          </form>

          <div className="mt-4 border-t border-line pt-4 text-sm text-muted">
            {isRegister ? (
              <p>
                Deja inscrit ?{" "}
                <button className="font-semibold text-brand-700 hover:text-brand-600" onClick={() => setMode("login")} type="button">
                  Revenir a la connexion
                </button>
              </p>
            ) : (
              <p>
                L'admin principal est cree automatiquement par le systeme. Les autres roles passent par l'inscription et l'approbation.
              </p>
            )}
          </div>
        </div>
      </div>
    </AuthShell>
  );
};
