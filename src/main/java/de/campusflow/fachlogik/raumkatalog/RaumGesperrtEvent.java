package de.campusflow.fachlogik.raumkatalog;

import java.time.Instant;

/**
 * Wird ausgeloest, wenn ein Raum gesperrt wird.
 * Buchungskern lauscht darauf, um bestehende Buchungen im Sperrzeitraum automatisch zu stornieren.
 */
public record RaumGesperrtEvent(String raumId, Instant von, Instant bis) {}
