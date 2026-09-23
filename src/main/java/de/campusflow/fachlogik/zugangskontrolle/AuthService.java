package de.campusflow.fachlogik.zugangskontrolle;

import de.campusflow.datenhaltung.zugangskontrolle.NutzerRepository;
import de.campusflow.fachlogik.gemeinsam.AuthentifizierungsFehler;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * EC-14 "Login mit Uni-Account".
 * Platzhalter-Implementierung: prueft aktuell nur gegen die im
 * NutzerRepository hinterlegte uniKennung, OHNE echtes Passwort/SSO --
 * das eigentliche Login-Protokoll ist laut EC-4 noch offen (SAML/OAuth/LDAP).
 * Sobald das entschieden ist, wird hier nur die pruefeZugangsdaten-Methode
 * ausgetauscht, der Rest (Tokenausgabe) bleibt gleich.
 *
 * Ein fehlgeschlagener Login ist ein Authentifizierungsproblem (401), keine
 * Berechtigungsfrage (403) -- daher AuthentifizierungsFehler statt
 * BerechtigungsFehler, passend zum openapi-Vertrag.
 */
@Service
public class AuthService {

    private static final long TOKEN_GUELTIGKEIT_STUNDEN = 8;

    private final NutzerRepository nutzerRepository;
    private final Map<String, TokenEintrag> tokenSpeicher = new ConcurrentHashMap<>();

    public AuthService(NutzerRepository nutzerRepository) {
        this.nutzerRepository = nutzerRepository;
    }

    public LoginErgebnis login(String uniKennung, String credential) {
        Nutzer nutzer = nutzerRepository.findeNachUniKennung(uniKennung)
                .orElseThrow(() -> new AuthentifizierungsFehler("Login fehlgeschlagen"));

        // TODO: echte Pruefung sobald Uni-Login-Protokoll feststeht (EC-4)
        if (credential == null || credential.isBlank()) {
            throw new AuthentifizierungsFehler("Login fehlgeschlagen");
        }

        String token = UUID.randomUUID().toString();
        Instant gueltigBis = Instant.now().plus(TOKEN_GUELTIGKEIT_STUNDEN, ChronoUnit.HOURS);
        tokenSpeicher.put(token, new TokenEintrag(nutzer, gueltigBis));
        return new LoginErgebnis(nutzer, token, gueltigBis);
    }

    /** Wird von den Controllern genutzt, um aus dem Bearer-Token den Nutzer aufzuloesen. */
    public Optional<Nutzer> nutzerFuerToken(String token) {
        TokenEintrag eintrag = tokenSpeicher.get(token);
        if (eintrag == null || eintrag.gueltigBis.isBefore(Instant.now())) {
            return Optional.empty();
        }
        return Optional.of(eintrag.nutzer);
    }

    private record TokenEintrag(Nutzer nutzer, Instant gueltigBis) {}

    public record LoginErgebnis(Nutzer nutzer, String token, Instant gueltigBis) {}
}
