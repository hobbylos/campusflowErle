# CampusFlow — OpenAPI-Vertrag (Raumkatalog, Buchungskern, Zugangskontrolle)

Dieser Vertrag deckt die REST-Schnittstellen der drei Module ab, die als Nächstes umgesetzt werden sollen. Er ist die externe Schnittstelle zwischen GUI und Backend (siehe vorherige Diskussion: JSON-über-HTTP, REST als Architekturstil).

**Hinweis zu den Annahmen:** Für **Raumkatalog** (EC-2) und **Zugangskontrolle** (EC-4) bilden die Endpunkte direkt die bereits entworfenen Klassen ab (`RaumKatalogService`, `RaumKatalogQuery`, `BerechtigungsPruefer`). Für **Buchungskern** (EC-3) gibt es noch keinen Klassenentwurf — die Endpunkte hier sind aus den EC-3-Fragen abgeleitet (Buchung anlegen/ändern/stornieren, Doppelbuchungsprüfung, Buchungsstatus) und sollten mit dem Team abgeglichen werden, sobald ihr die Buchungskern-Klassen entwerft.

```yaml
openapi: 3.0.3
info:
  title: CampusFlow API
  description: >
    REST-Schnittstelle für die Module Raumkatalog (EC-2),
    Buchungskern (EC-3) und Zugangskontrolle (EC-4).
  version: "0.1.0"

servers:
  - url: /api/v1

tags:
  - name: Raumkatalog
    description: Verwaltung von Räumen (EC-2)
  - name: Buchungskern
    description: Buchungsprozess (EC-3)
  - name: Zugangskontrolle
    description: Authentifizierung und Rollen (EC-4)

security:
  - bearerAuth: []

paths:
  # ---------- Raumkatalog (EC-2) ----------

  /raeume:
    get:
      tags: [Raumkatalog]
      summary: Räume auflisten (RaumKatalogQuery.getRaeume)
      parameters:
        - name: gebaeude
          in: query
          schema: { type: string }
        - name: kategorie
          in: query
          schema: { type: string }
        - name: minKapazitaet
          in: query
          schema: { type: integer, minimum: 1 }
        - name: ausstattung
          in: query
          description: Kommagetrennte Liste geforderter Ausstattungsmerkmale
          schema: { type: string }
      responses:
        "200":
          description: Liste der passenden Räume
          content:
            application/json:
              schema:
                type: array
                items: { $ref: "#/components/schemas/Raum" }

    post:
      tags: [Raumkatalog]
      summary: Raum anlegen (RaumKatalogService.raumAnlegen)
      security: [{ bearerAuth: [] }]
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: "#/components/schemas/RaumEingabe" }
      responses:
        "201":
          description: Raum wurde angelegt
          content:
            application/json:
              schema: { $ref: "#/components/schemas/Raum" }
        "403":
          description: Berechtigung verweigert (BerechtigungsPruefer)
          content:
            application/json:
              schema: { $ref: "#/components/schemas/Fehler" }
        "422":
          description: Raumdaten verletzen eine Invariante (z. B. Kapazität <= 0)
          content:
            application/json:
              schema: { $ref: "#/components/schemas/Fehler" }

  /raeume/{raumId}:
    get:
      tags: [Raumkatalog]
      summary: Einzelnen Raum abrufen (RaumKatalogQuery.getRaum)
      parameters:
        - $ref: "#/components/parameters/RaumId"
      responses:
        "200":
          description: Der Raum
          content:
            application/json:
              schema: { $ref: "#/components/schemas/Raum" }
        "404":
          description: Raum nicht gefunden
          content:
            application/json:
              schema: { $ref: "#/components/schemas/Fehler" }

    put:
      tags: [Raumkatalog]
      summary: Raum aktualisieren (RaumKatalogService.raumAktualisieren)
      security: [{ bearerAuth: [] }]
      parameters:
        - $ref: "#/components/parameters/RaumId"
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: "#/components/schemas/RaumEingabe" }
      responses:
        "200":
          description: Raum wurde aktualisiert
          content:
            application/json:
              schema: { $ref: "#/components/schemas/Raum" }
        "403":
          description: Berechtigung verweigert
          content:
            application/json:
              schema: { $ref: "#/components/schemas/Fehler" }
        "404":
          description: Raum nicht gefunden
          content:
            application/json:
              schema: { $ref: "#/components/schemas/Fehler" }

    delete:
      tags: [Raumkatalog]
      summary: Raum löschen (RaumKatalogService.raumLoeschen)
      security: [{ bearerAuth: [] }]
      parameters:
        - $ref: "#/components/parameters/RaumId"
      responses:
        "204":
          description: Raum wurde gelöscht
        "403":
          description: Berechtigung verweigert
          content:
            application/json:
              schema: { $ref: "#/components/schemas/Fehler" }
        "404":
          description: Raum nicht gefunden
          content:
            application/json:
              schema: { $ref: "#/components/schemas/Fehler" }
        "409":
          description: >
            Raum kann nicht gelöscht werden, da noch aktive Buchungen bestehen
            (offene EC-2-Frage, siehe Epic-Liste)
          content:
            application/json:
              schema: { $ref: "#/components/schemas/Fehler" }

  # ---------- Buchungskern (EC-3) — Annahme, noch kein Klassenentwurf vorhanden ----------

  /buchungen:
    get:
      tags: [Buchungskern]
      summary: Buchungen auflisten
      parameters:
        - name: raumId
          in: query
          schema: { type: string }
        - name: nutzerId
          in: query
          schema: { type: string }
        - name: status
          in: query
          schema:
            type: string
            enum: [GEPLANT, BESTAETIGT, STORNIERT, NICHT_ANGETRETEN]
        - name: von
          in: query
          schema: { type: string, format: date-time }
        - name: bis
          in: query
          schema: { type: string, format: date-time }
      responses:
        "200":
          description: Liste der passenden Buchungen
          content:
            application/json:
              schema:
                type: array
                items: { $ref: "#/components/schemas/Buchung" }

    post:
      tags: [Buchungskern]
      summary: Buchung anlegen
      security: [{ bearerAuth: [] }]
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: "#/components/schemas/BuchungEingabe" }
      responses:
        "201":
          description: Buchung wurde angelegt
          content:
            application/json:
              schema: { $ref: "#/components/schemas/Buchung" }
        "409":
          description: Doppelbuchung — Raum im gewünschten Zeitraum bereits belegt
          content:
            application/json:
              schema: { $ref: "#/components/schemas/Fehler" }
        "422":
          description: Buchungsdaten ungültig (z. B. Vorlauf-/Mengenlimit überschritten)
          content:
            application/json:
              schema: { $ref: "#/components/schemas/Fehler" }

  /buchungen/{buchungId}:
    get:
      tags: [Buchungskern]
      summary: Einzelne Buchung abrufen
      parameters:
        - $ref: "#/components/parameters/BuchungId"
      responses:
        "200":
          description: Die Buchung
          content:
            application/json:
              schema: { $ref: "#/components/schemas/Buchung" }
        "404":
          description: Buchung nicht gefunden
          content:
            application/json:
              schema: { $ref: "#/components/schemas/Fehler" }

    patch:
      tags: [Buchungskern]
      summary: Buchung ändern (z. B. Zeitraum verschieben)
      security: [{ bearerAuth: [] }]
      parameters:
        - $ref: "#/components/parameters/BuchungId"
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: "#/components/schemas/BuchungAenderung" }
      responses:
        "200":
          description: Buchung wurde geändert
          content:
            application/json:
              schema: { $ref: "#/components/schemas/Buchung" }
        "403":
          description: Berechtigung verweigert (nicht eigene Buchung)
          content:
            application/json:
              schema: { $ref: "#/components/schemas/Fehler" }
        "409":
          description: Neuer Zeitraum kollidiert mit anderer Buchung
          content:
            application/json:
              schema: { $ref: "#/components/schemas/Fehler" }

    delete:
      tags: [Buchungskern]
      summary: Buchung stornieren
      security: [{ bearerAuth: [] }]
      parameters:
        - $ref: "#/components/parameters/BuchungId"
      responses:
        "204":
          description: Buchung wurde storniert
        "403":
          description: Berechtigung verweigert
          content:
            application/json:
              schema: { $ref: "#/components/schemas/Fehler" }
        "404":
          description: Buchung nicht gefunden
          content:
            application/json:
              schema: { $ref: "#/components/schemas/Fehler" }

  # ---------- Zugangskontrolle (EC-4) ----------

  /auth/login:
    post:
      tags: [Zugangskontrolle]
      summary: Uni-Login (SAML/OAuth-Austausch, Protokoll noch offen laut EC-4)
      security: []
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: "#/components/schemas/LoginAnfrage" }
      responses:
        "200":
          description: Login erfolgreich, Token wird zurückgegeben
          content:
            application/json:
              schema: { $ref: "#/components/schemas/LoginAntwort" }
        "401":
          description: Login fehlgeschlagen
          content:
            application/json:
              schema: { $ref: "#/components/schemas/Fehler" }

  /nutzer/{nutzerId}/rollen:
    get:
      tags: [Zugangskontrolle]
      summary: Rollen eines Nutzers abrufen (Nutzer.rollen)
      security: [{ bearerAuth: [] }]
      parameters:
        - $ref: "#/components/parameters/NutzerId"
      responses:
        "200":
          description: Rollen des Nutzers
          content:
            application/json:
              schema:
                type: array
                items: { $ref: "#/components/schemas/Rolle" }
        "404":
          description: Nutzer nicht gefunden
          content:
            application/json:
              schema: { $ref: "#/components/schemas/Fehler" }

    put:
      tags: [Zugangskontrolle]
      summary: Rollen eines Nutzers setzen (nur Admin, siehe EC-4 "Wer darf Rollen vergeben?")
      security: [{ bearerAuth: [] }]
      parameters:
        - $ref: "#/components/parameters/NutzerId"
      requestBody:
        required: true
        content:
          application/json:
            schema:
              type: array
              items: { type: string }
              description: Liste von Rollennamen
      responses:
        "200":
          description: Rollen wurden aktualisiert
          content:
            application/json:
              schema:
                type: array
                items: { $ref: "#/components/schemas/Rolle" }
        "403":
          description: Berechtigung verweigert (nur Admins dürfen Rollen vergeben)
          content:
            application/json:
              schema: { $ref: "#/components/schemas/Fehler" }

components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT

  parameters:
    RaumId:
      name: raumId
      in: path
      required: true
      schema: { type: string }
    BuchungId:
      name: buchungId
      in: path
      required: true
      schema: { type: string }
    NutzerId:
      name: nutzerId
      in: path
      required: true
      schema: { type: string }

  schemas:
    Raum:
      type: object
      properties:
        id: { type: string }
        name: { type: string }
        kapazitaet: { type: integer, minimum: 1 }
        ausstattung:
          type: array
          items: { type: string }
        kategorie: { type: string }
        status:
          type: string
          enum: [AKTIV, GESPERRT]
      required: [id, name, kapazitaet, status]

    RaumEingabe:
      type: object
      properties:
        name: { type: string }
        kapazitaet: { type: integer, minimum: 1 }
        ausstattung:
          type: array
          items: { type: string }
        kategorie: { type: string }
      required: [name, kapazitaet]

    Buchung:
      type: object
      properties:
        id: { type: string }
        raumId: { type: string }
        nutzerId: { type: string }
        von: { type: string, format: date-time }
        bis: { type: string, format: date-time }
        status:
          type: string
          enum: [GEPLANT, BESTAETIGT, STORNIERT, NICHT_ANGETRETEN]
      required: [id, raumId, nutzerId, von, bis, status]

    BuchungEingabe:
      type: object
      properties:
        raumId: { type: string }
        von: { type: string, format: date-time }
        bis: { type: string, format: date-time }
      required: [raumId, von, bis]

    BuchungAenderung:
      type: object
      properties:
        von: { type: string, format: date-time }
        bis: { type: string, format: date-time }

    Nutzer:
      type: object
      properties:
        id: { type: string }
        name: { type: string }
        rollen:
          type: array
          items: { $ref: "#/components/schemas/Rolle" }
      required: [id, name]

    Rolle:
      type: object
      properties:
        name: { type: string }
        berechtigungen:
          type: array
          items: { type: string }
      required: [name]

    LoginAnfrage:
      type: object
      properties:
        uniKennung: { type: string }
        credential: { type: string }
      required: [uniKennung, credential]

    LoginAntwort:
      type: object
      properties:
        token: { type: string }
        gueltigBis: { type: string, format: date-time }
      required: [token, gueltigBis]

    Fehler:
      type: object
      properties:
        code: { type: string }
        nachricht: { type: string }
      required: [code, nachricht]
```

## Offene Punkte für euch

- **Buchungskern-Endpunkte prüfen**: Sobald ihr `Buchungskern` auf Klassenebene entwerft (analog zu Raumkatalog), gleicht ab, ob Methodennamen/-signaturen zu den hier angenommenen Endpunkten passen — insbesondere Serienbuchungen und Konfliktpriorisierung sind hier noch nicht abgebildet.
- **`DELETE /raeume/{raumId}` mit 409**: Bildet eure offene EC-2-Frage ab ("was passiert beim Löschen mit bestehenden Buchungen") — falls ihr das anders lösen wollt (z. B. Soft-Delete statt Fehler), muss der Vertrag angepasst werden.
- **Login-Protokoll**: `/auth/login` ist bewusst allgemein gehalten, da SAML/OAuth/LDAP bei euch laut EC-4 noch nicht entschieden ist — bei SAML würde der Endpunkt anders aussehen (Redirect-Flow statt direktem POST mit Credentials).
