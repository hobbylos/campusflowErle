package de.campusflow.fachlogik.raumkatalog;

/** Filterkriterien fuer getRaeume (EC-22 "Raeume nach Kriterien filtern"). Alle Felder optional. */
public record RaumFilter(String gebaeude, String kategorie, Integer minKapazitaet, String ausstattungsMerkmal) {

    public static RaumFilter leer() {
        return new RaumFilter(null, null, null, null);
    }
}
