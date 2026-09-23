package de.campusflow.fachlogik.zugangskontrolle;

/**
 * Alle Aktionen im System, die einer Berechtigungspruefung unterliegen.
 * Erweitert gegenueber der urspruenglichen Raumkatalog-only-Fassung (Aufgabe C),
 * da EC-16 "Rollenbasierte Rechte" eine modulweite, nicht nur raumbezogene
 * Rechtepruefung verlangt.
 */
public enum Aktion {
    RAUM_ANLEGEN,
    RAUM_AKTUALISIEREN,
    RAUM_SPERREN,
    BUCHUNG_ANLEGEN,
    BUCHUNG_AENDERN,
    BUCHUNG_STORNIEREN,
    ROLLEN_VERWALTEN
}
