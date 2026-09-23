package de.campusflow.fachlogik.gemeinsam;

/**
 * Wird geworfen, wenn die Identitaet des Anfragenden nicht feststeht: Login
 * schlaegt fehl (falsche/unbekannte Zugangsdaten) oder das Bearer-Token
 * fehlt/ist ungueltig/abgelaufen. Bewusst von BerechtigungsFehler getrennt --
 * "wer bist du" (401) ist ein anderer Fall als "ich weiss wer du bist, du
 * darfst das nur nicht" (403, BerechtigungsFehler), siehe openapi-Vertrag
 * (401 bei /auth/login).
 */
public class AuthentifizierungsFehler extends RuntimeException {
    public AuthentifizierungsFehler(String nachricht) {
        super(nachricht);
    }
}
