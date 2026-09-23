package de.campusflow.fachlogik.raumkatalog;

import java.util.List;

/** Eingabe-Wertobjekt fuer raumAnlegen/raumAktualisieren -- getrennt vom DTO der Darstellungsschicht. */
public record RaumDaten(String name, Integer kapazitaet, List<String> ausstattung, String kategorie, String gebaeude) {}
