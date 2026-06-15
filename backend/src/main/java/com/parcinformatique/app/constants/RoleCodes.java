package com.parcinformatique.app.constants;

public final class RoleCodes {

    public static final String ADMINISTRATOR = "ADMINISTRATOR";
    public static final String RESPONSABLE_INFORMATIQUE = "RESPONSABLE_INFORMATIQUE";
    public static final String TECHNICIEN = "TECHNICIEN";
    public static final String EMPLOYE = "EMPLOYE";

    private RoleCodes() {
    }

    public static String normalizeAlias(String roleName) {
        if (roleName == null) {
            return null;
        }

        String normalized = roleName.trim().toUpperCase();
        if ("BENEFICIAIRE".equals(normalized) || "BENEFICIARY".equals(normalized)) {
            return EMPLOYE;
        }
        if ("GESTIONNAIRE".equals(normalized)) {
            return RESPONSABLE_INFORMATIQUE;
        }
        if ("ADMIN".equals(normalized)) {
            return ADMINISTRATOR;
        }
        return normalized;
    }
}
