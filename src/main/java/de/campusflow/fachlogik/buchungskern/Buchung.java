package de.campusflow.fachlogik.buchungskern;

import java.time.Instant;

/** Entitaet Buchung. Noch nicht auf Klassenebene wie Raumkatalog entworfen -- direkt aus EC-3-Stories abgeleitet. */
public class Buchung {

    private final String id;
    private final String raumId;
    private final String nutzerId;
    private Instant von;
    private Instant bis;
    private final String zweck;
    private BuchungStatus status;

    public Buchung(String id, String raumId, String nutzerId, Instant von, Instant bis, String zweck) {
        pruefeZeitraum(von, bis);
        this.id = id;
        this.raumId = raumId;
        this.nutzerId = nutzerId;
        this.von = von;
        this.bis = bis;
        this.zweck = zweck;
        this.status = BuchungStatus.GEPLANT;
    }

    public boolean ueberlapptMit(Instant andereVon, Instant andereBis) {
        return von.isBefore(andereBis) && andereVon.isBefore(bis);
    }

    /** Buchung aendern (z. B. Zeitraum verschieben). */
    public void zeitraumAendern(Instant neuesVon, Instant neuesBis) {
        pruefeZeitraum(neuesVon, neuesBis);
        this.von = neuesVon;
        this.bis = neuesBis;
    }

    /** Buchung stornieren. */
    public void stornieren() {
        this.status = BuchungStatus.STORNIERT;
    }

    private static void pruefeZeitraum(Instant von, Instant bis) {
        if (von == null || bis == null || !von.isBefore(bis)) {
            throw new IllegalArgumentException("Zeitraum ungueltig");
        }
    }

    public String getId() { return id; }
    public String getRaumId() { return raumId; }
    public String getNutzerId() { return nutzerId; }
    public Instant getVon() { return von; }
    public Instant getBis() { return bis; }
    public String getZweck() { return zweck; }
    public BuchungStatus getStatus() { return status; }
}
