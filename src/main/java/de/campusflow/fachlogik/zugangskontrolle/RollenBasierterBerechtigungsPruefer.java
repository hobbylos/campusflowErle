package de.campusflow.fachlogik.zugangskontrolle;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Konkrete Implementierung von BerechtigungsPruefer.
 * Die Rechtematrix ist als sinnvoller Platzhalter hart hinterlegt -- fachlich
 * muss das Team festlegen, welche Rolle welche Aktion darf (EC-4-Frage
 * "Welche Berechtigungen besitzen die Rollen?").
 *
 * NUTZER darf Buchungen aendern/stornieren (EC-3) -- die Einschraenkung auf
 * die EIGENE Buchung ist keine Rollenfrage mehr, sondern eine
 * Eigentuemer-Pruefung, die BuchungService zusaetzlich zu dieser
 * rollenbasierten Pruefung durchfuehrt.
 */
@Component
public class RollenBasierterBerechtigungsPruefer implements BerechtigungsPruefer {

    private final Map<Rolle, Set<Aktion>> rollenBerechtigungen = Map.of(
            Rolle.ADMIN, EnumSet.allOf(Aktion.class),
            Rolle.NUTZER, EnumSet.of(Aktion.BUCHUNG_ANLEGEN, Aktion.BUCHUNG_AENDERN, Aktion.BUCHUNG_STORNIEREN)
    );

    @Override
    public boolean darf(Nutzer nutzer, Aktion aktion) {
        return nutzer.getRollen().stream()
                .anyMatch(rolle -> rollenBerechtigungen
                        .getOrDefault(rolle, Set.of())
                        .contains(aktion));
    }
}
