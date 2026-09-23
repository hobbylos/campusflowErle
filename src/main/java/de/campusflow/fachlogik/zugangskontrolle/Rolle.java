package de.campusflow.fachlogik.zugangskontrolle;

import java.util.Objects;

/**
 * Rolle im Sinne von EC-4. Der konkrete Rollenkatalog (ADMIN, NUTZER, ...)
 * ist als Platzhalter gesetzt -- das Team sollte ihn gegen die tatsaechliche
 * Stakeholder-Liste (Studenten, Dozenten, Verwaltung, Sekretariat, Putzkraefte, ...)
 * abgleichen und ggf. erweitern.
 */
public final class Rolle {

    public static final Rolle ADMIN = new Rolle("ADMIN");
    public static final Rolle NUTZER = new Rolle("NUTZER");

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
