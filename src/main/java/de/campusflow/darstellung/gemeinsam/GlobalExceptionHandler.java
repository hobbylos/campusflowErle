package de.campusflow.darstellung.gemeinsam;

import de.campusflow.fachlogik.gemeinsam.AuthentifizierungsFehler;
import de.campusflow.fachlogik.gemeinsam.BerechtigungsFehler;
import de.campusflow.fachlogik.gemeinsam.KonfliktFehler;
import de.campusflow.fachlogik.gemeinsam.NichtGefundenFehler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Uebersetzt Fachlogik-Exceptions zentral in HTTP-Statuscodes, passend zum
 * OpenAPI-Vertrag (401/403/404/409). Gehoert bewusst zur Darstellungsschicht:
 * HTTP-Statuscodes sind ein Praesentationsdetail, keine Fachlogik.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthentifizierungsFehler.class)
    public ResponseEntity<FehlerDto> handleAuthentifizierung(AuthentifizierungsFehler e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new FehlerDto("NICHT_AUTHENTIFIZIERT", e.getMessage()));
    }

    @ExceptionHandler(BerechtigungsFehler.class)
    public ResponseEntity<FehlerDto> handleBerechtigung(BerechtigungsFehler e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new FehlerDto("BERECHTIGUNG_VERWEIGERT", e.getMessage()));
    }

    @ExceptionHandler(NichtGefundenFehler.class)
    public ResponseEntity<FehlerDto> handleNichtGefunden(NichtGefundenFehler e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new FehlerDto("NICHT_GEFUNDEN", e.getMessage()));
    }

    @ExceptionHandler(KonfliktFehler.class)
    public ResponseEntity<FehlerDto> handleKonflikt(KonfliktFehler e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new FehlerDto("KONFLIKT", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<FehlerDto> handleUngueltig(IllegalArgumentException e) {
        return ResponseEntity.unprocessableEntity()
                .body(new FehlerDto("UNGUELTIGE_DATEN", e.getMessage()));
    }
}
