package de.campusflow.darstellung.buchungskern;

import de.campusflow.fachlogik.buchungskern.Buchung;
import java.time.Instant;

record BuchungDto(String id, String raumId, String nutzerId, Instant von, Instant bis,
                          String zweck, String status) {

    public static BuchungDto von(Buchung buchung) {
        return new BuchungDto(buchung.getId(), buchung.getRaumId(), buchung.getNutzerId(),
                buchung.getVon(), buchung.getBis(), buchung.getZweck(), buchung.getStatus().name());
    }
}

record BuchungEingabeDto(String raumId, Instant von, Instant bis, String zweck) {}

/** Eingabe fuer PATCH /buchungen/{buchungId}; von/bis jeweils optional (siehe openapi-Schema BuchungAenderung). */
record BuchungAenderungDto(Instant von, Instant bis) {}
