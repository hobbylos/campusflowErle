package de.campusflow.darstellung.zugangskontrolle;

import de.campusflow.darstellung.gemeinsam.AktuellerNutzerAufloeser;
import de.campusflow.fachlogik.zugangskontrolle.Nutzer;
import de.campusflow.fachlogik.zugangskontrolle.Rolle;
import de.campusflow.fachlogik.zugangskontrolle.RollenVerwaltungService;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.*;

/** EC-15 "Rollen verwalten" (nur Admin, siehe RollenVerwaltungService). */
@RestController
@RequestMapping("/api/v1/nutzer")
public class NutzerController {

    private final RollenVerwaltungService rollenVerwaltungService;
    private final AktuellerNutzerAufloeser nutzerAufloeser;

    public NutzerController(RollenVerwaltungService rollenVerwaltungService, AktuellerNutzerAufloeser nutzerAufloeser) {
        this.rollenVerwaltungService = rollenVerwaltungService;
        this.nutzerAufloeser = nutzerAufloeser;
    }

    @GetMapping("/{nutzerId}/rollen")
    public Set<String> rollenAbrufen(@PathVariable String nutzerId) {
        return rollenVerwaltungService.rollenAbrufen(nutzerId).stream().map(Rolle::getName).collect(Collectors.toSet());
    }

    @PutMapping("/{nutzerId}/rollen")
    public NutzerDto rollenSetzen(@PathVariable String nutzerId,
                                   @RequestBody RollenZuweisungDto eingabe,
                                   @RequestHeader("Authorization") String authHeader) {
        Nutzer ausfuehrenderNutzer = nutzerAufloeser.aufloesen(authHeader);
        Set<Rolle> neueRollen = eingabe.rollen().stream().map(Rolle::new).collect(Collectors.toSet());
        Nutzer aktualisiert = rollenVerwaltungService.rollenZuweisen(nutzerId, neueRollen, ausfuehrenderNutzer);
        return NutzerDto.von(aktualisiert);
    }
}
