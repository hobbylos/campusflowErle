package de.campusflow.fachlogik.buchungskern;

import java.time.Instant;

public record BuchungDaten(String raumId, Instant von, Instant bis, String zweck) {}
