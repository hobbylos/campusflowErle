package de.campusflow.fachlogik.zugangskontrolle;

/**
 * Zentrale Autorisierungs-Schnittstelle (EC-16 "Rollenbasierte Rechte").
 * Gegenueber dem Klassendiagramm aus Aufgabe C bewusst verallgemeinert:
 * statt der raumspezifischen Methode darfRaumVerwalten(nutzer, aktion) gibt
 * es jetzt ein generisches darf(nutzer, aktion), das von allen drei Modulen
 * (Raumkatalog, Buchungskern, Zugangskontrolle selbst) genutzt wird.
 */
public interface BerechtigungsPruefer {
    boolean darf(Nutzer nutzer, Aktion aktion);
}
