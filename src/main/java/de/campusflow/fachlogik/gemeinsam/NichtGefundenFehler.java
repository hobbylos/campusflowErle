package de.campusflow.fachlogik.gemeinsam;

/** Wird geworfen, wenn eine angefragte Entitaet (Raum, Buchung, Nutzer) nicht existiert. */
public class NichtGefundenFehler extends RuntimeException {
    public NichtGefundenFehler(String nachricht) {
        super(nachricht);
    }
}
