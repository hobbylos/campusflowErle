package de.campusflow.fachlogik.gemeinsam;

/** Wird geworfen, wenn ein Nutzer eine Aktion ausfuehren will, fuer die ihm die Rolle fehlt. */
public class BerechtigungsFehler extends RuntimeException {
    public BerechtigungsFehler(String nachricht) {
        super(nachricht);
    }
}
