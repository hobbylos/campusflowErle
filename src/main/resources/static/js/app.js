/**
 * CampusFlow - Hauptanwendungslogik (app.js)
 * Steuerung der Views, Formulare, Dialoge, Toast-Meldungen und Demo-Präsentation
 */

document.addEventListener("DOMContentLoaded", () => {
  initNavigation();
  initUserSessionUI();
  initFilters();
  initModals();
  initDemoActions();

  // Prüfen, ob bereits angemeldet
  checkAuthGateState();
  checkApiStatus();
});

// ==========================================
// 1. Toast & Benachrichtigungssystem
// ==========================================

function showToast(type, title, message, statusCode = null) {
  const container = document.getElementById("toast-container");
  if (!container) return;

  const toast = document.createElement("div");
  toast.className = `toast toast-${type}`;

  let icon = "✓";
  if (type === "error") icon = "✕";
  if (type === "warning") icon = "⚠";

  const statusBadge = statusCode ? `<span style="font-family: var(--font-mono); font-size: 0.75rem; background: rgba(0,0,0,0.06); padding: 2px 6px; border-radius: 4px; margin-left: 6px;">HTTP ${statusCode}</span>` : "";

  toast.innerHTML = `
    <div class="toast-icon">${icon}</div>
    <div class="toast-content">
      <div class="toast-title">${title} ${statusBadge}</div>
      <div class="toast-message">${message}</div>
    </div>
    <button class="toast-close" onclick="this.parentElement.remove()">×</button>
  `;

  container.appendChild(toast);

  // Automatisch nach 5 Sekunden ausblenden
  setTimeout(() => {
    toast.style.opacity = "0";
    toast.style.transform = "translateX(40px)";
    setTimeout(() => toast.remove(), 300);
  }, 5000);
}

// ==========================================
// 2. Navigation & View-Routing
// ==========================================

function initNavigation() {
  const tabs = document.querySelectorAll(".nav-tab-btn");
  tabs.forEach(btn => {
    btn.addEventListener("click", () => {
      const target = btn.getAttribute("data-target");

      // Buttons aktualisieren
      tabs.forEach(t => t.classList.remove("active"));
      btn.classList.add("active");

      // Views aktualisieren
      document.querySelectorAll(".tab-view").forEach(v => v.classList.remove("active"));
      const targetView = document.getElementById(target);
      if (targetView) targetView.classList.add("active");

      // Ggf. Daten neu laden
      if (target === "view-rooms") loadRooms();
      if (target === "view-bookings") loadBookings();
      if (target === "view-auth") loadUsersAndRoles();
    });
  });
}

function switchView(viewId) {
  const btn = document.querySelector(`.nav-tab-btn[data-target="${viewId}"]`);
  if (btn) btn.click();
}

// ==========================================
// 3. Benutzer- & Sitzungsverwaltung (EC-4)
// ==========================================

function checkAuthGateState() {
  const gateScreen = document.getElementById("auth-gate-screen");
  const appContainer = document.getElementById("app-container");

  if (!window.api.currentUser) {
    if (gateScreen) gateScreen.classList.remove("hidden");
    if (appContainer) appContainer.classList.add("hidden");
  } else {
    if (gateScreen) gateScreen.classList.add("hidden");
    if (appContainer) appContainer.classList.remove("hidden");
    updateUserDisplay();
    loadRooms();
    loadBookings();
    loadUsersAndRoles();
  }
}

async function handleGateLogin(e) {
  e.preventDefault();
  const username = document.getElementById("gate-username").value;
  const password = document.getElementById("gate-password").value;

  try {
    const resp = await window.api.login(username, password);
    checkAuthGateState();
    showToast("success", "Login erfolgreich!", `Willkommen zurück, ${resp.user?.name || username}!`, 200);
  } catch (err) {
    showToast("error", "Login fehlgeschlagen (401)", err.nachricht, err.status);
  }
}

async function quickLoginAs(username, password) {
  try {
    const resp = await window.api.login(username, password);
    checkAuthGateState();
    showToast("success", "Login erfolgreich!", `Angemeldet als ${resp.user?.name || username}.`, 200);
  } catch (err) {
    showToast("error", "Login fehlgeschlagen (401)", err.nachricht, err.status);
  }
}

function handleLogout() {
  window.api.logout();
  checkAuthGateState();
  const form = document.getElementById("form-gate-login");
  if (form) form.reset();
  showToast("warning", "Abgemeldet", "Du hast dich erfolgreich abgemeldet.", 200);
}

function handleSwitchUser() {
  openModal("modal-user-switch");
}

// Global für Mobile- und Inline-Handler verfügbar machen
window.quickLoginAs = quickLoginAs;
window.handleLogout = handleLogout;
window.handleSwitchUser = handleSwitchUser;
window.quickSwitchUser = quickSwitchUser;

function initUserSessionUI() {
  updateUserDisplay();

  const userPill = document.getElementById("user-pill");
  if (userPill) {
    userPill.addEventListener("click", () => {
      openModal("modal-user-switch");
    });
  }
}

function updateUserDisplay() {
  const user = window.api.currentUser;
  const nameEl = document.getElementById("current-user-name");
  const roleEl = document.getElementById("current-user-role");
  const avatarEl = document.getElementById("current-user-avatar");

  if (!user) {
    if (nameEl) nameEl.textContent = "Nicht angemeldet";
    if (roleEl) roleEl.textContent = "Gast";
    if (avatarEl) avatarEl.textContent = "?";
    return;
  }

  const firstRole = user.rollen && user.rollen[0];
  const mainRole = firstRole ? (typeof firstRole === "string" ? firstRole : firstRole.name) : "NUTZER";
  if (roleEl) roleEl.textContent = mainRole;
  if (avatarEl) avatarEl.textContent = user.name ? user.name.charAt(0) : "U";

  // Buttons sperren/ausblenden je nach Berechtigung (EC-16)
  const isAdmin = window.api.hasPermission("CREATE");
  const adminOnlyButtons = document.querySelectorAll(".admin-only");
  adminOnlyButtons.forEach(el => {
    el.style.display = isAdmin ? "inline-flex" : "none";
  });

  const createRoomBtn = document.getElementById("btn-create-room");
  if (createRoomBtn) {
    createRoomBtn.style.display = isAdmin ? "inline-flex" : "none";
  }
}

async function quickSwitchUser(userId) {
  const db = getMockStorage();
  const target = db.users.find(u => u.id === userId);
  if (!target) return;

  if (window.api.mode === "live") {
    try {
      const resp = await window.api.login(target.uniKennung, target.password || "admin");
      checkAuthGateState();
      closeModal("modal-user-switch");
      showToast("success", "Benutzer gewechselt", `Angemeldet als ${resp.user?.name || target.name}`, 200);
      return;
    } catch (err) {
      showToast("error", "Benutzerwechsel fehlgeschlagen", err.nachricht, err.status);
      return;
    }
  }

  window.api.currentUser = target;
  window.api.token = "mock-bearer-token-" + target.id;
  window.api.saveSession();

  checkAuthGateState();
  closeModal("modal-user-switch");
  showToast("success", "Benutzer gewechselt", `Angemeldet als ${target.name} (${target.rollen[0]?.name})`, 200);
}

// ==========================================
// 4. Epic 2: Raumverwaltung (EC-2)
// ==========================================

let currentFilterTimeout = null;

function initFilters() {
  const searchInput = document.getElementById("room-search-input");
  const buildingFilter = document.getElementById("room-filter-building");
  const categoryFilter = document.getElementById("room-filter-category");
  const capacityFilter = document.getElementById("room-filter-capacity");

  const triggerSearch = () => {
    clearTimeout(currentFilterTimeout);
    currentFilterTimeout = setTimeout(() => loadRooms(), 200);
  };

  if (searchInput) searchInput.addEventListener("input", triggerSearch);
  if (buildingFilter) buildingFilter.addEventListener("change", triggerSearch);
  if (categoryFilter) categoryFilter.addEventListener("change", triggerSearch);
  if (capacityFilter) capacityFilter.addEventListener("change", triggerSearch);
}

async function loadRooms() {
  const isAdmin = window.api.hasPermission("CREATE");
  const createRoomBtn = document.getElementById("btn-create-room");
  if (createRoomBtn) {
    createRoomBtn.style.display = isAdmin ? "inline-flex" : "none";
  }

  const grid = document.getElementById("rooms-grid");
  if (!grid) return;

  const searchInput = document.getElementById("room-search-input");
  const buildingFilter = document.getElementById("room-filter-building");
  const categoryFilter = document.getElementById("room-filter-category");
  const capacityFilter = document.getElementById("room-filter-capacity");

  const params = {};
  if (buildingFilter && buildingFilter.value) params.gebaeude = buildingFilter.value;
  if (categoryFilter && categoryFilter.value) params.kategorie = categoryFilter.value;
  if (capacityFilter && capacityFilter.value) params.minKapazitaet = capacityFilter.value;

  try {
    let rooms = await window.api.getRaeume(params);

    // Textsuche im Namen oder Ausstattung
    if (searchInput && searchInput.value.trim()) {
      const q = searchInput.value.toLowerCase().trim();
      rooms = rooms.filter(r => 
        r.name.toLowerCase().includes(q) || 
        (r.ausstattung && r.ausstattung.some(a => a.toLowerCase().includes(q)))
      );
    }

    renderRooms(rooms);
  } catch (err) {
    showToast("error", "Fehler beim Laden der Räume", err.nachricht, err.status);
  }
}

function renderRooms(rooms) {
  const grid = document.getElementById("rooms-grid");
  if (!grid) return;

  if (rooms.length === 0) {
    grid.innerHTML = `
      <div style="grid-column: 1 / -1; padding: 3rem 1rem; text-align: center; color: var(--text-secondary); background: #fff; border-radius: var(--radius-lg); border: 1px dashed var(--border-light);">
        <div style="font-size: 2rem; margin-bottom: 0.5rem;">🔍</div>
        <div style="font-weight: 700; font-size: 1.1rem; color: var(--text-primary);">Keine passenden Räume gefunden</div>
        <div style="font-size: 0.9rem; margin-top: 0.25rem;">Passe die Filterkriterien an oder lege einen neuen Raum an.</div>
      </div>
    `;
    return;
  }

  const isAdmin = window.api.hasPermission("CREATE");

  grid.innerHTML = rooms.map(room => {
    const isAktiv = room.status === "AKTIV";
    const statusClass = isAktiv ? "aktiv" : "gesperrt";
    const statusText = isAktiv ? "Aktiv / Buchbar" : "Gesperrt";

    const tags = (room.ausstattung || []).map(t => `<span class="tag-badge">${escapeHtml(t)}</span>`).join("");

    return `
      <div class="room-card ${isAktiv ? '' : 'status-gesperrt'}" id="card-${room.id}">
        <div>
          <div class="room-card-header">
            <div>
              <div class="room-name">${escapeHtml(room.name)}</div>
              <span class="room-building-badge">${escapeHtml(room.gebaeude || "Campus")}</span>
            </div>
            <span class="status-badge ${statusClass}">
              <span class="mode-dot"></span>
              ${statusText}
            </span>
          </div>

          <div class="room-details">
            <div class="room-meta-row">
              <span>Kategorie</span>
              <span class="room-meta-val">${escapeHtml(room.kategorie || "Standard")}</span>
            </div>
            <div class="room-meta-row">
              <span>Kapazität</span>
              <span class="room-meta-val">${room.kapazitaet} Plätze</span>
            </div>
            <div style="margin-top: 0.4rem;">
              <span style="font-size: 0.78rem; font-weight: 600; color: var(--text-muted); text-transform: uppercase;">Ausstattung</span>
              <div class="room-tags">${tags || '<span style="font-size: 0.8rem; color: var(--text-muted);">Keine Ausstattung</span>'}</div>
            </div>
          </div>
        </div>

        <div class="room-card-actions">
          ${isAktiv ? `
            <button class="btn btn-primary btn-sm" onclick="openBookingModalForRoom('${room.id}', '${escapeHtml(room.name)}')">
              📅 Buchen
            </button>
          ` : `
            <button class="btn btn-secondary btn-sm" disabled style="opacity: 0.6; cursor: not-allowed;">
              🔒 Gesperrt
            </button>
          `}

          <div style="display: flex; gap: 0.35rem;">
            ${isAdmin ? `
              <button class="btn btn-secondary btn-sm" title="Bearbeiten" onclick="openEditRoomModal('${room.id}')">
                ✏️
              </button>
              <button class="btn btn-secondary btn-sm" title="${isAktiv ? 'Sperren' : 'Entsperren'}" onclick="toggleRoomLock('${room.id}', '${room.status}')">
                ${isAktiv ? '🔒' : '🔓'}
              </button>
              <button class="btn btn-secondary btn-sm" title="Löschen" onclick="confirmDeleteRoom('${room.id}', '${escapeHtml(room.name)}')">
                🗑️
              </button>
            ` : `
              <button class="btn btn-secondary btn-sm" title="Nur für Administratoren" onclick="triggerForbiddenDemo()">
                🔒 Admin
              </button>
            `}
          </div>
        </div>
      </div>
    `;
  }).join("");
}

// Raum anlegen (EC-8)
async function handleCreateRoomSubmit(e) {
  e.preventDefault();
  const name = document.getElementById("create-room-name").value;
  const kapazitaet = document.getElementById("create-room-capacity").value;
  const kategorie = document.getElementById("create-room-category").value;
  const gebaeude = document.getElementById("create-room-building").value;
  const ausstattungStr = document.getElementById("create-room-equipment").value;

  const ausstattung = ausstattungStr
    ? ausstattungStr.split(",").map(s => s.trim()).filter(Boolean)
    : [];

  const payload = {
    name,
    kapazitaet: kapazitaet !== "" ? Number(kapazitaet) : undefined,
    kategorie,
    gebaeude,
    ausstattung
  };

  try {
    const created = await window.api.createRaum(payload);
    closeModal("modal-create-room");
    document.getElementById("form-create-room").reset();
    showToast("success", "Raum erfolgreich angelegt", `'${created.name}' wurde gespeichert und ist jetzt verfügbar.`, 201);
    loadRooms();
  } catch (err) {
    // 400 oder 422 oder 403
    showToast("error", `Fehler: ${err.code || 'Ungültige Eingabe'}`, err.nachricht, err.status);
  }
}

// Raum sperren / entsperren (EC-10)
async function toggleRoomLock(roomId, currentStatus) {
  const isCurrentlyActive = currentStatus === "AKTIV";
  if (isCurrentlyActive) {
    openLockRoomModal(roomId);
  } else {
    try {
      await window.api.entsperreRaum(roomId);
      showToast("success", "Raum entsperrt", "Der Raum ist ab sofort wieder für Buchungen verfügbar.", 200);
      loadRooms();
      loadBookings();
    } catch (err) {
      showToast("error", "Entsperren fehlgeschlagen", err.nachricht, err.status);
    }
  }
}

async function openLockRoomModal(roomId) {
  try {
    const room = await window.api.getRaum(roomId);
    document.getElementById("lock-room-id").value = room.id;
    const desc = document.getElementById("lock-room-desc");
    if (desc) {
      desc.innerHTML = `Raum <strong>'${escapeHtml(room.name)}'</strong> für Buchungen sperren. <strong>Alle kollidierenden Buchungen im Sperrzeitraum werden automatisch storniert.</strong>`;
    }
    document.getElementById("lock-start-time").value = "";
    document.getElementById("lock-end-time").value = "";
    openModal("modal-lock-room");
  } catch (err) {
    showToast("error", "Fehler", err.nachricht, err.status);
  }
}

async function submitQuickImmediateLock() {
  const roomId = document.getElementById("lock-room-id").value;
  try {
    await window.api.sperreRaum(roomId);
    closeModal("modal-lock-room");
    showToast("warning", "Raum gesperrt", "Der Raum wurde sofort gesperrt. Alle kollidierenden Buchungen wurden automatisch storniert.", 200);
    loadRooms();
    loadBookings();
  } catch (err) {
    showToast("error", "Sperren fehlgeschlagen", err.nachricht, err.status);
  }
}

async function handleLockRoomSubmit(e) {
  e.preventDefault();
  const roomId = document.getElementById("lock-room-id").value;
  const startInput = document.getElementById("lock-start-time").value;
  const endInput = document.getElementById("lock-end-time").value;

  const von = startInput ? new Date(startInput).toISOString() : null;
  const bis = endInput ? new Date(endInput).toISOString() : null;

  try {
    await window.api.sperreRaum(roomId, von, bis);
    closeModal("modal-lock-room");
    showToast("warning", "Raum gesperrt", "Der Raum wurde für den gewählten Zeitraum gesperrt. Kollidierende Buchungen wurden storniert.", 200);
    loadRooms();
    loadBookings();
  } catch (err) {
    showToast("error", "Sperren fehlgeschlagen", err.nachricht, err.status);
  }
}

// Global verfügbar machen
window.openLockRoomModal = openLockRoomModal;
window.submitQuickImmediateLock = submitQuickImmediateLock;
window.handleLockRoomSubmit = handleLockRoomSubmit;

// Raum bearbeiten (EC-9)
async function openEditRoomModal(roomId) {
  try {
    const room = await window.api.getRaum(roomId);
    document.getElementById("edit-room-id").value = room.id;
    document.getElementById("edit-room-name").value = room.name;
    document.getElementById("edit-room-capacity").value = room.kapazitaet;
    document.getElementById("edit-room-category").value = room.kategorie || "Seminarraum";
    document.getElementById("edit-room-building").value = room.gebaeude || "Haus A";
    document.getElementById("edit-room-equipment").value = (room.ausstattung || []).join(", ");
    openModal("modal-edit-room");
  } catch (err) {
    showToast("error", "Fehler", err.nachricht, err.status);
  }
}

async function handleEditRoomSubmit(e) {
  e.preventDefault();
  const roomId = document.getElementById("edit-room-id").value;
  const name = document.getElementById("edit-room-name").value;
  const kapazitaetVal = document.getElementById("edit-room-capacity").value;
  const kapazitaet = kapazitaetVal !== "" ? Number(kapazitaetVal) : undefined;
  const kategorie = document.getElementById("edit-room-category").value;
  const gebaeude = document.getElementById("edit-room-building").value;
  const ausstattung = document.getElementById("edit-room-equipment").value
    .split(",").map(s => s.trim()).filter(Boolean);

  try {
    await window.api.updateRaum(roomId, { name, kapazitaet, kategorie, gebaeude, ausstattung });
    closeModal("modal-edit-room");
    showToast("success", "Raum aktualisiert", `Änderungen für '${name}' wurden übernommen.`, 200);
    loadRooms();
  } catch (err) {
    showToast("error", `Fehler: ${err.code || 'Ungültige Eingabe'}`, err.nachricht, err.status);
  }
}

// Raum löschen (DELETE /api/v1/raeume/{id})
async function confirmDeleteRoom(roomId, roomName) {
  if (!confirm(`Soll der Raum '${roomName}' wirklich gelöscht werden?\n\nHinweis: Alle zugehörigen Buchungen für diesen Raum werden automatisch storniert.`)) return;

  try {
    await window.api.deleteRaum(roomId);
    showToast("success", "Raum gelöscht", `'${roomName}' und zugehörige Buchungen wurden storniert bzw. entfernt.`, 204);
    loadRooms();
    loadBookings();
  } catch (err) {
    showToast("error", `Löschen fehlgeschlagen (${err.code})`, err.nachricht, err.status);
  }
}

// ==========================================
// 5. Epic 3: Buchungsprozess (EC-3)
// ==========================================

async function loadBookings() {
  const tableBody = document.getElementById("bookings-table-body");
  if (!tableBody) return;

  const statusFilter = document.getElementById("booking-filter-status")?.value;
  const params = {};
  if (statusFilter && statusFilter !== "ALLE") params.status = statusFilter;

  try {
    const bookings = await window.api.getBuchungen(params);
    const rooms = await window.api.getRaeume();
    const roomsMap = Object.fromEntries(rooms.map(r => [r.id, r.name]));

    renderBookings(bookings, roomsMap);
  } catch (err) {
    showToast("error", "Fehler beim Laden der Buchungen", err.nachricht, err.status);
  }
}

function renderBookings(bookings, roomsMap) {
  const tableBody = document.getElementById("bookings-table-body");
  if (!tableBody) return;

  if (bookings.length === 0) {
    tableBody.innerHTML = `
      <tr>
        <td colspan="6" style="text-align: center; padding: 2.5rem; color: var(--text-secondary);">
          Keine Buchungen für die gewählten Kriterien gefunden.
        </td>
      </tr>
    `;
    return;
  }

  tableBody.innerHTML = bookings.map(b => {
    const roomName = roomsMap[b.raumId] || b.raumId;
    const start = new Date(b.von);
    const end = new Date(b.bis);

    const dateStr = start.toLocaleDateString('de-DE', { day: '2-digit', month: '2-digit', year: 'numeric' });
    const timeStr = `${start.toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' })} - ${end.toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' })} Uhr`;

    const statusClass = b.status.toLowerCase();

    return `
      <tr>
        <td style="font-weight: 700;">${escapeHtml(roomName)}</td>
        <td>
          <div style="font-weight: 600;">${dateStr}</div>
          <div style="font-size: 0.8rem; color: var(--text-muted);">${timeStr}</div>
        </td>
        <td>${escapeHtml(b.zweck || 'Veranstaltung')}</td>
        <td style="font-family: var(--font-mono); font-size: 0.82rem; color: var(--text-secondary);">${escapeHtml(b.nutzerId)}</td>
        <td>
          <span class="booking-status ${statusClass}">${b.status}</span>
        </td>
        <td style="text-align: right;">
          ${b.status !== 'STORNIERT' ? `
            <button class="btn btn-secondary btn-sm" onclick="openEditBookingModal('${b.id}', '${b.von}', '${b.bis}')" style="margin-right: 0.4rem;">
              Ändern
            </button>
            <button class="btn btn-danger btn-sm" onclick="cancelBooking('${b.id}')">
              Stornieren
            </button>
          ` : `
            <span style="font-size: 0.8rem; color: var(--text-muted);">Inaktiv</span>
          `}
        </td>
      </tr>
    `;
  }).join("");
}

// Buchungsmodal vorbereiten
async function openBookingModalForRoom(roomId = null, roomName = null) {
  const select = document.getElementById("booking-room-select");
  if (select) {
    const rooms = await window.api.getRaeume({ status: "AKTIV" });
    select.innerHTML = rooms.map(r => `
      <option value="${r.id}" ${r.id === roomId ? 'selected' : ''}>
        ${escapeHtml(r.name)} (${r.kapazitaet} Plätze, ${escapeHtml(r.gebaeude)})
      </option>
    `).join("");
  }

  // Standard-Zeiten (nächste volle Stunde)
  const now = new Date();
  now.setMinutes(0, 0, 0);
  now.setHours(now.getHours() + 1);

  const startIso = toLocalDatetimeString(now);
  now.setHours(now.getHours() + 2);
  const endIso = toLocalDatetimeString(now);

  document.getElementById("booking-start-time").value = startIso;
  document.getElementById("booking-end-time").value = endIso;

  openModal("modal-create-booking");
}

async function handleCreateBookingSubmit(e) {
  e.preventDefault();
  const raumId = document.getElementById("booking-room-select").value;
  const von = document.getElementById("booking-start-time").value;
  const bis = document.getElementById("booking-end-time").value;
  const zweck = document.getElementById("booking-purpose").value;

  const payload = {
    raumId,
    von: new Date(von).toISOString(),
    bis: new Date(bis).toISOString(),
    zweck
  };

  try {
    const booking = await window.api.createBuchung(payload);
    closeModal("modal-create-booking");
    document.getElementById("form-create-booking").reset();
    showToast("success", "Buchung bestätigt!", `Buchung für Raum erfolgreich reserviert.`, 201);
    loadBookings();
  } catch (err) {
    // Z.B. 409 Doppelbuchung oder 422
    showToast("error", `Buchungsfehler (${err.code})`, err.nachricht, err.status);
  }
}

async function cancelBooking(bookingId) {
  if (!confirm("Möchtest du diese Buchung wirklich stornieren?")) return;
  try {
    await window.api.cancelBuchung(bookingId);
    showToast("success", "Buchung storniert", "Der Raum ist im betreffenden Zeitraum wieder freigegeben.", 204);
    loadBookings();
  } catch (err) {
    showToast("error", "Stornierung fehlgeschlagen", err.nachricht, err.status);
  }
}

// Buchung ändern (Zeitraum verschieben, PATCH /buchungen/{buchungId})
function openEditBookingModal(bookingId, von, bis) {
  document.getElementById("edit-booking-id").value = bookingId;
  document.getElementById("edit-booking-start-time").value = toLocalDatetimeString(new Date(von));
  document.getElementById("edit-booking-end-time").value = toLocalDatetimeString(new Date(bis));
  openModal("modal-edit-booking");
}

async function handleUpdateBookingSubmit(e) {
  e.preventDefault();
  const bookingId = document.getElementById("edit-booking-id").value;
  const von = document.getElementById("edit-booking-start-time").value;
  const bis = document.getElementById("edit-booking-end-time").value;

  const payload = {
    von: new Date(von).toISOString(),
    bis: new Date(bis).toISOString()
  };

  try {
    await window.api.updateBuchung(bookingId, payload);
    closeModal("modal-edit-booking");
    showToast("success", "Buchung geändert", "Der neue Zeitraum wurde gespeichert.", 200);
    loadBookings();
  } catch (err) {
    // Z.B. 409 Konflikt oder 403 (nicht die eigene Buchung)
    showToast("error", `Änderung fehlgeschlagen (${err.code})`, err.nachricht, err.status);
  }
}

// ==========================================
// 6. Epic 4: Rollen & Zugangskontrolle (EC-4)
// ==========================================

async function loadUsersAndRoles() {
  const container = document.getElementById("roles-users-list");
  if (!container) return;

  const db = getMockStorage();
  const users = db.users;

  container.innerHTML = users.map(user => {
    const currentRoles = (user.rollen || []).map(r => r.name);
    const isAdmin = currentRoles.includes("ADMIN");
    const isDocent = currentRoles.includes("DOZENT");
    const isStudent = currentRoles.includes("STUDENT");

    return `
      <div style="background: var(--bg-card); border: 1px solid var(--border-light); border-radius: var(--radius-md); padding: 1.25rem; display: flex; align-items: center; justify-content: space-between; gap: 1rem; flex-wrap: wrap;">
        <div>
          <div style="font-weight: 800; font-size: 1.05rem;">${escapeHtml(user.name)}</div>
          <div style="font-size: 0.85rem; color: var(--text-muted); font-family: var(--font-mono);">Uni-Kennung: ${escapeHtml(user.uniKennung)}</div>
          <div style="display: flex; gap: 0.35rem; margin-top: 0.5rem;">
            ${(user.rollen || []).map(r => `
              <span class="tag-badge" style="background: var(--neon-cyan-bg); color: var(--neon-cyan-dark); font-weight: 700;">
                ${r.name}
              </span>
            `).join("")}
          </div>
        </div>

        <div style="display: flex; align-items: center; gap: 0.5rem;">
          <button class="btn btn-secondary btn-sm" onclick="quickSwitchUser('${user.id}')">
            Als dieser Nutzer agieren
          </button>
          <button class="btn btn-secondary btn-sm" onclick="openEditRolesModal('${user.id}')">
            ⚙️ Rollen bearbeiten
          </button>
        </div>
      </div>
    `;
  }).join("");
}

// Uni-Login Submit (EC-14 / /api/v1/auth/login)
async function handleLoginSubmit(e) {
  e.preventDefault();
  const uniKennung = document.getElementById("login-username").value;
  const credential = document.getElementById("login-password").value;

  try {
    const resp = await window.api.login(uniKennung, credential);
    closeModal("modal-login");
    updateUserDisplay();
    showToast("success", "Login erfolgreich!", `Angemeldet als ${resp.user?.name}. JWT Token empfangen.`, 200);
    loadRooms();
    loadBookings();
  } catch (err) {
    showToast("error", "Login fehlgeschlagen (401)", err.nachricht, err.status);
  }
}

async function openEditRolesModal(userId) {
  const db = getMockStorage();
  const u = db.users.find(x => x.id === userId);
  if (!u) return;

  document.getElementById("edit-roles-user-id").value = u.id;
  document.getElementById("edit-roles-user-name").textContent = u.name;

  const currentNames = (u.rollen || []).map(r => r.name);
  document.getElementById("role-check-admin").checked = currentNames.includes("ADMIN");
  document.getElementById("role-check-dozent").checked = currentNames.includes("DOZENT");
  document.getElementById("role-check-student").checked = currentNames.includes("STUDENT");

  openModal("modal-edit-roles");
}

async function handleEditRolesSubmit(e) {
  e.preventDefault();
  const userId = document.getElementById("edit-roles-user-id").value;
  const roles = [];
  if (document.getElementById("role-check-admin").checked) roles.push("ADMIN");
  if (document.getElementById("role-check-dozent").checked) roles.push("DOZENT");
  if (document.getElementById("role-check-student").checked) roles.push("STUDENT");

  if (roles.length === 0) {
    alert("Ein Nutzer muss mindestens eine Rolle besitzen.");
    return;
  }

  try {
    await window.api.updateNutzerRollen(userId, roles);
    closeModal("modal-edit-roles");
    showToast("success", "Rollen aktualisiert", `Berechtigungen für den Nutzer wurden erfolgreich gespeichert.`, 200);
    loadUsersAndRoles();
    updateUserDisplay();
  } catch (err) {
    showToast("error", "Rechtevergabe blockiert (403)", err.nachricht, err.status);
  }
}

// ==========================================
// 7. Modale Dialog-Steuerung
// ==========================================

function initModals() {
  document.querySelectorAll(".modal-backdrop").forEach(backdrop => {
    backdrop.addEventListener("click", (e) => {
      if (e.target === backdrop) closeModal(backdrop.id);
    });
  });

  // Formular-Listener
  document.getElementById("form-gate-login")?.addEventListener("submit", handleGateLogin);
  document.getElementById("form-create-room")?.addEventListener("submit", handleCreateRoomSubmit);
  document.getElementById("form-edit-room")?.addEventListener("submit", handleEditRoomSubmit);
  document.getElementById("form-lock-room")?.addEventListener("submit", handleLockRoomSubmit);
  document.getElementById("form-create-booking")?.addEventListener("submit", handleCreateBookingSubmit);
  document.getElementById("form-edit-booking")?.addEventListener("submit", handleUpdateBookingSubmit);
  document.getElementById("form-login")?.addEventListener("submit", handleLoginSubmit);
  document.getElementById("form-edit-roles")?.addEventListener("submit", handleEditRolesSubmit);
  document.getElementById("booking-filter-status")?.addEventListener("change", () => loadBookings());
}

function openModal(modalId) {
  const el = document.getElementById(modalId);
  if (el) el.classList.add("active");
}

function closeModal(modalId) {
  const el = document.getElementById(modalId);
  if (el) el.classList.remove("active");
}

// ==========================================
// 8. Live-Demo Schnellwahl & Präsentations-Helfer
// ==========================================

function initDemoActions() {
  // Positivfall-Trigger
  document.getElementById("demo-trigger-pos")?.addEventListener("click", () => {
    runPositiveDemo();
  });

  // Negativfall 1 (Ungültige Eingabe 422/400)
  document.getElementById("demo-trigger-neg1")?.addEventListener("click", () => {
    runNegativeInputDemo();
  });

  // Negativfall 2 (Doppelbuchung 409)
  document.getElementById("demo-trigger-neg2")?.addEventListener("click", () => {
    runNegativeConflictDemo();
  });

  // Negativfall 3 (403 Forbidden)
  document.getElementById("demo-trigger-neg3")?.addEventListener("click", () => {
    runNegativeForbiddenDemo();
  });
}

// Positivfall: Admin legt neuen Raum fehlerfrei an
async function runPositiveDemo() {
  switchView("view-rooms");
  // Als Admin sicherstellen
  quickSwitchUser("usr-admin-1");

  document.getElementById("create-room-name").value = "Audimax Erle C01";
  document.getElementById("create-room-capacity").value = "150";
  document.getElementById("create-room-category").value = "Hörsaal";
  document.getElementById("create-room-building").value = "Haus C";
  document.getElementById("create-room-equipment").value = "Beamer, Mikrofon, Smartboard, Barrierefrei";

  openModal("modal-create-room");
  showToast("warning", "Demo-Hinweis: Positivfall", "Formular wurde mit validen Daten befüllt. Klicke auf 'Raum speichern'!", null);
}

// Negativfall 1: Validierungsfehler Kapazität <= 0 (EC-8 & OpenAPI 422)
async function runNegativeInputDemo() {
  switchView("view-rooms");
  quickSwitchUser("usr-admin-1");

  document.getElementById("create-room-name").value = "Kleiner Gruppenraum X";
  document.getElementById("create-room-capacity").value = "-5"; // UNGÜLTIG!
  document.getElementById("create-room-category").value = "Seminarraum";
  document.getElementById("create-room-building").value = "Haus A";
  document.getElementById("create-room-equipment").value = "Whiteboard";

  openModal("modal-create-room");
  showToast("warning", "Demo-Hinweis: Negativfall 1", "Kapazität ist auf '-5' gesetzt (Invariante verletzt). Klicke auf 'Raum speichern' für die 422-Reaktion.", null);
}

// Negativfall 2: Doppelbuchung (EC-12 & OpenAPI 409)
async function runNegativeConflictDemo() {
  switchView("view-bookings");

  const db = getMockStorage();
  const existingBooking = db.bookings[0]; // z. B. Hörsaal Erle heute
  if (!existingBooking) return;

  await openBookingModalForRoom(existingBooking.raumId);

  // Exakt denselben Zeitraum wählen
  const start = new Date(existingBooking.von);
  const end = new Date(existingBooking.bis);

  document.getElementById("booking-start-time").value = toLocalDatetimeString(start);
  document.getElementById("booking-end-time").value = toLocalDatetimeString(end);
  document.getElementById("booking-purpose").value = "Konflikt-Buchungsversuch (Präsentation)";

  showToast("warning", "Demo-Hinweis: Negativfall 2", "Zeitfenster überschneidet sich exakt mit einer bestehenden Buchung. Klicke auf 'Jetzt buchen' für den 409-Konflikt.", null);
}

// Negativfall 3: Fehlende Berechtigung (EC-16 & OpenAPI 403)
async function runNegativeForbiddenDemo() {
  switchView("view-rooms");
  // Zu Studentin Anna wechseln
  quickSwitchUser("usr-student-1");

  showToast("warning", "Demo-Hinweis: Negativfall 3", "Rolle wurde auf 'Student' gesetzt. Ein Klick auf Admin-Funktionen oder Raum-Löschen wird mit 403 blockiert.", null);

  setTimeout(async () => {
    try {
      // Versuch einen Raum zu löschen
      await window.api.deleteRaum("room-101");
    } catch (err) {
      showToast("error", "Zugriff verweigert (403)", err.nachricht, err.status);
    }
  }, 1000);
}

function triggerForbiddenDemo() {
  showToast("error", "Zugriff verweigert (403)", "Nur Benutzer mit der Rolle 'ADMIN' dürfen Räume verwalten oder sperren.", 403);
}

// ==========================================
// 9. Hilfsfunktionen & Live-/Demo-Umschaltung
// ==========================================

async function checkApiStatus() {
  const badge = document.getElementById("mode-badge");
  const demoBar = document.getElementById("demo-bar");
  const demoTab = document.getElementById("nav-tab-demo");
  const isAvailable = await window.api.checkBackendAvailability();

  if (isAvailable) {
    window.api.setMode("live");
    // Im Live-Modus: Alle Demo-Anzeigen automatisch komplett ausblenden!
    if (badge) badge.classList.add("hidden");
    if (demoBar) demoBar.classList.add("hidden");
    if (demoTab) demoTab.classList.add("hidden");
  } else {
    window.api.setMode("mock");
    // Im Mock-/Demo-Modus: Hilfsleiste und Badge für Vorbereitung sichtbar
    if (badge) {
      badge.classList.remove("hidden");
      badge.className = "mode-badge";
      badge.innerHTML = `<span class="mode-dot"></span> Demo-Modus`;
    }
    if (demoBar) demoBar.classList.remove("hidden");
    if (demoTab) demoTab.classList.remove("hidden");
  }
}

// Manueller Umschalter (z. B. mit Tastenkombination Strg+D)
function toggleDemoHelpers() {
  const demoBar = document.getElementById("demo-bar");
  const demoTab = document.getElementById("nav-tab-demo");
  const badge = document.getElementById("mode-badge");

  if (demoBar) demoBar.classList.toggle("hidden");
  if (demoTab) demoTab.classList.toggle("hidden");
  if (badge) badge.classList.toggle("hidden");

  const isVisible = demoBar && !demoBar.classList.contains("hidden");
  showToast("warning", isVisible ? "Demo-Helfer eingeblendet" : "Demo-Helfer ausgeblendet", "Umschalten jederzeit mit Tastenkombination Strg+D möglich.", null);
}

// Tastenkombination Strg+D (oder Cmd+D) registrieren
document.addEventListener("keydown", (e) => {
  if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === "d") {
    e.preventDefault();
    toggleDemoHelpers();
  }
});


function toLocalDatetimeString(date) {
  const pad = n => String(n).padStart(2, '0');
  const yyyy = date.getFullYear();
  const mm = pad(date.getMonth() + 1);
  const dd = pad(date.getDate());
  const hh = pad(date.getHours());
  const min = pad(date.getMinutes());
  return `${yyyy}-${mm}-${dd}T${hh}:${min}`;
}

function escapeHtml(str) {
  if (!str) return "";
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}
