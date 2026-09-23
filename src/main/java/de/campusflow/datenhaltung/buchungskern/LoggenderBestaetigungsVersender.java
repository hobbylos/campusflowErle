package de.campusflow.datenhaltung.buchungskern;

import de.campusflow.fachlogik.buchungskern.Buchung;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Platzhalter: protokolliert nur. Spaeter durch echten E-Mail-/In-App-Versand ersetzbar, ohne BuchungService anzufassen. */
@Component
public class LoggenderBestaetigungsVersender implements BestaetigungsVersender {

    private static final Logger log = LoggerFactory.getLogger(LoggenderBestaetigungsVersender.class);

    @Override
    public void bestaetigungSenden(Buchung buchung) {
        log.info("Bestaetigung (Platzhalter) fuer Buchung {} an Nutzer {}", buchung.getId(), buchung.getNutzerId());
    }
}
