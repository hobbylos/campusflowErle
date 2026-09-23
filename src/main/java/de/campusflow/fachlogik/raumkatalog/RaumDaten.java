package de.campusflow.fachlogik.raumkatalog;

import java.util.List;

/** Eingabe-Wertobjekt fuer raumAnlegen/raumAktualisieren -- getrennt vom DTO der Darstellungsschicht. */
public record RaumDaten(String name, int kapazitaet, List<String> ausstattung, String kategorie) {}
