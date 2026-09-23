package de.campusflow.darstellung.gemeinsam;

import de.campusflow.fachlogik.gemeinsam.BerechtigungsFehler;
import de.campusflow.fachlogik.zugangskontrolle.AuthService;
import de.campusflow.fachlogik.zugangskontrolle.Nutzer;
import org.springframework.stereotype.Component;

/**
 * Kleiner Helfer, den alle Controller nutzen, um aus dem Bearer-Token den
 * aufrufenden Nutzer zu ermitteln. Ersetzt bewusst eine volle
 * Spring-Security-Filterkette (aus Aufwandsgruenden fuer diese
 * Uebung) -- das laesst sich spaeter 1:1 gegen echtes Spring Security
 * austauschen, ohne die Controller-Methoden selbst zu aendern.
 */
@Component
public class AktuellerNutzerAufloeser {

    private final AuthService authService;

    public AktuellerNutzerAufloeser(AuthService authService) {
        this.authService = authService;
    }

    public Nutzer aufloesen(String autorisierungsHeader) {
        String token = entferneBearerPraefix(autorisierungsHeader);
        return authService.nutzerFuerToken(token)
                .orElseThrow(() -> new BerechtigungsFehler("Nicht angemeldet oder Token abgelaufen"));
    }

    private String entferneBearerPraefix(String header) {
        if (header == null) {
            throw new BerechtigungsFehler("Authorization-Header fehlt");
        }
        return header.startsWith("Bearer ") ? header.substring(7) : header;
    }
}
