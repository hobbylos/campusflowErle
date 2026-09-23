package de.campusflow.fachlogik.zugangskontrolle;

import java.util.HashSet;
import java.util.Set;

/** Nutzer im Sinne von EC-4. */
public class Nutzer {

    private final String id;
    private final String uniKennung;
    private final String name;
    private final Set<Rolle> rollen;

    public Nutzer(String id, String uniKennung, String name, Set<Rolle> rollen) {
        this.id = id;
        this.uniKennung = uniKennung;
        this.name = name;
        this.rollen = new HashSet<>(rollen);
    }

    public String getId() {
        return id;
    }

    public String getUniKennung() {
        return uniKennung;
    }

    public String getName() {
        return name;
    }

    public Set<Rolle> getRollen() {
        return new HashSet<>(rollen);
    }

    /** EC-15: Rollen eines Nutzers ersetzen. */
    public void setRollen(Set<Rolle> neueRollen) {
        this.rollen.clear();
        this.rollen.addAll(neueRollen);
    }
}
