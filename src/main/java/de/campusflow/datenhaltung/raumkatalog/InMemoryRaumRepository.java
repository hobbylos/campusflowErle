package de.campusflow.datenhaltung.raumkatalog;

import de.campusflow.fachlogik.raumkatalog.Raum;
import de.campusflow.fachlogik.raumkatalog.RaumFilter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/** In-Memory-Implementierung, da laut Rahmenbedingungen keine eigene Datenspeicherung vorgesehen ist. */
@Repository
public class InMemoryRaumRepository implements RaumRepository {

    private final Map<String, Raum> raeume = new ConcurrentHashMap<>();

    @Override
    public void speichern(Raum raum) {
        raeume.put(raum.getId(), raum);
    }

    @Override
    public Optional<Raum> findeNachId(String raumId) {
        return Optional.ofNullable(raeume.get(raumId));
    }

    @Override
    public List<Raum> findeAlle(RaumFilter filter) {
        return raeume.values().stream()
                .filter(r -> filter.gebaeude() == null || filter.gebaeude().equalsIgnoreCase(r.getGebaeude()))
                .filter(r -> filter.kategorie() == null || filter.kategorie().equals(r.getKategorie()))
                .filter(r -> filter.minKapazitaet() == null || r.getKapazitaet() >= filter.minKapazitaet())
                .filter(r -> filter.ausstattungsMerkmal() == null || r.getAusstattung().contains(filter.ausstattungsMerkmal()))
                .toList();
    }

    @Override
    public void loeschen(String raumId) {
        raeume.remove(raumId);
    }
}
