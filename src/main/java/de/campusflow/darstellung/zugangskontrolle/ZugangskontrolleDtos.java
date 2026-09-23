package de.campusflow.darstellung.zugangskontrolle;

import de.campusflow.fachlogik.zugangskontrolle.Nutzer;
import de.campusflow.fachlogik.zugangskontrolle.Rolle;
import java.time.Instant;
import java.util.List;
import java.util.Set;

record LoginAnfrageDto(String uniKennung, String credential) {}

record LoginAntwortDto(String token, Instant gueltigBis) {}

record NutzerDto(String id, String name, List<String> rollen) {
    public static NutzerDto von(Nutzer nutzer) {
        return new NutzerDto(nutzer.getId(), nutzer.getName(),
                nutzer.getRollen().stream().map(Rolle::getName).toList());
    }
}

record RollenZuweisungDto(Set<String> rollen) {}
