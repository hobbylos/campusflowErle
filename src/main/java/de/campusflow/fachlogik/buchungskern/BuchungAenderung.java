package de.campusflow.fachlogik.buchungskern;

import java.time.Instant;

/**
 * Eingabe fuer PATCH /buchungen/{buchungId}. Beide Felder sind optional
 * (siehe openapi-Vertrag BuchungAenderung, kein "required") -- ein
 * weggelassenes Feld laesst den bisherigen Wert der Buchung unveraendert,
 * die Zusammenfuehrung passiert in BuchungService.buchungAendern.
 */
public record BuchungAenderung(Instant von, Instant bis) {}
