package de.campusflow.datenhaltung.buchungskern;

import de.campusflow.fachlogik.buchungskern.Buchung;

/** EC-13 "Buchungsbestaetigung". Kanal (E-Mail vs. In-App) ist laut Story noch offen. */
public interface BestaetigungsVersender {
    void bestaetigungSenden(Buchung buchung);
}
