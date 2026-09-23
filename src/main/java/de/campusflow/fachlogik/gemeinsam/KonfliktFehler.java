package de.campusflow.fachlogik.gemeinsam;

/** Wird geworfen bei Doppelbuchungen oder anderen Zustandskonflikten (EC-12). */
public class KonfliktFehler extends RuntimeException {
    public KonfliktFehler(String nachricht) {
        super(nachricht);
    }
}
