package de.campusflow.fachlogik.raumkatalog;

/**
 * Wird ausgeloest, wenn ein Raum geloescht wird.
 * Buchungskern lauscht darauf, um saemtliche Buchungen fuer den Raum automatisch zu stornieren/bereinigen.
 */
public record RaumGeloeschtEvent(String raumId) {}
