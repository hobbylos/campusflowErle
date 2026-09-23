/**
 * CampusFlow - OpenAPI REST API Client (v0.1.0)
 * 100% konform zum Vertrag campusflow_openapi.md
 * 
 * Unterstützt Live-Modus (/api/v1) und interaktiven Mock-Modus für Präsentation/Tests.
 */

class CampusFlowApiClient {
  constructor() {
    this.baseUrl = "/api/v1";
    this.mode = safeGetItem("campusflow_api_mode") || "mock"; // 'live' oder 'mock'
    this.token = safeGetItem("campusflow_jwt_token") || null;
    try {
      this.currentUser = JSON.parse(safeGetItem("campusflow_current_user") || "null");
    } catch (e) {
      this.currentUser = null;
    }
  }

  saveSession() {
    if (this.token) safeSetItem("campusflow_jwt_token", this.token);
    else safeRemoveItem("campusflow_jwt_token");

    if (this.currentUser) safeSetItem("campusflow_current_user", JSON.stringify(this.currentUser));
    else safeRemoveItem("campusflow_current_user");

    safeSetItem("campusflow_api_mode", this.mode);
  }

  setMode(newMode) {
    this.mode = newMode;
    this.saveSession();
  }

  getHeaders() {
    const headers = {
      "Content-Type": "application/json",
      "Accept": "application/json"
    };
    if (this.token) {
      headers["Authorization"] = `Bearer ${this.token}`;
    }
    return headers;
  }

  hasPermission(action) {
    if (!this.currentUser || !this.currentUser.rollen) return false;
    for (const role of this.currentUser.rollen) {
      const roleName = typeof role === "string" ? role : role.name;
      if (roleName === "ADMIN") return true;
      if (typeof role === "object" && role.berechtigungen && role.berechtigungen.includes(action)) return true;
      if (roleName === "NUTZER" || roleName === "STUDENT") {
        if (["READ", "BOOK"].includes(action)) return true;
      }
      if (roleName === "DOZENT") {
        if (["READ", "BOOK", "UPDATE_OWN_BOOKING"].includes(action)) return true;
      }
    }
    return false;
  }

  // ==========================================
  // ---------- Raumkatalog (EC-2) ------------
  // ==========================================

  /**
   * GET /api/v1/raeume
   * Parameter: gebaeude, kategorie, minKapazitaet, ausstattung
   */
  async getRaeume(params = {}) {
    if (this.mode === "live") {
      const query = new URLSearchParams();
      if (params.gebaeude) query.append("gebaeude", params.gebaeude);
      if (params.kategorie) query.append("kategorie", params.kategorie);
      if (params.minKapazitaet) query.append("minKapazitaet", params.minKapazitaet);
      if (params.ausstattung) query.append("ausstattung", params.ausstattung);

      const res = await fetch(`${this.baseUrl}/raeume?${query.toString()}`, {
        headers: this.getHeaders()
      });
      if (!res.ok) throw await this.handleError(res);
      return await res.json();
    }

    // Mock-Logik
    await this.delay(60);
    const db = getMockStorage();
    let result = [...db.rooms];

    if (params.gebaeude) {
      result = result.filter(r => r.gebaeude && r.gebaeude.toLowerCase() === params.gebaeude.toLowerCase());
    }
    if (params.kategorie) {
      result = result.filter(r => r.kategorie && r.kategorie.toLowerCase() === params.kategorie.toLowerCase());
    }
    if (params.minKapazitaet) {
      result = result.filter(r => r.kapazitaet >= parseInt(params.minKapazitaet, 10));
    }
    if (params.ausstattung) {
      const needed = params.ausstattung.split(",").map(s => s.trim().toLowerCase());
      result = result.filter(r => 
        needed.every(feat => r.ausstattung && r.ausstattung.some(ea => ea.toLowerCase().includes(feat)))
      );
    }
    if (params.status) {
      result = result.filter(r => r.status === params.status);
    }
    return result;
  }

  /**
   * POST /api/v1/raeume
   * Body: RaumEingabe { name, kapazitaet, ausstattung, kategorie, gebaeude }
   */
  async createRaum(raumEingabe) {
    if (this.mode === "live") {
      const res = await fetch(`${this.baseUrl}/raeume`, {
        method: "POST",
        headers: this.getHeaders(),
        body: JSON.stringify(raumEingabe)
      });
      if (!res.ok) throw await this.handleError(res);
      return await res.json();
    }

    // Mock-Validierung nach OpenAPI
    await this.delay(100);
    if (!this.hasPermission("CREATE")) {
      throw {
        status: 403,
        code: "BERECHTIGUNG_VERWEIGERT",
        nachricht: "Zugriff verweigert: Nur Administratoren dürfen neue Räume anlegen."
      };
    }

    if (!raumEingabe.name || raumEingabe.name.trim() === "") {
      throw {
        status: 422,
        code: "INVALID_ROOM_DATA",
        nachricht: "Pflichtfeld verletzt: Raumname darf nicht leer sein."
      };
    }

    if (raumEingabe.kapazitaet === undefined || raumEingabe.kapazitaet === null || raumEingabe.kapazitaet <= 0) {
      throw {
        status: 422,
        code: "INVARIANTE_VERLETZT",
        nachricht: "Raumdaten verletzen eine Invariante: Kapazität muss mindestens 1 sein (eingegeben: " + raumEingabe.kapazitaet + ")."
      };
    }

    const db = getMockStorage();
    const newRoom = {
      id: "room-" + (Date.now() % 10000),
      name: raumEingabe.name.trim(),
      kapazitaet: parseInt(raumEingabe.kapazitaet, 10),
      ausstattung: raumEingabe.ausstattung || [],
      kategorie: raumEingabe.kategorie || "Seminarraum",
      gebaeude: raumEingabe.gebaeude || "Haus A",
      status: "AKTIV"
    };

    db.rooms.unshift(newRoom);
    saveMockStorage(db);
    return newRoom;
  }

  /**
   * GET /api/v1/raeume/{raumId}
   */
  async getRaum(raumId) {
    if (this.mode === "live") {
      const res = await fetch(`${this.baseUrl}/raeume/${encodeURIComponent(raumId)}`, {
        headers: this.getHeaders()
      });
      if (!res.ok) throw await this.handleError(res);
      return await res.json();
    }

    await this.delay(40);
    const db = getMockStorage();
    const room = db.rooms.find(r => r.id === raumId);
    if (!room) {
      throw {
        status: 404,
        code: "ROOM_NOT_FOUND",
        nachricht: `Raum mit ID '${raumId}' wurde nicht gefunden.`
      };
    }
    return room;
  }

  /**
   * PUT /api/v1/raeume/{raumId}
   */
  async updateRaum(raumId, raumEingabe) {
    if (this.mode === "live") {
      let payload = { ...raumEingabe };
      if (!payload.name || payload.kapazitaet === undefined || payload.kapazitaet === null) {
        try {
          const current = await this.getRaum(raumId);
          payload.name = payload.name || current.name;
          if (payload.kapazitaet === undefined || payload.kapazitaet === null) {
            payload.kapazitaet = current.kapazitaet;
          }
          payload.kategorie = payload.kategorie || current.kategorie;
          payload.ausstattung = payload.ausstattung || current.ausstattung || [];
        } catch (e) {}
      }
      const res = await fetch(`${this.baseUrl}/raeume/${encodeURIComponent(raumId)}`, {
        method: "PUT",
        headers: this.getHeaders(),
        body: JSON.stringify(payload)
      });
      if (!res.ok) throw await this.handleError(res);
      return await res.json();
    }

    await this.delay(80);
    if (!this.hasPermission("UPDATE")) {
      throw {
        status: 403,
        code: "BERECHTIGUNG_VERWEIGERT",
        nachricht: "Zugriff verweigert: Nur Administratoren dürfen Raumdaten bearbeiten."
      };
    }

    const db = getMockStorage();
    const index = db.rooms.findIndex(r => r.id === raumId);
    if (index === -1) {
      throw {
        status: 404,
        code: "ROOM_NOT_FOUND",
        nachricht: `Raum mit ID '${raumId}' wurde nicht gefunden.`
      };
    }

    if (raumEingabe.kapazitaet !== undefined && raumEingabe.kapazitaet !== null && Number(raumEingabe.kapazitaet) <= 0) {
      throw {
        status: 422,
        code: "INVARIANTE_VERLETZT",
        nachricht: "Invariante verletzt: Kapazität muss mindestens 1 sein (eingegeben: " + raumEingabe.kapazitaet + ")."
      };
    }

    const current = db.rooms[index];
    const updated = {
      ...current,
      name: raumEingabe.name !== undefined ? raumEingabe.name : current.name,
      kapazitaet: raumEingabe.kapazitaet !== undefined ? parseInt(raumEingabe.kapazitaet, 10) : current.kapazitaet,
      ausstattung: raumEingabe.ausstattung !== undefined ? raumEingabe.ausstattung : current.ausstattung,
      kategorie: raumEingabe.kategorie !== undefined ? raumEingabe.kategorie : current.kategorie,
      gebaeude: raumEingabe.gebaeude !== undefined ? raumEingabe.gebaeude : current.gebaeude,
      status: raumEingabe.status !== undefined ? raumEingabe.status : current.status
    };

    db.rooms[index] = updated;
    saveMockStorage(db);
    return updated;
  }

  /**
   * POST /api/v1/raeume/{raumId}/sperren
   */
  async sperreRaum(raumId, von = null, bis = null) {
    if (this.mode === "live") {
      const body = (von || bis) ? { von, bis } : {};
      const res = await fetch(`${this.baseUrl}/raeume/${encodeURIComponent(raumId)}/sperren`, {
        method: "POST",
        headers: this.getHeaders(),
        body: JSON.stringify(body)
      });
      if (!res.ok) throw await this.handleError(res);
      return await res.json();
    }

    await this.delay(60);
    if (!this.hasPermission("UPDATE")) {
      throw {
        status: 403,
        code: "BERECHTIGUNG_VERWEIGERT",
        nachricht: "Zugriff verweigert: Nur Administratoren dürfen Räume sperren."
      };
    }

    const db = getMockStorage();
    const room = db.rooms.find(r => r.id === raumId);
    if (!room) {
      throw {
        status: 404,
        code: "ROOM_NOT_FOUND",
        nachricht: `Raum mit ID '${raumId}' wurde nicht gefunden.`
      };
    }

    room.status = "GESPERRT";
    room.sperrVon = von || new Date().toISOString();
    room.sperrBis = bis || new Date(Date.now() + 3650 * 86400000).toISOString();

    // Automatische Stornierung aller aktiven Buchungen im Sperrzeitraum (EC-10)
    let canceledCount = 0;
    const lockStart = new Date(room.sperrVon);
    const lockEnd = new Date(room.sperrBis);

    db.bookings.forEach(b => {
      if (b.raumId === raumId && b.status !== "STORNIERT") {
        const bStart = new Date(b.von);
        const bEnd = new Date(b.bis);
        if (lockStart < bEnd && lockEnd > bStart) {
          b.status = "STORNIERT";
          canceledCount++;
        }
      }
    });

    saveMockStorage(db);
    return { ...room, canceledBookings: canceledCount };
  }

  /**
   * POST /api/v1/raeume/{raumId}/entsperren
   */
  async entsperreRaum(raumId) {
    if (this.mode === "live") {
      try {
        const res = await fetch(`${this.baseUrl}/raeume/${encodeURIComponent(raumId)}/entsperren`, {
          method: "POST",
          headers: this.getHeaders()
        });
        if (res.ok) return await res.json();
      } catch (e) {}

      // Fallback für vorherige Backend-Instanz
      const currentRoom = await this.getRaum(raumId);
      return await this.updateRaum(raumId, {
        name: currentRoom.name,
        kapazitaet: currentRoom.kapazitaet,
        kategorie: currentRoom.kategorie,
        status: "AKTIV"
      });
    }

    await this.delay(60);
    const db = getMockStorage();
    const room = db.rooms.find(r => r.id === raumId);
    if (!room) {
      throw {
        status: 404,
        code: "ROOM_NOT_FOUND",
        nachricht: `Raum mit ID '${raumId}' wurde nicht gefunden.`
      };
    }

    room.status = "AKTIV";
    room.sperrVon = null;
    room.sperrBis = null;
    saveMockStorage(db);
    return room;
  }

  /**
   * DELETE /api/v1/raeume/{raumId}
   * Buchungen werden automatisch mitstorniert!
   */
  async deleteRaum(raumId) {
    if (this.mode === "live") {
      const res = await fetch(`${this.baseUrl}/raeume/${encodeURIComponent(raumId)}`, {
        method: "DELETE",
        headers: this.getHeaders()
      });
      if (!res.ok) throw await this.handleError(res);
      return true;
    }

    await this.delay(80);
    if (!this.hasPermission("DELETE")) {
      throw {
        status: 403,
        code: "BERECHTIGUNG_VERWEIGERT",
        nachricht: "Zugriff verweigert: Nur Administratoren dürfen Räume löschen."
      };
    }

    const db = getMockStorage();
    const room = db.rooms.find(r => r.id === raumId);
    if (!room) {
      throw {
        status: 404,
        code: "ROOM_NOT_FOUND",
        nachricht: `Raum mit ID '${raumId}' wurde nicht gefunden.`
      };
    }

    // Automatische Stornierung aller Buchungen für den gelöschten Raum
    db.bookings.forEach(b => {
      if (b.raumId === raumId) {
        b.status = "STORNIERT";
      }
    });

    db.rooms = db.rooms.filter(r => r.id !== raumId);
    saveMockStorage(db);
    return true;
  }

  // ==========================================
  // ---------- Buchungskern (EC-3) -----------
  // ==========================================

  /**
   * GET /api/v1/buchungen
   * Parameter: raumId, nutzerId, status, von, bis
   */
  async getBuchungen(params = {}) {
    if (this.mode === "live") {
      const query = new URLSearchParams();
      if (params.raumId) query.append("raumId", params.raumId);
      if (params.nutzerId) query.append("nutzerId", params.nutzerId);
      if (params.status) query.append("status", params.status);
      if (params.von) query.append("von", params.von);
      if (params.bis) query.append("bis", params.bis);

      try {
        const res = await fetch(`${this.baseUrl}/buchungen?${query.toString()}`, {
          headers: this.getHeaders()
        });
        if (res.ok) {
          const list = await res.json();
          return Array.isArray(list) ? list : [list];
        }
        if (res.status === 404 || res.status === 405) {
          const resMeine = await fetch(`${this.baseUrl}/buchungen/meine`, {
            headers: this.getHeaders()
          });
          if (resMeine.ok) {
            const list = await resMeine.json();
            return Array.isArray(list) ? list : [list];
          }
        }
        throw await this.handleError(res);
      } catch (err) {
        if (err.status) throw err;
        try {
          const resMeine = await fetch(`${this.baseUrl}/buchungen/meine`, {
            headers: this.getHeaders()
          });
          if (resMeine.ok) {
            const list = await resMeine.json();
            return Array.isArray(list) ? list : [list];
          }
        } catch (e2) {}
        throw err;
      }
    }

    await this.delay(60);
    const db = getMockStorage();
    let list = [...db.bookings];

    if (params.raumId) list = list.filter(b => b.raumId === params.raumId);
    if (params.nutzerId) list = list.filter(b => b.nutzerId === params.nutzerId);
    if (params.status) list = list.filter(b => b.status === params.status);

    return list;
  }

  /**
   * POST /api/v1/buchungen
   * Body: BuchungEingabe { raumId, von, bis, zweck }
   */
  async createBuchung(buchungEingabe) {
    if (this.mode === "live") {
      const res = await fetch(`${this.baseUrl}/buchungen`, {
        method: "POST",
        headers: this.getHeaders(),
        body: JSON.stringify(buchungEingabe)
      });
      if (!res.ok) throw await this.handleError(res);
      return await res.json();
    }

    await this.delay(100);
    const db = getMockStorage();
    const room = db.rooms.find(r => r.id === buchungEingabe.raumId);

    if (!room) {
      throw {
        status: 404,
        code: "ROOM_NOT_FOUND",
        nachricht: "Der ausgewählte Raum existiert nicht."
      };
    }

    if (room.status === "GESPERRT") {
      throw {
        status: 409,
        code: "RAUM_GESPERRT",
        nachricht: `Raum '${room.name}' ist aktuell gesperrt und kann nicht gebucht werden.`
      };
    }

    const start = new Date(buchungEingabe.von);
    const end = new Date(buchungEingabe.bis);

    if (isNaN(start.getTime()) || isNaN(end.getTime()) || start >= end) {
      throw {
        status: 422,
        code: "ZEITRAUM_UNGUELTIG",
        nachricht: "Ungültiger Zeitraum: Startzeitpunkt muss vor dem Endzeitpunkt liegen."
      };
    }

    // Doppelbuchungsprüfung (EC-12 / OpenAPI 409)
    const conflict = db.bookings.find(b => {
      if (b.raumId !== buchungEingabe.raumId) return false;
      if (b.status === "STORNIERT") return false;
      const bStart = new Date(b.von);
      const bEnd = new Date(b.bis);
      // Überschneidung wenn: Start < bEnd && End > bStart
      return start < bEnd && end > bStart;
    });

    if (conflict) {
      const confStart = new Date(conflict.von).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
      const confEnd = new Date(conflict.bis).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
      throw {
        status: 409,
        code: "DOPPELBUCHUNG",
        nachricht: `Doppelbuchung erkannt: Raum '${room.name}' ist im gewünschten Zeitraum bereits belegt (${confStart} - ${confEnd} Uhr).`
      };
    }

    const newBooking = {
      id: "book-" + (Date.now() % 10000),
      raumId: buchungEingabe.raumId,
      nutzerId: this.currentUser ? this.currentUser.id : "usr-guest",
      zweck: buchungEingabe.zweck || "Veranstaltung",
      von: start.toISOString(),
      bis: end.toISOString(),
      status: "BESTAETIGT"
    };

    db.bookings.unshift(newBooking);
    saveMockStorage(db);
    return newBooking;
  }

  /**
   * DELETE /api/v1/buchungen/{buchungId} (Stornieren)
   */
  async cancelBuchung(buchungId) {
    if (this.mode === "live") {
      const res = await fetch(`${this.baseUrl}/buchungen/${encodeURIComponent(buchungId)}`, {
        method: "DELETE",
        headers: this.getHeaders()
      });
      if (!res.ok) throw await this.handleError(res);
      return true;
    }

    await this.delay(80);
    const db = getMockStorage();
    const index = db.bookings.findIndex(b => b.id === buchungId);
    if (index === -1) {
      throw {
        status: 404,
        code: "BUCHUNG_NICHT_GEFUNDEN",
        nachricht: `Buchung '${buchungId}' wurde nicht gefunden.`
      };
    }

    db.bookings[index].status = "STORNIERT";
    saveMockStorage(db);
    return true;
  }

  // ==========================================
  // ---------- Zugangskontrolle (EC-4) -------
  // ==========================================

  /**
   * POST /api/v1/auth/login
   * Body: LoginAnfrage { uniKennung, credential }
   * Returns: LoginAntwort { token, gueltigBis }
   */
  async login(uniKennung, credential) {
    const rawUser = (uniKennung || "").trim();
    const cleanUser = rawUser.toLowerCase();
    const cleanPw = (credential || "").trim();

    // Mapping von Aliassen für das Backend (z.B. admin -> admin01, student -> stud01)
    let backendUser = rawUser;
    if (cleanUser === "admin" || cleanUser === "lukas" || cleanUser === "verwaltung") {
      backendUser = "admin01";
    } else if (cleanUser === "student" || cleanUser === "anna" || cleanUser === "stud") {
      backendUser = "stud01";
    }

    if (this.mode === "live") {
      try {
        // Versuch 1: Mit backendUser (z. B. admin01)
        let res = await fetch(`${this.baseUrl}/auth/login`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ uniKennung: backendUser, credential: cleanPw })
        });

        // Falls mit backendUser nicht erfolgreich, aber rawUser abweicht, Versuch mit rawUser
        if (!res.ok && backendUser !== rawUser) {
          const retryRes = await fetch(`${this.baseUrl}/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ uniKennung: rawUser, credential: cleanPw })
          });
          if (retryRes.ok) res = retryRes;
        }

        if (res.ok) {
          const data = await res.json();
          this.token = data.token;

          if (data.user) {
            this.currentUser = {
              id: data.user.id,
              name: data.user.name,
              uniKennung: data.user.uniKennung || rawUser,
              rollen: (data.user.rollen || []).map(r => typeof r === "string" ? { name: r, berechtigungen: r === "ADMIN" ? ["CREATE", "READ", "UPDATE", "DELETE", "BOOK", "MANAGE_ROLES"] : ["READ", "BOOK"] } : r)
            };
          } else {
            // Aus Uni-Kennung ableiten
            const isAdmin = backendUser === "admin01" || cleanUser.includes("admin") || cleanUser.includes("lukas");
            this.currentUser = {
              id: isAdmin ? "n-1" : "n-2",
              name: isAdmin ? "Admin Verwaltung (Lukas)" : "Test Student (Anna)",
              uniKennung: rawUser,
              rollen: isAdmin
                ? [{ name: "ADMIN", berechtigungen: ["CREATE", "READ", "UPDATE", "DELETE", "BOOK", "MANAGE_ROLES"] }]
                : [{ name: "NUTZER", berechtigungen: ["READ", "BOOK"] }]
            };
          }
          this.saveSession();
          return { ...data, user: this.currentUser };
        } else {
          // Echter Fehler vom Backend (401 oder 403)
          throw await this.handleError(res);
        }
      } catch (err) {
        // Falls echter Fehler vom Backend (hat status) -> werfen!
        if (err.status) throw err;
        // Bei Verbindungsabbruch / Server offline -> Fallback auf Mock
        console.warn("Backend nicht erreichbar, Fallback auf Mock-Modus", err);
      }
    }

    // Mock-Logik
    await this.delay(60);
    const db = getMockStorage();

    // Alias-Unterstützung (lukas -> admin, dozent -> schneider, student -> anna, admin01 -> admin, stud01 -> student)
    let foundUser = db.users.find(u => {
      const uname = u.uniKennung.toLowerCase();
      if (uname === cleanUser) return true;
      if (cleanUser === "admin" && (uname === "admin01" || uname === "admin")) return true;
      if (cleanUser === "admin01" && (uname === "admin01" || uname === "admin")) return true;
      if (cleanUser === "lukas" && uname === "admin") return true;
      if ((cleanUser === "schneider" || cleanUser === "prof") && uname === "dozent") return true;
      if ((cleanUser === "anna" || cleanUser === "mueller" || cleanUser === "stud01") && (uname === "student" || uname === "stud01")) return true;
      if (cleanUser === "student" && (uname === "student" || uname === "stud01")) return true;
      return false;
    });

    // Falls Nutzer nicht existiert, aber Zugangsdaten angegeben wurden -> dynamischer Test-Nutzer
    if (!foundUser && cleanUser.length > 0 && (cleanPw === "pass" || cleanPw === "admin" || cleanPw === "1234" || cleanPw === "password")) {
      const isAdmin = cleanUser.includes("admin") || cleanUser.includes("lukas");
      foundUser = {
        id: "usr-" + cleanUser,
        name: cleanUser.charAt(0).toUpperCase() + cleanUser.slice(1) + " (Benutzer)",
        uniKennung: cleanUser,
        password: cleanPw,
        rollen: isAdmin
          ? [{ name: "ADMIN", berechtigungen: ["CREATE", "READ", "UPDATE", "DELETE", "BOOK", "MANAGE_ROLES"] }]
          : [{ name: "STUDENT", berechtigungen: ["READ", "BOOK"] }]
      };
      db.users.push(foundUser);
      saveMockStorage(db);
    }

    // Erlaubte Passwörter für Demo (gespeichertes PW oder Standard-Test-PWs)
    const validPasswords = [foundUser?.password, "pass", "admin", "password", "1234"].filter(Boolean);
    const isPwValid = foundUser && validPasswords.includes(cleanPw);

    if (!foundUser || !isPwValid) {
      throw {
        status: 401,
        code: "AUTHENTIFIZIERUNG_FEHLGESCHLAGEN",
        nachricht: "Login nicht möglich: Ungültige Uni-Kennung oder falsches Passwort."
      };
    }

    this.currentUser = foundUser;
    this.token = "mock-jwt-token-" + foundUser.id + "-" + Date.now();
    const response = {
      token: this.token,
      gueltigBis: new Date(Date.now() + 3600000 * 8).toISOString(),
      user: foundUser
    };
    this.saveSession();
    return response;
  }

  logout() {
    this.token = null;
    this.currentUser = null;
    this.saveSession();
  }

  /**
   * GET /api/v1/nutzer/{nutzerId}/rollen
   */
  async getNutzerRollen(nutzerId) {
    if (this.mode === "live") {
      const res = await fetch(`${this.baseUrl}/nutzer/${encodeURIComponent(nutzerId)}/rollen`, {
        headers: this.getHeaders()
      });
      if (!res.ok) throw await this.handleError(res);
      return await res.json();
    }

    await this.delay(50);
    const db = getMockStorage();
    const u = db.users.find(x => x.id === nutzerId);
    if (!u) {
      throw {
        status: 404,
        code: "USER_NOT_FOUND",
        nachricht: `Nutzer '${nutzerId}' nicht gefunden.`
      };
    }
    return u.rollen;
  }

  /**
   * PUT /api/v1/nutzer/{nutzerId}/rollen
   * Body: string[] (z. B. ["ADMIN", "DOZENT"])
   */
  async updateNutzerRollen(nutzerId, roleNames) {
    if (this.mode === "live") {
      const res = await fetch(`${this.baseUrl}/nutzer/${encodeURIComponent(nutzerId)}/rollen`, {
        method: "PUT",
        headers: this.getHeaders(),
        body: JSON.stringify(roleNames)
      });
      if (!res.ok) throw await this.handleError(res);
      return await res.json();
    }

    await this.delay(80);
    if (!this.hasPermission("MANAGE_ROLES")) {
      throw {
        status: 403,
        code: "BERECHTIGUNG_VERWEIGERT",
        nachricht: "Berechtigung verweigert: Nur Administratoren dürfen Rollen vergeben."
      };
    }

    const db = getMockStorage();
    const userIndex = db.users.findIndex(u => u.id === nutzerId);
    if (userIndex === -1) {
      throw {
        status: 404,
        code: "USER_NOT_FOUND",
        nachricht: `Nutzer '${nutzerId}' nicht gefunden.`
      };
    }

    // Rollen generieren
    const newRoles = roleNames.map(name => {
      let perms = ["READ", "BOOK"];
      if (name === "ADMIN") perms = ["CREATE", "READ", "UPDATE", "DELETE", "BOOK", "MANAGE_ROLES"];
      if (name === "DOZENT") perms = ["READ", "BOOK", "UPDATE_OWN_BOOKING"];
      return { name, berechtigungen: perms };
    });

    db.users[userIndex].rollen = newRoles;
    if (this.currentUser && this.currentUser.id === nutzerId) {
      this.currentUser.rollen = newRoles;
    }
    saveMockStorage(db);
    this.saveSession();
    return newRoles;
  }

  // ==========================================
  // ---------- Hilfsfunktionen ---------------
  // ==========================================

  async handleError(res) {
    let errBody = {};
    try {
      errBody = await res.json();
    } catch (e) {
      errBody = { nachricht: res.statusText };
    }

    const err = {
      status: res.status,
      code: errBody.code || "HTTP_" + res.status,
      nachricht: errBody.nachricht || res.statusText || "Serverfehler aufgetreten."
    };

    // Automatisches Abmelden bei ungültigem / abgelaufenem Token (z.B. nach Server-Neustart)
    if (res.status === 403 && (err.nachricht.includes("Nicht angemeldet") || err.nachricht.includes("Token abgelaufen") || err.nachricht.includes("Authorization-Header"))) {
      this.logout();
      if (typeof checkAuthGateState === "function") {
        checkAuthGateState();
      }
      err.nachricht = "Deine Sitzung ist abgelaufen oder der Server wurde neu gestartet. Bitte melde dich erneut an.";
    }

    return err;
  }

  delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  // Schneller Backend-Verfügbarkeits-Check
  async checkBackendAvailability() {
    try {
      const controller = new AbortController();
      const id = setTimeout(() => controller.abort(), 800);
      const res = await fetch(`${this.baseUrl}/raeume`, { signal: controller.signal });
      clearTimeout(id);
      return res.status !== 404 && res.status !== 502 && res.status !== 503;
    } catch (e) {
      return false;
    }
  }
}

// Globaler Singleton-Export
window.api = new CampusFlowApiClient();
