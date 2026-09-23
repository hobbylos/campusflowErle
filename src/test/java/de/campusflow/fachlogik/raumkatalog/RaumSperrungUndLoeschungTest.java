package de.campusflow.fachlogik.raumkatalog;

import de.campusflow.datenhaltung.buchungskern.BestaetigungsVersender;
import de.campusflow.datenhaltung.buchungskern.InMemoryBuchungRepository;
import de.campusflow.datenhaltung.raumkatalog.FacilityAdapter;
import de.campusflow.datenhaltung.raumkatalog.InMemoryRaumRepository;
import de.campusflow.fachlogik.buchungskern.Buchung;
import de.campusflow.fachlogik.buchungskern.BuchungDaten;
import de.campusflow.fachlogik.buchungskern.BuchungService;
import de.campusflow.fachlogik.buchungskern.BuchungStatus;
import de.campusflow.fachlogik.gemeinsam.BerechtigungsFehler;
import de.campusflow.fachlogik.zugangskontrolle.BerechtigungsPruefer;
import de.campusflow.fachlogik.zugangskontrolle.Nutzer;
import de.campusflow.fachlogik.zugangskontrolle.Rolle;
import de.campusflow.fachlogik.zugangskontrolle.RollenBasierterBerechtigungsPruefer;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;

class RaumSperrungUndLoeschungTest {

    private InMemoryRaumRepository raumRepo;
    private InMemoryBuchungRepository buchungRepo;
    private RaumKatalogService raumKatalogService;
    private BuchungService buchungService;
    private Nutzer admin;
    private Nutzer student;

    @BeforeEach
    void setUp() {
        raumRepo = new InMemoryRaumRepository();
        buchungRepo = new InMemoryBuchungRepository();
        BerechtigungsPruefer pruefer = new RollenBasierterBerechtigungsPruefer();
        BestaetigungsVersender versender = buchung -> {}; // Mock/No-op Versender

        buchungService = new BuchungService(buchungRepo, raumRepo, versender, pruefer);

        // EventPublisher leitet Events direkt an den buchungService weiter
        ApplicationEventPublisher eventPublisher = event -> {
            if (event instanceof RaumGesperrtEvent rge) {
                buchungService.onRaumGesperrt(rge);
            } else if (event instanceof RaumGeloeschtEvent rle) {
                buchungService.onRaumGeloescht(rle);
            }
        };

        FacilityAdapter facilityAdapter = new FacilityAdapter() {
            @Override public void raumErstellt(Raum raum) {}
            @Override public void raumAktualisiert(Raum raum) {}
            @Override public void raumGesperrt(Raum raum) {}
        };

        raumKatalogService = new RaumKatalogService(raumRepo, facilityAdapter, pruefer, eventPublisher);

        admin = new Nutzer("n-admin", "admin01", "Admin Lukas", Set.of(Rolle.ADMIN));
        student = new Nutzer("n-stud", "stud01", "Student Anna", Set.of(Rolle.STUDENT));
    }

    @Test
    @DisplayName("EC-10: Raum sperren storniert automatisch bestehende Buchungen im Zeitraum")
    void raumSperren_storniertUeberlappendeBuchungenAutomatisch() {
        // 1. Raum anlegen
        Raum raum = raumKatalogService.raumAnlegen(
                new RaumDaten("Seminarraum A1", 30, List.of("Beamer"), "Seminarraum", null), admin);

        // 2. Buchung anlegen
        Instant start = Instant.now().plus(2, ChronoUnit.HOURS);
        Instant ende = Instant.now().plus(4, ChronoUnit.HOURS);
        Buchung buchung = buchungService.buchungAnlegen(
                new BuchungDaten(raum.getId(), start, ende, "Mathe Tutorium"), student);

        assertEquals(BuchungStatus.GEPLANT, buchung.getStatus());

        // 3. Admin sperrt den Raum ohne Angabe von Name/Daten
        raumKatalogService.raumSperren(raum.getId(), start.minus(1, ChronoUnit.HOURS), ende.plus(1, ChronoUnit.HOURS), admin);

        // 4. Raum ist gesperrt
        Raum gesperrterRaum = raumKatalogService.getRaum(raum.getId());
        assertEquals(RaumStatus.GESPERRT, gesperrterRaum.getStatus());

        // 5. Buchung wurde automatisch storniert!
        Buchung aktualisierteBuchung = buchungService.getBuchung(buchung.getId());
        assertEquals(BuchungStatus.STORNIERT, aktualisierteBuchung.getStatus());
    }

    @Test
    @DisplayName("EC-2: Raum löschen storniert automatisch alle zugehörigen Buchungen")
    void raumLoeschen_storniertZugehoerigeBuchungenAutomatisch() {
        // 1. Raum anlegen
        Raum raum = raumKatalogService.raumAnlegen(
                new RaumDaten("Labor B12", 20, List.of("PCs"), "Labor", null), admin);

        // 2. Zwei Buchungen anlegen
        Instant t1 = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant t2 = Instant.now().plus(2, ChronoUnit.DAYS);
        Buchung b1 = buchungService.buchungAnlegen(new BuchungDaten(raum.getId(), t1, t1.plus(2, ChronoUnit.HOURS), "Übung 1"), student);
        Buchung b2 = buchungService.buchungAnlegen(new BuchungDaten(raum.getId(), t2, t2.plus(2, ChronoUnit.HOURS), "Übung 2"), student);

        // 3. Admin löscht den Raum
        raumKatalogService.raumLoeschen(raum.getId(), admin);

        // 4. Raum ist gelöscht
        assertTrue(raumRepo.findeNachId(raum.getId()).isEmpty());

        // 5. Buchungen wurden automatisch auf STORNIERT gesetzt
        assertEquals(BuchungStatus.STORNIERT, buchungService.getBuchung(b1.getId()).getStatus());
        assertEquals(BuchungStatus.STORNIERT, buchungService.getBuchung(b2.getId()).getStatus());
    }

    @Test
    @DisplayName("Negativfall (403): Student darf keinen Raum sperren")
    void studentDarfKeinenRaumSperren() {
        Raum raum = raumKatalogService.raumAnlegen(
                new RaumDaten("Seminarraum C3", 25, List.of(), "Seminarraum", null), admin);

        assertThrows(BerechtigungsFehler.class, () ->
                raumKatalogService.raumSperren(raum.getId(), Instant.now(), Instant.now().plus(1, ChronoUnit.HOURS), student));
    }

    @Test
    @DisplayName("Negativfall (422): Raumgröße zu -5 ändern wirft Invarianten-Fehler")
    void raumAktualisieren_wirftFehler_wennKapazitaetUngueltig() {
        Raum raum = raumKatalogService.raumAnlegen(
                new RaumDaten("Seminarraum D4", 40, List.of(), "Seminarraum", null), admin);

        // Versuch, die Raumgröße auf -5 zu ändern
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                raumKatalogService.raumAktualisieren(
                        raum.getId(),
                        new RaumDaten(null, -5, null, null, null),
                        admin));

        assertTrue(ex.getMessage().contains("Kapazitaet muss mindestens 1 sein") || ex.getMessage().contains("-5"));
    }
}
