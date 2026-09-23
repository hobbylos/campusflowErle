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
    private String gebaeude;
    private RaumStatus status;
    private Instant sperrVon;
    private Instant sperrBis;

    public Raum(String id, String name, int kapazitaet, List<String> ausstattung, String kategorie) {
        this(id, name, kapazitaet, ausstattung, kategorie, null);
    }

    public Raum(String id, String name, int kapazitaet, List<String> ausstattung, String kategorie, String gebaeude) {
        pruefeInvarianten(name, kapazitaet);
        this.id = id;
        this.name = name;
        this.kapazitaet = kapazitaet;
        this.ausstattung = ausstattung;
        this.kategorie = kategorie;
        this.gebaeude = gebaeude;
        this.status = RaumStatus.AKTIV;
    }

    private static void pruefeInvarianten(String name, int kapazitaet) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name darf nicht leer sein");
        }
        if (kapazitaet <= 0) {
            throw new IllegalArgumentException("Invariante verletzt: Kapazitaet muss mindestens 1 sein (eingegeben: " + kapazitaet + ")");
        }
    }

    public void aktualisiere(String name, int kapazitaet, List<String> ausstattung, String kategorie, String gebaeude) {
        pruefeInvarianten(name, kapazitaet);
        this.name = name;
        this.kapazitaet = kapazitaet;
        this.ausstattung = ausstattung;
        this.kategorie = kategorie;
        this.gebaeude = gebaeude;
    }

    /** EC-10: Raum sperren (optionaler Zeitraum, Standard: sofort & unbefristet). */
    public void sperren(Instant von, Instant bis) {
        Instant start = (von != null) ? von : Instant.now();
        Instant ende = (bis != null) ? bis : start.plus(3650, java.time.temporal.ChronoUnit.DAYS);
        if (!start.isBefore(ende)) {
            throw new IllegalArgumentException("Sperrzeitraum ungueltig: Start muss vor Ende liegen");
        }
        this.status = RaumStatus.GESPERRT;
        this.sperrVon = start;
        this.sperrBis = ende;
    }

    /** Raum wieder freigeben (entsperren). */
    public void entsperren() {
        this.status = RaumStatus.AKTIV;
        this.sperrVon = null;
        this.sperrBis = null;
    }

    public boolean istGesperrtAm(Instant zeitpunkt) {
        if (status != RaumStatus.GESPERRT) {
            return false;
        }
        if (sperrVon == null && sperrBis == null) {
            return true;
        }
        if (sperrVon != null && sperrBis != null) {
            return !zeitpunkt.isBefore(sperrVon) && zeitpunkt.isBefore(sperrBis);
        }
        return true;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getKapazitaet() { return kapazitaet; }
    public List<String> getAusstattung() { return ausstattung; }
    public String getKategorie() { return kategorie; }
    public String getGebaeude() { return gebaeude; }
    public RaumStatus getStatus() { return status; }
    public Instant getSperrVon() { return sperrVon; }
    public Instant getSperrBis() { return sperrBis; }
}
