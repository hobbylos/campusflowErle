package de.campusflow.darstellung.raumkatalog;

import de.campusflow.darstellung.gemeinsam.AktuellerNutzerAufloeser;
import de.campusflow.fachlogik.raumkatalog.Raum;
import de.campusflow.fachlogik.raumkatalog.RaumDaten;
import de.campusflow.fachlogik.raumkatalog.RaumFilter;
import de.campusflow.fachlogik.raumkatalog.RaumKatalogService;
import de.campusflow.fachlogik.zugangskontrolle.Nutzer;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST-Schnittstelle des Moduls Raumkatalog (EC-2), passend zum
 * campusflow_openapi.md-Vertrag. Greift laut strenger Schichtenbindung
 * ausschliesslich auf RaumKatalogService (Fachlogik) zu, nie direkt auf
 * die Datenhaltung.
 */
@RestController
@RequestMapping("/api/v1/raeume")
public class RaumController {

    private final RaumKatalogService raumKatalogService;
    private final AktuellerNutzerAufloeser nutzerAufloeser;

    public RaumController(RaumKatalogService raumKatalogService, AktuellerNutzerAufloeser nutzerAufloeser) {
        this.raumKatalogService = raumKatalogService;
        this.nutzerAufloeser = nutzerAufloeser;
    }

    @GetMapping
    public List<RaumDto> raeumeAuflisten(@RequestParam(required = false) String gebaeude,
                                          @RequestParam(required = false) String kategorie,
                                          @RequestParam(required = false) Integer minKapazitaet,
                                          @RequestParam(required = false) String ausstattung) {
        RaumFilter filter = new RaumFilter(gebaeude, kategorie, minKapazitaet, ausstattung);
        return raumKatalogService.getRaeume(filter).stream().map(RaumDto::von).toList();
    }

    @GetMapping("/{raumId}")
    public RaumDto raumAbrufen(@PathVariable String raumId) {
        return RaumDto.von(raumKatalogService.getRaum(raumId));
    }

    @PostMapping
    public ResponseEntity<RaumDto> raumAnlegen(@RequestBody RaumEingabeDto eingabe,
                                                @RequestHeader("Authorization") String authHeader) {
        Nutzer nutzer = nutzerAufloeser.aufloesen(authHeader);
        RaumDaten daten = new RaumDaten(eingabe.name(), eingabe.kapazitaet(), eingabe.ausstattung(), eingabe.kategorie(), eingabe.gebaeude());
        Raum raum = raumKatalogService.raumAnlegen(daten, nutzer);
        return ResponseEntity.status(HttpStatus.CREATED).body(RaumDto.von(raum));
    }

    @PutMapping("/{raumId}")
    public RaumDto raumAktualisieren(@PathVariable String raumId,
                                      @RequestBody RaumEingabeDto eingabe,
                                      @RequestHeader("Authorization") String authHeader) {
        Nutzer nutzer = nutzerAufloeser.aufloesen(authHeader);
        RaumDaten daten = new RaumDaten(eingabe.name(), eingabe.kapazitaet(), eingabe.ausstattung(), eingabe.kategorie(), eingabe.gebaeude());
        return RaumDto.von(raumKatalogService.raumAktualisieren(raumId, daten, nutzer));
    }

    @PostMapping("/{raumId}/sperren")
    public RaumDto raumSperren(@PathVariable String raumId,
                                @RequestBody(required = false) RaumSperrenDto sperrung,
                                @RequestHeader("Authorization") String authHeader) {
        Nutzer nutzer = nutzerAufloeser.aufloesen(authHeader);
        Instant von = (sperrung != null) ? sperrung.von() : null;
        Instant bis = (sperrung != null) ? sperrung.bis() : null;
        return RaumDto.von(raumKatalogService.raumSperren(raumId, von, bis, nutzer));
    }

    @PostMapping("/{raumId}/entsperren")
    public RaumDto raumEntsperren(@PathVariable String raumId,
                                  @RequestHeader("Authorization") String authHeader) {
        Nutzer nutzer = nutzerAufloeser.aufloesen(authHeader);
        return RaumDto.von(raumKatalogService.raumEntsperren(raumId, nutzer));
    }

    @DeleteMapping("/{raumId}")
    public ResponseEntity<Void> raumLoeschen(@PathVariable String raumId,
                                              @RequestHeader("Authorization") String authHeader) {
        Nutzer nutzer = nutzerAufloeser.aufloesen(authHeader);
        raumKatalogService.raumLoeschen(raumId, nutzer);
        return ResponseEntity.noContent().build();
    }
}
