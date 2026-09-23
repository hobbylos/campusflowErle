package de.campusflow.fachlogik.raumkatalog;

import de.campusflow.datenhaltung.raumkatalog.FacilityAdapter;
import de.campusflow.datenhaltung.raumkatalog.RaumRepository;
import de.campusflow.fachlogik.gemeinsam.BerechtigungsFehler;
import de.campusflow.fachlogik.gemeinsam.NichtGefundenFehler;
import de.campusflow.fachlogik.zugangskontrolle.Aktion;
import de.campusflow.fachlogik.zugangskontrolle.BerechtigungsPruefer;
import de.campusflow.fachlogik.zugangskontrolle.Nutzer;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Orchestriert EC-8 (Raum anlegen), EC-9 (Raum bearbeiten) und
 * EC-10 (Raum sperren). Greift laut strenger Schichtenbindung nur auf die
 * Datenhaltungsschicht zu (RaumRepository, FacilityAdapter), nie auf die
 * Darstellungsschicht.
 */
@Service
public class RaumKatalogService {

    private final RaumRepository raumRepository;
    private final FacilityAdapter facilityAdapter;
    private final BerechtigungsPruefer berechtigungsPruefer;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    public RaumKatalogService(RaumRepository raumRepository,
                               FacilityAdapter facilityAdapter,
                               BerechtigungsPruefer berechtigungsPruefer,
                               org.springframework.context.ApplicationEventPublisher eventPublisher) {
        this.raumRepository = raumRepository;
        this.facilityAdapter = facilityAdapter;
        this.berechtigungsPruefer = berechtigungsPruefer;
        this.eventPublisher = eventPublisher;
    }

    /** EC-8 */
    public Raum raumAnlegen(RaumDaten daten, Nutzer nutzer) {
        if (!berechtigungsPruefer.darf(nutzer, Aktion.RAUM_ANLEGEN)) {
            throw new BerechtigungsFehler("Keine Berechtigung zum Anlegen eines Raums");
        }
        int kapazitaet = (daten.kapazitaet() != null) ? daten.kapazitaet() : 0;
        Raum raum = new Raum(UUID.randomUUID().toString(), daten.name(), kapazitaet,
                daten.ausstattung(), daten.kategorie());
        raumRepository.speichern(raum);
        facilityAdapter.raumErstellt(raum);
        return raum;
    }

    /** EC-9 */
    public Raum raumAktualisieren(String raumId, RaumDaten daten, Nutzer nutzer) {
        if (!berechtigungsPruefer.darf(nutzer, Aktion.RAUM_AKTUALISIEREN)) {
            throw new BerechtigungsFehler("Keine Berechtigung zum Aendern eines Raums");
        }
        Raum raum = raumRepository.findeNachId(raumId)
                .orElseThrow(() -> new NichtGefundenFehler("Raum nicht gefunden: " + raumId));
        String neuerName = (daten.name() != null && !daten.name().isBlank()) ? daten.name() : raum.getName();
        int neueKapazitaet = (daten.kapazitaet() != null) ? daten.kapazitaet() : raum.getKapazitaet();
        List<String> neueAusstattung = daten.ausstattung() != null ? daten.ausstattung() : raum.getAusstattung();
        String neueKategorie = daten.kategorie() != null ? daten.kategorie() : raum.getKategorie();
        raum.aktualisiere(neuerName, neueKapazitaet, neueAusstattung, neueKategorie);
        raumRepository.speichern(raum);
        facilityAdapter.raumAktualisiert(raum);
        return raum;
    }

    /** EC-10: Raum sperren und Event fuer automatische Stornierung publizieren. */
    public Raum raumSperren(String raumId, Instant von, Instant bis, Nutzer nutzer) {
        if (!berechtigungsPruefer.darf(nutzer, Aktion.RAUM_SPERREN)) {
            throw new BerechtigungsFehler("Keine Berechtigung zum Sperren eines Raums");
        }
        Raum raum = raumRepository.findeNachId(raumId)
                .orElseThrow(() -> new NichtGefundenFehler("Raum nicht gefunden: " + raumId));
        raum.sperren(von, bis);
        raumRepository.speichern(raum);
        facilityAdapter.raumGesperrt(raum);
        if (eventPublisher != null) {
            eventPublisher.publishEvent(new RaumGesperrtEvent(raumId, raum.getSperrVon(), raum.getSperrBis()));
        }
        return raum;
    }

    /** EC-10: Raum entsperren. */
    public Raum raumEntsperren(String raumId, Nutzer nutzer) {
        if (!berechtigungsPruefer.darf(nutzer, Aktion.RAUM_SPERREN)) {
            throw new BerechtigungsFehler("Keine Berechtigung zum Entsperren eines Raums");
        }
        Raum raum = raumRepository.findeNachId(raumId)
                .orElseThrow(() -> new NichtGefundenFehler("Raum nicht gefunden: " + raumId));
        raum.entsperren();
        raumRepository.speichern(raum);
        facilityAdapter.raumAktualisiert(raum);
        return raum;
    }

    public Raum getRaum(String raumId) {
        return raumRepository.findeNachId(raumId)
                .orElseThrow(() -> new NichtGefundenFehler("Raum nicht gefunden: " + raumId));
    }

    public List<Raum> getRaeume(RaumFilter filter) {
        return raeumeFiltern(raumRepository.findeAlle(filter));
    }

    private List<Raum> raeumeFiltern(List<Raum> raeume) {
        return raeume;
    }

    /** OpenAPI DELETE /raeume/{raumId}: Raum loeschen und zugehoerige Buchungen automatisch bereinigen. */
    public void raumLoeschen(String raumId, Nutzer nutzer) {
        if (!berechtigungsPruefer.darf(nutzer, Aktion.RAUM_ANLEGEN)) {
            throw new BerechtigungsFehler("Keine Berechtigung zum Loeschen eines Raums");
        }
        Raum raum = raumRepository.findeNachId(raumId)
                .orElseThrow(() -> new NichtGefundenFehler("Raum nicht gefunden: " + raumId));
        raumRepository.loeschen(raumId);
        if (eventPublisher != null) {
            eventPublisher.publishEvent(new RaumGeloeschtEvent(raumId));
        }
    }
}
