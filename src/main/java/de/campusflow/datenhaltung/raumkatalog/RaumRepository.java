package de.campusflow.datenhaltung.raumkatalog;

import de.campusflow.fachlogik.raumkatalog.Raum;
import de.campusflow.fachlogik.raumkatalog.RaumFilter;
import java.util.List;
import java.util.Optional;

public interface RaumRepository {
    void speichern(Raum raum);
    Optional<Raum> findeNachId(String raumId);
    List<Raum> findeAlle(RaumFilter filter);
}
