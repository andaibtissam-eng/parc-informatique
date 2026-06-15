import { useMemo, useState } from "react";
import { ArrowRight, CheckCircle2, KeyRound, MailCheck, ShieldCheck } from "lucide-react";
import { Link, useSearchParams } from "react-router-dom";
import { AuthShell } from "../../components/layout/AuthShell.jsx";
import { Button } from "../../components/ui/Button.jsx";
import { useAuthStore } from "../../features/auth/auth.store.js";

const AuthCard = ({ eyebrow, title, description, children }) => (
  <div className="w-full max-w-[430px] rounded-[1.75rem] border border-white/80 bg-white/85 p-2 shadow-[0_30px_90px_rgba(47,111,237,0.20)] backdrop-blur">
    <div className="rounded-[1.35rem] border border-line/80 bg-white p-5">
      <div className="rounded-2xl bg-gradient-to-br from-brand-50 via-white to-violet-50 p-4">
        <p className="text-xs font-bold uppercase tracking-wider text-brand-600">{eyebrow}</p>
        <h2 className="mt-1 font-display text-xl font-bold text-ink">{title}</h2>
        <p className="mt-1 text-sm leading-6 text-muted">{description}</p>
      </div>
      <div className="mt-4">{children}</div>
    </div>
  </div>
);

export const ForgotPasswordPage = () => {
  const forgotPassword = useAuthStore((state) => state.forgotPassword);
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const submit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");
    try {
      await forgotPassword(email);
      setSuccess("Si le compte est actif, un lien de reinitialisation a ete envoye.");
    } catch (requestError) {
      setError(requestError.response?.data?.message || "Operation impossible");
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthShell
      badge="Account recovery"
      description="Le parcours de recuperation reste dans la meme experience SaaS, sans bascule vers une page HTML legacy."
      title="Recuperez l'acces a votre espace sans quitter l'interface React."
    >
      <AuthCard
        description="Saisissez votre email professionnel pour recevoir un lien de reinitialisation securise."
        eyebrow="Support compte"
        title="Mot de passe oublie"
      >
        <form className="space-y-3" onSubmit={submit}>
          <label className="block">
            <span className="text-sm font-bold text-ink">Email</span>
            <input
              className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none transition focus:border-brand-500 focus:bg-white focus:ring-4 focus:ring-brand-500/10"
              onChange={(event) => setEmail(event.target.value)}
              required
              type="email"
              value={email}
            />
          </label>

          {success && <p className="rounded-xl bg-emerald-50 px-3 py-2 text-sm font-semibold text-emerald-700">{success}</p>}
          {error && <p className="rounded-xl bg-red-50 px-3 py-2 text-sm font-semibold text-danger">{error}</p>}

          <Button className="h-11 w-full rounded-xl" disabled={loading} type="submit">
            {loading ? "Envoi..." : "Envoyer le lien"}
            <ArrowRight size={17} />
          </Button>
        </form>

        <div className="mt-4 flex items-center justify-between gap-3 text-sm">
          <Link className="font-semibold text-brand-700 hover:text-brand-600" to="/login">
            Retour connexion
          </Link>
          <div className="inline-flex items-center gap-2 text-muted">
            <MailCheck size={16} />
            Verifiez aussi vos spams
          </div>
        </div>
      </AuthCard>
    </AuthShell>
  );
};

export const ResetPasswordPage = () => {
  const resetPassword = useAuthStore((state) => state.resetPassword);
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token") || "";
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const tokenState = useMemo(() => (token ? "Token detecte et pret a l'emploi." : "Aucun token detecte dans l'URL."), [token]);

  const submit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");
    try {
      if (!token) {
        throw new Error("Le lien de reinitialisation est incomplet");
      }
      if (password !== confirmPassword) {
        throw new Error("Les mots de passe ne correspondent pas");
      }
      await resetPassword({ token, password });
      setSuccess("Le mot de passe a ete mis a jour. Vous pouvez vous reconnecter.");
      setPassword("");
      setConfirmPassword("");
    } catch (requestError) {
      setError(requestError.response?.data?.message || requestError.message || "Operation impossible");
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthShell
      badge="Secure password reset"
      description="Le reset reste clair, guide et compatible mobile pour ne pas casser l'experience utilisateur premium."
      title="Reinitialisez votre mot de passe dans une interface propre et guidee."
    >
      <AuthCard
        description="Definissez un nouveau mot de passe fort pour reactiver votre acces au workspace."
        eyebrow="Reset"
        title="Nouveau mot de passe"
      >
        <div className="mb-4 inline-flex items-center gap-2 rounded-full bg-brand-50 px-3 py-2 text-xs font-bold text-brand-700">
          <ShieldCheck size={14} />
          {tokenState}
        </div>

        <form className="space-y-3" onSubmit={submit}>
          <label className="block">
            <span className="text-sm font-bold text-ink">Mot de passe</span>
            <input
              className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none transition focus:border-brand-500 focus:bg-white focus:ring-4 focus:ring-brand-500/10"
              minLength={8}
              onChange={(event) => setPassword(event.target.value)}
              required
              type="password"
              value={password}
            />
          </label>
          <label className="block">
            <span className="text-sm font-bold text-ink">Confirmation</span>
            <input
              className="mt-1.5 h-11 w-full rounded-xl border border-line bg-slate-50 px-3 text-sm font-semibold outline-none transition focus:border-brand-500 focus:bg-white focus:ring-4 focus:ring-brand-500/10"
              minLength={8}
              onChange={(event) => setConfirmPassword(event.target.value)}
              required
              type="password"
              value={confirmPassword}
            />
          </label>

          {success && <p className="rounded-xl bg-emerald-50 px-3 py-2 text-sm font-semibold text-emerald-700">{success}</p>}
          {error && <p className="rounded-xl bg-red-50 px-3 py-2 text-sm font-semibold text-danger">{error}</p>}

          <Button className="h-11 w-full rounded-xl" disabled={loading} type="submit">
            {loading ? "Mise a jour..." : "Mettre a jour"}
            <KeyRound size={17} />
          </Button>
        </form>

        <div className="mt-4 text-sm">
          <Link className="font-semibold text-brand-700 hover:text-brand-600" to="/login">
            Retour connexion
          </Link>
        </div>
      </AuthCard>
    </AuthShell>
  );
};

export const VerifyEmailPage = () => {
  const verifyEmail = useAuthStore((state) => state.verifyEmail);
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token") || "";
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const submit = async () => {
    setLoading(true);
    setError("");
    setSuccess("");
    try {
      if (!token) {
        throw new Error("Le lien de verification est incomplet");
      }
      await verifyEmail(token);
      setSuccess("Email verifie. Vous pouvez attendre l'approbation admin puis vous connecter.");
    } catch (requestError) {
      setError(requestError.response?.data?.message || requestError.message || "Operation impossible");
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthShell
      badge="Email verification"
      description="La verification d'email reste integree au meme parcours React pour eviter toute rupture visuelle."
      title="Confirmez votre adresse email avant l'approbation administrateur."
    >
      <AuthCard
        description="Une fois l'email verifie, le compte restera en attente jusqu'a validation par l'administrateur principal."
        eyebrow="Verification"
        title="Verification d'email"
      >
        <div className="rounded-2xl bg-slate-50 p-4">
          <div className="flex items-center gap-3">
            <div className="grid h-10 w-10 place-items-center rounded-xl bg-emerald-50 text-emerald-600">
              <CheckCircle2 size={18} />
            </div>
            <div>
              <p className="text-sm font-bold text-ink">Token de verification</p>
              <p className="text-xs text-muted">{token ? "Lien detecte dans l'URL." : "Aucun token detecte."}</p>
            </div>
          </div>
        </div>

        {success && <p className="mt-4 rounded-xl bg-emerald-50 px-3 py-2 text-sm font-semibold text-emerald-700">{success}</p>}
        {error && <p className="mt-4 rounded-xl bg-red-50 px-3 py-2 text-sm font-semibold text-danger">{error}</p>}

        <div className="mt-4 space-y-3">
          <Button className="h-11 w-full rounded-xl" disabled={loading} onClick={submit} type="button">
            {loading ? "Verification..." : "Verifier mon email"}
            <ArrowRight size={17} />
          </Button>
          <Link className="block text-center text-sm font-semibold text-brand-700 hover:text-brand-600" to="/login">
            Retour connexion
          </Link>
        </div>
      </AuthCard>
    </AuthShell>
  );
};
