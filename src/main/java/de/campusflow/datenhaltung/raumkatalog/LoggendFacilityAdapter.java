package de.campusflow.datenhaltung.raumkatalog;

import de.campusflow.fachlogik.raumkatalog.Raum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Platzhalter-Implementierung: protokolliert nur, ruft noch kein echtes
 * externes System auf. Sobald die Facility-System-Schnittstelle feststeht,
 * wird nur diese Klasse ausgetauscht -- RaumKatalogService bleibt unveraendert.
 */
@Component
public class LoggendFacilityAdapter implements FacilityAdapter {

    private static final Logger log = LoggerFactory.getLogger(LoggendFacilityAdapter.class);

    @Override
    public void raumErstellt(Raum raum) {
        log.info("Facility-Sync (Platzhalter): Raum erstellt {}", raum.getId());
    }

    @Override
    public void raumAktualisiert(Raum raum) {
        log.info("Facility-Sync (Platzhalter): Raum aktualisiert {}", raum.getId());
    }

    @Override
    public void raumGesperrt(Raum raum) {
        log.info("Facility-Sync (Platzhalter): Raum gesperrt {}", raum.getId());
    }
}
