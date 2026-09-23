package de.campusflow.datenhaltung.zugangskontrolle;

import de.campusflow.fachlogik.zugangskontrolle.Nutzer;
import java.util.Optional;

/**
 * Repository-Schnittstelle, bewusst in der Datenhaltungsschicht definiert
 * (nicht in der Fachlogik): die Fachlogik greift damit nur "nach unten"
 * auf die Datenhaltung zu, ohne Umkehrung der Abhaengigkeitsrichtung
 * (strenge Schichtenbindung laut Vorgabe).
 */
public interface NutzerRepository {
    Optional<Nutzer> findeNachUniKennung(String uniKennung);
    Optional<Nutzer> findeNachId(String nutzerId);
    void speichern(Nutzer nutzer);
}
