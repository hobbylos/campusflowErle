package de.campusflow.datenhaltung.buchungskern;

import de.campusflow.fachlogik.buchungskern.Buchung;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BuchungRepository {
    void speichern(Buchung buchung);
    Optional<Buchung> findeNachId(String buchungId);
    List<Buchung> findeNachNutzer(String nutzerId);

    /**
     * Ueberlappende, noch geplante Buchungen fuer den angegebenen Raum/Zeitraum.
     * ausgenommenBuchungId schliesst eine Buchung (typischerweise die gerade per
     * PATCH geaenderte) von der Pruefung aus, damit sie sich nicht selbst als
     * Konflikt meldet -- bei einer neuen Buchung (EC-11) ist das null.
     */
    List<Buchung> findeUeberlappende(String raumId, Instant von, Instant bis, String ausgenommenBuchungId);

    default List<Buchung> findeUeberlappende(String raumId, Instant von, Instant bis) {
        return findeUeberlappende(raumId, von, bis, null);
    }
}
