package de.campusflow.darstellung.buchungskern;

import de.campusflow.darstellung.gemeinsam.AktuellerNutzerAufloeser;
import de.campusflow.fachlogik.buchungskern.Buchung;
import de.campusflow.fachlogik.buchungskern.BuchungAenderung;
import de.campusflow.fachlogik.buchungskern.BuchungDaten;
import de.campusflow.fachlogik.buchungskern.BuchungService;
import de.campusflow.fachlogik.zugangskontrolle.Nutzer;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST-Schnittstelle des Moduls Buchungskern (EC-3). */
@RestController
@RequestMapping("/api/v1/buchungen")
public class BuchungController {

    private final BuchungService buchungService;
    private final AktuellerNutzerAufloeser nutzerAufloeser;

    public BuchungController(BuchungService buchungService, AktuellerNutzerAufloeser nutzerAufloeser) {
        this.buchungService = buchungService;
        this.nutzerAufloeser = nutzerAufloeser;
    }

    /** EC-11 Akzeptanzkriterium "persoenliche Uebersicht" -- daher an den angemeldeten Nutzer gebunden statt an einen Query-Parameter. */
    @GetMapping("/meine")
    public List<BuchungDto> meineBuchungen(@RequestHeader("Authorization") String authHeader) {
        Nutzer nutzer = nutzerAufloeser.aufloesen(authHeader);
        return buchungService.getBuchungenFuerNutzer(nutzer).stream().map(BuchungDto::von).toList();
    }

    @GetMapping("/{buchungId}")
    public BuchungDto buchungAbrufen(@PathVariable String buchungId) {
        return BuchungDto.von(buchungService.getBuchung(buchungId));
    }

    /** EC-11 (Buchung anlegen) inkl. EC-12 (Konfliktpruefung, wirft 409) und EC-13 (Bestaetigung wird ausgeloest). */
    @PostMapping
    public ResponseEntity<BuchungDto> buchungAnlegen(@RequestBody BuchungEingabeDto eingabe,
                                                      @RequestHeader("Authorization") String authHeader) {
        Nutzer nutzer = nutzerAufloeser.aufloesen(authHeader);
        BuchungDaten daten = new BuchungDaten(eingabe.raumId(), eingabe.von(), eingabe.bis(), eingabe.zweck());
        Buchung buchung = buchungService.buchungAnlegen(daten, nutzer);
        return ResponseEntity.status(HttpStatus.CREATED).body(BuchungDto.von(buchung));
    }

    /** Buchung aendern (z. B. Zeitraum verschieben) -- 403 falls nicht die eigene Buchung, 409 bei Kollision. */
    @PatchMapping("/{buchungId}")
    public BuchungDto buchungAendern(@PathVariable String buchungId,
                                      @RequestBody BuchungAenderungDto aenderung,
                                      @RequestHeader("Authorization") String authHeader) {
        Nutzer nutzer = nutzerAufloeser.aufloesen(authHeader);
        Buchung buchung = buchungService.buchungAendern(
                buchungId, new BuchungAenderung(aenderung.von(), aenderung.bis()), nutzer);
        return BuchungDto.von(buchung);
    }

    /** Buchung stornieren -- 204 bei Erfolg, 403 falls nicht die eigene Buchung, 404 falls unbekannt. */
    @DeleteMapping("/{buchungId}")
    public ResponseEntity<Void> buchungStornieren(@PathVariable String buchungId,
                                                   @RequestHeader("Authorization") String authHeader) {
        Nutzer nutzer = nutzerAufloeser.aufloesen(authHeader);
        buchungService.buchungStornieren(buchungId, nutzer);
        return ResponseEntity.noContent().build();
    }
}
