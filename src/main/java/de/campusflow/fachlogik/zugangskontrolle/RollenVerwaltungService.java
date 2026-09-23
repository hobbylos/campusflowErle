package de.campusflow.fachlogik.zugangskontrolle;

import de.campusflow.datenhaltung.zugangskontrolle.NutzerRepository;
import de.campusflow.fachlogik.gemeinsam.BerechtigungsFehler;
import de.campusflow.fachlogik.gemeinsam.NichtGefundenFehler;
import java.util.Set;
import org.springframework.stereotype.Service;

/** EC-15 "Rollen verwalten". */
@Service
public class RollenVerwaltungService {

    private final NutzerRepository nutzerRepository;
    private final BerechtigungsPruefer berechtigungsPruefer;

    public RollenVerwaltungService(NutzerRepository nutzerRepository,
                                    BerechtigungsPruefer berechtigungsPruefer) {
        this.nutzerRepository = nutzerRepository;
        this.berechtigungsPruefer = berechtigungsPruefer;
    }

    public Nutzer rollenZuweisen(String nutzerId, Set<Rolle> neueRollen, Nutzer ausfuehrenderNutzer) {
        if (!berechtigungsPruefer.darf(ausfuehrenderNutzer, Aktion.ROLLEN_VERWALTEN)) {
            throw new BerechtigungsFehler("Nur Admins duerfen Rollen vergeben");
        }
        Nutzer nutzer = nutzerRepository.findeNachId(nutzerId)
                .orElseThrow(() -> new NichtGefundenFehler("Nutzer nicht gefunden: " + nutzerId));
        nutzer.setRollen(neueRollen);
        nutzerRepository.speichern(nutzer);
        return nutzer;
    }

    public Set<Rolle> rollenAbrufen(String nutzerId) {
        return nutzerRepository.findeNachId(nutzerId)
                .orElseThrow(() -> new NichtGefundenFehler("Nutzer nicht gefunden: " + nutzerId))
                .getRollen();
    }
}
