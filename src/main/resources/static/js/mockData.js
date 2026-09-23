/**
 * CampusFlow - Mock-Daten & Vorkonfiguration
 * Entspricht 1:1 den DTOs und Schemas aus campusflow_openapi.md
 */

const INITIAL_MOCK_DATA = {
  // Benutzer und Rollen (EC-4)
  users: [
    {
      id: "usr-admin-1",
      name: "Lukas (Admin)",
      uniKennung: "admin",
      password: "admin",
      rollen: [
        {
          name: "ADMIN",
          berechtigungen: ["CREATE", "READ", "UPDATE", "DELETE", "BOOK", "MANAGE_ROLES"]
        }
      ]
    },
    {
      id: "usr-docent-1",
      name: "Prof. Dr. Schneider (Dozent)",
      uniKennung: "dozent",
      password: "pass",
      rollen: [
        {
          name: "DOZENT",
          berechtigungen: ["READ", "BOOK", "UPDATE_OWN_BOOKING"]
        }
      ]
    },
    {
      id: "usr-student-1",
      name: "Anna Müller (Studentin)",
      uniKennung: "student",
      password: "pass",
      rollen: [
        {
          name: "STUDENT",
          berechtigungen: ["READ", "BOOK"]
        }
      ]
    }
  ],

  // Räume (EC-2)
  rooms: [
    {
      id: "room-101",
      name: "Hörsaal Erle (A101)",
      kapazitaet: 120,
      ausstattung: ["Beamer", "Mikrofon", "Klimaanlage", "Barrierefrei"],
      kategorie: "Hörsaal",
      gebaeude: "Haus A",
      status: "AKTIV"
    },
    {
      id: "room-102",
      name: "Seminarraum B204",
      kapazitaet: 25,
      ausstattung: ["Beamer", "Whiteboard"],
      kategorie: "Seminarraum",
      gebaeude: "Haus B",
      status: "AKTIV"
    },
    {
      id: "room-103",
      name: "IT-Labor & PC-Pool C01",
      kapazitaet: 30,
      ausstattung: ["PCs", "Beamer", "Klimaanlage", "LAN"],
      kategorie: "Labor",
      gebaeude: "Haus C",
      status: "AKTIV"
    },
    {
      id: "room-104",
      name: "Besprechungsraum A012",
      kapazitaet: 12,
      ausstattung: ["Whiteboard", "Videokonferenz-System"],
      kategorie: "Besprechung",
      gebaeude: "Haus A",
      status: "GESPERRT"
    },
    {
      id: "room-105",
      name: "Projektraum B105",
      kapazitaet: 16,
      ausstattung: ["Whiteboard", "Smartboard", "Barrierefrei"],
      kategorie: "Projektraum",
      gebaeude: "Haus B",
      status: "AKTIV"
    }
  ],

  // Buchungen (EC-3)
  bookings: [
    {
      id: "book-1",
      raumId: "room-101",
      nutzerId: "usr-docent-1",
      zweck: "Vorlesung: Software Engineering II",
      von: new Date(Date.now() + 3600000 * 2).toISOString(), // in 2 Stunden
      bis: new Date(Date.now() + 3600000 * 4).toISOString(), // in 4 Stunden
      status: "BESTAETIGT"
    },
    {
      id: "book-2",
      raumId: "room-102",
      nutzerId: "usr-student-1",
      zweck: "Lerngruppe Algorithmen",
      von: new Date(Date.now() + 3600000 * 24).toISOString(), // morgen
      bis: new Date(Date.now() + 3600000 * 26).toISOString(),
      status: "BESTAETIGT"
    },
    {
      id: "book-3",
      raumId: "room-103",
      nutzerId: "usr-admin-1",
      zweck: "Software-Update und Wartung PC-Pool",
      von: new Date(Date.now() + 3600000 * 48).toISOString(),
      bis: new Date(Date.now() + 3600000 * 52).toISOString(),
      status: "GEPLANT"
    }
  ]
};

// Im localStorage ablegen für konsistente Demo-Bearbeitung
function getMockStorage() {
  const stored = localStorage.getItem("campusflow_mock_data");
  if (!stored) {
    localStorage.setItem("campusflow_mock_data", JSON.stringify(INITIAL_MOCK_DATA));
    return JSON.parse(JSON.stringify(INITIAL_MOCK_DATA));
  }
  try {
    return JSON.parse(stored);
  } catch (e) {
    localStorage.setItem("campusflow_mock_data", JSON.stringify(INITIAL_MOCK_DATA));
    return JSON.parse(JSON.stringify(INITIAL_MOCK_DATA));
  }
}

function saveMockStorage(data) {
  localStorage.setItem("campusflow_mock_data", JSON.stringify(data));
}

function resetMockStorage() {
  localStorage.setItem("campusflow_mock_data", JSON.stringify(INITIAL_MOCK_DATA));
  return JSON.parse(JSON.stringify(INITIAL_MOCK_DATA));
}
