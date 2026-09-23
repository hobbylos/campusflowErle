package de.campusflow.datenhaltung.buchungskern;

import de.campusflow.fachlogik.buchungskern.Buchung;
import de.campusflow.fachlogik.buchungskern.BuchungStatus;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/** In-Memory-Implementierung, da laut Rahmenbedingungen keine eigene Datenspeicherung vorgesehen ist. */
@Repository
public class InMemoryBuchungRepository implements BuchungRepository {

    private final Map<String, Buchung> buchungen = new ConcurrentHashMap<>();

    @Override
    public void speichern(Buchung buchung) {
        buchungen.put(buchung.getId(), buchung);
    }

    @Override
    public Optional<Buchung> findeNachId(String buchungId) {
        return Optional.ofNullable(buchungen.get(buchungId));
    }

    @Override
    public List<Buchung> findeUeberlappende(String raumId, Instant von, Instant bis, String ausgenommenBuchungId) {
        return buchungen.values().stream()
                .filter(b -> b.getRaumId().equals(raumId))
                .filter(b -> b.getStatus() == BuchungStatus.GEPLANT)
                .filter(b -> ausgenommenBuchungId == null || !b.getId().equals(ausgenommenBuchungId))
                .filter(b -> b.ueberlapptMit(von, bis))
                .toList();
    }

    @Override
    public List<Buchung> findeNachNutzer(String nutzerId) {
        return buchungen.values().stream()
                .filter(b -> b.getNutzerId().equals(nutzerId))
                .toList();
    }
}
