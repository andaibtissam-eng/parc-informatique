export const hasRole = (user, role) => Array.isArray(user?.roles) && user.roles.includes(role);

export const hasAnyRole = (user, roles = []) => roles.length === 0 || roles.some((role) => hasRole(user, role));

export const hasPermission = (user, permission) => Array.isArray(user?.permissions) && user.permissions.includes(permission);

export const hasAnyPermission = (user, permissions = []) =>
  permissions.length === 0 || permissions.some((permission) => hasPermission(user, permission));

export const roleLabel = (role) =>
  (
    {
      ADMINISTRATOR: "Administrateur",
      RESPONSABLE_INFORMATIQUE: "Responsable informatique",
      TECHNICIEN: "Technicien",
      EMPLOYE: "Employe / beneficiaire",
      BENEFICIAIRE: "Employe / beneficiaire"
    }[role] || role
  );

export const registrationRoles = [
  { value: "RESPONSABLE_INFORMATIQUE", label: "Responsable informatique" },
  { value: "TECHNICIEN", label: "Technicien" },
  { value: "EMPLOYE", label: "Employe / beneficiaire" }
];
