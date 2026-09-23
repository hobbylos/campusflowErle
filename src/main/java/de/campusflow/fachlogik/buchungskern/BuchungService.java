package de.campusflow.fachlogik.buchungskern;

import de.campusflow.datenhaltung.buchungskern.BestaetigungsVersender;
import de.campusflow.datenhaltung.buchungskern.BuchungRepository;
import de.campusflow.datenhaltung.raumkatalog.RaumRepository;
import de.campusflow.fachlogik.gemeinsam.BerechtigungsFehler;
import de.campusflow.fachlogik.gemeinsam.KonfliktFehler;
import de.campusflow.fachlogik.gemeinsam.NichtGefundenFehler;
import de.campusflow.fachlogik.raumkatalog.Raum;
import de.campusflow.fachlogik.zugangskontrolle.Aktion;
import de.campusflow.fachlogik.zugangskontrolle.BerechtigungsPruefer;
import de.campusflow.fachlogik.zugangskontrolle.Nutzer;
import de.campusflow.fachlogik.zugangskontrolle.Rolle;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Orchestriert EC-11 (Raum buchen), EC-12 (Doppelbuchungen verhindern),
 * EC-13 (Buchungsbestaetigung) sowie Buchung aendern/stornieren (im
 * openapi-Vertrag als PATCH/DELETE /buchungen/{buchungId} vorgesehen).
 * Greift auf RaumRepository lesend zu, um den Sperrstatus zu pruefen -- das
 * ist die im Modulschnitt (Aufgabe C) als Abhaengigkeit
 * Buchungskern -> Raumkatalog vorgesehene Kopplung.
 */
@Service
public class BuchungService {

    private final BuchungRepository buchungRepository;
    private final RaumRepository raumRepository;
    private final BestaetigungsVersender bestaetigungsVersender;
    private final BerechtigungsPruefer berechtigungsPruefer;

    public BuchungService(BuchungRepository buchungRepository,
                           RaumRepository raumRepository,
                           BestaetigungsVersender bestaetigungsVersender,
                           BerechtigungsPruefer berechtigungsPruefer) {
        this.buchungRepository = buchungRepository;
        this.raumRepository = raumRepository;
        this.bestaetigungsVersender = bestaetigungsVersender;
        this.berechtigungsPruefer = berechtigungsPruefer;
    }

    /** EC-11 + EC-12 (Konfliktpruefung) + EC-13 (Bestaetigung). */
    public Buchung buchungAnlegen(BuchungDaten daten, Nutzer nutzer) {
        if (!berechtigungsPruefer.darf(nutzer, Aktion.BUCHUNG_ANLEGEN)) {
            throw new BerechtigungsFehler("Keine Berechtigung zum Buchen");
        }

        Raum raum = raumRepository.findeNachId(daten.raumId())
                .orElseThrow(() -> new NichtGefundenFehler("Raum nicht gefunden: " + daten.raumId()));

        if (raum.istGesperrtAm(daten.von()) || raum.istGesperrtAm(daten.bis())) {
            throw new KonfliktFehler("Raum ist im gewuenschten Zeitraum gesperrt");
        }

        List<Buchung> ueberlappende = buchungRepository.findeUeberlappende(daten.raumId(), daten.von(), daten.bis());
        if (!ueberlappende.isEmpty()) {
            // EC-12 Akzeptanzkriterium: keine Speicherung der Konfliktbuchung
            throw new KonfliktFehler("Raum ist im gewuenschten Zeitraum bereits belegt");
        }

        Buchung buchung = new Buchung(UUID.randomUUID().toString(), daten.raumId(), nutzer.getId(),
                daten.von(), daten.bis(), daten.zweck());
        buchungRepository.speichern(buchung);
        bestaetigungsVersender.bestaetigungSenden(buchung);
        return buchung;
    }

    /**
     * PATCH /buchungen/{buchungId}: Zeitraum einer Buchung aendern. Laesst
     * ein weggelassenes (null) Feld unveraendert (siehe BuchungAenderung).
     * Nur die eigene Buchung darf geaendert werden, ausser man ist ADMIN
     * (openapi 403 "nicht eigene Buchung").
     */
    public Buchung buchungAendern(String buchungId, BuchungAenderung aenderung, Nutzer nutzer) {
        if (!berechtigungsPruefer.darf(nutzer, Aktion.BUCHUNG_AENDERN)) {
            throw new BerechtigungsFehler("Keine Berechtigung zum Aendern von Buchungen");
        }

        Buchung buchung = buchungRepository.findeNachId(buchungId)
                .orElseThrow(() -> new NichtGefundenFehler("Buchung nicht gefunden: " + buchungId));

        if (!istEigentuemerOderAdmin(buchung, nutzer)) {
            throw new BerechtigungsFehler("Nur die eigene Buchung darf geaendert werden");
        }

        Instant neuesVon = aenderung.von() != null ? aenderung.von() : buchung.getVon();
        Instant neuesBis = aenderung.bis() != null ? aenderung.bis() : buchung.getBis();

        Raum raum = raumRepository.findeNachId(buchung.getRaumId())
                .orElseThrow(() -> new NichtGefundenFehler("Raum nicht gefunden: " + buchung.getRaumId()));
        if (raum.istGesperrtAm(neuesVon) || raum.istGesperrtAm(neuesBis)) {
            throw new KonfliktFehler("Raum ist im gewuenschten Zeitraum gesperrt");
        }

        List<Buchung> ueberlappende = buchungRepository.findeUeberlappende(
                buchung.getRaumId(), neuesVon, neuesBis, buchung.getId());
        if (!ueberlappende.isEmpty()) {
            throw new KonfliktFehler("Neuer Zeitraum kollidiert mit einer anderen Buchung");
        }

        buchung.zeitraumAendern(neuesVon, neuesBis);
        buchungRepository.speichern(buchung);
        return buchung;
    }

    /**
     * DELETE /buchungen/{buchungId}: Buchung stornieren. Setzt nur den
     * Status auf STORNIERT (siehe BuchungStatus) -- die Buchung bleibt
     * nachvollziehbar erhalten, statt geloescht zu werden.
     */
    public void buchungStornieren(String buchungId, Nutzer nutzer) {
        if (!berechtigungsPruefer.darf(nutzer, Aktion.BUCHUNG_STORNIEREN)) {
            throw new BerechtigungsFehler("Keine Berechtigung zum Stornieren von Buchungen");
        }

        Buchung buchung = buchungRepository.findeNachId(buchungId)
                .orElseThrow(() -> new NichtGefundenFehler("Buchung nicht gefunden: " + buchungId));

        if (!istEigentuemerOderAdmin(buchung, nutzer)) {
            throw new BerechtigungsFehler("Nur die eigene Buchung darf storniert werden");
        }

        buchung.stornieren();
        buchungRepository.speichern(buchung);
    }

    /** EC-11 Akzeptanzkriterium "Buchung erscheint in der persoenlichen Uebersicht". */
    public List<Buchung> getBuchungenFuerNutzer(Nutzer nutzer) {
        return buchungRepository.findeNachNutzer(nutzer.getId());
    }

    public List<Buchung> getAlleBuchungen() {
        return buchungRepository.findeAlle();
    }

    public Buchung getBuchung(String buchungId) {
        return buchungRepository.findeNachId(buchungId)
                .orElseThrow(() -> new NichtGefundenFehler("Buchung nicht gefunden: " + buchungId));
    }

    private boolean istEigentuemerOderAdmin(Buchung buchung, Nutzer nutzer) {
        return buchung.getNutzerId().equals(nutzer.getId()) || nutzer.getRollen().contains(Rolle.ADMIN);
    }
}
