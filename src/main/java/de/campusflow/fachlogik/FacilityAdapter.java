package de.campusflow.fachlogik;

import de.campusflow.model.Room;

public interface FacilityAdapter {

    public void raumErstellt(Room room);
    public void raumAktualisiert(Room room);
    public void raumGeloescht(Long roomId);
}
