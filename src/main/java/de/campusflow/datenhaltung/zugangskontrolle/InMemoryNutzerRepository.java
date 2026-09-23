package de.campusflow.datenhaltung.zugangskontrolle;

import de.campusflow.fachlogik.zugangskontrolle.Nutzer;
import de.campusflow.fachlogik.zugangskontrolle.Rolle;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/**
 * In-Memory-Implementierung, da laut Rahmenbedingungen keine eigene
 * Datenspeicherung vorgesehen ist. Mit zwei Testnutzern vorbelegt, damit
 * die REST-Schnittstelle ohne zusaetzliches Setup ausprobiert werden kann.
 *
 * Das Uni-Login-Protokoll (SAML/OAuth/LDAP) ist laut EC-4 noch nicht
 * entschieden -- diese Klasse ersetzt vorlaeufig nur die Datenhaltung,
 * nicht die eigentliche Authentifizierung gegen die Uni.
 */
@Repository
public class InMemoryNutzerRepository implements NutzerRepository {

    private final Map<String, Nutzer> nutzerNachId = new ConcurrentHashMap<>();

    public InMemoryNutzerRepository() {
        Nutzer admin = new Nutzer("n-1", "admin01", "Admin Verwaltung (Lukas)", Set.of(Rolle.ADMIN));
        Nutzer adminAlias = new Nutzer("n-1-alias", "admin", "Admin Verwaltung (Lukas)", Set.of(Rolle.ADMIN));
        Nutzer nutzer = new Nutzer("n-2", "stud01", "Test Student (Anna)", Set.of(Rolle.STUDENT));
        Nutzer nutzerAlias = new Nutzer("n-2-alias", "student", "Test Student (Anna)", Set.of(Rolle.STUDENT));
        Nutzer dozent = new Nutzer("n-3", "dozent01", "Dr. Schneider (Dozent)", Set.of(Rolle.DOZENT));
        Nutzer dozentAlias = new Nutzer("n-3-alias", "dozent", "Dr. Schneider (Dozent)", Set.of(Rolle.DOZENT));

        nutzerNachId.put(admin.getId(), admin);
        nutzerNachId.put(adminAlias.getId(), adminAlias);
        nutzerNachId.put(nutzer.getId(), nutzer);
        nutzerNachId.put(nutzerAlias.getId(), nutzerAlias);
        nutzerNachId.put(dozent.getId(), dozent);
        nutzerNachId.put(dozentAlias.getId(), dozentAlias);
    }

    @Override
    public Optional<Nutzer> findeNachUniKennung(String uniKennung) {
        if (uniKennung == null) return Optional.empty();
        String trimmed = uniKennung.trim();
        Optional<Nutzer> direct = nutzerNachId.values().stream()
                .filter(n -> n.getUniKennung().equalsIgnoreCase(trimmed))
                .findFirst();
        if (direct.isPresent()) return direct;

        if (trimmed.equalsIgnoreCase("admin") || trimmed.equalsIgnoreCase("lukas") || trimmed.equalsIgnoreCase("verwaltung")) {
            return findeNachUniKennung("admin01");
        }
        if (trimmed.equalsIgnoreCase("student") || trimmed.equalsIgnoreCase("anna") || trimmed.equalsIgnoreCase("stud")) {
            return findeNachUniKennung("stud01");
        }
        if (trimmed.equalsIgnoreCase("dozent") || trimmed.equalsIgnoreCase("schneider") || trimmed.equalsIgnoreCase("prof")) {
            return findeNachUniKennung("dozent01");
        }
        return Optional.empty();
    }

    @Override
    public Optional<Nutzer> findeNachId(String nutzerId) {
        return Optional.ofNullable(nutzerNachId.get(nutzerId));
    }

    @Override
    public void speichern(Nutzer nutzer) {
        nutzerNachId.put(nutzer.getId(), nutzer);
    }
}
