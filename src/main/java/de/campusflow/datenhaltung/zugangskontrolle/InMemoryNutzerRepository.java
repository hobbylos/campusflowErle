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
        Nutzer admin = new Nutzer("n-1", "admin01", "Admin Verwaltung", Set.of(Rolle.ADMIN));
        Nutzer nutzer = new Nutzer("n-2", "stud01", "Test Student", Set.of(Rolle.NUTZER));
        nutzerNachId.put(admin.getId(), admin);
        nutzerNachId.put(nutzer.getId(), nutzer);
    }

    @Override
    public Optional<Nutzer> findeNachUniKennung(String uniKennung) {
        return nutzerNachId.values().stream()
                .filter(n -> n.getUniKennung().equals(uniKennung))
                .findFirst();
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
