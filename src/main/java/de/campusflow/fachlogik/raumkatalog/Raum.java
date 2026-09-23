package de.campusflow.fachlogik.raumkatalog;

import java.time.Instant;
import java.util.List;

/**
 * Entitaet Raum (siehe Klassenentwurf Aufgabe C).
 * Um EC-10 "Raum sperren" korrekt abzubilden, wurde raumLoeschen aus dem
 * urspruenglichen Entwurf durch raumSperren ersetzt: die Jira-Story
 * verlangt eine zeitlich begrenzte Sperrung mit Sperrzeitraum, kein
 * endgueltiges Loeschen.
 */
public class Raum {

    private final String id;
    private String name;
    private int kapazitaet;
    private List<String> ausstattung;
    private String kategorie;
    private RaumStatus status;
    private Instant sperrVon;
    private Instant sperrBis;

    public Raum(String id, String name, int kapazitaet, List<String> ausstattung, String kategorie) {
        pruefeInvarianten(name, kapazitaet);
        this.id = id;
        this.name = name;
        this.kapazitaet = kapazitaet;
        this.ausstattung = ausstattung;
        this.kategorie = kategorie;
        this.status = RaumStatus.AKTIV;
    }

    private static void pruefeInvarianten(String name, int kapazitaet) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name darf nicht leer sein");
        }
        if (kapazitaet <= 0) {
            throw new IllegalArgumentException("Kapazitaet muss > 0 sein");
        }
    }

    public void aktualisiere(String name, int kapazitaet, List<String> ausstattung, String kategorie) {
        pruefeInvarianten(name, kapazitaet);
        this.name = name;
        this.kapazitaet = kapazitaet;
        this.ausstattung = ausstattung;
        this.kategorie = kategorie;
    }

    /** EC-10: Raum fuer einen definierten Zeitraum sperren. */
    public void sperren(Instant von, Instant bis) {
        if (von == null || bis == null || !von.isBefore(bis)) {
            throw new IllegalArgumentException("Sperrzeitraum ungueltig");
        }
        this.status = RaumStatus.GESPERRT;
        this.sperrVon = von;
        this.sperrBis = bis;
    }

    public boolean istGesperrtAm(Instant zeitpunkt) {
        return status == RaumStatus.GESPERRT
                && sperrVon != null && sperrBis != null
                && !zeitpunkt.isBefore(sperrVon) && zeitpunkt.isBefore(sperrBis);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getKapazitaet() { return kapazitaet; }
    public List<String> getAusstattung() { return ausstattung; }
    public String getKategorie() { return kategorie; }
    public RaumStatus getStatus() { return status; }
    public Instant getSperrVon() { return sperrVon; }
    public Instant getSperrBis() { return sperrBis; }
}
