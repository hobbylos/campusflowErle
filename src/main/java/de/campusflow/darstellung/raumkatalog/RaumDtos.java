package de.campusflow.darstellung.raumkatalog;

import de.campusflow.fachlogik.raumkatalog.Raum;
import java.time.Instant;
import java.util.List;

record RaumDto(String id, String name, int kapazitaet, List<String> ausstattung,
                       String kategorie, String gebaeude, String status, Instant sperrVon, Instant sperrBis) {

    public static RaumDto von(Raum raum) {
        return new RaumDto(raum.getId(), raum.getName(), raum.getKapazitaet(), raum.getAusstattung(),
                raum.getKategorie(), raum.getGebaeude(), raum.getStatus().name(), raum.getSperrVon(), raum.getSperrBis());
    }
}

record RaumEingabeDto(String name, Integer kapazitaet, List<String> ausstattung, String kategorie, String gebaeude) {}

record RaumSperrenDto(Instant von, Instant bis) {}
