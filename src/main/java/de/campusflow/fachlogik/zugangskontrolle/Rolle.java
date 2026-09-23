package de.campusflow.fachlogik.zugangskontrolle;

import java.util.Objects;

/**
 * Rolle im Sinne von EC-4. Rollenkatalog: ADMIN, DOZENT, STUDENT --
 * abgeglichen gegen die tatsaechliche Stakeholder-Liste (Studenten, Dozenten,
 * Verwaltung). Verwaltung/Sekretariat/Putzkraefte sind noch nicht als eigene
 * Rolle abgebildet, siehe offene Punkte in der ATAM-Bewertung.
 */
public final class Rolle {

    public static final Rolle ADMIN = new Rolle("ADMIN");
    public static final Rolle DOZENT = new Rolle("DOZENT");
    public static final Rolle STUDENT = new Rolle("STUDENT");

    private final String name;

    public Rolle(String name) {
        this.name = Objects.requireNonNull(name, "name darf nicht null sein");
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rolle)) return false;
        return name.equals(((Rolle) o).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
