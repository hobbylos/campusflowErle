package de.campusflow.datenhaltung.raumkatalog;

import de.campusflow.fachlogik.raumkatalog.Raum;

/** Port zum externen Facility-Management-System (EC-2). Aufrufe duerfen die Raumverwaltung nie blockieren. */
public interface FacilityAdapter {
    void raumErstellt(Raum raum);
    void raumAktualisiert(Raum raum);
    void raumGesperrt(Raum raum);
}
